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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.test.java.navigation;

import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Members" ResizablePopup$2.
 *
 * @author jp159440
 * @version 1.0
 */
public class MembersOperator extends JDialogOperator {

    /** Creates new Members that can handle it.
     */
    public MembersOperator() {
        super("Members");
    }

    private JSplitPaneOperator _sppJSplitPane;
    private JButtonOperator _btMetalSplitPaneDivider$1;
    private JButtonOperator _btMetalSplitPaneDivider$2;
    private JTreeOperator _treeJTree;
    private JEditorPaneOperator _txtJEditorPane;
    private JLabelOperator _lblFilter;
    private JTextFieldOperator _txtFilter;
    private JCheckBoxOperator _cbCaseSensitive;
    private JEditorPaneOperator _txtJEditorPane2;
    private JLabelOperator _lblFilters;
    private JToggleButtonOperator _tbJToggleButton;
    private JToggleButtonOperator _tbJToggleButton2;
    private JToggleButtonOperator _tbJToggleButton3;
    private JToggleButtonOperator _tbJToggleButton4;
    private JToggleButtonOperator _tbJToggleButton5;
    private JToggleButtonOperator _tbJToggleButton6;
    private JToggleButtonOperator _tbJToggleButton7;
    private JToggleButtonOperator _tbJToggleButton8;
    private JToggleButtonOperator _tbJToggleButton9;
    private JToggleButtonOperator _tbJToggleButton10;
    private JToggleButtonOperator _tbJToggleButton11;
    private JButtonOperator _btClose;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null JSplitPane in this dialog.
     * @return JSplitPaneOperator
     */
    public JSplitPaneOperator sppJSplitPane() {
        if (_sppJSplitPane==null) {
            _sppJSplitPane = new JSplitPaneOperator(this);
        }
        return _sppJSplitPane;
    }

    /** Tries to find null MetalSplitPaneDivider$1 in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMetalSplitPaneDivider$1() {
        if (_btMetalSplitPaneDivider$1==null) {
            _btMetalSplitPaneDivider$1 = new JButtonOperator(sppJSplitPane());
        }
        return _btMetalSplitPaneDivider$1;
    }

    /** Tries to find null MetalSplitPaneDivider$2 in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMetalSplitPaneDivider$2() {
        if (_btMetalSplitPaneDivider$2==null) {
            _btMetalSplitPaneDivider$2 = new JButtonOperator(sppJSplitPane(), 1);
        }
        return _btMetalSplitPaneDivider$2;
    }

    /** Tries to find null JTree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator treeJTree() {
        if (_treeJTree==null) {
            _treeJTree = new JTreeOperator(sppJSplitPane());
        }
        return _treeJTree;
    }

    /** Tries to find null JEditorPane in this dialog.
     * @return JEditorPaneOperator
     */
    public JEditorPaneOperator txtJEditorPane() {
        if (_txtJEditorPane==null) {
            _txtJEditorPane = new JEditorPaneOperator(sppJSplitPane());
        }
        return _txtJEditorPane;
    }

    /** Tries to find "Filter:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFilter() {
        if (_lblFilter==null) {
            _lblFilter = new JLabelOperator(this, "Filter:");
        }
        return _lblFilter;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtFilter() {
        if (_txtFilter==null) {
            _txtFilter = new JTextFieldOperator(this);
        }
        return _txtFilter;
    }

    /** Tries to find "Case sensitive" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbCaseSensitive() {
        if (_cbCaseSensitive==null) {
            _cbCaseSensitive = new JCheckBoxOperator(this, "Case sensitive");
        }
        return _cbCaseSensitive;
    }

    /** Tries to find null JEditorPane in this dialog.
     * @return JEditorPaneOperator
     */
    public JEditorPaneOperator txtJEditorPane2() {
        if (_txtJEditorPane2==null) {
            _txtJEditorPane2 = new JEditorPaneOperator(this, 1);
        }
        return _txtJEditorPane2;
    }

    /** Tries to find "Filters:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFilters() {
        if (_lblFilters==null) {
            _lblFilters = new JLabelOperator(this, "Filters:");
        }
        return _lblFilters;
    }

    /** Tries to find null JToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbJToggleButton() {
        if (_tbJToggleButton==null) {
            _tbJToggleButton = new JToggleButtonOperator(this, 1);
        }
        return _tbJToggleButton;
    }

    /** Tries to find null JToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbJToggleButton2() {
        if (_tbJToggleButton2==null) {
            _tbJToggleButton2 = new JToggleButtonOperator(this, 2);
        }
        return _tbJToggleButton2;
    }

    /** Tries to find null JToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbJToggleButton3() {
        if (_tbJToggleButton3==null) {
            _tbJToggleButton3 = new JToggleButtonOperator(this, 3);
        }
        return _tbJToggleButton3;
    }

    /** Tries to find null JToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbJToggleButton4() {
        if (_tbJToggleButton4==null) {
            _tbJToggleButton4 = new JToggleButtonOperator(this, 4);
        }
        return _tbJToggleButton4;
    }

    /** Tries to find null JToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbJToggleButton5() {
        if (_tbJToggleButton5==null) {
            _tbJToggleButton5 = new JToggleButtonOperator(this, 5);
        }
        return _tbJToggleButton5;
    }

    /** Tries to find null JToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbJToggleButton6() {
        if (_tbJToggleButton6==null) {
            _tbJToggleButton6 = new JToggleButtonOperator(this, 6);
        }
        return _tbJToggleButton6;
    }

    /** Tries to find null JToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbJToggleButton7() {
        if (_tbJToggleButton7==null) {
            _tbJToggleButton7 = new JToggleButtonOperator(this, 7);
        }
        return _tbJToggleButton7;
    }

    /** Tries to find null JToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbJToggleButton8() {
        if (_tbJToggleButton8==null) {
            _tbJToggleButton8 = new JToggleButtonOperator(this, 8);
        }
        return _tbJToggleButton8;
    }

    /** Tries to find null JToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbJToggleButton9() {
        if (_tbJToggleButton9==null) {
            _tbJToggleButton9 = new JToggleButtonOperator(this, 9);
        }
        return _tbJToggleButton9;
    }

    /** Tries to find null JToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbJToggleButton10() {
        if (_tbJToggleButton10==null) {
            _tbJToggleButton10 = new JToggleButtonOperator(this, 10);
        }
        return _tbJToggleButton10;
    }

    /** Tries to find null JToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbJToggleButton11() {
        if (_tbJToggleButton11==null) {
            _tbJToggleButton11 = new JToggleButtonOperator(this, 11);
        }
        return _tbJToggleButton11;
    }

    /** Tries to find "Close" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btClose() {
        if (_btClose==null) {
            _btClose = new JButtonOperator(this, "Close");
        }
        return _btClose;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** clicks on null MetalSplitPaneDivider$1
     */
    public void metalSplitPaneDivider$1() {
        btMetalSplitPaneDivider$1().push();
    }

