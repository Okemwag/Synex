# Synex Android

Synex is the mobile trading client for the Synex platform. It gives customers a clear, calm view of global markets, their connected Deriv trading accounts, and their open positions — behind a secure Auth0 sign-in, with all broker access mediated by the Synex Go API.

> **Status:** pre-release. Order execution and payments are intentionally not yet enabled in this client. See [Roadmap](#roadmap) and [Compliance and legal](#compliance-and-legal).

---

## Contents

- [Features](#features)
- [Architecture](#architecture)
- [Requirements](#requirements)
- [Configuration](#configuration)
- [Building and running](#building-and-running)
- [Testing](#testing)
- [Backend API contract](#backend-api-contract)
- [Security model](#security-model)
- [Compliance and legal](#compliance-and-legal)
- [Roadmap](#roadmap)

## Features

- **Secure sign-in** — Auth0 Universal Login using Authorization Code with PKCE. Credentials are encrypted at rest by Auth0's `SecureCredentialsManager` with Android Keystore-backed protection; Synex never sees or stores passwords.
- **Guided first visit** — branded splash, sign-in, plain-language legal introduction, privacy overview, and a server-recorded risk acknowledgement before the dashboard opens. Returning customers skip completed steps.
- **Overview** — account-backed equity, available cash, open P/L, working navigation, and a market watchlist.
- **Markets** — searchable, category-filtered catalogue of instruments (forex, crypto, commodities, derived indices).
- **Portfolio** — net equity, profit/loss, and open-position detail for the connected trading account.
- **Account** — live/demo identification, connected-account selection, balance, legal access, and sign-out.
- **Legal centre** — policies and disclosures open inside Synex, with guarded navigation that stays on the Synex domain.
- **Motion and navigation** — calm page transitions, animated selection states, smooth scrolling, and a premium icon treatment shared across the app.

## Architecture

A modular, single-direction Jetpack Compose application. Feature modules follow MVVM: ViewModels depend only on the `SynexRepository` contract, expose immutable `StateFlow` state, and keep Compose screens stateless. Feature modules never call Ktor or Deriv directly.

```text
app ──► feature:* ──► core:data ──► core:network ──► Synex Go API ──► Deriv
              │
              └─────► core:ui ──► core:model
```

| Module | Responsibility |
| --- | --- |
| `app` | Composition root, branded launcher/splash, build configuration, journey gates, bottom-tab navigation |
| `core:model` | Platform-neutral domain models (accounts, markets, candles, positions, portfolio) |
| `core:network` | Ktor HTTP client, API routes, and JSON transport DTOs for the Go API |
| `core:data` | `SynexRepository` contract, network-backed implementation, DTO→domain mappers |
| `core:ui` | Shared visual foundation: colors, typography, cards, rows, loading and error states |
| `feature:auth` | Auth0 Universal Login, PKCE session state, encrypted credentials, auth gate |
| `feature:onboarding` | First-visit legal journey, privacy overview, and versioned risk acknowledgement |
| `feature:overview` | Verified account equity, working navigation, and watchlist |
| `feature:markets` | Market catalogue with search and category filters |
| `feature:portfolio` | Portfolio summary and open positions |
| `feature:account` | Account selection, live/demo status, sign-out, and legal entry point |
| `feature:legal` | Legal centre listing policy and disclosure documents |

Dependency injection is manual: `app/.../di/AppContainer.kt` is the composition root, and constructor injection keeps the object graph explicit without a framework. Transport DTOs live in `core/network/.../dto/`, DTO→domain mappers in `core/data/.../mapper/`, API paths and request defaults in `core/network/Constants.kt`.

### Technology

| | |
| --- | --- |
| Language | Kotlin 2.0 (JVM target 11) |
| UI | Jetpack Compose, Material 3, Navigation Compose |
| Networking | Ktor (OkHttp engine), kotlinx.serialization |
| Auth | Auth0 Android SDK 3.x |
| Build | Gradle 8.13, AGP 8.13, version catalog (`gradle/libs.versions.toml`) |
| Android | minSdk 24, target/compileSdk 36 |

## Requirements

- Android Studio (latest stable) with JDK 11+
- An Auth0 **Native** application for Android
- A running Synex Go API instance (debug builds default to `http://10.0.2.2:8080`, the Android emulator's host loopback)

## Configuration

### Auth0

Both debug and release builds require a real Auth0 user session. Create a dedicated Auth0 **Native** application — never reuse or embed a client secret in this project. Provide these non-secret Gradle properties in your user-level `~/.gradle/gradle.properties`, or pass them with `-P`:

```properties
SYNEX_AUTH0_CLIENT_ID=your-native-application-client-id
SYNEX_AUTH0_DOMAIN=dev-5uxh5z65i7cmrxna.us.auth0.com
SYNEX_AUTH0_AUDIENCE=https://api.synex.app
```

In that Native application, configure this exact callback and logout URL:

```text
https://dev-5uxh5z65i7cmrxna.us.auth0.com/android/com.synex.mobile/callback
```

The Auth0 dashboard's **Android Device Settings** must also contain the package name `com.synex.mobile` and the SHA-256 fingerprint of every signing certificate, so Auth0 can publish the App Link verification file. The current local debug fingerprint is:

```text
E1:84:09:D1:36:F3:3C:94:A6:55:69:63:08:16:B4:7B:03:23:E0:47:6F:AA:8B:B1:3F:C7:47:1E:E6:40:63:A9
```

The tenant also needs the API user-access grant and the refresh-token grant enabled for this application.

Debug builds can use the documented development domain and audience defaults. Release
builds have no tenant fallback and `preReleaseBuild` fails unless all three Auth0
properties are explicitly supplied.

### API endpoints

API base URLs are fixed per build type in `app/build.gradle.kts`. All builds use `NetworkSynexRepository`.

| Build type | API base URL | Cleartext traffic |
| --- | --- | --- |
| `debug` | `http://10.0.2.2:8080` | allowed (local development only) |
| `release` | `https://api.synex.app` | disabled |

## Building and running

```bash
# Assemble a debug APK
./gradlew :app:assembleDebug

# Install on a connected device or emulator
./gradlew :app:installDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

The app opens with the supplied Synex launcher artwork and branded splash. After sign-in, first-time customers move through the legal, privacy, and risk journey before reaching the dashboard. Returning customers with the current risk acknowledgement go straight to the dashboard.

## Testing

```bash
# Unit tests
./gradlew testDebugUnitTest

# Full verification (build + unit tests)
./gradlew :app:assembleDebug testDebugUnitTest --max-workers=2
```

## Backend API contract

The client consumes the following Synex Go API routes:

| Route | Auth | Purpose |
| --- | --- | --- |
| `GET /v1/markets/symbols` | public | Deriv instrument catalogue |
| `GET /v1/markets/candles?symbol&granularity&count` | public | Chart history |
| `GET /v1/accounts` | bearer | Connected Deriv trading accounts |
| `GET /v1/portfolio?login_id` | bearer | Open contracts for an account |
| `GET /v1/onboarding/status` | bearer | Current onboarding and disclosure status |
| `POST /v1/onboarding/risk-acknowledgement` | bearer | Store the current risk acknowledgement with its audit evidence |

The Go backend is the trusted boundary for Deriv tokens, risk controls, trade audit, and account ownership. **No Deriv token, Auth0 client secret, or payment credential is ever embedded in this app.**

## Security model

- **Authentication:** Authorization Code with PKCE via Auth0 Universal Login; no client secret ships in the APK.
- **Credential storage:** Auth0 `SecureCredentialsManager` encrypts access and refresh credentials with Android Keystore-backed protection, refreshes expiring access tokens, and clears them on logout.
- **Transport:** HTTPS only in release builds; cleartext is permitted solely for the local emulator backend in debug builds.
- **Screen privacy:** `FLAG_SECURE` blocks screenshots and display capture throughout authenticated app content.
- **Backup:** Android backup and device transfer are disabled so encrypted Auth0 preferences are never restored without their originating Keystore keys.
- **Release build:** R8 minification and resource shrinking are enabled, and missing production Auth0 configuration fails the build.
- **Trust boundary:** the mobile client is untrusted. All broker credentials, order controls, and money movement live behind the Go API.

Planned hardening before public release: certificate pinning with an approved rotation policy and Play Integrity checks ahead of enabling payments.

## Compliance and legal

Synex is a financial product in development. Before public launch:

- All documents in the in-app Legal centre (privacy notice, platform terms, trading risk disclosure, AML policy, complaints procedure, data rights) are **drafts pending legal and compliance approval**.
- Licensing, registration, and disclosure obligations in target markets — including Capital Markets Authority requirements in Kenya and Deriv's third-party application terms — must be confirmed before distribution to customers.
- Google Play requirements for finance apps — privacy policy URL, Data Safety form, Financial Features declaration, and an account-deletion flow — are launch prerequisites.

This client deliberately excludes trading execution and payments until the corresponding regulatory, suitability, and risk-control work is approved:

1. **Order ticket and execution** — proposal, buy, sell, limits, and live position streams will be added to the repository only after the feature UX and suitability/risk checks are approved.
2. **Deposits and withdrawals** — the future payment gateway will connect behind backend-issued payment intents and verified webhooks. Funding controls stay hidden until that flow is real.

## Roadmap

- [x] Auth0 sign-in with secure credential storage
- [x] Markets, portfolio, overview, and account read flows
- [x] In-app legal centre
- [x] Versioned risk disclosure acknowledgement at onboarding
- [ ] Versioned platform-terms acceptance after the final terms are approved
- [x] Live/demo account distinction and account switching
- [ ] Order ticket and execution
- [ ] Deposits and withdrawals via backend-issued payment intents
- [ ] Complete release hardening (certificate pinning and Play Integrity remain)
- [ ] Localization and full string externalization

---

© Synex. All rights reserved. This repository is proprietary; no license is granted for reuse or redistribution.
