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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.ConverterException;

import com.sun.rave.web.ui.component.ImageComponent;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.RadioButton;
import com.sun.rave.web.ui.component.RbCbSelector;
import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.RenderingUtilities;

/**
 * <p>
 * The <code>RbCbRendererBase</code> class is the abstract base class for
 * {@link com.sun.rave.web.ui.renderer.RadioButtonRenderer} and
 * {@link com.sun.rave.web.ui.renderer.CheckboxRenderer}.
 * </p>
 * <p>
 * <code>RbCbRendererBase</code> provides encoding functionality for
 * the <code>RadioButtonRenderer</code> and <code>CheckboxRenderer</code>.
 * This includes an implementation of <code>getConvertedValue</code>, and
 * a method called <code>renderSelection</code> which a subclass calls to render
 * either a <code>Checkbox</code> or a <code>RadioButton</code> component
 * at the appropriate time.<br/>
 * The renderer subclass must implement 
 * <p>
 * <ul>
 * <li><code>isSelected</code> in order for this class to generically render
 * either component</li>
 * <li><code>getStyle</code> so the specific subclass can specify the 
 * appropriate <code>ThemeStyle</code> constants.</li>
 * </ul>
 * </p>
 * <h3>Decoding</h3>
 * <p>
 * See {@link com.sun.rave.web.ui.renderer.RadioButtonRenderer} and
 * {@link com.sun.rave.web.ui.renderer.CheckboxRenderer} for details on
 * decoding requests.
 * </p>
 * <h3>Encoding</h3>
 * <p>
 * The renderer emits the following HTML elements.
 * <ul>
 * <li> An INPUT element of type specified by the subclass in
 * <code>renderSelection</code>
 * </li>
 * <li> An optional {@link com.sun.rave.web.ui.component.ImageComponent} 
 * component is rendered for each INPUT element
 * </li>
 * <li> An optional {@link com.sun.rave.web.ui.component.Label} component
 * is rendered for each INPUT element
 * </li>
 * </ul>
 * </p>
 * <p>
 * The ID attributes for HTML elements are constructed as follows,
 * where &lt;cid&gt; is the <code>clientId</code> of the 
 * component being rendered.
 * <p>
 * <lo>
 * <li> &lt;cid&gt; for the INPUT element
 * </li>
 * <li> &lt;cid&gt;_image for the image component
 * </li>
 * <li> &lt;cid&gt;_label for the label component
 * </li>
 * </lo>
 * <h1>Encoding the INPUT element</h1>
 * <p>
 * If the <code>name</code> property of the component is <code>null</code>
 * the name attribute of the INPUT element will be set to the
 * value of the component's <code>clientId</code> and the control will
 * not be part of a group, and behave as an individual control.<br/>
 * If the <code>name</code> property is not <code>null</code> then its
 * value is used as the value of the name attribute of the HTML INPUT
 * element and the control will behave as part of a group.
 * </p>
 * <p>
 * The <code>ConversionUtilities.getValueAsString</code> method is called with
 * the value of the component's <code>selectedValue</code> property
 * and the result is used as the value of the HTML INPUT element's
 * value attribute. The <code>String</code> value that is returned may be
 * the actual value of the <code>selectedValue</code> property or
 * the result of a conversion of a developer defined object value
 * to a <code>String</code> or "true" if the <code>selectedValue</code>
 * property was null or never assigned a value.<em>The components
 * {@link com.sun.rave.web.ui.component.RadioButton} and
 * {@link com.sun.rave.web.ui.component.Checkbox} implement the behavior
 * of returning "true" when <code>selectedValue</code> is null. Therefore
 * if the component parameter is not one of these classes then this behavior
 * may vary.</em>
 * </p>
 * <p>
 * If <code>isSelected</code> returns <code>true</code> the 
 * the value of the HTML INPUT element's checked attribute is set to "checked", 
 * otherwise the checked attribute is not rendered.
 * </p>
 * <p>
 * The following component properties are obtained and rendered in turn and
 * equivalent to the HTML INPUT element's attributes of the same name, but
 * rendered in all lowercase.
 * <ul>
 * <li>disabled</li>
 * <li>readOnly</li>
 * <li>tabIndex</li>
 * <li>style</li>
 * </lu>
 * The component's <code>toolTip</code> property if not null is rendered as the 
 * value of the HTML INPUT element's title attribute.<br/>
 * The HTML INPUT element's class attribute is set to the 
 * component's <code>styleClass</code> property appended with 
 * the value returned from a call to the <code>getStyle</code> method.
 * </p>
 * <h1>Rendering the image component</h1>
 * <p>
 * The renderer calls the component's <code>getImageComponent</code>
 * method to obtain an instance of a 
 * {@link com.sun.rave.web.ui.component.ImageComponent} component. If
 * null is returned, no image will appear with the control.
 * If a non null instance is returned, the appropriate disabled or
 * enabled style class returned by <code>getStyle</code>
 * is appended to the image's <code>styleClass</code> property.
 * <code>RenderingUtilities.renderComponent</code> is called to render
 * the component.<br/>
 * </p>
 * <p>
 * If an image is rendered it appears to the immediate left of the
 * control.
 * </p>
 * <h1>Encoding the label component</h1>
 * <p>
 * The renderer calls the component's <code>getLabelComponent</code>
 * method to obtain an instance of a 
 * {@link com.sun.rave.web.ui.component.Label} component. If
 * null is returned, no label will appear with the control.
 * If a non null instance is returned, the appropriate disabled or
 * enabled style class returned by <code>getStyle</code>
 * is appended to the label's <code>styleClass</code> property.
 * <code>RenderingUtilities.renderComponent</code> is called to render
 * the component.<br/>
 * </p>
 */
