package net.carrossos.plib.net.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import net.carrossos.plib.utils.CollectionUtils;
import net.carrossos.plib.utils.StringParser;

public class HttpURL {

	private final boolean secure;

	private final String host;

	private final int port;

	private final String path;

	private final Map<String, String> parameters;

	private final Map<String, String> query;

	private final String anchor;

	private void checkRelative() {
		if (isRelative()) {
			throw new IllegalArgumentException("URL is relative: " + this);
		}
	}

	private String mergePath(String path) {
		String merged;

		int idxA = this.path.lastIndexOf('/');
		int idxB = path.indexOf('/');

		if (idxA == this.path.length() - 1 && idxB == 0) {
			merged = this.path + path.substring(1);
		} else if (idxA != this.path.length() - 1 && idxB != 0) {
			merged = this.path + '/' + path;
		} else {
			merged = this.path + path;
		}

		return merged;
	}

	public HttpURL addParameter(String key, Object value) {
		var map = new HashMap<>(parameters);
		map.put(key, value.toString());

		return new HttpURL(secure, host, port, path, map, query, anchor);
	}

	public HttpURL addQuery(String key, Object value) {
		var map = new HashMap<>(query);
		map.put(key, value.toString());

		return new HttpURL(secure, host, port, path, parameters, map, anchor);
	}

	public HttpURL appendPath(String path) {
		return new HttpURL(secure, host, port, mergePath(path), parameters, query, anchor);
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
		HttpURL other = (HttpURL) obj;
		if (anchor == null) {
			if (other.anchor != null) {
				return false;
			}
		} else if (!anchor.equals(other.anchor)) {
			return false;
		}
		if (host == null) {
			if (other.host != null) {
				return false;
			}
		} else if (!host.equals(other.host)) {
			return false;
		}
		if (parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!parameters.equals(other.parameters)) {
			return false;
		}
		if (path == null) {
			if (other.path != null) {
				return false;
			}
		} else if (!path.equals(other.path)) {
			return false;
		}
		if (port != other.port) {
			return false;
		}
		if (query == null) {
			if (other.query != null) {
				return false;
			}
		} else if (!query.equals(other.query)) {
			return false;
		}
		if (secure != other.secure) {
			return false;
		}
		return true;
	}

	public String getAnchor() {
		return anchor;
	}

	public String getHost() {
		checkRelative();

		return host;
	}

	public Map<String, String> getParameters() {
		return Collections.unmodifiableMap(parameters);
	}

	public String getPath() {
		return path;
	}

	public int getPort() {
		checkRelative();

		return port;
	}

	public Map<String, String> getQuery() {
		return Collections.unmodifiableMap(query);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (anchor == null ? 0 : anchor.hashCode());
		result = prime * result + (host == null ? 0 : host.hashCode());
		result = prime * result + (parameters == null ? 0 : parameters.hashCode());
		result = prime * result + (path == null ? 0 : path.hashCode());
		result = prime * result + port;
		result = prime * result + (query == null ? 0 : query.hashCode());
		result = prime * result + (secure ? 1231 : 1237);
		return result;
	}

	public boolean includesPath(HttpURL url) {
		if (!isRelative()) {
			if (!host.equals(url.host)) {
				return false;
			}

			if (port != url.port) {
				return false;
			}

			if (secure != url.secure) {
				return false;
			}
		}

		if (path == null) {
			if (url.path != null) {
				return false;
			}
		} else if (!url.path.startsWith(path)) {
			return false;
		}

		return true;
	}

	public boolean includesQuery(HttpURL url) {
		if (!isRelative()) {
			if (!host.equals(url.host)) {
				return false;
			}

			if (port != url.port) {
				return false;
			}

			if (secure != url.secure) {
				return false;
			}
		}

		if (path == null) {
			if (url.path != null) {
				return false;
			}
		} else if (!path.equals(url.path)) {
			return false;
		}

		if (!parameters.entrySet().stream()
				.allMatch(e -> Objects.equals(e.getValue(), url.parameters.get(e.getKey())))) {
			return false;
		}

		if (!query.entrySet().stream().allMatch(e -> Objects.equals(e.getValue(), url.query.get(e.getKey())))) {
			return false;
		}

		return true;
	}

	public boolean isRelative() {
		return host == null;
	}

	public boolean isSecure() {
		checkRelative();

		return secure;
	}

	public boolean isStandardPort() {
		checkRelative();

		if (secure) {
			return port == 443;
		} else {
			return port == 80;
		}
	}

