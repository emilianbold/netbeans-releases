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

package org.netbeans.modules.subversion.ui.wizards.importstep;

import java.awt.event.FocusEvent;
import java.io.File;
import java.net.MalformedURLException;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.WizardStepProgressSupport;
import org.netbeans.modules.subversion.ui.browser.Browser;
import org.netbeans.modules.subversion.ui.wizards.AbstractStep;
import org.netbeans.modules.subversion.ui.browser.BrowserAction;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.ui.checkout.CheckoutAction;
import org.netbeans.modules.subversion.util.FileUtils;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.SVNClientException;
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
            support =  new ImportProgressSupport(importPanel.progressPanel, importPanel.progressLabel);  
            SVNUrl url = getUrl();
            support.setRepositoryRoot(url);            
            RequestProcessor rp = Subversion.getInstance().getRequestProcessor(url);
            RequestProcessor.Task task = support.start(rp, url, org.openide.util.NbBundle.getMessage(ImportStep.class, "CTL_Import_Progress"));
            task.waitFinished();
        } finally {
            support = null;
        }
    }   
        
    public void prepareValidation() {        
    }

    private SVNUrl getUrl() {        
        RepositoryFile repositoryFile = getRepositoryFile();
        return repositoryFile.getRepositoryUrl();
    }
    
    public boolean validateUserInput() {
        invalid(null);
        
        String text = importPanel.repositoryPathTextField.getText().trim();
        if (text.length() == 0) {
            invalid(org.openide.util.NbBundle.getMessage(ImportStep.class, "BK2014")); // NOI18N
            return false;
        }        
        
        text = importPanel.messageTextArea.getText().trim();
        boolean valid = text.length() > 0;
        if(valid) {
            valid();
        } else {
            invalid(org.openide.util.NbBundle.getMessage(ImportStep.class, "CTL_Import_MessageRequired")); // NOI18N
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
        if(importPanel.repositoryPathTextField.getText().trim().equals("")) { // NOI18N
            // no value set yet ...
            if(repositoryPaths == null) {
                repositoryPaths =
                    new RepositoryPaths (
                        repositoryFile,
                        importPanel.repositoryPathTextField,
                        importPanel.browseRepositoryButton,
                        null,
                        null
                    );
                String browserPurposeMessage = org.openide.util.NbBundle.getMessage(ImportStep.class, "LBL_BrowserMessage");
                int browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY;
                repositoryPaths.setupBrowserBehavior(browserPurposeMessage, browserMode, actions, Browser.BROWSER_HELP_ID_IMPORT);
            } else {
                repositoryPaths.setRepositoryFile(repositoryFile);
            }
        }
        importPanel.repositoryPathTextField.setText(repositoryFile.getPath());
        validateUserInput();
    }

    public RepositoryFile getRepositoryFile() {
        try {
            return repositoryPaths.getRepositoryFiles()[0]; // more files doesn't make sence
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } 
        return null;
    }

    public SVNUrl getRepositoryFolderUrl() {
        return getRepositoryFile().getFileUrl();
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
        public ImportProgressSupport(JPanel panel, JLabel label) {
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
                    SvnClientExceptionHandler.notifyException(ex, true, true);
                    invalidMsg = SvnClientExceptionHandler.parseExceptionMessage(ex);
                    return;
                }

                try {
                    RepositoryFile repositoryFile = getRepositoryFile();
                    SVNUrl repositoryUrl = repositoryFile.getRepositoryUrl();
                    try {
                        // if the user came back from the last step and changed the repository folder name,
                        // then this could be already a working copy ...    
                        FileUtils.deleteRecursively(new File(importDirectory.getAbsoluteFile() + "/" + ".svn")); // NOI18N
                        FileUtils.deleteRecursively(new File(importDirectory.getAbsoluteFile() + "/" + "_svn")); // NOI18N
                        File importDummyFolder = new File(System.getProperty("java.io.tmpdir") + "/svn_dummy/" + importDirectory.getName()); // NOI18N
                        importDummyFolder.mkdirs();                     
                        importDummyFolder.deleteOnExit();
                        client.doImport(importDummyFolder, repositoryFile.getFileUrl(), getImportMessage(), false);
                    } catch (SVNClientException ex) {
                        if(SvnClientExceptionHandler.isFileAlreadyExists(ex.getMessage()) ) {
                            // ignore
                        } else {
                            throw ex;
                        }         
                    }
                    if(isCanceled()) {
                        return;
                    }

                    RepositoryFile[] repositoryFiles = new RepositoryFile[] { repositoryFile };
                    CheckoutAction.checkout(client, repositoryUrl, repositoryFiles, importDirectory, true, this);
                    Subversion.getInstance().versionedFilesChanged();
                    SvnUtils.refreshRecursively(importDirectory);
                    // XXX this is ugly and expensive! the client should notify (onNotify()) the cache. find out why it doesn't work...
                    forceStatusRefresh(importDirectory);  // XXX the same for another implementations like this in the code.... (see SvnUtils.refreshRecursively() )
                    if(isCanceled()) {                        
                        FileUtils.deleteRecursively(new File(importDirectory.getAbsoluteFile() + "/" + ".svn")); // NOI18N
                        FileUtils.deleteRecursively(new File(importDirectory.getAbsoluteFile() + "/" + "_svn")); // NOI18N
                        return;
                    }
                } catch (SVNClientException ex) {
                    annotate(ex);
                    invalidMsg = SvnClientExceptionHandler.parseExceptionMessage(ex);
                }

            } finally {
                Subversion.getInstance().versionedFilesChanged();
                if(isCanceled()) {
                    valid(org.openide.util.NbBundle.getMessage(ImportStep.class, "MSG_Import_ActionCanceled")); // NOI18N
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
    };

    private static void forceStatusRefresh(File file) {
        Subversion.getInstance().getStatusCache().refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        if(!file.isFile()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                forceStatusRefresh(files[i]);
            }
        }                
    }
    
}

