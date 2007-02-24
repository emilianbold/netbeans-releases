package org.netbeans.modules.uml.parser.java.attributetest.arraytest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class ArrayDeclarationTest extends AbstractParserTestCase {

	final String fileName = "ArrayDeclarationTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Array Declarator","Variable Definition","Modifiers","Type","Array Declarator","Array Declarator" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"ArrayDeclarationTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"[",  "Array Start"} ,{"int",  "Primitive Type"} ,{"]",  "Array End"} ,{"i",  "Name"} ,{"[",  "Array Start"} ,{"[",  "Array Start"} ,{"int",  "Primitive Type"} ,{"]",  "Array End"} ,{"]",  "Array End"} ,{"j",  "Name"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(ArrayDeclarationTest.class);
	}

	public void testArrayDeclaration() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
