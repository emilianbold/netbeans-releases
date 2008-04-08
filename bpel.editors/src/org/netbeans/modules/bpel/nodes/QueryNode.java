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
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Query;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class QueryNode extends BpelNode<Query> {
    
    public QueryNode(Query reference, Lookup lookup) {
        super(reference, lookup);
    }

    public QueryNode(Query reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }

    public NodeType getNodeType() {
        return NodeType.PARTNER_ROLE;
    }

    protected String getNameImpl() {
        String name = null;
        Query ref = getReference();
        if (ref != null) {
            name = ref.getContent();
            if (name != null && name.length() > MAX_CONTENT_NAME_LENGTH) {
                name = name.substring(0, MAX_CONTENT_NAME_LENGTH);
            }
        }
        
        return name == null ? "" : name; // NOI18N
    }
    
}
