package com.wplab.studyroom.service;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.wplab.studyroom.common.StudyroomException;
import com.wplab.studyroom.domain.CancelRequest;
import com.wplab.studyroom.domain.ReservationView;
import com.wplab.studyroom.domain.Resource;
import com.wplab.studyroom.domain.RevenueStat;
import com.wplab.studyroom.domain.User;

public interface StudyroomDAO {
	User getUser(String userId) throws SQLException;
	int insertUser(User user) throws SQLException;
	List<Resource> getResources(String type, Timestamp startTime, Timestamp endTime) throws SQLException;
	Resource getResource(long resourceId) throws SQLException;
	boolean isAvailable(long resourceId, Timestamp startTime, Timestamp endTime) throws SQLException;
	long insertReservationAndPayment(String userId, long resourceId, Timestamp startTime, Timestamp endTime, String method)
			throws SQLException, StudyroomException;
	List<ReservationView> getReservationsByUser(String userId) throws SQLException;
	ReservationView getReservation(long reservationId) throws SQLException;
	List<ReservationView> getAdminReservations(String userId, String status, String type, String date) throws SQLException;
	int insertCancelRequest(String userId, long reservationId, String reason) throws SQLException, StudyroomException;
	List<CancelRequest> getCancelRequests(String status) throws SQLException;
	int approveCancelRequest(long requestId) throws SQLException, StudyroomException;
	int rejectCancelRequest(long requestId) throws SQLException;
	List<RevenueStat> getRevenueStats(String mode) throws SQLException;
	int countUsers() throws SQLException;
	int countReservedReservations() throws SQLException;
	int countRequestedCancels() throws SQLException;
	int getApprovedRevenue() throws SQLException;
}
