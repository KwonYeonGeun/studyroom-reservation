<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jsp" %>
<h1>회원가입</h1>
<% if (request.getAttribute("errorMessage") != null) { %>
	<div class="error"><%= request.getAttribute("errorMessage") %></div>
<% } %>
<form method="post" action="<%= contextPath %>/signup.do">
	<div class="form-row">
		<label for="userId">아이디</label>
		<input type="text" id="userId" name="userId" maxlength="20" required>
	</div>
	<div class="form-row">
		<label for="password">비밀번호</label>
		<input type="password" id="password" name="password" maxlength="30" required>
	</div>
	<div class="form-row">
		<label for="name">이름</label>
		<input type="text" id="name" name="name" maxlength="30" required>
	</div>
	<div class="form-row">
		<label for="phone">전화번호</label>
		<input type="text" id="phone" name="phone" maxlength="20">
	</div>
	<button type="submit">가입</button>
</form>
<%@ include file="footer.jsp" %>
