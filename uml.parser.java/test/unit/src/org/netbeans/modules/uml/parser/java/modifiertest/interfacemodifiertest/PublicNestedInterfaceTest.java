package org.netbeans.modules.uml.parser.java.modifiertest.interfacemodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;


public class PublicNestedInterfaceTest extends AbstractParserTestCase {

	final String fileName = "PublicNestedInterfaceTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Interface Declaration","Modifiers","Generalization"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"PublicNestedInterfaceTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"PublicNestedInterface",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(PublicNestedInterfaceTest.class);
	}

	public void testPublicNestedInterface() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
