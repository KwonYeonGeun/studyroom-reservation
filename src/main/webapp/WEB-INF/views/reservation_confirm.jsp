<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.wplab.studyroom.domain.Resource" %>
<%@ include file="header.jsp" %>
<%
	Resource resource = (Resource)request.getAttribute("resource");
	String selectedDate = (String)request.getAttribute("selectedDate");
	Integer selectedHourObj = (Integer)request.getAttribute("selectedHour");
	Integer selectedDurationObj = (Integer)request.getAttribute("selectedDuration");
	Integer endHourObj = (Integer)request.getAttribute("endHour");
	String method = (String)request.getAttribute("method");
%>
<h1>예약 확인 및 결제</h1>
<table>
	<tr><th>예약 대상</th><td><%= resource.getName() %></td></tr>
	<tr><th>유형</th><td><%= "SEAT".equals(resource.getType()) ? "스터디룸 좌석" : "사물함" %></td></tr>
	<tr><th>위치</th><td><%= resource.getLocation() %></td></tr>
	<tr><th>예약 일시</th><td><%= selectedDate %> <%= selectedHourObj %>:00 ~ <%= endHourObj %>:00</td></tr>
	<tr><th>이용 시간</th><td><%= selectedDurationObj %>시간</td></tr>
	<tr><th>결제 방법</th><td><%= method == null || method.length() == 0 ? "CARD" : method %></td></tr>
	<tr><th>결제 금액</th><td>${amount}원</td></tr>
</table>
<form method="post" action="<%= contextPath %>/paymentApprove.do">
	<input type="hidden" name="resourceId" value="<%= resource.getResourceId() %>">
	<input type="hidden" name="date" value="<%= selectedDate %>">
	<input type="hidden" name="startHour" value="<%= selectedHourObj %>">
	<input type="hidden" name="durationHour" value="<%= selectedDurationObj %>">
	<input type="hidden" name="method" value="<%= method == null || method.length() == 0 ? "CARD" : method %>">
	<p><button type="submit">결제 승인</button></p>
</form>
<%@ include file="footer.jsp" %>
