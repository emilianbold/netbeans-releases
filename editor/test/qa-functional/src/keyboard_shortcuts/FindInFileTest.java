/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package keyboard_shortcuts;
/*
 * Main.java
 *
 * Created on 23. srpen 2004, 17:25
 */

import java.awt.event.KeyEvent;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import lib.EditorTestCase;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.EventTool;


/**
 *
 * @author  Petr Felenda
 */
public class FindInFileTest extends EditorTestCase {

    //private final String dialogTitle = Bundle.getStringTrimmed("org.netbeans.editor.Bundle","find-title"); // Find;
    private final String dialogTitle = "Find in Projects";
    private final int keyCode = KeyEvent.VK_F;
    private final int modifiers = (KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
      
    /** Creates a new instance of Main */
    public FindInFileTest(String testMethodName) {
        super(testMethodName);
    }
    
    public void testFindInFile(){
        openDefaultProject();
        openDefaultSampleFile();
        try {
        
            EditorOperator editor = getDefaultSampleEditorOperator();

            JEditorPaneOperator txtOper = editor.txtEditorPane();
            txtOper.pushKey(keyCode, modifiers);            
            closeDialog(dialogTitle);

        } finally {
            closeFileWithDiscard();
        }
    }
    
    public static void main(String[] args) {
        TestRunner.run(FindInFileTest.class);
    }

    
}
