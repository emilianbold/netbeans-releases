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
 * Use the ui:button tag to display
 * an input button in the rendered HTML page. The input button submits the
 * associated form when activated by the user. The
 * corresponding ActionEvent events then occur on the server. <br
 *  style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">HTML
 * Elements and Layout</h3>
 * <span
 *  style="text-decoration: line-through; color: rgb(0, 0, 0);"></span><span
 *  style="color: rgb(0, 0, 0);">This tag uses the XHTML
 * &lt;input&gt; element to create a button with a
 * text label.</span><br
 *  style="color: rgb(0, 0, 0);">
 * <h3 style="color: rgb(0, 0, 0);">Client
 * Side Javascript Functions</h3>
 * The button component supports a client side javascript
 * object.&nbsp; To use this object all you need to do is get the
 * object by using document.getElementById() function to get the
 * object.&nbsp; It is recommended though you pass the result of the
 * getElementById() function to a local variable rather than accessing the
 * functions directly.&nbsp; The object supports the following
 * functions:
 * <ul>
 *   <li style="color: rgb(0, 0, 0);"><span
 *  style="font-style: italic;">isMini()</span>:
 * Test if button style is "mini" for
 * the button.&nbsp; Returns a boolean.</li>
 *   <li style="color: rgb(0, 0, 0);"><span
 *  style="font-style: italic;">isPrimary(): </span>Test
 * if button style is "primary" for
 * the button.&nbsp; Returns a boolean.</li>
 *   <li style="color: rgb(0, 0, 0);"><span
 *  style="font-style: italic;">isSecondary()</span>:
 *     <span style="font-style: italic;">DEPRECATED
 * use isPrimary instead </span>Test
 * if button style is "secondary" for
 * the button.&nbsp; Returns a boolean.<br>
 *   </li>
 *   <li style="color: rgb(0, 0, 0);"><span
 *  style="font-style: italic;">setMini(mini)</span>:
 * Set button style&nbsp;<span
 *  style="text-decoration: line-through;"></span>to&nbsp;"mini"
 * for the button. If
 * the mini <span
 *  style="text-decoration: line-through;"></span>attribute
 * is set to true,
 * the
 * element is shown with mini styles.</li>
 *   <li style="color: rgb(0, 0, 0);"><span
 *  style="font-style: italic;">setPrimary(primary)</span>:
 * Set button style<span
 *  style="text-decoration: line-through;"></span>
 * to "primary" for the
 * given button. If the primary <span
 *  style="text-decoration: line-through;"></span>attribute
 * is set to
 * true, the element is shown with primary style.</li>
 *   <li style="color: rgb(0, 0, 0);"><span
 *  style="font-style: italic;">setSecondary(secondary)</span>:
 *     <span style="font-style: italic;">DEPRECATED
 * use setPrimary instead</span> Set
 * button style<span
 *  style="text-decoration: line-through;"></span>
 * to "secondary" for the
 * given button. If the secondary <span
 *  style="text-decoration: line-through;"></span>attribute
 * is set to
 * true, the element is shown with secondary styles.</li>
 * </ul>
 * There are two other common functions that are useful for a button.
 * <ul>
 *   <li style="color: rgb(0, 0, 0);"><span
 *  style="font-style: italic;">&nbsp;common_stripStyleClass(javascriptObj,
 * classToRemove)</span>: Use this
 * function to remove a style class from a any js object that supports the
 * class property.&nbsp; Takes a javascript object and the style class
 * string to remove.<br>
 *   </li>
 *   <li style="color: rgb(0, 0, 0);"><span
 *  style="font-style: italic;">&nbsp;common_addStyleClass(this,
 * newType)</span>: Use this function
 * to add a new style class to any js object that supports the class
 * property. Takes a javascript object and the style class string to add.<br>
 *     <span style="font-style: italic;"><br>
 *     <br>
 *     </span></li>
 *   <span style="font-style: italic;"></span>
 * </ul>
 * <h3>Examples</h3>
 * <h4>Example 1: Create a primary
 * button:</h4>
 * <code>&lt;ui:button
 * id="button1" text="#{ButtonBean.text}"
 * action="#{ButtonBean.success}" /&gt;<br>
 * </code>
 * <h4>Example 2: Create a primary
 * mini button:</h4>
 * <code>&lt;ui:button
 * id="button1" text="#{ButtonBean.text}"
 * action="#{ButtonBean.success}" mini="true" /&gt;</code><br>
 * <h4>Example 3: Create a secondary
 * button:</h4>
 * <code>&lt;ui:button
 * id="button1" text="#{ButtonBean.text}"
 * action="#{ButtonBean.success}" secondary="true" /&gt;<br>
 * </code>
 * <h4>Example 4: Create a secondary
 * mini button:</h4>
 * <code>&lt;ui:button
 * id="button1" text="#{ButtonBean.text}"
 * action="#{ButtonBean.success}" secondary="true" isMini="true" /&gt;</code><br>
 * <h4>Example 5: Create a reset
 * button:</h4>
 * <code>&lt;ui:button
 * id="button1"
 * text="#{ButtonBean.text}" action="#{ButtonBean.success}" reset="true"
 * /&gt;<br>
 * </code>
 * <h4>Example 6: Create an image
 * button:</h4>
 * <code>&lt;ui:button
 * id="button1" imageURL="#{ButtonBean.image}"
 * action="#{ButtonBean.success}" /&gt;</code>
 * <br>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class ButtonBase extends javax.faces.component.UICommand {

    /**
     * <p>Construct a new <code>ButtonBase</code>.</p>
     */
    public ButtonBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Button");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Button";
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

    // alt
    private String alt = null;

    /**
 * <p>Alternative text description used by screen reader tools</p>
     */
    public String getAlt() {
        if (this.alt != null) {
            return this.alt;
        }
        ValueBinding _vb = getValueBinding("alt");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Alternative text description used by screen reader tools</p>
     * @see #getAlt()
     */
    public void setAlt(String alt) {
        this.alt = alt;
    }

    // disabled
    private boolean disabled = false;
    private boolean disabled_set = false;

    /**
 * <p>Indicates that activation of this component by the user is not currently 
 *         permitted. In this component library, the disabled attribute also causes 
 *         the button to be renderered using a particular style.</p>
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
 * <p>Indicates that activation of this component by the user is not currently 
 *         permitted. In this component library, the disabled attribute also causes 
 *         the button to be renderered using a particular style.</p>
     * @see #isDisabled()
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        this.disabled_set = true;
    }

    // escape
    private boolean escape = false;
    private boolean escape_set = false;

    /**
 * <p>Escape HTML markup in the button text</p>
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
 * <p>Escape HTML markup in the button text</p>
     * @see #isEscape()
     */
    public void setEscape(boolean escape) {
        this.escape = escape;
        this.escape_set = true;
    }

    // imageURL
    private String imageURL = null;

    /**
 * <p>Resource path of an image to be displayed to create the visual 
 *          appearance of this button instead of the standard button image. Either 
 *          the "imageURL" or  "text" attributes must be specified.  When an 
 *          imageURL value is given, the button type is set to "image".</p>
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
 * <p>Resource path of an image to be displayed to create the visual 
 *          appearance of this button instead of the standard button image. Either 
 *          the "imageURL" or  "text" attributes must be specified.  When an 
 *          imageURL value is given, the button type is set to "image".</p>
     * @see #getImageURL()
     */
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    // mini
    private boolean mini = false;
    private boolean mini_set = false;

    /**
 * <p>Indicates that the button should be rendered using a different style 
 *          than normal buttons. If the value is set to true, the button shall 
 *          appear somewhat smaller than a normal button. Mini buttons are useful 
 *          in situations where a button applies to an individual field on the 
 *          page, rather than a section, table, or whole page.</p>
     */
    public boolean isMini() {
        if (this.mini_set) {
            return this.mini;
        }
        ValueBinding _vb = getValueBinding("mini");
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
 * <p>Indicates that the button should be rendered using a different style 
 *          than normal buttons. If the value is set to true, the button shall 
 *          appear somewhat smaller than a normal button. Mini buttons are useful 
 *          in situations where a button applies to an individual field on the 
 *          page, rather than a section, table, or whole page.</p>
     * @see #isMini()
     */
    public void setMini(boolean mini) {
        this.mini = mini;
        this.mini_set = true;
    }

    // noTextPadding
    private boolean noTextPadding = false;
    private boolean noTextPadding_set = false;

    /**
 * <p>Indicates that padding should not be applied to the button text. By 
 *         default, whitespace characters are padded to button text greater than 
 *         or equal to 4 characters in length. If the value is set to true, no 
 *         padding is applied.</p>
     */
    public boolean isNoTextPadding() {
        if (this.noTextPadding_set) {
            return this.noTextPadding;
        }
        ValueBinding _vb = getValueBinding("noTextPadding");
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
 * <p>Indicates that padding should not be applied to the button text. By 
 *         default, whitespace characters are padded to button text greater than 
 *         or equal to 4 characters in length. If the value is set to true, no 
 *         padding is applied.</p>
     * @see #isNoTextPadding()
     */
    public void setNoTextPadding(boolean noTextPadding) {
        this.noTextPadding = noTextPadding;
        this.noTextPadding_set = true;
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

    // primary
    private boolean primary = false;
    private boolean primary_set = false;

    /**
 * <p>Indicates that the button is the most commonly used button within a 
 *         group.</p>
     */
    public boolean isPrimary() {
        if (this.primary_set) {
            return this.primary;
        }
        ValueBinding _vb = getValueBinding("primary");
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
 * <p>Indicates that the button is the most commonly used button within a 
 *         group.</p>
     * @see #isPrimary()
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
        this.primary_set = true;
    }

    // reset
    private boolean reset = false;
    private boolean reset_set = false;

    /**
 * <p>Indicates that the button should be a HTML reset button. By default, 
 *         this value is false and the button is created as a submit button. If the
 *         value is set to true, no action listener will be invoked.</p>
     */
    public boolean isReset() {
        if (this.reset_set) {
            return this.reset;
        }
        ValueBinding _vb = getValueBinding("reset");
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
 * <p>Indicates that the button should be a HTML reset button. By default, 
 *         this value is false and the button is created as a submit button. If the
 *         value is set to true, no action listener will be invoked.</p>
     * @see #isReset()
     */
    public void setReset(boolean reset) {
        this.reset = reset;
        this.reset_set = true;
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

    // text
    /**
 * <p>Textual label used to create the visual appearance of this button. 
 *         Either the "imageURL" or "text" attributes must be specified.  When a 
 *         text value is given, the standard button image is used, with the 
 *         specified text displayed on the button.</p>
     */
    public Object getText() {
        return getValue();
    }

    /**
 * <p>Textual label used to create the visual appearance of this button. 
 *         Either the "imageURL" or "text" attributes must be specified.  When a 
 *         text value is given, the standard button image is used, with the 
 *         specified text displayed on the button.</p>
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
        this.action = (javax.faces.el.MethodBinding) restoreAttachedState(_context, _values[1]);
        this.actionListener = (javax.faces.el.MethodBinding) restoreAttachedState(_context, _values[2]);
        this.alt = (String) _values[3];
        this.disabled = ((Boolean) _values[4]).booleanValue();
        this.disabled_set = ((Boolean) _values[5]).booleanValue();
        this.escape = ((Boolean) _values[6]).booleanValue();
        this.escape_set = ((Boolean) _values[7]).booleanValue();
        this.imageURL = (String) _values[8];
        this.mini = ((Boolean) _values[9]).booleanValue();
        this.mini_set = ((Boolean) _values[10]).booleanValue();
        this.noTextPadding = ((Boolean) _values[11]).booleanValue();
        this.noTextPadding_set = ((Boolean) _values[12]).booleanValue();
        this.onBlur = (String) _values[13];
        this.onClick = (String) _values[14];
        this.onDblClick = (String) _values[15];
        this.onFocus = (String) _values[16];
        this.onKeyDown = (String) _values[17];
        this.onKeyPress = (String) _values[18];
        this.onKeyUp = (String) _values[19];
        this.onMouseDown = (String) _values[20];
        this.onMouseMove = (String) _values[21];
        this.onMouseOut = (String) _values[22];
        this.onMouseOver = (String) _values[23];
        this.onMouseUp = (String) _values[24];
        this.primary = ((Boolean) _values[25]).booleanValue();
        this.primary_set = ((Boolean) _values[26]).booleanValue();
        this.reset = ((Boolean) _values[27]).booleanValue();
        this.reset_set = ((Boolean) _values[28]).booleanValue();
        this.style = (String) _values[29];
        this.styleClass = (String) _values[30];
        this.tabIndex = ((Integer) _values[31]).intValue();
        this.tabIndex_set = ((Boolean) _values[32]).booleanValue();
        this.toolTip = (String) _values[33];
        this.visible = ((Boolean) _values[34]).booleanValue();
        this.visible_set = ((Boolean) _values[35]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[36];
        _values[0] = super.saveState(_context);
        _values[1] = saveAttachedState(_context, action);
        _values[2] = saveAttachedState(_context, actionListener);
        _values[3] = this.alt;
        _values[4] = this.disabled ? Boolean.TRUE : Boolean.FALSE;
        _values[5] = this.disabled_set ? Boolean.TRUE : Boolean.FALSE;
        _values[6] = this.escape ? Boolean.TRUE : Boolean.FALSE;
        _values[7] = this.escape_set ? Boolean.TRUE : Boolean.FALSE;
        _values[8] = this.imageURL;
        _values[9] = this.mini ? Boolean.TRUE : Boolean.FALSE;
        _values[10] = this.mini_set ? Boolean.TRUE : Boolean.FALSE;
        _values[11] = this.noTextPadding ? Boolean.TRUE : Boolean.FALSE;
        _values[12] = this.noTextPadding_set ? Boolean.TRUE : Boolean.FALSE;
        _values[13] = this.onBlur;
        _values[14] = this.onClick;
        _values[15] = this.onDblClick;
        _values[16] = this.onFocus;
        _values[17] = this.onKeyDown;
        _values[18] = this.onKeyPress;
        _values[19] = this.onKeyUp;
        _values[20] = this.onMouseDown;
        _values[21] = this.onMouseMove;
        _values[22] = this.onMouseOut;
        _values[23] = this.onMouseOver;
        _values[24] = this.onMouseUp;
        _values[25] = this.primary ? Boolean.TRUE : Boolean.FALSE;
        _values[26] = this.primary_set ? Boolean.TRUE : Boolean.FALSE;
        _values[27] = this.reset ? Boolean.TRUE : Boolean.FALSE;
        _values[28] = this.reset_set ? Boolean.TRUE : Boolean.FALSE;
        _values[29] = this.style;
        _values[30] = this.styleClass;
        _values[31] = new Integer(this.tabIndex);
        _values[32] = this.tabIndex_set ? Boolean.TRUE : Boolean.FALSE;
        _values[33] = this.toolTip;
        _values[34] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[35] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
