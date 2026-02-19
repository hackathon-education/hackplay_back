package com.hackplay.hackplay.service;

import org.springframework.stereotype.Component;

@Component
public class ProjectRunCommandResolver {

    public String resolve(String template) {

        return switch (template) {

            // ===============================
            // Frontend
            // ===============================
            case "create-react-vite" ->
                    "npm install && npm run dev";

            case "intermediate-front-week-0" ->
                    "npm install && npm run dev";

            // ===============================
            // Backend
            // ===============================
            case "create-spring-boot" ->
                    "./gradlew bootRun";

            // ===============================
            // Python
            // ===============================
            case "create-python-basic" ->
                    "python main.py";

            default ->
                    throw new IllegalArgumentException(
                        "Unknown project template: " + template
                    );
        };
    }
}
