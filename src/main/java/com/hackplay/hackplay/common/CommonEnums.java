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
}
