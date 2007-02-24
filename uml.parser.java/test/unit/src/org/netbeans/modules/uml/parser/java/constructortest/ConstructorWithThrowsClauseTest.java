package org.netbeans.modules.uml.parser.java.constructortest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class ConstructorWithThrowsClauseTest extends AbstractParserTestCase {

	final String fileName = 
			 "ConstructorWithThrowsClauseTestFile.java";

	// These are the expected states for the above mentioned file

	final String[] expectedStates = { "Class Declaration","Modifiers","Generalization","Realization","Body","Constructor Definition","Modifiers","Parameters","Throws Declaration","Identifier","Constructor Body"};

	// These are the expected tokens for the above mentioned file

	final String[][] expectedTokens ={ {"ConstructorWithThrowsClauseTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"public",  "Modifier"} ,{"ConstructorWithThrowsClauseTestFile",  "Name"} ,{"throws", "Keyword"} ,{"Exception",  "Identifier"} ,{"{",  "Method Body Start"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(ConstructorWithThrowsClauseTest.class);
	}

	public void testConstructorWithThrowsClause() {
		execute(fileName, expectedStates, expectedTokens);
	}

}
