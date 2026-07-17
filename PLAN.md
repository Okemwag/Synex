# Synex Deriv API Completion Plan

Last audited: 2026-07-17
Official schema baseline: [`production_v20260709_1`](https://github.com/deriv-com/deriv-api-schemas/releases/latest)

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

- [ ] Implement demo purchase for each contract family.
- [x] Display a complete purchase receipt, including extended expiry, barrier, multiplier, accumulator, tick, and limit-order terms.
- [x] Show open-contract status and live profit/loss.
- [x] Support eligible early sell operations.
- [x] Support eligible cancellation operations.
- [x] Support contract updates and update history.
- [x] Show settled contract results with paid, returned, profit/loss, contract ID, and Deriv transaction ID as separate customer-visible values.
- [x] Require explicit confirmation before every real-money purchase.
- [ ] Add configurable stake, daily-loss, and session-loss limits.

### Market data completion

- [ ] Implement `contracts_list`.
- [ ] Implement server time.
- [ ] Implement trading times and market-session availability.
- [ ] Expose raw tick history as well as candle history.
- [ ] Support start and end timestamps.
- [ ] Support available history styles and granularities.
- [ ] Support history subscriptions where useful.
- [ ] Support complete pagination/query options.
- [ ] Add market-data caching, rate-limit handling, and reconnect behavior.

### Activity and reporting

- [ ] Add statement date-range filters.
- [ ] Add statement action/type filters.
- [ ] Add offset-based pagination.
- [ ] Add sorting.
- [ ] Add profit-table filters and pagination.
- [ ] Show complete transaction details.
- [ ] Reconcile live transaction events with statement and profit-table history.
- [ ] Add CSV or equivalent activity export.

### Android manual-trading parity

- [ ] Add a Trade destination.
- [ ] Add contract discovery and contract-specific ticket forms.
- [ ] Add proposal pricing and expiry handling.
- [ ] Add demo and real purchase confirmation.
- [ ] Add live open-position monitoring.
- [ ] Add sell, cancel, update, and update-history actions.
- [ ] Add activity, statement, and profit-table screens.

## Phase 2 — Accounts, wallets, and funding

### Options account setup

- [ ] Implement options account creation.
- [ ] Implement demo-balance reset.
- [ ] Keep account WebSocket OTP acquisition working for demo and real accounts.
- [ ] Display account type, currency, status, jurisdiction, and readiness.
- [ ] Provide clear switching between demo and real accounts.
- [ ] Prevent real trading until required onboarding and risk acknowledgement are complete.

### OAuth payment scope

- [ ] Add the Deriv `payment` scope only when wallet/payment-agent workflows are ready.
- [ ] Document why existing users must reconnect to grant the new scope.
- [ ] Detect missing payment permission and present a reconnect action.
- [ ] Preserve all required scopes when refreshing access tokens.

### Wallets

- [ ] Implement wallet listing and balances.
- [ ] Implement wallet transactions by wallet type.
- [ ] Add wallet screens to the web app.
- [ ] Add wallet screens to Android.
- [ ] Add pagination, filtering, empty states, and errors.
- [ ] Reconcile wallet transactions with relevant trading activity where possible.

### Payment agents

- [ ] Implement payment-agent listing.
- [ ] Implement payment-agent details.
- [ ] Implement payment-agent statistics.
- [ ] Implement client settings retrieval.
- [ ] Implement client settings updates.
- [ ] Implement transfer initiation.
- [ ] Implement transfer-status tracking.
- [ ] Implement withdrawal verification-code requests.
- [ ] Implement withdrawal initiation.
- [ ] Implement withdrawal-status tracking.
- [ ] Replace the current funding HTTP 501 placeholders.
- [ ] Add idempotency, OTP handling, audit logs, limits, status polling, and failure recovery.
- [ ] Add clear compliance and risk notices around third-party payment agents.

## Phase 3 — Automated and advanced trading

### Automated trading

- [ ] Implement strategy listing.
- [ ] Implement automation-run listing.
- [ ] Implement retrieval of one automation run.
- [ ] Implement automation start.
- [ ] Implement automation pause.
- [ ] Implement automation resume.
- [ ] Implement automation stop.
- [ ] Build strategy configuration forms.
- [ ] Support explicit demo/real account selection.
- [ ] Display live run status, trades, profit/loss, and errors.
- [ ] Store run history and audit events.
- [ ] Enforce stake, loss, duration, and concurrency limits.
- [ ] Add an emergency kill switch.
- [ ] Require stronger confirmation for real-account automation.
- [ ] Implement safe reconnect and state reconciliation after backend restarts.

### Bulk purchase

- [ ] Implement demo bulk contract purchase.
- [ ] Implement real bulk contract purchase.
- [ ] Define which user or operator roles may use bulk purchase.
- [ ] Validate every proposal and account before submission.
- [ ] Add idempotency and partial-failure handling.
- [ ] Display per-contract success/failure results.
- [ ] Add audit logs and real-money confirmation controls.

## Phase 4 — Migration, system, and operator capabilities

### Legacy options migration

- [ ] Implement legacy migration status.
- [ ] Implement legacy account listing.
- [ ] Implement legacy statement retrieval.
- [ ] Explain the temporary/legacy nature of these endpoints in the UI.
- [ ] Remove the feature cleanly when Deriv retires the APIs.

### System operations

- [ ] Keep WebSocket `ping` operating as internal connection maintenance.
- [ ] Keep `forget` and `forget_all` operating as internal subscription cleanup.
- [ ] Implement server time.
- [ ] Implement trading times.
- [ ] Implement Deriv REST health monitoring.
- [ ] Surface relevant upstream outages without leaking internal details.
- [ ] Add operational alerts for repeated Deriv connection and schema errors.

### Account and application operations

- [ ] Implement account nickname retrieval.
- [ ] Implement application markup statistics.
- [ ] Use a separate application-owner credential for `application_read` operations.
- [ ] Never expose operator credentials to web or Android clients.
- [ ] Add an authorized operator dashboard for application statistics.

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
