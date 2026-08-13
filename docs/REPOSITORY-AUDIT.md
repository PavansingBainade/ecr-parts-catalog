# Repository Audit – ECR Parts Catalog

## Audit scope and method

This audit was performed directly against the repository's source files (uploaded as a zip archive; GitHub's own web crawler blocks automated directory listing, so the earlier version of this document — produced against README claims alone — could not be verified file-by-file). Every claim below was checked by reading the actual file content, not inferred from naming conventions or prior documentation.

Repository: `https://github.com/PavansingBainade/ecr-parts-catalog`

## Top-level structure (verified)

```text
docs/
ecr-parts-catalog-vue/
src/main/
tcl/
README.md
pom.xml
cp.txt
.gitignore
```

`cp.txt` is a local Maven classpath dump (`C:\Users\Pavan\.m2\repository\...`) — not a project artifact, likely committed by accident.

## Java packages verified

```text
com.ecrtracker.client       PartSupplierClient.java
com.ecrtracker.config       AdminObjectConfigReader.java
com.ecrtracker.exception    InvalidStatusTransitionException.java
com.ecrtracker.filter       CorsFilter.java
com.ecrtracker.model        ECR.java, Part.java
com.ecrtracker.mql          SimulatedMQL.java
com.ecrtracker.repository   ECRRepository.java, PartRepository.java
com.ecrtracker.trigger      ECRTriggerJPO.java
com.ecrtracker.web          ECRAdminServlet.java, ECRDetailServlet.java,
                             ECRWebServiceServlet.java, PartServlet.java,
                             PartSyncServlet.java
com.ecrtracker (root)       Main.java, PartSupplierTest.java
```

There is no `web.xml` anywhere in the project — all servlet/filter mapping is annotation-based (`@WebServlet`, `@WebFilter`). No `load-on-startup` is configured on any servlet.

## Vue source inventory (verified)

```text
src/App.vue
src/main.js
src/router/index.js
src/services/api.js
src/stores/ecrStore.js
src/components/StatusBadge.vue
src/views/ECRListView.vue
src/views/ECRDetailView.vue
src/views/ECRForm.vue
```

`package.json` dependencies: `vue ^3.5.40`, `vue-router ^5.2.0`, `pinia ^4.0.2`, `axios ^1.19.0`. Dev: `vite ^8.1.5`, `eslint ^10.7.0`, `oxlint ^1.73.0`, others. These are the exact ranges as pinned in `package.json` and `package-lock.json` — worth a sanity check on `npm install` since some are unusually high major versions, but that's a packaging note, not something this audit can verify without running the install.

## Verified architecture facts

### Vue

- Vue 3, routes `/`, `/ecrs/:id`, `/ecrs/new` — confirmed in `router/index.js`.
- Pinia installed and initialized in `main.js` — confirmed.
- Axios `baseURL` is `http://localhost:8080/ecr-tracker/api`, hardcoded in `services/api.js` — confirmed, and it correctly includes the `/ecr-tracker` context path matching `pom.xml`'s `<finalName>`.
- `ECRDetailView.vue`'s valid-transition logic is a **transition map hardcoded in the component**, not fetched from the backend — confirmed by reading the `<script setup>` block.

### Java

- Maven packaging: WAR. Java source/target: 11. Confirmed in `pom.xml`.
- Dependencies confirmed: `javax.servlet-api:4.0.1` (provided), `javax.servlet.jsp-api:2.3.3` (provided), `jackson-databind:2.17.2`, `jstl:1.2`.
- `<finalName>ecr-tracker</finalName>` — confirmed.

### JSP — corrected from the prior version of this document

The prior audit stated `ECRAdminServlet forwards to ecrList.jsp` and `request.setAttribute("ecrList", ...)`. **Neither is accurate.** Verified by direct inspection of `ECRAdminServlet.java`:

```java
request.setAttribute("ecrs", ecrs);
request.getRequestDispatcher("/WEB-INF/jsp/ecrList.jsp").forward(request, response);
```

- The attribute name is `"ecrs"`, not `"ecrList"`.
- The dispatch target is `/WEB-INF/jsp/ecrList.jsp`. There is **no `WEB-INF/jsp/` directory anywhere in the repository** (confirmed via full file listing) — the real file is `src/main/webapp/ecrList.jsp`.
- `ecrList.jsp` itself reads `${ecrList}` in `<c:forEach var="ecr" items="${ecrList}">` — confirmed by direct inspection.

