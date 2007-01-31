/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package dwarfvsmodel;

import java.util.*;

/**
 * Represents a set of
 * - model or dwarf declarations and
 * - nested nodes
 *
 * @author vk155633
 */
public class Node<T> {

    private Node<T> parent;
    private List<T> declarations = new ArrayList<T>();
    private List<Node<T>> subnodes = new ArrayList<Node<T>>();

    public Iterable<T> getDeclarations() {
	return declarations;
    }

    public Iterable<Node<T>> getSubnodes() {
	return subnodes;
    }

    public void addDeclaration(T declaration) {
	declarations.add(declaration);
    }

    public void addSubnode(Node<T> node) {
//	    subnodes.add(node);
	if( node.getDeclarations().iterator().hasNext() ) {
	    subnodes.add(node);
	    node.setParent(this);
	}
	else {
	    for( Node<T> subnode : node.getSubnodes() ) {
		addSubnode(subnode);
	    }
	}
    }

    private void setParent(Node<T> parent) {
	this.parent = parent;
    }

    public boolean isEmpty() {
	return declarations.isEmpty() && subnodes.isEmpty();
    }

    public void flatten() {
	flatten3();
    }

    private void flatten1() {
	List<Node<T>> newSubnodes = new ArrayList<Node<T>>();
	for( Node<T> child : subnodes ) {
	    child.flatten();
	    if( ! child.isEmpty() ) {
		newSubnodes.add(child);
		newSubnodes.addAll(child.subnodes);
		child.subnodes.clear();
	    }
	}
	subnodes = newSubnodes;
    }
    
    private void flatten2() {
	List<T> newDeclarations = new ArrayList<T>();
	getDeclarationsRecursively(newDeclarations, subnodes);
	Node<T> subnode = new Node<T>();
	subnode.declarations = newDeclarations;
	subnodes.clear();
	addSubnode(subnode);
    }
    
    private void flatten3() {
	getDeclarationsRecursively(declarations, subnodes);
	subnodes.clear();
    }
    
    /** 
     * Adds all declarations from nodes into the given list, recursively 
     * @param declarations list to add declarations to
     * @param nodes nodes from which to extract declarations
     */
    private void getDeclarationsRecursively(List<T> declarations, Iterable<Node<T>> nodes) {
	if( nodes != null ) {
	    for( Node<T> node : nodes ) {
		DMUtils.addAll(declarations, node.getDeclarations());
		getDeclarationsRecursively(declarations, node.subnodes);
	    }
	}
    }
    
    public int getDeclarationsCount() {
	int sum = declarations.size();
	for( Node<T> child : subnodes ) {
	    sum += child.getDeclarationsCount();
	}
	return sum;
    }
    
    private class NodeComparator implements Comparator<Node<T>> {
	
	private Comparator<T> comparator;
	
	public NodeComparator(Comparator<T> comparator) {
	    this.comparator = comparator;
	}
	
	public int compare(Node<T> node1, Node<T> node2) {
	    // NB: we assume that both nodes are already sorted
	    if( node1.declarations.isEmpty() ) {
		return 0;
	    }
	    else if( node2.declarations.isEmpty() ) {
		return 0;
	    }
	    else {
		return comparator.compare(node1.declarations.get(0), node2.declarations.get(0));
	    }
	}
    }
    
    public void sort(final Comparator<T> comparator) {
	Collections.sort(declarations, comparator);
	for( Node<T> child : subnodes ) {
	    child.sort(comparator);
	}
	// NB: first call sort for each child, then sort children
	Collections.sort(subnodes, new NodeComparator(comparator));
    }
}
