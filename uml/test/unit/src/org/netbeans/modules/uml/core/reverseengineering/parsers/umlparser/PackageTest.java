package org.netbeans.modules.uml.core.reverseengineering.parsers.umlparser;

import junit.textui.TestRunner;

public class PackageTest extends AbstractUmlParserTestCase {
	public static void main(String[] args) {
		TestRunner.run(PackageTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testPackage() {
		execute(getClass().getSimpleName());
	}
}
