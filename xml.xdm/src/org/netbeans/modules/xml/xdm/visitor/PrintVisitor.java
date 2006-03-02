/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.xdm.visitor;

import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.w3c.dom.NodeList;


/**
 *
 * @author Chris Webster
 */
public class PrintVisitor extends ChildVisitor {
    
    public void visit(Attribute attr) {
        System.out.printf("attr: %s\n", attr.getValue());
    }
    
    public void visit(Document doc) {
        super.visit(doc);
    }
    
    public void visit(Element e) {
        System.out.printf("element %s\n", e.getLocalName());
        super.visit(e);
    }
    
    public void visit(Text txt) {
        System.out.printf("text %s\n", txt.getText());
    }
    
}
