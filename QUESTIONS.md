# Technical Assessment -- Questions and Answers

This document contains my answers to the three questions about the
warehouse management codebase. The answers reflect the current
implementation and explain the reasoning behind the design and testing
decisions.

------------------------------------------------------------------------

## 1. Database access strategies

**Question:** In this codebase, we have some different implementation
strategies when it comes to database access layer and manipulation. If
you would maintain this codebase, would you refactor any of those? Why?

**Answer:**

Yes. I would gradually make the database access layer more consistent
and maintainable. The codebase uses different approaches, including the
Panache Active Record pattern, the Repository pattern, and direct use of
`EntityManager`. These approaches are all valid, but using them without
clear conventions can make the code harder to understand and test.

Each approach has its own strengths and trade-offs:

-   **Panache Active Record:** Convenient for simple CRUD operations
    because persistence methods are available directly on the entity.
    However, it can couple domain logic to persistence concerns.
-   **Repository pattern:** Keeps persistence operations behind a
    separate abstraction. This makes business logic easier to test and
    helps keep database-specific code in one place. It does introduce
    some additional classes and methods.
-   **`EntityManager`:** Gives fine-grained control over persistence
    operations, queries, entity state, and locking. It is useful for
    advanced scenarios, but direct use throughout the application can
    lead to inconsistent data-access patterns.

For most new and evolving business functionality, I would prefer the
Repository pattern, with business rules kept in use cases or service
classes.

My reasons are:

1.  **Separation of concerns:** Use cases should coordinate business
    rules, while repositories should handle data retrieval and
    persistence.
2.  **Testability:** Repository dependencies can be mocked when
    unit-testing business logic. Repository implementations can
    separately be tested against PostgreSQL.
3.  **Maintainability:** A consistent convention makes the code easier
    for new developers to understand and reduces duplicated persistence
    logic.
4.  **Flexibility:** Database queries and persistence details can change
    with less impact on business logic.

I would not remove `EntityManager` completely. It remains appropriate
when a use case needs capabilities such as pessimistic locking or more
specialized persistence operations. For example, the warehouse
fulfillment functionality uses repositories for normal data access and
`EntityManager` to lock Store and Warehouse records while enforcing
limits. This helps protect the rules when concurrent requests attempt to
create fulfillment relationships.

I would also keep related database changes within a clear transaction
boundary at the use-case or service layer. This is particularly
important for warehouse replacement, where archiving the existing
warehouse and creating its replacement must succeed or fail together.

I would not rewrite the entire application just to use one pattern. I
would refactor incrementally, starting with areas that have duplicated
queries, complex business logic mixed with persistence code,
inconsistent transaction handling, or frequent changes. This approach
reduces regression risk while improving consistency over time.

------------------------------------------------------------------------

## 2. OpenAPI-generated endpoints vs manually implemented endpoints

**Question:** When it comes to API specifications and endpoint handlers,
we have an OpenAPI YAML file for the `Warehouse` API from which we
generate code, but for the other endpoints --- `Product` and `Store` ---
we coded everything directly. What are the pros and cons of each
approach, and what would be your choice?

**Answer:**

Both approaches are useful. The right choice depends on the API's
expected lifetime, complexity, consumers, and the need for a stable
contract.

### OpenAPI contract-first approach

With this approach, the API contract is defined in an OpenAPI YAML file.
Code-generation tooling can then generate API interfaces, models, or
other supporting code. The application implements the generated
contract.

**Advantages:**

1.  **Clear contract:** The API structure, operations, request
    parameters, response models, and status codes can be reviewed before
    implementation.
2.  **Parallel development:** Frontend, backend, and integration teams
    can work against the same agreed contract.
3.  **Consistent documentation:** The specification can serve as a
    source of API documentation and can be used by tools to generate
    clients.
4.  **Consistency:** It encourages consistent request and response
    definitions across endpoints.
5.  **Change visibility:** Contract changes can be reviewed and
    discussed explicitly.

**Disadvantages:**

1.  The OpenAPI specification must be maintained alongside the
    implementation.
2.  Code generation adds build configuration and a regeneration step.
3.  Generated code can be less convenient to customize, so business
    logic should remain outside generated classes.
4.  Specification and generated code can become out of sync if the
    generation process is not followed consistently.

### Manually implemented endpoints

With this approach, developers write the resource classes, request and
response models, validation, and endpoint handlers directly.

**Advantages:**

1.  **Flexibility:** Developers have direct control over endpoint
    behaviour and implementation.
2.  **Simplicity for small APIs:** There is no code-generation
    configuration to understand or maintain.
3.  **Fast initial development:** It can be convenient while
    requirements are still being explored.
4.  **Straightforward debugging:** The endpoint implementation is
    written directly in the application code.

**Disadvantages:**

1.  Documentation can become outdated if it is not maintained.
2.  Similar endpoints may gradually use inconsistent request, response,
    or error formats.
3.  Repetitive API definitions and boilerplate may need to be maintained
    manually.
4.  Contract changes need to be coordinated and communicated through the
    development process.

