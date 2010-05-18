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

import com.sun.rave.web.ui.model.OptionTitle;
import com.sun.rave.web.ui.util.ThemeUtilities;
import java.beans.Beans;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import com.sun.rave.web.ui.model.Option;
import com.sun.rave.web.ui.model.OptionGroup;
import com.sun.rave.web.ui.model.Separator;
import com.sun.rave.web.ui.model.list.EndGroup; 
import com.sun.rave.web.ui.model.list.ListItem;
import com.sun.rave.web.ui.model.list.StartGroup; 
import com.sun.rave.web.ui.util.ConversionUtilities; 
import com.sun.rave.web.ui.util.MessageUtil;
import com.sun.rave.web.ui.util.ValueType; 
import com.sun.rave.web.ui.component.util.Util;
import javax.faces.component.ValueHolder;

/**
 *
 * @author avk
 */
public class ListSelector extends ListSelectorBase implements ListManager {

    // If true, debugging statements are printed to stdout
    private static final boolean DEBUG = false;

    // Holds the options for this component
    protected ArrayList listItems = null; 
    private int separatorLength = 0; 
      
    private static final String READONLY_ID = "_readOnly"; //NOI18N
    private static final String LABEL_ID = "_label"; //NOI18N
    private static final String READONLY_FACET = "readOnly"; //NOI18N
    private static final String LABEL_FACET = "label";               
    public static final String VALUE_ID = "_list_value";        //NOI18N
    public static final String LIST_ID = "_list";               //NOI18N

    /** Creates a new instance of ListSelector */
    public ListSelector() {
    }
  
    /**
     * Check that this component has a valuebinding that matches the
     * value of the "multiple" attribute. 
     * @param context The FacesContext of the request
     */
    public void checkSelectionModel(FacesContext context) {
	
        if(DEBUG) { 
            log("checkSelectionModel()"); //NOI18N
            log("\tComponent multiple = " + String.valueOf(isMultiple())); //NOI18N
            log("\tValueType " + valueTypeEvaluator.getValueType().toString()); //NOI18N
        }
             
        if(isMultiple() && 
	   valueTypeEvaluator.getValueType() != ValueType.ARRAY) {
         
            if(DEBUG) log("\tMultiple selection enabled for non-array value");
	    Object[] params = {  toString() }; 
	    String msg = MessageUtil.getMessage
                    ("com.sun.rave.web.ui.resources.LogMessages", //NOI18N
		     "Selector.multipleError",           //NOI18N
                     params); 
            throw new RuntimeException(msg);
        }
        return;
    } 


    /**
     * Retrieve an Iterator of ListSelector.ListItem, to be used by the
     * renderer. 
     * @return an Iterator over {@link ListItem}. 
     */
    public Iterator getListItems(FacesContext context, boolean rulerAtEnd) 
	throws FacesException {
        
        if(DEBUG) log("getListItems()");


        listItems = new ArrayList();
	separatorLength = 0; 

	// Retrieve the current selections. If there are selected
	// objects, mark the corresponding items as selected. 
        processOptions(getOptions());
	
	processSelections(); 
        
	return listItems.iterator(); 
    } 
    
    /**
     * Retrieve an Iterator of ListSelector.ListItem, to be used when 
     * evaluting the list items. If the list items are needed by the 
     * renderer, use getListItems(context, rulerAtEnd) instead.
     * @return an Iterator over {@link ListItem}. 
     */
    public Iterator getListItems() throws FacesException {
        
        if(DEBUG) log("getListItems()");
        if(listItems != null) { 
            return listItems.iterator();
        } 
       
        listItems = new ArrayList();
        processOptions(getOptions());
	return listItems.iterator();
    } 


    /**
     * This method resets the options. Use this only if you need to
     * add or remove options after the component has been rendered once.
    public void resetOptions() { 
	listItems = null; 
    } 
     */

    public int getSeparatorLength() {
        return separatorLength;
    }

    /** 
     * Processes the component's SelectItems. Constructs an ArrayList
     * of Selector.Options.
     *
     * <ul>
     * <li>General algorithm copied from the RI, except that I modified
     * the class casts for readability. I don't think the algorithm is 
     * correct though, need to verify. </li> 
     * <li>The list of allowed data types must match the spec. </li>
     * <li>This code will have to be replaced when switching
     * to Selection.</li>
     * </ul> 
     */

