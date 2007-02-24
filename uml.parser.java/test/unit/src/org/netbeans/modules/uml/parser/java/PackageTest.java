package org.netbeans.modules.uml.parser.java;

import org.netbeans.modules.uml.parser.java.AbstractParserTestCase;

import junit.textui.TestRunner;

/**
 * 
 */
public class PackageTest extends AbstractParserTestCase {
    
    final String fileName = "PackageTestFile.java";
    
    // These are the expected states for the above mentioned file
    
    final String[] expectedStates = { "Package","Identifier" };
    
    // These are the expected tokens for the above mentioned file
    
    final String[][] expectedTokens = { {"package",  "Keyword"} ,{"com",  "Identifier"}  };
    
    @Override
            protected void setUp() throws Exception {
        super.setUp();
    }
    
    public static void main(String[] args) {
        TestRunner.run(PackageTest.class);
    }
    
    public void testPackage() {        
        execute(fileName, expectedStates, expectedTokens);
    }
    
}
