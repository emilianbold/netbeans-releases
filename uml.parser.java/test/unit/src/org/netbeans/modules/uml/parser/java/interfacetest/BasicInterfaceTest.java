package org.netbeans.modules.uml.parser.java.interfacetest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class BasicInterfaceTest extends AbstractParserTestCase {

	final String fileName = "BasicInterfaceTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Interface Declaration","Modifiers","Generalization" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"BasicInterfaceTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(BasicInterfaceTest.class);
	}

	public void testBasicInterface() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
