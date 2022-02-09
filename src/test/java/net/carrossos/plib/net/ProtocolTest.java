package net.carrossos.plib.net;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ProtocolTest {

	@ParameterizedTest
	@CsvSource({ "tcp/123, udp/53, icmp, tcp/80-100, udp/53-200, udp/4" })
	void testParse(String proto) {
		assertEquals(proto, Protocol.valueOf(proto).toString());
	}

	@ParameterizedTest
	@CsvSource({ "tcp/10-9, udp/, tcp, tcp/99999, icmp/3, tcp/-1, udp/0, icmp/2-33" })
	void testParseFail(String proto) {
		assertThrows(IllegalArgumentException.class, () -> Protocol.valueOf(proto));
	}

	@ParameterizedTest
	@CsvSource({ "tcp/445, tcp/445, true", "tcp/80-90, tcp/85, true", "tcp/80-90, tcp/91, false",
			"tcp/80-90, udp/90, false" })
	void testContain(String a, String b, boolean expect) {
		assertEquals(expect, Protocol.valueOf(a).contains(Protocol.valueOf(b)));
	}
}
