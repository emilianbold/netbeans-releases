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
package com.sun.rave.web.ui.renderer;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.rave.web.ui.component.RadioButton;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.MessageUtil;
import com.sun.rave.web.ui.util.ThemeUtilities;

/**
 * <p>
 * The <code>RadioButtonRenderer</code> renders a
 * {@link com.sun.rave.web.ui.component.RadioButton} component.
 * </p>
 * <h3>Encoding</h3>
 * <p>
 * The <code>RadioButtonRenderer</code> renders a <code>RadioButton</code> as:
 * <ul>
 * <li> An INPUT element of type radio for each radio button.
 * </li>
 * <li> An optional image. The component rendered for this feature is obtained
 * from a call to <code>getImageComponent()</code> on the component being
 * rendered. </li>
 * <li> An optional label. The component rendered for this feature is obtained
 * from a call to <code>getLabelComponent()</code> on the component being
 * rendered. </li>
 * </li>
 * </ul>
 * </p>
 * <p>
 * The CSS selectors for the elements and components that comprise a
 * radio button are identified by java
 * constants defined in the {@link ThemeStyles} class.
 * </p>
 * <ul>
 * <li>RADIOBUTTON for the INPUT element</li>
 * <li>RADIOBUTTON_DISABLED for the INPUT element of a disabled radio
 * button</li>
 * <li>RADIOBUTTON_LABEL for label component if a label is rendered</li>
 * <li>RADIOBUTTON_LABEL_DISABLED for a label component of a disabled
 * radio button, if a label is rendered</li>
 * <li>RADIOBUTTON_IMAGE for an image component if an image is rendered</li>
 * <li>RADIOBUTTON_IMAGE_DISABLED for an image component of a disabled 
 * radio button if an image is rendered.</li>
 * </ul>
 * <em>Note that these selectors are appended to any existing selectors
 * that may already exist on the <code>styleClass</code> property of the
 * <code>RadioButton</code> component and the optional image and label
 * components.</em>
 * <p>
 * For more details on the encoding the
 * <code>RadioButton</code> component see the super class
 * {@link com.sun.rave.web.ui.renderer.RbCbRendererBase}
 * </p>
 * <p>
 * <h3>Decoding</h3>
 * <p>
 * If the INPUT element representing a radio button is selected on the
 * the client, the submitted request will contain a request parameter
 * whose name is the value of the name attribute of the selected
 * HTML INPUT element. The value of the request parameter will be the
 * value of the value attribute of the selected HTML INPUT element.
 * </p>
 * <p>
 * The component being decoded is selected if the component's
 * <code>isDisabled</code> and <code>isReadOnly</code> methods
 * return false and:
 * </p>
 * <ul>
 * <li>a request parameter exists that is equal to its <code>name</code>
 * property. If the <code>name</code> property is null, then a
 * request parameter exists that is equal to its <code>clientId</code>
 * property.
 * <li/>
 * </ul>
 * <p>
 * And
 * </p>
 * <ul>
 * <li>the request parameter's value is <code>String.equal</code> to the
 * the component's <code>selectedValue</code> property, after conversion
 * to a <code>String</code>, by calling
 * <code>ConversionUtilities.convertValueToString</code>. If the component
 * was encoded as a boolean control, then the request parameter's value
 * must be equal to the component's <code>clientId</code> property.
 * </li>
 * </ul>
 * <p>
 * If selected, a <code>String[1]</code> array is assigned as the component's
 * submitted value where the single array element is the <code>String</code>
 * version of the <code>selectedValue</code> property or "true" if the
 * component was encoded as a boolean control.<br/>
 * If not selected, a <code>String[0]</code> array is assigned as the
 * component's submitted value or a <code>String[1]</code> array where the
 * single array element is "false" if the component was encoded as a 
 * boolean control.
 * </p>
 * <p>
 * If the component's <code>isDisabled</code> or <code>isReadOnly</code>
 * methods return true no submitted value is assigned to the component,
 * and results in a null submitted value implying the component
 * was not submitted, and the state of the component is unchanged.
 * <p>
 * Since the <code>RadioButtonRenderer</code> only renders a single 
 * <code>RadioButton</code> component it cannot enforce that at least
 * one radio button should be selected among a group of <code>RadioButton</code>
 * components with the same <code>name</code> property.
 * </p>
 * <p>
 * If the <code>RadioButton</code> is selected, the <code>selected<code>
 * property will be the same value as the <code>selectedValue</code>
 * property. If more than one <code>RadioButton</code> component is
 * encoded with the same <code>name</code> property and more than one
 * <code>RadioButton</code>'s is selectred,
 * the last selected <code>RadioButton</code> component that is encoded
 * will be appear as checked in the HTML page. Subsequently during the
 * next submit, only the checked <code>RadioButton</code> component
 * will be selected.
 * </p>
 */
