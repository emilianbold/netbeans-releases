/*
 * RepositoryBrowserOperator.java
 *
 * Created on 19/04/06 13:26
 */
package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.TreeTableOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "RepositoryBrowserOperator" NbDialog.
 *
 * @author peter
 * @version 1.0
 */
public class RepositoryBrowserOperator extends NbDialogOperator {

    /** Creates new RepositoryBrowserOperator that can handle it.
     */
    public RepositoryBrowserOperator() {
        super("Browse Repository");
    }

    private TreeTableOperator _tree;
    private JLabelOperator _lblSpecifyFolderToCheckout;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public TreeTableOperator tree() {
        if (_tree == null) {
            _tree= new TreeTableOperator(this);
        }
        return _tree;
    }

    /** Tries to find "Specify Folder to checkout:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSpecifyFolderToCheckout() {
        if (_lblSpecifyFolderToCheckout==null) {
            _lblSpecifyFolderToCheckout = new JLabelOperator(this);
        }
        return _lblSpecifyFolderToCheckout;
    }
    
    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** Selects a folder denoted by path.
     * @param path path to folder without root (e.g. "folder|subfolder")
     */
    public void selectFolder(String path) {
        new Node(tree().tree(), path).select();
    }
    
    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, "OK");
        }
        return _btOK;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }

    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, "Help");
        }
        return _btHelp;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** clicks on "OK" JButton
     */
    @Override
    public void ok() {
        btOK().push();
    }

    /** clicks on "Cancel" JButton
     */
    @Override
    public void cancel() {
        btCancel().push();
    }

    /** clicks on "Help" JButton
     */
    @Override
    public void help() {
        btHelp().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of RepositoryBrowserOperator by accessing all its components.
     */
    public void verify() {
        tree();
        lblSpecifyFolderToCheckout();
        btOK();
        btCancel();
        btHelp();
    }
}

