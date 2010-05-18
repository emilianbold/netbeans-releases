/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
