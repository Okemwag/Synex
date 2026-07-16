# Synex Android

Synex Android is a modular Jetpack Compose application that mirrors the calm, editorial visual language of the Synex web product. It uses MVVM at the feature boundary and keeps all Deriv access behind the Synex Go API.

## Modules

| Module | Responsibility |
| --- | --- |
| `app` | Composition root, build configuration, activity, and bottom-tab navigation |
| `core:model` | Platform-neutral account, market, candle, position, and portfolio models |
| `core:network` | Ktor client and JSON contracts for the Go API |
| `core:data` | Repository contract, Go-backed implementation, and DTO mappers |
| `core:ui` | Synex colors, typography, cards, rows, charts, loading, and error states |
| `feature:overview` | Equity, performance, quick actions, and watchlist MVVM flow |
| `feature:markets` | Searchable/filterable market catalogue MVVM flow |
| `feature:portfolio` | Portfolio summary and open-position MVVM flow |
| `feature:account` | Connected account, security, preferences, and funding boundaries |
| `feature:auth` | Auth0 Universal Login, PKCE session state, and encrypted credentials |
| `feature:legal` | Legal-centre links and disclosure access |

The dependency direction is one-way:

```text
app -> feature modules -> core:data -> core:network
                    \-> core:ui   -> core:model
```

Feature modules never call Ktor or Deriv directly. Their ViewModels depend only on `SynexRepository`, expose immutable `StateFlow` UI state, and keep Compose screens stateless.

## Source organization

- `app/.../di/AppContainer.kt` is the manual DI composition root. Constructor injection keeps dependencies visible without adding a framework.
- `core/network/.../dto/` contains small account, market, and portfolio transport models.
- `core/data/.../mapper/` converts DTOs into domain models.
- `core/network/Constants.kt` owns API paths and request defaults.
- `core/model/Enums.kt` owns shared domain enums.
- Feature state, ViewModels, screens, and larger screen components live in separate files.
- Shared colors, typography, feedback, cards, headings, and market components are separate `core:ui` files.

All production Kotlin source files are kept below 100 lines at this stage.

## Authentication

Both debug and release builds require a real Auth0 user session. Create a separate
Auth0 **Native** application for Android; do not reuse a client secret or embed one
in this project. Add these non-secret Gradle properties to your user-level
`~/.gradle/gradle.properties` or pass them with `-P`:

```properties
SYNEX_AUTH0_CLIENT_ID=your-native-application-client-id
SYNEX_AUTH0_DOMAIN=dev-5uxh5z65i7cmrxna.us.auth0.com
SYNEX_AUTH0_AUDIENCE=https://api.synex.app
```

Configure this exact callback and logout URL in that Native application:

```text
https://dev-5uxh5z65i7cmrxna.us.auth0.com/android/com.synex.mobile/callback
```

The Auth0 dashboard's Android Device Settings must also contain package
`com.synex.mobile` and the SHA-256 fingerprints for every signing certificate.
That allows Auth0 to publish the App Link verification file.

The current local debug fingerprint is:

```text
E1:84:09:D1:36:F3:3C:94:A6:55:69:63:08:16:B4:7B:03:23:E0:47:6F:AA:8B:B1:3F:C7:47:1E:E6:40:63:A9
```

```bash
./gradlew :app:assembleDebug
```

The generated APK is at `app/build/outputs/apk/debug/app-debug.apk`.

All builds use `NetworkSynexRepository`. Debug points to the emulator host at
`http://10.0.2.2:8080`; release disables cleartext traffic and points to
`https://api.synex.app`.

## Go API contract

The Android client is aligned with the existing backend routes:

- `GET /v1/markets/symbols` — public Deriv instrument catalogue
- `GET /v1/markets/candles?symbol=...&granularity=...&count=...` — public chart history
- `GET /v1/accounts` — authenticated connected Deriv accounts
- `GET /v1/portfolio?login_id=...` — authenticated open contracts

The Go backend remains the trusted boundary for Deriv tokens, risk controls, trade audit, and account ownership. The mobile app must never embed a Deriv token, Auth0 client secret, or payment credential.

## Explicit integration boundaries

The remaining security-sensitive integration boundaries are:

1. **Auth0 tenant setup:** add the Native client ID, callback/logout URL, API user-access grant, refresh-token grant, and signing fingerprints described above.
2. **Order ticket and execution:** add proposal, buy, sell, limits, and live position streams to the repository after the corresponding feature UX and suitability/risk checks are approved.
3. **Deposit and withdrawal:** connect the future payment gateway behind backend-issued payment intents and verified webhooks. The current account UI labels this boundary.

Auth0 Android performs Authorization Code with PKCE. `SecureCredentialsManager`
encrypts access and refresh credentials with Android Keystore-backed protection,
refreshes expiring access tokens, and clears them on logout.

## Verification

Run the app build and unit tests with:

```bash
./gradlew :app:assembleDebug testDebugUnitTest --max-workers=2
```
