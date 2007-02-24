package org.netbeans.modules.uml.parser.java.statementtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class IfElseIfTest extends AbstractParserTestCase {

	final String fileName = "IfElseIfTestFile.java";
	
	//These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Variable Definition","Modifiers","Type","Initializer","Conditional","Test Condition","Equality Expression","Identifier","Body","Else Conditional","Test Condition","Equality Expression","Identifier","Body" };

	//These are the expected tokens for the above mentioned file
	
	final String[][] expectedTokens = { {"IfElseIfTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"0",  "Integer Constant"} ,{"if",  "Keyword"} ,{"==",  "Operator"} ,{"i",  "Identifier"} ,{"0",  "Integer Constant"} ,{"{",  "Body Start"} ,{"}",  "Body End"} ,{"else",  "Keyword"} ,{"if",  "Keyword"} ,{"==",  "Operator"} ,{"i",  "Identifier"} ,{"1",  "Integer Constant"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(IfElseIfTest.class);
	}

	public void testIfElseIf() {		
		execute(fileName, expectedStates, expectedTokens);
	}

}
