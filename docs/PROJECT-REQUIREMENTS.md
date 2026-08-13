# Project Requirements and Implementation Matrix

This document separates requirements that are implemented from concepts that are simulated or documented.

## Functional requirements

| Requirement | Implementation |
|---|---|
| List ECRs | Implemented in Vue and JSP |
| Search ECRs | Implemented in Vue |
| Filter by status | Implemented in Vue and JSP admin list |
| Create ECR | Implemented in Vue + REST backend |
| Generate ECR ID | Backend |
| Initial status Draft | Backend |
| Generate creation date | Backend |
| View ECR details | Implemented in Vue and JSP |
| Change status | Implemented |
| Validate status transitions | Backend |
| Display linked parts | Implemented in JSP detail |
| Part API | Implemented |
| Supplier synchronization | Implemented through external REST API |
| JSP admin screen | Implemented |
| JSP JSTL | Implemented |
| JSP EL | Implemented |
| Servlet MVC | Implemented |

## PLM/enterprise concepts

| Concept | Status | Notes |
|---|---|---|
| Admin Objects | Implemented as XML configuration |
| JPO | Implemented as Java JPO-style simulation |
| Trigger | Implemented through Java validation |
| MQL | Simulated with Java Streams |
| TCL | Simulated with standalone TCL |
| SOAP | Documentation only |
| ENOVIA/3DEXPERIENCE | Not connected |

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

## Important distinction

The project should be presented as a **working Java/Vue web application with PLM concepts demonstrated through configuration and simulations**, not as a real ENOVIA/3DEXPERIENCE implementation.
