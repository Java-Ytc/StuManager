package com.example.stumanager.Impl;

import com.example.stumanager.domain.Stu;
import com.example.stumanager.mapper.StuMapper;
import com.example.stumanager.service.StuService;
import com.example.stumanager.util.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StuServiceImpl implements StuService {
    @Autowired
    private StuMapper stuMapper;

    @Override
    public PageBean<Stu> queryPage(Map<String, Object> paramMap) {
        PageBean<Stu> pageBean = new PageBean<>((Integer) paramMap.get("pageno"),(Integer) paramMap.get("pagesize"));

        Integer startIndex = pageBean.getStartIndex();
        paramMap.put("startIndex",startIndex);
        List<Stu> datas = stuMapper.queryList(paramMap);
        pageBean.setDatas(datas);

        Integer totalsize = stuMapper.queryCount(paramMap);
        pageBean.setTotalsize(totalsize);
        return pageBean;
    }

    @Override
    public int deleteStudent(List<Integer> ids) {
        return stuMapper.deleteStudent(ids);
    }

    @Override
    public int addStudent(Stu stu) {
        return stuMapper.addStudent(stu);
    }

    @Override
    public Stu findById(Integer sid) {
        return stuMapper.findById(sid);
    }

    @Override
    public int editStudent(Stu stu) {
        return stuMapper.editStudent(stu);
    }

    @Override
    public Stu findByStudent(Stu stu) {
        return stuMapper.findByStudent(stu);
    }

    @Override
    public boolean isStudentByClazzId(Integer id) {
        List<Stu> studentList = stuMapper.isStudentByClazzId(id);
        if (studentList.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public int editPswdByStudent(Stu student) {
        return stuMapper.editPswdByStudent(student);
    }

    @Override
    public int findByName(String name) {
        return stuMapper.findByName(name);
    }
}
