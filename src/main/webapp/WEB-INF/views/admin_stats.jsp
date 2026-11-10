<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, com.wplab.studyroom.domain.RevenueStat" %>
<%@ include file="header.jsp" %>
<%
	List<RevenueStat> stats = (List<RevenueStat>)request.getAttribute("stats");
	String mode = (String)request.getAttribute("mode");
%>
<h1>매출 통계</h1>
<p>
	<a class="button <%= "DAY".equals(mode) ? "" : "secondary" %>" href="<%= contextPath %>/adminStats.do?mode=DAY">일자별</a>
	<a class="button <%= "MONTH".equals(mode) ? "" : "secondary" %>" href="<%= contextPath %>/adminStats.do?mode=MONTH">월별</a>
	<a class="button <%= "RESOURCE".equals(mode) ? "" : "secondary" %>" href="<%= contextPath %>/adminStats.do?mode=RESOURCE">자원별</a>
</p>
<table>
	<thead>
		<tr><th>구분</th><th>결제 건수</th><th>매출 금액</th></tr>
	</thead>
	<tbody>
	<% if (stats == null || stats.isEmpty()) { %>
		<tr><td colspan="3">매출 데이터가 없습니다.</td></tr>
	<% } else { %>
		<% for (RevenueStat stat : stats) { %>
			<tr>
				<td><%= stat.getLabel() %></td>
				<td><%= stat.getCount() %>건</td>
				<td><%= stat.getAmount() %>원</td>
			</tr>
		<% } %>
	<% } %>
	</tbody>
</table>
<%@ include file="footer.jsp" %>
