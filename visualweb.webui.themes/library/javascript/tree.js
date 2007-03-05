// FIXME: Verify attributes
// FIXME: Deal w/ image badging

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */


function setCookieValue(cookieName, val) {
    document.cookie = cookieName + "=" + val;
}

function getCookieValue(cookieName) {
    var docCookie = document.cookie;
    var pos= docCookie.indexOf(cookieName+"=");

    if (pos == -1) {
		 return null;
    }

    var start = pos+cookieName.length+1;
    var end = docCookie.indexOf(";", start );

    if (end == -1) {
		 end= docCookie.length;
    }

    return docCookie.substring(start, end);
}

/**
 *  <p>	This function expands or collapses the given tree node.  It expects the
 *	source of the given event object (if supplied) to be a tree handle
 *	image.  It will change this image to point in the correct direction
 *	(right or down).  This implementation depends on the tree handle image
 *	names including "tree_handleright" and "tree_handledown" in them.
 *	Swapping "right" and "down" in these names must change the handle
 *	direction to right and down respectively.</p>
 */
function expandCollapse(treeNode, event) {
    var tree = getTree(treeNode);
    var childNodes = document.getElementById(treeNode.id+"_children");
    if (childNodes) {
	// Get the event source
	if (!event) {
	    event = window.event;
	}
	var elt;
	if (event) {
	    elt = (event.target) ? event.target : event.srcElement;
	}

	if (elt.id.indexOf("_image") < 0) {
		var baseId = elt.id.substring(0, elt.id.lastIndexOf(":") + 1);    
		
		elt = document.getElementById(baseId + "turner:turner_image");
	}

	// First, unhighlight the parent if applicable
	unhighlightParent(getSelectedTreeNode(tree.id));

	// Change the style to cause the expand / collapse & switch the image
	var display = childNodes.style.display;
	if (display == "none") {
	    childNodes.style.display = "block";
	    if (elt && elt.src) {
		elt.src = elt.src.replace(
		    "tree_handleright", "tree_handledown");
	    }
	} else {
	    childNodes.style.display = "none";
	    if (elt && elt.src) {
		elt.src = elt.src.replace(
		    "tree_handledown", "tree_handleright");
	    }
	}

	// Last, update the visible parent of the selected node if now hidden
	highlightParent(getSelectedTreeNode(tree.id));
    }
}

/**
 *  <p>	This function returns the Tree for the given TreeNode.  From
 *	a DOM point of view, the tree directly contains all its children
 *	(excluding *_children div tags.  This function will return the first
 *	parentNode that is a div w/ an id != "*_children".</p>
 */
function getTree(treeNode) {
    var tree = treeNode.parentNode;
    var SUFFIX = new String("_children");

    while (tree) {
		// Ignore all div's ending w/ SUFFIX
		if ((tree.nodeName == "DIV") &&
				(tree.id.substr(tree.id.length-SUFFIX.length) != SUFFIX)) {
			break;
		}
		tree = tree.parentNode;
	 }

    return tree;
}

/**
 *  <p>	This method handles TreeNode onClick events.  It takes the TreeNode
 *	&lt;div&gt; object that was clicked on in order to process the
 *	highlighting changes that are necessary.  This object may be obtained by
 *	calling <code>getElementById("&lt;TreeNode.getClientId()&gt;")</code>.
 *	If this function is invoked from the TreeNode &lt;div&gt; object itself
 *	(as is the case when this method is implicitly called), the TreeNode
 *	object is simply the <code>this</code> variable.</p>
 */
function onTreeNodeClick(treeNode, event) {
    // Check for Tree Handles
    if (isTreeHandle(event)) {
	expandCollapse(treeNode, event);
	return true;
    }

    // Make sure they clicked on an href
    if (!isAnHref(event)) {
	// Do nothing
	return true;
    }

    // If we're here, we should select the TreeNode
    return selectTreeNode(treeNode);
}

/**
 *  <p>	This function may be used to select the given TreeNode.  It will clear
 *	the previous TreeNode and select the given one.</p>
 */
function selectTreeNode(treeNode) {
    // Find the top of the tree
    var tree = getTree(treeNode);

    // Clear the old highlighting
    clearAllHighlight(tree.id);

    // Mark the node as selected
    setCookieValue(tree.id+"-hi", treeNode.id);

    // first highlight is as a parent
    // when the left frame loads the nodes will be hightlighted correctly
    highlightParent(treeNode);
    highlight(treeNode);

    // onClick handler should proceed with hyperlink
    return true;
}

