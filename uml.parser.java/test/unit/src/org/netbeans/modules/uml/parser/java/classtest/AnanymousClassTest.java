package org.netbeans.modules.uml.parser.java.classtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class AnanymousClassTest extends AbstractParserTestCase {

	final String fileName = "AnanymousClassTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = {"Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Identifier","Initializer","Object Creation","Identifier","Expression List","Body","Variable Definition","Modifiers","Type" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"AnanymousClassTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"Object",  "Identifier"} ,{"o",  "Name"} ,{"new",  "Operator"} ,{"Object",  "Identifier"} ,{"(",  "Argument Start"} ,{")",  "Argument End"} ,{"{",  "Class Body Start"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"}",  "Class Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(AnanymousClassTest.class);
	}

	public void testClassAnanymous() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
