# Installation Guide

This guide will help you set up and run the Techstars Jobs Scraper application.

## Prerequisites

- Docker and Docker Compose
- Java 17 or higher (for local development)
- Maven (for local development)
- PostgreSQL (for local development)

## Installation Steps

### Using Docker (Recommended)

1. Clone the repository:

   ```bash
   git clone <repository-url>
   cd techstars-jobs-scraper
   ```

2. Build and start the containers:

   ```bash
   docker-compose up --build
   ```

3. The application will be available at `http://localhost:8080`

### Local Development Setup

1. Clone the repository:

   ```bash
   git clone <repository-url>
   cd techstars-jobs-scraper
   ```

2. Set up PostgreSQL:

   - Create a database named `techstars_jobs`
   - Update `application.properties` with your database credentials

3. Build the project:

   ```bash
   mvn clean install
   ```

4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## Configuration

The application can be configured through environment variables or the `application.properties` file:

- `spring.datasource.url`: Database connection URL
- `spring.datasource.username`: Database username
- `spring.datasource.password`: Database password
- `server.port`: Application port (default: 8080)

## Usage

1. Start the application using either Docker or local setup
2. Access the API endpoint:
   ```
   POST http://localhost:8080/api/jobs/scrape?jobFunction=<job-function>
   ```

Example:

```bash
curl -X POST "http://localhost:8080/api/jobs/scrape?jobFunction=Software%20Engineering"
```

## Troubleshooting

1. If you encounter Chrome/ChromeDriver issues:

   - For Docker: The container includes the necessary setup
   - For local: Install Chrome and ChromeDriver matching your Chrome version

2. Database connection issues:

   - Verify PostgreSQL is running
   - Check database credentials in configuration
   - Ensure database exists

3. Application startup issues:
   - Check logs for detailed error messages
   - Verify all required ports are available
   - Ensure all dependencies are properly installed
