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

import java.awt.Dialog;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.MalformedURLException;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.ui.browser.BrowserAction;
import org.netbeans.modules.subversion.ui.browser.CreateFolderAction;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * // XXX could be a descriptor subclass
 * @author Tomas Stupka
 */
public class SwitchTo implements DocumentListener, FocusListener {

    private RepositoryFile repositoryRoot;
    private String context;
    private Object value;
    private SwitchToPanel panel;
    private DialogDescriptor dialogDescriptor;
    private JButton copyButton;
    private RepositoryPaths repositoryPaths;
    
    /** Creates a new instance of CreateCopy */
    public SwitchTo(RepositoryFile repositoryRoot, String context) {
        this.repositoryRoot = repositoryRoot;
        this.context = context;
        
        panel = new SwitchToPanel();           
        
        ((JTextField) panel.urlComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        
        dialogDescriptor = new DialogDescriptor(panel, "Switch to..."); // XXX
        
        copyButton = new JButton("Switch");
        dialogDescriptor.setOptions(new Object[] {copyButton, "Cancel"});
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(CreateCopyAction.class));
        dialogDescriptor.setValid(false);                                
        
        repositoryPaths = 
            new RepositoryPaths(
                repositoryRoot, 
                (JTextField) panel.urlComboBox.getEditor().getEditorComponent(),
                panel.browseRepositoryButton,
                panel.revisionTextField,
                panel.searchRevisionButton
            );
        repositoryPaths.setupBrowserBehavior(true, false, new BrowserAction[] { new CreateFolderAction(context)} );                        
    }
    
    public boolean switchTo() {                        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.setVisible(true);
        return dialogDescriptor.getValue()==copyButton;       
    }        

    public void insertUpdate(DocumentEvent e) {
        validateUserInput();        
    }

    public void removeUpdate(DocumentEvent e) {
        validateUserInput();        
    }

    public void changedUpdate(DocumentEvent e) {        
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        validateUserInput();
    }

    private boolean validateUserInput() {
        // XXX error message
        
//        String text = ((JTextField)panel.urlComboBox.getEditor().getEditorComponent()).getText();
//        if (text == null || text.length() == 0) {            
//            copyButton.setEnabled(false);
//            return false;
//        }
//        
//        try {
//            new SVNUrl(repositoryRoot.getRepositoryUrl().toString() + "/" + text);
//        } catch (MalformedURLException ex) {
//            copyButton.setEnabled(false);
//            return false;
//        }
        
        try {            
            repositoryPaths.getRepositoryFiles();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            copyButton.setEnabled(false);
            return false;
        }
                                
        try {
            repositoryPaths.getRevision();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            copyButton.setEnabled(false);
            return false;            
        }
        
        copyButton.setEnabled(true);
        return true;
    }    

    RepositoryFile getRepositoryFile() {        
        try {
            return repositoryPaths.getRepositoryFiles()[0];
        } catch (MalformedURLException ex) {
            // should be already checked and 
            // not happen at this place anymore
            ex.printStackTrace(); 
            
        }
        return null;
    }    

    SVNRevision getRevision() {
        return repositoryPaths.getRevision();
    }

    boolean replaceModifications() {
        return panel.replaceModifiedCheckBox.isSelected();
    }
}