/**
 *  <p>	This function returns the selected TreeNode given the treeId of the
 *	Tree.</p>
 */
function getSelectedTreeNode(treeId) {
    var id = getCookieValue(treeId+"-hi");
    if (id) {
	return document.getElementById(id);
    }
    return null;
}

function clearAllHighlight(cookieId) {
    // Clear
    var selectedNode = getSelectedTreeNode(cookieId);
    clearHighlight(selectedNode);

    setCookieValue(cookieId+"-hi", "");

// FIXME: Fix this...
//    clearHighlight(document.getElementById(currentHighlightParent));

    return true;
}

function clearHighlight(node) {
    if (node) {
        node.style.backgroundColor="transparent";
        node.style.fontWeight = "normal";
        node.style.color = getNormalTreeTextColor();
    }
    return true;
}

/**
 *  <p>	This function determines if the event source was a tree handle image.
 *	This implementation depends on the tree handle image file name
 *	containing "tree_handle" and no other images containing this
 *	string.</p>
 */
function isTreeHandle(event) {
    if (!event) {
	event = window.event;
	if (!event) {
	    return false;
	}
    }
    var elt = (event.target) ? event.target : event.srcElement;

    // Ignore Tree Handles b/c they should not update highlighting
    if (elt.nodeName == "IMG") {
	var url = new String(elt.src);
	if ((url.indexOf("tree_handle") > 0) && (url.indexOf("theme") > 0)) {
	    // This is a tree handle
	    return true;
	}
    } else if (elt.nodeName == "A") {
		 // might have been user pressing enter on a around image
		 if (elt.innerHTML.toLowerCase().indexOf("<img") == 0) {
			 // This is a tree handle
			 return true;
		 }
	 }

    // Not a tree handle
    return false;
}

/**
 *  <p>	This method checks to see if the event.target is an href, or if any of
 *	the parent nodes which contain it is an href.  To be an href, it must be
 *	an "A" tag with an "href" attribute containing atleast 4 characters.
 *	(Note: Browsers will add on the protocol if you supply a relative URL
 *	    such as one starting with a '#', '/', or filename).</p>
 */
function isAnHref(event) {
    if (!event) {
	event = window.event;
	if (!event) {
	    return false;
	}
    }
    var elt = (event.target) ? event.target : event.srcElement;

    // Look for parent href
    while (elt != null) {
	if (elt.nodeName == "A") {
	    // Creates a String containing the url
	    var url = new String(elt);
	    if (url.length > 4) {
		// All URLs are atleast this long
		return true;
	    }
	}
	elt = elt.parentNode;
    }

    // Not an href
    return false;
}

/**
 *  <p>	This function updates the highlighting for the given Tree client id.
 *	This function provides a way to restore the highlighting when a Tree is
 *	reloaded in a window (necessary each page load).</p>
 */
function updateHighlight(cookieId) {
    var selNode = getSelectedTreeNode(cookieId)
    highlight(selNode);
// FIXME: This doesn't work if the TreeNode element doesn't exist (which is the case for the server-side tree)
    highlightParent(selNode);
}

/**
 *  <p>	This function highlights the given <code>TreeNode</code>.  The
 *	<code>obj</code> passed in is actually the &lt;div&gt; around the html
 *	for the <code>TreeNode</code> and may be obtained by calling
 *	<code>getElementById("&lt;TreeNode.getClidentId()&gt;")</code>.</p>
 */
function highlight(node) {
    if (node) {
        node.style.backgroundColor = getHighlightTreeBgColor();
        node.style.fontWeight = "bold";
        node.style.color = getHighlightTreeTextColor();

        return true;
    }
    return false;
}

/**
 *  <p>	This function finds a node of the given type w/ matching property name
 *	and value by looking recursively deep at the children of the given
 *	node.  The type is the type of node to find (i.e. "IMG").  The propName
 *	is the name of the property to match (i.e. "src" on an "IMG" node).
 *	The propVal is the value that must be contained in propName; this value
 *	does not have to match exactly, it only needs to exist within the
 *	property.</p>
 */
function findNodeByTypeAndProp(node, type, propName, propVal) {
    if (node == null) {        
        return null;
    }
    // First check to see if node is what we are looking for...
    if (node.nodeName == type) {
	if (node[propName].indexOf(propVal) > -1) {
	    return node;
	}
    }

    // Not what we want, walk its children if any
    var nodeList = node.childNodes;
    if (!nodeList || (nodeList.length == 0)) {
	return null;
    }
    var result;
    for (var count = 0; count<nodeList.length; count++) {
	// Recurse
	result = findNodeByTypeAndProp(nodeList[count], type, propName, propVal);
	if (result) {
	    // Propagate the result
	    return result;
	}
    }

    // Not found
    return null;
}

