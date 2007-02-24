package org.netbeans.modules.uml.parser.java.statementtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class EnhancedForLoopTest extends AbstractParserTestCase {

	final String fileName = "EnhancedForLoopTestFile.java";
	
	//These are the expected states for the above mentioned file

	final String[] expectedStates = {"Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Parameter","Modifiers","Type","Array Declarator","Method Body","Loop","Loop Initializer","Variable Definition","Modifiers","Type","Test Condition","Identifier","Body"};

	//These are the expected tokens for the above mentioned file
	
	final String[][] expectedTokens = { {"EnhancedForLoopTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"[",  "Array Start"} ,{"int",  "Primitive Type"} ,{"]",  "Array End"} ,{"a",  "Name"} ,{"{",  "Method Body Start"} ,{"for",  "Keyword"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"a",  "Identifier"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(EnhancedForLoopTest.class);
	}

	public void testForEach() {		
		execute(fileName, expectedStates, expectedTokens);
	}

}
