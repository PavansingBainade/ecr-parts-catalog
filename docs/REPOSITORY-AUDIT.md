# Repository Audit – ECR Parts Catalog

## Audit scope

This audit was performed against the current public `main` branch repository and the source files relevant to application behavior, frontend routing/state, Java backend, JSP, workflow configuration, supplier integration, MQL/TCL simulations, Maven configuration, and deployment.

Repository:
`https://github.com/PavansingBainade/ecr-parts-catalog`

## Top-level structure verified

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

## Java packages verified

```text
com.ecrtracker.client
com.ecrtracker.config
com.ecrtracker.exception
com.ecrtracker.filter
com.ecrtracker.model
com.ecrtracker.mql
com.ecrtracker.repository
com.ecrtracker.trigger
com.ecrtracker.web
```

Additional Java entry/demo classes:

```text
Main.java
PartSupplierTest.java
```

## Web servlet inventory

```text
ECRAdminServlet.java
ECRDetailServlet.java
ECRWebServiceServlet.java
PartServlet.java
PartSyncServlet.java
```

## JSP inventory

```text
ecrList.jsp
ecrDetail.jsp
```

## Vue source inventory

The Vue `src` directory contains:

```text
components/
router/
services/
stores/
views/
App.vue
main.js
```

Main ECR views:

```text
ECRListView.vue
ECRForm.vue
ECRDetailView.vue
```

## Verified architecture facts

### Vue

- Vue 3 is used.
- Vue Router defines `/`, `/ecrs/:id`, and `/ecrs/new`.
- Pinia is installed and initialized.
- Axios is configured with the Tomcat REST base URL.
- ECR creation uses POST.
- Status updates use PUT.
- Detail view fetches the ECR list when necessary.

### Java

- Maven packaging is WAR.
- Java source/target is 11.
- Servlet API is provided.
- JSP API is provided.
- JSTL 1.2 is a dependency.
- Jackson Databind is used.

### JSP

- Both JSP files use JSTL.
- Both JSP files use EL.
- `ECRAdminServlet` forwards to `ecrList.jsp`.
- `ECRDetailServlet` forwards to `ecrDetail.jsp`.
- `ECRDetailServlet` calculates linked parts.
- Linked parts are rendered in `ecrDetail.jsp`.

### Workflow

Source of truth:

```text
adminObjects.xml
```

Transitions:

```text
Draft → InReview
InReview → Approved
InReview → Rejected
Rejected → Draft
```

### External API

`PartSupplierClient` calls:

```text
https://fakestoreapi.com/products
```

`PartSyncServlet` exposes:

```text
POST /api/parts/sync
```

### Simulation

- `SimulatedMQL.java` is a Java Streams simulation.
- `tcl/ecr_trigger.tcl` is a TCL simulation.
- SOAP is documentation only.
- No real ENOVIA/3DEXPERIENCE connection is present.

## Important architecture caveats discovered

### 1. In-memory repositories

The ECR and Part repositories use `ArrayList`.

There is no database persistence.

### 2. JSP repository separation

The JSP admin servlets create their own repository instances and seed sample data.

They are not the same repository instance as the REST servlet.

### 3. Linked parts

Linked parts are implemented in the Java model/repository and JSP detail flow.

They are not currently rendered in the Vue ECR detail view.

### 4. Vue Parts/Suppliers navigation

The sidebar contains disabled Parts and Suppliers items. There are no corresponding Vue routes/pages.

### 5. SOAP

No SOAP implementation exists.

## Documentation conclusion

The final documentation should explicitly describe:

1. Vue 3 SPA
2. Java Servlet REST API
3. JSP/JSTL/EL admin screens
4. Servlet/MVC architecture
5. In-memory repositories
6. XML workflow configuration
7. JPO-style Java validation
8. Part/supplier REST integration
9. Linked parts in JSP detail
10. MQL simulation
11. TCL simulation
12. SOAP as documentation only
13. Real ENOVIA/3DEXPERIENCE as not used

Any documentation claiming more than these points should be treated as inaccurate.
