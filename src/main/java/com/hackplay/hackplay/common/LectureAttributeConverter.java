package com.hackplay.hackplay.common;

import com.hackplay.hackplay.common.CommonEnums.Lecture;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LectureAttributeConverter
        implements AttributeConverter<Lecture, Short> {

    @Override
    public Short convertToDatabaseColumn(Lecture lecture) {
        return lecture == null ? null : (short) lecture.getId();
    }

    @Override
    public Lecture convertToEntityAttribute(Short dbData) {
        return dbData == null ? null : Lecture.fromId(dbData);
    }
}
