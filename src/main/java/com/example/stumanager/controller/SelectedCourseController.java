package com.example.stumanager.controller;

import com.example.stumanager.domain.SelectedCourse;
import com.example.stumanager.domain.Stu;
import com.example.stumanager.service.SelectedCourseService;
import com.example.stumanager.util.AjaxResult;
import com.example.stumanager.util.Const;
import com.example.stumanager.util.PageBean;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/selectedCourse")
public class SelectedCourseController {
    private static final Logger logger = LoggerFactory.getLogger(SelectedCourseController.class);

    @Autowired
    private SelectedCourseService selectedCourseService;

    @GetMapping("/selectedCourse_list")
    public String selectedCourseList() {
        return "course/selectedCourseList";
    }

    /**
     * 异步加载选课信息列表
     *
     * @param page      当前页码，默认值为1
     * @param rows      每页显示的记录数，默认值为100
     * @param studentid 学生ID，用于筛选特定学生的选课信息
     * @param courseid  课程ID，用于筛选特定课程的选课信息
     * @param from      请求来源标识，用于区分不同的请求场景
     * @param session   HTTP会话对象，用于获取当前登录用户信息
     * @return 返回分页数据或数据列表，取决于请求来源
     */
    @PostMapping("/getSelectedCourseList")
    @ResponseBody
    public Object getClazzList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "100") Integer rows,
            @RequestParam(value = "studentid", defaultValue = "0") String studentid,
            @RequestParam(value = "courseid", defaultValue = "0") String courseid,
            String from,
            HttpSession session) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pageno", page);
        paramMap.put("pagesize", rows);

        if (!"0".equals(studentid)) {
            paramMap.put("studentId", studentid);
        }

        if (!"0".equals(courseid)) {
            paramMap.put("courseId", courseid);
        }

        // 判断是老师还是学生权限
        Stu student = (Stu) session.getAttribute(Const.STUDENT);
        if (!ObjectUtils.isEmpty(student)) {
            // 是学生权限，只能查询自己的信息
            paramMap.put("studentid", student.getId());
        }

        PageBean<SelectedCourse> pageBean = selectedCourseService.queryPage(paramMap);

        if (!ObjectUtils.isEmpty(from) && "combox".equals(from)) {
            return pageBean.getDatas();
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("total", pageBean.getTotalsize());
            result.put("rows", pageBean.getDatas());
            return result;
        }
    }

    /**
     * 学生进行选课
     *
     * @param selectedCourse 选课信息对象
     * @return 返回操作结果，包含成功状态和消息
     */
    @PostMapping("/addSelectedCourse")
    @ResponseBody
    public AjaxResult addSelectedCourse(SelectedCourse selectedCourse) {
        AjaxResult ajaxResult = new AjaxResult();

        try {
            int count = selectedCourseService.addSelectedCourse(selectedCourse);

            if (count == 1) {
                ajaxResult.setSuccess(true);
                ajaxResult.setMessage("选课成功");
            } else if (count == 0) {
                ajaxResult.setSuccess(false);
                ajaxResult.setMessage("选课人数已满");
            } else if (count == 2) {
                ajaxResult.setSuccess(false);
                ajaxResult.setMessage("已选择这门课程");
            }
        } catch (Exception e) {
            logger.error("选课操作发生异常", e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("系统内部出错，请联系管理员!");
        }

        return ajaxResult;
    }

    /**
     * 删除选课信息
     *
     * @param id 选课记录ID
     * @return 返回操作结果，包含成功状态和消息
     */
    @PostMapping("/deleteSelectedCourse")
    @ResponseBody
    public AjaxResult deleteSelectedCourse(Integer id) {
        AjaxResult ajaxResult = new AjaxResult();

        try {
            int count = selectedCourseService.deleteSelectedCourse(id);

            if (count > 0) {
                ajaxResult.setSuccess(true);
                ajaxResult.setMessage("移除成功");
            } else {
                ajaxResult.setSuccess(false);
                ajaxResult.setMessage("移除失败");
            }
        } catch (Exception e) {
            logger.error("删除选课信息发生异常，ID: {}", id, e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("系统内部出错，请联系管理员!");
        }

        return ajaxResult;
    }
}
