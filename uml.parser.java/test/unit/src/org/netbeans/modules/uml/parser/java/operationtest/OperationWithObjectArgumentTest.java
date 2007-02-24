package org.netbeans.modules.uml.parser.java.operationtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class OperationWithObjectArgumentTest extends
		AbstractParserTestCase {

	final String fileName = "OperationWithObjectArgumentTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Parameter","Modifiers","Type","Identifier","Method Body"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"OperationWithObjectArgumentTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"void",  "Primitive Type"} ,{"op1",  "Name"} ,{"Obj1",  "Identifier"} ,{"a",  "Name"} ,{"{",  "Method Body Start"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(OperationWithObjectArgumentTest.class);
	}

	public void testOperationWithUseDefinedDatatypeArgument() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
