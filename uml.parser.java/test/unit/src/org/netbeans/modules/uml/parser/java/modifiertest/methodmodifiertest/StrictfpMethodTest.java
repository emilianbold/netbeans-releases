package org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class StrictfpMethodTest extends AbstractParserTestCase {

	final String fileName = "StrictfpMethodTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"StrictfpMethodTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"strictfp",  "Modifier"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(StrictfpMethodTest.class);
	}

	public void testStrictfpMethod() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
