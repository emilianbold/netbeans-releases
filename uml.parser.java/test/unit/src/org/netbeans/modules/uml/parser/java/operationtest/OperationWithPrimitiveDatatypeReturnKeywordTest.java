package org.netbeans.modules.uml.parser.java.operationtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class OperationWithPrimitiveDatatypeReturnKeywordTest extends AbstractParserTestCase {

	final String fileName = "OperationWithPrimitiveDatatypeReturnKeywordTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Return" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"OperationWithPrimitiveDatatypeReturnKeywordTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"int",  "Primitive Type"} ,{"op1",  "Name"} ,{"{",  "Method Body Start"} ,{"return",  "Keyword"} ,{"0",  "Integer Constant"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(OperationWithPrimitiveDatatypeReturnKeywordTest.class);
	}

	public void testOperationWithReturnKeywordTest() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
