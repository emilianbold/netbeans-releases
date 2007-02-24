package org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class StrictfpClassTest extends AbstractParserTestCase {

	final String fileName = "StrictfpClassTestFile.java";
	
	//These are the expected states for the above mentioned file

	final String[] expectedStates = {"Class Declaration","Modifiers","Generalization","Realization","Body"};

	//These are the expected tokens for the above mentioned file
	
	final String[][] expectedTokens = { {"strictfp",  "Modifier"} ,{"StrictfpClassTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(StrictfpClassTest.class);
	}

	public void testStrictfpTopLableClass() {		
		execute(fileName, expectedStates, expectedTokens);
	}

}
