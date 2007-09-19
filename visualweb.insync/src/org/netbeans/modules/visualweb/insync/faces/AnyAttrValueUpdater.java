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
package org.netbeans.modules.visualweb.insync.faces;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.netbeans.modules.visualweb.insync.markup.MarkupVisitor;


/**
 * 
 *
 * @author 
 */
public class AnyAttrValueUpdater extends MarkupVisitor {
    protected String oldValue;
    protected String newValue;

    public AnyAttrValueUpdater(String oldValue, String newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public void visit(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE ) {
        	NamedNodeMap attrNodeMap = node.getAttributes();
        	for (int i = 0; i < attrNodeMap.getLength(); i++) {
                Node attrNode = attrNodeMap.item(i);
        		if (attrNode.getNodeValue().equals(oldValue)) {
        			attrNode.setNodeValue(newValue);
        		}
        	}
        }
    }
}
