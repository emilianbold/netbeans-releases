package org.netbeans.modules.uml.parser.java.interfacetest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class InterfaceBodyTest extends AbstractParserTestCase {

	final String fileName = "InterfaceBodyTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Interface Declaration","Modifiers","Generalization","Variable Definition","Modifiers","Type","Initializer","Method Declaration","Modifiers","Type","Parameters"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"InterfaceBodyTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"static",  "Modifier"} ,{"int",  "Primitive Type"} ,{"s",  "Name"} ,{"0",  "Integer Constant"} ,{"public",  "Modifier"} ,{"void",  "Primitive Type"} ,{"op1",  "Name"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(InterfaceBodyTest.class);
	}

	public void testInterfacewithMethodandAttribute() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
