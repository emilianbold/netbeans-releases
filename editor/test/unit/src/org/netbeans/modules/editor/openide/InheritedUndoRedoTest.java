/*
 * InheritedUndoRedoTest.java
 *
 * Created on February 15, 2005, 5:33 PM
 */

package org.netbeans.modules.editor.openide;

import javax.swing.text.EditorKit;
import org.openide.text.*;

/**
 *
 * @author mmetelka
 */
public class InheritedUndoRedoTest extends UndoRedoCooperationTest {
    
    /** Creates a new instance of InheritedUndoRedoTest */
    public InheritedUndoRedoTest(String methodName) {
        super(methodName);
    }
    
    protected EditorKit createEditorKit() {
        return new org.netbeans.modules.editor.NbEditorKit();
    }
    
}
