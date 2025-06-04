package com.example.stumanager.Impl;

import com.example.stumanager.domain.Grade;
import com.example.stumanager.mapper.GradeMapper;
import com.example.stumanager.service.GradeService;
import com.example.stumanager.util.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GradeServiceImpl implements GradeService {
    @Autowired
    private GradeMapper gradeMapper;

    @Override
    public PageBean<Grade> queryPage(Map<String, Object> paramMap) {
        PageBean<Grade> pageBean = new PageBean<>((Integer) paramMap.get("pageno"),(Integer) paramMap.get("pagesize"));

        Integer startIndex = pageBean.getStartIndex();
        paramMap.put("startIndex",startIndex);
        List<Grade> datas = gradeMapper.queryList(paramMap);
        pageBean.setDatas(datas);

        Integer totalsize = gradeMapper.queryCount(paramMap);
        pageBean.setTotalsize(totalsize);
        return pageBean;
    }

    @Override
    public int addGrade(Grade grade) {
        return gradeMapper.addGrade(grade);
    }

    @Override
    public int deleteGrade(Integer id) {
        return gradeMapper.deleteGrade(id);
    }

    @Override
    public int editGrade(Grade grade) {
        return gradeMapper.editGrade(grade);
    }

    @Override
    public Grade findById(Integer id) {
        return gradeMapper.findById(id);
    }
}
