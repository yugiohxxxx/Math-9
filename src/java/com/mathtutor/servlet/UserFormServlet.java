package com.mathtutor.servlet;

import com.mathtutor.util.*;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** (Admin) Form thêm mới / sửa một người dùng. Có tham số id là sửa, không có là thêm mới. */
@WebServlet("/admin/user-form")
public class UserFormServlet extends BaseServlet {
    /** Hiện form: nếu có id thì nạp sẵn thông tin người dùng đó; luôn nạp danh sách vai trò cho ô chọn. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = intParam(req, "id");
        if (id != null)
            req.setAttribute("u", Db.one("SELECT UserId, Username, FullName, Email, Phone, RoleId FROM Users WHERE UserId=?", id));
        req.setAttribute("roles", Db.query("SELECT RoleId, RoleName FROM Roles ORDER BY RoleId"));
        view(req, resp, "admin/user-form.jsp");
    }

    /** Lưu form. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = intParam(req, "id");
        String pass = req.getParameter("password");
        // Kiểm tra dữ liệu bắt buộc (thêm mới thì cần cả tên đăng nhập và mật khẩu)
        if (intParam(req, "roleId") == null || Text.blank(param(req, "fullName"))
                || (id == null && (Text.blank(param(req, "username")) || Text.blank(pass)))) {
            flash(req, "Vui lòng nhập đủ tên đăng nhập, mật khẩu, họ tên và vai trò.");
            redirect(req, resp, "/admin/user-form" + (id == null ? "" : "?id=" + id));
            return;
        }
        try {
            if (id == null) {
                // Thêm mới: mật khẩu được băm SHA-256 trước khi lưu
                Db.insert("INSERT INTO Users(Username,PasswordHash,FullName,Email,Phone,RoleId,CreatedBy) VALUES(?,?,?,?,?,?,?)",
                        param(req, "username"), Security.sha256(pass), param(req, "fullName"),
                        Text.nullIfBlank(req.getParameter("email")), Text.nullIfBlank(req.getParameter("phone")),
                        intParam(req, "roleId"), uid(req));
            } else {
                Db.update("UPDATE Users SET FullName=?, Email=?, Phone=?, RoleId=? WHERE UserId=?",
                        param(req, "fullName"), Text.nullIfBlank(req.getParameter("email")),
                        Text.nullIfBlank(req.getParameter("phone")), intParam(req, "roleId"), id);
                // Khi sửa: để trống ô mật khẩu nghĩa là giữ mật khẩu cũ
                if (!Text.blank(pass))
                    Db.update("UPDATE Users SET PasswordHash=? WHERE UserId=?", Security.sha256(pass), id);
            }
            flash(req, "Đã lưu người dùng.");
            redirect(req, resp, "/admin/users");
        // Lỗi trùng tên đăng nhập/email (ràng buộc UNIQUE trong CSDL)
        } catch (Db.DbException e) {
            flash(req, "Không lưu được: tên đăng nhập hoặc email đã tồn tại.");
            redirect(req, resp, "/admin/user-form" + (id == null ? "" : "?id=" + id));
        }
    }
}
