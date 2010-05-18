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

import java.util.ArrayList;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.render.Renderer;

import com.sun.rave.web.ui.util.ConversionUtilities;

/**
 * <p>A component that represents a checkbox.</p>
 * <p>
 * The <code>Checkbox</code> can be used as a single checkbox
 * or one checkbox among a group of checkboxes. A group
 * of checkboxes represents a multiple selection list which can have any
 * number of checkboxes selected, or none selected. A checkbox can
 * represent a <code>Boolean</code> value, a <code>String</code> value,
 * or a developer defined <code>Object</code> value.
 * </p>
 * <h3>Detecting a selected checkbox</h3>
 * <p>
 * The <code>Checkbox</code> uses both the <code>selected</code>
 * and <code>selectedValue</code> properties to pass information about
 * the checkbox's selection status. The <code>selected</code>
 * property is used to indicate that the checkbox is selected.
 * The <code>selectedValue</code> property is used to pass a data value,
 * a string by default, for the checkbox. A checkbox is considered to be
 * selected when the value of the <code>selected</code> property is equal
 * to the value of the <code>selectedValue</code> property. A checkbox can
 * be initally selected by assigning the same value
 * to the <code>selectedValue</code> and the <code> selected</code> 
 * properties. <code>isChecked</code> is called to determine
 * if this <code>Checkbox</code> is selected.
 * </p>
 * <p>If the <code>selectedValue</code> property is not specified or its
 * value is <code>null</code> then the checkbox behaves like a
 * boolean control. If the checkbox is selected, the value of the
 * <code>selected</code> property is a true <code>Boolean</code>
 * instance. If the checkbox is not selected, the value of the
 * <code>selected</code> property will be a false <code>Boolean</code>
 * instance.
 * </p>
 * <p> <em>Note that a value binding expression that evaluates to a
 * primitive boolean value can be assigned to the <code>selected</code>
 * property. Proper type coercion from <code>Boolean</code> to
 * <code>boolean</code> occurs.</em>
 * </p>
 * <p>
 * When checkboxes are part of a group, an <code>ArrayList</code> of
 * selected checkboxes is maintained. If any checkboxes within a group are
 * selected, a request attribute whose name is the value of the
 * <code>name</code> property is created and added to the
 * <code>RequestMap</code>. The request attribute value is an
 * <code>ArrayList</code> containing the value of the
 * <code>selectedValue</code> property of each selected
 * checkbox. If no checkboxes are selected, no request attribute is
 * created. The <code>selected</code> property of each selected checkbox
 * within the group will also contain the value of the
 * <code>selectedValue</code> property of the respective selected checkbox.
 * </p>
 * <h3>Using a <code>checkbox</code> tag as a boolean control</h3>
 * <p>
 * If the <code>selectedValue</code> property is not specified or its
 * value is <code>null</code> then the checkbox behaves like a
 * boolean control.
 * </p>
 * <p>
 * To use the <code>Checkbox</code> as a boolean control, do not
 * specify a value for the <code>selectedValue</code> property. The
 * checkbox is selected if the <code>selected</code> property is not
 * null and has the value of a Boolean instance with a <code>true</code>
 * value. If the checkbox is not selected, then the value of the
 * <code>selected</code> property is a false <code>Boolean</code> instance.
 * </p>
 * <p><em>Note that using a boolean checkbox in a group and
 * referencing the request property for the selected checkboxes is not
 * useful, since the value of the request property will be an <code>ArrayList
 * </code> of indistinguishable <code>true</code> values.</em>
 * </p>
 * <h3>Using a <code>Checkbox</code> to represent a developer defined
 * value</h3>
 * <p> The <code>selectedValue</code> property can be assigned a
 * developer defined object value to represent the value of a selected
 * checkbox. If the checkbox is selected, the value of the <code>selected</code>
 * property is assigned the value of the <code>selectedValue</code>
 * property.
 * </p>
 * <p>
 * If the value of the <code>selectedValue</code> property is a
 * developer defined object, a <code>Converter</code> must be registered
 * to convert to and from a <code>String</code> value.<br>
 * In addition the object must support an
 * <code>equals</code> method that returns <code>true</code> when the 
 * value of the <code>selectedValue</code> property is compared to
 * the <code>selected</code> property value in order to detect a
 * selected checkbox.
 * </p>
 * <h3>Using a <code>Checkbox</code> as one control in a group</h3>
 * <p>
 * The <code>name</code> property determines whether a
 * checkbox is part of a group. A checkbox is treated as part of a group
 * of checkboxes if the <code>name</code> property of the checkbox is
 * assigned a value equal to the <code>name</code> property of the other
 * checkboxes in the group. In other words, all checkboxes of a group have the
 * same <code>name</code> property value. The group behaves
 * like a multiple selection list, where zero or more checkboxes
 * can be selected. The value of the name property must
 * be unique within the scope of the Form parent containing the
 * checkboxes.
 * </p>
 * <h3>Facets</h3>
 * <p>
 * The following facets are supported:
 * </p>
 * <ul>
 *   <li><em>image</em> If the image facet exists, it replaces the 
 *	{@link com.sun.rave.web.ui.component.ImageComponent} subcompoent
 *	normally created for the image associated with the checkbox
 *	if the <code>imageURL</code> property is not null.</li>
 *   <li><em>label</em> If the label facet exists, it replaces the
 *	{@link com.sun.rave.web.ui.component.Label} subcomponent normally
 *	created for the label associated with the checkbox, if the
 *	label property is not null.</li>
 * </ul>
 * <p>
 * Add an image or label facet to the <code>Checkbox</code> if more
 * control over the properties of the subcomponents is needed.
 * </p>
 * <p>
 * <em>Note that if a facet is exists, <code>Checkbox</code> properties
 * that would normally be assigned to the created subcomponent, will
 * not be assigned to the facet</em>
 * </p>
 * <p>
 * <em>Note that unexpected layout of the <code>Checkbox</code> may occur
 * if the component specified by the facet is not a
 * {@link com.sun.rave.web.ui.component.ImageComponent} for the image facet or
 * {@link com.sun.rave.web.ui.component.Label} for the label facet.</em>
 * </p>
 * <h3>ImageComponent and Label subcomponents</h3>
 * <p>
 * An image and a label may be associated with the <code>Checkbox</code>.<br/>
 * If the <code>imageURL</code> property is not null and an image facet
 * does not exist then a {@link com.sun.rave.web.ui.component.ImageComponent}
 * component is created.<br/>
 * If the <code>label</code> property is not null and a label facet does not
 * exist then a {@link com.sun.rave.web.ui.component.Label} component is
 * created.
 * </p>
 * <p>
 * The following <code>Checkbox</code> properties are assigned to the
 * subcomponents only if a facet does not exist.<br/>
 * For the {@link com.sun.rave.web.ui.component.ImageComponent} subcomponent
 * <ul>
 * <li>this.getId() + "_image" is assigned to the <code>id</code> property.</li>
 * <li>this.getImageURL() is assigned to the <code>url</code> property.</li>
 * <li>this.getToolTip() is assigned to the <code>toolTip</code> property.</li>
 * <li>this.getToolTip() is assigned to the <code>alt</code> property.</li>
 * <li>this.isVisible() is assigned to the <code>visible</code> property.</li>
 * <li>this.isRendered() is assigned to the <code>renderer</code> property.</li>
 * </ul>
 * </p>
 * <p>
 * For the {@link com.sun.rave.web.ui.component.Label} subcomponent
 * <ul>
 * <li>this.getId() + "_label" is assigned to the <code>id</code>
 * property.</li>
 * <li>this.getClientId() is assigned to the <code>for</code>
 * property.</li>
 * <li>this.getLabel() is assigned to the <code>text</code> property.</li>
 * <li>this.getLabelLevel is assigned to the <code>labelLevel</code> property.</li>
 * <li>this.getToolTip is assigned to the <code>toolTip</code> property.</li>
 * <li>this.isVisible is assigned to the <code>visible</code> property.</li>
 * <li>this.isRendered is assigned to the <code>renderer</code> property.</li>
 * </ul>
 * </ul>
 * </p>
 * <em>Note that if a value binding exists for one of the <code>Checkbox</code>
 * properties mentioned above, the value binding is set on the subcomponent
 * for that property.</em>
 * </p>
 */
