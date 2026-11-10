package com.wplab.studyroom.service.impl;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.wplab.studyroom.common.StudyroomException;
import com.wplab.studyroom.domain.CancelRequest;
import com.wplab.studyroom.domain.ReservationView;
import com.wplab.studyroom.domain.Resource;
import com.wplab.studyroom.domain.RevenueStat;
import com.wplab.studyroom.domain.User;
import com.wplab.studyroom.service.StudyroomDAO;
import com.wplab.studyroom.service.StudyroomService;

public class StudyroomServiceImpl implements StudyroomService {
	private StudyroomDAO studyroomDao = null;

	public StudyroomServiceImpl(StudyroomDAO studyroomDao) {
		this.studyroomDao = studyroomDao;
	}

	@Override
	public User authenticateUser(String userId, String password) throws StudyroomException {
		if (isEmpty(userId) || isEmpty(password)) {
			throw new StudyroomException("아이디와 비밀번호를 입력하여야 합니다.");
		}

		User user = null;
		try {
			user = studyroomDao.getUser(userId);
		} catch (SQLException e) {
			throw new StudyroomException("DB 작업 중 오류가 발생했습니다.", e);
		}

		if (user == null) {
			throw new StudyroomException("가입되지 않은 사용자입니다.");
		}
		if (!password.equals(user.getPassword())) {
			throw new StudyroomException("비밀번호가 올바르지 않습니다.");
		}
		return user;
	}

	@Override
	public void registerUser(User user) throws StudyroomException {
		if (isEmpty(user.getUserId()) || isEmpty(user.getPassword()) || isEmpty(user.getName())) {
			throw new StudyroomException("아이디, 비밀번호, 이름은 필수입니다.");
		}
		try {
			User oldUser = studyroomDao.getUser(user.getUserId());
			if (oldUser != null) {
				throw new StudyroomException("이미 가입된 아이디입니다.");
			}
			studyroomDao.insertUser(user);
		} catch (SQLException e) {
			throw new StudyroomException("회원가입 중 DB 오류가 발생했습니다.", e);
		}
	}

	@Override
	public List<Resource> getResources(String type, Timestamp startTime, Timestamp endTime) throws StudyroomException {
		try {
			return studyroomDao.getResources(cleanType(type), startTime, endTime);
		} catch (SQLException e) {
			throw new StudyroomException("자원 목록 조회 중 DB 오류가 발생했습니다.", e);
		}
	}

	@Override
	public Resource getResource(long resourceId) throws StudyroomException {
		try {
			Resource resource = studyroomDao.getResource(resourceId);
			if (resource == null) {
				throw new StudyroomException("예약 대상을 찾을 수 없습니다.");
			}
			return resource;
		} catch (SQLException e) {
			throw new StudyroomException("자원 조회 중 DB 오류가 발생했습니다.", e);
		}
	}

	@Override
	public boolean isAvailable(long resourceId, Timestamp startTime, Timestamp endTime) throws StudyroomException {
		try {
			return studyroomDao.isAvailable(resourceId, startTime, endTime);
		} catch (SQLException e) {
			throw new StudyroomException("예약 가능 여부 확인 중 DB 오류가 발생했습니다.", e);
		}
	}

	@Override
	public long approvePayment(String userId, long resourceId, Timestamp startTime, Timestamp endTime, String method)
			throws StudyroomException {
		if (isEmpty(userId)) {
			throw new StudyroomException("로그인이 필요합니다.");
		}
		if (isEmpty(method)) {
			method = "CARD";
		}
		try {
			return studyroomDao.insertReservationAndPayment(userId, resourceId, startTime, endTime, method);
		} catch (SQLException e) {
			throw new StudyroomException("예약 결제 처리 중 DB 오류가 발생했습니다.", e);
		}
	}

	@Override
	public List<ReservationView> getMyReservations(String userId) throws StudyroomException {
		try {
			return studyroomDao.getReservationsByUser(userId);
		} catch (SQLException e) {
			throw new StudyroomException("예약 목록 조회 중 DB 오류가 발생했습니다.", e);
		}
	}