abstract class RbCbRendererBase extends AbstractRenderer {
    
    /**
     * The define constant indicating the style class 
     * for an INPUT element.
     */
    protected final static int INPUT = 0;
    /**
     * The define constant indicating the style class 
     * for a disabled INPUT element.
     */
    protected final static int INPUT_DIS = 1;
    /**
     * The define constant indicating the style class 
     * for the LABEL element.
     */
    protected final static int LABEL = 2;
    /**
     * The define constant indicating the style class 
     * for a disabled LABEL element.
     */
    protected final static int LABEL_DIS = 3;
    /**
     * The define constant indicating the style class 
     * for the IMG element.
     */
    protected final static int IMAGE = 4;
    /**
     * The define constant indicating the style class 
     * for a disabled IMG element.
     */
    protected final static int IMAGE_DIS = 5;
    /**
     * The define constant indicating the style class for
     * for the containing span element
     */
    protected final static int SPAN = 6;
    /**
     * The define constant indicating the style class for
     * for the containing span element, when disabled.
     */
    protected final static int SPAN_DIS = 7;

    // Collect most NOI18N in one place
    //
    private static final String INPUT_ELEM = "input"; //NOI18N
    private static final String SPAN_ELEM = "span"; //NOI18N

    private static final String CHECKED_ATTR = "checked"; //NOI18N
    private static final String DISABLED_ATTR = "disabled"; //NOI18N
    private static final String CLASS_ATTR = "class"; //NOI18N
    private static final String ID_ATTR = "id"; //NOI18N
    private static final String NAME_ATTR = "name"; //NOI18N
    private static final String READONLY_ATTR = "readonly"; //NOI18N
    private static final String READONLY_CC_ATTR = "readOnly"; //NOI18N
    private static final String STYLE_ATTR = "style"; //NOI18N
    private static final String STYLECLASS_ATTR = "styleClass"; //NOI18N
    private static final String TABINDEX_ATTR = "tabindex"; //NOI18N
    private static final String TABINDEX_CC_ATTR = "tabIndex"; //NOI18N
    private static final String TITLE_ATTR = "title"; //NOI18N
    private static final String TOOLTIP_ATTR = "toolTip"; //NOI18N
    private static final String TYPE_ATTR = "type"; //NOI18N
    private static final String VALUE_ATTR = "value"; //NOI18N

