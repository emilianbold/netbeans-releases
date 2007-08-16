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
 * <h4>About this tag</h4>
 * 
 *     <p>This tag renders an EditableList component. Use this component
 *     when web application users need to create and modify a list of
 *     strings. The application user can add new strings by typing them
 *     into the textfield and clicking the "Add" button, and remove them
 *     by selecting one or more items from the list and clicking the
 *     "Remove" button.</p>  
 * 
 *     <h4>Configuring the <code>ui:editableList</code> tag</h4>
 * 
 *     <p> Use the <code>list</code> attribute to bind the component
 *     to a model. The value must be an EL expression that corresponds to
 *     a managed bean or a property of a managed bean, and it must
 *     evaluate to an array of  <code>java.lang.String</code>. 
 *     </p> 
 * 
 *     <p>To set the label of the textfield, use the
 *     <code>fieldLabel</code> attribute. To set the label of the
 *     textfield, use the <code>listLabel</code> attribute. To validate
 *     new items, use the <code>fieldValidator</code> attribute; to
 *     validate the contents of the list once the user has finished
 *     adding and removing items, specify a <code>labelValidator</code>.</p> 
 * 
 *     <h4>Facets</h4>
 * 
 *     <ul>
 *     <li><code>fieldLabel</code>: use this facet to specify a custom 
 *     component for the textfield label.</li>
 *     <li><code>listLabel</code>: use this facet to specify a custom 
 *     component for the textfield label.</li>
 *     <li><code>field</code>: use this facet to specify a custom 
 *     component for the textfield.</li>
 *      <li><code>addButton</code>: use this facet to specify a custom 
 *     component for the add button.</li>
 *      <li><code>removeButton</code>: use this facet to specify a custom 
 *     component for the remove button.</li>
 *     <li><code>search</code>: use this facet to specify a custom 
 *     component for the search button. The component is rendered
 *     on the same row as the text field and the "Add" button, 
 *     after the "Add" button.</li>
 *     <li><code>readOnly</code>: use this facet to specify a custom 
 *     component for display a readonly version of the component.</li>
 *     <li><code>header</code>: use this facet to specify a header,
 *     rendered in a table row above the component.</li>
 *     <li><code>footer</code>: use this facet to specify a header,
 *     rendered in a table row below the component.</li>
 *     </ul>
 * 
 *     <h4>Client-side JavaScript functions</h4>
 * 
 *     <ul>
 *     <li>NONE yet</li> 
 *     </ul>
 * 
 *     <h4>Example</h4>
 * <pre>
 *         &lt;ui:editableList id="compid"
 *                          list="#{ListBean.list}" 
 *                          fieldLabel="#{msgs.textfield_label}"
 *                          listLabel="#{msgs.list_label}"
 *                          sorted="true" 
 * 			 searchButton="true"
 *                          fieldValidator="#{ListBean.validateNewItem}"
 *                          listValidator="#{ListBean.validateList}"/&gt;
 * 
 * </pre>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class EditableListBase extends javax.faces.component.UIInput {

    /**
     * <p>Construct a new <code>EditableListBase</code>.</p>
     */
    public EditableListBase() {
        super();
        setRendererType("com.sun.rave.web.ui.EditableList");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.EditableList";
    }

    /**
     * <p>Return the <code>ValueBinding</code> stored for the
     * specified name (if any), respecting any property aliases.</p>
     *
     * @param name Name of value binding to retrieve
     */
    public ValueBinding getValueBinding(String name) {
        if (name.equals("list")) {
            return super.getValueBinding("value");
        }
        if (name.equals("listValidator")) {
            return super.getValueBinding("validator");
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
        if (name.equals("list")) {
            super.setValueBinding("value", binding);
            return;
        }
        if (name.equals("listValidator")) {
            super.setValueBinding("validator", binding);
            return;
        }
        super.setValueBinding(name, binding);
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

    // fieldLabel
    private String fieldLabel = null;

    /**
 * <p>The label of the text field</p>
     */
    public String getFieldLabel() {
        if (this.fieldLabel != null) {
            return this.fieldLabel;
        }
        ValueBinding _vb = getValueBinding("fieldLabel");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The label of the text field</p>
     * @see #getFieldLabel()
     */
    public void setFieldLabel(String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    // fieldValidator
    private javax.faces.el.MethodBinding fieldValidator = null;

    /**
 * <p>
 *           A validator which will be applied to entries made into the
 * 	  textfield. Specify this to be the <code>validate()</code>
 * 	  method of a <code>javax.faces.validator.Validator</code>, or
 * 	  to another method with the same argument structure and
 * 	  exceptions.  </p>
     */
    public javax.faces.el.MethodBinding getFieldValidator() {
        if (this.fieldValidator != null) {
            return this.fieldValidator;
        }
        ValueBinding _vb = getValueBinding("fieldValidator");
        if (_vb != null) {
            return (javax.faces.el.MethodBinding) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>
 *           A validator which will be applied to entries made into the
 * 	  textfield. Specify this to be the <code>validate()</code>
 * 	  method of a <code>javax.faces.validator.Validator</code>, or
 * 	  to another method with the same argument structure and
 * 	  exceptions.  </p>
     * @see #getFieldValidator()
     */
    public void setFieldValidator(javax.faces.el.MethodBinding fieldValidator) {
        this.fieldValidator = fieldValidator;
    }

    // labelLevel
    private int labelLevel = Integer.MIN_VALUE;
    private boolean labelLevel_set = false;

    /**
 * <p>Sets the style level for the generated labels. Valid values
 * 	are 1 (largest), 2 and 3 (smallest). The default value is 2.</p>
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
 * <p>Sets the style level for the generated labels. Valid values
 * 	are 1 (largest), 2 and 3 (smallest). The default value is 2.</p>
     * @see #getLabelLevel()
     */
    public void setLabelLevel(int labelLevel) {
        this.labelLevel = labelLevel;
        this.labelLevel_set = true;
    }

    // list
    /**
 * <p>
 *       The object that represents the list. It must be a JavaServer
 *       Faces EL expression that evaluates to an array of Objects or to
 *       a <code>java.util.List</code>.</p>
     */
    public Object getList() {
        return getValue();
    }

    /**
 * <p>
 *       The object that represents the list. It must be a JavaServer
 *       Faces EL expression that evaluates to an array of Objects or to
 *       a <code>java.util.List</code>.</p>
     * @see #getList()
     */
    public void setList(Object list) {
        setValue(list);
    }

    // listLabel
    private String listLabel = null;

    /**
 * <p>The label of the list</p>
     */
    public String getListLabel() {
        if (this.listLabel != null) {
            return this.listLabel;
        }
        ValueBinding _vb = getValueBinding("listLabel");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>The label of the list</p>
     * @see #getListLabel()
     */
    public void setListLabel(String listLabel) {
        this.listLabel = listLabel;
    }

    // listOnTop
    private boolean listOnTop = false;
    private boolean listOnTop_set = false;

    /**
 * <p>Flag indicating whether the list should be on top of the
 * 	textfield or vice versa. By default, the textfield is on top.</p>
     */
    public boolean isListOnTop() {
        if (this.listOnTop_set) {
            return this.listOnTop;
        }
        ValueBinding _vb = getValueBinding("listOnTop");
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
 * <p>Flag indicating whether the list should be on top of the
 * 	textfield or vice versa. By default, the textfield is on top.</p>
     * @see #isListOnTop()
     */
    public void setListOnTop(boolean listOnTop) {
        this.listOnTop = listOnTop;
        this.listOnTop_set = true;
    }

    // listValidator
    /**
 * <p>
 *       A validator which will be applied to the contents of the list
 *       (e.g. to verify that the list has a minimum number of
 *       entries). Specify this to be the <code>validate()</code>
 * 	  method of a <code>javax.faces.validator.Validator</code>, or
 * 	  to another method with the same argument structure and
 * 	  exceptions.  </p>
     */
    public javax.faces.el.MethodBinding getListValidator() {
        return getValidator();
    }

    /**
 * <p>
 *       A validator which will be applied to the contents of the list
 *       (e.g. to verify that the list has a minimum number of
 *       entries). Specify this to be the <code>validate()</code>
 * 	  method of a <code>javax.faces.validator.Validator</code>, or
 * 	  to another method with the same argument structure and
 * 	  exceptions.  </p>
     * @see #getListValidator()
     */
    public void setListValidator(javax.faces.el.MethodBinding listValidator) {
        setValidator(listValidator);
    }

    // maxLength
    private int maxLength = Integer.MIN_VALUE;
    private boolean maxLength_set = false;

    /**
 * <p>The maximum number of characters allowed for each string in the list.</p>
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
        return 25;
    }

    /**
 * <p>The maximum number of characters allowed for each string in the list.</p>
     * @see #getMaxLength()
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        this.maxLength_set = true;
    }

    // multiple
    private boolean multiple = false;
    private boolean multiple_set = false;

    /**
 * <p>Flag indicating that the application user may make select
 * 	more than one option from the listbox .</p>
     */
    public boolean isMultiple() {
        if (this.multiple_set) {
            return this.multiple;
        }
        ValueBinding _vb = getValueBinding("multiple");
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
 * <p>Flag indicating that the application user may make select
 * 	more than one option from the listbox .</p>
     * @see #isMultiple()
     */
    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
        this.multiple_set = true;
    }

    // readOnly
    private boolean readOnly = false;
    private boolean readOnly_set = false;

    /**
 * <p>If this attribute is set to true, the value of the component is
 *       rendered as text, preceded by the label if one was defined.</p>
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
 * <p>If this attribute is set to true, the value of the component is
 *       rendered as text, preceded by the label if one was defined.</p>
     * @see #isReadOnly()
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        this.readOnly_set = true;
    }

    // rows
    private int rows = Integer.MIN_VALUE;
    private boolean rows_set = false;

    /**
 * <p>The number of items to display. The default value is 6.</p>
     */
    public int getRows() {
        if (this.rows_set) {
            return this.rows;
        }
        ValueBinding _vb = getValueBinding("rows");
        if (_vb != null) {
            Object _result = _vb.getValue(getFacesContext());
            if (_result == null) {
                return Integer.MIN_VALUE;
            } else {
                return ((Integer) _result).intValue();
            }
        }
        return 6;
    }

    /**
 * <p>The number of items to display. The default value is 6.</p>
     * @see #getRows()
     */
    public void setRows(int rows) {
        this.rows = rows;
        this.rows_set = true;
    }

    // sorted
    private boolean sorted = false;
    private boolean sorted_set = false;

    /**
 * <p>Set this attribute to true if the list items should be
 *       sorted. The sorting is performed using a Collator configured
 *       with the locale from the FacesContext.</p>
     */
    public boolean isSorted() {
        if (this.sorted_set) {
            return this.sorted;
        }
        ValueBinding _vb = getValueBinding("sorted");
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
 * <p>Set this attribute to true if the list items should be
 *       sorted. The sorting is performed using a Collator configured
 *       with the locale from the FacesContext.</p>
     * @see #isSorted()
     */
    public void setSorted(boolean sorted) {
        this.sorted = sorted;
        this.sorted_set = true;
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
        this.disabled = ((Boolean) _values[1]).booleanValue();
        this.disabled_set = ((Boolean) _values[2]).booleanValue();
        this.fieldLabel = (String) _values[3];
        this.fieldValidator = (javax.faces.el.MethodBinding) restoreAttachedState(_context, _values[4]);
        this.labelLevel = ((Integer) _values[5]).intValue();
        this.labelLevel_set = ((Boolean) _values[6]).booleanValue();
        this.listLabel = (String) _values[7];
        this.listOnTop = ((Boolean) _values[8]).booleanValue();
        this.listOnTop_set = ((Boolean) _values[9]).booleanValue();
        this.maxLength = ((Integer) _values[10]).intValue();
        this.maxLength_set = ((Boolean) _values[11]).booleanValue();
        this.multiple = ((Boolean) _values[12]).booleanValue();
        this.multiple_set = ((Boolean) _values[13]).booleanValue();
        this.readOnly = ((Boolean) _values[14]).booleanValue();
        this.readOnly_set = ((Boolean) _values[15]).booleanValue();
        this.rows = ((Integer) _values[16]).intValue();
        this.rows_set = ((Boolean) _values[17]).booleanValue();
        this.sorted = ((Boolean) _values[18]).booleanValue();
        this.sorted_set = ((Boolean) _values[19]).booleanValue();
        this.style = (String) _values[20];
        this.styleClass = (String) _values[21];
        this.tabIndex = ((Integer) _values[22]).intValue();
        this.tabIndex_set = ((Boolean) _values[23]).booleanValue();
        this.toolTip = (String) _values[24];
        this.visible = ((Boolean) _values[25]).booleanValue();
        this.visible_set = ((Boolean) _values[26]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[27];
        _values[0] = super.saveState(_context);
        _values[1] = this.disabled ? Boolean.TRUE : Boolean.FALSE;
        _values[2] = this.disabled_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.fieldLabel;
        _values[4] = saveAttachedState(_context, fieldValidator);
        _values[5] = new Integer(this.labelLevel);
        _values[6] = this.labelLevel_set ? Boolean.TRUE : Boolean.FALSE;
        _values[7] = this.listLabel;
        _values[8] = this.listOnTop ? Boolean.TRUE : Boolean.FALSE;
        _values[9] = this.listOnTop_set ? Boolean.TRUE : Boolean.FALSE;
        _values[10] = new Integer(this.maxLength);
        _values[11] = this.maxLength_set ? Boolean.TRUE : Boolean.FALSE;
        _values[12] = this.multiple ? Boolean.TRUE : Boolean.FALSE;
        _values[13] = this.multiple_set ? Boolean.TRUE : Boolean.FALSE;
        _values[14] = this.readOnly ? Boolean.TRUE : Boolean.FALSE;
        _values[15] = this.readOnly_set ? Boolean.TRUE : Boolean.FALSE;
        _values[16] = new Integer(this.rows);
        _values[17] = this.rows_set ? Boolean.TRUE : Boolean.FALSE;
        _values[18] = this.sorted ? Boolean.TRUE : Boolean.FALSE;
        _values[19] = this.sorted_set ? Boolean.TRUE : Boolean.FALSE;
        _values[20] = this.style;
        _values[21] = this.styleClass;
        _values[22] = new Integer(this.tabIndex);
        _values[23] = this.tabIndex_set ? Boolean.TRUE : Boolean.FALSE;
        _values[24] = this.toolTip;
        _values[25] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[26] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
