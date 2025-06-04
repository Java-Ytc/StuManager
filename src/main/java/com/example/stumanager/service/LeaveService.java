package com.example.stumanager.service;


import com.example.stumanager.domain.Leave;
import com.example.stumanager.util.PageBean;

import java.util.Map;

public interface LeaveService {
    PageBean<Leave> queryPage(Map<String, Object> paramMap);

    int addLeave(Leave leave);

    int editLeave(Leave leave);

    int checkLeave(Leave leave);

    int deleteLeave(Integer id);
}
