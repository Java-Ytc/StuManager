package com.example.stumanager.mapper;

import com.example.stumanager.domain.Admin;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminMapper {
    Admin findByAdmin(Admin admin);


    int editPswdByAdmin(Admin admin);
}
