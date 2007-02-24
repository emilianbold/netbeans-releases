package org.netbeans.modules.uml.core.configstringframework;

import junit.framework.TestCase;

public class ConfigStringHelperTestCase extends TestCase {
	private ConfigStringHelper configStringHelper = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(ConfigStringHelperTestCase.class);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		configStringHelper = ConfigStringHelper.instance();
	}

	public void testInstance() {
		assertNotNull(configStringHelper);
	}

	public void testGetTranslator() {
		IConfigStringTranslator configStringTranslator = configStringHelper
				.getTranslator();
		assertNotNull(configStringTranslator);
	}

}
