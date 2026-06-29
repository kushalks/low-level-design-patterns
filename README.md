# low-level-design-patterns

A collection of low-level design (LLD) problems solved in Java — built for
interview preparation (SDE-3 / Senior Software Engineer at product companies).

## Designs

| Design | Folder | Highlights |
|--------|--------|-----------|
| E-Commerce Application (Amazon-style) | [ecommerce-lld/](ecommerce-lld/) | Strategy, State, Observer, Factory, Builder patterns; SOLID; concurrency-safe inventory. Runnable end-to-end demo. |
| BookMyShow / Ticket Booking | [bookmyshow-lld/](bookmyshow-lld/) | Concurrent seat booking (no double-booking); time-boxed seat locks + double-checked locking; Strategy/Factory/Observer; SOLID. Runnable demo with a 10-thread seat race. |
| Parking Lot | [parking-lot-lld/](parking-lot-lld/) | Small/interview-sized: 3 levels, 3 vehicle types, EnumSet spot-fit rules, Strategy fee, synchronized spot claim. Runnable demo. |

## Running a design

Each folder is self-contained with its own `README.md`. Generally:

```bash
cd <design-folder>
javac -d out $(find src -name "*.java")
java -cp out com.ecommerce.Main   # entry point varies per design
```

See the folder's README for the interview walkthrough, requirements, class
design, and pattern rationale.
