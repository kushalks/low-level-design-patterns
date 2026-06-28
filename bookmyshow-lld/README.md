# BookMyShow / Ticket Booking Platform — Low-Level Design (Java)

A compact, runnable LLD of a movie-ticket booking platform (BookMyShow / Fandango
style), built for a **1-hour SDE-3 / Senior Engineer / MTS design interview**.

The centerpiece is **concurrent seat booking** — the problem of never letting two
users book the same seat. The demo includes a live race (10 threads fighting for
one seat) and the code is verified to let exactly one win.

---

## 1. How to run

Requires a JDK (8+).

```bash
cd bookmyshow-lld
javac -d out $(find src -name "*.java")
java -cp out com.bookmyshow.Main
```

The demo: builds a theatre/screen/show → a normal booking → then fires **10
threads at the same seat** and shows that exactly one succeeds.

---

## 2. Interview flow (suggested 60-min walkthrough)

| Time | Topic |
|------|-------|
| 0–5 min | Clarify requirements & scope |
| 5–12 min | Core entities (the Show vs ShowSeat insight) |
| 12–25 min | Class design + the booking workflow |
| 25–40 min | **Concurrency: seat locking** (the main event) |
| 40–50 min | Patterns (Strategy / Factory / Observer), SOLID |
| 50–60 min | Distributed scaling + trade-offs |

---

## 3. Requirements

**Functional**
- Search movies and shows (by city / movie / theatre)
- View seat layout & availability for a show
- Select seats, hold them while paying, confirm via payment
- Multiple payment methods; per-seat-type pricing
- Notify the user on booking confirmation

**Non-functional**
- **No double-booking** under high concurrency (the headline requirement)
- Held seats auto-release if the user abandons checkout (no seats stuck forever)
- Easy to swap the locking mechanism as we scale (in-memory → Redis → DB)
- Thread-safe shared state

**Out of scope** (say this aloud): auth, real payment gateway, persistence,
recommendations, dynamic pricing engine, multi-region. Good "next steps" material.

---

## 4. Core entities — the key modelling insight

The insight interviewers look for: **separate the static layout from per-show state.**

```
City ──*──> Theatre ──*──> Screen ──*──> Seat        (static layout, never changes)
                                            │
Movie ──*──> Show ───────────────> ShowSeat ┘        (per-show: status + price)
                                    │
User ──*──> Booking ──*──> seatIds  ┘
```

- **Seat** = a physical chair (row A, seat 5). Doesn't know if it's booked.
- **ShowSeat** = that seat *for one specific show* — carries `AVAILABLE/BOOKED`
  and the price. The same seat is free for the 6pm show and booked for the 9pm.

This separation is why one movie hall can run many shows without duplicating seats.

---

## 5. The concurrency story (THE main event)

### The race
Two users click seat **C5** at the same millisecond. Naively:

```
read "C5 available?"  ->  (other thread sneaks in)  ->  write "C5 booked"
```

Both read `AVAILABLE`, both book → **double-booked seat**. The fix everywhere is
the same idea: make check-and-set **atomic**, and re-verify durable state under
the lock.

### How this design solves it — two layers

**Layer 1 — a short-lived seat lock (`SeatLockProvider`).**
When you select seats, we place a **time-boxed lock** (default 5 min) on each.
`InMemorySeatLockProvider.acquireOne()` uses `ConcurrentHashMap.compute()`, which
holds the lock on that one seat's bin while the lambda runs — so "is it locked? →
if free, take it" is a single atomic step. Different seats never block each other.

The lock has an **expiry**, which makes the system self-healing: if a user
abandons checkout, the hold simply expires and the seat frees up — no cron, no
stuck seats. Locks are also **all-or-nothing**: booking A1+A2 either locks both or
rolls back, so we never strand a half-reservation.

