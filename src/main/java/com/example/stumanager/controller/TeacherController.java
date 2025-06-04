package com.example.stumanager.controller;

import com.example.stumanager.domain.Teacher;
import com.example.stumanager.service.TeacherService;
import com.example.stumanager.util.*;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/teacher")
public class TeacherController {
    private static final Logger logger = LoggerFactory.getLogger(TeacherController.class);
    private static final String LOG_PREFIX = "[教师模块] ";

    @Autowired
    private TeacherService teacherService;

    @RequestMapping("/teacher_list")
    public String teacherList() {
        return "/teacher/teacherList";
    }

    /**
     * 异步加载老师数据列表
     */
    @PostMapping("/getTeacherList")
    @ResponseBody
    public Object getTeacherList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "100") Integer rows,
            String teacherName,
            @RequestParam(value = "clazzid", defaultValue = "0") String clazzid,
            String from,
            HttpSession session
    ) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("pageno", page);
        paramMap.put("pagesize", rows);

        if (!ObjectUtils.isEmpty(teacherName)) {
            paramMap.put("username", teacherName);
            logger.debug("{}添加教师姓名搜索条件: {}", LOG_PREFIX, teacherName);
        }

        if (!"0".equals(clazzid)) {
            paramMap.put("clazzid", clazzid);
            logger.debug("{}添加班级ID筛选条件: {}", LOG_PREFIX, clazzid);
        }

        // 判断用户权限
        Teacher currentTeacher = (Teacher) session.getAttribute(Const.TEACHER);
        if (!ObjectUtils.isEmpty(currentTeacher)) {
            paramMap.put("teacherid", currentTeacher.getId());
            logger.debug("{}教师权限: 查询自身信息（ID={}）", LOG_PREFIX, currentTeacher.getId());
        }

        PageBean<Teacher> pageBean = teacherService.queryPage(paramMap);
        logger.debug("{}获取教师列表: 总记录数={}, 当前页={}", LOG_PREFIX, pageBean.getTotalsize(), page);

        return !ObjectUtils.isEmpty(from) && "combox".equals(from) ?
                pageBean.getDatas() :
                Map.of("total", pageBean.getTotalsize(), "rows", pageBean.getDatas());
    }

    /**
     * 删除教师
     */
    @PostMapping("/deleteTeacher")
    @ResponseBody
    public AjaxResult deleteTeacher(IdListData data) {
        AjaxResult ajaxResult = new AjaxResult();
        File fileDir = UploadUtil.getImgDirFile();

        try {
            for (Integer id : data.getIds()) {
                Teacher teacher = teacherService.findById(id);
                if (ObjectUtils.isEmpty(teacher)) {
                    logger.warn("{}跳过无效教师ID: {}", LOG_PREFIX, id);
                    continue;
                }

                handlePhotoDeletion(teacher, fileDir);
                logger.info("{}准备删除教师: ID={}, 姓名={}", LOG_PREFIX, id, teacher.getUsername());
            }

            int count = teacherService.deleteTeacher(data.getIds());
            ajaxResult.setSuccess(count > 0);
            ajaxResult.setMessage(ajaxResult.isSuccess() ? "删除成功" : "删除失败");
            logger.info("{}批量删除教师结果: 成功删除{}条记录", LOG_PREFIX, count);

        } catch (Exception e) {
            logger.error("{}删除教师失败: {}", LOG_PREFIX, e.getMessage(), e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("删除失败");
        }

        return ajaxResult;
    }

    /**
     * 添加教师
     */
    @RequestMapping("/addTeacher")
    @ResponseBody
    public AjaxResult addTeacher(@RequestParam("file") MultipartFile[] files, Teacher teacher) {
        AjaxResult ajaxResult = new AjaxResult();
        File fileDir = UploadUtil.getImgDirFile();

        try {
            // 生成教师编号
            teacher.setSn(SnGenerateUtil.generateTeacherSn(teacher.getClazzId()));
            logger.debug("{}生成教师编号: {}", LOG_PREFIX, teacher.getSn());

            // 处理文件上传
            handleFileUpload(files, teacher, fileDir);

            // 保存到数据库
            int count = teacherService.addTeacher(teacher);
            ajaxResult.setSuccess(count > 0);
            ajaxResult.setMessage(ajaxResult.isSuccess() ? "保存成功" : "保存失败");
            logger.info("{}添加教师结果: ID={}, 姓名={}", LOG_PREFIX, teacher.getId(), teacher.getUsername());

        } catch (Exception e) {
            logger.error("{}添加教师失败: {}", LOG_PREFIX, e.getMessage(), e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("保存失败");
        }

        return ajaxResult;
    }

    /**
     * 修改教师信息
     */
    @PostMapping("/editTeacher")
    @ResponseBody
    public AjaxResult editTeacher(@RequestParam("file") MultipartFile[] files, Teacher teacher) {
        AjaxResult ajaxResult = new AjaxResult();
        File fileDir = UploadUtil.getImgDirFile();

        try {
            Teacher existingTeacher = teacherService.findById(teacher.getId());
            if (ObjectUtils.isEmpty(existingTeacher)) {
                logger.warn("{}修改失败: 教师不存在（ID={}）", LOG_PREFIX, teacher.getId());
                ajaxResult.setSuccess(false);
                ajaxResult.setMessage("修改失败: 教师不存在");
                return ajaxResult;
            }

            // 处理文件上传和旧文件删除
            handleFileUpdate(files, existingTeacher, teacher, fileDir);

            // 更新数据库
            int count = teacherService.editTeacher(teacher);
            ajaxResult.setSuccess(count > 0);
            ajaxResult.setMessage(ajaxResult.isSuccess() ? "修改成功" : "修改失败");
            logger.info("{}更新教师信息: ID={}, 姓名={}", LOG_PREFIX, teacher.getId(), teacher.getUsername());

        } catch (Exception e) {
            logger.error("{}修改教师失败: {}", LOG_PREFIX, e.getMessage(), e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("修改失败");
        }

        return ajaxResult;
    }

    // ====================== 私有方法 ======================

    /**
     * 处理头像文件删除
     */
    private void handlePhotoDeletion(Teacher teacher, File fileDir) {
        if (!ObjectUtils.isEmpty(teacher.getPhoto())) {
            File photoFile = new File(fileDir.getAbsolutePath() + File.separator + teacher.getPhoto());
            if (photoFile.exists()) {
                if (photoFile.delete()) {
                    logger.debug("{}成功删除头像文件: {}", LOG_PREFIX, photoFile.getAbsolutePath());
                } else {
                    logger.warn("{}删除头像文件失败: {}", LOG_PREFIX, photoFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 处理文件上传（添加场景）
     */
    private void handleFileUpload(MultipartFile[] files, Teacher teacher, File fileDir) throws IOException {
        for (MultipartFile fileImg : files) {
            if (fileImg.isEmpty()) {
                continue;
            }

            String originalFilename = fileImg.getOriginalFilename();
            if (ObjectUtils.isEmpty(originalFilename)) {
                logger.warn("{}跳过空文件名的上传文件", LOG_PREFIX);
                continue;
            }

            String extName = getValidExtension(originalFilename);
            String uuidName = UUID.randomUUID().toString();
            File newFile = new File(fileDir, uuidName + extName);

            fileImg.transferTo(newFile);
            teacher.setPhoto(uuidName + extName);
            logger.debug("{}上传新头像: 文件名={}", LOG_PREFIX, newFile.getName());
        }
    }

    /**
     * 处理文件更新（修改场景）
     */
    private void handleFileUpdate(MultipartFile[] files, Teacher existingTeacher, Teacher newTeacher, File fileDir)
            throws IOException {
        for (MultipartFile fileImg : files) {
            if (fileImg.isEmpty()) {
                continue;
            }

            // 上传新文件
            String originalFilename = fileImg.getOriginalFilename();
            String extName = getValidExtension(originalFilename);
            String uuidName = UUID.randomUUID().toString();
            File newFile = new File(fileDir, uuidName + extName);
            fileImg.transferTo(newFile);
            newTeacher.setPhoto(uuidName + extName);
            logger.debug("{}上传更新头像: 文件名={}", LOG_PREFIX, newFile.getName());

            // 删除旧文件
            handlePhotoDeletion(existingTeacher, fileDir);
        }
    }

    /**
     * 获取有效文件扩展名（处理空名/无扩展名情况）
     */
    private String getValidExtension(String originalFilename) {
        // 防御性校验：确保originalFilename不为null
        if (ObjectUtils.isEmpty(originalFilename)) {
            logger.warn("{}文件名不能为空", LOG_PREFIX);
            return ".jpg"; // 默认扩展名
        }

        int lastIndex = originalFilename.lastIndexOf(".");
        if (lastIndex == -1) {
            logger.warn("{}文件无扩展名，使用默认扩展名: {}", LOG_PREFIX, originalFilename);
            return ".jpg";
        }
        return originalFilename.substring(lastIndex);
    }

}
