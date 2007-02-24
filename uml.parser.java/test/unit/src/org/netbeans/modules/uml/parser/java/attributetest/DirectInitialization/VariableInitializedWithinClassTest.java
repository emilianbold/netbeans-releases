package org.netbeans.modules.uml.parser.java.attributetest.DirectInitialization;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class VariableInitializedWithinClassTest extends AbstractParserTestCase {

	final String fileName =  "VariableInitializedWithinClassTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Initializer","Variable Definition","Modifiers","Type","Identifier","Initializer","Object Creation","Identifier","Expression List"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"VariableInitializedWithinClassTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"10",  "Integer Constant"} ,{"Object",  "Identifier"} ,{"o",  "Name"} ,{"new",  "Operator"} ,{"Object",  "Identifier"} ,{"(",  "Argument Start"} ,{")",  "Argument End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(VariableInitializedWithinClassTest.class);
	}

	public void testVariableInitializedWithinClass() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
