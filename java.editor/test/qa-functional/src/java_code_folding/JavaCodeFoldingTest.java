/*
 * JavaCodeFoldingTest.java
 *
 * Created on 24 January 2005, 16:49
 */

package java_code_folding;

import code_folding.CodeFoldingTest;
import lib.JavaEditorTestCase;

/**
 *
 * @author mato
 */
public class JavaCodeFoldingTest extends CodeFoldingTest{
    
    /** Creates a new instance of JavaCodeFoldingTest */
    public JavaCodeFoldingTest(String testMethodName) {
        super(testMethodName);
    }
    
    protected String getDefaultProjectName() {
        return JavaEditorTestCase.PROJECT_NAME;
    }
    
}
