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
 * Node.java
 *
 * Created on August 3, 2005, 5:46 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.xml.xdm.nodes;

import org.netbeans.modules.xml.xdm.XDMModel;
import org.w3c.dom.*;
import java.util.List;
import org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor;

/**
 * @author Ajit
 */
public interface Node extends org.w3c.dom.Node {
    
    int getId();
    
	/*
	 * Support for the visitor pattern
	 */
	void accept(XMLNodeVisitor visitor);
	
	/**
	 * A node can only be added to a tree once. Invoking this method signifies
	 * that a node has been placed into a tree and thus cannot be added. A node
	 * can be referenced by multiple trees but only added once. 
	 */
	void addedToTree(XDMModel model);
	
	/**
	 * @return tree if node has already been added to the tree. 
	 * @see #addedToTree()
	 */
	boolean isInTree();

	/**
	 * @return true the passed node has same id and belongs to same model. 
	 * @param node Node to compare
	 */
	boolean isEquivalentNode(Node node);

    /**
     * This api clones the node object and returns the clone. A node object has
     * content, attributes and children. The api will allow or disallow
     * modification of this underlying data based on the input.
     * @param cloneContent If true the content of clone can be modified.
     * @param cloneAttributes If true the attributes of the clone can be modified.
     * @param cloneChildren If true the children of the clone can be modified.
     * @return returns the clone of this node
     */
    Node clone(boolean cloneContent, boolean cloneAttributes, boolean cloneChildren);
    
    /**
     * Lookup child index of given child based on node ID.
     * @return child index of given child or -1 if not a child.
     */
    int getIndexOfChild(Node child);
}
