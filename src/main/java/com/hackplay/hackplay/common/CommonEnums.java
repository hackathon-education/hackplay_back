package com.hackplay.hackplay.common;

public class CommonEnums {
    public enum Role {
        PLAN("기획"),
        FRONT("프론트"),
        DESIGN("디자이너"),
        BACK("백");

        private final String description;

        Role(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum Status {
        ACTIVE("활성"),
        DEACTIVE("비활성"),
        DELETED("삭제됨");

        private final String description;

        Status(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
    
    public enum Auth {
        ADMIN("관리자"),
        USER("일반");

        private final String description;

        Auth(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum SubmissionStatus {
        PENDING("대기"),
        PASS("PASS"),
        FAIL("FAIL");

        private final String description;

        SubmissionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum Lecture {
        PROJECT(
            "실전 프로젝트 해보기",
            4,
            Role.FRONT,
            Level.INTERMEDIATE
        );

        private final String title;       // 강의명
        private final int totalWeek;      // 전체 주차 수
        private final Role role;          // 담당 역할(Front/Back/Full/공통 등)
        private final Level level;        // 난이도(초급/중급/고급)

        Lecture(String title, int totalWeek, Role role, Level level) {
            this.title = title;
            this.totalWeek = totalWeek;
            this.role = role;
            this.level = level;
        }

        public String getTitle() {
            return title;
        }

        public int getTotalWeek() {
            return totalWeek;
        }

        public Role getRole() {
            return role;
        }

        public Level getLevel() {
            return level;
        }
    }

    public enum Level {
        BASIC,
        INTERMEDIATE,
        ADVANCED
    }
}
