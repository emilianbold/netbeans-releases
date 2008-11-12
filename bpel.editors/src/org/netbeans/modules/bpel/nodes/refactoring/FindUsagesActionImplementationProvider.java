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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.nodes.refactoring;

import java.util.Collection;
import org.netbeans.modules.bpel.nodes.actions.FindUsagesAction;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider.class)
public class FindUsagesActionImplementationProvider extends ActionsImplementationProvider {
    
    private FindUsagesAction findUsagesAction;
    
    /** Creates a new instance of FindUsagesActionImplementationProvider */
    public FindUsagesActionImplementationProvider() {
        findUsagesAction = SystemAction.get(FindUsagesAction.class);
    }
    
    @Override
    public boolean canFindUsages(Lookup lookup) {
        Node[] nodes = getNodes(lookup);
        
        return nodes != null && findUsagesAction.enable(nodes);//super.canFindUsages(lookup);
    }

    @Override
    public void doFindUsages(Lookup lookup) {
        //super.doFindUsages(lookup);
        Node[] nodes = getNodes(lookup);
        
        if (nodes != null) {
            findUsagesAction.performAction(nodes);
        }
    }
    
    private Node[] getNodes(Lookup lookup) {
        Node[] nodes = null;
        
        Collection<? extends Node> nodesCollection = null;
        if (lookup != null) {
            nodesCollection = lookup.lookupAll(Node.class);
        }
        
        if (nodesCollection != null) {
            nodes = nodesCollection.toArray(new Node[nodesCollection.size()]);
        }

        return nodes;
    }
    
}
