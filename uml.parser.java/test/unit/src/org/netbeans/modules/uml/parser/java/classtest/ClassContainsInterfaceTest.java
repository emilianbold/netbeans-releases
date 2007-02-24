package org.netbeans.modules.uml.parser.java.classtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class ClassContainsInterfaceTest extends AbstractParserTestCase {

	final String fileName =  "ClassContainsInterfaceTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Interface Declaration","Modifiers","Generalization"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"ClassContainsInterfaceTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"InterFace",  "Name"} ,{"{",  "Class Body Start"} ,{"}",  "Class Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(ClassContainsInterfaceTest.class);
	}

	public void testClassContainsInterface() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
