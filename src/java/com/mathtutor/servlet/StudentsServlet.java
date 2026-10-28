package com.mathtutor.servlet;

import com.mathtutor.util.*;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** (Giáo viên) Danh sách học sinh của tôi: xem tiến độ, thêm học sinh mới, bỏ học sinh khỏi danh sách. */
@WebServlet("/teacher/students")
public class StudentsServlet extends BaseServlet {
    /** Học sinh của giáo viên đang đăng nhập + % bài giảng đã hoàn thành (chỉ tính bài đang hiển thị). */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("students", Db.query(
            "SELECT u.UserId, u.Username, u.FullName, u.Email,"
            + " CASE WHEN (SELECT COUNT(*) FROM Lessons WHERE IsPublished=1)=0 THEN 0"
            + " ELSE (SELECT COUNT(*) FROM LessonProgress lp WHERE lp.StudentId=u.UserId AND lp.Status='completed' AND lp.LessonId IN (SELECT LessonId FROM Lessons WHERE IsPublished=1))*100"
            + "/(SELECT COUNT(*) FROM Lessons WHERE IsPublished=1) END AS progressPercent"
            + " FROM TeacherStudent ts JOIN Users u ON u.UserId=ts.StudentId WHERE ts.TeacherId=? ORDER BY u.FullName", uid(req)));
        view(req, resp, "manage/students.jsp");
    }

    /** action=add: tạo tài khoản học sinh và gắn vào giáo viên; action=remove: bỏ liên kết (không xoá tài khoản). */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String a = param(req, "action");
        if ("add".equals(a) && (Text.blank(param(req, "username")) || Text.blank(req.getParameter("password"))
                || Text.blank(param(req, "fullName")))) {
            flash(req, "Vui lòng nhập đủ tên đăng nhập, mật khẩu và họ tên.");
        } else if ("add".equals(a)) {
            try {
                // Tạo tài khoản với vai trò student, mật khẩu băm SHA-256, rồi gắn học sinh vào giáo viên hiện tại
                long sid = Db.insert("INSERT INTO Users(Username,PasswordHash,FullName,Email,RoleId,CreatedBy) "
                        + "VALUES(?,?,?,?,(SELECT RoleId FROM Roles WHERE RoleName='student'),?)",
                        param(req, "username"), Security.sha256(req.getParameter("password")), param(req, "fullName"),
                        Text.nullIfBlank(req.getParameter("email")), uid(req));
                Db.update("INSERT INTO TeacherStudent(TeacherId,StudentId) VALUES(?,?)", uid(req), sid);
                flash(req, "Đã thêm học sinh.");
            } catch (Db.DbException e) {
                flash(req, "Không thêm được: tên đăng nhập hoặc email đã tồn tại.");
            }
        } else if ("remove".equals(a) && intParam(req, "id") != null) {
            Db.update("DELETE FROM TeacherStudent WHERE TeacherId=? AND StudentId=?", uid(req), intParam(req, "id"));
            flash(req, "Đã bỏ học sinh khỏi danh sách.");
        }
        redirect(req, resp, "/teacher/students");
    }
}
