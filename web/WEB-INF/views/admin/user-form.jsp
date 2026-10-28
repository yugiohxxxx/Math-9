<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Thông tin người dùng"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<h3 class="mb-3">${empty u ? 'Thêm người dùng' : 'Sửa người dùng'}</h3>
<div class="card" style="max-width:640px"><div class="card-body">
<form method="post" action="${ctx}/admin/user-form">
  <input type="hidden" name="id" value="${u.userId}">
  <div class="mb-3"><label class="form-label">Tên đăng nhập</label>
    <input class="form-control" name="username" value="<c:out value='${u.username}'/>" ${empty u ? '' : 'readonly'} required></div>
  <div class="mb-3"><label class="form-label">Mật khẩu ${empty u ? '' : '(để trống nếu không đổi)'}</label>
    <input type="password" class="form-control" name="password" ${empty u ? 'required' : ''}></div>
  <div class="mb-3"><label class="form-label">Họ và tên</label>
    <input class="form-control" name="fullName" value="<c:out value='${u.fullName}'/>" required></div>
  <div class="row">
    <div class="col-md-6 mb-3"><label class="form-label">Email</label>
      <input type="email" class="form-control" name="email" value="<c:out value='${u.email}'/>"></div>
    <div class="col-md-6 mb-3"><label class="form-label">Điện thoại</label>
      <input class="form-control" name="phone" value="<c:out value='${u.phone}'/>"></div>
  </div>
  <div class="mb-3"><label class="form-label">Vai trò</label>
    <select class="form-select" name="roleId" required>
      <c:forEach var="r" items="${roles}">
        <option value="${r.roleId}" ${u.roleId==r.roleId?'selected':''}>${r.roleName}</option>
      </c:forEach></select></div>
  <button class="btn btn-primary">Lưu</button>
  <a class="btn btn-secondary" href="${ctx}/admin/users">Huỷ</a>
</form></div></div>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
