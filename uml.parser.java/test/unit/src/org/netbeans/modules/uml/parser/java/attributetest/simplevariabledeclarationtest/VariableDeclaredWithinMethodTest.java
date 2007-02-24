package org.netbeans.modules.uml.parser.java.attributetest.simplevariabledeclarationtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class VariableDeclaredWithinMethodTest extends AbstractParserTestCase {

	final String fileName =  "VariableDeclaredWithinMethodTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Variable Definition","Modifiers","Type" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"VariableDeclaredWithinMethodTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(VariableDeclaredWithinMethodTest.class);
	}

	public void testVariableDeclaredWithinMethod() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
