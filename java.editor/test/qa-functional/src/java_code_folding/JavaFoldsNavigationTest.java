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

package java_code_folding;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import javax.swing.KeyStroke;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import lib.EditorTestCase;
import code_folding.CodeFoldingTest;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;

/**
 * Test behavior of navigation through java code folds.
 *
 * Test covers following actions:
 * caret-forward [RIGHT]
 * caret-backward [LEFT]
 * caret-down [DOWN]
 * caret-up [UP]
 * selection-forward [SHIFT-RIGHT]
 * selection-backward [SHIFT-LEFT]
 * selection-down [SHIFT-DOWN]
 * selection-up [SHIFT-UP]
 * caret-begin-line [HOME]
 * caret-end-line [END]
 * selection-begin-line [SHIFT-HOME]
 * selection-end-line [SHIFT-END]
 *
 * Actions:
 * caret-next-word [CTRL-RIGHT]
 * caret-previous-word [CTRL-LEFT]
 * selection-next-word [CTRL-SHIFT-RIGHT]
 * selection-previous-word [CTRL-SHIFT-LEFT]
 * should be added to testcase after issue #47454 will be fixed
 *
 * @author Martin Roskanin
 */
  public class JavaFoldsNavigationTest extends JavaCodeFoldingTest {

    private JEditorPaneOperator txtOper;
    private EditorOperator editor;
     
    /** Creates a new instance of Main */
    public JavaFoldsNavigationTest(String testMethodName) {
        super(testMethodName);
    }
    
    private ValueResolver getResolver(final JEditorPaneOperator txtOper, final int etalon){
        ValueResolver resolver = new ValueResolver(){
            public Object getValue(){
                int newCaretPos = txtOper.getCaretPosition();
                return (newCaretPos == etalon) ? Boolean.TRUE : Boolean.FALSE;
            }
        };
        
        return resolver;
    }
    
    private void checkActionByKeyStroke(int key, int mod, int caretPosToSet, int etalon, boolean checkSelection){
        if (caretPosToSet == -1){
            caretPosToSet = txtOper.getCaretPosition();
        }else{
            editor.setCaretPosition(caretPosToSet);
            txtOper.getCaret().setMagicCaretPosition(null);
        }
        txtOper.pushKey(key,mod);
        waitMaxMilisForValue(3500, getResolver(txtOper, etalon), Boolean.TRUE);
        int newCaretOffset = txtOper.getCaretPosition();
        if (checkSelection){
            int selectionStart = txtOper.getSelectionStart();
            int selectionEnd = txtOper.getSelectionEnd(); 
            if (selectionStart != Math.min(caretPosToSet, etalon) ||
                    selectionEnd != Math.max(caretPosToSet, etalon)){
                String keyString = KeyStroke.getKeyStroke(key, mod).toString();
                fail(keyString+": Action failed: [etalon/newCaretOffset/selectionStart/selectionEnd]: ["+etalon+"/"+
                        newCaretOffset+"/"+selectionStart+"/"+selectionEnd+"]");
            }
        }else{
            if (etalon != newCaretOffset){
                String keyString = KeyStroke.getKeyStroke(key, mod).toString();
                fail(keyString+": Action failed: [etalon/newCaretOffset]: ["+etalon+"/"+
                        newCaretOffset+"]");
            }
        }
    }
    
    public void testJavaFoldsNavigation(){
        openDefaultProject();
        openDefaultSampleFile();
        try {
            editor = getDefaultSampleEditorOperator();
            JTextComponentOperator txtCompOper = new JTextComponentOperator(editor);
            JTextComponent target = (JTextComponent)txtCompOper.getSource();
            txtOper = editor.txtEditorPane();

            // wait max. 6 second for code folding initialization
            waitForFolding(target, 6000);

            //01 collapse initial comment fold. [ */|]
            // check caret left action
            collapseFoldAtCaretPosition(editor, 4, 4); // 4,4 -caret offset 70
            
            // check left
            checkActionByKeyStroke(KeyEvent.VK_LEFT, 0, 70, 0, false);
            
            //check selectin
            checkActionByKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK, 70, 0, true);
            
            // check caret right action
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, 0, 0, 70, false);
            
            // check caret right action, selection
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK, 0, 70, true);
            
            // check home action
            checkActionByKeyStroke(KeyEvent.VK_HOME, 0, 70, 0, false);
            
            // check home action, selection
            checkActionByKeyStroke(KeyEvent.VK_HOME, KeyEvent.SHIFT_DOWN_MASK, 70, 0, true);

            // check end action
            checkActionByKeyStroke(KeyEvent.VK_END, 0, 0, 70, false);
            
            // check end action, selection
            checkActionByKeyStroke(KeyEvent.VK_END, KeyEvent.SHIFT_DOWN_MASK, 0, 70, true);
            
            // check up action
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 71, 0, false);
            
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 71, 0, true);
            
            // check down action
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 0, 71, false);
            
            // check down action, selection
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 0, 71, true);

            // checking end of fold
            // check up action
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 78, 70, false);
            
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 78, 70, true);
            
            // check down action
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 70, 78, false);
            
            // check down action, selection
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 70, 78, true);
            
            // check magic position
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 80, 70, false);
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, -1, 80, false);
            
            
            // ------------------------------------------------------------------------
            
            
            // check actions on one-line fold
            collapseFoldAtCaretPosition(editor, 25, 13); // 25,13 - caret offset 422

            
            // check left
            checkActionByKeyStroke(KeyEvent.VK_LEFT, 0, 454, 414, false);
            
            //check selectin
            checkActionByKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK, 454, 414, true);
            
            // check caret right action
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, 0, 414, 454, false);
            
            // check caret right action, selection
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK, 414, 454, true);
            
            // check home action
            checkActionByKeyStroke(KeyEvent.VK_HOME, 0, 454, 414, false);
            
            // check home action, selection
            checkActionByKeyStroke(KeyEvent.VK_HOME, KeyEvent.SHIFT_DOWN_MASK, 454, 414, true);

            // check end action
            checkActionByKeyStroke(KeyEvent.VK_END, 0, 414, 454, false);
            
            // check end action, selection
            checkActionByKeyStroke(KeyEvent.VK_END, KeyEvent.SHIFT_DOWN_MASK, 414, 454, true);
            
            // check up action
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 459, 414, false);
            
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 459, 414, true);
            
            // check down action
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 414, 459, false);
            
            // check down action, selection
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 414, 459, true);

            // checking end of fold
            // check up action
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 467, 454, false);
            
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 467, 454, true);
            
            // check down action
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 454, 467, false);
            
            // check down action, selection
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 454, 467, true);
            
            // check magic position
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 469, 454, false);
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, -1, 469, false);
            
            //----------------------------------------------------------------
            //check multi fold on line

            collapseFoldAtCaretPosition(editor, 36, 84); // 36,84 -caret offset 897
            
            // check left
            checkActionByKeyStroke(KeyEvent.VK_LEFT, 0, 898, 896, false);
            
            //check selectin
            checkActionByKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK, 898, 896, true);
            
            // check caret right action
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, 0, 896, 898, false);
            
            // check caret right action, selection
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK, 896, 898, true);
            
            // check home action
            checkActionByKeyStroke(KeyEvent.VK_HOME, 0, 898, 818, false);
            
            // check home action, selection
            checkActionByKeyStroke(KeyEvent.VK_HOME, KeyEvent.SHIFT_DOWN_MASK, 898, 818, true);

            // check end action
            checkActionByKeyStroke(KeyEvent.VK_END, 0, 896, 926, false);
            
            // check end action, selection
            checkActionByKeyStroke(KeyEvent.VK_END, KeyEvent.SHIFT_DOWN_MASK, 896, 926, true);
            
            // check up action
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 1010, 896, false);
            
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 1010, 896, true);
            
            // check down action
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 896, 1009, false);
            
            // check down action, selection
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 896, 1009, true);

            // checking end of fold
            // check up action
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 1014, 898, false);
            
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 1014, 898, true);
            
            // check down action
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 898, 1014, false);
            
            // check down action, selection
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 898, 1014, true);
            
            // check magic position
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 1011, 896, false);
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, -1, 1011, false);
            
            
        } finally{
            closeFileWithDiscard();    
        }
    }
    
}
