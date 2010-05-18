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
import java.util.ArrayList;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.el.ValueBinding;
import javax.faces.render.Renderer;

import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.ImageComponent;
import com.sun.rave.web.ui.component.util.Util;

/**
 * Superclass for Checkbox and RadioButton.
 */
public class RbCbSelector extends RbCbSelectorBase {

    /**
     * Image facet name.
     */
    public final static String IMAGE_FACET = "image"; //NOI18N

    // Properties to transfer from "this" to the image subcomponent
    //
    private final static String IMAGE_URL_PROP = "imageURL";
    private final static String URL_PROP = "url";
    private final static String ALT_PROP = "alt";

    /**
     * Label facet name.
     */
    public final static String LABEL_FACET = "label"; //NOI18N

    // Properties to transfer from "this" to the label subcomponent
    //
    private final static String LABEL_PROP = "label";
    private final static String TEXT_PROP = "text";
    private final static String LABEL_LEVEL_PROP = "labelLevel";

    // Props for both subcomponents
    //
    private final static String TOOLTIP_PROP = "toolTip";
    private final static String VISIBLE_PROP = "visible";
    private final static String RENDERED_PROP = "rendered";

    private final static String ID_SEPARATOR = "_"; //NOI18N

    // This is the default value for selectedValue.
    // Because of the generation and alias of "items"
    // (Need to reconsider this inheritance)
    // its not possible to set up a default value.
    // If selectedValue is not set, then allow this
    // component to behave as a boolean control.
    // The component is selected if both "selected" and
    // "selectedValue" are "true".
    //
    private final static Boolean trueSelectedValue = Boolean.TRUE;

    public RbCbSelector() { 
	super();
    } 
    
    /**
     * Implemented by subclasses in order to reflect the selection
     * state of this component id part of a group.
     * This method is called if the component is part of a group.
     * 
     * @param context the context for this request.
     * @param groupName the value of the <code>name</code> property.
     */
    protected void addToRequestMap(FacesContext context, String groupName) {
    }

    /**
     * Encode the component.
     * <p>
     * If this component is part of a group, ensure that the initial
     * state is reflected in the request map by calling
     * <code>addToRequestMap</code>.
     * </p>
     * @param context the context for this request.
     */
    // Implement this here to initialize the RequestMap ArrayList
    // of selected grouped checkboxes, so that initially selected
    // check boxes are available on the first render cycle
    //
    public void encodeBegin(FacesContext context) throws IOException {

        if (context == null) {
            throw new NullPointerException();
        }
        if (!isRendered()) {
            return;
        }

	// If the checkbox or radio button isn't valid, or
	// not in a group or not
	// selected, don't put it in the RequestMap.
	//
	String groupName = getName();
	if (groupName == null || !isValid() || !isChecked()) {
	    return;
	}

	addToRequestMap(context, groupName);

        String rendererType = getRendererType();
        if (rendererType != null) {
            getRenderer(context).encodeBegin(context, this);
        }

    }

    /**
     * Convert the <code>submittedValue</code> argument.
     * <p>
     * If there is a renderer for this component,
     * its <code>getConvertedValue()</code> method is called and
     * the value returned by that method is returned. 
     * </p>
     * <p>
     * If there is no renderer, and <code>submittedValue</code> is not
     * an instance of <code>String[]</code> or
     * <code>String</code> a <code>ConverterException</code> is thrown.
     * </p>
     * <p>
     * The <code>submittedValue</code> indicates selected if it is
     * <code>String[1].length() != 0</code> or 
     * <code>String.length() != 0</code>.
     * </p>
     * If not selected and <code>getSelectedValue()</code> returns an
     * instance of <code>Boolean</code>, <code>Boolean.FALSE</code> is
     * returned.
     * </p>
     * <p>
     * If not selected and it's not a boolean control then an unselected
     * value is returned appropriate for the type of the <code>selected</code>
     * property. If the type of the <code>selected</code> property evaluates
     * to a primitive type by virtue of a value binding the appropriate
     * <code>MIN_VALUE</code> constant is returned. For example if the
     * type is <code>int</code>, <code>new Integer(Integer.MIN_VALUE)</code>
     * is returned.<br/>
     * If the type is not a primitive value <code>""</code> is returned.
     * </p>
     * <p>
     * If the control is selected
     * <code>ConversionUtilities.convertValueToObject()</code> is called to
     * convert <code>submittedValue</code>.
     * </p>
     * <p>
     * If <code>ConversionUtilities.convertValueToObject()</code> returns
     * <code>submittedValue</code>, the value of the 
     * <code>getSelectedValue()</code> property
     * is returned, else the value returned by 
     * <code>ConversionUtilities.convertValueToObject()</code> is returned.
     * </p>
     * @param context the context of this request.
     * @param submittedValue the submitted String value of this component.
     */
    public Object getConvertedValue(FacesContext context, 
				    Object submittedValue)
	    throws ConverterException {

	// First defer to the renderer.
	//
	Renderer renderer = getRenderer(context);
	if (renderer != null) {
	    return renderer.getConvertedValue(context, this, submittedValue);
	}
	return getConvertedValue(context, this, submittedValue);
    }