    protected Option[] getOptions() { 

	Option[] options = null; 
	Object optionsObject = getItems(); 

	// TODO - add some error reporting... 

	if(optionsObject instanceof Option[]) { 
	    options = (Option[])optionsObject;
	} 
	else if(optionsObject instanceof Collection) { 
            Object[] objects = ((Collection)optionsObject).toArray(); 
            if(objects == null || objects.length == 0) {
                options = new Option[0];
            }
            
            int numObjects = objects.length;
            options = new Option[numObjects]; 
            for(int counter = 0; counter < numObjects; ++counter) { 
                options[counter] = (Option)objects[counter];
            }
	} 
	else if(optionsObject instanceof Map) {
            Collection itemsCollection = ((Map)optionsObject).values();
            Option[] newOptions = new Option[itemsCollection.size()];
	    options = (Option[]) itemsCollection.toArray(newOptions); 
	} 
	// The items attribute has not been specified
	else { 
	    // do nothing
	    options =  new Option[0]; 
	} 
        return options;
    } 

    protected void processOptions(Option[] options) { 

	if(DEBUG) log("processOptions()"); 
	int length = options.length; 
	
	for (int counter = 0; counter < length; ++counter) {

	    if(options[counter] instanceof OptionGroup) {

		OptionGroup selectionGroup = 
                    (OptionGroup)options[counter]; 
		String groupLabel = selectionGroup.getLabel(); 

		if(DEBUG) { 
		    log("\tFound SelectionGroup"); //NOI18N
		    log("\tLabel is " + groupLabel); //NOI18N
		} 

                // <RAVE>
                // if((groupLabel.length() * 1.5) > separatorLength) { 
                // </RAVE>
		if(groupLabel != null && (groupLabel.length() * 1.5) > separatorLength) { 
		    // FIXME - needs to be dependent on the
		    // browser if not the OS... ARRGGH.
		    separatorLength = (int)(groupLabel.length() * 1.5); 
		} 
                
		listItems.add(new StartGroup(groupLabel)); 
		processOptions(selectionGroup.getOptions());
		listItems.add(new EndGroup()); 
	    } 
	    else if(options[counter] instanceof Separator) {
		listItems.add(options[counter]); 
	    }
	    else {
                listItems.add(createListItem(options[counter]));
            }
	}
    }


    /**
     * Retrieve the current selections and compare them with the list
     * items. 
     */
    protected void processSelections() { 
        
        if(DEBUG) log("processSelections()");

	// For the "immediate" case: 
        Object value = getSubmittedValue();

	if(value != null) { 

	    if(DEBUG) log("Found submitted value"); 

	    if(value instanceof String[]) {

		if(DEBUG) log("found submitted value (string array)");

		String[] obj = (String[])value;
		ArrayList list = new ArrayList(obj.length);
		for(int counter =0; counter < obj.length; ++counter) { 
		    list.add(obj[counter]); 
		    if(DEBUG) log("\tAdded " + obj[counter]); 
		} 
		markSelectedListItems(list, false); 
		return;
	    }

	    throw new IllegalArgumentException
		("Illegal submitted value"); //NOI18N
	}
        
	// For the first time and "non-immediate" case: 
        if(DEBUG) log("No submitted values, use actual value");

	// Covers List cases
        if(valueTypeEvaluator.getValueType() == ValueType.NONE || 
	   valueTypeEvaluator.getValueType() == ValueType.INVALID) { 
            if(DEBUG) log("\tNo value");
	    markSelectedListItems(new ArrayList(), true); 
	    return; 
        }

        value = getValue();
        
        if(DEBUG) {
            if(value == null) log("\t actual value is null"); //NOI18N
            else log("\t actual value is of type " +          //NOI18N
                    value.getClass().getName());
        }
	if(value == null) { 
            if(DEBUG) log("\tNo value");
	    markSelectedListItems(new ArrayList(), true); 
	    return; 
	} 

	// Covers List cases
        /*
        if(valueTypeEvaluator.getValueType() == ValueType.LIST) { 
            if(DEBUG) log("found actual value (list)");

	    Object[] params = {  toString() }; 
	    String msg =
                ThemeUtilities.getTheme(FacesContext.getCurrentInstance()).
		    getMessage("ListSelector.multipleError", params); //NOI18N
            throw new IllegalArgumentException(msg);

	    //markSelectedOptions((java.util.List)value, true); 
	    //return; 
        }
         */

	ArrayList list = new ArrayList(); 

	// Covers Object array
        if(valueTypeEvaluator.getValueType() == ValueType.ARRAY) { 

	    int length = Array.getLength(value); 
	    for(int counter = 0; counter < length; ++counter) { 
		list.add(Array.get(value, counter)); 
		if(DEBUG) log(String.valueOf(Array.get(value, counter))); 
	    } 
	    markSelectedListItems(list, true); 
	    return; 
        }

	// Covers Object array
	list.add(value); 
	if(DEBUG) log("\tAdded object " + String.valueOf(value));  
	markSelectedListItems(list, true); 
	return; 
    }


