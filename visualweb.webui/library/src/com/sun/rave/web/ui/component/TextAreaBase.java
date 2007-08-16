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
 * <p>Use the <code>ui:textArea</code> tag to create a multiple-line
 *     input field for text.</p>    
 * 
 * <h3>HTML Elements and Layout</h3>
 * 
 * <p>The textArea component renders an HTML &lt;textarea&gt; element.</p> 
 * 
 *     <h3>Configuring the <code>ui:textArea</code> Tag</h3>
 * 
 * <p>Use the <code>text</code> attribute to associate
 * the component with a model object that represents the current value,
 * by setting the attribute's value to a JavaServer Faces EL expression
 *     that evaluates to a backing bean or a backing bean property.</p>
 * 
 *     <p>To optionally specify a label for the component, use the
 *     <code>label</code> attribute, or specify a label facet.</p>
 * 
 *     <h3>Facets</h3>
 * 
 *     <ul>
 *     <li><code>label</code>: use this facet to specify a custom 
 *     component for the label.</li>
 *     <li><code>readOnly</code>: use this facet to specify a custom 
 *     component for displaying the readOnly value of this component.</li>
 *     </ul>
 * 
 *     <h3>Theme Identifiers</h3> 
 * 
 *     <p>The input element has a style class "TxtAra", or "TxtAraDis"
 *     when the field is disabled. If a label attribute is specified, the
 *     label element's class attribute is set to "LstAln" followed by
 *     "LblLvl1Txt", "LblLvl2Txt" or "LblLvl3Txt" depending on the label
 *     level.</p>  
 * 
 * 
 *     <h3>Client-side JavaScript functions</h3>
 * 
 *     <p>In all the functions below, <code>&lt;id&gt;</code> should be
 *     the generated id of the TextArea component. 
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
 *     Get hold of a reference to the textArea element rendered by this
 *     component.
 *     </td>
 *     </tr>
 *     <tr>
 *       <td style="vertical-align: top">
 *     <code>component_setVisible(&lt;id&gt;)</code>
 *   </td>
 *       <td style="vertical-align: top">Hide or show this component.
 *       </td>
 *     </tr>
 *   </tbody>
 * </table>
 * 
 * 
 *     <h3>Examples</h3>
 * 
 * <p> This example uses a backing bean <code>FieldTest</code> with a property
 *     string. The tag generates a textarea with a label "Comment:". The
 *     rows and columns attributes have been set, to ensure that the
 *     component has the same size on all browsers. </p> 
 * <pre>
 *      &lt;ui:textArea id="textarea" type="textarea"
 *                label="Comment:" 
 *                text="#{FieldTest.string}"
 *                rows="5" columns="50"/&gt;
 * </pre>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class TextAreaBase extends com.sun.rave.web.ui.component.Field {

    /**
     * <p>Construct a new <code>TextAreaBase</code>.</p>
     */
    public TextAreaBase() {
        super();
        setRendererType("com.sun.rave.web.ui.TextArea");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.TextArea";
    }

    // rows
    private int rows = Integer.MIN_VALUE;
    private boolean rows_set = false;

    /**
 * <p>Number of rows used to render the textarea. You should set a value
 *     for this attribute to ensure that it is rendered correctly in all
 *     browsers.  Browsers vary in the default number of rows used for
 *     textarea fields.</p>
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
        return 2;
    }

    /**
 * <p>Number of rows used to render the textarea. You should set a value
 *     for this attribute to ensure that it is rendered correctly in all
 *     browsers.  Browsers vary in the default number of rows used for
 *     textarea fields.</p>
     * @see #getRows()
     */
    public void setRows(int rows) {
        this.rows = rows;
        this.rows_set = true;
    }

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.rows = ((Integer) _values[1]).intValue();
        this.rows_set = ((Boolean) _values[2]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[3];
        _values[0] = super.saveState(_context);
        _values[1] = new Integer(this.rows);
        _values[2] = this.rows_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