**Layer 2 — double-checked locking before confirming.**
A lock holds the seat *during* checkout, but the durable truth is
`ShowSeat.status`. After acquiring the lock we **re-read the status under the
lock** and only then set it to `BOOKED`. (See `BookingService.book` step 3.)

### A real bug this design caught (great story to tell)
The first version checked availability *before* locking, then trusted it. Running
the 10-thread race repeatedly produced an occasional **2 successes**:

```
Thread B: reads C5 = AVAILABLE
Thread A: (holds lock) sets C5 = BOOKED, releases lock
Thread B: now acquires the freed lock, never re-checks  -> books a booked seat
```

The lock alone wasn't enough — the pre-check read was stale by the time the lock
was acquired. Fix: **re-verify status after acquiring the lock** (double-checked
locking). After that, 10/10 race runs give exactly 1 success. Telling this story
shows you understand *why* the lock is necessary but not sufficient.

### Scaling it out (the SDE-3 differentiator)
`SeatLockProvider` is an interface precisely so the locking strategy can change
without touching booking logic:

| Approach | How | When |
|----------|-----|------|
| **In-memory** (this repo) | `ConcurrentHashMap.compute()` | single JVM only |
| **Redis distributed lock** | `SET show:seat <user> NX PX 300000` (atomic set-if-absent with TTL) | many app servers; TTL = auto-expiry for free |
| **DB pessimistic** | `SELECT ... FOR UPDATE` on the show_seat row, then update | strong consistency, hot-row contention serializes |
| **DB optimistic** | `version` column; `UPDATE ... WHERE version=?`; retry on 0 rows | low contention, no blocking |

> One-liner to say: *"The principle is identical at every layer — fuse
> check-and-set into one atomic op and re-verify durable state — only the scope of
> the lock changes: a map bin, a Redis key, or a DB row."*

---

## 6. Design patterns used

| Pattern | Where | Why |
|---------|-------|-----|
| **Strategy** | `SeatLockProvider`, `PricingStrategy`, `PaymentStrategy` | Swap locking mechanism / pricing rules / payment methods without touching booking. |
| **Factory** | `PaymentFactory` | Centralised creation; callers depend on the abstraction. |
| **Observer** | `BookingObserver` (email) | Decoupled notification channels. |
| **Dependency Injection** | `BookingService` constructor | Lock provider & observers injected → mockable, swappable. |

---

## 7. SOLID highlights

- **S** — `ShowService` (setup/search) vs `BookingService` (workflow) vs
  `SeatLockProvider` (concurrency) are cleanly separated.
- **O** — new payment / pricing / lock backend = a new class, no edits to callers.
- **L** — any `SeatLockProvider` is substitutable behind the interface.
- **I** — small, focused interfaces.
- **D** — `BookingService` depends on the `SeatLockProvider` abstraction, not
  the in-memory implementation.

---

## 8. Package layout

```
src/com/bookmyshow/
├── Main.java                  # runnable demo incl. 10-thread seat race
├── model/                     # City, Movie, Theatre, Screen, Seat, Show, ShowSeat, Booking, User
├── lock/                      # SeatLockProvider (Strategy) + InMemory impl, SeatLock  <-- concurrency core
├── pricing/                   # PricingStrategy
├── payment/                   # PaymentStrategy + Factory
├── notification/              # BookingObserver (Observer)
└── service/                   # ShowService, BookingService (orchestrator)
```

---

## 9. Where to take it next (closing talking points)

- **Distributed locks**: implement `RedisSeatLockProvider` (SET NX PX) for
  multi-server correctness; the interface already allows it with zero ripple.
- **Lock expiry sweeper**: background reaper / rely on Redis TTL so abandoned
  carts free seats automatically.
- **Idempotency**: idempotency key on `book()` so a retried request doesn't
  double-charge or double-book.
- **Payment robustness**: async confirmation/webhooks; on timeout, release seats.
- **Search at scale**: Elasticsearch for movie/show search; cache seat maps.
- **Waitlist / notify-when-available**, group booking, seat recommendations.
```
