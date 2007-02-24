package org.netbeans.modules.uml.parser.java.attributetest.DirectInitialization;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class VariableInitializedWithinBlockTest extends AbstractParserTestCase {

	final String fileName = "VariableInitializedWithinBlockTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Initializer" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"VariableInitializedWithinBlockTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"{",  "Body Start"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"10",  "Integer Constant"} ,{"}",  "Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(VariableInitializedWithinBlockTest.class);
	}

	public void testVariableInitializedWithinBlock() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
