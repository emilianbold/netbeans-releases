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

/**
 * @author Danila_Dugurov
 *
 */
public abstract class NodeBase<T> implements WritableNode<T> {
  
  protected String id;
  
  private T content;
  
  protected WritableNode parent;
  
  protected WritableNode next;
  
  protected WritableNode previous;
  
  private static int nextId;
  
  protected NodeBase() {
    id = this.getClass().getName() + nextId++;
  }
  
  public T getMeta() {
    return content;
  }
  
  public void setMeta(T meta) {
    content = meta;
  }
  
  public String id() {
    return id;
  }
  
  public Node getParent() {
    return parent;
  }
  
  public Node getNextSibling() {
    return next;
  }
  
  public Node getPrevSibling() {
    return previous;
  }
  
  protected void checkAgnation(Node child) {
    if (child.getParent() != this)
      throw new IllegalArgumentException("child node expected!");
  }
  
  protected void checkCircles(Node child) {
    Node tmp = this;
    while (tmp != null) {
      if (tmp == child)
        throw new IllegalArgumentException(
          "circles were dertimined - operation was fobbided!");
      tmp = tmp.getParent();
    }
  }
  
  public void setNextSibling(WritableNode next) {
    this.next = next;
  }
  
  public void setParent(WritableNode parent) {
    this.parent = parent;
  }
  
  public void setPrevSibling(WritableNode previous) {
    this.previous = previous;
  }
}
