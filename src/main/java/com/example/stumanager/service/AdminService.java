package com.example.stumanager.service;

import com.example.stumanager.domain.Admin;

public interface AdminService {
    Admin findByAdmin(Admin admin);


    int editPswdByAdmin(Admin admin);
}