public class Checkbox extends CheckboxBase {

    /**
     * Constructor for a <code>Checkbox</code>.
     */
    public Checkbox() {
	super();
	// When used in a group you can choose multiple
	// and this behavior is provided, but the single
	// implementation of Checkbox vs. CheckboxGroup
	// does not need Multiple to be explicit.
	//
	setMultiple(false);
    }

    /**
     * Return an <code>ArrayList</code> containing the value of the
     * <code>selectedValue</code> property of each selected checkbox
     * in the group of checkboxes identified by the <code>name</code>
     * parameter.
     * A <code>Checkbox</code> is one of a group of checkboxes
     * if more than on checkbox has the same value for the
     * <code>name</code> property.<br/>
     * When one of the checkboxes among that group is selected,
     * the value of its <code>selectedValue</code> property 
     * is maintained within an <code>ArrayList</code> that is stored
     * in a request attribute identified by the value of its <code>name</code>
     * property.
     *
     * @param name the value a Checkbox name property.
     */
    public static ArrayList getSelected(String name) {

	Map rm = FacesContext.getCurrentInstance().getExternalContext().
		getRequestMap();

	if (name != null) {
	    return (ArrayList)rm.get(name);
	} else {
	    return null;
	}
    }


    /**
     * <p>Update the request parameter that holds the value of the
     * <code>selectedValue</code> property of the selected check box.</p>
     * If the <code>name</code> property has been set
     * a request attribute is created.
     * The value of the <code>name</code> property will
     * be used for the request attribute name and the value of the request 
     * attribute will be an <code>ArrayList</code> containing the value of the
     * <code>selectedValue</code> property of the selected check boxes
     * that have the same <code>name</code> property value.
     * If no check box is selected then no request attribute
     * will be created.
     * </p>
     * <p>
     * The request attribute described above is available during
     * a <code>ValueChangeEvent</code>.
     * </p>
     *
     * @param context The context of this request.
     */
    public void validate(FacesContext context) {

	super.validate(context);

	// If not valid or in a group don't add the checkbox to
	// the request map.
	//
	if (!isValid()) {
	    return;
	}
	String groupName = getName();
	if (groupName == null) {
	    return;
	}

	// If the submitted value is valid and the 
	// checkbox is selected add it to the 
	// request map array list. To check if the component
	// is selected, can't call "isChecked" or getValue()
	// cause if there is a value binding, it will return the previously
	// selected state and not the state of this submit
	// so use getLocalValue() which is set if isValid is true.
	//
	Object selected = getLocalValue();
	if (!getSelectedValue().equals(selected)) {
	    return;
	}

	addToRequestMap(context, groupName, selected);
    }

    protected void addToRequestMap(FacesContext context, String groupName,
		Object selected) {

	Map requestMap = context.getExternalContext().getRequestMap();
	ArrayList selectedCB = (ArrayList)requestMap.get(groupName);
	if (selectedCB == null) {
	    selectedCB = new ArrayList();
	    requestMap.put(groupName, selectedCB);
	}
	if (!selectedCB.contains(selected)) {
	    selectedCB.add(selected);
	}
    }
}
