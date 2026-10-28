<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Đăng nhập | MathTutor</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.css" rel="stylesheet">
  <link href="${ctx}/assets/css/app.css" rel="stylesheet">
</head>
<body>
<div class="auth">

  <!-- Bảng đen bên trái (ẩn trên điện thoại) -->
  <section class="auth-board" aria-hidden="true">
    <a class="brand" href="${ctx}/login">
      <span class="brand-mark">∑</span>
      <span class="brand-name">MathTutor<span class="brand-sub">Gia sư Toán 9</span></span>
    </a>
    <div class="board-formula">$$x_{1,2} = \frac{-b \pm \sqrt{\Delta}}{2a}$$</div>
    <p class="board-caption"><strong>Học Toán 9 theo từng bước.</strong> Xem bài giảng, luyện trắc nghiệm và nộp ảnh bài tự luận để AI chấm.</p>
  </section>

  <!-- Biểu mẫu đăng nhập -->
  <section class="auth-form-wrap">
    <div class="auth-form">
      <div class="auth-mobile-brand">
        <a class="brand" href="${ctx}/login">
          <span class="brand-mark">∑</span>
          <span class="brand-name">MathTutor<span class="brand-sub">Gia sư Toán 9</span></span>
        </a>
      </div>

      <h1>Đăng nhập</h1>
      <p class="lead-sm">Chào mừng bạn quay lại MathTutor.</p>

      <c:if test="${not empty error}">
        <div class="alert alert-danger d-flex align-items-center gap-2 py-2" role="alert">
          <i class="bi bi-exclamation-triangle"></i><span>${error}</span></div>
      </c:if>

      <form action="${ctx}/login" method="post">
        <div class="mb-3">
          <label class="form-label" for="username">Tên đăng nhập</label>
          <div class="input-icon">
            <i class="bi bi-person"></i>
            <input type="text" id="username" name="username" class="form-control" autocomplete="username" required autofocus>
          </div>
        </div>
        <div class="mb-4">
          <label class="form-label" for="password">Mật khẩu</label>
          <div class="input-icon">
            <i class="bi bi-lock"></i>
            <input type="password" id="password" name="password" class="form-control" autocomplete="current-password" required>
          </div>
        </div>
        <button class="btn btn-primary btn-lg w-100" type="submit">Đăng nhập</button>
      </form>
      <div class="text-center mt-3">Chưa có tài khoản? <a href="${ctx}/register" class="fw-semibold">Đăng ký tài khoản học sinh</a></div>
      <p class="text-muted small mt-4 mb-0">Quên mật khẩu? Liên hệ giáo viên hoặc quản trị viên để được cấp lại.</p>
    </div>
  </section>
</div>

<script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/auto-render.min.js"></script>
<script>
  document.addEventListener("DOMContentLoaded", function () {
    renderMathInElement(document.querySelector('.board-formula'), {delimiters: [{left: "$$", right: "$$", display: true}]});
  });
</script>
</body>
</html>
