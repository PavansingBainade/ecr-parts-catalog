# ECR Parts Catalog — Architecture & Technical Documentation

## 1. Project Overview

The ECR Parts Catalog is a full-stack web application for managing
Engineering Change Requests (ECRs).

The application demonstrates an enterprise-style architecture where users
can create ECRs, view ECRs, inspect ECR details, and move ECRs through a
controlled status workflow.

The project also demonstrates concepts related to the
3DEXPERIENCE / ENOVIA development ecosystem, including:

- REST APIs
- JPO-style business logic
- Trigger concepts
- Admin Object configuration
- TCL scripting
- MQL concepts
- SOAP integration concepts
- Maven
- Tomcat
- Git
- Vue.js
- Vue Router
- Pinia

---

# 2. Technology Stack

## Frontend

- Vue.js
- Vue Router
- Pinia
- Axios
- HTML
- CSS
- JavaScript

## Backend

- Java
- Java Servlets
- Jackson
- REST API
- Maven
- Apache Tomcat

## Business Logic

- ECRTriggerJPO
- InvalidStatusTransitionException
- AdminObjectConfigReader
- adminObjects.xml

## Supporting Enterprise Concepts

- TCL
- MQL
- SOAP
- 3DEXPERIENCE / ENOVIA concepts

## Version Control

- Git
- GitHub

---

# 3. High-Level Architecture

The application follows a layered architecture.

```text
                    USER
                     |
                     v
            +------------------+
            |   Vue.js UI      |
            |                  |
            | ECR Dashboard    |
            | ECR Detail       |
            | Create ECR       |
            +--------+---------+
                     |
                     | Axios / HTTP
                     v
            +------------------+
            | Java REST API    |
            |                  |
            | Servlet          |
            +--------+---------+
                     |
                     v
            +------------------+
            | ECR Repository   |
            |                  |
            | In-memory list   |
            +--------+---------+
                     |
                     v
            +------------------+
            | Business Logic   |
            |                  |
            | ECRTriggerJPO    |
            +--------+---------+
                     |
                     v
            +------------------+
            | Admin Config     |
            |                  |
            | adminObjects.xml |
            +------------------+
````

---

# 4. Frontend Architecture

The Vue application is responsible for the user interface and user
interaction.

Main frontend components include:

```text
src/
|
+-- views/
|   |
|   +-- ECRListView.vue
|   +-- ECRDetailView.vue
|   +-- ECRForm.vue
|
+-- stores/
|   |
|   +-- ecrStore.js
|
+-- router/
|   |
|   +-- index.js
|
+-- services/
    |
    +-- api.js
```

## ECRListView

The list view displays ECR records in a table.

It provides:

* ECR ID
* Title
* Status
* Priority
* Requested By
* Date Created
* Status filtering
* Navigation to ECR details

ECR IDs use Vue Router links to navigate to:

```
/ecrs/{id}
```

---

## ECRDetailView

The detail view displays a single ECR.

It shows:

* ECR title
* Description
* Status
* Priority
* Requested By
* Date Created
* Workflow state
* Valid status transition buttons

The available status buttons are determined by the current status.

Example:

```text
Draft
  |
  v
InReview
 /      \
v        v
Approved Rejected
            |
            v
          Draft
```

---

## ECRForm

The create form allows users to create a new ECR.

Fields include:

* Title
* Description
* Priority

The form sends a POST request to:

```
POST /api/ecrs
```

The backend generates:

* ID
* Status
* Date Created

New ECRs always start in:

```
Draft
```

---

# 5. Pinia State Management

Pinia is used to manage shared ECR state.

The store contains:

```text
ecrs
loading
error
```

It also provides:

```text
draftCount
```

which counts ECRs whose status is:

```
Draft
```

The store also contains actions such as:

```text
fetchECRs()
updateStatus()
```

Using Pinia avoids passing ECR data through multiple components using
props and events.

The list and detail views can access the same shared ECR state.

---

# 6. Vue Router

Vue Router provides client-side navigation.

Main routes:

```text
/                  → ECRListView
/ecrs/:id          → ECRDetailView
/create            → ECRForm
```

Example:

```text
http://localhost:5173/ecrs/101
```

The `:id` value is read using Vue Router's `useRoute()`.

---

# 7. REST API

The Java backend exposes REST endpoints.

## Get ECRs

```text
GET /api/ecrs
```

Returns the available ECR records.

---

## Create ECR

```text
POST /api/ecrs
```

Example request:

```json
{
  "title": "Wheel Design Change",
  "description": "Update wheel assembly",
  "priority": "HIGH",
  "requestedBy": "Pavan"
}
```

The backend controls:

```text
id
status
dateCreated
```

The new ECR starts in:

```text
Draft
```

---

## Update Status

```text
PUT /api/ecrs/{id}/status
```

Example:

```json
{
  "status": "InReview"
}
```

The backend validates the transition before updating the ECR.

---

# 8. Complete Request Flow

## Create ECR

```text
User
 |
 | Submit Create ECR form
 v
