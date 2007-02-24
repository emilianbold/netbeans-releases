package org.netbeans.modules.uml.parser.java.attributetest.AssignValueToFinalVariabletest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class FinalVariableInitializationWithinBlockTest extends
		AbstractParserTestCase {

    final String fileName = "FinalVariableInitializationWithinBlockTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = {"Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Assignment Expression","Identifier" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"FinalVariableInitializationWithinBlockTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"final",  "Modifier"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"{",  "Body Start"} ,{"=",  "Operator"} ,{"i",  "Identifier"} ,{"10",  "Integer Constant"} ,{"}",  "Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(FinalVariableInitializationWithinBlockTest.class);
	}

	public void testAssignValueUninitializedVariableWithinBlock() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
