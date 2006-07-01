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
import java.beans.PropertyChangeEvent;
import java.net.MalformedURLException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.subversion.RepositoryFile;
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
   
    private RepositoryPaths repositoryPaths;
    
    /** Creates a new instance of CreateCopy */
    public CreateCopy(RepositoryFile repositoryRoot, String context, boolean localChanges) {
        super(new CreateCopyPanel(), NbBundle.getMessage(CreateCopy.class, "CTL_CopyDialog_Prompt", context), NbBundle.getMessage(CreateCopy.class, "CTL_CopyDialog_Title")); // NOI18N
        CreateCopyPanel panel = getCreateCopyPanel();
        panel.warningLabel.setVisible(localChanges);                              
        panel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CreateCopy.class, "CTL_CopyDialog_Title"));        
        repositoryPaths = 
            new RepositoryPaths(
                repositoryRoot, 
                (JTextComponent) panel.urlComboBox.getEditor().getEditorComponent(),
                panel.browseRepositoryButton,
                null,
                null
            );
        repositoryPaths.setupBrowserBehavior(true, false, false, new BrowserAction[] { new CreateFolderAction(context)} );                
        repositoryPaths.addPropertyChangeListener(this);

        setupUrlComboBox(panel.urlComboBox, CreateCopy.class.getName());
        panel.messageTextArea.getDocument().addDocumentListener(this);
    }    
    
    protected void validateUserInput() {                        
        String text = getCreateCopyPanel().messageTextArea.getText();
        if (text == null || text.length() == 0) {            
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
}
