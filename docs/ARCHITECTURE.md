# Architecture – ECR Parts Catalog

## 1. Purpose

This document describes the architecture as implemented, verified line-by-line against the source in this repository. Where the implementation is broken or inconsistent with its own intent, that is called out explicitly rather than smoothed over — see §10.

The application has two UI paths on one WAR:

1. A Vue 3 SPA that consumes a REST API.
2. JSP administrative screens using Servlet/MVC, JSTL, and EL.

Data is in-memory (`ArrayList`-backed repositories); no database.

## 2. System context

```text
                           Browser / User
                         /               \
                        /                 \
                       v                   v
              Vue 3 SPA                 JSP Admin
                  |                         |
          Router + Pinia                  Servlet
                  |                       /     \
                Axios                    /       \
                  |                     v         v
                  |              ECR Repository  Part Repository
                  |              (see §10 — not      |
                  |               uniformly shared)   |
                  +-----------> REST                  |
                               API                     |
                                  \                    |
                                   v                    v
                                Java Backend
```

## 3. Vue architecture

```text
main.js
  └─ createApp(App)
       ├─ Pinia
       └─ Vue Router
            ├─ /            ECRListView.vue
            ├─ /ecrs/new    ECRForm.vue
            └─ /ecrs/:id    ECRDetailView.vue
```

### State/data flow

```text
ECRListView / ECRDetailView
        ↓
    ecrStore.js (Pinia)
        ↓
    services/api.js (Axios, baseURL http://localhost:8080/ecr-tracker/api)
        ↓
    ECRWebServiceServlet
```

`ECRDetailView.vue` has no dedicated GET-by-id call — it reads the ECR out of the Pinia-held list by `Number(id)`, fetching the list first if it's empty. Its "which buttons to show" logic uses a transition map **hardcoded in the component**, separate from (though currently matching) `adminObjects.xml`.

## 4. Backend architecture

```text
HTTP
 ↓
Servlet Layer
 ├── ECRWebServiceServlet   /api/ecrs/*
 ├── PartServlet            /api/parts
 ├── PartSyncServlet        /api/parts/sync
 ├── ECRAdminServlet        /admin/ecrs/*
 └── ECRDetailServlet       /admin/ecr
 ↓
Repository / Client Layer
 ├── ECRRepository   (singleton via getInstance())
 ├── PartRepository  (NOT a singleton — see §10)
 └── PartSupplierClient
 ↓
Model
 ├── ECR
 └── Part
```

Cross-cutting: `CorsFilter` (`@WebFilter("/*")`).

Workflow/configuration path:

```text
ECRWebServiceServlet → AdminObjectConfigReader → adminObjects.xml → ECRTriggerJPO → ECRRepository.updateStatus()
```

## 5. REST ECR flow

### Read

```text
Vue → Pinia → Axios GET /api/ecrs → ECRWebServiceServlet.doGet()
    → ECRRepository.getInstance().getAll() → Jackson JSON → Vue
```

### Create

```text
ECRForm.vue → Axios POST /api/ecrs → ECRWebServiceServlet.doPost()
    → validate title/requestedBy/priority
    → clear client id/status/dateCreated
    → ECRRepository.save() → generate ID, force Draft, generate date
    → HTTP 201 + JSON
```

### Status update

```text
ECRDetailView.vue → Pinia updateStatus() → Axios PUT /api/ecrs/{id}/status
    → ECRWebServiceServlet.doPut()
    → validate path (/\d+/status) and body (status non-blank)
    → 404 if ECR missing
    → ECRRepository.updateStatus() → ECRTriggerJPO.validateTransition()
    → invalid → 400 {"error": "..."}
    → valid → set status → 200 + updated ECR JSON
```

## 6. Workflow architecture

Source of truth: `src/main/resources/adminObjects.xml`.

```text
Draft → InReview
InReview → Approved
InReview → Rejected
Rejected → Draft
```

`AdminObjectConfigReader` parses the XML with `javax.xml.parsers.DocumentBuilder` (DOM), reads every `<transition from="" to=""/>` element, and builds `Map<String, List<String>>`. `ECRTriggerJPO` receives this map as a parameter — it does not read the XML itself, and does not cache/reload it (each servlet instance loads it once, in its constructor).

