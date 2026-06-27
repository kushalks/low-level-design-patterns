# E-Commerce Application — Low-Level Design (Java)

A compact, runnable LLD of an Amazon-style e-commerce platform, built for a
**1-hour SDE-3 / Senior Engineer design interview**. It is deliberately scoped to
the pieces an interviewer actually probes: clean domain modelling, the right
design patterns, SOLID, and concurrency-safe inventory.

---

## 1. How to run

Requires a JDK (8+).

```bash
cd ecommerce-lld
javac -d out $(find src -name "*.java")
java -cp out com.ecommerce.Main
```

`Main` runs an end-to-end flow: browse → search → add to cart → checkout (with a
discount + payment strategy) → fulfilment lifecycle → inventory update → an
illegal-transition guard.

---

## 2. Interview flow (suggested 60-min walkthrough)

| Time | Topic |
|------|-------|
| 0–5 min | Clarify requirements & scope (below) |
| 5–10 min | Identify core entities |
| 10–25 min | Class design + relationships |
| 25–45 min | Patterns & why (Strategy, State, Observer, Factory, Builder) |
| 45–55 min | Concurrency (inventory / overselling), extensibility |
| 55–60 min | Trade-offs, what you'd add next (scaling, persistence) |

---

## 3. Requirements

**Functional**
- Browse / search products by keyword and category
- Manage a shopping cart (add / update / remove)
- Apply discounts (coupons, % off, flat off)
- Checkout with multiple payment methods
- Track order lifecycle (created → paid → shipped → delivered, or cancelled)
- Notify the customer on status changes (email / SMS)

**Non-functional**
- No overselling under concurrent checkouts (correctness)
- Easy to extend with new payment methods / discounts / notification channels
  *without modifying existing code* (Open/Closed)
- Thread-safe shared state (catalog, inventory, carts)

**Explicitly out of scope** (state this in the interview): auth, real payment
gateway integration, persistence/DB, recommendations, reviews, distributed
deployment. Mentioning these as "next steps" scores points.

---

## 4. Core entities

```
User ──1:1──> Cart ──*──> CartItem ──> Product
User ──1:*──> Order ──*──> OrderItem (price snapshot)
Product ──> Category (enum)
InventoryService: productId -> stock (kept separate from Product)
```

---

## 5. Design patterns used (and why)

| Pattern | Where | Why it earns its place |
|---------|-------|------------------------|
| **Strategy** | `PaymentStrategy`, `DiscountStrategy` | Interchangeable algorithms; add new payment/discount types without touching checkout. |
| **State** | `OrderState` + `Created/Paid/Shipped/Delivered/Cancelled` | Each state owns its legal transitions; no giant switch; illegal moves are rejected at the source. |
| **Observer** | `OrderObserver` + Email/SMS observers | Decouples notification channels from the order; add analytics/warehouse listeners freely. |
| **Factory** | `PaymentFactory` | Centralises creation; callers depend on the abstraction, not concrete classes. |
| **Builder** | `Order.Builder` | Orders have several required + optional fields; build them validated and consistent. |
| **Null Object** | `NoDiscount` | Avoids null checks — there's always a discount strategy to call. |

---

## 6. SOLID highlights (call these out)

- **S** — `Product` (metadata) vs `InventoryService` (stock) are separate concerns.
- **O** — new payment / discount / notification = a new class, no edits to `OrderService`.
- **L** — every `PaymentStrategy` / `OrderState` is substitutable.
- **I** — small, focused interfaces (`OrderObserver` has one method).
- **D** — `OrderService` depends on abstractions injected via its constructor.

---

## 7. The concurrency question (almost always asked)

> *"Two customers try to buy the last unit at the same time — how do you prevent
> overselling?"*

See `InventoryService.reserve()`. It uses `ConcurrentHashMap.compute()` to make
the **check-and-decrement atomic per product key**, so only one thread can take
the last unit. On payment failure or cancellation, stock is returned via
`release()`. `OrderService.placeOrder()` reserves all lines first and **rolls
back** partial reservations if any line is unavailable.

