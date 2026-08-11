package com.ecrtracker;

import com.ecrtracker.client.PartSupplierClient;
import com.ecrtracker.model.Part;

import java.util.List;

public class PartSupplierTest {

    public static void main(String[] args)
            throws Exception {

        PartSupplierClient client =
                new PartSupplierClient();

        List<Part> parts =
                client.fetchParts();

        System.out.println(
                "Total parts fetched: "
                        + parts.size()
        );

        System.out.println("\nFirst 5 parts:");

        for (int i = 0;
             i < Math.min(5, parts.size());
             i++) {

            System.out.println(parts.get(i));
        }
    }
}