package com.mathtutor.servlet;

import com.mathtutor.util.Db;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** (Giáo viên) Danh sách bài giảng và xoá bài giảng. */
@WebServlet("/manage/lessons")
public class LessonsServlet extends BaseServlet {
    /** Danh sách bài giảng, sắp xếp theo chương -> chủ đề -> thứ tự bài. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("lessons", Db.query("SELECT l.LessonId, l.Title, t.Title AS TopicTitle, l.ContentType, l.IsPublished "
                + "FROM Lessons l JOIN Topics t ON t.TopicId=l.TopicId ORDER BY t.ChapterId, t.SortOrder, l.SortOrder, l.LessonId"));
        view(req, resp, "manage/lessons.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = intParam(req, "id");
        if ("delete".equals(param(req, "action")) && id != null) {
            // Trước khi xoá bài giảng: xoá tiến độ học của học sinh, và gỡ liên kết bài giảng khỏi câu hỏi / phiên luyện tập
            // (để không vi phạm khoá ngoại và không mất câu hỏi, lịch sử luyện tập)
            Db.update("DELETE FROM LessonProgress WHERE LessonId=?", id);
            Db.update("UPDATE Questions SET LessonId=NULL WHERE LessonId=?", id);
            Db.update("UPDATE PracticeSessions SET LessonId=NULL WHERE LessonId=?", id);
            Db.update("DELETE FROM Lessons WHERE LessonId=?", id);
            flash(req, "Đã xoá bài giảng.");
        }
        redirect(req, resp, "/manage/lessons");
    }
}
