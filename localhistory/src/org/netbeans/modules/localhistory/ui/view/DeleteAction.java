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

import org.netbeans.modules.localhistory.LocalHistory;
import org.netbeans.modules.localhistory.store.StoreEntry;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Tomas Stupka
 */
public class DeleteAction extends NodeAction {
    

    protected void performAction(final Node[] activatedNodes) {
        // XXX progress support ???
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() { 
                for(Node node : activatedNodes) {
                    StoreEntry se =  node.getLookup().lookup(StoreEntry.class);
                    LocalHistory.getInstance().getLocalHistoryStore().deleteEntry(se.getFile(), se.getTimestamp());
                }
            }
        });
        // XXX refresh view         
    }

    protected boolean enable(Node[] activatedNodes) {
        if(activatedNodes == null || activatedNodes.length < 1) {
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
        return NbBundle.getMessage(DeleteAction.class, "LBL_DeleteAction");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(DeleteAction.class);
    }    
    
}
