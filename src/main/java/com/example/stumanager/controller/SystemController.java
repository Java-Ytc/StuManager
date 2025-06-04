package com.example.stumanager.controller;

import com.example.stumanager.domain.Admin;
import com.example.stumanager.domain.Stu;
import com.example.stumanager.domain.Teacher;
import com.example.stumanager.service.AdminService;
import com.example.stumanager.service.StuService;
import com.example.stumanager.service.TeacherService;
import com.example.stumanager.util.AjaxResult;
import com.example.stumanager.util.Const;
import com.example.stumanager.util.CpachaUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Controller
@RequestMapping("/system")
public class SystemController {
    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);

    @Autowired
    private AdminService adminService;
    @Autowired
    private StuService stuService;
    @Autowired
    private TeacherService teacherService;

    /**
     * 跳转登录界面
     * @return 登录页面视图名称
     */
    @GetMapping("/login")
    public String login(){
        return "/login";
    }

    /**
     * 登录表单提交校验
     *
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param type 用户类型(1-管理员,2-学生,3-教师)
     * @param session HTTP会话
     * @return 包含登录结果的Ajax响应
     */
    @PostMapping("/login")
    @ResponseBody
    public AjaxResult submitlogin(String username, String password, String code, String type,
                                  HttpSession session){
        AjaxResult ajaxResult = new AjaxResult();

        if(ObjectUtils.isEmpty(username)){
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("请填写用户名");
            return ajaxResult;
        }
        if(ObjectUtils.isEmpty(password)){
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("请填写密码");
            return ajaxResult;
        }
        if(ObjectUtils.isEmpty(code)){
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("请填验证码");
            return ajaxResult;
        }
        if(ObjectUtils.isEmpty(session.getAttribute(Const.CODE))){
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("会话时间过长，请刷新");
            return ajaxResult;
        }else{
            if(!code.equalsIgnoreCase((String) session.getAttribute(Const.CODE))){
                ajaxResult.setSuccess(false);
                ajaxResult.setMessage("验证码错误");
                return ajaxResult;
            }
        }

        // 数据库校验
        switch (type){
            case "1":{ // 管理员
                Admin admin = new Admin();
                admin.setPassword(password);
                admin.setUsername(username);
                Admin ad = adminService.findByAdmin(admin);
                if(ObjectUtils.isEmpty(ad)){
                    ajaxResult.setSuccess(false);
                    ajaxResult.setMessage("用户名或密码错误");
                    return ajaxResult;
                }
                ajaxResult.setSuccess(true);
                session.setAttribute(Const.ADMIN, ad);
                session.setAttribute(Const.USERTYPE, "1");
                break;
            }
            case "2":{
                Stu stu = new Stu();
                stu.setPassword(password);
                stu.setUsername(username);
                Stu st = stuService.findByStudent(stu);
                if(ObjectUtils.isEmpty(st)){
                    ajaxResult.setSuccess(false);
                    ajaxResult.setMessage("用户名或密码错误");
                    return ajaxResult;
                }
                ajaxResult.setSuccess(true);
                session.setAttribute(Const.STUDENT, st);
                session.setAttribute(Const.USERTYPE, "2");
                break;
            }
            case "3":{
                Teacher teacher = new Teacher();
                teacher.setPassword(password);
                teacher.setUsername(username);
                Teacher tr = teacherService.findByTeacher(teacher);
                if(ObjectUtils.isEmpty(tr)){
                    ajaxResult.setSuccess(false);
                    ajaxResult.setMessage("用户名或密码错误");
                    return ajaxResult;
                }
                ajaxResult.setSuccess(true);
                session.setAttribute(Const.TEACHER, tr);
                session.setAttribute(Const.USERTYPE, "3");
                break;
            }
        }
        return ajaxResult;
    }

    /**
     * 生成并显示验证码图片
     *
     * @param request HTTP请求对象，用于获取会话
     * @param response HTTP响应对象，用于输出验证码图片
     * @param vl 验证码长度，默认值4
     * @param w 图片宽度，默认值110
     * @param h 图片高度，默认值39
     */
    @GetMapping("/checkCode")
    public void generateCpacha(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam(value="vl", defaultValue="4", required=false) Integer vl,
                               @RequestParam(value="w", defaultValue="110", required=false) Integer w,
                               @RequestParam(value="h", defaultValue="39", required=false) Integer h){
        CpachaUtil cpachaUtil = new CpachaUtil(vl, w, h);
        String generatorVCode = cpachaUtil.generatorVCode();
        request.getSession().setAttribute(Const.CODE, generatorVCode);
        BufferedImage generatorRotateVCodeImage = cpachaUtil.generatorRotateVCodeImage(generatorVCode, true);

        try {
            ImageIO.write(generatorRotateVCodeImage, "gif", response.getOutputStream());
        } catch (IOException e) {
            logger.error("生成验证码图片失败", e);
        }
    }

    /**
     * 跳转后台主页
     * @return 后台主页视图名称
     */
    @GetMapping("/index")
    public String index(){
        return "/system/index";
    }

    /**
     * 用户登出
     * @param session HTTP会话对象
     * @return 重定向到登录页面
     */
    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/system/login";
    }

    /**
     * 获取用户头像图片地址
     *
     * @param sid 学生ID，默认值0
     * @param tid 教师ID，默认值0
     * @return 包含图片URL的Ajax响应
     */
    @RequestMapping("/getPhoto")
    @ResponseBody
    public AjaxResult getPhoto(@RequestParam(value = "sid", defaultValue = "0") Integer sid,
                               @RequestParam(value = "tid", defaultValue = "0") Integer tid){
        AjaxResult ajaxResult = new AjaxResult();

        if(sid != 0){
            Stu stu = stuService.findById(sid);
            ajaxResult.setImgurl(stu.getPhoto());
            return ajaxResult;
        }

        if(tid != 0){
            Teacher teacher = teacherService.findById(tid);
            ajaxResult.setImgurl(teacher.getPhoto());
            return ajaxResult;
        }

        return ajaxResult;
    }

    @GetMapping("/personalView")
    public String personalView(){
        return "/system/personalView";
    }

    /**
     * 修改用户密码
     *
     * @param password 原密码
     * @param newpassword 新密码
     * @param session HTTP会话，用于获取当前用户信息
     * @return 包含修改结果的Ajax响应
     */
    @PostMapping("/editPassword")
    @ResponseBody
    public AjaxResult editPassword(String password, String newpassword, HttpSession session) {
        AjaxResult ajaxResult = new AjaxResult();
        String usertype = (String) session.getAttribute(Const.USERTYPE);

        try {
            // 使用switch表达式优化多重条件判断
            ajaxResult = switch (usertype) {
                case "1" -> processAdminPasswordChange(password, newpassword, session);
                case "2" -> processStudentPasswordChange(password, newpassword, session);
                case "3" -> processTeacherPasswordChange(password, newpassword, session);
                default -> {
                    ajaxResult.setSuccess(false);
                    ajaxResult.setMessage("未知用户类型");
                    yield ajaxResult;
                }
            };
        } catch (Exception e) {
            logger.error("修改密码失败", e);
            ajaxResult.setSuccess(false);
            ajaxResult.setMessage("修改失败");
        }

        return ajaxResult;
    }

    /**
     * 处理管理员密码修改
     */
    private AjaxResult processAdminPasswordChange(String password, String newpassword, HttpSession session) {
        AjaxResult result = new AjaxResult();
        Admin admin = (Admin) session.getAttribute(Const.ADMIN);

        if (!password.equals(admin.getPassword())) {
            result.setSuccess(false);
            result.setMessage("原密码错误");
            return result;
        }

        admin.setPassword(newpassword);
        int count = adminService.editPswdByAdmin(admin);

        if (count > 0) {
            result.setSuccess(true);
            result.setMessage("修改成功,请重新登录");
        } else {
            result.setSuccess(false);
            result.setMessage("修改失败");
        }

        return result;
    }

    /**
     * 处理学生密码修改
     */
    private AjaxResult processStudentPasswordChange(String password, String newpassword, HttpSession session) {
        AjaxResult result = new AjaxResult();
        Stu stu = (Stu) session.getAttribute(Const.STUDENT);

        if (!password.equals(stu.getPassword())) {
            result.setSuccess(false);
            result.setMessage("原密码错误");
            return result;
        }

        stu.setPassword(newpassword);
        int count = stuService.editPswdByStudent(stu);

        if (count > 0) {
            result.setSuccess(true);
            result.setMessage("修改成功,请重新登录");
        } else {
            result.setSuccess(false);
            result.setMessage("修改失败");
        }

        return result;
    }

    /**
     * 处理教师密码修改
     */
    private AjaxResult processTeacherPasswordChange(String password, String newpassword, HttpSession session) {
        AjaxResult result = new AjaxResult();
        Teacher teacher = (Teacher) session.getAttribute(Const.TEACHER);

        if (!password.equals(teacher.getPassword())) {
            result.setSuccess(false);
            result.setMessage("原密码错误");
            return result;
        }

        teacher.setPassword(newpassword);
        int count = teacherService.editPswdByTeacher(teacher);

        if (count > 0) {
            result.setSuccess(true);
            result.setMessage("修改成功,请重新登录");
        } else {
            result.setSuccess(false);
            result.setMessage("修改失败");
        }

        return result;
    }
}
