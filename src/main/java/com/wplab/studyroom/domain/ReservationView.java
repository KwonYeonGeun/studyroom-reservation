package com.wplab.studyroom.domain;

import java.util.Date;

public class ReservationView {
	private long reservationId;
	private String userId;
	private String userName;
	private long resourceId;
	private String resourceType;
	private String resourceName;
	private String location;
	private Date startTime;
	private Date endTime;
	private String reservationStatus;
	private int amount;
	private String paymentStatus;
	private Long cancelRequestId;
	private String cancelRequestStatus;

	public ReservationView() {
	}

	public long getReservationId() {
		return reservationId;
	}

	public void setReservationId(long reservationId) {
		this.reservationId = reservationId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public long getResourceId() {
		return resourceId;
	}

	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getReservationStatus() {
		return reservationStatus;
	}

	public void setReservationStatus(String reservationStatus) {
		this.reservationStatus = reservationStatus;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public Long getCancelRequestId() {
		return cancelRequestId;
	}

	public void setCancelRequestId(Long cancelRequestId) {
		this.cancelRequestId = cancelRequestId;
	}

	public String getCancelRequestStatus() {
		return cancelRequestStatus;
	}

	public void setCancelRequestStatus(String cancelRequestStatus) {
		this.cancelRequestStatus = cancelRequestStatus;
	}
}
