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

package org.netbeans.modules.subversion.ui.wizards.importstep;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.OutputLogger;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.ExceptionHandler;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.WizardStepProgressSupport;
import org.netbeans.modules.subversion.ui.wizards.AbstractStep;
import org.netbeans.modules.subversion.ui.browser.BrowserAction;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.ui.checkout.CheckoutAction;
import org.netbeans.modules.subversion.ui.wizards.repositorystep.RepositoryStep;
import org.netbeans.modules.subversion.util.FileUtils;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * @author Tomas Stupka
 */
public class ImportStep extends AbstractStep implements DocumentListener, WizardDescriptor.AsynchronousValidatingPanel, WizardDescriptor.FinishablePanel {
    
    private ImportPanel importPanel;

    private RepositoryPaths repositoryPaths;
    private BrowserAction[] actions;
    private File importDirectory;       
    private WizardStepProgressSupport support;
    
    public ImportStep(BrowserAction[] actions, File importDirectory) {
        this.actions = actions;
        this.importDirectory = importDirectory;
    }
    
    public HelpCtx getHelp() {    
        return new HelpCtx(ImportStep.class);
    }    

    protected JComponent createComponent() {
        if (importPanel == null) {
            importPanel = new ImportPanel();            
            importPanel.messageTextArea.getDocument().addDocumentListener(this);            
            importPanel.repositoryPathTextField.getDocument().addDocumentListener(this);                       
        }            
        return importPanel;              
    }

    protected void validateBeforeNext() {
        try {
            if(support != null) {
                support.performInCurrentThread("Importing"); 
            }
        } finally {
            support = null;
        }
    }   

    public void prepareValidation() {
        support = new ImportProgressSupport(importPanel.progressPanel);
        support.startProgress();
    }

    public boolean validateUserInput() {
        String text = importPanel.repositoryPathTextField.getText().trim();
        if (text.length() == 0) {
            invalid(org.openide.util.NbBundle.getMessage(ImportStep.class, "BK2014"));
            return false;
        }        
        
        text = importPanel.messageTextArea.getText().trim();
        boolean valid = text.length() > 0;
        if(valid) {
            valid();
        } else {
            invalid("Import message required");
        }

        return valid;
    }
    
    public void insertUpdate(DocumentEvent e) {
        validateUserInput();
    }

    public void removeUpdate(DocumentEvent e) {
        validateUserInput();
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void focusGained(FocusEvent e) {
        
    }

    public void focusLost(FocusEvent e) {
        validateUserInput();
    }

    public String getImportMessage() {
        return importPanel.messageTextArea.getText();
    }

    public void setup(RepositoryFile repositoryFile) {
        if(importPanel.repositoryPathTextField.getText().trim().equals("")) {
            // no value set yet ...
            if(repositoryPaths == null) {
                repositoryPaths =
                    new RepositoryPaths (
                        repositoryFile,
                        importPanel.repositoryPathTextField,
                        importPanel.browseRepositoryButton
                    );
                repositoryPaths.setupBrowserBehavior(true, false, false, actions);
            } else {
                repositoryPaths.setRepositoryFile(repositoryFile);
            }
            importPanel.repositoryPathTextField.setText(repositoryFile.getPath());
        }
    }

    public SVNUrl getRepositoryFolderUrl() {
        try {
            return repositoryPaths.getRepositoryFiles()[0].getFileUrl(); // more files doesn't make sence
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } 
        return null;
    }

    public void stop() {
        if(support != null) {
            support.cancel();
        }
    }

    public boolean isFinishPanel() {
        return true;
    }

    private class ImportProgressSupport extends WizardStepProgressSupport {
        public ImportProgressSupport(JPanel panel) {
            super(panel);
        }
        public void perform() {
            String invalidMsg = null;
            try {
                if(!validateUserInput()) {
                    return;
                }        

                invalid(null);

                SvnClient client;
                try {
                    client = Subversion.getInstance().getClient(repositoryPaths.getRepositoryUrl(), this);
                } catch (SVNClientException ex) {
                    ErrorManager.getDefault().notify(ex);
                    valid(ex.getLocalizedMessage());
                    return;
                }

                try {
                    SVNUrl repositoryUrl = repositoryPaths.getRepositoryUrl();
                    SVNUrl repositoryFolderUrl = getRepositoryFolderUrl();
                    try {
                        client.mkdir(repositoryFolderUrl, getImportMessage());
                    } catch (SVNClientException ex) {
                        if(ExceptionHandler.isFileAlreadyExists(ex)) {
                            // ignore
                        } else {
                            throw ex;
                        }                        
                    }
                    if(isCanceled()) {
                        return;
                    }

                    RepositoryFile[] repositoryFile = new RepositoryFile[] { new RepositoryFile(repositoryUrl, repositoryFolderUrl, SVNRevision.HEAD) };                    
                    CheckoutAction.checkout(client, repositoryUrl, repositoryFile, importDirectory, true, this);
                    refreshRecursively(importDirectory);
                    if(isCanceled()) {                        
                        FileUtils.deleteRecursively(new File(importDirectory.getAbsoluteFile() + "/" + ".svn"));
                        FileUtils.deleteRecursively(new File(importDirectory.getAbsoluteFile() + "/" + "_svn"));
                        refreshRecursively(importDirectory);
                        return;
                    }
                } catch (SVNClientException ex) {
                    ExceptionHandler eh = new ExceptionHandler(ex);
                    eh.annotate();
                    invalidMsg = ExceptionHandler.parseExceptionMessage(ex);
                }

            } finally {
                if(isCanceled()) {
                    valid("Action cancelled by user.");
                } else if(invalidMsg != null) {
                    valid(invalidMsg);
                } else {
                    valid();
                }
            }
        }            

        public void setEditable(boolean editable) {
            importPanel.browseRepositoryButton.setEnabled(editable);
            importPanel.messageTextArea.setEditable(editable);
            importPanel.repositoryPathTextField.setEditable(editable);
        }

        private void deleteDirectory(File file) {
             File[] files = file.listFiles();
             if(files !=null || files.length > 0) {
                 for (int i = 0; i < files.length; i++) {
                     if(files[i].isDirectory()) {
                         deleteDirectory(files[i]);
                     } else {
                        files[i].delete();
                     }
                 }
             }
             file.delete();
        }

        private void refreshRecursively(File folder) {
            if (folder == null) return;
            refreshRecursively(folder.getParentFile());
            Subversion.getInstance().getStatusCache().refresh(folder, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }        
    };
}

