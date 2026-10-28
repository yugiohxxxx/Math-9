package com.mathtutor.servlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

/** Trả về ảnh / PDF đã tải lên (lưu ngoài thư mục build, xem BaseServlet.uploadRoot). Cần đăng nhập (AuthFilter). */
@WebServlet("/uploads/*")
public class UploadsServlet extends BaseServlet {
    /** Trả về tệp theo đường dẫn /uploads/...; chặn các đường dẫn có ".." để không đọc được tệp ngoài thư mục uploads. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String p = req.getPathInfo();
        if (p == null || p.contains("..") || p.contains("\\") || p.contains(":")) { resp.sendError(404); return; }
        File f = uploadFile("uploads" + p);
        if (!f.isFile()) { resp.sendError(404); return; }
        resp.setContentType(mimeOf(f.getName()));
        resp.setHeader("Cache-Control", "private, max-age=86400");
        Files.copy(f.toPath(), resp.getOutputStream());
    }
}
