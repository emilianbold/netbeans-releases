/*
 * JavaCodeFoldingTest.java
 *
 * Created on 24 January 2005, 16:49
 */

package java_editor_actions;

import editor_actions.EditorActionsTest;
import lib.JavaEditorTestCase;


/**
 *
 * @author mato
 */
public class JavaEditorActionsTest extends EditorActionsTest{
    
    /** Creates a new instance of JavaCodeFoldingTest */
    public JavaEditorActionsTest(String testMethodName) {
        super(testMethodName);
    }
    
    protected String getDefaultProjectName() {
        return JavaEditorTestCase.PROJECT_NAME;
    }
    
}
