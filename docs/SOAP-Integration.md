# SOAP Integration

## Status

SOAP is **not implemented as a running endpoint** in the current project.

The implemented external integration is REST-based.

## Current external integration

```text
PartSyncServlet
      ↓
PartSupplierClient
      ↓
FakeStoreAPI
      ↓
JSON response
      ↓
Part objects
      ↓
PartRepository
```

The supplier client uses Java `HttpClient` and Jackson.

## Why SOAP is documented

SOAP is included as an enterprise integration concept because the project is intended to demonstrate PLM/enterprise integration patterns.

A future SOAP design could expose an operation such as:

```text
getParts()
```

and return a SOAP XML response.

However, the current repository does not contain:

- a SOAP servlet
- WSDL
- JAX-WS service
- SOAP client
- SOAP endpoint configuration

Therefore SOAP must be described as **conceptual/documentation only**.

## REST vs SOAP in this project

| Area | Current project |
|---|---|
| ECR API | REST |
| Part API | REST |
| Supplier integration | REST |
| SOAP endpoint | Not implemented |
| SOAP WSDL | Not implemented |
| SOAP client | Not implemented |

## Presentation answer

If asked "Did you implement SOAP?":

> "No. SOAP is documented as a possible enterprise integration approach, but the actual external integration in my implementation uses REST through Java HttpClient and FakeStoreAPI."

That is the accurate description of the current repository.
