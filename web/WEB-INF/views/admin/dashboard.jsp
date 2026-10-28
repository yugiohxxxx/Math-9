<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Thống kê"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<h3 class="mb-4"><i class="bi bi-speedometer2"></i> Thống kê hệ thống</h3>
<div class="row g-3 mb-4">
  <div class="col-6 col-xl-3"><div class="stat tone-teal"><div class="stat-icon"><i class="bi bi-mortarboard"></i></div><div><div class="stat-value">${stats.totalStudents}</div><div class="stat-label">Học sinh</div></div></div></div>
  <div class="col-6 col-xl-3"><div class="stat tone-green"><div class="stat-icon"><i class="bi bi-journal-richtext"></i></div><div><div class="stat-value">${stats.totalLessons}</div><div class="stat-label">Bài giảng</div></div></div></div>
  <div class="col-6 col-xl-3"><div class="stat tone-chalk"><div class="stat-icon"><i class="bi bi-patch-question"></i></div><div><div class="stat-value">${stats.totalQuestions}</div><div class="stat-label">Câu hỏi</div></div></div></div>
  <div class="col-6 col-xl-3"><div class="stat tone-blue"><div class="stat-icon"><i class="bi bi-pencil-square"></i></div><div><div class="stat-value">${stats.totalSessions}</div><div class="stat-label">Lượt luyện tập</div></div></div></div>
</div>
<div class="row g-3">
  <div class="col-lg-7"><div class="card h-100"><div class="card-header">Điểm trung bình theo chủ đề</div>
    <div class="card-body"><canvas id="chartTopic"></canvas></div></div></div>
  <div class="col-lg-5"><div class="card h-100"><div class="card-header">Tiến độ bài giảng</div>
    <div class="card-body d-flex align-items-center justify-content-center"><div style="max-width:300px;width:100%"><canvas id="chartProgress"></canvas></div></div></div></div>
</div>
<div class="card mt-4">
  <div class="card-header">Thống kê từng học sinh</div>
  <div class="table-responsive"><table class="table table-striped datatable mb-0">
    <thead><tr><th>Học sinh</th><th>Bài giảng đã hoàn thành</th><th>Số lượt luyện tập</th><th>Điểm TB</th></tr></thead>
    <tbody>
    <c:forEach var="s" items="${studentStats}">
      <tr><td><c:out value="${s.fullName}"/></td><td>${s.lessonsDone} / ${s.lessonsTotal}</td>
          <td>${s.sessions}</td><td><fmt:formatNumber value="${s.avgScore}" maxFractionDigits="2"/></td></tr>
    </c:forEach></tbody></table></div>
</div>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
<script>
// topicStats: danh sách {title, avgScore}; progressStats: {done, doing, notStarted}
new Chart(document.getElementById('chartTopic'), {type:'bar', data:{
  labels:[<c:forEach var="t" items="${topicStats}" varStatus="st">"<c:out value='${t.title}'/>"${st.last?'':','}</c:forEach>],
  datasets:[{label:'Điểm TB', backgroundColor:MT_COLORS.teal, borderRadius:6, maxBarThickness:42,
    data:[<c:forEach var="t" items="${topicStats}" varStatus="st">${t.avgScore}${st.last?'':','}</c:forEach>]}]},
  options:{plugins:{legend:{display:false}},scales:{y:{beginAtZero:true,max:10},x:{grid:{display:false}}}}});
new Chart(document.getElementById('chartProgress'), {type:'doughnut', data:{
  labels:['Hoàn thành','Đang học','Chưa học'],
  datasets:[{backgroundColor:[MT_COLORS.green,MT_COLORS.chalk,MT_COLORS.grey], borderWidth:0,
    data:[${empty progressStats ? 0 : progressStats.done},${empty progressStats ? 0 : progressStats.doing},${empty progressStats ? 0 : progressStats.notStarted}]}]}, options:{cutout:'68%', plugins:{legend:{position:'bottom'}}}});
</script>
