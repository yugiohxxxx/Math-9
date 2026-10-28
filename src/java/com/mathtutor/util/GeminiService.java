package com.mathtutor.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/** Gọi Gemini API. Cấu hình (API key, model, prompt) lấy từ bảng AiConfigs do Admin chỉnh. */
public class GeminiService {

    /** Model mặc định. gemini-1.5 / 2.0 đã bị Google tắt; 2.5 không còn mở cho tài khoản mới. */
    public static final String DEFAULT_MODEL = "gemini-3.8-flash";

    /** Đổi tên model đã ngừng hoạt động sang model mặc định. */
    static String upgrade(String model) {
        String m = model == null ? "" : model.trim();
        if (m.isEmpty() || m.startsWith("gemini-1.") || m.startsWith("gemini-2.0") || m.equals("gemini-pro")) return DEFAULT_MODEL;
        return m;
    }

    public static class Cfg {
        public String key, model, url, prompt;
        public boolean ready() { return key != null; }
    }

    private static boolean validKey(Object k) {
        if (k == null) return false;
        String s = k.toString().trim();
        return !s.isEmpty() && !s.equals("YOUR_API_KEY");
    }

    public static Cfg load(String promptType) {
        Cfg c = new Cfg();
        Map<String, Object> row = Db.one("SELECT TOP 1 ApiKey, ModelName, ApiUrl, PromptContent FROM AiConfigs "
                + "WHERE PromptType=? AND IsActive=1 ORDER BY ConfigId DESC", promptType);
        c.model = DEFAULT_MODEL;
        c.prompt = "";
        if (row != null) {
            if (row.get("modelName") != null) c.model = upgrade(row.get("modelName").toString());
            if (row.get("apiUrl") != null) c.url = row.get("apiUrl").toString();
            if (row.get("promptContent") != null) c.prompt = row.get("promptContent").toString();
            if (validKey(row.get("apiKey"))) c.key = row.get("apiKey").toString().trim();
        }
        if (c.key == null) { // dùng key của bất kỳ cấu hình nào đã nhập
            Map<String, Object> any = Db.one("SELECT TOP 1 ApiKey FROM AiConfigs WHERE IsActive=1 "
                    + "AND ApiKey NOT IN ('', 'YOUR_API_KEY') ORDER BY ConfigId DESC");
            if (any != null) c.key = any.get("apiKey").toString().trim();
        }
        if (c.key == null && validKey(System.getenv("GEMINI_API_KEY"))) c.key = System.getenv("GEMINI_API_KEY");
        return c;
    }

    /** Trả về text AI hoặc null nếu chưa có key / gọi lỗi. data/mime: tệp đính kèm (ảnh, pdf) - có thể null. */
    public static String generate(Cfg c, String prompt, byte[] data, String mime) {
        return generate(c, prompt, data, mime, 90000);
    }

    /** Model dự phòng: nhẹ, nhanh, dùng khi model chính quá tải (HTTP 429/500/503/504). */
    public static final String FALLBACK_MODEL = "gemini-3.5-flash-lite";

    private static final ThreadLocal<String> LAST_ERROR = new ThreadLocal<>();
    private static final ThreadLocal<Integer> LAST_CODE = new ThreadLocal<>();

    /** Lỗi của lần gọi gần nhất trên luồng hiện tại (null nếu thành công). */
    public static String lastError() { return LAST_ERROR.get(); }

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(GeminiService.class.getName());

    /** Như trên nhưng tự đặt thời gian chờ đọc phản hồi (ms). */
    public static String generate(Cfg c, String prompt, byte[] data, String mime, int readTimeoutMs) {
        if (c == null || !c.ready()) return null;
        // Gemini 3.x mặc định "suy nghĩ" mức medium -> chậm. Ứng dụng chỉ cần trả lời ngắn nên đặt mức low.
        boolean custom = c.url != null && c.url.startsWith("http");
        boolean low = c.model != null && c.model.startsWith("gemini-3") && !custom;
        String out = call(c, prompt, data, mime, readTimeoutMs, low);
        // Model chính đang quá tải / lỗi tạm thời phía Google -> thử ngay một lần với model dự phòng
        Integer code = LAST_CODE.get();
        if (out == null && !custom && !FALLBACK_MODEL.equals(c.model) && code != null
                && (code == 429 || code == 500 || code == 503 || code == 504)) {
            LOG.warning("[Gemini] Model " + c.model + " lỗi " + code + ", chuyển sang model dự phòng " + FALLBACK_MODEL);
            Cfg d = new Cfg();
            d.key = c.key; d.prompt = c.prompt; d.model = FALLBACK_MODEL;
            out = call(d, prompt, data, mime, readTimeoutMs, true);
        }
        return out;
    }

    /** Gọi đúng model trong cấu hình, KHÔNG tự chuyển sang model dự phòng (dùng cho nút "Kiểm tra kết nối AI"). */
    public static String probe(Cfg c, String prompt, int readTimeoutMs) {
        if (c == null || !c.ready()) return null;
        boolean custom = c.url != null && c.url.startsWith("http");
        return call(c, prompt, null, null, readTimeoutMs, c.model != null && c.model.startsWith("gemini-3") && !custom);
    }

