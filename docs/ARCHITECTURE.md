# Architecture – ECR Parts Catalog

## 1. Purpose

This document describes the architecture that is actually implemented in the repository.

The application has **two UI paths**:

1. A Vue 3 single-page application that consumes REST APIs.
2. JSP administrative screens that use Servlet/MVC, JSTL, and Expression Language.

The backend is a Java Servlet WAR deployed to Apache Tomcat. Data is stored in memory.

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
                  |                     |
                  +-----------> REST    |
                               API      |
                                  \     |
                                   v    v
                                Java Backend
```

## 3. Vue architecture

```text
main.js
  |
  +--> createApp(App)
  |
  +--> Pinia
  |
  +--> Vue Router
             |
             +--> /
             |     ECRListView.vue
             |
             +--> /ecrs/new
             |     ECRForm.vue
             |
             +--> /ecrs/:id
                   ECRDetailView.vue
```

### State/data flow

```text
ECRListView / ECRDetailView
            ↓
        ecrStore.js
            ↓
        services/api.js
            ↓
          Axios
            ↓
http://localhost:8080/ecr-tracker/api
```

Pinia stores ECRs in memory on the client and refreshes them through `GET /api/ecrs`.

## 4. Backend architecture

```text
HTTP
 ↓
Servlet Layer
 ├── ECRWebServiceServlet
 ├── PartServlet
 ├── PartSyncServlet
 ├── ECRAdminServlet
 └── ECRDetailServlet
 ↓
Repository / Client Layer
 ├── ECRRepository
 ├── PartRepository
 └── PartSupplierClient
 ↓
Model
 ├── ECR
 └── Part
```

Cross-cutting:

```text
CorsFilter
```

Workflow/configuration:

```text
ECRWebServiceServlet
        ↓
AdminObjectConfigReader
        ↓
adminObjects.xml
        ↓
ECRTriggerJPO
        ↓
ECRRepository.updateStatus()
```

## 5. REST ECR flow

### Read

```text
Vue
 ↓
Pinia
 ↓
Axios GET /api/ecrs
 ↓
ECRWebServiceServlet.doGet()
 ↓
ECRRepository.getAll()
 ↓
Jackson JSON
 ↓
Vue
```

### Create

```text
ECRForm.vue
 ↓
Axios POST /api/ecrs
 ↓
ECRWebServiceServlet.doPost()
 ↓
Validate title/requester/priority
 ↓
Clear client ID/status/date
 ↓
ECRRepository.save()
 ↓
Generate ID
 ↓
Set Draft
 ↓
Generate date
 ↓
Return HTTP 201 JSON
```

### Status update

```text
ECRDetailView.vue
 ↓
Pinia updateStatus()
 ↓
Axios PUT /api/ecrs/{id}/status
 ↓
ECRWebServiceServlet.doPut()
 ↓
ECRRepository.updateStatus()
 ↓
ECRTriggerJPO.validateTransition()
 ↓
allowedTransitions loaded from adminObjects.xml
 ↓
Valid? ── No → HTTP 400
   |
  Yes
   ↓
Set new status
   ↓
Return updated ECR
```

## 6. Workflow architecture

The workflow source of truth is:

```text
src/main/resources/adminObjects.xml
```

It contains:

```text
Draft      → InReview
InReview   → Approved
InReview   → Rejected
Rejected   → Draft
```

`AdminObjectConfigReader` parses the XML using DOM APIs and creates:

```java
Map<String, List<String>>
```

The trigger does not hard-code the XML itself. It receives the parsed transition map.

This gives the application a configuration-driven workflow.

## 7. JPO-style trigger

`ECRTriggerJPO` is intentionally named to represent the ENOVIA JPO concept.

Actual implementation:

```java
validateTransition(currentStatus, newStatus, allowedTransitions)
```

It checks whether the new state is contained in the configured list.

Important distinction:

```text
Real ENOVIA JPO      → Not used
Java JPO-style class → Implemented
```

## 8. JSP / Servlet MVC architecture

The project also contains a traditional Java web MVC path.

### List screen

```text
GET /admin/ecrs
      ↓
ECRAdminServlet
      ↓
ECRRepository
      ↓
request.setAttribute("ecrList", ...)
      ↓
