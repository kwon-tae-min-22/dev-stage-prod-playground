# Repository Guidelines

## Project Structure & Module Organization
This Spring Boot board sample keeps application code in `src/main/java/com/example/demo`. Controllers/services/entities sit under domain-specific packages (e.g., `board`) so each module stays cohesive. Web assets live in `src/main/resources/templates` and `static`, while configuration defaults belong in `application.properties`. Tests mirror the same package boundaries under `src/test/java`, enabling Spring to pick up slices automatically; when adding a feature, create the production class and matching test folder in tandem.

## Build, Test, and Development Commands
- `./gradlew bootRun` starts the embedded server with live classpath reloads for quick UI checks.
- `./gradlew test` runs the JUnit 5 suite; add `--tests 'com.example.demo.board.*'` to focus on one module.
- `./gradlew build` compiles, tests, and packages the fat JAR consumed by CI/CD.
- `./gradlew clean test` clears previous outputs to catch dependency or order-sensitive failures before pushing.

## Coding Style & Naming Conventions
The Gradle toolchain pins Java 25, so stick to modern language features but keep 4-space indentation and 120-character lines. Name artifacts by responsibility (`PostService`, `PostRepository`, `BoardController`) and use constructor injection with `final` fields. Prefer descriptive verbs for service methods, kebab-case template filenames, and camelCase variables. Thymeleaf templates should stay presentation-only; move conditional logic into the controller model.

## Testing Guidelines
JUnit, Spring Boot Test, and `spring-security-test` are already on the classpath. Name classes `*Test` and mirror packages to inherit component scanning. Reach for `@WebMvcTest` or `@DataJpaTest` to isolate layers, reserving `@SpringBootTest` for full-stack scenarios. Controllers typically use MockMvc assertions for status, view name, and model attributes; repositories should exercise CRUD paths against the in-memory H2 database. Treat 80% line coverage as a floor and document any intentional gaps in the PR.

## Commit & Pull Request Guidelines
Write imperative, scope-prefixed commits such as `feat: add post detail view`, keeping the subject under ~72 characters and using wrapped body lines for nuance. PRs must describe motivation, list key changes, capture test commands, and link related issues or Jira tickets. Attach screenshots when altering Thymeleaf templates and call out migrations or SQL changes explicitly. Keep PRs small and self-contained to maintain reviewer velocity.

## Security & Configuration Tips
Avoid hardcoding secrets in `application.properties`; prefer profile overrides or environment variables. Restrict anonymous access to read-only endpoints inside `SecurityConfig` and add regression tests when permissions change. Disable the H2 console outside local development and scrub sample data before sharing build artifacts.
