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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

/*
 * Find.java
 *
 * Created on 2/11/03 1:29 PM
 */
package org.netbeans.modules.visualweb.gravy.properties.editors;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.netbeans.jemmy.operators.*;


import javax.swing.JDialog;
import org.netbeans.jellytools.Bundle;
import org.netbeans.modules.visualweb.gravy.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Find" NbDialog.
 */
public class FindEditorOperator extends NbDialogOperator {
    
    /** Creates new Find that can handle it.
     */
    public FindEditorOperator() {
        super(java.util.ResourceBundle.getBundle("org.netbeans.editor.Bundle").getString("find-title"));
    }
    
    /** Creates new StringArrayCustomEditorOperator
     * @param wrapper JDialogOperator wrapper for custom editor */    
    public FindEditorOperator(JDialogOperator wrapper) {
        super((JDialog)wrapper.getSource());
    }

    private JLabelOperator _lblFindWhat;
    private JComboBoxOperator _cboFindWhat;
    public static final String ITEM_ = "";
    private JCheckBoxOperator _cbHighlightSearch;
    private JCheckBoxOperator _cbIncrementalSearch;
    private JCheckBoxOperator _cbMatchCase;
    private JCheckBoxOperator _cbSmartCase;
    private JCheckBoxOperator _cbMatchWholeWordsOnly;
    private JCheckBoxOperator _cbBackwardSearch;
    private JCheckBoxOperator _cbWrapSearch;
    private JButtonOperator _btFind;
    private JButtonOperator _btClose;
    private JButtonOperator _btHelp;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Find What:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFindWhat() {
        if (_lblFindWhat==null) {
            _lblFindWhat = new JLabelOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.editor.Bundle").getString("find-what"));
        }
        return _lblFindWhat;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboFindWhat() {
        if (_cboFindWhat==null) {
            _cboFindWhat = new JComboBoxOperator(this);
        }
        return _cboFindWhat;
    }
    
    /** Tries to find " Highlight Search" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbHighlightSearch() {
        if (_cbHighlightSearch==null) {
            _cbHighlightSearch = new JCheckBoxOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.editor.Bundle").getString("find-highlight-search"));
        }
        return _cbHighlightSearch;
    }
    
    /** Tries to find " Incremental Search" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbIncrementalSearch() {
        if (_cbIncrementalSearch==null) {
            _cbIncrementalSearch = new JCheckBoxOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.editor.Bundle").getString("find-inc-search"));
        }
        return _cbIncrementalSearch;
    }
    
    /** Tries to find " Match Case" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbMatchCase() {
        if (_cbMatchCase==null) {
            _cbMatchCase = new JCheckBoxOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.editor.Bundle").getString("find-match-case"));
        }
        return _cbMatchCase;
    }
    
    /** Tries to find " Smart Case" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbSmartCase() {
        if (_cbSmartCase==null) {
            _cbSmartCase = new JCheckBoxOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.editor.Bundle").getString("find-smart-case"));
        }
        return _cbSmartCase;
    }
    
    /** Tries to find " Match Whole Words Only" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbMatchWholeWordsOnly() {
        if (_cbMatchWholeWordsOnly==null) {
            _cbMatchWholeWordsOnly = new JCheckBoxOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.editor.Bundle").getString("find-whole-words"));
        }
        return _cbMatchWholeWordsOnly;
    }
    
    /** Tries to find " Backward Search" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbBackwardSearch() {
        if (_cbBackwardSearch==null) {
            _cbBackwardSearch = new JCheckBoxOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.editor.Bundle").getString("find-backward-search"));
        }
        return _cbBackwardSearch;
    }
    
    /** Tries to find " Wrap Search" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbWrapSearch() {
        if (_cbWrapSearch==null) {
            _cbWrapSearch = new JCheckBoxOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.editor.Bundle").getString("find-wrap-search"));
        }
        return _cbWrapSearch;
    }
    
    /** Tries to find "Find" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btFind() {
        if (_btFind==null) {
            _btFind = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.editor.Bundle").getString("find-button-find"));
        }
        return _btFind;
    }
    
    /** Tries to find "Close" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btClose() {
        if (_btClose==null) {
            _btClose = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.editor.Bundle").getString("find-button-cancel"));
        }
        return _btClose;
    }
    
    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.openide.explorer.propertysheet.Bundle").getString("CTL_Help"));
        }
        return _btHelp;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** returns selected item for cboFindWhat
     * @return String item
     */
    public String getSelectedFindWhat() {
        return cboFindWhat().getSelectedItem().toString();
    }
    
    /** selects item for cboFindWhat
     * @param item String item
     */
    public void selectFindWhat(String item) {
        cboFindWhat().selectItem(item);
    }
    
    /** types text for cboFindWhat
     * @param text String text
     */
    public void typeFindWhat(String text) {
        cboFindWhat().typeText(text);
    }
    
    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkHighlightSearch(boolean state) {
        if (cbHighlightSearch().isSelected()!=state) {
            cbHighlightSearch().push();
        }
    }
    
    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkIncrementalSearch(boolean state) {
        if (cbIncrementalSearch().isSelected()!=state) {
            cbIncrementalSearch().push();
        }
    }
    
    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkMatchCase(boolean state) {
        if (cbMatchCase().isSelected()!=state) {
            cbMatchCase().push();
        }
    }
    
    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkSmartCase(boolean state) {
        if (cbSmartCase().isSelected()!=state) {
            cbSmartCase().push();
        }
    }
    
    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkMatchWholeWordsOnly(boolean state) {
        if (cbMatchWholeWordsOnly().isSelected()!=state) {
            cbMatchWholeWordsOnly().push();
        }
    }
    
    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkBackwardSearch(boolean state) {
        if (cbBackwardSearch().isSelected()!=state) {
            cbBackwardSearch().push();
        }
    }
    
    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkWrapSearch(boolean state) {
        if (cbWrapSearch().isSelected()!=state) {
            cbWrapSearch().push();
        }
    }
    
    /** checks or unchecks All JCheckBoxes
     * @param state boolean requested state
     */
    public void checkAll(boolean state) {
        checkMatchCase(state);
        checkSmartCase(state);
        checkMatchWholeWordsOnly(state);

        checkHighlightSearch(state);
        checkIncrementalSearch(state);
        checkBackwardSearch(state);
        checkWrapSearch(state);
    }

    /** clicks on "Find" JButton
     */
    public void find() {
        btFind().pushNoBlock();
    }
    
    /** clicks on "Close" JButton
     */
    public void close() {
        btClose().push();
    }
    
    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }
    
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of Find by accessing all its components.
     */
    public void verify() {
        lblFindWhat();
        cboFindWhat();
        cbHighlightSearch();
        cbIncrementalSearch();
        cbMatchCase();
        cbSmartCase();
        cbMatchWholeWordsOnly();
        cbBackwardSearch();
        cbWrapSearch();
        btFind();
        btClose();
        btHelp();
    }
    
