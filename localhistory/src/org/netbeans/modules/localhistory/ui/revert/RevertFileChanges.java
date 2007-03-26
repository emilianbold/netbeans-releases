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
package org.netbeans.modules.localhistory.ui.revert;

import java.awt.Dialog;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.netbeans.modules.localhistory.LocalHistorySettings;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.netbeans.modules.localhistory.ui.view.LocalHistoryFileView;
import org.netbeans.modules.localhistory.utils.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class RevertFileChanges implements PropertyChangeListener {
           
    private LocalHistoryFileView view;
    private DialogDescriptor dialogDescriptor;
    private JButton okButton;
    
    RevertFileChanges () {
        view = new LocalHistoryFileView();                                
        view.getPanel().setPreferredSize(new Dimension(550, 250));        
        
        okButton = new JButton(NbBundle.getMessage(this.getClass(), "CTL_Revert"));// XXX
        okButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(this.getClass(), "CTL_Revert"));// XXX
        JButton cancelButton = new JButton(NbBundle.getMessage(this.getClass(), "CTL_Cancel"));// XXX
        cancelButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(this.getClass(), "CTL_Cancel"));    // XXX
        
        dialogDescriptor = new DialogDescriptor (view.getPanel(), NbBundle.getMessage(this.getClass(), "LBL_RevertToAction")); // XXX
        dialogDescriptor.setOptions(new Object[] {okButton, cancelButton});                         
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));        
        
        view.getExplorerManager().addPropertyChangeListener(this);
    }        
    
    void show(File root) {                                
        long ts = LocalHistorySettings.getInstance().getLastSelectedEntry(root);        
        view.refresh(new File[] {root}, ts);                
        if(show()) {            
            StoreEntry[] entries = view.getSelectedEntries();
            if(entries != null && entries.length > 0) {
                revert(entries[0]); 
                LocalHistorySettings.getInstance().setLastSelectedEntry(root, entries[0].getTimestamp());    
            }            
        }        
    }

    protected boolean show() {                
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        dialog.getAccessibleContext().setAccessibleDescription("LBL_RevertToAction"); // XXX
        dialog.setVisible(true);                
                        
        return dialogDescriptor.getValue() == okButton;
    }        
    
    private void revert(final StoreEntry entry) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {                 
                Utils.revert(entry);   
            }
        });
    }        

    public void propertyChange(PropertyChangeEvent evt) {  
        if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {  
            Node[] nodes = (Node[]) evt.getNewValue();
            okButton.setEnabled(isEnabled(nodes));        
        }
    }   
    
    private boolean isEnabled(Node[] nodes) {
        if(nodes == null || nodes.length != 1) {
            return false;
        }        
        for(Node node : nodes) {
            StoreEntry se =  node.getLookup().lookup(StoreEntry.class);
            if(se == null) {
                return false;
            }            
        }
        return true; 
    }      
 
}    