    private static final String SPAN_SUFFIX = "_span"; //NOI18N

    
   /**
     * <p>The list of attribute names for Rb and Cb
     *
    **/
    public static final String RBCB_EVENTS_ATTRIBUTES[] =
    { "onFocus", "onBlur", "onClick", "onDblClick", "onChange", // NOI18N
      "onMouseDown", "onMouseUp", "onMouseOver", "onMouseMove", "onMouseOut", // NOI18N
              "onKeyPress", "onKeyDown", "onKeyUp", // NOI18N
    };    
    /**
     * Creates a new instance of RbCbRendererBase
     */
    public RbCbRendererBase() {
        super();
    }
    
    /**
     * The getStlye method is implemented by subclasses
     * to return the actual CSS style class name for
     * the given structural element of the rendered component.
     * 
     * @param theme Theme for the request we are processing.
     * @param styleCode one of the previously defined constants.
     */
    protected abstract String getStyle(Theme theme, int styleCode);

    /**
     * Implemented in the subclass to determine if the <code>item</code>
     * is the currently selected control.
     *
     * @param Object selectedValue contol value.
     * @param currentValue the value of the currently selected control.
     */
    protected abstract boolean isSelected(FacesContext context,
	UIComponent component);

    /*
     * <p>
     * Decode the <code>RadioButton</code> or <code>Checkbox</code> selection. 
     * If the value of the component's <code>name</code> property
     * has been set, the value is used to match a request parameter.
     * If it has not been set the component clientId is used to match
     * a request parameter. If a match is found, and the value of the 
     * of the request parameter matches the value of the 
     * <code>selectedValue</code> component property, the 
     * value of the <code>selectedValue</code> property is set
     * as the submitted value, as a one element array containing this value.
     * </p>
     * <p>
     * In the case of a <code>Checkbox</code> component where the
     * check box is part of a group, the value of the request parameter
     * may contain more than one value. If the value of the component's
     * <code>selectedValue</code> property is among the returned values
     * then the value of the <code>selectedValue</code> property is
     * set as the submitted value in a one element array.
     * In the case of a <code>RadioButton</code>
     * there is always only one element selected when part of a group.
     * </p>
     * <p>
     * If no matching request parameter is found, an instance of 
     * <code>String[0]</code> is assigned as the submitted value,
     * meaning that this is a component was not selected.
     * </p>
     *
     * @param context FacesContext for the request we are processing.
     * @param component The <code>RadioButton</code> or <code>Checkbox</code>
     * component to be decoded.
     */

    // This should probably be in RadioButtonRenderer.
    //
    /**
     * Render the child components of this UIComponent, following the rules
     * described for encodeBegin() to acquire the appropriate value to be
     * rendered. This method will only be called if the rendersChildren property
     * of this component is true.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     */
    public void encodeChildren(FacesContext context, UIComponent component)
	throws IOException {
    }

    /**
     * Render a radio button or a checkbox.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     * @param writer <code>ResponseWriter</code> to which the HTML will
     * be output
     * @param type the INPUT element type attribute value.
     */
    protected void renderSelection(FacesContext context,
	    UIComponent component, Theme theme, ResponseWriter writer,
	    String type) throws IOException {

	// Contain the radio button components within a span element
	// assigning the style and styleClass attribute to its
	// style and class attributes.
	//
	writer.startElement(SPAN_ELEM, component);
	writer.writeAttribute(ID_ATTR,
		component.getClientId(context).concat(SPAN_SUFFIX), null);

	// Transfer explicit style attribute value to the span's style
	//
	String prop = (String)((RbCbSelector)component).getStyle();
	if (prop != null) {
	    writer.writeAttribute(STYLE_ATTR, prop, STYLE_ATTR);
	}
	
	// Merge the standard style class with the styleClass
	// attribute
	//
	String styleClass = getStyle(theme, 
		((RbCbSelector)component).isDisabled() ? SPAN_DIS : SPAN);
	styleClass = RenderingUtilities.getStyleClasses(context, component,
		styleClass);
	if (styleClass != null) {
            writer.writeAttribute(CLASS_ATTR, styleClass, null);
	}

	renderInput(context, component, theme, writer, type);
	renderImage(context, component, theme, writer);
	renderLabel(context, component, theme, writer);

	writer.endElement(SPAN_ELEM);
    }

