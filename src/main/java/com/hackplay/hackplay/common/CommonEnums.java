package com.hackplay.hackplay.common;

import com.fasterxml.jackson.annotation.JsonCreator;

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
            1,
            "실전 프로젝트 해보기",
            4,
            Role.FRONT,
            Level.INTERMEDIATE
        );

        private final int id;
        private final String title;
        private final int totalWeek;
        private final Role role;
        private final Level level;

        Lecture(int id, String title, int totalWeek, Role role, Level level) {
            this.id = id;
            this.title = title;
            this.totalWeek = totalWeek;
            this.role = role;
            this.level = level;
        }

        public int getId() {
            return id;
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

        @JsonCreator
        public static Lecture fromJson(int id) {
            return fromId(id);
        }

        public static Lecture fromId(int id) {
            for (Lecture lecture : values()) {
                if (lecture.id == id) {
                    return lecture;
                }
            }
            throw new IllegalArgumentException("Invalid lecture id: " + id);
        }
    }

    public enum Level {
        BASIC,
        INTERMEDIATE,
        ADVANCED
    }

    public enum WeekProgressStatus {
        COMPLETED,
        IN_PROGRESS,
        NOT_STARTED
    }

}
