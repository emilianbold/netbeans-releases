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

package org.netbeans.modules.subversion.ui.wizards.checkoutstep;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.settings.HistorySettings;
import org.netbeans.modules.subversion.ui.wizards.AbstractStep;
import org.netbeans.modules.subversion.ui.wizards.CheckoutWizard;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.util.AccessibleJFileChooser;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * @author Tomas Stupka
 */
public class CheckoutStep extends AbstractStep implements ActionListener, DocumentListener, FocusListener {
    
    private CheckoutPanel workdirPanel;
    private RepositoryPaths repositoryPaths; 
    
    public HelpCtx getHelp() {    
        return new HelpCtx(CheckoutStep.class);
    }    

    protected JComponent createComponent() {
        if (workdirPanel == null) {
            workdirPanel = new CheckoutPanel();
            workdirPanel.browseWorkdirButton.addActionListener(this);
            workdirPanel.browseRepositoryButton.addActionListener(this);
            
            workdirPanel.workdirTextField.setText(defaultWorkingDirectory().getPath());            
            workdirPanel.workdirTextField.getDocument().addDocumentListener(this);                
            workdirPanel.workdirTextField.addFocusListener(this);
            workdirPanel.repositoryPathTextField.getDocument().addDocumentListener(this);        
            workdirPanel.repositoryPathTextField.addFocusListener(this);
            workdirPanel.revisionTextField.getDocument().addDocumentListener(this);
            workdirPanel.revisionTextField.addFocusListener(this);
        }          
        validateUserInput(true);                                
        return workdirPanel;              
    }

    public void setup(RepositoryFile repositoryFile) {
        if(repositoryPaths == null) {                    
            repositoryPaths = 
                new RepositoryPaths(
                        repositoryFile, 
                        workdirPanel.repositoryPathTextField, 
                        workdirPanel.browseRepositoryButton, 
                        workdirPanel.revisionTextField, 
                        workdirPanel.searchRevisionButton
                );        
            repositoryPaths.setupBrowserBehavior(false, false, false);
        } else {
            repositoryPaths.setRepositoryFile(repositoryFile);
        }                
        workdirPanel.repositoryPathTextField.setText(repositoryFile.getPath());
        if(!repositoryFile.getRevision().equals(SVNRevision.HEAD)) {
            workdirPanel.revisionTextField.setText(repositoryFile.getRevision().toString());
        }
    }    
     
    protected void validateBeforeNext() {
        if (validateUserInput(true)) {
            String text = workdirPanel.workdirTextField.getText();
            File file = new File(text);
            if (file.exists() == false) {
                boolean done = file.mkdirs();
                if (done == false) {
                    invalid(org.openide.util.NbBundle.getMessage(CheckoutWizard.class, "BK2013") + file.getPath());// NOI18N
                }
            }
        }
    }

    private boolean validateUserInput(boolean full) {                
        if(repositoryPaths != null) {
            try {           
                repositoryPaths.getRepositoryFiles();
            } catch (NumberFormatException ex) {
                invalid(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2018"));// NOI18N
                return false;
            } catch (MalformedURLException ex) {
                invalid(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2015"));// NOI18N
                return false;
            }
        }
        
        String text = workdirPanel.workdirTextField.getText();
        if (text == null || text.length() == 0) {
            invalid(org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2014"));// NOI18N
            return false;
        }
        
        String errorMessage = null;
        if (full) {
            File file = new File(text);
            if (file.exists() == false) {
                // it's automaticaly create later on, check for permisions here
                File parent = file.getParentFile();
                while (parent != null) {
                    if (parent.exists()) {
                        if (parent.canWrite() == false) {
                            errorMessage = org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2016") + parent.getPath();// NOI18N
                        }
                        break;
                    }
                    parent = parent.getParentFile();
                }
            } else {
                if (file.isFile()) {
                    errorMessage = org.openide.util.NbBundle.getMessage(CheckoutStep.class, "BK2017");// NOI18N
                }
            }
        }

        if (errorMessage == null) {
            valid();
        } else {
            invalid(errorMessage);
        }

        return errorMessage == null;
    }
    
    private void onBrowseWorkdir() {
        File defaultDir = defaultWorkingDirectory();
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(CheckoutStep.class, "ACSD_BrowseFolder"), defaultDir);// NOI18N
        fileChooser.setDialogTitle(NbBundle.getMessage(CheckoutStep.class, "BK0010"));// NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        FileFilter[] old = fileChooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            FileFilter fileFilter = old[i];
            fileChooser.removeChoosableFileFilter(fileFilter);

        }
        fileChooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory();
            }
            public String getDescription() {
                return NbBundle.getMessage(CheckoutStep.class, "BK0008");// NOI18N
            }
        });
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(workdirPanel, NbBundle.getMessage(CheckoutStep.class, "BK0009"));// NOI18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            workdirPanel.workdirTextField.setText(f.getAbsolutePath());
        }                
    }    
    
    /**
     * Returns file to be initaly used.
     * <ul>
     * <li>first is takes text in workTextField
     * <li>then recent project folder
     * <li>then recent checkout folder
     * <li>finally <tt>user.home</tt>
     * <ul>
     */
    private File defaultWorkingDirectory() {
        File defaultDir = null;
        String current = workdirPanel.workdirTextField.getText();
        if (current != null && !(current.trim().equals(""))) {  // NOI18N
            File currentFile = new File(current);
            while (currentFile != null && currentFile.exists() == false) {
                currentFile = currentFile.getParentFile();
            }
            if (currentFile != null) {
                if (currentFile.isFile()) {
                    defaultDir = currentFile.getParentFile();
                } else {
                    defaultDir = currentFile;
                }
            }
        }

        if (defaultDir == null) {
            List recent = HistorySettings.getRecent(HistorySettings.PROP_CHECKOUT_DIRECTORY);
            Iterator it = recent.iterator();

            while (it.hasNext()) {
                String path = (String) it.next();
                File file = new File(path);
                File parent = file.getParentFile();
                if (parent != null && parent.exists() && parent.isDirectory()) {
                    defaultDir = file;
                    break;
                }
            }
        }

        if (defaultDir == null) {
            File projectFolder = ProjectChooser.getProjectsFolder();
            if (projectFolder.exists() && projectFolder.isDirectory()) {
                defaultDir = projectFolder;
            }
        }

        if (defaultDir == null) {
            defaultDir = new File(System.getProperty("user.home"));  // NOI18N
        }

        return defaultDir;
    }

    public void insertUpdate(DocumentEvent e) {
        validateUserInput(false);
    }

    public void removeUpdate(DocumentEvent e) {
        validateUserInput(false);
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        validateUserInput(true);
    }
        
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==workdirPanel.browseWorkdirButton) {            
            onBrowseWorkdir();
        } 
    }
    
    public File getWorkdir() {
        return new File(workdirPanel.workdirTextField.getText());
    }        

    public RepositoryFile[] getRepositoryFiles() {
        try {
            return repositoryPaths.getRepositoryFiles();
        } catch (MalformedURLException ex) {
            ex.printStackTrace(); // should not happen
        }
        return null;
    }
}

