package com.ecrtracker.repository;

import com.ecrtracker.model.ECR;

import com.ecrtracker.trigger.ECRTriggerJPO;

import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ECRRepository {

    private final List<ECR> ecrList = new ArrayList<>();

    public List<ECR> getAll() {
        return new ArrayList<>(ecrList);
    }

    public ECR getById(Long id) {

        for (ECR ecr : ecrList) {

            if (ecr.getId().equals(id)) {
                return ecr;
            }
        }

        return null;
    }

    public void save(ECR ecr) {
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