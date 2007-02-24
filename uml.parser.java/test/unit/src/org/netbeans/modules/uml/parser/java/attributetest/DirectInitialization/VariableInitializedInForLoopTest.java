package org.netbeans.modules.uml.parser.java.attributetest.DirectInitialization;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class VariableInitializedInForLoopTest extends AbstractParserTestCase {

	final String fileName =  "VariableInitializedInForLoopTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Variable Definition","Modifiers","Type","Initializer","Loop","Loop Initializer","Variable Definition","Modifiers","Type","Initializer","Test Condition","LT Relational Expression","Identifier","Loop PostProcess","Expression List","Increment Post Unary Expression","Identifier","Body","Assignment Expression","Identifier"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"VariableInitializedInForLoopTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"int",  "Primitive Type"} ,{"j",  "Name"} ,{"0",  "Integer Constant"} ,{"for",  "Keyword"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"1",  "Integer Constant"} ,{";",  "Conditional Separator"} ,{"<",  "Operator"} ,{"j",  "Identifier"} ,{"10",  "Integer Constant"} ,{";",  "PostProcessor Separator"} ,{"++",  "Operator"} ,{"j",  "Identifier"} ,{"{",  "Body Start"} ,{"=",  "Operator"} ,{"i",  "Identifier"} ,{"10",  "Integer Constant"} ,{"}",  "Body End"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(VariableInitializedInForLoopTest.class);
	}

	public void testVariableInitializedInForLoop() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
