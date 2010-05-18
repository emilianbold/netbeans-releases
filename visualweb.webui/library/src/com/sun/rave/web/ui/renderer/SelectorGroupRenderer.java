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
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.component.UIInput;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import com.sun.rave.web.ui.component.Checkbox;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.ImageComponent;
import com.sun.rave.web.ui.component.RadioButton;
import com.sun.rave.web.ui.component.Selector;
import com.sun.rave.web.ui.component.RbCbSelector;
import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.model.Option;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.renderer.RadioButtonGroupRenderer; // for javadoc
import com.sun.rave.web.ui.renderer.CheckboxGroupRenderer; // for javadoc
import com.sun.rave.web.ui.renderer.RowColumnRenderer; // for javadoc
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.RenderingUtilities;

/**
 * The <code>SelectorGroupRenderer</code> is the abstract base class for 
 * {@link RadioButtonGroupRenderer} and
 * {@link CheckboxGroupRenderer}. It provides
 * common behavior for driving the 
 * {@link RowColumnRenderer} superclass.
 * Its <code>decode</code> method provides decoding for both
 * <code>CheckboxGroup</code>
 * and <code>RadioButtonGroup</code> components. It decodes the value as a
 * <code>String[]</code> for both components. For <code>CheckboxGroup</code>
 * components this an array of possibly 0 or more elements. For
 * the <code>RadioButtonGroup</code> component the value is decoded as a
 * <code>String[]</code> of at least one element.
 */
abstract class SelectorGroupRenderer extends RowColumnRenderer {
    
    private static final String LABELFACTORY =
	"com.sun.rave.web.ui.component.util.factories.LabelFactory"; //NOI18N

    /**
     * The define constant indicating the style class 
     * for the top level TABLE element.
     */
    protected final static int GRP = 0;
    /**
     * The define constant indicating the style class 
     * for the CSS table CAPTION (a CELL element).
     */
    protected final static int GRP_CAPTION = 1;
    /**
     * The define constant indicating the style class 
     * for a disabled CSS table CAPTION (a LABEL) element.
     */
    protected final static int GRP_LABEL = 2;
    /**
     * The define constant indicating the style class 
     * for a disabled CSS table CAPTION (a LABEL) element.
     */
    protected final static int GRP_LABEL_DIS = 3;
    /**
     * The define constant indicating the style class 
     * for the even rows.
     */
    protected final static int GRP_ROW_EVEN = 4;
    /**
     * The define constant indicating the style class 
     * for the odd rows.
     */
    protected final static int GRP_ROW_ODD = 5;
    /**
     * The define constant indicating the style class 
     * for the even cells.
     */
    protected final static int GRP_CELL_EVEN = 6;
    /**
     * The define constant indicating the style class 
     * for the odd cells.
     */
    protected final static int GRP_CELL_ODD = 7;
    /**
     * The define constant indicating the style class 
     * for an INPUT element.
     */
    protected final static int INPUT = 8;
    /**
     * The define constant indicating the style class 
     * for a disabled INPUT element.
     */
    protected final static int INPUT_DIS = 9;
    /**
     * The define constant indicating the style class 
     * for the LABEL element.
     */
    protected final static int LABEL = 10;
    /**
     * The define constant indicating the style class 
     * for a disabled LABEL element.
     */
    protected final static int LABEL_DIS = 11;
    /**
     * The define constant indicating the style class 
     * for the IMG element.
     */
    protected final static int IMAGE = 12;
    /**
     * The define constant indicating the style class 
     * for a disabled IMG element.
     */
    protected final static int IMAGE_DIS = 13;
    /**
     * The define constant indicating the label level style class 
     * for a LABEL element.
     */
    protected final static int LABEL_LVL1 = 14;
    /**
     * The define constant indicating the label level style class 
     * for a LABEL element.
     */
    protected final static int LABEL_LVL2 = 15;
    /**
     * The define constant indicating the label level style class 
     * for a LABEL element.
     */
    protected final static int LABEL_LVL3 = 16;
    /**
     * The define constant indicating the default label level style class 
     * for a LABEL element.
     */
    protected final static int LABEL_LVL_DEF = 0;

