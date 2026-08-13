# Project Requirements and Implementation Matrix

This document separates requirements that are implemented and working, implemented but broken, simulated, or documented-only — verified against source.

## Functional requirements

| Requirement | Implementation |
|---|---|
| List ECRs | Vue: working. JSP `/admin/ecrs`: **broken** (see notes) |
| Search ECRs | Implemented in Vue |
| Filter by status | Implemented in Vue; also in JSP list markup/JS, but the JSP list route itself is broken |
| Create ECR | Implemented in Vue + REST backend |
| Generate ECR ID | Backend (`ECRRepository.save()`) |
| Initial status Draft | Backend |
| Generate creation date | Backend |
| View ECR details | Vue: working (no linked parts). JSP `/admin/ecr?id=`: working, but against isolated hardcoded data, not the live REST repository |
| Change status | Implemented (Vue → REST) |
| Validate status transitions | Backend (`ECRTriggerJPO` + `adminObjects.xml`) |
| Display linked parts | Implemented in JSP detail (`/admin/ecr?id=`) only, against seeded demo parts |
| Part API | Implemented, with a servlet-init-order hazard (see notes) |
| Supplier synchronization | Implemented through external REST API (FakeStoreAPI) |
| JSP admin screen | Present in code; `/admin/ecrs` list route broken, `/admin/ecr` detail route working |
| JSP JSTL | Implemented (used in both JSPs) |
| JSP EL | Implemented (used in both JSPs) |
| Servlet MVC | Implemented for `/admin/ecr`; implemented but non-functional for `/admin/ecrs` |

## PLM/enterprise concepts

| Concept | Status | Notes |
|---|---|---|
| Admin Objects | Implemented as XML configuration | `adminObjects.xml` |
| JPO | Implemented as Java JPO-style simulation | `ECRTriggerJPO.java` |
| Trigger | Implemented through Java validation | `validateTransition()` |
| MQL | Simulated with Java Streams | `SimulatedMQL.java` |
| TCL | Simulated with standalone TCL, not invoked by Java | `tcl/ecr_trigger.tcl` |
| SOAP | Documentation only | `docs/SOAP-Integration.md` |
| ENOVIA/3DEXPERIENCE | Not connected | — |

## Not currently implemented

- Database persistence
- Authentication
- Authorization
- Role-based access
- Audit trail
- Real ENOVIA integration
- Real MQL
- Real JPO deployment
- Real TCL trigger deployment
- SOAP endpoint
- Vue Parts page
- Vue Supplier page

## Implemented but currently broken (verified defects)

- `GET /admin/ecrs` (JSP admin list) — dispatcher points to a nonexistent path and the JSP reads an attribute the servlet never sets. See `docs/REPOSITORY-AUDIT.md`.
- `GET /admin/ecrs/{id}` (JSP admin detail via `ECRAdminServlet`) — same dispatcher problem, plus `linkedParts` is never populated.
- `GET /api/parts` — returns HTTP 500 if called before `POST /api/parts/sync` has run at least once, due to servlet initialization order.

## Important distinction

The project should be presented as a **working Java/Vue web application with PLM concepts demonstrated through configuration and simulations**, with two known-broken JSP admin routes and one servlet-ordering hazard, not as a real ENOVIA/3DEXPERIENCE implementation and not as a fully working JSP admin module.
