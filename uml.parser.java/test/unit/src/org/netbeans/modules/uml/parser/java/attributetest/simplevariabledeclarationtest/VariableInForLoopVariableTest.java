package org.netbeans.modules.uml.parser.java.attributetest.simplevariabledeclarationtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class VariableInForLoopVariableTest extends AbstractParserTestCase {

	final String fileName = "VariableInForLoopVariableTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = {"Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Variable Definition","Modifiers","Type","Initializer","Loop","Loop Initializer","Variable Definition","Modifiers","Type","Test Condition","LT Relational Expression","Identifier","Loop PostProcess","Expression List","Increment Post Unary Expression","Identifier","Body","Assignment Expression","Identifier"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"VariableInForLoopVariableTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"int",  "Primitive Type"} ,{"j",  "Name"} ,{"0",  "Integer Constant"} ,{"for",  "Keyword"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{";",  "Conditional Separator"} ,{"<",  "Operator"} ,{"j",  "Identifier"} ,{"10",  "Integer Constant"} ,{";",  "PostProcessor Separator"} ,{"++",  "Operator"} ,{"j",  "Identifier"} ,{"{",  "Body Start"} ,{"=",  "Operator"} ,{"i",  "Identifier"} ,{"10",  "Integer Constant"} ,{"}",  "Body End"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(VariableInForLoopVariableTest.class);
	}

	public void testVariableInForLoopVariable() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
