# StockPulse — AI Inventory & Dynamic Pricing

StockPulse is an autonomous retail merchandising platform that monitors inventory run-rates and demand velocity spikes to generate real-time dynamic pricing adjustments and replenishment orders.

---

## 🏗 System Architecture

```
                               +-----------------------------+
                               | React + TS + Vite Dashboard |
                               |   (Port 5174, Live Polling) |
                               +--------------+--------------+
                                              | HTTP / REST (3s auto-refresh)
                                              v
+-------------------------------------------------------------------------------------------+
| Spring Boot 3 Monolith Backend (Port 8080)                                                |
|                                                                                           |
|  [ Product Controller ] <---------> [ Suggestion Controller ] <-----> [ Admin Controller ]|
|          |                                     |                              |           |
|          v                                     v                              v           |
|  [ Product Service ]               [ Suggestion Service ]            [ Strategy Config ]  |
|          | (On Order Event)                    |                              |           |
|          +------------+                        |                              |           |
|                       v                        v                              v           |
|            [ Signal Detector ]        (Idempotency Check)        [ CommerceAdvisorFactory]|
|             - Stock < Threshold?               |                              |           |
|             - Velocity > 2x Avg?               |                              v           |
|                       |                        |                     +-----------------+  |
|                       v                        |                     | CommerceAdvisor |  |
|            [ Spring Event Bus ]                |                     +--------+--------+  |
|                       |                        |                              |           |
|                       v                        |               +--------------+-------+   |
|            [ StockSignalListener ]             |               |                      |   |
|                (@Async Pool)                   |               v                      v   |
|                       +------------------------+      [ AiCommerceAdvisor ]  [ RuleAdvisor]|
|                                                               |                      ^    |
|                                                               | (On Any Failure)     |    |
|                                                               +----------------------+    |
+---------------------------------------------------------------|---------------------------+
                                                                | /v1/chat/completions
                                                                v
                                                  +-----------------------------+
                                                  | LiteLLM / OpenAI Proxy API  |
                                                  |   (Model: qwen-cursor)      |
                                                  +-----------------------------+
```

---

## ⚡ Core Features & Rules

### 1. The Autonomous Agentic Loop
- **Order Event**: Customer purchases via `POST /products/{id}/orders` reduce stock and increase demand velocity in <10ms.
- **Signal Triggers**:
  - `INVENTORY_LOW`: When `stockLevel < reorderThreshold`.
  - `DEMAND_SPIKE`: When product `demandVelocity > 2 * categoryAverageVelocity`.
- **Async Event Bus**: Publishes `StockSignalEvent` to background worker thread pool (`StockSignalListener`). The HTTP response returns immediately without blocking on LLM inference.

### 2. Strategy Engine: AI vs. RULE
The application supports two advisor modes via the `CommerceAdvisor` interface:

- **AI Mode (`AiCommerceAdvisor`)**:
  - Connects to LiteLLM OpenAI-compatible endpoint.
  - Generates combined pricing & reorder recommendations in a single JSON call.
  - Contextual prompts tailored for `INVENTORY_LOW` (protection) and `DEMAND_SPIKE` (margin capture).
  - **Fail-Safe Fallback**: If LiteLLM is offline, times out, or returns invalid JSON, it immediately runs `RuleBasedCommerceAdvisor` with `strategyUsed = RULE`.
- **Rule Mode (`RuleBasedCommerceAdvisor`)**:
  - **Pricing Rule**:
    - `IF stock < threshold`: price * 1.10 (+10%), `direction = INCREASE`.
    - `ELSE IF velocity > 2 * categoryAvg`: price * 1.05 (+5%), `direction = INCREASE`.
    - `ELSE`: current price (0%), `direction = HOLD`.
  - **Reorder Rule**:
    - `recommendedQuantity = max(1, (reorderThreshold * 3) - currentStock)`.
    - Lead time = 7 days.

### 3. Human-in-the-Loop Safeguard
- AI and rules **NEVER** mutate product state automatically.
- All proposals start in `PENDING` status.
- Clicking **"Accept Price"** updates `Product.currentPrice`.
- Clicking **"Accept Reorder"** increases `Product.stockLevel`.
- Clicking **"Reject"** marks the suggestion as rejected with zero state changes.
- Duplicate `PENDING` suggestions are prevented at the database level.

### 4. Embedded SQLite Database
- Database operations persist to **`backend/stockpulse.db`** via `sqlite-jdbc` and Hibernate.
- Pre-seeds **28 demo products** across Electronics, Apparel, and Home.
- Supports adding custom products via the frontend UI.

