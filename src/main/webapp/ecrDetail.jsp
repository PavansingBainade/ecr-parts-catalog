<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>

<head>

    <title>ECR Details</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/admin.css">

</head>

<body>

<div class="container">

    <h1>ECR Details</h1>

    <div class="detail-card">

        <h2>${ecr.title}</h2>

        <p>
            <strong>ID:</strong>
            ${ecr.id}
        </p>

        <p>
            <strong>Description:</strong>
            ${ecr.description}
        </p>

        <p>
            <strong>Status:</strong>
            ${ecr.status}
        </p>

        <p>
            <strong>Priority:</strong>
            ${ecr.priority}
        </p>

        <p>
            <strong>Requested By:</strong>
            ${ecr.requestedBy}
        </p>

        <p>
            <strong>Date Created:</strong>
            ${ecr.dateCreated}
        </p>

    </div>


    <h2>Linked Parts</h2>

    <c:choose>

        <c:when test="${empty linkedParts}">

            <p>No parts are linked to this ECR.</p>

        </c:when>

        <c:otherwise>

            <table>

                <thead>

                    <tr>
                        <th>ID</th>
                        <th>Part Number</th>
                        <th>Name</th>
                        <th>Category</th>
                        <th>Price</th>
                    </tr>

                </thead>

                <tbody>

                    <c:forEach
                            var="part"
                            items="${linkedParts}">

                        <tr>

                            <td>${part.id}</td>

                            <td>${part.partNumber}</td>

                            <td>${part.name}</td>

                            <td>${part.category}</td>

                            <td>${part.price}</td>

                        </tr>

                    </c:forEach>

                </tbody>

            </table>

        </c:otherwise>

    </c:choose>


    <br>

    <a href="${pageContext.request.contextPath}/admin/ecrs">
        ← Back to ECR List
    </a>

</div>

</body>

</html>