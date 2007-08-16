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
 *     <p>This tag renders a drop-down menu. Use the <code>selected</code>
 *     attribute to associate the component with a model object that
 *     represents the current choice, by setting the value to an EL
 *     expression that corresponds to a property of a
 *     managed bean.</p> 
 * 
 *     <h4>Configuring the dropdown tag</h4>
 * 
 *     <p>Use the <code>items</code> attribute to specify the options
 *     from which the web application user can choose. The value must be
 *     an EL expression that identifies an array, a
 *     <code>java.util.Collection</code> or a <code>java.util.Map</code>
 *     of <code>com.sun.rave.web.ui.model.Option</code>. </p> 
 * 
 *     <p>The first time the component is rendered, the option which
 *     corresponds to the value of the <code>selected</code> model object
 *     property is marked as selected, using the <code>equals</code>
 *     method on the model object. </p> 
 * 
 *     <p>To optionally specify a label for the component, use the
 *     <code>label</code> attribute, or specify a label facet.</p>
 * 
 * 
 *     <h4>Facets</h4>
 * 
 *     <ul>
 *     <li><code>label</code>: use this facet to specify a custom 
 *     component for the label.</li>
 *     </ul>
 * 
 *     <h4>Client-side JavaScript functions</h4>
 * 
 *     <ul>
 *     <li><code>dropDown_setDisabled(&lt;id&gt;, &lt;disabled&gt;)</code>: use
 *     this function to enable/disable the drop-down menu. <code>&lt;id&gt;</code>
 *     must be the generated id of the component. Set
 *     <code>&lt;disabled&gt;</code> to true to disable the component, 
 *     false to enable it. </li>
 *     <li><code>dropDown_changed(&lt;id&gt;)</code>: this 
 *     function is automatically invoked by the drop-down menu's
 *     <code>onchange</code> handler. <code>&lt;id&gt;</code>
 *     must be the generated id of the component.</li>
 *     </ul>
 * 
 *     <h4>Examples</h4>
 * 
 * <pre>
 * 
 *     &lt;ui:dropDown selected="#{flightSearch.leaveAirport}" 
 *                  items="#{dataBean.airports}" 
 *                  id="leaveAirport" 
 *                  tooltip="#{msgs.chooseAirport}"            label="#{msgs.chooseDepartureAirport}" /&gt;
 * 
 *     &lt;ui:dropDown selected="#{flightSearch.leaveAirport}" 
 *                  items="#{dataBean.airports}" 
 *  	         id="leaveAirport" 
 *                  tooltip="#{msgs.chooseAirport}"
 *                  label="#{msgs.chooseDepartureAirport}" &gt;
 *         &lt;f:facet name="label"&gt;
 *             &lt;facet component goes here&gt;
 *         &lt;/f:facet&gt;
 *     &lt;/ui:dropDown&gt;
 * </pre>
 * <p>The dataBean backing bean would include the following definition for the "airports" items:<br>
 * <pre>
 *     private Option[] airports = null;
 *      // Creates a new instance of backing bean //
 *     public DataBean() {
 *         airports = new Option[11];
 *         airports[0] = new Option("SFO", "San Francisco");
 *         airports[1] = new Option("OAK", "Oakland");
 *         airports[2] = new Option("SAN", "San Diego");
 *         airports[3] = new Option("BOS", "Boston");
 *         airports[4] = new Option("ARN", "Stockholm");
 *         airports[5] = new Option("MNL", "Manila");
 *         airports[6] = new Option("CDG", "Paris");
 *         airports[7] = new Option("PDX", "Portland");
 *         airports[8] = new Option("LAX", "Los Angeles");
 *         airports[9] = new Option("NRT", "Tokyo");
 *         airports[10] = new Option("TBD", "Future Airport");
 *         airports[10].setDisabled(true);
 *     }
 *     public Option[] getAirports() {
 *         return airports;
 *     }
 * </pre>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class DropDownBase extends com.sun.rave.web.ui.component.ListSelector {

    /**
     * <p>Construct a new <code>DropDownBase</code>.</p>
     */
    public DropDownBase() {
        super();
        setRendererType("com.sun.rave.web.ui.DropDown");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.DropDown";
    }

    // action
    private javax.faces.el.MethodBinding action = null;

    /**
 * <p>Method binding representing a method that processes
 *         application actions from this component. This attribute is
 *         only referenced when submitForm is true.</p>
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
 *         application actions from this component. This attribute is
 *         only referenced when submitForm is true.</p>
     * @see #getAction()
     */
    public void setAction(javax.faces.el.MethodBinding action) {
        this.action = action;
    }

    // actionListener
    private javax.faces.el.MethodBinding actionListener = null;

    /**
 * <p>Method binding representing a method that receives action
 *         from this, and possibly other, components. The Action Listener is invoked only when
 *         submitForm is true.</p>
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
 * <p>Method binding representing a method that receives action
 *         from this, and possibly other, components. The Action Listener is invoked only when
 *         submitForm is true.</p>
     * @see #getActionListener()
     */
    public void setActionListener(javax.faces.el.MethodBinding actionListener) {
        this.actionListener = actionListener;
    }

    // forgetValue
    private boolean forgetValue = false;
    private boolean forgetValue_set = false;

    /**
 * <p>If this flag is set to true, then the component is always
 *       rendered with no initial selection.</p>
     */
    public boolean isForgetValue() {
        if (this.forgetValue_set) {
            return this.forgetValue;
        }
        ValueBinding _vb = getValueBinding("forgetValue");
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
 * <p>If this flag is set to true, then the component is always
 *       rendered with no initial selection.</p>
     * @see #isForgetValue()
     */
    public void setForgetValue(boolean forgetValue) {
        this.forgetValue = forgetValue;
        this.forgetValue_set = true;
    }

    // navigateToValue
    private boolean navigateToValue = false;
    private boolean navigateToValue_set = false;

    /**
 * <p>If this flag is set to true, then selecting an item from this
 *       component will cause the application to navigate using the
 *       DropDown's value as the action. Use this in place of defining 
 *       the navigation outcome using the action MethodBinding. This
 *       applies only if submitForm is true.</p>
     */
    public boolean isNavigateToValue() {
        if (this.navigateToValue_set) {
            return this.navigateToValue;
        }
        ValueBinding _vb = getValueBinding("navigateToValue");
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
 * <p>If this flag is set to true, then selecting an item from this
 *       component will cause the application to navigate using the
 *       DropDown's value as the action. Use this in place of defining 
 *       the navigation outcome using the action MethodBinding. This
 *       applies only if submitForm is true.</p>
     * @see #isNavigateToValue()
     */
    public void setNavigateToValue(boolean navigateToValue) {
        this.navigateToValue = navigateToValue;
        this.navigateToValue_set = true;
    }

    // submitForm
    private boolean submitForm = false;
    private boolean submitForm_set = false;

    /**
 * <p>Flag indicating that the form should be submitted when the
 *       value of the component changes.</p>
     */
    public boolean isSubmitForm() {
        if (this.submitForm_set) {
            return this.submitForm;
        }
        ValueBinding _vb = getValueBinding("submitForm");
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
 * <p>Flag indicating that the form should be submitted when the
 *       value of the component changes.</p>
     * @see #isSubmitForm()
     */
    public void setSubmitForm(boolean submitForm) {
        this.submitForm = submitForm;
        this.submitForm_set = true;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.action = (javax.faces.el.MethodBinding) restoreAttachedState(_context, _values[1]);
        this.actionListener = (javax.faces.el.MethodBinding) restoreAttachedState(_context, _values[2]);
        this.forgetValue = ((Boolean) _values[3]).booleanValue();
        this.forgetValue_set = ((Boolean) _values[4]).booleanValue();
        this.navigateToValue = ((Boolean) _values[5]).booleanValue();
        this.navigateToValue_set = ((Boolean) _values[6]).booleanValue();
        this.submitForm = ((Boolean) _values[7]).booleanValue();
        this.submitForm_set = ((Boolean) _values[8]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[9];
        _values[0] = super.saveState(_context);
        _values[1] = saveAttachedState(_context, action);
        _values[2] = saveAttachedState(_context, actionListener);
        _values[3] = this.forgetValue ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = this.forgetValue_set ? Boolean.TRUE : Boolean.FALSE;
        _values[5] = this.navigateToValue ? Boolean.TRUE : Boolean.FALSE;
        _values[6] = this.navigateToValue_set ? Boolean.TRUE : Boolean.FALSE;
        _values[7] = this.submitForm ? Boolean.TRUE : Boolean.FALSE;
        _values[8] = this.submitForm_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
