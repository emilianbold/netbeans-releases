package org.netbeans.modules.uml.parser.java.attributetest.AssignValueToFinalVariabletest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class FinalVariableDirectInitializationTest extends
		AbstractParserTestCase {

    final String fileName = "FinalVariableDirectInitializationTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = {"Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Initializer"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"FinalVariableDirectInitializationTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"final",  "Modifier"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"0",  "Integer Constant"} ,{"}",  "Class Body End"}  };

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
