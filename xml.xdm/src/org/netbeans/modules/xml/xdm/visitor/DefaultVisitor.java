/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * DefaultVisitor.java
 *
 * Created on August 18, 2005, 6:04 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.xml.xdm.visitor;


/**
 *
 * @author ChrisWebster
 */
public class DefaultVisitor implements XMLNodeVisitor {
    public void visit(org.netbeans.modules.xml.xdm.nodes.Attribute attr) {
        visitNode(attr);
    }
    
    public void visit(org.netbeans.modules.xml.xdm.nodes.Document doc) {
        visitNode(doc);
    }
    
    public void visit(org.netbeans.modules.xml.xdm.nodes.Element e) {
        visitNode(e);
    }
    
    public void visit(org.netbeans.modules.xml.xdm.nodes.Text txt) {
        visitNode(txt);
    }
    
    protected void visitNode(org.netbeans.modules.xml.xdm.nodes.Node node) {
    }
    
}
