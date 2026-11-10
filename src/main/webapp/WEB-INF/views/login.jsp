<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jsp" %>
<h1>로그인</h1>
<% if (request.getAttribute("errorMessage") != null) { %>
	<div class="error"><%= request.getAttribute("errorMessage") %></div>
<% } %>
<form method="post" action="<%= contextPath %>/login.do">
	<div class="form-row">
		<label for="userId">아이디</label>
		<input type="text" id="userId" name="userId" required>
	</div>
	<div class="form-row">
		<label for="password">비밀번호</label>
		<input type="password" id="password" name="password" required>
	</div>
	<button type="submit">로그인</button>
</form>
<%@ include file="footer.jsp" %>
