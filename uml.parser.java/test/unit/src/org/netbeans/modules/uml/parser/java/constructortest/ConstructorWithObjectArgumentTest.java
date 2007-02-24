package org.netbeans.modules.uml.parser.java.constructortest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class ConstructorWithObjectArgumentTest extends AbstractParserTestCase {

	final String fileName = 
			 "ConstructorWithObjectArgumentTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = {"Class Declaration","Modifiers","Generalization","Realization","Body","Constructor Definition","Modifiers","Parameters","Parameter","Modifiers","Type","Identifier","Constructor Body"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"ConstructorWithObjectArgumentTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"ConstructorWithObjectArgumentTestFile",  "Name"} ,{"Object",  "Identifier"} ,{"o",  "Name"} ,{"{",  "Method Body Start"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(ConstructorWithObjectArgumentTest.class);
	}

	public void testConstructorUserDefinedType() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
