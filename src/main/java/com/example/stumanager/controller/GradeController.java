package com.example.stumanager.controller;

import com.example.stumanager.domain.Grade;
import com.example.stumanager.service.GradeService;
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
import java.util.function.Supplier;

@Controller
@RequestMapping("/grade")
public class GradeController {
    private static final Logger logger = LoggerFactory.getLogger(GradeController.class);

    @Autowired
    private GradeService gradeService;

    @GetMapping("/grade_list")
    public String gradeList() {
        return "/grade/gradeList";
    }

    @PostMapping("/getGradeList")
    @ResponseBody
    public Object getGradeList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "rows", defaultValue = "100") Integer rows,
                               String gradeName, String from) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pageno", page);
        paramMap.put("pagesize", rows);

        if (!ObjectUtils.isEmpty(gradeName)) {
            paramMap.put("name", gradeName);
        }

        PageBean<Grade> pageBean = gradeService.queryPage(paramMap);

        return "combox".equals(from) ? pageBean.getDatas() : Map.of("total", pageBean.getTotalsize(), "rows", pageBean.getDatas());
    }

    private AjaxResult handleDatabaseOperation(Supplier<Integer> operation, String action) {
        AjaxResult ajaxResult = new AjaxResult();
        try {
            int count = operation.get();
            boolean success = count > 0;
            ajaxResult.setSuccess(success);
            ajaxResult.setMessage(success ? action + "成功" : action + "失败");
        } catch (Exception e) {
            logger.error("{}操作失败: {}", action, e.getMessage(), e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage(action + "失败，服务器内部错误");
        }
        return ajaxResult;
    }

    @PostMapping("/addGrade")
    @ResponseBody
    public AjaxResult addGrade(Grade grade) {
        return handleDatabaseOperation(() -> gradeService.addGrade(grade), "添加");
    }

    @PostMapping("/deleteGrade")
    @ResponseBody
    public AjaxResult deleteGrade(IdListData data) {
        return handleDatabaseOperation(() -> data.getIds().stream().mapToInt(gradeService::deleteGrade).sum(), "删除");
    }

    @PostMapping("/editGrade")
    @ResponseBody
    public AjaxResult editGrade(Grade grade) {
        return handleDatabaseOperation(() -> gradeService.editGrade(grade), "修改");
    }
}
