package org.netbeans.modules.uml.parser.java.constructortest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class ConstructorMethodCallUsingThisKeywordTest extends AbstractParserTestCase {

	final String fileName = 
			 "ConstructorMethodCallUsingThisKeywordTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Initializer","Constructor Definition","Modifiers","Parameters","Parameter","Modifiers","Type","Constructor Body","Constructor Definition","Modifiers","Parameters","Parameter","Modifiers","Type","Parameter","Modifiers","Type","Identifier","Constructor Body","Constructor Call","Expression List","Identifier" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"ConstructorMethodCallUsingThisKeywordTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"0",  "Integer Constant"} ,{"public",  "Modifier"} ,{"ConstructorMethodCallUsingThisKeywordTestFile",  "Name"} ,{"int",  "Primitive Type"} ,{"a",  "Name"} ,{"{",  "Method Body Start"} ,{"public",  "Modifier"} ,{"ConstructorMethodCallUsingThisKeywordTestFile",  "Name"} ,{"int",  "Primitive Type"} ,{"a",  "Name"} ,{"String",  "Identifier"} ,{"s",  "Name"} ,{"{",  "Method Body Start"} ,{"a",  "Identifier"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(ConstructorMethodCallUsingThisKeywordTest.class);
	}

	public void testConstructorWithThisMethod() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
