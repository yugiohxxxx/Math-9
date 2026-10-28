<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Trang chủ"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<c:set var="pct" value="${empty overallPercent ? 0 : overallPercent}"/>
<section class="hero-board mb-4">
  <h2>Xin chào, <c:out value="${sessionScope.user.fullName}"/></h2>
  <p class="mb-3">Bạn đã hoàn thành <strong class="text-white">${pct}%</strong> chương trình. Tiếp tục từ bài giảng hoặc luyện thêm vài câu nhé.</p>
  <div class="progress" role="progressbar" aria-label="Tiến độ tổng quan" aria-valuenow="${pct}" aria-valuemin="0" aria-valuemax="100">
    <div class="progress-bar" style="width:${pct}%"></div></div>
  <div class="formula" aria-hidden="true">$$ax^2 + bx + c = 0$$</div>
</section>

<div class="row g-3">
  <div class="col-md-4"><div class="card h-100"><div class="action-card tone-teal">
    <div class="stat-icon"><i class="bi bi-journal-richtext"></i></div>
    <h5>Bài giảng</h5><p>Xem bài giảng và video theo từng chủ đề.</p>
    <a class="btn btn-primary" href="${ctx}/student/lessons">Vào học</a></div></div></div>
  <div class="col-md-4"><div class="card h-100"><div class="action-card tone-green">
    <div class="stat-icon"><i class="bi bi-pencil-square"></i></div>
    <h5>Luyện tập</h5><p>Làm trắc nghiệm, hoặc tự luận rồi chụp ảnh bài làm để AI chấm.</p>
    <a class="btn btn-success" href="${ctx}/student/practice">Bắt đầu luyện tập</a></div></div></div>
  <div class="col-md-4"><div class="card h-100"><div class="action-card tone-chalk">
    <div class="stat-icon"><i class="bi bi-graph-up-arrow"></i></div>
    <h5>Tiến độ</h5><p>Xem bài đã xong, bài đang học và điểm theo thời gian.</p>
    <a class="btn btn-outline-secondary" href="${ctx}/student/progress">Xem tiến độ</a></div></div></div>
</div>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
