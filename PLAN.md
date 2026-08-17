# Synex Deriv API Completion Plan

Last audited: 2026-08-17
Official schema baseline: [`production_v20260804_0`](https://github.com/deriv-com/deriv-api-schemas/releases/latest)

## Goal

Make every capability in Deriv's current official APIs accessible through Synex as one of:

- A user-facing workflow.
- An operator or administrator workflow.
- An internal transport operation.
- An intentionally unsupported capability with a documented reason.

Completion does **not** mean exposing Deriv as an unrestricted raw proxy. Every operation must use typed inputs, account-ownership checks, authorization, auditing, validation, and appropriate real-money safety controls.

## Repositories in scope

- Go backend: `/home/okemwag/wanderlog`
- Web frontend: `/home/okemwag/cremia`
- Android app: `/home/okemwag/AndroidStudioProjects/Synex`

## Current baseline

The audited Deriv surface contains approximately:

- 30 WebSocket request types.
- 27 REST route operations.
- 35 option contract types in the proposal schema.

Current product assessment:

| Product area | Backend | Web | Android |
|---|---|---|---|
| Public market data | Partial | Partial | Broken/partial |
| Manual options trading | Partial | Partial | Missing |
| Portfolio and positions | Partial | Partial | Partial |
| Statements and activity | Partial | Partial | Missing |
| Deriv account setup | Partial | Partial | Partial |
| Wallets | Missing | Missing | Missing |
| Payment agents | Missing | Disabled | Missing |
| Automated trading | Missing | Missing | Missing |
| Bulk purchasing | Missing | Missing | Missing |
| Legacy migration | Missing | Missing | Missing |
| Operator/application statistics | Missing | Missing | Missing |
| System and status utilities | Internal/partial | Missing | Missing |

The backend currently touches 19 of the 30 WebSocket request types, but several are only partially implemented or are not complete user workflows.

## Phase 0 — Make the current product trustworthy

Progress update (2026-07-17): implementation is underway. Manual web and Android testing is owned by the product owner and is intentionally not tracked in this plan.

### Production Deriv connection

- [x] Register the production callback in the Deriv application as exactly `https://synex-backend.onrender.com/v1/auth/deriv/callback`.
- [x] Store encrypted OAuth grants and connected Deriv accounts after a successful link.
- [x] Refresh expiring Deriv access tokens, rotate refresh tokens when supplied, and preserve previously granted scopes.
- [x] Reload connected accounts after authentication and retain the selected account across app workflows.
- [x] Configure the web callback to return the user to the correct `wanderlog.xyz` route.
- [x] Keep Deriv connection/demo readiness separate from optional live-trading onboarding so the web flow cannot bounce between Accounts and Finish setup.
- [x] Implement a native Android app-link/deep-link return flow instead of relying only on manual return from the browser. Implemented with a signed, backend-allow-listed `synex://deriv-connect` return target and Android browsable intent filter.

### Live position correctness

- [x] Fix `proposal_open_contract` decoding so numeric strings such as `buy_price`, `current_spot`, `profit`, and `payout` are accepted safely.
- [x] Wire live position values into Android portfolio state through the authenticated account SSE stream.
- [x] Remove settled and sold contracts from Android portfolio state when the live stream reports completion.
- [x] Restore account subscriptions automatically after a dropped WebSocket connection.

### Android production correctness

- [x] Replace the dead release URL `https://api.synex.app` with the deployed Synex backend URL or a valid production API domain. Release builds now default to `https://synex-backend.onrender.com` and reject non-HTTPS release overrides.
- [x] Replace legacy market DTO fields such as `symbol` and `display_name` with the current Deriv V2/backend contract.
- [x] Replace legacy portfolio DTO fields with the current response contract.
- [x] Stop showing artificial zero quote/change values when active-symbol responses do not contain prices. Android now shows market availability and explicitly labels live price as unavailable.
- [x] Wire candle/history data into the Android market experience. Tapping a market now loads its 30-day candles and opens a price-history card.

### Contract and deployment safety

- [x] Pin the official Deriv schema version used by the backend. Recorded in backend `DERIV_SCHEMA_VERSION` as `production_v20260709_1`.

## Phase 1 — Complete manual options trading

### Schema-driven contract catalogue

- [x] Inventory all 35 current Deriv proposal contract types. The pinned schema enum is enforced at the backend proposal boundary.
- [x] Group contract types into UI families so the ticket can render barriers, digits, multipliers, accumulators, tick selection, vanilla, and turbo controls appropriately.
- [x] Record required and optional proposal fields for every contract type and attach the applicable rule to each live `contracts_for` result.
- [x] Record account, jurisdiction, symbol, and availability restrictions in the shared contract rule returned to clients.
- [ ] Generate or validate the catalogue from the official schema instead of maintaining an unverified handwritten list.
- [x] Show only contract types returned by Deriv's `contracts_for` response for the selected market and use its barrier/range metadata to configure the ticket.

### Complete trade ticket

- [x] Render contract-specific fields instead of one generic stake/duration form.
- [x] Support `barrier`.
- [x] Support `barrier2`.
- [x] Support `growth_rate`.
- [x] Support `multiplier`.
- [x] Support cancellation configuration.
- [x] Support `selected_tick`.
- [x] Support `payout_per_point`.
- [x] Support duration-based expiry.
- [x] Support date-based expiry.
- [x] Support take-profit and stop-loss limit orders through Deriv's nested `limit_order` contract.
- [x] Validate required fields locally before requesting a proposal, using the selected `contracts_for` metadata and the pinned proposal schema constraints.
- [x] Show understandable validation errors returned by Deriv while retaining safe provider detail for errors that do not match a known category.
- [x] Refresh expired proposals before purchase and require the customer to review and reconfirm the changed price.
- [x] Prevent double purchases through UI locking, instruction tracking, request hashing, and backend idempotency keys.

### Purchase and position lifecycle

- [x] Implement demo purchase for each contract family through the shared contract-aware proposal and idempotent buy pipeline.
- [x] Display a complete purchase receipt, including extended expiry, barrier, multiplier, accumulator, tick, and limit-order terms.
- [x] Show open-contract status and live profit/loss.
- [x] Support eligible early sell operations.
- [x] Support eligible cancellation operations.
- [x] Support contract updates and update history.
- [x] Show settled contract results with paid, returned, profit/loss, contract ID, and Deriv transaction ID as separate customer-visible values.
- [x] Require explicit confirmation before every real-money purchase.
- [x] Add configurable per-account stake, daily-loss, and session-loss limits, enforce them before execution, and allow an explicit session reset.

### Market data completion

- [x] Implement `contracts_list` through the public Synex market API.
- [x] Implement server time and expose Deriv time in the web market experience.
- [x] Implement trading times and show the selected market's current session when Deriv supplies one.
- [x] Expose raw tick history as well as candle history through the bounded public history API.
- [x] Support start and end timestamps.
- [x] Support available history styles and granularities.
- [ ] Support history subscriptions where useful.
- [ ] Support complete pagination/query options.
- [ ] Add market-data caching, rate-limit handling, and reconnect behavior.

### Activity and reporting

- [x] Add statement date-range filters.
- [x] Add statement action/type filters.
- [x] Add offset-based pagination.
- [x] Add sorting.
- [x] Add profit-table filters and pagination.
- [x] Show complete transaction details.
- [x] Reconcile live transaction events by refreshing statement/profit-table history when the authenticated account stream reports a new transaction.
- [x] Add CSV or equivalent activity export.

### Android manual-trading parity

- [x] Add a Trade destination.
- [x] Add contract discovery and contract-specific ticket forms.
- [x] Add proposal pricing and expiry handling.
- [x] Add demo and real purchase confirmation.
- [x] Add live open-position monitoring.
- [x] Add sell, cancel, update, and update-history actions.
- [x] Add activity, statement, and profit-table screens.

## Phase 2 — Accounts, wallets, and funding

### Options account setup

- [x] Implement options account creation.
- [x] Implement demo-balance reset.
- [x] Keep account WebSocket OTP acquisition working for demo and real accounts.
- [x] Display account type, currency, status, jurisdiction, and readiness.
- [x] Provide clear switching between demo and real accounts.
- [x] Prevent real trading until required onboarding and risk acknowledgement are complete.

### OAuth payment scope

- [x] Add the Deriv `payment` scope only when wallet/payment-agent workflows are ready.
- [x] Document why existing users must reconnect to grant the new scope.
- [x] Detect missing payment permission and present a reconnect action.
- [x] Preserve all required scopes when refreshing access tokens.

### Wallets

- [x] Implement wallet listing and balances.
- [x] Implement wallet transactions by wallet type.
- [x] Add wallet screens to the web app.
- [x] Add wallet screens to Android.
- [x] Add pagination, filtering, empty states, and errors.
- [x] Reconcile wallet transactions with relevant activity where possible by refreshing the selected wallet after tracked payment-agent operations settle.

### Payment agents

- [x] Implement payment-agent listing.
- [x] Implement payment-agent details.
- [x] Implement payment-agent statistics.
- [x] Implement client settings retrieval.
- [x] Implement client settings updates.
- [x] Implement transfer initiation.
- [x] Implement transfer-status tracking.
- [x] Implement withdrawal verification-code requests.
- [x] Implement withdrawal initiation.
- [x] Implement withdrawal-status tracking.
- [x] Replace the current funding HTTP 501 placeholders.
- [x] Add provider request-ID idempotency, OTP handling, audit logs without OTP leakage, bounded amounts, status polling, and failure recovery.
- [x] Add clear compliance and risk notices around third-party payment agents.

## Phase 3 — Automated and advanced trading

### Automated trading

- [x] Implement strategy listing.
- [x] Implement automation-run listing.
- [x] Implement retrieval of one automation run.
- [x] Implement automation start.
- [x] Implement automation pause.
- [x] Implement automation resume.
- [x] Implement automation stop.
- [x] Build strategy configuration forms on web and Android.
- [x] Support explicit demo/real account selection through the selected strategy account.
- [x] Display live run status, execution counts, settled profit/loss, committed loss budget, and errors; continue reconciling unsettled contracts after a run stops.
- [x] Store immutable run snapshots, run history, trade audits, and automation events.
- [x] Enforce account stake/daily/session loss limits plus run trade, loss, duration, and concurrency limits.
- [x] Add an emergency kill switch that permanently stops every open run for the user.
- [x] Require stronger confirmation for real-account automation.
- [x] Implement restart-safe execution leases, durable next-run state, and unresolved-order reconciliation gates.

### Bulk purchase

Implementation note (2026-08-17): the current Deriv REST endpoints require raw per-account Personal Access Tokens and explicitly reject the OAuth grants used by Synex. The transport must remain operator-only and is pending explicit product-owner authorization to accept and transmit those credentials.

- [ ] Implement demo bulk contract purchase.
- [ ] Implement real bulk contract purchase.
- [ ] Define which user or operator roles may use bulk purchase.
- [ ] Validate every proposal and account before submission.
- [ ] Add idempotency and partial-failure handling.
- [ ] Display per-contract success/failure results.
- [ ] Add audit logs and real-money confirmation controls.

## Phase 4 — Migration, system, and operator capabilities

### Legacy options migration

- [x] Implement legacy migration status.
- [x] Implement legacy account listing.
- [x] Implement legacy statement retrieval.
- [x] Explain the temporary/legacy nature of these endpoints in the UI.
- [x] Remove the feature cleanly when Deriv retires the APIs. The backend handler and the isolated web/Android legacy screens can be removed without affecting current account activity.

### System operations

- [x] Keep WebSocket `ping` operating as internal connection maintenance.
- [x] Keep `forget` and `forget_all` operating as internal subscription cleanup.
- [x] Implement server time.
- [x] Implement trading times.
- [x] Implement Deriv REST health monitoring.
- [x] Surface relevant upstream outages without leaking internal details.
- [x] Add operational alerts for repeated Deriv connection and schema errors.

### Account and application operations

- [x] Implement account nickname retrieval.
- [x] Implement application markup statistics.
- [x] Use a separate application-owner credential for `application_read` operations. Deployment uses the server-only `DERIV_APPLICATION_READ_TOKEN`.
- [x] Never expose operator credentials to web or Android clients.
- [x] Add an authorized operator dashboard for application statistics. `/operations` accepts the separate Synex operations key; the Deriv owner credential remains backend-only.

## Phase 5 — Schema maintenance and release discipline

### Schema management

- [ ] Store the pinned schema release in configuration or repository tooling.
- [ ] Generate or validate Go request/response types from the official schemas.
- [ ] Generate or validate TypeScript contracts from the backend/OpenAPI contract.
- [ ] Generate or validate Kotlin DTOs from the backend/OpenAPI contract.
- [ ] Add CI detection for new, changed, and removed Deriv operations.
- [ ] Fail CI when consumed response fields drift incompatibly.
- [ ] Remove or clearly mark stale types such as old multi-account, copy-trading, website-status, settings, and `get_limits` definitions.

### Capability register

- [ ] Create a register containing all 30 WebSocket request types.
- [ ] Create a register containing all 27 REST operations.
- [ ] Assign each operation one status: user-facing, operator-facing, internal-only, intentionally unsupported, or pending.
- [ ] Link every supported operation to its backend handler and user-facing workflow.
- [ ] Link user-facing operations to web and Android screens.
- [ ] Record a reason and review date for every intentionally unsupported operation.
- [ ] Update the register whenever the pinned Deriv schema changes.

### Release operations

- [ ] Document production Auth0, Deriv, Vercel, Render, database, CORS, and redirect configuration.
- [ ] Add rollback instructions for backend, web, Android, and schema changes.

## Definition of complete

Synex can claim full current Deriv API coverage only when all of the following are true:

- [ ] Every current REST and WebSocket operation is accounted for in the capability register.
- [ ] Every internal transport operation is implemented and monitored.
- [ ] Every user-facing operation has a complete backend workflow and at least one supported client experience.
- [ ] Every operator operation has role enforcement and isolated credentials.
- [ ] All available option contract families can be correctly configured, proposed, purchased, monitored, and settled.
- [ ] Real-money workflows use explicit confirmation, limits, audit logs, idempotency, and recovery.
- [ ] Web and Android parity is documented, including every intentional difference.
- [ ] Funding, wallet, and automation operations meet security and compliance requirements.
- [ ] No known current-schema incompatibility remains in Go, TypeScript, or Kotlin models.

## Tracking notes

When ticking off an item:

1. Link the implementation commit or pull request beside the checkbox when available.
2. Mark implementation items complete when the functionality is present in the relevant clients/backend.
3. Manual web and Android testing is handled separately by the product owner.
4. If an API is intentionally excluded, move it to the capability register with the reason, owner, and review date.
5. Work through Phase 0 first, then continue through the Deriv capability phases in order.
