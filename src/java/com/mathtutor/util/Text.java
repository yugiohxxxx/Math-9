package com.mathtutor.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Text {
    public static boolean blank(String s) { return s == null || s.trim().isEmpty(); }

    public static String nullIfBlank(String s) { return blank(s) ? null : s.trim(); }

    /** Chuyển văn bản AI (markdown nhẹ) sang HTML an toàn; giữ nguyên $công thức$ cho KaTeX. */
    public static String aiToHtml(String s) {
        if (s == null) return null;
        s = s.replace("```", "").trim();
        s = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        s = s.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
        s = s.replace("\r\n", "\n").replace("\n", "<br>");
        return s;
    }

    public static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        return out.toByteArray();
    }

    /** Lấy phần nội dung sau nhãn TAG: cho đến nhãn kế tiếp (hoặc hết chuỗi). */
    public static String section(String text, String tag, String... nextTags) {
        if (text == null) return null;
        // Chấp nhận cả nhãn AI in đậm / gạch đầu dòng: "Q:", "**Q:**", "**Q**:", "- Q:", "### Q:"
        String pre = "[ \\t*_#>-]*", post = "[*_]*\\s*:[*_]*";
        StringBuilder stop = new StringBuilder("$");
        for (String t : nextTags) stop.append("|\\n").append(pre).append(t).append(post);
        Matcher m = Pattern.compile("(?s)(?:^|\\n)" + pre + tag + post + "\\s*(.*?)(?=" + stop + ")").matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }

    /** Link YouTube -> link embed. */
    public static String embedUrl(String url) {
        if (blank(url)) return url;
        Matcher m = Pattern.compile("(?:v=|youtu\\.be/|embed/)([\\w-]{11})").matcher(url);
        return m.find() ? "https://www.youtube.com/embed/" + m.group(1) : url;
    }
}
