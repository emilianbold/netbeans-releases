package org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class StaticMethodTest extends AbstractParserTestCase {

	final String fileName = "StaticMethodTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"StaticMethodTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"static",  "Modifier"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"} , };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(StaticMethodTest.class);
	}

	public void testStaticMethod() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
