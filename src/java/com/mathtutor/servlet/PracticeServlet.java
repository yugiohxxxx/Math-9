package com.mathtutor.servlet;

import com.mathtutor.util.Db;
import java.io.IOException;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** Trang chọn bài/chủ đề/số câu/mức độ rồi tạo phiên luyện tập. */
@WebServlet("/student/practice")
public class PracticeServlet extends BaseServlet {
    /** Hiện form chọn chủ đề / bài học / hình thức / số câu / mức độ, kèm 10 phiên luyện tập gần nhất. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("topics", Db.query("SELECT TopicId, Title FROM Topics WHERE IsActive=1 ORDER BY ChapterId, SortOrder"));
        req.setAttribute("lessons", Db.query("SELECT LessonId, Title, TopicId FROM Lessons WHERE IsPublished=1 ORDER BY SortOrder, LessonId"));
        req.setAttribute("history", Db.query("SELECT TOP 10 ps.SessionId, ps.StartedAt, t.Title AS TopicTitle, ps.PracticeType, ps.Score "
                + "FROM PracticeSessions ps JOIN Topics t ON t.TopicId=ps.TopicId WHERE ps.StudentId=? ORDER BY ps.SessionId DESC", uid(req)));
        view(req, resp, "student/practice.jsp");
    }

    /** Tạo phiên luyện tập mới: chọn ngẫu nhiên câu hỏi phù hợp rồi chuyển sang trang làm bài. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer topicId = intParam(req, "topicId"), lessonId = intParam(req, "lessonId"), level = intParam(req, "level");
        // Số câu: mặc định 5, giới hạn từ 1 đến 30
        int n = intParam(req, "numQuestions") == null ? 5 : Math.max(1, Math.min(30, intParam(req, "numQuestions")));
        String type = "essay".equals(param(req, "practiceType")) ? "essay" : "mcq";
        if (topicId == null) { redirect(req, resp, "/student/practice"); return; }

        // Lấy ngẫu nhiên (ORDER BY NEWID()) N câu đang dùng, đúng chủ đề và hình thức; thêm lọc bài học / mức độ nếu có
        StringBuilder sql = new StringBuilder("SELECT TOP (?) QuestionId FROM Questions WHERE IsActive=1 AND TopicId=? AND QuestionType=?");
        List<Object> p = new ArrayList<>(Arrays.asList(n, topicId, type));
        if (lessonId != null) { sql.append(" AND LessonId=?"); p.add(lessonId); }
        if (level != null) { sql.append(" AND Level=?"); p.add(level); }
        if ("mcq".equals(type))
            // Trắc nghiệm: chỉ lấy câu có đúng MỘT đáp án đúng (bỏ qua câu bị lỗi dữ liệu)
            sql.append(" AND (SELECT COUNT(*) FROM QuestionOptions o WHERE o.QuestionId=Questions.QuestionId AND o.IsCorrect=1)=1");
        sql.append(" ORDER BY NEWID()");
        List<Map<String, Object>> qs = Db.query(sql.toString(), p.toArray());
        if (qs.isEmpty()) {
            flash(req, "Chưa có câu hỏi phù hợp với lựa chọn của bạn. Hãy thử đổi chủ đề / mức độ / hình thức.");
            redirect(req, resp, "/student/practice");
            return;
        }
        // Tạo phiên luyện tập, rồi tạo sẵn một dòng bài làm (PracticeAnswers) cho mỗi câu
        long sid = Db.insert("INSERT INTO PracticeSessions(StudentId,TopicId,LessonId,PracticeType,NumQuestions,Level) VALUES(?,?,?,?,?,?)",
                uid(req), topicId, lessonId, type, qs.size(), level);
        for (Map<String, Object> q : qs)
            Db.update("INSERT INTO PracticeAnswers(SessionId,QuestionId) VALUES(?,?)", sid, q.get("questionId"));
        redirect(req, resp, "mcq".equals(type) ? "/student/practice-mcq?sessionId=" + sid : "/student/practice-essay?sessionId=" + sid + "&index=1");
    }
}
