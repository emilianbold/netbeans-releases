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
package org.netbeans.modules.subversion.ui.copy;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.MalformedURLException;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.ui.browser.Browser;
import org.netbeans.modules.subversion.ui.browser.BrowserAction;
import org.netbeans.modules.subversion.ui.browser.CreateFolderAction;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class CreateCopy extends CopyDialog implements DocumentListener, FocusListener {
   
    private CopyRepositoryPaths repositoryPaths;
    private final File localeFile;
    
    /** Creates a new instance of CreateCopy */
    public CreateCopy(RepositoryFile repositoryRoot, File localeFile, boolean localChanges) {        
        super(new CreateCopyPanel(), NbBundle.getMessage(CreateCopy.class, "CTL_CopyDialog_Prompt", localeFile.getName()), NbBundle.getMessage(CreateCopy.class, "CTL_CopyDialog_Title")); // NOI18N
        
        this.localeFile = localeFile;        
        final CreateCopyPanel panel = getCreateCopyPanel();        
        
        panel.warningLabel.setVisible(localChanges);                              
        panel.copyFromTextField.setText(localeFile.getAbsolutePath());                              
        panel.switchToWarningLabel.setVisible(false);
        if(localChanges) {
            panel.switchToCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent arg0) {
                    panel.switchToWarningLabel.setVisible(panel.switchToCheckBox.isSelected());
                }
            });
        }
        if(localeFile.isFile()) {
            panel.copyFromLabel.setText(org.openide.util.NbBundle.getMessage(CreateCopy.class, "CTL_CopyForm_fromFile"));               // NOI18N
            panel.copyToLabel.setText(org.openide.util.NbBundle.getMessage(CreateCopy.class, "CTL_CopyForm_toFile"));                   // NOI18N
        } else {
            panel.copyFromLabel.setText(org.openide.util.NbBundle.getMessage(CreateCopy.class, "CTL_CopyForm_fromFolder"));             // NOI18N
            panel.copyToLabel.setText(org.openide.util.NbBundle.getMessage(CreateCopy.class, "CTL_CopyForm_toFolder"));                 // NOI18N
        }        
        
        panel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CreateCopy.class, "CTL_CopyDialog_Title"));        
        
        repositoryPaths = 
            new CopyRepositoryPaths(
                repositoryRoot, 
                (JTextComponent) panel.urlComboBox.getEditor().getEditorComponent(),
                panel.browseRepositoryButton,
                null,
                null
            );
                
        String browserPurposeMessage = "";
        if(localeFile.isFile()) {
            browserPurposeMessage = org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_BrowserMessageCopyFile");    
        } else {
            browserPurposeMessage = org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_BrowserMessageCopyFolder");
        }

        String defaultFolderName = localeFile.isFile() ? "" : localeFile.getName();
        int browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY;
        repositoryPaths.setupBrowserBehavior(browserPurposeMessage, browserMode, new BrowserAction[] { new CreateFolderAction(defaultFolderName)} );                
        repositoryPaths.addPropertyChangeListener(this);

        setupUrlComboBox(panel.urlComboBox, CreateCopy.class.getName());
        
        //repositoryPaths.setRepositoryTextField(defaultFolderName);
                
        panel.messageTextArea.getDocument().addDocumentListener(this);
    }       
    
    protected void validateUserInput() {                        
        String text = getCreateCopyPanel().messageTextArea.getText();
        if (text == null || text.length() == 0) {            
            getOKButton().setEnabled(false);        
            return;
        }        
        try {
            RepositoryFile rf[] = repositoryPaths.getRepositoryFiles();
            if(rf == null || rf.length == 0) {
                getOKButton().setEnabled(false);        
                return;
            }
        } catch (NumberFormatException ex) {
            getOKButton().setEnabled(false);        
            return;
        } catch (MalformedURLException ex) {
            getOKButton().setEnabled(false);        
            return;
        }        
        getOKButton().setEnabled(true);              
    }    

    CreateCopyPanel getCreateCopyPanel() {
        return (CreateCopyPanel) getPanel();
    }
    
    RepositoryFile getRepositoryFile() {
        try {
            return repositoryPaths.getRepositoryFiles()[0];
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
        }
        return null;
    }

    String getMessage() {
        return getCreateCopyPanel().messageTextArea.getText();
    }

    boolean getSwitchTo() {
        return getCreateCopyPanel().switchToCheckBox.isSelected();
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

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        validateUserInput();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if( evt.getPropertyName().equals(RepositoryPaths.PROP_VALID) ) {
            boolean valid = ((Boolean)evt.getNewValue()).booleanValue();
            if(valid) {
                // it's a bit more we have to validate
                validateUserInput();
            } else {
                getOKButton().setEnabled(valid);
            }            
        }        
    }
    
    private class CopyRepositoryPaths extends RepositoryPaths  {
        public CopyRepositoryPaths(RepositoryFile repositoryFile, 
                                   JTextComponent repositoryPathTextField,  
                                   JButton browseButton, 
                                   JTextField revisionTextField, 
                                   JButton searchRevisionButton) 
        {        
            super(repositoryFile, repositoryPathTextField, browseButton, revisionTextField, searchRevisionButton);
        }

        protected void setRepositoryTextField(String url) {
            super.setRepositoryTextField(url + "/" + localeFile.getName());                     // NOI18N
        }

        protected void setRevisionTextField(String revision) {
            super.setRevisionTextField(revision);
        }        

        protected String getRepositoryTextField() {
            String url = super.getRepositoryTextField();                     
            int idx = url.lastIndexOf("/");                                                     // NOI18N
            if(idx > 0) {
                // skip the last segment - it's the destination folder/file
                return url.substring(0, idx - 1);
            } else {
                return "";                                                                      // NOI18N    
            }
        }
        
    }
}
