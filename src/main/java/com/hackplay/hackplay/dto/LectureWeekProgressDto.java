package com.hackplay.hackplay.dto;

import com.hackplay.hackplay.common.CommonEnums.WeekProgressStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LectureWeekProgressDto {
    private int week;
    private WeekProgressStatus status;
    private Long projectId;
}