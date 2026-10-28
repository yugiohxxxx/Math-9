<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Soạn bài giảng"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<h3 class="mb-3">${empty lesson ? 'Thêm bài giảng' : 'Sửa bài giảng'}</h3>
<form method="post" action="${ctx}/manage/lesson-form" class="card card-body">
  <input type="hidden" name="id" value="${lesson.lessonId}">
  <div class="row">
    <div class="col-md-8 mb-3"><label class="form-label">Tiêu đề</label>
      <input class="form-control" name="title" value="<c:out value='${lesson.title}'/>" required></div>
    <div class="col-md-4 mb-3"><label class="form-label">Chủ đề</label>
      <select class="form-select" name="topicId" required>
        <c:forEach var="t" items="${topics}"><option value="${t.topicId}" ${lesson.topicId==t.topicId?'selected':''}><c:out value="${t.title}"/></option></c:forEach>
      </select></div>
  </div>
  <div class="row">
    <div class="col-md-4 mb-3"><label class="form-label">Loại nội dung</label>
      <select class="form-select" name="contentType" id="ctype">
        <option value="text" ${lesson.contentType=='text'?'selected':''}>Văn bản</option>
        <option value="video" ${lesson.contentType=='video'?'selected':''}>Video (link YouTube/website)</option>
        <option value="iframe" ${lesson.contentType=='iframe'?'selected':''}>Iframe nhúng</option></select></div>
    <div class="col-md-4 mb-3"><label class="form-label">Thứ tự</label>
      <input type="number" class="form-control" name="sortOrder" value="${empty lesson ? 0 : lesson.sortOrder}"></div>
    <div class="col-md-4 mb-3 d-flex align-items-end"><div class="form-check">
      <input class="form-check-input" type="checkbox" name="published" id="pub" ${empty lesson || lesson.published ? 'checked' : ''}>
      <label class="form-check-label" for="pub">Hiển thị cho học sinh</label></div></div>
  </div>
  <div class="mb-3" id="videoBox"><label class="form-label">Link video (YouTube hoặc website)</label>
    <input class="form-control" name="videoUrl" value="<c:out value='${lesson.videoUrl}'/>" placeholder="https://www.youtube.com/watch?v=..."></div>
  <div class="mb-3" id="iframeBox"><label class="form-label">Link iframe</label>
    <input class="form-control" name="iframeUrl" value="<c:out value='${lesson.iframeUrl}'/>" placeholder="https://www.youtube.com/embed/..."></div>
  <div class="mb-3"><label class="form-label">Nội dung bài giảng (công thức: <code>&#36;x^2+1=0&#36;</code>)</label>
    <textarea class="editor" name="contentHtml"><c:out value="${lesson.contentHtml}"/></textarea></div>
  <div><button class="btn btn-primary">Lưu</button> <a class="btn btn-secondary" href="${ctx}/manage/lessons">Huỷ</a></div>
</form>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
<script>
function toggleBoxes(){var v=document.getElementById('ctype').value;
  document.getElementById('videoBox').style.display = v==='video'?'block':'none';
  document.getElementById('iframeBox').style.display = v==='iframe'?'block':'none';}
document.getElementById('ctype').addEventListener('change',toggleBoxes); toggleBoxes();
</script>
