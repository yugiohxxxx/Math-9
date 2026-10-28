package com.mathtutor.servlet;

import com.mathtutor.util.Db;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** Trang chủ học sinh: tính % bài giảng đã hoàn thành để hiện thanh tiến độ. */
@WebServlet("/student/home")
public class StudentHomeServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int total = Db.count("SELECT COUNT(*) FROM Lessons WHERE IsPublished=1");
        int done = Db.count("SELECT COUNT(*) FROM LessonProgress lp JOIN Lessons l ON l.LessonId=lp.LessonId "
                + "WHERE lp.StudentId=? AND lp.Status='completed' AND l.IsPublished=1", uid(req));
        req.setAttribute("overallPercent", total == 0 ? 0 : done * 100 / total);
        view(req, resp, "student/home.jsp");
    }
}
