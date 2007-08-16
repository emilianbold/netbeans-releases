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
 * Represents an input field whose content will be included
 *       when the surrounding form is submitted.
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class FieldBase extends com.sun.rave.web.ui.component.HiddenField {

    /**
     * <p>Construct a new <code>FieldBase</code>.</p>
     */
    public FieldBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Field");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Field";
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

    // columns
    private int columns = Integer.MIN_VALUE;
    private boolean columns_set = false;

    /**
 * <p>Number of character columns used to render this field.</p>
     */
    public int getColumns() {
        if (this.columns_set) {
            return this.columns;
        }
        ValueBinding _vb = getValueBinding("columns");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) _result).intValue();
            }
        }
        return 20;
    }

    /**
 * <p>Number of character columns used to render this field.</p>
     * @see #getColumns()
     */
    public void setColumns(int columns) {
        this.columns = columns;
        this.columns_set = true;
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

    // label
    private String label = null;

    /**
 * <p>If set, a label is rendered adjacent to the component with the
 *         value of this attribute as the label text.</p>
     */
    public String getLabel() {
        if (this.label != null) {
            return this.label;
        }
        ValueBinding _vb = getValueBinding("label");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>If set, a label is rendered adjacent to the component with the
 *         value of this attribute as the label text.</p>
     * @see #getLabel()
     */
    public void setLabel(String label) {
        this.label = label;
    }

    // labelLevel
    private int labelLevel = Integer.MIN_VALUE;
    private boolean labelLevel_set = false;

    /**
 * <p>Sets the style level for the generated label, provided the
 *         label attribute has been set. Valid values are 1 (largest), 2 and
 *         3 (smallest). The default value is 2.</p>
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
 * <p>Sets the style level for the generated label, provided the
 *         label attribute has been set. Valid values are 1 (largest), 2 and
 *         3 (smallest). The default value is 2.</p>
     * @see #getLabelLevel()
     */
    public void setLabelLevel(int labelLevel) {
        this.labelLevel = labelLevel;
        this.labelLevel_set = true;
    }

    // maxLength
    private int maxLength = Integer.MIN_VALUE;
    private boolean maxLength_set = false;

    /**
 * <p>The maximum number of characters that can be entered for this field.</p>
     */
    public int getMaxLength() {
        if (this.maxLength_set) {
            return this.maxLength;
        }
        ValueBinding _vb = getValueBinding("maxLength");
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
 * <p>The maximum number of characters that can be entered for this field.</p>
     * @see #getMaxLength()
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        this.maxLength_set = true;
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

    // onChange
    private String onChange = null;

    /**
 * <p>Scripting code executed when the element
 *     value of this component is changed.</p>
     */
    public String getOnChange() {
        if (this.onChange != null) {
            return this.onChange;
        }
        ValueBinding _vb = getValueBinding("onChange");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when the element
 *     value of this component is changed.</p>
     * @see #getOnChange()
     */
    public void setOnChange(String onChange) {
        this.onChange = onChange;
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

    // onSelect
    private String onSelect = null;

    /**
 * <p>Scripting code executed when some text in this
 *     component value is selected.</p>
     */
    public String getOnSelect() {
        if (this.onSelect != null) {
            return this.onSelect;
        }
        ValueBinding _vb = getValueBinding("onSelect");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>Scripting code executed when some text in this
 *     component value is selected.</p>
     * @see #getOnSelect()
     */
    public void setOnSelect(String onSelect) {
        this.onSelect = onSelect;
    }

    // readOnly
    private boolean readOnly = false;
    private boolean readOnly_set = false;

    /**
 * <p>Flag indicating that modification of this component by the
 *         user is not currently permitted, but that it will be
 *         included when the form is submitted.</p>
     */
    public boolean isReadOnly() {
        if (this.readOnly_set) {
            return this.readOnly;
        }
        ValueBinding _vb = getValueBinding("readOnly");
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
 * <p>Flag indicating that modification of this component by the
 *         user is not currently permitted, but that it will be
 *         included when the form is submitted.</p>
     * @see #isReadOnly()
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        this.readOnly_set = true;
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
 * <p>Literal value to be rendered in this input field.
 *         If this property is specified by a value binding
 *         expression, the corresponding value will be updated
 *         if validation succeeds.</p>
     */
    public Object getText() {
        return getValue();
    }

    /**
 * <p>Literal value to be rendered in this input field.
 *         If this property is specified by a value binding
 *         expression, the corresponding value will be updated
 *         if validation succeeds.</p>
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

    // trim
    private boolean trim = false;
    private boolean trim_set = false;

    /**
 * <p>Flag indicating that any leading and trailing blanks will be
 *         trimmed prior to conversion to the destination data type.
 *         Default value is true.</p>
     */
    public boolean isTrim() {
        if (this.trim_set) {
            return this.trim;
        }
        ValueBinding _vb = getValueBinding("trim");
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
 * <p>Flag indicating that any leading and trailing blanks will be
 *         trimmed prior to conversion to the destination data type.
 *         Default value is true.</p>
     * @see #isTrim()
     */
    public void setTrim(boolean trim) {
        this.trim = trim;
        this.trim_set = true;
    }

    // visible
    private boolean visible = false;
    private boolean visible_set = false;

    /**
 * <p>Use the visible attribute to indicate whether the component should be 
 *     viewable by the user in the rendered HTML page.</p>
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
 *     viewable by the user in the rendered HTML page.</p>
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
        this.columns = ((Integer) _values[1]).intValue();
        this.columns_set = ((Boolean) _values[2]).booleanValue();
        this.disabled = ((Boolean) _values[3]).booleanValue();
        this.disabled_set = ((Boolean) _values[4]).booleanValue();
        this.label = (String) _values[5];
        this.labelLevel = ((Integer) _values[6]).intValue();
        this.labelLevel_set = ((Boolean) _values[7]).booleanValue();
        this.maxLength = ((Integer) _values[8]).intValue();
        this.maxLength_set = ((Boolean) _values[9]).booleanValue();
        this.onBlur = (String) _values[10];
        this.onChange = (String) _values[11];
        this.onClick = (String) _values[12];
        this.onDblClick = (String) _values[13];
        this.onFocus = (String) _values[14];
        this.onKeyDown = (String) _values[15];
        this.onKeyPress = (String) _values[16];
        this.onKeyUp = (String) _values[17];
        this.onMouseDown = (String) _values[18];
        this.onMouseMove = (String) _values[19];
        this.onMouseOut = (String) _values[20];
        this.onMouseOver = (String) _values[21];
        this.onMouseUp = (String) _values[22];
        this.onSelect = (String) _values[23];
        this.readOnly = ((Boolean) _values[24]).booleanValue();
        this.readOnly_set = ((Boolean) _values[25]).booleanValue();
        this.style = (String) _values[26];
        this.styleClass = (String) _values[27];
        this.tabIndex = ((Integer) _values[28]).intValue();
        this.tabIndex_set = ((Boolean) _values[29]).booleanValue();
        this.toolTip = (String) _values[30];
        this.trim = ((Boolean) _values[31]).booleanValue();
        this.trim_set = ((Boolean) _values[32]).booleanValue();
        this.visible = ((Boolean) _values[33]).booleanValue();
        this.visible_set = ((Boolean) _values[34]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[35];
        _values[0] = super.saveState(_context);
        _values[1] = new Integer(this.columns);
        _values[2] = this.columns_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.disabled ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = this.disabled_set ? Boolean.TRUE : Boolean.FALSE;
        _values[5] = this.label;
        _values[6] = new Integer(this.labelLevel);
        _values[7] = this.labelLevel_set ? Boolean.TRUE : Boolean.FALSE;
        _values[8] = new Integer(this.maxLength);
        _values[9] = this.maxLength_set ? Boolean.TRUE : Boolean.FALSE;
        _values[10] = this.onBlur;
        _values[11] = this.onChange;
        _values[12] = this.onClick;
        _values[13] = this.onDblClick;
        _values[14] = this.onFocus;
        _values[15] = this.onKeyDown;
        _values[16] = this.onKeyPress;
        _values[17] = this.onKeyUp;
        _values[18] = this.onMouseDown;
        _values[19] = this.onMouseMove;
        _values[20] = this.onMouseOut;
        _values[21] = this.onMouseOver;
        _values[22] = this.onMouseUp;
        _values[23] = this.onSelect;
        _values[24] = this.readOnly ? Boolean.TRUE : Boolean.FALSE;
        _values[25] = this.readOnly_set ? Boolean.TRUE : Boolean.FALSE;
        _values[26] = this.style;
        _values[27] = this.styleClass;
        _values[28] = new Integer(this.tabIndex);
        _values[29] = this.tabIndex_set ? Boolean.TRUE : Boolean.FALSE;
        _values[30] = this.toolTip;
        _values[31] = this.trim ? Boolean.TRUE : Boolean.FALSE;
        _values[32] = this.trim_set ? Boolean.TRUE : Boolean.FALSE;
        _values[33] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[34] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
