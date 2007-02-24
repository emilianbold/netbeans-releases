package org.netbeans.modules.uml.parser.java.modifiertest.interfacemodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;


public class StaticNestedInterfaceTest extends AbstractParserTestCase {

	final String fileName = "StaticNestedInterfaceTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = {"Class Declaration","Modifiers","Generalization","Realization","Body","Interface Declaration","Modifiers","Generalization"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"StaticNestedInterfaceTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"static",  "Modifier"} ,{"StaticNestedInterface",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(StaticNestedInterfaceTest.class);
	}

	public void testStaticNestedInterface() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
