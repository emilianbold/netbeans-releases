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
import java.io.Serializable;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeMap;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.MethodNotFoundException;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
//import javax.faces.validator.ValidatorException;
import com.sun.rave.web.ui.component.Button;
import com.sun.rave.web.ui.component.TextField;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.StaticText;
import com.sun.rave.web.ui.model.list.ListItem;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.util.MessageUtil;
import com.sun.rave.web.ui.util.ThemeUtilities;
import com.sun.rave.web.ui.util.ConversionUtilities;
import com.sun.rave.web.ui.validator.StringLengthValidator;



/**
 * <h4>About this tag.</h4>
 *
 *    <p>This tag renders an EditableList component. Use this component
 *    when web application users need to create and modify a list of
 *    strings. The application user can add new strings by typing them
 *    into the textfield and clicking the "Add" button, and remove them
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
 *     <li><code>addButton</code>: use this facet to specify a custom
 *    component for the add button.</li>
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
public class EditableList extends EditableListBase implements ListManager {
    
    /**
     * The component id for the ADD button
     */
    public static final String ADD_BUTTON_ID = "_addButton"; //NOI18N
    public static final String ADD_BUTTON_FACET = "addButton"; //NOI18N
    /**
     * The component ID for the remove button
     */
    public static final String REMOVE_BUTTON_ID = "_removeButton"; //NOI18N
    public static final String REMOVE_BUTTON_FACET = "removeButton"; //NOI18N
    /**
     * The component ID for the textfield
     */
    public static final String FIELD_ID = "_field"; //NOI18N
    public static final String FIELD_FACET = "field"; //NOI18N
    
    /**
     * The component ID for the textfield
     */
    public static final String LIST_LABEL_ID = "_listLabel"; //NOI18N
    public static final String LIST_LABEL_FACET = "listLabel"; //NOI18N
    
    /**
     * The component ID for the textfield
     */
    public static final String FIELD_LABEL_ID = "_fieldLabel"; //NOI18N
    public static final String FIELD_LABEL_FACET = "fieldLabel"; //NOI18N
    
    /**
     * The component ID for the textfield
     */
    public static final String READ_ONLY_ID = "_readOnly"; //NOI18N
    public static final String READ_ONLY_FACET = "readOnly"; //NOI18N
    
    /**
     * Facet name for the header facet
     */
    public static final String HEADER_FACET = "header"; //NOI18N
    
    /**
     * Facet name for the footer facet
     */
    public static final String FOOTER_FACET = "footer"; //NOI18N
    
    /**
     * The name of the component attribute that stores the name of the JavaScript object that organizes the JavsSCript functions for the component
     */
    public static final String JSOBJECT = "com.sun.rave.web.ui.EditabelListJS";
    
    /**
     * Name of the JavaScript function which is responsible for adding elements from the availble list to the selected list
     */
    public static final String ADD_FUNCTION = ".add(); ";
    
    /**
     * Name of the JavaScript function which is responsible for
     * enabling/disabling the add button
     */
    public static final String ENABLE_ADD_FUNCTION = ".enableAdd(); ";
    
    /**
     * Name of the JavaScript function which is responsible for
     * enabling/disabling the add button
     */
    public static final String SET_ADD_DISABLED_FUNCTION = ".setAddDisabled(false);";
    
    /**
     * Name of the JavaScript function which is responsible for
     * enabling/disabling the remove button
     */
    public static final String ENABLE_REMOVE_FUNCTION = ".enableRemove(); ";
    
    /**
     * Name of the JavaScript function that updates the buttons
     */
    public static final String UPDATE_BUTTONS_FUNCTION = ".updateButtons(); ";
    /**
     *
     * /**
     * Read only separator string
     */
    private static final String READ_ONLY_SEPARATOR = ", "; //NOI18N
    
    /**
     * Facet name for the search facet
     */
    public static final String SEARCH_FACET = "search"; //NOI18N
    public static final String SPACER_STRING = "_"; //NOI18N
    
    private static final int MIN_LENGTH = 20;
    
    private static final boolean DEBUG = false;
    
    private TreeMap listItems = null;
    private Collator collator = null;
    private transient Theme theme = null;
    private String selectedValue = null;
    private String[] valuesToRemove = null;
    
    /**
     * Get the maximum length of the strings on the list
     * @return An integer value for the maximum number of characters on the list
     */
    public int getMaxlLength() {
        int length = super.getMaxLength();
        if(length < 1) {
            length = 25;
            super.setMaxLength(length);
        }
        return length;
    }
    
    // Buttons
    /**
     * Get or create the ADD button. Retrieves the component specified by the
     * addButton facet (if there is one) or creates a new Button component.
     * @return A UI Component for the Add button
     */
    public UIComponent getAddButtonComponent() {
        
        if(DEBUG) log("getAddButtonComponent()");
        
        String id = getId();
        
        // Check if the page author has defined an addbutton facet
        UIComponent buttonComponent = getFacet(ADD_BUTTON_FACET);
        // If the page author has not defined a button facet,
        // check if the page author specified a button.
        if(buttonComponent == null) {
            
            buttonComponent =
                    createButton(getTheme().getMessage("EditableList.add"), //NOI18N
                    ADD_BUTTON_ID, ADD_BUTTON_FACET,
                    new AddListener());
            ((Button)buttonComponent).setPrimary(true);
        } else if(DEBUG) {
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
     * @return A UI Component for the REMOVE button
     */
    public UIComponent getRemoveButtonComponent() {
        
        if(DEBUG) log("getRemoveButtonComponent()");
        
        String id = getId();
        
        // Check if the page author has defined an addbutton facet
        UIComponent buttonComponent = getFacet(REMOVE_BUTTON_FACET);
        
        // If the page author has not defined a button facet,
        // check if the page author specified a button.
        if(buttonComponent == null) {
            
            buttonComponent =
                    createButton(getTheme().getMessage("EditableList.remove"), //NOI18N
                    REMOVE_BUTTON_ID, REMOVE_BUTTON_FACET,
                    new RemoveListener());
        } else if(DEBUG) {
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
    public UIComponent getListLabelComponent() {
        
        if(DEBUG) log("getListLabelComponent()");
        
        String id = getId();
        
        // Check if the page author has defined a label facet
        UIComponent labelComponent = getFacet(LIST_LABEL_FACET);
        
        // If the page author has not defined a label facet,
        // check if the page author specified a label.
        if(labelComponent == null) {
            
            String labelString = getListLabel();
            if(labelString == null || labelString.length() == 0) {
                labelString =
                        getTheme().getMessage("EditableList.defaultListLabel"); //NOI18N
            }
            
            labelComponent = createLabel(labelString,
                    LIST_LABEL_ID,
                    LIST_LABEL_FACET);
            
            ((Label)labelComponent).setLabeledComponent(this);
        } else if(DEBUG) {
            log("\tFound facet."); //NOI18N
        }
        
        return labelComponent;
    }
    
    /**
     * Gets or creates a component for the textfield label. Retrieves the
     * fieldLabel facet if one was specified, or creates a new label component.
     * @return A UIComponent for the field label
     */
    public UIComponent getFieldLabelComponent() {
        
        if(DEBUG) log("getFieldLabelComponent()");
        
        String id = getId();
        
        // Check if the page author has defined a label facet
        UIComponent labelComponent = getFacet(FIELD_LABEL_FACET);
        // If the page author has not defined a label facet,
        // check if the page author specified a label.
        if(labelComponent == null) {
            
            String labelString = getFieldLabel();
            if(labelString == null || labelString.length() == 0) {
                labelString =
                        getTheme().getMessage("EditableList.defaultFieldLabel"); //NOI18N
            }
            
            labelComponent = createLabel(labelString,
                    FIELD_LABEL_ID,
                    FIELD_LABEL_FACET);
            
            ((Label)labelComponent).setLabeledComponent(getFieldComponent());
        } else if(DEBUG) {
            log("\tFound facet.");//NOI18N
        }
        
        return labelComponent;
    }
    
    
    // Other
    /**
     * Gets or creates a component for the textfield. Retrieves the
     * field facet if one was specified, or creates a new TextField component.
     * @return A UIComponent for the textfield
     */
    public UIComponent getFieldComponent() {
        
        if(DEBUG) log("getFieldComponent()");
        
        String id = getId();
        
        // Check if the page author has defined a label facet
        UIComponent fieldComponent = getFacet(FIELD_FACET);
        
        // If the page author has not defined a field facet,
        // check if the page author specified a field.
        if(fieldComponent == null) {
            
            fieldComponent = createField(FIELD_ID, FIELD_FACET);
        } else if(DEBUG) {
            log("\tFound facet.");//NOI18N
        }
        
        if(fieldComponent != null && isDisabled()) {
            fieldComponent.getAttributes().put("disabled", Boolean.TRUE);//NOI18N
        }
        return fieldComponent;
        
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
     * Creates a component for the EditableList in case the component
     * is read-only.
     * @return A UIComponent that displays the read-only value
     */
    public UIComponent getReadOnlyValueComponent() {
        
        if(DEBUG) log("getReadOnlyValueComponent()");
        
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
        } else if(DEBUG) {
            log("\tFound facet."); //NOI18N
        }
        return textComponent;
    }
    
    // The following methods overrides default behaviour that does not
    // make sense for this component
    /**
     *
     * @param converter
     */
    public void setConverter(javax.faces.convert.Converter converter) {
        String msg = getTheme().getMessage("EditableList.noConversion"); //NOI18N
        throw new RuntimeException(msg);
    }
    
    public String getJavaScriptObjectName() {
        Object o = this.getAttributes().get(JSOBJECT);
        String name = null; 
        if(o != null && o instanceof String) { 
            name = (String)o; 
        }
        else {
            FacesContext context = FacesContext.getCurrentInstance();
            name = this.getClientId(context).replace(':', '_');
            name = "EditableList_".concat(name);
            this.getAttributes().put(JSOBJECT, name);
        }
        return name;
    }

    private Theme getTheme() {
        return ThemeUtilities.getTheme(FacesContext.getCurrentInstance());
    }
    
    public String getOnChange() {
        StringBuffer onchangeBuffer = new StringBuffer(128);
        onchangeBuffer.append(getJavaScriptObjectName());
        onchangeBuffer.append(ENABLE_REMOVE_FUNCTION);
        return onchangeBuffer.toString();
    }
    
    public String getPrimaryElementID(FacesContext context) {
        
        // This is a little sketchy, because in this case we'd actually prefer
        // to return different values for focus and for the labelled component.
        // We should always label the list (that's the one that should have the
        // invalid icon if the list is empty, for example. But we should
        // probably also set the focus to the top input component which could
        // be either the field or the label. Ah well. I can get around this
        // if I implement some extra bits on the label.
        // TODO
        return getClientId(context).concat(ListSelector.LIST_ID);
    }
    
    
    /**
     * Getter for property valuesToRemove.
     * @return Value of property valuesToRemove.
     */
    public String[] getValuesToRemove() {
        if(valuesToRemove == null) {
            return new String[0];
        }
        return this.valuesToRemove;
    }
    
    /**
     * Setter for property valuesToRemove.
     * @param valuesToRemove New value of property valuesToRemove.
     */
    public void setValuesToRemove(String[] valuesToRemove) {
        
        this.valuesToRemove = valuesToRemove;
    }
    
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
        label.setId(getId().concat(id));
        label.setText(labelString);
        label.setLabelLevel(getLabelLevel());
        this.getFacets().put(facetName, label);
        return label;
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
        getFacets().put(facetName, field);
        return field;
    }
    
    private TextField createField(String id, String facetName) {
        
        if(DEBUG) log("createField()");
        
        String jsObjectName = getJavaScriptObjectName(); 
        StringBuffer onkeypressBuffer = new StringBuffer(128);
        onkeypressBuffer.append("if(event.keyCode == 13) { ");  //NOI18N      
        onkeypressBuffer.append(jsObjectName);
        onkeypressBuffer.append(ADD_FUNCTION);
        onkeypressBuffer.append("return false; } "); //NOI18N
        
        StringBuffer onfocusBuffer = new StringBuffer(128);
        onfocusBuffer.append(jsObjectName);
        onfocusBuffer.append(SET_ADD_DISABLED_FUNCTION);
        onfocusBuffer.append("return false;"); //NOI18N
        
        StringBuffer onfocuslostBuffer = new StringBuffer(128);
        onfocuslostBuffer.append(jsObjectName);
        onfocuslostBuffer.append(ENABLE_ADD_FUNCTION);
        onfocuslostBuffer.append("return false;"); //NOI18N
        
        TextField field = new TextField();
        field.setId(getId().concat(id));
        int columns = getMaxLength();
        if(columns < MIN_LENGTH) {
            columns = MIN_LENGTH;
        }
        
        field.setColumns(columns);
        field.setOnKeyPress(onkeypressBuffer.toString());
        field.setOnFocus(onfocusBuffer.toString());
        field.setOnBlur(onfocuslostBuffer.toString());
        field.setTrim(true);
        if(getFieldValidator() != null) {
            field.setValidator(getFieldValidator());
        }
        if(getTabIndex() > 0) {
            field.setTabIndex(getTabIndex());
        }
        
        Theme theme = getTheme();
        StringLengthValidator strl =
                new StringLengthValidator(getMaxLength(), 1);
        strl.setTooLongMessage(theme.getMessage("EditableList.itemTooLong"));
        strl.setTooShortMessage(theme.getMessage("EditableList.fieldEmpty"));
        field.addValidator(strl);
        getFacets().put(facetName, field);
        return field;
    }
    
    private Button createButton(String text, String id, String facetName,
            ActionListener actionListener) {
        
        if(DEBUG) log("createButton()");
        
        Button button = new Button();
        button.setId(getId().concat(id));
        button.setText(text);
        if(getTabIndex() > 0) {
            button.setTabIndex(getTabIndex());
        }
        button.setImmediate(true);
        button.addActionListener(actionListener);
        getFacets().put(facetName, button);
        return button;
    }
    
    /**
     * Retrieve an Iterator of ListSelector.ListItem, to be used by the
     * renderer.
     * @return an Iterator over {@link ListItem}.
     * @throws javax.faces.FacesException
     */
    public Iterator getListItems(FacesContext context, boolean rulerAtEnd) throws FacesException {
        
        if(DEBUG) log("getListItems()");
        
        Locale locale = context.getViewRoot().getLocale();
        if(DEBUG) log("\tLocale is " + locale.toString());
        collator =  Collator.getInstance(locale);
        listItems = new TreeMap(collator);
        
        // We have to make sure that the long empty list item (whose
        // purpose is to guarantee that the size of the list stays
        // constant) is alwasy at the bottom of the list.  (=has the
        // highest key in the map). We do this by identifying the
        // longest key in the map, as long as the collator is
        // concerned and appending something at the end. (It's not
        // possible to use a constant for this, since an o with an
        // umlaut comes after z in Swedish, but before it in German,
        // for example).
        String lastKey = ""; //NOI18N
        String[] currentValues = getCurrentValueAsStringArray();
        if(DEBUG) {
            log("\tValues are:");
            for(int i=0; i<currentValues.length; ++i) {
                log(currentValues[i]);
            }
        }
        
        // The string currently being evaluated
        String currentString;
        
        // Two cases:
        // First case: the page author requested a sorted map (by
        // character), in which case we sort by the strings
        // themselves. The last key is set to the string that the
        // collator deems to be the last.
        // Second case: the list is sorted by the order they were
        // added to the map. We deal with that by generating a
        // successively longer key for each entry (maps do not
        // conserve the order the items were added). The last key
        // is set to the last key generated.
        
        ListItem listItem = null;
        // If the page author does not want the list items to be
        // sorted (alphabetically by locale), then they're
        // supposed to be sorted by the order they were added.
        // Maps are not guaranteed to return items in the order
        // they were added, so we have to create this order
        // artificially. We do that by creating a successively
        // longer key for each element. (a, aa, aaa...).
        StringBuffer unsortedKeyBuffer = new StringBuffer("a"); //NOI18N
        for(int counter=0; counter<currentValues.length; ++ counter) {
            currentString = currentValues[counter];
            if(DEBUG) {
                log("Current string is " + currentString); //NOI18N
                log("SelectedValue is " + String.valueOf(selectedValue)); //NOI18N
            }
            
            if (currentString == null) {
                String msg = MessageUtil.getMessage
                        ("com.sun.rave.web.ui.resources.LogMessages",
                        "EditableList.badValue", new Object[]{ getClientId(context) });
                throw new FacesException(msg);
            }
            
            listItem = new ListItem(currentString);
            listItem.setValue(currentString);
            if(currentString.equals(selectedValue)) {
                if(DEBUG) log("Selected value");
                listItem.setSelected(true);
            }
            if(isSorted()) {
                if(collator.compare(currentString, lastKey) > 0) {
                    lastKey = currentString;
                }
                listItems.put(currentString, listItem);
            } else {
                listItems.put(unsortedKeyBuffer.toString(), listItem);
                unsortedKeyBuffer.append("a"); //NOI18N
            }
        }
        if(!isSorted()) {
            lastKey = unsortedKeyBuffer.toString();
        }
        
        
        // rulerAtEnd will be true if the invoker needs a blank
        // disabled list option at the end. Typically this is
        // needed by the renderer, to guarantee that the widget
        // stays the same in size when items are added and removed.
        if(rulerAtEnd) {
            
            int length = getMaxlLength();
            if(length < MIN_LENGTH) {
                length = MIN_LENGTH;
            }
            StringBuffer labelBuffer = new StringBuffer(length);
            
            for(int counter=0; counter < length; ++counter) {
                labelBuffer.append(SPACER_STRING); //NOI18N
            }
            ListItem item = new ListItem(labelBuffer.toString());
            item.setDisabled(true);
            listItems.put(lastKey.concat("a"), item); //NOI18N
        }
        
        return listItems.values().iterator();
    }
    
    private String[] getCurrentValueAsStringArray() {
        
        
        if(DEBUG) log("getCurrentValueAsStringArray()");
        Object value = getSubmittedValue();
        if(value == null) {
            if(DEBUG) log("\tUsing regular value");
            value = getValue();
        } else if(DEBUG) log("\tUsing submitted value");
        
        if(value == null) {
            return new String[0];
        }
        if(value instanceof String[]) {
            return (String[])value;
        }
        
        String msg = MessageUtil.getMessage
                ("com.sun.rave.web.ui.resources.LogMessages",
                "EditableList.badValue", 
                new Object[]{ getClientId(FacesContext.getCurrentInstance()) });
        throw new FacesException(msg);
    }
    
    private void log(String s) {
        System.out.println(this.getClass().getName() + "::" + s); //NOI18N
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
        return submittedValue;
    }
    
    /**
     * @exception NullPointerException
     */
    public void processValidators(FacesContext context) {
        
        if (context == null) {
            throw new NullPointerException();
        }
        
        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }
        
        // This component may be a developer defined facet.
        // It is explicitly validated during an Add action.
        // It must not be validated during a submit. The assumption
        // is that processValidators is being called during
        // a submit and not in an immediate context.
        // Compare the id of this component with the children
        // and facets and if it matches don't call its
        // processValidators method.
        //
        UIComponent field = getFieldComponent();
        String fieldId = field.getId();
        
        // Process all the facets and children of this component
        Iterator kids = getFacetsAndChildren();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            // We probably should ensure that fieldId is not
            // null, during getFieldComponent() even if
            // it is a developer defined facet.
            //
            if (fieldId != null && fieldId.equals(kid.getId())) {
                continue;
            }
            kid.processValidators(context);
        }
        
        // Validate the EditableList
        //
        checkValid(context);
    }
    
    public void processAddAction() {
        
        if(DEBUG) log("processAddAction()");
        
        // If we are rendering prematurely don't do anything
        //
        if (FacesContext.getCurrentInstance().getRenderResponse()) {
            return;
        }
        
        selectedValue = null;
        
        String[] values = getCurrentValueAsStringArray();
        
        Object value = getAddedObject();
        if(value == null) {
            return;
        }
        //TODO - fix this when implementing conversion for this component
        selectedValue = value.toString();
        
        int numValues = values.length;
        
        String[] newValues = new String[numValues + 1];
        int counter;
        for(counter=0; counter < numValues; ++counter) {
            newValues[counter] = values[counter];
            if(DEBUG) log("\tAdding " + newValues[counter]);
        }
        newValues[counter] = selectedValue;
        if(DEBUG) log("\tAdding " + newValues[counter]);
        setSubmittedValue(newValues);
    }
    
    public void processRemoveAction() {
        
        if(DEBUG) log("processRemoveAction()");
        
        // If we are rendering prematurely don't do anything
        //
        if (FacesContext.getCurrentInstance().getRenderResponse()) {
            return;
        }
        
        // Reset the selected value
        selectedValue = null;
        
        ArrayList items = new ArrayList();
        int counter;
        
        if(getValue() != null) {
            if(DEBUG) log("\tList was not empty");
            String[] strings = getCurrentValueAsStringArray();
            int length = strings.length;
            for(counter=0; counter<length; ++counter) {
                items.add(strings[counter]);
                if(DEBUG) log("Added " + strings[counter]);
            }
        }
        
        String[] valuesToRemove = getValuesToRemove();
        for(counter=0; counter < valuesToRemove.length; ++counter) {
            items.remove(valuesToRemove[counter]);
            if(DEBUG) log("remove " + valuesToRemove[counter]);
        }
        
        String[] newValues = new String[items.size()];
        for(counter=0; counter < items.size(); ++counter) {
            newValues[counter] = (String)(items.get(counter));
            if(DEBUG) log("\tAdding back " + newValues[counter]);
        }
        
        setValuesToRemove(null);
        setSubmittedValue(newValues);
    }
    
    private void checkValid(FacesContext context) {
        
        
        if(DEBUG) log("checkValid()");
        
        try {
            validate(context);
        } catch (RuntimeException e) {
            if(DEBUG) log("Error during validation");
            context.renderResponse();
            throw e;
        }
        
        if (!isValid()) {
            if(DEBUG) log("Component is not valid");
            context.renderResponse();
        }
    }
    
    private Object getAddedObject() {
        
        FacesContext context = FacesContext.getCurrentInstance();
        
        if(DEBUG) log("\tAdd a new item");
        
        // Need to get the field's value validated first
        // The field can't be immediate because we don't want
        // to validate it if the value is not going to be added.
        // For example in a real submit request.
        //
        EditableValueHolder field = (EditableValueHolder)getFieldComponent();
        
        // This is ok to do here.
        // We are currently after the APPLY_REQUEST_VALUES phase
        // and before the PROCESS_VALIDATIONS phase.
        // If the field were marked immediate, then the validation
        // would have occured before we get here. If not done
        // here it will be done in the next phase. But it needs
        // to be done here, sort a simulation of immediate
        // henavior. But we don't want the side effect of immediate
        // behavior from external immediate components.
        //
        ((UIComponent)field).processValidators(context);
        
        if (!field.isValid()) {
            return null;
        }
        // Get the value from the field.
        //
        Object value = field.getValue();
        
        // This is a policy of the EditableList.
        // An emptyString or null value cannot be added to the list.
        //
        if (value == null ||
                (value instanceof String && value.toString().length() == 0)) {
            
            field.setValid(false);
            context.renderResponse();
            
            if(DEBUG) log("No value from the field");
            
            String message = ThemeUtilities.getTheme(context).
                    getMessage("EditableList.fieldEmpty");
            context.addMessage(getClientId(context), new FacesMessage(message));
            return null;
        }
        // The new value was added so clear the value.
        // This set is questionable, if the field is a developer
        // defined facet. This will cause an update to the model
        // before the value change event.
        //
        if(DEBUG) log("\tFound new value: " + value);
        field.setValue(null);
        
        return value;
    }
    
    /* Don't need this. Only needed for debugging.
    public void setValue(Object value) {
     
        if(DEBUG) log("setValue()...");
        super.setValue(value);
        if(DEBUG) log("\tLocal value set: " +
                String.valueOf(isLocalValueSet()));
    }
     */
    
    /** Always returns false for EditableList **/
    public boolean isImmediate() {
        return false;
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
    
    public String[] getValueAsStringArray(FacesContext context) {
        
        if(DEBUG) log("getValueAsStringArray)");
        
        Iterator iterator = getListItems(context, false);
        int numItems = listItems.size();
        String[] values = new String[numItems];
        
        int counter = 0;
        while(counter < numItems) {
            values[counter] = ((ListItem)(iterator.next())).getValue();
            ++counter;
        }
        return values;
    }
    
    public boolean mainListSubmits() {
        return true;
    }
}

class AddListener implements ActionListener, Serializable {
    
    public void processAction(ActionEvent event) {
        
        UIComponent comp = event.getComponent();
        comp = comp.getParent();
        if(comp instanceof EditableList) {
            ((EditableList)comp).processAddAction();
        }
    }
}

class RemoveListener implements ActionListener, Serializable {
    
    public void processAction(ActionEvent event) {
        
        UIComponent comp = event.getComponent();
        comp = comp.getParent();
        if(comp instanceof EditableList) {
            ((EditableList)comp).processRemoveAction();
        }
    }
    
}
