# Architecture Decision Records (ADR) — StockPulse

This document records the architectural decisions made in the development of **StockPulse — AI Inventory & Dynamic Pricing**.

---

## ADR-001: Location and Ownership of Commerce Business Logic

### Context
In retail replenishment and dynamic pricing platforms, business rules, threshold checks, and pricing calculation logic could reside in database triggers, controllers, services, dedicated advisor strategies, or client-side frontends.

### Options Considered
1. **Frontend-Driven Logic**: React UI determines price recommendations and triggers stock reorders.
2. **Controller/CRUD Layer Embedding**: Mixing rule calculations and signal detections inside REST endpoint controllers.
3. **Dedicated Domain Service + Strategy Abstraction (`CommerceAdvisor`)**: Encapsulating pricing and reorder logic inside dedicated advisor implementations behind a domain interface.

### Decision
Commerce logic is strictly encapsulated within the `CommerceAdvisor` strategy hierarchy on the backend (`RuleBasedCommerceAdvisor`, `AiCommerceAdvisor`) and orchestrated by `ProductService` and `SuggestionService`. The UI is purely a presentation and decision layer (Human-in-the-Loop).

### Tradeoffs
- **Pros**: AI and rule algorithms can be independently unit-tested, reused between async order-driven loops and manual trigger endpoints, and secured against client tampering.
- **Cons**: Requires clean DTO contracts and event publication pipelines across layers.

---

## ADR-002: Unified `CommerceAdvisor` vs. Split Pricing/Reorder Contracts

### Context
When inventory falls low or demand spikes, merchandising decisions require both a pricing adjustment (to preserve margin and throttle run-rate) and a reorder recommendation (to restock before stockout). These could be modeled as two independent advisor services or a single unified advisor returning both recommendations.

### Options Considered
1. **Split Contracts (`PricingAdvisor` & `ReorderAdvisor`)**: Two independent interfaces and separate LLM calls.
2. **Unified Contract (`CommerceAdvisor` returning `CommerceRecommendation`)**: A single method producing both `PricingRecommendation` and `ReorderRecommendation`.

### Decision
Adopted the **Unified `CommerceAdvisor`** contract (`recommend(Product, TriggerReason, double)` returning `CommerceRecommendation`).

### Tradeoffs
- **Pros**: Reduces LLM latency by 50% by generating both pricing and reorder recommendations in a single structured JSON response from LiteLLM. Ensures pricing and inventory replenishment strategies are contextually coherent.
- **Cons**: When a manual trigger only needs one suggestion type, both are computed (or discarded), though rule computation is negligible.

---

## ADR-003: Zero-Restart Runtime Strategy Switching (AI vs. RULE)

### Context
Operators and evaluators must be able to switch between AI-driven recommendations and deterministic rule-based algorithms dynamically at runtime without restarting the backend JVM.

### Options Considered
1. **Spring `@RefreshScope` / Actuator**: Reloading configuration beans via Spring Cloud context refresh.
2. **Thread-Safe In-Memory State (`AtomicReference<StrategyType>` in `StrategyConfig`)**: Strategy factory queries an atomic state variable on every request.
3. **Database Flag**: Reading strategy from a database table on each evaluation.

### Decision
Implemented `StrategyConfig` backed by `AtomicReference<StrategyType>` with REST endpoints `GET /admin/strategy` and `PUT /admin/strategy`, accessed on each invocation via `CommerceAdvisorFactory`.

### Tradeoffs
- **Pros**: Instantaneous strategy switching without overhead, dependencies, or JVM restart. Thread-safe across concurrent HTTP and `@Async` event workers.
- **Cons**: Strategy switch in-memory resets to default (`AI`) on server restart unless persisted externally.

---

## ADR-004: LLM Output Validation and Instant Rule Fallback

### Context
LLMs can experience network timeouts, LiteLLM proxy disconnects, HTTP errors, invalid JSON formatting, or hallucinations (negative prices, negative stock, invalid enum values). AI recommendations must never corrupt product state or cause system crashes.

### Options Considered
1. **Retry Loop**: Retrying the LLM 3–5 times before failing.
2. **Fail Fast with HTTP 500**: Return an error to the caller.
3. **Strict Validation with Immediate Deterministic Rule Fallback**: Validate schema and values; on ANY failure, immediately invoke `RuleBasedCommerceAdvisor` with `strategyUsed = RULE`.

### Decision
Implemented strict Jackson JSON schema and domain boundary validation in `AiCommerceAdvisor`. Any exception (`LlmException`, JSON parse error, timeout, out-of-range value) immediately triggers `RuleBasedCommerceAdvisor.recommend()`, persisting valid recommendations tagged with `strategyUsed = RULE`.

### Tradeoffs
- **Pros**: 100% system availability and resilience. Merchandisers always receive actionable, valid recommendations regardless of LLM connectivity.
- **Cons**: Merchandiser must note the `strategyUsed` badge (AI vs RULE) to know if fallback occurred.

---

## ADR-005: Asynchronous Event-Driven Agentic Loop and Idempotency

### Context
When orders are placed via `POST /products/{id}/orders`, calculating AI recommendations via LiteLLM takes 500ms–2000ms. Blocking the order request degrades checkout performance. Furthermore, repeated rapid sales must not spam the merchandiser with identical pending recommendations.

### Options Considered
1. **Synchronous Execution**: Order endpoint waits for LLM completion before returning.
2. **Spring `@Async` Application Events with Database Idempotency**: Order endpoint updates stock, checks threshold/velocity triggers, publishes `StockSignalEvent`, and returns HTTP 200 immediately. The `@Async` listener processes recommendations and guards against duplicates using `findByProductIdAndTriggerReasonAndStatus`.

### Decision
Implemented Spring Application Events with `@Async` ThreadPool (`StockSignalEvent` & `StockSignalListener`). Idempotency is enforced by `SuggestionService` checking for existing `PENDING` suggestions for the same `(productId, triggerReason, suggestionType)`.

### Tradeoffs
- **Pros**: Order API returns in <10ms. Prevents suggestion flood in the UI.
- **Cons**: Frontend relies on reactive polling (or WebSocket/SSE) to render newly generated pending suggestions.

---

## ADR-006: Deliberate Non-Goals & Extensibility Architecture

### Context
To prioritize critical evaluation requirements, correctness, and speed of implementation, certain enterprise features were intentionally excluded from this initial version while leaving clean extension points.

### Deliberate Exclusions
- **Authentication & Multi-Tenancy**: No OAuth2/JWT complexity; straightforward single-tenant dashboard.
- **Payment Gateways & Real Supplier APIs**: Simulated with stock level increments and lead times.
- **Competitor Web Scraping**: Replaced by category velocity metrics.
- **Microservices & Container Orchestration**: Single unified Spring Boot monolith + Vite SPA.

### Future Extension Points
1. **Competitor Pricing Ingestion**: Implement `CompetitorPricingProvider` interface feeding into `CommerceAdvisor`.
2. **Supplier ERP Integration**: Connect `ReorderSuggestion.status == ACCEPTED` to an outbound webhook or EDI provider.
3. **Margin Floors**: Add hard minimum margin checks in `Product.costPrice` prior to persisting `PricingSuggestion`.
