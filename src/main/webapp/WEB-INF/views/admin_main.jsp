<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="header.jsp" %>
<h1>관리자 대시보드</h1>
<div class="grid">
	<div class="box"><h2>회원 수</h2><p>${userCount}명</p></div>
	<div class="box"><h2>예약 중</h2><p>${reservationCount}건</p></div>
	<div class="box"><h2>취소 요청</h2><p>${cancelCount}건</p></div>
	<div class="box"><h2>승인 매출</h2><p>${totalRevenue}원</p></div>
</div>
<h2>관리 메뉴</h2>
<p>
	<a class="button" href="<%= contextPath %>/adminResources.do">배치 현황</a>
	<a class="button" href="<%= contextPath %>/adminReservations.do">예약 관리</a>
	<a class="button" href="<%= contextPath %>/adminStats.do">매출 통계</a>
</p>
<%@ include file="footer.jsp" %>
