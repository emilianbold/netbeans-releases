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
import org.netbeans.jellytools.EditorOperator;
import lib.EditorTestCase;
import org.netbeans.jellytools.HelpOperator;
import org.netbeans.jellytools.actions.FindAction;
import org.netbeans.jellytools.modules.editor.Find;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;

/**
 * Test of find functionality in editor.
 *
 * @author Roman Strobl
 */
public class SearchTest extends EditorTestCase {
    
    /** Creates a new instance of Main */
    public SearchTest(String testMethodName) {
        super(testMethodName);
    }
    
    /**
     * TC1 - open and close find dialog
     */
    public void testFindDialogOpenClose() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            JEditorPaneOperator txtOper = editor.txtEditorPane();

            // open find and close
            new FindAction().perform();
            txtOper.pushKey(KeyEvent.VK_ESCAPE);
            
            // open find and open help
            txtOper.pushKey(KeyEvent.VK_F, KeyEvent.CTRL_MASK);
            Find find = new Find();
            find.btHelp().clickMouse();
            
            // close help
            HelpOperator help = new HelpOperator();
            help.close();
            
            // close find
            find.btClose().clickMouse();
            
        } finally {
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC2 - Find Selection Repeated
     */
    public void testFindSelectionRepeated() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // choose the "public" word
            editor.select(13, 1, 6);
            new FindAction().perform();
            Find find = new Find();
            String text = find.cboFindWhat().getTextField().getText();
            // compare
            assertEquals(text, "public");
            find.find();
            new EventTool().waitNoEvent(1000);
            find.close();
            // check status bar
            assertEquals(editor.lblStatusBar().getText(), "'public' found at 16:5");

            // choose the "testFindSelectionRepeated" word
            editor.select(13, 14, 38);
            new FindAction().perform();
            Find find2 = new Find();
            text = find.cboFindWhat().getTextField().getText();
            // compare
            assertEquals(text, "testFindSelectionRepeated");
            find2.find();
            new EventTool().waitNoEvent(1000);
            find2.close();
            // check status bar
            assertEquals(editor.lblStatusBar().getText(), "'testFindSelectionRepeated' found at 15:35");
            
        } finally {
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC3 - Find Dialog Combo Box
     */
    public void testFindDialogComboBox() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
                       
            // first search
            editor.setCaretPosition(0);
            new FindAction().perform();
            Find find = new Find();
            find.cboFindWhat().getTextField().setText("public");
            find.find();
            new EventTool().waitNoEvent(1000);
            find.close();
            
            // second search
            editor.setCaretPosition(0);
            new FindAction().perform();
            Find find2 = new Find();
            find2.cboFindWhat().getTextField().setText("class");
            find2.find();
            new EventTool().waitNoEvent(1000);
            find2.close();
            
            // search for an item from history - word "public"
            editor.setCaretPosition(0);
            new FindAction().perform();
            Find find3 = new Find();
            JComboBoxOperator cbo = find3.cboFindWhat();
            cbo.selectItem(1);
            find3.find();
            new EventTool().waitNoEvent(1000);
            find3.close();
            // check status bar
            assertEquals(editor.lblStatusBar().getText(), "'public' found at 16:5");
                        
        } finally {
            closeFileWithDiscard();
        }        
    }
    
    /**
     * TC4 - Unselected All Options
     */
    public void testUnselectedAllOptions() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // perform search with all options unselected
            editor.setCaretPosition(0);
            new FindAction().perform();
            Find find = new Find();
            JComboBoxOperator cbo = find.cboFindWhat();
            cbo.getTextField().setText("cLaSs");
            find.cbHighlightSearch().clickMouse();
            find.cbIncrementalSearch().clickMouse();
            find.cbWrapSearch().clickMouse();
            find.find();
            new EventTool().waitNoEvent(1000);
            find.close();
            // check status bar
            assertEquals(editor.lblStatusBar().getText(), "'cLaSs' found at 13:8");
            
        } finally {
            closeFileWithDiscard();
        }
    }
}
