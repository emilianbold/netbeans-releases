package org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class PublicMethodTest extends AbstractParserTestCase {

	final String fileName = "PublicMethodTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"PublicMethodTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(PublicMethodTest.class);
	}

	public void testPublicMethod() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
