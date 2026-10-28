<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Lỗi"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<div class="text-center py-5">
  <div class="error-code mb-3">${empty code ? 'Lỗi' : code}</div>
  <p class="text-muted mb-4" style="max-width:46ch;margin:0 auto"><c:out value="${empty message ? 'Đã xảy ra lỗi hoặc bạn không có quyền truy cập trang này.' : message}"/></p>
  <a class="btn btn-primary" href="${ctx}/home">Về trang chủ</a>
</div>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
