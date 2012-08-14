/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.test.java.editor.actions;

import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import junit.textui.TestRunner;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbModuleSuite;
import org.openide.util.Exceptions;

/**
 * Basic Edit Actions Test class.
 * The base edit actions can be found at:
 * http://editor.netbeans.org/doc/UserView/apdx_a_eshortcuts.html
 * 
 * @author Martin Roskanin, Jiri Prox
 */
public class JavaEditActionsTest extends JavaEditorActionsTestCase {

    private static EditorOperator editor;
    private static JEditorPaneOperator txtOper;

    /** Creates a new instance of Main */
    public JavaEditActionsTest(String testMethodName) {
        super(testMethodName);
        FIX_STATE_WHEN_FAILURE = true;
    }

    private void initTests() {
        initTests("");
    }

    private void initTests(String TestName) {
        resetCounter();
        openDefaultProject();
        if (!TestName.equals("")) {
            String cPackage = "Source Packages|org.netbeans.test.java.editor.actions.JavaEditActionsTest|";
            Node node = new Node(ProjectsTabOperator.invoke().getProjectRootNode(getDefaultProjectName()), cPackage + TestName);
            new Action(null, "Open").performPopup(node);
            editor = new EditorOperator(TestName);
        } else {
            openDefaultSampleFile();
            editor = getDefaultSampleEditorOperator();
        }
        editor.requestFocus();
        txtOper = editor.txtEditorPane();
    }

    private void cleanUpTests() {
        closeFileWithDiscard();
    }

