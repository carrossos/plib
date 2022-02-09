package net.carrossos.plib.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class IPv4Test {

	@ParameterizedTest
	@CsvSource({ "192.168.0.5/24, 5, 192.168.0.10", "10.0.0.0/22, 255, 10.0.0.255" })
	void testAdd(String ip, int add, String result) {
		assertEquals(result, CIDRv4.fromString(ip).add(add).toIPString());
	}

	@ParameterizedTest
	@CsvSource({ "255.0.0.0, true", "255.255.252.0, true", "255.255.255.255, true", "255.255.255.254, true",
			"255.248.0.0, true", "255.0.2.0, false", "255.253.252.0, false", "1.2.3.4, false", "255.255.255.253, false",
			"255.248.0.1, false" })
	void testBlock(String ip, boolean expect) {
		if (expect) {
			assertEquals(ip, IPv4.fromBlock(IPv4.fromString(ip).getBlock()).toString());
		} else {
			assertThrows(IllegalStateException.class, () -> IPv4.fromBlock(IPv4.fromString(ip).getBlock()).toString());
		}
	}

	@ParameterizedTest
	@CsvSource({ "192.33.23.255/24, true", "192.33.255.255/16, true", "192.169.255.255/15, true",
			"192.33.23.254/24, false", "192.33.1.255/16, false", "192.168.1.15/15, false", "192.168.255.255/15, false",
			"192.168.1.3/30,true" })
	void testBroadcast(String ip, boolean expect) {
		assertEquals(expect, CIDRv4.fromString(ip).isBroadcast());
	}

	@ParameterizedTest
	@CsvSource({ "1, 2, 3, 4, 1.2.3.4", "10, 0, 100, 200, 10.0.100.200" })
	void testBytes(int a, int b, int c, int d, String expected) {
		assertEquals(expected, IPv4.fromBytes(new int[] { a, b, c, d }).toString());
	}

	@ParameterizedTest
	@ValueSource(strings = { "192.33.23.4", "0.0.0.0", "10.1.2.3", "8.8.8.8", "1.2.3.4", "0.0.1.0", "8.4.132.250" })
	void testBytes(String ip) {
		assertEquals(ip, IPv4.fromBytes(IPv4.fromString(ip).toBytes()).toString());
	}

	@ParameterizedTest
	@CsvSource({ "10.25.1.96/22, 10.25.0.63/22, 1", "10.25.0.31/22, 10.25.0.32/22, -1",
			"10.25.0.32/22, 10.25.0.32/22, 0", "1.2.3.4/0, 250.10.52.2/0, -1" })
	void testCompare(String a, String b, int expect) {
		assertEquals(expect, CIDRv4.fromString(a).compareTo(CIDRv4.fromString(b)));
	}

	@ParameterizedTest
	@CsvSource({ "10.0.0.1, 10.0.0.200, 10.0.0.0/24", "10.0.1.200, 10.0.0.1, 10.0.0.0/23",
			"10.10.10.10, 10.10.10.10, 10.10.10.10/32", "10.0.0.0, 250.0.0.0, 0.0.0.0/0",
			"1.0.0.0, 126.0.0.0, 0.0.0.0/1" })
	void testCover(String a, String b, String result) {
		assertEquals(result, CIDRv4.cover(IPv4.fromString(a), IPv4.fromString(b)).toString());
	}

	@ParameterizedTest
	@CsvSource({ "10.0.2.0/24, 10.0.2.15, 15", "10.0.0.0/23, 10.0.1.15, 271" })
	void testDistance(String subnet, String ip, int distance) {
		assertEquals(distance, CIDRv4.fromString(subnet).getDistance(IPv4.fromString(ip)));
	}

	@ParameterizedTest
	@CsvSource({ "10.0.0.250/24, 10.0.0.0/24", "125.0.4.4/0, 0.0.0.0/0", "192.168.127.255/16, 192.168.0.0/16",
			"1.2.3.4/32, 1.2.3.4/32" })
	void testGetNetwork(String ip, String network) {
		assertEquals(network, CIDRv4.fromString(ip).getNetwork().toString());
	}

	@ParameterizedTest
	@CsvSource({ "10.0.0.0/24, 10.0.0.0, true", "10.0.0.0/24, 10.0.0.1, true", "10.0.0.0/24, 10.0.1.0, false",
			"10.0.0.0/16, 10.0.45.80, true", "10.0.0.0/9, 10.129.45.80, false", "192.168.1.0/30, 192.168.1.4, false",
			"192.168.1.0/30, 192.168.1.2, true" })
	void testInNetwork(String network, String ip, boolean expect) {
		assertEquals(expect, CIDRv4.fromString(network).sameNetwork(IPv4.fromString(ip)));
	}

	@ParameterizedTest
	@ValueSource(strings = { "192.33.23.4", "0.0.0.0", "10.1.2.3", "8.8.8.8", "1.2.3.4", "0.0.1.0", "8.4.132.250" })
	void testIP(String ip) {
		assertEquals(ip, IPv4.fromString(ip).toString());
	}

	@ParameterizedTest
	@ValueSource(strings = { "255.0.0.0" })
	void testMask(String ip) {
		assert IPv4.fromString(ip).isMask();
	}

	@ParameterizedTest
	@CsvSource({ "0.0.0.0, 0", "255.0.0.0, 8", "255.255.255.255, 32", "255.255.255.0, 24", "255.255.255.252, 30",
			"255.255.252.0, 22" })
	void testMask(String ip, int block) {
		assertEquals(block, IPv4.fromString(ip).getBlock());
		assertEquals(ip, IPv4.fromBlock(block).toString());
	}

	@ParameterizedTest
	@CsvSource({ "192.33.23.0/24,true", "192.33.0.0/16, true", "192.168.0.0/15, true", "192.33.23.1/24, false",
			"192.33.1.0/16, false", "192.168.1.15/15, false", "192.168.0.255/15, false", "192.168.1.4/30, true",
			"192.168.1.4/29, false", "192.168.1.2/32, true" })
	void testNetwork(String ip, boolean expect) {
		assertEquals(expect, CIDRv4.fromString(ip).isNetwork());
	}

	@ParameterizedTest
	@CsvSource({ "192.168.1.2, false", "10.0.1.2, false", "172.16.204.120, false", "192.68.1.3, true",
			"127.0.0.1, false", "127.0.3.1, false", "127.100.3.1, false", "11.0.1.2, true" })
	void testPublic(String ip, boolean expect) {
		assertEquals(expect, IPv4.fromString(ip).isPublic());
	}

	@ParameterizedTest
	@CsvSource({ "10.120.0.0/16, 0.0.42.0/24, 10.120.42.0/24", "10.8.2.8/8, 192.150.8.6/16, 10.150.0.0/16",
			"10.155.8.8/16, 198.168.45.250/32, 10.155.45.250/32", "10.0.0.0/8, 0.25.0.0/16, 10.25.0.0/16" })
	void testReplaceHost(String ip, String subnet, String result) {
		assertEquals(result, CIDRv4.fromString(ip).replaceHost(CIDRv4.fromString(subnet)).toString());
	}

	@ParameterizedTest
	@CsvSource({ "10.120.0.0/16, 192.0.0.0/8, 192.120.0.0/16", "10.120.160.120/24, 172.16.0.0/16, 172.16.160.120/24",
			"10.120.139.15/24, 10.250.124.0/24, 10.250.124.15/24" })
	void testReplaceNetwork(String ip, String subnet, String result) {
		assertEquals(result, CIDRv4.fromString(ip).replaceNetwork(CIDRv4.fromString(subnet)).toString());
	}

	@ParameterizedTest
	@CsvSource({ "192.168.0.0/24, 1, 192.168.0.254", "10.0.2.10/22, 3, 10.0.3.252" })
	void testSetFromEnd(String ip, int add, String result) {
		assertEquals(result, CIDRv4.fromString(ip).setFromEnd(add).toIPString());
	}

	@ParameterizedTest
	@CsvSource({ "192.168.0.200/24, 1, 192.168.0.1", "10.0.3.10/22, 300, 10.0.1.44" })
	void testSetFromStart(String ip, int add, String result) {
		assertEquals(result, CIDRv4.fromString(ip).setFromStart(add).toIPString());
	}
}
