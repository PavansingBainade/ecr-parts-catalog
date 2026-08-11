package com.ecrtracker.web;

import com.ecrtracker.model.ECR;
import com.ecrtracker.model.Part;
import com.ecrtracker.repository.ECRRepository;
import com.ecrtracker.repository.PartRepository;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/admin/ecr")
public class ECRDetailServlet extends HttpServlet {

    private ECRRepository ecrRepository;
    private PartRepository partRepository;

    @Override
    public void init() throws ServletException {

        ecrRepository = new ECRRepository();
        partRepository = new PartRepository();

        // -------------------------
        // Sample ECR 101
        // -------------------------

        ECR ecr1 = new ECR();

        ecr1.setId(101L);
        ecr1.setTitle("Brake Design Change");
        ecr1.setDescription("Update brake assembly");
        ecr1.setStatus("Draft");
        ecr1.setPriority("HIGH");
        ecr1.setRequestedBy("Pavan");
        ecr1.setDateCreated("2026-08-10");

        ecrRepository.save(ecr1);


        // -------------------------
        // Sample ECR 102
        // -------------------------

        ECR ecr2 = new ECR();

        ecr2.setId(102L);
        ecr2.setTitle("Engine Component Update");
        ecr2.setDescription("Update engine component");
        ecr2.setStatus("InReview");
        ecr2.setPriority("MEDIUM");
        ecr2.setRequestedBy("Rahul");
        ecr2.setDateCreated("2026-08-10");

        ecrRepository.save(ecr2);


        // -------------------------
        // Sample Parts
        // -------------------------

        Part part1 = new Part();

        part1.setId(1L);
        part1.setPartNumber("PART-1001");
        part1.setName("Brake Assembly");
        part1.setCategory("Mechanical");
        part1.setPrice(2500.0);
        part1.setLinkedEcrId(101L);

        partRepository.save(part1);


        Part part2 = new Part();

        part2.setId(2L);
        part2.setPartNumber("PART-1002");
        part2.setName("Brake Disc");
        part2.setCategory("Mechanical");
        part2.setPrice(1200.0);
        part2.setLinkedEcrId(101L);

        partRepository.save(part2);


        Part part3 = new Part();

        part3.setId(3L);
        part3.setPartNumber("PART-2001");
        part3.setName("Engine Mount");
        part3.setCategory("Engine");
        part3.setPrice(1800.0);
        part3.setLinkedEcrId(102L);

        partRepository.save(part3);
    }


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // -------------------------
        // 1. Get ID from URL
        // -------------------------

        String idParameter =
                request.getParameter("id");

        if (idParameter == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "ECR id is required"
            );

            return;
        }


        // -------------------------
        // 2. Convert ID to Long
        // -------------------------

        Long id;

        try {

            id = Long.parseLong(idParameter);

        } catch (NumberFormatException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid ECR id"
            );

            return;
        }


        // -------------------------
        // 3. Find ECR
        // -------------------------

        ECR ecr =
                ecrRepository.getById(id);

        if (ecr == null) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "ECR not found"
            );

            return;
        }


        // -------------------------
        // 4. Find linked Parts
        // -------------------------

        List<Part> linkedParts =
                partRepository.getAll()
                        .stream()
                        .filter(part ->
                                part.getLinkedEcrId() != null
                                && part.getLinkedEcrId().equals(ecr.getId())
                        )
                        .collect(Collectors.toList());


        // -------------------------
        // 5. Send data to JSP
        // -------------------------

        request.setAttribute(
                "ecr",
                ecr
        );

        request.setAttribute(
                "linkedParts",
                linkedParts
        );


        // -------------------------
        // 6. Forward to JSP
        // -------------------------

        request.getRequestDispatcher(
                "/ecrDetail.jsp"
        ).forward(
                request,
                response
        );
    }
}