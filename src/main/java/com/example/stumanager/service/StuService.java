package com.example.stumanager.service;

import com.example.stumanager.domain.Stu;
import com.example.stumanager.util.PageBean;

import java.util.List;
import java.util.Map;

public interface StuService {
    PageBean<Stu> queryPage(Map<String, Object> paramMap);

    int deleteStudent(List<Integer> ids);

    int addStudent(Stu student);

    Stu findById(Integer sid);

    int editStudent(Stu student);

    Stu findByStudent(Stu student);

    boolean isStudentByClazzId(Integer next);

    int editPswdByStudent(Stu student);

    int findByName(String username);
}
