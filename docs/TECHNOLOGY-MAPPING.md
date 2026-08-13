# Technology Mapping

This document maps repository files to the technologies and concepts they actually implement, verified against source.

| Technology / Concept | Actual implementation | Main files |
|---|---|---|
| Vue 3 | SPA UI | `ecr-parts-catalog-vue/src/views/*.vue` |
| Vue Router | Client-side routes | `ecr-parts-catalog-vue/src/router/index.js` |
| Pinia | ECR client state | `ecr-parts-catalog-vue/src/stores/ecrStore.js` |
| Axios | HTTP client | `ecr-parts-catalog-vue/src/services/api.js` |
| Vite | Vue build/dev server | `ecr-parts-catalog-vue/package.json`, `vite.config.js` |
| Java 11 | Backend source/target | `pom.xml` |
| Servlet API (4.0.1) | HTTP controllers | `src/main/java/com/ecrtracker/web/*` |
| REST | ECR/Part endpoints | `ECRWebServiceServlet`, `PartServlet`, `PartSyncServlet` |
| Jackson (2.17.2) | JSON serialization/deserialization | REST servlets, `PartSupplierClient` |
| JSP | Server-rendered admin UI | `src/main/webapp/ecrList.jsp`, `ecrDetail.jsp` |
| JSTL (1.2) | JSP control structures | both JSPs |
| Expression Language | JavaBean property access | both JSPs |
| Servlet/MVC | JSP controller/view architecture | `ECRAdminServlet` (broken — see REPOSITORY-AUDIT.md), `ECRDetailServlet` (working) |
| Repository pattern | In-memory data access | `ECRRepository` (singleton), `PartRepository` (not a singleton) |
| XML configuration | Workflow transitions | `adminObjects.xml` |
| DOM XML parsing | Admin Object reader | `AdminObjectConfigReader` |
| JPO-style logic | Transition validation | `ECRTriggerJPO` |
| Exception handling | Invalid transitions | `InvalidStatusTransitionException` |
| External REST integration | Supplier product retrieval | `PartSupplierClient` (`https://fakestoreapi.com/products`) |
| Java `HttpClient` | External HTTP call | `PartSupplierClient` |
| CORS | Vue-to-Tomcat browser access | `CorsFilter` |
| MQL concept | Java Streams simulation | `SimulatedMQL.java` |
| TCL concept | Standalone TCL trigger simulation | `tcl/ecr_trigger.tcl` |
| Maven | Backend build | `pom.xml` |
| WAR | Tomcat deployment artifact (`ecr-tracker.war`) | `pom.xml` |
| Apache Tomcat | Servlet/JSP runtime | deployment target |
| SOAP | Documentation/concept only | `docs/SOAP-Integration.md` |
| ENOVIA/3DEXPERIENCE | Conceptual target, not connected | project documentation |

## JSP/JSTL/EL mapping

### JSP files

```text
src/main/webapp/ecrList.jsp
src/main/webapp/ecrDetail.jsp
```

Note: both files live directly under `webapp/`, not under `WEB-INF/`. `ECRAdminServlet`'s dispatcher paths incorrectly assume a `WEB-INF/jsp/` location — see `docs/REPOSITORY-AUDIT.md`.

### JSTL

```jsp
<c:forEach>
<c:choose>
<c:when>
<c:otherwise>
```

### EL

```jsp
${ecr.id} ${ecr.title} ${ecr.status} ${ecr.priority} ${ecr.requestedBy} ${ecr.dateCreated}
${part.id} ${part.partNumber} ${part.name} ${part.category} ${part.price}
${empty linkedParts}
${pageContext.request.contextPath}
```

`ecrList.jsp`'s `<c:forEach>` iterates `${ecrList}` specifically — the one attribute name that `ECRAdminServlet` does *not* set (it sets `"ecrs"`). See `docs/REPOSITORY-AUDIT.md` for the full defect.

### MVC responsibilities

```text
Servlet    → Controller
Repository → Data access
ECR/Part   → Model
JSP        → View
JSTL       → View-side control structures
EL         → View-side property access
```

## Enterprise concept mapping

```text
ENOVIA / PLM concept
          ↓
Project implementation
          ↓
Admin Object → adminObjects.xml
JPO         → ECRTriggerJPO.java
Trigger     → validateTransition()
MQL         → SimulatedMQL.java
TCL         → tcl/ecr_trigger.tcl (standalone; not called from Java)
```

These are educational representations, not real 3DEXPERIENCE runtime objects.
