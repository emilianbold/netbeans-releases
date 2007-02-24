package org.netbeans.modules.uml.parser.java.operationtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class OperationWithThrowsClauseTest extends AbstractParserTestCase {

	final String fileName = "OperationWithThrowsClauseTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Throws Declaration","Identifier","Method Body" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"OperationWithThrowsClauseTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"void",  "Primitive Type"} ,{"op1",  "Name"} ,{"throws",  "Keyword"} ,{"Exception",  "Identifier"} ,{"{",  "Method Body Start"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(OperationWithThrowsClauseTest.class);
	}

	public void testOperationWithThrowsClauseTest() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
