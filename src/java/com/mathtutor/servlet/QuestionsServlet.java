package com.mathtutor.servlet;

import com.mathtutor.util.*;
import java.io.IOException;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** (Giáo viên) Ngân hàng câu hỏi: xem, lọc, xoá và nhờ AI sinh câu hỏi. */
@WebServlet("/manage/questions")
public class QuestionsServlet extends BaseServlet {
    /** Danh sách câu hỏi, lọc theo chủ đề / bài giảng / loại / độ khó (các bộ lọc đều tuỳ chọn). */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        StringBuilder sql = new StringBuilder("SELECT q.QuestionId, q.Content, t.Title AS TopicTitle, l.Title AS LessonTitle, q.QuestionType, q.Level, q.Source "
                + "FROM Questions q JOIN Topics t ON t.TopicId=q.TopicId LEFT JOIN Lessons l ON l.LessonId=q.LessonId WHERE q.IsActive=1");
        // Ghép thêm điều kiện WHERE theo từng bộ lọc mà người dùng đã chọn; tham số đưa vào danh sách p (tránh SQL injection)
        List<Object> p = new ArrayList<>();
        if (intParam(req, "topicId") != null) { sql.append(" AND q.TopicId=?"); p.add(intParam(req, "topicId")); }
        if ("none".equals(param(req, "lessonId"))) sql.append(" AND q.LessonId IS NULL");
        else if (intParam(req, "lessonId") != null) { sql.append(" AND q.LessonId=?"); p.add(intParam(req, "lessonId")); }
        if (!Text.blank(req.getParameter("type"))) { sql.append(" AND q.QuestionType=?"); p.add(param(req, "type")); }
        if (intParam(req, "level") != null) { sql.append(" AND q.Level=?"); p.add(intParam(req, "level")); }
        sql.append(" ORDER BY q.QuestionId DESC");
        req.setAttribute("questions", Db.query(sql.toString(), p.toArray()));
        req.setAttribute("topics", Db.query("SELECT TopicId, Title FROM Topics WHERE IsActive=1 ORDER BY ChapterId, SortOrder"));
        req.setAttribute("lessons", Db.query("SELECT l.LessonId, l.Title, l.TopicId, l.IsPublished FROM Lessons l JOIN Topics t ON t.TopicId=l.TopicId ORDER BY t.ChapterId, t.SortOrder, l.SortOrder, l.LessonId"));
        view(req, resp, "manage/questions.jsp");
    }

    /** action=delete: xoá câu hỏi; action=aiGenerate: nhờ AI sinh câu hỏi. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String a = param(req, "action");
        if ("delete".equals(a) && intParam(req, "id") != null) {
            Db.update("UPDATE Questions SET IsActive=0 WHERE QuestionId=?", intParam(req, "id")); // xoá mềm để giữ lịch sử luyện tập
            flash(req, "Đã xoá câu hỏi.");
        } else if ("aiGenerate".equals(a)) {
            flash(req, aiGenerate(req));
        }
        redirect(req, resp, "/manage/questions");
    }

    /**
     * Nhờ AI sinh câu hỏi theo chủ đề (và bài giảng nếu có chọn), mức độ, loại câu, số câu (tối đa 20).
     * Trả về thông báo để hiện cho giáo viên.
     */
    private String aiGenerate(HttpServletRequest req) {
        Integer topicId = intParam(req, "topicId"), level = intParam(req, "level"), count = intParam(req, "count"), lessonId = intParam(req, "lessonId");
        String type = "essay".equals(param(req, "type")) ? "essay" : "mcq";
        if (topicId == null || level == null) return "Thiếu thông tin.";
        if (!QuestionDao.lessonInTopic(lessonId, topicId)) return "Bài giảng đã chọn không thuộc chủ đề này.";
        if (count == null || count < 1) count = 5;
        count = Math.min(count, 20); // tránh yêu cầu AI quá dài
        GeminiService.Cfg cfg = GeminiService.load("generate_question");
        if (!cfg.ready()) return "Chưa cấu hình API key trong mục Cấu hình AI.";
        Map<String, Object> tp = Db.one("SELECT Title FROM Topics WHERE TopicId=?", topicId);
        if (tp == null) return "Chủ đề không tồn tại.";
        String topic = (String) tp.get("title");
        String[] lv = {"", "nhận biết", "thông hiểu", "vận dụng", "vận dụng cao"};
        // Nếu chọn bài giảng: đưa tên bài và nội dung bài (đã bỏ thẻ HTML, tối đa 6000 ký tự) vào prompt để AI ra câu sát bài
        String lessonPart = "";
        if (lessonId != null) {
            Map<String, Object> l = Db.one("SELECT Title, ContentHtml FROM Lessons WHERE LessonId=?", lessonId);
            String body = l.get("contentHtml") == null ? "" : l.get("contentHtml").toString().replaceAll("<[^>]+>", " ").replaceAll("&nbsp;", " ").replaceAll("\\s+", " ").trim();
            if (body.length() > 6000) body = body.substring(0, 6000);
            lessonPart = ", bài giảng: " + l.get("title") + (body.isEmpty() ? "" : ". Câu hỏi phải bám sát nội dung bài giảng sau: " + body);
        }
        // Prompt yêu cầu AI trả về đúng định dạng ### / Q: / A:..D: / ANS: / SOL: để hệ thống đọc được
        String spec = cfg.prompt + "\n\nMôn Toán lớp 9, chủ đề: " + topic + lessonPart + ". Hãy tạo " + count + " câu hỏi "
                + ("mcq".equals(type) ? "trắc nghiệm 4 đáp án" : "tự luận") + " mức độ " + lv[Math.max(1, Math.min(4, level))] + ".\n"
                + "Viết công thức bằng LaTeX trong dấu $...$. Trả về ĐÚNG định dạng, mỗi câu bắt đầu bằng một dòng ###:\n"
                + ("mcq".equals(type)
                    ? "###\nQ: nội dung câu hỏi\nA: đáp án A\nB: đáp án B\nC: đáp án C\nD: đáp án D\nANS: chữ cái đáp án đúng (A/B/C/D)\nSOL: lời giải ngắn gọn\n"
                    : "###\nQ: đề bài\nSOL: lời giải chi tiết\n")
                + "Không thêm lời dẫn hay giải thích nào khác.";
        String out = GeminiService.generate(cfg, spec, null, null);
        if (out == null) return "AI không phản hồi (kiểm tra API key / model trong Cấu hình AI).";
        // Đọc kết quả AI và lưu từng câu vào CSDL (nguồn "ai")
        int n = QuestionDao.importFromAi(out, topicId, lessonId, type, level, "ai", uid(req));
        return n > 0 ? "AI đã sinh và thêm " + n + " câu hỏi." : "AI trả về định dạng không đọc được, hãy thử lại.";
    }
}
