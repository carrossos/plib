package net.carrossos.plib.persistency.reader;

public class StringReader extends DefaultReader {

	private String value;

	public void accept(String value) {
		this.value = value;
	}

	@Override
	public String getLocation() {
		return "Unknown";
	}

	@Override
	public boolean isPresent() {
		return value != null;
	}

	@Override
	public String readString() {
		return value;
	}

}
