# Parking Lot — Low-Level Design (Java)

A small, runnable LLD of a multi-level parking lot, sized for a **30–45 min LLD
interview**. Deliberately minimal: 3 levels, 3 vehicle types, spot-fit rules, and
a pluggable fee strategy.

---

## 1. How to run

Requires a JDK (8+).

```bash
cd parking-lot-lld
javac -d out $(find src -name "*.java")
java -cp out com.parkinglot.Main
```

Demo: builds a 3-level lot → parks a motorcycle / car / truck → exits one with a
fee → fills the LARGE spots to show full-lot rejection.

---

## 2. Requirements

**Functional**
- Park a vehicle; get a ticket. Unpark with the ticket; pay a fee.
- 3 levels, each with a mix of SMALL / MEDIUM / LARGE spots.
- Vehicle types: MOTORCYCLE, CAR, TRUCK — each fits certain spot sizes.
- Reject when the lot is full for that vehicle type.

**Non-functional**
- Two cars must never get the same spot (basic thread-safety).
- Easy to add a new vehicle type, spot size, or fee scheme.

**Out of scope** (say so): payments gateway, reservations, license-plate OCR,
multiple entry/exit gates, persistence.

---

## 3. Core entities

```
ParkingLot ──*──> ParkingLevel ──*──> ParkingSpot ──> SpotType (SMALL/MEDIUM/LARGE)
Vehicle ──> VehicleType (MOTORCYCLE/CAR/TRUCK)
Ticket  ──> Vehicle + ParkingSpot + entryTime
FeeStrategy (interface) ──> HourlyFeeStrategy
```

**The fit rule** (the one bit of real logic): each `SpotType` declares which
vehicle types it accepts via an `EnumSet`:

| Spot | Fits |
|------|------|
| SMALL | Motorcycle |
| MEDIUM | Motorcycle, Car |
| LARGE | Motorcycle, Car, Truck |

Keeping this on the enum (data-driven) means a new vehicle/spot type is a
localized change, not scattered `if/else`.

---

## 4. Design patterns & principles

| Pattern / principle | Where | Why |
|---------------------|-------|-----|
| **Strategy** | `FeeStrategy` / `HourlyFeeStrategy` | Swap flat / hourly / weekend pricing without touching the lot. |
| **Facade** | `ParkingLot` | Simple `park()` / `unpark()` over levels + spots. |
| **Encapsulated state** | `ParkingSpot.park()` is `synchronized` | The spot itself guards its own occupancy. |
| **Open/Closed** | fit rules on `SpotType` enum | Add a vehicle type → edit one enum, not the algorithm. |

---

## 5. The one concurrency question

> *"Two cars try to take the last spot at the same time — what happens?"*

`ParkingSpot.park()` is `synchronized` and does the check-and-claim atomically:

```java
public synchronized boolean park(Vehicle vehicle) {
    if (!canPark(vehicle)) return false; // already taken or doesn't fit
    this.vehicle = vehicle;              // claim it
    return true;
}
```

So only one thread succeeds; the other gets `false` and the level simply tries the
next spot. The orchestration in `ParkingLevel`/`ParkingLot` stays lock-free because
each spot guards itself.

*If asked to scale to a distributed lot:* move the spot state to a DB row and use
an atomic conditional update (`UPDATE spot SET vehicle=? WHERE id=? AND vehicle IS NULL`)
or a Redis lock — same idea, just a wider lock scope.

---

## 6. Package layout

```
src/com/parkinglot/
├── Main.java                 # runnable demo
├── ParkingLot.java           # facade: park / unpark / fee
├── model/                    # Vehicle, VehicleType, SpotType, ParkingSpot, ParkingLevel, Ticket
└── fee/                      # FeeStrategy (Strategy) + HourlyFeeStrategy
```

---

## 7. Where to take it next

- **Spot assignment strategy**: nearest-to-entrance instead of first-fit
  (another Strategy).
- **Multiple gates**: entry/exit gate objects; display boards per level (Observer).
- **Reservations / EV charging spots** as new `SpotType`s.
- **Persistence + distributed locking** for a real multi-server deployment.
```
