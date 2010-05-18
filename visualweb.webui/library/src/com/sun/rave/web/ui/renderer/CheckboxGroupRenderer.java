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
import java.util.Collection;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sun.rave.web.ui.component.Checkbox;
import com.sun.rave.web.ui.component.CheckboxGroup;
import com.sun.rave.web.ui.component.Selector;
import com.sun.rave.web.ui.model.Option;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.ThemeUtilities;

/**
 * The <code>CheckboxGroupRenderer</code> renders a <code>CheckboxGroup</code>
 * component as set of checkboxes. The <code>CheckboxGroupRenderer</code>
 * creates an instance of <code>Checkbox</code> for each
 * <code>Option</code> instance in the <code>Array</code>, <code>Map</code>, or
 * <code>Collection</code> returned by the <code>CheckboxGroup</code>
 * <code>getItems()</code> method and renders them. It also creates
 * a <code>Label</code> component and renders it as the label for the group.
 * <p>
 * Zero or more checkboxes may be selected.
 * The value of the <code>CheckboxGroup</code> will determine
 * which checkboxes shall be initially selected and subsequetly hold
 * the current selections.
 * </p>
 * <p>
 * The checkboxes are rendered as a single column or some number of
 * rows and columns. The rows and columns are rendered as a table as
 * defined by the {@link com.sun.rave.web.ui.renderer.RowColumnRenderer} superclass.
 * The elements
 * that make up the checkbox occupy a cell in the table.
 * The style class selector for the group elements is identified by a java
 * constants defined in the {@link com.sun.rave.web.ui.theme.ThemeStyles} class.
 * </p>
 * <ul>
 * <li>CHECKBOX_GROUP for the TABLE element.</li>
 * <li>CHECKBOX_GROUP_CAPTION for the TD element containing the group label</li>
 * <li>CHECKBOX_GROUP_LABEL for the LABEL element used as the CAPTION</li>
 * <li>CHECKBOX_GROUP_LABEL_DISABLED for the LABEL used as the CAPTION if the
 * group is disabled</li>
 * <li>CHECKBOX_GROUP_ROW_EVEN for even TR elements</li>
 * <li>CHECKBOX_GROUP_ROW_ODD for odd TR elements</li>
 * <li>CHECKBOX_GROUP_CELL_EVEN for even TD elements</li>
 * <li>CHECKBOX_GROUP_CELL_ODD for odd TD elements</li>
 * <li>CHECKBOX for the INPUT element</li>
 * <li>CHECKBOX_DISABLED for the INPUT element for disabled checkbox</li>
 * <li>CHECKBOX_LABEL for a LABEL element of a checkbox</li>
 * <li>CHECKBOX_LABEL_DISABLED for a LABEL element of a disabled checkbox</li>
 * <li>CHECKBOX_IMAGE for an IMG element of a checkbox</li>
 * <li>CHECKBOX_IMAGE_DISABLED for an IMG element of a disabled checkbox</li>
 * </ul>
 * <p>
 * The <code>name</code> property of each checkbox is the component id of the
 * <code>CheckboxGroup</code> instance. The id of a <code>Checkbox</code>
 * component is <em>cbgrpid_N</em> where <em>cbgrpid</em> is the id of the
 * <code>CheckboxGroup</code> instance and <em>_N</em> is the nth checkbox.
 * </p>
 * <p>
 * The <code>CheckboxGroup</code> is decoded by identifying the
 * <code>CheckboxGroup</code> instance component id which is
 * returned as a request parameter. It represents the name attribute
 * of the selected checkbox's &lt;input&gt; element. The values of the
 * identified request parameter are assigned as the submitted value of the
 * <code>CheckboxGroup</code> component.
 * </p>
 * <p>
 * If the items property of the <code>CheckboxGroup</code> is null or
 * zero length, no output is produced.
 * </p>
 */
public class CheckboxGroupRenderer extends SelectorGroupRenderer {
    
    private final String MSG_COMPONENT_NOT_CHECKBOXGROUP =
    "CheckboxGroupRenderer only renders CheckboxGroup components.";//NOI18N

    /**
     * Creates a new instance of CheckboxGroupRenderer
     */
    public CheckboxGroupRenderer() {
        super();
    }
    
    /**
     * Ensure that the component to be rendered is a CheckboxGroup instance.
     * Actual rendering occurs during the <code>renderEnd</code> method.
     *
     * @param context FacesContext for the request we are processing.
     * @param component UIComponent to be decoded.
     */
    public void renderStart(FacesContext context, UIComponent component,
	ResponseWriter writer)
	throws IOException {

	// Bail out if the component is not a CheckboxGroup component.
	if (!(component instanceof CheckboxGroup)) {
	    throw new
		IllegalArgumentException(MSG_COMPONENT_NOT_CHECKBOXGROUP);
	}
    }
    
