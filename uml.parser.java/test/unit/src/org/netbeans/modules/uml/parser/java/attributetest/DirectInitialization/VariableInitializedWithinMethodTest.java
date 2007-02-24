package org.netbeans.modules.uml.parser.java.attributetest.DirectInitialization;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class VariableInitializedWithinMethodTest extends AbstractParserTestCase {

	final String fileName =  "VariableInitializedWithinMethodTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Variable Definition","Modifiers","Type","Initializer"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"VariableInitializedWithinMethodTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"10",  "Integer Constant"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(VariableInitializedWithinMethodTest.class);
	}

	public void testVariableInitializedWithinMethod() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
