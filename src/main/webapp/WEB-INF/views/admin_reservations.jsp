<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.text.SimpleDateFormat, java.util.List, com.wplab.studyroom.domain.ReservationView, com.wplab.studyroom.domain.CancelRequest" %>
<%@ include file="header.jsp" %>
<%
	List<ReservationView> reservations = (List<ReservationView>)request.getAttribute("reservations");
	List<CancelRequest> cancelRequests = (List<CancelRequest>)request.getAttribute("cancelRequests");
	SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	String userKeyword = (String)request.getAttribute("userKeyword");
	String status = (String)request.getAttribute("status");
	String selectedType = (String)request.getAttribute("selectedType");
	String selectedDate = (String)request.getAttribute("selectedDate");
%>
<h1>관리자 예약 관리</h1>
<form method="get" action="<%= contextPath %>/adminReservations.do">
	<div class="form-row">
		<label for="userKeyword">회원</label>
		<input type="text" id="userKeyword" name="userKeyword" value="<%= userKeyword == null ? "" : userKeyword %>">
	</div>
	<div class="form-row">
		<label for="status">예약 상태</label>
		<select id="status" name="status">
			<option value="" <%= status == null || status.length() == 0 ? "selected" : "" %>>전체</option>
			<option value="RESERVED" <%= "RESERVED".equals(status) ? "selected" : "" %>>RESERVED</option>
			<option value="CANCELLED" <%= "CANCELLED".equals(status) ? "selected" : "" %>>CANCELLED</option>
		</select>
	</div>
	<div class="form-row">
		<label for="type">자원 유형</label>
		<select id="type" name="type">
			<option value="" <%= selectedType == null || selectedType.length() == 0 ? "selected" : "" %>>전체</option>
			<option value="SEAT" <%= "SEAT".equals(selectedType) ? "selected" : "" %>>스터디룸 좌석</option>
			<option value="LOCKER" <%= "LOCKER".equals(selectedType) ? "selected" : "" %>>사물함</option>
		</select>
	</div>
	<div class="form-row">
		<label for="date">날짜</label>
		<input type="date" id="date" name="date" value="<%= selectedDate == null ? "" : selectedDate %>">
		<button type="submit">검색</button>
	</div>
</form>

<h2>취소 요청</h2>
<table>
	<thead>
		<tr><th>요청번호</th><th>예약번호</th><th>회원</th><th>자원</th><th>사유</th><th>요청일</th><th>처리</th></tr>
	</thead>
	<tbody>
	<% if (cancelRequests == null || cancelRequests.isEmpty()) { %>
		<tr><td colspan="7">대기 중인 취소 요청이 없습니다.</td></tr>
	<% } else { %>
		<% for (CancelRequest cancel : cancelRequests) { %>
			<tr>
				<td><%= cancel.getRequestId() %></td>
				<td><%= cancel.getReservationId() %></td>
				<td><%= cancel.getUserName() %> (<%= cancel.getUserId() %>)</td>
				<td><%= cancel.getResourceName() %></td>
				<td><%= cancel.getReason() == null ? "" : cancel.getReason() %></td>
				<td><%= fmt.format(cancel.getRequestedAt()) %></td>
				<td>
					<form method="post" action="<%= contextPath %>/cancelApprove.do" style="display:inline;">
						<input type="hidden" name="requestId" value="<%= cancel.getRequestId() %>">
						<button type="submit">승인</button>
					</form>
					<form method="post" action="<%= contextPath %>/cancelReject.do" style="display:inline;">
						<input type="hidden" name="requestId" value="<%= cancel.getRequestId() %>">
						<button class="secondary" type="submit">반려</button>
					</form>
				</td>
			</tr>
		<% } %>
	<% } %>
	</tbody>
</table>

<h2>전체 예약</h2>
<table>
	<thead>
		<tr><th>예약번호</th><th>회원</th><th>자원</th><th>시간</th><th>예약상태</th><th>결제</th><th>상세</th></tr>
	</thead>
	<tbody>
	<% if (reservations == null || reservations.isEmpty()) { %>
		<tr><td colspan="7">예약 내역이 없습니다.</td></tr>
	<% } else { %>
		<% for (ReservationView reservation : reservations) { %>
			<tr>
				<td><%= reservation.getReservationId() %></td>
				<td><%= reservation.getUserName() %> (<%= reservation.getUserId() %>)</td>
				<td><%= reservation.getResourceName() %></td>
				<td><%= fmt.format(reservation.getStartTime()) %> ~ <%= fmt.format(reservation.getEndTime()) %></td>
				<td><%= reservation.getReservationStatus() %></td>
				<td><%= reservation.getPaymentStatus() %> / <%= reservation.getAmount() %>원</td>
				<td><a href="<%= contextPath %>/reservationDetail.do?reservationId=<%= reservation.getReservationId() %>">보기</a></td>
			</tr>
		<% } %>
	<% } %>
	</tbody>
</table>
<%@ include file="footer.jsp" %>
