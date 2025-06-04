package com.example.stumanager.service;

import com.example.stumanager.domain.Grade;
import com.example.stumanager.util.PageBean;

import java.util.Map;

public interface GradeService {
    PageBean<Grade> queryPage(Map<String, Object> paramMap);

    int addGrade(Grade grade);

    int deleteGrade(Integer id);

    int editGrade(Grade grade);

    Grade findById(Integer id);
}
