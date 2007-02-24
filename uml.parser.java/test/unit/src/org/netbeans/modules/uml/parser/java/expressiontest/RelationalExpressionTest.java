package org.netbeans.modules.uml.parser.java.expressiontest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class RelationalExpressionTest extends AbstractParserTestCase {

	final String fileName = "RelationalExpressionTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Variable Definition","Modifiers","Type","Initializer","Variable Definition","Modifiers","Type","Variable Definition","Modifiers","Type","Variable Definition","Modifiers","Type","Initializer","Variable Definition","Modifiers","Type","Initializer","Assignment Expression","Identifier","GE Relational Expression","Identifier","Identifier","Variable Definition","Modifiers","Type","Initializer","Conditional Expression","LT Relational Expression","Identifier","Identifier" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"RelationalExpressionTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"boolean",  "Primitive Type"} ,{"b1",  "Name"} ,{"true",  "Boolean"} ,{"boolean",  "Primitive Type"} ,{"b2",  "Name"} ,{"boolean",  "Primitive Type"} ,{"b3",  "Name"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"10",  "Integer Constant"} ,{"int",  "Primitive Type"} ,{"j",  "Name"} ,{"5",  "Integer Constant"} ,{"=",  "Operator"} ,{"b2",  "Identifier"} ,{"(",  "Precedence Start"} ,{">=",  "Operator"} ,{"i",  "Identifier"} ,{"j",  "Identifier"} ,{")",  "Precedence End"} ,{"int",  "Primitive Type"} ,{"ternary",  "Name"} ,{"?",  "Operator"} ,{"(",  "Precedence Start"} ,{"<",  "Operator"} ,{"i",  "Identifier"} ,{"j",  "Identifier"} ,{")",  "Precedence End"} ,{"5",  "Integer Constant"} ,{"10",  "Integer Constant"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(RelationalExpressionTest.class);
	}

	public void testRelationalExpression() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
