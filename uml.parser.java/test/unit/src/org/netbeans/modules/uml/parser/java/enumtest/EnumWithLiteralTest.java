package org.netbeans.modules.uml.parser.java.enumtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class EnumWithLiteralTest extends AbstractParserTestCase {

	final String fileName = "EnumWithLiteralTestFile.java";
	
	//These are the expected states for the above mentioned file

	final String[] expectedStates = {"Enumeration Declaration","Modifiers","Realization","Body","Enum Member","Enum Member"};

	//These are the expected tokens for the above mentioned file
	
	final String[][] expectedTokens = { {"EnumWithLiteralTestFile",  "Name"} ,{"Literal1",  "Name"} ,{"Literal2",  "Name"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(EnumWithLiteralTest.class);
	}

	public void testEnumWithLiteral() {		
		execute(fileName, expectedStates, expectedTokens);
	}

}
