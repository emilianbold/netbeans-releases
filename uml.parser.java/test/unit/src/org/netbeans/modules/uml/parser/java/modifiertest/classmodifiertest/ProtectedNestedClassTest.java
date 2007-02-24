package org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;


public class ProtectedNestedClassTest extends AbstractParserTestCase {

	final String fileName = "ProtectedNestedClassTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Class Declaration","Modifiers","Generalization","Realization","Body" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"ProtectedNestedClassTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"protected",  "Modifier"} ,{"ProtectedInnerClass",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(ProtectedNestedClassTest.class);
	}

	public void testProtectedNestedClass() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
