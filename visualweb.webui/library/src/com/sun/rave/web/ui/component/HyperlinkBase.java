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
 * Use the ui:hyperlink tag to display a HTML hyperlink to a URL, or a
 * link that submits a form. If the action attribute is used, the form is
 * submitted. If the url attribute is used, the link is a normal hyperlink
 * that sends the browser to a new location.
 * <br>
 * <p style="color: rgb(0, 0, 0);">The
 * <span style="color: rgb(51, 51, 255);"><code
 *  style="color: rgb(0, 0, 0);">ui:imageHyperlink</code><span
 *  style="color: rgb(0, 0, 0);"></span></span>
 * component
 * can be also be used to submit forms. If the action attribute is used,
 * the form is submitted. If the
 * url attribute is used, the link is a normal hyperlink that sends the
 * browser to a new location.</p>
 * 
 * <br>
 * <h3>HTML Elements and Layout</h3>
 * The rendered HTML page displays an XHTML <a>
 * element. If the link submits the
 * form the onclick will have some additional behavior that the user
 * should be
 * aware of. See the onclick attribute below.
 * <br>
 * </a>
 * <h3><a>Client
 * Side Javascript Functions</a></h3>
 * <a>None.
 * <br>
 * </a>
 * <h3><a>Examples</a></h3>
 * <h4><a>Example
 * 1: Create a hyperlink that submits the form:</a></h4>
 * <code><a>&lt;ui:hyperlink
 * id="hyperlinktest1"
 * text="#{HyperlinkBean.text}" action="#{HyperlinkBean.success}"
 * /&gt;
 * <br>
 * <br>
 * </a></code><a>Note:
 * id can be used as a unique identifier if the hyperlink is
 * programatically
 * addressed<br>
 * </a>
 * <h4><a>Example
 * 2: Create a hyperlink that goes to another page:</a></h4>
 * <code><a>&lt;ui:hyperlink
 * id="hyperlinktest2"
 * text="#{HyperlinkBean.text}" url="http://www.google.com" /&gt;
 * <br>
 * <br>
 * </a></code><a>Note:
 * url property being set signifies that this *will not*
 * submit to the server and will be a straight hyperlink
 * <i><br>
 * </i>
 * </a>
 * <h4><a>Example
 * 3: Using the body of a hyperlink to render text:</a></h4>
 * <code><a>&lt;ui:hyperlink
 * id="hyperlinktest3" url="http://www.sun.com"
 * &gt; &lt;ui:image url="myyahoo.gif"
 * /&gt;&lt;ui:staticText text="more
 * text" /&gt;
 * &lt;/ui:hyperlink&gt;
 * </a></code>
 * <h4><a>Example
 * 4: Using f:params to add additional request parameters when the
 * hyperlink action is invoked:</a></h4>
 * <code><a>&lt;ui:hyperlink&nbsp;
 * id="hyperlinktest1"&nbsp; text="#{HyperlinkBean.label}"
 * action="#{HyperlinkBean.success}" &gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;f:param name="testingParam1"
 * value="success!"/&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;f:param name="testingParam2"
 * value="failure!"/&gt;<br>
 * &lt;/ui:hyperlink&gt;<br>
 * <br>
 * </a></code><a>Note:&nbsp;
 * After clicking on this hyperlink, the page will be submitted and the
 * request parameter map will have 2 additional items in it: testingParam1
 * and testingParam2 with their associtated values<br>
 * </a><code><a></a></code>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class HyperlinkBase extends javax.faces.component.UICommand {

    /**
     * <p>Construct a new <code>HyperlinkBase</code>.</p>
     */
    public HyperlinkBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Hyperlink");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Hyperlink";
    }

    /**
     * <p>Return the <code>ValueBinding</code> stored for the
     * specified name (if any), respecting any property aliases.</p>
     *
     * @param name Name of value binding to retrieve
     */
    public ValueBinding getValueBinding(String name) {
        if (name.equals("text")) {
            return super.getValueBinding("value");
        }
        return super.getValueBinding(name);
    }

    /**
     * <p>Set the <code>ValueBinding</code> stored for the
     * specified name (if any), respecting any property
     * aliases.</p>
     *
     * @param name    Name of value binding to set
     * @param binding ValueBinding to set, or null to remove
     */
    public void setValueBinding(String name,ValueBinding binding) {
        if (name.equals("text")) {
            super.setValueBinding("value", binding);
            return;
        }
        super.setValueBinding(name, binding);
    }

    // action
    private javax.faces.el.MethodBinding action = null;

    /**
 * <p>Method binding representing a method that processes
 *         application actions from this component.</p>
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
 * <p>Method binding representing a method that processes
 *         application actions from this component.</p>
     * @see #getAction()
     */
    public void setAction(javax.faces.el.MethodBinding action) {
        this.action = action;
    }

    // actionListener
    private javax.faces.el.MethodBinding actionListener = null;

    /**
 * <p>Method binding representing a method that receives action from this, and possibly other, components.</p>
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
 * <p>Method binding representing a method that receives action from this, and possibly other, components.</p>
     * @see #getActionListener()
     */
    public void setActionListener(javax.faces.el.MethodBinding actionListener) {
        this.actionListener = actionListener;
    }

    // disabled
    private boolean disabled = false;
    private boolean disabled_set = false;

    /**
 * <p>Flag indicating that the user is not permitted to activate this
 *         component, and that the component's value will not be submitted with the
 *         form.</p>
     */
    public boolean isDisabled() {
        if (this.disabled_set) {
            return this.disabled;
        }
        ValueBinding _vb = getValueBinding("disabled");
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
 * <p>Flag indicating that the user is not permitted to activate this
 *         component, and that the component's value will not be submitted with the
 *         form.</p>
     * @see #isDisabled()
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        this.disabled_set = true;
    }

    // onBlur
    private String onBlur = null;

    /**
 * <p>Scripting code executed when this element loses focus.</p>
     */
    public String getOnBlur() {
        if (this.onBlur != null) {
            return this.onBlur;
        }
        ValueBinding _vb = getValueBinding("onBlur");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when this element loses focus.</p>
     * @see #getOnBlur()
     */
    public void setOnBlur(String onBlur) {
        this.onBlur = onBlur;
    }

    // onClick
    private String onClick = null;

    /**
 * <p>Scripting code executed when a mouse click occurs over this component.
 *           If the component submits the form, the script should not 
 *           return from this function. The return will be handled by the script 
 *           that is appended to the onclick. It is ok to return from this script 
 *           to abort the submit process if necessary.</p>
     */
    public String getOnClick() {
        if (this.onClick != null) {
            return this.onClick;
        }
        ValueBinding _vb = getValueBinding("onClick");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when a mouse click occurs over this component.
 *           If the component submits the form, the script should not 
 *           return from this function. The return will be handled by the script 
 *           that is appended to the onclick. It is ok to return from this script 
 *           to abort the submit process if necessary.</p>
     * @see #getOnClick()
     */
    public void setOnClick(String onClick) {
        this.onClick = onClick;
    }

    // onDblClick
    private String onDblClick = null;

    /**
 * <p>Scripting code executed when a mouse double click
 *     occurs over this component.</p>
     */
    public String getOnDblClick() {
        if (this.onDblClick != null) {
            return this.onDblClick;
        }
        ValueBinding _vb = getValueBinding("onDblClick");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when a mouse double click
 *     occurs over this component.</p>
     * @see #getOnDblClick()
     */
    public void setOnDblClick(String onDblClick) {
        this.onDblClick = onDblClick;
    }

    // onFocus
    private String onFocus = null;

    /**
 * <p>Scripting code executed when this component  receives focus. An
 *     element receives focus when the user selects the element by pressing
 *     the tab key or clicking the mouse.</p>
     */
    public String getOnFocus() {
        if (this.onFocus != null) {
            return this.onFocus;
        }
        ValueBinding _vb = getValueBinding("onFocus");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when this component  receives focus. An
 *     element receives focus when the user selects the element by pressing
 *     the tab key or clicking the mouse.</p>
     * @see #getOnFocus()
     */
    public void setOnFocus(String onFocus) {
        this.onFocus = onFocus;
    }

    // onKeyDown
    private String onKeyDown = null;

    /**
 * <p>Scripting code executed when the user presses down on a key while the
 *     component has focus.</p>
     */
    public String getOnKeyDown() {
        if (this.onKeyDown != null) {
            return this.onKeyDown;
        }
        ValueBinding _vb = getValueBinding("onKeyDown");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user presses down on a key while the
 *     component has focus.</p>
     * @see #getOnKeyDown()
     */
    public void setOnKeyDown(String onKeyDown) {
        this.onKeyDown = onKeyDown;
    }

    // onKeyPress
    private String onKeyPress = null;

    /**
 * <p>Scripting code executed when the user presses and releases a key while
 *     the component has focus.</p>
     */
    public String getOnKeyPress() {
        if (this.onKeyPress != null) {
            return this.onKeyPress;
        }
        ValueBinding _vb = getValueBinding("onKeyPress");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user presses and releases a key while
 *     the component has focus.</p>
     * @see #getOnKeyPress()
     */
    public void setOnKeyPress(String onKeyPress) {
        this.onKeyPress = onKeyPress;
    }

    // onKeyUp
    private String onKeyUp = null;

    /**
 * <p>Scripting code executed when the user releases a key while the
 *     component has focus.</p>
     */
    public String getOnKeyUp() {
        if (this.onKeyUp != null) {
            return this.onKeyUp;
        }
        ValueBinding _vb = getValueBinding("onKeyUp");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user releases a key while the
 *     component has focus.</p>
     * @see #getOnKeyUp()
     */
    public void setOnKeyUp(String onKeyUp) {
        this.onKeyUp = onKeyUp;
    }

    // onMouseDown
    private String onMouseDown = null;

    /**
 * <p>Scripting code executed when the user presses a mouse button while the
 *     mouse pointer is on the component.</p>
     */
    public String getOnMouseDown() {
        if (this.onMouseDown != null) {
            return this.onMouseDown;
        }
        ValueBinding _vb = getValueBinding("onMouseDown");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user presses a mouse button while the
 *     mouse pointer is on the component.</p>
     * @see #getOnMouseDown()
     */
    public void setOnMouseDown(String onMouseDown) {
        this.onMouseDown = onMouseDown;
    }

    // onMouseMove
    private String onMouseMove = null;

    /**
 * <p>Scripting code executed when the user moves the mouse pointer while
 *     over the component.</p>
     */
    public String getOnMouseMove() {
        if (this.onMouseMove != null) {
            return this.onMouseMove;
        }
        ValueBinding _vb = getValueBinding("onMouseMove");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user moves the mouse pointer while
 *     over the component.</p>
     * @see #getOnMouseMove()
     */
    public void setOnMouseMove(String onMouseMove) {
        this.onMouseMove = onMouseMove;
    }

    // onMouseOut
    private String onMouseOut = null;

    /**
 * <p>Scripting code executed when a mouse out movement
 *     occurs over this component.</p>
     */
    public String getOnMouseOut() {
        if (this.onMouseOut != null) {
            return this.onMouseOut;
        }
        ValueBinding _vb = getValueBinding("onMouseOut");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when a mouse out movement
 *     occurs over this component.</p>
     * @see #getOnMouseOut()
     */
    public void setOnMouseOut(String onMouseOut) {
        this.onMouseOut = onMouseOut;
    }

    // onMouseOver
    private String onMouseOver = null;

    /**
 * <p>Scripting code executed when the user moves the  mouse pointer into
 *     the boundary of this component.</p>
     */
    public String getOnMouseOver() {
        if (this.onMouseOver != null) {
            return this.onMouseOver;
        }
        ValueBinding _vb = getValueBinding("onMouseOver");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user moves the  mouse pointer into
 *     the boundary of this component.</p>
     * @see #getOnMouseOver()
     */
    public void setOnMouseOver(String onMouseOver) {
        this.onMouseOver = onMouseOver;
    }

    // onMouseUp
    private String onMouseUp = null;

    /**
 * <p>Scripting code executed when the user releases a mouse button while
 *     the mouse pointer is on the component.</p>
     */
    public String getOnMouseUp() {
        if (this.onMouseUp != null) {
            return this.onMouseUp;
        }
        ValueBinding _vb = getValueBinding("onMouseUp");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the user releases a mouse button while
 *     the mouse pointer is on the component.</p>
     * @see #getOnMouseUp()
     */
    public void setOnMouseUp(String onMouseUp) {
        this.onMouseUp = onMouseUp;
    }

    // shape
    private String shape = null;

    /**
 * <p>The shape of the hot spot on the screen (for use in client-side image 
 *         maps). Valid values are: default (entire region); rect (rectangular 
 *         region); circle (circular region); and poly (polygonal region).</p>
     */
    public String getShape() {
        if (this.shape != null) {
            return this.shape;
        }
        ValueBinding _vb = getValueBinding("shape");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The shape of the hot spot on the screen (for use in client-side image 
 *         maps). Valid values are: default (entire region); rect (rectangular 
 *         region); circle (circular region); and poly (polygonal region).</p>
     * @see #getShape()
     */
    public void setShape(String shape) {
        this.shape = shape;
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

    // tabIndex
    private int tabIndex = Integer.MIN_VALUE;
    private boolean tabIndex_set = false;

    /**
 * <p>The position of this component in the tabbing order sequence</p>
     */
    public int getTabIndex() {
        if (this.tabIndex_set) {
            return this.tabIndex;
        }
        ValueBinding _vb = getValueBinding("tabIndex");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) _result).intValue();
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
 * <p>The position of this component in the tabbing order sequence</p>
     * @see #getTabIndex()
     */
    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
        this.tabIndex_set = true;
    }

    // target
    private String target = null;

    /**
 * <p>The resource at the specified URL is displayed in the frame that is 
 *         specified with the target attribute. Values such as "_blank" that are 
 *         valid for the target attribute of a HTML anchor element are also valid 
 *         for this attribute in this component</p>
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
 * <p>The resource at the specified URL is displayed in the frame that is 
 *         specified with the target attribute. Values such as "_blank" that are 
 *         valid for the target attribute of a HTML anchor element are also valid 
 *         for this attribute in this component</p>
     * @see #getTarget()
     */
    public void setTarget(String target) {
        this.target = target;
    }

    // text
    /**
 * <p>The text description of the hyperlink</p>
     */
    public Object getText() {
        return getValue();
    }

    /**
 * <p>The text description of the hyperlink</p>
     * @see #getText()
     */
    public void setText(Object text) {
        setValue(text);
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

    // type
    private String type = null;

    /**
 * <p>The MIME content type of the resource specified by this component.</p>
     */
    public String getType() {
        if (this.type != null) {
            return this.type;
        }
        ValueBinding _vb = getValueBinding("type");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The MIME content type of the resource specified by this component.</p>
     * @see #getType()
     */
    public void setType(String type) {
        this.type = type;
    }

    // url
    private String url = null;

    /**
 * <p>Absolute, relative, or context relative (starting with "/") URL to the 
 *         resource selected by this hyperlink. If not specified, clicking this 
 *         hyperlink will submit the surrounding form.</p>
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
 * <p>Absolute, relative, or context relative (starting with "/") URL to the 
 *         resource selected by this hyperlink. If not specified, clicking this 
 *         hyperlink will submit the surrounding form.</p>
     * @see #getUrl()
     */
    public void setUrl(String url) {
        this.url = url;
    }

    // urlLang
    private String urlLang = null;

    /**
 * <p>The language code of the resource designated by this hyperlink.</p>
     */
    public String getUrlLang() {
        if (this.urlLang != null) {
            return this.urlLang;
        }
        ValueBinding _vb = getValueBinding("urlLang");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The language code of the resource designated by this hyperlink.</p>
     * @see #getUrlLang()
     */
    public void setUrlLang(String urlLang) {
        this.urlLang = urlLang;
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
        this.disabled = ((Boolean) _values[3]).booleanValue();
        this.disabled_set = ((Boolean) _values[4]).booleanValue();
        this.onBlur = (String) _values[5];
        this.onClick = (String) _values[6];
        this.onDblClick = (String) _values[7];
        this.onFocus = (String) _values[8];
        this.onKeyDown = (String) _values[9];
        this.onKeyPress = (String) _values[10];
        this.onKeyUp = (String) _values[11];
        this.onMouseDown = (String) _values[12];
        this.onMouseMove = (String) _values[13];
        this.onMouseOut = (String) _values[14];
        this.onMouseOver = (String) _values[15];
        this.onMouseUp = (String) _values[16];
        this.shape = (String) _values[17];
        this.style = (String) _values[18];
        this.styleClass = (String) _values[19];
        this.tabIndex = ((Integer) _values[20]).intValue();
        this.tabIndex_set = ((Boolean) _values[21]).booleanValue();
        this.target = (String) _values[22];
        this.toolTip = (String) _values[23];
        this.type = (String) _values[24];
        this.url = (String) _values[25];
        this.urlLang = (String) _values[26];
        this.visible = ((Boolean) _values[27]).booleanValue();
        this.visible_set = ((Boolean) _values[28]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[29];
        _values[0] = super.saveState(_context);
        _values[1] = saveAttachedState(_context, action);
        _values[2] = saveAttachedState(_context, actionListener);
        _values[3] = this.disabled ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = this.disabled_set ? Boolean.TRUE : Boolean.FALSE;
        _values[5] = this.onBlur;
        _values[6] = this.onClick;
        _values[7] = this.onDblClick;
        _values[8] = this.onFocus;
        _values[9] = this.onKeyDown;
        _values[10] = this.onKeyPress;
        _values[11] = this.onKeyUp;
        _values[12] = this.onMouseDown;
        _values[13] = this.onMouseMove;
        _values[14] = this.onMouseOut;
        _values[15] = this.onMouseOver;
        _values[16] = this.onMouseUp;
        _values[17] = this.shape;
        _values[18] = this.style;
        _values[19] = this.styleClass;
        _values[20] = new Integer(this.tabIndex);
        _values[21] = this.tabIndex_set ? Boolean.TRUE : Boolean.FALSE;
        _values[22] = this.target;
        _values[23] = this.toolTip;
        _values[24] = this.type;
        _values[25] = this.url;
        _values[26] = this.urlLang;
        _values[27] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[28] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
