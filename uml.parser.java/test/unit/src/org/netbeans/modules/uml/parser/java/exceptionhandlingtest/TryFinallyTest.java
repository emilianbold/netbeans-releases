package org.netbeans.modules.uml.parser.java.exceptionhandlingtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class TryFinallyTest extends AbstractParserTestCase {

	final String fileName = "TryFinallyTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Exception Processing","Body","Default Processing"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"TryFinallyTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"try",  "Keyword"} ,{"{",  "Body Start"} ,{"}",  "Body End"} ,{"{",  "Body Start"} ,{"}",  "Body End"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(TryFinallyTest.class);
	}

	public void testTryFinally() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
