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
 * PathFromRootVisitor.java
 *
 * Created on August 4, 2005, 6:43 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.xml.xdm.visitor;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;

/**
 *
 * @author Chris Webster
 */
public class PathFromRootVisitor extends ChildVisitor {

    public List<Node> findPath(org.w3c.dom.Document root, org.w3c.dom.Node target) {
        Document wroot = root instanceof Document ? (Document) root : null;
        Node wtarget = target instanceof Node ? (Node) target : null;
        return findPath(wroot, wtarget);
    }
    
    public List<Node> findPathToRootElement(org.w3c.dom.Element root, org.w3c.dom.Node target) {
        Element wroot = root instanceof Element ? (Element) root : null;
        Node wtarget = target instanceof Node ? (Node) target : null;
        assert root != null && target != null;
        
        this.target = wtarget;
        found = false;
        pathToTarget = null;
        wroot.accept(this);
        return pathToTarget;
    }
    
    public List<Node> findPath(Document root, Node target) {
        assert root != null;
        assert target != null;
        this.target = target;
        found = false;
        pathToTarget = null;
        root.accept(this);
        return pathToTarget;
    }
    
    protected void visitNode(Node n) {
        // if already found just return
        if(found) return;
        if (target.getId() == n.getId()) {
            pathToTarget = new LinkedList<Node>();
            pathToTarget.add(n);
            found = true;
        } else {
            super.visitNode(n);
            if(found) {
                // add the ancestors to the list 
                pathToTarget.add(n);
            }
        }
    }
    
    private boolean found;
    private List<Node> pathToTarget;
    private Node target;
}
