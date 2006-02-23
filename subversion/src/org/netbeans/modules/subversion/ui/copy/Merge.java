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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.MalformedURLException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
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
 * @author Tomas Stupka
 */
public class Merge extends CopyDialog implements ActionListener {
    
    private RepositoryPaths mergeFromRepositoryPaths;
    private RepositoryPaths mergeAfterRepositoryPaths;
    
    public Merge(RepositoryFile repositoryRoot, String context) {
        super(new MergePanel(), "Merge " + context + " to...", "Merge");

        MergePanel panel = getMergePanel();
        JTextComponent mergeFromUrlEditor = ((JTextComponent) panel.mergeFromUrlComboBox.getEditor().getEditorComponent());
        JTextComponent mergeAfterUrlEditor = ((JTextComponent) panel.mergeAfterUrlComboBox.getEditor().getEditorComponent());
        registerDocument( mergeFromUrlEditor.getDocument() );        
        registerDocument( panel.mergeFromRevisionTextField.getDocument() );        
        registerDocument( mergeAfterUrlEditor.getDocument() );        
        registerDocument( panel.mergeAfterRevisionTextField.getDocument() );        
        
        panel.onlyMadeAfterCheckBox.addActionListener(this);
        
        mergeFromRepositoryPaths = 
            new RepositoryPaths(
                repositoryRoot, 
                mergeFromUrlEditor,
                panel.mergeFromBrowseRepositoryButton,
                panel.mergeFromRevisionTextField,
                panel.mergeFromSearchRevisionButton
            );
        mergeFromRepositoryPaths.setupBrowserBehavior(true, false);                        

        mergeAfterRepositoryPaths = 
            new RepositoryPaths(
                repositoryRoot, 
                mergeAfterUrlEditor,
                panel.mergeAfterBrowseRepositoryButton,
                panel.mergeAfterRevisionTextField,
                panel.mergeAfterSearchRevisionButton
            );
        mergeAfterRepositoryPaths.setupBrowserBehavior(true, false);                        
        
    }        

    protected void validateUserInput() {
        // XXX error message               
        if(!validateRepositoryPath(mergeFromRepositoryPaths)) {
            return;            
        }
        
        if(getMergePanel().onlyMadeAfterCheckBox.isSelected() && 
           !validateRepositoryPath(mergeAfterRepositoryPaths)) 
        {    
            return;            
        }
        
        getOKButton().setEnabled(true);
    }    

    
    
    RepositoryFile getMergeFromRepositoryFile() {        
        try {
            return mergeFromRepositoryPaths.getRepositoryFiles()[0];
        } catch (MalformedURLException ex) {
            // should be already checked and 
            // not happen at this place anymore
            ex.printStackTrace();             
        }
        return null;
    }    

    RepositoryFile getMergeAfterRepositoryFile() {        
        try {
            return mergeAfterRepositoryPaths.getRepositoryFiles()[0];
        } catch (MalformedURLException ex) {
            // should be already checked and 
            // not happen at this place anymore
            ex.printStackTrace();             
        }
        return null;
    }    
    
    boolean madeAfter() {
        return getMergePanel().onlyMadeAfterCheckBox.isSelected();
    }

    private MergePanel getMergePanel() {
        return (MergePanel) getPanel();
    }

    public void actionPerformed(ActionEvent e) {
        JCheckBox chk = getMergePanel().onlyMadeAfterCheckBox;
        if(e.getSource() == chk) {
            setMergeAfterEnabled(chk.isSelected());
        }
    }
    
    private void setMergeAfterEnabled(boolean bl) {
        MergePanel panel = getMergePanel();
        panel.mergeAfterUrlComboBox.setEnabled(bl);        
        panel.mergeAfterBrowseRepositoryButton.setEnabled(bl);
        panel.mergeAfterRevisionTextField.setEnabled(bl);
        panel.mergeAfterSearchRevisionButton.setEnabled(bl);        
        panel.mergeAfterEmptyLabel.setEnabled(bl);        
        panel.mergeAfterRevisionLabel.setEnabled(bl);        
        panel.mergeAfterRepositoryFolderLabel.setEnabled(bl);        
        
    }
}
