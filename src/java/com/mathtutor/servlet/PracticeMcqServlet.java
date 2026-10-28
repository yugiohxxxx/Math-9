package com.mathtutor.servlet;

import com.mathtutor.util.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** GET: hiển thị đề trắc nghiệm. POST (/student/practice-submit): chấm bài, nhờ AI nhận xét câu sai. */
@WebServlet({"/student/practice-mcq", "/student/practice-submit"})
public class PracticeMcqServlet extends BaseServlet {
    /** Tổng thời gian tối đa chờ AI nhận xét khi nộp bài (giây). */
    private static final int AI_TOTAL_SECONDS = 25;
    /** Số câu gọi AI cùng lúc. */
    private static final int AI_THREADS = 5;

    /** Lấy phiên luyện tập, chỉ khi phiên thuộc học sinh đang đăng nhập (không xem được bài người khác). */
    private Map<String, Object> session(HttpServletRequest req, Integer sid) {
        return sid == null ? null : Db.one("SELECT ps.SessionId, ps.PracticeType, ps.Score, ps.FinishedAt, t.Title AS TopicTitle "
                + "FROM PracticeSessions ps JOIN Topics t ON t.TopicId=ps.TopicId WHERE ps.SessionId=? AND ps.StudentId=?", sid, uid(req));
    }

