<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Quản lý bài giảng"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<div class="d-flex justify-content-between mb-3">
  <h3><i class="bi bi-journal-richtext"></i> Bài giảng</h3>
  <a class="btn btn-primary" href="${ctx}/manage/lesson-form"><i class="bi bi-plus-lg"></i> Thêm bài giảng</a>
</div>
<div class="card"><div class="table-responsive"><table class="table table-hover datatable mb-0">
  <thead><tr><th>#</th><th>Tiêu đề</th><th>Chủ đề</th><th>Loại</th><th>Hiển thị</th><th></th></tr></thead><tbody>
  <c:forEach var="l" items="${lessons}">
    <tr><td>${l.lessonId}</td><td><c:out value="${l.title}"/></td><td><c:out value="${l.topicTitle}"/></td>
      <td><span class="badge text-bg-info">${l.contentType}</span></td>
      <td>${l.published ? 'Có' : 'Ẩn'}</td>
      <td class="text-end text-nowrap">
        <a class="btn btn-sm btn-outline-secondary" href="${ctx}/student/lesson?id=${l.lessonId}" target="_blank"><i class="bi bi-eye"></i></a>
        <a class="btn btn-sm btn-outline-primary" href="${ctx}/manage/lesson-form?id=${l.lessonId}"><i class="bi bi-pencil"></i></a>
        <form class="d-inline" method="post" action="${ctx}/manage/lessons" onsubmit="return confirm('Xoá bài giảng?')">
          <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${l.lessonId}">
          <button class="btn btn-sm btn-outline-danger"><i class="bi bi-trash"></i></button></form>
      </td></tr></c:forEach></tbody></table></div></div>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
