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
package org.netbeans.modules.subversion.ui.update;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import javax.swing.JButton;
import javax.swing.JRadioButton;
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
public class RevertModifications implements PropertyChangeListener {

    private RevertModificationsPanel panel;
    private JButton okButton;
    private JButton cancelButton;
    private RevertType[] types;
    
    /** Creates a new instance of RevertModifications */
    public RevertModifications(RepositoryFile repositoryFile) {
        this (repositoryFile, null);
    }

    /** Creates a new instance of RevertModifications */
    public RevertModifications(RepositoryFile repositoryFile, String defaultRevision) {
        OneCommitRevertType ocrt = new OneCommitRevertType(repositoryFile, getPanel().oneCommitRadioButton);
        types = new RevertType[] {
            new LocalRevertType(getPanel().localChangesRadioButton),
            ocrt,
            new MoreCommitsRevertType(repositoryFile, getPanel().moreCommitsRadioButton)
        };
        okButton = new JButton(org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_RevertForm_Action_Revert")); // NOI18N
        okButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSD_RevertForm_Action_Revert")); // NOI18N
        cancelButton = new JButton(org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_RevertForm_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSD_RevertForm_Action_Cancel")); // NOI18N
        if (defaultRevision != null) {
            panel.oneCommitRadioButton.setSelected(true);
            panel.oneRevisionTextField.setText(defaultRevision);
            ocrt.actionPerformed(null);
        }
    } 
    
    private RevertModificationsPanel getPanel() {
        if(panel == null) {
            panel = new RevertModificationsPanel();
        }
        return panel;
    }

    RevisionInterval getRevisionInterval() {
        for (int i = 0; i < types.length; i++) {
            if(types[i].isSelected()) {
                return types[i].getRevisionInterval();
            }
        }
        return null;
    }      

    boolean revertNewFiles() {
        for (int i = 0; i < types.length; i++) {
            if(types[i].isSelected()) {
                return types[i].revertNewFiles();
            }
        }
        return false;
    }      
    
    public boolean showDialog() {
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, org.openide.util.NbBundle.getMessage(RevertModifications.class, "CTL_RevertDialog")); // NOI18N
        dialogDescriptor.setOptions(new Object[] {okButton, cancelButton});
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
        dialogDescriptor.setValid(false);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RevertModifications.class, "ACSD_RevertDialog")); // NOI18N
        dialog.setVisible(true);
        dialog.setResizable(false);
        boolean ret = dialogDescriptor.getValue() == okButton;
        return ret;       
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if( evt.getPropertyName().equals(RepositoryPaths.PROP_VALID) ) {
            if(okButton != null) {
                boolean valid = ((Boolean)evt.getNewValue()).booleanValue();
                okButton.setEnabled(valid);
            }
        }        
    }

    protected void setMoreCommitsFieldsEnabled(boolean b) {
        getPanel().startRevisionTextField.setEnabled(b);
        getPanel().endRevisionTextField.setEnabled(b);
        getPanel().startSearchButton.setEnabled(b);
        getPanel().endSearchButton.setEnabled(b);
    }

    protected void setOneCommitFieldsEnabled(boolean b) {
        getPanel().oneRevisionSearchButton.setEnabled(b);
        getPanel().oneRevisionTextField.setEnabled(b);
    }
        
    static class RevisionInterval {
        SVNRevision startRevision;
        SVNRevision endRevision;
    }

    private abstract class RevertType implements ActionListener, DocumentListener {
        private JRadioButton button;

        RevertType(JRadioButton button) {
            this.button = button;
            button.addActionListener(this);
        }

        boolean isSelected() {
            return button.isSelected();
        }

        boolean revertNewFiles() {
            return panel.revertNewFilesCheckBox.isSelected();
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

        void validateUserInput() {
            // default means nothing to do
        }

        RevisionInterval getRevisionInterval() {
            return null; // default means null
        }
        
        protected SVNRevision getRevision(RepositoryPaths path) {
            try {
                return path.getRepositoryFiles()[0].getRevision();
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

        protected boolean validateRevision(SVNRevision revision) {
            boolean valid = revision == null || revision.equals(SVNRevision.HEAD) || revision.getKind() == SVNRevision.Kind.number;
            RevertModifications.this.okButton.setEnabled(valid);
            return valid;
        }
    }

    private class LocalRevertType extends RevertType {

        LocalRevertType (JRadioButton button) {
            super(button);
        }

        RevertModifications.RevisionInterval getRevisionInterval() {
            return null;
        }

        public void actionPerformed(ActionEvent e) {
            setOneCommitFieldsEnabled(false);
            setMoreCommitsFieldsEnabled(false);
        }
    }

    private class OneCommitRevertType extends RevertType {

        private RepositoryPaths oneRevisionPath;

        OneCommitRevertType (RepositoryFile repositoryFile, JRadioButton button) {
            super(button);
            oneRevisionPath =
                new RepositoryPaths(
                    repositoryFile,
                    null,
                    null,
                    getPanel().oneRevisionTextField,
                    getPanel().oneRevisionSearchButton
                );
            oneRevisionPath.addPropertyChangeListener(RevertModifications.this);
        }

        RevertModifications.RevisionInterval getRevisionInterval() {
            SVNRevision revision = getRevision(oneRevisionPath);
            RevisionInterval ret = new RevisionInterval();
            ret.startRevision = revision;
            ret.endRevision = revision;
            return ret;
        }

        void validateUserInput() {
            validateRevision(getRevision(oneRevisionPath));
        }

        public void actionPerformed(ActionEvent e) {
            setOneCommitFieldsEnabled(true);
            setMoreCommitsFieldsEnabled(false);
            validateUserInput();
        }

    }

    private class MoreCommitsRevertType extends RevertType {

        private RepositoryPaths endPath;
        private RepositoryPaths startPath;

        MoreCommitsRevertType (RepositoryFile repositoryFile, JRadioButton button) {
            super(button);
            startPath =
                new RepositoryPaths(
                    repositoryFile,
                    null,
                    null,
                    getPanel().startRevisionTextField,
                    getPanel().startSearchButton
                );
            startPath.addPropertyChangeListener(RevertModifications.this);

            endPath =
                new RepositoryPaths(
                    repositoryFile,
                    null,
                    null,
                    getPanel().endRevisionTextField,
                    getPanel().endSearchButton
                );
            endPath.addPropertyChangeListener(RevertModifications.this);
        }

        RevertModifications.RevisionInterval getRevisionInterval() {                       
            SVNRevision revision1 = getRevision(startPath);
            SVNRevision revision2 = getRevision(endPath);
            if(revision1 == null || revision2 == null) {
                return null;
            }

            return getResortedRevisionInterval(revision1, revision2);            
        }

        void validateUserInput() {
            if(!validateRevision(getRevision(startPath))) {
                return;
            }
            if(!validateRevision(getRevision(endPath))) {
                return;
            }
        }

        public void actionPerformed(ActionEvent e) {
            setMoreCommitsFieldsEnabled(true);
            setOneCommitFieldsEnabled(false);
            validateUserInput();
        }

        private RevisionInterval getResortedRevisionInterval(SVNRevision revision1, SVNRevision revision2) {
            RevisionInterval ret = new RevisionInterval ();
            if(revision1.equals(SVNRevision.HEAD) &&
               revision1.equals(SVNRevision.HEAD))
            {
                ret.startRevision = revision1;
                ret.endRevision = revision2;
            } else if (revision1.equals(SVNRevision.HEAD)) {
                ret.startRevision = revision2;
                ret.endRevision = revision1;
            } else if (revision2.equals(SVNRevision.HEAD)) {
                ret.startRevision = revision1;
                ret.endRevision = revision2;
            } else {
                Long r1 = Long.parseLong(revision1.toString());
                Long r2 = Long.parseLong(revision2.toString());
                if(r1.compareTo(r2) < 0) {
                    ret.startRevision = revision1;
                    ret.endRevision = revision2;
                } else {
                    ret.startRevision = revision2;
                    ret.endRevision = revision1;
                }
            }
            return ret;
        }
        
    }    

}