    public void testEditActionsTestCase_0() {
        initTests("testEditActions");
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
        // -> previous word "ins|ert", with caret at | should be modified to "insdx|rt"
        // 6. compare document content to golden file to check if the change took place
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_000", "testEditActions00", "testEditActions00");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions00.pass", 5, 19, errMsg);
        }
    }

    public void testEditActionsTestCase_1() {
        // 01 -------- test delete word action. Caret in the middle of the word ---
        // remove-word action has been removed. Changing test to delete selected word
        editor.setCaretPosition(17, 20);
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DELETE);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_100", "testEditActions01", "testEditActions01");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions01.pass", 17, 17, errMsg);
        }
    }

    public void testEditActionsTestCase_2() {
        // 02 -------- test delete previous word action. Caret after the word ------
        //  delete word - Caret after the word was removed
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_200", "testEditActions02", "testEditActions02");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions02.pass", 17, 10, errMsg);
        }
    }

    public void testEditActionsTestCase_3() {
        // 03 --------- test remove the current line --------------------
        txtOper.pushKey(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_300", "testEditActions03", "testEditActions03");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions03.pass", 17, 1, errMsg);
        }
    }

    public void testEditActionsTestCase_4() {
        // 04 -- test Select the word the insertion point is on or
        // -- deselect any selected text (Alt + j)
        // -- after that test CUT action ---------------
        editor.setCaretPosition(9, 24);
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        cutCopyViaStrokes(txtOper, KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_400", "testEditActions04", "testEditActions04");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions04.pass", 9, 21, errMsg);
        }
    }

    public void testEditActionsTestCase_5() {
        // 05 -- test PASTE ------
        editor.setCaretPosition(11, 17);
        txtOper.pushKey(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_500", "testEditActions05", "testEditActions05");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions05.pass", 11, 23, errMsg);
        }
    }

    public void testEditActionsTestCase_6() throws InterruptedException {
        // 06 -- test UNDO/REDO ----
        int oldDocLength = txtOper.getDocument().getLength();
        txtOper.pushKey(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
        waitMaxMilisForValue(WAIT_MAX_MILIS_FOR_UNDO_REDO, getFileLengthChangeResolver(txtOper, oldDocLength), Boolean.FALSE);
        oldDocLength = txtOper.getDocument().getLength();
        txtOper.pushKey(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
        waitMaxMilisForValue(WAIT_MAX_MILIS_FOR_UNDO_REDO, getFileLengthChangeResolver(txtOper, oldDocLength), Boolean.FALSE);
        oldDocLength = txtOper.getDocument().getLength();
        txtOper.pushKey(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK);
        waitMaxMilisForValue(WAIT_MAX_MILIS_FOR_UNDO_REDO, getFileLengthChangeResolver(txtOper, oldDocLength), Boolean.FALSE);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_600", "testEditActions06", "testEditActions06");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions06.pass", 9, 21, errMsg);
        }
    }

    public void testEditActionsTestCase_7() {
        // 07 -- test CTRL+backspace -- delete previous word
        editor.setCaretPosition(9,21);
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE, KeyEvent.CTRL_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_700", "testEditActions07", "testEditActions07");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions07.pass", 9, 2, errMsg);
        }
    }

    public void testEditActionsTestCase_8() {
        // 08 -- test CTRL+u -- delete the indentation level
        txtOper.pushKey(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_800", "testEditActions08", "testEditActions08");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions08.pass", 9, 2, errMsg);
        }
    }

    public void testEditActionsTestCase_9() {
        // 09 -- test CTRL+u -- delete the line break
        editor.setCaretPosition(9,2);
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE);
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE);
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE);
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE);
//        txtOper.typeKey(' ');
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_900", "testEditActions09", "testEditActions09");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions09.pass", 8, 6, errMsg);
        }
    }

    public void testEditActionsTestCase_10() {
        // 10 -- test delete action
        txtOper.pushKey(KeyEvent.VK_BACK_SPACE);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_1000", "testEditActions10", "testEditActions10");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions10.pass", 8, 5, errMsg);
        }
    }

    public void testEditActionsTestCase_11() {
        // 11 -- test delete selected block and selecting to end of the line
        txtOper.pushKey(KeyEvent.VK_END, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DELETE);
        txtOper.typeText("   ");
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_1100", "testEditActions11", "testEditActions11");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions11.pass", 8, 5, errMsg);
        }
    }

    public void testEditActionsTestCase_12() {
        // 12 -- test COPY action ---
        editor.setCaretPosition(9, 15);
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
        cutCopyViaStrokes(txtOper, KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
        editor.setCaretPosition(10, 17);
        txtOper.pushKey(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_1200", "testEditActions12", "testEditActions12");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions12.pass", 10, 23, errMsg);
        }
    }

    public void testEditActionsTestCase_12a() {
        // 12a -- test Select All ---
        txtOper.pushKey(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK);
        if (txtOper.getSelectionStart() != 0 || txtOper.getSelectionEnd() != txtOper.getDocument().getLength()) {
            fail("Select all action fails. [start/end of selection] [docLength]: [" + txtOper.getSelectionStart() + "/" + txtOper.getSelectionEnd() + "] [" + txtOper.getDocument().getLength() + "]");
        }
    }

    public void testEditActionsTestCase_13() {
        // 13 -- test Shift+delete (CUT) and shift+insert (PASTE)---
        editor.setCaretPosition(5, 17);
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        cutCopyViaStrokes(txtOper, KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK);
        editor.setCaretPosition(13, 8);
        txtOper.pushKey(KeyEvent.VK_INSERT, KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_1300", "testEditActions13", "testEditActions13");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions13.pass", 13, 25, errMsg);
        }
    }

    public void testEditActionsTestCase_14() {
        // 14 -- test ctrl+insert (COPY)---
        editor.setCaretPosition(10, 20);
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        cutCopyViaStrokes(txtOper, KeyEvent.VK_INSERT, KeyEvent.CTRL_DOWN_MASK);
        editor.setCaretPosition(13, 15);
        txtOper.pushKey(KeyEvent.VK_INSERT, KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_1400", "testEditActions14", "testEditActions14");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions14.pass", 13, 31, errMsg);
        }
    }

    public void testEditActionsTestCase_15() {
        // 15 -- test CTRL+K ----
        editor.setCaretPosition(6, 21);
        txtOper.pushKey(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_1500", "testEditActions15", "testEditActions15");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions15.pass", 6, 32, errMsg);
        }
    }

    public void testEditActionsTestCase_16() {
        // 16 -- test CTRL+SHITF+K ----
        editor.setCaretPosition(10, 20);
        //type space to change String to Str ing
        txtOper.typeKey(' ');
        editor.setCaretPosition(10, 23);
        txtOper.pushKey(KeyEvent.VK_K, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_1600", "testEditActions16", "testEditActions16");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions16.pass", 10, 34, errMsg);
        }
    }

    public void testEditActionsTestCase_17() {
        /// 17 -- test expanding abbreviation
        editor.setCaretPosition(19, 12);
        txtOper.typeKey('s');
        txtOper.typeKey('t');
        txtOper.pressKey(KeyEvent.VK_TAB);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_1700", "testEditActions17", "testEditActions17");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions17.pass", 19, 19, errMsg);
        }
    }

    public void testEditActionsTestCase_18() {
        // 18 -- test Insert space without expanding abbreviation (SPACE)
        editor.setCaretPosition(20, 9);
        txtOper.typeKey('s');
        txtOper.typeKey('t');
        txtOper.typeKey(' ');
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_1800", "testEditActions18", "testEditActions18");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions18.pass", 20, 12, errMsg);
        }
    }

    public void testEditActionsTestCase_19() {
        /* __________________ Capitlization ___________________ */
        // 19 -- w/o selection upper case ------
        editor.setCaretPosition(13, 18);
        txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_U);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_1900", "testEditActions19", "testEditActions19");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions19.pass", 13, 19, errMsg);
        }
    }

    public void testEditActionsTestCase_20() {
        // 20 -- selection upper case ------
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_U);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_2000", "testEditActions20", "testEditActions20");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions20.pass", 13, 21, errMsg);
        }
    }

    public void testEditActionsTestCase_21() {
        // 21 -- w/o selection lower case ------
        editor.setCaretPosition(13, 18);
        txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_L);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_2100", "testEditActions21", "testEditActions21");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions21.pass", 13, 19, errMsg);
        }
    }

    public void testEditActionsTestCase_22() {
        // 22 -- selection lower case ------
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_L);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_2200", "testEditActions22", "testEditActions22");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions22.pass", 13, 21, errMsg);
        }
    }

    public void testEditActionsTestCase_23() {
        // 23 -- w/o selection reverse case ------
        editor.setCaretPosition(13, 18);
        txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_S);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_2300", "testEditActions23", "testEditActions23");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions23.pass", 13, 19, errMsg);
        }
    }

    public void testEditActionsTestCase_24() {
        // 24 -- selection reverse case ------
        txtOper.pushKey(KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_S);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_2400", "testEditActions24", "testEditActions24");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions24.pass", 13, 21, errMsg);
        }
    }

    public void testEditActionsTestCase_25() {
        /* __________________ Several Indentation Actions ___________________ */
        // 25 -- Shift left  ------
        editor.setCaretPosition(10, 9);
        txtOper.pushKey(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_2500", "testEditActions25", "testEditActions25");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions25.pass", 10, 5, errMsg);
        }
    }

    public void testEditActionsTestCase_26() {
        // 26 -- insert tab  ------
        txtOper.pushKey(KeyEvent.VK_TAB);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_2600", "testEditActions26", "testEditActions26");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions26.pass", 10, 9, errMsg);
        }
    }

    public void testEditActionsTestCase_27() {
        // 27 -- Shift selection left  ------
        editor.setCaretPosition(9, 1);
        //select method
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        // shift left
        txtOper.pushKey(KeyEvent.VK_TAB, KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_2700", "testEditActions27", "testEditActions27");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions27.pass", 12, 1, errMsg);
        }
    }

    public void testEditActionsTestCase_28() {
        // 28 -- Shift  selection right  ------
        txtOper.pushKey(KeyEvent.VK_TAB);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_2800", "testEditActions28", "testEditActions28");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions28.pass", 12, 1, errMsg);
        }
    }

    public void testEditActionsTestCase_29() {
        // 29 -- Shift selection left (Alt+Shift+left) ------
        editor.setCaretPosition(9, 1);
        //select method
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        // shift left
        txtOper.pushKey(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_2900", "testEditActions29", "testEditActions29");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions29.pass", 12, 1, errMsg);
        }
    }

    public void testEditActionsTestCase_30() {
        // 30 -- Shift  selection right (Alt+Shift+Right) ------
        txtOper.pushKey(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_3000", "testEditActions30", "testEditActions30");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions30.pass", 12, 1, errMsg);
        }
    }

    public void testEditActionsTestCase_31() {
        // 31 -- reformat the selection + testing BACK_SPACE----
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
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_3100", "testEditActions31", "testEditActions31");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testEditActions31.pass", 12, 1, errMsg);
        }
    }

    public void testEditActionsTestCase_32() {
        try {
            //32 -- reformat the entire file ----
            // deselect
            txtOper.setSelectionStart(1);
            txtOper.setSelectionEnd(1);
            // invoke formatter
            txtOper.pushKey(KeyEvent.VK_F, KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
            String errMsg = compareToGoldenFile(txtOper.getDocument(), "testEditActionsTestCase_3200", "testEditActions32", "testEditActions32");
            if (errMsg != null) {
                setEditorStateWithGoldenFile(editor, "testEditActions32.pass", 12, 1, errMsg);
            }

        //<editor-fold defaultstate="collapsed" desc="Extension Actions">

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

        //</editor-fold>

        } finally {
            cleanUpTests();
        }
    }

    public void testLineToolsTestCase_0() {
        initTests("testLineTools");
        editor.setCaretPosition(7, 25);
        // 00
        txtOper.pushKey(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        compareToGoldenFile(txtOper.getDocument(), "testLineToolsTestCase_000", "testLineTools00", "testLineTools00");
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testLineToolsTestCase_000", "testLineTools00", "testLineTools00");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testLineTools00.pass", 7, 21, errMsg);
        }
    }

    public void testLineToolsTestCase_1() {
        //01
        txtOper.pushKey(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testLineToolsTestCase_100", "testLineTools01", "testLineTools01");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testLineTools01.pass", 7, 25, errMsg);
        }
    }

    public void testLineToolsTestCase_2() {
        //02
        txtOper.pushKey(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testLineToolsTestCase_200", "testLineTools02", "testLineTools02");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testLineTools02.pass", 6, 25, errMsg);
        }
    }

    public void testLineToolsTestCase_3() {
        //03
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testLineToolsTestCase_300", "testLineTools03", "testLineTools03");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testLineTools03.pass", 7, 25, errMsg);
        }
    }

    public void testLineToolsTestCase_4() {
        //04 - the same with block
        editor.setCaretPosition(7, 25);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testLineToolsTestCase_400", "testLineTools04", "testLineTools04");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testLineTools04.pass", 8, 21, errMsg);
        }
    }

    public void testLineToolsTestCase_5() {
        //05
        txtOper.pushKey(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testLineToolsTestCase_500", "testLineTools05", "testLineTools05");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testLineTools05.pass", 8, 25, errMsg);
        }
    }

    public void testLineToolsTestCase_6() {
        //06
        txtOper.pushKey(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testLineToolsTestCase_600", "testLineTools06", "testLineTools06");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testLineTools06.pass", 7, 25, errMsg);
        }
    }

    public void testLineToolsTestCase_7() {
        //07
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testLineToolsTestCase_700", "testLineTools07", "testLineTools07");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testLineTools07.pass", 8, 25, errMsg);
        }
    }

    public void testLineToolsTestCase_8() {
        //08
        editor.setCaretPosition(7, 25);
        txtOper.pushKey(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testLineToolsTestCase_800", "testLineTools08", "testLineTools08");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testLineTools08.pass", 7, 25, errMsg);
        }
    }

    public void testLineToolsTestCase_9() {
        //09
        txtOper.pushKey(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
        editor.setCaretPosition(7, 25);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testLineToolsTestCase_900", "testLineTools09", "testLineTools09");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testLineTools09.pass", 8, 25, errMsg);
        }
    }

    public void testLineToolsTestCase_10() {
        //10
        txtOper.pushKey(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
        editor.setCaretPosition(7, 25);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testLineToolsTestCase_1000", "testLineTools10", "testLineTools10");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testLineTools10.pass", 8, 25, errMsg);
        }
    }

    public void testLineToolsTestCase_11() {
        try {
            //11
            txtOper.pushKey(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
            editor.setCaretPosition(7, 25);
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
            txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
            String errMsg = compareToGoldenFile(txtOper.getDocument(), "testLineToolsTestCase_1100", "testLineTools11", "testLineTools11");
            if (errMsg != null) {
                setEditorStateWithGoldenFile(editor, "testLineTools11.pass", 10, 25, errMsg);
            }
        } finally {
            closeFileWithDiscard();
        }
    }

    public void testSyntaxSelection() {
        int[] begins = {602, 591, 587, 570, 548, 489, 471, 459, 422, 401, 367, 328, 176};
        int[] ends = {608, 609, 611, 612, 630, 630, 644, 644, 655, 655, 661, 663, 663};
        try {
            initTests();
            try { // just a small timeout to make the output visible (for the purpose of the screenshots, i.e.)
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            editor.setCaretPosition(27, 56);
            int x = 0;
            while (x < begins.length) {
                txtOper.pushKey(KeyEvent.VK_PERIOD, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                int start = txtOper.getSelectionStart();
                int end = txtOper.getSelectionEnd();
                //System.out.println(start+" "+end);
                if (start != begins[x] || end != ends[x]) {
                    fail("Wrong selection expected <" + begins[x] + "," + ends[x] + "> but got <" + start + "," + end + ">");
                }
                x++;
            }
            x--;
            while (x > 0) {
                x--;
                txtOper.pushKey(KeyEvent.VK_COMMA, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
                int start = txtOper.getSelectionStart();
                int end = txtOper.getSelectionEnd();
                //System.out.println(start+" "+end);
                if (start != begins[x] || end != ends[x]) {
                    fail("Wrong selection expected <" + begins[x] + "," + ends[x] + "> but got <" + start + "," + end + ">");
                }
            }
        } finally {
            cleanUpTests();
        }
    }

    public void testCommentUncommentTestCase_0() {
        initTests("testCommentUncomment");
        //00
        editor.setCaretPosition(6, 1);
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testCommentUncommentTestCase_000", "testCommentUncomment00", "testCommentUncomment00");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testCommentUncomment00.pass", 6, 3, errMsg);
        }
    }

    public void testCommentUncommentTestCase_1() {
        //01
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testCommentUncommentTestCase_100", "testCommentUncomment01", "testCommentUncomment01");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testCommentUncomment01.pass", 6, 1, errMsg);
        }
    }

    public void testCommentUncommentTestCase_2() {
        //02
        editor.setCaretPosition(10, 1);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testCommentUncommentTestCase_200", "testCommentUncomment02", "testCommentUncomment02");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testCommentUncomment02.pass", 12, 1, errMsg);
        }
    }

    public void testCommentUncommentTestCase_3() {
        //03
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testCommentUncommentTestCase_300", "testCommentUncomment03", "testCommentUncomment03");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testCommentUncomment03.pass", 12, 1, errMsg);
        }
    }

    public void testCommentUncommentTestCase_4() {
        //04
        editor.setCaretPosition(15, 1);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK);
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testCommentUncommentTestCase_400", "testCommentUncomment04", "testCommentUncomment04");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testCommentUncomment04.pass", 17, 1, errMsg);
        }
    }

    public void testCommentUncommentTestCase_5() {
        //05
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testCommentUncommentTestCase_500", "testCommentUncomment05", "testCommentUncomment05");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testCommentUncomment05.pass", 17, 1, errMsg);
        }
    }

    public void testCommentUncommentTestCase_6() {
        //06
        editor.setCaretPosition(20, 1);
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testCommentUncommentTestCase_600", "testCommentUncomment06", "testCommentUncomment06");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testCommentUncomment06.pass", 20, 1, errMsg);
        }
    }

    public void testCommentUncommentTestCase_7() {
        //07
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testCommentUncommentTestCase_700", "testCommentUncomment07", "testCommentUncomment07");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testCommentUncomment07.pass", 20, 3, errMsg);
        }
    }

    public void testCommentUncommentTestCase_8() {
        //08
        editor.setCaretPosition(21, 1);
        txtOper.pushKey(KeyEvent.VK_SLASH, KeyEvent.CTRL_DOWN_MASK);
        String errMsg = compareToGoldenFile(txtOper.getDocument(), "testCommentUncommentTestCase_800", "testCommentUncomment08", "testCommentUncomment08");
        if (errMsg != null) {
            setEditorStateWithGoldenFile(editor, "testCommentUncomment08.pass", 21, 1, errMsg);
        }
    }

    public void testCommentUncommentTestCase_9() {
        //09
        try {
            editor.setCaretPosition(21, 1);
            txtOper.pushKey(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
            String errMsg = compareToGoldenFile(txtOper.getDocument(), "testCommentUncommentTestCase_900", "testCommentUncomment09", "testCommentUncomment09");
            if (errMsg != null) {
                setEditorStateWithGoldenFile(editor, "testCommentUncomment09.pass", 21, 1, errMsg);
            }
        } finally {
            cleanUpTests();
        }
    }

    public static void main(String[] args) {
        TestRunner.run(JavaEditActionsTest.class);
    }

    public static Test suite() {
        NbModuleSuite.Configuration config = NbModuleSuite.createConfiguration(JavaEditActionsTest.class);
        // Add testEditActions tests
        for (int i = 0; i < 33; i++) {
            config = config.addTest("testEditActionsTestCase_" + i);
            if (i == 12) {
                config = config.addTest("testEditActionsTestCase_12a");
            }
        }
        // Add testSyntaxSelection
        config = config.addTest("testSyntaxSelection");
        // Add testLineTools tests
        for (int i = 0; i < 12; i++) {
            config = config.addTest("testLineToolsTestCase_" + i);
        }
        // Add testCommentUncomment tests
        for (int i = 0; i < 10; i++) {
            config = config.addTest("testCommentUncommentTestCase_" + i);
        }
        config = config.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(config);
    }
}
