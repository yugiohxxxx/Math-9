package com.mathtutor.servlet;

import com.mathtutor.util.Db;
import java.io.IOException;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** Học sinh xem danh sách bài giảng dạng cây: chương -> chủ đề -> bài giảng, kèm trạng thái đã xong / đang học / chưa học. */
@WebServlet("/student/lessons")
public class StudentLessonsServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Map<String, Object>> chapters = Db.query("SELECT ChapterId, Title FROM Chapters WHERE IsActive=1 ORDER BY SortOrder, ChapterId");
        List<Map<String, Object>> topics = Db.query("SELECT TopicId, ChapterId, Title FROM Topics WHERE IsActive=1 ORDER BY SortOrder, TopicId");
        List<Map<String, Object>> lessons = Db.query(
            "SELECT l.LessonId, l.TopicId, l.Title, l.ContentType, ISNULL(lp.Status,'not_started') AS Status FROM Lessons l "
            + "LEFT JOIN LessonProgress lp ON lp.LessonId=l.LessonId AND lp.StudentId=? WHERE l.IsPublished=1 "
            + "ORDER BY l.SortOrder, l.LessonId", uid(req));
        // Gắn bài giảng vào chủ đề, rồi gắn chủ đề vào chương (dữ liệu dạng cây cho trang JSP)
        for (Map<String, Object> t : topics) {
            List<Map<String, Object>> l = new ArrayList<>();
            for (Map<String, Object> x : lessons) if (x.get("topicId").equals(t.get("topicId"))) l.add(x);
            t.put("lessons", l);
        }
        for (Map<String, Object> c : chapters) {
            List<Map<String, Object>> t = new ArrayList<>();
            for (Map<String, Object> x : topics) if (x.get("chapterId").equals(c.get("chapterId"))) t.add(x);
            c.put("topics", t);
        }
        req.setAttribute("chapters", chapters);
        view(req, resp, "student/lessons.jsp");
    }
}
