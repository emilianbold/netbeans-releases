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

import com.sun.rave.web.ui.component.RadioButton;
import com.sun.rave.web.ui.component.RadioButtonGroup;
import com.sun.rave.web.ui.component.Selector;
import com.sun.rave.web.ui.model.Option;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

/**
 * The <code>RadioButtonRenderer</code> renders a <code>RadioButtonGroup</code>
 * component as set of radio buttons. The <code>RadioButtonGroupRenderer</code>
 * creates an instance of <code>RadioButton</code> for each
 * <code>Option</code> instance in the <code>Array</code>, <code>Map</code>, or
 * <code>Collection</code> returned by the <code>RadioButtonGroup</code>
 * <code>getItems()</code> method and renders them. It also
 * creates a <code>Label</code> component and renders it as the label for the
 * group.
 * <p>
 * Only one radio button may be selected at any time and one radio button
 * must always be selected. The value of the <code>RadioButtonGroup</code>
 * will determine which radio button shall be initially selected.
 * Subsequently, the <code>RadioButtonGroup</code>'s value holds the
 * currently selected radio button value.
 * </p>
 * <p>
 * The radio buttons are rendered as a single column or some number of
 * rows and columns. The rows and columns are rendered as a table as
 * defined by the {@link com.sun.rave.web.ui.renderer.RowColumnRenderer} superclass.
 * The elements
 * that make up the radio button occupy a cell in the table.
 * The style class selector for the group elements is identified by a java
 * constants defined in the {@link com.sun.rave.web.ui.theme.ThemeStyles} class.
 * </p>
 * <ul>
 * <li>RADIOBUTTON_GROUP for the TABLE element.</li>
 * <li>RADIOBUTTON_GROUP_CAPTION for the TD element containing the group
 * label</li>
 * <li>RADIOBUTTON_GROUP_LABEL for the LABEL element used as the CAPTION</li>
 * <li>RADIOBUTTON_GROUP_LABEL_DISABLED for the LABEL used as the CAPTION if
 * the group is disabled</li>
 * <li>RADIOBUTTON_GROUP_ROW_EVEN for even TR elements</li>
 * <li>RADIOBUTTON_GROUP_ROW_ODD for odd TR elements</li>
 * <li>RADIOBUTTON_GROUP_CELL_EVEN for even TD elements</li>
 * <li>RADIOBUTTON_GROUP_CELL_ODD for odd TD elements</li>
 * <li>RADIOBUTTON for the INPUT element</li>
 * <li>RADIOBUTTON_DISABLED for the INPUT element for disabled radio
 * button </li>
 * <li>RADIOBUTTON_LABEL for a LABEL element of a radio button</li>
 * <li>RADIOBUTTON_LABEL_DISABLED for a LABEL element of a disabled radio
 * button</li>
 * <li>RADIOBUTTON_IMAGE for an IMG element of a radio button</li>
 * <li>RADIOBUTTON_IMAGE_DISABLED for an IMG element of a disabled radio
 * button</li>
 * </ul>
 * <p>
 * The <code>name</code> property of each radio button is the component id of
 * the <code>RadioButtonGroup</code> instance. The id of a
 * <code>RadioButton</code> component is <em>rbgrpid_N</em> where
 * <em>rbgrpid</em> is the id of the
 * <code>RadioButtonGroup</code> instance and <em>_N</em> is the nth
 * radio button.
 * </p>
 * <p>
 * The <code>RadioButtonGroup</code> is decoded by identifying the 
 * <code>RadioButtonGroup</code> instance component id which is 
 * returned as a request parameter. It represents the name attribute
 * of the selected radio button's &lt;input&gt; element. The value of
 * the identified request parameter is assigned as the submitted value of the
 * <code>RadioButtonGroup</code> component.
 * </p>
 * <p>
 * If the items property of the <code>RadioButtonGroup</code> is null or 
 * zero length no output is produced.
 * </p>
 *
 */
public class RadioButtonGroupRenderer extends SelectorGroupRenderer {
    
    private final String MSG_COMPONENT_NOT_RADIOBUTTONGROUP =
    "RadioButtonGroupRender only renders RadioButtonGroup components."; //NOI18N

    /**
     * Creates a new instance of RadioButtonGroupRenderer
     */
    public RadioButtonGroupRenderer() {
        super();
    }
    
    /**
     * Ensure that the component to be rendered is a RadioButtonGroup instance.
     * Actual rendering occurs during <code>renderEnd</code>
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     */
    public void renderStart(FacesContext context, UIComponent component,
	ResponseWriter writer)
	throws IOException {

	// Bail out if the component is not a RadioButtonGroup component.
	if (!(component instanceof RadioButtonGroup)) {
	    throw new
		IllegalArgumentException(MSG_COMPONENT_NOT_RADIOBUTTONGROUP);
	}
    }

