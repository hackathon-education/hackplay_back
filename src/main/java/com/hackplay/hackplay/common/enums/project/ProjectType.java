package com.hackplay.hackplay.common.enums.project;

public enum ProjectType {
    REACT("React", "프론트엔드 - React"),
    SPRING("Spring", "백엔드 - Spring Boot"),
    PYTHON("Python", "백엔드 - Python"),
    VUE("Vue", "프론트엔드 - Vue"),
    NODE("Node.js", "백엔드 - Node.js");

    private final String name;
    private final String description;

    ProjectType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static ProjectType fromString(String value) {
        try {
            return ProjectType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid project type: " + value);
        }
    }

    // 프론트엔드 타입 확인
    public boolean isFrontend() {
        return this == REACT || this == VUE;
    }

    // 백엔드 타입 확인
    public boolean isBackend() {
        return this == SPRING || this == PYTHON || this == NODE;
    }

    // 런타임 환경에 ��는 커맨드 반환
    public String getRunCommand() {
        return switch (this) {
            case REACT -> "npm run dev";
            case SPRING -> "gradle bootRun";
            case PYTHON -> "python main.py";
            case VUE -> "npm run serve";
            case NODE -> "npm start";
        };
    }

    // 빌드 커맨드
    public String getBuildCommand() {
        return switch (this) {
            case REACT -> "npm run build";
            case SPRING -> "gradle build";
            case PYTHON -> "python setup.py build";
            case VUE -> "npm run build";
            case NODE -> "npm run build";
        };
    }
}