/**
 *  <p>	This function determines if the given TreeNode is expanded.  It returns
 *	<code>true</code> if it is, <code>false</code> otherwise.</p>
 */
function treeNodeIsExpanded(treeNode) {
    // Find the div containing the tree images for this TreeNode row
    var node = document.getElementById(treeNode.id + "LineImages");
    node = findNodeByTypeAndProp(node, "IMG", "src", "tree_handle");
    if (!node) {
	// This shouldn't happen, but if it does return true b/c nothing
	// happens in this case
	return true;
    }

    // If the image contains this string, it is not expanded
    return (node.src.indexOf("tree_handleright") == -1);
}

/**
 *  <p>	This function returns the parent TreeNode of the given TreeNode.</p>
 */
function getParentTreeNode(treeNode) {
    // Get the parent id
    var parentId = treeNode.parentNode.id;
    var childrenIdx = parentId.indexOf("_children");
    if (childrenIdx == -1) {
	return null;
    }

    // This is really a peer div id to what we really want... remove _children
    parentId = parentId.substring(0, childrenIdx);

    // Return the parent TreeNode
    return document.getElementById(parentId);
}

function unhighlightParent(childNode) {
    if (!childNode) {
	return false;
    }

    // First find the parent node and make sure it is collapsed (we don't
    // highlight parent nodes when the selected node is visible)
    var parentNode = getParentTreeNode(childNode);
    var highlight = null;
    while (parentNode != null) {
	if (!treeNodeIsExpanded(parentNode)) {
	    highlight = parentNode;
	}
	parentNode = getParentTreeNode(parentNode);
    }
    if (highlight) {
	highlight.style.fontWeight = "normal";
    }
    return true;
}

function highlightParent(childNode) {
    if (!childNode) {
	return false;
    }

    // First find the parent node and make sure it is collapsed (we don't
    // highlight parent nodes when the selected node is visible)
    var parentNode = getParentTreeNode(childNode);
    var highlight = null;
    while (parentNode != null) {
	if (!treeNodeIsExpanded(parentNode)) {
	    highlight = parentNode;
	}
	parentNode = getParentTreeNode(parentNode);
    }
    if (highlight) {
	highlight.style.fontWeight = "bold";
    }
    return true;
}

function getNormalTreeTextColor() {
    return "#003399";
}

function getHighlightTreeBgColor() {
    return "#CBDCAF";  // ~greenish color
}

function getHighlightTreeTextColor() {
    return "#000000";  // black
}

/**
 * If the Tree's expandOnSelect property is true, this method is called to 
 * expand the turner of the tree node with the given labelLink.
 */
function expandTurner(labelLink, isClientSide, event) {
	var labelLinkId = labelLink.id;
	var formId = labelLinkId.substring(0, labelLinkId.indexOf(":"));
	var nodeId = labelLinkId.substring(0, labelLinkId.lastIndexOf(":"));  

	var node = document.getElementById(nodeId);
	var turnerLink = document.getElementById(nodeId + ":turner"); 

	if (turnerLink == null) {
		return;
	}

	if (!treeNodeIsExpanded(node)) {
		// folder is currently closed, expand it
		if (isClientSide) {
			expandCollapse(node, event);      
		} else {
			turnerLink.onclick();
		}    
	}
}

/**
 * When a TreeNode's content link has an associated action, this method should
 * be called to ensure selection highlighting and (if necessary) node expansion
 * occurs.
 *
 * If the developer specifies the content facet for a given TreeNode, he should
 * call this function from his facet hyperlink's onClick.
 */
function treecontent_submit(hyperlink, nodeId, formId, params) {
	var linkId = hyperlink.id;

	if (nodeId == null) {
		// try to parse the node id (may not work)
		var nodeId = linkId.substring(0, linkId.lastIndexOf(":"));
	}

	var node = document.getElementById(nodeId);
	var tree = getTree(node);

	if (formId == null) {
		formId = linkId.substring(0, linkId.indexOf(":"));
	}

	// update the current selection
	selectTreeNode(node);

	// set a cookie that the Tree's decode method will inspect and expand the 
	// corresponding node if necessary
	setCookieValue(tree.id + "-expand", nodeId);
	
	// now execute the standard hyperlink_submit to invoke the action
	return hyperlink_submit(hyperlink, formId, params);
}