    /**
     * RadioButtonGroupRenderer renders the entire RadioButtonGroup
     * component within the renderEnd method.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     */
    public void renderEnd(FacesContext context, UIComponent component,
	ResponseWriter writer)
	throws IOException {

	// Use only the cols value. If not valid render a single column.
	// If there are more items than columns, render additional rows.
	//
	RadioButtonGroup rbgrp = (RadioButtonGroup)component;

	Theme theme = ThemeUtilities.getTheme(context);
	renderSelectorGroup(context, component, theme,
		writer, rbgrp.getColumns());
        
    }

    /**
     * Return a RadioButton component to render.
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>CheckboxGroup</code> component rendered
     * @param theme <code>Theme</code> for the component
     * @param option the <code>Option</code> being rendered.
     */
    protected UIComponent getSelectorComponent(FacesContext context,
	UIComponent component, Theme theme, String id, Option option) {

	RadioButtonGroup rbgrp = (RadioButtonGroup)component;

	RadioButton rb = new RadioButton();
	rb.setId(id);
	rb.setParent(component);
	rb.setName(rbgrp.getClientId(context));
	rb.setImageURL(option.getImage());
	rb.setSelectedValue(option.getValue());
	rb.setLabel(option.getLabel());
	rb.setDisabled(rbgrp.isDisabled());
	rb.setReadOnly(rbgrp.isReadOnly());
	rb.setTabIndex(rbgrp.getTabIndex());
        
        //<RAVE>
        // Bug Fix: 6274989 
        if (option.getTooltip() != null){
           rb.setToolTip(option.getTooltip());
        }else{
           rb.setToolTip(rbgrp.getToolTip());
        }
        
        //mbohm 6300361,6300362
        //transfer event attributes from rbgrp to rb
        //see RowColumnRenderer.renderRowColumnLayout
        transferEventAttributes(rbgrp, rb);
        //</RAVE>

	// Default to not selected
	//
	rb.setSelected(null);

	// Need to check the submittedValue for immediate condition
	// 
	String[] subValue = (String[])rbgrp.getSubmittedValue();
	if (subValue == null) {
	    if (isSelected(option, rbgrp.getSelected())) {
		rb.setSelected(rb.getSelectedValue());
	    }
	} else
	if (subValue.length != 0) {
	    Object selectedValue = rb.getSelectedValue();
	    String selectedValueAsString =
		ConversionUtilities.convertValueToString(component,
			selectedValue);
	    if (subValue[0] != null && 
			subValue[0].equals(selectedValueAsString)) {
		rb.setSelected(rb.getSelectedValue());
	    }
	}

	return rb;
    }

    /**
     * Return true if the <code>item</item> argument is the currently
     * selected radio button. Equality is determined by the <code>equals</code>
     * method of the object instance stored as the <code>value</code> of
     * <code>item</code>. Return false otherwise.
     *
     * @param item the current radio button being rendered.
     * @param currentValue the value of the current selected radio button.
     */
    private boolean isSelected(Option item, Object currentValue) {
	return currentValue != null && item.getValue() != null &&
		item.getValue().equals(currentValue);
    }

    protected String[] styles = {
	ThemeStyles.RADIOBUTTON_GROUP, 		/* GRP */
	ThemeStyles.RADIOBUTTON_GROUP_CAPTION,	/* GRP_CAPTION */
	ThemeStyles.RADIOBUTTON_GROUP_LABEL,	/* GRP_LABEL */
	ThemeStyles.RADIOBUTTON_GROUP_LABEL_DISABLED, /* GRP_CAPTION_DIS */
	ThemeStyles.RADIOBUTTON_GROUP_ROW_EVEN,	/* GRP_ROW_EVEN */
	ThemeStyles.RADIOBUTTON_GROUP_ROW_ODD,	/* GRP_ROW_EVEN */
	ThemeStyles.RADIOBUTTON_GROUP_CELL_EVEN,/* GRP_CELL_EVEN */
	ThemeStyles.RADIOBUTTON_GROUP_CELL_ODD,	/* GRP_CELL_ODD */
	ThemeStyles.RADIOBUTTON,	 	/* INPUT */
	ThemeStyles.RADIOBUTTON_DISABLED, 	/* INPUT_DIS */
	ThemeStyles.RADIOBUTTON_LABEL,		/* LABEL */
	ThemeStyles.RADIOBUTTON_LABEL_DISABLED, /* LABEL_DIS */
	ThemeStyles.RADIOBUTTON_IMAGE,		/* IMAGE */
	ThemeStyles.RADIOBUTTON_IMAGE_DISABLED, /* IMAGE_DIS */
	ThemeStyles.LABEL_LEVEL_ONE_TEXT,	/* LABEL_LVL1 */
	ThemeStyles.LABEL_LEVEL_TWO_TEXT,	/* LABEL_LVL2 */
	ThemeStyles.LABEL_LEVEL_THREE_TEXT	/* LABLE_LVL3 */
    };

    /**
     * Return style constants for a <code>RadioButton</code> component.
     */
    protected String[] getStyles() {
	return styles;
    }
}
