package net.dragoncoding.groupbot.common.utils;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public abstract class MessageReader {
	private final static String MESSAGES_KEY = "messages";

	private static ResourceBundle bundle;

	public static String getMessage(String key) {
		if(bundle == null) {
			bundle = ResourceBundle.getBundle(MESSAGES_KEY);
		}
		return bundle.getString(key);
	}

	public static String format(String text, Object... arguments) {
		return MessageFormat.format(text, arguments);
	}
}
