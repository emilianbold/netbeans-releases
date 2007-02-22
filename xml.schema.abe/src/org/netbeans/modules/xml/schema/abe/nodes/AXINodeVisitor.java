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
package org.netbeans.modules.xml.schema.abe.nodes;

import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.openide.nodes.Node;

/**
 * Provide a visitor to determine the appropriate nodes for AXIComponents
 * @author Chris Webster
 */
public class AXINodeVisitor implements AXIVisitor {
    
    
    public Node getNode(AXIComponent component) {
	n = null;
	component.accept(this);
	return n;
    }
    
    public void visit(AnyAttribute attribute) {
	// no node for this
    }
    
    public void visit(Element element) {
	n = new ElementNode(element);
    }
    
    public void visit(ContentModel model) {
	n = new ContentModelNode(model);
    }
    
    public void visit(Datatype model) {
		//TODO
    }
	
    public void visit(AnyElement element) {
	n = new AnyElementNode(element);
    }
    
    public void visit(AXIDocument root) {
	n = new ABEDocumentNode(root);
    }
    
    public void visit(Attribute attribute) {
	// no nodes to create for this
    }
    
    public void visit(Compositor compositor) {
	n = new CompositorNode(compositor);
    }
    
    private Node n = null;
}
