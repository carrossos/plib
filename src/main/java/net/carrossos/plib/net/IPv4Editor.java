package net.carrossos.plib.net;

import java.beans.PropertyEditorSupport;

public class IPv4Editor extends PropertyEditorSupport {

	@Override
	public String getAsText() {
		return getValue().toString();
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		setValue(IPv4.fromString(text));
	}

}
