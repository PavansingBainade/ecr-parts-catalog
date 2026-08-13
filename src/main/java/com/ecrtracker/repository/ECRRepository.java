package com.ecrtracker.repository;

import com.ecrtracker.model.ECR;
import com.ecrtracker.trigger.ECRTriggerJPO;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ECRRepository {

    private final List<ECR> ecrList = new ArrayList<>();

    public List<ECR> getAll() {
        return new ArrayList<>(ecrList);
    }

    public ECR getById(Long id) {

        for (ECR ecr : ecrList) {

            if (id != null && id.equals(ecr.getId())) {
                return ecr;
            }
        }

        return null;
    }

    public void save(ECR ecr) {

    // Always generate ID on the server
    long nextId = ecrList.stream()
            .map(ECR::getId)
            .filter(id -> id != null)
            .mapToLong(Long::longValue)
            .max()
            .orElse(100L) + 1;

    ecr.setId(nextId);

    // New ECR always starts in Draft
    ecr.setStatus("Draft");

    // Generate creation date if not provided
    if (ecr.getDateCreated() == null ||
            ecr.getDateCreated().trim().isEmpty()) {

        ecr.setDateCreated(
                java.time.LocalDate.now().toString()
        );
    }

    ecrList.add(ecr);
}
    public void updateStatus(
        Long id,
        String newStatus,
        ECRTriggerJPO trigger,
        Map<String, List<String>> allowedTransitions) {

        ECR ecr = getById(id);

        if (ecr == null) {
            return;
        }

        trigger.validateTransition(
                ecr.getStatus(),
                newStatus,
                allowedTransitions
        );

        ecr.setStatus(newStatus);
    }

    public List<ECR> getByStatus(String status) {

        return ecrList.stream()
                .filter(ecr -> ecr.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    public List<String> getAllTitles() {

        return ecrList.stream()
                .map(ECR::getTitle)
                .collect(Collectors.toList());
    }
}