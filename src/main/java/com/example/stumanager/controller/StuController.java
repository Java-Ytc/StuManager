package com.example.stumanager.controller;

import com.example.stumanager.domain.Stu;
import com.example.stumanager.service.SelectedCourseService;
import com.example.stumanager.service.StuService;
import com.example.stumanager.util.*;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/stu")
public class StuController {
    private static final Logger logger = LoggerFactory.getLogger(StuController.class);

    @Autowired
    private StuService stuService;

    @Autowired
    private SelectedCourseService selectedCourseService;

    /**
     * 跳转学生列表页面
     *
     * @return 视图名称
     */
    @GetMapping("/student_list")
    public String studentList() {
        return "/student/studentList";
    }

    /**
     * 异步加载学生列表
     *
     * @param page         当前页码，默认值为1
     * @param rows         每页显示的记录数，默认值为100
     * @param studentName  学生姓名，用于筛选
     * @param clazzid      班级ID，用于筛选
     * @param from         请求来源标识，用于区分不同的请求场景
     * @param session      HTTP会话对象，用于获取当前登录用户信息
     * @return 返回分页数据或数据列表，取决于请求来源
     */
    @RequestMapping("/getStudentList")
    @ResponseBody
    public Object getStudentList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "100") Integer rows,
            String studentName,
            @RequestParam(value = "clazzid", defaultValue = "0") String clazzid,
            String from,
            HttpSession session) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pageno", page);
        paramMap.put("pagesize", rows);

        if (!ObjectUtils.isEmpty(studentName)) {
            paramMap.put("username", studentName);
        }

        if (!"0".equals(clazzid)) {
            paramMap.put("clazzid", clazzid);
        }

        // 判断是老师还是学生权限
        Stu student = (Stu) session.getAttribute(Const.STUDENT);
        if (!ObjectUtils.isEmpty(student)) {
            // 是学生权限，只能查询自己的信息
            paramMap.put("studentid", student.getId());
        }

        PageBean<Stu> pageBean = stuService.queryPage(paramMap);

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
     * 删除学生
     *
     * @param data 包含要删除的学生ID列表的数据对象
     * @return 返回操作结果，包含成功状态和消息
     */
    @PostMapping("/deleteStudent")
    @ResponseBody
    public AjaxResult deleteStudent(IdListData data) {
        AjaxResult ajaxResult = new AjaxResult();

        try {
            List<Integer> ids = data.getIds();

            // 检查是否存在课程关联学生
            for (Integer id : ids) {
                if (!selectedCourseService.isStudentId(id)) {
                    ajaxResult.setSuccess(false);
                    ajaxResult.setMessage("无法删除，存在课程关联学生");
                    return ajaxResult;
                }
            }

            File fileDir = UploadUtil.getImgDirFile();

            for (Integer id : ids) {
                Stu byId = stuService.findById(id);

                if (!ObjectUtils.isEmpty(byId.getPhoto())) {
                    File file = new File(fileDir.getAbsolutePath() + File.separator + byId.getPhoto());

                    if (file.exists()) {
                        if (!file.delete()) {
                            logger.warn("删除学生图片失败，文件: {}", file.getAbsolutePath());
                        }
                    }
                }
            }

            int count = stuService.deleteStudent(ids);

            if (count > 0) {
                ajaxResult.setSuccess(true);
                ajaxResult.setMessage("全部删除成功");
            } else {
                ajaxResult.setSuccess(false);
                ajaxResult.setMessage("删除失败");
            }
        } catch (Exception e) {
            logger.error("删除学生信息发生异常", e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("删除失败");
        }

        return ajaxResult;
    }

    /**
     * 添加学生
     *
     * @param files 上传的图片文件数组
     * @param stu   学生信息对象
     * @return 返回操作结果，包含成功状态和消息
     */
    @RequestMapping("/addStudent")
    @ResponseBody
    public AjaxResult addStudent(@RequestParam("file") MultipartFile[] files, Stu stu) {
        return processStudentFileOperation(files, stu, true);
    }

    /**
     * 修改学生信息
     *
     * @param files 上传的图片文件数组
     * @param stu   学生信息对象
     * @return 返回操作结果，包含成功状态和消息
     */
    @PostMapping("/editStudent")
    @ResponseBody
    public AjaxResult editStudent(@RequestParam("file") MultipartFile[] files, Stu stu) {
        return processStudentFileOperation(files, stu, false);
    }

    /**
     * 处理学生文件操作（添加/修改）的通用方法
     *
     * @param files     上传的图片文件数组
     * @param stu       学生信息对象
     * @param isAdd     是否为添加操作
     * @return 返回操作结果，包含成功状态和消息
     */
    private AjaxResult processStudentFileOperation(MultipartFile[] files, Stu stu, boolean isAdd) {
        AjaxResult ajaxResult = new AjaxResult();
        String operation = isAdd ? "添加" : "修改";

        try {
            if (isAdd) {
                stu.setSn(SnGenerateUtil.generateSn(stu.getClazzId()));
            }

            File fileDir = UploadUtil.getImgDirFile();

            for (MultipartFile fileImg : files) {
                if (fileImg.isEmpty()) {
                    continue;
                }

                String originalFilename = fileImg.getOriginalFilename();
                if (ObjectUtils.isEmpty(originalFilename)) {
                    logger.warn("{}学生时上传的文件没有文件名", operation);
                    continue;
                }

                String extName = getFileExtension(originalFilename);
                String uuidName = UUID.randomUUID().toString();

                try {
                    File newFile = new File(fileDir.getAbsolutePath() + File.separator + uuidName + extName);
                    fileImg.transferTo(newFile);

                    // 如果是修改操作，删除旧图片
                    if (!isAdd) {
                        Stu byId = stuService.findById(stu.getId());
                        if (!ObjectUtils.isEmpty(byId.getPhoto())) {
                            File oldFile = new File(fileDir.getAbsolutePath() + File.separator + byId.getPhoto());
                            if (oldFile.exists() && !oldFile.delete()) {
                                logger.warn("删除旧图片失败，文件: {}", oldFile.getAbsolutePath());
                            }
                        }
                    }

                    stu.setPhoto(uuidName + extName);
                } catch (IOException e) {
                    logger.error("{}学生图片失败", operation, e);
                    ajaxResult.setSuccess(false);
                    ajaxResult.setMessage(operation + "图片失败");
                    return ajaxResult;
                }
            }

            int count = isAdd ? stuService.addStudent(stu) : stuService.editStudent(stu);
            ajaxResult.setSuccess(count > 0);
            ajaxResult.setMessage(count > 0 ? operation + "成功" : operation + "失败");
        } catch (Exception e) {
            logger.error("{}学生信息发生异常", operation, e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage(operation + "失败");
        }

        return ajaxResult;
    }

    /**
     * 安全获取文件扩展名，避免NullPointerException
     *
     * @param fileName 文件名
     * @return 文件扩展名，包含点号（如.jpg）
     */
    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }

        int lastIndex = fileName.lastIndexOf('.');
        return lastIndex >= 0 ? fileName.substring(lastIndex) : "";
    }
}