    /** 
     * Marks options corresponding to objects listed as values of this
     * components as selected.
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

	if(DEBUG) log("markSelectedListItems()");
        
	ListItem option = null; 
        Object nextItem = null;
	Iterator items = listItems.iterator(); 
	Iterator selected = null; 
       
	while(items.hasNext()) { 
            nextItem = items.next(); 
            // If the next item is a selection group, we continue. 
            // Need to check this with the guidelines, perhaps
            // you can select options too... 
	    if(!(nextItem instanceof ListItem)) { 
                  continue;
            }
            
	    option = (ListItem)nextItem; 

	    // By default, the option will not be marked as selected
	    option.setSelected(false); 
	    
	    if(DEBUG) { 
		log("\tItem value: " + option.getValue()); //NOI18N
		log("\tItem type: " +                      //NOI18N
		    option.getValueObject().getClass().getName());
	    }

	    // There are no more selected items, continue with the
	    // next option
	    if(list.isEmpty()) { 
		if(DEBUG) log("No more selected items"); //NOI18N
		continue; 
	    } 

	    // There are still selected items to account for
	    selected = list.iterator();
	    while(selected.hasNext()) { 
		if(processed) { 
		    Object o = selected.next(); 
                    if(DEBUG) {
                        log("\tSelected object value: " +  //NOI18N
			    String.valueOf(o));
                        log("\tSelected object type: " +   //NOI18N 
			    o.getClass().getName());
                    }
		    if (option.getValueObject().equals(o)) { 
			if(DEBUG) { 
			    log("\tFound a match: " +  //NOI18N
				String.valueOf(o)); 
			} 
			option.setSelected(true); 
			list.remove(o); 
			break;
		    }
                }
		else { 
		    String s = (String)selected.next(); 
		    if(s.equals(option.getValue())) { 
			if(DEBUG) { 
			    log("\tFound a match: " + s);   //NOI18N
			} 
			option.setSelected(true); 
			list.remove(s); 
			break;
                    }
                }
            }
        }

	// At this point the selected list should be empty.
	if(!list.isEmpty() && !Beans.isDesignTime()) { 
	    String msg = MessageUtil.getMessage(
                    "com.sun.rave.web.ui.resources.LogMessages", //NOI18N
		    "List.badValue", 
                new Object[]{ getClientId(FacesContext.getCurrentInstance()) });
            //throw new FacesException(msg); 
	    log(msg); 
	}
    }
    
    /* Add an option to the list */ 
    protected ListItem createListItem(Option si) {
        
        if(DEBUG) log("createListItem()");
        
	String label = si.getLabel(); 
         
         String valueString = 
 	    ConversionUtilities.convertValueToString(this, si.getValue());
         
         if(label == null)
             label = valueString;
          
	if((label.length() * 1.5) > separatorLength) { 
	    separatorLength = (int)(label.length() * 1.5); 
	} 

        ListItem listItem = new ListItem(si.getValue(), label, si.getDescription(), 
                                   si.isDisabled());
        
	listItem.setValue(valueString); 	         
        if(si instanceof OptionTitle)
 	    listItem.setTitle(true);
        return listItem;
        
    }
    
    // Labels
    /**
     * Return a component that implements the label for this ListSelector.
     * If a facet named <code>label</code> is found
     * that component is returned. Otherwise a <code>Label</code> component
     * is returned. It is assigned the id</br>
     * <code>getId() + "_label"</code></br>
     * <p>
     * If the facet is not defined then the returned <code>Label</code>
     * component is re-intialized every time this method is called.
     * </p>
     *
     * @return a label component for this ListSelector
     */
    public UIComponent getLabelComponent() { 
	
	if(DEBUG) log("getLabelComponent()"); 

	String id = getId(); 

	// Check if the page author has defined a label facet
	UIComponent labelComponent = getFacet(LABEL_FACET); //NOI18N

	// If the page author has not defined a label facet,
	// check if the page author specified a label.
	if(labelComponent == null && 
           getLabel () != null && 
           getLabel().length() > 0) { 
            labelComponent = createLabel(getLabel()); //NOI18N
	} 
	else if(DEBUG) { 
	    log("\tFound facet."); //NOI18N
	} 
	
	return labelComponent; 
    } 

    // Readonly value
    public UIComponent getReadOnlyValueComponent() { 
	
	if(DEBUG) log("getListLabelComponent()"); 

	String id = getId(); 
        
        //<RAVE>
        FacesContext context = FacesContext.getCurrentInstance();
        String readOnlyValue = getValueAsReadOnly(context);

	//Check if the page author has defined a readonly facet, 
        //or if we've stored one
	UIComponent textComponent = getFacet(READONLY_FACET); //NOI18N

	if(textComponent == null) { 
	    textComponent = createText(readOnlyValue); //NOI18N
	} 
	else {
            if(DEBUG) { 
                log("\tFound facet."); //NOI18N
            }
            modifyReadOnlyTextComponent(textComponent, readOnlyValue);
	}
        //</RAVE>
	
	return textComponent; 
    } 

