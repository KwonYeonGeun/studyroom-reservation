package com.wplab.studyroom.common;

public class StudyroomException extends Exception {
	private static final long serialVersionUID = 1L;

	public StudyroomException(String message) {
		super(message);
	}

	public StudyroomException(String message, Throwable cause) {
		super(message, cause);
	}
}
