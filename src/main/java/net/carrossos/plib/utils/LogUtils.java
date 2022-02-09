package net.carrossos.plib.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import org.apache.commons.io.output.WriterOutputStream;
import org.slf4j.Logger;

public class LogUtils {

	private LogUtils() {
	}

	private static class LogWriter extends Writer {

		private final Consumer<String> log;

		private final char[] buffer = new char[1024];

		private int idx = 0;

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			for (int i = 0; i < len; i++) {
				char c = cbuf[off + i];

				switch (c) {
				case '\r':
					break;
				case '\n':
					flush();

					break;
				default:
					buffer[idx++] = c;
				}
			}
		}

		@Override
		public void flush() throws IOException {
			log.accept(new String(buffer, 0, idx));
			idx = 0;
		}

		@Override
		public void close() throws IOException {
			flush();
		}

		public LogWriter(Consumer<String> log) {
			this.log = log;
		}
	}
	
	public static PrintWriter buildWriter(Consumer<String> log) {
		return new PrintWriter(new LogWriter(log));
	}

	public static OutputStream buildOutputStream(Consumer<String> log, Charset charset) {
		return new WriterOutputStream(buildWriter(log), charset);
	}

	public static PrintWriter buildDebugWriter(Logger logger) {
		return buildWriter(logger::debug);
	}

	public static OutputStream buildDebugOutputStream(Consumer<String> log, Charset charset) {
		return buildOutputStream(log, charset);
	}
}
