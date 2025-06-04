package com.example.stumanager.mapper;

import com.example.stumanager.domain.Grade;

import java.util.List;
import java.util.Map;

public interface GradeMapper {
    // 根据条件查询Grade列表
    List<Grade> queryList(Map<String, Object> paramMap);

    // 查询Grade的数量
    Integer queryCount(Map<String, Object> paramMap);

    // 添加Grade信息
    int addGrade(Grade grade);

    // 根据ID删除Grade信息
    int deleteGrade(Integer id);

    // 更新Grade信息
    int editGrade(Grade grade);

    // 根据ID查找Grade信息
    Grade findById(Integer id);
}
