package com.hackplay.hackplay.service;

import org.springframework.stereotype.Component;

import com.hackplay.hackplay.common.CommonEnums.ProjectType;

@Component
public class ProjectRunCommandResolver {

    public String resolve(ProjectType type) {

        return switch (type) {
            case REACT -> "npm run dev";
            case SPRING -> "./gradlew bootRun";
            case PYTHON -> "python main.py";
        };
    }
}
