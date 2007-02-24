package org.netbeans.modules.uml.parser.java.operationtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class OperationContainsClassTest extends AbstractParserTestCase {

	final String fileName = "OperationContainsClassTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Class Declaration","Modifiers","Generalization","Realization","Body" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"OperationContainsClassTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"void",  "Primitive Type"} ,{"Operation",  "Name"} ,{"{",  "Method Body Start"} ,{"innerClass",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(OperationContainsClassTest.class);
	}

	public void testOperationContainsClass() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
