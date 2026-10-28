<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Học sinh của tôi"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<div class="d-flex justify-content-between mb-3">
  <h3><i class="bi bi-mortarboard"></i> Học sinh của tôi</h3>
  <button class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#stuModal"><i class="bi bi-person-plus"></i> Thêm học sinh</button>
</div>
<div class="card"><div class="table-responsive"><table class="table table-hover datatable mb-0">
  <thead><tr><th>#</th><th>Tên đăng nhập</th><th>Họ tên</th><th>Email</th><th>Tiến độ</th><th></th></tr></thead><tbody>
  <c:forEach var="s" items="${students}">
    <tr><td>${s.userId}</td><td><c:out value="${s.username}"/></td><td><c:out value="${s.fullName}"/></td><td><c:out value="${s.email}"/></td>
      <td><div class="progress" style="min-width:120px"><div class="progress-bar" style="width:${s.progressPercent}%">${s.progressPercent}%</div></div></td>
      <td class="text-end"><form method="post" action="${ctx}/teacher/students" onsubmit="return confirm('Bỏ học sinh khỏi danh sách?')">
        <input type="hidden" name="action" value="remove"><input type="hidden" name="id" value="${s.userId}">
        <button class="btn btn-sm btn-outline-danger"><i class="bi bi-person-dash"></i></button></form></td></tr></c:forEach></tbody></table></div></div>

<div class="modal fade" id="stuModal"><div class="modal-dialog"><form class="modal-content" method="post" action="${ctx}/teacher/students">
  <input type="hidden" name="action" value="add">
  <div class="modal-header"><h5 class="modal-title">Thêm học sinh</h5><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
  <div class="modal-body">
    <div class="mb-3"><label class="form-label">Tên đăng nhập</label><input class="form-control" name="username" required></div>
    <div class="mb-3"><label class="form-label">Mật khẩu</label><input type="password" class="form-control" name="password" required></div>
    <div class="mb-3"><label class="form-label">Họ tên</label><input class="form-control" name="fullName" required></div>
    <div class="mb-3"><label class="form-label">Email</label><input type="email" class="form-control" name="email"></div>
  </div>
  <div class="modal-footer"><button class="btn btn-primary">Thêm</button></div>
</form></div></div>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
