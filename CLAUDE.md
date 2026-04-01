# CLAUDE.md

## Project Overview
AI Job Hunter is a Java 21 CLI application that automates job searching end-to-end: it scrapes listings from Simplify Jobs, scores each against the user's resume and GitHub profile using Claude AI, tailors LaTeX or .docx resumes for matches, and sends an HTML email report. Built with Maven, Picocli, Playwright, jsoup, and the Anthropic Java SDK.

## Architecture
```
src/main/java/com/jobhunter/
├── cli/              — Entry point (Main.java), interactive menu, Spinner, Console logging
├── cli/options/      — HuntCommand and ViewProfileCommand implementations
├── ai/               — ClaudeService: API client, semaphore rate limiting, retry logic
├── job/              — Core pipeline: JobRunner, JobScraper, JobFilter, JobTailor
├── job/source/       — JobSource interface + SimplifyJobSource implementation
├── profile/          — ProfileBuilder (caches to profile-cache.json), Profile model
├── profile/github/   — GitHubFetcher (fetches repos, summarizes READMEs via Claude)
├── profile/resume/   — ResumeParser (.tex and .docx support via Claude extraction)
├── scraper/          — PageFetcher (jsoup primary, Playwright fallback), BrowserPool
├── email/            — EmailService (Gmail SMTP with HTML report)
├── db/               — JobRepository (SQLite dedup — currently commented out)
├── exception/        — Custom exception hierarchy rooted at JobHunterException
└── utils/            — LatexPair (splits LaTeX preamble/body), Utils
src/main/resources/application.conf  — HOCON config: prompts, AI models, job sources, scheduler
```

## Pipeline Flow
```
Main → InteractiveMenu → HuntCommand → JobRunner.runAll()
  ├─ JobScraper.scrape()          — fetch listings, extract descriptions (virtual threads)
  ├─ JobFilter.filter()           — Claude scores each job; ≥ 60 = match (virtual threads)
  ├─ JobTailor.tailor()           — Claude tailors resume per match (virtual threads)
  └─ EmailService.sendJobReport() — Gmail SMTP HTML report
```

## Development Commands
```bash
mvn clean package          # build fat JAR → target/ai-job-hunter-1.0-SNAPSHOT.jar
mvn test                   # run JUnit tests (JaCoCo coverage generated)
mvn formatter:validate     # check Google Java Style compliance (CI gate)
mvn formatter:format       # auto-format to Google Java Style
mvn spotbugs:check         # static analysis

java -jar target/ai-job-hunter-1.0-SNAPSHOT.jar       # run interactive menu
java -jar target/ai-job-hunter-1.0-SNAPSHOT.jar -v    # verbose mode (full stack traces)
```

## Environment Setup
Copy `.env.example` to `.env` and populate:
```
ANTHROPIC_API_KEY=...
EMAIL_SENDER=your_gmail@gmail.com
APP_PASSWORD=...        # Gmail App Password — NOT your account password
                        # Generate at myaccount.google.com/apppasswords
RECIPIENT_EMAIL=...
RESUME_PATH=/absolute/path/to/resume.tex   # or .docx
TARGET_DIR=/absolute/path/to/output/
```
Requires **JDK Temurin 21** — other JDK versions are not tested.

## Key Conventions
- **AI model selection:** Use the `fast` model (Haiku) for extraction and README summarization; `strong` model (Sonnet) for filtering and resume tailoring. Configured in `application.conf`, dispatched through `ClaudeService`. Do not swap these without good reason.
- **LaTeX resume invariant:** `JobTailor` splits the resume at `\begin{document}` using `LatexPair`. Claude only receives and returns the *body* — the preamble is reattached after. Never pass the full `.tex` file to Claude and never return preamble+body from tailoring.
- **Google Java Style is a CI gate:** Always run `mvn formatter:format` before committing. `mvn formatter:validate` runs in CI and will fail the build if violated.
- **Virtual threads:** All bulk operations use `ExecutorService.newVirtualThreadPerTaskExecutor()`. Avoid introducing blocking I/O directly on virtual threads when it can be avoided.
- **Rate limiting:** `ClaudeService` uses a semaphore (set via `jobhunter.ai.concurrency`) to cap parallel Claude calls. Do not bypass this.
- **Tailored resume naming:** Output files follow `{company}_{title}.tex` pattern in `TARGET_DIR`.

## Configuration
`src/main/resources/application.conf` (HOCON) controls:
- Claude prompts for extraction, filtering, tailoring, and resume/README parsing
- AI model IDs (`jobhunter.ai.models.fast` / `.strong`) and token limits per operation
- GitHub username and repo list for profile building
- Job sources (type, name, URL)
- Quartz cron schedule (default: 8 AM & 6 PM daily — not auto-started at launch)

## Important Context
- **SQLite deduplication (`JobRepository`)** is currently commented out and not wired into the pipeline.
- **Playwright** is the fallback scraper for JS-heavy job sites (e.g., Workday). `BrowserPool` manages reusable browser instances across virtual threads.
- **Profile caching:** `ProfileBuilder` caches to `profile-cache.json`. Delete this file to force a fresh GitHub + resume rebuild.
- **API timeout:** `AnthropicOkHttpClient` uses a 10-minute timeout to handle long resume tailoring requests.
- **Jackson lenient mode:** `ObjectMapper` allows unescaped control chars — required for malformed HTML content Claude receives from scraping.
- **Exception handling:** Domain errors subclass `JobHunterException`. `AiServiceException` wraps Claude API failures; `ScrapingException` wraps Playwright/jsoup failures.
