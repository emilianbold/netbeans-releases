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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
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
public class CreateCopy extends CopyDialog implements DocumentListener, FocusListener, ActionListener, PropertyChangeListener {

    private final RepositoryPaths copyToRepositoryPaths;
    private final RepositoryPaths copyFromRepositoryPaths;
    
    private final File localeFile;
    private final RepositoryFile repositoryFile;
    private final boolean localChanges;
    
    /** Creates a new instance of CreateCopy */
    public CreateCopy(RepositoryFile repositoryFile, File localeFile, boolean localChanges) {        
        super(new CreateCopyPanel(), NbBundle.getMessage(CreateCopy.class, "CTL_CopyDialog_Prompt", localeFile.getName()), NbBundle.getMessage(CreateCopy.class, "CTL_CopyDialog_Title")); // NOI18N
        
        this.localeFile = localeFile;        
        this.repositoryFile = repositoryFile;
        this.localChanges = localChanges;
        
        CreateCopyPanel panel = getCreateCopyPanel();                
                       
        panel.localRadioButton.addActionListener(this);        
        panel.remoteRadioButton.addActionListener(this);        
        panel.skipCheckBox.addActionListener(this);        

        panel.copyFromLocalTextField.setText(localeFile.getAbsolutePath());
        panel.copyFromRemoteTextField.setText(repositoryFile.getFileUrl().toString());        
                        
        copyFromRepositoryPaths = 
            new RepositoryPaths(
                repositoryFile, 
                panel.copyFromRemoteTextField,
                null,
                panel.copyFromRevisionTextField,
                panel.searchButton
            );
        
        if(localeFile.isFile()) {
            org.openide.awt.Mnemonics.setLocalizedText(panel.localRadioButton, org.openide.util.NbBundle.getMessage(CreateCopy.class, "CTL_CopyForm_fromLocalFile"));               // NOI18N                 
            org.openide.awt.Mnemonics.setLocalizedText(panel.remoteRadioButton, org.openide.util.NbBundle.getMessage(CreateCopy.class, "CTL_CopyForm_fromRemoteFile"));             // NOI18N            
            panel.skipCheckBox.setEnabled(false);
        } else {
            org.openide.awt.Mnemonics.setLocalizedText(panel.localRadioButton, org.openide.util.NbBundle.getMessage(CreateCopy.class, "CTL_CopyForm_fromLocalFolder"));             // NOI18N            
            org.openide.awt.Mnemonics.setLocalizedText(panel.remoteRadioButton, org.openide.util.NbBundle.getMessage(CreateCopy.class, "CTL_CopyForm_fromRemoteFolder"));           // NOI18N
        }        
        
        panel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CreateCopy.class, "CTL_CopyDialog_Title"));                   // NOI18N
        
        copyToRepositoryPaths = 
            new RepositoryPaths(
                repositoryFile, 
                (JTextComponent) panel.urlComboBox.getEditor().getEditorComponent(),
                panel.browseRepositoryButton,
                null,
                null
            );
                
        String browserPurposeMessage = "";
        if(localeFile.isFile()) {
            browserPurposeMessage = org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_BrowserMessageCopyFile");                       // NOI18N
        } else {
            browserPurposeMessage = org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_BrowserMessageCopyFolder");                     // NOI18N
        }

        String defaultFolderName = localeFile.isFile() ? "" : localeFile.getName();
        int browserMode = Browser.BROWSER_SINGLE_SELECTION_ONLY;
        copyToRepositoryPaths.setupBehavior(browserPurposeMessage, browserMode, new BrowserAction[] { new CreateFolderAction(defaultFolderName)} , Browser.BROWSER_HELP_ID_COPY, null);                
        copyToRepositoryPaths.addPropertyChangeListener(this);

        setupUrlComboBox(panel.urlComboBox, CreateCopy.class.getName());                        
        panel.messageTextArea.getDocument().addDocumentListener(this);
        ((JTextComponent) panel.urlComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        
        setFromLocal();
        validateUserInput();
    }       
    
    protected void validateUserInput() {                        
        String text = getCreateCopyPanel().messageTextArea.getText();    
        try {
            RepositoryFile rf[] = copyToRepositoryPaths.getRepositoryFiles();
            if(rf == null || rf.length == 0) {
                
                setErrorText(org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_MISSING_REPOSITORY_FOLDER"));
                
                getOKButton().setEnabled(false);        
                return;
            }
        } catch (NumberFormatException ex) {            
            setErrorText(ex.getLocalizedMessage()); // should not happen            
            getOKButton().setEnabled(false);        
            return;
        } catch (MalformedURLException ex) {            
            setErrorText(ex.getLocalizedMessage()); // should not happen           
            getOKButton().setEnabled(false);        
            return;
        }        
        if (text == null || text.length() == 0) {   
                        
            setErrorText(org.openide.util.NbBundle.getMessage(CreateCopy.class, "LBL_MISSING_COPY_MESSAGE"));
            
            getOKButton().setEnabled(false);        
            return;
        }    
        resetErrorText();
        getOKButton().setEnabled(true);              
    }    

    private void setErrorText(String txt) {
        CreateCopyPanel panel = getCreateCopyPanel();
        panel.invalidValuesLabel.setVisible(true);
        panel.invalidValuesLabel.setText(txt);
    }
    
    private void resetErrorText() {
        CreateCopyPanel panel = getCreateCopyPanel();        
        panel.invalidValuesLabel.setVisible(false);
        panel.invalidValuesLabel.setText("");        
    }
    
    CreateCopyPanel getCreateCopyPanel() {
        return (CreateCopyPanel) getPanel();
    }
    
    RepositoryFile getToRepositoryFile() {
        try {
            return getToRepositoryFileIntern();
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
        } catch (NumberFormatException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
        }
        return null;
    }

    private RepositoryFile getToRepositoryFileIntern() throws NumberFormatException, MalformedURLException {
        RepositoryFile[] toRepositoryFiles = copyToRepositoryPaths.getRepositoryFiles();
        if(toRepositoryFiles.length > 0) {
            RepositoryFile toRepositoryFile = toRepositoryFiles[0];
            if(skipContents()) {
                return toRepositoryFile;                
            } else {
                if(isLocal()) {
                    return toRepositoryFile.appendPath(localeFile.getName());   
                } else {
                    return toRepositoryFile.appendPath(repositoryFile.getFileUrl().getLastPathSegment());   
                }             
            }
        } else {
            return null;
        }
    }

    String getMessage() {
        return getCreateCopyPanel().messageTextArea.getText();
    }

    boolean isLocal() {
        return getCreateCopyPanel().localRadioButton.isSelected();        
    }
    
    File getLocalFile() {
        return localeFile;
    }
    
    RepositoryFile getFromRepositoryFile() {
        try {
            return copyFromRepositoryPaths.getRepositoryFiles()[0];
        }
        catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
        } catch (NumberFormatException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
        }
        return null;
    }
    
    boolean switchTo() {
        return getCreateCopyPanel().switchToCheckBox.isSelected();
    }

    boolean skipContents() {
        return getCreateCopyPanel().skipCheckBox.isSelected();
    }
    
    public void insertUpdate(DocumentEvent e) {
        validateUserInput();
        setPreview();
    }

    public void removeUpdate(DocumentEvent e) {
        validateUserInput();        
        setPreview();        
    }

    public void changedUpdate(DocumentEvent e) {
        validateUserInput();        
        setPreview();        
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        validateUserInput();        
        setPreview();        
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

    public void actionPerformed(ActionEvent evt) {
        if(evt.getSource() == getCreateCopyPanel().localRadioButton) {
            setFromLocal();        
            setPreview();        
        } else if(evt.getSource() == getCreateCopyPanel().remoteRadioButton) {
            selectFromRemote();
            setPreview();        
        } else if(evt.getSource() == getCreateCopyPanel().skipCheckBox) {
            setPreview();
        }        
    }
    
    private void setFromLocal() {
        CreateCopyPanel panel = getCreateCopyPanel();
        panel.copyFromLocalTextField.setEnabled(true);        
        panel.copyFromRemoteTextField.setEnabled(false);
        panel.warningLabel.setVisible(localChanges);                                              
    }

    private void selectFromRemote() {
        CreateCopyPanel panel = getCreateCopyPanel();
        panel.copyFromLocalTextField.setEnabled(false);        
        panel.copyFromRemoteTextField.setEnabled(true);
        panel.warningLabel.setVisible(false);                                              
    }

    private void setPreview() {
        try {
            RepositoryFile repositoryFile = getToRepositoryFileIntern();
            if(repositoryFile!=null) {
                getCreateCopyPanel().previewTextField.setText(repositoryFile.getFileUrl().toString());    
            } else {
                getCreateCopyPanel().previewTextField.setText("");              // NOI18N
            }
        }
        catch (NumberFormatException ex) {
            // wrong value -> we can't copy anything
            getCreateCopyPanel().previewTextField.setText("");                  // NOI18N
        } catch (MalformedURLException ex) {
            // wrong value -> we can't copy anything
            getCreateCopyPanel().previewTextField.setText("");                  // NOI18N
        };                
    }
    
}
