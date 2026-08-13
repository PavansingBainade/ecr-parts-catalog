package com.ecrtracker.mql;

import com.ecrtracker.model.ECR;

import java.util.List;
import java.util.stream.Collectors;

public class SimulatedMQL {

    /**
     * Simulates an MQL query that finds ECR objects
     * where a particular field has a particular value.
     *
     * Example conceptual MQL:
     *
     * temp query bus ECR * *
     * where "status == Draft";
     */
    public static List<ECR> queryByField(
            List<ECR> ecrs,
            String field,
            String value) {

        return ecrs.stream()
                .filter(ecr -> {

                    switch (field) {

                        case "id":
                            return String.valueOf(ecr.getId())
                                    .equals(value);

                        case "title":
                            return value.equals(ecr.getTitle());

                        case "description":
                            return value.equals(ecr.getDescription());

                        case "status":
                            return value.equals(ecr.getStatus());

                        case "priority":
                            return value.equals(ecr.getPriority());

                        case "requestedBy":
                            return value.equals(ecr.getRequestedBy());

                        case "dateCreated":
                            return value.equals(ecr.getDateCreated());

                        default:
                            return false;
                    }
                })
                .collect(Collectors.toList());
    }


    /**
     * Simple demonstration.
     */
    public static void main(String[] args) {

        ECR ecr1 = new ECR(
                101L,
                "Wheel Design Change",
                "Update wheel assembly",
                "Draft",
                "HIGH",
                "Pavan",
                "2026-08-13"
        );

        ECR ecr2 = new ECR(
                102L,
                "Engine Change",
                "Update engine component",
                "InReview",
                "MEDIUM",
                "Pavan",
                "2026-08-13"
        );

        ECR ecr3 = new ECR(
                103L,
                "Brake Change",
                "Update brake assembly",
                "Draft",
                "LOW",
                "Rahul",
                "2026-08-13"
        );

        List<ECR> ecrs =
                List.of(ecr1, ecr2, ecr3);


        // Simulated MQL:
        // Find all ECRs where status == Draft

        List<ECR> draftEcrs =
                queryByField(
                        ecrs,
                        "status",
                        "Draft"
                );

        System.out.println("Draft ECRs:");

        for (ECR ecr : draftEcrs) {
            System.out.println(ecr);
        }


        // Find ECRs requested by Pavan

        List<ECR> pavanEcrs =
                queryByField(
                        ecrs,
                        "requestedBy",
                        "Pavan"
                );

        System.out.println();
        System.out.println("ECRs requested by Pavan:");

        for (ECR ecr : pavanEcrs) {
            System.out.println(ecr);
        }
    }
}