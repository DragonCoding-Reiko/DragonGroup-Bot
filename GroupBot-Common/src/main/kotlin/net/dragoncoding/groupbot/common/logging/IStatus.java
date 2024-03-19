package net.dragoncoding.groupbot.common.logging;

public interface IStatus {
	StatusCode getStatusCode();

	String getMessage();

	Throwable getException();

	default boolean isNotOk() {
		return !isOk();
	}

	default boolean isOk() {
		return getStatusCode().isOk();
	}
}
