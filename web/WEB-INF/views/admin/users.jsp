<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Quản lý người dùng"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<div class="d-flex justify-content-between mb-3">
  <h3><i class="bi bi-people"></i> Quản lý người dùng</h3>
  <a class="btn btn-primary" href="${ctx}/admin/user-form"><i class="bi bi-plus-lg"></i> Thêm người dùng</a>
</div>
<div class="card"><div class="table-responsive">
<table class="table table-hover datatable mb-0">
  <thead><tr><th>#</th><th>Tên đăng nhập</th><th>Họ tên</th><th>Email</th><th>Vai trò</th><th>Trạng thái</th><th></th></tr></thead>
  <tbody>
  <c:forEach var="u" items="${users}">
    <tr>
      <td>${u.userId}</td><td><c:out value="${u.username}"/></td><td><c:out value="${u.fullName}"/></td>
      <td><c:out value="${u.email}"/></td>
      <td><span class="badge text-bg-${u.roleName=='admin'?'danger':(u.roleName=='teacher'?'success':'secondary')}">${u.roleName}</span></td>
      <td><c:choose><c:when test="${u.active}"><span class="badge text-bg-success">Hoạt động</span></c:when>
          <c:otherwise><span class="badge text-bg-dark">Đã khoá</span></c:otherwise></c:choose></td>
      <td class="text-end text-nowrap">
        <a class="btn btn-sm btn-outline-primary" href="${ctx}/admin/user-form?id=${u.userId}"><i class="bi bi-pencil"></i></a>
        <form class="d-inline" method="post" action="${ctx}/admin/users">
          <input type="hidden" name="action" value="toggle"><input type="hidden" name="id" value="${u.userId}">
          <button class="btn btn-sm btn-outline-warning" title="Khoá/Mở khoá"><i class="bi bi-lock"></i></button>
        </form>
        <form class="d-inline" method="post" action="${ctx}/admin/users" onsubmit="return confirm('Xoá người dùng này?')">
          <input type="hidden" name="action" value="delete"><input type="hidden" name="id" value="${u.userId}">
          <button class="btn btn-sm btn-outline-danger"><i class="bi bi-trash"></i></button>
        </form>
      </td>
    </tr>
  </c:forEach></tbody>
</table></div></div>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
