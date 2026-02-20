package com.hackplay.hackplay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ImageUploadRespDto {
    private String imageUrl;
}