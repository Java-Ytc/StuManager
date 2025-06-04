package com.example.stumanager.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Stu {
    private int id;
    private String sn;//学号
    private String username;
    private String password;
    private int clazzId;
    private String sex = "男";
    private String mobile;
    private String qq;
    private String photo;//头像
}
