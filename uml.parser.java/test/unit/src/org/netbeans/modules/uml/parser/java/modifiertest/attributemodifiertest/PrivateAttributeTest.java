package org.netbeans.modules.uml.parser.java.modifiertest.attributemodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class PrivateAttributeTest extends AbstractParserTestCase {

	final String fileName = "PrivateAttributeTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens ={ {"PrivateAttributeTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"private",  "Modifier"} ,{"int",  "Primitive Type"} ,{"a",  "Name"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(PrivateAttributeTest.class);
	}

	public void testPrivateAttribute() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
