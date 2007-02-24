package org.netbeans.modules.uml.parser.java.attributetest.arraytest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class AssignElementsTest extends AbstractParserTestCase {

	final String fileName = "AssignElementsTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Array Declarator","Initializer","Array Initializer","Variable Definition","Modifiers","Type","Array Declarator","Array Declarator","Initializer","Array Initializer","Array Initializer","Array Initializer"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"AssignElementsTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"[",  "Array Start"} ,{"int",  "Primitive Type"} ,{"]",  "Array End"} ,{"i",  "Name"} ,{"{",  "Start Array Init"} ,{"1",  "Integer Constant"} ,{"2",  "Integer Constant"} ,{"3",  "Integer Constant"} ,{"4",  "Integer Constant"} ,{"5",  "Integer Constant"} ,{"}",  "End Array Init"} ,{"[",  "Array Start"} ,{"[",  "Array Start"} ,{"int",  "Primitive Type"} ,{"]",  "Array End"} ,{"]",  "Array End"} ,{"j",  "Name"} ,{"{",  "Start Array Init"} ,{"{",  "Start Array Init"} ,{"1",  "Integer Constant"} ,{"2",  "Integer Constant"} ,{"}",  "End Array Init"} ,{"{",  "Start Array Init"} ,{"3",  "Integer Constant"} ,{"4",  "Integer Constant"} ,{"}",  "End Array Init"} ,{"}",  "End Array Init"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(AssignElementsTest.class);
	}

	public void testAssignElements() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
