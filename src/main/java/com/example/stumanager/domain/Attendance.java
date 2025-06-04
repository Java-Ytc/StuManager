package com.example.stumanager.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Attendance {
    private Integer id;
    private Integer courseId;
    private Integer studentId;
    private String type;
    private String date;
}
