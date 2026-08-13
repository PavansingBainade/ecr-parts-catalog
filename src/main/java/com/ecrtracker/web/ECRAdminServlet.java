package com.ecrtracker.web;

import com.ecrtracker.model.ECR;
import com.ecrtracker.repository.ECRRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/ecrs/*")
public class ECRAdminServlet extends HttpServlet {

    private final ECRRepository repository =
            ECRRepository.getInstance();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        // /admin/ecrs
        if (pathInfo == null ||
                pathInfo.equals("/") ||
                pathInfo.isEmpty()) {

            List<ECR> ecrs = repository.getAll();

            request.setAttribute("ecrs", ecrs);

            request.getRequestDispatcher(
                    "/WEB-INF/jsp/ecrList.jsp"
            ).forward(request, response);

            return;
        }

        // /admin/ecrs/{id}
        if (pathInfo.matches("/\\d+")) {

            Long id = Long.parseLong(
                    pathInfo.substring(1)
            );

            ECR ecr = repository.getById(id);

            if (ecr == null) {

                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "ECR not found: " + id
                );

                return;
            }

            request.setAttribute("ecr", ecr);

            request.getRequestDispatcher(
                    "/WEB-INF/jsp/ecrDetail.jsp"
            ).forward(request, response);

            return;
        }

        response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Invalid admin ECR URL"
        );
    }
}