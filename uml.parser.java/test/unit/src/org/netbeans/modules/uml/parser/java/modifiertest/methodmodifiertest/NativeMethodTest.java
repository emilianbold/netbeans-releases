package org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class NativeMethodTest extends AbstractParserTestCase {

	final String fileName = "NativeMethodTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens ={ {"NativeMethodTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"native",  "Modifier"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(NativeMethodTest.class);
	}

	public void testNativeMethod() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
