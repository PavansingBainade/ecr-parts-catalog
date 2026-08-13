# Technology Mapping

This document maps repository files to the technologies and concepts they actually implement.

| Technology / Concept | Actual implementation | Main files |
|---|---|---|
| Vue 3 | SPA UI | `ecr-parts-catalog-vue/src/views/*.vue` |
| Vue Router | Client-side routes | `ecr-parts-catalog-vue/src/router/index.js` |
| Pinia | ECR client state | `ecr-parts-catalog-vue/src/stores/ecrStore.js` |
| Axios | HTTP client | `ecr-parts-catalog-vue/src/services/api.js` |
| Vite | Vue build/dev server | `ecr-parts-catalog-vue/package.json` |
| Java 11 | Backend source/target | `pom.xml` |
| Servlet API | HTTP controllers | `src/main/java/com/ecrtracker/web/*` |
| REST | ECR/Part endpoints | `ECRWebServiceServlet`, `PartServlet`, `PartSyncServlet` |
| Jackson | JSON serialization/deserialization | REST servlets, supplier client |
| JSP | Server-rendered admin UI | `ecrList.jsp`, `ecrDetail.jsp` |
| JSTL | JSP control structures | both JSPs |
| Expression Language | JavaBean property access | both JSPs |
| Servlet/MVC | JSP controller/view architecture | `ECRAdminServlet`, `ECRDetailServlet` |
| Repository pattern | In-memory data access | `ECRRepository`, `PartRepository` |
| XML configuration | Workflow transitions | `adminObjects.xml` |
| DOM XML parsing | Admin Object reader | `AdminObjectConfigReader` |
| JPO-style logic | Transition validation | `ECRTriggerJPO` |
| Exception handling | Invalid transitions | `InvalidStatusTransitionException` |
| External REST integration | Supplier product retrieval | `PartSupplierClient` |
| Java HttpClient | External HTTP call | `PartSupplierClient` |
| CORS | Vue-to-Tomcat browser access | `CorsFilter` |
| MQL concept | Java Streams simulation | `SimulatedMQL.java` |
| TCL concept | TCL trigger simulation | `tcl/ecr_trigger.tcl` |
| Maven | Backend build | `pom.xml` |
| WAR | Tomcat deployment artifact | `pom.xml` |
| Apache Tomcat | Servlet/JSP runtime | deployment target |
| SOAP | Documentation/concept only | `docs/SOAP-Integration.md` |
| ENOVIA/3DEXPERIENCE | Conceptual target, not connected | project documentation |

## JSP/JSTL/EL mapping

### JSP

```text
ecrList.jsp
ecrDetail.jsp
```

### JSTL

```jsp
<c:forEach>
<c:choose>
<c:when>
<c:otherwise>
```

### EL

```jsp
${ecr.id}
${ecr.title}
${ecr.status}
${ecr.priority}
${ecr.requestedBy}
${ecr.dateCreated}

${part.id}
${part.partNumber}
${part.name}
${part.category}
${part.price}

${empty linkedParts}
${pageContext.request.contextPath}
```

### MVC

```text
Servlet → Controller
Repository → data access
ECR/Part → Model
JSP → View
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
TCL         → ecr_trigger.tcl
```

These are educational representations rather than real 3DEXPERIENCE runtime objects.
