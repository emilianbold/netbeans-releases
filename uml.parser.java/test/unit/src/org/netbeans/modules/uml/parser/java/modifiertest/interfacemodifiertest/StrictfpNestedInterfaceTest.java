package org.netbeans.modules.uml.parser.java.modifiertest.interfacemodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;


public class StrictfpNestedInterfaceTest extends AbstractParserTestCase {

	final String fileName = "StrictfpNestedInterfaceTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Interface Declaration","Modifiers","Generalization"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"StrictfpNestedInterfaceTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"strictfp",  "Modifier"} ,{"StrictfpNestedInterface",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(StrictfpNestedInterfaceTest.class);
	}

	public void testStrictfpNestedInterface() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
