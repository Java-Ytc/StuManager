package com.example.stumanager.service;

import com.example.stumanager.domain.Score;
import com.example.stumanager.domain.ScoreStats;
import com.example.stumanager.util.PageBean;

import java.util.List;
import java.util.Map;

public interface ScoreService {
    PageBean<Score> queryPage(Map<String, Object> paramMap);

    boolean isScore(Score score);

    int addScore(Score score);

    int editScore(Score score);

    int deleteScore(Integer id);

    List<Score> getAll(Score score);

    ScoreStats getAvgStats(Integer courseid);
}
