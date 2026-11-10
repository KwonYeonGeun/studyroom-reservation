<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jsp" %>
<h1>스터디룸 좌석 및 사물함 예약 시스템</h1>
<p>스터디룸 좌석 및 사물함을 예약해보세요.</p>

<div class="grid main-grid">
	<div class="box">
		<h2>예약</h2>
		<p>좌석 및 사물함 예약하기</p>
		<a class="button" href="<%= contextPath %>/resourceList.do">예약하기</a>
	</div>
	<div class="box">
		<h2>마이페이지</h2>
		<p>예약 내역과 결제 상태를 확인하실 수 있습니다.</p>
		<a class="button secondary" href="<%= contextPath %>/myPage.do">내 예약 확인</a>
	</div>
	<% if (loginUser != null && loginUser.isAdmin()) { %>
	<div class="box">
		<h2>관리자</h2>
		<p>배치 현황, 예약 목록, 취소 요청, 매출 통계 관리가 가능합니다.</p>
		<a class="button secondary" href="<%= contextPath %>/adminMain.do">관리자 페이지</a>
	</div>
	<% } %>
</div>
<%@ include file="footer.jsp" %>