	@Override
	public ReservationView getReservation(long reservationId) throws StudyroomException {
		try {
			ReservationView reservation = studyroomDao.getReservation(reservationId);
			if (reservation == null) {
				throw new StudyroomException("예약 정보를 찾을 수 없습니다.");
			}
			return reservation;
		} catch (SQLException e) {
			throw new StudyroomException("예약 조회 중 DB 오류가 발생했습니다.", e);
		}
	}

	@Override
	public List<ReservationView> getAdminReservations(String userId, String status, String type, String date)
			throws StudyroomException {
		try {
			return studyroomDao.getAdminReservations(userId, status, cleanType(type), date);
		} catch (SQLException e) {
			throw new StudyroomException("관리자 예약 조회 중 DB 오류가 발생했습니다.", e);
		}
	}

	@Override
	public void requestCancel(String userId, long reservationId, String reason) throws StudyroomException {
		try {
			studyroomDao.insertCancelRequest(userId, reservationId, reason);
		} catch (SQLException e) {
			throw new StudyroomException("취소 요청 등록 중 DB 오류가 발생했습니다.", e);
		}
	}

	@Override
	public List<CancelRequest> getCancelRequests(String status) throws StudyroomException {
		try {
			return studyroomDao.getCancelRequests(isEmpty(status) ? null : status);
		} catch (SQLException e) {
			throw new StudyroomException("취소 요청 조회 중 DB 오류가 발생했습니다.", e);
		}
	}

	@Override
	public void approveCancel(long requestId) throws StudyroomException {
		try {
			if (studyroomDao.approveCancelRequest(requestId) == 0) {
				throw new StudyroomException("승인할 취소 요청이 없습니다.");
			}
		} catch (SQLException e) {
			throw new StudyroomException("취소 승인 중 DB 오류가 발생했습니다.", e);
		}
	}

	@Override
	public void rejectCancel(long requestId) throws StudyroomException {
		try {
			if (studyroomDao.rejectCancelRequest(requestId) == 0) {
				throw new StudyroomException("반려할 취소 요청이 없습니다.");
			}
		} catch (SQLException e) {
			throw new StudyroomException("취소 반려 중 DB 오류가 발생했습니다.", e);
		}
	}

	@Override
	public List<RevenueStat> getRevenueStats(String mode) throws StudyroomException {
		try {
			return studyroomDao.getRevenueStats(mode);
		} catch (SQLException e) {
			throw new StudyroomException("매출 통계 조회 중 DB 오류가 발생했습니다.", e);
		}
	}

	@Override
	public int countUsers() throws StudyroomException {
		try {
			return studyroomDao.countUsers();
		} catch (SQLException e) {
			throw new StudyroomException("회원 수 조회 중 DB 오류가 발생했습니다.", e);
		}
	}

	@Override
	public int countReservedReservations() throws StudyroomException {
		try {
			return studyroomDao.countReservedReservations();
		} catch (SQLException e) {
			throw new StudyroomException("예약 수 조회 중 DB 오류가 발생했습니다.", e);
		}
	}

	@Override
	public int countRequestedCancels() throws StudyroomException {
		try {
			return studyroomDao.countRequestedCancels();
		} catch (SQLException e) {
			throw new StudyroomException("취소 요청 수 조회 중 DB 오류가 발생했습니다.", e);
		}
	}

	@Override
	public int getApprovedRevenue() throws StudyroomException {
		try {
			return studyroomDao.getApprovedRevenue();
		} catch (SQLException e) {
			throw new StudyroomException("매출 합계 조회 중 DB 오류가 발생했습니다.", e);
		}
	}

	private String cleanType(String type) {
		if ("SEAT".equals(type) || "LOCKER".equals(type)) {
			return type;
		}
		return null;
	}

	private boolean isEmpty(String value) {
		return value == null || value.trim().length() == 0;
	}
}
