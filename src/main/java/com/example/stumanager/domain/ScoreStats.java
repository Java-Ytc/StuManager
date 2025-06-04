package com.example.stumanager.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoreStats {
    private Double max_score;
    private Double avg_score;
    private Double min_score;
    private String courseName;
}
