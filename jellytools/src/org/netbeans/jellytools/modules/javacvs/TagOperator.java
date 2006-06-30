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

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.modules.javacvs.actions.TagAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/** Class implementing all necessary methods for handling "Tag" NbDialog.
 * dialog. It is opened from main menu or from popup on versioned file.
 * <br>
 * Usage:<br>
 * <pre>
 *      Node node = new Node(new SourcePackagesNode("MyProject"), "mypackage|MyFile");
 *      TagOperator to = TagOperator.invoke(node);
 *      to.setTagName("mytag");
 *      to.checkAvoidTaggingLocallyModifiedFiles(true);
 *      to.checkMoveExistingTag(true);
 *      to.tag();
 * </pre>
 *
 * @see org.netbeans.jellytools.modules.javacvs.actions.TagAction
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class TagOperator extends NbDialogOperator {

    /** Waits for dialog with "Tag" title. */
    public TagOperator() {
        super(Bundle.getStringTrimmed(
                "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                "CTL_TagDialog_Title"));
    }

    /** Selects nodes and call tag action on them.
     * @param nodes an array of nodes
     * @return TagOperator instance
     */
    public static TagOperator invoke(Node[] nodes) {
        new TagAction().perform(nodes);
        return new TagOperator();
    }
    
    /** Selects node and call merge action on it.
     * @param node node to be selected
     * @return TagOperator instance
     */
    public static TagOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }
    
    private JCheckBoxOperator _cbAvoidTaggingLocallyModifiedFilesC;
    private JCheckBoxOperator _cbMoveExistingTagF;
    private JTextFieldOperator _txtTagName;
    private JButtonOperator _btTag;
    private JButtonOperator _btBrowse;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "avoid tagging locally modified files (-c)" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbAvoidTaggingLocallyModifiedFiles() {
        if (_cbAvoidTaggingLocallyModifiedFilesC==null) {
            _cbAvoidTaggingLocallyModifiedFilesC = new JCheckBoxOperator(this,
                    Bundle.getStringTrimmed(
                        "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                        "CTL_TagForm_EnsureUptodate"));
        }
        return _cbAvoidTaggingLocallyModifiedFilesC;
    }

    /** Tries to find "move existing tag (-F)" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbMoveExistingTag() {
        if (_cbMoveExistingTagF==null) {
            _cbMoveExistingTagF = new JCheckBoxOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                    "CTL_TagForm_MoveExisting"));
        }
        return _cbMoveExistingTagF;
    }

    /** Tries to find tag name JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtTagName() {
        if (_txtTagName==null) {
            _txtTagName = new JTextFieldOperator(this);
        }
        return _txtTagName;
    }

    
    /** Returns operator of "Tag" button.
     * @return  JButtonOperator instance of "Tag" button
     */
    public JButtonOperator btTag() {
        if (_btTag == null) {
            String tagCaption = Bundle.getString("org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle", "CTL_TagDialog_Action_Tag");
            _btTag = new JButtonOperator(this, tagCaption);
        }
        return _btTag;
    }
    
    
    /** Returns operator of "Browse" button.
     * @return  JButtonOperator instance of "Browse" button
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse == null) {
            String browseCaption = Bundle.getStringTrimmed("org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle", "CTL_BrowseTag");
            _btBrowse = new JButtonOperator(this, browseCaption);
        }
        return _btBrowse;
    }
    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkAvoidTaggingLocallyModifiedFiles(boolean state) {
        if (cbAvoidTaggingLocallyModifiedFiles().isSelected()!=state) {
            cbAvoidTaggingLocallyModifiedFiles().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkMoveExistingTag(boolean state) {
        if (cbMoveExistingTag().isSelected()!=state) {
            cbMoveExistingTag().push();
        }
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

    /** Pushes "Tag" button. */
    public void tag() {
        btTag().push();
    }
    
    /** Pushes "Browse..." button. */
    public void browse() {
        btBrowse().pushNoBlock();
    }
    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of TagOperator by accessing all its components.
     */
    public void verify() {
        cbAvoidTaggingLocallyModifiedFiles();
        cbMoveExistingTag();
        txtTagName();
        btTag();
        btBrowse();
    }
}

