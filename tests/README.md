# tests/

Reserved for cross-module integration/end-to-end tests that don't belong to
a single module (e.g. full parse → edit → generate round-trips spanning
`core`, `adapter-api`, and `adapter-plantuml-c4`).

Per-module unit tests live inside each module's own `src/test/kotlin`
(see `modules/*/src/test`). This directory is intentionally empty until
Milestone 6+ introduces integration scenarios that need it.
