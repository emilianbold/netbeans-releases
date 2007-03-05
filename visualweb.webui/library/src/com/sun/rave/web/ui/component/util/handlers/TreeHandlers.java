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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.rave.web.ui.component.util.handlers;

import com.sun.rave.web.ui.component.Tree;
import com.sun.rave.web.ui.component.TreeNode;
import com.sun.rave.web.ui.component.util.event.HandlerContext;

import java.util.List;

import javax.faces.component.UIComponent;


/**
 *  <P>	This class contains {@link com.sun.rave.web.ui.component.util.event.Handler}
 *	methods that perform {@link Tree} and
 *	{@link TreeNode} specific functions.</P>
 *
 *  @author  Ken Paulsen (ken.paulsen@sun.com)
 */
public class TreeHandlers {

    /**
     *	<P> Default Constructor.</P>
     */
    public TreeHandlers() {
    }

    /**
     *	<p> This {@link com.sun.rave.web.ui.component.util.event.Handler} creates a
     *	    <code>List</code> of image theme icons that should be rendered from
     *	    left to right representing the lines and other images that create a
     *	    portion of the tree.</p>
     *
     *	@param	context	    The {@link HandlerContext}.
     */
    public void getImageKeys(HandlerContext context) {
	// Get the tree node
	TreeNode treeNode = (TreeNode)context.getInputValue(CURRENT_NODE);

	// Get the list (delegate back to TreeNode so that handler does not
	// contain this logic)
	List list = treeNode.getImageKeys();

	// Set the output value
	context.setOutputValue("result", list);
    }


    /**
     *	<p> This {@link com.sun.rave.web.ui.component.util.event.Handler} selects
     *	    the specified {@link TreeNode}.  The previous selection (if any)
     *	    will be unselected.</p>
     *
     *	@param	context	    The {@link HandlerContext}.
     */
    public void selectTreeNode(HandlerContext context) {
	TreeNode treeNode = (TreeNode) context.getInputValue("treeNode");
	UIComponent comp = treeNode;
	while ((comp != null)
		&& !(comp instanceof Tree)) {
	    comp = comp.getParent();
	}
	if (comp != null) {
	    ((Tree) comp).selectTreeNode(treeNode);
	}
    }

    /**
     *	<p> This method provides the <code>decode()</code> functionality for
     *	    the {@link Tree}.</p>
     *
     *	@param	context	    The {@link HandlerContext}.
     */
    public void decodeTree(HandlerContext context) {
        Tree comp = (Tree) context.getEventObject().getSource();
	comp.setSubmittedValue(comp.getCookieSelectedTreeNode());
        
        String nodeId = comp.getCookieExpandNode();
        
        if (nodeId != null && comp.isExpandOnSelect()) {
            // a selection was made - expand the corresponding TreeNode
            TreeNode node = (TreeNode) comp.findComponent(":".concat(nodeId));            
            
            if (node != null) {
                node.setExpanded(true);
            }            
        }
    }

    /**
     *	<p> This constant defines the input parameter key used to pass in the
     *	    {@link TreeNode}. ("node")</p>
     */
    public static final String CURRENT_NODE =	"node"; // NO18N
}
