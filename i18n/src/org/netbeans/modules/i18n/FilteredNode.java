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


package org.netbeans.modules.i18n;

import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;




/**
 * This class implements a filter node that can be used to display a
 * hierarchy of nodes filtered by a criteriea defined by
 * <code>NodeFilter</code>. We don't use NodeAcceptor because of
 * efficiency - node acceptor requires an array as a parameter,
 * which we don't want to create.
 */
public class FilteredNode extends FilterNode {

  private NodeFilter filter;
  private String newName = null;

  /**
   * Decides which nodes should be included in the hierarchy and which
   * not.
   */
  public interface NodeFilter {
    public boolean acceptNode(Node node) ;
  }

  public FilteredNode(Node original, NodeFilter filter ) {
      this(original, filter, null);
  }



  public FilteredNode(Node original, NodeFilter filter, String newName) {
    super(original, new FilteredChildren(original, filter));
    this.filter = filter;
    this.newName = newName;
  }

  public String getDisplayName() { 
    if (newName != null) return newName; else return super.getDisplayName();
  }
    
  public Node cloneNode() {
    return new FilteredNode(this.getOriginal().cloneNode(), this.filter);
  }

    


  /**
   * A mutualy recursive children that ensure propagation of the
   * filter to deeper levels of hiearachy. That is, it creates
   * FilteredNodes filtered by the same filter.
   */
  public static class FilteredChildren extends FilterNode.Children {
    private NodeFilter filter;

    public FilteredChildren(Node original, NodeFilter filter) {
      super(original);
      this.filter = filter;
    }

    protected Node copyNode(Node node) {
      return new FilteredNode(node, this.filter);
    }

    protected Node[] createNodes(Object key) {
      if (filter.acceptNode((Node)key)) 
	return super.createNodes(key);
      else 
	return new Node [0];
    }

  }

}
