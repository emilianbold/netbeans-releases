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
package org.netbeans.jellytools.modules.javacvs;

import javax.swing.JTextField;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.modules.javacvs.actions.BranchAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;


/** Class implementing all necessary methods for handling "Branch" dialog.
 * It is opened from main menu or from popup on versioned file.
 * <br>
 * Usage:<br>
 * <pre>
 *      Node node = new Node(new SourcePackagesNode("MyProject"), "mypackage|MyFile");
 *      BranchOperator bro = BranchOperator.invoke(node);
 *      bro.setBranchName("mybranch");
 *      // or
 *      //BrowseTagsOperator bto = bro.browse();
 *      //bto.selectBranch("mybranch");
 *      //bto.ok();
 *      //bro.checkTagBeforeBranching(false);
 *      bro.setTagName("tagname");
 *      bro.checkSwitchToThisBranchAftewards(false);
 *      bro.branch();
 *</pre>
 *
 * @see BrowseTagsOperator
 * @see org.netbeans.jellytools.modules.javacvs.actions.BranchAction
 *
 * @author Jiri.Skrivanek@sun.com
*/
public class BranchOperator extends NbDialogOperator {
    
    /** Waits for "Branch" dialog. */
    public BranchOperator() {
        super(Bundle.getStringTrimmed(
                "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                "CTL_BranchDialog_Title"));
    }
    
    /** Selects nodes and call branch action on them.
     * @param nodes an array of nodes
     * @return BranchOperator instance
     */
    public static BranchOperator invoke(Node[] nodes) {
        new BranchAction().perform(nodes);
        return new BranchOperator();
    }
    
    /** Selects node and call branch action on it.
     * @param node node to be selected
     * @return BranchOperator instance
     */
    public static BranchOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }
    
    private JCheckBoxOperator _cbTagBeforeBranching;
    private JCheckBoxOperator _cbSwitchToThisBranchAftewards;
    private JLabelOperator _lblBranchName;
    private JTextFieldOperator _txtBranchName;
    private JButtonOperator _btBrowse;
    private JLabelOperator _lblTagName;
    private JTextFieldOperator _txtTagName;
    private JButtonOperator _btBranch;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Tag Before Branching" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbTagBeforeBranching() {
        if (_cbTagBeforeBranching==null) {
            _cbTagBeforeBranching = new JCheckBoxOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_BranchForm_TagBase"));
        }
        return _cbTagBeforeBranching;
    }
    
    /** Tries to find "Switch to This Branch Aftewards" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbSwitchToThisBranchAftewards() {
        if (_cbSwitchToThisBranchAftewards==null) {
            _cbSwitchToThisBranchAftewards = new JCheckBoxOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_BranchForm_UpdateToBranch"));
        }
        return _cbSwitchToThisBranchAftewards;
    }
    
    /** Tries to find "Branch Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblBranchName() {
        if (_lblBranchName==null) {
            _lblBranchName = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_BranchForm_BranchName"));
        }
        return _lblBranchName;
    }
    
    /** Tries to find Branch Name JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtBranchName() {
        if (_txtBranchName==null) {
            _txtBranchName = new JTextFieldOperator(
                    (JTextField)lblBranchName().getLabelFor());
        }
        return _txtBranchName;
    }
    
    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_BranchForm_BrowseBranch"));
        }
        return _btBrowse;
    }
    
    /** Tries to find "Tag Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTagName() {
        if (_lblTagName==null) {
            _lblTagName = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_BranchForm_BaseTagName"));
        }
        return _lblTagName;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtTagName() {
        if (_txtTagName==null) {
            _txtTagName = new JTextFieldOperator(
                    (JTextField)lblTagName().getLabelFor());
        }
        return _txtTagName;
    }
    
    /** Tries to find "Branch" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBranch() {
        if (_btBranch==null) {
            _btBranch = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_BranchDialog_Action_Branch"));
        }
        return _btBranch;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkTagBeforeBranching(boolean state) {
        if (cbTagBeforeBranching().isSelected()!=state) {
            cbTagBeforeBranching().push();
        }
    }
    
    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkSwitchToThisBranchAftewards(boolean state) {
        if (cbSwitchToThisBranchAftewards().isSelected()!=state) {
            cbSwitchToThisBranchAftewards().push();
        }
    }
    
    /** gets text for txtBranchName
     * @return String text
     */
    public String getBranchName() {
        return txtBranchName().getText();
    }
    
    /** sets text for txtBranchName
     * @param text String text
     */
    public void setBranchName(String text) {
        txtBranchName().clearText();
        txtBranchName().typeText(text);
    }
    
    /** clicks on "Browse..." JButton and returns BrowseTagsOperator.
     * @return BrowseTagsOperator instance
     */
    public BrowseTagsOperator browse() {
        btBrowse().pushNoBlock();
        return new BrowseTagsOperator();
    }
    
    /** gets text for txtTagName
     * @return String text
     */
    public String getTagName() {
        return txtTagName().getText();
    }
    
    /** sets text for txtTagName
     * @param text String text
     */
    public void setTagName(String text) {
        txtTagName().clearText();
        txtTagName().typeText(text);
    }
    
    /** clicks on "Branch" JButton
     */
    public void branch() {
        btBranch().push();
    }
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of BranchOperator by accessing all its components.
     */
    public void verify() {
        cbTagBeforeBranching();
        cbSwitchToThisBranchAftewards();
        lblBranchName();
        txtBranchName();
        btBrowse();
        lblTagName();
        txtTagName();
        btBranch();
    }
}
