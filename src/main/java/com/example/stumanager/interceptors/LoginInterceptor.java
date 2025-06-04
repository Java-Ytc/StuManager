package com.example.stumanager.interceptors;

import com.example.stumanager.domain.Admin;
import com.example.stumanager.domain.Stu;
import com.example.stumanager.domain.Teacher;
import com.example.stumanager.util.Const;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        Admin user = (Admin)request.getSession().getAttribute(Const.ADMIN);
        Teacher teacher = (Teacher)request.getSession().getAttribute(Const.TEACHER);
        Stu student = (Stu)request.getSession().getAttribute(Const.STUDENT);

        // 直接使用 != null 进行判断
        if(user != null || teacher != null || student != null){
            return true;
        }

        response.sendRedirect(request.getContextPath() + "/system/login");
        return false;
    }
}
