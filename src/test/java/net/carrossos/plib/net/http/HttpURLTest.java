package net.carrossos.plib.net.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.google.gson.Gson;

class HttpURLTest {

	@ParameterizedTest
	@CsvSource({ "http://example.com, null", "https://test.com/#TOKEN, TOKEN" })
	public void testAnchor(String url, String anchor) {
		assertEquals(anchor.equals("null") ? null : anchor, HttpURL.parse(url).getAnchor());
	}

	@ParameterizedTest
	@CsvSource({ "http://example.com/, false, example.com, 80, /, {}, {}, null",
			"https://example.com:444/api/svc?action=test#token, true, example.com, 444, /api/svc, {}, {'action': 'test'}, token",
			"https://example.com/nav_to.do?uri=%2Fhome.do%3Fsysparm_auto_request%3Dtrue, true, example.com, 443, /nav_to.do, {}, {'uri': '/home.do?sysparm_auto_request=true'}, null",
			"https://example.com/nav_to.do;sess=12345?uri=%2Fhome.do%3Fsysparm_auto_request%3Dtrue#abcdef, true, example.com, 443, /nav_to.do, {'sess': '12345'}, {'uri': '/home.do?sysparm_auto_request=true'}, abcdef" })
	public void testComplex(String url, boolean secure, String host, int port, String path, String paramString,
			String queryString, String anchor) {
		@SuppressWarnings("unchecked")
		Map<String, String> parameters = new Gson().fromJson(paramString, Map.class);

		@SuppressWarnings("unchecked")
		Map<String, String> query = new Gson().fromJson(queryString, Map.class);

		HttpURL httpURL = HttpURL.parse(url);

		assertEquals(secure, httpURL.isSecure());
		assertEquals(host, httpURL.getHost());
		assertEquals(port, httpURL.getPort());
		assertEquals(path, httpURL.getPath());
		assertEquals(parameters, httpURL.getParameters());
		assertEquals(query, httpURL.getQuery());
		assertEquals(anchor.equals("null") ? null : anchor, httpURL.getAnchor());
		assertEquals(url, httpURL.toString());
	}

	@ParameterizedTest
	@CsvSource({ "http://example.com/a/b, http://example.com/a/b?test, true",
			"http://example.com/a/b, http://example.com/a/?test, false", "/a/b, /a/?test, false",
			"https://example.com/a/b?test, https://example.com/a/b?test, true",
			"http://example.com/a/b?test, http://example.com/a/b?test&b=1, true", "/b?test, /b?test&b=1, true",
			"http://example.com/a/b?test, http://example.com/0/b?test&b=1, false",
			"http://example.com/, http://example.com, true", "http://example.com/a, http://example.com/a/?test, true",
			"http://example.com/, http://example.com/b/c/a/?test, true",
			"http://example.com/b, http://example.com/b/c/a/?test, true", "/b, http://example.com/b/c/a/?test, true",
			"/b/c, https://example.com/b/c/a/?test, true", "https://example.com/b/c, /b/c, false",
			"http://example.com/d, http://example.com/b/c/a/?test, false",
			"http://example.com/b/c/, http://example.com/b/c/a/?test, true" })
	public void testIncludePath(String a, String b, boolean included) {
		assertEquals(included, HttpURL.parse(a).includesPath(HttpURL.parse(b)));
	}

	@ParameterizedTest
	@CsvSource({ "http://example.com/a/b, http://example.com/a/b?test, true",
			"http://example.com/a/b, http://example.com/a/?test, false", "/a/b, /a/?test, false",
			"https://example.com/a/b?test, https://example.com/a/b?test, true",
			"http://example.com/a/b?test, http://example.com/a/b?test&b=1, true", "/b?test, /b?test&b=1, true",
			"http://example.com/a/b?test, http://example.com/0/b?test&b=1, false",
			"/a/b?test, http://example.com/0/b?test&b=1, false", "/a/b?test, https://example.com:443/a/b?test, true",
			"http://example.com/, http://example.com, true" })
	public void testIncludeQuery(String a, String b, boolean included) {
		assertEquals(included, HttpURL.parse(a).includesQuery(HttpURL.parse(b)));
	}

	@Test
	public void testParameters() {
		assertEquals(Map.of("a", "0", "b", "1"),
				HttpURL.parse("https://example.com/test/action.svc;a=0&b=1").getParameters());
		assertEquals(Map.of("a", ""), HttpURL.parse("https://example.com/test/action.svc;a").getParameters());
		assertEquals(Map.of("a", "0", "b", "1"),
				HttpURL.parse("https://example.com/test/action.svc;a=0;b=1").getParameters());
		assertEquals(Map.of("a", "0", "b", "1"),
				HttpURL.parse("https://example.com/test/action.svc;a=0;b=1#OTKEN").getParameters());
	}

	@Test
	public void testQuery() {
		assertEquals(Map.of("a", "0", "b", "1"),
				HttpURL.parse("https://example.com/test/action.svc?a=0&b=1").getQuery());
		assertEquals(Map.of("a", ""), HttpURL.parse("https://example.com/test/action.svc?a").getQuery());
		assertEquals(Map.of("a", "0", "b", "1"),
				HttpURL.parse("https://example.com/test/action.svc?a=0;b=1").getQuery());
		assertEquals(Map.of("c", "2"), HttpURL.parse("https://example.com/test/action.svc;a=0;b=1?c=2").getQuery());
		assertEquals(Map.of("c", "2", "d", "3"),
				HttpURL.parse("https://example.com/test/action.svc;a=0;b=1?c=2&d=3").getQuery());
		assertEquals(Map.of("c", "2", "d", "3"),
				HttpURL.parse("https://example.com/test/action.svc;a=0;b=1?c=2;d=3#TOKEN").getQuery());
	}

	@ParameterizedTest
	@CsvSource({ "http://example.com/a/b, false", "/a/b, true", "/a, true", "https://test.com, false" })
	public void testRelative(String url, boolean relative) {
		assertEquals(relative, HttpURL.parse(url).isRelative());
	}

	@ParameterizedTest
	@CsvSource({ "http://example.com/a/b?test, http://example.com/", "http://example.com/a/b#f, http://example.com/",
			"http://example.com/?test, http://example.com/", "http://example.com, http://example.com/" })
	public void testRoot(String a, String b) {
		assertEquals(b, HttpURL.parse(a).toRoot().toString());
	}

	@ParameterizedTest
	@CsvSource({ "http://example.com, false", "https://test.com, true", "https://example.com:444/, true" })
	public void testSecure(String url, boolean secure) {
		assertEquals(secure, HttpURL.parse(url).isSecure());
	}
}
