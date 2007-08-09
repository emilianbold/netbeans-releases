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
package org.netbeans.modules.subversion.ui.browser;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.ui.search.SvnSearch;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
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
    private int browserMode;
    private BrowserAction[] browserActions;
    private String browserPurpose;
    private String browserHelpID;
    private String searchHelpID; 
    
    private boolean valid = false;
    public static final String PROP_VALID = "valid"; // NOI18N
    private List<PropertyChangeListener> listeners;
    
    private PropertyChangeSupport propertyChangeSupport;
    
    public RepositoryPaths(RepositoryFile repositoryFile, 
                           JTextComponent repositoryPathTextField,  
                           JButton browseButton, 
                           JTextField revisionTextField, 
                           JButton searchRevisionButton) 
    {                
        assert repositoryFile != null;
        assert (repositoryPathTextField !=null && browseButton != null) ||
               (revisionTextField != null && searchRevisionButton != null);

        this.repositoryFile = repositoryFile;

        if(repositoryPathTextField!=null) {
            this.repositoryPathTextField = repositoryPathTextField;
            repositoryPathTextField.getDocument().addDocumentListener(this);
            this.browseButton = browseButton;
            if(browseButton != null) {
                browseButton.addActionListener(this);   
            }            
        }        

        if(revisionTextField!=null) {
            this.revisionTextField = revisionTextField;
            revisionTextField.getDocument().addDocumentListener(this);
            this.searchRevisionButton = searchRevisionButton;
            if(searchRevisionButton != null) {
                searchRevisionButton.addActionListener(this);   
            }            
        }                
    }

    public void setupBehavior(String browserPurpose, int browserMode, String browserHelpID, String searchHelpID) {
        this.browserMode    = browserMode;                
        this.browserPurpose = browserPurpose;
        this.browserHelpID  = browserHelpID;
        this.searchHelpID   = searchHelpID;
    }            
    
    public void setupBehavior(String browserPurpose, int browserMode, BrowserAction[] browserActions, String browserHelpID, String searchHelpID) {
        setupBehavior(browserPurpose, browserMode, browserHelpID, searchHelpID);
        this.browserActions = browserActions;
        this.browserPurpose = browserPurpose;
    }            
    
    public RepositoryFile[] getRepositoryFiles() throws MalformedURLException, NumberFormatException {
        return getRepositoryFiles(null);
    }
    
    public RepositoryFile[] getRepositoryFiles(String defaultPath) throws MalformedURLException, NumberFormatException {

        SVNRevision revision = getRevision();
        
        if(repositoryPathTextField==null) {
            RepositoryFile rf = new RepositoryFile(repositoryFile.getRepositoryUrl(), repositoryFile.getFileUrl(), revision);
            return new RepositoryFile[] {rf};
        }
     
        if(getRepositoryString().equals("")) { // NOI18N
            if(defaultPath != null && !defaultPath.trim().equals("") ) {                
                return new RepositoryFile[] { new RepositoryFile(getRepositoryUrl(), defaultPath, revision) } ;
            } else {
                return EMPTY_REPOSITORY_FILES;   
            }            
        }
        if(revision == null) {
            // should not be possible to get here!
            return EMPTY_REPOSITORY_FILES;
        }        
        String[] paths = getRepositoryString().split(","); // NOI18N
        RepositoryFile[] ret = new RepositoryFile[paths.length];
        SVNUrl repositoryUrl = getRepositoryUrl();
       
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i].trim();
            String repositoryUrlString = getRepositoryUrl().toString();
            if(path.startsWith("file://")  ||   // NOI18N
               path.startsWith("http://")  ||   // NOI18N
               path.startsWith("https://") ||   // NOI18N
               path.startsWith("svn://")   ||   // NOI18N
               path.startsWith("svn+ssh://")) { // NOI18N   // XXX check only for svn+ and concat the remaining part from the protocol
                // must be a complete URL 
                // so check if it matches with the given repository URL
                if(path.startsWith(repositoryUrlString)) {
                    // lets take only the part without the repository base URL
                    ret[i] = new RepositoryFile(repositoryUrl, path.substring(repositoryUrlString.length()), revision);
                } else {
                    throw new MalformedURLException(NbBundle.getMessage(RepositoryPaths.class, "MSG_RepositoryPath_WrongStart", path, repositoryUrlString)); // NOI18N
                }
            } else {                
                ret[i] = new RepositoryFile(repositoryUrl, path, revision);    
            }                
        }                                    
        return ret;
    }

    private void browseRepository() {
        SVNRevision revision = getRevision();
        RepositoryFile[] repositoryFilesToSelect;
        try {
            repositoryFilesToSelect = getRepositoryFiles();
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ex);
            return;
        }
        
        RepositoryFile repositoryFile = new RepositoryFile(getRepositoryUrl(), revision);
        Browser browser = new Browser(browserPurpose, browserMode, repositoryFile, repositoryFilesToSelect, browserActions, browserHelpID);                               

        // handle results    
        RepositoryFile[] selectedFiles = browser.getRepositoryFiles();

        if(selectedFiles.length > 0) {                
            StringBuffer paths = new StringBuffer();
            for (int i = 0; i < selectedFiles.length; i++) {
                paths.append(selectedFiles[i].getPath());
                if(i < selectedFiles.length - 1) {
                    paths.append(", "); // NOI18N
                }
            }  
            setRepositoryTextField(paths.toString());
        }             
    }          
    
    private void searchRepository() {
        SVNRevision revision = getRevision();     
        
        final SvnSearch svnSearch;
        try {
            RepositoryFile[] repositoryFiles = getRepositoryFiles();
            if(repositoryFiles.length > 0) {
                svnSearch = new SvnSearch(repositoryFiles); 
            } else {
                svnSearch = new SvnSearch(new RepositoryFile[] { repositoryFile }); 
            }                            
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ex);
            return;
        }
                               
        final DialogDescriptor dialogDescriptor = 
                new DialogDescriptor(svnSearch.getSearchPanel(), java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/browser/Bundle").getString("CTL_RepositoryPath_SearchRevisions")); 
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(searchHelpID));
        dialogDescriptor.setValid(false);
        
        svnSearch.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //if( ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()) ) {
                    dialogDescriptor.setValid(svnSearch.getSelectedRevision() != null);
               // }
            }
        });
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.setVisible(true);

        // handle results
        if (DialogDescriptor.OK_OPTION.equals(dialogDescriptor.getValue())) {       
            revision = svnSearch.getSelectedRevision();
            if(revision != null) {
                if(revision.equals(SVNRevision.HEAD) ) {
                    setRevisionTextField(""); // NOI18N
                } else {
                    setRevisionTextField(revision.toString());                       
                }
            }
        } else {
            svnSearch.cancel(); 
        }
    }
    
    public SVNRevision getRevision() {
        
        if (revisionTextField == null) {
            return SVNRevision.HEAD;
        }
        String revisionString = getRevisionString();

        if (revisionString.equals("")) {
            return SVNRevision.HEAD;
        }            
        return SvnUtils.getSVNRevision(revisionString);        
    }           
    
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == browseButton) {
            browseRepository();
        } else if (e.getSource() == searchRevisionButton) {
            searchRepository();
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
        boolean oldValue = this.valid;
        boolean valid = true;

        RepositoryFile[] files = null;
        try {
            files = getRepositoryFiles();
        } catch (NumberFormatException ex) {
            valid = false;
        } catch (MalformedURLException ex) {            
            valid = false;
        }
        
        if(browseButton != null) {
            browseButton.setEnabled(valid);
        }        
        if(searchRevisionButton != null) {
            searchRevisionButton.setEnabled(valid);
        }
        
        if(valid && !acceptEmptyUrl() && repositoryPathTextField != null && getRepositoryString().equals("")) { // NOI18N
            valid = false;
        }

        if(valid && !acceptEmptyRevision() && revisionTextField != null && getRevisionString().equals("")) { // NOI18N
            valid = false;
        }        
        
        this.valid = valid;
        fireValidPropertyChanged(oldValue, valid);

    }

    private void fireValidPropertyChanged(boolean oldValue, boolean valid) {
        getChangeSupport().firePropertyChange(new PropertyChangeEvent(this, PROP_VALID, new Boolean(oldValue), new Boolean(valid)));        
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        getChangeSupport().addPropertyChangeListener(l);        
        validateUserInput();
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        getChangeSupport().removePropertyChangeListener(l);
    }
    
    private PropertyChangeSupport getChangeSupport() {
        if(propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        return propertyChangeSupport;
    }

    protected boolean acceptEmptyUrl() {
        return false;
    }
        
    protected boolean acceptEmptyRevision() {
        return true;
    }

    public SVNUrl getRepositoryUrl() {
        return repositoryFile.getRepositoryUrl();
    }

    public void setRepositoryFile(RepositoryFile repositoryFile) {
        this.repositoryFile = repositoryFile;
    }        

    public void setRepositoryTextField(String url) {
        repositoryPathTextField.setText(url);
    }
    
    public void setRevisionTextField(String revision) {
        revisionTextField.setText(revision); 
    }
    
    protected String getRepositoryString() {
        return repositoryPathTextField.getText().trim();
    }
    
    protected String getRevisionString() {
        return revisionTextField.getText().trim();
    }    
}
