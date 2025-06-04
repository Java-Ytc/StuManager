package com.example.stumanager.controller;

import com.example.stumanager.domain.Leave;
import com.example.stumanager.service.LeaveService;
import com.example.stumanager.util.AjaxResult;
import com.example.stumanager.util.PageBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/leave")
public class LeaveController {
    private static final Logger logger = LoggerFactory.getLogger(LeaveController.class);

    @Autowired
    private LeaveService leaveService;

    /**
     * 跳转请假管理页面
     * @return 请假列表视图名称
     */
    @RequestMapping("leave_list")
    public String leaveList() {
        return "/leave/leaveList";
    }

    /**
     * 异步加载请假列表
     * @param page 当前页码，默认值为 1
     * @param rows 每页显示记录数，默认值为 100
     * @param studentid 学生 ID（可选，默认值为 0，代表查询所有学生的请假记录）
     * @param from 请求来源标识（可选，若为"combox"则返回精简数据列表）
     * @return 分页数据（total/rows）或数据列表
     */
    @PostMapping("/getLeaveList")
    @ResponseBody
    public Object getLeaveList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "100") Integer rows,
            @RequestParam(value = "studentid", defaultValue = "0") String studentid,
            String from) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pageno", page);
        paramMap.put("pagesize", rows);

        // 使用 ObjectUtils 替代已弃用的 StringUtils.isEmpty()
        if (!"0".equals(studentid)) {
            paramMap.put("studentId", studentid);
        }

        PageBean<Leave> pageBean = leaveService.queryPage(paramMap);

        // 简化条件判断，使用泛型避免原始类型
        return Objects.equals(from, "combox")
                ? pageBean.getDatas()
                : Map.of("total", pageBean.getTotalsize(), "rows", pageBean.getDatas());
    }

    /**
     * 添加学生请假条
     * @param leave 请假实体（包含学生 ID、请假时间、原因等信息）
     * @return 操作结果：成功/失败及提示消息
     */
    @PostMapping("/addLeave")
    @ResponseBody
    public AjaxResult addLeave(Leave leave) {
        AjaxResult ajaxResult = new AjaxResult();
        try {
            int count = leaveService.addLeave(leave);
            ajaxResult.setSuccess(count > 0);
            ajaxResult.setMessage(count > 0 ? "添加成功" : "添加失败");
        } catch (Exception e) {
            // 使用日志记录替代 printStackTrace()
            logger.error("添加请假条失败：学生ID={}", leave.getStudentId(), e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("系统异常，请重试");
        }
        return ajaxResult;
    }

    /**
     * 修改请假条
     * @param leave 请假实体（需包含 ID 和更新后的信息）
     * @return 操作结果：成功/失败及提示消息
     */
    @PostMapping("/editLeave")
    @ResponseBody
    public AjaxResult editLeave(Leave leave) {
        AjaxResult ajaxResult = new AjaxResult();
        try {
            int count = leaveService.editLeave(leave);
            ajaxResult.setSuccess(count > 0);
            ajaxResult.setMessage(count > 0 ? "修改成功" : "修改失败");
        } catch (Exception e) {
            logger.error("修改请假条失败：ID={}", leave.getId(), e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("系统异常，请重试");
        }
        return ajaxResult;
    }

    /**
     * 审核请假条
     * @param leave 请假实体（需包含 ID 和审核状态）
     * @return 操作结果：成功/失败及提示消息
     */
    @PostMapping("/checkLeave")
    @ResponseBody
    public AjaxResult checkLeave(Leave leave) {
        AjaxResult ajaxResult = new AjaxResult();
        try {
            int count = leaveService.checkLeave(leave);
            ajaxResult.setSuccess(count > 0);
            ajaxResult.setMessage(count > 0 ? "审批成功" : "审批失败");
        } catch (Exception e) {
            logger.error("审核请假条失败：ID={}", leave.getId(), e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("系统异常，请重试");
        }
        return ajaxResult;
    }

    /**
     * 删除请假条
     * @param id 请假条 ID
     * @return 操作结果：成功/失败及提示消息
     */
    @PostMapping("/deleteLeave")
    @ResponseBody
    public AjaxResult deleteLeave(Integer id) {
        AjaxResult ajaxResult = new AjaxResult();
        try {
            int count = leaveService.deleteLeave(id);
            ajaxResult.setSuccess(count > 0);
            ajaxResult.setMessage(count > 0 ? "删除成功" : "删除失败");
        } catch (Exception e) {
            logger.error("删除请假条失败：ID={}", id, e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("系统异常，请重试");
        }
        return ajaxResult;
    }
}
