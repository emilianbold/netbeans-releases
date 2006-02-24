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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.settings.HistorySettings;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;

/**
 *
 * @author Tomas Stupka
 */
public class Merge extends CopyDialog implements ActionListener {
    
    private RepositoryPaths mergeFromRepositoryPaths;
    private RepositoryPaths mergeAfterRepositoryPaths;
    
    private String MERGE_FROM_URL_HISTORY_KEY = Merge.class.getName() + "_merge_from";
    private String MERGE_AFTER_URL_HISTORY_KEY = Merge.class.getName() + "_merge_after";
    
    public Merge(RepositoryFile repositoryRoot, String context) {
        super(new MergePanel(), "Merge " + context + " to...", "Merge");

        MergePanel panel = getMergePanel();        
        
        mergeFromRepositoryPaths = 
            new RepositoryPaths(
                repositoryRoot, 
                (JTextComponent) panel.mergeFromUrlComboBox.getEditor().getEditorComponent(),
                panel.mergeFromBrowseRepositoryButton,
                panel.mergeFromRevisionTextField,
                panel.mergeFromSearchRevisionButton
            );
        mergeFromRepositoryPaths.setupBrowserBehavior(true, false);                        

        mergeAfterRepositoryPaths = 
            new RepositoryPaths(
                repositoryRoot, 
                (JTextComponent) panel.mergeAfterUrlComboBox.getEditor().getEditorComponent(),
                panel.mergeAfterBrowseRepositoryButton,
                panel.mergeAfterRevisionTextField,
                panel.mergeAfterSearchRevisionButton
            );
        mergeAfterRepositoryPaths.setupBrowserBehavior(true, false);                                        
        
        setupUrlComboBox(panel.mergeFromUrlComboBox,MERGE_FROM_URL_HISTORY_KEY);        
        setupUrlComboBox(panel.mergeAfterUrlComboBox, MERGE_AFTER_URL_HISTORY_KEY);        
        panel.mergeFromRevisionTextField.getDocument().addDocumentListener(this);                                
        panel.mergeAfterRevisionTextField.getDocument().addDocumentListener(this);
        
        panel.onlyMadeAfterCheckBox.addActionListener(this);
        setMergeAfterEnabled(false);
        
    }        
    
    protected void validateUserInput() {
        // XXX error message               
        if(mergeFromRepositoryPaths!= null && !validateRepositoryPath(mergeFromRepositoryPaths)) {
            return;            
        }
        
        if(getMergePanel().onlyMadeAfterCheckBox.isSelected() &&                 
           mergeAfterRepositoryPaths != null &&     
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
