<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Date, java.util.List, com.wplab.studyroom.domain.Resource" %>
<%@ include file="header.jsp" %>
<%
	List<Resource> resources = (List<Resource>)request.getAttribute("resources");
	String selectedType = (String)request.getAttribute("selectedType");
	String selectedDate = (String)request.getAttribute("selectedDate");
	Integer selectedHourObj = (Integer)request.getAttribute("selectedHour");
	Integer selectedDurationObj = (Integer)request.getAttribute("selectedDuration");
	Integer maxDurationObj = (Integer)request.getAttribute("maxDuration");
	Integer minStartHourObj = (Integer)request.getAttribute("minStartHour");
	Integer lastStartHourObj = (Integer)request.getAttribute("lastStartHour");
	Integer closeHourObj = (Integer)request.getAttribute("closeHour");
	int selectedHour = selectedHourObj == null ? 9 : selectedHourObj.intValue();
	int selectedDuration = selectedDurationObj == null ? 1 : selectedDurationObj.intValue();
	int maxDuration = maxDurationObj == null ? 14 : maxDurationObj.intValue();
	int minStartHour = minStartHourObj == null ? 8 : minStartHourObj.intValue();
	int lastStartHour = lastStartHourObj == null ? 22 : lastStartHourObj.intValue();
	int closeHour = closeHourObj == null ? 23 : closeHourObj.intValue();
	Date slotStart = (Date)request.getAttribute("startTime");
	Date slotEnd = (Date)request.getAttribute("endTime");
	Date now = new Date();
	boolean currentSlot = slotStart != null && slotEnd != null && !now.before(slotStart) && now.before(slotEnd);
%>
<h1>관리자 배치 현황</h1>
<form class="filter-form" method="get" action="<%= contextPath %>/adminResources.do">
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
		<input type="date" id="date" name="date" value="<%= selectedDate %>" required>
	</div>
	<div class="form-row">
		<label for="startHour">시작 시간</label>
		<select id="startHour" name="startHour">
			<% for (int h = minStartHour; h <= lastStartHour; h++) { %>
				<option value="<%= h %>" <%= h == selectedHour ? "selected" : "" %>><%= h %>:00</option>
			<% } %>
		</select>
	</div>
	<div class="form-row">
		<label for="durationHour">조회 시간</label>
		<select id="durationHour" name="durationHour">
			<% for (int h = 1; h <= maxDuration; h++) { %>
				<option value="<%= h %>" <%= h == selectedDuration ? "selected" : "" %>><%= h %>시간</option>
			<% } %>
		</select>
		<button type="submit">조회</button>
	</div>
</form>
<script>
(function() {
	var start = document.getElementById("startHour");
	var duration = document.getElementById("durationHour");
	var closeHour = <%= closeHour %>;
	var selectedDuration = <%= selectedDuration %>;
	function updateDuration() {
		if (start == null || duration == null || start.value.length == 0) {
			return;
		}
		var max = closeHour - parseInt(start.value, 10);
		duration.innerHTML = "";
		for (var h = 1; h <= max; h++) {
			var option = document.createElement("option");
			option.value = h;
			option.text = h + "시간";
			if (h == selectedDuration && h <= max) {
				option.selected = true;
			}
			duration.appendChild(option);
		}
	}
	if (start != null) {
		start.onchange = updateDuration;
	}
})();
</script>

<% if (resources == null || resources.isEmpty()) { %>
	<div class="message">조회 결과가 없습니다.</div>
<% } else { %>
	<% if (selectedType == null || selectedType.length() == 0 || "SEAT".equals(selectedType)) { %>
		<h2>스터디룸 좌석</h2>
		<div class="resource-grid">
			<% for (Resource resource : resources) { %>
				<% if ("SEAT".equals(resource.getType())) { %>
					<div class="resource-cell <%= resource.isAvailable() ? "unused" : (!"AVAILABLE".equals(resource.getStatus()) ? "disabled-cell" : "reserved-other") %>">
						<div class="name"><%= resource.getName() %></div>
						<div><%= resource.getLocation() %></div>
						<% if (!"AVAILABLE".equals(resource.getStatus())) { %>
							<span class="disabled">비활성</span>
						<% } else if (resource.isReserved()) { %>
							<span class="unavailable"><%= currentSlot ? "입실 중" : "예약됨" %></span>
						<% } else { %>
							<span class="available">예약가능</span>
						<% } %>
					</div>
				<% } %>
			<% } %>
		</div>
	<% } %>

	<% if (selectedType == null || selectedType.length() == 0 || "LOCKER".equals(selectedType)) { %>
		<h2>사물함</h2>
		<div class="resource-grid">
			<% for (Resource resource : resources) { %>
				<% if ("LOCKER".equals(resource.getType())) { %>
					<div class="resource-cell <%= resource.isAvailable() ? "unused" : (!"AVAILABLE".equals(resource.getStatus()) ? "disabled-cell" : "reserved-other") %>">
						<div class="name"><%= resource.getName() %></div>
						<div><%= resource.getLocation() %></div>
						<% if (!"AVAILABLE".equals(resource.getStatus())) { %>
							<span class="disabled">비활성</span>
						<% } else if (resource.isReserved()) { %>
							<span class="unavailable"><%= currentSlot ? "사용 중" : "예약됨" %></span>
						<% } else { %>
							<span class="available">예약가능</span>
						<% } %>
					</div>
				<% } %>
			<% } %>
		</div>
	<% } %>
<% } %>
<%@ include file="footer.jsp" %>
