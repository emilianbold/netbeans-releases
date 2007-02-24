package org.netbeans.modules.uml.parser.java.attributetest.simplevariabledeclarationtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class VariableDeclaredWithinClassTest extends AbstractParserTestCase {

	final String fileName = "VariableDeclaredWithinClassTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = {"Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Variable Definition","Modifiers","Type","Identifier" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens ={ {"VariableDeclaredWithinClassTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"Object",  "Identifier"} ,{"o",  "Name"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(VariableDeclaredWithinClassTest.class);
	}

	public void testVariableDeclaredWithinClass() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
