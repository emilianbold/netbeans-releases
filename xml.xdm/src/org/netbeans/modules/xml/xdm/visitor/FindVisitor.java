/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
