/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
/*
 * CheckinOperator.java
 *
 * Created on 05/03/08 20:05
 */
package org.netbeans.test.clearcase.operators;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;
import org.netbeans.test.clearcase.operators.actions.CheckinAction;

/** Class implementing all necessary methods for handling "Checkin" NbDialog.
 *
 * @author peter
 * @version 1.0
 */
public class CheckinOperator extends NbDialogOperator {

    /** Creates new CheckinOperator that can handle it.
     */
    public CheckinOperator() {
        super("Checkin");
    }

    private JTableOperator _tabFilesToCheckin;
    private JButtonOperator _btWindowsScrollBarUI$WindowsArrowButton;
    private JButtonOperator _btWindowsScrollBarUI$WindowsArrowButton2;
    private JTextAreaOperator _txtJTextArea;
    private JLabelOperator _lblCheckinMessage;
    private JButtonOperator _btJButton;
    private JLabelOperator _lblFilesToCheckin;
    private JCheckBoxOperator _cbPreserveModificationTime;
    private JCheckBoxOperator _cbForceCheckinOfUnmodifiedFiles;
    private JButtonOperator _btCheckin;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;

    public static CheckinOperator invoke(Node[] nodes) {
        new CheckinAction().perform(nodes);
        return new CheckinOperator();
    }
    
    public static CheckinOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }
    
    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null JTable in this dialog.
     * @return JTableOperator
     */
    public JTableOperator tabFilesToCheckin() {
        if (_tabFilesToCheckin==null) {
            _tabFilesToCheckin = new JTableOperator(this);
        }
        return _tabFilesToCheckin;
    }

    /** Tries to find null WindowsScrollBarUI$WindowsArrowButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btWindowsScrollBarUI$WindowsArrowButton() {
        if (_btWindowsScrollBarUI$WindowsArrowButton==null) {
            _btWindowsScrollBarUI$WindowsArrowButton = new JButtonOperator(this);
        }
        return _btWindowsScrollBarUI$WindowsArrowButton;
    }

    /** Tries to find null WindowsScrollBarUI$WindowsArrowButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btWindowsScrollBarUI$WindowsArrowButton2() {
        if (_btWindowsScrollBarUI$WindowsArrowButton2==null) {
            _btWindowsScrollBarUI$WindowsArrowButton2 = new JButtonOperator(this, 1);
        }
        return _btWindowsScrollBarUI$WindowsArrowButton2;
    }

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtJTextArea() {
        if (_txtJTextArea==null) {
            _txtJTextArea = new JTextAreaOperator(this);
        }
        return _txtJTextArea;
    }

    /** Tries to find "Checkin Message:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCheckinMessage() {
        if (_lblCheckinMessage==null) {
            _lblCheckinMessage = new JLabelOperator(this, "Checkin Message:");
        }
        return _lblCheckinMessage;
    }

    /** Tries to find null JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btJButton() {
        if (_btJButton==null) {
            _btJButton = new JButtonOperator(this, 2);
        }
        return _btJButton;
    }

    /** Tries to find "Files To Checkin:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFilesToCheckin() {
        if (_lblFilesToCheckin==null) {
            _lblFilesToCheckin = new JLabelOperator(this, "Files To Checkin:");
        }
        return _lblFilesToCheckin;
    }

    /** Tries to find "Preserve Modification Time" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbPreserveModificationTime() {
        if (_cbPreserveModificationTime==null) {
            _cbPreserveModificationTime = new JCheckBoxOperator(this, "Preserve Modification Time");
        }
        return _cbPreserveModificationTime;
    }

    /** Tries to find "Force Checkin Of Unmodified Files" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbForceCheckinOfUnmodifiedFiles() {
        if (_cbForceCheckinOfUnmodifiedFiles==null) {
            _cbForceCheckinOfUnmodifiedFiles = new JCheckBoxOperator(this, "Force Checkin Of Unmodified Files");
        }
        return _cbForceCheckinOfUnmodifiedFiles;
    }

    /** Tries to find "Checkin" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCheckin() {
        if (_btCheckin==null) {
            _btCheckin = new JButtonOperator(this, "Checkin");
        }
        return _btCheckin;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }

    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, "Help");
        }
        return _btHelp;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** clicks on null WindowsScrollBarUI$WindowsArrowButton
     */
    public void windowsScrollBarUI$WindowsArrowButton() {
        btWindowsScrollBarUI$WindowsArrowButton().push();
    }

    /** clicks on null WindowsScrollBarUI$WindowsArrowButton
     */
    public void windowsScrollBarUI$WindowsArrowButton2() {
        btWindowsScrollBarUI$WindowsArrowButton2().push();
    }

    /** gets text for txtJTextArea
     * @return String text
     */
    public String getJTextArea() {
        return txtJTextArea().getText();
    }

    /** sets text for txtJTextArea
     * @param text String text
     */
    public void setJTextArea(String text) {
        txtJTextArea().setText(text);
    }

    /** types text for txtJTextArea
     * @param text String text
     */
    public void typeJTextArea(String text) {
        txtJTextArea().typeText(text);
    }

    /** clicks on null JButton
     */
    public void jButton() {
        btJButton().push();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkPreserveModificationTime(boolean state) {
        if (cbPreserveModificationTime().isSelected()!=state) {
            cbPreserveModificationTime().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkForceCheckinOfUnmodifiedFiles(boolean state) {
        if (cbForceCheckinOfUnmodifiedFiles().isSelected()!=state) {
            cbForceCheckinOfUnmodifiedFiles().push();
        }
    }

    /** clicks on "Checkin" JButton
     */
    public void checkin() {
        btCheckin().push();
    }

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }

    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of CheckinOperator by accessing all its components.
     */
    public void verify() {
        tabFilesToCheckin();
        btWindowsScrollBarUI$WindowsArrowButton();
        btWindowsScrollBarUI$WindowsArrowButton2();
        txtJTextArea();
        lblCheckinMessage();
        btJButton();
        lblFilesToCheckin();
        cbPreserveModificationTime();
        cbForceCheckinOfUnmodifiedFiles();
        btCheckin();
        btCancel();
        btHelp();
    }

    /** Performs simple test of CheckinOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new CheckinOperator().verify();
        System.out.println("CheckinOperator verification finished.");
    }
}

