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

@WebServlet("/admin/ecrs")
public class ECRAdminServlet extends HttpServlet {

    private ECRRepository repository;

    @Override
    public void init() throws ServletException {

        repository = new ECRRepository();

        // Sample data
        ECR ecr1 = new ECR();
        ecr1.setId(101L);
        ecr1.setTitle("Brake Design Change");
        ecr1.setDescription("Update brake assembly");
        ecr1.setStatus("Draft");
        ecr1.setPriority("HIGH");
        ecr1.setRequestedBy("Pavan");
        ecr1.setDateCreated("2026-08-10");

        repository.save(ecr1);

        ECR ecr2 = new ECR();
        ecr2.setId(102L);
        ecr2.setTitle("Engine Component Update");
        ecr2.setDescription("Update engine component");
        ecr2.setStatus("InReview");
        ecr2.setPriority("MEDIUM");
        ecr2.setRequestedBy("Rahul");
        ecr2.setDateCreated("2026-08-10");

        repository.save(ecr2);
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Get all ECRs from repository
        List<ECR> ecrList =
                repository.getAll();

        // Send data to JSP
        request.setAttribute(
                "ecrList",
                ecrList
        );

        // Forward to JSP
        request.getRequestDispatcher(
                "/ecrList.jsp"
        ).forward(
                request,
                response
        );
    }
}