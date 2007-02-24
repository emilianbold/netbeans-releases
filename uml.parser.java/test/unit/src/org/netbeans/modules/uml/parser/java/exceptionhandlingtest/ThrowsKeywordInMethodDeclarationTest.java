package org.netbeans.modules.uml.parser.java.exceptionhandlingtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class ThrowsKeywordInMethodDeclarationTest extends AbstractParserTestCase {

	final String fileName =  "ThrowsKeywordInMethodDeclarationTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Throws Declaration","Identifier","Method Body" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"ThrowsKeywordInMethodDeclarationTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"throws",  "Keyword"} ,{"ArithmeticException",  "Identifier"} ,{"{",  "Method Body Start"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(ThrowsKeywordInMethodDeclarationTest.class);
	}

	public void testThrowsKeywordInMethodDeclaration() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
