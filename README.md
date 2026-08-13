# ECR Parts Catalog

A full-stack Engineering Change Request (ECR) management application: a Java Servlet/Tomcat backend with **two separate UI paths** — a Vue 3 SPA that talks to a REST API, and JSP/JSTL/EL admin screens rendered server-side. The project also demonstrates PLM/ENOVIA-style concepts (Admin Objects, JPO-style trigger validation, MQL-style querying, TCL trigger logic) and an external REST supplier integration.

> **Implementation note:** Data is stored in in-memory Java `ArrayList`s, not a database. ENOVIA/3DEXPERIENCE is not connected. MQL and TCL are learning simulations. SOAP is documentation only, not an implemented endpoint. See [Known issues](#13-known-issues--verified-against-source) — two of the admin routes have real bugs, documented below rather than glossed over.

## 1. What the application does

- ECR dashboard (Vue) with total/status counts, search by ID/title/requester, status filter
- Create ECR from the Vue UI (`POST /api/ecrs`)
- View ECR detail in Vue, with workflow visualization and valid-transition buttons
- Change ECR status via `PUT /api/ecrs/{id}/status`, validated server-side
- Server-side generation of ECR ID, initial `Draft` status, and `dateCreated`
- JSP-based admin ECR list and ECR detail screens (Servlet/MVC + JSTL + EL)
- Part listing REST endpoint and part/supplier sync from an external API
- Java simulation of MQL-style field querying
- TCL simulation of workflow transition validation

The Vue sidebar shows **Parts** and **Suppliers** as disabled nav items — there is no Vue Parts or Suppliers page.

## 2. High-level architecture

```text
                         USER
                           |
             +-------------+-------------+
             |                           |
             v                           v
      Vue 3 Dashboard              JSP Admin UI
             |                           |
      Vue Router / Pinia           Servlet / MVC
             |                           |
           Axios                         |
             |                           |
             +-------------+-------------+
                           |
                           v
                    Java Servlet Layer
                           |
              +------------+-------------+
              |            |             |
              v            v             v
          ECR REST      Parts REST    JSP Servlets
              |            |             |
              v            v             v
        ECRRepository  PartRepository  (see below —
        (singleton)                    two different
                                        repository setups)
              |
              v
       ECRTriggerJPO-style
          validation
              |
              v
       AdminObjectConfigReader
              |
              v
       src/main/resources/adminObjects.xml
```

### Vue request path

```text
Vue View → Pinia → Axios → ECRWebServiceServlet → ECRRepository.getInstance()
                                                        → ECRTriggerJPO → adminObjects.xml
```

### JSP request path

```text
Browser → ECRAdminServlet / ECRDetailServlet → request.setAttribute(...) → JSP → JSTL + EL → HTML
```

Both JSP routes exist in the repo, but **only one of them currently works** — see [§9](#9-jsp--jstl--el-admin-screens) and [§13](#13-known-issues--verified-against-source).

## 3. Frontend: Vue 3

Project: `ecr-parts-catalog-vue/`. Stack: Vue 3, Vue Router, Pinia, Axios, Vite.

### Routes (`src/router/index.js`)

```text
/             → ECRListView.vue
/ecrs/:id     → ECRDetailView.vue
/ecrs/new     → ECRForm.vue
```

### Dashboard — `ECRListView.vue`

Total/Draft/InReview/Approved/Rejected counts, search by ID/title/requester, status filter, clear filters, ECR table with `StatusBadge`, links to detail, "Create ECR" nav. Parts/Suppliers sidebar entries are disabled placeholders.

### Create ECR — `ECRForm.vue`

Collects Title (required), Requested By (required), Description, Priority (`LOW`/`MEDIUM`/`HIGH`, default `MEDIUM`). Validates only that Title is non-empty client-side, then `POST /ecrs` through the configured Axios instance. Shows the backend's `error` message on failure.

### Detail view — `ECRDetailView.vue`

Reads the ECR from the already-loaded Pinia list by numeric ID (fetching the list first if it isn't loaded yet); there is no single-ECR GET endpoint. Shows ID, title, description, status badge, priority, requested-by, date-created, a 4-step workflow visualization, and buttons for the valid next transitions (computed from a **hardcoded transition map duplicated in this component**, not fetched from the backend). Buttons are disabled while a status update is in flight and show the backend's error text if the PUT is rejected.

**This view does not render linked parts.**

## 4. State management and HTTP

Pinia is initialized in `main.js`. `stores/ecrStore.js` exposes:

```text
ecrs, loading, error, draftCount, fetchECRs(), updateStatus(id, newStatus)
```

Axios is configured in `services/api.js`:

```js
const api = axios.create({
  baseURL: 'http://localhost:8080/ecr-tracker/api'
})
```

This baseURL is hardcoded (no `.env`), and it already includes the `/ecr-tracker` context path, which matches the WAR's `finalName` in `pom.xml`.

## 5. Java backend

Maven WAR project, Java source/target 11 (`pom.xml`). Dependencies: `javax.servlet-api` 4.0.1 (provided), `javax.servlet.jsp-api` 2.3.3 (provided), `jackson-databind` 2.17.2, `jstl` 1.2. Build plugins: `maven-compiler-plugin` 3.13.0, `maven-war-plugin` 3.4.0. `finalName` is `ecr-tracker`, so the deployed context path is `/ecr-tracker`.

```text
com.ecrtracker
├── client       PartSupplierClient
├── config       AdminObjectConfigReader
├── exception    InvalidStatusTransitionException
├── filter       CorsFilter
├── model        ECR, Part
├── mql          SimulatedMQL
├── repository   ECRRepository, PartRepository
├── trigger      ECRTriggerJPO
├── web          ECRAdminServlet, ECRDetailServlet, ECRWebServiceServlet, PartServlet, PartSyncServlet
├── Main.java
└── PartSupplierTest.java
```

### Model

`ECR`: `id, title, description, status, priority, requestedBy, dateCreated` — plain JavaBean, all-args + no-args constructors, getters/setters, `toString()`.

`Part`: `id, partNumber, name, category, price, linkedEcrId` — same pattern.

## 6. ECR REST API

`ECRWebServiceServlet` → `/api/ecrs/*`. Holds `ECRRepository.getInstance()`, a `new ECRTriggerJPO()`, and loads `allowedTransitions` once in its constructor via `AdminObjectConfigReader`.

### `GET /api/ecrs`

Returns all ECRs as JSON (`repository.getAll()`).

### `POST /api/ecrs`

Validates the JSON body, then `title`, `requestedBy`, and `priority` are required (400 if missing/blank). `priority` is uppercased and must be `LOW`/`MEDIUM`/`HIGH` (400 otherwise). The servlet then explicitly clears `id`, `status`, and `dateCreated` on the incoming object before saving — the client cannot set any of them. `ECRRepository.save()` assigns the next ID (`max existing ID, or 100, + 1`), forces `status = "Draft"`, and fills `dateCreated` with today's date if blank. Returns `201 Created` with the saved ECR as JSON.

```json
{
  "title": "Wheel Design Change",
  "description": "Update wheel assembly",
  "priority": "HIGH",
  "requestedBy": "Pavan"
}
```

### `PUT /api/ecrs/{id}/status`

Path must match `/\d+/status` (400 otherwise). Body must contain a non-blank `status` (400 otherwise). 404 if the ECR doesn't exist. Otherwise `ECRRepository.updateStatus()` calls `ECRTriggerJPO.validateTransition()`; an invalid transition throws `InvalidStatusTransitionException`, caught and returned as `400` with `{"error": "..."}`. On success, returns the updated ECR as JSON with `200`.

```json
{ "status": "InReview" }
```

## 7. Workflow

Configured in `src/main/resources/adminObjects.xml`:

```xml
<adminObjects>
  <type name="ECR">
    <policy name="ECRPolicy">
      <transition from="Draft" to="InReview"/>
      <transition from="InReview" to="Approved"/>
      <transition from="InReview" to="Rejected"/>
      <transition from="Rejected" to="Draft"/>
    </policy>
  </type>
</adminObjects>
```

```text
Draft → InReview → Approved
                 └→ Rejected → Draft
```

`Approved` has no outgoing transition.

`AdminObjectConfigReader.loadTransitions()` loads `adminObjects.xml` from the classpath via DOM parsing, reads every `<transition>` element, and builds a `Map<String, List<String>>` of `from → [to, to, ...]`.

`ECRTriggerJPO.validateTransition(currentStatus, newStatus, allowedTransitions)` looks up `currentStatus` in the map and checks whether `newStatus` is in the allowed list; if not, throws `InvalidStatusTransitionException("Invalid status transition: X -> Y")`. This is a **Java class named to represent the JPO concept**, not a deployed ENOVIA JPO.

## 8. ECR identity and status ownership

`ECRRepository` is a **singleton** (`ECRRepository.getInstance()`), shared by `ECRWebServiceServlet` and `ECRAdminServlet` — see [§13](#13-known-issues--verified-against-source) for the one servlet that does *not* share it. `save()` always assigns the ID and forces `Draft` server-side, regardless of what the client sent, so a hand-crafted request cannot choose an ID or create an ECR already `Approved`.

## 9. JSP / JSTL / EL admin screens

Two JSP files exist under `src/main/webapp/` (webapp root — **not** under `WEB-INF/`): `ecrList.jsp`, `ecrDetail.jsp`.

### `ECRAdminServlet` → `/admin/ecrs/*` — currently broken

Uses `ECRRepository.getInstance()` (the same repository as the REST API). On `GET /admin/ecrs`, sets `request.setAttribute("ecrs", ecrs)` and forwards to `"/WEB-INF/jsp/ecrList.jsp"`. **That path does not exist** — there is no `WEB-INF/jsp/` directory anywhere in the project; the real file is `src/main/webapp/ecrList.jsp`. Separately, `ecrList.jsp` reads `${ecrList}` in its `<c:forEach>`, an attribute name the servlet never sets (it sets `"ecrs"`). Both problems have to be fixed for this route to render a populated table. See [§13](#13-known-issues--verified-against-source).

On `GET /admin/ecrs/{id}`, the servlet sets `request.setAttribute("ecr", ecr)` and forwards to `"/WEB-INF/jsp/ecrDetail.jsp"` — same nonexistent-path problem, and it never sets `linkedParts`, which `ecrDetail.jsp` also expects.

### `ECRDetailServlet` → `/admin/ecr` — works

Takes `?id=` as a query parameter (not a path segment). In `init()`, it creates its **own, separate** `ECRRepository` and `PartRepository` (not the singleton) and seeds two hardcoded ECRs (101, 102) and three hardcoded Parts. On `GET`, it looks up the ECR by ID, filters parts where `linkedEcrId` matches, sets `ecr` and `linkedParts` request attributes, and forwards to `"/ecrDetail.jsp"` (root-relative — this path is correct). This route only ever shows the two seeded ECRs; ECRs created through the Vue form or the REST API are invisible to it, because it isn't backed by the singleton.

### JSTL / EL actually used

```jsp
<c:forEach var="ecr" items="${ecrList}">   <!-- ecrList.jsp -->
<c:choose> <c:when> <c:otherwise>
${ecr.id} ${ecr.title} ${ecr.status} ${ecr.priority} ${ecr.requestedBy} ${ecr.dateCreated}
${part.id} ${part.partNumber} ${part.name} ${part.category} ${part.price}
${empty linkedParts}
${pageContext.request.contextPath}
```

`ecrList.jsp` also includes `js/ecr-filter.js`, a client-side status filter matching on `.ecr-row[data-status]`. `js/admin.js` implements the same filter against `#ecrTable` but **is not referenced by any JSP** — it's dead code left in the repo.

## 10. Parts and supplier integration

### `PartServlet` → `GET /api/parts`

Reads a `PartRepository` from `ServletContext.getAttribute("partRepository")`. If that attribute hasn't been set yet, returns `500` with `{"error":"Part repository not initialized"}` — see [§13](#13-known-issues--verified-against-source).

### `PartSyncServlet` → `POST /api/parts/sync`

In `init()`, creates a `PartRepository` and stores it in the `ServletContext` under `"partRepository"` **if not already present** — this is the only place that attribute is ever created. On `POST`, calls `PartSupplierClient.fetchParts()` and replaces the repository's contents wholesale (`saveAll`). Returns `{"message": "Parts synchronized successfully", "count": N}`.

### `PartSupplierClient`

Calls `GET https://fakestoreapi.com/products` with Java's built-in `HttpClient`, checks for HTTP 200, parses the JSON with Jackson, and maps each product to a `Part`: `partNumber = "PART-" + id`, `name = title`, `category`, `price`, `linkedEcrId = null` (synced parts are never linked to an ECR).

This is a real external REST call — not mocked, not SOAP.

## 11. CORS

`CorsFilter` (`@WebFilter("/*")`) sets `Access-Control-Allow-Origin: http://localhost:5173`, allows `GET, POST, PUT, DELETE, OPTIONS`, allows `Content-Type, Authorization`, and short-circuits `OPTIONS` preflight requests with `200`.

## 12. MQL and TCL simulations

`com.ecrtracker.mql.SimulatedMQL.queryByField(ecrs, field, value)` filters a `List<ECR>` in memory with a `switch` over the supported fields (`id, title, description, status, priority, requestedBy, dateCreated`) using Java Streams. Its `main()` method runs a small standalone demo. Not a real MQL interpreter; no ENOVIA connection.

`tcl/ecr_trigger.tcl` uses a TCL associative array (`allowedTransitions`) and a `validateTransition` proc with `lsearch` to check transitions, then runs six hardcoded test cases including valid and invalid transitions. It is a standalone script — not invoked by the Java backend and not deployed to any ENOVIA/3DEXPERIENCE runtime.

## 13. Known issues — verified against source

| # | Issue | Where | Effect |
|---|---|---|---|
| 1 | `ECRAdminServlet` forwards to `/WEB-INF/jsp/ecrList.jsp` and `/WEB-INF/jsp/ecrDetail.jsp`, but no `WEB-INF/jsp/` directory exists — the real files are at the webapp root. | `ECRAdminServlet.java` | `GET /admin/ecrs` and `/admin/ecrs/{id}` cannot resolve the forward target as written. |
| 2 | `ECRAdminServlet` sets attribute `"ecrs"`; `ecrList.jsp` reads `${ecrList}`. | `ECRAdminServlet.java` / `ecrList.jsp` | Even with issue #1 fixed, the ECR table would render with zero rows. |
| 3 | `ECRAdminServlet`'s detail branch never sets `linkedParts`. | `ECRAdminServlet.java` / `ecrDetail.jsp` | The linked-parts table would always show "No parts are linked to this ECR" via that route. |
| 4 | `ECRDetailServlet` creates its own `ECRRepository`/`PartRepository` instead of using `ECRRepository.getInstance()`. | `ECRDetailServlet.java` | `/admin/ecr?id=` only ever shows the two hardcoded sample ECRs (101, 102) — ECRs created via Vue/REST are invisible there. |
| 5 | `GET /api/parts` reads a `ServletContext` attribute that's only created inside `PartSyncServlet.init()`, and there's no `web.xml`/`load-on-startup` forcing early init. | `PartServlet.java` / `PartSyncServlet.java` | Calling `/api/parts` before `/api/parts/sync` has ever run returns HTTP 500, not an empty list. |
| 6 | `js/admin.js` duplicates `js/ecr-filter.js` and is not included by any JSP. | `src/main/webapp/js/admin.js` | Dead code. |
| 7 | `Main.java` calls `new ECRRepository()` instead of `ECRRepository.getInstance()`. | `Main.java` | Harmless (it's a standalone demo `main()`, not run inside the servlet container), but inconsistent with the singleton pattern used elsewhere. |
| 8 | `cp.txt` at the repo root is a local Maven classpath dump (`C:\Users\Pavan\.m2\...`). | `cp.txt` | Looks like an accidental commit; not part of the build. |

None of these affect the Vue SPA path (`/api/ecrs`, `/api/ecrs/{id}/status`, `/api/parts/sync`), which is fully functional as documented in §6–7 and §10.

## 14. Build and run

Backend:

```powershell
mvn clean package
```

Produces `target/ecr-tracker.war`; deploy to Apache Tomcat. Context path is `/ecr-tracker` (from `finalName`).

Frontend:

```powershell
cd ecr-parts-catalog-vue
npm install
npm run dev      # http://localhost:5173
npm run build    # production build
```

`npm run lint` runs `oxlint --fix` then `eslint --fix --cache` (see `package.json`).

## 15. Project structure

```text
ecr-parts-catalog/
├── src/main/
│   ├── java/com/ecrtracker/
│   │   ├── client/PartSupplierClient.java
│   │   ├── config/AdminObjectConfigReader.java
│   │   ├── exception/InvalidStatusTransitionException.java
│   │   ├── filter/CorsFilter.java
│   │   ├── model/ECR.java
│   │   ├── model/Part.java
│   │   ├── mql/SimulatedMQL.java
│   │   ├── repository/ECRRepository.java
│   │   ├── repository/PartRepository.java
│   │   ├── trigger/ECRTriggerJPO.java
│   │   ├── web/ECRAdminServlet.java
│   │   ├── web/ECRDetailServlet.java
│   │   ├── web/ECRWebServiceServlet.java
│   │   ├── web/PartServlet.java
│   │   ├── web/PartSyncServlet.java
│   │   ├── Main.java
│   │   └── PartSupplierTest.java
│   ├── resources/adminObjects.xml
│   └── webapp/
│       ├── css/admin.css
│       ├── js/admin.js          (dead code — unused)
│       ├── js/ecr-filter.js
│       ├── ecrList.jsp
│       └── ecrDetail.jsp
├── ecr-parts-catalog-vue/
│   ├── src/
│   │   ├── components/StatusBadge.vue
│   │   ├── router/index.js
│   │   ├── services/api.js
│   │   ├── stores/ecrStore.js
│   │   ├── views/ECRListView.vue
│   │   ├── views/ECRDetailView.vue
│   │   ├── views/ECRForm.vue
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
├── tcl/ecr_trigger.tcl
├── docs/
│   ├── ARCHITECTURE.md
│   ├── PROJECT-REQUIREMENTS.md
│   ├── PRESENTATION-NOTES.md
│   ├── REPOSITORY-AUDIT.md
│   ├── TECHNOLOGY-MAPPING.md
│   └── SOAP-Integration.md
├── pom.xml
├── README.md
└── cp.txt   (stray local classpath dump — see §13)
```

## 16. Implementation status

| Component | Status |
|---|---|
| Vue dashboard / search / filter | Implemented |
| Vue ECR creation | Implemented |
| Vue ECR detail + status transitions | Implemented (no linked parts) |
| REST ECR API (`GET`/`POST`/`PUT status`) | Implemented |
| Backend-controlled ID/status/date | Implemented |
| Workflow validation (`ECRTriggerJPO` + `adminObjects.xml`) | Implemented |
| JSP admin list (`/admin/ecrs`) | **Broken** — see §13 #1–2 |
| JSP admin detail via `/admin/ecrs/{id}` | **Broken** — see §13 #1, #3 |
| JSP admin detail via `/admin/ecr?id=` | Implemented, but isolated demo data only — see §13 #4 |
| Parts REST API | Implemented, with init-order hazard — see §13 #5 |
| Supplier sync (FakeStoreAPI) | Implemented |
| MQL simulation | Simulated |
| TCL simulation | Simulated, standalone |
| SOAP | Documentation only |
| Real ENOVIA/3DEXPERIENCE | Not used |
| Database persistence | Not implemented |
| Authentication/authorization | Not implemented |
| Vue Parts / Suppliers pages | Not implemented |

## 17. Project objective

The project pairs a working Vue 3 + REST Java web application with server-rendered JSP/Servlet-MVC screens, and layers PLM-style concepts (Admin Objects, JPO-style triggers, MQL, TCL) on top as configuration and simulations rather than a real ENOVIA/3DEXPERIENCE integration. It's a learning implementation, and this document — including §13 — is meant to describe exactly what the current code does, not what it was intended to do.
