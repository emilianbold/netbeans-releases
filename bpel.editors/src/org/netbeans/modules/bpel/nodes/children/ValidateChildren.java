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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.Validate;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.nodes.navigator.NavigatorNodeFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 *
 */
public class ValidateChildren extends BpelNodeChildren<Validate> {
    
    public ValidateChildren(Validate entity, Lookup contextLookup) {
        super(entity, contextLookup);
    }
    
    public Collection getNodeKeys() {
        Validate ref = getReference();
        if (ref == null) {
            return Collections.EMPTY_LIST;
        }
        
        ArrayList<Object> childs = new ArrayList<Object>();
        
        // set variables ref nodes
        List<BpelReference<VariableDeclaration>> varRefs = ref.getVariables();
        if (varRefs != null) {
            childs.addAll(varRefs);
        }
        
        return childs;
    }

    protected Node[] createNodes(Object object) {
        if (object instanceof BpelReference && VariableDeclaration.class == ((BpelReference)object).getType()) {
            NavigatorNodeFactory factory = NavigatorNodeFactory.getInstance();
            Node childNode = factory.createNode(NodeType.VARIABLE_REFERENCE, object, lookup);
            if (childNode != null) {
                return new Node[] {childNode};
            }
        } 
        return new Node[0];
    }
}