    /**
     * Convert the <code>submittedValue</code> argument.
     * <p>
     * If <code>submittedValue</code> is not
     * an instance of <code>String[]</code> or
     * <code>String</code> a <code>ConverterException</code> is thrown.
     * </p>
     * <p>
     * The <code>submittedValue</code> indicates selected if it is
     * <code>String[1].length() != 0</code> or 
     * <code>String.length() != 0</code>.
     * </p>
     * If not selected and <code>getSelectedValue()</code> returns an
     * instance of <code>Boolean</code>, <code>Boolean.FALSE</code> is
     * returned.
     * </p>
     * <p>
     * If not selected and it's not a boolean control then an unselected
     * value is returned appropriate for the type of the <code>selected</code>
     * property. If the type of the <code>selected</code> property evaluates
     * to a primitive type by virtue of a value binding the appropriate
     * <code>MIN_VALUE</code> constant is returned. For example if the
     * type is <code>int</code>, <code>new Integer(Integer.MIN_VALUE)</code>
     * is returned.<br/>
     * If the type is not a primitive value <code>""</code> is returned.
     * </p>
     * <p>
     * If the control is selected
     * <code>ConversionUtilities.convertValueToObject()</code> is called to
     * convert <code>submittedValue</code>.
     * </p>
     * <p>
     * If <code>ConversionUtilities.convertValueToObject()</code> returns
     * <code>submittedValue</code>, the value of the 
     * <code>getSelectedValue()</code> property
     * is returned, else the value returned by 
     * <code>ConversionUtilities.convertValueToObject()</code> is returned.
     * </p>
     * @param context the context of this request.
     * @param component an RbCbSelector instance.
     * @param submittedValue the submitted String value of this component.
     */
    public Object getConvertedValue(FacesContext context, 
	    RbCbSelector component, Object submittedValue)
	    throws ConverterException {

	// This would indicate minimally not selected
	//
	if (submittedValue == null) {
	    throw new ConverterException(
	    "The submitted value is null. " + //NOI18N
	    "The submitted value must be a String or String array.");//NOI18N
	}

	// Expect a String or String[]
	// Should be made to be just String.
	//
	boolean isStringArray = submittedValue instanceof String[];
	boolean isString = submittedValue instanceof String;
	if (!(isStringArray || isString)) {
	    throw new ConverterException(
	    "The submitted value must be a String or String array.");//NOI18N
	}

	String rawValue = null;
	if (isStringArray) {
	    if (((String[])submittedValue).length > 0) {
		rawValue = ((String[])submittedValue)[0];
	    }
	} else if (isString) {
	    rawValue = (String)submittedValue;
	}

	// Need to determine if the submitted value is not checked
	// and unchanged. If it is unchecked, rawValue == null or
	// rawValue == "". Compare with the rendered value. If the
	// rendered value is "" or null, then the component is unchanged
	// and if the rendered value was not null, try and convert it.
	//
	boolean unselected = rawValue == null || rawValue.length() == 0;

	// If the component was unselected then we need to know if it
	// was rendered unselected due to a value that was an empty
	// string or null. If it is was submitted as unselected
	// and rendered as unselected, we need the rendered value that
	// implied unselected, since it may not null, just different
	// than "selectedValue"
	//
	Object newValue = null;
	Object selectedValue = getSelectedValue();
	if (unselected) {
	    newValue = ConversionUtilities.convertRenderedValue(context,
		rawValue, this);
	    // Determine the unselected value for Boolean controls
	    // if the converted value is null but the the component
	    // value wasn't rendered as null.
	    // For example if the control rendered as null, and is 
	    // still unselected, then we don't want to return FALSE
	    // for a Boolean control, since it is unchanged.
	    // But if it has changed and is unselected then return
	    // the unselected value of FALSE.
	    //
	    if (!ConversionUtilities.renderedNull(component) &&
		    selectedValue instanceof Boolean &&
		    newValue == null) {

		// return the complement of the selectedValue
		// Boolean value.
		//
		newValue = ((Boolean)selectedValue).booleanValue() ?
			Boolean.FALSE : Boolean.TRUE;
	    }
	    return getUnselectedValue(context, component, newValue);
	} else {
	    newValue = ConversionUtilities.convertValueToObject
			(component, rawValue, context);
	    return newValue == rawValue ? selectedValue : newValue;
	}
    }

