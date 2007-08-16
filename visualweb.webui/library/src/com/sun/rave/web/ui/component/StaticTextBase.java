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
 * <span style="color: rgb(51, 51, 255);"><span
 * style="color: rgb(0, 0, 0);">Use the </span><code
 * style="color: rgb(0, 0, 0);">ui:staticText</code><span
 * style="color: rgb(0, 0, 0);">
 * tag to display text that is not interactive in the rendered HTML
 * page.&nbsp; The text can be plain static text, or be formatted using
 * parameters to insert variable text in the rendered HTML. The JSF core
 * tag </span><code style="color: rgb(0, 0, 0);">f:param</code><span
 * style="color: rgb(0, 0, 0);"> can be used along with view beans to
 * provide
 * the variable data.</span><br>
 * </span></p>
 * <p><code></code>If there are one or more
 * params, the component will convert the list of parameter values to an <code>Object</code>
 * array, and call <code>MessageFormat.format()</code>, passing the value
 * of the param
 * of this component as the first argument, the value of the
 * array of parameter
 * values as the second argument, and render the result. See <code>MessageFormat.format()</code>for
 * details.<code> </code>Otherwise, render
 * the text of
 * this component unmodified.</p>
 * <span style="color: rgb(255, 153, 0);"></span>
 * <h3>HTML Elements and Layout</h3>
 * <span style="color: rgb(0, 0, 0);">The rendered HTML page includes
 * a </span><code style="color: rgb(0, 0, 0);">&lt;span&gt;</code><span
 * style="color: rgb(0, 0, 0);"> element that contains the resulting
 * text.&nbsp; In the </span><code style="color: rgb(0, 0, 0);">&lt;span&gt;</code><span
 * style="color: rgb(0, 0, 0);"> element, the class and style
 * attribute values are set to the values specified with the </span><code
 * style="color: rgb(0, 0, 0);">ui:staticText</code><span
 * style="color: rgb(0, 0, 0);"> tag's styleclass and style
 * attributes.</span>
 * <h3>Client Side Javascript Functions</h3>
 * None.
 * <br>
 * <h3>Examples</h3>
 * <h4>Example 1: Render plain old text<br>
 * </h4>
 * <div style="margin-left: 40px;"><code>&lt;ui:staticText
 * id="statictext1" text="#{bean.someText}" /&gt; </code><br>
 * <code></code><code>&lt;ui:staticText id="statictext1" text="some text
 * to display" /&gt;</code><code></code><br>
 * <code></code></div>
 * <h4>Example 2: Use Params to format a whole line<br>
 * </h4>
 * <code>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;ui:staticText id="blah"
 * text="At {1,time} on {1,date}, there was {2} on planet
 * {0,number,integer}."&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;f:param
 * id="b1" value="#{HyperlinkBean.myInt}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;f:param
 * id="b2" value="#{HyperlinkBean.date}"/&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;f:param
 * id="b3" value="2
 * fools"/&gt;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &lt;/ui:staticText&gt;<br>
 * <br>
 * </code>The above will render:&nbsp; <span id="form1:blah">"At 8:36:18
 * AM on Dec 13, 2004, there was 2 fools on planet 7." <br>
 * </span>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class StaticTextBase extends javax.faces.component.UIOutput {

    /**
     * <p>Construct a new <code>StaticTextBase</code>.</p>
     */
    public StaticTextBase() {
        super();
        setRendererType("com.sun.rave.web.ui.StaticText");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.StaticText";
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

    // escape
    private boolean escape = false;
    private boolean escape_set = false;

    /**
 * <p>Escape the text so it won't be interpreted by the browser as HTML markup</p>
     */
    public boolean isEscape() {
        if (this.escape_set) {
            return this.escape;
        }
        ValueBinding _vb = getValueBinding("escape");
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
 * <p>Escape the text so it won't be interpreted by the browser as HTML markup</p>
     * @see #isEscape()
     */
    public void setEscape(boolean escape) {
        this.escape = escape;
        this.escape_set = true;
    }

    // onClick
    private String onClick = null;

    /**
 * <p>Scripting code executed when a mouse click
 *     occurs over this component.</p>
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
 * <p>Scripting code executed when a mouse click
 *     occurs over this component.</p>
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
    /**
 * <p>The text value of this component</p>
     */
    public Object getText() {
        return getValue();
    }

    /**
 * <p>The text value of this component</p>
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
        this.escape = ((Boolean) _values[1]).booleanValue();
        this.escape_set = ((Boolean) _values[2]).booleanValue();
        this.onClick = (String) _values[3];
        this.onDblClick = (String) _values[4];
        this.onMouseDown = (String) _values[5];
        this.onMouseMove = (String) _values[6];
        this.onMouseOut = (String) _values[7];
        this.onMouseOver = (String) _values[8];
        this.onMouseUp = (String) _values[9];
        this.style = (String) _values[10];
        this.styleClass = (String) _values[11];
        this.toolTip = (String) _values[12];
        this.visible = ((Boolean) _values[13]).booleanValue();
        this.visible_set = ((Boolean) _values[14]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[15];
        _values[0] = super.saveState(_context);
        _values[1] = this.escape ? Boolean.TRUE : Boolean.FALSE;
        _values[2] = this.escape_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.onClick;
        _values[4] = this.onDblClick;
        _values[5] = this.onMouseDown;
        _values[6] = this.onMouseMove;
        _values[7] = this.onMouseOut;
        _values[8] = this.onMouseOver;
        _values[9] = this.onMouseUp;
        _values[10] = this.style;
        _values[11] = this.styleClass;
        _values[12] = this.toolTip;
        _values[13] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[14] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
