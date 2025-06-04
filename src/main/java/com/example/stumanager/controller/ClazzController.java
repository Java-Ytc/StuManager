package com.example.stumanager.controller;

import com.example.stumanager.domain.Clazz;
import com.example.stumanager.service.ClazzService;
import com.example.stumanager.service.StuService;
import com.example.stumanager.util.AjaxResult;
import com.example.stumanager.util.IdListData;
import com.example.stumanager.util.PageBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/clazz")
public class ClazzController {
    private static final Logger logger = LoggerFactory.getLogger(ClazzController.class);

    @Autowired
    private ClazzService clazzService;
    @Autowired
    private StuService studentService;

    /**
     * 跳转班级页面
     * @return 班级管理页面视图名称
     */
    @GetMapping("/clazz_list")
    public String clazzList() {
        return "/clazz/clazzList";
    }

    /**
     * 异步加载班级列表
     * @param page 当前页码，默认值为 1
     * @param rows 每页显示的记录数，默认值为 100
     * @param clazzName 班级名称（可选），用于模糊查询
     * @param from 请求来源标识（可选），若为"combox"则仅返回数据列表
     * @return 分页数据（total/rows）或数据列表
     */
    @PostMapping("/getClazzList")
    @ResponseBody
    public Object getClazzList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "100") Integer rows,
            String clazzName,
            String from) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pageno", page);
        paramMap.put("pagesize", rows);
        if (!ObjectUtils.isEmpty(clazzName)) paramMap.put("name", clazzName);

        PageBean<Clazz> pageBean = clazzService.queryPage(paramMap);
        return Objects.equals(from, "combox") ? pageBean.getDatas() : Map.of(
                "total", pageBean.getTotalsize(),
                "rows", pageBean.getDatas()
        );
    }

    /**
     * 添加班级
     * @param clazz 班级实体，包含班级名称等信息
     * @return 操作结果（成功/失败）及消息
     */
    @PostMapping("/addClazz")
    @ResponseBody
    public AjaxResult addClazz(Clazz clazz) {
        AjaxResult ajaxResult = new AjaxResult();
        try {
            int count = clazzService.addClazz(clazz);
            ajaxResult.setSuccess(count > 0);
            ajaxResult.setMessage(count > 0 ? "添加成功" : "添加失败");
        } catch (Exception e) {
            logger.error("添加班级失败: {}", clazz.getName(), e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("添加失败");
        }
        return ajaxResult;
    }

    /**
     * 删除班级（支持批量删除）
     * @param data 包含待删除班级ID列表的对象
     * @return 操作结果（成功/失败）及消息
     */
    @PostMapping("/deleteClazz")
    @ResponseBody
    public AjaxResult deleteClazz(IdListData data) {
        AjaxResult ajaxResult = new AjaxResult();
        try {
            List<Integer> ids = data.getIds();
            for (Integer id : ids) {
                if (!studentService.isStudentByClazzId(id)) {
                    ajaxResult.setSuccess(false);
                    ajaxResult.setMessage("无法删除,班级下存在学生");
                    return ajaxResult;
                }
            }

            int count = clazzService.deleteClazz(ids);
            ajaxResult.setSuccess(count > 0);
            ajaxResult.setMessage(count > 0 ? "删除成功" : "删除失败");
        } catch (Exception e) {
            logger.error("删除班级失败: {}", data.getIds(), e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("删除失败,该班级存在老师或学生");
        }
        return ajaxResult;
    }

    /**
     * 班级信息修改
     * @param clazz 班级实体，包含待更新的班级信息（需包含ID）
     * @return 操作结果（成功/失败）及消息
     */
    @PostMapping("/editClazz")
    @ResponseBody
    public AjaxResult editClazz(Clazz clazz) {
        AjaxResult ajaxResult = new AjaxResult();
        try {
            int count = clazzService.editClazz(clazz);
            ajaxResult.setSuccess(count > 0);
            ajaxResult.setMessage(count > 0 ? "修改成功" : "修改失败");
        } catch (Exception e) {
            logger.error("修改班级失败: {}", clazz.getId(), e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("修改失败");
        }
        return ajaxResult;
    }
}
