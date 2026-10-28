package com.mathtutor.servlet;

import com.mathtutor.util.*;
import java.io.IOException;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;

/**
 * Tự luận: hiện đề bài, học sinh làm ra giấy rồi CHỤP ẢNH bài làm để nộp; AI chấm ảnh (prompt grade_essay).
 * Bài chưa đạt thì được nộp lại ảnh mới (tối đa MAX_PHOTO lần). Câu hoàn thành khi đạt hoặc hết lượt.
 * Mỗi lần nộp lưu ở bảng AiSolveSteps với StepNo = PHOTO_BASE + n (không cần đổi CSDL).
 */
@WebServlet("/student/practice-essay")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024)
public class PracticeEssayServlet extends BaseServlet {
    private static final int PHOTO_BASE = 100;
    private static final int MAX_PHOTO = 3;
    private static final Set<String> IMG = new HashSet<>(Arrays.asList("png", "jpg", "jpeg", "webp"));

    /** Lấy phiên luyện tập, chỉ khi phiên thuộc học sinh đang đăng nhập. */
    private Map<String, Object> session(HttpServletRequest req, Integer sid) {
        return sid == null ? null : Db.one("SELECT ps.SessionId, ps.PracticeType, ps.Score, ps.FinishedAt, t.Title AS TopicTitle "
                + "FROM PracticeSessions ps JOIN Topics t ON t.TopicId=ps.TopicId WHERE ps.SessionId=? AND ps.StudentId=?", sid, uid(req));
    }

    /** Các câu của phiên, theo thứ tự. IsCorrect = null nghĩa là câu đó chưa hoàn thành. */
    private List<Map<String, Object>> answers(int sid) {
        return Db.query("SELECT AnswerId, QuestionId, IsCorrect, EssayImageUrl FROM PracticeAnswers WHERE SessionId=? ORDER BY AnswerId", sid);
    }

    /** Các lần nộp ảnh của một câu (lưu trong bảng AiSolveSteps với StepNo >= 100). */
    private List<Map<String, Object>> photos(Object answerId) {
        return Db.query("SELECT StepId, StepNo, AiPrompt, StudentInput, AiResponse, IsStepCorrect FROM AiSolveSteps "
                + "WHERE AnswerId=? AND StepNo>=? ORDER BY StepNo", answerId, PHOTO_BASE);
    }

    /** Bỏ thẻ HTML để đưa vào prompt cho AI. */
    private static String plain(Object html) {
        if (html == null) return "";
        return html.toString().replaceAll("<br\\s*/?>", "\n").replaceAll("<[^>]+>", " ")
                .replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&").replace("&nbsp;", " ").replaceAll("[ \\t]+", " ").trim();
    }

    /** Hiện một câu tự luận (theo tham số index), các ảnh đã nộp, ô nộp ảnh và cây danh sách câu bên phải. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer sid = intParam(req, "sessionId");
        Map<String, Object> ps = session(req, sid);
        if (ps == null) { fail(req, resp, 404, "Không tìm thấy phiên luyện tập."); return; }
        List<Map<String, Object>> ans = answers(sid);
        if (ans.isEmpty()) { fail(req, resp, 404, "Phiên luyện tập không có câu hỏi."); return; }
        // Không truyền index thì mở câu đầu tiên chưa làm
        int idx = intParam(req, "index") == null ? firstOpen(ans) : Math.max(1, Math.min(ans.size(), intParam(req, "index")));
        Map<String, Object> a = ans.get(idx - 1);
        Map<String, Object> q = Db.one("SELECT QuestionId, Content, ImageUrl, Solution FROM Questions WHERE QuestionId=?", a.get("questionId"));
        boolean finished = a.get("correct") != null;
        List<Map<String, Object>> photos = photos(a.get("answerId"));

        List<Map<String, Object>> tree = new ArrayList<>(); // cây bên phải
        for (int i = 0; i < ans.size(); i++) {
            Map<String, Object> n = new HashMap<>();
            n.put("index", i + 1);
            n.put("done", ans.get(i).get("correct") != null);
            n.put("correct", Boolean.TRUE.equals(ans.get(i).get("correct")));
            tree.add(n);
        }
        req.setAttribute("practice", ps);
        req.setAttribute("question", q);
        req.setAttribute("currentIndex", idx);
        req.setAttribute("totalQuestions", ans.size());
        req.setAttribute("photos", photos);
        req.setAttribute("photoLeft", MAX_PHOTO - photos.size());
        req.setAttribute("finished", finished);
        req.setAttribute("tree", tree);
        req.setAttribute("hasNext", idx < ans.size());
        req.setAttribute("allDone", ans.stream().allMatch(x -> x.get("correct") != null));
        view(req, resp, "student/practice-essay.jsp");
    }

    /** Vị trí (từ 1) của câu đầu tiên chưa hoàn thành; nếu đã xong hết thì trả về 1. */
    private static int firstOpen(List<Map<String, Object>> ans) {
        for (int i = 0; i < ans.size(); i++) if (ans.get(i).get("correct") == null) return i + 1;
        return 1;
    }