In a distributed system you'd push this to the database (`SELECT ... FOR UPDATE`,
optimistic locking with a version column, or an atomic decrement in Redis).

---

## 8. Package layout

```
src/com/ecommerce/
├── Main.java                  # runnable end-to-end demo
├── model/                     # domain entities (User, Product, Cart, Order, ...)
├── service/                   # use-case layer (Catalog, Inventory, Cart, Order)
├── payment/                   # Strategy + Factory for payments
├── discount/                  # Strategy for discounts
├── notification/              # Observer-based notifications
└── state/                     # State pattern for order lifecycle
```

---

## 9. Where to take it next (good closing talking points)

- **Persistence**: introduce repository interfaces; in-memory maps → JPA/SQL.
- **Coupon engine**: chain multiple discounts (Decorator), validate coupon codes.
- **Idempotency**: idempotency keys on `placeOrder` to dedupe retried requests.
- **Payment robustness**: async confirmation, webhooks, refund/partial-refund flows.
- **Search**: replace in-memory filter with Elasticsearch; add ranking & facets.
- **Scale**: shard inventory by region; event-driven fulfilment via a queue.

Concurrency(Locking Strategy):

Distributed: SELECT ... FOR UPDATE (pessimistic locking)
Now stock lives in a shared database, and multiple app servers talk to it. We push the locking down into the DB.


BEGIN;

-- Lock this product's row. Any other txn doing the same waits here.
SELECT stock FROM inventory WHERE product_id = 'P1' FOR UPDATE;

-- App checks: is stock >= 1? If yes:
UPDATE inventory SET stock = stock - 1 WHERE product_id = 'P1';

COMMIT;   -- lock released here
Plain words: FOR UPDATE tells the database "lock this row — I'm about to change it." Any other transaction that tries to SELECT ... FOR UPDATE the same row blocks and waits until the first one does COMMIT. It's the database-level equivalent of compute() locking the bucket.

Replay:

Server A: SELECT ... FOR UPDATE on P1 → gets the lock → sees 1 → updates to 0 → COMMIT.
Server B: its SELECT ... FOR UPDATE was waiting the whole time → now runs → sees 0 → refuses to sell.
It's called pessimistic because you assume a conflict will happen, so you grab the lock up front. Safe and simple. Downside: while A holds the lock, B is blocked and idle. Under heavy contention on one hot product, this serializes everyone and hurts throughput. Also risks deadlocks if you lock multiple rows in inconsistent orders.


Using ConcurrentHashMap.compute():

In your code: ConcurrentHashMap.compute()
Look at InventoryService.reserve():


stock.compute(productId, (id, current) -> {
    int available = (current == null) ? 0 : current;
    if (available >= quantity) {
        success[0] = true;
        return available - quantity;   // check + decrement together
    }
    return available;                  // unchanged
});
Plain words: ConcurrentHashMap is divided into buckets, and compute() locks the single bucket for that one productId while your lambda runs. So the "read available → check ≥ quantity → write new value" all happens while the key is locked. No other thread can touch that product's stock until your lambda finishes.

So replaying the race:

Thread A enters compute("P1") → locks the key → sees 1 → 1≥1 → writes 0 → unlocks. Returns success.
Thread B now enters compute("P1") → sees 0 (A already wrote it) → 0≥1 is false → leaves it at 0. Returns failure.
Exactly one wins. The key word is atomic: read-modify-write fused into one uninterruptible operation.

Two things to point out in an interview:

It locks per key, not the whole map — so a sale of P1 doesn't block a sale of P2. Good throughput.
The boolean[] success array is a trick to get a result out of the lambda (Java won't let a lambda assign to a plain local variable, but it can mutate an array's contents).
The catch: this only works inside one JVM / one server. The lock lives in that process's memory. Run two servers behind a load balancer and each has its own ConcurrentHashMap — they don't know about each other. That's why we go distributed.

