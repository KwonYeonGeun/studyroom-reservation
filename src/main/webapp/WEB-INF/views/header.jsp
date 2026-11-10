<%@ page import="com.wplab.studyroom.domain.User" %>
<%
	User loginUser = (User)session.getAttribute("user");
	String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>스터디룸 및 사물함 예약 시스템</title>
	<link rel="stylesheet" href="<%= contextPath %>/css/style.css?v=20260603-ui3">
</head>
<body>
	<div class="topbar">
		<div class="nav-links">
			<span class="title"><a href="<%= contextPath %>/main.do">스터디룸 예약</a></span>
			<a href="<%= contextPath %>/resourceList.do">예약하기</a>
			<% if (loginUser != null) { %>
				<a href="<%= contextPath %>/myPage.do">마이페이지</a>
				<% if (loginUser.isAdmin()) { %>
					<a href="<%= contextPath %>/adminMain.do">관리자</a>
				<% } %>
			<% } %>
		</div>
		<div class="auth-links">
			<% if (loginUser != null) { %>
			<span><%= loginUser.getName() %>님</span>
			<a href="<%= contextPath %>/logout.do">로그아웃</a>
			<% } else { %>
			<a href="<%= contextPath %>/loginForm.do">로그인</a>
			<a href="<%= contextPath %>/signupForm.do">회원가입</a>
			<% } %>
		</div>
	</div>
	<div class="container">
