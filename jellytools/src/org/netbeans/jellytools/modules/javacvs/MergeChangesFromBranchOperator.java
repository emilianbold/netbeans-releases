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
import org.netbeans.jellytools.modules.javacvs.actions.MergeAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;


/** Class implementing all necessary methods for handling "Merge Changes from Branch"
 * dialog. It is opened from main menu or from popup on versioned file.
 * <br>
 * Usage:<br>
 * <pre>
 *      Node node = new Node(new SourcePackagesNode("MyProject"), "mypackage|MyFile");
 *      MergeChangesFromBranchOperator mcfbo = MergeChangesFromBranchOperator.invoke(node);
 *      mcfbo.mergeFromBranch();
 *      BrowseTagsOperator bto = mcfbo.browseMergeFromBranch();
 *      bto.selectPath("HEAD");
 *      bto.ok();
 *      // or mcfbo.setMergeFromBranch("mybranch");
 *      mcfbo.checkMergeAfterTag(true);
 *      bto = mcfbo.browseMergeAfterTag();
 *      bto.selectTag("myTag");
 *      bto.ok();
 *      // or mcfbo.setMergeAfterTag("myTag");
 *      mcfbo.checkTagAfterMerge(true);
 *      bto = mcfbo.browseTagAfterMerge();
 *      bto.selectTag("myTagAfterMerge");
 *      bto.ok();
 *      // or mcfbo.setTagAfterMerge("myTagAfterMerge");
 *      mcfbo.merge();
 *</pre>
 *
 * @see BrowseTagsOperator
 * @see org.netbeans.jellytools.modules.javacvs.actions.MergeAction
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class MergeChangesFromBranchOperator extends NbDialogOperator {
    
    /** Waits for dialog with "Merge Changes from Branch" title. */
    public MergeChangesFromBranchOperator() {
        super(Bundle.getStringTrimmed(
                "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                "CTL_MergeBranchDialog_Title"));
    }
    
    /** Selects nodes and call merge action on them.
     * @param nodes an array of nodes
     * @return MergeChangesFromBranchOperator instance
     */
    public static MergeChangesFromBranchOperator invoke(Node[] nodes) {
        new MergeAction().perform(nodes);
        return new MergeChangesFromBranchOperator();
    }
    
    /** Selects node and call merge action on it.
     * @param node node to be selected
     * @return MergeChangesFromBranchOperator instance
     */
    public static MergeChangesFromBranchOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }
    
    private JLabelOperator _lblCurrentWorkingBranch;
    private JTextFieldOperator _txtCurrentWorkingBranch;
    private JRadioButtonOperator _rbMergeFromTrunk;
    private JRadioButtonOperator _rbMergeFromBranch;
    private JTextFieldOperator _txtMergeFromBranch;
    private JButtonOperator _btBrowseMergeFromBranch;
    private JCheckBoxOperator _cbMergeOnlyChangesMadeAfterTag;
    private JLabelOperator _lblTagName;
    private JTextFieldOperator _txtMergeAfterTag;
    private JButtonOperator _btBrowseMergeAfterTag;
    private JCheckBoxOperator _cbTagBranch_nameBranchAfterMerge;
    private JLabelOperator _lblTagName2;
    private JTextFieldOperator _txtTagAfterMerge;
    private JButtonOperator _btBrowseTagAfterMerge;
    private JButtonOperator _btMerge;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Current Working Branch:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCurrentWorkingBranch() {
        if (_lblCurrentWorkingBranch==null) {
            _lblCurrentWorkingBranch = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_MergeBranchForm_CurrentBranch"));
        }
        return _lblCurrentWorkingBranch;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtCurrentWorkingBranch() {
        if (_txtCurrentWorkingBranch==null) {
            _txtCurrentWorkingBranch = new JTextFieldOperator(
                    (JTextField)lblCurrentWorkingBranch().getLabelFor());
        }
        return _txtCurrentWorkingBranch;
    }
    
    /** Tries to find "Merge from Trunk" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbMergeFromTrunk() {
        if (_rbMergeFromTrunk==null) {
            _rbMergeFromTrunk = new JRadioButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_MergeBranchForm_MergeFromTrunk"));
        }
        return _rbMergeFromTrunk;
    }
    
    /** Tries to find "Merge from Branch" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbMergeFromBranch() {
        if (_rbMergeFromBranch==null) {
            _rbMergeFromBranch = new JRadioButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_MergeBranchForm_MergeFromBranch"));
        }
        return _rbMergeFromBranch;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtMergeFromBranch() {
        if (_txtMergeFromBranch==null) {
            _txtMergeFromBranch = new JTextFieldOperator(this, 1);
        }
        return _txtMergeFromBranch;
    }
    
    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseMergeFromBranch() {
        if (_btBrowseMergeFromBranch==null) {
            _btBrowseMergeFromBranch = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_MergeBranchForm_Browse"));
        }
        return _btBrowseMergeFromBranch;
    }
    
    /** Tries to find "Merge Only Changes Made after Tag" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbMergeOnlyChangesMadeAfterTag() {
        if (_cbMergeOnlyChangesMadeAfterTag==null) {
            _cbMergeOnlyChangesMadeAfterTag = new JCheckBoxOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_MergeBranchForm_UseMergeTag"));
        }
        return _cbMergeOnlyChangesMadeAfterTag;
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
    public JTextFieldOperator txtMergeAfterTag() {
        if (_txtMergeAfterTag==null) {
            _txtMergeAfterTag = new JTextFieldOperator(this, 2);
        }
        return _txtMergeAfterTag;
    }
    
    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseMergeAfterTag() {
        if (_btBrowseMergeAfterTag==null) {
            _btBrowseMergeAfterTag = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_MergeBranchForm_Browse"), 1);
        }
        return _btBrowseMergeAfterTag;
    }
    
    /** Tries to find "Tag "branch_name" Branch after Merge" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbTagAfterMerge() {
        if (_cbTagBranch_nameBranchAfterMerge==null) {
            _cbTagBranch_nameBranchAfterMerge = new JCheckBoxOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_MergeBranchForm_TagAfterMerge_Branch"));
        }
        return _cbTagBranch_nameBranchAfterMerge;
    }
    
    /** Tries to find "Tag Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTagName2() {
        if (_lblTagName2==null) {
            _lblTagName2 = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_BranchForm_BaseTagName"), 1);
        }
        return _lblTagName2;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtTagAfterMerge() {
        if (_txtTagAfterMerge==null) {
            _txtTagAfterMerge = new JTextFieldOperator(this, 3);
        }
        return _txtTagAfterMerge;
    }
    
    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseTagAfterMerge() {
        if (_btBrowseTagAfterMerge==null) {
            _btBrowseTagAfterMerge = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_MergeBranchForm_Browse"), 2);
        }
        return _btBrowseTagAfterMerge;
    }
    
    /** Tries to find "Merge" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMerge() {
        if (_btMerge==null) {
            _btMerge = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_MergeBranchDialog_Action_Merge"));
        }
        return _btMerge;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** gets text for txtCurrentWorkingBranch
     * @return String text
     */
    public String getCurrentWorkingBranch() {
        return txtCurrentWorkingBranch().getText();
    }
    
    /** clicks on "Merge from Trunk" JRadioButton
     */
    public void mergeFromTrunk() {
        rbMergeFromTrunk().push();
    }
    
    /** clicks on "Merge from Branch" JRadioButton
     */
    public void mergeFromBranch() {
        rbMergeFromBranch().push();
    }
    
    /** gets text for txtMergeFromBranch
     * @return String text
     */
    public String getMergeFromBranch() {
        return txtMergeFromBranch().getText();
    }
    
    /** sets text for txtMergeFromBranch
     * @param text String text
     */
    public void setMergeFromBranch(String text) {
        txtMergeFromBranch().clearText();
        txtMergeFromBranch().typeText(text);
    }
    
    /** clicks on "Browse..." JButton and returns BrowseTagsOperator
     * @return BrowseTagsOperator instance
     */
    public BrowseTagsOperator browseMergeFromBranch() {
        btBrowseMergeFromBranch().pushNoBlock();
        return new BrowseTagsOperator();
    }
    
    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkMergeAfterTag(boolean state) {
        if (cbMergeOnlyChangesMadeAfterTag().isSelected()!=state) {
            cbMergeOnlyChangesMadeAfterTag().push();
        }
    }
    
    /** gets text for txtMergeAfterTag
     * @return String text
     */
    public String getMergeAfterTag() {
        return txtMergeAfterTag().getText();
    }
    
    /** sets text for txtMergeAfterTag
     * @param text String text
     */
    public void setMergeAfterTag(String text) {
        txtMergeAfterTag().clearText();
        txtMergeAfterTag().typeText(text);
    }
    
    /** clicks on "Browse..." JButtonand returns BrowseTagsOperator
     * @return BrowseTagsOperator instance
     */
    public BrowseTagsOperator browseMergeAfterTag() {
        btBrowseMergeAfterTag().pushNoBlock();
        return new BrowseTagsOperator();
    }
    
    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkTagAfterMerge(boolean state) {
        if (cbTagAfterMerge().isSelected()!=state) {
            cbTagAfterMerge().push();
        }
    }
    
    /** gets text for txtTagAfterMerge
     * @return String text
     */
    public String getTagAfterMerge() {
        return txtTagAfterMerge().getText();
    }
    
    /** sets text for txtTagAfterMerge
     * @param text String text
     */
    public void setTagAfterMerge(String text) {
        txtTagAfterMerge().clearText();
        txtTagAfterMerge().typeText(text);
    }
    
    /** clicks on "Browse..." JButton and returns BrowseTagsOperator
     * @return BrowseTagsOperator instance
     */
    public BrowseTagsOperator browseTagAfterMerge() {
        btBrowseTagAfterMerge().pushNoBlock();
        return new BrowseTagsOperator();
    }
    
    /** clicks on "Merge" JButton
     */
    public void merge() {
        btMerge().push();
    }
    
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /**
     * Performs verification of MergeChangesFromBranchOperator by accessing all its components.
     */
    public void verify() {
        lblCurrentWorkingBranch();
        txtCurrentWorkingBranch();
        rbMergeFromTrunk();
        rbMergeFromBranch();
        txtMergeFromBranch();
        btBrowseMergeFromBranch();
        cbMergeOnlyChangesMadeAfterTag();
        lblTagName();
        txtMergeAfterTag();
        btBrowseMergeAfterTag();
        cbTagAfterMerge();
        lblTagName2();
        txtTagAfterMerge();
        btBrowseTagAfterMerge();
        btMerge();
    }
}

