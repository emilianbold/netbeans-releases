package org.netbeans.modules.uml.parser.java.classtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class BasicClassTest extends AbstractParserTestCase {

	final String fileName = "BasicClassTestFile.java";
	
	//These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body" };

	//These are the expected tokens for the above mentioned file
	
	final String[][] expectedTokens = { {"BasicClassTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(BasicClassTest.class);
	}

	public void testBasicClass() {		
		execute(fileName, expectedStates, expectedTokens);
	}

}
