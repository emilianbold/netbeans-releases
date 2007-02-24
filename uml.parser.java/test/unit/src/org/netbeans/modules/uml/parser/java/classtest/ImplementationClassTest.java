package org.netbeans.modules.uml.parser.java.classtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class ImplementationClassTest extends AbstractParserTestCase {
	final String fileName = "ImplementationClassTestFile.java";

	//These are the expected states for the above mentioned file
	
	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Identifier","Body" };

	//These are the expected tokens for the above mentioned file
	
	final String[][] expectedTokens = { {"ImplementationClassTestFile",  "Name"} ,{"ImplementTestFile",  "Identifier"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(ImplementationClassTest.class);
	}

	public void testClassImplementation() {		
		execute(fileName, expectedStates, expectedTokens);
	}
}
