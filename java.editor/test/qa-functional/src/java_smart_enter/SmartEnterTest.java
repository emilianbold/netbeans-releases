/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package java_smart_enter;
/*
 * Main.java
 *
 * Created on 23. srpen 2004, 17:25
 */

import java.awt.event.KeyEvent;
import javax.swing.text.Document;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import lib.JavaEditorTestCase;
import org.netbeans.jemmy.operators.JEditorPaneOperator;


/**
 *
 * @author  Petr Felenda
 */
public class SmartEnterTest extends JavaEditorTestCase {
      
    private final int keyCode = KeyEvent.VK_ENTER;
      
    /** Creates a new instance of Main */
    public SmartEnterTest(String testMethodName) {
        super(testMethodName);
    }
    

    public void testSmartEnter(){
        openDefaultProject();
        openDefaultSampleFile();
        try {
            
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // 1. move to adequate place 
            editor.setCaretPosition(5, 28);

            // 2. hit Enter 
            JEditorPaneOperator txtOper = editor.txtEditorPane();
            txtOper.pushKey(KeyEvent.VK_ENTER);

            // Compare document content to golden file
            compareReferenceFiles(txtOper.getDocument());

        } finally {
            closeFileWithDiscard();
        }
    }
    
}
