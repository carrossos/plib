package net.carrossos.plib.net.api;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import net.carrossos.plib.net.http.HttpException;
import net.carrossos.plib.net.http.HttpURL;

public class JsonAPI {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Duration ASYNC_TIMEOUT = Duration.ofMinutes(3);

	private static final int RESPONSE_DUMP_SIZE_LIMIT = 1024 * 500;

	private final HttpURL base;

	private final Gson serializer;

	private final HttpClient client;

	private final Map<String, String> defaultHeaders = new HashMap<>();

	private CompletableFuture<Void> callAsync(HttpRequest.Builder builder) {
		return callAsync(builder, BodyHandlers.discarding()).handle((v, t) -> {
			if (t != null) {
				LOGGER.error("HTTP request failed: " + builder.build(),
						t instanceof CompletionException ? t.getCause() : t);
			}

			return null;
		});
	}

	private <R> CompletableFuture<R> callAsync(HttpRequest.Builder builder, BodyHandler<R> handler) {
		// Add default headers to request
		defaultHeaders.forEach(builder::header);

		HttpRequest request = builder.build();

		LOGGER.trace("Async request {} '{}'", request.method(), request.uri());

		return client.sendAsync(request, handler).thenApply(response -> {
			int status = response.statusCode();

			LOGGER.trace("Async response {} '{}' = {}", request.method(), request.uri(), status);

			if (status != 200) {
				throw new HttpException(request.method(), request.uri(), status);
			}

			return response.body();
		});
	}

	private <R> CompletableFuture<R> callAsync(HttpRequest.Builder builder, Class<R> clazz) {
		Objects.requireNonNull(clazz);

		return callAsync(builder, BodyHandlers.ofString()).thenApply(s -> {
			try {
				return serializer.fromJson(s, clazz);
			} catch (RuntimeException e) {
				throw handleParseError(e, s);
			}
		});
	}

	private <R> CompletableFuture<R> callAsync(HttpRequest.Builder builder, Type type) {
		Objects.requireNonNull(type);

		return callAsync(builder, BodyHandlers.ofString()).thenApply(s -> {
			try {
				return serializer.fromJson(s, type);
			} catch (RuntimeException e) {
				throw handleParseError(e, s);
			}
		});
	}

	private HttpRequest.Builder forPath(HttpURL path) {
		return HttpRequest.newBuilder(base.merge(path).toURI());
	}

	private HttpRequest.Builder forPayload(HttpURL path, String method, Object payload) {
		return HttpRequest.newBuilder(base.merge(path).toURI())
				.header("Content-Type", "application/json; charset=utf-8")
				.method(method, BodyPublishers.ofString(serializer.toJson(payload)));
	}

