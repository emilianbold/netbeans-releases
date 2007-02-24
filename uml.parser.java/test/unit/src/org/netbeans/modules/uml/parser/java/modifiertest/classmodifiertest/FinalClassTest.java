package org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class FinalClassTest extends AbstractParserTestCase {

	final String fileName = "FinalClassTestFile.java";
	
	//These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body"};

	//These are the expected tokens for the above mentioned file
	
	final String[][] expectedTokens = { {"final",  "Modifier"} ,{"FinalClassTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(FinalClassTest.class);
	}

	public void testFinalTopLabelClass() {		
		execute(fileName, expectedStates, expectedTokens);
	}

}
