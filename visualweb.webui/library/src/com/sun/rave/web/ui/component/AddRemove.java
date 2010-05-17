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

import java.beans.Beans;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map;
import java.util.HashMap;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIInput;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.el.MethodBinding;
import javax.faces.el.MethodNotFoundException;
import javax.faces.el.EvaluationException;
import com.sun.rave.web.ui.component.Button;
import com.sun.rave.web.ui.component.TextField;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.StaticText;
import com.sun.rave.web.ui.model.Option;
import com.sun.rave.web.ui.model.OptionGroup;
import com.sun.rave.web.ui.model.Separator;
import com.sun.rave.web.ui.model.list.ListItem;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeStyles;
import com.sun.rave.web.ui.util.MessageUtil;
import com.sun.rave.web.ui.util.ThemeUtilities;
import com.sun.rave.web.ui.validator.StringLengthValidator;

/**
 * Use the AddRemove component when the web application user makes selections from a list and they need to see the currently selected items displayed together, and/or they need to reorder the selected items.
 * @author avk
 */

public class AddRemove extends AddRemoveBase implements ListManager {


    /**
     * The component id for the ADD button
     */
    public static final String ADD_BUTTON_ID = "_addButton"; //NOI18N
    /**
     * The facet name of the add button
     */
    public static final String ADD_BUTTON_FACET = "addButton"; //NOI18N


    /**
     * The component id for the ADD ALL button
     */
    public static final String ADDALL_BUTTON_ID = "_addAllButton"; //NOI18N
    /**
     * The facet name of the Add All button
     */
    public static final String ADDALL_BUTTON_FACET = "addAllButton"; //NOI18N

    /**
     * The component ID for the remove button
     */
    public static final String REMOVE_BUTTON_ID = "_removeButton"; //NOI18N
    /**
     * The facet name of the remove button
     */
    public static final String REMOVE_BUTTON_FACET = "removeButton"; //NOI18N

    /**
     * The component ID for the remove all button
     */
    public static final String REMOVEALL_BUTTON_ID = "_removeAllButton"; //NOI18N
    /**
     * The facet name of the "Remove All" button
     */
    public static final String REMOVEALL_BUTTON_FACET = "removeAllButton"; //NOI18N

    /**
     * The component ID for the move up button
     */
    public static final String MOVEUP_BUTTON_ID = "_moveUpButton"; //NOI18N
    /**
     * The facet name of the "Move Up" button
     */
    public static final String MOVEUP_BUTTON_FACET = "moveUpButton"; //NOI18N

    /**
     * The component ID for the move down button
     */
    public static final String MOVEDOWN_BUTTON_ID = "_moveDownButton"; //NOI18N
    /**
     * The facet name of the "Move Down" button
     */
    public static final String MOVEDOWN_BUTTON_FACET = "moveDownButton"; //NOI18N

    /**
     * The component ID for the items list
     */
    public static final String AVAILABLE_LABEL_ID = "_availableLabel"; //NOI18N
    /**
     * The facet name of the label over the "Available" list
     */
    public static final String AVAILABLE_LABEL_FACET = "availableLabel"; //NOI18N


    /**
     * The component ID for the selected list
     */
    public static final String SELECTED_LABEL_ID = "_selectedLabel"; //NOI18N
    /**
     * The facet name of the label over the "Selected" list
     */
    public static final String SELECTED_LABEL_FACET = "selectedLabel"; //NOI18N

    /** 
     * Facet name for the header facet
     */ 
    public static final String HEADER_FACET = "header"; //NOI18N
    /**
     * The facet name of the header (component label)
     */
    public static final String HEADER_ID = "_header"; //NOI18N

    /** 
     * Facet name for the footer facet
     */ 
    public static final String FOOTER_FACET = "footer"; //NOI18N

    /**
     * The id of the label component that functions as the label above the available list
     */
    public static final String AVAILABLE_ID = "_available"; //NOI18N
    /**
     * The ID of the component that functions as the label above the "Selected" list
     */
    public static final String SELECTED_ID = "_selected"; //NOI18N

