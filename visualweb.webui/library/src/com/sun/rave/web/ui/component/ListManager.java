/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.rave.web.ui.component;

import java.util.Iterator;
import javax.faces.context.FacesContext;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;

/**
 * This interface is used to allow both list components which
 * allow the user to select from a set of Options (e.g. Listbox,
 * AddRemove) and list components which allow the user to edit
 * a list to use the same renderer.
 *
 * TODO: consider making this a base class instead. There
 * is code which is shared between Selectors and Editable List,
 * Orderable List. (getConvertedValue, getValueAsString, ...)
 *
 * @author avk
 */
public interface ListManager extends EditableValueHolder,
                                     SelectorManager,
                                     ComplexComponent {

    /**
    * Get an Iterator of the items to display. The items are of 
    * type <code>com.sun.rave.web.ui.model.list.ListItem</code> and
    * are an abstraction over different types of actual data 
    * to be used by the renderer
    * @param rulerAtEnd If this attribute is set to true, the iterator will contain, as the last item, a disabled list option with a blank label whose sole function is to guarantee that the list stays the same size
    * @return An Iterator of  <code>com.sun.rave.web.ui.model.list.ListItem</code>
    */
   public Iterator getListItems(FacesContext context, boolean rulerAtEnd);     

    /**
     * Retrieves the tooltip for the list
     * @return A string with the text for the tool tip
     */
    public String getToolTip();
    
     /**
     * Get the value of the component as a String array. The array consists
     * of the converted value of each list item is shown.
     * @param context The FacesContext of the request
     * @return A string representation of the value
     */
    public String[] getValueAsStringArray(FacesContext context); 
    /**
     * Get the number of rows to display (the size of the HTML 
     * select element)
     * @return The size of the list
     */
    public int getRows();
   
    /**
     * Returns a UIComponent used to display the readonly value for this 
     * component
     * @return a UIComponent used to display the readonly value for this 
     * component
     */
    public UIComponent getReadOnlyValueComponent(); 

    public boolean isVisible();
    
    // return true if the select element associated with the component 
    // represents the value in the HTTP request. 
    public boolean mainListSubmits(); 
    
}
