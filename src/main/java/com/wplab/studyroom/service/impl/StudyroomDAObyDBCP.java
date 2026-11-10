package com.wplab.studyroom.service.impl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.wplab.studyroom.common.StudyroomException;
import com.wplab.studyroom.domain.CancelRequest;
import com.wplab.studyroom.domain.ReservationView;
import com.wplab.studyroom.domain.Resource;
import com.wplab.studyroom.domain.RevenueStat;
import com.wplab.studyroom.domain.User;
import com.wplab.studyroom.service.StudyroomDAO;

public class StudyroomDAObyDBCP implements StudyroomDAO {
	private DataSource ds = null;

	public StudyroomDAObyDBCP(String dbcpResourceName) {
		try {
			Context initContext = new InitialContext();
			Context envContext = (Context)initContext.lookup("java:/comp/env");
			ds = (DataSource)envContext.lookup(dbcpResourceName);
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	private Connection connectDB() throws SQLException {
		return ds.getConnection();
	}

	private void disconnectDB(ResultSet rs, Statement stmt, Connection conn) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void disconnectDB(Statement stmt, Connection conn) {
		disconnectDB(null, stmt, conn);
	}

	@Override
	public User getUser(String userId) throws SQLException {
		String sql = "SELECT * FROM SR_USERS WHERE USER_ID=?";
		Connection conn = connectDB();
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, userId);
		ResultSet rs = pstmt.executeQuery();

		User user = null;
		if (rs.next()) {
			user = mapUser(rs);
		}

		disconnectDB(rs, pstmt, conn);
		return user;
	}

	@Override
	public int insertUser(User user) throws SQLException {
		String sql = "INSERT INTO SR_USERS(USER_ID, PASSWORD, NAME, PHONE, ROLE) VALUES(?, ?, ?, ?, 'USER')";
		Connection conn = connectDB();
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, user.getUserId());
		pstmt.setString(2, user.getPassword());
		pstmt.setString(3, user.getName());
		pstmt.setString(4, user.getPhone());
		int result = pstmt.executeUpdate();
		disconnectDB(pstmt, conn);
		return result;
	}

	@Override
	public List<Resource> getResources(String type, Timestamp startTime, Timestamp endTime) throws SQLException {
		String sql = "SELECT RES.*, "
				+ "(SELECT COUNT(*) FROM SR_RESERVATIONS RV "
				+ " WHERE RV.RESOURCE_ID=RES.RESOURCE_ID AND RV.STATUS='RESERVED' "
				+ " AND RV.START_TIME < ? AND RV.END_TIME > ?) AS RESERVED_COUNT "
				+ "FROM SR_RESOURCES RES "
				+ "WHERE (? IS NULL OR RES.TYPE=?) "
				+ "ORDER BY RES.TYPE, RES.DISPLAY_ORDER";
		Connection conn = connectDB();
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setTimestamp(1, endTime);
		pstmt.setTimestamp(2, startTime);
		pstmt.setString(3, type);
		pstmt.setString(4, type);
		ResultSet rs = pstmt.executeQuery();

		List<Resource> resources = new ArrayList<Resource>();
		while (rs.next()) {
			Resource resource = mapResource(rs);
			resource.setReserved(rs.getInt("RESERVED_COUNT") > 0);
			resources.add(resource);
		}

		disconnectDB(rs, pstmt, conn);
		return resources;
	}

	@Override
	public Resource getResource(long resourceId) throws SQLException {
		String sql = "SELECT * FROM SR_RESOURCES WHERE RESOURCE_ID=?";
		Connection conn = connectDB();
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setLong(1, resourceId);
		ResultSet rs = pstmt.executeQuery();

		Resource resource = null;
		if (rs.next()) {
			resource = mapResource(rs);
		}

		disconnectDB(rs, pstmt, conn);
		return resource;
	}

