package net.carrossos.plib.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPv4 {

	public static final int BITS = 32;

	private static final Pattern IP = Pattern.compile("(\\d+)\\s*\\.\\s*(\\d+)\\s*\\.\\s*(\\d+)\\s*\\.\\s*(\\d+)");

	private final int ip;

	private int countLeftBits() {
		return Integer.numberOfLeadingZeros(~ip);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		IPv4 other = (IPv4) obj;
		if (ip != other.ip) {
			return false;
		}
		return true;
	}

	public int getBlock() {
		if (!isMask()) {
			throw new IllegalStateException("Not a mask: " + this);
		}

		return countLeftBits();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ip;
		return result;
	}

	public boolean inRange(Collection<CIDRv4> ranges) {
		for (CIDRv4 range : ranges) {
			if (range.sameNetwork(this)) {
				return true;
			}
		}

		return false;
	}
	
	public boolean inRange(CIDRv4... ranges) {
		for (CIDRv4 range : ranges) {
			if (range.sameNetwork(this)) {
				return true;
			}
		}

		return false;
	}

	public boolean isMask() {
		return ip == 0 || (blocktoMask(countLeftBits()) ^ ip) == 0;
	}

	public boolean isPublic() {
		return !inRange(CIDRv4.PRIVATE_RANGES) && !inRange(CIDRv4.LOOPBACK_RANGE) && !inRange(CIDRv4.LINK_LOCAL);
	}

	public int[] toBytes() {
		int[] result = new int[4];

		for (int i = 3; i >= 0; i--) {
			result[3 - i] = ip >> 8 * i & 0xFF;
		}

		return result;
	}

	public int toInteger() {
		return ip;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(18);

		for (int shift = 24; shift > 0; shift -= 8) {
			builder.append(Integer.toString(ip >> shift & 0xFF));
			builder.append('.');
		}

		builder.append(Integer.toString(ip & 0xFF));

		return builder.toString();
	}

	IPv4(int ip) {
		this.ip = ip;
	}

	private static int blocktoMask(int block) {
		if (block == 0) {
			return 0;
		} else {
			return 0xFFFFFFFF << BITS - block;
		}
	}

	static <R> R fromBytes(IntFunction<R> constructor, int[] bytes) {
		int ip = 0;

		for (int n = 1; n <= 4; n++) {
			int value = bytes[n - 1];

			if (value < 0 || value > 255) {
				throw new IllegalArgumentException("Invalid byte: " + value);
			}

			ip += value << (4 - n) * 8;
		}

		return constructor.apply(ip);
	}

	static <R> R fromString(IntFunction<R> constructor, String str) {
		Matcher matcher = IP.matcher(str);

		if (!matcher.matches()) {
			throw new IllegalArgumentException("Invalid IPv4: '" + str + "'");
		}

		int ip = 0;

		for (int n = 1; n <= 4; n++) {
			int value = Integer.parseInt(matcher.group(n));

			if (value < 0 || value > 255) {
				throw new IllegalArgumentException("Invalid byte: " + value);
			}

			ip += value << (4 - n) * 8;
		}

		return constructor.apply(ip);
	}

	public static IPv4 fromAddress(InetAddress address) {
		byte[] array = address.getAddress();

		if (array.length != 4) {
			throw new IllegalArgumentException("Not IPv4 address: " + address);
		}

		return fromBytes(array[0] & 0xFF, array[1] & 0xFF, array[2] & 0xFF, array[3] & 0xFF);
	}

	public static IPv4 fromBlock(int block) {
		return new IPv4(blocktoMask(block));
	}

	public static IPv4 fromBytes(int a, int b, int c, int d) {
		return fromBytes(new int[] { a, b, c, d });
	}

	public static IPv4 fromBytes(int[] bytes) {
		return fromBytes(IPv4::new, bytes);
	}

	public static IPv4 fromInteger(int ip) {
		return new IPv4(ip);
	}

	public static IPv4 fromSocket(InetSocketAddress address) {
		return fromAddress(address.getAddress());
	}

	public static IPv4 fromString(String str) {
		return fromString(IPv4::new, str);
	}
}
