# CLAUDE.md

## Project Overview
AI-powered Java 21 CLI that scrapes job listings (GitHub-hosted boards via Playwright/jsoup),
filters them against a user profile (GitHub repos + parsed resume), tailors LaTeX/docx resumes
using Claude, and emails match reports. Built with Maven, picocli, Anthropic Java SDK, SQLite,
Quartz, and JLine.

## Architecture

```
src/main/java/com/jobhunter/
├── cli/              — Main entrypoint, InteractiveMenu, Console, Spinner
│   └── options/      — MenuItem (abstract base), MenuItemFactory, HuntCommand,
│                       HuntOneCommand, ViewProfileCommand
├── job/              — HuntPipeline (orchestrator), JobScraper, JobFilter, JobTailor,
│                       JobRunner, Job, JobScraperResult
│   └── source/       — JobSource interface, JobSourceFactory, SimplifyJobSource
├── ai/               — ClaudeService, ExtractionResult, FilterResult, JobMetaResult
├── profile/          — Profile, ProfileBuilder, ResumeParser
│   └── github/       — GitHubFetcher, GitHubProfile, GitHubRepo
├── scraper/          — BrowserPool, PageFetcher, FetchResult, FetchStatus
├── db/               — JobRepository (SQLite dedup)
├── email/            — EmailService
├── exception/        — JobHunterException (abstract base) + typed subclasses
└── utils/            — Utils, LatexPair
```

Config lives in two places:
- `.env` — secrets (API keys, email credentials, file paths)
- `src/main/resources/application.conf` — AI prompts, model names, GitHub repos, scheduler
  cron, DB path, profile cache path, job sources

## Development Commands
```bash
mvn clean package          # build fat JAR → target/ai-job-hunter-1.0-SNAPSHOT.jar
mvn test                   # run JUnit Jupiter tests
mvn formatter:validate     # check Google Java Style formatting
mvn formatter:format       # auto-format all sources
mvn spotbugs:check         # static analysis (threshold: High)

# Run the app
java -jar target/ai-job-hunter-1.0-SNAPSHOT.jar          # interactive menu
java -jar target/ai-job-hunter-1.0-SNAPSHOT.jar -v       # verbose (full stack traces)
java -jar target/ai-job-hunter-1.0-SNAPSHOT.jar --help
```

## Key Conventions

### Error handling
- All exceptions extend `JobHunterException` (abstract RuntimeException in `exception/`)
- Typed subclasses: `AiServiceException`, `ConfigurationException`, `ProfileBuildException`,
  `ResumeNotFoundException`, `ScrapingException` — add new exception types here, never throw
  raw `RuntimeException` or `Exception`
- Use `Console.error(msg, e)` for user-facing errors; stack traces only print in verbose mode

### Output / UI
- All terminal output goes through `Console` (static methods: `status`, `error`, `header`,
  `footer`, `blank`, `println`) — never use `System.out.println` directly
- Use `Spinner` for any blocking operation with a progress indicator

### AI prompts
- All Claude prompts live in `application.conf` under `jobhunter.prompts` — do not hardcode
  prompt strings in Java
- Two model tiers: `fast` (claude-haiku-4-5) for extraction/filtering, `strong`
  (claude-sonnet-4-6) for resume tailoring
- All Claude calls go through `ClaudeService`; result types are typed records in `ai/`

### Job sources
- Implement `JobSource` interface to add new boards; register in `JobSourceFactory`
- Current source: `SimplifyJobSource` (scrapes SimplifyJobs GitHub repo)

### Code style
- Google Java Style enforced by `formatter-maven-plugin` with `config/eclipse-java-google-style.xml`
- Run `mvn formatter:format` before committing; CI validates with `mvn formatter:validate`

## Important Context
- Resume tailoring is async with retry and timeout
- Claude API calls use Guava `RateLimiter` to avoid hitting rate limits
- Job dedup is tracked in SQLite (`job-hunter.db`) via `JobRepository` — jobs already seen
  are skipped on subsequent runs
- Profile is cached to `profile-cache.json` after first GitHub fetch to avoid redundant API calls
- Resume supports both `.tex` (LaTeX) and `.docx` formats; `ResumeParser` handles both
- `HuntPipeline` runs the full automated flow; `HuntOneCommand` handles single-URL ad-hoc hunts
- `Main.validate()` runs at startup and fails fast if `.env` is misconfigured
