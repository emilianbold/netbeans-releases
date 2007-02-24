package org.netbeans.modules.uml.parser.java.modifiertest.classmodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;


public class PublicNestedClassTest extends AbstractParserTestCase {

	final String fileName = "PublicNestedClassTestFile.java";
	
	//These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Class Declaration","Modifiers","Generalization","Realization","Body"};

	//These are the expected tokens for the above mentioned file
	
	final String[][] expectedTokens = { {"PublicNestedClassTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"PublicInnerClass",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(PublicNestedClassTest.class);
	}

	public void testPublicNestedClass() {		
		execute(fileName, expectedStates, expectedTokens);
	}

}
