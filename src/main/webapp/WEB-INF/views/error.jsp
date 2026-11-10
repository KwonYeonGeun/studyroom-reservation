<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jsp" %>
<h1>오류</h1>
<div class="error">${errorMessage}</div>
<p><a class="button secondary" href="<%= contextPath %>/main.do">메인으로</a></p>
<%@ include file="footer.jsp" %>
