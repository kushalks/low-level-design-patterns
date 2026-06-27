# low-level-design-patterns

A collection of low-level design (LLD) problems solved in Java — built for
interview preparation (SDE-3 / Senior Software Engineer at product companies).

## Designs

| Design | Folder | Highlights |
|--------|--------|-----------|
| E-Commerce Application (Amazon-style) | [ecommerce-lld/](ecommerce-lld/) | Strategy, State, Observer, Factory, Builder patterns; SOLID; concurrency-safe inventory. Runnable end-to-end demo. |

## Running a design

Each folder is self-contained with its own `README.md`. Generally:

```bash
cd <design-folder>
javac -d out $(find src -name "*.java")
java -cp out com.ecommerce.Main   # entry point varies per design
```

See the folder's README for the interview walkthrough, requirements, class
design, and pattern rationale.
