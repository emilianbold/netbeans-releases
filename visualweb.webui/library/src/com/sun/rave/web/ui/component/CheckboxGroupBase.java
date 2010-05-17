/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package com.sun.rave.web.ui.component;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

/**
 * <p>
 * 	Use the <code>ui:checkboxGroup</code> tag to display two or more
 * 	check boxes in a grid layout in the rendered HTML page. The
 * 	<code>ui:checkboxGroup</code> tag attributes that you
 * 	specify determine how the check boxes are displayed. 
 * </p>
 * <p>
 * 	If the <code>label</code> attribute is specified a
 * 	<code>com.sun.rave.web.ui.component.Label</code> component
 * 	is rendered before the first checkbox and
 * 	identifies the checkbox group. The label component's
 * 	<code>for</code> attribute is 
 * 	set to the <code>id</code> attribute of the first checkbox in
 * 	the rendered HTML page.
 * </p>
 * <p>
 * 	The check boxes are laid out in rows and columns in an HTML 
 * 	&lt;table&gt; element. The number of rows is defined by the length 
 * 	of the items array. The number of columns is defined by the columns
 * 	attribute. The default layout is a single vertical column.
 * </p>
 * <p>
 * </p>
 * <p>
 * 	The <code>items</code> attribute must be a value binding expression.
 * 	The value binding expression assigned to the <code>items</code>
 * 	property evaluates to an <code>Object</code> array of 
 * 	<code>com.sun.rave.web.ui.model.Option</code> instances. Each
 * 	instance represents one checkbox. The <code>value</code> property
 * 	of an <code>Option</code> instance represents the value of a
 * 	selected checkbox.
 * 	If the <code>items</code> array is empty nothing is rendered.
 * </p>
 * <p>	
 * 	Zero or more check boxes may be selected.
 * 	The <code>selected</code> attribute must also be a value binding
 * 	expression that is evaluated to read and write an <code>Object</code>
 * 	array. When an array is read from the value binding expression,
 * 	it identifies the selected checkboxes. Each element in the array
 * 	is an <code>Object</code> value. Each <code>Object</code> value must
 * 	be equal to the value property of at least one <code>Option</code>
 * 	instance specified in the array obtained from the value binding
 * 	expression assigned to the <code>items</code> attribute.
 * </p>
 * <p>
 * 	The write method of the <code>selected</code> attribute's value
 * 	binding expression is called during the <code>UPDATE_MODEL_PHASE</code>
 * 	of the JSF lifecyle. If one or more checkboxes are selected
 * 	an <code>Object</code> array is passed as an argument to the
 * 	write method. This array contains the <code>Object</code>
 * 	values of the selected checkboxes.
 * </p>
 * 	<h3>HTML Elements and Layout</h3>
 * <p>
 * 	A <code>ui:checkboxGroup</code> renders one
 * 	<code>com.sun.rave.web.ui.component.Checkbox</code> component for
 * 	each element in the <code>items</code> array.
 * 	See <a href="checkbox.html" target="tagFrame">ui:checkbox</a> for
 * 	details on the HTML elements and components rendered for a 
 * 	checkbox.
 * </p>
 * <p>
 * 	The value of the <code>name</code> attribute of each 
 * 	<code>Checkbox</code> component rendered is assigned the
 * 	<code>clientId</code> of the <code>CheckboxGroup<code>
 * 	component instance associated with this tag. The <code>id</code>
 * 	attribute of each <code>Checkbox</code> component rendered 
 * 	is formed as follows, where <em>cbgrpid</em> is the id of the
 * 	<code>CheckboxGroup</code> instance and <em>N</em> is the nth
 * 	checkbox.
 * </p>
 * 	<ul>
 * 	<li> <em>cbgrpid_N</em></li>
 * 	</ul>
 * <p>
 * 	See <a href="checkbox.html" target="tagFrame">ui:checkbox</a> for
 * 	details on how the id properties of the components that make up the 
 * 	checkbox are defined.
 * </p>
 * 	<h3>Client Side Javascript Functions</h3>
 * <p>
 * 	none.
 * </p>
 * 
 * 	<h3>Example</h3>
 * 	<b>Example 1: Create a checkbox group</b>
 * <p>
 * 	<code>
 * 	&nbsp;&lt;ui:checkboxGroup items="#{rbcbGrp.selections}"</br>
 * 	&nbsp;&nbsp;&nbsp;&nbsp;label="#{rbcbGrp.cbGrpLabel}"</br>
 * 	&nbsp;&nbsp;&nbsp;&nbsp;toolTip="cbgrp-tooltip"</br>
 * 	&nbsp;&nbsp;&nbsp;&nbsp;disabled="false"</br>
 * 	&nbsp;&nbsp;&nbsp;&nbsp;tabIndex="4"</br>
 * 	&nbsp;&nbsp;&nbsp;&nbsp;selected="#{rbcbGrp.cbvalue}"&gt;</br>
 * 	&nbsp;&lt;/ui:checkboxGroup&gt;</br>
 * 	</code>
 * </p>
 * <p>
 * 	This example creates a checkbox group with an identifying
 * 	label for the group before the first checkbox. The 
 * 	data for the checkboxes is obtained from the value binding
 * 	expression <code>#{rbcbGrp.selections}</code>. <code>rbcbGrp</code>
 * 	is an application defined managed bean and provides the values for
 * 	other attributes such as <code>selected</code> to receive the
 * 	value of the selected checkboxes in the group.
 * </p>
 * 	<!--
 * 	<h3>Theme Identifiers</h3>
 * <p>
 * 	<ul>
 * 	<li>CbGrp for the TABLE element.</li>
 * 	<li>CbGrpCpt for the TD element containing the group label</li>
 * 	<li>CbGrpLbl for the LABEL element used as the CAPTION</li>
 * 	<li>CbGrpLblDis for the LABEL used as the CAPTION if the group is disabled</li>
 * 	<li>CbGrpRwEv for even TR elements</li>
 * 	<li>CbGrpRwOd for odd TR elements</li>
 * 	<li>CbGrpClEv for even TD elements</li>
 * 	<li>CbGrpClOd for odd TD elements</li>
 * 	<li>Cb for the INPUT element</li>
 * 	<li>CbDis for the INPUT element for disabled check box </li>
 * 	<li>CbLbl for a LABEL element of a check box</li>
 * 	<li>CbLblDis for a LABEL element of a disabled check box</li>
 * 	<li>CbImg for an IMG element of a check box</li>
 * 	<li>CbImgDis for an IMG element of a disabled check box</li>
 * 	<li>CbGrp - for the <em>span</em> element that encloses the
 * 	  entire set of elements rendered for the <code>checkboxGroup</code>.
 * 	</li>
 * 	</ul>
 * </p>
 * 	-->
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class CheckboxGroupBase extends com.sun.rave.web.ui.component.Selector {

    /**
     * <p>Construct a new <code>CheckboxGroupBase</code>.</p>
     */
    public CheckboxGroupBase() {
        super();
        setRendererType("com.sun.rave.web.ui.CheckboxGroup");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.CheckboxGroup";
    }

    // columns
    private int columns = Integer.MIN_VALUE;
    private boolean columns_set = false;

    /**
 * <p>Defines how many columns may be used to lay out the check boxes.
 *         The value must be greater than or equal to one. The default value is one.
 *         Invalid values are ignored and the value is set to one.</p>
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
 * <p>Defines how many columns may be used to lay out the check boxes.
 *         The value must be greater than or equal to one. The default value is one.
 *         Invalid values are ignored and the value is set to one.</p>
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
