package net.dragoncoding.groupbot.common.logging;

public enum StatusCode {
	OK,
	INFO,
	WARNING,
	ERROR;

	public boolean isOk() {
		return OK.equals(this) || INFO.equals(this);
	}
}