Net effect, verified against both files together: `/admin/ecrs` cannot currently forward successfully, and even with the dispatch path corrected, the resulting page would show zero rows due to the attribute-name mismatch. This is a genuine defect in the code, not a documentation gap.

`ECRDetailServlet`, by contrast, is correctly wired: `request.setAttribute("ecr", ecr)`, `request.setAttribute("linkedParts", linkedParts)`, `getRequestDispatcher("/ecrDetail.jsp")` — all three confirmed to match what `ecrDetail.jsp` expects and where the file actually lives.

### Repository sharing — corrected from the prior version of this document

The prior audit stated all three servlets touching ECR data (`ECRWebServiceServlet`, `ECRAdminServlet`, `ECRDetailServlet`) use "their own" `ECRRepository`. Verified by grepping each file for its repository field:

```text
ECRWebServiceServlet.java:  ECRRepository.getInstance()
ECRAdminServlet.java:       ECRRepository.getInstance()
ECRDetailServlet.java:      new ECRRepository()   (and new PartRepository())
```

`ECRWebServiceServlet` and `ECRAdminServlet` share the **same singleton instance**. Only `ECRDetailServlet` is isolated, with its own freshly-constructed repositories seeded with two hardcoded ECRs and three hardcoded Parts in `init()`. This is a real, verifiable distinction that the prior documentation flattened into "all separate."

### Workflow

Source of truth: `adminObjects.xml`, confirmed:

```text
Draft → InReview
InReview → Approved
InReview → Rejected
Rejected → Draft
```

`AdminObjectConfigReader` parses via `javax.xml.parsers.DocumentBuilder` (DOM), confirmed by reading the full method body.

### External API

`PartSupplierClient` calls `https://fakestoreapi.com/products` via `java.net.http.HttpClient` — confirmed. `PartSyncServlet` exposes `POST /api/parts/sync` — confirmed. `PartServlet` exposes `GET /api/parts`, reading a `PartRepository` from `ServletContext.getAttribute("partRepository")` that is only ever populated inside `PartSyncServlet.init()` — confirmed by reading both files; since there's no `web.xml`/`load-on-startup`, calling `/api/parts` before the sync servlet has been touched at least once will hit a `null` repository and return HTTP 500.

### Simulations

- `SimulatedMQL.java` — Java Streams `.filter()` over a `switch` on field name. Confirmed.
- `tcl/ecr_trigger.tcl` — standalone TCL script with an associative array and `lsearch`-based validation, six hardcoded test invocations. Confirmed. Not called from Java.
- SOAP: no servlet, no WSDL, no JAX-WS annotation, no SOAP client anywhere in the codebase. Confirmed absent.
- No ENOVIA/3DEXPERIENCE connection anywhere in the codebase. Confirmed absent.

## Defects found (new in this audit)

1. `ECRAdminServlet` dispatcher paths point to a nonexistent `WEB-INF/jsp/` directory.
2. `ECRAdminServlet` sets `"ecrs"`; `ecrList.jsp` expects `"ecrList"`.
3. `ECRAdminServlet`'s detail branch never sets `linkedParts`.
4. `ECRDetailServlet` does not use the `ECRRepository` singleton — it is functionally disconnected from every ECR created through the Vue UI or the REST API.
5. `GET /api/parts` throws a 500 if called before `/api/parts/sync` has ever run, due to servlet init ordering with no `web.xml` safeguard.
6. `webapp/js/admin.js` is dead code — not referenced by any JSP.
7. `Main.java` bypasses the `ECRRepository` singleton pattern used everywhere else (low-severity — it's a standalone demo entry point).
8. `cp.txt` is a stray local classpath dump that shouldn't be in version control.

## Documentation conclusion

The documentation set (README, ARCHITECTURE, PROJECT-REQUIREMENTS, PRESENTATION-NOTES, TECHNOLOGY-MAPPING, SOAP-Integration, this file) has been rewritten to reflect the above. In particular, any future documentation should:

1. Distinguish `ECRAdminServlet` (shares the singleton, but is currently broken) from `ECRDetailServlet` (isolated demo data, but works).
2. Not claim `/admin/ecrs` renders a populated list — it currently cannot.
3. Flag the `PartServlet` init-order hazard for anyone testing the API fresh.
4. Continue to correctly describe MQL, TCL, SOAP, and ENOVIA as simulated/documentation-only/not connected — those claims were accurate and remain unchanged.