    /**
     * The name of the component attribute that stores the name of the JavaScript object that organizes the JavsSCript functions for the component
     */
    public static final String JSOBJECT = "com.sun.rave.web.ui.AddRemoveJS";
    /**
     * Represents the "javascript:" printed at the start of javascript event handler code
     */
    public static final String JAVASCRIPT_PREFIX = "javascript: ";
    /**
     * String representing "return false" printed at the end of the javascript event handlers
     */
    public static final String RETURN = "return false;"; 
    /**
     * Name of the JavaScript function which is responsible for adding elements from the availble list to the selected list
     */
    public static final String ADD_FUNCTION = ".add(); ";
    /**
     * Name of the JavaScript function which is responsible for selecting
     * all the available items
     */
    public static final String ADDALL_FUNCTION = ".addAll();";
    /**
     * Name of the JavaScript function which removes items from the seleted list
     */
    public static final String REMOVE_FUNCTION = ".remove(); ";
    /**
     * Name of the JavaScript function which removes all the items from the seleted list
     */
    public static final String REMOVEALL_FUNCTION = ".removeAll(); ";
    /**
     * Name of the JavaScript function which moves elements up
     */
    public static final String MOVEUP_FUNCTION = ".moveUp(); ";
    /**
     * Name of the JavaScript function which moves elements down
     */
    public static final String MOVEDOWN_FUNCTION = ".moveDown();";            
    /**
     * Name of the JavaScript function that updates the buttons
     */
    public static final String UPDATEBUTTONS_FUNCTION = ".updateButtons(); ";
    /**
     * Name of the JavaScript function that handles changes on the available list
     */
    public static final String AVAILABLE_ONCHANGE_FUNCTION = 
	".availableOnChange(); ";
    /**
     * Name of the JavaScript function which handles changes to the selected list
     */
    public static final String SELECTED_ONCHANGE_FUNCTION = 
	".selectedOnChange(); ";
    /**
     * The name of the JavaScript function used to hook up the correct
     * add and remove functions when the component allows items to be
     * added to the selected items list more than once
     */
    public static final String MULTIPLEADDITIONS_FUNCTION = 
	".allowMultipleAdditions()"; 
    
    public static final String SPACER_STRING = "_"; //NOI18N

    /**
     * The string used as a separator between the selected values
     */
    public static final String SEPARATOR_VALUE = "com.sun.rave.web.ui.separator"; 

    private TreeMap availableItems = null; 
    private TreeMap selectedItems = null; 
    private Collator collator = null;

    private String allValues = "";
    private String selectedValues = "";

    private static final boolean DEBUG = false; 

    /**
     * Constructor for the AddRemove component
     */
    public AddRemove() { 
	setMultiple(true); 
    }

    /**
     * Get the number of rows to disaplay (the default is 12)
     * @return the number of rows to disaplay
     */
    public int getRows() {

	int rows = super.getRows();
	if(rows < 1) { 
	    rows = 12; 
	    super.setRows(rows);
	}
	return rows;
    }

    /**
     * Get the separator string that is used to separate the selected values on the client.
     * The default value is "|". When the AddRemove component is decoded, the 
     * value is taken from a hidden variable whose value is a list of the 
     * values of all the options in the list representing the selected items. 
     * Consider a case where the AddRemove has a list of options including 
     * <option value="1">One</option> 
     * <option value="2">Two</option> 
     * Assume that these two options are disabled. If the separator 
     * string is set to "|", then the value of the hidden
     * variable will be |1|2|.
     * 
     * You will only need to set this variable if the string 
     * representation of one of the option values contain the 
     * character "|". If you do need to change from the default,
     * bear in mind that the value of the hidden component 
     * is sent as part of the body of the HTTP request body. 
     * Make sure to select a character that does not change 
     * the syntax of the request.
     * @return The separator string. 
     */
    public String getSeparator() { 
	return "|"; 
    } 

    /**
     * Returns an iterator over the selected items
     * @return an iterator over the selected items
     */
    public Iterator getSelectedItems() { 
	return selectedItems.values().iterator();
    }

    /**
     * This function returns a String consisting of the String representation of the
     * values of all the available Options, separated by the separator
     * String (see getSeparator())
     * @return eturns a String consisting of the String representation of the
     * values of all the available Options, separated by the separator
     * String
     */
    public String getAllValues() { 
	return allValues; 
    } 

    /**
     * This function returns a String consisting of the String representation of the
     * values of the selected Options, separated by the separator
     * String
     * @return a String consisting of the String representation of the
     * values of the selected Options, separated by the separator
     * String 
     */
    public String getSelectedValues() { 
	return selectedValues; 
    } 