public class RadioButtonRenderer extends RbCbRendererBase {
    

    private final String MSG_COMPONENT_NOT_RADIOBUTTON =
	"RadioButtonRenderer only renders RadioButton components.";

    /**
     * Creates a new instance of RadioButtonRenderer
     */
    public RadioButtonRenderer() {
        super();
    }
    
    /**
     * <p>Decode the <code>RadioButton</code> selection.</p>
     * <p>
     * If the value of the component's <code>name</code> property
     * has been set, the value is used to match a request parameter.
     * If it has not been set the component clientId is used to match
     * a request parameter. If a match is found, and the value of the 
     * of the request parameter matches the <code>String</code> value of the 
     * component's <code>selectedValue</code> property, the
     * radio button is selected. The component's submitted value is
     * assigned a <code>String[1]</code> array where the single array
     * element is the matching parameter value.
     * </p>
     * <p>
     * If no matching request parameter or value is found, an instance of 
     * <code>String[0]</code> is assigned as the submitted value,
     * meaning that this is a component was not selected.
     * </p>
     * <p>
     * If the component was encoded as a boolean control the
     * value of the matching request attribute will be the component's
     * <code>clientId</code> property if selected. If selected the 
     * submitted value is <code>new String[] { "true" }</code>
     * and <code>new String[] { "false" }</code> if not selected.
     * </p>
     * <p>
     * It is the developer's responsibility to ensure the validity of the
     * <code>name</code> property (the name attribute on the
     * INPUT element) by ensuring that it is unique to the radio buttons
     * for a given group within a form.
     * </p>
     *
     * @param context FacesContext for the request we are processing.
     * @param component The <code>RadioButton</code>
     * component to be decoded.
     */
    public void decode(FacesContext context, UIComponent component) {

	// We need to know the last state of the component before decoding
	// this radio button. This disabled check is not to determine
	// if the radio button was disabled on the client.
	// We assume that the disabled state is in the same state as it was
	// when this radio button was last rendered.
	// If the radio button was disabled then it can not have changed on
	// the client. We ignore the case that it might have been
	// enabled in javascript on the client.
	// This allows us to distinguish that no radio button was selected.
	// No radio buttons are selected when "isDisabled || isReadOnly -> false
	// and no request parameters match the name attribute if part of a
	// group or the clientId if a single radio button.
	//
        if (isDisabled(component) || isReadOnly(component)) {
	    return;
	}
	// If there is a request parameter that that matches the
	// name property, this component is one of the possible
	// selections. We need to match the value of the parameter to the
	// the component's value to see if this is the selected component.
	//
	RadioButton radioButton = (RadioButton)component;
	String name = radioButton.getName();
	boolean inGroup = name != null;

	// If name is null use the clientId.
	//
	if (name == null) {
	    name = component.getClientId(context);
	}

	Map requestParameterMap = context.getExternalContext().
	    getRequestParameterMap();

	// The request parameter map contains the INPUT element
	// name attribute value as a parameter. The value is the
	// the "selectedValue" value of the RadioButton component.
	//
	if (requestParameterMap.containsKey(name)) {

	    String newValue = (String)requestParameterMap.get(name);

	    // We need to discern the case where the radio button
	    // is part of a group and it is a boolean radio button.
	    // If the radio button is part of a group and it is a
	    // boolean radio button then the submitted value contains the
	    // value of "component.getClientId()". If 
	    // the value was not a unique value within the group
	    // of boolean radio buttons, then all will appear selected,
	    // since name will be the same for all the radio buttons
	    // and the submitted value would always be "true" and then
	    // every radio button component in the group would decode
	    // as selected. Due to the HTML implementation of radio
	    // buttons, only the last radio button will appear selected.
	    //
            Object selectedValue = radioButton.getSelectedValue();
	    String selectedValueAsString = null;

	    if (inGroup && selectedValue instanceof Boolean) {
		selectedValueAsString = component.getClientId(context);
		// Use the toString value of selectedValue even if
		// it is a Boolean control, in case the application
		// wants "FALSE == FALSE" to mean checked.
		//
		if (selectedValueAsString.equals(newValue)) {
		    ((UIInput)component).setSubmittedValue(
			    new String[] { selectedValue.toString() });
		    return;
		}
	    } else {
		selectedValueAsString =
		    ConversionUtilities.convertValueToString(component,
			selectedValue);
		if (selectedValueAsString.equals(newValue)) {
		    ((UIInput)component).setSubmittedValue(
			    new String[] { newValue });
		    return;
		}
	    }
	    // Not selected possibly deselected.
	    // 
	    ((UIInput) component).setSubmittedValue(new String[0]);
	}
	return;
    }

