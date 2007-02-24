package org.netbeans.modules.uml.parser.java.classtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class GeneralizationClassTest extends AbstractParserTestCase {

	final String fileName = "GeneralizationClassTestFile.java";
	
	//These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Identifier","Realization","Body" };

	//These are the expected tokens for the above mentioned file
	
	final String[][] expectedTokens = { {"GeneralizationClassTestFile",  "Name"} ,{"ExtentTestFile",  "Identifier"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(GeneralizationClassTest.class);
	}

	public void testClassGeneralization() {		
		execute(fileName, expectedStates, expectedTokens);
	}

}
