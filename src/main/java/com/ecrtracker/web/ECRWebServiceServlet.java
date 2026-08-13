package com.ecrtracker.web;

import com.ecrtracker.config.AdminObjectConfigReader;
import com.ecrtracker.exception.InvalidStatusTransitionException;
import com.ecrtracker.model.ECR;
import com.ecrtracker.repository.ECRRepository;
import com.ecrtracker.trigger.ECRTriggerJPO;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/ecrs/*")
public class ECRWebServiceServlet extends HttpServlet {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ECRRepository repository =
            new ECRRepository();

    private final ECRTriggerJPO trigger =
            new ECRTriggerJPO();

    private final Map<String, List<String>> allowedTransitions;

    public ECRWebServiceServlet() {

        AdminObjectConfigReader configReader =
                new AdminObjectConfigReader();

        allowedTransitions =
                configReader.loadTransitions();
    }

    // =========================
    // GET /api/ecrs
    // =========================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        List<ECR> ecrs = repository.getAll();

        String json =
                objectMapper.writeValueAsString(ecrs);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(json);
    }

 // =========================
// POST /api/ecrs
// =========================

@Override
protected void doPost(
        HttpServletRequest request,
        HttpServletResponse response)
        throws ServletException, IOException {

    ECR ecr;

    try {

        ecr = objectMapper.readValue(
                request.getReader(),
                ECR.class
        );

    } catch (Exception e) {

        sendError(
                response,
                HttpServletResponse.SC_BAD_REQUEST,
                "Invalid JSON request"
        );

        return;
    }

    // =========================
    // Validate required fields
    // =========================

    if (ecr.getTitle() == null ||
            ecr.getTitle().trim().isEmpty()) {

        sendError(
                response,
                HttpServletResponse.SC_BAD_REQUEST,
                "title is required"
        );

        return;
    }

    if (ecr.getRequestedBy() == null ||
            ecr.getRequestedBy().trim().isEmpty()) {

        sendError(
                response,
                HttpServletResponse.SC_BAD_REQUEST,
                "requestedBy is required"
        );

        return;
    }

    if (ecr.getPriority() == null ||
            ecr.getPriority().trim().isEmpty()) {

        sendError(
                response,
                HttpServletResponse.SC_BAD_REQUEST,
                "priority is required"
        );

        return;
    }

    // =========================
    // Validate priority value
    // =========================

    String priority =
            ecr.getPriority().trim().toUpperCase();

    if (!priority.equals("LOW") &&
            !priority.equals("MEDIUM") &&
            !priority.equals("HIGH")) {

        sendError(
                response,
                HttpServletResponse.SC_BAD_REQUEST,
                "priority must be LOW, MEDIUM, or HIGH"
        );

        return;
    }

    ecr.setPriority(priority);

    // =========================
    // Backend controls these
    // =========================

    // Client cannot decide the ID
    ecr.setId(null);

    // New ECR always starts as Draft
    ecr.setStatus("Draft");

    // Backend generates dateCreated
    ecr.setDateCreated(null);

    // =========================
    // Save
    // =========================

    repository.save(ecr);

    // =========================
    // Return created ECR
    // =========================

    String json =
            objectMapper.writeValueAsString(ecr);

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    response.setStatus(
            HttpServletResponse.SC_CREATED
    );

    response.getWriter().write(json);
}

    // =========================
    // PUT /api/ecrs/{id}/status
    // =========================

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        // Example:
        // /api/ecrs/103/status
        //
        // pathInfo:
        // /103/status

        if (pathInfo == null ||
                !pathInfo.matches("/\\d+/status")) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid URL. Expected /api/ecrs/{id}/status"
            );

            return;
        }

        String[] pathParts =
                pathInfo.split("/");

        Long id =
                Long.parseLong(pathParts[1]);

        // -------------------------
        // Read JSON request
        // -------------------------

        Map<String, String> requestBody =
                objectMapper.readValue(
                        request.getReader(),
                        Map.class
                );

        String newStatus =
                requestBody.get("status");

        if (newStatus == null ||
                newStatus.trim().isEmpty()) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "status is required"
            );

            return;
        }

        // -------------------------
        // Find ECR
        // -------------------------

        ECR ecr =
                repository.getById(id);

        if (ecr == null) {

            sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "ECR not found: " + id
            );

            return;
        }

        // -------------------------
        // Validate + update
        // -------------------------

        try {

            repository.updateStatus(
                    id,
                    newStatus,
                    trigger,
                    allowedTransitions
            );

        } catch (InvalidStatusTransitionException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

            return;
        }

        // -------------------------
        // Return updated ECR
        // -------------------------

        String json =
                objectMapper.writeValueAsString(
                        repository.getById(id)
                );

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.setStatus(
                HttpServletResponse.SC_OK
        );

        response.getWriter().write(json);
    }

    // =========================
    // Error response helper
    // =========================

    private void sendError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        Map<String, String> error =
                new HashMap<>();

        error.put("error", message);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.setStatus(status);

        response.getWriter().write(
                objectMapper.writeValueAsString(error)
        );
    }
}