    /** Học sinh nộp ảnh bài làm: lưu ảnh, gửi ảnh + đề + lời giải mẫu cho AI chấm, lưu kết quả. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer sid = intParam(req, "sessionId");
        Map<String, Object> ps = session(req, sid);
        if (ps == null) { fail(req, resp, 404, "Không tìm thấy phiên luyện tập."); return; }
        List<Map<String, Object>> ans = answers(sid);
        Integer qid = intParam(req, "questionId");
        int idx = 0;
        Map<String, Object> a = null;
        for (int i = 0; i < ans.size(); i++)
            if (qid != null && qid.equals(ans.get(i).get("questionId"))) { a = ans.get(i); idx = i + 1; }
        // Tìm câu đang nộp trong phiên (chống gửi questionId không thuộc phiên)
        if (a == null) { redirect(req, resp, "/student/practice"); return; }
        String back = "/student/practice-essay?sessionId=" + sid + "&index=" + idx;
        if (a.get("correct") != null) { redirect(req, resp, back); return; }

        // Kiểm tra tệp gửi lên phải là ảnh
        Part img = req.getPart("image");
        String name = partName(img).toLowerCase();
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "";
        byte[] bytes = partBytes(img);
        if (bytes == null || !IMG.contains(ext)) {
            flash(req, "Hãy chụp ảnh bài làm và chọn tệp ảnh (png, jpg, webp).");
            redirect(req, resp, back);
            return;
        }
        String path = store(bytes, name, "essays");
        List<Map<String, Object>> prev = photos(a.get("answerId"));
        int n = prev.size() + 1;
        Map<String, Object> q = Db.one("SELECT Content, Solution FROM Questions WHERE QuestionId=?", qid);

        StringBuilder before = new StringBuilder();
        for (Map<String, Object> p : prev)
            before.append("- Lần ").append(((Number) p.get("stepNo")).intValue() - PHOTO_BASE).append(": ").append(plain(p.get("aiResponse"))).append("\n");
        // Gửi cho AI: prompt chấm bài + đề + lời giải mẫu + nhận xét các lần trước + ẢNH bài làm; chờ tối đa 60 giây
        GeminiService.Cfg cfg = GeminiService.load("grade_essay");
        String out = GeminiService.generate(cfg, cfg.prompt + "\n\nĐỀ BÀI: " + plain(q.get("content"))
                + (q.get("solution") == null ? "" : "\nLỜI GIẢI THAM KHẢO: " + plain(q.get("solution")))
                + (before.length() == 0 ? "" : "\nNhận xét các lần nộp trước:\n" + before)
                + "\nẢnh đính kèm là bài làm viết tay của học sinh (lần nộp thứ " + n + "). Hãy chấm bài làm trong ảnh. "
                + "Nếu ảnh không phải bài làm của đề này hoặc không đọc được thì coi là chưa đạt.\n"
                + "Trả lời ĐÚNG định dạng (tiếng Việt, công thức trong $...$):\n"
                + "CORRECT: yes nếu lời giải đúng và đầy đủ, no nếu chưa\n"
                + "SCORE: điểm trên thang 10\n"
                + "FEEDBACK: nhận xét ngắn (tối đa 4 câu), chỉ rõ chỗ sai nếu có, không viết lại toàn bộ lời giải",
                bytes, mimeOf(path), 60000);

        // Đọc kết quả AI theo định dạng CORRECT / SCORE / FEEDBACK
        boolean ok, aiMissing = out == null;
        String fb;
        if (aiMissing) {
            ok = false;
            fb = "Đã nhận ảnh bài làm. AI chưa được cấu hình hoặc không phản hồi nên chưa chấm được"
                    + (q.get("solution") == null ? "." : ". <b>Lời giải mẫu:</b><br>" + q.get("solution"));
        } else {
            String c = Text.section(out, "CORRECT", "SCORE", "FEEDBACK");
            ok = c != null && c.replaceAll("[^A-Za-z]", "").toLowerCase().startsWith("y");
            String sc = Text.section(out, "SCORE", "CORRECT", "FEEDBACK");
            String f = Text.section(out, "FEEDBACK", "CORRECT", "SCORE");
            fb = (Text.blank(sc) ? "" : "<b>Điểm: " + Text.aiToHtml(sc) + "</b><br>") + Text.aiToHtml(f != null ? f : out);
        }
        // Lưu lần nộp này (StepNo = 100 + số lần) và cập nhật ảnh mới nhất của câu
        Db.update("INSERT INTO AiSolveSteps(AnswerId,StepNo,AiPrompt,StudentInput,AiResponse,IsStepCorrect) VALUES(?,?,?,?,?,?)",
                a.get("answerId"), PHOTO_BASE + n, "Ảnh bài làm (lần " + n + ")", path, fb, ok ? 1 : 0);
        Db.update("UPDATE PracticeAnswers SET EssayImageUrl=? WHERE AnswerId=?", path, a.get("answerId"));

        // Kết thúc câu khi: AI chấm đạt, hoặc AI không chạy, hoặc đã nộp đủ 3 lần.
        // Nếu tất cả câu trong phiên đã xong thì tính điểm phiên = số câu đạt x 10 / tổng số câu.
        if (ok || aiMissing || n >= MAX_PHOTO) {
            Db.update("UPDATE PracticeAnswers SET IsCorrect=?, AiComment=?, AnsweredAt=GETDATE() WHERE AnswerId=?", ok ? 1 : 0, fb, a.get("answerId"));
            if (Db.count("SELECT COUNT(*) FROM PracticeAnswers WHERE SessionId=? AND IsCorrect IS NULL", sid) == 0)
                Db.update("UPDATE PracticeSessions SET FinishedAt=GETDATE(), Score=(SELECT CAST(SUM(CASE WHEN IsCorrect=1 THEN 1.0 ELSE 0 END)*10/COUNT(*) AS DECIMAL(5,2)) "
                        + "FROM PracticeAnswers WHERE SessionId=?) WHERE SessionId=?", sid, sid);
            flash(req, ok ? "Bài làm đạt yêu cầu. 🎉"
                    : (aiMissing ? "Đã nộp ảnh. Câu này đã hoàn thành." : "Bạn đã dùng hết " + MAX_PHOTO + " lần nộp. Xem nhận xét và lời giải mẫu ở trang kết quả."));
        } else {
            flash(req, "Bài làm chưa đạt. Xem nhận xét, sửa lại trên giấy rồi chụp và nộp ảnh mới (còn " + (MAX_PHOTO - n) + " lần).");
        }
        redirect(req, resp, back);
    }
}
