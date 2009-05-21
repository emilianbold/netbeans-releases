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
package org.netbeans.jellytools.modules.javacvs;

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
 *       Node node = new Node(new SourcePackagesNode("MyProject"), "mypackage|MyFile");
 *       MergeChangesFromBranchOperator mcfbo = MergeChangesFromBranchOperator.invoke(node);
 *       mcfbo.mergeFromTag();
 *       BrowseTagsOperator bto = mcfbo.browseStartingFromTag();
 *       bto.selectPath("HEAD");
 *       bto.ok();
 *       // or mcfbo.setStartingFromTag("mytag");
 *       mcfbo.mergeUntilBranchHead();
 *       bto = mcfbo.browseBranchHead();
 *       bto.selectBranch("mybranch");
 *       bto.ok();
 *       // or mcfbo.setBranchHead("myBranch");
 *       mcfbo.mergeUntilTag();
 *       bto = mcfbo.browseUntilTag();
 *       bto.selectTag("myTag");
 *       bto.ok();
 *       // or mcfbo.setUntilTag("myTag");
 *       mcfbo.mergeTrunkHead();
 *       mcfbo.checkTagAfterMerge(true);
 *       bto = mcfbo.browseTagName();
 *       bto.selectTag("mytag");
 *       bto.ok();
 *       // or mcfbo.setTagName("myTagAfterMerge");
 *       mcfbo.merge();
 * </pre>
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
    
    private JLabelOperator _lblWorkingBranch;
    private JTextFieldOperator _txtWorkingBranch;
    private JRadioButtonOperator _rbBranchingPoint;
    private JRadioButtonOperator _rbStartingFromTag;
    private JTextFieldOperator _txtStartingFromTag;
    private JButtonOperator _btBrowseStartingFromTag;
    private JRadioButtonOperator _rbTrunkHead;
    private JRadioButtonOperator _rbBranchHead;
    private JTextFieldOperator _txtBranchHead;
    private JButtonOperator _btBrowseBranchHead;
    private JRadioButtonOperator _rbUntilTag;
    private JTextFieldOperator _txtTagName; 
    private JTextFieldOperator _txtUntilTag;
    private JButtonOperator _btBrowseUntilTag;
    private JCheckBoxOperator _cbTagAfterMerge;
    private JButtonOperator _btBrowseTagName;
    private JButtonOperator _btMerge;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Merge Changes Into Working Branch:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblWorkingBranch() {
        if (_lblWorkingBranch==null) {
            _lblWorkingBranch = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "MergePanel.jLabel1.text"));
        }
        return _lblWorkingBranch;
    }
    
    /** Tries to find "Merge Changes Into Working Branch:" JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtWorkingBranch() {
        if (_txtWorkingBranch == null) {
            _txtWorkingBranch = new JTextFieldOperator(this, 1);
        }
        return _txtWorkingBranch;
    }
    
    /** Tries to find "Branching Point / Branch Root" JRadioButton in this dialog.
     * @return Branching Point / Branch Root JRadioButtonOperator
     */
    public JRadioButtonOperator rbBranchingPoint() {
        if (_rbBranchingPoint==null) {
            _rbBranchingPoint = new JRadioButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle", 
                    "MergePanel.rbFromBranchRoot.text"));
        }
        return _rbBranchingPoint;
    }
    
    /** Tries to find "Tag / Revision" JRadioButton in Starting From section of this dialog.
     * @return "Tag / Revision" JRadioButtonOperator
     */
    public JRadioButtonOperator rbStartingFromTag() {
        if (_rbStartingFromTag == null) {
            _rbStartingFromTag = new JRadioButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle", 
                    "MergePanel.rbFromTag.text"));
        }
        return _rbStartingFromTag;
    }
    
    /** Tries to find "Tag / Revision" JTextField in in Starting From section of this dialog.
     * @return "Tag / Revision" JTextFieldOperator
     */
    public JTextFieldOperator txtStartingFromTag() {
        if (_txtStartingFromTag == null) {
            _txtStartingFromTag = new JTextFieldOperator(this, 2);
        }
        return _txtStartingFromTag;
    }
    
    /** Tries to find "Browse..." JButton in in Starting From section of this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseStartingFromTag() {
        if (_btBrowseStartingFromTag == null) {
            _btBrowseStartingFromTag = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_MergeBranchForm_Browse"), 1);
        }
        return _btBrowseStartingFromTag;
    }
    
    /** Tries to find "Trunk HEAD" JRadioButton in Until section of this dialog.
     * @return "Trunk HEAD" JRadioButtonOperator
     */
    public JRadioButtonOperator rbTrunkHead() {
        if (_rbTrunkHead == null) {
            _rbTrunkHead = new JRadioButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "MergePanel.rbToHEAD.text"));
        }
        return _rbTrunkHead;
    }
    
    /** Tries to find "Branch Head" JRadioButton in Until section of this dialog.
     * @return "Branch Head" JRadioButtonOperator
     */
    public JRadioButtonOperator rbBranchHead() {
        if (_rbBranchHead == null) {
            _rbBranchHead = new JRadioButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "MergePanel.rbToBranchHead.text"));
        }
        return _rbBranchHead;
    }
 
    /** Tries to find "Branch Head" JTextField in in Until section of this dialog.
     * @return "Branch Head" JTextFieldOperator
     */
    public JTextFieldOperator txtBranchHead() {
        if (_txtBranchHead == null) {
            _txtBranchHead = new JTextFieldOperator(this, 4);
        }
        return _txtBranchHead;
    }
    
    /** Tries to find "Browse..." JButton in Until Branch Head section of this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseBranchHead() {
        if (_btBrowseBranchHead == null) {
            _btBrowseBranchHead = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_MergeBranchForm_Browse"), 2);
        }
        return _btBrowseBranchHead;
    }
    
    /** Tries to find "Tag / Revision" JRadioButton in Until section of this dialog.
     * @return "Tag / Revision" JRadioButtonOperator
     */
    public JRadioButtonOperator rbUntilTag() {
        if (_rbUntilTag == null) {
            _rbUntilTag = new JRadioButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "MergePanel.rbToTag.text"), 1);
        }
        return _rbUntilTag;
    }
 
    /** Tries to find "Tag Name" JTextField in this dialog.
     * @return "Tag Name" JTextFieldOperator
     */
    public JTextFieldOperator txtTagName() {
        if (_txtTagName == null) {
            _txtTagName = new JTextFieldOperator(this, 0);
        }
        return _txtTagName;
    }
    
    /** Tries to find "Tag / Revision" JTextField in Until section of this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtUntilTag() {
        if (_txtUntilTag == null) {
            _txtUntilTag = new JTextFieldOperator(this, 3);
        }
        return _txtUntilTag;
    }
    
    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseUntilTag() {
        if (_btBrowseUntilTag==null) {
            _btBrowseUntilTag = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_MergeBranchForm_Browse"), 3);
        }
        return _btBrowseUntilTag;
    }
    
    /** Tries to find "Tag "branch_name" after Merge" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbTagAfterMerge() {
        if (_cbTagAfterMerge==null) {
            _cbTagAfterMerge = new JCheckBoxOperator(this);
        }
        return _cbTagAfterMerge;
    }
    
    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseTagName() {
        if (_btBrowseTagName == null) {
            _btBrowseTagName = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "MergePanel.browseAfterMergeTag.text"), 0);
        }
        return _btBrowseTagName;
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
    
    /** Gets working branch name.
     * @return Working branch name.
     */
    public String getWorkingBranch() {
        return txtWorkingBranch().getText();
    }
    
    /** clicks on "Branching Point" JRadioButton
     */
    public void mergeFromBranchingPoint() {
        rbBranchingPoint().push();
    }
    
    /** clicks on "Tag / Revision" JRadioButton in Starting From Section
     */
    public void mergeFromTag() {
        rbStartingFromTag().push();
    }
    
    /** clicks on "Trunk HEAD" JRadioButton in Until section
     */
    public void mergeTrunkHead() {
        rbTrunkHead().push();
    }
    
    /** clicks on "Branch Head" JRadioButton in Until section. */
    public void mergeUntilBranchHead() {
        rbBranchHead().push();
    }
    
    /** clicks on "Tag / Revision" JRadioButton in Until section. */
    public void mergeUntilTag() {
        rbUntilTag().push();
    }

    /** gets text for "Tag / Revision" in Starting From Section
     * @return String text
     */
    public String getStartingFromTag() {
        return txtStartingFromTag().getText();
    }
    
    /** sets text for "Tag / Revision" in Starting From Section
     * @param text String text
     */
    public void setStartingFromTag(String text) {
        txtStartingFromTag().setText(text);
    }
    
    /** clicks on "Browse..." JButton and returns BrowseTagsOperator
     * @return BrowseTagsOperator instance
     */
    public BrowseTagsOperator browseStartingFromTag() {
        btBrowseStartingFromTag().pushNoBlock();
        return new BrowseTagsOperator();
    }
    
    /** gets text for "Branch Head" in Until section
     * @return String text
     */
    public String getBrancHead() {
        return txtBranchHead().getText();
    }
    
    /** sets text for "Branch Head" in Until section
     * @param text String text
     */
    public void setBranchHead(String text) {
        txtBranchHead().setText(text);
    }
    
    /** clicks on "Browse..." JButton Branch Head and returns BrowseTagsOperator
     * @return BrowseTagsOperator instance
     */
    public BrowseTagsOperator browseBranchHead() {
        btBrowseBranchHead().pushNoBlock();
        return new BrowseTagsOperator();
    }    
    
    /** gets text for "Tag / Revision" in Until section
     * @return String text
     */
    public String getUntilTag() {
        return txtUntilTag().getText();
    }
    
    /** sets text for "Tag / Revision" in Until section
     * @param text String text
     */
    public void setUntilTag(String text) {
        txtUntilTag().setText(text);
    }
    
    /** clicks on "Browse..." JButton "Until Tag / Revision" and returns BrowseTagsOperator
     * @return BrowseTagsOperator instance
     */
    public BrowseTagsOperator browseUntilTag() {
        btBrowseUntilTag().pushNoBlock();
        return new BrowseTagsOperator();
    }
    
    /** checks or unchecks "Tag After Merge" checkbox
     * @param state boolean requested state
     */
    public void checkTagAfterMerge(boolean state) {
        if (cbTagAfterMerge().isSelected() != state) {
            cbTagAfterMerge().push();
        }
    }
    
    /** Sets text for "Tag Name" text field.
     * @param text text
     */
    public void setTagName(String text) {
        txtTagName().setText(text);
    }
    
    /** clicks on "Browse..." JButton and returns BrowseTagsOperator
     * @return BrowseTagsOperator instance
     */
    public BrowseTagsOperator browseTagName() {
        btBrowseTagName().pushNoBlock();
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
        lblWorkingBranch();
        txtWorkingBranch();
        rbBranchingPoint();
        rbStartingFromTag();
        txtStartingFromTag();
        btBrowseStartingFromTag();
        txtUntilTag();
        btBrowseUntilTag();
        cbTagAfterMerge();
        btBrowseTagName();
        btMerge();
    }
}

