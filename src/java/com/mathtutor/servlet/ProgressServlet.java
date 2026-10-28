package com.mathtutor.servlet;

import com.mathtutor.util.Db;
import java.io.IOException;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** Học sinh xem tiến độ: số bài đã xong / đang học / chưa học và biểu đồ điểm luyện tập theo thời gian. */
@WebServlet("/student/progress")
public class ProgressServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Map<String, Object>> lessons = Db.query("SELECT l.LessonId, l.Title, ISNULL(lp.Status,'not_started') AS Status FROM Lessons l "
                + "LEFT JOIN LessonProgress lp ON lp.LessonId=l.LessonId AND lp.StudentId=? WHERE l.IsPublished=1 ORDER BY l.TopicId, l.SortOrder, l.LessonId", uid(req));
        // Đếm số bài theo trạng thái để hiện ở 3 ô số liệu
        int done = 0, doing = 0;
        for (Map<String, Object> l : lessons) {
            if ("completed".equals(l.get("status"))) done++;
            else if ("in_progress".equals(l.get("status"))) doing++;
        }
        Map<String, Object> prog = new HashMap<>();
        prog.put("done", done);
        prog.put("doing", doing);
        prog.put("notStarted", lessons.size() - done - doing);
        req.setAttribute("progress", prog);
        req.setAttribute("lessons", lessons);
        // Điểm các phiên luyện tập đã chấm, theo thời gian (biểu đồ đường)
        req.setAttribute("history", Db.query("SELECT StartedAt, Score FROM PracticeSessions WHERE StudentId=? AND Score IS NOT NULL ORDER BY StartedAt", uid(req)));
        view(req, resp, "student/progress.jsp");
    }
}
