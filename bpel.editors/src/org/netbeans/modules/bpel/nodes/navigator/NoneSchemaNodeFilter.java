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


package org.netbeans.modules.bpel.nodes.navigator;

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.properties.editors.controls.filter.NodeChildFilter;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.openide.nodes.Node;

/**
 *
 * @author
 */
public class NoneSchemaNodeFilter implements NodeChildFilter {

    public NoneSchemaNodeFilter() {
    }

    public boolean isPairAllowed(Node parentNode, Node childNode) {
        // the parentNode doesn't matter here!
        //
        return !(childNode instanceof  BpelNode &&
            ((BpelNode)childNode).getNodeType().equals(NodeType.SCHEMA_ELEMENT));
//    if (childNode instanceof  BpelNode && ((BpelNode)childNode).getNodeType().
//        equals(NodeType.SCHEMA_ELEMENT) )
//    {
////            Log.out("%%%%%%%%%%%%%%%%%%%%%%%%%%%% NoneSchemaNodeFilter: node: false; type"+(((BpelNode)childNode).getNodeType()));
//      return false;
//    } else {
////            Log.out("%%%%%%%%%%%%%%%%%%%%%%%%%%%% NoneSchemaNodeFilter: node: true;");
//      return true;
//    }
    }
    
}