    /** Hiện đề trắc nghiệm; phiên đã nộp rồi thì chuyển sang trang kết quả. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer sid = intParam(req, "sessionId");
        Map<String, Object> ps = session(req, sid);
        if (ps == null) { fail(req, resp, 404, "Không tìm thấy phiên luyện tập."); return; }
        if (ps.get("finishedAt") != null) { redirect(req, resp, "/student/practice-result?sessionId=" + sid); return; }
        List<Map<String, Object>> qs = Db.query("SELECT q.QuestionId, q.Content, q.ImageUrl, q.Level FROM PracticeAnswers pa "
                + "JOIN Questions q ON q.QuestionId=pa.QuestionId WHERE pa.SessionId=? ORDER BY pa.AnswerId", sid);
        // Nạp 4 đáp án cho từng câu
        for (Map<String, Object> q : qs)
            q.put("options", Db.query("SELECT OptionId, OptionLabel, Content FROM QuestionOptions WHERE QuestionId=? ORDER BY OptionLabel", q.get("questionId")));
        req.setAttribute("practice", ps);
        req.setAttribute("questions", qs);
        view(req, resp, "student/practice-mcq.jsp");
    }

    /** Nộp bài: chấm điểm, lưu đáp án đã chọn, AI nhận xét các câu sai, tính điểm thang 10. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer sid = intParam(req, "sessionId");
        Map<String, Object> ps = session(req, sid);
        if (ps == null) { fail(req, resp, 404, "Không tìm thấy phiên luyện tập."); return; }
        if (ps.get("finishedAt") != null) { redirect(req, resp, "/student/practice-result?sessionId=" + sid); return; }

        GeminiService.Cfg cfg = GeminiService.load("grade_mcq");
        List<Map<String, Object>> answers = Db.query("SELECT pa.AnswerId, q.QuestionId, q.Content, q.Solution FROM PracticeAnswers pa "
                + "JOIN Questions q ON q.QuestionId=pa.QuestionId WHERE pa.SessionId=? ORDER BY pa.AnswerId", sid);
        // 1) Chấm điểm (nhanh, chỉ truy vấn CSDL) và chuẩn bị câu hỏi cho AI với các câu sai
        int n = answers.size(), correct = 0;
        Integer[] chosenId = new Integer[n];
        boolean[] okArr = new boolean[n];
        String[] prompts = new String[n];
        for (int i = 0; i < n; i++) {
            Map<String, Object> a = answers.get(i);
            Integer chosen = intParam(req, "q" + a.get("questionId"));
            Map<String, Object> right = Db.one("SELECT OptionLabel, Content FROM QuestionOptions WHERE QuestionId=? AND IsCorrect=1", a.get("questionId"));
            Map<String, Object> sel = chosen == null ? null
                    : Db.one("SELECT OptionLabel, Content, IsCorrect FROM QuestionOptions WHERE OptionId=? AND QuestionId=?", chosen, a.get("questionId"));
            okArr[i] = sel != null && Boolean.TRUE.equals(sel.get("correct"));
            chosenId[i] = sel == null ? null : chosen;
            if (okArr[i]) correct++;
            else if (cfg.ready()) {
                String solution = (String) a.get("solution");
                prompts[i] = cfg.prompt + "\n\nCâu hỏi: " + a.get("content")
                        + "\nĐáp án đúng: " + (right == null ? "" : right.get("optionLabel") + ". " + right.get("content"))
                        + "\nHọc sinh chọn: " + (sel == null ? "(bỏ trống)" : sel.get("optionLabel") + ". " + sel.get("content"))
                        + (solution == null ? "" : "\nLời giải mẫu: " + solution)
                        + "\nHãy nhận xét ngắn gọn (tối đa 4 câu, tiếng Việt, công thức trong $...$): vì sao chưa đúng và gợi ý cách làm.";
            }
        }

        // 2) Gọi AI cho các câu sai CÙNG LÚC, tổng thời gian chờ có giới hạn
        String[] ai = askAiParallel(cfg, prompts);

        // 3) Lưu kết quả
        for (int i = 0; i < n; i++) {
            String comment = null;
            if (!okArr[i]) comment = ai[i] != null ? Text.aiToHtml(ai[i]) : (chosenId[i] == null ? "Bạn chưa chọn đáp án." : null);
            Db.update("UPDATE PracticeAnswers SET SelectedOptionId=?, IsCorrect=?, AiComment=?, AnsweredAt=GETDATE() WHERE AnswerId=?",
                    chosenId[i], okArr[i] ? 1 : 0, comment, answers.get(i).get("answerId"));
        }
        // Điểm = số câu đúng x 10 / tổng số câu (làm tròn 2 chữ số), rồi đánh dấu phiên đã hoàn thành
        BigDecimal score = answers.isEmpty() ? BigDecimal.ZERO
                : BigDecimal.valueOf(correct * 10.0 / answers.size()).setScale(2, java.math.RoundingMode.HALF_UP);
        Db.update("UPDATE PracticeSessions SET FinishedAt=GETDATE(), Score=? WHERE SessionId=?", score, sid);
        redirect(req, resp, "/student/practice-result?sessionId=" + sid);
    }

    /** Gọi AI cho các prompt khác null cùng lúc; câu nào quá AI_TOTAL_SECONDS thì để null (trang kết quả sẽ hiện lời giải mẫu). */
    private String[] askAiParallel(GeminiService.Cfg cfg, String[] prompts) {
        String[] out = new String[prompts.length];
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < prompts.length; i++) if (prompts[i] != null) idx.add(i);
        if (idx.isEmpty()) return out;
        ExecutorService pool = Executors.newFixedThreadPool(Math.min(AI_THREADS, idx.size()));
        try {
            Map<Integer, Future<String>> fs = new LinkedHashMap<>();
            for (int i : idx) {
                final String p = prompts[i];
                fs.put(i, pool.submit(() -> GeminiService.generate(cfg, p, null, null, AI_TOTAL_SECONDS * 1000)));
            }
            long deadline = System.currentTimeMillis() + AI_TOTAL_SECONDS * 1000L;
            for (Map.Entry<Integer, Future<String>> e : fs.entrySet()) {
                long left = deadline - System.currentTimeMillis();
                try {
                    out[e.getKey()] = e.getValue().get(Math.max(1, left), TimeUnit.MILLISECONDS);
                } catch (TimeoutException ex) {
                    e.getValue().cancel(true);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (ExecutionException ex) {
                    log("Lỗi gọi AI", ex);
                }
            }
        } finally {
            pool.shutdownNow();
        }
        return out;
    }
}
