package com.example.stumanager.service;

import com.example.stumanager.domain.Attendance;
import com.example.stumanager.util.PageBean;

import java.util.Map;

public interface AttendanceService {
    PageBean<Attendance> queryPage(Map<String, Object> paramMap);

    boolean isAttendance(Attendance attendance);

    int addAttendance(Attendance attendance);

    int deleteAttendance(Integer id);
}
