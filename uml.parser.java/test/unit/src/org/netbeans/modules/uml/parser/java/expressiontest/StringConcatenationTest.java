package org.netbeans.modules.uml.parser.java.expressiontest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class StringConcatenationTest extends AbstractParserTestCase {

	final String fileName = "StringConcatenationTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Identifier","Initializer","Variable Definition","Modifiers","Type","Identifier","Initializer","Variable Definition","Modifiers","Type","Identifier","Initializer","Plus Expression","Plus Expression","Identifier","Identifier" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"StringConcatenationTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"String",  "Identifier"} ,{"str1",  "Name"} ,{"\"Hello\"",  "String Constant"} ,{"String",  "Identifier"} ,{"str2",  "Name"} ,{"\"World\"",  "String Constant"} ,{"String",  "Identifier"} ,{"str3",  "Name"} ,{"+",  "Operator"} ,{"+",  "Operator"} ,{"str1",  "Identifier"} ,{"\" \"",  "String Constant"} ,{"str2",  "Identifier"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(StringConcatenationTest.class);
	}

	public void testStringConcatenation() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
