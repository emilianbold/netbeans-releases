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
import org.netbeans.modules.localhistory.ui.actions.*;
import org.netbeans.modules.localhistory.ui.revert.*;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.netbeans.modules.localhistory.utils.Utils;
import org.openide.LifecycleManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Tomas Stupka
 */
public class RevertFileAction extends NodeAction {

    public RevertFileAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }   
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }  
    
    protected void performAction(final Node[] activatedNodes) {      
       // XXX try to save files in invocation context only
        // list somehow modified file in the context and save
        // just them.
        // The same (global save) logic is in CVS, no complaint
        LifecycleManager.getDefault().saveAll();  
        
        // XXX progress support ???
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {                 
                Utils.revert(activatedNodes);
            }
        });
        // XXX refresh view        
    }
            
    protected boolean enable(Node[] activatedNodes) {
        if(activatedNodes == null || activatedNodes.length != 1) {
            return false;
        }        
        for(Node node : activatedNodes) {
            StoreEntry se =  node.getLookup().lookup(StoreEntry.class);
            if(se == null) {
                return false;
            }            
        }
        return true; 
    }

    public String getName() {
        return NbBundle.getMessage(RevertFileAction.class, "LBL_RevertFileAction");
    }      
}
