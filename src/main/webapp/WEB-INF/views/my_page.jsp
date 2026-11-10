<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.text.SimpleDateFormat, java.util.Date, java.util.List, com.wplab.studyroom.domain.Resource, com.wplab.studyroom.domain.ReservationView" %>
<%@ include file="header.jsp" %>
<%
	List<Resource> resources = (List<Resource>)request.getAttribute("resources");
	List<ReservationView> reservations = (List<ReservationView>)request.getAttribute("reservations");
	SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	String selectedDate = (String)request.getAttribute("selectedDate");
	Integer selectedHourObj = (Integer)request.getAttribute("selectedHour");
	int selectedHour = selectedHourObj == null ? 9 : selectedHourObj.intValue();
	Date slotStart = (Date)request.getAttribute("startTime");
	Date slotEnd = (Date)request.getAttribute("endTime");
	Date now = new Date();
%>
<h1>마이페이지</h1>
<form class="filter-form" method="get" action="<%= contextPath %>/myPage.do">
	<div class="form-row">
		<label for="date">날짜</label>
		<input type="date" id="date" name="date" value="<%= selectedDate %>" required>
	</div>
	<div class="form-row">
		<label for="startHour">시작 시간</label>
		<select id="startHour" name="startHour">
			<% for (int h = 8; h <= 22; h++) { %>
				<option value="<%= h %>" <%= h == selectedHour ? "selected" : "" %>><%= h %>:00</option>
			<% } %>
		</select>
		<button type="submit">조회</button>
	</div>
</form>

<h2>선택 시간 배치도</h2>
<p>하늘색 칸은 내 예약이며, 짙은 회색 칸은 다른 좌석 또는 사물함입니다.</p>

<% if (resources != null) { %>
	<h2>스터디룸 좌석</h2>
	<div class="resource-grid">
		<% for (Resource resource : resources) { %>
			<% if ("SEAT".equals(resource.getType())) { %>
				<%
					ReservationView mine = null;
					if (reservations != null) {
						for (ReservationView reservation : reservations) {
							if (reservation.getResourceId() == resource.getResourceId()
									&& "RESERVED".equals(reservation.getReservationStatus())
									&& reservation.getStartTime().before(slotEnd)
									&& reservation.getEndTime().after(slotStart)) {
								mine = reservation;
								break;
							}
						}
					}
					String cellClass = mine != null ? "mine" : "reserved-other";
					boolean inUse = mine != null && !now.before(mine.getStartTime()) && now.before(mine.getEndTime());
				%>
				<div class="resource-cell <%= cellClass %>">
					<% if (mine != null) { %>
						<div class="name"><%= resource.getName() %></div>
						<div><%= inUse ? "입실 중" : "내 예약" %></div>
						<div><%= fmt.format(mine.getStartTime()) %></div>
						<a href="<%= contextPath %>/reservationDetail.do?reservationId=<%= mine.getReservationId() %>">상세</a>
					<% } %>
				</div>
			<% } %>
		<% } %>
	</div>

	<h2>사물함</h2>
	<div class="resource-grid">
		<% for (Resource resource : resources) { %>
			<% if ("LOCKER".equals(resource.getType())) { %>
				<%
					ReservationView mine = null;
					if (reservations != null) {
						for (ReservationView reservation : reservations) {
							if (reservation.getResourceId() == resource.getResourceId()
									&& "RESERVED".equals(reservation.getReservationStatus())
									&& reservation.getStartTime().before(slotEnd)
									&& reservation.getEndTime().after(slotStart)) {
								mine = reservation;
								break;
							}
						}
					}
					String cellClass = mine != null ? "mine" : "reserved-other";
					boolean inUse = mine != null && !now.before(mine.getStartTime()) && now.before(mine.getEndTime());
				%>
				<div class="resource-cell <%= cellClass %>">
					<% if (mine != null) { %>
						<div class="name"><%= resource.getName() %></div>
						<div><%= inUse ? "사용 중" : "내 예약" %></div>
						<div><%= fmt.format(mine.getStartTime()) %></div>
						<a href="<%= contextPath %>/reservationDetail.do?reservationId=<%= mine.getReservationId() %>">상세</a>
					<% } %>
				</div>
			<% } %>
		<% } %>
	</div>
<% } %>

<h2>내 예약 목록</h2>
<div class="grid reservation-list-grid">
	<% if (reservations == null || reservations.isEmpty()) { %>
		<div class="box">예약 내역이 없습니다.</div>
	<% } else { %>
		<% for (ReservationView reservation : reservations) { %>
			<%
				boolean inUse = !now.before(reservation.getStartTime()) && now.before(reservation.getEndTime())
						&& "RESERVED".equals(reservation.getReservationStatus());
				String displayStatus = inUse
						? ("SEAT".equals(reservation.getResourceType()) ? "입실 중" : "사용 중")
						: reservation.getReservationStatus();
			%>
			<div class="box">
				<div><strong><%= reservation.getResourceName() %></strong></div>
				<div><%= fmt.format(reservation.getStartTime()) %> ~ <%= fmt.format(reservation.getEndTime()) %></div>
				<div>예약: <%= displayStatus %></div>
				<div>결제: <%= reservation.getPaymentStatus() %></div>
				<a href="<%= contextPath %>/reservationDetail.do?reservationId=<%= reservation.getReservationId() %>">상세 보기</a>
			</div>
		<% } %>
	<% } %>
</div>
<%@ include file="footer.jsp" %>
