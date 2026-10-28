<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Luyện tập trắc nghiệm"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<h3 class="mb-3">Trắc nghiệm: <c:out value="${practice.topicTitle}"/></h3>
<div class="row g-4">
<div class="col-lg-9">
<form method="post" action="${ctx}/student/practice-submit" id="mcqForm">
  <input type="hidden" name="sessionId" value="${practice.sessionId}">
  <c:forEach var="q" items="${questions}" varStatus="st">
    <div class="card mb-3 mcq-q" id="cau${st.count}" data-no="${st.count}"><div class="card-body">
      <h6>Câu ${st.count} <span class="badge text-bg-light">Mức ${q.level}</span></h6>
      <div class="mb-2">${q.content}</div>
      <c:if test="${not empty q.imageUrl}"><img src="${ctx}/${q.imageUrl}" class="img-fluid mb-2" style="max-height:200px" alt="Hình minh hoạ"></c:if>
      <c:forEach var="o" items="${q.options}">
        <div class="form-check">
          <input class="form-check-input" type="radio" name="q${q.questionId}" id="o${o.optionId}" value="${o.optionId}">
          <label class="form-check-label" for="o${o.optionId}"><strong>${o.optionLabel}.</strong> <c:out value="${o.content}"/></label></div>
      </c:forEach>
    </div></div>
  </c:forEach>
  <button class="btn btn-primary btn-lg" id="submitBtn"><i class="bi bi-send"></i> Nộp bài</button>
</form>
</div>
<!-- Cây câu hỏi bên phải: chưa làm = đỏ đậm, đã làm = chữ thường -->
<div class="col-lg-3">
  <aside class="card progress-tree">
    <div class="card-header fw-semibold"><i class="bi bi-list-check"></i> Danh sách câu <span class="float-end" id="doneCount">0/${fn:length(questions)}</span></div>
    <div class="card-body">
      <ul class="tree tree-grid">
        <c:forEach var="q" items="${questions}" varStatus="st">
          <li><a href="#cau${st.count}" class="t-todo" id="nav${st.count}">Câu ${st.count}</a></li>
        </c:forEach>
      </ul>
      <div class="small text-muted border-top pt-2 mt-2"><span class="t-todo">Đỏ đậm</span>: chưa làm · <span class="t-done">Chữ thường</span>: đã làm</div>
    </div>
  </aside>
</div>
</div>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
<script>
(function () {
  var cards = document.querySelectorAll('.mcq-q'), total = cards.length;
  function refresh() {
    var done = 0;
    cards.forEach(function (c) {
      var ok = c.querySelector('input[type=radio]:checked') !== null, a = document.getElementById('nav' + c.dataset.no);
      a.classList.toggle('t-todo', !ok); a.classList.toggle('t-done', ok);
      if (ok) done++;
    });
    document.getElementById('doneCount').textContent = done + '/' + total;
    return done;
  }
  document.getElementById('mcqForm').addEventListener('change', refresh);
  document.getElementById('mcqForm').addEventListener('submit', function (e) {
    var left = total - refresh();
    if (!confirm(left > 0 ? 'Bạn còn ' + left + ' câu chưa làm (đánh dấu đỏ). Vẫn nộp bài?' : 'Nộp bài?')) { e.preventDefault(); return; }
    var b = document.getElementById('submitBtn');
    b.disabled = true;
    b.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang chấm bài, AI đang nhận xét (tối đa 30 giây)...';
  });
  refresh();
})();
</script>
