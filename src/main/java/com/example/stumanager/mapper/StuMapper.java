package com.example.stumanager.mapper;

import com.example.stumanager.domain.Stu;

import java.util.List;
import java.util.Map;

public interface StuMapper {
    List<Stu> queryList(Map<String, Object> paramMap);

    Integer queryCount(Map<String, Object> paramMap);

    int deleteStudent(List<Integer> ids);

    int addStudent(Stu student);

    Stu findById(Integer sid);

    int editStudent(Stu student);

    Stu findByStudent(Stu student);

    List<Stu> isStudentByClazzId(Integer id);

    int editPswdByStudent(Stu student);

    int findByName(String name);
}
