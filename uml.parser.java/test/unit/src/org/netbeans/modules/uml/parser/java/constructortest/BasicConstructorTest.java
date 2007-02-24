package org.netbeans.modules.uml.parser.java.constructortest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class BasicConstructorTest extends AbstractParserTestCase {

	final String fileName = "BasicConstructorTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Constructor Definition","Modifiers","Parameters","Constructor Body" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"BasicConstructorTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"BasicConstructorTestFile",  "Name"} ,{"{",  "Method Body Start"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(BasicConstructorTest.class);
	}

	public void testBasicConstructor() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
