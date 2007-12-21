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

package org.netbeans.modules.bpel.nodes.children;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Else;
import org.netbeans.modules.bpel.model.api.ElseIf;
import org.netbeans.modules.bpel.model.api.If;
import org.netbeans.modules.bpel.nodes.navigator.NavigatorNodeFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 5 April 2006
 *
 */
public class IfChildren extends BpelNodeChildren<If> {
    
    public IfChildren(If entity, Lookup contextLookup) {
        super(entity, contextLookup);
    }
    
    public Collection getNodeKeys() {
        If ref = getReference();
        if (ref == null) {
            return Collections.EMPTY_LIST;
        }
        
        ArrayList<Object> childs = new ArrayList<Object>();
        
        // set then node
        Activity thenActivity = (Activity) ref.getActivity();
        if (thenActivity != null) {
            childs.add(NodeType.THEN);
        }
        
        // set elseif nodes
        ElseIf[] elseIfs = ref.getElseIfs();
        if (elseIfs != null && elseIfs.length > 0) {
            childs.addAll(Arrays.asList(elseIfs));
        }
        
        // set else node
        Else els = ref.getElse();
        if (els != null) {
            childs.add(els);
        }
        
        return childs;
    }

    protected Node[] createNodes(Object object) {
        if (object != null && object instanceof BpelEntity) {
            return super.createNodes(object);
        } 

        if (object.equals(NodeType.THEN)) {
            NavigatorNodeFactory factory = NavigatorNodeFactory.getInstance();
            Node childNode = factory.createNode(NodeType.THEN,getReference(),lookup);
            if (childNode != null) {
                return new Node[] {childNode};
            }
        }
        return new Node[0];
    }
}
