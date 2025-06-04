package com.example.stumanager.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectedCourse {
    private Integer id;
    private Integer studentId;
    private Integer courseId;
}