    private final static int LABEL_LVL = IMAGE_DIS;

    /**
     * Creates a new instance of SelectorGroupRenderer
     */
    public SelectorGroupRenderer() {
        super();
    }
    
    /**
     * Return style constants for the controls in the group.
     * The getStyles method is implemented by subclasses
     * to return a <code>String[]</code> of style constants as
     * defined in <code>ThemeStyles</code>
     * in an order defined by the constants in this class.
     */
    protected abstract String[] getStyles();

    /**
     * Implemented in the subclass to return the UIComponent for a
     * control in the group.
     *
     * @param context FacesContext for the request we are processing.
     * @param component The RadioButtonGroup component to be decoded.
     * @param theme Theme for the request we are processing.
     * @param id the new component's id.
     * @param option the <code>Option</code> being rendered.
     */
    protected abstract UIComponent getSelectorComponent(FacesContext context,
	UIComponent component, Theme theme, String id, Option option);

    /**
     * Decode the <code>RadioButtonGroup</code> or
     * <code>CheckboxGroup</code> selection. 
     * If the component clientId is found as a request parameter, which is
     * rendered as the value of the <code>name</code> attribute of
     * the INPUT elements of type radio or checkbox, the <code>String[]</code>
     * value is assigned as the submitted value on the component.
     * <p>
     * In the case of a <code>CheckboxGroup</code> component the array may
     * have zero or more elements. In the case of <code>RadioButtonGroup</code>
     * there is always only one element.
     * </p>
     * <p>
     * If the component clientId is not found as a request parameter a
     * <code>String[0]</code> is assigned as the submitted value,
     * meaning that this is a <code>CheckboxGroup</code> component with no
     * check boxes selected.
     *
     * @param context FacesContext for the request we are processing.
     * @param component The RadioButtonGroup component to be decoded.
     */
    public void decode(FacesContext context, UIComponent component) {

        if (context == null || component == null) {
            throw new NullPointerException();
        }

	if (isDisabled(component)|| isReadOnly(component)) {
	    return;
	}

	setSubmittedValues(context, component);
    }

    private void setSubmittedValues(FacesContext context,
	    UIComponent component) {

	String clientId = component.getClientId(context);

	Map requestParameterValuesMap = context.getExternalContext().
	    getRequestParameterValuesMap();

	// If the clientId is found some controls are checked
	//
	if (requestParameterValuesMap.containsKey(clientId)) {
	    String[] newValues = (String[])
		requestParameterValuesMap.get(clientId);

	    ((UIInput) component).setSubmittedValue(newValues);
	    return;
	}
	// Return if there are no disabledCheckedValues and there
	// were no controls checked
	//
	((UIInput) component).setSubmittedValue(new String[0]);
	return;
    }
    
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
     * Called from the renderEnd method of the subclass to begin
     * rendering the component.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     * @param writer <code>ResponseWriter</code> to which the HTML will
     * be output
     * @param columns the number of columns to use when rendering the controls
     */
    protected void renderSelectorGroup(FacesContext context,
	    UIComponent component, Theme theme,
	    ResponseWriter writer, int columns)
	    throws IOException {

	// If there are more items than columns, render additional rows.
	//
	Selector selector = (Selector)component;

	// See if we are rendering null for nothing selected
	//
	Object selected = selector.getSelected();

	// If the submittedValue is null record the rendered value
	// If the submittedValue is not null then it contains the
	// String values of the selected controls.
	// If the submittedValue is not null but zero in length
	// then nothing is selected. Assume that the component still
	// has the appropriate rendered state.
	//
	if (selector.getSubmittedValue() == null) {
	    ConversionUtilities.setRenderedValue(component, selected);
	}

	// If there aren't any items don't render anything
	//
	Option[] items = getItems((Selector)component);
	if (items == null) {
	    return;
	}
	int length = items.length;
	if (length == 0) {
	    return;
	}

	columns = columns <= 0 ? 1 :
		(columns > length ? length : columns);
	int rows = (length + (columns - 1)) / columns;

	// Render the table layout
	renderRowColumnLayout(context, component, theme,
		writer, rows, columns);
    }