	private <R> R toSync(HttpURL path, CompletableFuture<R> future) throws InterruptedException, APIException {
		try {
			return future.get(ASYNC_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
		} catch (CancellationException e) {
			InterruptedException i = new InterruptedException("Cancelled request: " + path);
			i.addSuppressed(e);

			throw i;
		} catch (ExecutionException e) {
			throw new APIException("Failed API call to " + base.merge(path), e.getCause());
		} catch (TimeoutException e) {
			// XXX: no idea why that seems to happen
			LOGGER.error("BUG! Asynchronous API call seems hung: {}", future);

			try {
				future.cancel(true);
			} catch (Throwable t) {
				LOGGER.error("BUG! Asynchronous API call seems hung and future cannot be cancelled: " + future, t);
			}

			throw new APIException("BUG! Asynchronous API call to " + base.merge(path) + " seems hung", e);
		}
	}

	public void delete(HttpURL path) throws APIException, InterruptedException {
		toSync(path, deleteAsync(path));
	}

	public <R> R delete(HttpURL path, Class<R> clazz) throws APIException, InterruptedException {
		return toSync(path, callAsync(forPath(path).DELETE(), clazz));
	}

	public <R> R delete(HttpURL path, Type type) throws APIException, InterruptedException {
		return toSync(path, callAsync(forPath(path).DELETE(), type));
	}

	public CompletableFuture<Void> deleteAsync(HttpURL path) {
		return callAsync(forPath(path).DELETE());
	}

	public <R> CompletableFuture<R> deleteAsync(HttpURL path, Class<R> clazz) {
		return callAsync(forPath(path).DELETE(), clazz);
	}

	public <R> CompletableFuture<R> deleteAsync(HttpURL path, Type type) {
		return callAsync(forPath(path).DELETE(), type);
	}

	public <R> R get(HttpURL path, Class<R> clazz) throws APIException, InterruptedException {
		return toSync(path, getAsync(path, clazz));
	}

	public <R> R get(HttpURL path, Type type) throws APIException, InterruptedException {
		return toSync(path, getAsync(path, type));
	}

	public <R> CompletableFuture<R> getAsync(HttpURL path, Class<R> clazz) {
		return callAsync(forPath(path).GET(), clazz);
	}

	public <R> CompletableFuture<R> getAsync(HttpURL path, Type type) {
		return callAsync(forPath(path).GET(), type);
	}

	public Map<String, String> getDefaultHeaders() {
		return defaultHeaders;
	}

	public void post(HttpURL path, Object payload) throws APIException, InterruptedException {
		toSync(path, postAsync(path, payload));
	}

	public <R> R post(HttpURL path, Object payload, Class<R> clazz) throws APIException, InterruptedException {
		return toSync(path, postAsync(path, payload, clazz));
	}

	public <R> R post(HttpURL path, Object payload, Type type) throws APIException, InterruptedException {
		return toSync(path, postAsync(path, payload, type));
	}

	public CompletableFuture<Void> postAsync(HttpURL path, Object payload) {
		return callAsync(forPayload(path, "POST", payload));
	}

	public <R> CompletableFuture<R> postAsync(HttpURL path, Object payload, Class<R> clazz) {
		return callAsync(forPayload(path, "POST", payload), clazz);
	}

	public <R> CompletableFuture<R> postAsync(HttpURL path, Object payload, Type type) {
		return callAsync(forPayload(path, "POST", payload), type);
	}

	public void put(HttpURL path, Object payload) throws APIException, InterruptedException {
		toSync(path, putAsync(path, payload));
	}

	public <R> R put(HttpURL path, Object payload, Class<R> clazz) throws APIException, InterruptedException {
		return toSync(path, putAsync(path, payload, clazz));
	}

	public <R> R put(HttpURL path, Object payload, Type type) throws APIException, InterruptedException {
		return toSync(path, putAsync(path, payload, type));
	}

	public CompletableFuture<Void> putAsync(HttpURL path, Object payload) {
		return callAsync(forPayload(path, "PUT", payload));
	}

	public <R> CompletableFuture<R> putAsync(HttpURL path, Object payload, Class<R> clazz) {
		return callAsync(forPayload(path, "PUT", payload), clazz);
	}

	public <R> CompletableFuture<R> putAsync(HttpURL path, Object payload, Type type) {
		return callAsync(forPayload(path, "PUT", payload), type);
	}

	public JsonAPI(HttpURL base, Gson serializer, HttpClient.Builder builder) {
		this.base = base;
		this.serializer = serializer;
		this.client = builder.build();
	}

	private static APIException handleParseError(RuntimeException e, String response) {
		if (response.length() > RESPONSE_DUMP_SIZE_LIMIT) {
			try {
				Path tempFile = Files.createTempFile("dmp", ".txt");

				Files.write(tempFile, List.of(response), StandardOpenOption.TRUNCATE_EXISTING);

				LOGGER.warn("Failed response is too big to be shown in logs ({} bytes): saved in: {}",
						response.length(), tempFile.toAbsolutePath());
			} catch (IOException ex) {
				LOGGER.error("Failed to save large response, dump was lost!", ex);
			}

			return new APIException("Invalid response: (saved in file)", e);
		} else {
			return new APIException("Invalid response: " + response, e);
		}
	}

}
