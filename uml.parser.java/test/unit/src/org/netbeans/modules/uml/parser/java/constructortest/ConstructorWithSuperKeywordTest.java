package org.netbeans.modules.uml.parser.java.constructortest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class ConstructorWithSuperKeywordTest extends AbstractParserTestCase {

	final String fileName = 
			 "ConstructorWithSuperKeywordTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Constructor Definition","Modifiers","Parameters","Constructor Body","Method Call","Identifier","Expression List" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"ConstructorWithSuperKeywordTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"ConstructorWithSuperKeywordTestFile",  "Name"} ,{"{",  "Method Body Start"} ,{".",  "Scope Operator"} ,{"super",  "Super Class Reference"} ,{"toString",  "Identifier"} ,{"(",  "Argument Start"} ,{")",  "Argument End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(ConstructorWithSuperKeywordTest.class);
	}

	public void testConstructorWithSuperKeyword() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
