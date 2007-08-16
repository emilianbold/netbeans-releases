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
 * <p> Use the <code>ui:orderableList</code> tag to create a list with
 *     buttons that allow the items on the list to be reordered.</p> 
 * 
 *     <h3>HTML Elements and Layout</h3>
 * 
 * <p>The textField component renders an XHTML fragment consisting of a
 * <code>select</code>element representing a list, an <code>input
 * type="hidden"</code> element which represents the current order of the
 * list items, two or four <code>input type="submit"</code> elements for the
 * buttons, and a <code>label</code> element for the label. The buttons are
 * organized using a <code>table</code>, and the whole component is laid
 * out using <code>div</code>s.  </p>
 * 
 *     <h3>Configuring the <code>ui:orderableList</code> Tag</h3>
 * 
 * 
 *     <p>Use the <code>list</code> attribute to associate the component
 *     with a set of orderable items. The value of the list attribute
 *     must be a JavaServer Faces EL expression that evaluates to an
 *     array of Objects or to a <code>java.util.List</code>.</p>
 * 
 *     <p>To allow users to select more than one item to move at a time, 
 *     set <code>multiple</code> to true. To display buttons which moves
 *     the selected items to the top and bottom of the list, set 
 *     <code>moveTopBottom</code> to true.</code> 
 * 
 *     <p>To specify a label for the component, use the
 *     <code>label</code> attribute, or specify a label facet. To place
 *     the label above the component, set <code>labelOnTop</code> to
 *     true.</p> 
 * 
 *     <h3>Facets</h3>
 * 
 *     <ul>
 *     <li><code>label</code>: use this facet to specify a custom 
 *     component for the label.</li>
 *     <li><code>readOnly</code>: use this facet to specify a custom 
 *     component for displaying the value of this component when it is
 *     marked as readonly. The default is a
 *     <code>ui:staticText</code>. </li>
 *      <li><code>moveUpButton</code>: use this facet to specify a custom 
 *     component for the button that moves items up one step.</li>
 *      <li><code>moveDownButton</code>: use this facet to specify a custom 
 *     component for the button that moves items down one step.</li>
 *      <li><code>moveTopButton</code>: use this facet to specify a custom 
 *     component for the button that moves items to the top.</li>
 *      <li><code>moveBottomButton</code>: use this facet to specify a custom 
 *     component for the button that moves items to the bottom.</li>
 *     </ul>
 * 
 *     <h3>Client-side JavaScript functions</h3>
 * 
 * 
 *     <p>The functions below must be invoked on the JavaScript object
 *     that corresponds to the orderableList. The name of this object is
 *     <code>OrderableList_</code><em>id</em> where <em>id</em> is the
 *     DOM ID of the OrderableList component (the id of the outermost
 *     <code>div</code> with the colons replaced by underscores,
 *     e.g. <code>OrderableList_form_taskOrder</code>.</p> 
 *  </p>
 *     <table cellpadding="2" cellspacing="2" border="1" 
 *            style="text-align: left; width: 100%;">
 *     <tbody>
 *     <tr>
 *     <td style="vertical-align">
 *     <code><em>object</em>.updateValue()</code>
 *     </td>
 *     <td style="vertical-align: top">
 *     Use this method to update the hidden field which represents the
 *     component value in any JavaScript function that changes the order
 *     of the items on the list. 
 *     </td>
 *     </tr>
 *     <tr>
 *     <td style="vertical-align">
 *     <code><em>object</em>.moveUp()</code>
 *     </td>
 *     <td style="vertical-align: top">
 *     Moves the selected items up one step and updates the component
 *     value. 
 *     </td>
 *     </tr>
 *     <tr>
 *     <td style="vertical-align">
 *     <code><em>object</em>.moveDown()</code>
 *     </td>
 *     <td style="vertical-align: top">
 *     Moves the selected items down one step and updates the component
 *     value. 
 *     </td>
 *     </tr>
 *     <tr>
 *     <td style="vertical-align">
 *     <code><em>object</em>.moveTop()</code>
 *     </td>
 *     <td style="vertical-align: top">
 *     Moves the selected items to the top of the list and updates the
 *     component value. 
 *     </td>
 *     </tr>
 *     <tr>
 *     <td style="vertical-align">
 *     <code><em>object</em>.moveBottom()</code>
 *     </td>
 *     <td style="vertical-align: top">
 *     Moves the selected items to the bottom of the list and updates the
 *     component value. 
 *     </td>
 *     </tr>
 * 
 *     <tr>
 *     <td style="vertical-align">
 *     <code><em>object</em>.updateButtons()</code>
 *     </td>
 *     <td style="vertical-align: top">
 *     Use this method to update which buttons are selected in any
 *     JavaScript method that programmatically selects elements on the
 *     list. 
 *     </td>
 *     </tr>
 *   </tbody>
 * </table>
 * 
 *     <h3>Example</h3>
 * <h4>Example 1: OrderableList with Move to Top and Move to Bottom
 *     Buttons</h4> 
 * 
 * <p>This example uses a backing bean called
 *     <code>AutomatedTasks</code> with a property <code>taskList</code>
 *     which represents a list of tasks.  Users are allowed to 
 *     select more than one task to move. Buttons to move the items to
 *     the top and to the bottom are shown in addition to the default
 *     move up and move down buttons. </p> 
 * 
 * <pre>
 *     &lt;ui:orderableList id="taskOrder"
 *                       list="#{AutomatedTasks.taskList}"
 *                       label="Task execution order: "
 *                       multiple="true"
 *                       moveTopBottom="true"/&gt;
 * 
 * </pre>
 * 
 * <h4>Example 1: OrderableList with Move to Top and Move to Bottom
 *     Buttons</h4> 
 * 
 * <p>This example uses a backing bean called <code>Volunteers</code> and
 *     a property <code>rotationOrder</code> which is an ordered list of
 *     objects representing individual persons. A converter is used to
 *     derive a string representation of the person. Only the default 
 *     move up and move down buttons are shown. </p> 
 * 
 * <pre>
 *     &lt;ui:orderableList id="callUpOrder"
 *                       list="#{Volunteers.rotationOrder}"
 *                       label="Call Up Order:" &gt;
 *         &lt;f:converter converterId="org.example.Person"/&gt;
 *     &lt;ui:orderableList&gt;
 * </pre>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class OrderableListBase extends javax.faces.component.UIInput {

    /**
     * <p>Construct a new <code>OrderableListBase</code>.</p>
     */
    public OrderableListBase() {
        super();
        setRendererType("com.sun.rave.web.ui.OrderableList");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.OrderableList";
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
        super.setValueBinding(name, binding);
    }

    // disabled
    private boolean disabled = false;
    private boolean disabled_set = false;

    /**
 * <p>Flag indicating that activation of this component by the user is not currently permitted.</p>
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
 * <p>Flag indicating that activation of this component by the user is not currently permitted.</p>
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

    // labelOnTop
    private boolean labelOnTop = false;
    private boolean labelOnTop_set = false;

    /**
 * <p>If this attribute is true, the label is rendered above the
 *       component. If it is false, the label is rendered next to the
 *       component. The default is false.</p>
     */
    public boolean isLabelOnTop() {
        if (this.labelOnTop_set) {
            return this.labelOnTop;
        }
        ValueBinding _vb = getValueBinding("labelOnTop");
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
 * <p>If this attribute is true, the label is rendered above the
 *       component. If it is false, the label is rendered next to the
 *       component. The default is false.</p>
     * @see #isLabelOnTop()
     */
    public void setLabelOnTop(boolean labelOnTop) {
        this.labelOnTop = labelOnTop;
        this.labelOnTop_set = true;
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

    // moveTopBottom
    private boolean moveTopBottom = false;
    private boolean moveTopBottom_set = false;

    /**
 * <p>If this attribute is true, the Move to Top and Move to Bottom
 *       buttons are shown. The default is false.</p>
     */
    public boolean isMoveTopBottom() {
        if (this.moveTopBottom_set) {
            return this.moveTopBottom;
        }
        ValueBinding _vb = getValueBinding("moveTopBottom");
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
 * <p>If this attribute is true, the Move to Top and Move to Bottom
 *       buttons are shown. The default is false.</p>
     * @see #isMoveTopBottom()
     */
    public void setMoveTopBottom(boolean moveTopBottom) {
        this.moveTopBottom = moveTopBottom;
        this.moveTopBottom_set = true;
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
        return 12;
    }

    /**
 * <p>The number of items to display. The default value is 6.</p>
     * @see #getRows()
     */
    public void setRows(int rows) {
        this.rows = rows;
        this.rows_set = true;
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
 * <p>Position of this element in the tabbing order for the current
 *       document. The tabbing order determines the sequence in which
 *       elements receive focus when the tab key is pressed. The tabIndex
 *       value must be an integer between 0 and 32767.</p>
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
 * <p>Position of this element in the tabbing order for the current
 *       document. The tabbing order determines the sequence in which
 *       elements receive focus when the tab key is pressed. The tabIndex
 *       value must be an integer between 0 and 32767.</p>
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
        this.label = (String) _values[3];
        this.labelLevel = ((Integer) _values[4]).intValue();
        this.labelLevel_set = ((Boolean) _values[5]).booleanValue();
        this.labelOnTop = ((Boolean) _values[6]).booleanValue();
        this.labelOnTop_set = ((Boolean) _values[7]).booleanValue();
        this.moveTopBottom = ((Boolean) _values[8]).booleanValue();
        this.moveTopBottom_set = ((Boolean) _values[9]).booleanValue();
        this.multiple = ((Boolean) _values[10]).booleanValue();
        this.multiple_set = ((Boolean) _values[11]).booleanValue();
        this.readOnly = ((Boolean) _values[12]).booleanValue();
        this.readOnly_set = ((Boolean) _values[13]).booleanValue();
        this.rows = ((Integer) _values[14]).intValue();
        this.rows_set = ((Boolean) _values[15]).booleanValue();
        this.style = (String) _values[16];
        this.styleClass = (String) _values[17];
        this.tabIndex = ((Integer) _values[18]).intValue();
        this.tabIndex_set = ((Boolean) _values[19]).booleanValue();
        this.toolTip = (String) _values[20];
        this.visible = ((Boolean) _values[21]).booleanValue();
        this.visible_set = ((Boolean) _values[22]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[23];
        _values[0] = super.saveState(_context);
        _values[1] = this.disabled ? Boolean.TRUE : Boolean.FALSE;
        _values[2] = this.disabled_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.label;
        _values[4] = new Integer(this.labelLevel);
        _values[5] = this.labelLevel_set ? Boolean.TRUE : Boolean.FALSE;
        _values[6] = this.labelOnTop ? Boolean.TRUE : Boolean.FALSE;
        _values[7] = this.labelOnTop_set ? Boolean.TRUE : Boolean.FALSE;
        _values[8] = this.moveTopBottom ? Boolean.TRUE : Boolean.FALSE;
        _values[9] = this.moveTopBottom_set ? Boolean.TRUE : Boolean.FALSE;
        _values[10] = this.multiple ? Boolean.TRUE : Boolean.FALSE;
        _values[11] = this.multiple_set ? Boolean.TRUE : Boolean.FALSE;
        _values[12] = this.readOnly ? Boolean.TRUE : Boolean.FALSE;
        _values[13] = this.readOnly_set ? Boolean.TRUE : Boolean.FALSE;
        _values[14] = new Integer(this.rows);
        _values[15] = this.rows_set ? Boolean.TRUE : Boolean.FALSE;
        _values[16] = this.style;
        _values[17] = this.styleClass;
        _values[18] = new Integer(this.tabIndex);
        _values[19] = this.tabIndex_set ? Boolean.TRUE : Boolean.FALSE;
        _values[20] = this.toolTip;
        _values[21] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[22] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