	@Override
	public boolean isAvailable(long resourceId, Timestamp startTime, Timestamp endTime) throws SQLException {
		String sql = "SELECT COUNT(*) AS CNT FROM SR_RESOURCES RES "
				+ "WHERE RES.RESOURCE_ID=? AND RES.STATUS='AVAILABLE' "
				+ "AND NOT EXISTS (SELECT 1 FROM SR_RESERVATIONS RV "
				+ " WHERE RV.RESOURCE_ID=RES.RESOURCE_ID AND RV.STATUS='RESERVED' "
				+ " AND RV.START_TIME < ? AND RV.END_TIME > ?)";
		Connection conn = connectDB();
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setLong(1, resourceId);
		pstmt.setTimestamp(2, endTime);
		pstmt.setTimestamp(3, startTime);
		ResultSet rs = pstmt.executeQuery();

		boolean available = false;
		if (rs.next()) {
			available = rs.getInt("CNT") == 1;
		}

		disconnectDB(rs, pstmt, conn);
		return available;
	}

	@Override
	public long insertReservationAndPayment(String userId, long resourceId, Timestamp startTime, Timestamp endTime,
			String method) throws SQLException, StudyroomException {
		Connection conn = connectDB();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		long reservationId = 0;

		try {
			conn.setAutoCommit(false);

			String lockSql = "SELECT * FROM SR_RESOURCES WHERE RESOURCE_ID=? FOR UPDATE";
			pstmt = conn.prepareStatement(lockSql);
			pstmt.setLong(1, resourceId);
			rs = pstmt.executeQuery();
			if (!rs.next()) {
				throw new StudyroomException("예약 대상 자원을 찾을 수 없습니다.");
			}
			Resource resource = mapResource(rs);
			rs.close();
			pstmt.close();

			if (!"AVAILABLE".equals(resource.getStatus())) {
				throw new StudyroomException("현재 사용할 수 없는 자원입니다.");
			}

			String overlapSql = "SELECT COUNT(*) AS CNT FROM SR_RESERVATIONS "
					+ "WHERE RESOURCE_ID=? AND STATUS='RESERVED' AND START_TIME < ? AND END_TIME > ?";
			pstmt = conn.prepareStatement(overlapSql);
			pstmt.setLong(1, resourceId);
			pstmt.setTimestamp(2, endTime);
			pstmt.setTimestamp(3, startTime);
			rs = pstmt.executeQuery();
			rs.next();
			if (rs.getInt("CNT") > 0) {
				throw new StudyroomException("이미 예약된 시간입니다.");
			}
			rs.close();
			pstmt.close();

			long millis = endTime.getTime() - startTime.getTime();
			int hours = (int)(millis / (60 * 60 * 1000));
			if (hours <= 0) {
				throw new StudyroomException("예약 시간은 1시간 이상이어야 합니다.");
			}
			int amount = resource.getPrice() * hours;

			String reservationSql = "INSERT INTO SR_RESERVATIONS(USER_ID, RESOURCE_ID, START_TIME, END_TIME, STATUS) "
					+ "VALUES(?, ?, ?, ?, 'RESERVED')";
			pstmt = conn.prepareStatement(reservationSql, Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, userId);
			pstmt.setLong(2, resourceId);
			pstmt.setTimestamp(3, startTime);
			pstmt.setTimestamp(4, endTime);
			pstmt.executeUpdate();
			rs = pstmt.getGeneratedKeys();
			if (rs.next()) {
				reservationId = rs.getLong(1);
			}
			rs.close();
			pstmt.close();

			String paymentSql = "INSERT INTO SR_PAYMENTS(RESERVATION_ID, AMOUNT, METHOD, PAYMENT_STATUS, PAID_AT) "
					+ "VALUES(?, ?, ?, 'APPROVED', CURRENT_TIMESTAMP)";
			pstmt = conn.prepareStatement(paymentSql);
			pstmt.setLong(1, reservationId);
			pstmt.setInt(2, amount);
			pstmt.setString(3, method);
			pstmt.executeUpdate();

			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		} catch (StudyroomException e) {
			conn.rollback();
			throw e;
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			disconnectDB(rs, pstmt, conn);
		}

		return reservationId;
	}

