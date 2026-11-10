<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.Calendar, com.wplab.studyroom.common.DateUtil, com.wplab.studyroom.domain.Resource" %>
<%@ include file="header.jsp" %>
<%
	Resource resource = (Resource)request.getAttribute("resource");
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
	String today = DateUtil.today();
	int todayMinStartHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1;
	if (todayMinStartHour < 8) {
		todayMinStartHour = 8;
	}
	if (todayMinStartHour > lastStartHour) {
		todayMinStartHour = closeHour;
	}
%>
<h1>예약 정보 입력</h1>
<form method="post" action="<%= contextPath %>/reservationConfirm.do">
	<input type="hidden" name="resourceId" value="<%= resource.getResourceId() %>">
	<div class="form-row">
		<label>예약 대상</label>
		<span><%= "SEAT".equals(resource.getType()) ? "스터디룸 좌석" : "사물함" %> / <%= resource.getName() %></span>
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
	</div>
	<div class="form-row">
		<label for="method">결제 방법</label>
		<select id="method" name="method">
			<option value="CARD">카드</option>
			<option value="CASH">현금</option>
		</select>
	</div>
	<button type="submit">예약 확인</button>
</form>
<script>
(function() {
	var date = document.getElementById("date");
	var start = document.getElementById("startHour");
	var duration = document.getElementById("durationHour");
	var submit = document.querySelector("form button[type='submit']");
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
<%@ include file="footer.jsp" %>
