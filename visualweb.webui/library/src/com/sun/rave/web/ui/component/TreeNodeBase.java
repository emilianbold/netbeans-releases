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
 * <h3>HTML Elements and Layout</h3>
 * 
 *     <p> The <code>TreeNode</code> component is designed to be used as a child
 * 	of a <code>Tree</code> or another <code>TreeNode</code> component.
 * 	This allows <code>TreeNode</code>s to form a tree structure.  When the
 * 	tree structure is rendered, the <code>TreeNode</code> component
 * 	represents a "node" in the tree.  The <code>TreeNode</code> component
 * 	is responsible for rendering:</p>
 * 
 *     <ul><li>A row of the tree lines that connect to other tree nodes.</li>
 * 	<li>An expand / collapse <code>IconHyperlink</code> if there is atleast
 * 	    1 child <code>TreeNode</code> (this will show or hide its child
 * 	    <code>TreeNode</code>(s)).</li>
 * 	<li>An optional <code>ImageHyperlink</code> that pertains to the
 * 	    content of the tree node.</li>
 * 	<li>Text or a <code>Hyperlink</code> for the content of the
 * 	    <code>TreeNode</code>.</li></ul>
 * 
 *     <p>	Portions of the <code>TreeNode</code> may be overriden using facets.
 * 	The following facets are supported:</p>
 * 
 *     <ul><li><div style="float:left; width: 100px;"><code>image</code></div>
 * 	    <div style="float:left; width: 50px;"><code>--</code></div>
 * 	    Replaces the <code>ImageHyperlink</code> which pertains to the
 * 	    content of the <code>TreeNode</code>.  When used properties that
 * 	    pertain to the <code>ImageHyperlink</code> will not have any
 * 	    effect.</li>
 * 	<li><div style="float:left; width: 100px;"><code>content</code></div>
 * 	    <div style="float:left; width: 50px;"><code>--</code></div>
 * 	    Replaces the static text/<code>Hyperlink</code> area.  When used
 * 	    properties that pertain to this area will not have any
 * 	    effect.</li></ul>
 * 
 * 
 *     <h3>Client Side Javascript Functions</h3>
 * 
 *     <p> None (although the <code>Tree</code> component does contain JavaScript functions).</p>
 * 
 * 
 *     <h3>Example:</h3>
 * 
 *     <p> For an example, please see the documentation for the <code>Tree</code> Tag.</p>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class TreeNodeBase extends com.sun.rave.web.ui.component.TemplateComponentBase {

    /**
     * <p>Construct a new <code>TreeNodeBase</code>.</p>
     */
    public TreeNodeBase() {
        super();
        setRendererType("com.sun.rave.web.ui.TreeNode");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.TreeNode";
    }

    // action
    private javax.faces.el.MethodBinding action = null;

    /**
 * <p>	Specifies the <code>action</code> for the <code>Hyperlink</code> and
 * 	for the <code>HyperlinkImage</code> of this component.  The<code>Hyperlink</code> may alternately be defined via the
 * 	"<code>content</code>" facet, and the image via the
 * 	"<code>image</code>" facet.  See <code>Hyperlink</code> documentation
 * 	for more information on how to use <code>action</code>.</p><p>	This property will not apply to the facets when a facet is used.</p>
     */
    public javax.faces.el.MethodBinding getAction() {
        if (this.action != null) {
            return this.action;
        }
        ValueBinding _vb = getValueBinding("action");
        if (_vb != null) {
            return (javax.faces.el.MethodBinding) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>	Specifies the <code>action</code> for the <code>Hyperlink</code> and
 * 	for the <code>HyperlinkImage</code> of this component.  The<code>Hyperlink</code> may alternately be defined via the
 * 	"<code>content</code>" facet, and the image via the
 * 	"<code>image</code>" facet.  See <code>Hyperlink</code> documentation
 * 	for more information on how to use <code>action</code>.</p><p>	This property will not apply to the facets when a facet is used.</p>
     * @see #getAction()
     */
    public void setAction(javax.faces.el.MethodBinding action) {
        this.action = action;
    }

    // actionListener
    private javax.faces.el.MethodBinding actionListener = null;

    /**
 * <p>Method binding to a method that is invoked when this tree node is toggled
 *        open or close</p>
     */
    public javax.faces.el.MethodBinding getActionListener() {
        if (this.actionListener != null) {
            return this.actionListener;
        }
        ValueBinding _vb = getValueBinding("actionListener");
        if (_vb != null) {
            return (javax.faces.el.MethodBinding) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Method binding to a method that is invoked when this tree node is toggled
 *        open or close</p>
     * @see #getActionListener()
     */
    public void setActionListener(javax.faces.el.MethodBinding actionListener) {
        this.actionListener = actionListener;
    }

    // expanded
    private boolean expanded = false;
    private boolean expanded_set = false;

    /**
 * <p>	Specifies if this <code>TreeNode</code> will be expanded or collapsed,
 * 	in other words if its child <code>TreeNode</code>s should be
 * 	displayed.</p>
     */
    public boolean isExpanded() {
        if (this.expanded_set) {
            return this.expanded;
        }
        ValueBinding _vb = getValueBinding("expanded");
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
 * <p>	Specifies if this <code>TreeNode</code> will be expanded or collapsed,
 * 	in other words if its child <code>TreeNode</code>s should be
 * 	displayed.</p>
     * @see #isExpanded()
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        this.expanded_set = true;
    }

    // imageURL
    private String imageURL = null;

    /**
 * <p>	Specifies <code>imageURL</code> for the <code>ImageHyperlink</code>
 * 	of this component.  The image may alternately be defined
 * 	via the "<code>image</code>" facet.  The "<code>image</code>" facet may
 * 	be an <code>IconHyperlink</code> component to utilize<code>Theme</code> images.</p>  This property will not apply to the
 * 	facet when the facet used.
     */
    public String getImageURL() {
        if (this.imageURL != null) {
            return this.imageURL;
        }
        ValueBinding _vb = getValueBinding("imageURL");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>	Specifies <code>imageURL</code> for the <code>ImageHyperlink</code>
 * 	of this component.  The image may alternately be defined
 * 	via the "<code>image</code>" facet.  The "<code>image</code>" facet may
 * 	be an <code>IconHyperlink</code> component to utilize<code>Theme</code> images.</p>  This property will not apply to the
 * 	facet when the facet used.
     * @see #getImageURL()
     */
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
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

    // target
    private String target = null;

    /**
 * <p> Specifies the target for the <code>Hyperlink</code> and the<code>ImageHyperlink</code> of this component.  The<code>Hyperlink</code> may alternately be defined via the
 * 	"<code>content</code>" facet  of this tree node, and the image via the
 * 	"<code>image</code>" facet.</p><p>	This property will not apply to the facet when a facet is used.</p>
     */
    public String getTarget() {
        if (this.target != null) {
            return this.target;
        }
        ValueBinding _vb = getValueBinding("target");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p> Specifies the target for the <code>Hyperlink</code> and the<code>ImageHyperlink</code> of this component.  The<code>Hyperlink</code> may alternately be defined via the
 * 	"<code>content</code>" facet  of this tree node, and the image via the
 * 	"<code>image</code>" facet.</p><p>	This property will not apply to the facet when a facet is used.</p>
     * @see #getTarget()
     */
    public void setTarget(String target) {
        this.target = target;
    }

    // text
    private String text = null;

    /**
 * <p>	Specifies the <code>text</code> for this component.  If a<code>url</code> or <code>action</code> is also specified, these
 * 	properties will be used to create a <code>Hyperlink</code> as the
 * 	content of this component.  If neither the<code>action</code> or <code>url</code> properties are specified, this
 * 	property will be used to display static text as the content for this
 * 	component.</p><p>	Alternately, the "<code>content</code>" facet may be used to specify
 * 	the content for this component.  If this facet is used, this
 * 	property has no effect.</p>
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
 * <p>	Specifies the <code>text</code> for this component.  If a<code>url</code> or <code>action</code> is also specified, these
 * 	properties will be used to create a <code>Hyperlink</code> as the
 * 	content of this component.  If neither the<code>action</code> or <code>url</code> properties are specified, this
 * 	property will be used to display static text as the content for this
 * 	component.</p><p>	Alternately, the "<code>content</code>" facet may be used to specify
 * 	the content for this component.  If this facet is used, this
 * 	property has no effect.</p>
     * @see #getText()
     */
    public void setText(String text) {
        this.text = text;
    }

    // toolTip
    private String toolTip = null;

    /**
 * <p>Display the text as a tooltip for this component</p>
     */
    public String getToolTip() {
        if (this.toolTip != null) {
            return this.toolTip;
        }
        ValueBinding _vb = getValueBinding("toolTip");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Display the text as a tooltip for this component</p>
     * @see #getToolTip()
     */
    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    // url
    private String url = null;

    /**
 * <p>	Specifies the <code>url</code> for the <code>Hyperlink</code> and the<code>ImageHyperlink</code> for this component.  The<code>Hyperlink</code> may alternately be defined via the
 * 	"<code>content</code>" facet, and the image via the
 * 	"<code>image</code>" facet.</p><p>	This property will not apply to the facet when a facet is used.</p>
     */
    public String getUrl() {
        if (this.url != null) {
            return this.url;
        }
        ValueBinding _vb = getValueBinding("url");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>	Specifies the <code>url</code> for the <code>Hyperlink</code> and the<code>ImageHyperlink</code> for this component.  The<code>Hyperlink</code> may alternately be defined via the
 * 	"<code>content</code>" facet, and the image via the
 * 	"<code>image</code>" facet.</p><p>	This property will not apply to the facet when a facet is used.</p>
     * @see #getUrl()
     */
    public void setUrl(String url) {
        this.url = url;
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
        this.action = (javax.faces.el.MethodBinding) restoreAttachedState(_context, _values[1]);
        this.actionListener = (javax.faces.el.MethodBinding) restoreAttachedState(_context, _values[2]);
        this.expanded = ((Boolean) _values[3]).booleanValue();
        this.expanded_set = ((Boolean) _values[4]).booleanValue();
        this.imageURL = (String) _values[5];
        this.style = (String) _values[6];
        this.styleClass = (String) _values[7];
        this.target = (String) _values[8];
        this.text = (String) _values[9];
        this.toolTip = (String) _values[10];
        this.url = (String) _values[11];
        this.visible = ((Boolean) _values[12]).booleanValue();
        this.visible_set = ((Boolean) _values[13]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[14];
        _values[0] = super.saveState(_context);
        _values[1] = saveAttachedState(_context, action);
        _values[2] = saveAttachedState(_context, actionListener);
        _values[3] = this.expanded ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = this.expanded_set ? Boolean.TRUE : Boolean.FALSE;
        _values[5] = this.imageURL;
        _values[6] = this.style;
        _values[7] = this.styleClass;
        _values[8] = this.target;
        _values[9] = this.text;
        _values[10] = this.toolTip;
        _values[11] = this.url;
        _values[12] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[13] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