	@Override
	public List<ReservationView> getReservationsByUser(String userId) throws SQLException {
		String sql = reservationViewSql()
				+ " WHERE RV.USER_ID=? ORDER BY RV.START_TIME DESC";
		Connection conn = connectDB();
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, userId);
		ResultSet rs = pstmt.executeQuery();
		List<ReservationView> reservations = new ArrayList<ReservationView>();
		while (rs.next()) {
			reservations.add(mapReservationView(rs));
		}
		disconnectDB(rs, pstmt, conn);
		return reservations;
	}

	@Override
	public ReservationView getReservation(long reservationId) throws SQLException {
		String sql = reservationViewSql() + " WHERE RV.RESERVATION_ID=?";
		Connection conn = connectDB();
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setLong(1, reservationId);
		ResultSet rs = pstmt.executeQuery();

		ReservationView reservation = null;
		if (rs.next()) {
			reservation = mapReservationView(rs);
		}

		disconnectDB(rs, pstmt, conn);
		return reservation;
	}

	@Override
	public List<ReservationView> getAdminReservations(String userId, String status, String type, String date)
			throws SQLException {
		String sql = reservationViewSql() + " WHERE 1=1 ";
		List<Object> params = new ArrayList<Object>();

		if (notEmpty(userId)) {
			sql += "AND (RV.USER_ID LIKE ? OR U.NAME LIKE ?) ";
			params.add("%" + userId + "%");
			params.add("%" + userId + "%");
		}
		if (notEmpty(status)) {
			sql += "AND RV.STATUS=? ";
			params.add(status);
		}
		if (notEmpty(type)) {
			sql += "AND RES.TYPE=? ";
			params.add(type);
		}
		if (notEmpty(date)) {
			sql += "AND CAST(RV.START_TIME AS DATE)=? ";
			params.add(Date.valueOf(date));
		}
		sql += "ORDER BY RV.START_TIME DESC";

		Connection conn = connectDB();
		PreparedStatement pstmt = conn.prepareStatement(sql);
		for (int i = 0; i < params.size(); i++) {
			Object param = params.get(i);
			if (param instanceof Date) {
				pstmt.setDate(i + 1, (Date)param);
			} else {
				pstmt.setString(i + 1, String.valueOf(param));
			}
		}
		ResultSet rs = pstmt.executeQuery();
		List<ReservationView> reservations = new ArrayList<ReservationView>();
		while (rs.next()) {
			reservations.add(mapReservationView(rs));
		}
		disconnectDB(rs, pstmt, conn);
		return reservations;
	}

	@Override
	public int insertCancelRequest(String userId, long reservationId, String reason)
			throws SQLException, StudyroomException {
		Connection conn = connectDB();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int result = 0;

		try {
			conn.setAutoCommit(false);

			String lockSql = "SELECT USER_ID, START_TIME, STATUS FROM SR_RESERVATIONS WHERE RESERVATION_ID=? FOR UPDATE";
			pstmt = conn.prepareStatement(lockSql);
			pstmt.setLong(1, reservationId);
			rs = pstmt.executeQuery();
			if (!rs.next()) {
				throw new StudyroomException("예약 정보를 찾을 수 없습니다.");
			}
			Timestamp startTime = rs.getTimestamp("START_TIME");
			if (!userId.equals(rs.getString("USER_ID"))) {
				throw new StudyroomException("본인 예약만 취소 요청할 수 있습니다.");
			}
			if (!"RESERVED".equals(rs.getString("STATUS"))) {
				throw new StudyroomException("예약 상태가 RESERVED일 때만 취소 요청할 수 있습니다.");
			}
			if (System.currentTimeMillis() > startTime.getTime() - (60L * 60L * 1000L)) {
				throw new StudyroomException("취소 요청은 예약 시작 1시간 전까지만 가능합니다.");
			}
			rs.close();
			pstmt.close();

			String checkSql = "SELECT COUNT(*) AS CNT FROM SR_CANCEL_REQUESTS "
					+ "WHERE RESERVATION_ID=? AND REQUEST_STATUS='REQUESTED'";
			pstmt = conn.prepareStatement(checkSql);
			pstmt.setLong(1, reservationId);
			rs = pstmt.executeQuery();
			rs.next();
			if (rs.getInt("CNT") > 0) {
				throw new StudyroomException("이미 처리 대기 중인 취소 요청이 있습니다.");
			}
			rs.close();
			pstmt.close();

			String insertSql = "INSERT INTO SR_CANCEL_REQUESTS(RESERVATION_ID, USER_ID, REQUEST_STATUS, REASON) "
					+ "VALUES(?, ?, 'REQUESTED', ?)";
			pstmt = conn.prepareStatement(insertSql);
			pstmt.setLong(1, reservationId);
			pstmt.setString(2, userId);
			pstmt.setString(3, reason);
			result = pstmt.executeUpdate();

			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		} catch (StudyroomException e) {
			conn.rollback();
			throw e;
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			disconnectDB(rs, pstmt, conn);
		}

		return result;
	}

	@Override
	public List<CancelRequest> getCancelRequests(String status) throws SQLException {
		String sql = "SELECT CR.*, U.NAME AS USER_NAME, RES.NAME AS RESOURCE_NAME "
				+ "FROM SR_CANCEL_REQUESTS CR "
				+ "JOIN SR_USERS U ON CR.USER_ID=U.USER_ID "
				+ "JOIN SR_RESERVATIONS RV ON CR.RESERVATION_ID=RV.RESERVATION_ID "
				+ "JOIN SR_RESOURCES RES ON RV.RESOURCE_ID=RES.RESOURCE_ID "
				+ "WHERE (? IS NULL OR CR.REQUEST_STATUS=?) "
				+ "ORDER BY CR.REQUESTED_AT DESC";
		Connection conn = connectDB();
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setString(1, status);
		pstmt.setString(2, status);
		ResultSet rs = pstmt.executeQuery();
		List<CancelRequest> requests = new ArrayList<CancelRequest>();
		while (rs.next()) {
			requests.add(mapCancelRequest(rs));
		}
		disconnectDB(rs, pstmt, conn);
		return requests;
	}

	@Override
	public int approveCancelRequest(long requestId) throws SQLException, StudyroomException {
		Connection conn = connectDB();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int result = 0;

		try {
			conn.setAutoCommit(false);

			String lockSql = "SELECT RESERVATION_ID, REQUEST_STATUS FROM SR_CANCEL_REQUESTS WHERE REQUEST_ID=? FOR UPDATE";
			pstmt = conn.prepareStatement(lockSql);
			pstmt.setLong(1, requestId);
			rs = pstmt.executeQuery();
			if (!rs.next()) {
				throw new StudyroomException("취소 요청을 찾을 수 없습니다.");
			}
			if (!"REQUESTED".equals(rs.getString("REQUEST_STATUS"))) {
				throw new StudyroomException("이미 처리된 취소 요청입니다.");
			}
			long reservationId = rs.getLong("RESERVATION_ID");
			rs.close();
			pstmt.close();

			pstmt = conn.prepareStatement("UPDATE SR_RESERVATIONS SET STATUS='CANCELLED' WHERE RESERVATION_ID=?");
			pstmt.setLong(1, reservationId);
			pstmt.executeUpdate();
			pstmt.close();

			pstmt = conn.prepareStatement("UPDATE SR_PAYMENTS SET PAYMENT_STATUS='REFUNDED' WHERE RESERVATION_ID=?");
			pstmt.setLong(1, reservationId);
			pstmt.executeUpdate();
			pstmt.close();

			pstmt = conn.prepareStatement("UPDATE SR_CANCEL_REQUESTS "
					+ "SET REQUEST_STATUS='APPROVED', PROCESSED_AT=CURRENT_TIMESTAMP WHERE REQUEST_ID=?");
			pstmt.setLong(1, requestId);
			result = pstmt.executeUpdate();

			conn.commit();
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		} catch (StudyroomException e) {
			conn.rollback();
			throw e;
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			disconnectDB(rs, pstmt, conn);
		}

		return result;
	}

	@Override
	public int rejectCancelRequest(long requestId) throws SQLException {
		String sql = "UPDATE SR_CANCEL_REQUESTS SET REQUEST_STATUS='REJECTED', PROCESSED_AT=CURRENT_TIMESTAMP "
				+ "WHERE REQUEST_ID=? AND REQUEST_STATUS='REQUESTED'";
		Connection conn = connectDB();
		PreparedStatement pstmt = conn.prepareStatement(sql);
		pstmt.setLong(1, requestId);
		int result = pstmt.executeUpdate();
		disconnectDB(pstmt, conn);
		return result;
	}

	@Override
	public List<RevenueStat> getRevenueStats(String mode) throws SQLException {
		String sql;
		if ("MONTH".equals(mode)) {
			sql = "SELECT FORMATDATETIME(P.PAID_AT, 'yyyy-MM') AS LABEL, "
					+ "SUM(P.AMOUNT) AS AMOUNT, COUNT(*) AS CNT "
					+ "FROM SR_PAYMENTS P WHERE P.PAYMENT_STATUS='APPROVED' "
					+ "GROUP BY FORMATDATETIME(P.PAID_AT, 'yyyy-MM') "
					+ "ORDER BY LABEL";
		} else if ("RESOURCE".equals(mode)) {
			sql = "SELECT RES.TYPE || ' ' || RES.NAME AS LABEL, SUM(P.AMOUNT) AS AMOUNT, COUNT(*) AS CNT "
					+ "FROM SR_PAYMENTS P "
					+ "JOIN SR_RESERVATIONS RV ON P.RESERVATION_ID=RV.RESERVATION_ID "
					+ "JOIN SR_RESOURCES RES ON RV.RESOURCE_ID=RES.RESOURCE_ID "
					+ "WHERE P.PAYMENT_STATUS='APPROVED' "
					+ "GROUP BY RES.TYPE, RES.NAME, RES.DISPLAY_ORDER ORDER BY RES.DISPLAY_ORDER";
		} else {
			sql = "SELECT FORMATDATETIME(P.PAID_AT, 'yyyy-MM-dd') AS LABEL, "
					+ "SUM(P.AMOUNT) AS AMOUNT, COUNT(*) AS CNT "
					+ "FROM SR_PAYMENTS P WHERE P.PAYMENT_STATUS='APPROVED' "
					+ "GROUP BY FORMATDATETIME(P.PAID_AT, 'yyyy-MM-dd') ORDER BY LABEL";
		}

		Connection conn = connectDB();
		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();
		List<RevenueStat> stats = new ArrayList<RevenueStat>();
		while (rs.next()) {
			RevenueStat stat = new RevenueStat();
			stat.setLabel(rs.getString("LABEL"));
			stat.setAmount(rs.getInt("AMOUNT"));
			stat.setCount(rs.getInt("CNT"));
			stats.add(stat);
		}
		disconnectDB(rs, pstmt, conn);
		return stats;
	}

	@Override
	public int countUsers() throws SQLException {
		return getInt("SELECT COUNT(*) FROM SR_USERS");
	}

	@Override
	public int countReservedReservations() throws SQLException {
		return getInt("SELECT COUNT(*) FROM SR_RESERVATIONS WHERE STATUS='RESERVED'");
	}

	@Override
	public int countRequestedCancels() throws SQLException {
		return getInt("SELECT COUNT(*) FROM SR_CANCEL_REQUESTS WHERE REQUEST_STATUS='REQUESTED'");
	}

	@Override
	public int getApprovedRevenue() throws SQLException {
		return getInt("SELECT COALESCE(SUM(AMOUNT), 0) FROM SR_PAYMENTS WHERE PAYMENT_STATUS='APPROVED'");
	}

	private int getInt(String sql) throws SQLException {
		Connection conn = connectDB();
		PreparedStatement pstmt = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();
		int value = 0;
		if (rs.next()) {
			value = rs.getInt(1);
		}
		disconnectDB(rs, pstmt, conn);
		return value;
	}

	private String reservationViewSql() {
		return "SELECT RV.RESERVATION_ID, RV.USER_ID, U.NAME AS USER_NAME, "
				+ "RV.RESOURCE_ID, RES.TYPE AS RESOURCE_TYPE, RES.NAME AS RESOURCE_NAME, RES.LOCATION, "
				+ "RV.START_TIME, RV.END_TIME, RV.STATUS AS RESERVATION_STATUS, "
				+ "COALESCE(P.AMOUNT, 0) AS AMOUNT, P.PAYMENT_STATUS, "
				+ "CR.REQUEST_ID AS CANCEL_REQUEST_ID, CR.REQUEST_STATUS AS CANCEL_REQUEST_STATUS "
				+ "FROM SR_RESERVATIONS RV "
				+ "JOIN SR_USERS U ON RV.USER_ID=U.USER_ID "
				+ "JOIN SR_RESOURCES RES ON RV.RESOURCE_ID=RES.RESOURCE_ID "
				+ "LEFT JOIN SR_PAYMENTS P ON RV.RESERVATION_ID=P.RESERVATION_ID "
				+ "LEFT JOIN SR_CANCEL_REQUESTS CR ON RV.RESERVATION_ID=CR.RESERVATION_ID "
				+ "AND CR.REQUEST_STATUS='REQUESTED' ";
	}

	private boolean notEmpty(String value) {
		return value != null && value.trim().length() > 0;
	}

	private User mapUser(ResultSet rs) throws SQLException {
		User user = new User();
		user.setUserId(rs.getString("USER_ID"));
		user.setPassword(rs.getString("PASSWORD"));
		user.setName(rs.getString("NAME"));
		user.setPhone(rs.getString("PHONE"));
		user.setRole(rs.getString("ROLE"));
		user.setJoinDate(rs.getTimestamp("JOIN_DATE"));
		return user;
	}

	private Resource mapResource(ResultSet rs) throws SQLException {
		Resource resource = new Resource();
		resource.setResourceId(rs.getLong("RESOURCE_ID"));
		resource.setType(rs.getString("TYPE"));
		resource.setName(rs.getString("NAME"));
		resource.setLocation(rs.getString("LOCATION"));
		resource.setPrice(rs.getInt("PRICE"));
		resource.setStatus(rs.getString("STATUS"));
		resource.setDisplayOrder(rs.getInt("DISPLAY_ORDER"));
		return resource;
	}

	private ReservationView mapReservationView(ResultSet rs) throws SQLException {
		ReservationView reservation = new ReservationView();
		reservation.setReservationId(rs.getLong("RESERVATION_ID"));
		reservation.setUserId(rs.getString("USER_ID"));
		reservation.setUserName(rs.getString("USER_NAME"));
		reservation.setResourceId(rs.getLong("RESOURCE_ID"));
		reservation.setResourceType(rs.getString("RESOURCE_TYPE"));
		reservation.setResourceName(rs.getString("RESOURCE_NAME"));
		reservation.setLocation(rs.getString("LOCATION"));
		reservation.setStartTime(rs.getTimestamp("START_TIME"));
		reservation.setEndTime(rs.getTimestamp("END_TIME"));
		reservation.setReservationStatus(rs.getString("RESERVATION_STATUS"));
		reservation.setAmount(rs.getInt("AMOUNT"));
		reservation.setPaymentStatus(rs.getString("PAYMENT_STATUS"));
		long requestId = rs.getLong("CANCEL_REQUEST_ID");
		if (!rs.wasNull()) {
			reservation.setCancelRequestId(Long.valueOf(requestId));
		}
		reservation.setCancelRequestStatus(rs.getString("CANCEL_REQUEST_STATUS"));
		return reservation;
	}

	private CancelRequest mapCancelRequest(ResultSet rs) throws SQLException {
		CancelRequest request = new CancelRequest();
		request.setRequestId(rs.getLong("REQUEST_ID"));
		request.setReservationId(rs.getLong("RESERVATION_ID"));
		request.setUserId(rs.getString("USER_ID"));
		request.setRequestStatus(rs.getString("REQUEST_STATUS"));
		request.setReason(rs.getString("REASON"));
		request.setRequestedAt(rs.getTimestamp("REQUESTED_AT"));
		request.setProcessedAt(rs.getTimestamp("PROCESSED_AT"));
		request.setUserName(rs.getString("USER_NAME"));
		request.setResourceName(rs.getString("RESOURCE_NAME"));
		return request;
	}
}
