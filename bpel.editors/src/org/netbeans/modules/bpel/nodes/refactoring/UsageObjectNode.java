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

import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.openide.nodes.Children;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class UsageObjectNode extends UsageFilterNode {
    private BpelNode originalNode;
    public UsageObjectNode(BpelNode originalNode) {
        super(originalNode, Children.LEAF);
        this.originalNode = originalNode;
        disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME
                | DELEGATE_GET_NAME | DELEGATE_SET_NAME 
                | DELEGATE_GET_SHORT_DESCRIPTION | DELEGATE_SET_SHORT_DESCRIPTION);
    }

    public String getName() {
        String usageObjectName = null;
        NodeType nodeType = originalNode.getNodeType();
        usageObjectName = Util.getNodeName(originalNode);
 
        return usageObjectName == null ? "" : usageObjectName; // NOI18N
    }

    public String getDisplayName() {
        return originalNode.getDisplayName();
    }
    
    public boolean canRename() {
        return false;
    }
    
    public String getShortDescription() {
        return "";// NOI18N
    }
}
