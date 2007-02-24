package org.netbeans.modules.uml.parser.java.attributetest.assignvaluetest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class AssignValueWithinMethodTest extends AbstractParserTestCase {

	final String fileName ="AssignValueWithinMethodTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Method Definition","Modifiers","Type","Parameters","Method Body","Assignment Expression","Identifier" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens ={ {"AssignValueWithinMethodTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"=",  "Operator"} ,{"i",  "Identifier"} ,{"10",  "Integer Constant"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(AssignValueWithinMethodTest.class);
	}

	public void testAssignValueWithinMethod() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
