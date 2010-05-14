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
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.xml.xam.Named;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class DefaultBpelEntityNode extends BpelNode<BpelEntity> {
    
    public DefaultBpelEntityNode(BpelEntity reference, Lookup lookup) {
        super(reference, lookup);
    }
    
    public DefaultBpelEntityNode(BpelEntity reference, Children children, Lookup lookup) {
        super(reference, children, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.DEFAULT_BPEL_ENTITY_NODE;
    }

    protected String getImplHtmlDisplayName() {
        return getNameImpl();
    }

    protected String getImplShortDescription() {
        return getNameImpl();
    }

    public String getDisplayName() {
        return getNameImpl();
    }
    
    protected String getNameImpl() {
        BpelEntity ref = getReference();
        String name = null;
        if (ref != null) {
            if (ref instanceof Named) {
                name = ((Named)ref).getName();
            }
        }
        
        if (name == null) {
            if (ref instanceof ContentElement) {
                name = ((ContentElement)ref).getContent();
            }
            
            // content of the element could contain html-elements, e.g.: documentation:
            name = EditorUtil.getCorrectedHtmlRenderedString(name);
            
            if (name != null) {
                String tagName = org.netbeans.modules.bpel.editors.api.EditorUtil.getTagName(ref);
                assert tagName != null;
                name = tagName+" ( "  // NOI18N
                        +(name.length() > MAX_CONTENT_NAME_LENGTH 
                        ? name.substring(0, MAX_CONTENT_NAME_LENGTH)+" ...)" : name+" )"); // NOI18N

            }
        }
        if (name == null) {
            name = org.netbeans.modules.bpel.editors.api.EditorUtil.getTagName(ref);
        }
        return (name != null) ? name : "";
    }
}
