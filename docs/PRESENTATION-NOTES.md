# Presentation / Viva Notes

## 1. One-minute project explanation

> "I developed an ECR Parts Catalog application using Vue 3 on the frontend and Java Servlets deployed on Apache Tomcat on the backend. The Vue application uses Pinia for state management and Axios for REST communication. ECR creation, retrieval, and status changes are handled through REST APIs. The backend generates the ECR ID, creation date, and initial Draft status and validates workflow transitions using an XML-based Admin Object configuration and a JPO-style Java trigger.
>
> The project also contains a second, JSP-based admin path using Servlet MVC, JSTL, and Expression Language. One of its two routes — the single-ECR detail view — works, but runs against its own isolated seed data rather than the same repository the REST API uses. The other — the admin ECR list — currently has a dispatcher and attribute-naming bug I found while auditing the code, so it doesn't render. I've documented both honestly rather than claiming the JSP module fully works. The project also includes an external REST supplier integration using FakeStoreAPI, plus simulations of MQL and TCL concepts. Real ENOVIA/3DEXPERIENCE and SOAP are not connected."

## 2. Why Vue?

- Component-based UI
- Client-side routing
- Reactive state
- Good fit for a dashboard
- Axios integration with REST APIs

## 3. Why Pinia?

Pinia provides centralized client-side state for ECR data and loading/error state.

```text
ecrs
loading
error
draftCount
fetchECRs()
updateStatus()
```

## 4. Why REST?

REST keeps the Vue frontend independent from the Java backend implementation.

```text
Vue → Axios → REST → Servlet → Repository
```

## 5. Why JSP if Vue is already used?

> "The project demonstrates both modern and traditional Java web UI approaches. Vue is used for the main dashboard and user-facing ECR workflow, while JSP is used for server-rendered administrative screens. In its current state, the detail screen works but the list screen doesn't — I can walk through exactly why if you'd like."

## 6. Explain JSP/JSTL/EL

### JSP

Files:

```text
src/main/webapp/ecrList.jsp
src/main/webapp/ecrDetail.jsp
```

Both live at the webapp root, not under `WEB-INF/`.

### JSTL

```jsp
<c:forEach>
<c:choose>
<c:when>
<c:otherwise>
```

### EL

```jsp
${ecr.title}
${ecr.status}
${part.name}
```

## 7. Explain MVC

```text
Controller → Servlet
Model      → ECR / Part
Data       → Repository
View       → JSP
```

The servlet prepares data with `request.setAttribute(...)` and forwards the request to the JSP — when the attribute name and dispatch path actually match what the JSP expects. (They do for `ECRDetailServlet`; they don't for `ECRAdminServlet` — see Q19 below.)

## 8. Explain workflow

```text
Draft → InReview
InReview → Approved
InReview → Rejected
Rejected → Draft
```

`Approved` has no configured outgoing transition.

## 9. How is workflow protected?

The frontend only displays valid buttons, but it is not trusted.

```text
request → ECRRepository.updateStatus() → ECRTriggerJPO.validateTransition()
        → allowedTransitions → accept/reject
```

A user cannot bypass the workflow simply by sending a direct HTTP request to `/api/ecrs/{id}/status`.

## 10. What is Admin Object?

`adminObjects.xml` represents the configuration of allowed ECR workflow transitions, parsed at runtime by `AdminObjectConfigReader`.

## 11. What is JPO?

The project does not deploy an ENOVIA JPO. `ECRTriggerJPO` is a Java class that demonstrates the JPO-style business-rule concept.

## 12. What is MQL?

`SimulatedMQL.java` demonstrates a conceptual query such as `status == Draft` using Java Streams. Not a real MQL engine.

## 13. What is TCL?

`tcl/ecr_trigger.tcl` demonstrates transition validation using TCL procedures and arrays. It's a standalone script, run independently — not called from the Java backend and not deployed to ENOVIA.

## 14. Explain supplier integration

```text
POST /api/parts/sync → PartSyncServlet → PartSupplierClient
    → https://fakestoreapi.com/products → JSON → Part objects → PartRepository.saveAll()
```

## 15. Why FakeStoreAPI?

> "It's an accessible external REST API to demonstrate supplier/part integration without depending on a real company supplier system."

## 16. Is SOAP implemented?

No.

> "SOAP is documented as a possible enterprise integration approach, but the actual external integration in my implementation uses REST through Java HttpClient and FakeStoreAPI."

## 17. Is ENOVIA implemented?

No.

> "The project demonstrates ENOVIA/3DEXPERIENCE concepts — Admin Objects, JPO-style triggers, MQL, TCL — but doesn't connect to a real ENOVIA/3DEXPERIENCE server."

## 18. Does the project use a database?

No — `ArrayList`-backed repositories.

## 19. Are there any known bugs? (be ready for this)

Yes, and it's better to volunteer this than have it found live:

- `ECRAdminServlet` (`/admin/ecrs`) forwards to `/WEB-INF/jsp/ecrList.jsp`, a path that doesn't exist in the project — the JSP actually lives at the webapp root. It also sets the request attribute as `"ecrs"` while the JSP reads `${ecrList}`. Both need fixing for that route to render.
- `ECRDetailServlet` (`/admin/ecr?id=`) works, but uses its own repository instances seeded with two hardcoded ECRs — it's not connected to the same data as the REST API or Vue app.
- `GET /api/parts` returns a 500 if called before `/api/parts/sync` has run once, because the shared `PartRepository` is only created inside the sync servlet's `init()`.

> "I found these while doing a full audit of my own code against my documentation — the REST/Vue path is solid, and I know exactly what's wrong with the JSP list route and could fix it in a few minutes if asked."

## 20. What happens when the server restarts?

In-memory repository data is lost.

## 21. What is the role of Tomcat?

Runs the Java Servlet/JSP web application from the generated WAR.

## 22. Why Maven?

Manages the Java build, dependencies, compilation, and WAR packaging.

## 23. Strong architecture answer

> "The application follows a layered architecture. The Vue frontend communicates through Axios with Java Servlet REST endpoints. The servlet layer delegates ECR operations to a shared repository singleton and uses a configuration-driven trigger for workflow validation, loaded from an Admin Object XML file. Separately, JSP administrative screens use Servlet MVC — one route works against isolated seed data, one route has a wiring bug I've documented. Parts are stored separately and can be synchronized from an external REST supplier service. MQL and TCL are included as learning simulations; SOAP and real ENOVIA integration are not implemented."

## 24. Avoid saying

- "We connected to ENOVIA."
- "We implemented real MQL."
- "We deployed a real JPO."
- "We implemented a SOAP service."
- "We have a production database."
- "Vue displays linked parts."
- "The JSP admin list screen works." (it currently doesn't — see Q19)
- "All three ECR servlets share one repository." (only two of the three do)

Those statements are not supported by the current code.
