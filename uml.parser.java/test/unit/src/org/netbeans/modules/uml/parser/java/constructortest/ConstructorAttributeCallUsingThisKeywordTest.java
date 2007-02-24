package org.netbeans.modules.uml.parser.java.constructortest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class ConstructorAttributeCallUsingThisKeywordTest extends AbstractParserTestCase {

	final String fileName = 
			 "ConstructorAttributeCallUsingThisKeywordTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Initializer","Constructor Definition","Modifiers","Parameters","Parameter","Modifiers","Type","Constructor Body","Assignment Expression","Identifier","Identifier"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"ConstructorAttributeCallUsingThisKeywordTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"0",  "Integer Constant"} ,{"public",  "Modifier"} ,{"ConstructorAttributeCallUsingThisKeywordTestFile",  "Name"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"{",  "Method Body Start"} ,{"=",  "Operator"} ,{".",  "Scope Operator"} ,{"this",  "This Reference"} ,{"i",  "Identifier"} ,{"i",  "Identifier"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(ConstructorAttributeCallUsingThisKeywordTest.class);
	}

	public void testConstructorWithThisKeyword() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
