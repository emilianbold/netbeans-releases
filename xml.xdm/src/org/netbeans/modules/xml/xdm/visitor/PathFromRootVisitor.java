/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
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