    // Buttons
    /**
     * Get or create the ADD button. Retrieves the component specified by the 
     * addButton facet (if there is one) or creates a new Button component.
     * @return A UI Component for the Add button
     * @param context The FacesContext for the request
     */
    public UIComponent getAddButtonComponent(FacesContext context) { 

	if(DEBUG) log("getAddButtonComponent()"); 

	String id = getId(); 

	// Check if the page author has defined an addbutton facet
	UIComponent buttonComponent = getFacet(ADD_BUTTON_FACET); 
	// If the page author has not defined a button facet,
	// check if the page author specified a button.
	if(buttonComponent == null) { 

	    String buttonLabel = getTheme().getMessage("AddRemove.add"); 
	    if(!isVertical()) { 
		buttonLabel = buttonLabel.concat(" > "); //NOI18N
	    } 

	    buttonComponent = 
		createButton(buttonLabel, ADD_BUTTON_ID, ADD_BUTTON_FACET); 
	    ((Button)buttonComponent).setPrimary(true);
            StringBuffer jsBuffer = new StringBuffer(200);   
	    jsBuffer.append(getAttributes().get(JSOBJECT)); 
	    jsBuffer.append(ADD_FUNCTION); 
	    jsBuffer.append(RETURN);
	    ((Button)buttonComponent).setOnClick(jsBuffer.toString());
	} 
	else if(DEBUG) { 
	    log("\tFound facet."); //NOI18N
	} 
	if(buttonComponent != null && isDisabled()) { 
	    buttonComponent.getAttributes().put("disabled", Boolean.TRUE); //NOI18N
	}
	return buttonComponent; 
    } 

    /**
     * Get or create the ADD button. Retrieves the component specified by the 
     * addButton facet (if there is one) or creates a new Button component.
     * @return A UI Component for the Add button
     */
    public UIComponent getAddAllButtonComponent() { 

	if(DEBUG) log("getAddAllButtonComponent()"); 

	String id = getId(); 

	// Check if the page author has defined an addbutton facet
	UIComponent buttonComponent = getFacet(ADDALL_BUTTON_FACET); 
	// If the page author has not defined a button facet,
	// check if the page author specified a button.
	if(buttonComponent == null) { 

	    String buttonLabel = getTheme().getMessage("AddRemove.addAll"); 
	    if(!isVertical()) { 
		buttonLabel = buttonLabel.concat(" >> "); //NOI18N
	    } 

	    buttonComponent = createButton(buttonLabel, ADDALL_BUTTON_ID, 
					   ADDALL_BUTTON_FACET); 
	    ((Button)buttonComponent).setPrimary(false);
	    StringBuffer jsBuffer = new StringBuffer(200); 
	    jsBuffer.append(JAVASCRIPT_PREFIX); 
	    jsBuffer.append(getAttributes().get(JSOBJECT)); 
	    jsBuffer.append(ADDALL_FUNCTION); 
	    jsBuffer.append(RETURN);
	    ((Button)buttonComponent).setOnClick(jsBuffer.toString());
	} 
	else if(DEBUG) { 
	    log("\tFound facet."); //NOI18N
	} 
	if(buttonComponent != null && isDisabled()) { 
	    buttonComponent.getAttributes().put("disabled", Boolean.TRUE); //NOI18N
	}
	return buttonComponent; 
    } 



    /**
     * Get or create the REMOVE button. Retrieves the component specified by the 
     * removeButton facet (if there is one) or creates a new Button component.
     * @return A UI Component for the Remove button
     */
    public UIComponent getRemoveButtonComponent() { 

	if(DEBUG) log("getRemoveButtonComponent()"); 

	String id = getId(); 

	// Check if the page author has defined an removebutton facet
	UIComponent buttonComponent = getFacet(REMOVE_BUTTON_FACET); 
	// If the page author has not defined a button facet,
	// check if the page author specified a button.
	if(buttonComponent == null) { 

	    String buttonLabel = getTheme().getMessage("AddRemove.remove"); 
	    if(!isVertical()) { 
		buttonLabel = " < ".concat(buttonLabel); //NOI18N
	    } 
	    buttonComponent = createButton(buttonLabel, REMOVE_BUTTON_ID, 
					   REMOVE_BUTTON_FACET); 
	    ((Button)buttonComponent).setPrimary(true);
            StringBuffer jsBuffer = new StringBuffer(200); 
	    jsBuffer.append(JAVASCRIPT_PREFIX); 
	    jsBuffer.append(getAttributes().get(JSOBJECT)); 
	    jsBuffer.append(REMOVE_FUNCTION); 
	    jsBuffer.append(RETURN);
	    ((Button)buttonComponent).setOnClick(jsBuffer.toString());
	} 
	else if(DEBUG) { 
	    log("\tFound facet."); //NOI18N
	} 
	if(buttonComponent != null && isDisabled()) { 
	    buttonComponent.getAttributes().put("disabled", Boolean.TRUE); //NOI18N
	}
	return buttonComponent; 
    } 

