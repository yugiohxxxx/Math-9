<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Chương / Chủ đề"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<div class="d-flex justify-content-between mb-3">
  <h3><i class="bi bi-diagram-3"></i> Chương &amp; Chủ đề môn Toán 9</h3>
  <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#chapterModal"><i class="bi bi-plus-lg"></i> Thêm chương</button>
</div>
<div class="accordion" id="acc">
<c:forEach var="ch" items="${chapters}">
  <div class="accordion-item">
    <h2 class="accordion-header"><button class="accordion-button collapsed" data-bs-toggle="collapse" data-bs-target="#c${ch.chapterId}">
      <c:out value="${ch.title}"/></button></h2>
    <div id="c${ch.chapterId}" class="accordion-collapse collapse" data-bs-parent="#acc"><div class="accordion-body">
      <ul class="list-group mb-3">
        <c:forEach var="t" items="${ch.topics}">
          <li class="list-group-item d-flex justify-content-between align-items-center">
            <span><i class="bi bi-dot"></i> <c:out value="${t.title}"/></span>
            <form method="post" action="${ctx}/manage/chapters" onsubmit="return confirm('Xoá chủ đề?')">
              <input type="hidden" name="action" value="deleteTopic"><input type="hidden" name="id" value="${t.topicId}">
              <button class="btn btn-sm btn-outline-danger"><i class="bi bi-trash"></i></button></form>
          </li>
        </c:forEach>
        <c:if test="${empty ch.topics}"><li class="list-group-item text-muted">Chưa có chủ đề</li></c:if>
      </ul>
      <form method="post" action="${ctx}/manage/chapters" class="row g-2">
        <input type="hidden" name="action" value="addTopic"><input type="hidden" name="chapterId" value="${ch.chapterId}">
        <div class="col-md-5"><input class="form-control" name="title" placeholder="Tên chủ đề mới" required></div>
        <div class="col-md-5"><input class="form-control" name="description" placeholder="Mô tả (tuỳ chọn)"></div>
        <div class="col-md-2"><button class="btn btn-success w-100">Thêm chủ đề</button></div>
      </form>
      <form method="post" action="${ctx}/manage/chapters" class="mt-3" onsubmit="return confirm('Xoá chương và chủ đề thuộc chương?')">
        <input type="hidden" name="action" value="deleteChapter"><input type="hidden" name="id" value="${ch.chapterId}">
        <button class="btn btn-sm btn-outline-danger">Xoá chương này</button></form>
    </div></div>
  </div>
</c:forEach></div>

<div class="modal fade" id="chapterModal"><div class="modal-dialog"><form class="modal-content" method="post" action="${ctx}/manage/chapters">
  <input type="hidden" name="action" value="addChapter">
  <div class="modal-header"><h5 class="modal-title">Thêm chương</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
  <div class="modal-body">
    <div class="mb-3"><label class="form-label">Tên chương</label><input class="form-control" name="title" required></div>
    <div class="mb-3"><label class="form-label">Mô tả</label><input class="form-control" name="description"></div>
    <div class="mb-3"><label class="form-label">Thứ tự</label><input type="number" class="form-control" name="sortOrder" value="0"></div>
  </div>
  <div class="modal-footer"><button class="btn btn-primary">Lưu</button></div>
</form></div></div>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
