package com.example.stumanager.mapper;

import com.example.stumanager.domain.Score;
import com.example.stumanager.domain.ScoreStats;

import java.util.List;
import java.util.Map;

public interface ScoreMapper {
    List<Score> queryList(Map<String, Object> paramMap);

    Integer queryCount(Map<String, Object> paramMap);

    int addScore(Score score);

    Score isScore(Score score);

    int editScore(Score score);

    int deleteScore(Integer id);

    List<Score> getAll(Score score);

    ScoreStats getAvgStats(Integer courseid);
}
