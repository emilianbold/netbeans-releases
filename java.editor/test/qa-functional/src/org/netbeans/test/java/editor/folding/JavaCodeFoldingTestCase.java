/*
 * JavaCodeFoldingTest.java
 *
 * Created on 24 January 2005, 16:49
 */

package org.netbeans.test.java.editor.folding;

import org.netbeans.test.java.editor.lib.JavaEditorTestCase;

/**
 *
 * @author mato
 */
public class JavaCodeFoldingTestCase extends CodeFoldingTestCase{
    
    /** Creates a new instance of JavaCodeFoldingTest */
    public JavaCodeFoldingTestCase(String testMethodName) {
        super(testMethodName);
    }
    
    protected String getDefaultProjectName() {
        return JavaEditorTestCase.PROJECT_NAME;
    }
    
}
