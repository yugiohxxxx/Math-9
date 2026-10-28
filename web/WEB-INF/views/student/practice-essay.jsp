<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Luyện tập tự luận"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<c:set var="base" value="${ctx}/student/practice-essay?sessionId=${practice.sessionId}"/>

<div class="row g-4">
<!-- ================= Cột chính ================= -->
<div class="col-lg-8">
  <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
    <h3 class="mb-0">Tự luận: <c:out value="${practice.topicTitle}"/></h3>
    <span class="badge text-bg-light border">Câu ${currentIndex} / ${totalQuestions}</span>
  </div>

  <div class="card mb-3 border-primary"><div class="card-header bg-primary text-white">Đề bài</div><div class="card-body">
    <div>${question.content}</div>
    <c:if test="${not empty question.imageUrl}"><img src="${ctx}/${question.imageUrl}" class="img-fluid mt-2" style="max-height:240px" alt="Hình minh hoạ đề bài"></c:if>
  </div></div>

  <%-- Các lần đã nộp ảnh --%>
  <c:forEach var="p" items="${photos}">
    <div class="card mb-2"><div class="card-body">
      <div class="fw-semibold mb-2"><i class="bi bi-camera"></i> <c:out value="${p.aiPrompt}"/></div>
      <a href="${ctx}/${p.studentInput}" target="_blank"><img src="${ctx}/${p.studentInput}" class="img-thumbnail mb-2" style="max-height:260px" alt="Ảnh bài làm"></a>
      <div class="alert ${p.stepCorrect ? 'alert-success' : 'alert-warning'} mb-0 py-2"><i class="bi bi-robot"></i> ${p.aiResponse}</div>
    </div></div>
  </c:forEach>

  <%-- Nộp ảnh bài làm --%>
  <c:if test="${not finished}">
    <div class="card border-danger mb-3"><div class="card-body">
      <h5 class="mb-2"><i class="bi bi-camera"></i> ${empty photos ? 'Nộp bài làm' : 'Nộp lại ảnh bài làm'}</h5>
      <p class="mb-2">Làm bài ra giấy, <b>chụp ảnh rõ nét toàn bộ lời giải</b> rồi nộp. AI sẽ chấm và nhận xét bài làm của bạn.</p>
      <p class="small text-muted">Bạn còn <b>${photoLeft}</b> lần nộp ảnh cho câu này.</p>
      <form method="post" action="${ctx}/student/practice-essay" enctype="multipart/form-data" id="photoForm">
        <input type="hidden" name="sessionId" value="${practice.sessionId}">
        <input type="hidden" name="questionId" value="${question.questionId}">
        <input type="file" class="form-control mb-2" name="image" accept="image/png,image/jpeg,image/webp" capture="environment" id="imgIn" required>
        <img id="preview" class="img-thumbnail mb-2 d-none" style="max-height:260px" alt="Xem trước ảnh">
        <div><button class="btn btn-success" id="photoBtn"><i class="bi bi-upload"></i> Nộp ảnh bài làm</button></div>
      </form>
    </div></div>
  </c:if>

  <c:if test="${finished}">
    <div class="text-center mt-3">
      <c:choose>
        <c:when test="${hasNext}"><a class="btn btn-primary" href="${base}&index=${currentIndex + 1}">Câu tiếp theo &raquo;</a></c:when>
        <c:when test="${allDone}"><a class="btn btn-success" href="${ctx}/student/practice-result?sessionId=${practice.sessionId}">Xem kết quả</a></c:when>
        <c:otherwise><span class="text-muted">Còn câu chưa làm, chọn câu màu đỏ ở cây bên phải.</span></c:otherwise>
      </c:choose></div>
  </c:if>
</div>

<!-- ================= Cây bên phải ================= -->
<div class="col-lg-4">
  <aside class="card progress-tree">
    <div class="card-header fw-semibold"><i class="bi bi-list-check"></i> Danh sách câu</div>
    <div class="card-body">
      <ul class="tree">
        <c:forEach var="n" items="${tree}">
          <li><a href="${base}&index=${n.index}" class="${n.done ? 't-done' : 't-todo'} ${n.index == currentIndex ? 't-current' : ''}">
            Câu ${n.index}<c:if test="${n.done}"> <i class="bi ${n.correct ? 'bi-check-circle text-success' : 'bi-x-circle text-secondary'}"></i></c:if></a></li>
        </c:forEach>
      </ul>
      <div class="small text-muted border-top pt-2 mt-2"><span class="t-todo">Đỏ đậm</span>: chưa làm · <span class="t-done">Chữ thường</span>: đã làm</div>
    </div>
  </aside>
</div>
</div>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
<script>
document.getElementById('imgIn')?.addEventListener('change', function (e) {
  var f = e.target.files[0]; if (!f) return; var p = document.getElementById('preview');
  p.src = URL.createObjectURL(f); p.classList.remove('d-none');
});
document.getElementById('photoForm')?.addEventListener('submit', function () {
  var b = document.getElementById('photoBtn');
  b.disabled = true; b.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>AI đang chấm bài...';
});
</script>
