package org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class PrivateMethodTest extends AbstractParserTestCase {

	final String fileName = "PrivateMethodTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"PrivateMethodTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"private",  "Modifier"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(PrivateMethodTest.class);
	}

	public void testPrivateMethod() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
