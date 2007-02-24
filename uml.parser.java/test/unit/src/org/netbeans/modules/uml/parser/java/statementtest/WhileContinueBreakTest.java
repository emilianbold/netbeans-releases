package org.netbeans.modules.uml.parser.java.statementtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;


public class WhileContinueBreakTest extends AbstractParserTestCase {

	final String fileName = "WhileContinueBreakTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Variable Definition","Modifiers","Type","Initializer","Loop","Test Condition","Body","Assignment Expression","Identifier","Plus Expression","Identifier","Conditional","Test Condition","LogicalAND Expression","GE Relational Expression","Identifier","LE Relational Expression","Identifier","Body","Continue","Conditional","Test Condition","Equality Expression","Identifier","Body","Break" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"WhileContinueBreakTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"void",  "Primitive Type"} ,{"test",  "Name"} ,{"{",  "Method Body Start"} ,{"int",  "Primitive Type"} ,{"x",  "Name"} ,{"3",  "Integer Constant"} ,{"while",  "Keyword"} ,{"true",  "Boolean"} ,{"{",  "Body Start"} ,{"=",  "Operator"} ,{"x",  "Identifier"} ,{"+",  "Operator"} ,{"x",  "Identifier"} ,{"1",  "Integer Constant"} ,{"if",  "Keyword"} ,{"&&",  "Operator"} ,{">=",  "Operator"} ,{"x",  "Identifier"} ,{"100",  "Integer Constant"} ,{"<=",  "Operator"} ,{"x",  "Identifier"} ,{"150",  "Integer Constant"} ,{"if",  "Keyword"} ,{"==",  "Operator"} ,{"x",  "Identifier"} ,{"200",  "Integer Constant"} ,{"}",  "Body End"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(WhileContinueBreakTest.class);
	}

	public void testWhileContinueBreak() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
