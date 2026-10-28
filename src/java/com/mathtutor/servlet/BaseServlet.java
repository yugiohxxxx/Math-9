package com.mathtutor.servlet;

import com.mathtutor.util.Db;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

/**
 * LỚP CHA CỦA MỌI SERVLET TRONG DỰ ÁN.
 * Gom các hàm dùng chung: bắt lỗi chung, chuyển tới trang JSP, chuyển hướng, lấy người dùng đang đăng nhập,
 * đọc tham số từ form, lưu tệp tải lên... Các servlet khác chỉ cần "extends BaseServlet" là dùng được.
 */
public abstract class BaseServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            super.service(req, resp);
        } catch (Db.DbException e) {
            log("Lỗi CSDL", e);
            fail(req, resp, 500, "Lỗi cơ sở dữ liệu. Chi tiết đã được ghi vào log của Tomcat.");
        } catch (RuntimeException e) {
            log("Lỗi", e);
            fail(req, resp, 500, "Đã xảy ra lỗi ngoài ý muốn. Chi tiết đã được ghi vào log của Tomcat.");
        }
    }

    /** Hiện trang lỗi chung (common/error.jsp) với mã lỗi HTTP (404, 500...) và thông báo cho người dùng. */
    protected void fail(HttpServletRequest req, HttpServletResponse resp, int code, String msg) throws ServletException, IOException {
        resp.setStatus(code);
        req.setAttribute("code", code);
        req.setAttribute("message", msg);
        view(req, resp, "common/error.jsp");
    }

    /** Chuyển (forward) sang trang JSP trong /WEB-INF/views để hiển thị. Dữ liệu truyền qua req.setAttribute(...). */
    protected void view(HttpServletRequest req, HttpServletResponse resp, String jsp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + jsp).forward(req, resp);
    }

    /** Chuyển hướng trình duyệt sang đường dẫn khác (tự thêm context path, vd /math9). Dùng sau khi xử lý POST để tránh gửi lại form khi F5. */
    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    /** Lấy thông tin người dùng đang đăng nhập (được LoginServlet lưu trong session với tên "user"). */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> user(HttpServletRequest req) {
        return (Map<String, Object>) req.getSession().getAttribute("user");
    }

    /** Mã người dùng (UserId) đang đăng nhập. */
    protected int uid(HttpServletRequest req) { return ((Number) user(req).get("userId")).intValue(); }

    /** Vai trò người dùng đang đăng nhập: "admin", "teacher" hoặc "student". */
    protected String role(HttpServletRequest req) { return String.valueOf(user(req).get("roleName")); }

    /** Lưu một thông báo ngắn vào session; trang kế tiếp hiển thị một lần rồi xoá (xem layout-header.jspf). */
    protected void flash(HttpServletRequest req, String msg) { req.getSession().setAttribute("flash", msg); }

    /** Đọc tham số dạng số nguyên từ form/URL. Trả về null nếu không có hoặc không phải số (không ném lỗi). */
    protected Integer intParam(HttpServletRequest req, String name) {
        try {
            String v = req.getParameter(name);
            return (v == null || v.trim().isEmpty()) ? null : Integer.valueOf(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Đọc tham số dạng chuỗi từ form/URL, đã bỏ khoảng trắng hai đầu. */
    protected String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? null : v.trim();
    }

    /** Đọc nội dung của part upload (null nếu không có tệp). */
    protected byte[] partBytes(Part p) throws IOException {
        if (p == null || p.getSize() == 0) return null;
        return com.mathtutor.util.Text.readAll(p.getInputStream());
    }

    /** Tên tệp gốc mà người dùng đã chọn khi tải lên (dùng để lấy phần mở rộng .png, .pdf...). */
    protected String partName(Part p) {
        String n = p == null ? null : p.getSubmittedFileName();
        return n == null ? "" : n;
    }

    /** Các loại tệp được phép tải lên. */
    protected static final Set<String> ALLOWED = new HashSet<>(Arrays.asList("png", "jpg", "jpeg", "gif", "webp", "pdf"));

    /** Lưu tệp vào webapp/uploads/<sub>/, trả về đường dẫn tương đối "uploads/<sub>/xxx.ext" (null nếu không hợp lệ). */
    protected String store(byte[] data, String origName, String sub) throws IOException {
        if (data == null || data.length == 0) return null;
        String ext = origName.contains(".") ? origName.substring(origName.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOWED.contains(ext)) return null;
        String rel = "uploads/" + sub + "/" + UUID.randomUUID() + "." + ext;
        File f = new File(uploadRoot(), rel);
        f.getParentFile().mkdirs();
        Files.write(f.toPath(), data);
        return rel;
    }

    /**
     * Thư mục gốc lưu tệp tải lên, nằm NGOÀI thư mục build nên không bị xoá khi Clean and Build.
     * Mặc định: <thư mục người dùng>/MathTutor (vd C:\Users\ten\MathTutor); đổi bằng biến môi trường MATHTUTOR_UPLOAD_DIR.
     */
    public static File uploadRoot() {
        String d = System.getenv("MATHTUTOR_UPLOAD_DIR");
        if (d == null || d.trim().isEmpty()) d = System.getProperty("user.home") + File.separator + "MathTutor";
        return new File(d);
    }

    /** Tìm tệp theo đường dẫn "uploads/<sub>/<tên>": ở thư mục mới trước, nếu không có thì tìm chỗ cũ trong webapp. */
    protected File uploadFile(String rel) {
        File f = new File(uploadRoot(), rel);
        if (!f.exists()) {
            String old = getServletContext().getRealPath("/" + rel);
            if (old != null && new File(old).exists()) return new File(old);
        }
        return f;
    }

    /** Đoán kiểu MIME theo phần mở rộng của tệp (để gửi ảnh/PDF cho AI hoặc trả về trình duyệt). */
    protected String mimeOf(String path) {
        String p = path == null ? "" : path.toLowerCase();
        if (p.endsWith(".png")) return "image/png";
        if (p.endsWith(".gif")) return "image/gif";
        if (p.endsWith(".webp")) return "image/webp";
        if (p.endsWith(".pdf")) return "application/pdf";
        return "image/jpeg";
    }
}
