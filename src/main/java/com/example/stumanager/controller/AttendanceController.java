package com.example.stumanager.controller;

import com.example.stumanager.domain.Attendance;
import com.example.stumanager.domain.SelectedCourse;
import com.example.stumanager.domain.Stu;
import com.example.stumanager.service.AttendanceService;
import com.example.stumanager.service.CourseService;
import com.example.stumanager.service.SelectedCourseService;
import com.example.stumanager.util.AjaxResult;
import com.example.stumanager.util.Const;
import com.example.stumanager.util.DataFormatUtil;
import com.example.stumanager.util.PageBean;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {
    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);

    @Autowired
    private AttendanceService attendanceService;
    @Autowired
    private SelectedCourseService selectedCourseService;
    @Autowired
    private CourseService courseService;

    @GetMapping("/attendance_list")
    public String attendanceList(){
        return "/attendance/attendanceList";
    }

    /**
     * 异步获取考勤列表数据
     */
    @RequestMapping("/getAttendanceList")
    @ResponseBody
    public Object getAttendanceList(@RequestParam(value = "page", defaultValue = "1")Integer page,
                                    @RequestParam(value = "rows", defaultValue = "100")Integer rows,
                                    @RequestParam(value = "studentid", defaultValue = "0")String studentid,
                                    @RequestParam(value = "courseid", defaultValue = "0")String courseid,
                                    String type, String date, String from, HttpSession session){
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("pageno", page);
        paramMap.put("pagesize", rows);
        if (!"0".equals(studentid))  paramMap.put("studentid", studentid);
        if (!"0".equals(courseid))  paramMap.put("courseid", courseid);
        if (StringUtils.hasText(type))  paramMap.put("type", type);
        if (StringUtils.hasText(date))  paramMap.put("date", date);

        // 判断是老师还是学生权限
        Stu stu = (Stu) session.getAttribute(Const.STUDENT);
        if (!Objects.isNull(stu)){
            // 是学生权限，只能查询自己的信息
            paramMap.put("studentid", stu.getId());
        }
        PageBean<Attendance> pageBean = attendanceService.queryPage(paramMap);
        if (StringUtils.hasText(from) && "combox".equals(from)){
            return pageBean.getDatas();
        } else {
            Map<String,Object> result = new HashMap<>();
            result.put("total", pageBean.getTotalsize());
            result.put("rows", pageBean.getDatas());
            return result;
        }
    }

    /**
     * 通过 选课信息中的课程id 查询 学生所选择的课程
     */
    @RequestMapping("/getStudentSelectedCourseList")
    @ResponseBody
    public Object getStudentSelectedCourseList(@RequestParam(value = "studentid", defaultValue = "0")String studentid){
        // 通过学生id 查询 选课信息
        List<SelectedCourse> selectedCourseList = selectedCourseService.getAllBySid(Integer.parseInt(studentid));

        // 直接返回课程列表，避免冗余变量
        return courseService.getCourseById(selectedCourseList.stream()
                .map(SelectedCourse::getCourseId)
                .toList());
    }

    /**
     * 添加考勤签到
     */
    @PostMapping("/addAttendance")
    @ResponseBody
    public AjaxResult addAttendance(Attendance attendance){
        AjaxResult ajaxResult = new AjaxResult();
        attendance.setDate(DataFormatUtil.getFormatDate(new Date(), "yyyy-MM-dd"));
        // 判断是否已签到
        if (attendanceService.isAttendance(attendance)){
            // true为已签到
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("已签到，请勿重复签到！");
        } else {
            int count = attendanceService.addAtendance(attendance);
            if (count > 0){
                // 签到成功
                ajaxResult.setSuccess(true);
                ajaxResult.setMessage("签到成功");
            } else {
                ajaxResult.setSuccess(false);
                ajaxResult.setMessage("系统错误，请重新签到");
            }
        }
        return ajaxResult;
    }

    /**
     * 删除考勤签到
     */
    @PostMapping("/deleteAttendance")
    @ResponseBody
    public AjaxResult deleteAttendance(Integer id){
        AjaxResult ajaxResult = new AjaxResult();
        try {
            int count = attendanceService.deleteAttendance(id);
            if (count > 0){
                ajaxResult.setSuccess(true);
                ajaxResult.setMessage("删除成功");
            } else {
                ajaxResult.setSuccess(false);
                ajaxResult.setMessage("删除失败");
            }
        } catch (Exception e) {
            // 使用SLF4J记录异常
            logger.error("删除考勤记录失败，ID: {}", id, e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("系统异常,请重试");
        }
        return ajaxResult;
    }
}
