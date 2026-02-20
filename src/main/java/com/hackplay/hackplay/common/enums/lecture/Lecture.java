package com.hackplay.hackplay.common.enums.lecture;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.hackplay.hackplay.common.enums.member.Role;

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

    @JsonValue
    public int toJson() {
        return id;
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

    public static Lecture fromString(String value) {
        try {
            return Lecture.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid lecture: " + value);
        }
    }
}