package org.netbeans.modules.uml.parser.java.modifiertest.attributemodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class FinalAttributeTest extends AbstractParserTestCase {

	final String fileName = "FinalAttributeTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"FinalAttributeTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"final",  "Modifier"} ,{"int",  "Primitive Type"} ,{"a",  "Name"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(FinalAttributeTest.class);
	}

	public void testFinalAttribute() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
