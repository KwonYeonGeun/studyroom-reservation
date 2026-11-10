<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Calendar, java.util.List, com.wplab.studyroom.common.DateUtil, com.wplab.studyroom.domain.Resource" %>
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
	Boolean hasAvailableTimeObj = (Boolean)request.getAttribute("hasAvailableTime");
	int selectedHour = selectedHourObj == null ? 9 : selectedHourObj.intValue();
	int selectedDuration = selectedDurationObj == null ? 1 : selectedDurationObj.intValue();
	int maxDuration = maxDurationObj == null ? 14 : maxDurationObj.intValue();
	int minStartHour = minStartHourObj == null ? 8 : minStartHourObj.intValue();
	int lastStartHour = lastStartHourObj == null ? 22 : lastStartHourObj.intValue();
	int closeHour = closeHourObj == null ? 23 : closeHourObj.intValue();
	boolean hasAvailableTime = hasAvailableTimeObj == null || hasAvailableTimeObj.booleanValue();
	String today = DateUtil.today();
	int todayMinStartHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1;
	if (todayMinStartHour < 8) {
		todayMinStartHour = 8;
	}
	if (todayMinStartHour > lastStartHour) {
		todayMinStartHour = closeHour;
	}
%>
<h1>예약 가능 현황</h1>
<form class="filter-form" method="get" action="<%= contextPath %>/resourceList.do">
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
		<input type="date" id="date" name="date" value="<%= selectedDate %>" min="<%= DateUtil.today() %>" required>
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
		<label for="durationHour">이용 시간</label>
		<select id="durationHour" name="durationHour">
			<% for (int h = 1; h <= maxDuration; h++) { %>
				<option value="<%= h %>" <%= h == selectedDuration ? "selected" : "" %>><%= h %>시간</option>
			<% } %>
		</select>
		<button type="submit" <%= hasAvailableTime ? "" : "disabled" %>>조회</button>
	</div>
</form>
<script>
(function() {
	var date = document.getElementById("date");
	var start = document.getElementById("startHour");
	var duration = document.getElementById("durationHour");
	var submit = document.querySelector(".filter-form button[type='submit']");
	var today = "<%= today %>";
	var openHour = 8;
	var todayMinStartHour = <%= todayMinStartHour %>;
	var lastStartHour = <%= lastStartHour %>;
	var closeHour = <%= closeHour %>;
	var selectedHour = <%= selectedHour %>;
	var selectedDuration = <%= selectedDuration %>;

	function updateStartHour() {
		if (date == null || start == null) {
			return;
		}
		var minHour = date.value == today ? todayMinStartHour : openHour;
		start.innerHTML = "";
		if (minHour > lastStartHour) {
			duration.innerHTML = "";
			if (submit != null) {
				submit.disabled = true;
			}
			return;
		}
		if (submit != null) {
			submit.disabled = false;
		}
		var selectHour = selectedHour;
		if (selectHour < minHour || selectHour > lastStartHour) {
			selectHour = minHour;
		}
		for (var h = minHour; h <= lastStartHour; h++) {
			var option = document.createElement("option");
			option.value = h;
			option.text = h + ":00";
			if (h == selectHour) {
				option.selected = true;
			}
			start.appendChild(option);
		}
		updateDuration();
	}

	function updateDuration() {
		if (start == null || duration == null || start.value.length == 0) {
			return;
		}
		var max = closeHour - parseInt(start.value, 10);
		var selectDuration = parseInt(duration.value, 10);
		if (isNaN(selectDuration)) {
			selectDuration = selectedDuration;
		}
		if (selectDuration > max) {
			selectDuration = max;
		}
		if (selectDuration < 1) {
			selectDuration = 1;
		}
		duration.innerHTML = "";
		for (var h = 1; h <= max; h++) {
			var option = document.createElement("option");
			option.value = h;
			option.text = h + "시간";
			if (h == selectDuration) {
				option.selected = true;
			}
			duration.appendChild(option);
		}
	}
	if (date != null) {
		date.onchange = updateStartHour;
	}
	if (start != null) {
		start.onchange = updateDuration;
	}
})();
</script>

<% if (!hasAvailableTime) { %>
	<div class="message">선택한 날짜에는 예약 가능한 시간이 없습니다.</div>
<% } else if (resources == null || resources.isEmpty()) { %>
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
						<div><%= resource.getPrice() %>원 / 1시간</div>
						<% if (resource.isAvailable()) { %>
							<form method="get" action="<%= contextPath %>/reservationForm.do">
								<input type="hidden" name="resourceId" value="<%= resource.getResourceId() %>">
								<input type="hidden" name="date" value="<%= selectedDate %>">
								<input type="hidden" name="startHour" value="<%= selectedHour %>">
								<input type="hidden" name="durationHour" value="<%= selectedDuration %>">
								<button type="submit">선택</button>
							</form>
						<% } else if (!"AVAILABLE".equals(resource.getStatus())) { %>
							<span class="disabled">비활성</span>
						<% } else { %>
							<span class="unavailable">예약됨</span>
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
						<div><%= resource.getPrice() %>원 / 1시간</div>
						<% if (resource.isAvailable()) { %>
							<form method="get" action="<%= contextPath %>/reservationForm.do">
								<input type="hidden" name="resourceId" value="<%= resource.getResourceId() %>">
								<input type="hidden" name="date" value="<%= selectedDate %>">
								<input type="hidden" name="startHour" value="<%= selectedHour %>">
								<input type="hidden" name="durationHour" value="<%= selectedDuration %>">
								<button type="submit">선택</button>
							</form>
						<% } else if (!"AVAILABLE".equals(resource.getStatus())) { %>
							<span class="disabled">비활성</span>
						<% } else { %>
							<span class="unavailable">예약됨</span>
						<% } %>
					</div>
				<% } %>
			<% } %>
		</div>
	<% } %>
<% } %>
<%@ include file="footer.jsp" %>
