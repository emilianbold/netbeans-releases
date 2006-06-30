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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
