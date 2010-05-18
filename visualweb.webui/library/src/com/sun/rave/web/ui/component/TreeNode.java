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
package com.sun.rave.web.ui.component;

import com.sun.rave.web.ui.theme.ThemeImages;
import com.sun.rave.web.ui.component.util.event.CommandEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.el.MethodBinding;

/**
 *  <p>	This class defines a <code>TreeNode UIComponent</code>.  This component
 *	is expected to exist as a child of a {@link Tree} or another
 *	<code>TreeNode</code>.</p>
 *
 *  @author  Ken Paulsen
 */
public class TreeNode extends TreeNodeBase {

    /**
     *	Constructor.
     */
    public TreeNode() {
	super();
	setLayoutDefinitionKey(LAYOUT_KEY);
    }

    /**
     *	<p> This method determines the theme images that should be drawn from
     *	    left to right (0 to x) when rendering the lines before the text
     *	    for this node.</p>
     *
     *	@return	A <code>List</code> of Strings that represent theme keys for
     *		the images to be drawn.  The first list element is the first
     *		image to display when rendering left to right.
     */
    public List getImageKeys() {
	// Walk backward up the tree, calculate the theme image
	Stack stack = new Stack();
	Object value = null;
	Map attributes = null;
	boolean last = false;
	boolean first = true;
	boolean bottomNode = false;
//value = attributes.get("firstChild");
//boolean topNode = (value == null) ? true : value.toString().equals("true");
	for (TreeNode node = getParentTreeNode(this); node != null;
		node = getParentTreeNode(node)) {
	    attributes = node.getAttributes();
//topNode = (""+attributes.get("firstChild")).equals("true");
	    bottomNode = (""+attributes.get("lastChild")).equals("true");
	    if (first) {
		// Direct parent is special
		first = false;
		if (getChildren().size() > 0) {
		    // For this property, we use 'this' for attributes
		    if (((Boolean)getAttributes().get("expanded")).booleanValue()) {
//			if (topNode) {
//			    stack.push(ThemeImages.TREE_HANDLE_DOWN_TOP);
//			} else
			if (bottomNode) {
			    stack.push(ThemeImages.TREE_HANDLE_DOWN_LAST);
			} else {
			    stack.push(ThemeImages.TREE_HANDLE_DOWN_MIDDLE);
			}
		    } else {
//			if (topNode) {
//			    stack.push(ThemeImages.TREE_HANDLE_RIGHT_TOP);
//			} else
			if (bottomNode) {
			    stack.push(ThemeImages.TREE_HANDLE_RIGHT_LAST);
			} else {
			    stack.push(ThemeImages.TREE_HANDLE_RIGHT_MIDDLE);
			}
		    }
//ThemeImages.TREE_HANDLE_DOWN_TOP_NOSIBLING
//ThemeImages.TREE_HANDLE_RIGHT_TOP_NOSIBLING
		} else {
//		    if (topNode) {
//			stack.push(ThemeImages.TREE_LINE_FIRST_NODE);
//		    } else
		    if (bottomNode) {
			stack.push(ThemeImages.TREE_LINE_LAST_NODE);
		    } else {
			stack.push(ThemeImages.TREE_LINE_MIDDLE_NODE);
		    }
		}
	    } else {
		// We get the attributes this way because we really want to parent's values
		// to see if we have a peer
		value = node.getAttributes().get("lastChild");
		last = (value == null) ? true : value.toString().equals("true");
		if (last || (node.getChildren().size() == 0)) {
		    stack.push(ThemeImages.TREE_BLANK);
		} else {
		    stack.push(ThemeImages.TREE_LINE_VERTICAL);
		}
	    }
	}

	// Handle special case where this.getParent() is the root node...
	// don't draw a line up to it unless the root node has an icon.
	TreeNode parent = getParentTreeNode(this);
	if (parent instanceof Tree) {
	    // Ok, so this is a direct child of the root... but is it the first?
	    Iterator children = parent.getChildren().iterator();
	    Object child = null;
	    while (children.hasNext()) {
		child = children.next();
		if (child instanceof TreeNode) {
		    // Check to see if the child is 'this'
		    if (child == this) {
			// Ok, so this is the child that is effected... make
			// sure the root node doesn't have an icon
			String imgURL = parent.getImageURL();
			if (((imgURL == null) || imgURL.equals("")) &&
				(parent.getFacet(IMAGE_FACET_KEY) == null)) {
			    // This is the special case
			    // Get the top image and change it
			    stack.push(topLineImageMapping.get(stack.pop()));
			}
		    }
		    // break b/c we only want to check the first TreeNode
		    break;
		}
	    }
	}

	// Reverse the order
	List list = new ArrayList();
	while (!stack.empty()) {
	    list.add(stack.pop());
	}

	// Return the list
	return list;
    }

    /**
     *	<p> This method returns the closest parent that is a TreeNode, or null
     *	    if not found.</p>
     *
     *	@param	node	The starting <code>TreeNode</code>.
     *
     *	@return	The clost parent <code>TreeNode</code>
     */
    public static TreeNode getParentTreeNode(UIComponent node) {
	node = node.getParent();
	while ((node != null) && !(node instanceof TreeNode)) {
	    node = node.getParent();
	}
	return (TreeNode) node;
    }

