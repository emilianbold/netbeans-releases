package org.netbeans.modules.uml.parser.java.interfacetest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class InterfaceContainsClassTest extends AbstractParserTestCase {

	final String fileName = "InterfaceContainsClassTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Interface Declaration","Modifiers","Generalization","Class Declaration","Modifiers","Generalization","Realization","Body" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"InterfaceContainsClassTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"innerClass",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(InterfaceContainsClassTest.class);
	}

	public void testInterfaceContainsClass() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
