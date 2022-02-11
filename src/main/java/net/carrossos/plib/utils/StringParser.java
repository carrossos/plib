package net.carrossos.plib.utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.IntPredicate;

public class StringParser {

	private final char[] string;

	private int pos = 0;

	private final Deque<IntPredicate> predicates = new ArrayDeque<>();

	private boolean consume0(CharSequence chars, boolean fail) {
		int start = pos;

		for (int i = 0; i < chars.length(); i++) {
			if (pos >= string.length) {
				if (fail) {
					throw new IllegalStateException(String.format(
							"Parsing failed at char %d, expected to read '%s' from char %d but string is too short: '%s'",
							pos, chars, start, new String(string, start, i)));
				} else {
					return false;
				}
			}

			if (string[pos] == chars.charAt(i)) {
				pos++;
			} else {
				if (fail) {
					throw new IllegalStateException(
							String.format("Parsing failed at char %d, expected to read '%s' from char %d but got '%s'",
									pos, chars, start, new String(string, start, i)));
				} else {
					pos = start;

					return false;
				}
			}
		}

		return true;
	}

	public void consume(CharSequence chars) {
		consume0(chars, true);
	}

	public String readUntil(char ch) {
		return readUntil(c -> c == ch);
	}

	public String readUntil(IntPredicate until) {
		StringBuilder builder = new StringBuilder();

		until(until, () -> {
			builder.append(string[pos]);
			pos++;
		});

		return builder.toString();
	}

	public String remaining() {
		String remaining = new String(string, pos, string.length - pos);

		pos = string.length;

		return remaining;
	}

	public boolean tryConsume(CharSequence chars) {
		return consume0(chars, false);
	}

//	public String readUntil(IntPredicate until) {
//		StringBuilder builder = new StringBuilder();
//
//		while (pos < string.length) {
//			char ch = string[pos];
//
//			if (until.test(ch)) {
//				return builder.toString();
//			} else {
//				builder.append(ch);
//				pos++;
//			}
//		}
//
//		return builder.toString();
//	}

	public void until(IntPredicate predicate, Runnable runnable) {
		predicates.addLast(predicate);

		while (pos < string.length && !predicates.stream().anyMatch(p -> p.test(string[pos]))) {
			runnable.run();
		}

		predicates.removeLast();
	}

	public StringParser(String string) {
		this.string = string.toCharArray();
	}

}
