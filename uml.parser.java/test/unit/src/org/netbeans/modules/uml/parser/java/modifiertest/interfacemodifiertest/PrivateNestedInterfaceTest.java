package org.netbeans.modules.uml.parser.java.modifiertest.interfacemodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;


public class PrivateNestedInterfaceTest extends AbstractParserTestCase {

	final String fileName = "PrivateNestedInterfaceTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = {"Class Declaration","Modifiers","Generalization","Realization","Body","Interface Declaration","Modifiers","Generalization" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens ={ {"PrivateNestedInterfaceTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"private",  "Modifier"} ,{"PrivateNestedInterface",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(PrivateNestedInterfaceTest.class);
	}

	public void testPrivateNestedInterface() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
