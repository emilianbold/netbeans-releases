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
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

public class MessagePartNode extends BpelNode<Part> {
    
    public MessagePartNode(Part reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public MessagePartNode(Part reference, final Lookup lookup) {
        super(reference, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.MESSAGE_PART;
    }
    
    public String getDisplayName() {
        Part ref = getReference();
        return ref == null ? null : ref.getName();
    }
    
    protected String getImplHtmlDisplayName() {
        String result;
        String typeName = getTypeName();
        result = SoaUtil.getGrayString(
                getName(),
                typeName == null ? "" : " " + typeName); // NOI18N
        //
        return result;
    }
    
    private String getTypeName() {
        String result = null;
        Part part = getReference();
        if (part != null) {
            NamedComponentReference<GlobalElement> elementRef = part.getElement();
            if (elementRef != null) {
                GlobalElement element = elementRef.get();
                if (element != null) {
                    result = element.getName();
                } else {
                    result = elementRef.getRefString();
                }
            }
            //
            if (result == null || result.length() == 0) {
                NamedComponentReference<GlobalType> typeRef = part.getType();
                if (typeRef != null) {
                    GlobalType type = typeRef.get();
                    if (type != null) {
                        result = type.getName();
                    } else {
                        result = typeRef.getRefString();
                    }
                }
            } 
        }
        //
        return result;
    }
    
}
