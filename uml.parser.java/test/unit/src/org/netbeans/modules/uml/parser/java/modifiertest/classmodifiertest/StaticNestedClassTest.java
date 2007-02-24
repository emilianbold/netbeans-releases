package org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class StaticNestedClassTest extends AbstractParserTestCase {

	final String fileName = "StaticNestedClassTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = {"Class Declaration","Modifiers","Generalization","Realization","Body","Class Declaration","Modifiers","Generalization","Realization","Body" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"StaticNestedClassTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"static",  "Modifier"} ,{"StaticInnerClass",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(StaticNestedClassTest.class);
	}

	public void testStaticNestedClass() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
