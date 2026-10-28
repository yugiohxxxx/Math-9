<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Cấu hình AI"/>
<%@ include file="/WEB-INF/jspf/layout-header.jspf" %>
<h3 class="mb-3"><i class="bi bi-robot"></i> Cấu hình AI (Gemini API)</h3>
<div class="alert alert-secondary">
  Nội dung prompt được đặt ở đầu yêu cầu gửi cho AI; hệ thống tự ghép thêm đề bài, đáp án và bài làm của học sinh nên không cần dùng biến.
  Model khuyên dùng: <code>gemini-3.5-flash-lite</code> (nhanh, ổn định). Khi model đang chọn bị quá tải, hệ thống tự chuyển sang <code>gemini-3.5-flash-lite</code>.
</div>
<c:forEach var="cfg" items="${configs}">
<div class="card mb-3"><div class="card-header">
  Prompt: <strong>${cfg.promptType}</strong>
  <span class="badge text-bg-${cfg.active?'success':'secondary'}">${cfg.active?'Đang bật':'Tắt'}</span></div>
<div class="card-body">
<form method="post" action="${ctx}/admin/ai-config">
  <input type="hidden" name="configId" value="${cfg.configId}">
  <div class="row">
    <div class="col-md-3 mb-3"><label class="form-label">Provider</label>
      <input class="form-control" name="provider" value="<c:out value='${cfg.provider}'/>"></div>
    <div class="col-md-3 mb-3"><label class="form-label">Model</label>
      <input class="form-control" name="modelName" value="<c:out value='${cfg.modelName}'/>"></div>
    <div class="col-md-6 mb-3"><label class="form-label">API Key</label>
      <input type="password" class="form-control" name="apiKey" value="<c:out value='${cfg.apiKey}'/>"></div>
  </div>
  <div class="mb-3"><label class="form-label">API URL (tuỳ chọn)</label>
    <input class="form-control" name="apiUrl" value="<c:out value='${cfg.apiUrl}'/>"></div>
  <div class="mb-3"><label class="form-label">Nội dung prompt</label>
    <textarea class="form-control" rows="5" name="promptContent"><c:out value="${cfg.promptContent}"/></textarea></div>
  <div class="form-check mb-3"><input class="form-check-input" type="checkbox" name="active" id="a${cfg.configId}" ${cfg.active?'checked':''}>
    <label class="form-check-label" for="a${cfg.configId}">Kích hoạt</label></div>
  <button class="btn btn-primary">Lưu cấu hình</button>
  <button class="btn btn-outline-secondary" name="action" value="test" formnovalidate
          onclick="this.innerHTML='<span class=&quot;spinner-border spinner-border-sm me-2&quot;></span>Đang thử (tối đa 1 phút)...'"><i class="bi bi-activity"></i> Kiểm tra kết nối AI</button>
</form></div></div>
</c:forEach>

<div class="card border-primary"><div class="card-header bg-primary text-white">Thêm prompt mới</div><div class="card-body">
<form method="post" action="${ctx}/admin/ai-config">
  <div class="row">
    <div class="col-md-4 mb-3"><label class="form-label">Loại prompt</label>
      <select class="form-select" name="promptType">
        <option value="generate_question">generate_question – AI sinh câu hỏi</option>
        <option value="grade_mcq">grade_mcq – nhận xét câu trắc nghiệm sai</option>
        <option value="solve_step">solve_step – giải tự luận từng bước</option>
        <option value="grade_essay">grade_essay – chấm bài tự luận (ảnh/chữ)</option></select></div>
    <div class="col-md-4 mb-3"><label class="form-label">Model</label>
      <input class="form-control" name="modelName" value="gemini-3.5-flash-lite"></div>
    <div class="col-md-4 mb-3"><label class="form-label">API Key</label>
      <input type="password" class="form-control" name="apiKey" required></div>
  </div>
  <input type="hidden" name="provider" value="gemini">
  <div class="mb-3"><label class="form-label">Nội dung prompt</label>
    <textarea class="form-control" rows="4" name="promptContent" required></textarea></div>
  <input type="hidden" name="active" value="on">
  <button class="btn btn-success">Thêm</button>
</form></div></div>
<%@ include file="/WEB-INF/jspf/layout-footer.jspf" %>
