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

import java.awt.Image;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.openide.nodes.Node;
import org.w3c.dom.Element;
/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class UnknownUsageNode extends AbstractNode {
    Object reference;
    public UnknownUsageNode(Object reference) {
        super(Children.LEAF);
        this.reference = reference;
    }

    public String getName() {
        String name = null;
        if (reference instanceof Named ) {
            name = ((Named) reference).getName();
        }
        
        if (name == null && reference instanceof DocumentComponent) {
            Element domElement = ((DocumentComponent)reference).getPeer();
            name = domElement.getTagName();
        }
        
        return name != null ?
                name : 
                reference == null ? null : reference.getClass().getName();
    }

    public Image getIcon(int type) {
        return NodeType.UNKNOWN_TYPE.getImage();
    }
}

