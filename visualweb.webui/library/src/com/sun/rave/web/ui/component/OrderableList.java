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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import com.sun.rave.web.ui.component.Button;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.StaticText;
import com.sun.rave.web.ui.model.list.ListItem;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.util.ThemeUtilities;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.util.ValueType;
import com.sun.rave.web.ui.util.ValueTypeEvaluator; 


/**
 * <h4>About this tag.</h4>
 * 
 *    <p>This tag renders an OrderableList component. Use this component
 *    when web application users need to create and modify a list of
 *    strings. The application user can add new strings by typing them
 *    into the textfield and clicking the "moveUp" button, and remove them
 *    by selecting one or more items from the list and clicking the
 *    "Remove" button.</p>  
 * 
 *    <h4>Configuring the listbox tag</h4>
 * 
 *    <p> Use the <code>list</code> attribute to bind the component
 *    to a model. The value must be an EL expression that corresponds to
 *    a managed bean or a property of a managed bean, and it must
 *    evaluate to an array of  <code>java.lang.String</code>. 
 *    </p> 
 * 
 *    <p>To set the label of the textfield, use the
 *    <code>fieldLabel</code> attribute. To set the label of the
 *    textfield, use the <code>listLabel</code> attribute. To validate
 *    new items, use the <code>fieldValidator</code> attribute; to
 *    validate the contents of the list once the user has finished
 *    adding and removing items, specify a <code>labelValidator</code>.</p> 
 * 
 *    <h4>Facets</h4>
 * 
 *    <ul>
 *    <li><code>fieldLabel</code>: use this facet to specify a custom 
 *    component for the textfield label.</li>
 *    <li><code>listLabel</code>: use this facet to specify a custom 
 *    component for the textfield label.</li>
 *    <li><code>field</code>: use this facet to specify a custom 
 *    component for the textfield.</li>
 *     <li><code>moveUpButton</code>: use this facet to specify a custom 
 *    component for the moveUp button.</li>
 *     <li><code>removeButton</code>: use this facet to specify a custom 
 *    component for the remove button.</li>
 *    <li><code>search</code>: use this facet to specify a custom 
 *    component for the search button. </li>
 *    <li><code>readOnly</code>: use this facet to specify a custom 
 *    component for display a readonly version of the component.</li>
 *    <li><code>header</code>: use this facet to specify a header,
 *    rendered in a table row above the component.</li>
 *    <li><code>footer</code>: use this facet to specify a header,
 *    rendered in a table row below the component.</li>
 *    </ul>
 * 
 *    <h4>Client-side JavaScript functions</h4>
 * 
 *    <ul>
 *    <li>NONE yet</li> 
 *    </ul>
 * @author avk
 */
public class OrderableList extends OrderableListBase implements ListManager {
    
    /**
     * The component id for the moveUp button.
     */
    public static final String MOVEUP_BUTTON_ID = "_moveUpButton"; //NOI18N
    /**
     * The facet name for the moveUp button.
     */
    public static final String MOVEUP_BUTTON_FACET = "moveUpButton"; //NOI18N
    
    /**
     * The component id for the moveDown button.
     */
    public static final String MOVEDOWN_BUTTON_ID = "_moveDownButton"; //NOI18N
    /**
     * The facet name for the moveDown button.
     */
    public static final String MOVEDOWN_BUTTON_FACET = "moveDownButton"; //NOI18N
    
    /**
     * The component id for the moveTop button.
     */
    public static final String MOVETOP_BUTTON_ID = "_moveTopButton"; //NOI18N
    /**
     * The facet name for the moveTop button.
     */
    public static final String MOVETOP_BUTTON_FACET = "moveTopButton"; //NOI18N
    
    /**
     * The component id for the moveBottom button.
     */
    public static final String MOVEBOTTOM_BUTTON_ID = "_moveBottomButton"; //NOI18N
    /**
     * The facet name for the moveBottom button.
     */
    public static final String MOVEBOTTOM_BUTTON_FACET = "moveBottomButton"; //NOI18N
    
    /**
     * The component ID for the remove button.
     */
    public static final String REMOVE_BUTTON_ID = "_removeButton"; //NOI18N
    /**
     * The facet name for the remove button.
     */
    public static final String REMOVE_BUTTON_FACET = "removeButton"; //NOI18N
   
