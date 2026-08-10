package com.ecrtracker.config;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminObjectConfigReader {

    public Map<String, List<String>> loadTransitions() {

        Map<String, List<String>> transitions = new HashMap<>();

        try {
            InputStream inputStream =
                    getClass()
                            .getClassLoader()
                            .getResourceAsStream("adminObjects.xml");

            if (inputStream == null) {
                throw new RuntimeException(
                        "adminObjects.xml not found"
                );
            }

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            DocumentBuilder builder =
                    factory.newDocumentBuilder();

            Document document =
                    builder.parse(inputStream);

            NodeList transitionNodes =
                    document.getElementsByTagName("transition");

            for (int i = 0; i < transitionNodes.getLength(); i++) {

                Node node = transitionNodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element transition =
                            (Element) node;

                    String from =
                            transition.getAttribute("from");

                    String to =
                            transition.getAttribute("to");

                    transitions
                            .computeIfAbsent(
                                    from,
                                    key -> new ArrayList<>()
                            )
                            .add(to);
                }
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to read adminObjects.xml",
                    e
            );
        }

        return transitions;
    }
}