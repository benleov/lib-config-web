package lib.config.web.impl;

import java.util.LinkedHashSet;

import lib.config.base.configuration.impl.BasicConfiguration;
import lib.config.web.DisplayableConfiguration;

public class DisplayableBasicConfiguration extends BasicConfiguration implements
		DisplayableConfiguration {

	@Override
	public String getDisplayName() {
		return getId();
	}

	@Override
	public LinkedHashSet<String> getKeys() {
		return new LinkedHashSet<String>(super.getKeys());
	}
}
