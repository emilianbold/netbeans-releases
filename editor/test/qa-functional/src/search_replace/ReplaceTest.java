/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package search_replace;

import java.awt.event.KeyEvent;
import lib.EditorTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.HelpOperator;
import org.netbeans.jellytools.actions.ReplaceAction;
import org.netbeans.jellytools.modules.editor.Replace;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JEditorPaneOperator;

/**
 *
 * @author Roman Strobl
 */
public class ReplaceTest extends EditorTestCase {
    
    private static int REPLACE_TIMEOUT = 1000;
    
    /** Creates a new instance of ReplaceTest */
    public ReplaceTest(String testMethodName) {
        super(testMethodName);
    }
    
    /**
     * TC1 - open and close replace dialog
     */
    public void testReplaceDialogOpenClose() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            JEditorPaneOperator txtOper = editor.txtEditorPane();
            
            // open replace and close
            new ReplaceAction().perform();
            txtOper.pushKey(KeyEvent.VK_ESCAPE);
            
            // open replace and open help
            txtOper.pushKey(KeyEvent.VK_F, KeyEvent.CTRL_MASK);
            Replace replace = new Replace();
            replace.btHelp().doClick();
            
            // close help
            HelpOperator help = new HelpOperator();
            help.close();
            
            // close replace
            replace.btClose().doClick();
            
        } finally {
            closeFileWithDiscard();
        }
    }   
    
    /**
     * TC2 - Replace Dialog Open - Selection
     */
    public void testReplaceSelectionRepeated() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // choose the "testFindSelectionRepeated" word
            editor.select(13, 14, 41);
            new ReplaceAction().perform();
            Replace replace = new Replace();
            String text = replace.cboFindWhat().getTextField().getText();
            // compare
            assertEquals(text, "testReplaceSelectionRepeated");
            replace.cboReplaceWith().clearText();
            replace.cboReplaceWith().typeText("testReplaceSelectionRepeated2");
            replace.replace();
            new EventTool().waitNoEvent(REPLACE_TIMEOUT);
            replace.close();
            // check status bar
            log(editor.lblStatusBar().getText());
            waitForLabel("'testReplaceSelectionRepeated' found at 16:12");
            
            // choose the "testFindSelectionRepeated" word
            editor.select(15, 35, 62);
            new ReplaceAction().perform();
            Replace replace2 = new Replace();
            text = replace2.cboFindWhat().getTextField().getText();
            // compare
            assertEquals(text, "testReplaceSelectionRepeated");
            replace2.replace();
            new EventTool().waitNoEvent(REPLACE_TIMEOUT);
            replace2.close();
            // check status bar
            waitForLabel("'testReplaceSelectionRepeated' found at 2:4; End of document reached. "
                    +"Continuing search from beginning.");
        
        } finally {
            closeFileWithDiscard();
        }
    }    
    
    /**
     * TC3 - Replace Dialog Combo Box
     */
    public void testReplaceDialogComboBox() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            editor.setCaretPosition(1,1);
            new ReplaceAction().perform();
            Replace replace = new Replace();
            replace.cboFindWhat().clearText();
            replace.cboFindWhat().typeText("package");
            replace.cboReplaceWith().clearText();
            replace.cboReplaceWith().typeText("pakaz");
            replace.replace();
            // check status bar
            waitForLabel("'package' not found");
            
            replace.cboFindWhat().clearText();
            replace.cboFindWhat().typeText("class");
            replace.cboReplaceWith().clearText();
            replace.cboReplaceWith().typeText("klasa");
            replace.replace();
            // check status bar
            waitForLabel("'class' not found");

            replace.cboFindWhat().clearText();
            replace.cboFindWhat().typeText("testReplaceDialogComboBox");
            replace.cboReplaceWith().clearText();
            replace.cboReplaceWith().typeText("testReplaceDialogComboBox2");
            replace.replace();            
            // check status bar
            waitForLabel("'testReplaceDialogComboBox' found at 13:35");
            
            assertEquals((String)replace.cboFindWhat().getItemAt(0), 
                    "testReplaceDialogComboBox");
            assertEquals((String)replace.cboFindWhat().getItemAt(1), 
                    "class");
            assertEquals((String)replace.cboFindWhat().getItemAt(2), 
                    "package");

            assertEquals((String)replace.cboReplaceWith().getItemAt(0), 
                    "testReplaceDialogComboBox2");
            assertEquals((String)replace.cboReplaceWith().getItemAt(1), 
                    "klasa");
            assertEquals((String)replace.cboReplaceWith().getItemAt(2), 
                    "pakaz");
                        
            new EventTool().waitNoEvent(REPLACE_TIMEOUT);
            replace.close();
            
        
        } finally {
            closeFileWithDiscard();
        }
    }
    
    /**
     * Waits for label to appear on Status Bar, checks it 10 times before 
     * failing.
     * @param label label which should be displayed on status bar
     */
    public void waitForLabel(String label) {
        EditorOperator editor = getDefaultSampleEditorOperator();
        for (int i = 0; i<10; i++) {
            if (editor.lblStatusBar().getText().equals(label)) break;
            new EventTool().waitNoEvent(REPLACE_TIMEOUT);
        }
        assertEquals(editor.lblStatusBar().getText(), label);
    }    
    
}
