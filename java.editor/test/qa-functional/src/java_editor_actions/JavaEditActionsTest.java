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

package java_editor_actions;

import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import editor_actions.EditorActionsTest;
import junit.textui.TestRunner;
import org.netbeans.jemmy.EventTool;

/**
 * Basic Edit Actions Test class.
 * The base edit actions can be found at:
 * http://editor.netbeans.org/doc/UserView/apdx_a_eshortcuts.html
 *
 * Test covers following actions:
 *
 *
 * @author Martin Roskanin, Jiri Prox
 */
public class JavaEditActionsTest extends JavaEditorActionsTest {

    /** Creates a new instance of Main */
    public JavaEditActionsTest(String testMethodName) {
        super(testMethodName);
    }

    public void XtestEditActions() {
        resetCounter();
        openDefaultProject();
        openDefaultSampleFile();
        try {

            EditorOperator editor = getDefaultSampleEditorOperator();
            editor.requestFocus();
            JEditorPaneOperator txtOper = editor.txtEditorPane();

            // 00 ---------------------- test insert action -----------------
            // 1. move to adequate place
            editor.setCaretPosition(5, 17);
            // 2. set insert Mode ON
            txtOper.pushKey(KeyEvent.VK_INSERT);
            // 3. type d
            txtOper.typeKey('d');
            // 4. set insert Mode OFF
            txtOper.pushKey(KeyEvent.VK_INSERT);
            // 5. type x
            txtOper.typeKey('x');
            // previous word ins|ert, with caret at | should be modified to
            // insdxrt
            // Compare document content to golden file
            compareToGoldenFile(txtOper.getDocument());
            //------------------------------------------------------------
            // 01 -------- test delete word action. Caret in the middle of the word ---
            // remove-word action has been removed. Changing test to delete selected word
            editor.setCaretPosition(17, 20);
            txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_DELETE);
            compareToGoldenFile(txtOper.getDocument());

            // 02 -------- test delete previous word action. Caret after the word ------
            //  delete word - Caret after the word was removed
            txtOper.pushKey(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());

            // 03 --------- test remove the current line --------------------
            txtOper.pushKey(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());

            // 04 -- test Select the word the insertion point is on or
            // -- deselect any selected text (Alt + j)
            // -- after that test CUT action ---------------
            editor.setCaretPosition(9, 24);
            txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            cutCopyViaStrokes(txtOper, KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());

            // 05 -- test PASTE ------
            editor.setCaretPosition(11, 17);
            txtOper.pushKey(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());

            // 06 -- test UNDO/REDO ----
            int oldDocLenhth = txtOper.getDocument().getLength();
            txtOper.pushKey(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
            waitMaxMilisForValue(WAIT_MAX_MILIS_FOR_UNDO_REDO, getFileLengthChangeResolver(txtOper, oldDocLenhth), Boolean.FALSE);
            oldDocLenhth = txtOper.getDocument().getLength();
            txtOper.pushKey(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
            waitMaxMilisForValue(WAIT_MAX_MILIS_FOR_UNDO_REDO, getFileLengthChangeResolver(txtOper, oldDocLenhth), Boolean.FALSE);
            oldDocLenhth = txtOper.getDocument().getLength();
            txtOper.pushKey(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK);
            waitMaxMilisForValue(WAIT_MAX_MILIS_FOR_UNDO_REDO, getFileLengthChangeResolver(txtOper, oldDocLenhth), Boolean.FALSE);
            compareToGoldenFile(txtOper.getDocument());

            // 07 -- test CTRL+backspace -- delete previous word
            txtOper.pushKey(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());

            // 08 -- test CTRL+u -- delete the indentation level
            txtOper.pushKey(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());

            // 09 -- test CTRL+u -- delete the line break
            txtOper.pushKey(KeyEvent.VK_BACK_SPACE);
            txtOper.pushKey(KeyEvent.VK_BACK_SPACE);
            txtOper.typeKey(' ');
            compareToGoldenFile(txtOper.getDocument());

            // 10 -- test delete action
            txtOper.pushKey(KeyEvent.VK_BACK_SPACE);
            compareToGoldenFile(txtOper.getDocument());

            // 11 -- test delete selected block and selecting to end of the line
            txtOper.pushKey(KeyEvent.VK_END, KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_DELETE);
            compareToGoldenFile(txtOper.getDocument());

            // 12 -- test COPY action ---
            editor.setCaretPosition(9, 15);
            txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
            cutCopyViaStrokes(txtOper, KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
            editor.setCaretPosition(10, 17);
            txtOper.pushKey(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());

            // -- test Select All ---
            txtOper.pushKey(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK);
            if (txtOper.getSelectionStart() != 0 || txtOper.getSelectionEnd() != txtOper.getDocument().getLength()) {
                fail("Select all action fails. [start/end of selection] [docLength]: [" + txtOper.getSelectionStart() + "/" + txtOper.getSelectionEnd() + "] [" + txtOper.getDocument().getLength() + "]");
            }

            // 13 -- test Shift+delete (CUT) and shift+insert (PASTE)---
            editor.setCaretPosition(5, 17);
            txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            cutCopyViaStrokes(txtOper, KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK);
            editor.setCaretPosition(13, 8);
            txtOper.pushKey(KeyEvent.VK_INSERT, KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());

            // 14 -- test ctrl+insert (COPY)---
            editor.setCaretPosition(10, 20);
            txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            cutCopyViaStrokes(txtOper, KeyEvent.VK_INSERT, KeyEvent.CTRL_DOWN_MASK);
            editor.setCaretPosition(13, 15);
            txtOper.pushKey(KeyEvent.VK_INSERT, KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());

            // 15 -- test CTRL+K ----
            editor.setCaretPosition(6, 21);
            txtOper.pushKey(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());

            // 16 -- test CTRL+SHITF+K ----
            editor.setCaretPosition(10, 20);
            //type space to change String to Str ing
            txtOper.typeKey(' ');
            editor.setCaretPosition(10, 23);
            txtOper.pushKey(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());

            /// 17 -- test expanding abbreviation
            editor.setCaretPosition(19, 12);
            txtOper.typeKey('s');
            txtOper.typeKey('t');
            txtOper.pressKey(KeyEvent.VK_TAB);
            compareToGoldenFile(txtOper.getDocument());

            // 18 -- test Insert space without expanding abbreviation (SPACE)
            editor.setCaretPosition(20, 9);
            txtOper.typeKey('s');
            txtOper.typeKey('t');
            txtOper.typeKey(' ');
            compareToGoldenFile(txtOper.getDocument());

            /* __________________ Capitlization ___________________ */


            // 19 -- w/o selection upper case ------
            editor.setCaretPosition(13, 18);
            txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_U);
            compareToGoldenFile(txtOper.getDocument());

            // 20 -- selection upper case ------
            txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_U);
            compareToGoldenFile(txtOper.getDocument());

            // 21 -- w/o selection lower case ------
            editor.setCaretPosition(13, 18);
            txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_L);
            compareToGoldenFile(txtOper.getDocument());

            // 22 -- selection lower case ------
            txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_L);
            compareToGoldenFile(txtOper.getDocument());

