package net.carrossos.plib.io.velocity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;

public class Velocity {

	private VelocityEngine engine;

	private boolean strict = true;

	private Path root;

	private String file;

	private String template;

	private final Map<String, Object> parameters = new HashMap<>();

	private VelocityContext context;

	private void build() {
		this.engine = new VelocityEngine();
		this.context = new VelocityContext(context);

		if (file == null) {
			engine.init();
		} else {
			Properties properties = new Properties();
			properties.put("file.resource.loader.path", root.toString());

			engine.init(properties);
		}

		EventCartridge cartridge = new EventCartridge();
		cartridge.addEventHandler(new VelocityEventHandler(strict));

		this.context.attachEventCartridge(cartridge);
		this.parameters.forEach(context::put);
	}

	public Velocity fromFile(Path root, String file) {
		this.root = root.toAbsolutePath();
		this.file = file;
		this.template = null;

		return this;
	}

	public Velocity fromFile(String root, String file) {
		return fromFile(Path.of(root), file);
	}

	public Velocity fromString(String template) {
		this.template = template;
		this.root = null;
		this.file = null;

		return this;
	}

	public String generate() throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		try (PrintWriter writer = new PrintWriter(buffer, false, StandardCharsets.UTF_8)) {
			generate(writer);
		}

		return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
	}

	public void generate(Path path) throws IOException {
		try (var writer = new PrintWriter(Files.newBufferedWriter(path))) {
			generate(writer);
		}
	}

	public void generate(Writer writer) throws IOException {
		build();

		try {
			if (file == null) {
				engine.evaluate(context, writer, "memTpl", template);
			} else {
				Template veloTemplate = engine.getTemplate(file);

				veloTemplate.merge(context, writer);
			}
		} catch (Exception e) {
			throw new IOException(String.format("Failed to generate using context: %s", Arrays.stream(context.getKeys())
					.map(k -> k + " => " + context.get(k)).collect(Collectors.joining(", "))), e);
		}
	}

	public Velocity lax() {
		this.strict = false;

		return this;
	}

	public Velocity put(String key, Object val) {
		parameters.put(key, val);

		return this;
	}

	public Velocity strict() {
		this.strict = true;

		return this;
	}

	public Velocity withContext(VelocityContext context) {
		this.context = context;

		return this;
	}

	public static Velocity create() {
		return new Velocity();
	}
}
