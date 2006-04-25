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

/**
 *
 * @author Tomas Stupka
 */
public class CreateCopy extends CopyDialog implements DocumentListener, FocusListener {

    private String context;
    private Object value;    
    private RepositoryPaths repositoryPaths;
    
    /** Creates a new instance of CreateCopy */
    public CreateCopy(RepositoryFile repositoryRoot, String context, boolean localChanges) {
        super(new CreateCopyPanel(), "Copy " + context + "to...", "Copy");        
        CreateCopyPanel panel = getCreateCopyPanel();
        panel.warningLabel.setVisible(localChanges);                              
                
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
