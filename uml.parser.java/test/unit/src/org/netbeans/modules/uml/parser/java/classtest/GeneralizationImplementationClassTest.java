package org.netbeans.modules.uml.parser.java.classtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class GeneralizationImplementationClassTest extends AbstractParserTestCase {

	final String fileName = "GeneralizationImplementationClassTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Identifier","Realization","Identifier","Body"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"GeneralizationImplementationClassTestFile",  "Name"} ,{"ExtentTest",  "Identifier"} ,{"ImplementTest",  "Identifier"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(GeneralizationImplementationClassTest.class);
	}

	public void testClassGeneralizationImplementation() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