## 7. JPO-style trigger

`ECRTriggerJPO.validateTransition(currentStatus, newStatus, allowedTransitions)` looks up `allowedTransitions.get(currentStatus)` and checks whether `newStatus` is in that list; if not, throws `InvalidStatusTransitionException`.

```text
Real ENOVIA JPO      → Not used
Java JPO-style class → Implemented (ECRTriggerJPO)
```

## 8. JSP / Servlet-MVC architecture

### List screen — `/admin/ecrs` (broken as written)

```text
GET /admin/ecrs
      ↓
ECRAdminServlet   (uses ECRRepository.getInstance() — same singleton as REST)
      ↓
request.setAttribute("ecrs", ecrs)
      ↓
getRequestDispatcher("/WEB-INF/jsp/ecrList.jsp")   ← path does not exist in the WAR
```

There is no `WEB-INF/jsp/` directory in the project; the actual file is `src/main/webapp/ecrList.jsp`. Separately, that JSP's `<c:forEach var="ecr" items="${ecrList}">` reads an attribute named `ecrList`, but the servlet sets `"ecrs"`. Both defects are independent and both must be fixed for this route to work: correct the dispatcher path to `/ecrList.jsp`, and rename either the servlet attribute or the JSP's EL reference so they match.

The detail branch of the same servlet (`GET /admin/ecrs/{id}`) has the analogous dispatcher-path bug against `/WEB-INF/jsp/ecrDetail.jsp`, and additionally never sets a `linkedParts` attribute, so even once reachable it can only ever show "No parts are linked to this ECR."

### Detail screen — `/admin/ecr?id=` (works)

```text
GET /admin/ecr?id=101
      ↓
ECRDetailServlet.init()
      → own ECRRepository (new, not the singleton)
      → own PartRepository (new)
      → seeds ECR 101, 102 and three Parts, hardcoded
      ↓
doGet(): ecrRepository.getById(id) → filter parts by linkedEcrId == id
      ↓
request.setAttribute("ecr", ecr)
request.setAttribute("linkedParts", linkedParts)
      ↓
getRequestDispatcher("/ecrDetail.jsp")   ← correct, root-relative
```

This route works, but only for the two hardcoded ECRs — it is not connected to the singleton `ECRRepository`, so ECRs created via the REST API or Vue are never visible here.

## 9. Repository sharing — the actual picture

```text
ECRWebServiceServlet  ──┐
                         ├──> ECRRepository.getInstance()   (SAME instance)
ECRAdminServlet        ──┘

ECRDetailServlet       ──> new ECRRepository()  +  new PartRepository()   (SEPARATE, seeded)

PartServlet            ──> reads PartRepository from ServletContext
PartSyncServlet.init() ──> creates that PartRepository and stores it in ServletContext
                            (only place it's ever created — see §10 #5)
```

`ECRRepository` implements the singleton pattern (`private static final ECRRepository INSTANCE`, `getInstance()`), and it is genuinely used as a singleton by two of the three servlets that touch ECR data. `ECRDetailServlet` is the outlier.

`PartRepository` has no singleton pattern at all — its one shared instance exists only because `PartSyncServlet.init()` happens to store it in the `ServletContext`, and `ECRDetailServlet` separately creates a third, unrelated `PartRepository` for its own seeded parts.

## 10. Known defects (verified against source, not inferred)

