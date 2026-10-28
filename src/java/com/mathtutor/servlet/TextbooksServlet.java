package com.mathtutor.servlet;

import com.mathtutor.util.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;

/** (Admin) Quản lý sách giáo khoa (PDF hoặc link) và nhờ AI tách bài tập tự luận vào ngân hàng câu hỏi. */
@WebServlet("/admin/textbooks")
@MultipartConfig(maxFileSize = 30 * 1024 * 1024, maxRequestSize = 32 * 1024 * 1024)
public class TextbooksServlet extends BaseServlet {
    /** Hiện danh sách sách và danh sách chủ đề (để chọn khi tách bài tập). */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("textbooks", Db.query("SELECT TextbookId, Title, SourceUrl, PdfPath, UploadedAt FROM Textbooks ORDER BY TextbookId DESC"));
        req.setAttribute("topics", Db.query("SELECT TopicId, Title FROM Topics WHERE IsActive=1 ORDER BY ChapterId, SortOrder"));
        view(req, resp, "admin/textbooks.jsp");
    }

    /** action=add: thêm sách; action=delete: xoá sách; action=extract: nhờ AI tách bài tập. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = param(req, "action");
        if ("add".equals(action)) {
            // Lưu file PDF (nếu có) vào thư mục uploads/pdf, rồi ghi thông tin sách vào CSDL
            Part pdf = req.getPart("pdf");
            String path = store(partBytes(pdf), partName(pdf), "pdf");
            Db.update("INSERT INTO Textbooks(Title,SourceUrl,PdfPath,UploadedBy) VALUES(?,?,?,?)",
                    param(req, "title"), Text.nullIfBlank(req.getParameter("sourceUrl")), path, uid(req));
            flash(req, "Đã thêm sách.");
        } else if ("delete".equals(action)) {
            Db.update("DELETE FROM Textbooks WHERE TextbookId=?", intParam(req, "id"));
            flash(req, "Đã xoá sách.");
        } else if ("extract".equals(action)) {
            flash(req, extract(intParam(req, "id"), intParam(req, "topicId"), uid(req)));
        }
        redirect(req, resp, "/admin/textbooks");
    }

    /** Nhờ AI tách các bài tập tự luận từ file PDF (hoặc nội dung trang web) rồi lưu vào ngân hàng câu hỏi. */
    private String extract(Integer id, Integer topicId, int userId) throws IOException {
        Map<String, Object> b = Db.one("SELECT Title, SourceUrl, PdfPath FROM Textbooks WHERE TextbookId=?", id);
        if (b == null || topicId == null) return "Thiếu dữ liệu.";
        // Dùng cấu hình AI của loại "generate_question"
        GeminiService.Cfg cfg = GeminiService.load("generate_question");
        if (!cfg.ready()) return "Chưa cấu hình API key trong mục Cấu hình AI.";

        String task = "Từ tài liệu Toán 9 được cung cấp, hãy tách các BÀI TẬP TỰ LUẬN (không lấy lý thuyết). "
                + "Giữ nguyên đề, viết công thức bằng LaTeX trong $...$. Mỗi bài bắt đầu bằng một dòng ###, "
                + "sau đó là dòng 'Q:' (đề bài) và dòng 'SOL:' (lời giải ngắn gọn nếu có). Không thêm lời dẫn. Tối đa 15 bài.";
        String out;
        // Ưu tiên gửi nguyên file PDF cho AI đọc
        if (b.get("pdfPath") != null) {
            File pf = uploadFile(b.get("pdfPath").toString());
            if (!pf.isFile()) return "Không tìm thấy file PDF của sách này (có thể đã mất khi build lại). Hãy xoá sách và tải PDF lên lại.";
            byte[] data = Files.readAllBytes(pf.toPath());
            out = GeminiService.generate(cfg, task, data, "application/pdf");
        // Không có PDF: tải trang web về, bỏ thẻ HTML/script, chỉ giữ chữ (tối đa 40.000 ký tự) rồi gửi cho AI
        } else if (b.get("sourceUrl") != null) {
            String html;
            try {
                HttpURLConnection h = (HttpURLConnection) new URL(b.get("sourceUrl").toString()).openConnection();
                h.setRequestProperty("User-Agent", "Mozilla/5.0");
                h.setConnectTimeout(15000);
                h.setReadTimeout(30000);
                html = new String(Text.readAll(h.getInputStream()), StandardCharsets.UTF_8);
            } catch (IOException e) {
                return "Không tải được link: " + e.getMessage();
            }
            String text = html.replaceAll("(?s)<script.*?</script>|<style.*?</style>", " ").replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ");
            if (text.length() > 40000) text = text.substring(0, 40000);
            out = GeminiService.generate(cfg, task + "\n\nNỘI DUNG:\n" + text, null, null);
        } else {
            return "Sách chưa có PDF hoặc link.";
        }
        if (out == null) return "AI không phản hồi (kiểm tra API key / model).";
        // Đọc kết quả AI theo định dạng ### / Q: / SOL: và lưu thành câu hỏi tự luận mức 2, nguồn "textbook"
        int n = QuestionDao.importFromAi(out, topicId, null, "essay", 2, "textbook", userId);
        return "AI đã tách và thêm " + n + " bài tập vào ngân hàng câu hỏi.";
    }
}