    /** clicks on null MetalSplitPaneDivider$2
     */
    public void metalSplitPaneDivider$2() {
        btMetalSplitPaneDivider$2().push();
    }

    /** gets text for txtJEditorPane
     * @return String text
     */
    public String getJEditorPane() {
        return txtJEditorPane().getText();
    }

    /** sets text for txtJEditorPane
     * @param text String text
     */
    public void setJEditorPane(String text) {
        txtJEditorPane().setText(text);
    }

    /** types text for txtJEditorPane
     * @param text String text
     */
    public void typeJEditorPane(String text) {
        txtJEditorPane().typeText(text);
    }

    /** gets text for txtFilter
     * @return String text
     */
    public String getFilter() {
        return txtFilter().getText();
    }

    /** sets text for txtFilter
     * @param text String text
     */
    public void setFilter(String text) {
        txtFilter().setText(text);
    }

    /** types text for txtFilter
     * @param text String text
     */
    public void typeFilter(String text) {
        txtFilter().typeText(text);
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkCaseSensitive(boolean state) {
        if (cbCaseSensitive().isSelected()!=state) {
            cbCaseSensitive().push();
        }
    }

    /** gets text for txtJEditorPane2
     * @return String text
     */
    public String getJEditorPane2() {
        return txtJEditorPane2().getText();
    }

    /** sets text for txtJEditorPane2
     * @param text String text
     */
    public void setJEditorPane2(String text) {
        txtJEditorPane2().setText(text);
    }

    /** types text for txtJEditorPane2
     * @param text String text
     */
    public void typeJEditorPane2(String text) {
        txtJEditorPane2().typeText(text);
    }

    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkJToggleButton(boolean state) {
        if (tbJToggleButton().isSelected()!=state) {
            tbJToggleButton().push();
        }
    }

    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkJToggleButton2(boolean state) {
        if (tbJToggleButton2().isSelected()!=state) {
            tbJToggleButton2().push();
        }
    }

    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkJToggleButton3(boolean state) {
        if (tbJToggleButton3().isSelected()!=state) {
            tbJToggleButton3().push();
        }
    }

    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkJToggleButton4(boolean state) {
        if (tbJToggleButton4().isSelected()!=state) {
            tbJToggleButton4().push();
        }
    }

    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkJToggleButton5(boolean state) {
        if (tbJToggleButton5().isSelected()!=state) {
            tbJToggleButton5().push();
        }
    }

    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkJToggleButton6(boolean state) {
        if (tbJToggleButton6().isSelected()!=state) {
            tbJToggleButton6().push();
        }
    }

    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkJToggleButton7(boolean state) {
        if (tbJToggleButton7().isSelected()!=state) {
            tbJToggleButton7().push();
        }
    }

    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkJToggleButton8(boolean state) {
        if (tbJToggleButton8().isSelected()!=state) {
            tbJToggleButton8().push();
        }
    }

    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkJToggleButton9(boolean state) {
        if (tbJToggleButton9().isSelected()!=state) {
            tbJToggleButton9().push();
        }
    }

    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkJToggleButton10(boolean state) {
        if (tbJToggleButton10().isSelected()!=state) {
            tbJToggleButton10().push();
        }
    }

    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkJToggleButton11(boolean state) {
        if (tbJToggleButton11().isSelected()!=state) {
            tbJToggleButton11().push();
        }
    }

    /** clicks on "Close" JButton
     */
    public void close() {
        btClose().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of Members by accessing all its components.
     */
    public void verify() {
        sppJSplitPane();
        btMetalSplitPaneDivider$1();
        btMetalSplitPaneDivider$2();
        treeJTree();
        txtJEditorPane();
        lblFilter();
        txtFilter();
        cbCaseSensitive();
        txtJEditorPane2();
        lblFilters();
        tbJToggleButton();
        tbJToggleButton2();
        tbJToggleButton3();
        tbJToggleButton4();
        tbJToggleButton5();
        tbJToggleButton6();
        tbJToggleButton7();
        tbJToggleButton8();
        tbJToggleButton9();
        tbJToggleButton10();
        tbJToggleButton11();
        btClose();
    }

    /** Performs simple test of Members
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new MembersOperator().verify();
        System.out.println("Members verification finished.");
    }
}

