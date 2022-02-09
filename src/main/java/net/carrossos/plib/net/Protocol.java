package net.carrossos.plib.net;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Protocol implements Comparable<Protocol> {

	public static enum Type {
		TCP, UDP, ICMP;
	}

	private static final Pattern REGEX = Pattern.compile("([a-z]+)(?:/(\\d+))?(?:\\-(\\d+))?");

	private final Type type;

	private final int portMin;

	private final int portMax;

	public boolean contains(Protocol protocol) {
		return type.equals(protocol.getType()) && portMin <= protocol.getPortMin() && portMax >= protocol.getPortMax();
	}

	@Override
	public int compareTo(Protocol o) {
		int compare = type.compareTo(o.type);

		if (compare != 0) {
			return compare;
		}

		compare = Integer.compare(portMin, o.portMin);

		if (compare != 0) {
			return compare;
		}

		return Integer.compare(portMax, o.portMax);
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
		Protocol other = (Protocol) obj;
		if (portMax != other.portMax) {
			return false;
		}
		if (portMin != other.portMin) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}

	public int getPort() {
		if (isRange()) {
			throw new IllegalStateException("Port is a range");
		}

		return portMin;
	}

	public int getPortMax() {
		return portMax;
	}

	public int getPortMin() {
		return portMin;
	}

	public Type getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + portMax;
		result = prime * result + portMin;
		result = prime * result + (type == null ? 0 : type.hashCode());
		return result;
	}

	public boolean isRange() {
		return portMax != portMin;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(5);

		builder.append(type.toString().toLowerCase());

		if (type != Type.ICMP) {
			builder.append('/');
			builder.append(portMin);

			if (portMin != portMax) {
				builder.append('-');
				builder.append(portMax);
			}
		}

		return builder.toString();
	}

	private Protocol(Type type, int portMin, int portMax) {
		this.type = Objects.requireNonNull(type);
		this.portMin = portMin;
		this.portMax = portMax;

		if (type == Type.ICMP) {
			if (portMin != -1 || portMax != -1) {
				throw new IllegalArgumentException("ICMP do not support ports");
			}
		} else {
			if (portMax < portMin) {
				throw new IllegalArgumentException("Port range must start with smaller port");
			}

			if (portMin <= 0 || portMax > 65536) {
				throw new IllegalArgumentException("Invalid port: " + portMin);
			}

			if (portMax <= 0 || portMax > 65536) {
				throw new IllegalArgumentException("Invalid port: " + portMin);
			}
		}
	}

	public static Protocol valueOf(String str) {
		Matcher match = REGEX.matcher(str);

		if (!match.matches()) {
			throw new IllegalArgumentException("Invalid protocol: " + str);
		}

		Type type;
		try {
			type = Type.valueOf(match.group(1).toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid type: " + str, e);
		}

		int portMin;
		if (match.group(2) == null) {
			portMin = -1;
		} else {
			try {
				portMin = Integer.valueOf(match.group(2));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid port number: " + str, e);
			}
		}

		int portMax;
		if (match.group(3) == null) {
			portMax = portMin;
		} else {
			try {
				portMax = Integer.valueOf(match.group(3));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid port number: " + str, e);
			}
		}

		return new Protocol(type, portMin, portMax);
	}

}
