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

import java.net.MalformedURLException;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.ui.browser.BrowserAction;
import org.netbeans.modules.subversion.ui.browser.CreateFolderAction;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;

/**
 *
 * @author Tomas Stupka
 */
public class CreateCopy extends CopyDialog {

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
                panel.browseRepositoryButton
            );
        repositoryPaths.setupBrowserBehavior(true, false, new BrowserAction[] { new CreateFolderAction(context)} );                
        
        setupUrlComboBox(panel.urlComboBox, CreateCopy.class.getName());
        panel.messageTextArea.getDocument().addDocumentListener(this);
    }    
    
    protected void validateUserInput() {        
        if(!validateRepositoryPath(repositoryPaths)) {
            return;
        }
                
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
            ex.printStackTrace(); // should not happen
        }
        return null;
    }

    String getMessage() {
        return getCreateCopyPanel().messageTextArea.getText();
    }
}
