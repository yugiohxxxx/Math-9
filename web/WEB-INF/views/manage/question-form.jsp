<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Câu hỏi"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<h3 class="mb-3">${empty q ? 'Thêm câu hỏi' : 'Sửa câu hỏi'}</h3>
<form method="post" action="${ctx}/manage/question-form" enctype="multipart/form-data" class="card card-body">
  <input type="hidden" name="id" value="${q.questionId}">
  <div class="row">
    <div class="col-md-4 mb-3"><label class="form-label">Chủ đề</label><select class="form-select" name="topicId" id="qTopic" required>
      <c:forEach var="t" items="${topics}"><option value="${t.topicId}" ${q.topicId==t.topicId?'selected':''}><c:out value="${t.title}"/></option></c:forEach></select></div>
    <div class="col-md-4 mb-3"><label class="form-label">Bài giảng</label><select class="form-select" name="lessonId" data-lesson-of="#qTopic">
      <option value="">-- Cả chủ đề (không gắn bài) --</option>
      <c:forEach var="l" items="${lessons}"><option value="${l.lessonId}" data-topic="${l.topicId}" ${q.lessonId==l.lessonId?'selected':''}><c:out value="${l.title}"/>${l.published ? '' : ' (đang ẩn)'}</option></c:forEach></select></div>
    <div class="col-md-2 mb-3"><label class="form-label">Loại</label><select class="form-select" name="questionType" id="qtype">
      <option value="mcq" ${q.questionType=='mcq'?'selected':''}>Trắc nghiệm</option>
      <option value="essay" ${q.questionType=='essay'?'selected':''}>Tự luận</option></select></div>
    <div class="col-md-2 mb-3"><label class="form-label">Độ khó</label><select class="form-select" name="level">
      <option value="1" ${q.level==1?'selected':''}>1 - Nhận biết</option><option value="2" ${q.level==2?'selected':''}>2 - Thông hiểu</option>
      <option value="3" ${q.level==3?'selected':''}>3 - Vận dụng</option><option value="4" ${q.level==4?'selected':''}>4 - Vận dụng cao</option></select></div>
  </div>
  <div class="mb-3"><label class="form-label">Nội dung câu hỏi</label>
    <textarea class="editor" name="content"><c:out value="${q.content}"/></textarea></div>
  <div class="mb-3"><label class="form-label">Ảnh minh hoạ (tuỳ chọn)</label><input type="file" class="form-control" name="image" accept="image/*">
    <c:if test="${not empty q.imageUrl}"><img src="${ctx}/${q.imageUrl}" class="img-thumbnail mt-2" style="max-height:120px"></c:if></div>

  <div id="mcqBox" class="mb-3"><label class="form-label">Các đáp án (chọn đáp án đúng)</label>
    <c:forEach var="lb" items="A,B,C,D" varStatus="st">
      <c:set var="opt" value="${q.optByLabel[lb]}"/>
      <div class="input-group mb-2">
        <span class="input-group-text"><input type="radio" name="correct" value="${lb}" ${opt.correct?'checked':''} class="form-check-input mt-0"></span>
        <span class="input-group-text fw-bold">${lb}</span>
        <input class="form-control" name="opt${lb}" value="<c:out value='${opt.content}'/>">
      </div>
    </c:forEach></div>

  <div class="mb-3"><label class="form-label">Lời giải / đáp án mẫu</label>
    <textarea class="form-control" rows="4" name="solution"><c:out value="${q.solution}"/></textarea></div>
  <div><button class="btn btn-primary">Lưu</button> <a class="btn btn-secondary" href="${ctx}/manage/questions">Huỷ</a></div>
</form>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
<script>
function toggleQ(){document.getElementById('mcqBox').style.display=document.getElementById('qtype').value==='mcq'?'block':'none';}
document.getElementById('qtype').addEventListener('change',toggleQ); toggleQ();
</script>
