# ai-job-hunter

An AI-powered CLI that scrapes job listings, filters them against your profile, tailors your LaTeX resume for each match, and emails you the results — automatically.

---

## Prerequisites

- **Java 21 JDK** — [Download Temurin 21](https://adoptium.net/temurin/releases/?version=21)
- **Apache Maven 3.x** — [Download Maven](https://maven.apache.org/download.cgi)
- **Git**

Verify your installs:
```bash
java -version   # should print 21.x.x
mvn -version    # should print 3.x.x
```

---

## Setup

### 1. Clone the repo

```bash
git clone https://github.com/MatthewOsorio/ai-job-hunter.git
cd ai-job-hunter
```

### 2. Create your `.env` file

Create a `.env` file in the project root with the following variables:

```env
ANTHROPIC_API_KEY=your_anthropic_api_key
EMAIL_SENDER=your_gmail@gmail.com
APP_PASSWORD=your_gmail_app_password
RECIPIENT_EMAIL=recipient@gmail.com
RESUME_PATH=/absolute/path/to/your/resume.tex
TARGET_DIR=/absolute/path/to/output/directory
```

| Variable | Description |
|---|---|
| `ANTHROPIC_API_KEY` | Your Claude API key from [console.anthropic.com](https://console.anthropic.com) |
| `EMAIL_SENDER` | Gmail address used to send match notifications |
| `APP_PASSWORD` | Gmail [App Password](https://myaccount.google.com/apppasswords) (not your account password) |
| `RECIPIENT_EMAIL` | Email address to receive job match notifications |
| `RESUME_PATH` | Absolute path to your `.tex` LaTeX resume file |
| `TARGET_DIR` | Absolute path to the directory where tailored resumes will be saved |

### 3. Configure `application.conf`

Open `src/main/resources/application.conf` and update:

- `github.username` — your GitHub username
- `github.repos` — list of repo names to include in your profile
- `sources` — job listing repos to scrape (set `enabled = true/false`)
- `scheduler.cron` — when the scheduler runs (default: 8 AM & 6 PM daily)

### 4. Build

```bash
mvn clean package
```

This produces a self-contained JAR at `target/ai-job-hunter-1.0-SNAPSHOT.jar`.

### 5. Set up the `jobhunter` alias

**macOS / Linux**

Add to your `~/.zshrc` or `~/.bashrc`:

```bash
alias jobhunter='java -jar /absolute/path/to/ai-job-hunter/target/ai-job-hunter-1.0-SNAPSHOT.jar'
```

Then reload your shell:

```bash
source ~/.zshrc   # or source ~/.bashrc
```

**Windows (PowerShell)**

Open your PowerShell profile:

```powershell
notepad $PROFILE
```

Add the following function:

```powershell
function jobhunter { java -jar "C:\absolute\path\to\ai-job-hunter\target\ai-job-hunter-1.0-SNAPSHOT.jar" @args }
```

Save the file, then reload your profile:

```powershell
. $PROFILE
```

---

## Usage

```bash
jobhunter          # Launch the interactive menu
jobhunter --help   # Show help and options
jobhunter -v       # Run with verbose output (full stack traces)
```

The interactive menu presents two options:

- **hunt** — scrape job listings, filter by your profile, tailor your resume, and send email notifications
- **view-profile** — display your cached GitHub profile used for job matching

---

## Development

```bash
mvn formatter:validate   # Check code formatting
mvn formatter:format     # Auto-format code
mvn test                 # Run unit tests
mvn spotbugs:check       # Run static analysis
```