	public HttpURL merge(HttpURL url) {
		if (isRelative() || !url.isRelative()) {
			throw new IllegalArgumentException(
					"Merge is only between absolute and relative URL: " + this + " <> " + url);
		}

		return new HttpURL(secure, host, port, mergePath(url.getPath()),
				CollectionUtils.merge(parameters, url.parameters), CollectionUtils.merge(query, url.query), url.anchor);
	}

	public HttpURL toRoot() {
		return new HttpURL(secure, host, port, "/", Map.of(), Map.of(), null);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		if (host != null) {
			if (secure) {
				builder.append("https://");
			} else {
				builder.append("http://");
			}

			builder.append(host);

			if (!isStandardPort()) {
				builder.append(':');
				builder.append(port);
			}
		}

		builder.append(path);

		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			builder.append(';');
			builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));

			if (!entry.getValue().isEmpty()) {
				builder.append('=');
				builder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
			}
		}

		boolean first = true;
		for (Map.Entry<String, String> entry : query.entrySet()) {
			if (first) {
				builder.append('?');
				first = false;
			} else {
				builder.append('&');
			}

			builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));

			if (!entry.getValue().isEmpty()) {
				builder.append('=');
				builder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
			}
		}

		if (anchor != null) {
			builder.append('#');
			builder.append(anchor);
		}

		return builder.toString();
	}

	public URI toURI() {
		checkRelative();

		return URI.create(toString());
	}

	public URL toURL() {
		checkRelative();

		try {
			return new URL(toString());
		} catch (MalformedURLException e) {
			throw new AssertionError(e);
		}
	}

	private HttpURL(boolean secure, String host, int port, String path, Map<String, String> parameters,
			Map<String, String> query, String anchor) {
		this.secure = secure;
		this.host = host;
		this.port = port;
		this.path = path;
		this.parameters = Collections.unmodifiableMap(parameters);
		this.query = Collections.unmodifiableMap(query);
		this.anchor = anchor;
	}

	public static HttpURL forHost(String host, boolean secure) {
		return new HttpURL(secure, host, secure ? 443 : 80, "/", Map.of(), Map.of(), null);
	}

	public static HttpURL parse(String url) {
		try {
			StringParser parser = new StringParser(url);
			int port;
			boolean secure;
			String host;

			if (parser.tryConsume("http")) {
				secure = parser.tryConsume("s");

				parser.consume("://");

				host = parser.readUntil(c -> c == '/' || c == ':');

				if (parser.tryConsume(":")) {
					try {
						port = Integer.valueOf(parser.readUntil('/'));
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("Invalid port: " + url);
					}
				} else {
					port = secure ? 443 : 80;
				}
			} else {
				port = -1;
				secure = false;
				host = null;
			}

			String path = parser.readUntil(c -> c == ';' || c == '?' || c == '#');

			if (path.isEmpty()) {
				path = "/";
			}

			Map<String, String> parameters;
			if (parser.tryConsume(";")) {
				parameters = new LinkedHashMap<>();

				parser.until(c -> c == '?' || c == '#', () -> {
					parser.tryConsume(";");
					parser.tryConsume("&");

					String key = URLDecoder.decode(parser.readUntil(c -> c == ';' || c == '=' || c == '&'),
							StandardCharsets.UTF_8);

					parser.tryConsume("=");

					String value = URLDecoder.decode(parser.readUntil(c -> c == ';' || c == '&'),
							StandardCharsets.UTF_8);

					parameters.put(key, value);
				});
			} else {
				parameters = new LinkedHashMap<>();
			}

			Map<String, String> query;
			if (parser.tryConsume("?")) {
				query = new LinkedHashMap<>();

				parser.until(c -> c == '#', () -> {
					parser.tryConsume(";");
					parser.tryConsume("&");

					String key = URLDecoder.decode(parser.readUntil(c -> c == ';' || c == '=' || c == '&'),
							StandardCharsets.UTF_8);

					parser.tryConsume("=");

					String value = URLDecoder.decode(parser.readUntil(c -> c == ';' || c == '&'),
							StandardCharsets.UTF_8);

					query.put(key, value);
				});
			} else {
				query = new LinkedHashMap<>();
			}

			String anchor;
			if (parser.tryConsume("#")) {
				anchor = parser.remaining();
			} else {
				anchor = null;
			}

			return new HttpURL(secure, host, port, path, parameters, query, anchor);
		} catch (RuntimeException e) {
			throw new IllegalArgumentException(String.format("Failed to parse '%s'", url), e);
		}
	}
}
