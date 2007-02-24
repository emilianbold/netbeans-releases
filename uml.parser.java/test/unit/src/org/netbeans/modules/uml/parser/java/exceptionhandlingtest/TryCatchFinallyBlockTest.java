package org.netbeans.modules.uml.parser.java.exceptionhandlingtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class TryCatchFinallyBlockTest extends AbstractParserTestCase {

	final String fileName = "TryCatchFinallyBlockTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Exception Processing","Body","Exception Handler","Parameter","Modifiers","Type","Identifier","Default Processing"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"TryCatchFinallyBlockTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"try",  "Keyword"} ,{"{",  "Body Start"} ,{"}",  "Body End"} ,{"Exception",  "Identifier"} ,{"e",  "Name"} ,{"{",  "Body Start"} ,{"}",  "Body End"} ,{"{",  "Body Start"} ,{"}",  "Body End"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(TryCatchFinallyBlockTest.class);
	}

	public void testTryCatchFinallyBlock() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
