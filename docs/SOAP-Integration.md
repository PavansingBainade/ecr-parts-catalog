# SOAP Integration Documentation

## Purpose

The ECR Parts Catalog application currently exposes its backend functionality
through REST APIs.

This document describes how the ECR status update operation could be exposed
through SOAP for integration with a legacy enterprise system.

No SOAP service is implemented in this project.

---

## Existing REST Operation

The application currently uses:

    PUT /api/ecrs/{id}/status

Example request:

```json
{
  "status": "Approved"
}
````

Example response:

```json
{
  "id": 101,
  "title": "Wheel Design Change",
  "description": "Update wheel assembly",
  "status": "Approved",
  "priority": "HIGH",
  "requestedBy": "Pavan",
  "dateCreated": "2026-08-13"
}
```

---

## SOAP Equivalent

A SOAP service could expose an operation such as:

```
updateECRStatus
```

Example conceptual request:

```xml
<soap:Envelope
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:ecr="http://ecrtracker.com/ecr">

    <soap:Header/>

    <soap:Body>

        <ecr:updateECRStatus>

            <ecr:ecrId>101</ecr:ecrId>

            <ecr:newStatus>Approved</ecr:newStatus>

        </ecr:updateECRStatus>

    </soap:Body>

</soap:Envelope>
```

---

## Conceptual SOAP Response

A successful response could contain:

```xml
<soap:Envelope
    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:ecr="http://ecrtracker.com/ecr">

    <soap:Body>

        <ecr:updateECRStatusResponse>

            <ecr:ecrId>101</ecr:ecrId>

            <ecr:status>Approved</ecr:status>

            <ecr:message>Status updated successfully</ecr:message>

        </ecr:updateECRStatusResponse>

    </soap:Body>

</soap:Envelope>
```

---

## Error Handling

If the requested transition violates the configured workflow rules,
the SOAP service could return a SOAP Fault.

Example:

```xml
<soap:Fault>

    <faultcode>soap:Client</faultcode>

    <faultstring>
        Invalid status transition: Draft -> Approved
    </faultstring>

</soap:Fault>
```

This corresponds to the validation already implemented by the Java
`ECRTriggerJPO`.

---

## Request Flow

The conceptual integration flow would be:

```
Legacy Enterprise System
         |
         | SOAP Request
         v
    SOAP Endpoint
         |
         v
   ECR Service Layer
         |
         v
   ECRRepository
         |
         v
   ECRTriggerJPO
         |
         v
 Validate Transition
         |
         v
   Update ECR Status
         |
         v
    SOAP Response
```

---

## REST vs SOAP

| Feature        | Current Application       | SOAP Integration          |
| -------------- | ------------------------- | ------------------------- |
| Protocol       | HTTP REST                 | SOAP over HTTP            |
| Request format | JSON                      | XML                       |
| Status update  | PUT /api/ecrs/{id}/status | updateECRStatus operation |
| Validation     | ECRTriggerJPO             | Same backend validation   |
| Response       | JSON                      | XML                       |
| Implementation | Implemented               | Documentation only        |

---

## Important Note

This project does not implement a SOAP endpoint.

The SOAP examples above document a possible enterprise integration approach
while the actual application continues to use REST APIs.

The purpose is to demonstrate understanding of SOAP concepts and how the
existing ECR functionality could participate in a legacy enterprise
integration.