    /**
     * Get or create the REMOVE button. Retrieves the component specified by the 
     * removeButton facet (if there is one) or creates a new Button component.
     * @return A UI Component for the Remove button
     */
    public UIComponent getRemoveAllButtonComponent() { 

	if(DEBUG) log("getRemoveAllButtonComponent()"); 

	String id = getId(); 

	// Check if the page author has defined an removebutton facet
	UIComponent buttonComponent = getFacet(REMOVEALL_BUTTON_FACET); 
	// If the page author has not defined a button facet,
	// check if the page author specified a button.
	if(buttonComponent == null) { 


	    String buttonLabel = getTheme().getMessage("AddRemove.removeAll"); 
	    if(!isVertical()) { 
		buttonLabel = " << ".concat(buttonLabel); //NOI18N
	    } 

	    buttonComponent = createButton(buttonLabel, REMOVEALL_BUTTON_ID, 
					   REMOVEALL_BUTTON_FACET);
	    ((Button)buttonComponent).setPrimary(false);
	    StringBuffer jsBuffer = new StringBuffer(200); 
	    jsBuffer.append(JAVASCRIPT_PREFIX); 
	    jsBuffer.append(getAttributes().get(JSOBJECT)); 
	    jsBuffer.append(REMOVEALL_FUNCTION); 
	    jsBuffer.append(RETURN);
	    ((Button)buttonComponent).setOnClick(jsBuffer.toString());
	} 
	else if(DEBUG) { 
	    log("\tFound facet."); //NOI18N
	} 
	if(buttonComponent != null && isDisabled()) { 
	    buttonComponent.getAttributes().put("disabled", Boolean.TRUE); //NOI18N
	}
	return buttonComponent; 
    } 


    /**
     * Get or create the MOVEUP button. Retrieves the component specified by the 
     * moveUpButton facet (if there is one) or creates a new Button component.
     * @return A UI Component for the MoveUp button
     */
    public UIComponent getMoveUpButtonComponent() { 

	if(DEBUG) log("getMoveUpButtonComponent()"); 

	String id = getId(); 

	// Check if the page author has defined an moveUpbutton facet
	UIComponent buttonComponent = getFacet(MOVEUP_BUTTON_FACET); 
	// If the page author has not defined a button facet,
	// check if the page author specified a button.
	if(buttonComponent == null) { 

	    buttonComponent = 
		createButton(getTheme().getMessage("AddRemove.moveUp"), //NOI18N
			     MOVEUP_BUTTON_ID, MOVEUP_BUTTON_FACET); 
	    ((Button)buttonComponent).setPrimary(false);
            StringBuffer jsBuffer = new StringBuffer(200); 
	    jsBuffer.append(JAVASCRIPT_PREFIX); 
	    jsBuffer.append(getAttributes().get(JSOBJECT)); 
	    jsBuffer.append(MOVEUP_FUNCTION); 
	    jsBuffer.append(RETURN);
	    ((Button)buttonComponent).setOnClick(jsBuffer.toString());
	} 
	else if(DEBUG) { 
	    log("\tFound facet."); //NOI18N
	} 
	if(buttonComponent != null && isDisabled()) { 
	    buttonComponent.getAttributes().put("disabled", Boolean.TRUE); //NOI18N
	}
	return buttonComponent; 
    } 


    /**
     * Get or create the MOVEDOWN button. Retrieves the component specified by the 
     * moveDownButton facet (if there is one) or creates a new Button component.
     * @return A UI Component for the MoveDown button
     */
    public UIComponent getMoveDownButtonComponent() { 

	if(DEBUG) log("getMoveDownButtonComponent()"); 

	String id = getId(); 

	// Check if the page author has defined an moveDownbutton facet
	UIComponent buttonComponent = getFacet(MOVEDOWN_BUTTON_FACET); 
	// If the page author has not defined a button facet,
	// check if the page author specified a button.
	if(buttonComponent == null) { 

	    buttonComponent = 
		createButton(getTheme().getMessage("AddRemove.moveDown"), //NOI18N
			     MOVEDOWN_BUTTON_ID, MOVEDOWN_BUTTON_FACET); 
	    ((Button)buttonComponent).setPrimary(false);
            StringBuffer jsBuffer = new StringBuffer(200); 
	    jsBuffer.append(JAVASCRIPT_PREFIX); 
	    jsBuffer.append(getAttributes().get(JSOBJECT)); 
	    jsBuffer.append(MOVEDOWN_FUNCTION); 
	    jsBuffer.append(RETURN);
	    ((Button)buttonComponent).setOnClick(jsBuffer.toString());
	} 
	else if(DEBUG) { 
	    log("\tFound facet."); //NOI18N
	} 
	if(buttonComponent != null && isDisabled()) { 
	    buttonComponent.getAttributes().put("disabled", Boolean.TRUE); //NOI18N
	}
	return buttonComponent; 
    } 


