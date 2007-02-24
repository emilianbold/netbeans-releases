package org.netbeans.modules.uml.parser.java.importtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class ImportPackageDependencyTest extends AbstractParserTestCase {

	final String fileName =  "ImportPackageDependencyTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Dependency","Identifier","Identifier","Identifier" };

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens = { {"import",  "Keyword"} ,{".",  "Scope Operator"} ,{".",  "Scope Operator"} ,{"java",  "Identifier"} ,{"util",  "Identifier"} ,{"*",  "OnDemand Operator"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(ImportPackageDependencyTest.class);
	}

	public void testPackage() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
