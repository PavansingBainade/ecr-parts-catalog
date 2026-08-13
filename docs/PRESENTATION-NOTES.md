# Presentation / Viva Notes

## 1. One-minute project explanation

> "I developed an ECR Parts Catalog application using Vue 3 on the frontend and Java Servlets deployed on Apache Tomcat on the backend. The Vue application uses Pinia for state management and Axios for REST communication. ECR creation, retrieval, and status changes are handled through REST APIs. The backend generates the ECR ID, creation date, and initial Draft status and validates workflow transitions using an XML-based Admin Object configuration and a JPO-style Java trigger.
>
> In addition to the Vue dashboard, the project contains JSP administrative screens using Servlet MVC, JSTL, and Expression Language. The JSP detail screen also demonstrates linked ECR parts. The project includes an external REST supplier integration using FakeStoreAPI, plus simulations of MQL and TCL concepts. Real ENOVIA/3DEXPERIENCE and SOAP are not connected."

## 2. Why Vue?

- Component-based UI
- Client-side routing
- Reactive state
- Good fit for a dashboard
- Axios integration with REST APIs

## 3. Why Pinia?

Pinia provides centralized client-side state for ECR data and loading/error state.

The ECR store exposes:

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

Example:

```text
Vue → Axios → REST → Servlet → Repository
```

## 5. Why JSP if Vue is already used?

A strong answer:

> "The project demonstrates both modern and traditional Java web UI approaches. Vue is used for the main dashboard and user-facing ECR workflow, while JSP is used for server-rendered administrative screens. The JSP screens demonstrate Servlet MVC, JSTL, Expression Language, and JavaBean-based view rendering."

## 6. Explain JSP/JSTL/EL

### JSP

JSP is the server-side view technology.

Files:

```text
ecrList.jsp
ecrDetail.jsp
```

### JSTL

Used for view-side control logic:

```jsp
<c:forEach>
<c:choose>
<c:when>
<c:otherwise>
```

### EL

Used to access model properties:

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

The servlet prepares data with:

```java
request.setAttribute(...)
```

and forwards the request to the JSP.

## 8. Explain workflow

```text
Draft → InReview
InReview → Approved
InReview → Rejected
Rejected → Draft
```

Approved has no configured outgoing transition.

## 9. How is workflow protected?

The frontend only displays valid buttons, but it is not trusted.

The backend:

```text
request
 ↓
ECRRepository.updateStatus()
 ↓
ECRTriggerJPO.validateTransition()
 ↓
allowedTransitions
 ↓
accept/reject
```

So a user cannot bypass the workflow simply by sending a direct HTTP request.

## 10. What is Admin Object?

In this project, `adminObjects.xml` represents the configuration of allowed ECR workflow transitions.

It is parsed at runtime by `AdminObjectConfigReader`.

## 11. What is JPO?

The actual project does not deploy an ENOVIA JPO.

`ECRTriggerJPO` is a Java class that demonstrates the JPO-style business-rule concept.

## 12. What is MQL?

The project contains `SimulatedMQL.java`.

It demonstrates a conceptual query such as:

```text
status == Draft
```

using Java Streams.

It is not a real MQL engine.

## 13. What is TCL?

`tcl/ecr_trigger.tcl` demonstrates transition validation using TCL procedures and arrays.

It is a simulation, not a deployed ENOVIA trigger.

## 14. Explain supplier integration

```text
POST /api/parts/sync
 ↓
PartSyncServlet
 ↓
PartSupplierClient
 ↓
FakeStoreAPI
 ↓
JSON
 ↓
Part objects
 ↓
PartRepository
```

The UI/backend is separated from the external API through a dedicated client class.

## 15. Why FakeStoreAPI?

> "It is used as an accessible external REST API to demonstrate supplier/part integration without depending on a real company supplier system."

## 16. Is SOAP implemented?

No.

Correct answer:

> "SOAP is documented as an enterprise integration concept, but the implemented external integration in this project is REST-based."

## 17. Is ENOVIA implemented?

No.

Correct answer:

> "The project demonstrates ENOVIA/3DEXPERIENCE concepts such as Admin Objects, JPO-style triggers, MQL and TCL, but it does not connect to a real ENOVIA/3DEXPERIENCE server."

## 18. Does the project use a database?

No.

The current repositories use Java `ArrayList` collections.

## 19. What happens when the server restarts?

In-memory repository data is lost.

This is a known limitation of the demonstration implementation.

## 20. What is the role of Tomcat?

Tomcat runs the Java Servlet/JSP web application and serves the generated WAR.

## 21. Why Maven?

Maven manages:

- Java build
- dependencies
- compilation
- WAR packaging

## 22. Strong architecture answer

> "The application follows a layered architecture. The Vue frontend communicates through Axios with Java Servlet REST endpoints. The servlet layer delegates ECR operations to repositories and uses a configuration-driven trigger for workflow validation. Workflow transitions are loaded from an Admin Object XML file. Separately, JSP administrative screens use Servlet MVC, with servlets acting as controllers, JavaBeans/repositories as the model/data layer, and JSP with JSTL and EL as the view. Parts are stored separately and can be synchronized from an external REST supplier service. MQL and TCL are included as learning simulations, while SOAP and real ENOVIA integration are not implemented."

## 23. Avoid saying

Do not say:

- "We connected to ENOVIA."
- "We implemented real MQL."
- "We deployed a real JPO."
- "We implemented a SOAP service."
- "We have a production database."
- "Vue displays linked parts."
- "JSP and Vue share one persistent repository."

Those statements are not supported by the current code.