    // Labels
    /**
     * Gets or creates a component for the "available" list
     * label. Retrieves the  availableLabel facet if one was
     * specified, or creates a new label component. 
     * @return A UIComponent for the list label
     */
    public UIComponent getAvailableLabelComponent() { 

	if(DEBUG) log("getAvailableLabelComponent()"); 

	String id = getId(); 

	// Check if the page author has defined a label facet
	UIComponent label = null;
        //mbohm (6452122): check for !Beans.isDesignTime
        if (!Beans.isDesignTime()) {
            label = getFacet(AVAILABLE_LABEL_FACET);
        } 

	// If the page author has not defined a label facet,
	// check if the page author specified a label.
	if(label == null) { 

	    String labelString = getAvailableItemsLabel(); 
	    if(labelString == null || labelString.length() == 0) { 
		labelString = 
		    getTheme().getMessage("AddRemove.available"); //NOI18N
	    }

	    String styleClass = null; 
            int labelLevel = 2; 
	    if(getLabel() == null || 
	       getLabel().length() == 0) { 
		styleClass = 
		    getTheme().getStyleClass(ThemeStyles.ADDREMOVE_LABEL); 
	    } 
	    else { 
		styleClass = 
                    getTheme().getStyleClass(ThemeStyles.ADDREMOVE_LABEL2); 
                labelLevel = 3; 
	    }
	    // TODO - what should we show here? 
	    String forID = getClientId
		(FacesContext.getCurrentInstance()).concat(AVAILABLE_ID); 
	    label = createLabel(labelString, styleClass, forID, labelLevel,
				AVAILABLE_LABEL_ID, 
				AVAILABLE_LABEL_FACET); 
	} 
	else if(DEBUG) { 
	    log("\tFound facet."); //NOI18N
	} 

	return label; 
    } 



    /**
     * Gets or creates a component for the "selected" list
     * label. Retrieves the  selectedLabel facet if one was
     * specified, or creates a new label component. 
     * @return A UIComponent for the list label
     */
    public UIComponent getSelectedLabelComponent() { 

	if(DEBUG) log("getSelectedLabelComponent()"); 

	String id = getId(); 

	// Check if the page author has defined a label facet
	UIComponent label = null;
        //mbohm (6452122): check for !Beans.isDesignTime
        if (!Beans.isDesignTime()) {
            label = getFacet(SELECTED_LABEL_FACET);
        } 

	// If the page author has not defined a label facet,
	// check if the page author specified a label.
	if(label == null) { 

	    String labelString = getSelectedItemsLabel(); 
	    if(labelString == null || labelString.length() == 0) { 
		labelString = 
		    getTheme().getMessage("AddRemove.selected"); //NOI18N
	    }

	    String styleClass = null; 
            int labelLevel = 2; 
	    if(getLabel() == null || 
	       getLabel().length() == 0) { 
		styleClass = 
		    getTheme().getStyleClass(ThemeStyles.ADDREMOVE_LABEL); 
	    } 
	    else { 
                styleClass = 
		    getTheme().getStyleClass(ThemeStyles.ADDREMOVE_LABEL2); 
                labelLevel = 3;
	    }
	    // TODO - what should we show here? 
	    String forID = getClientId
		(FacesContext.getCurrentInstance()).concat(SELECTED_ID); 
	    label = createLabel(labelString, styleClass, forID, labelLevel,
				SELECTED_LABEL_ID, 
				SELECTED_LABEL_FACET); 
	} 
	else if(DEBUG) { 
	    log("\tFound facet."); //NOI18N
	} 

	return label; 
    } 


    /**
     * Gets or creates a component for the "component" list
     * label. Retrieves the  componentLabel facet if one was
     * specified, or creates a new label component. 
     * @return A UIComponent for the list label
     */
    public UIComponent getHeaderComponent() { 

	if(DEBUG) log("getHeaderComponent()"); 

	String id = getId(); 

	// Check if the page author has defined a label facet
	UIComponent label = null;
        //mbohm (6452122): check for !Beans.isDesignTime
        if (!Beans.isDesignTime()) {
            label = getFacet(HEADER_FACET);
        } 

	// If the page author has not defined a label facet,
	// check if the page author specified a label.
	if(label == null) { 

	    String labelString = getLabel(); 
	    if(labelString == null || labelString.length() == 0) { 
		return null; 
	    }

	    String styleClass = 
		getTheme().getStyleClass(ThemeStyles.ADDREMOVE_LABEL); 
	    label = createLabel(labelString, styleClass, null, 2,
				HEADER_ID, HEADER_FACET); 
	    ((Label)label).setLabeledComponent(this); 
	} 
	else if(DEBUG) { 
	    log("\tFound facet."); //NOI18N
	} 

	return label; 
    } 
    
    public String getPrimaryElementID(FacesContext context) {
        return this.getClientId(context).concat(AVAILABLE_ID); 
    }

