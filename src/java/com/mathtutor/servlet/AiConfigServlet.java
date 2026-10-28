package com.mathtutor.servlet;

import com.mathtutor.util.*;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/**
 * (Admin) Cấu hình AI: API key, model, prompt cho từng loại việc:
 *   generate_question (sinh câu hỏi), grade_mcq (nhận xét câu trắc nghiệm sai),
 *   solve_step (giải từng bước - hiện không dùng), grade_essay (chấm ảnh bài tự luận).
 */
@WebServlet("/admin/ai-config")
public class AiConfigServlet extends BaseServlet {
    /** Hiện danh sách cấu hình hiện có. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("configs", Db.query("SELECT ConfigId, Provider, ModelName, ApiKey, ApiUrl, PromptType, PromptContent, IsActive "
                + "FROM AiConfigs ORDER BY ConfigId"));
        view(req, resp, "admin/ai-config.jsp");
    }

    /** Lưu cấu hình (có configId là sửa, không có là thêm mới), hoặc chạy kiểm tra kết nối (action=test). */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer id = intParam(req, "configId");
        if ("test".equals(param(req, "action")) && id != null) { flash(req, test(id)); redirect(req, resp, "/admin/ai-config"); return; }
        // Ô "Kích hoạt": checkbox chỉ gửi lên khi được tích
        int active = req.getParameter("active") != null ? 1 : 0;
        if (id != null) {
            Db.update("UPDATE AiConfigs SET Provider=?, ModelName=?, ApiKey=?, ApiUrl=?, PromptContent=?, IsActive=?, UpdatedBy=?, UpdatedAt=GETDATE() WHERE ConfigId=?",
                    param(req, "provider"), param(req, "modelName"), param(req, "apiKey"), Text.nullIfBlank(req.getParameter("apiUrl")),
                    req.getParameter("promptContent"), active, uid(req), id);
        } else {
            Db.update("INSERT INTO AiConfigs(Provider,ApiKey,ModelName,PromptType,PromptContent,IsActive,UpdatedBy) VALUES(?,?,?,?,?,?,?)",
                    param(req, "provider"), param(req, "apiKey"), param(req, "modelName"), param(req, "promptType"),
                    req.getParameter("promptContent"), active, uid(req));
        }
        flash(req, "Đã lưu cấu hình AI.");
        redirect(req, resp, "/admin/ai-config");
    }

    /** Gửi một câu hỏi rất ngắn tới vài model để đo thời gian phản hồi và xem lỗi cụ thể. */
    private String test(int configId) {
        java.util.Map<String, Object> row = Db.one("SELECT ApiKey, ModelName, ApiUrl FROM AiConfigs WHERE ConfigId=?", configId);
        if (row == null || row.get("apiKey") == null || row.get("apiKey").toString().trim().isEmpty()) return "Cấu hình này chưa có API key.";
        java.util.LinkedHashSet<String> models = new java.util.LinkedHashSet<>();
        if (row.get("modelName") != null) models.add(row.get("modelName").toString().trim());
        models.add("gemini-3.5-flash-lite");
        models.add("gemini-2.5-flash");
        StringBuilder sb = new StringBuilder("Kết quả kiểm tra kết nối AI:");
        for (String mdl : models) {
            GeminiService.Cfg c = new GeminiService.Cfg();
            c.key = row.get("apiKey").toString().trim();
            c.model = mdl;
            c.prompt = "";
            if (row.get("apiUrl") != null && mdl.equals(row.get("modelName"))) c.url = row.get("apiUrl").toString();
            long t0 = System.currentTimeMillis();
            String out = GeminiService.probe(c, "Chỉ trả lời đúng một từ: OK", 20000); // thử đúng model này, không tự chuyển model dự phòng
            long ms = System.currentTimeMillis() - t0;
            sb.append(" | ").append(mdl).append(": ").append(out != null ? "trả lời sau " + (ms / 100) / 10.0 + " giây"
                    : "LỖI sau " + (ms / 100) / 10.0 + " giây (" + GeminiService.lastError() + ")");
        }
        return sb.toString();
    }
}
