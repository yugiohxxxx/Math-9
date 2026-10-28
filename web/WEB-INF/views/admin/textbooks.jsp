<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Sách giáo khoa"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<h3 class="mb-3"><i class="bi bi-book"></i> Sách giáo khoa &amp; tách bài tập bằng AI</h3>
<div class="card mb-4"><div class="card-body">
<form method="post" action="${ctx}/admin/textbooks" enctype="multipart/form-data" class="row g-3">
  <input type="hidden" name="action" value="add">
  <div class="col-md-4"><label class="form-label">Tên sách</label><input class="form-control" name="title" required placeholder="Toán 9 - Tập 1"></div>
  <div class="col-md-4"><label class="form-label">Link nguồn</label><input class="form-control" name="sourceUrl" placeholder="https://loigiaihay.com/..."></div>
  <div class="col-md-4"><label class="form-label">File PDF</label><input type="file" class="form-control" name="pdf" accept="application/pdf"></div>
  <div class="col-12"><button class="btn btn-primary"><i class="bi bi-upload"></i> Thêm sách</button></div>
</form></div></div>
<div class="card"><div class="table-responsive"><table class="table datatable mb-0">
  <thead><tr><th>#</th><th>Tên sách</th><th>Link</th><th>PDF</th><th>Ngày tải</th><th></th></tr></thead><tbody>
  <c:forEach var="b" items="${textbooks}">
  <tr><td>${b.textbookId}</td><td><c:out value="${b.title}"/></td>
    <td><c:if test="${not empty b.sourceUrl}"><a href="${b.sourceUrl}" target="_blank">Mở link</a></c:if></td>
    <td><c:out value="${b.pdfPath}"/></td>
    <td><fmt:formatDate value="${b.uploadedAt}" pattern="dd/MM/yyyy HH:mm"/></td>
    <td class="text-end text-nowrap">
      <form class="d-inline" method="post" action="${ctx}/admin/textbooks">
        <input type="hidden" name="action" value="extract"><input type="hidden" name="id" value="${b.textbookId}">
        <select name="topicId" class="form-select form-select-sm d-inline w-auto" required>
          <c:forEach var="t" items="${topics}"><option value="${t.topicId}"><c:out value="${t.title}"/></option></c:forEach></select>
        <button class="btn btn-sm btn-warning"><i class="bi bi-magic"></i> AI tách bài tập</button>
      </form>
      <form class="d-inline" method="post" action="${ctx}/admin/textbooks" onsubmit="return confirm('Xoá?')">
        <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${b.textbookId}">
        <button class="btn btn-sm btn-outline-danger"><i class="bi bi-trash"></i></button></form>
    </td></tr></c:forEach></tbody></table></div></div>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
