package net.carrossos.plib.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

public class CompressUtils {

	private CompressUtils() {
	}

	public static byte[] compressBZ2(String str) {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();

			try (PrintStream ouput = new PrintStream(
					new BZip2CompressorOutputStream(bytes, BZip2CompressorOutputStream.MAX_BLOCKSIZE), false,
					StandardCharsets.UTF_8)) {
				ouput.print(str);
			}

			return bytes.toByteArray();
		} catch (IOException e) {
			throw new UncheckedIOException("Unexpected compression error", e);
		}
	}

	public static String decompressBZ2(byte[] bytes) {
		try {
			try (InputStream input = new BZip2CompressorInputStream(new ByteArrayInputStream(bytes))) {
				return new String(input.readAllBytes(), StandardCharsets.UTF_8);
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Unexpected compression error", e);
		}
	}
}
