package org.netbeans.modules.uml.parser.java.jdk5.generics;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class GenericsArgumentAndReturntypeTest extends AbstractParserTestCase {

	final String fileName = "GenericsArgumentAndReturntypeTestFile.java";
	
	//These are the expected states for the above mentioned file

	final String[] expectedStates = { "Dependency","Identifier","Identifier","Identifier","Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Template Instantiation","Identifier","Type","Identifier","Initializer","Object Creation","Template Instantiation","Identifier","Type","Identifier","Expression List","Method Definition","Modifiers","Type","Parameters","Method Body","Variable Definition","Modifiers","Type","Template Instantiation","Identifier","Type","Identifier","Initializer","Method Call","Identifier","Expression List","Identifier","Method Definition","Modifiers","Type","Template Instantiation","Identifier","Type","Identifier","Parameters","Parameter","Modifiers","Type","Template Instantiation","Identifier","Type","Identifier","Method Body","Return","Identifier"};

	//These are the expected tokens for the above mentioned file
	
	final String[][] expectedTokens = { {"import",  "Keyword"} ,{".",  "Scope Operator"} ,{".",  "Scope Operator"} ,{"java",  "Identifier"} ,{"util",  "Identifier"} ,{"ArrayList",  "Identifier"} ,{"public",  "Modifier"} ,{"GenericsArgumentAndReturntypeTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"ArrayList",  "Identifier"} ,{"Integer",  "Identifier"} ,{"arrayList",  "Name"} ,{"new",  "Operator"} ,{"ArrayList",  "Identifier"} ,{"Integer",  "Identifier"} ,{"(",  "Argument Start"} ,{")",  "Argument End"} ,{"public",  "Modifier"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"ArrayList",  "Identifier"} ,{"Integer",  "Identifier"} ,{"a1",  "Name"} ,{"addCollection",  "Identifier"} ,{"(",  "Argument Start"} ,{"arrayList",  "Identifier"} ,{")",  "Argument End"} ,{"}",  "Method Body End"} ,{"ArrayList",  "Identifier"} ,{"Integer",  "Identifier"} ,{"addCollection",  "Name"} ,{"ArrayList",  "Identifier"} ,{"Integer",  "Identifier"} ,{"arrayList",  "Name"} ,{"{",  "Method Body Start"} ,{"return",  "Keyword"} ,{"arrayList",  "Identifier"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(AutoBoxingTest.class);
	}

	public void testBasicClass() {		
		execute(fileName, expectedStates, expectedTokens);
	}

}
