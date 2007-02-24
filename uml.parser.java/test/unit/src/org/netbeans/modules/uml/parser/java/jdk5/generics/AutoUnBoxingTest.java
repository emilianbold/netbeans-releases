package org.netbeans.modules.uml.parser.java.jdk5.generics;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;
import org.netbeans.modules.uml.parser.java.jdk5.generics.AutoBoxingTest;

import junit.textui.TestRunner;

/**
 * 
 */
public class AutoUnBoxingTest extends AbstractParserTestCase {
    
    final String fileName = "AutoUnBoxingTestFile.java";
    
    //These are the expected states for the above mentioned file
    
    final String[] expectedStates = { "Dependency","Identifier","Identifier","Identifier","Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type","Template Instantiation","Identifier","Type","Identifier","Initializer","Object Creation","Template Instantiation","Identifier","Type","Identifier","Expression List","Method Definition","Modifiers","Type","Parameters","Method Body","Loop","Loop Initializer","Variable Definition","Modifiers","Type","Identifier","Test Condition","Identifier","Body","Variable Definition","Modifiers","Type","Initializer","Identifier"};
    
    //These are the expected tokens for the above mentioned file
    
    final String[][] expectedTokens = { {"import",  "Keyword"} ,{".",  "Scope Operator"} ,{".",  "Scope Operator"} ,{"java",  "Identifier"} ,{"util",  "Identifier"} ,{"ArrayList",  "Identifier"} ,{"public",  "Modifier"} ,{"AutoUnBoxingTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"ArrayList",  "Identifier"} ,{"Integer",  "Identifier"} ,{"arrayList",  "Name"} ,{"new",  "Operator"} ,{"ArrayList",  "Identifier"} ,{"Integer",  "Identifier"} ,{"(",  "Argument Start"} ,{")",  "Argument End"} ,{"public",  "Modifier"} ,{"void",  "Primitive Type"} ,{"method",  "Name"} ,{"{",  "Method Body Start"} ,{"for",  "Keyword"} ,{"Integer",  "Identifier"} ,{"array",  "Name"} ,{"arrayList",  "Identifier"} ,{"{",  "Body Start"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"array",  "Identifier"} ,{"}",  "Body End"} ,{"}",  "Method Body End"} ,{"}",  "Class Body End"}  };
    
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
