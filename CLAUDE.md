# CLAUDE.md

## Project Overview

AI-powered CLI that scrapes job listings, filters them against a user profile, tailors a LaTeX resume for each match, and emails results. Built with Java 21 / Maven. Key libraries: picocli (CLI args), JLine3 (interactive menu), Jsoup + Playwright (scraping), Anthropic Java SDK (Claude API), Jakarta Mail (email), Typesafe Config (HOCON config), dotenv-java (env vars), Apache POI (.docx parsing).

## Architecture

```
src/main/java/com/jobhunter/
  ai/           - ClaudeService (all Claude API calls, semaphore-limited concurrency + retry)
  cli/          - Entry point (Main.java), interactive menu (InteractiveMenu), Console, Spinner
  cli/options/  - MenuItem (abstract), HuntCommand, ViewProfileCommand, MenuItemFactory
  db/           - JobRepository (SQLite dedup — currently commented out, will be re-enabled)
  email/        - EmailService (Gmail SMTP, HTML report generation)
  exception/    - Exception hierarchy: JobHunterException (abstract) → domain subclasses
  job/          - Job POJO, HuntPipeline (pipeline orchestrator), JobFilter, JobScraper, JobTailor
  job/source/   - JobSource (abstract), JobSourceFactory (config-driven), SimplifyJobSource
  profile/      - Profile, ProfileBuilder (resume + GitHub, cached to profile-cache.json)
  profile/github/ - GitHubFetcher, GitHubProfile, GitHubRepo
  profile/resume/ - ResumeParser (.tex and .docx support)
  scraper/      - PageFetcher (Jsoup→Playwright fallback), BrowserPool (5 Chromium instances), FetchResult
  utils/        - Utils (LaTeX splitting, file type detection), LatexPair

src/main/resources/application.conf  - HOCON config: prompts, AI model names, token limits, concurrency, github config
.env                                  - Secrets (ANTHROPIC_API_KEY, EMAIL_*, RESUME_PATH, TARGET_DIR) — never commit
```

## Development Commands

```bash
# Build
mvn clean package           # full build → target/ai-job-hunter-1.0-SNAPSHOT.jar
mvn compile                 # compile only (no jar)

# Run locally (after build)
java -jar target/ai-job-hunter-1.0-SNAPSHOT.jar
java -jar target/ai-job-hunter-1.0-SNAPSHOT.jar --help
java -jar target/ai-job-hunter-1.0-SNAPSHOT.jar -v   # verbose (full stack traces)

# Tests
mvn test                    # run unit tests
mvn test jacoco:report      # run tests + generate coverage report at target/site/jacoco/

# Code quality
mvn formatter:validate      # check Google-style formatting (runs in CI)
mvn formatter:format        # auto-format all Java sources
mvn spotbugs:check          # static analysis (threshold: High)

# CI mirrors these exactly — format validate, package, spotbugs, test+jacoco all run on every PR
```

## Key Conventions

- **Static config access**: `Main.dotenv` and `Main.config` are global statics — no DI framework. Access them directly anywhere.
- **Virtual threads for concurrency**: Use `Executors.newVirtualThreadPerTaskExecutor()` for all parallel work (scraping, filtering, tailoring, profile building). Do not use traditional thread pools.
- **Claude API calls go through ClaudeService only**: Never instantiate `AnthropicOkHttpClient` outside of `ClaudeService`. Add new operations as methods there.
- **New job sources**: Extend `JobSource` (abstract class), add a new type string to `JobSourceFactory`'s switch, and add the source block to `application.conf`.
- **New menu items**: Extend `MenuItem`, register in `MenuItemFactory`.
- **Prompts live in application.conf**: All Claude prompt templates use `{{PLACEHOLDER}}` substitution and are defined under `prompts.*` in config — not hardcoded in Java.
- **Exception handling**: Throw domain-specific subclasses of `JobHunterException` (unchecked). Let `Main` / `Console` handle printing. Never swallow exceptions silently.
- **Formatting is enforced**: `formatter:format` runs at `process-sources` phase automatically during `mvn package`. Run `mvn formatter:format` before committing to avoid CI failures.
- **SpotBugs exclusions**: Suppressions are in `.github/spotbugs-exclude.xml` — add entries there if a finding is a false positive.

## Important Context

- **No tests exist yet** — `src/test/` does not exist. JUnit 5 is in the pom. When adding tests, create `src/test/java/com/jobhunter/` and mirror the main package structure.
- **JobRepository is commented out intentionally** — SQLite dedup tracking is scaffolded but disabled. The `db.path` config and `sqlite-jdbc` dependency are already in place; do not remove them.
- **BrowserPool is a shared resource** — 5 Playwright Chromium instances. Do not create `Playwright` instances outside `BrowserPool`; always borrow/return via the pool.
- **Workday special case** in `PageFetcher`: Workday job pages always use Playwright with a specific CSS selector wait. If adding new site-specific scraping logic, follow the same pattern.
- **Profile cache** (`profile-cache.json`) and the SQLite DB (`*.db`) are gitignored — don't commit them.
- **Per-task model selection**: Each `ClaudeService` method should use the model best suited to its task. Current guidance:
  - Resume tailoring (`tailorResumeTex`) — Sonnet or Opus: high-stakes, complex generation
  - Job filtering (`filterJob`) — Haiku: simple structured JSON judgment, high volume
  - Job description extraction (`extractJobDescription`) — Haiku: fast, lightweight extraction
  - Resume parsing (`parseResumeTexOrDocx`) — Haiku: straightforward text extraction
  - README summarization (`summarizeReadMe`) — Haiku: low complexity, high volume
  - When adding new operations, choose based on complexity and cost sensitivity; document the choice here. Models are configured in `ai.models` in `application.conf` — prefer named config keys over hardcoded model strings.

## Planned Features (Roadmap)

- **Hunt Daemon**: Background process (separate JVM, not in-process) that runs `hunt` on the cron schedule in `scheduler.cron` using **Quartz**. Will re-enable `JobRepository` (SQLite) to skip already-seen job URLs across runs. Managed entirely through the interactive menu — no CLI flags. Menu items to add: `Start Daemon`, `Stop Daemon`, `Enable Auto-start on Login`, `Disable Auto-start on Login`, and `Daemon Status`. Implementation notes:
  - Daemon process writes a PID file to `~/.jobhunter/daemon.pid`; menu reads it to check running state
  - Auto-start on macOS via a launchd plist written to `~/Library/LaunchAgents/com.aijobhunter.daemon.plist`; enabled/disabled with `launchctl load/unload`
  - The plist needs the absolute JAR path — store it in `application.conf` or auto-detect via `ProcessHandle.current()` at first run
  - Daemon does not run while the machine is asleep (JVM is suspended); this is acceptable behavior
  - New `MenuItem` subclasses per action, registered in `MenuItemFactory`
- **Single Job Posting Mode**: User provides a URL to a specific job posting → scrape it → run through filter pipeline → display match score → optionally tailor resume and save to `TARGET_DIR`.
- **Pre-packaged Distribution**: Ship a pre-built JAR via GitHub Releases so users can download and run without building from source. After configuring `.env` and `application.conf`, `jobhunter` in the terminal should just work.
