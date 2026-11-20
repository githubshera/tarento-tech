# Java Code Assignment -- Warehouse Management

A Quarkus-based Java application for managing Warehouses, Stores,
Products, and warehouse fulfillment relationships. The project was
completed as part of a Java code assignment and includes REST APIs,
PostgreSQL persistence, business validations, automated tests, code
coverage checks, and a GitHub Actions CI workflow.

## Technology stack

-   Java 17
-   Quarkus 3.13.3
-   Maven
-   PostgreSQL 13
-   Hibernate ORM with Panache
-   REST endpoints with JSON
-   OpenAPI code generation for the Warehouse API
-   JUnit 5, Mockito, REST Assured, and Quarkus test support
-   JaCoCo for code coverage
-   GitHub Actions for continuous integration

## Prerequisites

Install or configure:

-   JDK 17 or later
-   Docker Desktop, or a PostgreSQL 13-compatible database
-   Git
-   Maven (if you are not using the Maven Wrapper)

Verify Java:

``` bash
java -version
```

The output should show Java 17 or later. Make sure `JAVA_HOME` points to
the JDK installation.

## Clone the repository

``` bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd java-assignment
```

Replace `<YOUR_GITHUB_REPOSITORY_URL>` with the URL of your GitHub
repository.

## Start PostgreSQL

The application configuration uses the following local database
settings:

Setting    Value
  ---------- ----------------
Database   `quarkus_test`
Username   `quarkus_test`
Password   `quarkus_test`
Host       `localhost`
Port       `15432`

Start a PostgreSQL 13 container:

``` bash
docker run -d --name quarkus_test \
  -e POSTGRES_USER=quarkus_test \
  -e POSTGRES_PASSWORD=quarkus_test \
  -e POSTGRES_DB=quarkus_test \
  -p 15432:5432 \
  postgres:13.3
```

If the container already exists, start it instead:

``` bash
docker start quarkus_test
```

Check that it is running:

``` bash
docker ps
```

The database must be available before starting the application or
running tests.

> **Database note:** The current Hibernate configuration uses
> `drop-and-create` and loads the configured SQL import script. This is
> suitable for the assignment's local development setup, but it
> recreates the schema when the application starts. Do not use this
> setting with production data.

## Configure the application

The datasource settings are in:

``` text
src/main/resources/application.properties
```

The local development and test profiles are configured to connect to:

``` text
jdbc:postgresql://localhost:15432/quarkus_test
```

If you use a different database, update the relevant profile-specific
datasource URL, username, and password before running the application or
tests.

## Build the project

From the project root, run:

### Using Maven

``` bash
mvn clean package
```

### Using the Maven Wrapper on Linux or macOS

``` bash
./mvnw clean package
```

### Using the Maven Wrapper on Windows PowerShell

``` powershell
.\mvnw.cmd clean package
```

If the Windows wrapper script cannot run in your environment, use the
wrapper JAR directly from PowerShell:

``` powershell
java "-Dmaven.multiModuleProjectDirectory=$PWD" -classpath ".mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain clean package
```

## Run the application

### Quarkus development mode

Using Maven:

``` bash
mvn quarkus:dev
```

Using the Maven Wrapper:

``` bash
./mvnw quarkus:dev
```

On Windows, use:

``` powershell
.\mvnw.cmd quarkus:dev
```

Or, if the Windows wrapper script fails, run:

``` powershell
java "-Dmaven.multiModuleProjectDirectory=$PWD" -classpath ".mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain quarkus:dev
```

Quarkus development mode supports live coding. The application is
available at:

``` text
http://localhost:8080
```

The provided landing page, if enabled in the project, is available at:

``` text
http://localhost:8080/index.html
```

### Run the packaged application

First package the application:

``` bash
mvn clean package
```

Then run the generated Quarkus application:

``` bash
java -jar target/quarkus-app/quarkus-run.jar
```

Make sure PostgreSQL is running and the datasource configuration is
correct before starting the packaged application.

## Main functionality

### Warehouse management

The Warehouse API supports:

-   Retrieving active Warehouses.
-   Creating a Warehouse.
-   Retrieving a Warehouse by its identifier.
-   Archiving a Warehouse.
-   Replacing a Warehouse while preserving its Business Unit Code and
    historical record.

