package org.netbeans.modules.uml.parser.java.expressiontest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class LogicalExpressionTest extends AbstractParserTestCase {

	final String fileName = "LogicalExpressionTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Variable Definition","Modifiers","Type","Variable Definition","Modifiers","Type","Variable Definition","Modifiers","Type","Assignment Expression","Identifier","Assignment Expression","Identifier","Assignment Expression","Identifier","Variable Definition","Modifiers","Type","Initializer","BinaryAND Expression","Identifier","Identifier","Variable Definition","Modifiers","Type","Initializer","LogicalAND Expression","GT Relational Expression","Identifier","Identifier","GT Relational Expression","Identifier","Identifier","Variable Definition","Modifiers","Type","Initializer","LogicalOR Expression","LT Relational Expression","Identifier","Identifier","LT Relational Expression","Identifier","Identifier","Variable Definition","Modifiers","Type","Initializer","Binary Not Unary Expression","Identifier"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"LogicalExpressionTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"int",  "Primitive Type"} ,{"j",  "Name"} ,{"int",  "Primitive Type"} ,{"k",  "Name"} ,{"=",  "Operator"} ,{"i",  "Identifier"} ,{"5",  "Integer Constant"} ,{"=",  "Operator"} ,{"j",  "Identifier"} ,{"10",  "Integer Constant"} ,{"=",  "Operator"} ,{"k",  "Identifier"} ,{"15",  "Integer Constant"} ,{"int",  "Primitive Type"} ,{"m",  "Name"} ,{"&",  "Operator"} ,{"i",  "Identifier"} ,{"j",  "Identifier"} ,{"boolean",  "Primitive Type"} ,{"n",  "Name"} ,{"&&",  "Operator"} ,{"(",  "Precedence Start"} ,{">",  "Operator"} ,{"i",  "Identifier"} ,{"j",  "Identifier"} ,{")",  "Precedence End"} ,{"(",  "Precedence Start"} ,{">",  "Operator"} ,{"j",  "Identifier"} ,{"k",  "Identifier"} ,{")",  "Precedence End"} ,{"boolean",  "Primitive Type"} ,{"x",  "Name"} ,{"||",  "Operator"} ,{"(",  "Precedence Start"} ,{"<",  "Operator"} ,{"i",  "Identifier"} ,{"j",  "Identifier"} ,{")",  "Precedence End"} ,{"(",  "Precedence Start"} ,{"<",  "Operator"} ,{"j",  "Identifier"} ,{"k",  "Identifier"} ,{")",  "Precedence End"} ,{"int",  "Primitive Type"} ,{"l",  "Name"} ,{"~",  "Operator"} ,{"i",  "Identifier"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(LogicalExpressionTest.class);
	}

	public void testLogicalExpression() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
