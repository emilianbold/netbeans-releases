/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools.modules.testtools;

/*
 * NodeGeneratorOperator.java
 *
 * Created on 8/30/02 3:45 PM
 */
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Jelly Node Generator" NbDialog.
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @version 1.0
 */
public class NodeGeneratorOperator extends NbDialogOperator {

    /** Creates new NodeGeneratorOperator that can handle it.
     */
    public NodeGeneratorOperator() {
        super("Jelly Node Generator");
    }

    private JTreeOperator _treeFilesystems;
    private JLabelOperator _lblSelectFilesystem;
    private JButtonOperator _btStart;
    private JButtonOperator _btClose;
    private JTextFieldOperator _txtNodesPackage;
    private JTextFieldOperator _txtActionsPackage;
    private JLabelOperator _lblNodesPackage;
    private JLabelOperator _lblActionsPackage;
    private JCheckBoxOperator _cbDefaultInline;
    private JCheckBoxOperator _cbDefaultNoBlock;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator treeFilesystems() {
        if (_treeFilesystems==null) {
            _treeFilesystems = new JTreeOperator(this);
        }
        return _treeFilesystems;
    }

    /** Tries to find "Select Destination Filesystem:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSelectFilesystem() {
        if (_lblSelectFilesystem==null) {
            _lblSelectFilesystem = new JLabelOperator(this, "Select Destination Filesystem:");
        }
        return _lblSelectFilesystem;
    }

    /** Tries to find "Start" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btStart() {
        if (_btStart==null) {
            _btStart = new JButtonOperator(this, "Start");
        }
        return _btStart;
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

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtNodesPackage() {
        if (_txtNodesPackage==null) {
            _txtNodesPackage = new JTextFieldOperator(this);
        }
        return _txtNodesPackage;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtActionsPackage() {
        if (_txtActionsPackage==null) {
            _txtActionsPackage = new JTextFieldOperator(this, 1);
        }
        return _txtActionsPackage;
    }

    /** Tries to find "Nodes Package: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblNodesPackage() {
        if (_lblNodesPackage==null) {
            _lblNodesPackage = new JLabelOperator(this, "Nodes Package: ");
        }
        return _lblNodesPackage;
    }

    /** Tries to find "Actions Package: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblActionsPackage() {
        if (_lblActionsPackage==null) {
            _lblActionsPackage = new JLabelOperator(this, "Actions Package: ");
        }
        return _lblActionsPackage;
    }

    /** Tries to find "Default Inline" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbDefaultInline() {
        if (_cbDefaultInline==null) {
            _cbDefaultInline = new JCheckBoxOperator(this, "Default Inline");
        }
        return _cbDefaultInline;
    }

    /** Tries to find "Default NoBlock" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbDefaultNoBlock() {
        if (_cbDefaultNoBlock==null) {
            _cbDefaultNoBlock = new JCheckBoxOperator(this, "Default NoBlock");
        }
        return _cbDefaultNoBlock;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** clicks on "Start" JButton
     */
    public void start() {
        btStart().push();
    }

    /** clicks on "Close" JButton
     */
    public void close() {
        btClose().push();
    }

    /** gets text for txtNodesPackage
     * @return String text
     */
    public String getNodesPackage() {
        return txtNodesPackage().getText();
    }

    /** sets text for txtNodesPackage
     * @param text String text
     */
    public void setNodesPackage(String text) {
        txtNodesPackage().setText(text);
    }

    /** types text for txtNodesPackage
     * @param text String text
     */
    public void typeNodesPackage(String text) {
        txtNodesPackage().typeText(text);
    }

    /** gets text for txtActionsPackage
     * @return String text
     */
    public String getActionsPackage() {
        return txtActionsPackage().getText();
    }

    /** sets text for txtActionsPackage
     * @param text String text
     */
    public void setActionsPackage(String text) {
        txtActionsPackage().setText(text);
    }

    /** types text for txtActionsPackage
     * @param text String text
     */
    public void typeActionsPackage(String text) {
        txtActionsPackage().typeText(text);
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkDefaultInline(boolean state) {
        if (cbDefaultInline().isSelected()!=state) {
            cbDefaultInline().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkDefaultNoBlock(boolean state) {
        if (cbDefaultNoBlock().isSelected()!=state) {
            cbDefaultNoBlock().push();
        }
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of NodeGeneratorOperator by accessing all its components.
     */
    public void verify() {
        treeFilesystems();
        lblSelectFilesystem();
        btStart();
        btClose();
        txtNodesPackage();
        txtActionsPackage();
        lblNodesPackage();
        lblActionsPackage();
        cbDefaultInline();
        cbDefaultNoBlock();
    }
}

