package org.netbeans.modules.uml.parser.java.statementtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class DoWhileTest extends AbstractParserTestCase {

	final String fileName = "DoWhileTestFile.java";
	
	//These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Variable Definition","Modifiers","Type","Initializer","Loop","Body","Test Condition","LE Relational Expression","Identifier" };

	//These are the expected tokens for the above mentioned file
	
	final String[][] expectedTokens = { {"DoWhileTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"void",  "Primitive Type"} ,{"test",  "Name"} ,{"{",  "Method Body Start"} ,{"int",  "Primitive Type"} ,{"x",  "Name"} ,{"3",  "Integer Constant"} ,{"do",  "Keyword"} ,{"{",  "Body Start"} ,{"}",  "Body End"} ,{"<=",  "Operator"} ,{"x",  "Identifier"} ,{"1",  "Integer Constant"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(DoWhileTest.class);
	}

	public void testDoWhile() {		
		execute(fileName, expectedStates, expectedTokens);
	}

}
