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
import org.netbeans.modules.bpel.editors.api.EditorUtil;
/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class Util {
    
    private Util() {
    }
    
    public static String getNodeName(BpelNode node) {
        assert node != null;
            
        Object ref = node.getReference();
        if (ref == null || !(ref instanceof BpelEntity)) {
            return node.getHtmlDisplayName();
        }
        String nodeName = null;
        NodeType nodeType = node.getNodeType();
        switch (nodeType) {
            case BOOLEAN_EXPR :
                nodeName = EditorUtil.getName((BpelEntity)ref);
                break;
            case INVOKE :
            case RECEIVE :
            case REPLY :
            case ON_EVENT :
            case MESSAGE_HANDLER :
            case PARTNER_LINK :
                nodeName = node.getShortDescription();
                if (nodeName != null 
                        && nodeName.startsWith(nodeType.getDisplayName())) 
                {
                    String tmpNodeName = nodeName
                                    .substring(nodeType.getDisplayName().length());
                    if (tmpNodeName.trim().length() > 0) {
                        nodeName = tmpNodeName;
                    }
                }
                break;
            default:
                nodeName = node.getHtmlDisplayName();
        }
        return nodeName;
    }
    
}
