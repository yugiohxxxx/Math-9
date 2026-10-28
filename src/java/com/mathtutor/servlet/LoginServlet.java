package com.mathtutor.servlet;

import com.mathtutor.util.Db;
import com.mathtutor.util.Security;
import java.io.IOException;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** Đăng nhập: GET hiện form, POST kiểm tra tên đăng nhập + mật khẩu. */
@WebServlet("/login")
public class LoginServlet extends BaseServlet {
    /** Hiện trang đăng nhập; nếu đã đăng nhập rồi thì chuyển thẳng về trang chủ. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s != null && s.getAttribute("user") != null) { redirect(req, resp, "/home"); return; }
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    /** Xử lý khi bấm "Đăng nhập". */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String u = param(req, "username"), p = req.getParameter("password");
        // Mật khẩu không lưu dạng chữ thường mà lưu dạng băm SHA-256, nên băm mật khẩu vừa nhập rồi so sánh.
        // Chỉ tài khoản đang hoạt động (IsActive=1) mới đăng nhập được.
        Map<String, Object> user = (u == null || p == null) ? null : Db.one(
                "SELECT u.UserId, u.Username, u.FullName, u.Email, r.RoleName FROM Users u "
                + "JOIN Roles r ON r.RoleId=u.RoleId WHERE u.Username=? AND u.PasswordHash=? AND u.IsActive=1",
                u, Security.sha256(p));
        // Sai thông tin: quay lại form kèm thông báo lỗi
        if (user == null) {
            req.setAttribute("error", "Sai tên đăng nhập hoặc mật khẩu.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }
        // Tạo session MỚI sau khi đăng nhập (chống tấn công giữ phiên - session fixation), rồi lưu người dùng vào session
        HttpSession old = req.getSession(false);
        if (old != null) old.invalidate();
        req.getSession(true).setAttribute("user", user);
        redirect(req, resp, "/home");
    }
}
