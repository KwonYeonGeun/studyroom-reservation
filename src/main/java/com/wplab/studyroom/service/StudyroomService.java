package com.wplab.studyroom.service;

import java.sql.Timestamp;
import java.util.List;

import com.wplab.studyroom.common.StudyroomException;
import com.wplab.studyroom.domain.CancelRequest;
import com.wplab.studyroom.domain.ReservationView;
import com.wplab.studyroom.domain.Resource;
import com.wplab.studyroom.domain.RevenueStat;
import com.wplab.studyroom.domain.User;

public interface StudyroomService {
	User authenticateUser(String userId, String password) throws StudyroomException;
	void registerUser(User user) throws StudyroomException;
	List<Resource> getResources(String type, Timestamp startTime, Timestamp endTime) throws StudyroomException;
	Resource getResource(long resourceId) throws StudyroomException;
	boolean isAvailable(long resourceId, Timestamp startTime, Timestamp endTime) throws StudyroomException;
	long approvePayment(String userId, long resourceId, Timestamp startTime, Timestamp endTime, String method)
			throws StudyroomException;
	List<ReservationView> getMyReservations(String userId) throws StudyroomException;
	ReservationView getReservation(long reservationId) throws StudyroomException;
	List<ReservationView> getAdminReservations(String userId, String status, String type, String date)
			throws StudyroomException;
	void requestCancel(String userId, long reservationId, String reason) throws StudyroomException;
	List<CancelRequest> getCancelRequests(String status) throws StudyroomException;
	void approveCancel(long requestId) throws StudyroomException;
	void rejectCancel(long requestId) throws StudyroomException;
	List<RevenueStat> getRevenueStats(String mode) throws StudyroomException;
	int countUsers() throws StudyroomException;
	int countReservedReservations() throws StudyroomException;
	int countRequestedCancels() throws StudyroomException;
	int getApprovedRevenue() throws StudyroomException;
}
