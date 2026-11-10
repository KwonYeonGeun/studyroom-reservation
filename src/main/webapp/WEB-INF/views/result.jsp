<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jsp" %>
<h1>처리 결과</h1>
<div class="message">${message}</div>
<p><a class="button" href="<%= contextPath %>/${redirectUrl}">계속</a></p>
<%@ include file="footer.jsp" %>
