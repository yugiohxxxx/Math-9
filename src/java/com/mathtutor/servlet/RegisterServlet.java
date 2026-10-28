package com.mathtutor.servlet;

import com.mathtutor.util.Db;
import com.mathtutor.util.Security;
import com.mathtutor.util.Text;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/**
 * Đăng ký tài khoản cho người mới (trang công khai, không cần đăng nhập).
 * Người tự đăng ký luôn là HỌC SINH; tài khoản giáo viên/admin vẫn do admin tạo.
 * Đăng ký xong thì tự đăng nhập luôn và chuyển tới trang chủ học sinh.
 */
@WebServlet("/register")
public class RegisterServlet extends BaseServlet {
    /** Tên đăng nhập: 3-30 ký tự, chỉ chữ không dấu, số, dấu chấm và gạch dưới. */
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9._]{3,30}");
    private static final Pattern EMAIL = Pattern.compile("[^\\s@]+@[^\\s@]+\\.[^\\s@]+");
    private static final int MIN_PASSWORD = 6;

    /** Hiện form đăng ký; nếu đã đăng nhập rồi thì chuyển về trang chủ. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        if (s != null && s.getAttribute("user") != null) { redirect(req, resp, "/home"); return; }
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    /** Xử lý khi bấm "Tạo tài khoản". */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fullName = param(req, "fullName"), username = param(req, "username"), email = param(req, "email");
        String pass = req.getParameter("password"), pass2 = req.getParameter("password2");

        // Kiểm tra dữ liệu nhập; sai thì quay lại form, giữ nguyên những gì đã nhập (trừ mật khẩu)
        String err = null;
        if (Text.blank(fullName) || fullName.length() > 100) err = "Vui lòng nhập họ và tên (tối đa 100 ký tự).";
        else if (username == null || !USERNAME.matcher(username).matches())
            err = "Tên đăng nhập gồm 3-30 ký tự: chữ không dấu, số, dấu chấm hoặc gạch dưới.";
        else if (email == null || email.length() > 100 || !EMAIL.matcher(email).matches()) err = "Email không hợp lệ.";
        else if (pass == null || pass.length() < MIN_PASSWORD) err = "Mật khẩu phải có ít nhất " + MIN_PASSWORD + " ký tự.";
        else if (!pass.equals(pass2)) err = "Hai lần nhập mật khẩu không khớp.";
        // Kiểm tra trùng trước khi thêm để báo lỗi rõ ràng
        else if (Db.count("SELECT COUNT(*) FROM Users WHERE Username=?", username) > 0) err = "Tên đăng nhập này đã có người dùng.";
        else if (Db.count("SELECT COUNT(*) FROM Users WHERE Email=?", email) > 0) err = "Email này đã được dùng cho tài khoản khác.";
        if (err != null) { back(req, resp, err); return; }

        try {
            // Tạo tài khoản vai trò student; mật khẩu băm SHA-256 giống trang đăng nhập.
            // CreatedBy ghi là tài khoản admin đầu tiên (người đăng ký chưa có mã lúc tạo).
            Db.insert("INSERT INTO Users(Username,PasswordHash,FullName,Email,RoleId,CreatedBy) VALUES(?,?,?,?,"
                    + "(SELECT RoleId FROM Roles WHERE RoleName='student'),"
                    + "(SELECT MIN(u.UserId) FROM Users u JOIN Roles r ON r.RoleId=u.RoleId WHERE r.RoleName='admin'))",
                    username, Security.sha256(pass), fullName, email);
        } catch (Db.DbException e) { // trùng do hai người đăng ký cùng lúc, hoặc ràng buộc khác của CSDL
            log("Đăng ký thất bại", e);
            back(req, resp, "Không tạo được tài khoản (tên đăng nhập hoặc email đã tồn tại). Vui lòng thử lại.");
            return;
        }

        // Tự đăng nhập: lấy lại thông tin như LoginServlet rồi tạo session mới
        Map<String, Object> user = Db.one("SELECT u.UserId, u.Username, u.FullName, u.Email, r.RoleName FROM Users u "
                + "JOIN Roles r ON r.RoleId=u.RoleId WHERE u.Username=?", username);
        HttpSession old = req.getSession(false);
        if (old != null) old.invalidate();
        req.getSession(true).setAttribute("user", user);
        flash(req, "Chào mừng " + fullName + "! Tài khoản học sinh của bạn đã được tạo.");
        redirect(req, resp, "/home");
    }

    /** Quay lại form đăng ký kèm thông báo lỗi. */
    private void back(HttpServletRequest req, HttpServletResponse resp, String err) throws ServletException, IOException {
        req.setAttribute("error", err);
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }
}
