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
package org.netbeans.modules.bpel.properties.editors.controls;

import javax.swing.Action;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

/**
 * Provides a preferred action for the any nodes and their children.
 *
 * Be aware that all nodes of BpelNode type use another approach to gain the aim.
 * See the {@link PreferredActionProvider} interface.
 *
 * @author nk160297
 */

public class ActionProxyNode extends FilterNode {

    private static NodeAction myAction;

    public ActionProxyNode(Node original, NodeAction preferredAction) {
        super(original, new ProxyChildren(original));
        myAction = preferredAction;
    }

    public Action getPreferredAction() {
        return myAction;
    }
    
    /* 
     * This class is necessary for the ClassRulesFilter
     */ 
    public Class getOriginalNodeClass() {
        Node original = getOriginal();
        return original == null ? null : original.getClass();
    }
    
    private static class ProxyChildren extends FilterNode.Children {
        
        public ProxyChildren(Node owner) {
            super(owner);
        }
        
        protected Node copyNode(Node original) {
            return new ActionProxyNode(original, myAction);
        }
    }
}
