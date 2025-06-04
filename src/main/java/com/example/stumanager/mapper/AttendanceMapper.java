package com.example.stumanager.mapper;

import com.example.stumanager.domain.Attendance;

import java.util.List;
import java.util.Map;

public interface AttendanceMapper {
    List<Attendance> queryList(Map<String, Object> paramMap);

    Integer queryCount(Map<String, Object> paramMap);

    int addAttendance(Attendance attendance);

    Attendance isAttendance(Attendance attendance);

    int deleteAttendance(Integer id);
}
