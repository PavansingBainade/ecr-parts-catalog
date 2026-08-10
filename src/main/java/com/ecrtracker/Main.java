package com.ecrtracker;

import com.ecrtracker.config.AdminObjectConfigReader;
import com.ecrtracker.exception.InvalidStatusTransitionException;
import com.ecrtracker.model.ECR;
import com.ecrtracker.repository.ECRRepository;
import com.ecrtracker.trigger.ECRTriggerJPO;

import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        // -------------------------
        // 1. Load allowed transitions
        // -------------------------

        AdminObjectConfigReader configReader =
                new AdminObjectConfigReader();

        Map<String, List<String>> allowedTransitions =
                configReader.loadTransitions();

        System.out.println("Allowed transitions:");
        System.out.println(allowedTransitions);


        // -------------------------
        // 2. Create repository
        // -------------------------

        ECRRepository repository =
                new ECRRepository();


        // -------------------------
        // 3. Create ECRs
        // -------------------------

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


        // -------------------------
        // 4. Display all ECRs
        // -------------------------

        System.out.println("\nAll ECRs:");

        for (ECR ecr : repository.getAll()) {
            System.out.println(ecr);
        }


        // -------------------------
        // 5. Stream filter()
        // -------------------------

        System.out.println("\nDraft ECRs:");

        List<ECR> draftEcrs =
                repository.getByStatus("Draft");

        for (ECR ecr : draftEcrs) {
            System.out.println(ecr);
        }


        // -------------------------
        // 6. Stream map()
        // -------------------------

        System.out.println("\nECR Titles:");

        List<String> titles =
                repository.getAllTitles();

        System.out.println(titles);


        // -------------------------
        // 7. Trigger/JPO validation
        // -------------------------

        ECRTriggerJPO trigger =
                new ECRTriggerJPO();
        // VALID TRANSITION

        System.out.println("\nTesting valid transition:");

        try {

            trigger.validateTransition(
                    "Draft",
                    "InReview",
                    allowedTransitions
            );

            System.out.println(
                    "Draft -> InReview : VALID"
            );

        } catch (InvalidStatusTransitionException e) {

            System.out.println(e.getMessage());
        }


        // INVALID TRANSITION

        System.out.println("\nTesting invalid transition:");

        try {

            trigger.validateTransition(
                    "Draft",
                    "Approved",
                    allowedTransitions
            );

            System.out.println(
                    "Draft -> Approved : VALID"
            );

        } catch (InvalidStatusTransitionException e) {

            System.out.println(
                    "Exception: " + e.getMessage()
            );
        }


        // -------------------------
        // 8. Repository + Trigger integration
        // -------------------------

        System.out.println("\nTesting repository status update:");

        try {

            repository.updateStatus(
                    101L,
                    "InReview",
                    trigger,
                    allowedTransitions
            );

            System.out.println("ECR 101 updated successfully.");
            System.out.println(repository.getById(101L));

        } catch (InvalidStatusTransitionException e) {

            System.out.println("Exception: " + e.getMessage());
        }


        // -------------------------
        // 9. Invalid repository update
        // -------------------------

        System.out.println("\nTesting invalid repository update:");

        try {

            repository.updateStatus(
                    101L,
                    "Draft",
                    trigger,
                    allowedTransitions
            );

        } catch (InvalidStatusTransitionException e) {

            System.out.println("Exception: " + e.getMessage());
        }
    }
}