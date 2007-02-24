package org.netbeans.modules.uml.parser.java.statementtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;


public class AssertStatementTest extends AbstractParserTestCase {

	final String fileName = "AssertStatementTestFile.java";
	
	//These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Variable Definition","Modifiers","Type","Initializer","Identifier"};

	//These are the expected tokens for the above mentioned file
	
	final String[][] expectedTokens = { {"AssertStatementTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"boolean",  "Primitive Type"} ,{"val",  "Name"} ,{"true",  "Boolean"} ,{"val",  "Identifier"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(AssertStatementTest.class);
	}

	public void testAssertStatement() {		
		execute(fileName, expectedStates, expectedTokens);
	}

}
