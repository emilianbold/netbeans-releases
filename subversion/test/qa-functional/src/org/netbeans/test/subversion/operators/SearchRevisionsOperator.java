/*
 * SearchRevisions.java
 *
 * Created on 14/06/06 16:50
 */
package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Search Revisions" NbDialog.
 *
 * @author peter
 * @version 1.0
 */
public class SearchRevisionsOperator extends NbDialogOperator {

    /** Creates new SearchRevisions that can handle it.
     */
    public SearchRevisionsOperator() {
        super("Search Revisions");
    }

    private JListOperator _lstSelectARevision;
    private JButtonOperator _btWindowsScrollBarUI$WindowsArrowButton;
    private JButtonOperator _btWindowsScrollBarUI$WindowsArrowButton2;
    private JLabelOperator _lblSelectARevision;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null ListView$NbList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstSelectARevision() {
        if (_lstSelectARevision==null) {
            _lstSelectARevision = new JListOperator(this);
        }
        return _lstSelectARevision;
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

    /** Tries to find "Select a Revision:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSelectARevision() {
        if (_lblSelectARevision==null) {
            _lblSelectARevision = new JLabelOperator(this, "Select a Revision:");
        }
        return _lblSelectARevision;
    }

    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, "OK");
        }
        return _btOK;
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

    /** clicks on "OK" JButton
     */
    public void ok() {
        btOK().push();
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

    /** Performs verification of SearchRevisions by accessing all its components.
     */
    public void verify() {
        lstSelectARevision();
        btWindowsScrollBarUI$WindowsArrowButton();
        btWindowsScrollBarUI$WindowsArrowButton2();
        lblSelectARevision();
        btOK();
        btCancel();
        btHelp();
    }
}

