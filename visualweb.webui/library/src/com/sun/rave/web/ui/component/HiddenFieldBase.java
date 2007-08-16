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
 * Use the <code>ui:hiddenField</code> tag to create a hidden field,
 *     which is present in the HTML, but not displayed to the user.
 *     Hidden fields are useful for saving state information.  
 * 
 * <h3>HTML Elements and Layout</h3> 
 * 
 * <p>The hiddenField component renders an XHTML <code>&lt;input
 *     type="hidden"&gt;</code> element. </p> 
 * 
 *     <h3>Configuring the <code>ui:hiddenField</code> Tag</h3>
 * 
 * <p>Use the <code>value</code> attribute to associate
 * the component with a model object that represents the current value,
 * by setting the attribute's value to a JavaServer Faces EL expression
 *     that corresponds to a property of a backing bean.</p>
 * 
 *     <h3>Facets</h3>
 * 
 *     <p>This component has no facets.</p> 
 * 
 *     <h3>Theme Identifiers</h3>
 * 
 *     <p>This component does not use any style classes from the theme.</p> 
 * 
 *     <h3>Client-side JavaScript functions</h3>
 * 
 *     <p>In all the functions below, <code>&lt;id&gt;</code> should be
 *     the generated id of the HiddenField component. 
 * 
 *     <table cellpadding="2" cellspacing="2" border="1" 
 *            style="text-align: left; width: 100%;">
 *     <tbody>
 *     <tr>
 *     <td style="vertical-align">
 *     <code>field_setDisabled(&lt;id&gt;, &lt;disabled&gt;)</code>
 *     </td>
 *     <td style="vertical-align: top">
 *     Enable/disable the field. Set <code>&lt;disabled&gt;</code>
 *     to true to disable the component, or false to enable it.
 *     </td>
 *     </tr>
 *     <tr>
 *     <td style="vertical-align: top">
 *     <code>field_setValue(&lt;id&gt;, &lt;newValue&gt;)</code>
 *     </td>
 *     <td style="vertical-align: top">
 *     Set the value of the field to <code>&lt;newValue&gt;</code>.
 *     </td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: top">
 *     <code>field_getValue(&lt;id&gt;)</code>
 *   </td>
 *     <td style="vertical-align: top">Get the value of the field.</td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: top">
 *     <code>field_getInputElement(&lt;id&gt;)</code></td>
 *     <td style="vertical-align: top">
 *     Get hold of a reference to the input element rendered by this
 *     component.
 *     </td>
 *     </tr>
 *   </tbody>
 * </table>
 * 
 *     <h3>Examples</h3>
 * 
 * <p>This example uses a backing bean <code>FieldTest</code> with a
 * property <code>counter</code>. The property is an <code>int</code> but
 *     it is not necessary to specify a converter since the default
 *     JavaServer Faces converter will be used. The value of the hidden
 *     field may be updated through a JavaScript.  The tag generates an
 *     HTML input element.</p>
 * <pre>
 * &lt;ui:hiddenField id="counter" value="#{FieldTest.counter}"/&gt;
 * </pre>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class HiddenFieldBase extends javax.faces.component.UIInput {

    /**
     * <p>Construct a new <code>HiddenFieldBase</code>.</p>
     */
    public HiddenFieldBase() {
        super();
        setRendererType("com.sun.rave.web.ui.HiddenField");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.HiddenField";
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

    // disabled
    private boolean disabled = false;
    private boolean disabled_set = false;

    /**
 * <p>Flag indicating that the hidden field should not send its value to the
 *       server.</p>
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
 * <p>Flag indicating that the hidden field should not send its value to the
 *       server.</p>
     * @see #isDisabled()
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
        this.disabled_set = true;
    }

    // text
    /**
 * <p>Literal value to be rendered in this hidden field.
 *         If this property is specified by a value binding
 *         expression, the corresponding value will be updated
 *         if validation succeeds.</p>
     */
    public Object getText() {
        return getValue();
    }

    /**
 * <p>Literal value to be rendered in this hidden field.
 *         If this property is specified by a value binding
 *         expression, the corresponding value will be updated
 *         if validation succeeds.</p>
     * @see #getText()
     */
    public void setText(Object text) {
        setValue(text);
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.disabled = ((Boolean) _values[1]).booleanValue();
        this.disabled_set = ((Boolean) _values[2]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[3];
        _values[0] = super.saveState(_context);
        _values[1] = this.disabled ? Boolean.TRUE : Boolean.FALSE;
        _values[2] = this.disabled_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