    private Label createLabel(String labelString, String styleClass, 
                              String forID, int labelLevel, String id, 
                              String facetName) { 

	if(DEBUG) log("createLabel()"); 

	// If we find a label, define a component and add it to the
	// children, unless it has been added in a previous cycle
	// (the component is being redisplayed). 

	if(labelString == null || labelString.length() < 1) { 
	    // TODO - maybe print a default? 
	    labelString = new String(); 
	} 

	Label label = new Label(); 
        label.setLabelLevel(labelLevel);
        label.setId(getId().concat(id));
	label.setText(labelString); 
	label.setStyleClass(styleClass); 
	if(forID != null) { 
	    label.setFor(forID);
	} 

        //mbohm (6452122): always put in facet, since jsf1.2 is not forgiving of "parentless" Label
        this.getFacets().put(facetName, label);
        
	return label;
    } 

    private Button createButton(String text, String id, String facetName) {

	if(DEBUG) log("createButton()"); 

	Button button = new Button(); 
	button.setId(getId().concat(id)); 
	//button.setImmediate(true); 
	button.setText(text); 
	if(getTabIndex() > 0) { 
	    button.setTabIndex(getTabIndex());  
	} 
        button.setPrimary(true);
        // <RAVE>
	// getFacets().put(facetName, button);
        if (!Beans.isDesignTime())
            getFacets().put(facetName, button);
        // </RAVE>
	return button; 
    } 

    private StaticText createText(String text, String id, String facetName) { 

	if(DEBUG) log("createText()"); 

	// If we find a label, define a component and add it to the
	// children, unless it has been added in a previous cycle
	// (the component is being redisplayed). 

	if(text == null || text.length() < 1) { 
	    // TODO - maybe print a default? 
	    text = new String(); 
	} 

	StaticText field = new StaticText(); 
	field.setValue(text);
	field.setId(getId().concat(id)); 
        // <RAVE>
	// getFacets().put(facetName, field);
        if (!Beans.isDesignTime())
            getFacets().put(facetName, field);
        // </RAVE>
	return field;
    } 

    private Theme getTheme() {
	return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
    }

    /**
     * Retrieve an Iterator of ListSelector.ListItem representing the available selections only. 
     * This method is used by the renderer, to create the options of 
     * the list of available items.
     * @return an Iterator over {@link ListItem}.
     * @param context The FacesContext used for the request
     * @param rulerAtEnd If true, a disabled  list item with a blank label is appended at 
     * the end of the options. The role of the blank 
     * item is to guarantee that the width of the lists
     * do not change when items are moved from one to the 
     * other.
     * @throws javax.faces.FacesException If something goes wrong when the options are processed
     */
    public Iterator getListItems(FacesContext context, boolean rulerAtEnd) 
	throws FacesException { 

	if(DEBUG) log("getListItems()");

        Locale locale = context.getViewRoot().getLocale(); 
	if(DEBUG) log("\tLocale is " + locale.toString()); 
	collator =  Collator.getInstance(locale);
	collator.setStrength(Collator.IDENTICAL);
        
	availableItems = new TreeMap(collator);
        selectedItems = new TreeMap(collator); 

	// Retrieve the current selections. If there are selected
	// objects, mark the corresponding items as selected. 
	processOptions(context, collator, locale, rulerAtEnd);

	// We construct a string representation of all values (whether
	// they are selected or not) before we remove the selected
	// items in the processSelections step
	allValues = constructValueString(availableItems); 

	processSelections(); 

	// We construct a string representation of the selected values
	// only 
	selectedValues = 
	    constructValueString(selectedItems, SEPARATOR_VALUE); 

	return availableItems.values().iterator(); 
    } 

