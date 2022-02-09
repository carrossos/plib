package net.carrossos.plib.net;

public class CIDRv4 extends IPv4 implements Comparable<CIDRv4> {

	public static CIDRv4 ANY = CIDRv4.fromString("0.0.0.0/0");
	
	public static CIDRv4[] PRIVATE_RANGES = { CIDRv4.fromIPString("10.0.0.0/8"), CIDRv4.fromIPString("172.16.0.0/12"),
			CIDRv4.fromIPString("192.168.0.0/16") };

	public static CIDRv4[] LOOPBACK_RANGE = { CIDRv4.fromIPString("127.0.0.0/8") };

	public static CIDRv4[] LINK_LOCAL = { CIDRv4.fromIPString("169.254.0.0/16") };

	private final IPv4 mask;

	private <R extends IPv4> R checkSameNetwork(R result) {
		if (!sameNetwork(result)) {
			throw new IllegalArgumentException(
					String.format("Invalid offset: '%s' will move to another network than '%s'", result, this));
		}

		return result;
	}

	public CIDRv4 add(int add) {
		return checkSameNetwork(new CIDRv4(toInteger() + add, mask));
	}

	public CIDRv4 addByteA(int a) {
		int[] bytes = toBytes();
		bytes[0] += a;

		return fromBytes(bytes, getMask().getBlock());
	}

	public CIDRv4 addByteB(int b) {
		int[] bytes = toBytes();
		bytes[1] += b;

		return fromBytes(bytes, getMask().getBlock());
	}

	public CIDRv4 addByteC(int c) {
		int[] bytes = toBytes();
		bytes[2] += c;

		return fromBytes(bytes, getMask().getBlock());
	}

	@Override
	public int compareTo(CIDRv4 o) {
		int compare = Integer.compareUnsigned(toInteger(), o.toInteger());

		if (compare != 0) {
			return compare;
		}

		return Integer.compareUnsigned(mask.toInteger(), o.mask.toInteger());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CIDRv4 other = (CIDRv4) obj;
		if (mask == null) {
			if (other.mask != null) {
				return false;
			}
		} else if (!mask.equals(other.mask)) {
			return false;
		}
		return true;
	}

	public boolean equalsIgnoreMask(Object obj) {
		return super.equals(obj);
	}

	public CIDRv4 getBroadcast() {
		return new CIDRv4(toInteger() | ~getMask().toInteger(), mask);
	}

	public int getDistance(IPv4 ip) {
		if (!sameNetwork(ip)) {
			throw new IllegalArgumentException(String.format("%s is not in subnet %s", ip, this));
		}

		return ip.toInteger() - toInteger();
	}

	public IPv4 getMask() {
		return mask;
	}

	public CIDRv4 getNetwork() {
		return new CIDRv4(toInteger() & getMask().toInteger(), mask);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (mask == null ? 0 : mask.hashCode());
		return result;
	}

	public boolean isBroadcast() {
		return equals(getBroadcast());
	}

	public boolean isNetwork() {
		return equals(getNetwork());
	}

	public boolean isSingleHost() {
		return ~mask.toInteger() == 0;
	}

	public CIDRv4 replaceHost(CIDRv4 host) {
		return checkSameNetwork(new CIDRv4(
				getNetwork().toInteger() | ~getMask().toInteger() & host.getNetwork().toInteger(), host.mask));
	}

	public CIDRv4 replaceNetwork(CIDRv4 network) {
		if (!network.isNetwork()) {
			throw new IllegalArgumentException("Argument is not a network: " + network);
		}

		return new CIDRv4(toInteger() & ~network.getMask().toInteger() | network.toInteger(), mask);
	}

	public boolean sameNetwork(IPv4 ip) {
		return (getMask().toInteger() & ip.toInteger()) == getNetwork().toInteger();
	}

	public CIDRv4 setFromEnd(int set) {
		return checkSameNetwork(new CIDRv4(getBroadcast().toInteger() - set, mask));
	}

	public CIDRv4 setFromStart(int set) {
		return checkSameNetwork(new CIDRv4(getNetwork().toInteger() + set, mask));
	}

	public CIDRv4 setMask(int block) {
		return new CIDRv4(toInteger(), block);
	}

	public IPv4 toIP() {
		return new IPv4(toInteger());
	}

	public String toIPString() {
		return super.toString();
	}

	@Override
	public String toString() {
		return super.toString() + "/" + mask.getBlock();
	}

	private CIDRv4(int ip, int block) {
		super(ip);

		this.mask = IPv4.fromBlock(block);
	}

	private CIDRv4(int ip, IPv4 mask) {
		super(ip);

		this.mask = mask;
	}

	private static CIDRv4 fromString0(String str, boolean strict) {
		String[] split = str.split("/");
		int block;

		if (split.length > 2) {
			throw new IllegalArgumentException("Invalid CIDR IP: " + str);
		} else if (split.length == 2) {
			try {
				block = Integer.valueOf(split[1]);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid CIDR IP: " + str, e);
			}
		} else {
			if (strict) {
				throw new IllegalArgumentException("Invalid CIDR IP: " + str);
			} else {
				block = 32;
			}
		}

		return IPv4.fromString(ip -> new CIDRv4(ip, block), split[0]);
	}

	public static CIDRv4 cover(IPv4 a, IPv4 b) {
		for (int i = BITS - 1; i >= 0; i--) {
			if ((a.toInteger() & 1 << i) != (b.toInteger() & 1 << i)) {
				return new CIDRv4(a.toInteger(), BITS - i - 1).getNetwork();
			}
		}

		return new CIDRv4(a.toInteger(), BITS);
	}

	public static CIDRv4 fromBytes(int a, int b, int c, int d, int block) {
		return fromBytes(new int[] { a, b, c, d }, block);
	}

	public static CIDRv4 fromBytes(int[] bytes, int block) {
		return IPv4.fromBytes(ip -> new CIDRv4(ip, block), bytes);
	}

	public static CIDRv4 fromIP(IPv4 ip, int block) {
		return new CIDRv4(ip.toInteger(), block);
	}

	public static CIDRv4 fromIPString(String str) {
		return fromString0(str, false);
	}

	public static CIDRv4 fromString(String str) {
		return fromString0(str, true);
	}
}
