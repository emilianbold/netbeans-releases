package org.netbeans.modules.uml.parser.java.jdk5;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class StaticImportTest extends AbstractParserTestCase {
    
    final String fileName = "StaticImportTestFile.java";
    
    //These are the expected states for the above mentioned file
    
    final String[] expectedStates = {"Identifier","Identifier","Identifier","Identifier","Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Initializer","Identifier"};
    
    //These are the expected tokens for the above mentioned file
    
    final String[][] expectedTokens = { {".",  "Scope Operator"} ,{".",  "Scope Operator"} ,{".",  "Scope Operator"} ,{"java",  "Identifier"} ,{"lang",  "Identifier"} ,{"Math",  "Identifier"} ,{"*",  "OnDemand Operator"} ,{"public",  "Modifier"} ,{"StaticImportTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"double",  "Primitive Type"} ,{"piValue",  "Name"} ,{"PI",  "Identifier"} ,{"}",  "Class Body End"}  };
    
    @Override
            protected void setUp() throws Exception {
        super.setUp();
    }
    
    public static void main(String[] args) {
        TestRunner.run(StaticImportTest.class);
    }
    
    public void testBasicClass() {
        execute(fileName, expectedStates, expectedTokens);
    }
    
}
