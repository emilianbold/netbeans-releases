package org.netbeans.modules.uml.parser.java.attributetest.assignvaluetest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class AssignValueWithinBlockTest extends AbstractParserTestCase {

	final String fileName =  "AssignValueWithinBlockTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Assignment Expression","Identifier" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"AssignValueWithinBlockTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"{",  "Body Start"} ,{"=",  "Operator"} ,{"i",  "Identifier"} ,{"10",  "Integer Constant"} ,{"}",  "Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(AssignValueWithinBlockTest.class);
	}

	public void testAssignValueWithinBlock() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
