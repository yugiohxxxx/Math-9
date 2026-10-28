<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Tiến độ học tập"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<h3 class="mb-3"><i class="bi bi-graph-up"></i> Tiến độ học tập</h3>
<div class="row g-3 mb-4">
  <div class="col-md-4"><div class="stat tone-green"><div class="stat-icon"><i class="bi bi-check2-circle"></i></div><div><div class="stat-value">${progress.done}</div><div class="stat-label">Bài đã xong</div></div></div></div>
  <div class="col-md-4"><div class="stat tone-chalk"><div class="stat-icon"><i class="bi bi-hourglass-split"></i></div><div><div class="stat-value">${progress.doing}</div><div class="stat-label">Đang học</div></div></div></div>
  <div class="col-md-4"><div class="stat tone-grey"><div class="stat-icon"><i class="bi bi-circle"></i></div><div><div class="stat-value">${progress.notStarted}</div><div class="stat-label">Chưa học</div></div></div></div>
</div>
<div class="row g-3">
  <div class="col-lg-7"><div class="card h-100"><div class="card-header">Điểm luyện tập theo thời gian</div><div class="card-body"><canvas id="scoreChart"></canvas></div></div></div>
  <div class="col-lg-5"><div class="card h-100"><div class="card-header">Chi tiết bài giảng</div>
    <ul class="list-group list-group-flush">
      <c:forEach var="l" items="${lessons}"><li class="list-group-item d-flex justify-content-between">
        <a href="${ctx}/student/lesson?id=${l.lessonId}"><c:out value="${l.title}"/></a>
        <span class="badge text-bg-${l.status=='completed'?'success':(l.status=='in_progress'?'warning':'secondary')}">
          ${l.status=='completed'?'Đã xong':(l.status=='in_progress'?'Đang học':'Chưa học')}</span></li></c:forEach></ul></div></div>
</div>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
<script>
new Chart(document.getElementById('scoreChart'),{type:'line',data:{
  labels:[<c:forEach var="h" items="${history}" varStatus="st">"<fmt:formatDate value='${h.startedAt}' pattern='dd/MM'/>"${st.last?'':','}</c:forEach>],
  datasets:[{label:'Điểm',borderColor:MT_COLORS.teal,backgroundColor:'rgba(31,122,108,.10)',fill:true,pointBackgroundColor:MT_COLORS.teal,tension:.3,data:[<c:forEach var="h" items="${history}" varStatus="st">${h.score}${st.last?'':','}</c:forEach>]}]},
  options:{plugins:{legend:{display:false}},scales:{y:{beginAtZero:true,max:10},x:{grid:{display:false}}}}});
</script>
