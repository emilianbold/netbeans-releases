package org.netbeans.modules.uml.parser.java.operationtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class BasicOperationTest extends AbstractParserTestCase {

	final String fileName = "BasicOperationTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"BasicOperationTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"void",  "Primitive Type"} ,{"op1",  "Name"} ,{"{",  "Method Body Start"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(BasicOperationTest.class);
	}

	public void testBasicOperation() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
