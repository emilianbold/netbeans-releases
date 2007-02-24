package org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class NestedFinalClassTest extends AbstractParserTestCase {

	final String fileName = "NestedFinalClassTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Class Declaration","Modifiers","Generalization","Realization","Body" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"NestedFinalClassTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"final",  "Modifier"} ,{"FinalInnerClass",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(NestedFinalClassTest.class);
	}

	public void testFinalNestedClass() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
