package org.netbeans.modules.uml.parser.java.modifiertest.interfacemodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class PublicInterfaceTest extends AbstractParserTestCase {

	final String fileName = "PublicInterfaceTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Interface Declaration","Modifiers","Generalization"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"public",  "Modifier"} ,{"PublicInterfaceTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(PublicInterfaceTest.class);
	}

	public void testPublicTopLabelInterface() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
