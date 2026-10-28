package com.mathtutor.servlet;

import com.mathtutor.util.Db;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** (Admin) Danh sách người dùng: xem, khoá/mở khoá, xoá tài khoản. */
@WebServlet("/admin/users")
public class UsersServlet extends BaseServlet {
    /** Lấy toàn bộ người dùng kèm tên vai trò để hiển thị bảng. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("users", Db.query("SELECT u.UserId, u.Username, u.FullName, u.Email, r.RoleName, u.IsActive "
                + "FROM Users u JOIN Roles r ON r.RoleId=u.RoleId ORDER BY u.UserId"));
        view(req, resp, "admin/users.jsp");
    }

    /** Xử lý các nút trong bảng: action=toggle (khoá/mở khoá) hoặc action=delete (xoá). */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = intParam(req, "id");
        String action = param(req, "action");
        // Không cho admin tự khoá/xoá chính mình (tránh bị mất quyền truy cập)
        if (id != null && id == uid(req)) {
            flash(req, "Không thể khoá/xoá chính tài khoản đang đăng nhập.");
        } else if (id != null && "toggle".equals(action)) {
            // Đảo trạng thái: đang hoạt động -> khoá, đang khoá -> hoạt động
            Db.update("UPDATE Users SET IsActive = CASE WHEN IsActive=1 THEN 0 ELSE 1 END WHERE UserId=?", id);
            flash(req, "Đã cập nhật trạng thái tài khoản.");
        } else if (id != null && "delete".equals(action)) {
            try {
                // Xoá trong MỘT giao dịch: bỏ liên kết giáo viên - học sinh rồi xoá tài khoản.
                // Nếu tài khoản đã có dữ liệu liên quan (bài làm, câu hỏi...) thì CSDL báo lỗi và mọi thay đổi được huỷ.
                Db.tx(new Object[]{"DELETE FROM TeacherStudent WHERE TeacherId=? OR StudentId=?", id, id},
                      new Object[]{"DELETE FROM Users WHERE UserId=?", id});
                flash(req, "Đã xoá người dùng.");
            } catch (Db.DbException e) {
                flash(req, "Không xoá được vì người dùng đã có dữ liệu liên quan. Hãy dùng nút Khoá.");
            }
        }
        redirect(req, resp, "/admin/users");
    }
}
