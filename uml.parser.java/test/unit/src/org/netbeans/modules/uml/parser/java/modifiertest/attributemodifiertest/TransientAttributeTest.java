package org.netbeans.modules.uml.parser.java.modifiertest.attributemodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;


public class TransientAttributeTest extends AbstractParserTestCase {

	final String fileName = "TransientAttributeTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"TransientAttributeTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"transient",  "Modifier"} ,{"int",  "Primitive Type"} ,{"a",  "Name"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(TransientAttributeTest.class);
	}

	public void testTransientAttribute() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
