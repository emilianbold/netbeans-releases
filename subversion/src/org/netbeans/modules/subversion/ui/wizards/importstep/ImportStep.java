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

package org.netbeans.modules.subversion.ui.wizards.importstep;

import java.awt.event.FocusEvent;
import java.net.MalformedURLException;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.ui.wizards.AbstractStep;
import org.netbeans.modules.subversion.ui.browser.BrowserAction;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.ui.wizards.checkoutstep.WorkdirStep;
import org.openide.util.HelpCtx;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * rename me
 * @author Tomas Stupka
 */
public class ImportStep extends AbstractStep implements DocumentListener {
    
    private ImportPanel messagePanel;

    private RepositoryPaths repositoryPaths;
    
    public HelpCtx getHelp() {    
        return new HelpCtx(ImportStep.class);
    }    

    protected JComponent createComponent() {
        if (messagePanel == null) {
            messagePanel = new ImportPanel();            
            messagePanel.messageTextArea.getDocument().addDocumentListener(this);            
            messagePanel.repositoryPathTextField.getDocument().addDocumentListener(this);                       
        }         
        validateUserInput();        
        return messagePanel;              
    }

    protected void validateBeforeNext() {        
        validateUserInput();
    }   

    private boolean validateUserInput() {
        String text = messagePanel.repositoryPathTextField.getText();
        if (text == null || text.length() == 0) {
            // XXX can't see the msg 
            invalid(org.openide.util.NbBundle.getMessage(WorkdirStep.class, "BK2014"));
            return false;
        }        
        
        text = messagePanel.messageTextArea.getText().trim();
        boolean valid = text.length() > 0;
        if(valid) {
            valid();
        } else {
            invalid(null);                
        }
        return valid;
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

    public String getMessage() {
        return messagePanel.messageTextArea.getText();
    }

    public void setup(RepositoryFile repositoryFile, BrowserAction[] browserAction) {
        repositoryPaths = 
            new RepositoryPaths (
                repositoryFile,
                messagePanel.repositoryPathTextField,
                messagePanel.browseRepositoryButton
            );   
        repositoryPaths.setupBrowserBehavior(true, false, browserAction);
        messagePanel.repositoryPathTextField.setText(repositoryFile.getPath());        
    }

    public SVNUrl getRepositoryFolderUrl() {
        try {
            return repositoryPaths.getRepositoryFiles()[0].getFileUrl(); // more files doesn't make sence
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } 
        return null;
    }

    public boolean checkoutAfterImport() {
        return messagePanel.checkoutCheckBox.isSelected();
    }
}

