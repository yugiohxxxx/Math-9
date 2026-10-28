<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Bài giảng"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<h3 class="mb-3"><i class="bi bi-journal-richtext"></i> Bài giảng Toán 9</h3>
<div class="accordion" id="acc">
<c:forEach var="ch" items="${chapters}" varStatus="st">
  <div class="accordion-item">
    <h2 class="accordion-header"><button class="accordion-button ${st.first?'':'collapsed'}" data-bs-toggle="collapse" data-bs-target="#c${ch.chapterId}"><c:out value="${ch.title}"/></button></h2>
    <div id="c${ch.chapterId}" class="accordion-collapse collapse ${st.first?'show':''}"><div class="accordion-body">
      <c:forEach var="t" items="${ch.topics}">
        <h6 class="text-primary mt-2"><c:out value="${t.title}"/></h6>
        <div class="list-group mb-2">
          <c:forEach var="l" items="${t.lessons}">
            <a class="list-group-item list-group-item-action d-flex justify-content-between align-items-center" href="${ctx}/student/lesson?id=${l.lessonId}">
              <span><i class="bi bi-${l.contentType=='text'?'file-text':'play-circle'}"></i> <c:out value="${l.title}"/></span>
              <c:choose>
                <c:when test="${l.status=='completed'}"><span class="badge text-bg-success">Đã xong</span></c:when>
                <c:when test="${l.status=='in_progress'}"><span class="badge text-bg-warning">Đang học</span></c:when>
                <c:otherwise><span class="badge text-bg-secondary">Chưa học</span></c:otherwise></c:choose>
            </a></c:forEach>
          <c:if test="${empty t.lessons}"><div class="list-group-item text-muted">Chưa có bài giảng</div></c:if>
        </div></c:forEach>
    </div></div></div>
</c:forEach></div>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
