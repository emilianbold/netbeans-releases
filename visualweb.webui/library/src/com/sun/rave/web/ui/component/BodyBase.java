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
 * <span style="color: rgb(0, 0, 0);">Use the </span><code
 * style="color: rgb(0, 0, 0);">ui:body</code><span
 * style="color: rgb(51, 51, 255);"><span style="color: rgb(0, 0, 0);">
 * tag to create an HTML </span><code style="color: rgb(0, 0, 0);">&lt;body&gt;</code><span
 * style="color: rgb(0, 0, 0);">
 * tag and attributes in the rendered HTML page</span>.&nbsp; </span>The
 * ui:body tag should be used in conjunction with the <a
 * href="./head.html">ui:head</a> and the <a href="./page.html">ui:page</a>
 * tag.<br>
 * <h3>HTML Elements and Layout</h3>
 * <span style="color: rgb(0, 0, 0);">The </span><code
 * style="color: rgb(0, 0, 0);">ui:body</code><span
 * style="color: rgb(0, 0, 0);"> tag encloses the body content of the
 * JSP page, just as a </span><code style="color: rgb(0, 0, 0);">&lt;body&gt;</code><span
 * style="color: rgb(0, 0, 0);"> tag encloses the content of an HTML
 * page. The </span><code style="color: rgb(0, 0, 0);">ui:body</code><span
 * style="color: rgb(0, 0, 0);"> tag should be placed after the <code>ui:head</code>
 * tag
 * within the <code>ui:page</code>
 * tag. Attributes that are
 * specified with the <code>ui:body</code> tag are used to specify the
 * corresponding attributes in the rendered <code>&lt;body&gt;</code>
 * element.</span>
 * <h3>Client Side Javascript Functions</h3>
 * None.
 * <br>
 * <br>
 * <b>Example 1: Create a body tag<br>
 * <br>
 * </b><code>&lt;ui:page&gt;<br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:head title="body test" &gt;<br>
 * &nbsp; &nbsp; &nbsp; &nbsp;&nbsp; &lt;ui:meta httpHead="refresh"
 * content="5" /&gt;<br>
 * &nbsp; &nbsp; &lt;/ui:head&gt;&nbsp; <br>
 * &nbsp;&nbsp;&nbsp; &lt;ui:body id="bodytest"&gt;
 * <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ....your body content
 * ...<br>
 * &nbsp;&nbsp;&nbsp; &lt;/ui:body id="bodytest"&gt;<br>
 * &lt;/ui:page&gt;</code>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class BodyBase extends javax.faces.component.UIComponentBase {

    /**
     * <p>Construct a new <code>BodyBase</code>.</p>
     */
    public BodyBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Body");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Body";
    }

    // focus
    private String focus = null;

    /**
 * <p>Specify the ID of the component that should receive focus when
 *       the page is loaded. If the attribute is not set, or if the value
 *       is null, the component which submitted the page (if any)
 *       receives focus.</p>
     */
    public String getFocus() {
        if (this.focus != null) {
            return this.focus;
        }
        ValueBinding _vb = getValueBinding("focus");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Specify the ID of the component that should receive focus when
 *       the page is loaded. If the attribute is not set, or if the value
 *       is null, the component which submitted the page (if any)
 *       receives focus.</p>
     * @see #getFocus()
     */
    public void setFocus(String focus) {
        this.focus = focus;
    }

    // imageURL
    private String imageURL = null;

    /**
 * <p>Resource path of an image used to appear in the background</p>
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
 * <p>Resource path of an image used to appear in the background</p>
     * @see #getImageURL()
     */
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
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

    // onLoad
    private String onLoad = null;

    /**
 * <p>Scripting code executed when when this page is loaded in a browser.</p>
     */
    public String getOnLoad() {
        if (this.onLoad != null) {
            return this.onLoad;
        }
        ValueBinding _vb = getValueBinding("onLoad");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when when this page is loaded in a browser.</p>
     * @see #getOnLoad()
     */
    public void setOnLoad(String onLoad) {
        this.onLoad = onLoad;
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

    // onUnload
    private String onUnload = null;

    /**
 * <p>Scripting code executed when this page is unloaded from a browser as a user exits the page.</p>
     */
    public String getOnUnload() {
        if (this.onUnload != null) {
            return this.onUnload;
        }
        ValueBinding _vb = getValueBinding("onUnload");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when this page is unloaded from a browser as a user exits the page.</p>
     * @see #getOnUnload()
     */
    public void setOnUnload(String onUnload) {
        this.onUnload = onUnload;
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
        this.focus = (String) _values[1];
        this.imageURL = (String) _values[2];
        this.onBlur = (String) _values[3];
        this.onClick = (String) _values[4];
        this.onDblClick = (String) _values[5];
        this.onFocus = (String) _values[6];
        this.onKeyDown = (String) _values[7];
        this.onKeyPress = (String) _values[8];
        this.onKeyUp = (String) _values[9];
        this.onLoad = (String) _values[10];
        this.onMouseDown = (String) _values[11];
        this.onMouseMove = (String) _values[12];
        this.onMouseOut = (String) _values[13];
        this.onMouseOver = (String) _values[14];
        this.onMouseUp = (String) _values[15];
        this.onUnload = (String) _values[16];
        this.style = (String) _values[17];
        this.styleClass = (String) _values[18];
        this.visible = ((Boolean) _values[19]).booleanValue();
        this.visible_set = ((Boolean) _values[20]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[21];
        _values[0] = super.saveState(_context);
        _values[1] = this.focus;
        _values[2] = this.imageURL;
        _values[3] = this.onBlur;
        _values[4] = this.onClick;
        _values[5] = this.onDblClick;
        _values[6] = this.onFocus;
        _values[7] = this.onKeyDown;
        _values[8] = this.onKeyPress;
        _values[9] = this.onKeyUp;
        _values[10] = this.onLoad;
        _values[11] = this.onMouseDown;
        _values[12] = this.onMouseMove;
        _values[13] = this.onMouseOut;
        _values[14] = this.onMouseOver;
        _values[15] = this.onMouseUp;
        _values[16] = this.onUnload;
        _values[17] = this.style;
        _values[18] = this.styleClass;
        _values[19] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[20] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
