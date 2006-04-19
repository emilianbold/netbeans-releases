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
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * @author Tomas Stupka
 */
public class ImportStep extends AbstractStep implements DocumentListener, WizardDescriptor.AsynchronousValidatingPanel {
    
    private ImportPanel importPanel;

    private RepositoryPaths repositoryPaths;
    private BrowserAction[] actions;
    private File importDirectory;

    private Thread backgroundValidationThread;

    private ProgressHandle progress;
    private JPanel progressComponent;
    private JLabel progressLabel;
    private String invalidMsg;

    private SvnClient client;
    
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

            if(!validateUserInput()) {
                validationDone();
                return;
            }
        try {
            backgroundValidationThread = Thread.currentThread();
            try {
                client = Subversion.getInstance().getClient(repositoryPaths.getRepositoryUrl());
            } catch (SVNClientException ex) {
                ErrorManager.getDefault().notify(ex);
                valid(ex.getLocalizedMessage());
                validationDone();
                client = null;
                return;
            } 

            invalidMsg = null;
            invalid(null);

            try {
                SVNUrl repositoryUrl = repositoryPaths.getRepositoryUrl();
                SVNUrl repositoryFolderUrl = getRepositoryFolderUrl();
                client.mkdir(repositoryFolderUrl, getImportMessage());

                RepositoryFile[] repositoryFile = new RepositoryFile[] { new RepositoryFile(repositoryUrl, repositoryFolderUrl, SVNRevision.HEAD) };
                File checkoutFile = new File(importDirectory.getAbsolutePath() + ".co");
                // support - let's handle it as an atomic operation
                CheckoutAction.checkout(client, repositoryUrl, repositoryFile, checkoutFile, true, null);

                copyMetadata(checkoutFile, importDirectory);
                refreshRecursively(importDirectory);

                FileUtils.deleteRecursively(checkoutFile);

            } catch (SVNClientException ex) {
                ExceptionHandler eh = new ExceptionHandler(ex);
                eh.annotate();
                invalidMsg = ExceptionHandler.parseExceptionMessage(ex);
            } finally {
                client = null;
            }

   
        } finally {
            backgroundValidationThread = null;
            if(invalidMsg != null) {
                valid(invalidMsg);
            } else {
                valid();
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    validationDone();
                }
            });
        }          
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

    private void copyMetadata(File sourceFolder, File targetFolder) {
        // XXX there is already somewhere a utility method giving the metadata file suffix - ".svn", "_svn", ...
        FileUtils.copyDirFiles(new File(sourceFolder.getAbsolutePath() + "/.svn"), new File(targetFolder.getAbsolutePath() + "/.svn"), true);
        targetFolder.setLastModified(sourceFolder.lastModified());
        File[] files = sourceFolder.listFiles();
        for (int i = 0; i < files.length; i++) {
            if(files[i].isDirectory() && !files[i].getName().equals(".svn")) {
                copyMetadata(files[i], new File(targetFolder.getAbsolutePath() + "/" + files[i].getName()));
            } else {
                (new File(targetFolder.getAbsolutePath() + "/" + files[i].getName())).setLastModified(files[i].lastModified());
            }
        }
    }

    public void prepareValidation() {
        progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryStep.class, "BK2012")); // NOI18N
        JComponent bar = ProgressHandleFactory.createProgressComponent(progress);
        JButton stopButton = new JButton(org.openide.util.NbBundle.getMessage(RepositoryStep.class, "BK2022")); // NOI18N
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stop();
            }
        });
        progressComponent = new JPanel();
        progressComponent.setLayout(new BorderLayout(6, 0));
        progressLabel = new JLabel();
        progressComponent.add(progressLabel, BorderLayout.NORTH);
        progressComponent.add(bar, BorderLayout.CENTER);
        progressComponent.add(stopButton, BorderLayout.LINE_END);
        progress.start(/*2, 5*/);
        importPanel.progressPanel.setVisible(true);
        importPanel.progressPanel.add(progressComponent, BorderLayout.SOUTH);
        importPanel.progressPanel.revalidate();

        setEditable(false);

        OutputLogger logger = new OutputLogger(); // XXX to use the logger this way is a hack
        logger.logCommandLine("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + this.getClass().getName() + ".prepareValidation()");
    }

    private void validationDone() {
        progress.finish();
        importPanel.progressPanel.remove(progressComponent);
        importPanel.progressPanel.revalidate();
        importPanel.progressPanel.repaint();
        importPanel.progressPanel.setVisible(false);
        setEditable(true);

        OutputLogger logger = new OutputLogger(); // XXX to use the logger this way is a hack
        if(isValid()) {
            logger.logCommandLine("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + this.getClass().getName() + ".validationDone() - finnished");
        } else {
            logger.logCommandLine("==[IDE]== " + DateFormat.getDateTimeInstance().format(new Date()) + " " + this.getClass().getName() + ".validationDone() - finnished with error");
        }
    }

    private void setEditable(boolean editable) {
        importPanel.browseRepositoryButton.setEnabled(editable);
        importPanel.messageTextArea.setEditable(editable);
        importPanel.repositoryPathTextField.setEditable(editable);
    }
    
    public void stop() {
        if (backgroundValidationThread != null) {
            backgroundValidationThread.interrupt();
            invalidMsg = "Action cancelled by user.";
            if(client != null) {
                try {
                    client.cancelOperation(); 
                } catch (SVNClientException ex) {
                    ExceptionHandler eh = new ExceptionHandler(ex); 
                    eh.annotate();
                }
            }
        }
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

}

