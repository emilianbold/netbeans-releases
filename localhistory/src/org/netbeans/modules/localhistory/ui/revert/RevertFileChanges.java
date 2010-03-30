/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.localhistory.ui.revert;

import java.awt.Dialog;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JButton;
import org.netbeans.modules.localhistory.LocalHistory;
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

/**
 *
 * @author Tomas Stupka
 */
public class RevertFileChanges implements PropertyChangeListener {
           
    private LocalHistoryFileView view;
    private DialogDescriptor dialogDescriptor;
    private JButton okButton;
    private Node[] selectedNodes;
    
    RevertFileChanges () {
        view = new LocalHistoryFileView();                                
        view.getPanel().setPreferredSize(new Dimension(550, 250));        
        
        okButton = new JButton(NbBundle.getMessage(this.getClass(), "CTL_Revert"));
        okButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(this.getClass(), "CTL_Revert"));
        JButton cancelButton = new JButton(NbBundle.getMessage(this.getClass(), "CTL_Cancel"));
        cancelButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(this.getClass(), "CTL_Cancel"));   
        
        dialogDescriptor = new DialogDescriptor (view.getPanel(), NbBundle.getMessage(this.getClass(), "LBL_RevertToDialog")); 
        dialogDescriptor.setOptions(new Object[] {okButton, cancelButton});                         
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));        
        
        view.getExplorerManager().addPropertyChangeListener(this);
    }        
    
    void show(File root) {                                
        long ts = LocalHistorySettings.getInstance().getLastSelectedEntry(root);        
        view.refresh(new File[] {root}, ts);                
        if(show()) {            
            StoreEntry[] entries = getSelectedEntries();
            if(entries != null && entries.length > 0) {
                revert(entries[0]); 
                LocalHistorySettings.getInstance().setLastSelectedEntry(root, entries[0].getTimestamp());    
            }            
        }        
    }

    protected boolean show() {                
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        dialog.getAccessibleContext().setAccessibleDescription("LBL_RevertToDialog"); 
        dialog.setVisible(true);                
                        
        return dialogDescriptor.getValue() == okButton;
    }        
    
    private void revert(final StoreEntry entry) {
        LocalHistory.getInstance().getParallelRequestProcessor().post(new Runnable() {
            public void run() {                 
                Utils.revert(entry);   
            }
        });
    }        

    public void propertyChange(PropertyChangeEvent evt) {  
        if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {  
            Node[] nodes = (Node[]) evt.getNewValue();
            if (nodes != null && nodes.length > 0) {
                selectedNodes = nodes;
            }
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

    private StoreEntry[] getSelectedEntries() {
        Node[] nodes = selectedNodes;
        if(nodes != null && nodes.length > 0) {
            ArrayList<StoreEntry> entries = new ArrayList<StoreEntry>();
            for(Node node : nodes) {
                entries.add(node.getLookup().lookup(StoreEntry.class));
            }
            return entries.toArray(new StoreEntry[entries.size()]);
        }
        return new StoreEntry[0];
    }
 
}    