    public static void find(String text) {
        FindEditorOperator op=new FindEditorOperator();
        op.typeFindWhat(text);
        op.find();
    }
    
    public static void find(String text,Robot robot) {
        FindEditorOperator op=new FindEditorOperator();
        robot.waitForIdle();
        java.awt.Point p=op.cboFindWhat().getLocationOnScreen();
        int x=p.x+op.cboFindWhat().getWidth()/2;
        int y=p.y+op.cboFindWhat().getHeight()/2;
        robot.mouseMove(x,y);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(50);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        char c;
        boolean upper;
        for (int i=0;i < text.length();i++) {
            c=text.charAt(i);
            if (Character.isUpperCase(c)) {
                upper=true;
            } else {
                upper=false;
                c=(char)(Character.toUpperCase((char)c));
            }
            if (upper) {
                robot.keyPress(KeyEvent.VK_SHIFT);
            }
            robot.keyPress((int)c);
            robot.delay(5);
            robot.keyRelease((int)c);
            robot.delay(5);
            if (upper) {
                robot.keyRelease(KeyEvent.VK_SHIFT);
            }
        }
        p=op.btFind().getLocationOnScreen();
        x=p.x+op.btFind().getWidth()/2;
        y=p.y+op.btFind().getHeight()/2;
        robot.mouseMove(x,y);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(50);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.delay(50);
        robot.waitForIdle();
    }
    
    
    /** Performs simple test of Find
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        //new Find().verify();
        System.out.println("Find verification finished.");
        try {
            Robot robot=new Robot();
            System.out.println("Start delay.");
            robot.delay(2000);
            System.out.println("Delayed");
            robot.waitForIdle();
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.delay(1);
            robot.keyPress(KeyEvent.VK_F);
            robot.delay(20);
            robot.keyRelease(KeyEvent.VK_F);
            robot.delay(1);
            robot.keyRelease(KeyEvent.VK_CONTROL);
            System.out.println("1");
            robot.waitForIdle();
            System.out.println("2");
            FindEditorOperator.find("aaa",robot);
            System.out.println("3");
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

