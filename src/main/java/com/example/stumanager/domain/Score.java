package com.example.stumanager.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Score {
    private Integer id;
    private Integer studentId;
    private Integer courseId;
    private double score;
    private String remark;

    //关联表
    private String courseName;
    private String studentName;
}
