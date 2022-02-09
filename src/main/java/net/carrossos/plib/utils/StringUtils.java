package net.carrossos.plib.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Objects;

public class StringUtils {

	// "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

	private static final String ALPHA_NUM = "abcdefghijklmnopqrstuvwxyz1234567890";

	private static final String HEX = "abcdef1234567890";

	private static final String SPECIAL = "!?@.*";

	private static final SecureRandom RANDOM = new SecureRandom();

	private StringUtils() {
	}

	public static String bytesToHex(byte[] buffer) {
		StringBuilder builder = new StringBuilder(buffer.length * 2);

		for (byte b : buffer) {
			String hex = Integer.toHexString(0xFF & b);

			if (hex.length() == 1) {
				builder.append('0');
			}

			builder.append(hex);
		}

		return builder.toString();
	}

	public static String printStacktrace(Throwable t) {
		ByteArrayOutputStream output = new ByteArrayOutputStream(200);

		try (PrintStream stream = new PrintStream(output, true, StandardCharsets.UTF_8)) {
			t.printStackTrace(stream);
		}

		return output.toString(StandardCharsets.UTF_8);
	}

	public static String randomAlphaNum(int length) {
		return randomNum(RANDOM, ALPHA_NUM, length);
	}

	public static String randomHex(int length) {
		return randomNum(RANDOM, HEX, length);
	}

	public static String randomPassword(SecureRandom random, int length) {
		return randomNum(random, ALPHA_NUM + SPECIAL, length);
	}

	public static String randomNum(SecureRandom random, CharSequence characters, int length) {
		return random.ints(0, characters.length()).limit(length)
				.collect(StringBuilder::new, (b, i) -> b.append(characters.charAt(i)), StringBuilder::append)
				.toString();
	}

	public static String readAll(InputStream input) throws IOException {
		return readAll(input, StandardCharsets.UTF_8);
	}

	public static String readAll(InputStream input, Charset charset) throws IOException {
		return new String(input.readAllBytes(), charset);
	}

	public static String requireNotEmpty(String str) {
		if (Objects.requireNonNull(str).isEmpty()) {
			throw new IllegalArgumentException("Empty string!");
		}

		return str;
	}

	public static String requireNotEmptyOrElse(String str, String def) {
		if (str == null || str.isEmpty()) {
			return def;
		} else {
			return str;
		}
	}
}