| # | Defect | Location | Consequence |
|---|---|---|---|
| 1 | `ECRAdminServlet` forwards to `/WEB-INF/jsp/ecrList.jsp` / `/WEB-INF/jsp/ecrDetail.jsp`; no `WEB-INF/jsp/` exists | `ECRAdminServlet.java` | `/admin/ecrs*` cannot resolve the forward target |
| 2 | Attribute name mismatch: servlet sets `"ecrs"`, JSP reads `${ecrList}` | `ECRAdminServlet.java` / `ecrList.jsp` | Table renders empty even if #1 is fixed |
| 3 | `linkedParts` never set by `ECRAdminServlet`'s detail branch | `ECRAdminServlet.java` | Linked-parts section always empty via that route |
| 4 | `ECRDetailServlet` uses `new ECRRepository()`/`new PartRepository()`, not the singleton | `ECRDetailServlet.java` | `/admin/ecr?id=` only shows hardcoded demo ECRs 101/102 |
| 5 | `PartRepository` in `ServletContext` is only created inside `PartSyncServlet.init()`; no `web.xml`/`load-on-startup` | `PartServlet.java` / `PartSyncServlet.java` | `GET /api/parts` before any sync → HTTP 500 |
| 6 | `js/admin.js` duplicates `js/ecr-filter.js`, unreferenced by any JSP | `webapp/js/admin.js` | Dead code |
| 7 | `Main.java` uses `new ECRRepository()` instead of `getInstance()` | `Main.java` | Cosmetic inconsistency only (standalone demo, not servlet-container code) |

None of these affect the REST/Vue path (§5), which is internally consistent and functional.

## 11. Parts integration

```text
POST /api/parts/sync
        ↓
PartSyncServlet
        ↓
PartSupplierClient  → GET https://fakestoreapi.com/products (Java HttpClient)
        ↓
Jackson JSON parse
        ↓
map to Part { partNumber="PART-"+id, name=title, category, price, linkedEcrId=null }
        ↓
PartRepository.saveAll()  (replaces existing contents)
```

`GET /api/parts` (`PartServlet`) reads that same `PartRepository` from the `ServletContext` — see defect #5 for the ordering hazard.

## 12. Linked-part architecture

`Part.linkedEcrId` associates a part with an ECR. Only `ECRDetailServlet` (`/admin/ecr?id=`) computes and renders this association, against its own isolated seeded data (defect #4). The REST-synced parts from FakeStoreAPI always have `linkedEcrId = null`. The Vue detail view does not render linked parts at all.

## 13. CORS architecture

`CorsFilter`, mapped `/*`: sets `Access-Control-Allow-Origin: http://localhost:5173`, allows `GET, POST, PUT, DELETE, OPTIONS`, allows `Content-Type, Authorization` headers, and returns `200` immediately for `OPTIONS` preflight without invoking the rest of the chain.

## 14. MQL and TCL simulations

```text
SimulatedMQL.queryByField(ecrs, field, value)
        ↓
Java Stream .filter() over a switch on field name
        ↓
List<ECR>
```

```text
tcl/ecr_trigger.tcl
        ↓
allowedTransitions (TCL associative array)
        ↓
validateTransition proc → lsearch
        ↓
valid / invalid, printed to stdout for 6 hardcoded test cases
```

Neither is invoked by the Java backend; the TCL script is a standalone file run independently, not a deployed ENOVIA trigger.

## 15. Build/deployment architecture

```text
Java source → Maven (WAR, Java 11) → ecr-tracker.war → Apache Tomcat → Servlets + JSP
```

```text
Vite dev server (localhost:5173) → Axios → Tomcat (localhost:8080/ecr-tracker)
```

## 16. Architecture boundaries

### Implemented (and working)

- Vue 3, Vue Router, Pinia, Axios
- REST ECR API with server-side ID/status/date control and workflow validation
- Jackson JSON (de)serialization
- `AdminObjectConfigReader` + XML-driven workflow
- `ECRTriggerJPO`-style validation
- `PartSupplierClient` external REST integration + `/api/parts/sync`
- `CorsFilter`
- MQL simulation (`SimulatedMQL`), TCL simulation (standalone script)
- `/admin/ecr?id=` JSP detail screen (against isolated demo data)

### Implemented but broken

- `/admin/ecrs` and `/admin/ecrs/{id}` (JSP list/detail via `ECRAdminServlet`) — see §10 #1–3
- `GET /api/parts` before first sync — see §10 #5

### Not implemented

- Real ENOVIA/3DEXPERIENCE server, real JPO deployment, real MQL execution, real ENOVIA TCL trigger execution
- SOAP endpoint
- Database persistence
- Authentication/authorization
- Vue Parts page, Vue Suppliers page
