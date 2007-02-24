package org.netbeans.modules.uml.parser.java.exceptionhandlingtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class ThrowKeywordInsideMethodTest extends AbstractParserTestCase {

	final String fileName = "ThrowKeywordInsideMethodTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","RaisedException","Exception","Object Creation","Identifier","Expression List" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"ThrowKeywordInsideMethodTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"throw",  "Keyword"} ,{"new",  "Operator"} ,{"ArithmeticException",  "Identifier"} ,{"(",  "Argument Start"} ,{")",  "Argument End"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(ThrowKeywordInsideMethodTest.class);
	}

	public void testThrowKeywordInsideMethod() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
