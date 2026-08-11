package com.ecrtracker.repository;

import com.ecrtracker.model.Part;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PartRepository {

    private final List<Part> partList =
            new ArrayList<>();

    public List<Part> getAll() {

        return new ArrayList<>(partList);
    }

    public Part getById(Long id) {

        for (Part part : partList) {

            if (part.getId().equals(id)) {
                return part;
            }
        }

        return null;
    }

    public void save(Part part) {

        partList.add(part);
    }

    public void saveAll(List<Part> parts) {

        partList.clear();

        partList.addAll(parts);
    }

    public List<Part> getByCategory(String category) {

        return partList.stream()
                .filter(part ->
                        part.getCategory().equals(category))
                .collect(Collectors.toList());
    }
}