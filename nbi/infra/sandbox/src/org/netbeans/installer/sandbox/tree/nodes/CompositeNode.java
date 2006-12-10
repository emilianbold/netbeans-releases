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
 *
 * $Id$
 */
package org.netbeans.installer.sandbox.tree.nodes;

import org.netbeans.installer.sandbox.tree.Node;
import org.netbeans.installer.sandbox.tree.NodeType;

/**
 * @author Danila_Dugurov
 *
 */
public class CompositeNode extends NodeBase {
  
  Node firstChild;
  
  public NodeType type() {
    return NodeType.COMPOSITE;
  }
  
  public Node getFirstChild() {
    return firstChild;
  }
  
  public Node getLastChild() {
    if (firstChild == null)
      return null;
    Node next = firstChild;
    while (next.getNextSibling() != null) {
      next = next.getNextSibling();
    }
    return next;
  }
  
  public void insertBefore(Node newChild, Node nextChild) {
    checkAgnation(nextChild);
    checkCircles(newChild);
    if (newChild.getParent() != null)
      newChild.getParent().removeChild(newChild);
    final NodeBase base = (NodeBase) newChild;
    final WritableNode next = (WritableNode) nextChild;
    final WritableNode previous = (WritableNode) next.getPrevSibling();
    if (next == firstChild)
      firstChild = base;
    if (next != null)
      next.setPrevSibling(base);
    if (previous != null)
      previous.setNextSibling(base);
    base.setNextSibling(next);
    base.setPrevSibling(previous);
    base.setParent(this);
  }
  
  public void addChild(Node newChild) {
    checkCircles(newChild);
    if (newChild.getParent() != null)
      newChild.getParent().removeChild(newChild);
    final NodeBase base = (NodeBase) newChild;
    final WritableNode last = (WritableNode) getLastChild();
    if (last == null)
      firstChild = base;
    else
      last.setNextSibling(base);
    base.setPrevSibling(last);
    base.setParent(this);
  }
  
  public Node replaceChild(Node newChild, Node oldChild) {
    checkAgnation(oldChild);
    checkCircles(newChild);
    insertBefore(newChild, oldChild);
    return removeChild(oldChild);
  }
  
  public Node removeChild(Node child) {
    checkAgnation(child);
    NodeBase base = (NodeBase) child;
    final WritableNode next = base.next;
    final WritableNode previous = base.previous;
    if (next != null)
      next.setPrevSibling(previous);
    if (previous != null)
      previous.setNextSibling(next);
    if (firstChild == base)
      firstChild = next;
    return clearRelationship(base);
  }
  
  public void setFirstChild(WritableNode firstChild) {
    this.firstChild = firstChild;
  }
  
  private Node clearRelationship(WritableNode child) {
    child.setNextSibling(null);
    child.setPrevSibling(null);
    child.setParent(null);
    return child;
  }
}
