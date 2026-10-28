package com.mathtutor.servlet;

import com.mathtutor.util.Db;
import java.io.IOException;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** Trang kết quả một phiên luyện tập: điểm, từng câu đúng/sai, đáp án đã chọn, đáp án đúng, nhận xét AI, lời giải mẫu. */
@WebServlet("/student/practice-result")
public class PracticeResultServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer sid = intParam(req, "sessionId");
        Map<String, Object> ps = sid == null ? null : Db.one("SELECT ps.SessionId, ps.PracticeType, ps.Score, t.Title AS TopicTitle "
                + "FROM PracticeSessions ps JOIN Topics t ON t.TopicId=ps.TopicId WHERE ps.SessionId=? AND ps.StudentId=?", sid, uid(req));
        if (ps == null) { fail(req, resp, 404, "Không tìm thấy phiên luyện tập."); return; }
        // so = đáp án học sinh chọn, co = đáp án đúng (LEFT JOIN vì câu tự luận không có đáp án);
        // với tự luận thì có ảnh bài làm (EssayImageUrl) và nhận xét AI (AiComment)
        List<Map<String, Object>> answers = Db.query(
            "SELECT pa.AnswerId, pa.IsCorrect, q.Content AS QuestionContent, q.Solution, so.OptionLabel AS SelectedLabel, so.Content AS SelectedContent,"
            + " co.OptionLabel AS CorrectLabel, co.Content AS CorrectContent, pa.EssayText, pa.EssayImageUrl, pa.AiComment"
            + " FROM PracticeAnswers pa JOIN Questions q ON q.QuestionId=pa.QuestionId"
            + " LEFT JOIN QuestionOptions so ON so.OptionId=pa.SelectedOptionId"
            + " LEFT JOIN QuestionOptions co ON co.QuestionId=q.QuestionId AND co.IsCorrect=1"
            + " WHERE pa.SessionId=? ORDER BY pa.AnswerId", sid);
        int ok = 0;
        for (Map<String, Object> a : answers) if (Boolean.TRUE.equals(a.get("correct"))) ok++;
        req.setAttribute("practice", ps);
        req.setAttribute("answers", answers);
        req.setAttribute("correctCount", ok);
        view(req, resp, "student/practice-result.jsp");
    }
}
