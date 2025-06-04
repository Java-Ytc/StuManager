package com.example.stumanager.controller;

import com.example.stumanager.domain.Course;
import com.example.stumanager.service.CourseService;
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
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/course")
public class CourseController {
    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Autowired
    private CourseService courseService;

    /**
     * 跳转课程管理页面
     * @return 课程列表视图名称
     */
    @GetMapping("/course_list")
    public String courseList() {
        return "course/courseList";
    }

    /**
     * 异步加载课程信息列表
     * @param page 当前页码，默认值为 1
     * @param rows 每页显示记录数，默认值为 100
     * @param name 课程名称（可选，用于模糊查询）
     * @param teacherid 教师 ID（可选，默认值为 0，代表查询所有教师的课程）
     * @param from 请求来源标识（可选，若为"combox"则返回精简数据列表）
     * @return 分页数据（total/rows）或数据列表
     */
    @PostMapping("/getCourseList")
    @ResponseBody
    public Object getCourseList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "100") Integer rows,
            String name,
            @RequestParam(value = "teacherid", defaultValue = "0") String teacherid,
            String from) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pageno", page);
        paramMap.put("pagesize", rows);

        // 使用 ObjectUtils 替代已弃用的 StringUtils.isEmpty()
        if (!ObjectUtils.isEmpty(name)) {
            paramMap.put("name", name);
        }
        if (!"0".equals(teacherid)) {
            paramMap.put("teacherId", teacherid);
        }

        PageBean<Course> pageBean = courseService.queryPage(paramMap);

        // 简化条件判断，使用泛型避免原始类型
        return Objects.equals(from, "combox")
                ? pageBean.getDatas()
                : Map.of("total", pageBean.getTotalsize(), "rows", pageBean.getDatas());
    }

    /**
     * 添加课程信息
     * @param course 课程实体（包含课程名称、教师 ID 等信息）
     * @return 操作结果：成功/失败及提示消息
     */
    @PostMapping("/addCourse")
    @ResponseBody
    public AjaxResult addCourse(Course course) {
        AjaxResult ajaxResult = new AjaxResult();
        try {
            int count = courseService.addCourse(course);
            ajaxResult.setSuccess(count > 0);
            ajaxResult.setMessage(count > 0 ? "添加成功" : "添加失败");
        } catch (Exception e) {
            // 使用日志记录替代 printStackTrace()
            logger.error("添加课程失败：{}", course.getName(), e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("添加失败");
        }
        return ajaxResult;
    }

    /**
     * 修改课程信息
     * @param course 课程实体（需包含 ID 和更新后的信息）
     * @return 操作结果：成功/失败及提示消息
     */
    @PostMapping("/editCourse")
    @ResponseBody
    public AjaxResult editCourse(Course course) {
        AjaxResult ajaxResult = new AjaxResult();
        try {
            int count = courseService.editCourse(course);
            ajaxResult.setSuccess(count > 0);
            ajaxResult.setMessage(count > 0 ? "修改成功" : "修改失败");
        } catch (Exception e) {
            logger.error("修改课程失败：ID={}", course.getId(), e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("修改失败");
        }
        return ajaxResult;
    }

    /**
     * 批量删除课程
     * @param data 包含待删除课程 ID 列表的对象
     * @return 操作结果：成功/失败及提示消息
     */
    @PostMapping("/deleteCourse")
    @ResponseBody
    public AjaxResult deleteCourse(IdListData data) {
        AjaxResult ajaxResult = new AjaxResult();
        try {
            int count = courseService.deleteCourse(data.getIds());
            ajaxResult.setSuccess(count > 0);
            ajaxResult.setMessage(count > 0 ? "删除成功" : "删除失败");
        } catch (Exception e) {
            logger.error("删除课程失败：IDs={}", data.getIds(), e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("删除失败，该课程可能关联学生或考勤记录");
        }
        return ajaxResult;
    }
}
