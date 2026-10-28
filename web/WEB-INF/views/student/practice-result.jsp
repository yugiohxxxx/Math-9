<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Kết quả luyện tập"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<div class="card mb-4 text-center"><div class="card-body">
  <h4>Kết quả: <c:out value="${practice.topicTitle}"/></h4>
  <div class="display-4 text-primary"><fmt:formatNumber value="${practice.score}" maxFractionDigits="2"/> / 10</div>
  <p class="text-muted mb-0">Đúng ${correctCount} / ${fn:length(answers)} câu</p>
</div></div>
<c:forEach var="a" items="${answers}" varStatus="st">
  <div class="card mb-3 border-${a.correct?'success':'danger'}">
    <div class="card-header bg-${a.correct?'success':'danger'}-subtle">
      Câu ${st.count} – <strong>${a.correct?'Đúng':'Sai'}</strong></div>
    <div class="card-body">
      <div class="mb-2">${a.questionContent}</div>
      <c:if test="${practice.practiceType=='mcq'}">
        <p class="mb-1">Bạn chọn: <strong><c:out value="${a.selectedLabel}"/>. <c:out value="${a.selectedContent}"/></strong></p>
        <c:if test="${not a.correct}"><p class="mb-1 text-success">Đáp án đúng: <strong><c:out value="${a.correctLabel}"/>. <c:out value="${a.correctContent}"/></strong></p></c:if>
      </c:if>
      <c:if test="${practice.practiceType=='essay'}">
        <c:if test="${not empty a.essayText}"><p><strong>Bài làm:</strong> <c:out value="${a.essayText}"/></p></c:if>
        <c:if test="${not empty a.essayImageUrl}"><img src="${ctx}/${a.essayImageUrl}" class="img-thumbnail mb-2" style="max-height:200px"></c:if>
      </c:if>
      <c:if test="${not a.correct && not empty a.aiComment}">
        <div class="alert alert-warning mb-2"><i class="bi bi-robot"></i> <strong>Nhận xét của AI:</strong><br>${a.aiComment}</div></c:if>
      <c:if test="${not empty a.solution}">
        <details><summary class="text-primary">Xem lời giải mẫu</summary><div class="mt-2">${a.solution}</div></details></c:if>
    </div></div>
</c:forEach>
<a class="btn btn-primary" href="${ctx}/student/practice">Luyện tập tiếp</a>
<a class="btn btn-outline-secondary" href="${ctx}/student/home">Trang chủ</a>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
