package org.netbeans.modules.uml.parser.java.classtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class ClassBlockTest extends AbstractParserTestCase {

	final String fileName = "ClassBlockTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Static Initializer","Variable Definition","Modifiers","Type","Variable Definition","Modifiers","Type"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"ClassBlockTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"{",  "Body Start"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"}",  "Body End"} ,{"{",  "Body Start"} ,{"int",  "Primitive Type"} ,{"j",  "Name"} ,{"}",  "Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(ClassBlockTest.class);
	}

	public void testClassBlock() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
