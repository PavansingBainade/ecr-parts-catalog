package com.ecrtracker.web;

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
import java.util.List;

@WebServlet("/api/parts")
public class PartServlet extends HttpServlet {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        ServletContext context =
                getServletContext();

        PartRepository repository =
                (PartRepository) context.getAttribute(
                        "partRepository"
                );

        if (repository == null) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.setContentType(
                    "application/json"
            );

            response.getWriter().write(
                    "{\"error\":\"Part repository not initialized\"}"
            );

            return;
        }

        List<Part> parts =
                repository.getAll();

        String json =
                objectMapper.writeValueAsString(parts);

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.setStatus(
                HttpServletResponse.SC_OK
        );

        response.getWriter().write(json);
    }
}