    /**
     * Evaluates the list of available Options, creating a ListItem for each
     * one.
     * @param context The FacesContext
     * @param rulerAtEnd the end of the options. The role of the blank 
     * item is to guarantee that the width of the lists
     * do not change when items are moved from one to the 
     * other.
     */
    protected void processOptions(FacesContext context, Collator collator,
                                  Locale locale, boolean rulerAtEnd) { 

	if(DEBUG) log("processOptions()"); 
        
        Option[] options = getOptions();
	int length = options.length; 
        
	ListItem listItem = null; 
	String label = null; 
	String lastKey = ""; 
	String longestString = ""; 
	StringBuffer unsortedKeyBuffer = new StringBuffer(100); 

	for (int counter = 0; counter < length; ++counter) {

	    if(options[counter] instanceof OptionGroup) {
		String msg = MessageUtil.getMessage
                    ("com.sun.rave.web.ui.resources.LogMessages", //NOI18N
		     "AddRemove.noGrouping");                //NOI18N
		log(msg); 
		continue; 
	    } 
	    if(options[counter] instanceof Separator) {
		String msg = MessageUtil.getMessage
                    ("com.sun.rave.web.ui.resources.LogMessages", //NOI18N
		     "AddRemove.noGrouping");                //NOI18N
		log(msg); 
		continue; 
	    }
	    // Convert the option to a list item 
	    listItem = createListItem(options[counter]); 
            
	    label = listItem.getLabel(); 
	    if(label.length() > longestString.length()) { 
		longestString = label; 
	    } 
            
	    if(isSorted()) { 

		availableItems.put(label, listItem); 
		if(collator.compare(label, lastKey) > 0) { 
		    lastKey = label;
		} 
	    } 
	    else { 

		// If the page author does not want the list items to be
		// sorted (alphabetically by locale), then they're
		// supposed to be sorted by the order they were added. 
		// Maps are not guaranteed to return items in the order
		// they were added, so we have to create this order
		// artificially. We do that by creating a successively
		// longer key for each element. (a, aa, aaa...). 
		unsortedKeyBuffer.append("a"); //NOI18N
		availableItems.put(unsortedKeyBuffer.toString(), listItem); 
		lastKey = unsortedKeyBuffer.toString(); 
	    } 
	}

	if(rulerAtEnd) { 

	    int seplength = longestString.length() + 5;
	    StringBuffer labelBuffer = new StringBuffer(seplength);

	    for(int counter=0; counter < seplength; ++counter) {
		labelBuffer.append(SPACER_STRING); 
	    }
	    ListItem item = new ListItem(labelBuffer.toString()); 
            item.setDisabled(true); 
            item.setValue(SEPARATOR_VALUE); 
	    if(isSorted()) { 
                lastKey = lastKey.concat("a");     //NOI18N
		availableItems.put(lastKey, item); //NOI18N
                lastKey = lastKey.concat("a");     //NOI18N
                selectedItems.put(lastKey, item);  //NOI18N
	    } 
	    else { 
		unsortedKeyBuffer.append("a");  //NOI18N
		availableItems.put(unsortedKeyBuffer.toString(), item);
                unsortedKeyBuffer.append("a");  //NOI18N
                selectedItems.put(unsortedKeyBuffer.toString(), item);
	    }
	} 
        
        if(DEBUG) { 
            log("AvailableItems keys"); 
            Iterator iterator = availableItems.keySet().iterator();
            while(iterator.hasNext()) { 
              log("next key " + iterator.next().toString());  
            } 
        }
    }
    private String constructValueString(TreeMap map) { 
	return constructValueString(map, null); 
    } 

    private String constructValueString(TreeMap map, String filter) { 

	// Set up the "All values" string. This is rendered as a
	// hidden input on the client side, and is used to 
	StringBuffer valuesBuffer = new StringBuffer(392); 
	Iterator values = map.values().iterator(); 
	ListItem listItem = null; 
	String separator = getSeparator(); 
	valuesBuffer.append(separator); 
	while(values.hasNext()) { 
	    listItem = (ListItem)(values.next()); 
	    if(filter != null && listItem.getValue().equals(filter)) { 
		continue; 
	    } 
	    valuesBuffer.append(listItem.getValue()); 
	    valuesBuffer.append(separator); 
	} 
	return valuesBuffer.toString(); 
    } 

    /**
     * Retrieve an Iterator of ListSelector.ListItem representing the selected selections only. 
     * This method is used by the renderer, to create the options of 
     * the list of selected items. It is also used when calculating a string
     * representation of the value of the component.
     * @return An Iterator over the selected ListItem
     */
    public Iterator getSelectedListItems() {
	return selectedItems.values().iterator();
    }


