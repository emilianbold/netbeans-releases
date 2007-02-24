package org.netbeans.modules.uml.parser.java.attributetest.AssignValueToFinalVariabletest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class FinalVariableInitializationWithinMethodTest extends
		AbstractParserTestCase {

	final String fileName ="FinalVariableInitializationWithinMethodTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Constructor Definition","Modifiers","Parameters","Constructor Body","Assignment Expression","Identifier" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"FinalVariableInitializationWithinMethodTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"final",  "Modifier"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"public",  "Modifier"} ,{"FinalVariableInitializationWithinMethodTestFile",  "Name"} ,{"{",  "Method Body Start"} ,{"=",  "Operator"} ,{"i",  "Identifier"} ,{"10",  "Integer Constant"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(FinalVariableInitializationWithinMethodTest.class);
	}

	public void testAssignValueUninitializedVariableWithinMethod() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
