package net.carrossos.plib.utils.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.carrossos.plib.utils.StringUtils;

public class DigestUtils {
	private DigestUtils() {
	}

	public static String md5(byte[] buf) {
		try {
			return StringUtils.bytesToHex(MessageDigest.getInstance("MD5").digest(buf));
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError("Missing algorithm", e);
		}
	}

	public static String sha1(byte[] buf) {
		try {
			return StringUtils.bytesToHex(MessageDigest.getInstance("SHA-1").digest(buf));
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError("Missing algorithm", e);
		}
	}
}
