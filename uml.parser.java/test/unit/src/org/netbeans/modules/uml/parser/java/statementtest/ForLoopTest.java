package org.netbeans.modules.uml.parser.java.statementtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class ForLoopTest extends AbstractParserTestCase {

	final String fileName = "ForLoopTestFile.java";
	
	//These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Loop","Loop Initializer","Variable Definition","Modifiers","Type","Initializer","Test Condition","LT Relational Expression","Identifier","Loop PostProcess","Expression List","Increment Post Unary Expression","Identifier","Body"};

	//These are the expected tokens for the above mentioned file
	
	final String[][] expectedTokens = { {"ForLoopTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"for",  "Keyword"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"0",  "Integer Constant"} ,{";",  "Conditional Separator"} ,{"<",  "Operator"} ,{"i",  "Identifier"} ,{"5",  "Integer Constant"} ,{";",  "PostProcessor Separator"} ,{"++",  "Operator"} ,{"i",  "Identifier"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(ForLoopTest.class);
	}

	public void testForLoop() {		
		execute(fileName, expectedStates, expectedTokens);
	}

}
