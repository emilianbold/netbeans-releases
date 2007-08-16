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
package com.sun.rave.web.ui.component;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

/**
 * <p> Use the <code>ui:tree</code> tag to display a tree structure in the
 * 	rendered HTML page.  Trees are often used for navigating in a web
 * 	application.</p>
 * 
 *     <h3>HTML Elements and Layout</h3>
 * 
 *     <p>	The Tree component defines and renders a tree structure. A tree
 * 	structure is commonly used for navigation through data, as in file
 * 	system navigators.  The base, or root, of the tree is shown at the top,
 * 	with the branches going downward.</p>
 * 
 *     <p>	The tree is rendered with <code>&lt;div&gt;</code> XHTML elements which
 * 	define rows as well as nesting within the tree.</p>
 * 
 *     <p> The <code>ui:tree</code> tag is the root of the tree, and acts as a
 * 	container for the <code>ui:treeNode</code> tags. The
 * 	<code>ui:treeNode</code> tags add branches, or nodes, to the tree. In
 * 	the rendered web page, nodes can be expanded and collapsed when you
 * 	click on small icons next to the nodes. In addition, the node is
 * 	highlighted when you click on the node's hyperlink to indicate the node
 * 	has focus.</p>
 * 
 *     <p>	The <code>ui:tree</code> tag provides the ability to render the tree
 * 	root in any of the following ways:</p>
 * 
 *     <ul><li>As a single root, with a title bar to make the root visually
 * 	    distinctive. You must specify attributes or facets in the
 * 	    <code>ui:tree</code> tag to allow the title bar to be rendered.</li>
 * 	<li>As a single root, without a title bar.  The root looks like any
 * 	    other container node in the tree. You must omit attributes and
 * 	    facets in the <code>ui:tree</code> tag, and specify a single
 * 	    <code>ui:treeNode</code> as the root node, with other
 * 	    <code>ui:treeNode</code> tags contained within the first
 * 	    <code>ui:treeNode</code>.</li>
 * 	<li>As a multi-root tree, without a title bar. There is no single top
 * 	    node, but two or more at the same level. You must omit attributes
 * 	    and facets in the <code>ui:tree</code> tag and include multiple
 * 	    <code>ui:treeNode</code> tags.</li></ul>
 * 
 *     <h4>Defining a Title Bar for the Single Root Tree</h4>
 * 
 *     <p>	The title bar consists of the following elements:</p>
 * 
 *     <ul><li>A shaded background color determined by the theme.</li>
 * 	<li>An optional graphic to the left of the title bar's text.</li>
 * 	<li>Text or a hyperlink for the content of the title bar.</li></ul>
 * 
 *     <p>	The title bar can be defined with either <code>ui:tree</code> tag
 * 	attributes or facets. The title bar is rendered if the
 * 	tree component includes <code>imageURL</code> property for the graphic,
 * 	the <code>text</code> property for the title text, the
 * 	<code>content</code> facet, or the <code>image</code> facet.</p>
 * 
 *     <p>	The graphic and title text areas can be overridden with the following
 * 	facets:</p>
 * 
 *     <ul><li style="clear: both">
 * 	    <div style="float:left; width: 100px; font-weight: bold;">
 * 		<code>content</code></div>
 * 	    <div style="float:left; width: 30px;"><code>--</code></div>
 * 	    <div style="float:right; width: 75%;">
 * 		Specifies the text or hyperlink for the title bar. When the
 * 		<code>content</code> facet is used, the text and hyperlink
 * 		properties have no effect.</div></li>
 * 	<li style="clear: both">
 * 	    <div style="float:left; width: 100px; font-weight: bold;">
 * 		<code>image</code></div>
 * 	    <div style="float:left; width: 30px;"><code>--</code></div>
 * 	    <div style="float:right; width: 75%;">
 * 		Specifies the image area. When the <code>image</code> facet is
 * 		used, image properties have no effect.</div></li></ul>
 *     <br style="clear: both" />
 * 
 *     <h4>Defining a Tree with Multiple Roots</h4>
 * 
 *     <p>	To define a tree that has multiple roots rather than a single top root,
 * 	you must use the <code>ui:tree</code> tag only as a container. You
 * 	should not specify attributes for the graphic or title text, or use
 * 	facets in the <code>ui:tree</code> tag.</p>
 * 
 *     <p>	You can use <code>ui:treeNode</code> tags as containers for other
 * 	<code>ui:treeNode</code> tags, but should not use one to contain all
 * 	the others to avoid the appearance of a single root.</p>
 * 
 * <!--
 *     <h3>Theme Identifiers</h3>
 * 
 *     <p>	The following identifiers are written as class values in the html.
 * 	The locations are described below.</p>
 * 
 *     <ul><li><code>Tree</code> for the outer <code>&lt;div&gt;</code> around the
 * 	    tree component.</li>
 * 	<li><code>TreeContent</code> for the <code>&lt;div&gt;</code> around the
 * 	    content area of the tree (or tree node) component.  This is the same
 * 	    area that may be replaced by the <code>content</code> facet.</li>
 * --	<li><code>TreeParentLink</code> </li> --
 * --	<li><code>TreeLink</code> </li> --
 * --	<li><code>TreeLinkSpace</code> </li> --
 * --	<li><code>TreeImg</code> </li> --
 * 	<li><code>TreeImgHeight</code> ensures each tree row is the correct
 * 	    height.</li>
 * 	<li><code>TreeRootRow</code> for the <code>&lt;div&gt;</code> around
 * 	    the title bar.</li>
 * 	<li><code>TreeRootRowHeader</code> for the <code>&lt;div&gt;</code>
 * 	    above the title bar.  This may be used to make the title bar
 * 	    taller.</li>
 * 	<li><code>TreeRow</code> for the <code>&lt;div&gt;</code> around each
 * 	    tree node component.  This includes the tree node and all its child
 * 	    tree nodes.</li>
 * --	<li><code>TreeSelLink</code> </li> --
 * --	<li><code>TreeSelRow</code> </li> --
 * --	<li><code>TreeSelText</code> </li> --
 * 	</ul>
 * -->
 * 
 * 
 *     <h3>Client Side Javascript Functions</h3>
 * 
 *     <p>	The JavaScript functions listed in the following table are defined in a
 * 	file that is referenced automatically by the Tree component.  The
 * 	functions are called automatically in the rendered HTML.  You can also
 * 	call these functions independently; you may choose to do this to select
 * 	or expand/collapse a TreeNode on the client.</p>
 * 
 *     <ul><li style="clear: both">
 * 	    <div style="float:left; width: 275px; font-weight: bold;">
 * 		<code>clearHightlight(treeNode)</code></div>
 * 	    <div style="float:left; width: 30px;"><code>--</code></div>
 * 	    <div style="float:right; width: 65%;">
 * 		This function clears the highlighting for a particular
 * 		TreeNode.  The <code>treeNode</code> passed in should be the
 * 		&lt;div&gt; around the html for the <code>TreeNode</code>.  This
 * 		may be obtained by calling
 * 		<code>getElementById("&lt;TreeNode.getClidentId()&gt;")</code>.
 * 		</div></li>
 * 	<li style="clear: both">
 * 	    <div style="float:left; width: 275px; font-weight: bold;">
 * 		<code>expandCollapse(treeNode, event)</code></div>
 * 	    <div style="float:left; width: 30px;"><code>--</code></div>
 * 	    <div style="float:right; width: 65%;">
 * 		This function expands or collapses the given tree node.  It
 * 		expects the source of the given event object (if supplied) to
 * 		be a tree handle image.  It will change this image to point in
 * 		the correct direction (right or down).  This implementation
 * 		depends on the tree handle image names including
 * 		"tree_handleright" and "tree_handledown" in them.  Swapping
 * 		"right" and "down" in these names must change the handle
 * 		direction to right and down respectively.</div></li>
 * 	<li style="clear: both">
 * 	    <div style="float:left; width: 275px; font-weight: bold;">
 * 		<code>getParentTreeNode(treeId)</code></div>
 * 	    <div style="float:left; width: 30px;"><code>--</code></div>
 * 	    <div style="float:right; width: 65%;">
 * 		This function returns the parent TreeNode of the given
 * 		TreeNode.</div></li>
 * 	<li style="clear: both">
 * 	    <div style="float:left; width: 275px; font-weight: bold;">
 * 		<code>getSelectedTreeNode(treeId)</code></div>
 * 	    <div style="float:left; width: 30px;"><code>--</code></div>
 * 	    <div style="float:right; width: 65%;">
 * 		This function returns the selected TreeNode given the treeId of
 * 		the Tree.</div></li>
 * 	<li style="clear: both">
 * 	    <div style="float:left; width: 275px; font-weight: bold;">
 * 		<code>getTree(treeNode)</code></div>
 * 	    <div style="float:left; width: 30px;"><code>--</code></div>
 * 	    <div style="float:right; width: 65%;">
 * 		This function provides access to the Tree object containing the
 * 		given TreeNode.</div></li>
 * 	<li style="clear: both">
 * 	    <div style="float:left; width: 275px; font-weight: bold;">
 * 		<code>highlight(treeNode)</code></div>
 * 	    <div style="float:left; width: 30px;"><code>--</code></div>
 * 	    <div style="float:right; width: 65%;">
 * 		This function highlights the given TreeNode.  The
 * 		<code>treeNode</code> passed in is the &lt;div&gt; around
 * 		the html for the TreeNode and may be obtained by calling
 * 		<code>getElementById("&lt;TreeNode.getClidentId()&gt;")</code>.
 * 		</div></li>
 * 	<li style="clear: both">
 * 	    <div style="float:left; width: 275px; font-weight: bold;">
 * 		<code>highlightParent(treeNode)</code></div>
 * 	    <div style="float:left; width: 30px;"><code>--</code></div>
 * 	    <div style="float:right; width: 65%;">
 * 		This function highlights the parent TreeNode of the given
 * 		TreeNode.  It only does so if the given TreeNode is <b>not</b>
 * 		visible.  The parent is considered the first visible parent of
 * 		this TreeNode.  The <code>treeNode</code> passed in is the
 * 		&lt;div&gt; around the html for the child <code>TreeNode</code>
 * 		and may be obtained by calling
 * 		<code>getElementById("&lt;TreeNode.getClidentId()&gt;")</code>.
 * 		</div></li>
 * 	<li style="clear: both">
 * 	    <div style="float:left; width: 275px; font-weight: bold;">
 * 		<code>isAnHref(event)</code></div>
 * 	    <div style="float:left; width: 30px;"><code>--</code></div>
 * 	    <div style="float:right; width: 65%;">
 * 		This method checks to see if the event.target is an href, or if
 * 		any of the parent nodes which contain it is an href.  To
 * 		considered an href, it must be an "A" tag with an "href"
 * 		attribute containing atleast 4 characters.  (Note: Browsers will
 * 		add on the protocol if you supply a relative URL such as one
 * 		starting with a '#', '/', or filename).</div></li>
 * 	<li style="clear: both">
 * 	    <div style="float:left; width: 275px; font-weight: bold;">
 * 		<code>isTreeHandle(event)</code></div>
 * 	    <div style="float:left; width: 30px;"><code>--</code></div>
 * 	    <div style="float:right; width: 65%;">
 * 		This function determines if the event source was a tree handle
 * 		image.  This implementation depends on the tree handle image
 * 		file name containing "tree_handle" and no other images
 * 		containing this string.</div></li>
 * 	<li style="clear: both">
 * 	    <div style="float:left; width: 275px; font-weight: bold;">
 * 		<code>onTreeNodeClick(treeNode)</code></div>
 * 	    <div style="float:left; width: 30px;"><code>--</code></div>
 * 	    <div style="float:right; width: 65%;">
 * 		This function Takes in the TreeNode &lt;div&gt; object that was
 * 		clicked in order to process the highlighting changes that are
 * 		necessary.  This object may be obtained by calling
 * 		<code>getElementById("&lt;TreeNode.getClidentId()&gt;")</code>.
 * 		If this function is invoked from the TreeNode &lt;div&gt; object
 * 		itself (as is the case when this method is implicitly called),
 * 		the TreeNode object is simply the <code>this</code> variable.
 * 		</div></li>
 * 	<li style="clear: both">
 * 	    <div style="float:left; width: 275px; font-weight: bold;">
 * 		<code>selectTreeNode(treeNode)</code></div>
 * 	    <div style="float:left; width: 30px;"><code>--</code></div>
 * 	    <div style="float:right; width: 65%;">
 * 		This function may be used to select the given TreeNode.  It will
 * 		clear the previous TreeNode and select the given one.</div></li>
 * 	<li style="clear: both">
 * 	    <div style="float:left; width: 275px; font-weight: bold;">
 * 		<code>treeNodeIsExpanded(treeNode)</code></div>
 * 	    <div style="float:left; width: 30px;"><code>--</code></div>
 * 	    <div style="float:right; width: 65%;">
 * 		This function determines if the given TreeNode is expanded.  It
 * 		returns <code>true</code> if it is, <code>false</code>
 * 		otherwise.</div></li>
 * 	<li style="clear: both">
 * 	    <div style="float:left; width: 275px; font-weight: bold;">
 * 		<code>unhighlightParent(treeNode)</code></div>
 * 	    <div style="float:left; width: 30px;"><code>--</code></div>
 * 	    <div style="float:right; width: 65%;">
 * 		This function un-applies parent highlighting to the parent
 * 		TreeNode of the given TreeNode.  It only does so if the given
 * 		TreeNode is <b>not</b> visible.  The parent is considered the
 * 		first visible parent of this TreeNode.  The
 * 		<code>treeNode</code> passed in is the &lt;div&gt; element
 * 		around the html for the TreeNode and may be obtained by calling
 * 		<code>getElementById("&lt;TreeNode.getClidentId()&gt;")</code>.
 * 		</div></li>
 * 	<li style="clear: both">
 * 	    <div style="float:left; width: 275px; font-weight: bold;">
 * 		<code>updateHightlight(id)</code></div>
 * 	    <div style="float:left; width: 30px;"><code>--</code></div>
 * 	    <div style="float:right; width: 65%;">
 * 		This function updates the highlighting for the given Tree client
 * 		id.  This function provides a way to restore the highlighting
 * 		when a Tree is reloaded in a window (necessary each page load).
 * 		</div></li></ul>
 *     <br style="clear: both" />
 * 
 *     <h3>Example:</h3>
 * 
 *     <p>	Below is an example showing how a tree may be defined in a JSP
 * 	page:</p>
 * 
 *     <p>
 * 	<code>
 * 	    <pre>
 * &lt;ui:tree id="MyTree" text="hi"&gt;
 *     &lt;f:facet name="image"&gt;
 * 	&lt;ui:image id="image" icon="TREE_SERVER" /&gt;
 *     &lt;/f:facet&gt;
 *     &lt;ui:treeNode id="Node0" text="About..." /&gt;
 *     &lt;ui:treeNode id="Node1" expanded="true" text="External URLs"&gt;
 * 	&lt;f:facet name="image"&gt;
 * 	    &lt;ui:image id="image" icon="TREE_SERVER" /&gt;
 * 	&lt;/f:facet&gt;
 * 	&lt;ui:treeNode id="Node1_1" text="Sun Microsystems, Inc." url="http://www.sun.com" target="external"&gt;
 * 	    &lt;f:facet name="image"&gt;
 * 		&lt;ui:image id="image" icon="TREE_STORAGE_MAJOR" /&gt;
 * 	    &lt;/f:facet&gt;
 * 	&lt;/ui:treeNode&gt;
 * 	&lt;ui:treeNode id="Node1_2" text="Search Engines"&gt;
 * 	    &lt;ui:treeNode id="Node1_2_1" text="Google" url="http://www.google.com" imageURL="../images/google.jpg" target="external" /&gt;
 * 	    &lt;ui:treeNode id="Node1_2_2" text="Yahoo!" url="http://www.yahoo.com" imageURL="../images/yahoo.jpg" target="external" /&gt;
 * 	    &lt;ui:treeNode id="Node1_2_3" text="Lycos" url="http://www.lycos.com" target="external"&gt;
 * 		&lt;f:facet name="image"&gt;
 * 		    &lt;ui:image id="image" url="http://ly.lygo.com/ly/srch/hp/dog_web_34x35.gif" height="16" width="16" /&gt;
 * 		&lt;/f:facet&gt;
 * 	    &lt;/ui:treeNode&gt;
 * 	&lt;/ui:treeNode&gt;
 *     &lt;/ui:treeNode&gt;
 * &lt;/ui:tree&gt;
 * 	    </pre>
 * 	</code>
 *     </p>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class TreeBase extends com.sun.rave.web.ui.component.TreeNode {

    /**
     * <p>Construct a new <code>TreeBase</code>.</p>
     */
    public TreeBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Tree");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Tree";
    }

    // clientSide
    private boolean clientSide = false;
    private boolean clientSide_set = false;

    /**
 * <p>Specifies if this <code>Tree</code> should run on the client, or if it
 * 	should interact with the server.  If it runs on the client, then
 * 	clicking on the tree icon to expand or collapse portions of the tree
 * 	will happen only on the client (browser).  Otherwise, it will make a
 * 	request to the server each time the tree is expanded or collapsed.</p>
     */
    public boolean isClientSide() {
        if (this.clientSide_set) {
            return this.clientSide;
        }
        ValueBinding _vb = getValueBinding("clientSide");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>Specifies if this <code>Tree</code> should run on the client, or if it
 * 	should interact with the server.  If it runs on the client, then
 * 	clicking on the tree icon to expand or collapse portions of the tree
 * 	will happen only on the client (browser).  Otherwise, it will make a
 * 	request to the server each time the tree is expanded or collapsed.</p>
     * @see #isClientSide()
     */
    public void setClientSide(boolean clientSide) {
        this.clientSide = clientSide;
        this.clientSide_set = true;
    }

    // expandOnSelect
    private boolean expandOnSelect = false;
    private boolean expandOnSelect_set = false;

    /**
 * <p>Flag indicating that folder / container nodes will automatically expand
 * 	when they are selected. This attribute is true by default. If you want a tree's container
 *         nodes to expand only when the handle icons are clicked, set expandOnSelect to false.</p>
     */
    public boolean isExpandOnSelect() {
        if (this.expandOnSelect_set) {
            return this.expandOnSelect;
        }
        ValueBinding _vb = getValueBinding("expandOnSelect");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return true;
    }

    /**
 * <p>Flag indicating that folder / container nodes will automatically expand
 * 	when they are selected. This attribute is true by default. If you want a tree's container
 *         nodes to expand only when the handle icons are clicked, set expandOnSelect to false.</p>
     * @see #isExpandOnSelect()
     */
    public void setExpandOnSelect(boolean expandOnSelect) {
        this.expandOnSelect = expandOnSelect;
        this.expandOnSelect_set = true;
    }

    // immediate
    private boolean immediate = false;
    private boolean immediate_set = false;

    /**
 * <p>Indicate that event handling for this component should be
 *          handled immediately (in Apply Request Values phase) rather than
 *          waiting until Invoke Application phase.</p>
     */
    public boolean isImmediate() {
        if (this.immediate_set) {
            return this.immediate;
        }
        ValueBinding _vb = getValueBinding("immediate");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>Indicate that event handling for this component should be
 *          handled immediately (in Apply Request Values phase) rather than
 *          waiting until Invoke Application phase.</p>
     * @see #isImmediate()
     */
    public void setImmediate(boolean immediate) {
        this.immediate = immediate;
        this.immediate_set = true;
    }

    // required
    private boolean required = false;
    private boolean required_set = false;

    /**
 * <p>Indicates that the user must select a value for this tree.</p>
     */
    public boolean isRequired() {
        if (this.required_set) {
            return this.required;
        }
        ValueBinding _vb = getValueBinding("required");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return false;
    }

    /**
 * <p>Indicates that the user must select a value for this tree.</p>
     * @see #isRequired()
     */
    public void setRequired(boolean required) {
        this.required = required;
        this.required_set = true;
    }

    // selected
    private String selected = null;

    /**
 * <p>Specifies the client id of the selected tree node.</p>
     */
    public String getSelected() {
        if (this.selected != null) {
            return this.selected;
        }
        ValueBinding _vb = getValueBinding("selected");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Specifies the client id of the selected tree node.</p>
     * @see #getSelected()
     */
    public void setSelected(String selected) {
        this.selected = selected;
    }

    // style
    private String style = null;

    /**
 * <p>CSS style(s) to be applied when this component is rendered.</p>
     */
    public String getStyle() {
        if (this.style != null) {
            return this.style;
        }
        ValueBinding _vb = getValueBinding("style");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>CSS style(s) to be applied when this component is rendered.</p>
     * @see #getStyle()
     */
    public void setStyle(String style) {
        this.style = style;
    }

    // styleClass
    private String styleClass = null;

    /**
 * <p>CSS style class(es) to be applied when this component is rendered.</p>
     */
    public String getStyleClass() {
        if (this.styleClass != null) {
            return this.styleClass;
        }
        ValueBinding _vb = getValueBinding("styleClass");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>CSS style class(es) to be applied when this component is rendered.</p>
     * @see #getStyleClass()
     */
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    // text
    private String text = null;

    /**
 * <p>The text displayed at root of the tree</p>
     */
    public String getText() {
        if (this.text != null) {
            return this.text;
        }
        ValueBinding _vb = getValueBinding("text");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The text displayed at root of the tree</p>
     * @see #getText()
     */
    public void setText(String text) {
        this.text = text;
    }

    // visible
    private boolean visible = false;
    private boolean visible_set = false;

    /**
 * <p>Use the visible attribute to indicate whether the component should be
 *     viewable by the user in the rendered HTML page. If set to false, the
 *     HTML code for the component is present in the page, but the component
 *     is hidden with style attributes. By default, visible is set to true, so
 *     HTML for the component HTML is included and visible to the user. If the
 *     component is not visible, it can still be processed on subsequent form
 *     submissions because the HTML is present.</p>
     */
    public boolean isVisible() {
        if (this.visible_set) {
            return this.visible;
        }
        ValueBinding _vb = getValueBinding("visible");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return false;
            } else {
                return ((Boolean) _result).booleanValue();
            }
        }
        return true;
    }

    /**
 * <p>Use the visible attribute to indicate whether the component should be
 *     viewable by the user in the rendered HTML page. If set to false, the
 *     HTML code for the component is present in the page, but the component
 *     is hidden with style attributes. By default, visible is set to true, so
 *     HTML for the component HTML is included and visible to the user. If the
 *     component is not visible, it can still be processed on subsequent form
 *     submissions because the HTML is present.</p>
     * @see #isVisible()
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
        this.visible_set = true;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.clientSide = ((Boolean) _values[1]).booleanValue();
        this.clientSide_set = ((Boolean) _values[2]).booleanValue();
        this.expandOnSelect = ((Boolean) _values[3]).booleanValue();
        this.expandOnSelect_set = ((Boolean) _values[4]).booleanValue();
        this.immediate = ((Boolean) _values[5]).booleanValue();
        this.immediate_set = ((Boolean) _values[6]).booleanValue();
        this.required = ((Boolean) _values[7]).booleanValue();
        this.required_set = ((Boolean) _values[8]).booleanValue();
        this.selected = (String) _values[9];
        this.style = (String) _values[10];
        this.styleClass = (String) _values[11];
        this.text = (String) _values[12];
        this.visible = ((Boolean) _values[13]).booleanValue();
        this.visible_set = ((Boolean) _values[14]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[15];
        _values[0] = super.saveState(_context);
        _values[1] = this.clientSide ? Boolean.TRUE : Boolean.FALSE;
        _values[2] = this.clientSide_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.expandOnSelect ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = this.expandOnSelect_set ? Boolean.TRUE : Boolean.FALSE;
        _values[5] = this.immediate ? Boolean.TRUE : Boolean.FALSE;
        _values[6] = this.immediate_set ? Boolean.TRUE : Boolean.FALSE;
        _values[7] = this.required ? Boolean.TRUE : Boolean.FALSE;
        _values[8] = this.required_set ? Boolean.TRUE : Boolean.FALSE;
        _values[9] = this.selected;
        _values[10] = this.style;
        _values[11] = this.styleClass;
        _values[12] = this.text;
        _values[13] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[14] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
