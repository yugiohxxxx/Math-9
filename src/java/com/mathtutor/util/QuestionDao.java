package com.mathtutor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestionDao {

    /** opts: 4 phần tử A-D (chỉ dùng cho mcq), correct: "A".."D". */
    public static long insert(int topicId, Integer lessonId, String type, String content, String imageUrl,
                              int level, String solution, String source, int createdBy,
                              String[] opts, String correct) {
        long id = Db.insert("INSERT INTO Questions(TopicId,LessonId,QuestionType,Content,ImageUrl,Level,Solution,Source,CreatedBy) "
                + "VALUES(?,?,?,?,?,?,?,?,?)", topicId, lessonId, type, content, imageUrl, level, solution, source, createdBy);
        if ("mcq".equals(type) && opts != null) {
            String[] labels = {"A", "B", "C", "D"};
            for (int i = 0; i < 4; i++) {
                if (Text.blank(opts[i])) continue;
                Db.update("INSERT INTO QuestionOptions(QuestionId,OptionLabel,Content,IsCorrect) VALUES(?,?,?,?)",
                        id, labels[i], opts[i].trim(), labels[i].equalsIgnoreCase(correct) ? 1 : 0);
            }
        }
        return id;
    }

    /** Bài giảng có thuộc chủ đề không (lessonId null luôn hợp lệ). */
    public static boolean lessonInTopic(Integer lessonId, Integer topicId) {
        return lessonId == null || (topicId != null && Db.count("SELECT COUNT(*) FROM Lessons WHERE LessonId=? AND TopicId=?", lessonId, topicId) > 0);
    }

    /**
     * Phân tích đầu ra AI. Mỗi câu bắt đầu bằng dòng ###, gồm các dòng Q:, A:, B:, C:, D:, ANS:, SOL:.
     * Trả về số câu đã thêm.
     */
    public static int importFromAi(String text, int topicId, Integer lessonId, String type, int level, String source, int createdBy) {
        if (text == null) return 0;
        text = text.replace("```", "");
        int added = 0;
        for (String block : text.split("(?m)^\\s*###.*$")) {
            if (block.trim().isEmpty()) continue;
            String q = Text.section(block, "Q", "A", "B", "C", "D", "ANS", "SOL");
            if (Text.blank(q)) continue;
            String sol = Text.section(block, "SOL", "Q", "A", "B", "C", "D", "ANS");
            if ("mcq".equals(type)) {
                String[] o = {
                    Text.section(block, "A", "B", "C", "D", "ANS", "SOL", "Q"),
                    Text.section(block, "B", "A", "C", "D", "ANS", "SOL", "Q"),
                    Text.section(block, "C", "A", "B", "D", "ANS", "SOL", "Q"),
                    Text.section(block, "D", "A", "B", "C", "ANS", "SOL", "Q")};
                String ans = Text.section(block, "ANS", "A", "B", "C", "D", "SOL", "Q");
                // lấy chữ cái A-D trong dòng ANS (chấp nhận "C", "**C**", "(C)", "Đáp án C")
                Matcher am = ans == null ? null : Pattern.compile("\\b([A-Da-d])\\b").matcher(ans.replaceAll("[*_()]", " "));
                if (o[0] == null || o[1] == null || am == null || !am.find()) continue;
                insert(topicId, lessonId, "mcq", Text.aiToHtml(q), null, level, Text.aiToHtml(sol), source, createdBy,
                        o, am.group(1).toUpperCase());
            } else {
                insert(topicId, lessonId, "essay", Text.aiToHtml(q), null, level, Text.aiToHtml(sol), source, createdBy, null, null);
            }
            added++;
        }
        return added;
    }
}
