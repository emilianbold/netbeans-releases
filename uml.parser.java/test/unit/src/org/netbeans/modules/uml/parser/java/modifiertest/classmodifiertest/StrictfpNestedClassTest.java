package org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class StrictfpNestedClassTest extends AbstractParserTestCase {

	final String fileName = "StrictfpNestedClassTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Class Declaration","Modifiers","Generalization","Realization","Body"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"StrictfpNestedClassTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"strictfp",  "Modifier"} ,{"StrictfpInnerClass",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(StrictfpNestedClassTest.class);
	}

	public void testStrictfpNestedClass() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
