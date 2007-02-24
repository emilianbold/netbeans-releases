package org.netbeans.modules.uml.parser.java.attributetest.simplevariabledeclarationtest;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class VariableDeclaredWithinBlockTest extends AbstractParserTestCase {
    
    final String fileName = "VariableDeclaredWithinBlockTestFile.java";
    
    // These are the expected states for the above mentioned file
    
    final String[] expectedStates = {"Class Declaration","Modifiers","Generalization","Realization","Body","Variable Definition","Modifiers","Type" };
    
    // These are the expected tokens for the above mentioned file
    
    final String[][] expectedTokens = { {"VariableDeclaredWithinBlockTestFile",  "Name"} ,{"{",  "Class Body Start"} ,{"{",  "Body Start"} ,{"int",  "Primitive Type"} ,{"i",  "Name"} ,{"}",  "Body End"} ,{"}",  "Class Body End"}  };
    
    @Override
            protected void setUp() throws Exception {
        super.setUp();
    }
    
    public static void main(String[] args) {
        TestRunner.run(VariableDeclaredWithinBlockTest.class);
    }
    
    public void testVariableDeclaredWthinBlock() {
        execute(fileName, expectedStates, expectedTokens);
    }
    
}
