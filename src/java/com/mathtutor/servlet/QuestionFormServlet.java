package com.mathtutor.servlet;

import com.mathtutor.util.*;
import java.io.IOException;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;

/** (Giáo viên) Form thêm / sửa một câu hỏi: chủ đề, bài giảng, loại, độ khó, nội dung, ảnh, 4 đáp án, lời giải. */
@WebServlet("/manage/question-form")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024)
public class QuestionFormServlet extends BaseServlet {
    /** Hiện form; có id thì nạp sẵn câu hỏi và các đáp án để sửa. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = intParam(req, "id");
        if (id != null) {
            Map<String, Object> q = Db.one("SELECT * FROM Questions WHERE QuestionId=?", id);
            if (q != null) {
                List<Map<String, Object>> os = Db.query("SELECT OptionId, OptionLabel, Content, IsCorrect FROM QuestionOptions WHERE QuestionId=? ORDER BY OptionLabel", id);
                Map<String, Object> byLabel = new HashMap<>(); // form hiển thị theo nhãn A-D, không theo thứ tự
                for (Map<String, Object> o : os) byLabel.put(String.valueOf(o.get("optionLabel")).trim(), o);
                q.put("options", os);
                q.put("optByLabel", byLabel);
            }
            req.setAttribute("q", q);
        }
        req.setAttribute("topics", Db.query("SELECT TopicId, Title FROM Topics WHERE IsActive=1 ORDER BY ChapterId, SortOrder"));
        // Danh sách bài giảng cho ô "Bài giảng" (trang JSP lọc theo chủ đề đang chọn)
        req.setAttribute("lessons", Db.query("SELECT l.LessonId, l.Title, l.TopicId, l.IsPublished FROM Lessons l JOIN Topics t ON t.TopicId=l.TopicId ORDER BY t.ChapterId, t.SortOrder, l.SortOrder, l.LessonId"));
        view(req, resp, "manage/question-form.jsp");
    }

    /** Lưu câu hỏi. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = intParam(req, "id");
        String type = "essay".equals(param(req, "questionType")) ? "essay" : "mcq";
        Part img = req.getPart("image");
        // Lưu ảnh minh hoạ (nếu có chọn) vào uploads/questions
        String imageUrl = store(partBytes(img), partName(img), "questions");
        String content = req.getParameter("content");
        String solution = Text.nullIfBlank(req.getParameter("solution"));
        int level = intParam(req, "level") == null ? 1 : intParam(req, "level");
        String[] labels = {"A", "B", "C", "D"};
        String[] opts = new String[4];
        for (int i = 0; i < 4; i++) opts[i] = req.getParameter("opt" + labels[i]);
        String correct = param(req, "correct");
        Integer topicId = intParam(req, "topicId"), lessonId = intParam(req, "lessonId");
        // Không cho gắn câu hỏi vào bài giảng thuộc chủ đề khác
        if (!QuestionDao.lessonInTopic(lessonId, topicId)) {
            flash(req, "Bài giảng đã chọn không thuộc chủ đề này.");
            redirect(req, resp, "/manage/question-form" + (id == null ? "" : "?id=" + id));
            return;
        }

        if ("mcq".equals(type)) {
            // Câu trắc nghiệm bắt buộc phải chọn đáp án đúng và đáp án đó phải có nội dung
            boolean ok = correct != null && correct.length() == 1 && "ABCD".contains(correct)
                    && !Text.blank(opts["ABCD".indexOf(correct)]);
            if (!ok) {
                flash(req, "Câu trắc nghiệm cần có đáp án đúng và nội dung của đáp án đó.");
                redirect(req, resp, "/manage/question-form" + (id == null ? "" : "?id=" + id));
                return;
            }
        }
        if (id == null) {
            // Thêm mới: lưu câu hỏi và các đáp án A-D
            QuestionDao.insert(topicId, lessonId, type, content, imageUrl, level, solution, "manual", uid(req), opts, correct);
        } else {
            // Sửa: cập nhật câu hỏi; không chọn ảnh mới thì giữ ảnh cũ (ISNULL)
            Db.update("UPDATE Questions SET TopicId=?, LessonId=?, QuestionType=?, Content=?, Level=?, Solution=?, ImageUrl=ISNULL(?,ImageUrl) WHERE QuestionId=?",
                    topicId, lessonId, type, content, level, solution, imageUrl, id);
            if ("mcq".equals(type)) {
                List<String> kept = new ArrayList<>();
                for (int i = 0; i < 4; i++) { // cập nhật tại chỗ để không vỡ khoá ngoại với bài đã làm
                    Map<String, Object> ex = Db.one("SELECT OptionId FROM QuestionOptions WHERE QuestionId=? AND OptionLabel=?", id, labels[i]);
                    int isC = labels[i].equals(correct) ? 1 : 0;
                    if (Text.blank(opts[i])) { // đáp án bị xoá trắng
                        if (ex == null) continue;
                        if (Db.count("SELECT COUNT(*) FROM PracticeAnswers WHERE SelectedOptionId=?", ex.get("optionId")) == 0) {
                            Db.update("DELETE FROM QuestionOptions WHERE OptionId=?", ex.get("optionId"));
                        } else { // đã có học sinh chọn -> giữ lại để không mất lịch sử, nhưng không còn là đáp án đúng
                            Db.update("UPDATE QuestionOptions SET IsCorrect=0 WHERE OptionId=?", ex.get("optionId"));
                            kept.add(labels[i]);
                        }
                        continue;
                    }
                    if (ex != null) Db.update("UPDATE QuestionOptions SET Content=?, IsCorrect=? WHERE OptionId=?", opts[i].trim(), isC, ex.get("optionId"));
                    else Db.update("INSERT INTO QuestionOptions(QuestionId,OptionLabel,Content,IsCorrect) VALUES(?,?,?,?)", id, labels[i], opts[i].trim(), isC);
                }
                // bảo đảm luôn có đúng MỘT đáp án đúng
                Db.update("UPDATE QuestionOptions SET IsCorrect = CASE WHEN OptionLabel=? THEN 1 ELSE 0 END WHERE QuestionId=?", correct, id);
                if (!kept.isEmpty()) {
                    flash(req, "Đã lưu câu hỏi. Đáp án " + String.join(", ", kept)
                            + " đã có học sinh chọn nên được giữ lại (không còn là đáp án đúng).");
                    redirect(req, resp, "/manage/questions");
                    return;
                }
            }
        }
        flash(req, "Đã lưu câu hỏi.");
        redirect(req, resp, "/manage/questions");
    }
}
