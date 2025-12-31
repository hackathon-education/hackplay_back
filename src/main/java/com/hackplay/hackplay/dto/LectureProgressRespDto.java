package com.hackplay.hackplay.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LectureProgressRespDto {
    private List<LectureWeekProgressDto> weeks;
}