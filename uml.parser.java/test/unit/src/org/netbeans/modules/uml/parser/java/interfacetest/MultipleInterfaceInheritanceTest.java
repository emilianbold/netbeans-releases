package org.netbeans.modules.uml.parser.java.interfacetest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class MultipleInterfaceInheritanceTest extends AbstractParserTestCase {

	final String fileName = "MultipleInterfaceInheritanceTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Interface Declaration","Modifiers","Generalization","Identifier","Identifier"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"MultipleInterfaceInheritanceTestFile",  "Name"} ,{"Interface1",  "Identifier"} ,{"Interface2",  "Identifier"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(MultipleInterfaceInheritanceTest.class);
	}

	public void testInterfaceExtendMoreThanInterfaces() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
