package com.example.stumanager.mapper;

import com.example.stumanager.domain.SelectedCourse;

import java.util.List;
import java.util.Map;

public interface SelectedCourseMapper {
    List<SelectedCourse> queryList(Map<String, Object> paramMap);

    Integer queryCount(Map<String, Object> paramMap);

    int addSelectedCourse(SelectedCourse selectedCourse);

    SelectedCourse findBySelectedCourse(SelectedCourse selectedCourse);

    SelectedCourse findById(Integer id);

    int deleteSelectedCourse(Integer id);

    List<SelectedCourse> isStudentId(int id);

    List<SelectedCourse> getAllBySid(int studentid);
}
