<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="${lesson.title}"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<nav aria-label="breadcrumb"><ol class="breadcrumb">
  <li class="breadcrumb-item"><a href="${ctx}/student/lessons">Bài giảng</a></li>
  <li class="breadcrumb-item"><c:out value="${lesson.topicTitle}"/></li>
  <li class="breadcrumb-item active"><c:out value="${lesson.title}"/></li></ol></nav>
<div class="card"><div class="card-body">
  <h3><c:out value="${lesson.title}"/></h3><hr>
  <c:if test="${lesson.contentType=='video' && not empty lesson.videoUrl}">
    <div class="ratio ratio-16x9 mb-3"><iframe src="${lesson.embedUrl}" allowfullscreen></iframe></div>
    <a href="${lesson.videoUrl}" target="_blank">Mở video trong tab mới</a>
  </c:if>
  <c:if test="${lesson.contentType=='iframe' && not empty lesson.iframeUrl}">
    <div class="ratio ratio-16x9 mb-3"><iframe src="${lesson.iframeUrl}" allowfullscreen></iframe></div>
  </c:if>
  <%-- contentHtml do giáo viên soạn bằng TinyMCE nên in nguyên HTML --%>
  <div class="lesson-content">${lesson.contentHtml}</div>
</div>
<div class="card-footer d-flex justify-content-between">
  <a class="btn btn-outline-secondary" href="${ctx}/student/lessons">&laquo; Danh sách</a>
  <div>
    <form class="d-inline" method="post" action="${ctx}/student/lesson">
      <input type="hidden" name="id" value="${lesson.lessonId}"><input type="hidden" name="status" value="completed">
      <button class="btn btn-success"><i class="bi bi-check2-circle"></i> Đánh dấu đã học xong</button></form>
    <a class="btn btn-primary" href="${ctx}/student/practice?topicId=${lesson.topicId}&lessonId=${lesson.lessonId}">Luyện tập bài này</a>
  </div>
</div></div>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
