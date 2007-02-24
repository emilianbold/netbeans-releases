package org.netbeans.modules.uml.parser.java.enumtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class BasicEnumTest extends AbstractParserTestCase {

	final String fileName = "BasicEnumTestFile.java";
	
	//These are the expected states for the above mentioned file

	final String[] expectedStates = { "Enumeration Declaration","Modifiers","Realization","Body" };

	//These are the expected tokens for the above mentioned file
	
	final String[][] expectedTokens = { {"BasicEnumTestFile",  "Name"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(BasicEnumTest.class);
	}

	public void testBasicEnum() {		
		execute(fileName, expectedStates, expectedTokens);
	}

}