            // 23 -- w/o selection reverse case ------
            editor.setCaretPosition(13, 18);
            txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_S);
            compareToGoldenFile(txtOper.getDocument());

            // 24 -- selection reverse case ------
            txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_S);
            compareToGoldenFile(txtOper.getDocument());


            /* __________________ Several Indentation Actions ___________________ */


            // 25 -- Shift left  ------
            editor.setCaretPosition(10, 9);
            txtOper.pushKey(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());

            // 26 -- insert tab  ------
            txtOper.pushKey(KeyEvent.VK_TAB);
            compareToGoldenFile(txtOper.getDocument());

            // 27 -- Shift selection left  ------
            editor.setCaretPosition(9, 1);
            //select method
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
            // shift left
            txtOper.pushKey(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());

            // 28 -- Shift  selection right  ------
            txtOper.pushKey(KeyEvent.VK_TAB);
            compareToGoldenFile(txtOper.getDocument());

            // 29 -- Shift selection left (Alt+Shift+left) ------
            editor.setCaretPosition(9, 1);
            //select method
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
            // shift left
            txtOper.pushKey(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());

            // 30 -- Shift  selection right (Alt+Shift+Right) ------
            txtOper.pushKey(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());

            //31 -- reformat the selection + testing BACK_SPACE----
            //delete syntax error - otherwise reformat will not work
            editor.setCaretPosition(20, 1);
            txtOper.pushKey(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK);
            //make a mess
            editor.setCaretPosition(6, 5);
            txtOper.typeKey(' ');
            editor.setCaretPosition(9, 5);
            txtOper.pushKey(KeyEvent.VK_BACK_SPACE);
            editor.setCaretPosition(9, 1);
            //select method
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_F, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());

            //32 -- reformat the entire file ----
            // deselect
            txtOper.setSelectionStart(1);
            txtOper.setSelectionEnd(1);
            // invoke formatter
            txtOper.pushKey(KeyEvent.VK_F, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());

            /* __________________ Extension Actions ___________________ */
