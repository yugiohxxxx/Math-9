package com.mathtutor.servlet;

import com.mathtutor.util.Db;
import java.io.IOException;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/**
 * Dashboard thống kê, dùng chung cho 2 đường dẫn:
 *  - /admin/dashboard  : admin xem số liệu của TẤT CẢ học sinh
 *  - /teacher/dashboard: giáo viên chỉ xem học sinh của mình (bảng TeacherStudent)
 */
@WebServlet({"/admin/dashboard", "/teacher/dashboard"})
public class DashboardServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Nếu là giáo viên thì thêm điều kiện lọc theo học sinh của giáo viên đó
        boolean teacher = "teacher".equals(role(req));
        String filter = teacher ? " AND u.UserId IN (SELECT StudentId FROM TeacherStudent WHERE TeacherId=?)" : "";
        Object[] p = teacher ? new Object[]{uid(req)} : new Object[0];

        // Thống kê từng học sinh: số bài giảng đã xong / đang học (chỉ tính bài đang hiển thị),
        // tổng số bài giảng, số lượt luyện tập và điểm trung bình
        List<Map<String, Object>> ss = Db.query(
            "SELECT u.FullName AS fullName,"
            + " (SELECT COUNT(*) FROM LessonProgress lp WHERE lp.StudentId=u.UserId AND lp.Status='completed' AND lp.LessonId IN (SELECT LessonId FROM Lessons WHERE IsPublished=1)) AS lessonsDone,"
            + " (SELECT COUNT(*) FROM LessonProgress lp WHERE lp.StudentId=u.UserId AND lp.Status='in_progress' AND lp.LessonId IN (SELECT LessonId FROM Lessons WHERE IsPublished=1)) AS lessonsDoing,"
            + " (SELECT COUNT(*) FROM Lessons WHERE IsPublished=1) AS lessonsTotal,"
            + " (SELECT COUNT(*) FROM PracticeSessions ps WHERE ps.StudentId=u.UserId) AS sessions,"
            + " ISNULL((SELECT AVG(Score) FROM PracticeSessions ps WHERE ps.StudentId=u.UserId AND ps.Score IS NOT NULL),0) AS avgScore"
            + " FROM Users u JOIN Roles r ON r.RoleId=u.RoleId WHERE r.RoleName='student'" + filter + " ORDER BY u.FullName", p);

        // Cộng dồn để vẽ biểu đồ tròn "Tiến độ bài giảng" và các ô số liệu ở đầu trang
        int done = 0, doing = 0, sessions = 0, total = 0;
        for (Map<String, Object> m : ss) {
            done += ((Number) m.get("lessonsDone")).intValue();
            doing += ((Number) m.get("lessonsDoing")).intValue();
            sessions += ((Number) m.get("sessions")).intValue();
            total += ((Number) m.get("lessonsTotal")).intValue();
        }
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", ss.size());
        stats.put("totalLessons", Db.count("SELECT COUNT(*) FROM Lessons"));
        stats.put("totalQuestions", Db.count("SELECT COUNT(*) FROM Questions WHERE IsActive=1"));
        stats.put("totalSessions", sessions);
        Map<String, Object> prog = new HashMap<>();
        prog.put("done", done);
        prog.put("doing", doing);
        prog.put("notStarted", Math.max(0, total - done - doing));

        String join = teacher ? " AND ps.StudentId IN (SELECT StudentId FROM TeacherStudent WHERE TeacherId=?)" : "";
        // Điểm trung bình theo từng chủ đề (biểu đồ cột)
        req.setAttribute("topicStats", Db.query(
            "SELECT t.Title AS title, ISNULL(AVG(ps.Score),0) AS avgScore FROM Topics t "
            + "LEFT JOIN PracticeSessions ps ON ps.TopicId=t.TopicId AND ps.Score IS NOT NULL" + join
            + " GROUP BY t.TopicId, t.Title ORDER BY t.TopicId", p));
        req.setAttribute("stats", stats);
        req.setAttribute("studentStats", ss);
        req.setAttribute("progressStats", prog);
        view(req, resp, "admin/dashboard.jsp");
    }
}
