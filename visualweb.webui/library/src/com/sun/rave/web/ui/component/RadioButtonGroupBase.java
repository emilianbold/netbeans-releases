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
 * <p>
 * 	Use the <code>ui:radioButtonGroup</code> tag to display two or more 
 * 	radio buttons in a grid layout in the rendered HTML page. The 
 * 	<code>ui:radioButtonGroup</code> tag attributes that
 * 	you specify determine how the radio buttons are displayed. 
 * </p>
 * <p>
 * 	If the <code>label</code> attribute is specified a
 * 	<code>com.sun.rave.web.ui.component.Label</code> component
 * 	is rendered before the first radio button and
 * 	identifies the radio button group. The label component's
 * 	<code>for</code> attribute is 
 * 	set to the <code>id</code> attribute of the first radio button in
 * 	the rendered HTML page.
 * </p>
 * <p>
 * 	The radio buttons are laid out in rows and columns in an HTML
 * 	&lt;table&gt;
 * 	element. The number of rows is defined by the length of the items
 * 	array. The number of columns is defined by the columns attribute. The
 * 	default layout is a single vertical column.
 * </p>
 * <p>
 * 	The <code>items</code> attribute must be a value binding expression.
 * 	The value binding expression assigned to the <code>items</code>
 * 	property evaluates to an <code>Object</code> array of 
 * 	<code>com.sun.rave.web.ui.model.Option</code> instances.
 * 	Each
 * 	instance represents one radio button. The <code>value</code> property
 * 	of an <code>Option</code> instance represents the value of a
 * 	selected radio button.
 * 	If the <code>items</code> array is empty nothing is rendered.
 * </p>
 * <p>	
 * 	At least one radio button should be selected by the application.
 * 	The <code>selected</code> attribute must also be a value binding
 * 	expression that is evaluated to read and write an <code>Object</code>.
 * 	When an <code>Object</code> value is read from the value binding
 * 	expression, it identifies the selected radio button.
 * 	The <code>Object</code> value must
 * 	be equal to the value property of at least one <code>Option</code>
 * 	instance specified in the array obtained from the value binding
 * 	expression assigned to the <code>items</code> attribute.
 * </p>
 * <p>
 * 	The write method of the <code>selected</code> attribute value
 * 	binding expression is called during the <code>UPDATE_MODEL_PHASE</code>
 * 	of the JSF lifecyle. If a radio button is selected
 * 	an <code>Object</code> value is passed as an argument to the
 * 	write method. The <ccode>Object</code> value is the 
 * 	value of the selected radio button.
 * </p>
 * 
 * 	<h3>HTML Elements and Layout</h3>
 * <p>
 * 	A <code>ui:radioButtonGroup</code> renders one
 * 	<code>com.sun.rave.web.ui.component.RadioButton</code> component for
 * 	each element in the <code>items</code> array. 
 * 	See <a href="radioButton.html" target="tagFrame">ui:radioButton</a> for
 * 	details on the HTML elements and components rendered for a 
 * 	radio button.
 * </p>
 * <p>
 * 	The value of the <code>name</code> attribute of each 
 * 	<code>RadioButton</code> component rendered is assigned the
 * 	<code>clientId</code> of the <code>RadioButtonGroup</code>
 * 	component instance associated with this tag. The <code>id</code>
 * 	attribute of each <code>RadioButton</code> component rendered
 * 	is formed as follows, where <em>rbgrpid</em> is the id of the
 * 	<code>RadioButtonGroup</code> instance and <em>N</em> is the nth
 * 	radio button.
 * </p>
 * 	<ul>
 * 	<li> <em>rbgrpid_N</em></li>
 * 	</ul>
 * <p>
 * 	See <a href="radioButton.html" target="tagFrame">ui:radioButton</a> for
 * 	details on how the id properties of the components that make up the 
 * 	radio button are defined.
 * </p>
 *         <h3>Client Side Javascript Functions</h3>
 * <p>
 * 	none.
 * </p>
 * 	<h3>Example</h3>
 * 	<b>Example 1: Create a radio button group</b>
 * 	<p>
 * 	<code>
 * 	&nbsp;&lt;ui:radioButtonGroup items="#{rbcbGrp.selections}"</br>
 * 	&nbsp;&nbsp;&nbsp;&nbsp;label="#{rbcbGrp.rbGrpLabel}"</br>
 * 	&nbsp;&nbsp;&nbsp;&nbsp;toolTip="rbgrp-tooltip"</br>
 * 	&nbsp;&nbsp;&nbsp;&nbsp;tabIndex="1"</br>
 * 	&nbsp;&nbsp;&nbsp;&nbsp;columns="3"</br>
 * 	&nbsp;&nbsp;&nbsp;&nbsp;labelLevel="2"</br>
 * 	&nbsp;&nbsp;&nbsp;&nbsp;selected="#{rbcbGrp.selection}"&gt;</br>
 * 	&nbsp;&lt;/ui:radioButtonGroup&gt;</br>
 * 	</code>
 * 	</p>
 * 	<p>
 * 	This example creates a radio button group with an identifying
 * 	label for the group before the first radio button. The 
 * 	data for the radio buttons is obtained from the value binding
 * 	expression <code>#{rbcbGrp.selections}</code> where <code>rbcbGrp</code>
 * 	is an application defined managed bean. The bean provides the values for
 * 	other attributes such as <code>selected</code> to receive the
 * 	value of the selected radio button in the group.
 * 	</p>
 * 
 * 	<!--
 * 	<h3>CSS style selectors</h3>
 * <p>
 * 	<ul>
 * 	<li>RbGrp for the TABLE element.</li>
 * 	<li>RbGrpCpt for the TD element containing the group label</li>
 * 	<li>RbGrpLbl for the LABEL element used as the CAPTION</li>
 * 	<li>RbGrpLblDis for the LABEL used as the CAPTION if the group is disabled</li>
 * 	<li>RbGrpRwEv for even TR elements</li>
 * 	<li>RbGrpRwOd for odd TR elements</li>
 * 	<li>RbGrpClEv for even TD elements</li>
 * 	<li>RbGrpClOd for odd TD elements</li>
 * 	<li>Rb for the INPUT element</li>
 * 	<li>RbDis for the INPUT element for disabled radio button </li>
 * 	<li>RbLbl for a LABEL element of a radio button</li>
 * 	<li>RbLblDis for a LABEL element of a disabled radio button</li>
 * 	<li>RbImg for an IMG element of a radio button</li>
 * 	<li>RbImgDis for an IMG element of a disabled radio button</li>
 * 	</ul>
 * </p>
 * 	-->
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class RadioButtonGroupBase extends com.sun.rave.web.ui.component.Selector {

    /**
     * <p>Construct a new <code>RadioButtonGroupBase</code>.</p>
     */
    public RadioButtonGroupBase() {
        super();
        setRendererType("com.sun.rave.web.ui.RadioButtonGroup");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.RadioButtonGroup";
    }

    // columns
    private int columns = Integer.MIN_VALUE;
    private boolean columns_set = false;

    /**
 * <p>Defines how many columns may be used to layout the radio buttons.
 * 	The value must be greater than or equal to one. The
 * 	default value is one. Invalid values are ignored and the value
 * 	is set to one.</p>
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
        return 1;
    }

    /**
 * <p>Defines how many columns may be used to layout the radio buttons.
 * 	The value must be greater than or equal to one. The
 * 	default value is one. Invalid values are ignored and the value
 * 	is set to one.</p>
     * @see #getColumns()
     */
    public void setColumns(int columns) {
        this.columns = columns;
        this.columns_set = true;
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
        this.columns = ((Integer) _values[1]).intValue();
        this.columns_set = ((Boolean) _values[2]).booleanValue();
        this.visible = ((Boolean) _values[3]).booleanValue();
        this.visible_set = ((Boolean) _values[4]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[5];
        _values[0] = super.saveState(_context);
        _values[1] = new Integer(this.columns);
        _values[2] = this.columns_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.visible ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = this.visible_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
