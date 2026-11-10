<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.text.SimpleDateFormat, java.util.Date, com.wplab.studyroom.domain.ReservationView" %>
<%@ include file="header.jsp" %>
<%
	ReservationView reservation = (ReservationView)request.getAttribute("reservation");
	SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	Date now = new Date();
	boolean inUse = !now.before(reservation.getStartTime()) && now.before(reservation.getEndTime())
			&& "RESERVED".equals(reservation.getReservationStatus());
	boolean beforeCancelLimit = now.getTime() <= reservation.getStartTime().getTime() - (60L * 60L * 1000L);
	String displayStatus = inUse
			? ("SEAT".equals(reservation.getResourceType()) ? "입실 중" : "사용 중")
			: reservation.getReservationStatus();
	boolean canRequestCancel = loginUser != null && !loginUser.isAdmin()
			&& "RESERVED".equals(reservation.getReservationStatus())
			&& reservation.getCancelRequestId() == null
			&& beforeCancelLimit;
%>
<h1>예약 상세</h1>
<table>
	<tr><th>예약번호</th><td><%= reservation.getReservationId() %></td></tr>
	<tr><th>회원</th><td><%= reservation.getUserName() %> (<%= reservation.getUserId() %>)</td></tr>
	<tr><th>자원</th><td><%= reservation.getResourceName() %></td></tr>
	<tr><th>유형</th><td><%= "SEAT".equals(reservation.getResourceType()) ? "스터디룸 좌석" : "사물함" %></td></tr>
	<tr><th>시간</th><td><%= fmt.format(reservation.getStartTime()) %> ~ <%= fmt.format(reservation.getEndTime()) %></td></tr>
	<tr><th>예약 상태</th><td><%= displayStatus %></td></tr>
	<tr><th>결제 금액</th><td><%= reservation.getAmount() %>원</td></tr>
	<tr><th>결제 상태</th><td><%= reservation.getPaymentStatus() %></td></tr>
	<tr><th>취소 요청</th><td><%= reservation.getCancelRequestStatus() == null ? "-" : reservation.getCancelRequestStatus() %></td></tr>
</table>

<% if (canRequestCancel) { %>
	<h2>취소 요청</h2>
	<form method="post" action="<%= contextPath %>/cancelRequest.do">
		<input type="hidden" name="reservationId" value="<%= reservation.getReservationId() %>">
		<div class="form-row">
			<label for="reason">사유</label>
			<textarea id="reason" name="reason" rows="3" cols="60"></textarea>
		</div>
		<button type="submit">취소 요청</button>
	</form>
<% } else if (loginUser != null && !loginUser.isAdmin()
		&& "RESERVED".equals(reservation.getReservationStatus())
		&& reservation.getCancelRequestId() == null) { %>
	<div class="message">취소 요청은 예약 시작 1시간 전까지만 가능합니다.</div>
<% } %>
<%@ include file="footer.jsp" %>
