package com.mathtutor.servlet;

import com.mathtutor.util.*;
import java.io.IOException;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** Xem một bài giảng. Học sinh xem thì hệ thống ghi nhận tiến độ học. Giáo viên/admin được xem thử. */
@WebServlet("/student/lesson")
public class LessonViewServlet extends BaseServlet {
    /**
     * Câu lệnh MERGE: nếu học sinh đã có dòng tiến độ của bài này thì cập nhật, chưa có thì thêm mới.
     * Không hạ trạng thái "completed" (đã xong) về "in_progress" (đang học) khi học sinh mở lại bài.
     */
    private static final String UPSERT =
        "MERGE LessonProgress AS t USING (SELECT ? AS s, ? AS l) AS src ON t.StudentId=src.s AND t.LessonId=src.l "
        + "WHEN MATCHED THEN UPDATE SET Status = CASE WHEN ?='in_progress' AND t.Status='completed' THEN t.Status ELSE ? END, LastViewedAt=GETDATE() "
        + "WHEN NOT MATCHED THEN INSERT(StudentId,LessonId,Status,LastViewedAt) VALUES(src.s,src.l,?,GETDATE());";

    /** Hiện bài giảng; học sinh mở bài thì đánh dấu "đang học". */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = intParam(req, "id");
        Map<String, Object> l = id == null ? null : Db.one(
            "SELECT l.LessonId, l.TopicId, l.Title, l.ContentType, l.ContentHtml, l.VideoUrl, l.IframeUrl, t.Title AS TopicTitle "
            + "FROM Lessons l JOIN Topics t ON t.TopicId=l.TopicId WHERE l.LessonId=?"
            + ("student".equals(role(req)) ? " AND l.IsPublished=1" : ""), id);
        if (l == null) { fail(req, resp, 404, "Không tìm thấy bài giảng."); return; }
        // Đổi link YouTube thường thành link nhúng (embed) để hiện trong khung video
        l.put("embedUrl", Text.embedUrl((String) l.get("videoUrl")));
        if ("student".equals(role(req))) Db.update(UPSERT, uid(req), id, "in_progress", "in_progress", "in_progress");
        req.setAttribute("lesson", l);
        view(req, resp, "student/lesson.jsp");
    }

    /** Nút "Đánh dấu đã học xong" của học sinh. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = intParam(req, "id");
        if (id != null && "student".equals(role(req))) {
            String st = "completed".equals(param(req, "status")) ? "completed" : "in_progress";
            Db.update(UPSERT, uid(req), id, st, st, st);
            flash(req, "Đã đánh dấu bài giảng là đã học xong. 🎉");
        }
        redirect(req, resp, "/student/lesson?id=" + id);
    }
}
