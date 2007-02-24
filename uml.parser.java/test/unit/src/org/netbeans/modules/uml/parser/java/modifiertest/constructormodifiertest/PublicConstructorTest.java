package org.netbeans.modules.uml.parser.java.modifiertest.constructormodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class PublicConstructorTest extends AbstractParserTestCase {

	final String fileName = "PublicConstructorTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Constructor Definition","Modifiers","Parameters","Constructor Body"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens ={ {"public",  "Modifier"} ,{"PublicConstructorTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"PublicConstructorTest",  "Name"} ,{"{",  "Method Body Start"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(PrivateConstructorTest.class);
	}

	public void testAbstractMethod() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
