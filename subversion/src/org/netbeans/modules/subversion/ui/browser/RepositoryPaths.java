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
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JTextField;
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
public class RepositoryPaths implements ActionListener {
    
    private final static RepositoryFile[] EMPTY_REPOSITORY_FILES = new RepositoryFile[0];

    // controled components
    private RepositoryFile repositoryFile;
    private JTextField repositoryPathTextField;
    private JTextField revisionTextField;
    private JButton browseButton;
    private JButton searchRevisionButton;

    // browser
    private boolean showFiles = false;
    private boolean singleSelection = false;
    private boolean writeable;
    private BrowserAction[] browserActions;
    
    public RepositoryPaths(RepositoryFile repositoryFile, 
                           JTextField repositoryPathTextField,  
                           JButton browseButton) 
    {
        assert repositoryFile != null;
        assert repositoryPathTextField != null;
        assert browseButton != null;
        
        this.repositoryFile = repositoryFile;
        this.repositoryPathTextField = repositoryPathTextField;
        this.browseButton = browseButton;      
        
        browseButton.addActionListener(this);                
    }
    
    public RepositoryPaths(RepositoryFile repositoryFile, 
                           JTextField repositoryPathTextField,  
                           JButton browseButton, 
                           JTextField  revisionTextField, 
                           JButton searchRevisionButton) 
    {
        this(repositoryFile, repositoryPathTextField, browseButton);
        
        assert revisionTextField != null;
        assert searchRevisionButton != null;
        
        this.revisionTextField = revisionTextField;
        this.searchRevisionButton = searchRevisionButton;
    }

    public void setupBrowserBehavior(boolean singleSelection, boolean showFiles) {
        this.singleSelection = singleSelection;
        this.showFiles = showFiles;                
    }            
    
    public void setupBrowserBehavior(boolean singleSelection, boolean showFiles, BrowserAction[] browserActions) {
        setupBrowserBehavior(singleSelection, showFiles);        
        this.browserActions = browserActions;
    }            
    
    public RepositoryFile[] getRepositoryFiles() {
        if(repositoryPathTextField.getText().equals("")) {
            return EMPTY_REPOSITORY_FILES;
        }
        String[] paths = repositoryPathTextField.getText().trim().split(",");
        RepositoryFile[] ret = new RepositoryFile[paths.length];
        SVNUrl repositoryUrl = repositoryFile.getRepositoryUrl();
        SVNRevision revision = getRevision();
        if(revision == null) {
            // XXX should not be even allowed to get here!
        }
        for (int i = 0; i < paths.length; i++) {
            try {
                ret[i] = new RepositoryFile(repositoryUrl, paths[i].trim(), revision);
            } catch (MalformedURLException ex) {
                ErrorManager.getDefault().notify(ex);
                // XXX somethig more userfirendly
            }
        }
        return ret;
    }

    public void browseRepository() {
        SVNRevision revision = getRevision();
        if(revision==null) {
            // XXX
            return;
        }
        RepositoryFile[] repositoryFilesToSelect = getRepositoryFiles();
        
        final Browser browser = 
            new Browser(
                org.openide.util.NbBundle.getMessage(CheckoutWizard.class, "LBL_CheckoutBrowser"), 
                showFiles, 
                singleSelection);        
        
        final DialogDescriptor dialogDescriptor = 
                new DialogDescriptor(browser.getBrowserPanel(), "Repository browser - " + repositoryFile.getRepositoryUrl().toString()); // XXX
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
            new RepositoryFile(repositoryFile.getRepositoryUrl(), revision), 
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
            browser.reset(); // XXX
        }
    }      
    
    public SVNRevision getRevision() {
        if(revisionTextField == null) {
            return SVNRevision.HEAD;
        }
        String revisionString = revisionTextField.getText();
        if(revisionString.equals("") || revisionString.equals(SVNRevision.HEAD.toString())) {
            return SVNRevision.HEAD;    
        }
        try {            
            return new SVNRevision.Number(Long.parseLong(revisionString));
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            return null;
        }                
    }       
    
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==browseButton) {
            browseRepository();
        }        
    }

    public RepositoryFile getRepositoryFile() {
        return repositoryFile;
    }
    
}
