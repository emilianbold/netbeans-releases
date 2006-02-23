/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A showDialog of the License is available at
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
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;

/**
 *
 * @author Tomas Stupka
 */
public abstract class CopyDialog implements DocumentListener, FocusListener {

    private DialogDescriptor dialogDescriptor;
    private JButton okButton;
    private JPanel panel;
    
    public CopyDialog(JPanel panel, String title, String okLabel) {                
        this.panel = panel;
        dialogDescriptor = new DialogDescriptor(panel, title); // XXX
        
        okButton = new JButton(okLabel);
        dialogDescriptor.setOptions(new Object[] {okButton, "Canel"});
        
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
        dialogDescriptor.setValid(false);                                                
    }
    
    protected JPanel getPanel() {
        return panel;
    }
        
    protected boolean validateRepositoryPath(RepositoryPaths paths) {
        RepositoryFile[] files;
        try {
            files = paths.getRepositoryFiles();
            if(files == null || files.length == 0) {
                getOKButton().setEnabled(false);        
                return false;
            }
        } catch (NumberFormatException ex) {
            getOKButton().setEnabled(false);        
            return false;
        } catch (MalformedURLException ex) {
            getOKButton().setEnabled(false);        
            return false;
        }            
        return true;
    }

    public boolean showDialog() {                        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.setVisible(true);
        return dialogDescriptor.getValue()==okButton;       
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

    protected abstract void validateUserInput();   
    
    protected void registerDocument(Document doc) {
        doc.addDocumentListener(this);    
    }   

    protected JButton getOKButton() {
        return okButton;
    }
}
