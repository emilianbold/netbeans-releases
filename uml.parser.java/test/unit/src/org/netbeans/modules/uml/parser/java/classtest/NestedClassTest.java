package org.netbeans.modules.uml.parser.java.classtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class NestedClassTest extends AbstractParserTestCase {

	final String fileName ="NestedClassTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Class Declaration","Modifiers","Generalization","Realization","Body"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"NestedClassTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"InnerClass",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(NestedClassTest.class);
	}

	public void testClassGeneralization() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