    /**
     * Ensure that the component to be rendered is a RadioButton instance.
     * Actual rendering occurs during <code>renderEnd</code>
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     */
    public void renderStart(FacesContext context, UIComponent component,
	ResponseWriter writer)
	throws IOException {

	// Bail out if the component is not a RadioButton component.
	// This message should be logged.
	//
	if (!(component instanceof RadioButton)) {
	    throw new
		IllegalArgumentException(MSG_COMPONENT_NOT_RADIOBUTTON);
	}
    }

    /**
     * RadioButtonRenderer renders the entire RadioButton
     * component within the renderEnd method.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     */
    public void renderEnd(FacesContext context, UIComponent component,
	ResponseWriter writer)
	throws IOException {

	Theme theme = ThemeUtilities.getTheme(context);
	renderSelection(context, component, theme, writer, "radio");
        
    }

    /**
     * Return true if the <code>component</code> is selected, false
     * otherwise.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to test for selected.
     */
    protected boolean isSelected(FacesContext context, UIComponent component) {
	return ((RadioButton)component).isChecked();
    }

    protected String[] styles = {
	ThemeStyles.RADIOBUTTON,	 	/* INPUT */
	ThemeStyles.RADIOBUTTON_DISABLED, 	/* INPUT_DIS */
	ThemeStyles.RADIOBUTTON_LABEL,		/* LABEL */
	ThemeStyles.RADIOBUTTON_LABEL_DISABLED, /* LABEL_DIS */
	ThemeStyles.RADIOBUTTON_IMAGE,		/* IMAGE */
	ThemeStyles.RADIOBUTTON_IMAGE_DISABLED, /* IMAGE_DIS */
	ThemeStyles.RADIOBUTTON_SPAN,		/* SPAN */
	ThemeStyles.RADIOBUTTON_SPAN_DISABLED	/* SPAN_DIS */
    };

    /**
     * Return the style class name for the structural element indicated
     * by <code>styleCode</code>
     *
     * @param theme The Theme for this request.
     * @param styleCode identifies the style class for the element about
     * to be rendered.
     */
    protected String getStyle(Theme theme, int styleCode) {
	String style = null;
	try {
	    style = theme.getStyleClass(styles[styleCode]);
	} catch (Exception e) {
	    // Don't care
	}
	return style;
    }
}
