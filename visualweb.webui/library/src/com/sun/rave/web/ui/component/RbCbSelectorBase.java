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
 * This renderer meta-data is not mapped one to one with a component.
 *       A renderer of this name does exist as the super class of the
 *       RadioButton and Checkbox renderers.
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class RbCbSelectorBase extends com.sun.rave.web.ui.component.Selector {

    /**
     * <p>Construct a new <code>RbCbSelectorBase</code>.</p>
     */
    public RbCbSelectorBase() {
        super();
        setRendererType("com.sun.rave.web.ui.RbCbSelectorRenderer");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.RbCbSelector";
    }

    /**
     * <p>Return the <code>ValueBinding</code> stored for the
     * specified name (if any), respecting any property aliases.</p>
     *
     * @param name Name of value binding to retrieve
     */
    public ValueBinding getValueBinding(String name) {
        if (name.equals("selected")) {
            return super.getValueBinding("value");
        }
        if (name.equals("selectedValue")) {
            return super.getValueBinding("items");
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
        if (name.equals("selected")) {
            super.setValueBinding("value", binding);
            return;
        }
        if (name.equals("selectedValue")) {
            super.setValueBinding("items", binding);
            return;
        }
        super.setValueBinding(name, binding);
    }

    // imageURL
    private String imageURL = null;

    /**
 * <p>
 * 	    A context relative path of an image to be displayed with
 * 	    the control. If you want to be able to specify attributes
 * 	    for the image, specify an <code>image</code> facet instead
 * 	    of the <code>imageURL</code> attribute.</p>
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
 * <p>
 * 	    A context relative path of an image to be displayed with
 * 	    the control. If you want to be able to specify attributes
 * 	    for the image, specify an <code>image</code> facet instead
 * 	    of the <code>imageURL</code> attribute.</p>
     * @see #getImageURL()
     */
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    // name
    private String name = null;

    /**
 * <p>
 * 	    Identifies the control as participating as part
 * 	    of a group. The <code>RadioButton</code> and <code>Checkbox</code>
 * 	    classes determine the behavior of the group,
 * 	    that are assigned the same value to the <code>name</code>
 * 	    property. The value of this property must be unique for components
 * 	    in the group, within the scope of the <code>Form</code>
 * 	    parent component containing the grouped components.</p>
     */
    public String getName() {
        if (this.name != null) {
            return this.name;
        }
        ValueBinding _vb = getValueBinding("name");
        if (_vb != null) {
            return (String) _vb.getValue(getFacesContext());
        }
        return null;
    }

    /**
 * <p>
 * 	    Identifies the control as participating as part
 * 	    of a group. The <code>RadioButton</code> and <code>Checkbox</code>
 * 	    classes determine the behavior of the group,
 * 	    that are assigned the same value to the <code>name</code>
 * 	    property. The value of this property must be unique for components
 * 	    in the group, within the scope of the <code>Form</code>
 * 	    parent component containing the grouped components.</p>
     * @see #getName()
     */
    public void setName(String name) {
        this.name = name;
    }

    // selected
    /**
 * <p>The object that represents the selections made from the
 *              available options. If multiple selections are allowed, this
 *              must be bound to ArrayList, an Object array, or an array of
 *              primitives.</p>
     */
    public Object getSelected() {
        return getValue();
    }

    /**
 * <p>The object that represents the selections made from the
 *              available options. If multiple selections are allowed, this
 *              must be bound to ArrayList, an Object array, or an array of
 *              primitives.</p>
     * @see #getSelected()
     */
    public void setSelected(Object selected) {
        setValue(selected);
    }

    // selectedValue
    /**
 * <p>
 * 	    The value of the component when it is selected. The value of this
 * 	    property is assigned to the <code>selected</code> property when
 * 	    the component is selected. The component is selected
 * 	    when the <code>selected</code> property is equal to this value.</br>
 * 	    This attribute can be bound to a <code>String</code>, or <code>
 * 	    Object</code> value.</br>
 * 	    If this property is not assigned a value, the component behaves
 * 	    as a boolean component. A boolean component
 * 	    is selected when the <code>selected</code> property is equal to a
 * 	    true <code>Boolean</code> instance.<br>
 * 	    If a boolean component is not selected, the <code>selected</code>
 * 	    property value is a false <code>Boolean</code> instance.</p>
     */
    public Object getSelectedValue() {
        return getItems();
    }

    /**
 * <p>
 * 	    The value of the component when it is selected. The value of this
 * 	    property is assigned to the <code>selected</code> property when
 * 	    the component is selected. The component is selected
 * 	    when the <code>selected</code> property is equal to this value.</br>
 * 	    This attribute can be bound to a <code>String</code>, or <code>
 * 	    Object</code> value.</br>
 * 	    If this property is not assigned a value, the component behaves
 * 	    as a boolean component. A boolean component
 * 	    is selected when the <code>selected</code> property is equal to a
 * 	    true <code>Boolean</code> instance.<br>
 * 	    If a boolean component is not selected, the <code>selected</code>
 * 	    property value is a false <code>Boolean</code> instance.</p>
     * @see #getSelectedValue()
     */
    public void setSelectedValue(Object selectedValue) {
        setItems(selectedValue);
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.imageURL = (String) _values[1];
        this.name = (String) _values[2];
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[3];
        _values[0] = super.saveState(_context);
        _values[1] = this.imageURL;
        _values[2] = this.name;
        return _values;
    }

}
