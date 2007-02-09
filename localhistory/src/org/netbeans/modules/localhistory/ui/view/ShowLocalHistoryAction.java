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
package org.netbeans.modules.localhistory.ui.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.localhistory.ui.view.LocalHistoryDiffView;
import java.io.File;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.modules.localhistory.ui.view.LocalHistoryTopComponent;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Tomas Stupka
 * // XXX context
 */
public class ShowLocalHistoryAction extends NodeAction {
    
    /** Creates a new instance of ShowLocalHistoryAction */
    public ShowLocalHistoryAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    protected void performAction(final Node[] activatedNodes) {                        
        VCSContext ctx = VCSContext.forNodes(activatedNodes);
        final Set<File> rootSet = ctx.getRootFiles();                    

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final LocalHistoryTopComponent tc = new LocalHistoryTopComponent();
                tc.setName(NbBundle.getMessage(this.getClass(), "CTL_LocalHistoryTopComponent", activatedNodes[0].getDisplayName()));
                tc.open();
                tc.requestActive();                                

                File[] files = rootSet.toArray(new File[rootSet.size()]);
                if(files[0].isFile()) {
                    // XXX hm 
                    LocalHistoryFileView fileView = new LocalHistoryFileView(files);                
                    LocalHistoryDiffView diffView = new LocalHistoryDiffView(); 
                    fileView.getExplorerManager().addPropertyChangeListener(diffView); 
                    fileView.getExplorerManager().addPropertyChangeListener(new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            if(ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {                            
                                tc.setActivatedNodes((Node[]) evt.getNewValue());  
                            }
                        } 
                    });
                    tc.init(diffView.getPanel(), fileView);
                } 
            }
        });

    }
    
    protected boolean enable(Node[] activatedNodes) {     
        if(activatedNodes == null || activatedNodes.length != 1) {
            return false;
        }
        VCSContext ctx = VCSContext.forNodes(activatedNodes);
        Set<File> rootSet = ctx.getRootFiles();        
        if(rootSet == null) { 
            return false;
        }                        
        for (File file : rootSet) {            
            if(file != null && !file.isFile()) {

                return false;
            }

        }        
        return true;   
        
//        boolean files = false;
//        boolean folders = false;
//        for (File file : rootSet) {            
//            if(file.isFile()) {
//                if(folders) {
//                    return false;
//                }
//                files = true;
//            }
//            if(!file.isFile()) {
//                if(files) {
//                    return false;
//                }
//                folders = true;
//            }                        
//        }        
//        return files && !folders || !files && folders;                
    }
    
    public String getName() {
        return NbBundle.getMessage(this.getClass(), "CTL_ShowLocalHistory");        
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ShowLocalHistoryAction.class);
    }
    
}
