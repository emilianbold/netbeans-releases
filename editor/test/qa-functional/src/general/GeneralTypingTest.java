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

package general;

import java.awt.event.KeyEvent;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import lib.EditorTestCase;
import org.netbeans.jemmy.operators.JEditorPaneOperator;

/**
 * Test of typing at begining/end and other typing tests.
 *
 * @author Miloslav Metelka
 */
public class GeneralTypingTest extends EditorTestCase {
      
    public GeneralTypingTest(String testMethodName) {
        super(testMethodName);
    }
    
    public void testJavaEnterBeginAndEnd(){
        openDefaultProject();
        openDefaultSampleFile();
        try {
        
            EditorOperator editor = getDefaultSampleEditorOperator();

            // 1. move to position [1:1]
            editor.setCaretPosition(0);

            // 2. hit Enter 
            JEditorPaneOperator txtOper = editor.txtEditorPane();
            txtOper.pushKey(KeyEvent.VK_ENTER);
            
            // 3. move to end of the file
            editor.setCaretPosition(txtOper.getDocument().getLength());
            
            // 4. hit Enter
            txtOper.pushKey(KeyEvent.VK_ENTER);

            // Compare document content to golden file
            compareReferenceFiles(txtOper.getDocument());

        } finally {
            closeFileWithDiscard();
        }
    }
 
}
