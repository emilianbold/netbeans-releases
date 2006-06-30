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
 * FindVisitor.java
 *
 * Created on August 10, 2005, 4:28 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
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
 * @author Ajit
 */
public class FindVisitor extends ChildVisitor {
    
    public Node find(Document root, int targetId) {
        this.targetId = targetId;
        found = false;
        result = null;
        root.accept(this);
        return result;
    }
    
    public Node find(Document root, Node target) {
        return find(root,target.getId());
    }
    
    protected void visitNode(Node node) {
        if (node.getId() == targetId) {
            result = node;
            found = true;
            return;
        } else {
            super.visitNode(node);
        }
    }
    
    private boolean found;
    private Node result;
    private int targetId;
}