//
//            //33 -- Prefix the identifier with get ------
//            editor.setCaretPosition(19,32);
//            txtOper.pushKey(KeyEvent.VK_ENTER);
//            new EventTool().waitNoEvent(2000);
//            txtOper.pushKey(KeyEvent.VK_S);
//            new EventTool().waitNoEvent(2000);
//            txtOper.pushKey(KeyEvent.VK_T);
//            new EventTool().waitNoEvent(2000);
//            editor.setCaretPosition(20, 10);
//            new EventTool().waitNoEvent(2000);
//            txtOper.pushKey(KeyEvent.VK_U, KeyEvent.ALT_DOWN_MASK);
//            txtOper.pushKey(KeyEvent.VK_G);
//            compareToGoldenFile(txtOper.getDocument());
//
//            //34 -- Prefix the identifier with set ------
//            txtOper.pushKey(KeyEvent.VK_U, KeyEvent.ALT_DOWN_MASK);
//            txtOper.pushKey(KeyEvent.VK_S);
//            compareToGoldenFile(txtOper.getDocument());
//
//            //35 -- Prefix the identifier with is ------
//            txtOper.pushKey(KeyEvent.VK_U, KeyEvent.ALT_DOWN_MASK);
//            txtOper.pushKey(KeyEvent.VK_I);
//            compareToGoldenFile(txtOper.getDocument());
//
//            //36 -- Comment out the current line ------
//            txtOper.pushKey(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//            compareToGoldenFile(txtOper.getDocument());
//
//            //37 -- Remove comment from the current line ------
//            txtOper.pushKey(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//            compareToGoldenFile(txtOper.getDocument());
//
//            //38 -- Comment out the selected lines of code. ------
//            editor.setCaretPosition(19, 1);
//            //select method
//            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
//            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
//            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
//            txtOper.pushKey(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//            compareToGoldenFile(txtOper.getDocument());
//
//            //39 -- Remove comment from the selected lines. ------
//            txtOper.pushKey(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//            compareToGoldenFile(txtOper.getDocument());
//
//            // 40 -- Paste Formatted action ----------
//            //make a mess
//            editor.setCaretPosition(6, 1);
//            txtOper.typeKey(' ');
//            editor.setCaretPosition(7, 1);
//            txtOper.pushKey(KeyEvent.VK_DELETE);
//            editor.setCaretPosition(6, 1);
//            //select method
//            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
//            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
//            //copy
//            cutCopyViaStrokes(txtOper, KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
//            editor.setCaretPosition(12, 1);
//            //paste formatted
//            txtOper.pushKey(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
//            compareToGoldenFile(txtOper.getDocument());
//
//            // 41 -- Split a line  (CTRL-ENTER)---
//            editor.setCaretPosition(15, 21);
//            txtOper.pushKey(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK);
//            txtOper.typeKey('x');
//            compareToGoldenFile(txtOper.getDocument());
//
//            // 42 -- Start a new line (SHIFT-ENTER) ---
//            editor.setCaretPosition(15, 15);
//            txtOper.pushKey(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
//            txtOper.typeKey('x');
//            compareToGoldenFile(txtOper.getDocument());
//
//            //------------- newly added action --------------
//
//            //43  -- remove previous word, caret at the middle of the word
//            editor.setCaretPosition(23, 15);
//            txtOper.pushKey(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK);
//            compareToGoldenFile(txtOper.getDocument());
//
//            //44  -- remove next word, caret at the middle of the word
//            editor.setCaretPosition(23, 7);
//            txtOper.pushKey(KeyEvent.VK_DELETE, KeyEvent.CTRL_DOWN_MASK);
//            compareToGoldenFile(txtOper.getDocument());
//
//            //45  -- remove previous word, caret after the word
//            editor.setCaretPosition(5, 14);
//            txtOper.pushKey(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK);
//            compareToGoldenFile(txtOper.getDocument());
//
//            //46  -- remove next word, caret before the word
//            editor.setCaretPosition(5, 10);
//            txtOper.pushKey(KeyEvent.VK_DELETE, KeyEvent.CTRL_DOWN_MASK);
//            compareToGoldenFile(txtOper.getDocument());
//
//            //47  -- remove previous word, caret at the middle of the first word in document
//            // #51866
//            editor.setCaretPosition(1, 5);
//            txtOper.pushKey(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK);
//            compareToGoldenFile(txtOper.getDocument());
//
//            //48  -- remove next word, caret at the middle of the last word in document
//            // #51866
//            editor.setCaretPosition(3, 4);
//            txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK);
//            cutCopyViaStrokes(txtOper, KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
//            txtOper.pushKey(KeyEvent.VK_END, KeyEvent.CTRL_DOWN_MASK);
//            txtOper.pushKey(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK);
//            txtOper.pushKey(KeyEvent.VK_LEFT, 0);
//            txtOper.pushKey(KeyEvent.VK_LEFT, 0);
//            txtOper.pushKey(KeyEvent.VK_LEFT, 0);
//            txtOper.pushKey(KeyEvent.VK_DELETE, KeyEvent.CTRL_DOWN_MASK);
//            compareToGoldenFile(txtOper.getDocument());
        } finally {
            closeFileWithDiscard();
        }
    }

    public void XtestLineTools() {
        resetCounter();
        openDefaultProject();
        openDefaultSampleFile();
        try {

            EditorOperator editor = getDefaultSampleEditorOperator();
            editor.requestFocus();
            JEditorPaneOperator txtOper = editor.txtEditorPane();
            editor.setCaretPosition(7,25);
            // 00
            txtOper.pushKey(KeyEvent.VK_LEFT,KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            //01
            txtOper.pushKey(KeyEvent.VK_RIGHT,KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            //02
            txtOper.pushKey(KeyEvent.VK_UP,KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            //03
            txtOper.pushKey(KeyEvent.VK_DOWN,KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
                        
            //04 - the same with block
            editor.setCaretPosition(7,25);
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
            
            txtOper.pushKey(KeyEvent.VK_LEFT,KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            //05
            txtOper.pushKey(KeyEvent.VK_RIGHT,KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            //06
            txtOper.pushKey(KeyEvent.VK_UP,KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            //07
            txtOper.pushKey(KeyEvent.VK_DOWN,KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());            
            
            //copy lines
            editor.setCaretPosition(7,25);
            
            //08
            txtOper.pushKey(KeyEvent.VK_UP,KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            
            //09
            txtOper.pushKey(KeyEvent.VK_Z,KeyEvent.CTRL_DOWN_MASK );
            editor.setCaretPosition(7,25);
            txtOper.pushKey(KeyEvent.VK_DOWN,KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            
            //10
            txtOper.pushKey(KeyEvent.VK_Z,KeyEvent.CTRL_DOWN_MASK );
            editor.setCaretPosition(7,25);
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_UP,KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            
            //11
            txtOper.pushKey(KeyEvent.VK_Z,KeyEvent.CTRL_DOWN_MASK );
            editor.setCaretPosition(7,25);
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_DOWN,KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            
            
        } finally {
            closeFileWithDiscard();
        }
    }
    
    public void testSyntaxSelection() {
        resetCounter();
        openDefaultProject();
        openDefaultSampleFile();
        int[] begins = {584,573,569,552,530,471,453,441,404,383,349,310,176};
        int[] ends =   {590,591,593,594,612,612,626,626,637,637,643,645,645};
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            editor.requestFocus();
            JEditorPaneOperator txtOper = editor.txtEditorPane();
            editor.setCaretPosition(27,56);
            int x = 0;
            while(x<begins.length) {
                txtOper.pushKey(KeyEvent.VK_PERIOD, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK );
                int start = txtOper.getSelectionStart();
                int end = txtOper.getSelectionEnd();                
                if(start!=begins[x] || end != ends[x]) fail("Wrong selection expected <"+begins[x]+","+ends[x]+"> but got <"+start+","+end+">");
                x++;
            }
            x--;
            while(x>0) {
                x--;
                txtOper.pushKey(KeyEvent.VK_COMMA, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK );
                int start = txtOper.getSelectionStart();
                int end = txtOper.getSelectionEnd();                
                if(start!=begins[x] || end != ends[x]) fail("Wrong selection expected <"+begins[x]+","+ends[x]+"> but got <"+start+","+end+">");
            }            
        } finally {
            closeFileWithDiscard();
        }
    }
    
    public void testCommentUncomment() {
        resetCounter();
        openDefaultProject();
        openDefaultSampleFile();        
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            editor.requestFocus();            
            JEditorPaneOperator txtOper = editor.txtEditorPane();
            //00
            editor.setCaretPosition(6,1);            
            txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            
            //01            
            txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            
            //02
            editor.setCaretPosition(10,1);        
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            
            //03
            txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            
            //04
            editor.setCaretPosition(15,1);        
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            
            //05
            txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            
            //06
            editor.setCaretPosition(20,1);                    
            txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            
            //07
            txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            
            //08
            editor.setCaretPosition(21,1);                    
            txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            
            //09
            editor.setCaretPosition(21,1);                    
            txtOper.pushKey(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
            compareToGoldenFile(txtOper.getDocument());
            
        } finally {
            closeFileWithDiscard();
        }
    }    

    public static void main(String[] args) {
        TestRunner.run(JavaEditActionsTest.class);
    }
}
