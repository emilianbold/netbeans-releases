package org.netbeans.modules.uml.parser.java.enumtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class EnumWithLiteralAndAttributeTest extends AbstractParserTestCase {

	final String fileName = "EnumWithLiteralAndAttributeTestFile.java";
	
	//These are the expected states for the above mentioned file

	final String[] expectedStates = {"Enumeration Declaration","Modifiers","Realization","Body","Enum Member","Variable Definition","Modifiers","Type","Initializer"};

	//These are the expected tokens for the above mentioned file
	
	final String[][] expectedTokens = { {"EnumWithLiteralAndAttributeTestFile",  "Name"} ,{"a",  "Name"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"0",  "Integer Constant"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(EnumWithLiteralAndAttributeTest.class);
	}

	public void testEnumWithLiteralAttr() {		
		execute(fileName, expectedStates, expectedTokens);
	}

}