    /**
     * The component ID for the label.
     */
    public static final String LABEL_ID = "_label"; //NOI18N
    /**
     * The facet name for the label.
     */
    public static final String LABEL_FACET = "label"; //NOI18N
    
    /**
     * The component ID for the read only text field.
     */
    public static final String READ_ONLY_ID = "_readOnly"; //NOI18N
    /**
     * The facet name for the readOnly text field.
     */
    public static final String READ_ONLY_FACET = "readOnly"; //NOI18N

    /** 
     * The name for the footer facet.
     */ 
    public static final String FOOTER_FACET = "footer"; //NOI18N

   /**
     * The name of the component attribute that stores the name of the
     * JavaScript object that organizes the JavsSCript functions for the 
     * component.
     */
    public static final String JSOBJECT = "com.sun.rave.web.ui.OrderableListJS";
    /**
     * Represents the "javascript:" printed at the start of javascript
     * event handler code.
     */
    public static final String JAVASCRIPT_PREFIX = "javascript: ";
    /**
     * String representing "return false" printed at the end of the
     * javascript event handlers.
     */
    public static final String RETURN = "return false;"; 
  
    
    /**
     * Name of the JavaScript function which moves elements up.
     */
    public static final String MOVEUP_FUNCTION = ".moveUp(); ";
    /**
     * Name of the JavaScript function which moves elements down.
     */
    public static final String MOVEDOWN_FUNCTION = ".moveDown();";      
    /**
     * Name of the JavaScript function which moves elements to the top.
     */
    public static final String MOVETOP_FUNCTION = ".moveTop(); ";
    /**
     * Name of the JavaScript function which moves elements to the bottom.
     */
    public static final String MOVEBOTTOM_FUNCTION = ".moveBottom();";          
    /**
     * Name of the JavaScript function that updates the buttons.
     */
    public static final String UPDATEBUTTONS_FUNCTION = ".updateButtons(); ";
    /**
     * Name of the JavaScript function that handles changes on the
     * available list.
     */
    public static final String ONCHANGE_FUNCTION = ".onChange(); ";

    /**
     * Read only separator string
     */
    private static final String READ_ONLY_SEPARATOR = ", "; //NOI18N

    // Holds the ValueType of this component
    private ValueTypeEvaluator valueTypeEvaluator = null; 
    private ArrayList listItems = null; 
    private transient Theme theme = null; 
  
    private static final boolean DEBUG = false; 
    
    public OrderableList() {
        valueTypeEvaluator = new ValueTypeEvaluator(this);
    }

