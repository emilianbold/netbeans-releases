/*
 * MergeOperator.java
 *
 * Created on 16/05/06 15:09
 */
package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.netbeans.test.subversion.operators.actions.MergeAction;

/** Class implementing all necessary methods for handling "Merge AnagramGame to..." NbDialog.
 *
 * @author peter
 * @version 1.0
 */
public class MergeOperator extends JDialogOperator {

    /**
     * Creates new MergeOperator that can handle it.
     */
    public MergeOperator() {
        super("Merge");
    }

    /** Selects nodes and call merge action on them.
     * @param nodes an array of nodes
     * @return MergeOperator instance
     */
    public static MergeOperator invoke(Node[] nodes) {
        new MergeAction().perform(nodes);
        return new MergeOperator();
    }
    
    /** Selects node and call merge action on it.
     * @param node node to be selected
     * @return CommitOperator instance
     */
    public static MergeOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }

    private JComboBoxOperator _cboMergeFrom;
    private JButtonOperator _btMerge;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboMergeFrom() {
        if (_cboMergeFrom==null) {
            _cboMergeFrom = new JComboBoxOperator(this, 1);
        }
        return _cboMergeFrom;
    }

    
    /** Tries to find "Merge" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMerge() {
        if (_btMerge==null) {
            _btMerge = new JButtonOperator(this, "Merge");
        }
        return _btMerge;
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

    /**
     * returns selected item for cboMergeFrom
     * 
     * @return String item
     */
    public String getSelectedMergeFrom() {
        return cboMergeFrom().getSelectedItem().toString();
    }

    /**
     * selects item for cboMergeFrom
     * 
     * @param item String item
     */
    public void selectMergeFrom(String item) {
        cboMergeFrom().selectItem(item);
    }

    /**
     * types text for cboMergeFrom
     * 
     * @param text String text
     */
    public void setMergeFrom(String text) {
        cboMergeFrom().clearText();
        cboMergeFrom().typeText(text);
    }
}

