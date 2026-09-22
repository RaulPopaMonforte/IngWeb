# Lab 1 Git Race -- Project Report

## What I specified

I wanted to extend the hello application with a greeting chosen by the server from the current time in Madrid. A named visitor should see "Buenos días" before or at noon and "Buenas tardes" after noon. The page should also show the time of the request in a readable Spanish format. A new JSON route should return the greeting and a timestamp. The existing default page message and `/api/hello` route should keep working.

I would check the result by opening the page with and without a name, calling the new JSON route, and running the unit, MVC, and integration tests. The time shown on the page should agree with the `Europe/Madrid` zone.

## What I changed

- `src/main/kotlin/controller/HelloController.kt`: added `TimeDependingController` in the same Kotlin file. Its `timeDependingGreeting()` function returns a `Pair<String, ZonedDateTime>`. It compares the current `Instant` with today's noon in `Europe/Madrid`. The new `GET /api/helloTime?nombre=Raul` route returns JSON with `message` and `timestamp`; the default name is `Desconocido`.
- The `/` controller now uses that greeting when the `name` query parameter is present and adds a formatted timestamp to the Thymeleaf model. With no name, it still uses the configured `app.message`. The existing `GET /api/hello` route still returns its original English message and UTC timestamp.
- `src/main/resources/templates/welcome.html`: added a paragraph that displays the `timestamp` model attribute. Thymeleaf replaces the sample text inside that paragraph when the template is rendered.
- `src/test/kotlin/controller/HelloControllerUnitTests.kt`, `HelloControllerMVCTests.kt`, and `src/test/kotlin/IntegrationTest.kt`: adapted construction and expectations for the time-based greeting. The MVC test includes the new controller in its Spring test slice and checks the `/api/helloTime` JSON response.
- `.gitignore`: added an entry for `comportamiento_peticiones_server.png`.

To try the increment, run `./gradlew bootRun`, then visit `http://localhost:8080/?name=Raul` and `http://localhost:8080/api/helloTime?nombre=Raul`. The page uses `name`; the new JSON route uses `nombre`. There are no new configuration properties or dependencies.

## Technical decisions

I used `Instant` to compare two moments and `ZoneId.of("Europe/Madrid")` to calculate local noon. I convert the request time to `ZonedDateTime` for display, so daylight-saving changes are handled by the Java time API. The comparison uses `isAfter`, so exactly noon still produces "Buenos días". The HTML timestamp uses `DateTimeFormatter` with a Spanish locale and the pattern `d 'de' MMMM 'de' yyyy, HH:mm`. The JSON route uses the `ZonedDateTime` string representation, including its zone. No additional date library was needed.

I kept the original `/api/hello` route and the configured message for visitors without a name. `HelloController` receives `TimeDependingController` through Spring constructor injection; the unit tests create it directly. I did not add storage or change the application's dependencies. The MVC test for `/api/helloTime` checks the HTTP status, JSON content type, personalized message, and a parseable timestamp in the Madrid zone. It accepts either valid greeting so the test does not depend on the current time. The earlier page tests still calculate an expected greeting from the real clock, so they could become unreliable if a request crosses noon.

## How I verified

I ran `./gradlew check` and `./gradlew test --rerun-tasks` on 22 September 2026; both finished with `BUILD SUCCESSFUL`. The second command executed the tests instead of relying on Gradle's up-to-date result. After adding the new endpoint test, I ran `./gradlew test --tests es.unizar.webeng.hello.controller.HelloControllerMVCTests`; all four MVC tests passed. An earlier MVC test run failed because `@WebMvcTest` did not include `TimeDependingController`, which `HelloController` now requires. Adding that controller to the MVC test slice fixed the missing-bean error. The personalized-message expectations in the unit, MVC, and integration tests were also changed from the fixed English greeting to the Spanish greeting selected at runtime.

The compiler still reports a deprecation warning for the two-argument `Locale` constructor. `git diff --check main` reports trailing whitespace in the controller and MVC test. Neither prevented the test run. I have not recorded a manual HTTP check here.

## AI disclosure

- **Tools / skills:** ChatGPT/Codex assistant; no special skills.
- **Purpose:** Clarify Kotlin and Java time types, Spring controller injection and test failures, imports, string templates, and Thymeleaf behavior; help organize and edit this report.
- **Representative prompts:** "what type does this return: tiempoActual.atZone(IdZona)?"; "por qué fallan los test de HelloControllerMVCTests.kt"; "explícame de manera completa la estructura del proyecto" y "revisa el fichero REPORT.md".
- **Affected files/sections:** The advice informed the time and formatting code in `HelloController.kt` and the earlier test updates; the assistant drafted the text of `REPORT.md` from the repository changes and observed test results. The assistant also added the MVC test for `/api/helloTime` in `HelloControllerMVCTests.kt`.
- **Citations:** No external code snippets were adapted for this increment.