---

## 📋 Prerequisites

- **Java 17+** (JDK 17, 21, or newer)
- **Maven 3.8+**
- **Node.js 18+** & **npm**

---

## ⚙️ Environment Variables & Configuration

Credentials are read exclusively from environment variables (never hardcoded):

| Variable | Description | Default Value |
|---|---|---|
| `LLM_BASE_URL` | Base URL of LiteLLM / OpenAI proxy | `http://localhost:4000` |
| `LLM_API_KEY` | LiteLLM / OpenAI API Key | `changeme` |
| `LLM_MODEL` | LLM Model name | `qwen-cursor` |

---

## 🚀 How to Run (Step-by-Step)

### Step 1: Start the Backend (Port 8080)

In a PowerShell terminal:

```powershell
cd c:\Stock-pulse\backend

# Set JDK 21 compiler path
$env:JAVA_HOME="C:\Users\zycus\.vscode\extensions\redhat.java-1.55.0-win32-x64\jre\21.0.11-win32-x86_64"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

# (Optional: set custom LLM endpoint if available)
# $env:LLM_BASE_URL="http://localhost:4000"
# $env:LLM_API_KEY="your-api-key"

mvn spring-boot:run
```

The backend boots on **`http://localhost:8080`** and initializes `stockpulse.db` with 28 seed products.

---

### Step 2: Start the Frontend (Port 5174)

In a second PowerShell terminal:

```powershell
cd c:\Stock-pulse\frontend

# Install dependencies if not already done
npm.cmd install

# Start Vite dev server
npm.cmd run dev
```

Open **`http://localhost:5174`** in your browser.

---

## 🧪 Running Automated Tests

Run the full test suite verifying rule calculations, AI fallback, signal detection, and idempotency:

```powershell
cd c:\Stock-pulse\backend
$env:JAVA_HOME="C:\Users\zycus\.vscode\extensions\redhat.java-1.55.0-win32-x64\jre\21.0.11-win32-x86_64"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
mvn test
```

### Test Suite Summary (14/14 Passing):
1. **`RuleBasedCommerceAdvisorTest`**:
   - `testInventoryLowPricingIncrease`: Verifies +10% price increase on low inventory.
   - `testDemandSpikePricingIncrease`: Verifies +5% price increase on demand surge.
   - `testNormalPricingHold`: Verifies HOLD with no price mutation.
   - `testReorderCalculation`: Verifies `max(1, (threshold * 3) - stock)`.
2. **`AiAdvisorFallbackTest`**:
   - `testFallbackOnLlmFailure`: Verifies fallback to `RULE` when LLM connection fails.
   - `testFallbackOnMalformedJson`: Verifies fallback when LLM returns invalid JSON.
   - `testValidAiResponse`: Verifies structured AI JSON parsing with `strategyUsed = AI`.
3. **`ProductServiceTest`**:
   - `testInventoryLowDetectionOnOrder`: Verifies order reducing stock triggers `INVENTORY_LOW` signal.
   - `testDemandSpikeDetectionOnOrder`: Verifies surge order triggers `DEMAND_SPIKE` signal.
4. **`SuggestionServiceIntegrationTest`**:
   - `testAcceptPricingUpdatesProductPrice`: Verifies ACCEPT updates `currentPrice`.
   - `testRejectPricingLeavesPriceUnchanged`: Verifies REJECT preserves current price.
   - `testAcceptReorderIncreasesStock`: Verifies ACCEPT increases `stockLevel`.
   - `testDuplicatePendingPrevention`: Verifies duplicate PENDING suppression.
   - `testCannotReResolveSuggestion`: Verifies `409 Conflict` on re-resolving.

---

## 🎯 Verification Demo Steps

1. **Catalog View**: Open `http://localhost:5174`. View all 28 catalog items.
2. **Simulate a Sale**: Click **"Simulate Sale"** on `PRD-008` (Hoodie) and order `2` units.
3. **Observe Recommendations**: Notice the 2 cards appear under **Pending Action Recommendations** within 1–2 seconds via live 3s polling.
4. **Accept / Reject**:
   - Click **"Accept Price"** &rarr; The catalog table price updates instantly.
   - Click **"Accept Reorder"** &rarr; The catalog table stock increases by recommended quantity.
5. **Runtime Strategy Switch**: Change the top dropdown from **AI** to **RULE**. Click **Pricing** next to any product &rarr; The generated card displays the **RULE** badge without restarting the server.
6. **Add Custom Product**: Click **"+ Add Product"** in the catalog bar, enter custom details, and submit &rarr; The product appears in the table immediately.
