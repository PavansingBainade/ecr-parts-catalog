package com.ecrtracker.client;

import com.ecrtracker.model.Part;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class PartSupplierClient {

    private static final String API_URL =
            "https://fakestoreapi.com/products";

    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    public PartSupplierClient() {

        httpClient = HttpClient.newHttpClient();

        objectMapper = new ObjectMapper();
    }

    public List<Part> fetchParts()
            throws IOException, InterruptedException {

        // -------------------------
        // 1. Create HTTP request
        // -------------------------

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .GET()
                        .build();

        // -------------------------
        // 2. Send HTTP request
        // -------------------------

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        // -------------------------
        // 3. Check HTTP status
        // -------------------------

        if (response.statusCode() != 200) {

            throw new IOException(
                    "Failed to fetch parts. HTTP status: "
                            + response.statusCode()
            );
        }

        // -------------------------
        // 4. Parse JSON
        // -------------------------

        JsonNode products =
                objectMapper.readTree(
                        response.body()
                );

        // -------------------------
        // 5. Convert API response
        //    into Part objects
        // -------------------------

        List<Part> parts =
                new ArrayList<>();

        for (JsonNode product : products) {

            Part part = new Part();

            part.setId(
                    product.get("id").asLong()
            );

            part.setPartNumber(
                    "PART-" + product.get("id").asText()
            );

            part.setName(
                    product.get("title").asText()
            );

            part.setCategory(
                    product.get("category").asText()
            );

            part.setPrice(
                    product.get("price").asDouble()
            );

            part.setLinkedEcrId(null);

            parts.add(part);
        }

        return parts;
    }
}