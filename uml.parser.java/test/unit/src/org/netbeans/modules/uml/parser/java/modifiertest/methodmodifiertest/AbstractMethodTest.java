package org.netbeans.modules.uml.parser.java.modifiertest.methodmodifiertest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class AbstractMethodTest extends AbstractParserTestCase {

	final String fileName = "AbstractMethodTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens ={ {"abstract",  "Modifier"} ,{"AbstractMethodTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"abstract",  "Modifier"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(AbstractMethodTest.class);
	}

	public void testAbstractMethod() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
