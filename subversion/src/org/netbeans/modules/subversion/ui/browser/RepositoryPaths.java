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
public class RepositoryPaths implements ActionListener {
    
    private final static RepositoryFile[] EMPTY_REPOSITORY_FILES = new RepositoryFile[0];

    // controled components
    private RepositoryFile repositoryFile;
    private JTextComponent repositoryPathTextField;
    private JTextComponent revisionTextField;
    
    // XXX don't use directly the fields - do it through accessors (cbo...)
    
    private JButton browseButton;
    private JButton searchRevisionButton;

    // browser
    private boolean showFiles = false;
    private boolean singleSelection = false;
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
    
    public RepositoryFile[] getRepositoryFiles() throws MalformedURLException, NumberFormatException {
        if(repositoryPathTextField.getText().equals("")) {
            return EMPTY_REPOSITORY_FILES;
        }
        
        String[] paths = repositoryPathTextField.getText().trim().split(",");
        RepositoryFile[] ret = new RepositoryFile[paths.length];
        SVNUrl repositoryUrl = getRepositoryUrl();
        SVNRevision revision = getRevision();
        if(revision == null) {
            // XXX should not be even possible to get here!
        }
        
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i].trim();
            String repositoryUrlString = getRepositoryUrl().toString();
            if(path.startsWith("file://") ||
               path.startsWith("http://") ||
               path.startsWith("https://") ||
               path.startsWith("svn://") ||
               path.startsWith("svn+ssh://")) { // XXX already listed at some another place
                // must be a complete URL 
                // so check if it matches with the given repository URL
                if(path.startsWith(repositoryUrlString)) {
                    // lets take only the part without the repository base URL
                    ret[i] = new RepositoryFile(repositoryUrl, path.substring(repositoryUrlString.length()), revision);
                } else {
                    // XXX some kind of error msg
                    return EMPTY_REPOSITORY_FILES;
                }
            } else {
                ret[i] = new RepositoryFile(repositoryUrl, path, revision);    
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
                singleSelection);        
        
        final DialogDescriptor dialogDescriptor = 
                new DialogDescriptor(browser.getBrowserPanel(), "Repository browser - " + getRepositoryUrl().toString()); // XXX
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
            browser.reset(); // XXX
        }
    }      
    
    private SVNRevision getRevision() throws NumberFormatException {
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
    
}