    private Object getUnselectedValue(FacesContext context,
	    UIComponent component, Object noValue) {

	// Determine the type of the component's value object
        ValueBinding valueBinding =
                component.getValueBinding("value"); //NOI18N
        
	// If there's no value binding we don't care
	// since the local value is an object and can support null or ""
	//
        if (valueBinding == null) {
            return noValue;
	} 
        // We have found a valuebinding.
        Class clazz = valueBinding.getType(context);
        
        // Null class
        if (clazz == null) {
            return noValue;
        }
	// Pass noValue for use in primitive boolean case.
	// If the "selectedValue" was Boolean.FALSE, unselected
	// will be Boolean.TRUE.
	//
	if (clazz.isPrimitive()) {
	    return getPrimitiveUnselectedValue(clazz, noValue);
	}

	// bail out
	return noValue;
    }

    private Object getPrimitiveUnselectedValue(Class clazz,
	    Object booleanUnselectedValue) {

	// it MUST be at least one of these
	//
	if (clazz.equals(Boolean.TYPE)) {
	    return booleanUnselectedValue;
	} else if (clazz.equals(Byte.TYPE)) {
	    return new Integer(Byte.MIN_VALUE);
	} else if (clazz.equals(Double.TYPE)) {
	    return new Double(Double.MIN_VALUE);
	} else if (clazz.equals(Float.TYPE)) {
	    return new Float(Float.MIN_VALUE);
	} else if (clazz.equals(Integer.TYPE)) {
	    return new Integer(Integer.MIN_VALUE);
	} else if (clazz.equals(Character.TYPE)) {
	    return new Character(Character.MIN_VALUE);
	} else if (clazz.equals(Short.TYPE)) {
	    return new Short(Short.MIN_VALUE);
	} else { 
	    // if (clazz.equals(Long.TYPE)) 
	    return new Long(Long.MIN_VALUE);
	}
    }
    /**
     * Return the value of the <code>selectedValue</code> property.
     * If <code>selectedValue</code> is null, then a <code>Boolean</code>
     * true instance is returned and the control will behave as a
     * boolean control.
     */
    public Object getSelectedValue() {
	Object sv = super.getSelectedValue();
	return sv == null ? trueSelectedValue : sv;
    }

    // Hack to overcome introspection of "isSelected"
    //
    /**
     * Return <code>true</code> if the control is checked.
     * A control is checked when the <code>selectedValue</code> property is
     * equal to the <code>selected</code> property.
     */
    public boolean isChecked() {
	Object selectedValue = getSelectedValue();
	Object selected = getSelected();
	if (selectedValue == null || selected == null) {
	    return false;
	}
	// Need to support "selected" set to a constant String
	// such as "true" or "false" when it is a boolean control.
	// This does not include when selected is bound to a String
	//
	if (getValueBinding("selected") == null && //NOI18N
	    selected instanceof String && selectedValue instanceof Boolean) {
	    return selectedValue.equals(Boolean.valueOf((String)selected));
	} else {
	    return selected.equals(selectedValue);
	}
    }