    // Buttons
    /**
     * Get or create the MOVE UP button. Retrieves the component specified by the 
     * moveUpButton facet (if there is one) or creates a new Button component.
     * @return A UI Component for the moveUp button
     */
    public UIComponent getMoveUpButtonComponent(FacesContext context) { 

	if(DEBUG) log("getMoveUpButtonComponent()"); 

	String id = getId(); 
  
	// Check if the page author has defined an moveUpbutton facet
	UIComponent buttonComponent = getFacet(MOVEUP_BUTTON_FACET); 
	// If the page author has not defined a button facet,
	// check if the page author specified a button.
	if(buttonComponent == null) { 

	    buttonComponent = 
                createButton(getTheme().getMessage("OrderableList.moveUp"), //NOI18N
                             MOVEUP_BUTTON_ID, MOVEUP_BUTTON_FACET); 
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
     * @return A UI Component for the MOVEDOWN button
     */
    public UIComponent getMoveDownButtonComponent(FacesContext context) { 

	if(DEBUG) log("getmoveDownButtonComponent()"); 

	String id = getId(); 
  
	// Check if the page author has defined an moveDownbutton facet
	UIComponent buttonComponent = getFacet(MOVEDOWN_BUTTON_FACET); 
	// If the page author has not defined a button facet,
	// check if the page author specified a button.
	if(buttonComponent == null) { 

	    buttonComponent = 
                createButton(getTheme().getMessage("OrderableList.moveDown"), //NOI18N
                             MOVEDOWN_BUTTON_ID, MOVEDOWN_BUTTON_FACET); 
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

    /**
     * Get or create the MOVETOP button. Retrieves the component specified by the 
     * moveTopButton facet (if there is one) or creates a new Button component.
     * @return A UI Component for the Add button
     */
    public UIComponent getMoveTopButtonComponent(FacesContext context) { 

	if(DEBUG) log("getAddButtonComponent()"); 

	String id = getId(); 
  
	// Check if the page author has defined an moveTopbutton facet
	UIComponent buttonComponent = getFacet(MOVETOP_BUTTON_FACET); 
	// If the page author has not defined a button facet,
	// check if the page author specified a button.
	if(buttonComponent == null) { 

	    buttonComponent = 
                createButton(getTheme().getMessage("OrderableList.moveTop"), //NOI18N
                             MOVETOP_BUTTON_ID, MOVETOP_BUTTON_FACET); 
            StringBuffer jsBuffer = new StringBuffer(200); 
	    jsBuffer.append(JAVASCRIPT_PREFIX); 
	    jsBuffer.append(getAttributes().get(JSOBJECT)); 
	    jsBuffer.append(MOVETOP_FUNCTION); 
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
     * Get or create the MOVEBOTTOM button. Retrieves the component specified by the 
     * moveBottomButton facet (if there is one) or creates a new Button component.
     * @return A UI Component for the Move Bottom button
     */
    public UIComponent getMoveBottomButtonComponent(FacesContext context) { 

	if(DEBUG) log("getMoveBottomButtonComponent()"); 

	String id = getId(); 
  
	// Check if the page author has defined an addbutton facet
	UIComponent buttonComponent = getFacet(MOVEBOTTOM_BUTTON_FACET); 
	// If the page author has not defined a button facet,
	// check if the page author specified a button.
	if(buttonComponent == null) { 

	    buttonComponent = 
                createButton(getTheme().getMessage("OrderableList.moveBottom"), //NOI18N
                             MOVEBOTTOM_BUTTON_ID, MOVEBOTTOM_BUTTON_FACET); 
            StringBuffer jsBuffer = new StringBuffer(200); 
	    jsBuffer.append(JAVASCRIPT_PREFIX); 
	    jsBuffer.append(getAttributes().get(JSOBJECT)); 
	    jsBuffer.append(MOVEBOTTOM_FUNCTION); 
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
     * Gets or creates a component for the list label. Retrieves the 
     * listLabel facet if one was specified, or creates a new label component.
     * @return A UIComponent for the list label
     */
    public UIComponent getHeaderComponent() { 
	
	if(DEBUG) log("getListLabelComponent()"); 

	String id = getId(); 

	// Check if the page author has defined a label facet
	UIComponent labelComponent = getFacet(LABEL_FACET); 

	// If the page author has not defined a label facet,
	// check if the page author specified a label.
	if(labelComponent == null) { 

	    String labelString = getLabel(); 
	    if(labelString == null || labelString.length() == 0) { 
		labelString = 
                    getTheme().getMessage("OrderableList.defaultListLabel"); //NOI18N
	    }

	    labelComponent = createLabel(labelString, 
                                         LABEL_ID, 
                                         LABEL_FACET); 
            
            ((Label)labelComponent).setLabeledComponent(this); 
	} 
	else if(DEBUG) { 
	    log("\tFound facet."); //NOI18N
	} 
      
	return labelComponent; 
    } 

     public String getPrimaryElementID(FacesContext context) {
        return this.getClientId(context).concat(ListSelector.LIST_ID); 
    }
     
    /**
     * Retrieve an Iterator of ListSelector.ListItem, to be used by the
     * renderer. 
     * @return an Iterator over {@link ListItem}.
     * @throws javax.faces.FacesException 
     */
    public Iterator getListItems(FacesContext context, boolean ruler) throws FacesException {
        
        if(DEBUG) log("getListItems()");
        
	listItems = new ArrayList(); 

        Object submittedValue = getSubmittedValue(); 
        if(submittedValue != null && submittedValue instanceof String[]) { 
            ListItem listItem = null; 
            String[] values = (String[])submittedValue; 
            for(int counter=0; counter < values.length; ++counter) {
                if(DEBUG) log("Adding listItem " + values[counter]); 
                listItem = new ListItem(values[counter], values[counter]);
                listItem.setValue(values[counter]);               
                listItems.add(listItem);
            }
            return listItems.iterator();           
        } 
             
	Object listItemsObject = getList(); 
	if(listItemsObject == null) { 
            if(DEBUG) log("\tNo list items!");
	    // do nothing...
	} 
        else if(valueTypeEvaluator.getValueType() == ValueType.LIST) {
	    Iterator items = ((java.util.List)listItemsObject).iterator(); 
	    Object item;
            ListItem listItem; 
	    while(items.hasNext()) { 
		item = items.next(); 
                listItems.add(createListItem(this, item)); 
	    } 
	} 
	
	 else if(valueTypeEvaluator.getValueType() == ValueType.ARRAY) {
            
            if(DEBUG) log("\tFound array value"); 

	    // The strings variable represents the strings entered by
	    // the user, as an array. 
            
            
 	    Object[] listObjects = (Object[])listItemsObject; 
            
            for(int counter=0; counter<listObjects.length; ++counter) {
                listItems.add(createListItem(this, listObjects[counter])); 
            } 
        }

	else { 
	    String msg = getTheme().getMessage("OrderableList.invalidListType"); //NOI18N 
	    throw new FacesException(msg); 
	} 
            
	return listItems.iterator(); 
    } 
    
    /**
     * Enforce non null values.
     * This is ok, since Converter returns null on null input.
     * And secondly this is equivalent to SelectItem and therefore
     * Option which do not allow null values.
     *
     * However we have to be wary of values that are "".
     * But if the null case is out of the way the this should
     * work ok.
     */
    protected ListItem createListItem(UIComponent comp, Object value) { 
        
	// Do not allow null values
	//
	if (value == null) {
	    throw new NullPointerException(
	    	"OrderableList ListItems cannot have null values");
	}
        if(DEBUG) log("createListItem()"); 
        String label = ConversionUtilities.convertValueToString(comp, value);
        if(DEBUG) log("\tLabel is " + label); 
        ListItem listItem = new ListItem(value, label); 
        if(DEBUG) log("\tCreated ListItem"); 
        listItem.setValue(label);  
        return listItem; 
    }
     
    public String[] getValueAsStringArray(FacesContext context) {
        
        if(DEBUG) log("getValueAsStringArray)");
        
        Iterator iterator = getListItems(context, false);
        int numItems = listItems.size();
        String[] values = new String[numItems];
        
        int counter = 0;
        while(counter < numItems) {
            values[counter] = ((ListItem)(iterator.next())).getValue();
            if(DEBUG) log("List item value " + String.valueOf(values[counter]));
            ++counter;
        }
        return values;
    }
        
        /**
     * Retrieve the value of this component (the "selected" property) as an  
     * object. This method is invoked by the JSF engine during the validation 
     * phase. The JSF default behaviour is for components to defer the 
     * conversion and validation to the renderer, but for the Selector based
     * components, the renderers do not share as much functionality as the 
     * components do, so it is more efficient to do it here. 
     * @param context The FacesContext of the request
     * @param submittedValue The submitted value of the component
     */    
    public Object getConvertedValue(FacesContext context, 
                                    Object submittedValue)
        throws ConverterException {
        
        if(DEBUG) log("getConvertedValue()");
        
        if(!(submittedValue instanceof String[])) { 
            throw new ConverterException(
		"Submitted value must be a String array"); 
        } 
        String[] rawValues = (String[])submittedValue; 

	// If there are no elements in rawValues nothing was submitted.
	// If null was rendered, return null
	//
	if(rawValues.length == 0) { 
	    if (ConversionUtilities.renderedNull(this)) {
		return null; 
	    }
	} 

        Object cValue = null; 
        try { 
            
	    if(valueTypeEvaluator.getValueType() == ValueType.ARRAY) { 
		if(DEBUG) log("\tComponent value is an array"); 
		cValue = ConversionUtilities.convertValueToArray
		    (this, rawValues, context); 
	    } 
	    // This case is not supported yet!
	    else if(valueTypeEvaluator.getValueType() == ValueType.LIST) { 
		if(DEBUG) log("\tComponent value is a list"); 
		cValue = ConversionUtilities.convertValueToList
		    (this, rawValues, context); 
	    } 
        } 
        catch(Exception ex) {
            if(DEBUG) ex.printStackTrace(); 
        }
        return cValue;
    } 

    
    // Readonly value
    /**
     * Return a string suitable for displaying the value in read only mode.
     * The default is to separate the list values with a comma.
     *
     * @param context The FacesContext
     * @throws javax.faces.FacesException If the list items cannot be processed
     */
    protected String getValueAsReadOnly(FacesContext context)
                                 throws FacesException {
        
	// The comma format READ_ONLY_SEPARATOR should be part of the theme
	// and/or configurable by the application
	//
	StringBuffer valueBuffer = new StringBuffer(200); 
        
        Iterator iterator = getListItems(context, false); 

        while(iterator.hasNext()) { 
            String string = ((ListItem)(iterator.next())).getLabel();
            // Do this with a boolean on getListItems instead
            if(string.indexOf("nbsp") > -1) {  //NOI18N
                continue;
            }
            valueBuffer.append(string);
            if(iterator.hasNext()) {
                valueBuffer.append(READ_ONLY_SEPARATOR);
            }
        }
	return valueBuffer.toString();
    }

    /**
     * Creates a component for the OrderableList in case the component
     * is read-only.
     * @return A UIComponent that displays the read-only value
     */
    public UIComponent getReadOnlyValueComponent() { 
	
	if(DEBUG) log("getListLabelComponent()");  

	String id = getId(); 

	// Check if the page author has defined a label facet
	UIComponent textComponent = getFacet(READ_ONLY_FACET); //NOI18N

	// If the page author has not defined a label facet,
	// check if the page author specified a label.
	if(textComponent == null) { 
            FacesContext context = FacesContext.getCurrentInstance();
	    textComponent = 
                createText(getValueAsReadOnly(context), //NOI18N
                           READ_ONLY_ID, READ_ONLY_FACET); 
	} 
	else if(DEBUG) { 
	    log("\tFound facet."); //NOI18N
	} 
	return textComponent; 
    } 

    // TODO - these are reused by several list components, can we refactor these? 
    private Label createLabel(String labelString, String id, 
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
	label.setText(labelString); 
        label.setLabelLevel(getLabelLevel()); 
        this.getFacets().put(facetName, label);
        label.setId(getId().concat(id)); 
	return label;
    } 
    
    // TODO - these are reused by several list components, can we refactor these?  
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
	getFacets().put(facetName, field); 
	return field;
    } 

   
     // TODO - these are reused by several list components, can we refactor these? 
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
	getFacets().put(facetName, button); 
	return button; 
    } 


    private void log(String s) {
        System.out.println(this.getClass().getName() + "::" + s); //NOI18N
    }
    
    private Theme getTheme() {
	return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
    }

    public int getRows() {

        int rows =  super.getRows();
        if(rows < 1) { 
            rows = 12; 
            super.setRows(12);
        }
        return rows;
    }

    public String getOnChange() {
        return null;
    }

    /**
     * <p>Return <code>true</code> if the new value is different from the
     * previous value.</p>
     *
     * This only implements a compareValues for value if it is an Array.
     * If value is not an Array, defer to super.compareValues.
     * The assumption is that the ordering of the elements
     * between the previous value and the new value is determined
     * in the same manner.
     *
     * Another assumption is that the two object arguments
     * are of the same type, both arrays of both not arrays.
     *
     * @param previous old value of this component (if any)
     * @param value new value of this component (if any)
     */
    protected boolean compareValues(Object previous, Object value) {

	// Let super take care of null cases
	//
	if (previous == null || value == null) {
	    return super.compareValues(previous, value);
	}
	if (value instanceof Object[]) {
	    // If the lengths aren't equal return true
	    //
	    int length = Array.getLength(value);
	    if (Array.getLength(previous) != length) {
		return true;
	    }
	    // Each element at index "i" in previous must be equal to the
	    // elementa at index "i" in value.
	    //
	    for (int i = 0; i < length; ++i) {

		Object newValue = Array.get(value, i);
		Object prevValue = Array.get(previous, i);

		// This is probably not necessary since
		// an Option's value cannot be null
		//
		if (newValue == null) {
		    if (prevValue == null) {
			continue;
		    } else {
			return true;
		    }
		}
		if (prevValue == null) {
		    return true;
		}

		if (!prevValue.equals(newValue)) {
		    return true;
		}
	    }
	    return false;
        }
	return super.compareValues(previous, value);
    }

    public boolean mainListSubmits() {
        return false;
    }
}