Business validations include:

-   Business Unit Code must be unique among active Warehouses.
-   The referenced Location must exist.
-   Location warehouse-count and total-capacity limits must be
    respected.
-   Warehouse stock must not exceed its capacity.
-   A replacement Warehouse must have enough capacity for the existing
    stock.
-   Replacement stock must match the stock of the Warehouse being
    replaced.

The Warehouse API contract is defined using the OpenAPI specification in
the project. API-related code is generated as part of the build.

### Product and Store APIs

The application also provides Product and Store REST endpoints for
managing these resources.

Store changes are persisted transactionally. Calls to the legacy Store
Manager gateway are scheduled to run after the database transaction
commits, so the external operation is not triggered when the database
transaction rolls back.

### Warehouse fulfillment -- bonus functionality

The fulfillment functionality associates a Store, Warehouse, and Product
and enforces these limits:

-   A Product can be fulfilled by a maximum of two different Warehouses
    for a Store.
-   A Store can be associated with a maximum of three different
    Warehouses.
-   A Warehouse can store a maximum of five different Product types.

The implementation uses database relationships and locking to help
protect these limits when requests are processed concurrently.

## Run tests

Make sure PostgreSQL is running and accessible using the test datasource
configuration.

Run all tests:

``` bash
mvn clean test
```

Run the full verification build, including the JaCoCo coverage check:

``` bash
mvn clean verify
```

The project has a configured minimum line-coverage threshold of **80%**.
The `verify` build fails if the configured threshold is not met.

For Windows PowerShell, if the Maven Wrapper batch file does not work,
use:

``` powershell
java "-Dmaven.multiModuleProjectDirectory=$PWD" -classpath ".mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain clean verify
```

### View the JaCoCo report

After running `mvn clean verify`, open:

``` text
target/site/jacoco/index.html
```

The report shows line, instruction, branch, method, and class coverage.
The configured quality gate checks line coverage.

## Continuous integration -- GitHub Actions

The repository includes a GitHub Actions workflow at:

``` text
.github/workflows/ci.yml
```

The workflow runs on pushes and pull requests targeting the `main` or
`master` branch. It:

1.  Checks out the repository.
2.  Starts a PostgreSQL service container.
3.  Sets up Java 17.
4.  Runs `mvn clean verify`.
5.  Enforces the configured JaCoCo line-coverage threshold.

To view the result, open the repository on GitHub and select the
**Actions** tab.

## Project structure

The main source and configuration directories are:

``` text
java-assignment/
├── .github/
│   └── workflows/
│       └── ci.yml
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       └── java/
├── target/
├── pom.xml
├── CODE_ASSIGNMENT.md
├── QUESTIONS.md
├── CASE_STUDY.md
└── README.md
```

Generated source files and build output are created under `target/` and
should not be edited manually.

## Troubleshooting

### PostgreSQL connection refused

-   Check that Docker is running.
-   Check that the PostgreSQL container is running with `docker ps`.
-   Confirm that port `15432` is mapped to container port `5432`.
-   Confirm the database name, username, and password match
    `application.properties`.

### Port 15432 is already in use

Check which process or container is using the port. Stop the conflicting
service or update the PostgreSQL port mapping and the datasource URL
consistently.

### Generated API code is not recognized by the IDE

Run a Maven build to trigger code generation, then reload the Maven
project in IntelliJ. If required, mark the generated source directory
under `target/` as a generated source root. Do not edit generated files
directly.

### Coverage check fails

Open `target/site/jacoco/index.html` and review uncovered lines. Add
meaningful tests for missing business scenarios rather than adding tests
solely to increase the percentage.

## Assessment documentation

The repository includes:

-   `CODE_ASSIGNMENT.md` -- original implementation requirements.
-   `QUESTIONS.md` -- design and engineering reasoning.
-   `CASE_STUDY.md` -- business-focused cost management case study.

## Notes

-   Keep credentials and environment-specific configuration out of
    production source control.
-   The current `drop-and-create` schema strategy is for
    assignment/development use only. A production deployment should use
    a controlled database migration strategy.
-   Before deploying to a shared or production environment, configure
    environment-specific secrets, database settings, logging,
    monitoring, and an appropriate external-system integration strategy.