ecrList.jsp
```

`ecrList.jsp` renders the list using JSTL and EL.

### Detail screen

```text
GET /admin/ecr?id=101
      ↓
ECRDetailServlet
      ↓
ECRRepository.getById()
      ↓
PartRepository.getAll()
      ↓
filter linkedEcrId == ecr.id
      ↓
request.setAttribute("ecr", ...)
request.setAttribute("linkedParts", ...)
      ↓
ecrDetail.jsp
```

## 9. JSP technology details

### JSP

The actual views are:

```text
src/main/webapp/ecrList.jsp
src/main/webapp/ecrDetail.jsp
```

### JSTL

The JSPs use the JSTL core tag library:

```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
```

Examples actually used:

```jsp
<c:forEach>
<c:choose>
<c:when>
<c:otherwise>
```

### Expression Language

The views access JavaBean properties using EL:

```jsp
${ecr.id}
${ecr.title}
${ecr.description}
${ecr.status}
${ecr.priority}
${ecr.requestedBy}
${ecr.dateCreated}

${part.id}
${part.partNumber}
${part.name}
${part.category}
${part.price}
```

They also use:

```jsp
${empty linkedParts}
${pageContext.request.contextPath}
```

### MVC responsibilities

| Layer | Responsibility |
|---|---|
| Servlet | Controller |
| Repository | Data access abstraction |
| ECR / Part | Model / JavaBeans |
| JSP | View |
| JSTL | View-side control structures |
| EL | View-side property access |

This is a real implemented Servlet/MVC flow.

## 10. JSP data isolation

A significant architectural detail is that the JSP servlets instantiate their own repositories and seed sample data.

Therefore:

```text
REST ECRWebServiceServlet
        |
        +--> its own ECRRepository

JSP ECRAdminServlet
        |
        +--> its own ECRRepository

JSP ECRDetailServlet
        |
        +--> its own ECRRepository
        +--> its own PartRepository
```

They are not connected to one shared database or singleton ECR repository.

This is acceptable for the project's learning/demo architecture but should not be described as persistent shared production data.

## 11. Parts integration

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
PartRepository.saveAll()
```

`PartSupplierClient` maps supplier fields to the application's `Part` model.

The supplier integration is REST, not SOAP.

## 12. Linked-part architecture

`Part.linkedEcrId` represents the association:

```text
ECR.id ←──── Part.linkedEcrId
```

The JSP detail servlet filters all parts by this relationship.

The current Vue detail view does not include linked-part rendering.

## 13. CORS architecture

`CorsFilter` is mapped to:

```text
/*
```

It permits the Vue development origin:

```text
http://localhost:5173
```

and supports:

```text
GET, POST, PUT, DELETE, OPTIONS
```

The filter also handles OPTIONS preflight requests.

## 14. MQL simulation

```text
SimulatedMQL.queryByField()
        ↓
Java Stream
        ↓
ECR field comparison
        ↓
List<ECR>
```

It demonstrates the idea of querying ECR business objects by field.

It is not a real MQL interpreter.

## 15. TCL simulation

```text
ecr_trigger.tcl
        ↓
allowedTransitions array
        ↓
validateTransition procedure
        ↓
lsearch
        ↓
valid / invalid result
```

It demonstrates trigger-style transition validation but is not deployed inside ENOVIA.

## 16. Build/deployment architecture

```text
Java source
   ↓
Maven
   ↓
WAR
   ↓
Apache Tomcat 9
   ↓
Servlets + JSP
```

The Maven project uses WAR packaging and Java source/target 11.

The Vue application runs independently during development:

```text
Vite
 ↓
localhost:5173
 ↓
Axios
 ↓
Tomcat backend :8080
```

## 17. Architecture boundaries

### Implemented

- Vue 3
- Vue Router
- Pinia
- Axios
- Java Servlets
- REST API
- Jackson
- JSP
- JSTL
- EL
- Servlet/MVC
- In-memory repositories
- XML workflow configuration
- JPO-style trigger
- external REST supplier client
- linked parts in JSP detail
- CORS filter
- MQL simulation
- TCL simulation

### Not implemented

- Real ENOVIA/3DEXPERIENCE server
- Real JPO deployment
- Real MQL execution
- Real ENOVIA TCL trigger execution
- SOAP endpoint
- Database persistence
- Authentication/authorization
- Vue Parts page
- Vue Suppliers page
