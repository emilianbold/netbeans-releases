package org.netbeans.modules.uml.parser.java.statementtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

public class SwitchCaseTest extends AbstractParserTestCase {
    
    final String fileName = "SwitchCaseTestFile.java";
    
    //These are the expected states for the above mentioned file
    
    final String[] expectedStates = {"Class Declaration","Modifiers","Generalization","Realization","Body","Method Definition","Modifiers","Type","Parameters","Method Body","Variable Definition","Modifiers","Type","Initializer","Option Conditional","Test Condition","Identifier","Option Group","Test Condition","Body","Method Call","Identifier","Identifier","Identifier","Expression List","Break","Option Group","Default Option","Body","Method Call","Identifier","Identifier","Identifier","Expression List","Identifier","Break" };
    
    //These are the expected tokens for the above mentioned file
    
    final String[][] expectedTokens = { {"SwitchCaseTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"void",  "Primitive Type"} ,{"test",  "Name"} ,{"{",  "Method Body Start"} ,{"int",  "Primitive Type"} ,{"month",  "Name"} ,{"8",  "Integer Constant"} ,{"switch",  "Keyword"} ,{"month",  "Identifier"} ,{"case",  "Keyword"} ,{"1",  "Integer Constant"} ,{".",  "Scope Operator"} ,{".",  "Scope Operator"} ,{"System",  "Identifier"} ,{"out",  "Identifier"} ,{"println",  "Identifier"} ,{"(",  "Argument Start"} ,{"\"1\"",  "String Constant"} ,{")",  "Argument End"} ,{"default",  "Keyword"} ,{".",  "Scope Operator"} ,{".",  "Scope Operator"} ,{"System",  "Identifier"} ,{"out",  "Identifier"} ,{"println",  "Identifier"} ,{"(",  "Argument Start"} ,{"month",  "Identifier"} ,{")",  "Argument End"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };
    
    @Override
            protected void setUp() throws Exception {
        super.setUp();
    }
    
    public static void main(String[] args) {
        TestRunner.run(SwitchCaseTest.class);
    }
    
    public void testSwitchCase() {
        execute(fileName, expectedStates, expectedTokens);
    }
    
}
