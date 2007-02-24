package org.netbeans.modules.uml.parser.java.constructortest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class ConstructorPrimitiveTypeArgumentTest extends AbstractParserTestCase {

	final String fileName = "ConstructorPrimitiveTypeArgumentTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Constructor Definition","Modifiers","Parameters","Parameter","Modifiers","Type","Constructor Body" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"ConstructorPrimitiveTypeArgumentTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"ConstructorPrimitiveTypeArgumentTestFile",  "Name"} ,{"int",  "Primitive Type"} ,{"a",  "Name"} ,{"{",  "Method Body Start"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(ConstructorPrimitiveTypeArgumentTest.class);
	}

	public void testConstructorPrimitiveType() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
