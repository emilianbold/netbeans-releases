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

import java.awt.Image;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.xml.reference.ReferenceChild;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 * @author Nikita Krjukov
 */
public class RefResourceNode extends BpelNode<ReferenceChild> {
    
    /**
     * @param name
     */
    public RefResourceNode(ReferenceChild node, Lookup lookup) {
        super(node, lookup);
    }
    
    public RefResourceNode(ReferenceChild node, Children children,  Lookup lookup) {
        super(node, children, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.REFERENCED_RESOURCE;
    }

    @Override
    public Image getIcon(int key) {
        return getReference().getIcon(key);
    }

    @Override
    public String getName() {
        return getReference().getName();
    }

    @Override
    public String getDisplayName() {
        return getReference().getDisplayName();
    }
}
