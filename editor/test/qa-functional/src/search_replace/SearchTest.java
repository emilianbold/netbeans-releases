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
            find.btHelp().doClick();
            
            // close help
            HelpOperator help = new HelpOperator();
            help.close();
            
            // close find
            find.btClose().doClick();
            
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
            assertEquals(editor.lblStatusBar().getText(),
                    "'public' found at 16:5");
            
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
            assertEquals(editor.lblStatusBar().getText(),
                    "'testFindSelectionRepeated' found at 15:35");
            
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
            find.cboFindWhat().getTextField().setText("package");
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
            
            // search for an item from history - word "package"
            new FindAction().perform();
            Find find3 = new Find();
            JComboBoxOperator cbo = find3.cboFindWhat();
            cbo.selectItem(1);
            find3.find();
            new EventTool().waitNoEvent(1000);
            find3.close();
            // check status bar
            assertEquals(editor.lblStatusBar().getText(),
                    "'package' found at 7:1; End of document reached. "
                    +"Continuing search from beginning.");
            
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
            find.cbHighlightSearch().setSelected(false);
            find.cbIncrementalSearch().setSelected(false);
            find.cbWrapSearch().setSelected(false);
            find.find();
            new EventTool().waitNoEvent(1000);
            find.close();
            // check status bar
            assertEquals(editor.lblStatusBar().getText(),
                    "'cLaSs' found at 13:8");
            
        } finally {
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC5 - Nothing Found
     */
    public void testNothingFound() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // perform search for nonexistent word
            new FindAction().perform();
            Find find = new Find();
            JComboBoxOperator cbo = find.cboFindWhat();
            cbo.getTextField().setText("foo");
            find.find();
            new EventTool().waitNoEvent(1000);
            find.close();
            // check status bar
            assertEquals(editor.lblStatusBar().getText(), "'foo' not found");
            
        } finally {
            closeFileWithDiscard();
        }        
    }
    
    /**
     * TC6 - Match Case
     */
    public void testMatchCase() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // perform case sensitive search - nothing found 
            new FindAction().perform();
            Find find = new Find();
            JComboBoxOperator cbo = find.cboFindWhat();
            cbo.getTextField().setText("Package");
            find.cbMatchCase().setSelected(true);
            find.find();
            new EventTool().waitNoEvent(1000);
            find.close();
            // check status bar
            assertEquals(editor.lblStatusBar().getText(), 
                    "'Package' not found");

            // perform case sensitive search - package found
            editor.setCaretPosition(0);            
            new FindAction().perform();
            Find find2 = new Find();
            JComboBoxOperator cbo2 = find2.cboFindWhat();
            cbo2.getTextField().setText("package");
            find2.find();
            new EventTool().waitNoEvent(1000);
            find2.close();
            // check status bar
            assertEquals(editor.lblStatusBar().getText(), 
                    "'package' found at 7:1");
            
        } finally {
            closeFileWithDiscard();
        }                
    }

    /**
     * TC7 - Smart Case
     */
    public void testSmartCase() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // perform smart case search 
            new FindAction().perform();
            Find find = new Find();
            JComboBoxOperator cbo = find.cboFindWhat();
            cbo.getTextField().setText("smarttest");
            // uncheck match case
            find.cbMatchCase().setSelected(false);
            // check smart case
            find.cbSmartCase().setSelected(true);
            find.find();
            new EventTool().waitNoEvent(1000);
            find.close();
            // check status bar            
            assertEquals(editor.lblStatusBar().getText(), 
                    "'smarttest' found at 17:16");
            
            // perform smart case search 
            new FindAction().perform();
            Find find2 = new Find();
            JComboBoxOperator cbo2 = find.cboFindWhat();
            cbo2.getTextField().setText("smarttest");
            find2.find();
            new EventTool().waitNoEvent(1000);
            find.close();
            // check status bar            
            assertEquals(editor.lblStatusBar().getText(), 
                    "'smarttest' found at 18:16");

            // perform smart case search 
            new FindAction().perform();
            Find find3 = new Find();
            JComboBoxOperator cbo3 = find.cboFindWhat();
            cbo3.getTextField().setText("smarttest");
            find3.find();
            new EventTool().waitNoEvent(1000);
            find3.close();
            // check status bar            
            assertEquals(editor.lblStatusBar().getText(), 
                    "'smarttest' found at 19:16");

            // perform smart case search - negative
            new FindAction().perform();
            Find find4 = new Find();
            JComboBoxOperator cbo4 = find.cboFindWhat();
            cbo4.getTextField().setText("smarttest");
            find4.find(); 
            new EventTool().waitNoEvent(1000); 
            find4.close(); 
            // check status bar             
            assertEquals(editor.lblStatusBar().getText(),  
                    "'smarttest' not found"); 
            
        } finally {
            closeFileWithDiscard();
        }        
    }    

    /**
     * TC8 - Smart Case Reverse
     */
    public void testSmartCaseReverse() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // perform search for Smarttest (found Smarttest)
            new FindAction().perform();
            Find find = new Find();
            JComboBoxOperator cbo = find.cboFindWhat();
            cbo.getTextField().setText("Smarttest");
            find.find();
            new EventTool().waitNoEvent(1000);
            find.close();
            // check status bar
            assertEquals(editor.lblStatusBar().getText(), 
                    "'Smarttest' found at 18:16");

            // perform smart case search - negative
            new FindAction().perform();
            Find find2 = new Find();
            JComboBoxOperator cbo2 = find2.cboFindWhat();
            cbo2.getTextField().setText("Smarttest");
            find2.find(); 
            new EventTool().waitNoEvent(1000); 
            find2.close(); 
            // check status bar             
            assertEquals(editor.lblStatusBar().getText(),  
                    "'Smarttest' not found"); 
            
        } finally {
            closeFileWithDiscard();
        }        
    }
    
    /**
     * TC9 - Match Whole Words Only
     */
    public void testMatchWholeWordsOnly() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // perform search for "word"
            new FindAction().perform();
            Find find = new Find();
            find.cbMatchCase().setSelected(false);
            find.cbMatchWholeWordsOnly().setSelected(true);
            JComboBoxOperator cbo = find.cboFindWhat();
            cbo.getTextField().setText("word");
            find.find();
            new EventTool().waitNoEvent(1000);
            find.close();
            // check status bar
            assertEquals(editor.lblStatusBar().getText(), 
                    "'word' found at 18:16");

            // perform search for "word"
            new FindAction().perform();
            Find find2 = new Find();
            JComboBoxOperator cbo2 = find2.cboFindWhat();
            cbo2.getTextField().setText("word");
            find2.find();
            new EventTool().waitNoEvent(1000);
            find2.close();
            // check status bar
            assertEquals(editor.lblStatusBar().getText(), 
                    "'word' found at 18:24");

            // perform search for "word"
            new FindAction().perform();
            Find find3 = new Find();
            JComboBoxOperator cbo3 = find3.cboFindWhat();
            cbo3.getTextField().setText("word");
            find3.find();
            new EventTool().waitNoEvent(1000);
            find3.close();
            // check status bar
            assertEquals(editor.lblStatusBar().getText(), 
                    "'word' found at 19:25");

            // perform search for "word" - negative
            new FindAction().perform();
            Find find4 = new Find();
            JComboBoxOperator cbo4 = find4.cboFindWhat();
            cbo4.getTextField().setText("word");
            find4.find();
            new EventTool().waitNoEvent(1000);
            find4.close();
            // check status bar
            assertEquals(editor.lblStatusBar().getText(), 
                    "'word' not found");
            
        } finally {
            closeFileWithDiscard();
        }        
    }    
}