    /**
     * CheckboxGroupRenderer renders the entire CheckboxGroup
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
	CheckboxGroup cbgrp = (CheckboxGroup)component;

	Theme theme = ThemeUtilities.getTheme(context);
	renderSelectorGroup(context, component, theme,
		writer, cbgrp.getColumns());
    }
    
    /**
     * Return a Checkbox component to render.
     *
     * @param context <code>FacesContext</code> for the current request
     * @param component <code>CheckboxGroup</code> component rendered
     * @param theme <code>Theme</code> for the component
     * @param option the <code>Option</code> being rendered.
     */
    protected UIComponent getSelectorComponent(FacesContext context,
	    UIComponent component, Theme theme, String id, Option option) {

	CheckboxGroup cbgrp = (CheckboxGroup)component;

	Checkbox cb = new Checkbox();
	cb.setId(id);
	cb.setParent(cbgrp);
	cb.setName(cbgrp.getClientId(context));
	cb.setImageURL(option.getImage());
	cb.setSelectedValue(option.getValue());
	cb.setLabel(option.getLabel());
	cb.setDisabled(cbgrp.isDisabled());
	cb.setReadOnly(cbgrp.isReadOnly());
	cb.setTabIndex(cbgrp.getTabIndex());
        
        //<RAVE>
        // Bug Fix: 6274989 
        if (option.getTooltip() != null){
           cb.setToolTip(option.getTooltip());
        }else{
           cb.setToolTip(cbgrp.getToolTip());
        }
     
        //mbohm 6300361,6300362
        //transfer event attributes from cbgrp to cb
        //see RowColumnRenderer.renderRowColumnLayout
        transferEventAttributes(cbgrp, cb);
        //</RAVE>

	// Default to not selected
	//
	cb.setSelected(null);

	// Need to check the submittedValue for immediate condition
	// 
	String[] subValue = (String[])cbgrp.getSubmittedValue();
	if (subValue == null) {
	    if (isSelected(option, cbgrp.getSelected())) {
		cb.setSelected(cb.getSelectedValue());
	    }
	} else
	if (subValue.length != 0) {
	    Object selectedValue = cb.getSelectedValue();
	    String selectedValueAsString =
		ConversionUtilities.convertValueToString(component,
			selectedValue);
	    for (int i = 0; i < subValue.length; ++i) {
		if (subValue[i] != null && 
			subValue[i].equals(selectedValueAsString)) {
		    cb.setSelected(cb.getSelectedValue());
		    break;
		}
	    }
	}

	return cb;
    }

    /**
     * Return true if the <code>item</item> argument is the currently
     * selected checkbox. Equality is determined by the <code>equals</code>
     * method of the object instance stored as the <code>value</code> of
     * <code>item</code>. Return false otherwise.
     *
     * @param item the current checkbox being rendered.
     * @param currentValue the value of the current selected checkbox.
     */
    private boolean isSelected(Option item, Object currentValue) {
	// How is the selected value determined ?
	// Is it the Selection value on CheckboxGroup or 
	// the boolean value on the current Selection being processed ?
	//
	Object value = item.getValue();
	if (value == null || currentValue == null) {
	    return false;
	}
	if (currentValue instanceof Map) {
	    return ((Map)currentValue).containsValue(value);
	} else 
	if (currentValue instanceof Collection) {
	    return ((Collection)currentValue).contains(value);
	} else
	if (currentValue instanceof Object[]) {
	    Object[] selectedValues = (Object[])currentValue;
	    for (int i = 0; i < selectedValues.length; ++i) {
		if (value.equals(selectedValues[i])) {
		    return true;
		}
	    }
	}
	return false;
	    
    }

    /**
     * The style constants defined in {@link com.sun.rave.web.ui.theme.ThemeStyles} mapped
     * to the value of constants defined in 
     * {@link com.sun.rave.web.ui.renderer.SelectorGroupRenderer}.
     */
    protected String[] styles = {
	ThemeStyles.CHECKBOX_GROUP, 		/* GRP */
	ThemeStyles.CHECKBOX_GROUP_CAPTION,	/* GRP_CAPTION */
	ThemeStyles.CHECKBOX_GROUP_LABEL,	/* GRP_LABEL */
	ThemeStyles.CHECKBOX_GROUP_LABEL_DISABLED, /* GRP_LABEL_DIS */
	ThemeStyles.CHECKBOX_GROUP_ROW_EVEN,	/* GRP_ROW_EVEN */
	ThemeStyles.CHECKBOX_GROUP_ROW_ODD,	/* GRP_ROW_EVEN */
	ThemeStyles.CHECKBOX_GROUP_CELL_EVEN,	/* GRP_CELL_EVEN */
	ThemeStyles.CHECKBOX_GROUP_CELL_ODD,	/* GRP_CELL_ODD */
	ThemeStyles.CHECKBOX,	 		/* INPUT */
	ThemeStyles.CHECKBOX_DISABLED, 		/* INPUT_DIS */
	ThemeStyles.CHECKBOX_LABEL,		/* LABEL */
	ThemeStyles.CHECKBOX_LABEL_DISABLED, 	/* LABEL_DIS */
	ThemeStyles.CHECKBOX_IMAGE,		/* IMAGE */
	ThemeStyles.CHECKBOX_IMAGE_DISABLED, 	/* IMAGE_DIS */
	ThemeStyles.LABEL_LEVEL_ONE_TEXT,	/* LABEL_LVL1 */
	ThemeStyles.LABEL_LEVEL_TWO_TEXT,	/* LABEL_LVL2 */
	ThemeStyles.LABEL_LEVEL_THREE_TEXT	/* LABLE_LVL3 */
    };

    /**
     * Return style constants for a <code>Checkbox</code> component.
     */
    protected String[] getStyles() {
	return styles;
    }
}
