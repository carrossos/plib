package net.carrossos.plib.persistency.impl.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class CSVPersistencyTest {

	@Test
	public void testBasics() {
		assertEquals(List.of("1", "2", "3"), CSVContainer.split(',', "1,2,3"));
		assertEquals(List.of("1", "", "3", "4"), CSVContainer.split(',', "1,,3,4"));

		assertEquals(List.of("", "", "", "A"), CSVContainer.split(',', ",,,A"));
		assertEquals(List.of("", "", "", "A"), CSVContainer.split(',', ",,\"\",A"));
		assertEquals(List.of("", "", "", "A"), CSVContainer.split(',', "\"\",,\"\",A"));
	}

	@Test
	public void testDoubleQuotes() {
		assertEquals(List.of("A", "1\"2", "C"), CSVContainer.split(',', "A,1\"2,C"));
		assertEquals(List.of("A", "1,\",2", "C"), CSVContainer.split(',', "A,\"1,\"\",2\",C"));
		assertEquals(List.of("A", "\"BC\" DE"), CSVContainer.split(',', "A,\"\"\"BC\"\" DE\""));
	}

	@Test
	public void testQuoting() {
		assertEquals(List.of("1", "2,3", "4"), CSVContainer.split(',', "1,\"2,3\",4"));
		assertEquals(List.of("1", "2", "3,4"), CSVContainer.split(',', "1,2,\"3,4\""));
		assertEquals(List.of("1", "2,3", ",,4,,"), CSVContainer.split(',', "1,\"2,3\",\",,4,,\""));
		assertEquals(List.of("1\"", "2\"", "3\""), CSVContainer.split(',', "1\",2\",3\""));
		assertEquals(List.of("1\"", "2\"", "3\""), CSVContainer.split(',', "\"1\"\"\",2\",\"3\"\"\""));
		assertEquals(List.of("", "2", "3"), CSVContainer.split(',', "\"\",2,\"3\""));
	}

	@Test
	public void testReal() {
		List<String> split = CSVContainer.split(',',
				"2007M08,85071000,AUTOMOTIVE BATTERIES N50,\"KOREA,REPUBLIC OF\",USD,,40.0,360.0,135013.0,954.97,25.0,33753.25,0.0,0.0,17.5,29534.05,");

		assertEquals(17, split.size());
		assertEquals("KOREA,REPUBLIC OF", split.get(3));
		assertEquals("USD", split.get(4));
		assertEquals("", split.get(5));
	}

}
