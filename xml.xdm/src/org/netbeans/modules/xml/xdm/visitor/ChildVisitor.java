/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.xdm.visitor;

import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

/**
 * This class provides the ability to walk nodes which have children.
 * @author Chris Webster
 */
public class ChildVisitor extends DefaultVisitor {
    
    protected void visitNode(Node container) {
        if(container.hasChildNodes()) {
            NodeList children = container.getChildNodes();
            for (int i =0; i<children.getLength(); i++) {
                Node l = (Node)children.item(i);
                l.accept(this);
            }
        }
        if(container.hasAttributes()) {
            NamedNodeMap attributes = container.getAttributes();
            for (int i =0; i<attributes.getLength(); i++) {
                Node l = (Node)attributes.item(i);
                l.accept(this);
            }
        }
    }
}