    // Should be in component
    //
    protected Option[] getItems(Selector selector) {
	Object items = selector.getItems();
	if (items == null) {
	    return null;
	} else
	if (items instanceof Option[]) {
	    return (Option[])items;
	} else
	if (items instanceof Map) {
	    int size = ((Map)items).size();
	    return (Option[])((Map)items).values().toArray(new Option[size]);
	} else
	if (items instanceof Collection) {
	    int size = ((Collection)items).size();
	    return (Option[])((Collection)items).toArray(new Option[size]);
	} else {
	    throw new IllegalArgumentException(
		"Selector.items is not Option[], Map, or Collection");
	}
    }

    protected void renderCellContent(FacesContext context,
				    UIComponent component,
				    Theme theme,
				    ResponseWriter writer,
				    int itemN) throws IOException {

	Option[] items = getItems((Selector)component);
	if (itemN >= items.length) {
	    renderEmptyCell(context, component, theme, writer);
	    return;
	}

	String id = component.getId().concat("_") + itemN; //NOI18N
	UIComponent content = getSelectorComponent(context, component,
		theme, id, items[itemN]);
	RenderingUtilities.renderComponent(content, context);
    }

    /**
     * Called by the RowColumnRenderer superclass when the 
     * group label should be rendered.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     * @param theme Theme for the request we are processing.
     * @param writer <code>ResponseWriter</code> to which the HTML will
     * be output
     */
    protected void renderCaption(FacesContext context,
	    UIComponent component, 
	    Theme theme, ResponseWriter writer)
	    throws IOException {

	UIComponent captionComponent = getCaptionComponent(context,
	    component, theme, component.getId().concat("_caption")); //NOI18N
	if (captionComponent != null) {
	    RenderingUtilities.renderComponent(captionComponent, context);
	}
    }

    private UIComponent getCaptionComponent(FacesContext context,
	    UIComponent component, Theme theme,
	    String captionId) throws IOException {

	// Check if the page author has defined a label facet
	//
	// What if the component is readonly ? Do we need to modify
	// the facet to be readonly, disabled, or required ?
	// What about styles, etc.
	//
	UIComponent labelComponent = component.getFacet("label"); //NOI18N
	if (labelComponent != null) {
	    return labelComponent;
	}

	// If we find a label, define a label component
	//
	String attrvalue = (String)component.getAttributes().get("label"); //NOI18N
	if (attrvalue == null || attrvalue.length() <= 0) { 
	    return null;
	}

	Properties props = new Properties();
	labelComponent = Util.getChild(component, captionId,
			    LABELFACTORY, props);

	Map labelAttrs = labelComponent.getAttributes();
	
	labelAttrs.put("text", attrvalue); //NOI18N
	// Set the for attribute for the first control ?
	//
	labelAttrs.put("for", //NOI18N
		component.getClientId(context).concat("_0_input"));  //NOI18N
	// This causes a back and forth conversion
	// from boolean to String to boolean
	//
	Boolean required = (Boolean)
		component.getAttributes().get("required"); //NOI18N
	if (attrvalue != null) {
	    labelAttrs.put("required", required); //NOI18N
	}

	attrvalue = (String)
		component.getAttributes().get("accesskey"); //NOI18N
	if (attrvalue != null) { 
	    labelAttrs.put("accesskey", attrvalue);  //NOI18N
	} 

	// Give the group's tooltip to the group label
	//
	attrvalue =
	    (String)component.getAttributes().get("toolTip"); //NOI18N
	if (attrvalue != null) {
	    labelAttrs.put("toolTip", attrvalue); //NOI18N
	}

	Integer lblLvl = (Integer)
	    component.getAttributes().get("labelLevel"); //NOI18N

	// Need to synch up defaults
	//
	if (lblLvl == null) {
	    lblLvl = new Integer(2);
	}

	// This does not work because the component interface for
	// {get,set}LabelLevel is int primitive and the Factories
	// convert to Integer.
	//
	//props.setProperty("labelLevel", String.valueOf(styleLevel)); //NOI18N
	labelAttrs.put("labelLevel", lblLvl); //NOI18N

	int styleCode = GRP_LABEL;
	Boolean disabled =
	    (Boolean)component.getAttributes().get("disabled"); //NOI18N
	if (disabled != null && disabled.booleanValue() == true) {
	    labelAttrs.put("disabled", disabled); //NOI18N
	    styleCode = GRP_LABEL_DIS;
	}

	String captionStyle = getStyle(theme, styleCode);
	if (captionStyle != null) {
	    labelAttrs.put("styleClass", captionStyle); //NOI18N
	}

	return labelComponent;
    }

