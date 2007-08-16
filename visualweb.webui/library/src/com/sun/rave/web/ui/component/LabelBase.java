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
 * <p>Use the <code>ui:label</code>
 *     tag to display a label for a component. To specify which component
 *     the label belongs to you can do one of the following:</p> 
 * <ul> 
 * <li>Place the component to be labeled in the body of
 *     <code>ui:label</code>; or </li>  
 * <li>Set the value of the <code>for</code> attribute of <code
 *     >ui:label</code> to the id of the component to be labeled.</li> 
 * </ul> 
 * <p>If the tags are adjacent on the page, it is simpler to place the
 *     component in the body of the <code>ui:label</code> tag. </p> 
 * 
 * <p>Note that many components in this library have <code>label</code> 
 *     and <code>labelLevel</code> attributes (and sometimes
 *     <code>labelOnTop</code>) which can be used instead of <code
 *     >ui:label</code>. The main reason
 *     to use <code>ui:label</code> is to specify the
 *     appearance of the label beyond what these attributes permit. 
 * </p>   
 * 
 * <h3>HTML Elements and Layout</h3>
 * 
 * <p> 
 * The <code>ui:label</code> tag results in a HTML <code>&lt;label&gt;</code>
 *     or <code>&lt;span&gt;</code> element. A <code>&lt;span&gt;</code>
 *     element is rendered if no labeled component is found. </p> 
 * 
 * <p> If the tag has a body, the body must consist of tags for
 *     JavaServer Faces components only. The components become children
 *     of the <code>Label</code> component, and are rendered after the
 *     <code>&lt;label&gt;</code> element.</p>  
 * 
 * <p>If the tag has a body but no <code>for</code> attribute, a
 *     <code>&lt;label&gt;</code> element is rendered. Its
 *     <code>for</code> attribute is set to the element ID of the first
 *     child component that is an <code>EditableValueHolder. </code> 
 * 
 * <h3>Theme Identifiers</h3>
 * 
 * <p>The Label renderer relies on the following theme classes:</p> 
 * 
 * <pre>
 * LblLev1Txt
 * LblLev2Txt 
 * LblLev3Txt 
 * LblRqdDiv
 * </pre>
 * 
 * <h3>Client Side Javascript Functions</h3>
 * 
 * <p>None.</p> 
 * 
 * <h3>Example</h3>
 * 
 * <h4>Example 1: <code>ui:label</code> with <code>for</code> attribute</h4>
 * 
 * <pre> 
 *       &lt;ui:label id="label1" text="Label 1:" for="field1" labelLevel="2"/&gt;
 *       &lt;ui:field id="field1" text="#{Bean.value}" type="text" trim="true"/&gt;
 * </pre> 
 * 
 * <h4>Example 2: <code>ui:label</code> with labeled component in tag body</h4>
 * 
 * <pre> 
 *       &lt;ui:label id="label2" text="Label 2:" labelLevel="2"/&gt;
 *           &lt;ui:field id="field2" text="#{Bean.value}" type="text" 
 *                     trim="true"/&gt;
 *       &lt;/ui:label&gt; 
 * </pre> 
 * 
 * <h4>Example 3: Using the label attribute instead of <code>ui:label</code></h4>
 * 
 * <pre> 
 *       &lt;ui:field id="field3" text="#{Bean.value}" type="text" 
 *                 trim="true" label="Label 3:" labelLevel="2"/&gt;
 * </pre>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class LabelBase extends javax.faces.component.UIOutput {

    /**
     * <p>Construct a new <code>LabelBase</code>.</p>
     */
    public LabelBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Label");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Label";
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

    // for
    private String _for = null;

    /**
 * <p>Use this attribute to specify the labeled component. The
 *       attribute is only relevant if the component to be labeled is not
 *       a child of the <code>ui:label</code> tag. The value of the
 *       attribute should be the id of the component relative to the
 *       label, not the DOM ID of the rendered HTML element.</p>
     */
    public String getFor() {
        return this._for;
    }

    /**
 * <p>Use this attribute to specify the labeled component. The
 *       attribute is only relevant if the component to be labeled is not
 *       a child of the <code>ui:label</code> tag. The value of the
 *       attribute should be the id of the component relative to the
 *       label, not the DOM ID of the rendered HTML element.</p>
     * @see #getFor()
     */
    public void setFor(String _for) {
        this._for = _for;
    }

    // hideIndicators
    private boolean hideIndicators = false;
    private boolean hideIndicators_set = false;

    /**
 * <p>Use the hideIndicators attribute to prevent display of the
 *       required and invalid icons with the label. When the required
 *       attribute on the component to be labeled is set to true, the
 *       required icon is displayed next to the label. If the user
 *       submits the page with an invalid value for the component, the
 *       invalid icon is displayed. This attribute is useful when the
 *       component has more than one label, and only one label should
 *       show the icons.</p>
     */
    public boolean isHideIndicators() {
        if (this.hideIndicators_set) {
            return this.hideIndicators;
        }
        ValueBinding _vb = getValueBinding("hideIndicators");
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
 * <p>Use the hideIndicators attribute to prevent display of the
 *       required and invalid icons with the label. When the required
 *       attribute on the component to be labeled is set to true, the
 *       required icon is displayed next to the label. If the user
 *       submits the page with an invalid value for the component, the
 *       invalid icon is displayed. This attribute is useful when the
 *       component has more than one label, and only one label should
 *       show the icons.</p>
     * @see #isHideIndicators()
     */
    public void setHideIndicators(boolean hideIndicators) {
        this.hideIndicators = hideIndicators;
        this.hideIndicators_set = true;
    }

    // labelLevel
    private int labelLevel = Integer.MIN_VALUE;
    private boolean labelLevel_set = false;

    /**
 * <p>Style level for this label, where lower values typically specify
 *         progressively larger font sizes, and/or bolder font weights.
 *         Valid values are 1, 2, and 3. The default label level is 2.  Any label
 *         level outside this range will result in no label level being added.</p>
     */
    public int getLabelLevel() {
        if (this.labelLevel_set) {
            return this.labelLevel;
        }
        ValueBinding _vb = getValueBinding("labelLevel");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) _result).intValue();
            }
        }
        return 2;
    }

    /**
 * <p>Style level for this label, where lower values typically specify
 *         progressively larger font sizes, and/or bolder font weights.
 *         Valid values are 1, 2, and 3. The default label level is 2.  Any label
 *         level outside this range will result in no label level being added.</p>
     * @see #getLabelLevel()
     */
    public void setLabelLevel(int labelLevel) {
        this.labelLevel = labelLevel;
        this.labelLevel_set = true;
    }

    // onClick
    private String onClick = null;

    /**
 * <p>Scripting code executed when a mouse click
 *       occurs over this component.</p>
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
 *       occurs over this component.</p>
     * @see #getOnClick()
     */
    public void setOnClick(String onClick) {
        this.onClick = onClick;
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

    // requiredIndicator
    private boolean requiredIndicator = false;
    private boolean requiredIndicator_set = false;

    /**
 * <p>Flag indicating that the labeled component should be marked as
 *         required. It is only relevant if the labeled component is not
 *         a child of the label tag. Set this flag to ensure that the 
 *       required icon shows up the first time the page is rendered.</p>
     */
    public boolean isRequiredIndicator() {
        if (this.requiredIndicator_set) {
            return this.requiredIndicator;
        }
        ValueBinding _vb = getValueBinding("requiredIndicator");
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
 * <p>Flag indicating that the labeled component should be marked as
 *         required. It is only relevant if the labeled component is not
 *         a child of the label tag. Set this flag to ensure that the 
 *       required icon shows up the first time the page is rendered.</p>
     * @see #isRequiredIndicator()
     */
    public void setRequiredIndicator(boolean requiredIndicator) {
        this.requiredIndicator = requiredIndicator;
        this.requiredIndicator_set = true;
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
 * <p>The label text to be displayed for this label. This attribute
 *       can be set to a literal string, to a value binding expression
 *       that corresponds to a property of a managed bean, or to a value
 *       binding expression that corresponds to a message from a resource
 *       bundle declared using <code>f:loadBundle</code>.</p>
     */
    public Object getText() {
        return getValue();
    }

    /**
 * <p>The label text to be displayed for this label. This attribute
 *       can be set to a literal string, to a value binding expression
 *       that corresponds to a property of a managed bean, or to a value
 *       binding expression that corresponds to a message from a resource
 *       bundle declared using <code>f:loadBundle</code>.</p>
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
        this._for = (String) _values[1];
        this.hideIndicators = ((Boolean) _values[2]).booleanValue();
        this.hideIndicators_set = ((Boolean) _values[3]).booleanValue();
        this.labelLevel = ((Integer) _values[4]).intValue();
        this.labelLevel_set = ((Boolean) _values[5]).booleanValue();
        this.onClick = (String) _values[6];
        this.onMouseDown = (String) _values[7];
        this.onMouseMove = (String) _values[8];
        this.onMouseOut = (String) _values[9];
        this.onMouseOver = (String) _values[10];
        this.onMouseUp = (String) _values[11];
        this.requiredIndicator = ((Boolean) _values[12]).booleanValue();
        this.requiredIndicator_set = ((Boolean) _values[13]).booleanValue();
        this.style = (String) _values[14];
        this.styleClass = (String) _values[15];
        this.toolTip = (String) _values[16];
        this.visible = ((Boolean) _values[17]).booleanValue();
        this.visible_set = ((Boolean) _values[18]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[19];
        _values[0] = super.saveState(_context);
        _values[1] = this._for;
        _values[2] = this.hideIndicators ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.hideIndicators_set ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = new Integer(this.labelLevel);
        _values[5] = this.labelLevel_set ? Boolean.TRUE : Boolean.FALSE;
        _values[6] = this.onClick;
        _values[7] = this.onMouseDown;
        _values[8] = this.onMouseMove;
        _values[9] = this.onMouseOut;
        _values[10] = this.onMouseOver;
        _values[11] = this.onMouseUp;
        _values[12] = this.requiredIndicator ? Boolean.TRUE : Boolean.FALSE;
        _values[13] = this.requiredIndicator_set ? Boolean.TRUE : Boolean.FALSE;
        _values[14] = this.style;
        _values[15] = this.styleClass;
        _values[16] = this.toolTip;
        _values[17] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[18] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
