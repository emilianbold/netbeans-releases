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
 * <h4>About this tag</h4>
 * 
 *     <p>This tag renders a listbox. Use the <code>selected</code>
 *     attribute to associate the component with a model object that
 *     represents the current choice, by setting the value to an EL
 *     expression that corresponds to a property of a
 *     managed bean.</p> 
 * 
 *     <h4>Configuring the listbox tag</h4>
 * 
 *     <p>Use the <code>multiple</code> attribute to specify whether the
 *     component accepts multiple selections. By default this is set to
 *     false. If multiple selections are allowed, the model object
 *     property must be either an array of primitives, an array of
 *     objects, or a (subclass of) <code>java.util.List</code>.</p> 
 * 
 *     <p>Use the <code>items</code> attribute to specify the options
 *     from which the web application user can choose. The value must be
 *     an EL expression that identifies an array, a
 *     <code>java.util.Collection</code> or a <code>java.util.Map</code>
 *     of <code>com.sun.rave.web.ui.Option</code>. </p> 
 * 
 *     <p>The first time the component is rendered, the options which
 *     correspond to the value of the <code>selected</code> model object
 *     property is marked as selected, using the <code>equals</code>
 *     method on the model object. </p> 
 * 
 *     <p>The number of list items simultaneously shown can be specified
 *     using the <code>rows</code> attribute, and the component will be
 *     rendered using a monospaced font if the <code>useMonospace</code>
 *     attribute is set to true.</p>
 * 
 *     <p>To optionally specify a label for the component, use the
 *     <code>label</code> attribute, or specify a label facet.</p>
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
 *     <li><code>listbox_setDisabled(&lt;id&gt;, &lt;disabled&gt;)</code>: use
 *     this function to enable/disable the listbox. <code>&lt;id&gt;</code>
 *     must be the generated id of the component. Set
 *     <code>&lt;disabled&gt;</code> to true to disable the component, 
 *     false to enable it. </li>
 *     <li><code>listbox_changed(&lt;id&gt;)</code>: this 
 *     function is automatically invoked by the listbox's
 *     <code>onchange</code> handler. <code>&lt;id&gt;</code>
 *     must be the generated id of the component.</li>
 *     </ul>
 * 
 *     <h4>Examples</h4>
 * <pre>
 * 
 *     &lt;ui:listbox selected="#{flightSearch.leaveAirport}" 
 *                  items="#{dataBean.airports}" 
 *                  rows="6"
 *                  id="leaveAirport" 
 *                  toolTip="#{msgs.chooseAirport}"
 *                  label="#{msgs.chooseDepartureAirport)" /&gt;
 * 
 *     &lt;ui:listbox selected="#{flightSearch.leaveAirport}" 
 *                  items="#{dataBean.airports}" 
 *                  rows="6"
 *  	         id="leaveAirport" 
 *                  toolTip="#{msgs.chooseAirport}"
 *                  label="#{msgs.chooseDepartureAirport)" &gt;
 *         &lt;f:facet name="label"&gt;
 *             &lt;facet component goes here&gt;
 *         &lt;/f:facet&gt;
 *     &lt;/ui:listbox&gt;
 * </pre>
 * <p>Auto-generated component class.
 * Do <strong>NOT</strong> modify; all changes
 * <strong>will</strong> be lost!</p>
 */

public abstract class ListboxBase extends com.sun.rave.web.ui.component.ListSelector {

    /**
     * <p>Construct a new <code>ListboxBase</code>.</p>
     */
    public ListboxBase() {
        super();
        setRendererType("com.sun.rave.web.ui.Listbox");
    }

    /**
     * <p>Return the identifier of the component family to which this
     * component belongs.  This identifier, in conjunction with the value
     * of the <code>rendererType</code> property, may be used to select
     * the appropriate {@link Renderer} for this component instance.</p>
     */
    public String getFamily() {
        return "com.sun.rave.web.ui.Listbox";
    }

    // monospace
    private boolean monospace = false;
    private boolean monospace_set = false;

    /**
 * <p>A property which indicates whether monospaced fonts should be
 *       used.</p>
     */
    public boolean isMonospace() {
        if (this.monospace_set) {
            return this.monospace;
        }
        ValueBinding _vb = getValueBinding("monospace");
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
 * <p>A property which indicates whether monospaced fonts should be
 *       used.</p>
     * @see #isMonospace()
     */
    public void setMonospace(boolean monospace) {
        this.monospace = monospace;
        this.monospace_set = true;
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

    /**
     * <p>Restore the state of this component.</p>
     */
    public void restoreState(FacesContext _context,Object _state) {
        Object _values[] = (Object[]) _state;
        super.restoreState(_context, _values[0]);
        this.monospace = ((Boolean) _values[1]).booleanValue();
        this.monospace_set = ((Boolean) _values[2]).booleanValue();
        this.multiple = ((Boolean) _values[3]).booleanValue();
        this.multiple_set = ((Boolean) _values[4]).booleanValue();
    }

    /**
     * <p>Save the state of this component.</p>
     */
    public Object saveState(FacesContext _context) {
        Object _values[] = new Object[5];
        _values[0] = super.saveState(_context);
        _values[1] = this.monospace ? Boolean.TRUE : Boolean.FALSE;
        _values[2] = this.monospace_set ? Boolean.TRUE : Boolean.FALSE;
        _values[3] = this.multiple ? Boolean.TRUE : Boolean.FALSE;
        _values[4] = this.multiple_set ? Boolean.TRUE : Boolean.FALSE;
        return _values;
    }

}