    /**
     * Returns a </code>UIComponent</code> that represents the label
     * for this component.
     * <p>
     * If a "label" facet exists, the <code>UIComponent</code> associated
     * with that facet is returned.<br/>
     * If a "label" facet does not exist and the <code>label</code>
     * property is not null, <code>createLabelComponent</code> is called
     * to create the label component.
     * </p>
     */
    public UIComponent getLabelComponent() {

	UIComponent labelComponent = getFacet(LABEL_FACET);
	if (labelComponent != null) {
	    return labelComponent;
	}
	return getLabel() != null ? createLabelComponent() : null;
    }

    /**
     * Returns a </code>UIComponent</code> that represents the image
     * for this component.
     * <p>
     * If an "image" facet exists, the <code>UIComponent</code> associated
     * with that facet is returned.<br/>
     * If an "image" facet does not exist and the <code>imageURL</code>
     * property is not null, <code>createImageComponent()</code> is called
     * to create the image component.
     * </p>
     */
    public UIComponent getImageComponent() {
	UIComponent imageComponent = getFacet(IMAGE_FACET);
	if (imageComponent != null) {
	    return imageComponent;
	}
	return getImageURL() != null ? createImageComponent() : null;
    }

    /**
     * Create a <code>com.sun.rave.web.ui.component.Label</code> to represent
     * the label for this component.
     */
    protected UIComponent createLabelComponent() {
	Label label = new Label();
        label.setId(getId() + ID_SEPARATOR + LABEL_FACET);
	label.setParent(this);

	if (label != null) {
	    label.setLabeledComponent(this);
	    if (!passValueBinding(label, LABEL_PROP, TEXT_PROP)) {
		label.setText(getLabel());
	    }
	    if (!passValueBinding(label, LABEL_LEVEL_PROP, LABEL_LEVEL_PROP)) {
		label.setLabelLevel(getLabelLevel());
	    }
	    if (!passValueBinding(label, TOOLTIP_PROP, TOOLTIP_PROP)) {
		label.setToolTip(getToolTip());
	    }
	    /* Don's transfer these properties. They shouldn't have
	     * to be passed on unless, "rendersChildren" == false
	     *
	    if (!passValueBinding(label, VISIBLE_PROP, VISIBLE_PROP)) {
		label.setVisible(isVisible());
	    }
	    if (!passValueBinding(label, RENDERED_PROP, RENDERED_PROP)) {
		label.setRendered(isRendered());
	    }
	     */
	}
	return label;
    }

    /**
     * Create a </code>com.sun.rave.web.ui.component.ImageComponent</code>
     * to represent the image for this component.
     */
    protected UIComponent createImageComponent() {

	ImageComponent image = new ImageComponent();
        image.setId(getId() + ID_SEPARATOR + IMAGE_FACET);
	image.setParent(this);

	if (image != null) {
	    if (!passValueBinding(image, IMAGE_URL_PROP, URL_PROP)) {
		image.setUrl(getImageURL());
	    }
	    if (!passValueBinding(image, TOOLTIP_PROP, TOOLTIP_PROP)) {
		image.setToolTip(getToolTip());
	    }
	    if (!passValueBinding(image, TOOLTIP_PROP, ALT_PROP)) {
		image.setAlt(getToolTip());
	    }
	    /* Don's transfer these properties. They shouldn't have
	     * to be passed on unless, "rendersChildren" == false
	     *
	    if (!passValueBinding(image, VISIBLE_PROP, VISIBLE_PROP)) {
		image.setVisible(isVisible());
	    }
	    if (!passValueBinding(image, RENDERED_PROP, RENDERED_PROP)) {
		image.setRendered(isRendered());
	    }
	     */
	}
	return image;
    }

    private boolean passValueBinding(UIComponent component,
		String fromAttr, String toAttr) {

	ValueBinding vb = getValueBinding(fromAttr);
	if (vb != null) {
	    component.setValueBinding(toAttr, vb);
	}
	return vb != null;
    }
}
