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
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Sequence;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.openide.nodes.Node;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class UsageContextNode extends UsageFilterNode {
    private Node originalNode;
    public UsageContextNode(Node originalNode) {
        super(originalNode, Children.LEAF);
        this.originalNode = originalNode;
        disableDelegation(DELEGATE_GET_DISPLAY_NAME | DELEGATE_SET_DISPLAY_NAME
                | DELEGATE_GET_NAME | DELEGATE_SET_NAME);
    }
    
    public String getHtmlDisplayName() {
        return getGraedContextName(getName());
    }
    
//    public String getName() {
//        Object ref = originalNode.getReference();
//        if (ref == null || !(ref instanceof BpelEntity)) {
//            return originalNode.getHtmlDisplayName();
//        }
//        return EditorUtil.getUsageContextPath((BpelEntity)ref, Sequence.class);
//    }
    
    public String getDisplayName() {
        return originalNode.getDisplayName();
    }
    
    // TODO r|m
    public String getName() {
//    public String getHtmlDisplayName() {
//        String  contextName = getName();
        if (!(originalNode  instanceof BpelNode)) {
            return originalNode.getHtmlDisplayName();
        }
        
        Object ref = ((BpelNode)originalNode).getReference();
        if (ref == null || !(ref instanceof BpelEntity)) {
            return originalNode.getHtmlDisplayName();
        }
        
        String contextName = null;
        NodeType nodeType = ((BpelNode)originalNode).getNodeType();
        switch (nodeType) {
            case VARIABLE_CONTAINER :
            case CORRELATION_SET_CONTAINER :
            case MESSAGE_EXCHANGE_CONTAINER :
                contextName = EditorUtil.getUsageContextPath(
                        ((BpelNode)originalNode).getHtmlDisplayName()
                        , (BpelEntity)ref
                        , Sequence.class);
                break;
            default :
                contextName =
                        EditorUtil.getUsageContextPath((BpelEntity)ref, Sequence.class);
        }
        
        if (contextName == null) {
            return originalNode.getHtmlDisplayName();
        }
// TODO r | a
//        contextName = getGraedContextName(contextName);
        return contextName;
    }
    
    public boolean canRename() {
        return false;
    }
    
    private String getGraedContextName(String contextPathName) {
        if (contextPathName == null) {
            return contextPathName;
        }
        
        int lastSepPosition = contextPathName.lastIndexOf(EditorUtil.ENTITY_SEPARATOR);
        if (lastSepPosition > 0) {
            lastSepPosition++;
            contextPathName = SoaUtil.getGrayString(
                    "",contextPathName.substring(0,lastSepPosition)// NOI18N
                    ,contextPathName.substring(lastSepPosition), false);
        }
        return contextPathName;
    }
}
