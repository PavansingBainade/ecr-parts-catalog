package com.ecrtracker.web;

import com.ecrtracker.client.PartSupplierClient;
import com.ecrtracker.model.Part;
import com.ecrtracker.repository.PartRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/parts/sync")
public class PartSyncServlet extends HttpServlet {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private final PartSupplierClient supplierClient =
            new PartSupplierClient();

    @Override
    public void init() throws ServletException {

        ServletContext context =
                getServletContext();

        PartRepository repository =
                (PartRepository) context.getAttribute(
                        "partRepository"
                );

        if (repository == null) {

            repository =
                    new PartRepository();

            context.setAttribute(
                    "partRepository",
                    repository
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {

            // Get shared repository
            PartRepository repository =
                    (PartRepository) getServletContext()
                            .getAttribute("partRepository");

            // Call external API
            List<Part> parts =
                    supplierClient.fetchParts();

            // Store parts
            repository.saveAll(parts);

            // Create response
            Map<String, Object> result =
                    new HashMap<>();

            result.put(
                    "message",
                    "Parts synchronized successfully"
            );

            result.put(
                    "count",
                    parts.size()
            );

            response.setContentType(
                    "application/json"
            );

            response.setCharacterEncoding(
                    "UTF-8"
            );

            response.setStatus(
                    HttpServletResponse.SC_OK
            );

            response.getWriter().write(
                    objectMapper.writeValueAsString(result)
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.setContentType(
                    "application/json"
            );

            response.getWriter().write(
                    "{\"error\":\"Request was interrupted\"}"
            );

        } catch (IOException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_GATEWAY
            );

            response.setContentType(
                    "application/json"
            );

            response.getWriter().write(
                    "{\"error\":\"Failed to fetch parts from supplier\"}"
            );
        }
    }
}