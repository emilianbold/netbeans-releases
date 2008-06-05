/*
 * JavaCodeFoldingTest.java
 *
 * Created on 24 January 2005, 16:49
 */

package org.netbeans.test.java.editor.actions;

import org.netbeans.test.java.editor.actions.EditorActionsTestCase;
import org.netbeans.test.java.editor.lib.JavaEditorTestCase;


/**
 *
 * @author mato
 */
public class JavaEditorActionsTestCase extends EditorActionsTestCase{
    
    /** Creates a new instance of JavaCodeFoldingTest */
    public JavaEditorActionsTestCase(String testMethodName) {
        super(testMethodName);
    }
    
    protected String getDefaultProjectName() {
        return JavaEditorTestCase.PROJECT_NAME;
    }
    
}
