<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Ngân hàng câu hỏi"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<div class="d-flex justify-content-between mb-3 flex-wrap gap-2">
  <h3><i class="bi bi-patch-question"></i> Ngân hàng câu hỏi</h3>
  <div>
    <button class="btn btn-warning" data-bs-toggle="modal" data-bs-target="#aiModal"><i class="bi bi-magic"></i> AI sinh câu hỏi</button>
    <a class="btn btn-primary" href="${ctx}/manage/question-form"><i class="bi bi-plus-lg"></i> Thêm câu hỏi</a>
  </div>
</div>
<form class="row g-2 mb-3" method="get" action="${ctx}/manage/questions">
  <div class="col-md-3"><select class="form-select" name="topicId" id="fTopic"><option value="">-- Tất cả chủ đề --</option>
    <c:forEach var="t" items="${topics}"><option value="${t.topicId}" ${param.topicId==t.topicId?'selected':''}><c:out value="${t.title}"/></option></c:forEach></select></div>
  <div class="col-md-3"><select class="form-select" name="lessonId" data-lesson-of="#fTopic"><option value="">-- Tất cả bài giảng --</option>
    <option value="none" ${param.lessonId=='none'?'selected':''}>(Chưa gắn bài giảng)</option>
    <c:forEach var="l" items="${lessons}"><option value="${l.lessonId}" data-topic="${l.topicId}" ${param.lessonId eq fn:escapeXml(l.lessonId) ? 'selected' : ''}><c:out value="${l.title}"/></option></c:forEach></select></div>
  <div class="col-md-2"><select class="form-select" name="type"><option value="">-- Loại --</option>
    <option value="mcq" ${param.type=='mcq'?'selected':''}>Trắc nghiệm</option>
    <option value="essay" ${param.type=='essay'?'selected':''}>Tự luận</option></select></div>
  <div class="col-md-2"><select class="form-select" name="level"><option value="">-- Độ khó --</option>
    <option value="1" ${param.level=='1'?'selected':''}>Nhận biết</option><option value="2" ${param.level=='2'?'selected':''}>Thông hiểu</option>
    <option value="3" ${param.level=='3'?'selected':''}>Vận dụng</option><option value="4" ${param.level=='4'?'selected':''}>Vận dụng cao</option></select></div>
  <div class="col-md-2"><button class="btn btn-outline-primary w-100">Lọc</button></div>
</form>
<div class="card"><div class="table-responsive"><table class="table table-hover datatable mb-0">
  <thead><tr><th>#</th><th>Nội dung</th><th>Chủ đề</th><th>Bài giảng</th><th>Loại</th><th>Độ khó</th><th>Nguồn</th><th></th></tr></thead><tbody>
  <c:forEach var="q" items="${questions}">
    <tr><td>${q.questionId}</td>
      <td style="max-width:420px"><c:out value="${fn:substring(q.content,0,120)}"/>${fn:length(q.content)>120?'...':''}</td>
      <td><c:out value="${q.topicTitle}"/></td>
      <td><c:choose><c:when test="${empty q.lessonTitle}"><span class="text-muted">—</span></c:when><c:otherwise><c:out value="${q.lessonTitle}"/></c:otherwise></c:choose></td>
      <td><span class="badge text-bg-${q.questionType=='mcq'?'primary':'success'}">${q.questionType=='mcq'?'Trắc nghiệm':'Tự luận'}</span></td>
      <td>${q.level}</td><td>${q.source}</td>
      <td class="text-end text-nowrap">
        <a class="btn btn-sm btn-outline-primary" href="${ctx}/manage/question-form?id=${q.questionId}"><i class="bi bi-pencil"></i></a>
        <form class="d-inline" method="post" action="${ctx}/manage/questions" onsubmit="return confirm('Xoá câu hỏi?')">
          <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${q.questionId}">
          <button class="btn btn-sm btn-outline-danger"><i class="bi bi-trash"></i></button></form></td></tr>
  </c:forEach></tbody></table></div></div>

<div class="modal fade" id="aiModal"><div class="modal-dialog"><form class="modal-content" method="post" action="${ctx}/manage/questions">
  <input type="hidden" name="action" value="aiGenerate">
  <div class="modal-header"><h5 class="modal-title">AI sinh câu hỏi</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
  <div class="modal-body">
    <div class="mb-3"><label class="form-label">Chủ đề</label><select class="form-select" name="topicId" id="aiTopic" required>
      <c:forEach var="t" items="${topics}"><option value="${t.topicId}"><c:out value="${t.title}"/></option></c:forEach></select></div>
    <div class="mb-3"><label class="form-label">Bài giảng</label><select class="form-select" name="lessonId" data-lesson-of="#aiTopic">
      <option value="">-- Cả chủ đề (không gắn bài) --</option>
      <c:forEach var="l" items="${lessons}"><option value="${l.lessonId}" data-topic="${l.topicId}"><c:out value="${l.title}"/></option></c:forEach></select>
      <div class="form-text">Chọn bài giảng thì AI sẽ dựa vào nội dung bài để ra câu hỏi.</div></div>
    <div class="row">
      <div class="col-4 mb-3"><label class="form-label">Loại</label><select class="form-select" name="type"><option value="mcq">Trắc nghiệm</option><option value="essay">Tự luận</option></select></div>
      <div class="col-4 mb-3"><label class="form-label">Độ khó</label><select class="form-select" name="level"><option value="1">Nhận biết</option><option value="2">Thông hiểu</option><option value="3">Vận dụng</option><option value="4">Vận dụng cao</option></select></div>
      <div class="col-4 mb-3"><label class="form-label">Số câu</label><input type="number" class="form-control" name="count" value="5" min="1" max="20"></div>
    </div>
  </div>
  <div class="modal-footer"><button class="btn btn-warning">Sinh câu hỏi</button></div>
</form></div></div>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
