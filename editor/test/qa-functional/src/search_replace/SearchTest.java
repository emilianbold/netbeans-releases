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

package search_replace;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.EditorOperator;
import lib.EditorTestCase;
import org.netbeans.jellytools.HelpOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.FindAction;
import org.netbeans.jellytools.modules.editor.Find;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.WindowOperator;

/**
 * Test of find functionality in editor.
 *
 * @author Roman Strobl
 */
public class SearchTest extends EditorTestCase {
    
    private static int FIND_TIMEOUT = 1000;
    
    /**
     * Creates a new instance of Main
     * @param testMethodName name of test
     */
    public SearchTest(String testMethodName) {
        super(testMethodName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        closeFindDialogIfOpened();
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
            closeFindDialogIfOpened();
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
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find.close();
            // check status bar
            waitForLabel("'public' found at 16:5");
            
            // choose the "testFindSelectionRepeated" word
            editor.select(13, 14, 38);
            new FindAction().perform();
            Find find2 = new Find();
            text = find2.cboFindWhat().getTextField().getText();
            // compare
            assertEquals("testFindSelectionRepeated",text);
            find2.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find2.close();
            // check status bar
            waitForLabel("'testFindSelectionRepeated' found at 15:35");
            
        } finally {
            closeFindDialogIfOpened();
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
            editor.setCaretPosition(1, 1);
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            new FindAction().perform();
            Find find = new Find();
            find.cboFindWhat().typeText("package");
            find.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find.close();
            
            // second search
            editor.setCaretPosition(1, 1);
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            new FindAction().perform();
            Find find2 = new Find();
            find2.cboFindWhat().typeText("class");
            find2.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find2.close();
            
            // search for an item from history - word "package"
            new FindAction().perform();
            Find find3 = new Find();
            JComboBoxOperator cbo = find3.cboFindWhat();
            cbo.selectItem(1);
            find3.cbWrapSearch().setSelected(true);
            find3.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find3.close();
            // check status bar
            waitForLabel("'package' found at 7:1; End of document reached. "
                    +"Continuing search from beginning.");
            
        } finally {
            closeFindDialogIfOpened();
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
            editor.setCaretPosition(1, 1);
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            new FindAction().perform();
            Find find = new Find();
            find.cboFindWhat().typeText("cLaSs");
            uncheckAll();
            find.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find.close();
            // check status bar
            waitForLabel("'cLaSs' found at 13:8");
            
        } finally {
            closeFindDialogIfOpened();
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
            uncheckAll();
            find.cboFindWhat().typeText("foo");
            find.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find.close();
            // check status bar
            waitForLabel("'foo' not found");
            
        } finally {
            closeFindDialogIfOpened();
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
            find.cboFindWhat().typeText("Package");
            uncheckAll();
            find.cbMatchCase().setSelected(true);
            find.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find.close();
            // check status bar
            waitForLabel("'Package' not found");
            
            // perform case sensitive search - package found
            editor.setCaretPosition(1, 1);
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            new FindAction().perform();
            Find find2 = new Find();
            find2.cboFindWhat().typeText("package");
            find2.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find2.close();
            // check status bar
            waitForLabel("'package' found at 7:1");
            
        } finally {
            closeFindDialogIfOpened();
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC7 - Smart Case
     */
    /*public void testSmartCase() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // perform smart case search
            new FindAction().perform();
            Find find = new Find();
            find.cboFindWhat().typeText("smarttest");
            uncheckAll();
            // check smart case
            find.cbSmartCase().setSelected(true);
            find.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find.close();
            // check status bar
            waitForLabel("'smarttest' found at 17:16");
            
            // perform smart case search
            new FindAction().perform();
            Find find2 = new Find();
            find2.cboFindWhat().typeText("smarttest");
            find2.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find2.close();
            // check status bar
            waitForLabel("'smarttest' found at 18:16");
            
            // perform smart case search
            new FindAction().perform();
            Find find3 = new Find();
            find3.cboFindWhat().typeText("smarttest");
            find3.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find3.close();
            // check status bar
            waitForLabel("'smarttest' found at 19:16");
            
            // perform smart case search - negative
            new FindAction().perform();
            Find find4 = new Find();
            find4.cboFindWhat().typeText("smarttest");
            find4.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find4.close();
            // check status bar
            waitForLabel("'smarttest' not found");
            
        } finally {
            closeFindDialogIfOpened();
            closeFileWithDiscard();
        }
    }*/
    
    /**
     * TC8 - Smart Case Reverse
     */
    /*public void testSmartCaseReverse() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // perform search for Smarttest (found Smarttest)
            new FindAction().perform();
            Find find = new Find();
            uncheckAll();
            // check smart case
            find.cbSmartCase().setSelected(true);
            find.cboFindWhat().typeText("Smarttest");
            find.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find.close();
            // check status bar
            waitForLabel("'Smarttest' found at 18:16");
            
            // perform smart case search - negative
            new FindAction().perform();
            Find find2 = new Find();
            find2.cboFindWhat().typeText("Smarttest");
            find2.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find2.close();
            // check status bar
            waitForLabel("'Smarttest' not found");
            
        } finally {
            closeFindDialogIfOpened();
            closeFileWithDiscard();
        }
    }*/
    
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
            uncheckAll();
            find.cbMatchWholeWordsOnly().setSelected(true);
            find.cboFindWhat().typeText("word");
            find.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find.close();
            // check status bar
            waitForLabel("'word' found at 18:16");
            
            // perform search for "word"
            new FindAction().perform();
            Find find2 = new Find();
            find2.cboFindWhat().typeText("word");
            find2.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find2.close();
            // check status bar
            waitForLabel("'word' found at 18:24");
            
            // perform search for "word"
            new FindAction().perform();
            Find find3 = new Find();
            find3.cboFindWhat().typeText("word");
            find3.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find3.close();
            // check status bar
            waitForLabel("'word' found at 19:25");
            
            // perform search for "word" - negative
            new FindAction().perform();
            Find find4 = new Find();
            find4.cboFindWhat().typeText("word");
            find4.find();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            find4.close();
            // check status bar
            waitForLabel("'word' not found");
            
        } finally {
            closeFindDialogIfOpened();
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC10 - Highlight Search
     */
    public void testHighlightSearch() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // test search highlighting - only checkbox
            new FindAction().perform();
            Find find = new Find();
            uncheckAll();
            find.cbHighlightSearch().setSelected(true);
            find.cboFindWhat().typeText("testHighlightSearch");
            find.find();
            waitForLabel("'testHighlightSearch' found at 2:4");
            find.find();
            waitForLabel("'testHighlightSearch' found at 13:14");
            find.find();
            waitForLabel("'testHighlightSearch' found at 15:35");
            find.find();
            waitForLabel("'testHighlightSearch' found at 16:12");
            find.find();
            waitForLabel("'testHighlightSearch' not found");
            find.close();
            
        } finally {
            closeFindDialogIfOpened();
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC11 - Incremental Search
     */
    public void testIncrementalSearch() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // perform search searched word, only checks checkbox
            new FindAction().perform();
            Find find = new Find();
            uncheckAll();
            find.cbHighlightSearch().setSelected(true);
            find.cboFindWhat().typeText("searchedWord");
            for (int i = 0; i<10; i++) {
                find.find();
                waitForLabel("'searchedWord' found at "+String.valueOf(i+17)
                        +":12");
            }
            find.close();
            
        } finally {
            closeFindDialogIfOpened();
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC12 - Backward Search
     */
    public void testBackwardSearch() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // perform backward search
            new FindAction().perform();
            Find find = new Find();
            uncheckAll();
            find.cboFindWhat().typeText("first");
            find.find();
            waitForLabel("'first' found at 21:12");
            find.cboFindWhat().clearText();
            find.cboFindWhat().typeText("second");
            find.cbBackwardSearch().setSelected(true);
            find.find();
            waitForLabel("'second' found at 20:12");
            find.cboFindWhat().clearText();
            find.cboFindWhat().typeText("third");
            find.find();
            waitForLabel("'third' found at 19:12");
            find.cboFindWhat().clearText();
            find.cboFindWhat().typeText("fourth");
            find.find();
            waitForLabel("'fourth' found at 18:12");
            find.close();
            
        } finally {
            closeFindDialogIfOpened();
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC13 - Wrap Search
     */
    public void testWrapSearch() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // perform backward search
            new FindAction().perform();
            Find find = new Find();
            uncheckAll();
            find.cbWrapSearch().setSelected(true);
            find.cboFindWhat().typeText("wrapWord");
            for (int i = 0; i<4; i++) {
                find.find();
                waitForLabel("'wrapWord' found at "+String.valueOf(i+18)
                        +":12");
            }
            find.find();
            waitForLabel("'wrapWord' found at 18:12; End of document reached. "
                    +"Continuing search from beginning.");
            for (int i = 0; i<3; i++) {
                find.find();
                waitForLabel("'wrapWord' found at "+String.valueOf(i+19)
                        +":12");
            }
            find.close();
            
        } finally {
            closeFindDialogIfOpened();
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC14 - Find Next - Previous
     */
    public void testFindNextPrevious() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // search first word
            editor.setCaretPosition(1, 1);
            new FindAction().perform();
            Find find = new Find();
            uncheckAll();
            find.cboFindWhat().clearText();
            find.cboFindWhat().typeText("testWord");
            find.find();
            waitForLabel("'testWord' found at 18:12");
            find.close();
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            
            // search next word
            for (int i = 0; i<7; i++) {
                MainWindowOperator.getDefault().pushKey(KeyEvent.VK_F3);
                waitForLabel("'testWord' found at "+String.valueOf(i+19)
                        +":12");
            }
            
            // search previous word
            for (int i = 7; i>0; i--) {
                MainWindowOperator.getDefault().pushKey(KeyEvent.VK_F3,
                        KeyEvent.SHIFT_MASK);
                waitForLabel("'testWord' found at "+String.valueOf(i+17)
                        +":12");
            }
        } finally {
            closeFindDialogIfOpened();
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC15 - Find Selection Without Dialog
     */
    public void testFindSelectionWithoutDialog() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // perform backward search
            editor.select(18, 12, 19);
            for (int i = 0; i<7; i++) {
                MainWindowOperator.getDefault().pushKey(KeyEvent.VK_F3);
                waitForLabel("'testWord' found at "+String.valueOf(i+19)
                        +":12");
            }
        } finally {
            closeFindDialogIfOpened();
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC16 - Search Selection
     */
    public void testSearchSelection() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
 
            // perform selection search
            editor.select(24, 20);
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            new FindAction().perform();
            Find find = new Find();
            uncheckAll();
            find.cbIncrementalSearch().setSelected(true);
            find.cbBlockSearch().setSelected(true);
            find.cboFindWhat().clearText();
            find.cboFindWhat().typeText("testWord2");
            find.find();
            waitForLabel("'testWord2' found at 22:12");
            find.close();
 
        } finally {
            closeFindDialogIfOpened();
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC17 - Search Selection Negative
     */
    public void testSearchSelectionNegative() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
 
            // perform negative selection search
            editor.select(25, 22);
            new EventTool().waitNoEvent(FIND_TIMEOUT);
            new FindAction().perform();
            Find find2 = new Find();
            uncheckAll();
            find2.cbIncrementalSearch().setSelected(true);
            find2.cbBlockSearch().setSelected(true);
            find2.cboFindWhat().clearText();
            find2.cboFindWhat().typeText("testWord2");
            find2.find();
            waitForLabel("'testWord2' not found");
            find2.close();
 
        } finally {
            closeFindDialogIfOpened();
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC18 - Regexp Search - Simple
     */
    public void testRegexpSimple() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // perform simple regexp search
            editor.setCaretPosition(1, 1);
            new FindAction().perform();
            Find find = new Find();
            uncheckAll();
            find.cbRegularExpressions().setSelected(true);
            find.cboFindWhat().clearText();
            find.cboFindWhat().typeText("[aA][hH][oO][jJ][0-9]{1,3}");
            find.find();
            waitForLabel("'[aA][hH][oO][jJ][0-9]{1,3}' found at 18:12");
            find.cboFindWhat().clearText();
            find.cboFindWhat().typeText("[aA][hH][oO][jJ][0-9]{1,3}");
            find.find();
            waitForLabel("'[aA][hH][oO][jJ][0-9]{1,3}' found at 19:12");
            find.cboFindWhat().clearText();
            find.cboFindWhat().typeText("[aA][hH][oO][jJ][0-9]{1,3}");
            find.find();
            waitForLabel("'[aA][hH][oO][jJ][0-9]{1,3}' found at 20:12");
            find.cboFindWhat().clearText();
            find.cboFindWhat().typeText("[aA][hH][oO][jJ][0-9]{1,3}");
            find.find();
            waitForLabel("'[aA][hH][oO][jJ][0-9]{1,3}' found at 21:12");
            find.cboFindWhat().clearText();
            find.cboFindWhat().typeText("[aA][hH][oO][jJ][0-9]{1,3}");
            find.find();
            waitForLabel("'[aA][hH][oO][jJ][0-9]{1,3}' found at 23:12");
            find.cboFindWhat().clearText();
            find.cboFindWhat().typeText("[aA][hH][oO][jJ][0-9]{1,3}");
            find.find();
            waitForLabel("'[aA][hH][oO][jJ][0-9]{1,3}' not found");
            find.close();
            
        } finally {
            closeFindDialogIfOpened();
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC19 - Regexp Search - Complex
     */
    public void testRegexpComplex() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            // perform simple regexp search
            editor.setCaretPosition(1, 1);
            new FindAction().perform();
            Find find = new Find();
            uncheckAll();
            find.cbRegularExpressions().setSelected(true);
            find.cboFindWhat().clearText();
            find.cboFindWhat().typeText("a?B*c{2}[dD]e{1,}\\.F{1,2}\\s[^g]");
            find.find();
            waitForLabel("'a?B*c{2}[dD]e{1,}\\.F{1,2}\\s[^g]' found at 18:12");
            find.cboFindWhat().clearText();
            find.cboFindWhat().typeText("a?B*c{2}[dD]e{1,}\\.F{1,2}\\s[^g]");
            find.find();
            waitForLabel("'a?B*c{2}[dD]e{1,}\\.F{1,2}\\s[^g]' found at 19:12");
            find.cboFindWhat().clearText();
            find.cboFindWhat().typeText("a?B*c{2}[dD]e{1,}\\.F{1,2}\\s[^g]");
            find.find();
            waitForLabel("'a?B*c{2}[dD]e{1,}\\.F{1,2}\\s[^g]' found at 21:12");
            find.cboFindWhat().clearText();
            find.cboFindWhat().typeText("a?B*c{2}[dD]e{1,}\\.F{1,2}\\s[^g]");
            find.find();
            waitForLabel("'a?B*c{2}[dD]e{1,}\\.F{1,2}\\s[^g]' found at 23:12");
            find.cboFindWhat().clearText();
            find.cboFindWhat().typeText("a?B*c{2}[dD]e{1,}\\.F{1,2}\\s[^g]");
            find.find();
            waitForLabel("'a?B*c{2}[dD]e{1,}\\.F{1,2}\\s[^g]' not found");
            find.close();
            
        } finally {
            closeFindDialogIfOpened();
            closeFileWithDiscard();
        }
    }
    
