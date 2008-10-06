/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.test.editor.search;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.KeyEvent;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.actions.ReplaceAction;
import org.netbeans.test.editor.lib.EditorTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.HelpOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.modules.editor.Replace;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.WindowOperator;
import org.netbeans.junit.NbModuleSuite;

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
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        closeReplaceDialogIfOpened();
    }
    
    private void openReplaceDialog() {
        final MainWindowOperator mwo = MainWindowOperator.getDefault();        
        mwo.requestFocus();
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                new ReplaceAction().performAPI();                
            }
        });        
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
            openReplaceDialog();
            txtOper.pushKey(KeyEvent.VK_ESCAPE);
            new EventTool().waitNoEvent(200);
            // open replace and open help
            txtOper.requestFocus();
            txtOper.pushKey(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK);
            new EventTool().waitNoEvent(200);
            Replace replace = new Replace();
            replace.btHelp().doClick();
            
            // close help
            HelpOperator help = new HelpOperator();
            help.close();
            new EventTool().waitNoEvent(200);
            // close replace
            replace.btClose().doClick();
            
        } finally {
            closeReplaceDialogIfOpened();
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
            
            // choose the "testReplaceSelectionRepeated" word            
            editor.setCaretPosition(12,1);            
            new EventTool().waitNoEvent(REPLACE_TIMEOUT);
            openReplaceDialog();            
            Replace replace = new Replace();
            
            // check only selected checkboxes
            uncheckAll();
            replace.cbHighlightSearch().doClick();
            replace.cbIncrementalSearch().doClick();
            replace.cbWrapSearch().doClick();
            replace.cboFindWhat().getTextField().typeText("testReplaceSelectionRepeated");
            String text = replace.cboFindWhat().getTextField().getText();
            // compare
            assertEquals(text, "testReplaceSelectionRepeated");
            replace.cboReplaceWith().clearText();
            replace.cboReplaceWith().typeText("testReplaceSelectionRepeated2");   
            new EventTool().waitNoEvent(250);
            replace.replace();
            new EventTool().waitNoEvent(REPLACE_TIMEOUT);
            replace.close();
            // check status bar
            log(editor.lblStatusBar().getText());
            waitForLabel("'testReplaceSelectionRepeated' found at 15:35");            
            // choose the "testReplaceSelectionRepeated" word
            editor.setCaretPosition(15,1);            
            //editor.select(15, 35, 62);
            openReplaceDialog();
            replace = new Replace();
            replace.cboFindWhat().getTextField().typeText("testReplaceSelectionRepeated");
            replace.cboReplaceWith().typeText("testReplaceSelectionRepeated2");   
            Replace replace2 = new Replace();
            text = replace2.cboFindWhat().getTextField().getText();
            // compare
            assertEquals(text, "testReplaceSelectionRepeated");            
            replace2.replace();
            new EventTool().waitNoEvent(REPLACE_TIMEOUT);
            replace2.close();
            // check status bar
            waitForLabel("'testReplaceSelectionRepeated' found at 16:12");
            
            
            
            //waitForLabel("'testReplaceSelectionRepeated' found at 2:4; End of document reached. "
            //        +"Continuing search from beginning.");
            
            ref(editor.getText());
            compareReferenceFiles();
            
        } finally {
            closeReplaceDialogIfOpened();
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
            openReplaceDialog();
            Replace replace = new Replace();
            //replace.cboFindWhat().removeAllItems();
            //replace.cboReplaceWith().removeAllItems();
            
            // check only selected checkboxes
            uncheckAll();
            replace.cbHighlightSearch().doClick();
            replace.cbIncrementalSearch().doClick();
            replace.cbWrapSearch().doClick();
            
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
            
            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            for (int i = 0; i<replace.cboFindWhat().getItemCount(); i++) {
                if (((String)replace.cboFindWhat().getItemAt(i)).equals(
                        "testReplaceDialogComboBox")) found1 = true;
                if (((String)replace.cboFindWhat().getItemAt(i)).equals(
                        "class")) found2 = true;
                if (((String)replace.cboFindWhat().getItemAt(i)).equals(
                        "package")) found3 = true;
            }
            assertEquals(found1, true);
            assertEquals(found2, true);
            assertEquals(found3, true);
            
            found1 = false;
            found2 = false;
            found3 = false;
            for (int i = 0; i<replace.cboReplaceWith().getItemCount(); i++) {
                if (((String)replace.cboReplaceWith().getItemAt(i)).equals(
                        "testReplaceDialogComboBox2")) found1 = true;
                if (((String)replace.cboReplaceWith().getItemAt(i)).equals(
                        "klasa")) found2 = true;
                if (((String)replace.cboReplaceWith().getItemAt(i)).equals(
                        "pakaz")) found3 = true;
            }
            assertEquals(found1, true);
            assertEquals(found2, true);
            assertEquals(found3, true);
            
            new EventTool().waitNoEvent(REPLACE_TIMEOUT);
            replace.close();
            
            ref(editor.getText());
            compareReferenceFiles();
            
        } finally {
            closeReplaceDialogIfOpened();
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC4 - Replace Match Case
     */
    public void testReplaceMatchCase() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            editor.setCaretPosition(1,1);
            openReplaceDialog();
            Replace replace = new Replace();
            
            // check only selected checkboxes
            uncheckAll();
            replace.cbMatchCase().doClick();
            
            replace.cboFindWhat().clearText();
            replace.cboFindWhat().typeText("testCase");
            replace.cboReplaceWith().clearText();
            replace.cboReplaceWith().typeText("xxxxXxxx");
            replace.replace();
            
            replace.close();
            
            ref(editor.getText());
            compareReferenceFiles();
            
        } finally {
            closeReplaceDialogIfOpened();
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC5 - Replace All
     */
    public void testReplaceAll() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            editor.setCaretPosition(1,1);
            openReplaceDialog();
            Replace replace = new Replace();
            
            // check only selected checkboxes
            uncheckAll();
            
            replace.cboFindWhat().clearText();
            replace.cboFindWhat().typeText("testWord");
            replace.cboReplaceWith().clearText();
            replace.cboReplaceWith().typeText("xxxxXxxx");
            replace.replaceAll();
            // check status bar
            waitForLabel("14 of 14 items replaced");
            
            replace.close();
            
            ref(editor.getText());
            compareReferenceFiles();
            
        } finally {
            closeReplaceDialogIfOpened();
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC6 - Replace in Selection Only
     */
    public void testReplaceInSelectionOnly() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            
            editor.select(20, 24);
            openReplaceDialog();
            Replace replace = new Replace();
            
            // check only selected checkboxes
            uncheckAll();
            replace.cbSearchSelection().doClick();
            replace.cboFindWhat().clearText();
            replace.cboFindWhat().typeText("testWord");
            replace.cboReplaceWith().clearText();
            replace.cboReplaceWith().typeText("xxxxXxxx");
            replace.replaceAll();
            // check status bar
            waitForLabel("5 of 5 items replaced");
            
            replace.close();
            
            ref(editor.getText());
            compareReferenceFiles();
            
        } finally {
            closeReplaceDialogIfOpened();
            closeFileWithDiscard();
        }
    }
    
    /**
     * Unchecks all checkboxes in find dialog.
     */
    public void uncheckAll() {
        Replace replace = new Replace();
        replace.cbBackwardSearch().setSelected(false);
        replace.cbSearchSelection().setSelected(false);
        replace.cbHighlightSearch().setSelected(false);
        replace.cbIncrementalSearch().setSelected(false);
        replace.cbMatchCase().setSelected(false);
        replace.cbMatchWholeWordsOnly().setSelected(false);
        replace.cbRegularExpressions().setSelected(false);
        //replace.cbSmartCase().setSelected(false);
        replace.cbWrapSearch().setSelected(false);
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
        assertEquals(label, editor.lblStatusBar().getText());
    }
    
    /**
     * Checks if a replace dialog is opened and if yes it closes it.
     */
    public void closeReplaceDialogIfOpened() {
        Window replaceWindow = WindowOperator.findWindow(
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
                        return("any replace dialog");  //NOI18N
                    }
                });
            }
            public String getDescription() {
                return "containing any replace dialog";  //NOI18N
            }
        });
        if(replaceWindow != null) {
            new Replace().close();
        }
    }
    
    
    public static void main(String[] args) {
        TestRunner.run(ReplaceTest.class);                
    }
    
    public static Test suite() {
      return NbModuleSuite.create(
              NbModuleSuite.createConfiguration(ReplaceTest.class).enableModules(".*").clusters(".*"));
   }

}
