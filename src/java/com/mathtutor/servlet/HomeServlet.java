package com.mathtutor.servlet;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** Trang /home: không có giao diện riêng, chỉ chuyển người dùng tới trang chủ đúng với vai trò của họ. */
@WebServlet("/home")
public class HomeServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // admin -> Dashboard quản trị, teacher -> Dashboard giáo viên, student -> Trang chủ học sinh
        String r = role(req);
        redirect(req, resp, r.equals("admin") ? "/admin/dashboard" : r.equals("teacher") ? "/teacher/dashboard" : "/student/home");
    }
}
