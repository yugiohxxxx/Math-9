# MathTutor – Gia sư Toán 9

Ứng dụng web học Toán lớp 9: bài giảng, luyện tập trắc nghiệm và tự luận (học sinh chụp ảnh bài làm, AI Gemini chấm), quản lý ngân hàng câu hỏi theo bài và độ khó, dashboard cho admin và giáo viên.

## Công nghệ
- Java 17, Jakarta Servlet 6 + JSP/JSTL, chạy trên Apache Tomcat 10.1 (dự án NetBeans)
- SQL Server (JDBC `mssql-jdbc`)
- Bootstrap 5, Chart.js, KaTeX, DataTables, TinyMCE
- Google Gemini API (cấu hình trong trang Admin → Cấu hình AI)

## Chạy dự án
1. Tạo cơ sở dữ liệu `MathTutor` trên SQL Server.
2. Sao chép `src/java/db.properties.example` thành `src/java/db.properties` và điền mật khẩu SQL Server
   (file `db.properties` không được đưa lên GitHub).
3. Mở dự án bằng NetBeans, thêm các thư viện: `mssql-jdbc`, `jakarta.servlet.jsp.jstl`, `jakarta.servlet.jsp.jstl-api`.
4. **Clean and Build** rồi **Run** trên Tomcat 10.1.
5. (Tuỳ chọn) Chạy `database/them_450_cau_hoi.sql` trong SSMS để thêm 450 câu hỏi mẫu.
6. Đăng nhập admin, vào **Cấu hình AI** nhập API key Gemini rồi bấm **Kiểm tra kết nối AI**.

## Cấu trúc mã nguồn
- `src/java/com/mathtutor/filter` – lọc UTF-8 và kiểm tra đăng nhập / phân quyền
- `src/java/com/mathtutor/servlet` – các servlet (đã có chú thích tiếng Việt)
- `src/java/com/mathtutor/util` – truy cập CSDL, gọi Gemini, xử lý chuỗi
- `web/WEB-INF/views` – các trang JSP theo vai trò (admin, manage, student)
- `web/assets/css/app.css` – giao diện riêng
