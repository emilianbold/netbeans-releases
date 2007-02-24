package org.netbeans.modules.uml.parser.java.interfacetest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class InterfaceContainsEnumTest extends AbstractParserTestCase {

	final String fileName = "InterfaceContainsEnumTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = {"Interface Declaration","Modifiers","Generalization","Enumeration Declaration","Modifiers","Realization","Body"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens ={ {"InterfaceContainsEnumTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"Enum1",  "Name"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(InterfaceContainsEnumTest.class);
	}

	public void testInterfaceContainsEnum() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
