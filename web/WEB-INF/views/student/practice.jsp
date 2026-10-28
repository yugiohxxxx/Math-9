<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Luyện tập"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<h3 class="mb-3"><i class="bi bi-pencil-square"></i> Bắt đầu luyện tập</h3>
<div class="card" style="max-width:720px"><div class="card-body">
<form method="post" action="${ctx}/student/practice">
  <div class="mb-3"><label class="form-label">Chủ đề</label>
    <select class="form-select" name="topicId" id="topicSel" required>
      <c:forEach var="t" items="${topics}"><option value="${t.topicId}" ${param.topicId==t.topicId?'selected':''}><c:out value="${t.title}"/></option></c:forEach></select></div>
  <div class="mb-3"><label class="form-label">Bài học (tuỳ chọn)</label>
    <select class="form-select" name="lessonId"><option value="">-- Tất cả bài trong chủ đề --</option>
      <c:forEach var="l" items="${lessons}"><option value="${l.lessonId}" data-topic="${l.topicId}" ${param.lessonId==l.lessonId?'selected':''}><c:out value="${l.title}"/></option></c:forEach></select></div>
  <div class="mb-3"><label class="form-label d-block">Hình thức</label>
    <div class="form-check form-check-inline"><input class="form-check-input" type="radio" name="practiceType" value="mcq" id="t1" checked><label class="form-check-label" for="t1">Trắc nghiệm</label></div>
    <div class="form-check form-check-inline"><input class="form-check-input" type="radio" name="practiceType" value="essay" id="t2"><label class="form-check-label" for="t2">Tự luận (chụp ảnh bài làm, AI chấm)</label></div></div>
  <div class="row">
    <div class="col-md-6 mb-3"><label class="form-label">Số câu</label><input type="number" class="form-control" name="numQuestions" value="5" min="1" max="30"></div>
    <div class="col-md-6 mb-3"><label class="form-label">Mức độ</label><select class="form-select" name="level">
      <option value="">Tất cả</option><option value="1">Nhận biết</option><option value="2">Thông hiểu</option><option value="3">Vận dụng</option><option value="4">Vận dụng cao</option></select></div>
  </div>
  <button class="btn btn-success"><i class="bi bi-play-fill"></i> Bắt đầu</button>
</form></div></div>

<c:if test="${not empty history}">
<div class="card mt-4"><div class="card-header">Lịch sử luyện tập gần đây</div>
<div class="table-responsive"><table class="table mb-0"><thead><tr><th>Thời gian</th><th>Chủ đề</th><th>Loại</th><th>Điểm</th><th></th></tr></thead><tbody>
<c:forEach var="h" items="${history}"><tr>
  <td><fmt:formatDate value="${h.startedAt}" pattern="dd/MM/yyyy HH:mm"/></td><td><c:out value="${h.topicTitle}"/></td>
  <td>${h.practiceType=='mcq'?'Trắc nghiệm':'Tự luận'}</td><td>${h.score}</td>
  <td><a href="${ctx}/student/practice-result?sessionId=${h.sessionId}">Xem lại</a></td></tr></c:forEach></tbody></table></div></div>
</c:if>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
<script>
// lọc danh sách bài học theo chủ đề đang chọn
var topicSel=document.getElementById('topicSel'), lessonSel=document.querySelector('select[name=lessonId]');
function filterLessons(){Array.from(lessonSel.options).forEach(function(o){
  if(!o.dataset.topic) return; o.hidden = o.dataset.topic!==topicSel.value; if(o.hidden&&o.selected) lessonSel.value='';});}
topicSel.addEventListener('change',filterLessons); filterLessons();
</script>