### My choice

For APIs that are public, consumed by other teams, or expected to remain
stable over time, I would prefer a contract-first approach using
OpenAPI. It makes the API contract explicit and supports collaboration
between teams.

In this codebase, the Warehouse API already uses an OpenAPI YAML
specification to generate API-related code, while Product and Store are
implemented manually. I would consider bringing Product and Store under
the same contract-first approach if they become important integration
APIs or grow in complexity.

I would not introduce code generation automatically for every small
internal endpoint. A manually implemented endpoint can be a reasonable
choice when the API is small, has limited consumers, and is unlikely to
need a separately managed contract.

Whichever approach is chosen, I would keep business rules in use cases
or service classes rather than putting them into generated API classes
or resource handlers. This keeps the API layer focused on HTTP concerns
and makes the business logic independently testable.

------------------------------------------------------------------------

## 3. Test prioritization and maintaining effective coverage

**Question:** Given the need to balance thorough testing with time and
resource constraints, how would you prioritize and implement tests for
this project? Which types of tests would you focus on, and how would you
ensure test coverage remains effective over time?

**Answer:**

With limited time and resources, I would prioritize tests according to
business impact, likelihood of failure, and the cost of a defect. I
would use a combination of unit tests, repository/integration tests, and
API tests rather than relying on only one type.

### 1. Unit tests for business rules

I would start with the use cases and validation rules because defects in
these areas can lead to invalid warehouse or fulfillment data.

For this project, important scenarios include:

-   Rejecting a duplicate active Business Unit Code.
-   Rejecting an unknown location.
-   Enforcing the maximum number of warehouses allowed at a location.
-   Ensuring warehouse capacity does not exceed the location's maximum
    capacity.
-   Ensuring stock does not exceed warehouse capacity.
-   Ensuring a replacement warehouse has enough capacity for the
    existing stock.
-   Ensuring replacement stock matches the old warehouse's stock.
-   Enforcing the bonus fulfillment limits: no more than two warehouses
    for a product in a store, three warehouses for a store, and five
    product types for a warehouse.

I would cover successful scenarios, invalid input, boundary values, and
expected failures. These tests are usually fast and give precise
feedback about business logic.

### 2. Repository and database integration tests

I would test persistence behaviour against PostgreSQL, not only with
mocked repositories. This helps catch issues that unit tests cannot
detect.

Examples include:

-   Creating, retrieving, updating, and archiving warehouses.
-   Verifying entity-to-domain and domain-to-entity mapping.
-   Checking relationships and database constraints.
-   Verifying transaction rollback behaviour.
-   Testing locking and concurrent updates where the business rules
    require them.

Mocks are useful for isolating business logic, but they cannot prove
that a query, mapping, or database constraint works correctly against
the actual database.

### 3. API tests

I would test the REST endpoints at the HTTP boundary to verify that the
application exposes the expected behaviour.

I would cover:

-   Correct HTTP status codes.
-   Request validation and invalid payloads.
-   Response body and response structure.
-   Missing resources and invalid identifiers.
-   Consistent error responses.
-   Correct interaction between resource classes and use cases.

For example, a successful warehouse creation should return HTTP `201`,
while a request for a warehouse that does not exist should return HTTP
`404`.

### 4. Transaction and external-system tests

I would give particular attention to the Store integration with the
legacy store manager. A key requirement is that calls to the legacy
system must happen only after the database transaction commits.

I would test both outcomes:

-   When the database transaction commits, the corresponding legacy
    operation should be invoked.
-   When the database transaction rolls back, the legacy operation
    should not be invoked.

This verifies an important consistency requirement between the database
and the external system. For a production-grade integration, I would
also consider retry handling, idempotency, and an outbox pattern if
reliable delivery must be guaranteed despite process failures after
commit.

### 5. Keeping coverage effective over time

In this project, I integrated JaCoCo with Maven and configured a minimum
**80% line-coverage threshold**. The Maven `verify` phase generates the
coverage report and fails the build when the configured threshold is not
met.

I also configured GitHub Actions to run the Maven `clean verify` build
when changes are pushed or pull requests are opened. The workflow starts
PostgreSQL, sets up Java 17, and runs the build and tests. The
successful workflow confirms that the automated build and configured
coverage check pass in CI.

I would not treat the coverage percentage as the only measure of test
quality. High line coverage does not necessarily mean that important
business scenarios, branches, or failure cases are tested. I would
therefore:

1.  Review coverage reports to identify untested code, especially in
    critical business logic.
2.  Add or update tests whenever a business rule or endpoint changes.
3.  Include regression tests for defects so that the same issue is less
    likely to return.
4.  Review boundary conditions, negative scenarios, and transaction
    behaviour during code reviews.
5.  Keep the CI quality gate in place and investigate coverage decreases
    rather than adding tests solely to satisfy a percentage.

**Overall approach:** I would test the highest-risk business rules
first, then verify persistence and API behaviour, and use automated CI
checks to keep the test suite and coverage threshold effective as the
codebase evolves.
