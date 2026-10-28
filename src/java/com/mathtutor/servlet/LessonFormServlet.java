package com.mathtutor.servlet;

import com.mathtutor.util.*;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** (Giáo viên) Form soạn / sửa bài giảng. Nội dung soạn bằng trình soạn thảo TinyMCE. */
@WebServlet("/manage/lesson-form")
public class LessonFormServlet extends BaseServlet {
    /** Hiện form; có id thì nạp sẵn bài giảng để sửa. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = intParam(req, "id");
        if (id != null) req.setAttribute("lesson", Db.one("SELECT * FROM Lessons WHERE LessonId=?", id));
        req.setAttribute("topics", Db.query("SELECT TopicId, Title FROM Topics WHERE IsActive=1 ORDER BY ChapterId, SortOrder"));
        view(req, resp, "manage/lesson-form.jsp");
    }

    /** Lưu bài giảng (thêm mới hoặc cập nhật). */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = intParam(req, "id");
        // Ô "Hiển thị cho học sinh": tích thì học sinh mới thấy bài giảng
        int published = req.getParameter("published") != null ? 1 : 0;
        Integer sort = intParam(req, "sortOrder") == null ? 0 : intParam(req, "sortOrder");
        // Gom các trường của form theo đúng thứ tự cột trong câu lệnh SQL bên dưới
        Object[] f = {intParam(req, "topicId"), param(req, "title"), param(req, "contentType"), req.getParameter("contentHtml"),
            Text.nullIfBlank(req.getParameter("videoUrl")), Text.nullIfBlank(req.getParameter("iframeUrl")), sort, published};
        if (id == null) {
            Db.update("INSERT INTO Lessons(TopicId,Title,ContentType,ContentHtml,VideoUrl,IframeUrl,SortOrder,IsPublished,CreatedBy) VALUES(?,?,?,?,?,?,?,?,?)",
                    f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7], uid(req));
        } else {
            Db.update("UPDATE Lessons SET TopicId=?,Title=?,ContentType=?,ContentHtml=?,VideoUrl=?,IframeUrl=?,SortOrder=?,IsPublished=?,UpdatedAt=GETDATE() WHERE LessonId=?",
                    f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7], id);
        }
        flash(req, "Đã lưu bài giảng.");
        redirect(req, resp, "/manage/lessons");
    }
}
