package com.mathtutor.servlet;

import com.mathtutor.util.Db;
import java.io.IOException;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** (Giáo viên) Quản lý chương và chủ đề của môn Toán 9. */
@WebServlet("/manage/chapters")
public class ChaptersServlet extends BaseServlet {
    /** Lấy danh sách chương, rồi gắn các chủ đề vào đúng chương của nó để hiển thị dạng accordion. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Map<String, Object>> chapters = Db.query("SELECT ChapterId, Title FROM Chapters WHERE IsActive=1 ORDER BY SortOrder, ChapterId");
        List<Map<String, Object>> topics = Db.query("SELECT TopicId, ChapterId, Title FROM Topics WHERE IsActive=1 ORDER BY SortOrder, TopicId");
        for (Map<String, Object> c : chapters) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (Map<String, Object> t : topics)
                if (t.get("chapterId").equals(c.get("chapterId"))) list.add(t);
            c.put("topics", list);
        }
        req.setAttribute("chapters", chapters);
        view(req, resp, "manage/chapters.jsp");
    }

    /** action = addChapter | addTopic | deleteTopic | deleteChapter. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String a = param(req, "action");
        Integer id = intParam(req, "id");
        if ("addChapter".equals(a)) {
            Db.update("INSERT INTO Chapters(Title,Description,SortOrder) VALUES(?,?,?)", param(req, "title"),
                    param(req, "description"), intParam(req, "sortOrder") == null ? 0 : intParam(req, "sortOrder"));
            flash(req, "Đã thêm chương.");
        } else if ("addTopic".equals(a)) {
            Db.update("INSERT INTO Topics(ChapterId,Title,Description,CreatedBy) VALUES(?,?,?,?)",
                    intParam(req, "chapterId"), param(req, "title"), param(req, "description"), uid(req));
            flash(req, "Đã thêm chủ đề.");
        } else if ("deleteTopic".equals(a) && id != null) {
            // Chỉ xoá chủ đề khi chưa có bài giảng, câu hỏi hay lượt luyện tập nào dùng đến
            if (Db.count("SELECT (SELECT COUNT(*) FROM Lessons WHERE TopicId=?)+(SELECT COUNT(*) FROM Questions WHERE TopicId=?)+(SELECT COUNT(*) FROM PracticeSessions WHERE TopicId=?)", id, id, id) > 0)
                flash(req, "Chủ đề đang có bài giảng/câu hỏi/lượt luyện tập nên không xoá được.");
            else { Db.update("DELETE FROM Topics WHERE TopicId=?", id); flash(req, "Đã xoá chủ đề."); }
        } else if ("deleteChapter".equals(a) && id != null) {
            // Chỉ xoá chương khi chương đã trống (không còn chủ đề)
            if (Db.count("SELECT COUNT(*) FROM Topics WHERE ChapterId=?", id) > 0)
                flash(req, "Hãy xoá hết chủ đề của chương trước.");
            else { Db.update("DELETE FROM Chapters WHERE ChapterId=?", id); flash(req, "Đã xoá chương."); }
        }
        redirect(req, resp, "/manage/chapters");
    }
}
