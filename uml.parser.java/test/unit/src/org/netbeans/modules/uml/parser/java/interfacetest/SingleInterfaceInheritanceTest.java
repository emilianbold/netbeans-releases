package org.netbeans.modules.uml.parser.java.interfacetest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class SingleInterfaceInheritanceTest extends AbstractParserTestCase {

	final String fileName = "SingleInterfaceInheritanceTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Interface Declaration","Modifiers","Generalization","Identifier"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"SingleInterfaceInheritanceTestFile",  "Name"} ,{"Interface1",  "Identifier"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"} };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(SingleInterfaceInheritanceTest.class);
	}

	public void testInterfaceExtendKeyword() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
