package com.example.stumanager.controller;

import com.example.stumanager.domain.Score;
import com.example.stumanager.domain.ScoreStats;
import com.example.stumanager.domain.Stu;
import com.example.stumanager.service.CourseService;
import com.example.stumanager.service.ScoreService;
import com.example.stumanager.service.StuService;
import com.example.stumanager.util.AjaxResult;
import com.example.stumanager.util.Const;
import com.example.stumanager.util.PageBean;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/score")
public class ScoreController {
    private static final Logger logger = LoggerFactory.getLogger(ScoreController.class);

    @Autowired
    private ScoreService scoreService;
    @Autowired
    private StuService stuService;
    @Autowired
    private CourseService courseService;

    @GetMapping("/score_list")
    public String scoreList(){
        return "/score/scoreList";
    }

    /**
     * 异步加载成绩数据列表
     *
     * @param page 当前页码，默认值为1
     * @param rows 每页显示的记录数，默认值为100
     * @param studentid 学生ID，默认值为0
     * @param courseid 课程ID，默认值为0
     * @param from 请求来源标识
     * @param session HTTP会话对象
     * @return 分页数据或数据列表
     */
    @RequestMapping("/getScoreList")
    @ResponseBody
    public Object getScoreList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "100") Integer rows,
            @RequestParam(value = "studentid", defaultValue = "0") String studentid,
            @RequestParam(value = "courseid", defaultValue = "0") String courseid,
            String from,
            HttpSession session) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pageno", page);
        paramMap.put("pagesize", rows);
        if (!"0".equals(studentid)) paramMap.put("studentid", studentid);
        if (!"0".equals(courseid)) paramMap.put("courseid", courseid);

        // 判断是老师还是学生权限
        Stu stu = (Stu) session.getAttribute(Const.STUDENT);
        if (!ObjectUtils.isEmpty(stu)) {
            // 是学生权限，只能查询自己的信息
            paramMap.put("studentid", stu.getId());
        }

        PageBean<Score> pageBean = scoreService.queryPage(paramMap);
        if (!ObjectUtils.isEmpty(from) && from.equals("combox")) {
            return pageBean.getDatas();
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("total", pageBean.getTotalsize());
            result.put("rows", pageBean.getDatas());
            return result;
        }
    }

    /**
     * 添加成绩
     * @param score 成绩对象
     * @return 操作结果
     */
    @PostMapping("/addScore")
    @ResponseBody
    public AjaxResult addScore(Score score) {
        AjaxResult ajaxResult = new AjaxResult();
        // 判断是否已录入成绩
        if (scoreService.isScore(score)) {
            // true为已签到
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("已录入，请勿重复录入！");
        } else {
            try {
                int count = scoreService.addScore(score);
                if (count > 0) {
                    // 签到成功
                    ajaxResult.setSuccess(true);
                    ajaxResult.setMessage("录入成功");
                } else {
                    ajaxResult.setSuccess(false);
                    ajaxResult.setMessage("系统错误，请重新录入");
                }
            } catch (Exception e) {
                logger.error("添加成绩失败", e);
                ajaxResult.setSuccess(false);
                ajaxResult.setMessage("系统异常，请稍后重试");
            }
        }
        return ajaxResult;
    }

    /**
     * 修改学生成绩
     * @param score 成绩对象
     * @return 操作结果
     */
    @PostMapping("/editScore")
    @ResponseBody
    public AjaxResult editScore(Score score) {
        AjaxResult ajaxResult = new AjaxResult();
        try {
            int count = scoreService.editScore(score);
            if (count > 0) {
                ajaxResult.setSuccess(true);
                ajaxResult.setMessage("修改成功");
            } else {
                ajaxResult.setSuccess(false);
                ajaxResult.setMessage("系统错误，请重新修改");
            }
        } catch (Exception e) {
            logger.error("修改成绩失败", e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("系统异常，请稍后重试");
        }
        return ajaxResult;
    }

    /**
     * 删除学生成绩
     * @param id 成绩ID
     * @return 操作结果
     */
    @PostMapping("/deleteScore")
    @ResponseBody
    public AjaxResult deleteScore(Integer id) {
        AjaxResult ajaxResult = new AjaxResult();
        try {
            int count = scoreService.deleteScore(id);
            if (count > 0) {
                ajaxResult.setSuccess(true);
                ajaxResult.setMessage("删除成功");
            } else {
                ajaxResult.setSuccess(false);
                ajaxResult.setMessage("系统错误，请重新删除");
            }
        } catch (Exception e) {
            logger.error("删除成绩失败", e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("系统异常，请稍后重试");
        }
        return ajaxResult;
    }

    /**
     * 导入xlsx表 并存入数据库
     * @param importScore 上传的文件
     * @param response HTTP响应
     */
    @PostMapping("/importScore")
    @ResponseBody
    public void importScore(@RequestParam("importScore") MultipartFile importScore, HttpServletResponse response) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (InputStream inputStream = importScore.getInputStream();
             XSSFWorkbook xssfWorkbook = new XSSFWorkbook(inputStream)) {

            XSSFSheet sheetAt = xssfWorkbook.getSheetAt(0);
            int count = 0;
            StringBuilder errorMsg = new StringBuilder();

            for (int rowNum = 1; rowNum <= sheetAt.getLastRowNum(); rowNum++) {
                XSSFRow row = sheetAt.getRow(rowNum); // 获取第rowNum行

                // 第0列 - 学生
                XSSFCell cell = row.getCell(0);
                if (cell == null) {
                    errorMsg.append("第").append(rowNum).append("行学生缺失！\n");
                    continue;
                }

                // 第1列 - 课程
                cell = row.getCell(1);
                if (cell == null) {
                    errorMsg.append("第").append(rowNum).append("行课程缺失！\n");
                    continue;
                }

                // 第2列 - 成绩
                cell = row.getCell(2);
                if (cell == null) {
                    errorMsg.append("第").append(rowNum).append("行成绩缺失！\n");
                    continue;
                }
                double scoreValue = cell.getNumericCellValue();

                // 第3列 - 备注
                cell = row.getCell(3);
                String remark = null;
                if (cell != null) {
                    remark = cell.getStringCellValue();
                }

                // 将学生，课程转换为id,存入数据库
                // 1)首先获取对应的id
                int studentId = stuService.findByName(row.getCell(0).getStringCellValue());
                int courseId = courseService.findByName(row.getCell(1).getStringCellValue());

                // 2)判断是否已存在数据库中
                Score score = new Score();
                score.setStudentId(studentId);
                score.setCourseId(courseId);
                score.setScore(scoreValue);
                score.setRemark(remark);

                if (!scoreService.isScore(score)) {
                    // 3)存入数据库
                    int i = scoreService.addScore(score);
                    if (i > 0) {
                        count++;
                    }
                } else {
                    errorMsg.append("第").append(rowNum).append("行已录入，不重复录入！\n");
                }
            }

            errorMsg.append("成功录入").append(count).append("条成绩信息！");
            response.getWriter().write("<div id='message'>" + errorMsg + "</div>");
        } catch (IOException e) {
            logger.error("导入成绩文件失败", e);
            try {
                response.getWriter().write("<div id='message'>上传错误</div>");
            } catch (IOException ex) {
                logger.error("写入响应失败", ex);
            }
        }
    }

    /**
     * 导出xlsx表
     * @param response HTTP响应
     * @param score 成绩查询条件
     * @param session HTTP会话
     */
    @RequestMapping("/exportScore")
    @ResponseBody
    private void exportScore(HttpServletResponse response, Score score, HttpSession session) {
        // 获取当前登录用户类型
        Stu stu = (Stu) session.getAttribute(Const.STUDENT);
        if (!ObjectUtils.isEmpty(stu)) {
            // 如果是学生，只能查看自己的信息
            score.setStudentId(stu.getId());
        }

        try (XSSFWorkbook xssfWorkbook = new XSSFWorkbook()) {
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    URLEncoder.encode("score_list_sid_" + score.getStudentId() + "_cid_" + score.getCourseId() + ".xlsx", StandardCharsets.UTF_8));
            response.setHeader("Connection", "close");
            response.setHeader("Content-Type", "application/octet-stream");

            List<Score> scoreList = scoreService.getAll(score);
            XSSFSheet createSheet = xssfWorkbook.createSheet("成绩列表");
            XSSFRow createRow = createSheet.createRow(0);

            createRow.createCell(0).setCellValue("学生");
            createRow.createCell(1).setCellValue("课程");
            createRow.createCell(2).setCellValue("成绩");
            createRow.createCell(3).setCellValue("备注");

            // 实现将数据装入到excel文件中
            int row = 1;
            for (Score s : scoreList) {
                createRow = createSheet.createRow(row++);
                createRow.createCell(0).setCellValue(s.getStudentName());
                createRow.createCell(1).setCellValue(s.getCourseName());
                createRow.createCell(2).setCellValue(s.getScore());
                createRow.createCell(3).setCellValue(s.getRemark());
            }

            try (ServletOutputStream outputStream = response.getOutputStream()) {
                xssfWorkbook.write(outputStream);
                outputStream.flush();
            }
        } catch (Exception e) {
            logger.error("导出成绩文件失败", e);
        }
    }

    /**
     * 跳转统计页面
     * @return 统计页面视图
     */
    @RequestMapping("/scoreStats")
    public String scoreStats() {
        return "/score/scoreStats";
    }

    /**
     * 统计成绩数据
     * @param courseid 课程ID，默认值为0
     * @param searchType 统计类型
     * @return 统计结果
     */
    @RequestMapping("/getScoreStatsList")
    @ResponseBody
    public Object getScoreStatsList(
            @RequestParam(value = "courseid", defaultValue = "0") Integer courseid,
            String searchType) {

        if ("avg".equals(searchType)) {
            ScoreStats scoreStats = scoreService.getAvgStats(courseid);

            List<Double> scoreList = new ArrayList<>();
            scoreList.add(scoreStats.getMax_score());
            scoreList.add(scoreStats.getMin_score());
            scoreList.add(scoreStats.getAvg_score());

            List<String> avgStringList = new ArrayList<>();
            avgStringList.add("最高分");
            avgStringList.add("最低分");
            avgStringList.add("平均分");

            return createResultMap(scoreStats.getCourseName(), scoreList, avgStringList);
        }

        Score score = new Score();
        score.setCourseId(courseid);
        List<Score> scoreList = scoreService.getAll(score);

        List<Integer> numberList = new ArrayList<>(List.of(0, 0, 0, 0, 0));

        List<String> rangeStringList = new ArrayList<>();
        rangeStringList.add("60分以下");
        rangeStringList.add("60~70分");
        rangeStringList.add("70~80分");
        rangeStringList.add("80~90分");
        rangeStringList.add("90~100分");

        String courseName = "";

        for (Score sc : scoreList) {
            courseName = sc.getCourseName();  // 获取课程名
            double scoreValue = sc.getScore();// 获取成绩

            if (scoreValue < 60) {
                numberList.set(0, numberList.get(0) + 1);
            } else if (scoreValue <= 70) {
                numberList.set(1, numberList.get(1) + 1);
            } else if (scoreValue <= 80) {
                numberList.set(2, numberList.get(2) + 1);
            } else if (scoreValue <= 90) {
                numberList.set(3, numberList.get(3) + 1);
            } else if (scoreValue <= 100) {
                numberList.set(4, numberList.get(4) + 1);
            }
        }

        return createResultMap(courseName, numberList, rangeStringList);
    }

    /**
     * 创建统计结果Map
     * @param courseName 课程名称
     * @param dataList 数据列表
     * @param labelList 标签列表
     * @return 结果Map
     */
    private Map<String, Object> createResultMap(String courseName, List<?> dataList, List<String> labelList) {
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("courseName", courseName);
        retMap.put("numberList", dataList);
        retMap.put("rangeList", labelList);
        retMap.put("type", "success");
        return retMap;
    }
}