    /**
     * Called from the <code>renderCellContent</code> method implemented
     * in the sublclass when there are no more controls to render.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     * @param theme Theme for the request we are processing.
     * @param writer <code>ResponseWriter</code> to which the HTML will
     * be output
     */
    protected void renderEmptyCell(FacesContext context,
	    UIComponent component, Theme theme,
	    ResponseWriter writer) throws IOException {

	/*
        writer.writeText("&nbsp;", null); //NOI18N
	*/
    }

    // Structural styles
    //
    private int[] rowcolstyle = {
	GRP, GRP_CAPTION, GRP_ROW_EVEN, GRP_ROW_ODD,
	GRP_CELL_EVEN, GRP_CELL_ODD
    };

    /**
     * Pass on the style request from the
     * {@link com.sun.rave.web.ui.renderer.RowColumnRenderer} to
     * the <code>SelectorGroupRenderer</code> subclass.
     *
     * @param theme Theme for the request we are processing.
     * @param styleCode the desired style class constant
     */
    protected final String getRowColumnStyle(Theme theme, int styleCode) {
	return getStyle(theme, rowcolstyle[styleCode]);
    }

    /**
     * Return the style class name for the structural element indicated
     * by <code>styleCode</code>
     *
     * @param styleCode identifies the style class for the element about
     * to be rendered.
     */
    private String getStyle(Theme theme, int styleCode) {
	String style = null;
	try {
	    style = theme.getStyleClass(getStyles()[styleCode]);
	} catch (Exception e) {
	    // Don't care
	}
	return style;
    }

    /**
     * Return the style class name and level for the structural element
     * indicated by <code>styleCode</code>
     *
     * @param styleCode identifies the style class for the element about
     * to be rendered.
     * @param styleLevelCode identifies the style class level for the 
     * element about to be rendered.
     */
    protected String getStyle(Theme theme,
		int styleCode, int styleLevelCode) {

	String style = getStyle(theme, styleCode);
	if (style == null) {
	    return null;
	}

	StringBuffer styleBuf = new StringBuffer(style);

	String styleLevel = null;
	if (styleLevelCode != LABEL_LVL_DEF) {
	    styleLevel = getStyle(theme, styleLevelCode);
	}

	// No style code for the desired one, get the default
	//
	if (styleLevel != null) {
	    if (styleBuf.length() != 0) {
		styleBuf.append(" "); //NOI18N
	    }
	    styleBuf.append(styleLevel);
	} else {
	    style = null;
	    switch (styleCode) {
	    case GRP_CAPTION:
		style = getStyle(theme, LABEL_LVL2);
	    break;
	    case LABEL:
		style = getStyle(theme, LABEL_LVL3);
	    break;
	    }
	    if (style != null) {
		if (styleBuf.length() != 0) {
		    styleBuf.append(" "); //NOI18N
		}
		styleBuf.append(style);
	    }
	}
	return styleBuf.toString();
    }
    
    //<RAVE>
    //mbohm 6300361,6300362
    //transfer event attributes from a radiobuttongroup/checkboxgroup to a 
    //radiobutton/checkbox
    protected void transferEventAttributes(Selector group, RbCbSelector rbcb) {
        Map groupAttributes = group.getAttributes();
        Map rbcbAttributes = rbcb.getAttributes();
        final String[] eventAttributeNames = AbstractRenderer.EVENTS_ATTRIBUTES;
        for (int i = 0; i < eventAttributeNames.length; i++) {
            Object eventAttributeValue = 
                    groupAttributes.get(eventAttributeNames[i]);
            if (eventAttributeValue != null) {
                rbcbAttributes.put(eventAttributeNames[i], eventAttributeValue);
            }
        }
    }
    //</RAVE>
}