Vue ECRForm
 |
 | POST /api/ecrs
 v
ECRWebServiceServlet
 |
 | Validate request
 v
ECRRepository.save()
 |
 | Generate ID
 | Set Draft
 | Generate date
 v
Response 201 Created
 |
 v
Vue
 |
 v
ECR List
```

---

# 9. Status Update Flow

The most important business flow in the application is the status update.

```text
User clicks "Approve"
        |
        v
ECRDetailView.vue
        |
        v
Pinia ecrStore.updateStatus()
        |
        v
Axios
        |
        | PUT /api/ecrs/{id}/status
        v
ECRWebServiceServlet
        |
        v
ECRRepository.updateStatus()
        |
        v
ECRTriggerJPO.validateTransition()
        |
        v
adminObjects.xml
        |
        v
Allowed transition?
       / \
     YES  NO
      |    |
      |    v
      |  Exception
      |    |
      v    v
 Update   HTTP 400
 ECR      Error
      |
      v
 HTTP 200
      |
      v
 Vue UI
```

---

# 10. ECRTriggerJPO

`ECRTriggerJPO` contains the status transition validation logic.

Its responsibility is to determine whether a requested transition is
allowed.

Conceptually:

```text
Current Status
      |
      v
Allowed Transitions
      |
      v
Requested New Status
      |
      v
Valid?
 /    \
YES    NO
 |      |
 v      v
Update  Exception
```

If the transition is invalid, the application throws:

```text
InvalidStatusTransitionException
```

The servlet converts the error into an HTTP 400 response.

The Vue application displays the backend error to the user.

---

# 11. Admin Object Configuration

The workflow is configured in:

```text
src/main/resources/adminObjects.xml
```

Current workflow:

```text
Draft → InReview

InReview → Approved
InReview → Rejected

Rejected → Draft
```

Approved has no outgoing transition.

This configuration is loaded by:

```text
AdminObjectConfigReader
```

The servlet passes the configuration to the trigger validation logic.

This separates workflow configuration from the core validation code.

---

# 12. Repository Layer

`ECRRepository` is responsible for storing and retrieving ECR objects.

The current implementation uses an in-memory:

```java
List<ECR>
```

The repository provides operations such as:

```text
getAll()
getById()
save()
updateStatus()
getByStatus()
getAllTitles()
```

The current implementation is intentionally simple and does not use a
database.

If this were extended into a production system, the repository could be
replaced with a database-backed persistence layer without changing the
frontend architecture.

---

# 13. Backend Validation

The backend is responsible for validating create requests.

Required fields include:

```text
title
requestedBy
priority
```

Priority must be one of:

```text
LOW
MEDIUM
HIGH
```

The client cannot control:

```text
id
status
dateCreated
```

These values are controlled by the backend.

This prevents a client from creating an ECR such as:

```json
{
  "id": 9999,
  "status": "Approved"
}
```

and bypassing the workflow.

---

# 14. TCL Trigger Simulation

The project contains:

```text
tcl/ecr_trigger.tcl
```

This script demonstrates the TCL representation of the ECR status
transition logic.

The TCL script uses:

* Associative arrays
* Procedures
* `lsearch`
* Status transition validation

Example:

```text
Draft → InReview
```

is valid.

While:

```text
Draft → Approved
```

is invalid.

The TCL script is a simulation for learning purposes.

It is not executed inside a real ENOVIA/3DEXPERIENCE server.

---

# 15. Simulated MQL

The project contains:

```text
src/main/java/com/ecrtracker/mql/SimulatedMQL.java
```

This demonstrates the concept of querying business objects by field and
value.

Example:

```java
queryByField(ecrs, "status", "Draft");
```

This conceptually represents an MQL query such as:

```text
temp query bus ECR * *
where "status == Draft";
```

The implementation uses Java Streams instead of a real MQL interpreter.

Therefore:

```text
SimulatedMQL = learning/demo implementation
```

and not:

```text
Real ENOVIA MQL
```

---

# 16. SOAP Integration

The project contains documentation describing how the ECR status update
could be exposed through SOAP.

Documentation:

```text
docs/SOAP-Integration.md
```

The actual application uses REST.

SOAP is documented only as an enterprise integration concept.

A conceptual SOAP operation could be:

```text
updateECRStatus
```

which would ultimately invoke the same business validation logic.

---

# 17. Maven

Maven is used as the Java project's build system.

Important commands:

```text
mvn clean package
```

This:

* Cleans previous build output
* Compiles Java sources
* Runs tests if configured
* Packages the application

The final application is packaged as a WAR file for deployment to Tomcat.

---

# 18. Apache Tomcat

Apache Tomcat hosts the Java web application.

Deployment flow:

```text
Java Source
    |
    v
