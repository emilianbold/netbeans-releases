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
package com.sun.rave.web.ui.model;

/**
 * Use the OptionsTitle class to add a Title to a list or
 * array of Options. The label text will be rendered marked by dashes.
 * <p>
 * Use this class instead of OptionTitle to indicate that no items have
 * been selected from this list. This is important in cases where a list
 * component like a DropDown or Listbox, may be bound to a null initial
 * component value.
 * </p><p>
 * When these components are bound to a null value, especialy a DropDown
 * where a value will always be submitted, it is not possible to
 * detect without a special Option that no change has actually occurred
 * and a user didn't explicitly select an option. This can
 * result in an incorrect value change event and model update.
 * </p><p>
 * When an OptionTitle is the first option in an array or
 * list of Option's assigned to the "items" property of a
 * DropDown, OptionTitle's value will be submitted when the form
 * is submitted, if a different option is not chosen by the user. When the
 * submitted value is decoded, this value will be identified and will treat the
 * submission of the DropDown as if it was not submitted.
 * </p><p>
 * This prevents a false update in the case where a user had not made
 * any change at all and the initial value of the DropDown does not
 * match the submitted value, such is the case when the initial value
 * of the component is null.
 * </p><p>
 * OptionTitle is also useful for a Listbox where once an
 * an item is selected it cannot be deselected. Using OptionTitle
 * gives the user an opportunity to select it, thereby deselecting a
 * previous selection. On form submit the list will appear as if nothing
 * had been selected.
 * </p><p>
 * NOTE: Calling setValue on this class will not change its
 * value. The label can be set.
 * </p>
 */
public class OptionTitle extends Option {
    
    public static final String NONESELECTED = "com_sun_rave_web_ui_NONESELECTED";
  
    public OptionTitle(String title) { 
        super(NONESELECTED, title); //NOI18N
    } 

    /*
    public String getLabel() {

	// FIXME: The dash "look" should be part of the Theme
	// ideally as a format String with one parameter.
	// 
        String label = super.getLabel();
        label = "&#8212; ".concat(label).concat(" &#8212;"); 
        return label; 
    }
     */

    /**
     * The value will not be changed, if this method is called.
     */
    public void setValue(String value) {
    }
}
