package com.ecrtracker;

import com.ecrtracker.config.AdminObjectConfigReader;
import com.ecrtracker.model.ECR;
import com.ecrtracker.model.Part;

import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        ECR ecr = new ECR();

        ecr.setId(101L);
        ecr.setTitle("Brake Design Change");
        ecr.setDescription("Update brake assembly");
        ecr.setStatus("Draft");
        ecr.setPriority("HIGH");
        ecr.setRequestedBy("Pavan");
        ecr.setDateCreated("2026-08-10");

        System.out.println("ECR:");
        System.out.println(ecr);

        Part part = new Part();

        part.setId(1L);
        part.setPartNumber("PART-1001");
        part.setName("Brake Assembly");
        part.setCategory("Mechanical");
        part.setPrice(2500.0);
        part.setLinkedEcrId(101L);

        System.out.println("\nPart:");
        System.out.println(part);

        AdminObjectConfigReader reader =
                new AdminObjectConfigReader();

        Map<String, List<String>> transitions =
                reader.loadTransitions();

        System.out.println("\nAllowed Transitions:");
        System.out.println(transitions);
    }
}