    /** 
     * Marks options corresponding to objects listed as values of this
     * component as selected.
     * @param list A list representation of the selected values
     * @param processed If true, compare the values object by
     * object (this is done if we compare the value of the object with
     * with the list items). If false, perform a string comparison of
     * the string representation of the submitted value of the
     * component with the string representation of the value from the
     * list items (this is done if we compare the submitted values
     * with the list items). */
    protected void markSelectedListItems(java.util.List list,
            boolean processed) {
        
        if (DEBUG) log("markSelectedListItems()"); //NOI18N
        
        // The "selected" variable is an iteration over the selected
        // items
        
        // CR 6359071 and 6369187
        // Drive the comparisons from the selected list vs. the
        // available list. This results in the resulting mapped
        // selected list reflecting the order of the original
        // selected list.
        //
        Iterator selected = list.iterator();
        
        boolean allowDups = isDuplicateSelections();
        
        // The selected items are sorted if "isSorted" is true and
        // "isMoveButtons" is false. If "isMoveButtons" is true then
        // the selected items are not sorted even if "isSorted" is true.
        // They appear as they were inserted.
        // If "isSorted" is false and "isMoveButtons" is false, the selected
        // items will appear as they were inserted.
        //
        boolean sorted = isSorted() && !isMoveButtons();
        
        // Use the HashMap "removeItems" to record the selected
        // items that must be removed from the available items.
        // This allows us to not use the available item keys in the
        // selectedItems list, enabling the
        // selectedItems to be sorted as inserted.
        //
        Map removeItems = new HashMap();
        
        // Devise a key to use for the selectedItems. Use the same
        // strategy as used for available items. Create an increasing
        // String of the letter KEY_STRING as selected items are recorded.
        // If sorting, use the available item key.
        //
        String selectedKey = ""; //NOI18N
        
        while (selected.hasNext()) {
            
            Object selectedValue = selected.next();
            
            // The "keys" are the keys of the options on the available map
            // Need to "rewind" for every selected item.
            //
            Iterator keys = availableItems.keySet().iterator();
            
            // Does the current listItem match the selected value?
            boolean match = false;
            
            while (keys.hasNext()) {
                
                Object key = keys.next();
                // The next object from the available map
                //
                Object nextItem = availableItems.get(key);
                ListItem listItem = null;
                
                // If we get an exception just log it and continue.
                // It's cheaper this way than testing with "instanceof".
                //
                try {
                    listItem = (ListItem)nextItem;
                } catch (Exception e) {
                    log("An available item was not a ListItem."); //NOI18N
                    continue;
                }
                
                if (DEBUG) {
                    log("Now processing ListItem " +  //NOI18N
                            listItem.getValue());
                    log("\tSelected object value: " +  //NOI18N
                            String.valueOf(selectedValue));
                    log("\tSelected object type: " +   //NOI18N
                            selectedValue.getClass().getName());
                    if (processed) {
                        log("\tMatching the values by " + //NOI18N
                                "object.equals()"); //NOI18N
                    } else {
                        log("\tMatching the values by string" + //NOI18N
                                "comparison on converted values."); //NOI18N
                    }
                }
                
                if (processed) {
                    match = listItem.getValueObject().equals(selectedValue);
                } else {
                    // Recall that "processed" means that we compare using the
                    // actual value of this component, and this case means that
                    // we compare from the submitted values. In other words, in
                    // this scenario, the selectedValue is an already converted
                    // String.
                    match =
                            selectedValue.toString().equals(listItem.getValue());
                }
                
                // Note that elements in the selected list that do
                // not match will not appear in the "selectedItems"
                // TreeMap.
                //
                if (!match) {
                    continue;
                }
                
                if (DEBUG) log("\tListItem and selected item match"); //NOI18N
                
                // Ensure that the selectedItems are sorted appropriately.
                // Use the sort order of the available items if sorted
                // and the insertion order if not.
                //
                if (sorted) {
                    selectedKey = key.toString();
                } else {
                    selectedKey = selectedKey.concat("a");
                }
                
                // See if we have a dup. If dups are allowed
                // create a new unique key for the dup and add it
                // to the selectedItems.
                // If not a dup, add it to the removeItems map
                // and add it to the selectedItems.
                //
                if (removeItems.containsKey(key)) {
                    if (allowDups) {
                        // In case users are allowed to add the same
                        // item more than once, use this complicated
                        // procedure.
                        // The assumption is that "1" comes before "a".
                        //
                        if (DEBUG) {
                            log("\tAdding duplicate " +  //NOI18N
                                    "and creating unique key."); //NOI18N
                        }
                        String key2 = selectedKey.toString().concat("1");
                        selectedItems.put(key2, listItem);
                    } else {
                        if (DEBUG) {
                            log("\tDuplicates not allowed " +  //NOI18N
                                    "ignoring this duplicate selected item."); //NOI18N
                        }
                    }
                } else {
                    // Add the found key to the removeItems map
                    // and add to the selectedItems.
                    //
                    removeItems.put(key, null);
                    selectedItems.put(selectedKey, listItem);
                }
                
                // We have a match break the loop
                //
                break;
            }
            if (DEBUG) {
                if (!match) {
                    log("\tSelected value " + //NOI18N
                            String.valueOf(selectedValue) +
                            " not present on the list of options."); //NOI18N
                }
            }
        }
        
        if (!allowDups) {
            if (DEBUG) {
                log("\tRemove the selected items from " +
                        "the available items"); //NOI18N
            }
            Iterator keys = removeItems.keySet().iterator();
            Object key = null;
            while(keys.hasNext()) {
                key = keys.next();
                availableItems.remove(key);
            }
        }
    }

    public boolean mainListSubmits() {
        return false;
    }
}