      /**
     * Get the value (the object representing the selection(s)) of this 
     * component as a String array.
     *
     * @param context The FacesContext of the request
     */
    public String[] getValueAsStringArray(FacesContext context) {

        String[] values = null; 
        
	Object value = getSubmittedValue();    
        if(value != null) {
            if(value instanceof String[]) {
               return (String[])value;
            } else if(value instanceof String) {
                values = new String[1];
                values[0] = (String)value;
                return values;
            }
        }
        
        value = getValue();
        if(value == null) { 
            return new String[0];
        }
        
        // No submitted value found - look for 

	if(valueTypeEvaluator.getValueType() == ValueType.NONE) { 
	    return new String[0]; 
	} 
        
	if(valueTypeEvaluator.getValueType() == ValueType.INVALID) { 
	    return new String[0]; 
	} 
        
        int counter = 0; 
        
	if(valueTypeEvaluator.getValueType() == ValueType.LIST) { 

	    java.util.List list = (java.util.List)value; 
            counter = list.size(); 
            values = new String[counter]; 
            
	    Iterator valueIterator = ((java.util.List)value).iterator();
	    String valueString = null; 

	    while(valueIterator.hasNext()) {
		valueString = ConversionUtilities.convertValueToString
			(this, valueIterator.next());
		values[counter] = valueString; 
                counter++;
	    }
	}
        else if(valueTypeEvaluator.getValueType() == ValueType.ARRAY) {

	    counter = Array.getLength(value); 
            values = new String[counter];
	    Object valueObject = null;
	    String valueString = null; 
	    
	    for(int i = 0; i < counter; ++i) { 
		valueObject = Array.get(value,i); 
		valueString = 
		    ConversionUtilities.convertValueToString
		    (this, valueObject); 
		values[i] = valueString; 
	    } 
	} 
        else if(valueTypeEvaluator.getValueType() == ValueType.OBJECT) {
	    
            values = new String[1]; 
            values[0] = ConversionUtilities.convertValueToString(this, value); 
	} 
        
        return values; 
    } 
    
    private UIComponent createLabel(String labelString) { 
        
      	if(DEBUG) log("createLabel()"); 

	// If we find a label, define a component and add it to the
	// children, unless it has been added in a previous cycle
	// (the component is being redisplayed). 

	if(labelString == null || labelString.length() < 1) { 
             if(DEBUG) log("\tNo label");
             return null; 
	} 
        else if(DEBUG) { 
            log("\tLabel is " + labelString);  //NOI18N
        }

        Label label = new Label(); 
        label.setId(getId().concat(LABEL_ID));
	label.setLabelLevel(getLabelLevel()); 
	label.setText(labelString); 
	label.setLabeledComponent(this); 
        if(DEBUG) log("Id of component is " + label.getId());
        // <RAVE>
        // this.getFacets().put(LABEL_FACET, label);
        if (!Beans.isDesignTime())
            this.getFacets().put(LABEL_FACET, label);
        // </RAVE>
	return label;
    } 
    
     private UIComponent createText(String string) { 

	if(DEBUG) log("createText()"); 
        
        // <RAVE>
	// define a component, bring it up to date, and add it as a facet
        StaticText text = new StaticText(); 
        modifyReadOnlyTextComponent(text, string);
        
        // this.getFacets().put(READONLY_FACET, text);
        if (!Beans.isDesignTime()){
            this.getFacets().put(READONLY_FACET, text);
        }
        // </RAVE>
	return text;
    } 
     
    //<RAVE>     
    //modify readOnlyComponent with up-to-date data
    private void modifyReadOnlyTextComponent(UIComponent text, String readOnlyValue) {
        if (readOnlyValue == null) {
            readOnlyValue = new String();
        }
        if (text instanceof StaticText) {
            ((StaticText)text).setText(readOnlyValue);
        }
        else if (text instanceof ValueHolder) {
            ((ValueHolder)text).setValue(readOnlyValue);
        }
        text.setId(getId().concat(READONLY_ID));
    }
    //</RAVE> 
     
    public String getPrimaryElementID(FacesContext context)  {
	if(getFacet(LABEL_FACET) == null) { 
	    return getClientId(context); 
	} 
	return this.getClientId(context).concat(LIST_ID);
    }

    // remove me when the interface method goes.
    /**
     * Return a string suitable for displaying the value in read only mode.
     * The default is to separate the list values with a comma.
     *
     * @param context The FacesContext
     * @throws javax.faces.FacesException If the list items cannot be processed
     */
    public String getValueAsReadOnly(FacesContext context, String separator) {
        return "FIX ME!";
    }

    public boolean mainListSubmits() {
        return true;
    }
}
