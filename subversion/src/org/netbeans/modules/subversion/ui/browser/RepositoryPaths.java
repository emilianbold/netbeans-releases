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
package org.netbeans.modules.subversion.ui.browser;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.ui.wizards.CheckoutWizard;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.explorer.ExplorerManager;
import org.openide.util.HelpCtx;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class RepositoryPaths implements ActionListener, DocumentListener {
    
    private final static RepositoryFile[] EMPTY_REPOSITORY_FILES = new RepositoryFile[0];

    private RepositoryFile repositoryFile;

    // controled components    
    private JTextComponent repositoryPathTextField;
    private JTextComponent revisionTextField;
    
    private JButton browseButton;
    private JButton searchRevisionButton;

    // browser
    private boolean showFiles = false;
    private boolean singleSelection = false;
    private boolean fileSelectionOnly = false;
    private boolean writeable;
    private BrowserAction[] browserActions;   
    
    public RepositoryPaths(RepositoryFile repositoryFile, 
                           JTextComponent repositoryPathTextField,  
                           JButton browseButton) 
    {
        assert repositoryFile != null;
        assert repositoryPathTextField != null;
        assert browseButton != null;
        
        this.repositoryFile = repositoryFile;
        this.repositoryPathTextField = repositoryPathTextField;
        repositoryPathTextField.getDocument().addDocumentListener(this);

        this.browseButton = browseButton;      
        
        browseButton.addActionListener(this);                
    }
    
    public RepositoryPaths(RepositoryFile repositoryFile, 
                           JTextComponent repositoryPathTextField,  
                           JButton browseButton, 
                           JTextField  revisionTextField, 
                           JButton searchRevisionButton) 
    {
        this(repositoryFile, repositoryPathTextField, browseButton);
        
        assert revisionTextField != null;
        assert searchRevisionButton != null;
        
        this.revisionTextField = revisionTextField;
        revisionTextField.getDocument().addDocumentListener(this);
        this.searchRevisionButton = searchRevisionButton;
    }

    public void setupBrowserBehavior(boolean singleSelection, boolean showFiles, boolean fileSelectionOnly) {
        this.singleSelection = singleSelection;
        this.fileSelectionOnly = fileSelectionOnly;
        this.showFiles = showFiles;                
    }            
    
    public void setupBrowserBehavior(boolean singleSelection, boolean showFiles, boolean fileSelectionOnly, BrowserAction[] browserActions) {
        setupBrowserBehavior(singleSelection, showFiles, fileSelectionOnly);
        this.browserActions = browserActions;
    }            
    
    public RepositoryFile[] getRepositoryFiles() throws MalformedURLException, NumberFormatException {

        SVNRevision revision = getRevision();

        if(repositoryPathTextField.getText().equals("")) {
            return EMPTY_REPOSITORY_FILES;
        }
        if(revision == null) {
            // should not be possible to get here!
            return EMPTY_REPOSITORY_FILES;
        }        
        String[] paths = repositoryPathTextField.getText().trim().split(",");
        RepositoryFile[] ret = new RepositoryFile[paths.length];
        SVNUrl repositoryUrl = getRepositoryUrl();
       
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i].trim();
            String repositoryUrlString = getRepositoryUrl().toString();
            if(path.startsWith("file://") ||
               path.startsWith("http://") ||
               path.startsWith("https://") ||
               path.startsWith("svn://") ||
               path.startsWith("svn+ssh://")) { 
                // must be a complete URL 
                // so check if it matches with the given repository URL
                if(path.startsWith(repositoryUrlString)) {
                    // lets take only the part without the repository base URL
                    ret[i] = new RepositoryFile(repositoryUrl, path.substring(repositoryUrlString.length()), revision);
                } else {
                    throw new MalformedURLException("The Url " + path + "doesn't start with " + repositoryUrlString);
                }
            } else {
                ret[i] = new RepositoryFile(repositoryUrl, path, revision);    
            }                
        }                                    
        return ret;
    }

    public void browseRepository() {
        SVNRevision revision = getRevision();
        RepositoryFile[] repositoryFilesToSelect;
        try {
            repositoryFilesToSelect = getRepositoryFiles();
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ex);
            return;
        }
        
        final Browser browser = 
            new Browser(
                org.openide.util.NbBundle.getMessage(CheckoutWizard.class, "LBL_CheckoutBrowser"), 
                showFiles, 
                singleSelection,
                fileSelectionOnly);        
        
        final DialogDescriptor dialogDescriptor = 
                new DialogDescriptor(browser.getBrowserPanel(), "Repository browser - " + getRepositoryUrl().toString()); 
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(Browser.class));
        dialogDescriptor.setValid(false);
        
        browser.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if( ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()) ) {
                    dialogDescriptor.setValid(browser.getSelectedNodes().length > 0);
                }
            }
        });
        
        browser.setup(
            new RepositoryFile(getRepositoryUrl(), revision), 
            repositoryFilesToSelect, 
            browserActions
        );
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.setVisible(true);

        // handle results
        if (DialogDescriptor.OK_OPTION.equals(dialogDescriptor.getValue())) {       
            RepositoryFile[] selectedFiles = browser.getSelectedFiles();
                        
            if(selectedFiles.length > 0) {                
                StringBuffer paths = new StringBuffer();
                for (int i = 0; i < selectedFiles.length; i++) {
                    paths.append(selectedFiles[i].getPath());
                    if(i < selectedFiles.length - 1) {
                        paths.append(", ");                    
                    }
                }                        
                repositoryPathTextField.setText(paths.toString());
            } 
        } else {
            browser.cancel(); 
        }
    }      
    
    private SVNRevision getRevision() {
        if(revisionTextField == null) {
            return SVNRevision.HEAD;
        }
        String revisionString = revisionTextField.getText();
        if(revisionString.equals("") || revisionString.equals(SVNRevision.HEAD.toString())) {
            return SVNRevision.HEAD;    
        }
        return new SVNRevision.Number(Long.parseLong(revisionString));        
    }       
    
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==browseButton) {
            browseRepository();
        }        
    }

    public SVNUrl getRepositoryUrl() {
        return repositoryFile.getRepositoryUrl();
    }

    public void setRepositoryFile(RepositoryFile repositoryFile) {
        this.repositoryFile = repositoryFile;
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
        try {
            getRepositoryFiles();
        } catch (NumberFormatException ex) {
            browseButton.setEnabled(false);
            if(searchRevisionButton!=null) {
                searchRevisionButton.setEnabled(false);
            }
            return;
        } catch (MalformedURLException ex) {
            browseButton.setEnabled(false);
            return;
        }
        browseButton.setEnabled(true);
        if(searchRevisionButton!=null) {
            searchRevisionButton.setEnabled(true);
        }
    }

}
