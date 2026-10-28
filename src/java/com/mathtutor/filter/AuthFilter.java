package com.mathtutor.filter;

import java.io.IOException;
import java.util.Map;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

/** Bắt buộc đăng nhập và kiểm tra role theo đường dẫn. */
@WebFilter(urlPatterns = {"/admin/*", "/teacher/*", "/manage/*", "/student/*", "/home", "/uploads/*"})
public class AuthFilter implements Filter {
    @Override public void init(FilterConfig c) { }
    @Override public void destroy() { }

    @Override
    @SuppressWarnings("unchecked")
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse resp = (HttpServletResponse) rs;
        HttpSession s = req.getSession(false);
        Map<String, Object> user = s == null ? null : (Map<String, Object>) s.getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String role = String.valueOf(user.get("roleName"));
        String path = req.getServletPath();
        boolean ok = true;
        if (path.startsWith("/admin/")) ok = role.equals("admin");
        else if (path.startsWith("/teacher/")) ok = role.equals("teacher");
        else if (path.startsWith("/manage/")) ok = role.equals("teacher"); // noi dung, cau hoi: chi giao vien
        else if (path.startsWith("/student/")) ok = role.equals("student") || path.equals("/student/lesson"); // GV/Admin xem thử bài giảng
        if (!ok) {
            resp.setStatus(403);
            req.setAttribute("code", 403);
            req.setAttribute("message", "Bạn không có quyền truy cập trang này.");
            req.getRequestDispatcher("/WEB-INF/views/common/error.jsp").forward(req, resp);
            return;
        }
        chain.doFilter(req, resp);
    }
}
