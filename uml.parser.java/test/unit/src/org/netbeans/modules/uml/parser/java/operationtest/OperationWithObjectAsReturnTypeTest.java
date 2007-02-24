package org.netbeans.modules.uml.parser.java.operationtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class OperationWithObjectAsReturnTypeTest extends
		AbstractParserTestCase {

	final String fileName = 
			 "OperationWithObjectAsReturnTypeTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Identifier","Parameters","Method Body","Variable Definition","Modifiers","Type","Identifier","Initializer","Object Creation","Identifier","Expression List","Return","Identifier"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"OperationWithObjectAsReturnTypeTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"Obj1",  "Identifier"} ,{"op1",  "Name"} ,{"{",  "Method Body Start"} ,{"Obj1",  "Identifier"} ,{"obj",  "Name"} ,{"new",  "Operator"} ,{"Obj1",  "Identifier"} ,{"(",  "Argument Start"} ,{")",  "Argument End"} ,{"return",  "Keyword"} ,{"obj",  "Identifier"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(OperationWithObjectAsReturnTypeTest.class);
	}

	public void testOperationWithUserdefinedObjectAsReturnType() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
