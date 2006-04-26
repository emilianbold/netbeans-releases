/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.update;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 *
 * @author Tomas Stupka
 */
public class RevertModifications implements ActionListener, DocumentListener {

    private RevertModificationsPanel panel;
    private RepositoryPaths endPath;
    private RepositoryPaths startPath;
    private JButton okButton;

    /** Creates a new instance of RevertModifications */
    public RevertModifications(RepositoryFile repositoryFile) {
        startPath =
            new RepositoryPaths(
                repositoryFile,
                null,
                null,
                getPanel().startRevisionTextField,
                getPanel().startSearchButton
            );

        endPath =
            new RepositoryPaths(
                repositoryFile,
                null,
                null,
                getPanel().endRevisionTextField,
                getPanel().endSearchButton
            );

        getPanel().lcoalChangesRadioButton.addActionListener(this);
        getPanel().commitsRadioButton.addActionListener(this);
    }

    private RevertModificationsPanel getPanel() {
        if(panel == null) {
            panel = new RevertModificationsPanel();
        }
        return panel;
    }

    SVNRevision getStartRevision() {
        try {
            return startPath.getRepositoryFiles()[0].getRevision();
        } catch (NumberFormatException ex) {
            // should be already checked and
            // not happen at this place anymore
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (MalformedURLException ex) {
            // should be already checked and
            // not happen at this place anymore
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }

    SVNRevision getEndRevision() {
        try {
            return endPath.getRepositoryFiles()[0].getRevision();
        } catch (NumberFormatException ex) {
            // should be already checked and
            // not happen at this place anymore
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (MalformedURLException ex) {
            // should be already checked and
            // not happen at this place anymore
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }

    boolean isLocal() {
        return getPanel().lcoalChangesRadioButton.isSelected();
    }

    boolean isCommits() {
        return getPanel().commitsRadioButton.isSelected();
    }

    public boolean showDialog() {
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, "RevertModifications");
        
        okButton = new JButton("Revert");
        dialogDescriptor.setOptions(new Object[] {okButton, "Cancel"});
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
        dialogDescriptor.setValid(false);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.setVisible(true);
        boolean ret = dialogDescriptor.getValue()==okButton;
        return ret;       
    }

    private void enableCommitsFields(boolean b) {
        getPanel().startRevisionTextField.setEnabled(b);
        getPanel().endRevisionTextField.setEnabled(b);
        getPanel().startSearchButton.setEnabled(b);
        getPanel().endSearchButton.setEnabled(b);
        getPanel().inclusiveCheckBox.setEnabled(b);
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == getPanel().lcoalChangesRadioButton) {
            enableCommitsFields(false);
            okButton.setEnabled(true);
        } else if (e.getSource() == getPanel().commitsRadioButton) {
            enableCommitsFields(true);
            validateUserInput();
        }
    }

    public void insertUpdate(DocumentEvent e) {
        validateUserInput();
    }

    public void removeUpdate(DocumentEvent e) {
        validateUserInput();
    }

    public void changedUpdate(DocumentEvent e) {
        validateUserInput();
    }

    private void validateUserInput() {
        SVNRevision revision = getStartRevision();
        if(revision == null || revision.equals(SVNRevision.HEAD)) {
            okButton.setEnabled(false);
        } else {
            okButton.setEnabled(true);
        }
    }
}