    /**
     * Unchecks all checkboxes in find dialog.
     */
    public void uncheckAll() {
        Find find = new Find();
        find.cbBackwardSearch().setSelected(false);
        find.cbBlockSearch().setSelected(false);
        find.cbHighlightSearch().setSelected(false);
        find.cbIncrementalSearch().setSelected(false);
        find.cbMatchCase().setSelected(false);
        find.cbMatchWholeWordsOnly().setSelected(false);
        find.cbRegularExpressions().setSelected(false);
        //find.cbSmartCase().setSelected(false);
        find.cbWrapSearch().setSelected(false);
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
            new EventTool().waitNoEvent(FIND_TIMEOUT);
        }
        assertEquals(label,editor.lblStatusBar().getText());
    }
    
    
    /**
     * Checks if a find dialog is opened and if yes it closes it.
     */
    public void closeFindDialogIfOpened() {
        Window findWindow = WindowOperator.findWindow(
                new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                WindowOperator winOper = new WindowOperator((Window)comp);
                winOper.setOutput(TestOut.getNullOutput());
                return null != winOper.findSubComponent(
                        new ComponentChooser() {
                    public boolean checkComponent(Component comp) {
                        return comp.getClass().getName().startsWith(
                                "org.netbeans.editor.ext.Find"); //NOI18N
                    }
                    public String getDescription() {
                        return("any find dialog");  //NOI18N
                    }
                });
            }
            public String getDescription() {
                return "containing any find dialog";  //NOI18N
            }
        });
        if(findWindow != null) {
            new Find().close();
        }
    }
    
}