    /**
     * Called from renderSelection to render an INPUT element of type
     * <code>type</code> for the specified <code>component</code>.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be rendered.
     * @param writer <code>ResponseWriter</code> to which the HTML will
     * be output
     * @param type the INPUT element type attribute value.
     */
    protected void renderInput(FacesContext context, 
	    UIComponent component, Theme theme, ResponseWriter writer,
	    String type) throws IOException {

	RbCbSelector rbcbSelector = (RbCbSelector)component;

	String componentId = component.getClientId(context);

	writer.startElement(INPUT_ELEM, component);
        writer.writeAttribute(TYPE_ATTR, type, null);

	// Set the control name to the radiobutton group id
	// and create a unique id from the radiobutton group id.
	// 
        writer.writeAttribute(ID_ATTR, componentId, ID_ATTR);

	// If name is not set use the component's clientId
	//
	boolean inGroup = true;
	String prop = rbcbSelector.getName();
	if (prop == null) {
	    prop = componentId;
	    inGroup = false;
	}
        writer.writeAttribute(NAME_ATTR, prop, NAME_ATTR);

	// If the selectedValue is Boolean and the component is part
	// of a group, "name != null", then set the value of the value
	// attribute to "component.getClientId()".
	// 
	Object selectedValue = rbcbSelector.getSelectedValue();
	prop = ConversionUtilities.convertValueToString(component,
		    selectedValue);

	// Need to check immediate conditions
	// submittedValue will be non null if immediate is true on
	// some action component or a component on the page was invalid
	//
	String[] subValue = (String[])rbcbSelector.getSubmittedValue();
	if (subValue == null) {
	    Object selected = rbcbSelector.getSelected();
	    if (isSelected(context, component)) {
		writer.writeAttribute(CHECKED_ATTR, CHECKED_ATTR, null);
	    }
	    // A component can't be selected if "getSelected" returns null
	    //
	    // Remember that the rendered value was null.
	    //
	    ConversionUtilities.setRenderedValue(component, selected);
	} else
	//
	// if the submittedValue is a 0 length array or the
	// first element is "" then the control is unchecked.
	//
	if (subValue.length != 0 && subValue[0].length() != 0) {
	    // The submitted value has the String value of the
	    // selectedValue property. Just compare the submittedValue
	    // to it to determine if it is checked.
	    //
	    // Assume that the RENDERED_VALUE_STATE is the same
	    // as the last rendering.
	    //
	    if (prop != null && prop.equals(subValue[0])) {
		writer.writeAttribute(CHECKED_ATTR, CHECKED_ATTR, null);
	    }
	}

	// If not ingroup prop has String version of selectedValue
	//
	boolean booleanControl = selectedValue instanceof Boolean;
	if (inGroup && booleanControl) {
	    prop = componentId;
	}
        writer.writeAttribute(VALUE_ATTR, prop, null);

	boolean readonly = rbcbSelector.isReadOnly();
	if (readonly) {
            writer.writeAttribute(READONLY_ATTR, READONLY_ATTR,
		READONLY_CC_ATTR);
	}

	String styleClass = null;
	boolean disabled = rbcbSelector.isDisabled();
	if (disabled) {
            writer.writeAttribute(DISABLED_ATTR, DISABLED_ATTR, DISABLED_ATTR);
		styleClass = getStyle(theme, INPUT_DIS);
        } else {
		styleClass = getStyle(theme, INPUT);
	}


	prop = rbcbSelector.getToolTip();
	if (prop != null) {
	    writer.writeAttribute(TITLE_ATTR, prop, TOOLTIP_ATTR);
	}

	// Output the component's event attributes
	// Probably want the 'no auto submit javascript at some point'
	//
	addStringAttributes(context, component, writer, RBCB_EVENTS_ATTRIBUTES);

	int tabIndex = rbcbSelector.getTabIndex();
	if (tabIndex > 0 && tabIndex < 32767) {
	    writer.writeAttribute(TABINDEX_ATTR, 
		String.valueOf(tabIndex), TABINDEX_CC_ATTR);
	}


        writer.endElement(INPUT_ELEM);
    }

