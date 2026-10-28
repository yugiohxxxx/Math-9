<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Đăng ký | MathTutor</title>
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
    <p class="board-caption"><strong>Bắt đầu học Toán 9 ngay hôm nay.</strong> Tạo tài khoản học sinh miễn phí để xem bài giảng và luyện tập cùng AI.</p>
  </section>

  <!-- Biểu mẫu đăng ký -->
  <section class="auth-form-wrap">
    <div class="auth-form">
      <div class="auth-mobile-brand">
        <a class="brand" href="${ctx}/login">
          <span class="brand-mark">∑</span>
          <span class="brand-name">MathTutor<span class="brand-sub">Gia sư Toán 9</span></span>
        </a>
      </div>

      <h1>Tạo tài khoản</h1>
      <p class="lead-sm">Tài khoản đăng ký ở đây là tài khoản <b>học sinh</b>.</p>

      <c:if test="${not empty error}">
        <div class="alert alert-danger d-flex align-items-center gap-2 py-2" role="alert">
          <i class="bi bi-exclamation-triangle"></i><span><c:out value="${error}"/></span></div>
      </c:if>

      <form action="${ctx}/register" method="post" id="regForm">
        <div class="mb-3">
          <label class="form-label" for="fullName">Họ và tên</label>
          <div class="input-icon">
            <i class="bi bi-person-vcard"></i>
            <input type="text" id="fullName" name="fullName" class="form-control" maxlength="100" autocomplete="name" required autofocus
                   value="<c:out value='${param.fullName}'/>">
          </div>
        </div>
        <div class="mb-3">
          <label class="form-label" for="username">Tên đăng nhập</label>
          <div class="input-icon">
            <i class="bi bi-person"></i>
            <input type="text" id="username" name="username" class="form-control" minlength="3" maxlength="30" pattern="[A-Za-z0-9._]{3,30}"
                   autocomplete="username" required value="<c:out value='${param.username}'/>">
          </div>
          <div class="form-text">3-30 ký tự: chữ không dấu, số, dấu chấm hoặc gạch dưới.</div>
        </div>
        <div class="mb-3">
          <label class="form-label" for="email">Email</label>
          <div class="input-icon">
            <i class="bi bi-envelope"></i>
            <input type="email" id="email" name="email" class="form-control" maxlength="100" autocomplete="email" required
                   value="<c:out value='${param.email}'/>">
          </div>
        </div>
        <div class="mb-3">
          <label class="form-label" for="password">Mật khẩu</label>
          <div class="input-icon">
            <i class="bi bi-lock"></i>
            <input type="password" id="password" name="password" class="form-control" minlength="6" autocomplete="new-password" required>
          </div>
          <div class="form-text">Ít nhất 6 ký tự.</div>
        </div>
        <div class="mb-4">
          <label class="form-label" for="password2">Nhập lại mật khẩu</label>
          <div class="input-icon">
            <i class="bi bi-lock-fill"></i>
            <input type="password" id="password2" name="password2" class="form-control" minlength="6" autocomplete="new-password" required>
          </div>
        </div>
        <button class="btn btn-primary btn-lg w-100" type="submit">Tạo tài khoản</button>
      </form>
      <p class="text-muted small mt-4 mb-0">Đã có tài khoản? <a href="${ctx}/login">Đăng nhập</a></p>
    </div>
  </section>
</div>

<script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/katex.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/katex@0.16.9/dist/contrib/auto-render.min.js"></script>
<script>
  document.addEventListener("DOMContentLoaded", function () {
    renderMathInElement(document.querySelector('.board-formula'), {delimiters: [{left: "$$", right: "$$", display: true}]});
  });
  // Báo ngay trên trình duyệt nếu hai mật khẩu không khớp (máy chủ vẫn kiểm tra lại)
  var p1 = document.getElementById('password'), p2 = document.getElementById('password2');
  function match() { p2.setCustomValidity(p2.value && p1.value !== p2.value ? 'Hai lần nhập mật khẩu không khớp' : ''); }
  p1.addEventListener('input', match); p2.addEventListener('input', match);
</script>
</body>
</html>
