package net.dragoncoding.groupbot.common.logging;

public class Status implements IStatus {

	public static final Status OK_STATUS = new Status(StatusCode.OK);

	private final StatusCode statusCode;
	private String message;
	private Throwable exception;

	public Status(StatusCode statusCode) {
		this.statusCode = statusCode;
	}

	public Status(StatusCode statusCode, String message) {
		this.statusCode = statusCode;
		this.message = message;
	}

	public Status(StatusCode statusCode, String message, Throwable exception) {
		this.statusCode = statusCode;
		this.message = message;
		this.exception = exception;
	}

	@Override
	public StatusCode getStatusCode() {
		return statusCode;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public Throwable getException() {
		return exception;
	}

	@Override
	public String toString() {
		return "Status[" +
				"statusCode=" + statusCode +
				", message='" + message + '\'' +
				", exception=" + exception +
				']';
	}
}
