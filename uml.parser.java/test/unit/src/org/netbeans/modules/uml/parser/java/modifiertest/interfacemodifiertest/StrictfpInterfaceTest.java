package org.netbeans.modules.uml.parser.java.modifiertest.interfacemodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;


public class StrictfpInterfaceTest extends AbstractParserTestCase {

	final String fileName = "StrictfpInterfaceTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Interface Declaration","Modifiers","Generalization" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"strictfp",  "Modifier"} ,{"StrictfpInterfaceTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(StrictfpInterfaceTest.class);
	}

	public void testStrictfpTopLableInterface() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
