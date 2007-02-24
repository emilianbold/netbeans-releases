package org.netbeans.modules.uml.parser.java.attributetest.arraytest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class ArrayInitializationTest extends AbstractParserTestCase {

	final String fileName = "ArrayInitializationTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = {"Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Array Declarator","Initializer","Object Creation","Array Declarator","Variable Definition","Modifiers","Type","Array Declarator","Array Declarator","Initializer","Object Creation","Array Declarator","Array Declarator" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"ArrayInitializationTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"[",  "Array Start"} ,{"int",  "Primitive Type"} ,{"]",  "Array End"} ,{"i",  "Name"} ,{"new",  "Operator"} ,{"int",  "Primitive Type"} ,{"[",  "Array Start"} ,{"10",  "Integer Constant"} ,{"]",  "Array End"} ,{"[",  "Array Start"} ,{"[",  "Array Start"} ,{"int",  "Primitive Type"} ,{"]",  "Array End"} ,{"]",  "Array End"} ,{"j",  "Name"} ,{"new",  "Operator"} ,{"int",  "Primitive Type"} ,{"[",  "Array Start"} ,{"[",  "Array Start"} ,{"10",  "Integer Constant"} ,{"]",  "Array End"} ,{"10",  "Integer Constant"} ,{"]",  "Array End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(ArrayInitializationTest.class);
	}

	public void testArrayInitialization() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
