# Techstars Jobs Scraper

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A Spring Boot application for scraping job listings from jobs.techstars.com based on specific job functions. This tool helps automate the process of collecting and organizing job postings from Techstars' career portal.

## 🚀 Features

- Scrapes job listings from Techstars Jobs website
- Filters jobs by specific job functions
- Stores job data in PostgreSQL database
- RESTful API for triggering scraping operations
- Docker support for easy deployment
- Multithreaded scraping for improved performance
- Automatic Chrome/ChromeDriver management
- Configurable scraping parameters

## 📊 Data Points Collected

For each job listing, the following information is collected:

- Position name
- Job page URL
- Organization URL
- Organization logo
- Organization title
- Labor function
- Location
- Posted date (Unix timestamp)
- Job description (with HTML formatting)
- Tags

## 🛠 Technical Stack

- Java 17
- Spring Boot 3.5.0
- PostgreSQL
- JPA/Hibernate
- JSoup for HTML parsing
- Selenium for dynamic content
- Docker & Docker Compose
- Maven

## ⚙️ Configuration

### Environment Variables

The following environment variables can be configured:

| Variable                     | Description                           | Default                                           |
| ---------------------------- | ------------------------------------- | ------------------------------------------------- |
| `SPRING_DATASOURCE_URL`      | PostgreSQL connection URL             | `jdbc:postgresql://localhost:5432/techstars_jobs` |
| `SPRING_DATASOURCE_USERNAME` | Database username                     | `postgres`                                        |
| `SPRING_DATASOURCE_PASSWORD` | Database password                     | `postgres`                                        |
| `SERVER_PORT`                | Application port                      | `8080`                                            |
| `SCRAPER_THREAD_COUNT`       | Number of concurrent scraping threads | `4`                                               |
| `SCRAPER_TIMEOUT_SECONDS`    | Timeout for scraping operations       | `30`                                              |

### Application Properties

Key configuration properties in `application.properties`:

```properties
# Database Configuration
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

# Scraper Configuration
scraper.thread-count=${SCRAPER_THREAD_COUNT}
scraper.timeout-seconds=${SCRAPER_TIMEOUT_SECONDS}
```

## 📁 Project Structure

```
src/main/java/dev/javajunior/techstars_jobs_scraper/
├── controller/
│   └── JobScraperController.java    # REST API endpoints
├── model/
│   └── Job.java                     # Job entity
├── repository/
│   └── JobRepository.java           # Database operations
├── service/
│   └── JobScraperService.java       # Business logic
└── TechstarsJobParserApplication.java
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8.x
- Docker and Docker Compose
- PostgreSQL (if running without Docker)

### Quick Start with Docker

1. Clone the repository:

   ```bash
   git clone https://github.com/yourusername/techstars-jobs-scraper.git
   cd techstars-jobs-scraper
   ```

2. Build and run with Docker Compose:
   ```bash
   docker-compose up --build
   ```

For detailed installation and setup instructions, see [INSTALL.md](INSTALL.md).

## 📡 API Endpoints

### Scrape Jobs

```
POST /api/jobs/scrape?jobFunction={jobFunction}
```

Parameters:

- `jobFunction` (required): The job function to filter by (e.g., "Software Engineering")

Example Request:

```bash
curl -X POST "http://localhost:8080/api/jobs/scrape?jobFunction=Software%20Engineering"
```

Response Format:

```json
{
  "status": "success",
  "message": "Scraping completed",
  "jobsScraped": 42
}
```

Response Fields:

- `status`: The status of the scraping operation ("success" or "error")
- `message`: A descriptive message about the operation result
- `jobsScraped`: The number of job listings successfully scraped and stored

Example Response:

```json
{
  "status": "success",
  "message": "Scraping completed",
  "jobsScraped": 42
}
```

Error Response:

```json
{
  "status": "error",
  "message": "Failed to scrape jobs: Invalid job function",
  "jobsScraped": 0
}
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Techstars for providing the job listings
- Spring Boot team for the amazing framework
- All contributors who have helped improve this project
