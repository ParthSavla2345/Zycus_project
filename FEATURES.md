# StockPulse — Comprehensive Feature & Implementation Guide

## 1. Executive Summary

**StockPulse** is an autonomous inventory replenishment and dynamic pricing platform designed for retail merchandisers. It operates on an **event-driven agentic loop** where sales events trigger background signal analysis, generating synchronized pricing and reorder recommendations for human approval (**Human-in-the-Loop**).

---

## 2. Core Functional Features

### A. Autonomous Agentic Order Processing
- **Instant Non-Blocking Response**: When `POST /products/{id}/orders` is invoked, the order reduces inventory and boosts demand velocity in <10ms.
- **Signal Detection**:
  - `INVENTORY_LOW`: Triggered when `stockLevel < reorderThreshold`.
  - `DEMAND_SPIKE`: Triggered when product `demandVelocity > 2 * categoryAverageVelocity`.
- **Spring ApplicationEvent + ThreadPool**: Publishes `StockSignalEvent` to an asynchronous thread pool executor (`StockSignalListener`) without blocking the customer checkout response.

### B. Dual Synchronized Recommendations
When an event signal fires, the system creates **BOTH** recommendations simultaneously:
1. **Pricing Recommendation**:
   - `INVENTORY_LOW` Rule: Applies a **+10%** price increase to protect scarce inventory and maximize gross margin.
   - `DEMAND_SPIKE` Rule: Applies a **+5%** price increase to capture willingness-to-pay while preserving volume.
   - Normal/Healthy: Recommends `HOLD` (0% change).
2. **Reorder Recommendation**:
   - Replenishment Formula: `recommendedQuantity = max(1, (reorderThreshold * 3) - currentStock)`.
   - Default Lead Time: 7 days.

### C. Human-in-the-Loop State Mutation (Safety Guarantee)
- **AI/Rules NEVER Mutate Products Automatically**: Recommendations are strictly saved in `status = PENDING`.
- **Accept Pricing**: Invoking `PATCH /pricing-suggestions/{id}` with `{"action": "ACCEPT"}` updates `Product.currentPrice` to `recommendedPrice` and marks suggestion `ACCEPTED`.
- **Reject Pricing**: Marks suggestion `REJECTED` with zero product mutation.
- **Accept Reorder**: Invoking `PATCH /reorder-suggestions/{id}` with `{"action": "ACCEPT"}` adds `recommendedQuantity` to `Product.stockLevel` and marks suggestion `ACCEPTED`.
- **Reject Reorder**: Marks suggestion `REJECTED`.
- **Conflict Prevention**: Re-resolving an already `ACCEPTED` or `REJECTED` suggestion returns `409 Conflict`.

### D. Zero-Restart Runtime Strategy Switching (AI vs. RULE)
- **Live State Switch**: Thread-safe `AtomicReference<StrategyType>` in `StrategyConfig`.
- **API**: `GET /admin/strategy` and `PUT /admin/strategy` (`{"strategy": "AI"}` or `{"strategy": "RULE"}`).
- **UI Header Switcher**: Switching the strategy dropdown in the top header affects all subsequent recommendations **instantly without restarting the Spring Boot JVM**.
- **Badges**: Suggestion cards display whether the proposal was created via `AI` or `RULE`.

### E. AI Resilience & Fallback Engine
- **LiteLLM / OpenAI-Compatible Client**: Calls `/v1/chat/completions` using credentials from environment variables (`LLM_BASE_URL`, `LLM_API_KEY`, `LLM_MODEL`).
- **Context-Specific Prompts**:
  - `INVENTORY_LOW`: Instructs model to prioritize inventory protection and compute restocking.
  - `DEMAND_SPIKE`: Instructs model to capitalize on velocity surge while staying competitive.
- **Strict Validation**: Price must be positive, quantity >= 1, confidence between 0.0 and 1.0, direction valid enum.
- **Zero-Downtime Fallback**: If LiteLLM times out, disconnects, or returns malformed JSON, `AiCommerceAdvisor` catches `LlmException` and **immediately executes `RuleBasedCommerceAdvisor`**, persisting valid suggestions tagged with `strategyUsed = RULE`.

### F. Database Idempotency
- `SuggestionService` queries `findByProductIdAndTriggerReasonAndStatus(productId, triggerReason, PENDING)`.
- If an unresolved `PENDING` suggestion already exists for that product and trigger, duplicate creation is suppressed.

### G. Interactive Merchandising Dashboard
- **Live 3-Second Polling**: Dashboard auto-refreshes pending recommendations and stock status.
- **Product Catalog Management**:
  - Filter by Category (`ELECTRONICS`, `APPAREL`, `HOME`).
  - Search by Name / SKU in real time.
  - Stock Health Indicators (`Healthy`, `Low Stock`, `Out of Stock`).
  - **"Simulate Sale"** modal with pre-warning if order drops below threshold.
  - **"+ Add Product"** modal for on-the-fly catalog creation with full validation.
  - **"Pricing"** and **"Reorder"** manual trigger buttons.
- **Action Cards**: Visual price difference % badges, confidence progress, and instant Accept / Reject buttons.

---

## 3. Database Persistence (SQLite)

- **Driver**: `sqlite-jdbc` 3.45.2.0 + `hibernate-community-dialects`.
- **File**: `backend/stockpulse.db` created automatically on disk.
- **Catalog Seeding**: 28 diverse products pre-seeded on startup across 3 categories.

---

## 4. REST API Reference

| Method | Endpoint | Payload / Params | Response | Description |
|---|---|---|---|---|
| `GET` | `/products` | `?category=APPAREL&search=t-shirt` | `ProductResponse[]` | List catalog products |
| `GET` | `/products/{id}` | - | `ProductResponse` | Get product details |
| `POST` | `/products` | `CreateProductRequest` JSON | `ProductResponse` (201) | Create new product |
| `PATCH` | `/products/{id}/stock` | `{"stockLevel": 50}` | `ProductResponse` | Update stock level |
| `POST` | `/products/{id}/orders` | `{"quantity": 2}` | `ProductResponse` | Process order (triggers async loop) |
| `POST` | `/products/{id}/suggest-pricing`| - | `PricingSuggestionResponse` | Manual pricing proposal |
| `POST` | `/products/{id}/suggest-reorder`| - | `ReorderSuggestionResponse` | Manual reorder proposal |
| `GET` | `/pricing-suggestions` | `?status=PENDING` | `PricingSuggestionResponse[]`| List pricing suggestions |
| `PATCH`| `/pricing-suggestions/{id}` | `{"action": "ACCEPT"}` | `PricingSuggestionResponse`| Accept or reject pricing |
| `GET` | `/reorder-suggestions` | `?status=PENDING` | `ReorderSuggestionResponse[]`| List reorder suggestions |
| `PATCH`| `/reorder-suggestions/{id}` | `{"action": "ACCEPT"}` | `ReorderSuggestionResponse`| Accept or reject reorder |
| `GET` | `/admin/strategy` | - | `{"strategy": "AI"}` | Get active strategy |
| `PUT` | `/admin/strategy` | `{"strategy": "RULE"}` | `{"strategy": "RULE"}` | Switch strategy live |
