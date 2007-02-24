package org.netbeans.modules.uml.parser.java.constructortest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class ConstructorWithSuperMethodTest extends AbstractParserTestCase {

	final String fileName = 
			 "ConstructorWithSuperMethodTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Constructor Definition","Modifiers","Parameters","Constructor Body","Super Constructor Call","Expression List" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"ConstructorWithSuperMethodTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"ConstructorWithSuperMethodTestFile",  "Name"} ,{"{",  "Method Body Start"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(ConstructorWithSuperMethodTest.class);
	}

	public void testConstructorWithSuperMethod() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