    /**
     *	<p> This <code>ActionListener</code> is invoked when the
     *	    <code>TreeNode</code> expand/collapse icon is clicked.  It will
     *	    first attempt to invoke user-defined
     *	    {@link com.sun.rave.web.ui.component.until.event.Handler}s, then will
     *	    perform the default operation of expanding or collapsing this
     *	    <code>TreeNode</code>.  If a user-defined
     *	    {@link com.sun.rave.web.ui.component.until.event.Handler} returns
     *	    "false", the default functionality will not be performed.</p>
     *
     *	@param	event	The <code>ActionEvent</code>.
     */
    public void toggleNode(ActionEvent event) throws AbortProcessingException {
	// Dispatch any user-defined Events
	FacesContext context = FacesContext.getCurrentInstance();
	Object val = getLayoutDefinition(context).dispatchHandlers(
		context, "toggle", new CommandEvent(this, event));
        
        // broadcast the turner click event
        ActionEvent newEvent = new ActionEvent(this);
        fireTurnerEvent(newEvent);

	// If user-defined handler return value is "false", skip default action
	boolean cont = true;
	if (val != null) {
	    cont = !val.toString().trim().equalsIgnoreCase("false"); // NOI18N
	}

	if (cont) {
	    // Next Toggle the Icon
	    setExpanded(!isExpanded());
	    Object src = event.getSource();
	    if (src instanceof UIComponent) {
		Map attributes = ((UIComponent)src).getAttributes();
		attributes.put("icon", getHandleIcon(
			(String) attributes.get("icon")));
	    }
	}
    }
    
    /**
     * <p>Add an action listener instance for the IconHyperlink representing
     * this node's turner.</p>
     *
     * @param listener The ActionListener instance to register for turner
     * IconHyperlink clicks.
     */
    public void addActionListener(ActionListener listener) {
        addFacesListener(listener);
    }
    
    /**
     * <p>Get all ActionListener instances for this node's turner IconHyperlink
     * click.</p>
     *
     * @return ActionListener[] The list of listeners for this node's turner
     * IconHyperlink click.
     */
    public ActionListener[] getActionListeners() {
        ActionListener al[] = (ActionListener [])
            getFacesListeners(ActionListener.class);
        return (al);
    }
    
    /**
     * <p>Remove an action listener instance from the list for this node's
     * turner IconHyperlink.</p>
     *
     * @param listener The ActionListener instance to remove.
     */
    public void removeActionListener(ActionListener listener) {
        removeFacesListener(listener);
    }
    
    public void fireTurnerEvent(ActionEvent event)
            throws AbortProcessingException {
        // fire the turner event to all registered action listener instances
        super.broadcast(event);
        
        FacesContext context = getFacesContext();

        // Notify the specified action listener method (if any)
        MethodBinding mb = getActionListener();
        if (mb != null) {
            mb.invoke(context, new Object[] { event });
        }
    }

    /**
     *	<p> This method enables the icon to switch from expanded to collapsed,
     *	    or from collapsed to expanded depending on the current state of
     *	    this component.</p>
     *
     *	@param	value	The current value of the Icon.  It will use the current
     *			value to re-use first/last information from the old key.
     *
     *	@return	The new (or same if the state hasn't changed) icon state
     */
    protected String getHandleIcon(String value) {
	// Make sure we have a value
	if ((value == null) || value.trim().equals("")) {
	    value = ThemeImages.TREE_HANDLE_RIGHT_TOP_NOSIBLING;
	}

	// Convert it to the current state
	if (isExpanded()) {
	    // RIGHT to DOWN
	    value = value.replaceFirst("RIGHT", "DOWN");
	} else {
	    // DOWN to RIGHT
	    value = value.replaceFirst("DOWN", "RIGHT");
	}
	return value;
    }

    /**
     *	<p> This Map maps the standard first line image icon to the "special
     *	    case" one.  The special case is when the root is not visible, the
     *	    icon directly below where the root should be looks different than
     *	    all others.</p>
     */
    private static Map topLineImageMapping = new HashMap(6);
    static {
	topLineImageMapping.put(ThemeImages.TREE_HANDLE_DOWN_MIDDLE,
		ThemeImages.TREE_HANDLE_DOWN_TOP);
	topLineImageMapping.put(ThemeImages.TREE_HANDLE_DOWN_LAST,
		ThemeImages.TREE_HANDLE_DOWN_TOP_NOSIBLING);
	topLineImageMapping.put(ThemeImages.TREE_HANDLE_RIGHT_MIDDLE,
		ThemeImages.TREE_HANDLE_RIGHT_TOP);
	topLineImageMapping.put(ThemeImages.TREE_HANDLE_RIGHT_LAST,
		ThemeImages.TREE_HANDLE_RIGHT_TOP_NOSIBLING);
	topLineImageMapping.put(ThemeImages.TREE_LINE_MIDDLE_NODE,
		ThemeImages.TREE_LINE_FIRST_NODE);
	topLineImageMapping.put(ThemeImages.TREE_LINE_LAST_NODE,
		ThemeImages.TREE_BLANK);
    }

    /**
     *	<p> This is the facet key used to set a custom image for this
     *	    <code>TreeNode</code>. (image)</p>
     */
    public static final String	IMAGE_FACET_KEY  =	"image";

    /**
     *	<p> This is the facet key used to define the content for the
     *	    </code>TreeNode</code>. (content)</p>
     */
    public static final String	CONTENT_FACET_KEY  =	"content";

    /**
     *	<p> This is the location of the XML file that declares the layout for
     *	    the PanelGroup. (layout/tree.xml)</p>
     */
    public static final String	LAYOUT_KEY  =	"layout/treeNode.xml";
}