Maven
    |
    v
WAR file
    |
    v
Apache Tomcat
    |
    v
REST API
```

The frontend runs separately through the Vue development server.

Example:

```text
Vue:
http://localhost:5173

Backend:
http://localhost:8080
```

---

# 19. Git and GitHub

Git is used for source control.

Typical workflow:

```text
git status

git add .

git commit -m "Description of changes"

git push origin main
```

The project is maintained in GitHub so that changes can be tracked and
the project can be shared with the team.

---

# 20. Real vs Simulated Components

| Component                       | Status          |
| ------------------------------- | --------------- |
| Vue.js frontend                 | Implemented     |
| Vue Router                      | Implemented     |
| Pinia                           | Implemented     |
| Axios REST calls                | Implemented     |
| Java Servlet API                | Implemented     |
| ECRRepository                   | Implemented     |
| ECRTriggerJPO                   | Implemented     |
| Admin Objects configuration     | Implemented     |
| Maven build                     | Implemented     |
| Tomcat deployment               | Implemented     |
| Git/GitHub                      | Implemented     |
| TCL trigger                     | Simulated       |
| MQL                             | Simulated       |
| SOAP                            | Documented only |
| Real ENOVIA/3DEXPERIENCE server | Not used        |

This distinction is important when presenting the project.

---

# 21. Design Decisions

## Backend Controls Important Fields

The backend generates:

```text
ID
Status
Date Created
```

This prevents clients from bypassing business rules.

---

## Workflow Configuration

Transitions are configured in:

```text
adminObjects.xml
```

rather than being hardcoded entirely inside the UI.

The Vue application uses the same workflow concept to display only valid
actions.

The backend remains the final authority.

---

## Pinia Instead of Passing Props

Pinia is used because ECR data is shared between multiple views.

Without shared state, components would need to pass data through props and
events.

Pinia provides a central store for:

```text
ECR list
Loading state
Error state
Actions
Derived state
```

---

# 22. Error Handling

The application handles errors across layers.

Example invalid transition:

```text
Vue
 |
 v
Axios
 |
 v
Servlet
 |
 v
ECRTriggerJPO
 |
 v
InvalidStatusTransitionException
 |
 v
HTTP 400
 |
 v
Axios error
 |
 v
Vue error message
```

Example:

```text
Invalid status transition: Draft -> Approved
```

The error generated by the backend is surfaced to the user interface.

---

# 23. Future Improvements

Possible production improvements include:

* Database persistence
* Authentication and authorization
* Role-based workflow permissions
* Real ENOVIA/3DEXPERIENCE integration
* Real MQL execution
* Real JPO deployment
* Real TCL triggers
* SOAP implementation if required by an external system
* Automated tests
* Docker deployment
* CI/CD pipeline
* Production logging
* Audit history
* Pagination
* Search
* Advanced filtering

---

# 24. Learning Outcome

This project demonstrates the complete flow from a modern frontend to
backend business logic.

The most important architecture to understand is:

```text
Vue
 ↓
Axios
 ↓
REST Servlet
 ↓
Repository
 ↓
JPO-style Business Logic
 ↓
Admin Configuration
```

The supporting enterprise concepts are:

```text
TCL
MQL
SOAP
ENOVIA / 3DEXPERIENCE
```

The project therefore provides a practical bridge between modern
full-stack development and enterprise PLM concepts.