    /**
     * Called from renderSelection to render an IMG element for the 
     * specified <code>item</code>control.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     * @param writer <code>ResponseWriter</code> to which the HTML will
     * be output
     */
    protected void renderImage(FacesContext context,
	    UIComponent component, Theme theme,
	    ResponseWriter writer) throws IOException {

	UIComponent imageComponent = getImageComponent(context, component,
	    theme);
	if (imageComponent != null) {
	    RenderingUtilities.renderComponent(imageComponent, context);
	}
    }

    // There is a serious issue creating child components for
    // renderering purposes. They must be updated to reflect
    // the application state. This can happen in two ways.
    // Literal property values in the current component being rendered
    // that are intended for the child component may have been
    // changed by the application.
    // Properties intended for the child component may be binding
    // expressions in which case the value binding must be 
    // assigned to the property in the child component.
    // Since both these values must be updated in the child component
    // since the application can change them at any time, it makes
    // sense to just always update the child with the value obtained
    // from the accessor for the property vs. obtaining and assigning
    // the ValueBinding.
    //
    // Also child creation should occur in the component and 
    // the process of this creation should produce a facet
    // so that the renderer just asks for the facet.
    // It may orginate from the component or the developer.
    //

    private UIComponent getImageComponent(FacesContext context,
	    UIComponent component, Theme theme) throws IOException {

	RbCbSelector rbcbComponent = (RbCbSelector)component;
	ImageComponent imageComponent =
	    (ImageComponent)rbcbComponent.getImageComponent();
	if (imageComponent == null) {
	    return null;
	}

	// Need to apply disabled class 
	//
	String styleClass = getStyle(theme, 
		rbcbComponent.isDisabled() ? IMAGE_DIS : IMAGE);
	styleClass = RenderingUtilities.getStyleClasses(context,
		imageComponent, styleClass);
	if (styleClass != null) {
	    imageComponent.setStyleClass(styleClass);
	}

	return imageComponent;
    }

    /**
     * Called from <code>renderSelection</code> to render a LABEL.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     * @param writer <code>ResponseWriter</code> to which the HTML will
     * be output
     */
    protected void renderLabel(FacesContext context, UIComponent component,
		Theme theme, ResponseWriter writer) throws IOException {

	UIComponent labelComponent = getLabelComponent(context, component,
	    theme);
	if (labelComponent != null) {
	    RenderingUtilities.renderComponent(labelComponent, context);
	}
    }

    private UIComponent getLabelComponent(FacesContext context,
		UIComponent component, Theme theme) throws IOException {

	RbCbSelector rbcbComponent = (RbCbSelector)component;
	Label labelComponent = (Label)rbcbComponent.getLabelComponent();
	if (labelComponent == null) {
	    return null;
	}

	// Need to apply disabled class 
	//
	String styleClass = getStyle(theme, 
		rbcbComponent.isDisabled() ? LABEL_DIS : LABEL);
	styleClass = RenderingUtilities.getStyleClasses(context,
		labelComponent, styleClass);
	if (styleClass != null) {
	    labelComponent.setStyleClass(styleClass);
	}
	return labelComponent;
    }

    /**
     * <p>
     * Attempt to convert previously stored state information into an
     * object of the type required for this component (optionally using the
     * registered {@link javax.faces.convert.Converter} for this component,
     * if there is one).  If conversion is successful, the new value
     * is returned and if not, a
     * {@link javax.faces.convert.ConverterException} is thrown.
     * </p>
     * 
     * @param context {@link FacesContext} for the request we are processing
     * @param component component being renderer.
     * @param submittedValue a value stored on the component during
     *    <code>decode</code>.
     * 
     * @exception ConverterException if the submitted value
     *   cannot be converted successfully.
     * @exception NullPointerException if <code>context</code>
     *  or <code>component</code> is <code>null</code>
     */
    public Object getConvertedValue(FacesContext context,
				    UIComponent  component,
				    Object submittedValue) 
	    throws ConverterException {

	// I know this looks odd but it gives an opportunity
	// for an alternative renderer for Checkbox and RadioButton
	// to provide a converter.
	//
	return ((RbCbSelector)component).getConvertedValue(context,
		(RbCbSelector)component, submittedValue);
    }
}
