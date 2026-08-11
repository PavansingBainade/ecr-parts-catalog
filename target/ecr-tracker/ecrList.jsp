<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>

    <meta charset="UTF-8">

    <title>ECR Admin</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/admin.css">

    <script src="${pageContext.request.contextPath}/js/ecr-filter.js"
            defer></script>

</head>

<body>

<h1>ECR Admin</h1>

<!-- Status Filter -->
<div class="filter-section">

    <label for="statusFilter">
        Filter by Status:
    </label>

    <select id="statusFilter">

        <option value="ALL">All</option>
        <option value="Draft">Draft</option>
        <option value="InReview">InReview</option>
        <option value="Approved">Approved</option>
        <option value="Rejected">Rejected</option>

    </select>

</div>


<!-- ECR Table -->
<table id="ecrTable">

    <thead>

        <tr>
            <th>ID</th>
            <th>Title</th>
            <th>Status</th>
            <th>Priority</th>
            <th>Requested By</th>
            <th>Date Created</th>
        </tr>

    </thead>

    <tbody>

        <c:forEach var="ecr" items="${ecrList}">

            <tr class="ecr-row"
                data-status="${ecr.status}">

                <td>${ecr.id}</td>

                <td>${ecr.title}</td>

                <td>

                    <c:choose>

                        <c:when test="${ecr.status == 'Draft'}">
                            <span class="status draft">
                                ${ecr.status}
                            </span>
                        </c:when>

                        <c:when test="${ecr.status == 'InReview'}">
                            <span class="status in-review">
                                ${ecr.status}
                            </span>
                        </c:when>

                        <c:when test="${ecr.status == 'Approved'}">
                            <span class="status approved">
                                ${ecr.status}
                            </span>
                        </c:when>

                        <c:when test="${ecr.status == 'Rejected'}">
                            <span class="status rejected">
                                ${ecr.status}
                            </span>
                        </c:when>

                        <c:otherwise>
                            <span class="status">
                                ${ecr.status}
                            </span>
                        </c:otherwise>

                    </c:choose>

                </td>

                <td>${ecr.priority}</td>

                <td>${ecr.requestedBy}</td>

                <td>${ecr.dateCreated}</td>

            </tr>

        </c:forEach>

    </tbody>

</table>

</body>
</html>