    private static String call(Cfg c, String prompt, byte[] data, String mime, int readTimeoutMs, boolean lowThinking) {
        long t0 = System.currentTimeMillis();
        LAST_ERROR.remove();
        LAST_CODE.remove();
        try {
            String endpoint = (c.url != null && c.url.startsWith("http")) ? c.url
                    : "https://generativelanguage.googleapis.com/v1beta/models/" + c.model + ":generateContent";
            if (c.url != null && c.url.startsWith("http")) endpoint += (endpoint.contains("?") ? "&" : "?") + "key=" + c.key; // URL tuỳ chỉnh: giữ cách cũ

            StringBuilder body = new StringBuilder("{\"contents\":[{\"parts\":[{\"text\":\"")
                    .append(esc(prompt)).append("\"}");
            if (data != null && mime != null)
                body.append(",{\"inline_data\":{\"mime_type\":\"").append(mime).append("\",\"data\":\"")
                        .append(Base64.getEncoder().encodeToString(data)).append("\"}}");
            body.append("]}]");
            if (lowThinking) body.append(",\"generationConfig\":{\"thinkingConfig\":{\"thinkingLevel\":\"low\"}}");
            body.append("}");

            HttpURLConnection h = (HttpURLConnection) new URL(endpoint).openConnection();
            h.setRequestMethod("POST");
            h.setConnectTimeout(Math.min(15000, readTimeoutMs));
            h.setReadTimeout(readTimeoutMs);
            h.setDoOutput(true);
            h.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            h.setRequestProperty("x-goog-api-key", c.key); // khoá đặt ở header, không lộ trong URL/log
            try (OutputStream os = h.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }
            int code = h.getResponseCode();
            InputStream in = code < 400 ? h.getInputStream() : h.getErrorStream();
            String resp = in == null ? "" : new String(Text.readAll(in), StandardCharsets.UTF_8);
            long ms = System.currentTimeMillis() - t0;
            if (code >= 400) {
                LOG.warning("[Gemini] HTTP " + code + " sau " + ms + " ms (model " + c.model + "): "
                        + (resp.length() > 600 ? resp.substring(0, 600) : resp));
                java.util.regex.Matcher em = java.util.regex.Pattern.compile("\"message\"\\s*:\\s*\"([^\"]{0,300})").matcher(resp);
                LAST_ERROR.set("HTTP " + code + (em.find() ? ": " + em.group(1) : ""));
                LAST_CODE.set(code);
                if (code == 400 && lowThinking && resp.toLowerCase().contains("thinking")) // model không hỗ trợ thinkingLevel
                    return call(c, prompt, data, mime, readTimeoutMs, false);
                // model không tồn tại / đã ngừng -> thử lại một lần với model mặc định
                if (code == 404 && !DEFAULT_MODEL.equals(c.model) && (c.url == null || !c.url.startsWith("http"))) {
                    Cfg d = new Cfg();
                    d.key = c.key; d.prompt = c.prompt; d.model = DEFAULT_MODEL;
                    return generate(d, prompt, data, mime, readTimeoutMs);
                }
                return null;
            }
            String text = extractText(resp);
            LOG.info("[Gemini] OK sau " + ms + " ms (model " + c.model + ", " + (text == null ? 0 : text.length()) + " ký tự)");
            if (text == null) LAST_ERROR.set("Phản hồi không có nội dung (có thể bị chặn bởi bộ lọc an toàn)");
            if (text == null) LOG.warning("[Gemini] Phản hồi không có nội dung: " + (resp.length() > 600 ? resp.substring(0, 600) : resp));
            return text;
        } catch (Exception e) {
            LOG.warning("[Gemini] Lỗi sau " + (System.currentTimeMillis() - t0) + " ms (model " + c.model + "): " + e);
            LAST_ERROR.set(e instanceof java.net.SocketTimeoutException ? "Quá thời gian chờ, Gemini không trả lời" : e.toString());
            return null;
        }
    }

    static String esc(String s) {
        StringBuilder sb = new StringBuilder();
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch)); else sb.append(ch);
            }
        }
        return sb.toString();
    }

    /** Lấy chuỗi JSON đầu tiên sau "text": trong phản hồi. */
    static String extractText(String json) {
        int i = json.indexOf("\"text\"");
        if (i < 0) return null;
        i = json.indexOf('"', json.indexOf(':', i) + 1);
        if (i < 0) return null;
        StringBuilder sb = new StringBuilder();
        for (int p = i + 1; p < json.length(); p++) {
            char ch = json.charAt(p);
            if (ch == '"') return sb.toString();
            if (ch == '\\' && p + 1 < json.length()) {
                char n = json.charAt(++p);
                switch (n) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case 'u': sb.append((char) Integer.parseInt(json.substring(p + 1, p + 5), 16)); p += 4; break;
                    default: sb.append(n);
                }
            } else sb.append(ch);
        }
        return null;
    }
}
