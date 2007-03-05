//<!--
// Copyright 2004 by Sun Microsystems, Inc. All rights reserved.
// Use is subject to license terms.
//
// This Javascript code will provide methods for dynamic enabling and
// disabling of Common Console components.

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// public global variables
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

// global variables needed for using submit form so timeout will work properly
var public_formToSubmit = null;
var public_submissionComponentId = null;

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// Common functions
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/** 
 * Use this function remove any styleClass for an html tag
 *
 * @param element the dom html tag element
 * @param styleClass the name of the class to remove
 * @return true if successful; otherwise, false
 */
function common_stripStyleClass(element, styleClass) {
    // routine protection in javascript
    if (element == null || styleClass == null || element.className == null) {
        return false;
    }

    // break out style classes into an array  
    var classes = common_splitStyleClasses(element);
    if (classes == null) {
        return false;
    }
    
    // For each styleClass, check if it's hidden and remove otherwise write it 
    // back out to the class
    for (var i = 0; i < classes.length; i++) {
        if (classes[i] != null && classes[i] == styleClass) {
            classes.splice(i,1);  	
        }
    }
    element.className = classes.join(" ");
}

/** 
 * Use this function add any styleClass to an html tag
 *
 * @param element the dom html tag element
 * @param styleClass the name of the class to add
 * @return true if successful; otherwise, false
 */

function common_addStyleClass(element, styleClass) {
    // routine protection in javascript
    if (element == null || styleClass == null) {
        return false;
    }

    // handle easy case first
    if (element.className == null) {
        element.className = styleClass;
        return true;
    }

    // break out style classes into an array  
    var classes = common_splitStyleClasses(element);
    if (classes == null) {
        return false;
    }

    // For each styleClass, check if it's hidden and remove otherwise write it 
    // back out to the class
    for (var i = 0; i < classes.length; i++) {
        if (classes[i] != null && classes[i] == styleClass) {
           return true;
        }
    }
    element.className = element.className + " " + styleClass;
}

/** 
 * Use this function to get array of style classes
 *
 * @param element the dom html tag element
 * @return array of classes
 */
function common_splitStyleClasses(element) {
    if (element != null && element.className != null) {
        return element.className.split(" ");
    } else {
        return null;
    }
}

/** 
 * Use this function to check if an array has a style class
 *
 * @param styleArray of style classes to check
 * @param styleClass the styleClass to check
 * @return array of classes
 */
function common_checkStyleClasses(styleArray, styleClass) {
    if (styleArray == null || styleClass == null) {
        return false;
    }
    for (var i = 0; i < styleArray.length; i++) {
        if (styleArray[i] != null && styleArray[i] == styleClass) {
           return true;
        }
    }   
    return false;
}

/** 
 * Use this function to test if the specified element is visible (i.e., it does 
 * not use the "hidden" style class).
 *
 * @param elementId The element ID of the html tag 
 * @return true if visible; otherwise, false
 */
function common_isVisible(elementId) {
    if (elementId == null) {
        return false;
    }
    // Get element.
    var element = document.getElementById(elementId);
    return common_isVisibleElement(element);
}

function common_isVisibleElement(element) {
    if (element == null) {
	return false;
    }
    // Test for the hidden style class.
    var styleClasses = common_splitStyleClasses(element); 
    return !common_checkStyleClasses(styleClasses, "hidden"); 
}

/** 
 * Use this function to show or hide any html element in the page
 *
 * @param elementId The element ID of the html tag 
 * @param visible true to make the element visible, false to hide the element
 * @return true if successful; otherwise, false
 */
function common_setVisible(elementId, visible) {
    if (elementId == null || visible == null ) {
        return false;
    }
    // Get element.
    var element = document.getElementById(elementId);
    common_setVisibleElement(element, visible);

}

function common_setVisibleElement(element, visible) {
    if (element == null || visible == null) {
	return false;
    }

    if (visible)
        common_stripStyleClass(element, "hidden");
    else
        common_addStyleClass(element, "hidden");    
}


function common_insertHiddenField( elementId, elementValue, parentForm) {

    // We have to assume that there is only one element
    // with elementId. document.getElementById, returns
    // the first one it finds, which appears to be the 
    // first one created dynamically, if more than one 
    // element is created dynamically with the same id.
    //
    // appendChild just appends even if there is an element
    // with the same id that exists.
    //
    // The assumption is that there should only be 
    // one element in the document with such an id.
    //
    // If the elementId exists just modifiy its value
    // instead of creating and appending.
    //
    var element = document.getElementById(elementId);
    if (element != null) {
	element.value = elementValue;
	return;
    }

    var newElement = document.createElement('input');
    newElement.type = 'hidden';
    newElement.id = elementId;
    newElement.value = elementValue;
    newElement.name = elementId;
    parentForm.appendChild(newElement);
}

// global variables needed for using submit form so timeout will work properly
// public_formToSubmit 
// public_submissionComponentId

function common_submitForm() {
//"public_formToSubmit" is a literal (not virtual) form.
//"public_submissionComponentId" is a component id (not client id).
//the virtual form implementation uses _submissionComponentId
//to determine which virtual form (if any) was submitted.

  if (public_formToSubmit == null) {
      return false;
  }
  if (public_submissionComponentId != null && public_submissionComponentId.length > 0) {
     common_insertHiddenField('_submissionComponentId', public_submissionComponentId, public_formToSubmit);
  }
  public_formToSubmit.submit();
  return false;
}

function common_timeoutSubmitForm(form, submissionComponentId) {
    public_formToSubmit = form;
    public_submissionComponentId = submissionComponentId;
    setTimeout('common_submitForm()', 0);
}

function common_leaveSubmitterTrace(form, submissionComponentId) {
// This function only needs to be called in the onclick handler of 
// an ActionSource component that appears within a -standard- table.
// Under those circumstances, if this function is not called, then when the component is clicked,
// the virtual form implementation will have no way of knowing that a virtual form was submitted.

    if (form != null && submissionComponentId != null && submissionComponentId.length > 0) {
       common_insertHiddenField('_submissionComponentId', submissionComponentId, form);
    }
}


/**
* delete a previously created element by createSubmittableArray.
*/
function common_deleteSubmittableArray(name, parentForm) {
   try {
       	var submittableArray  = document.getElementById(name);
	if (submittableArray != null) {
	    parentForm.removeChild(submittableArray);
	}
   } catch (e) {
   }
} 

/**
 * common_createSubmittableArray(string, string, array, array);
 *
 * This method creates a hidden "select" element with id 
 * and name attributes set name, values taken from the values
 * array argument, and display labels from the labels array.
 * It adds the element to the parentForm argument.
 * 
 * The pupose of this method is to create an array of values
 * that can be decoded using "name" as the key from a FacesContext
 * external context's "getRequestParameterValuesMap" as an
 * array of Strings. This reduces the need of rendering hidden input
 * field and delimiting several strings so that a multiple selection
 * can be returned.
 * The labels array provides an additional piece of data
 * for use on the client, but it is not contained in the submit.
 * All values added to the select are selected so that the
 * values will be submitted.
 *
 * Returns the created select element.
 *
 * It relies on the "hidden" style class.
 * An attempt is made to remove a possibly previously created element
 * by this name. It always deletes an element of name from parentForm.
 */
function common_createSubmittableArray(name, parentForm, labels, values) {

    common_deleteSubmittableArray(name, parentForm);

    if (values == null || values.length <= 0) {
	return;
    }

    var selections = document.createElement('select');

    selections.className = 'hidden';
    selections.name = name;
    selections.id = name;
    selections.multiple = true;

    // Start from the end of the array because
    // add puts things in at the head.
    //
    for (var i = 0; i < values.length; ++i) {
	var opt = document.createElement('option');
	opt.value = values[i];
	if (labels != null) {
	    opt.label = labels[i];
	}
	opt.defaultSelected = true;
	selections.add(opt, null);
    }
    parentForm.appendChild(selections);

    return selections;
}

/**
 * Replace occurences of delimiter with the escapeChar and the
 * delimiter.
 * For example replace "," with "/," if delimiter == "," and
 * escapeChar is "/".
 */
function common_escapeString(s, delimiter, escapeChar) {

    if (s == null) {
	return null;
    }
    if (delimiter == null) {
	return s;
    }
    if (escapeChar == null) {
	return null;
    }
    // Escape occurrences of delimiter with 
    // escapeChar and the delimiter.
    //
    // First escape the escape char.
    //
    var escape_escapeChar = escapeChar;
    if (escapeChar == "\\") {
	escape_escapeChar = escapeChar + escapeChar;
    }
    var rx = new RegExp(escape_escapeChar, "g");
    var s1 = s.replace(rx, escapeChar + escapeChar);

    rx = new RegExp(delimiter, "g");
    return s1.replace(rx, escapeChar + delimiter);
}

/**
 * Replace occurences of a sequence of 2 instances of delimiter 
 * with 1 instance of the delimiter.
 * For example replace ",," with "," if delimiter == ","
 */
function common_unescapeString(s, delimiter, escapeChar) {

    if (s == null) {
	return null;
    }
    if (delimiter == null) {
	return s;
    }
    if (escapeChar == null) {
	return null;
    }
    // UnEscape occurrences of delimiter with 
    // single instance of the delimiter
    //
    var escape_escapeChar = escapeChar;
    if (escapeChar == "\\") {
	escape_escapeChar = escapeChar + escapeChar;
    }

    // First unescape the escape char.
    //
    var rx = new RegExp(escape_escapeChar + escape_escapeChar, "g");
    var s1 = s.replace(rx, escapeChar);

    // Now replace escaped delimters
    //
    rx = new RegExp(escape_escapeChar + delimiter, "g");
    return s1.replace(rx, delimiter);
}

/**
 * Return an array of unescaped strings from escapedString
 * where the escaped character is delimiter.
 * If delimiter is "," escapedString might have the form
 *
 * XX\,XX,MM\,MM
 *
 * where "\" is the escape char.
 * 
 * and is returned as an array
 * array[0] == "XX,XX"
 * array[1] == "MM,MM"
 * 
 */
function common_unescapeStrings(escapedString, delimiter, escapeChar) {

    if (escapedString == null || escapedString == "") {
	return null;
    }
    if (delimiter == null || delimiter == "") {
	return escapedString;
    }
    if (escapeChar == null || escapeChar == "") {
       return null;
    }

    // Need to do this character by character.
    //
    var selections = new Array();
    var index = 0;
    var escseen = 0;
    var j = 0;
    for (var i = 0; i < escapedString.length; ++i) {
	if (escapedString.charAt(i) == delimiter) {
	    if (escseen % 2 == 0) {
		selections[index++] = escapedString.slice(j, i);
		j = i + 1;
	    }
	}
	if (escapedString.charAt(i) == escapeChar) {
	    ++escseen;
	    continue;
	} else {
	    escseen = 0;
	}
    }
    // Capture the last split.
    //
    selections[index] = escapedString.slice(j);

    // Now unescape each selection
    //
    var unescapedArray = new Array();

    // Now replace escaped delimiters
    // i.e.  "\," with ","
    //
    for (i = 0; i < selections.length; ++i) {
	unescapedArray[i] = common_unescapeString(selections[i],
		delimiter, escapeChar);
    }
    return unescapedArray;
}


// ---------------------------------------------------------------------
//
//  BODY 
//
// ---------------------------------------------------------------------

function Body(focusComponentId)  { 

    this.focusID = focusComponentId;
    this.setInitialFocus = body_setInitialFocus; 
    this.setScrollPosition = body_setScrollPosition;
    return this; 
} 

// This method is invoked from the onload event handler of the body 
function body_setInitialFocus() {

    if(this.focusID == null) { 
        return true;
    } 
    var focusElement = document.getElementById(this.focusID);
    if(focusElement != null && focusElement.tagName != "SPAN") { 
        focusElement.focus();
    }

    if (window.location.href.indexOf('#') != -1) {
	// # char found, anchor being used. forego scrolling.
	// CR 6342635. 
	sjwuic_ScrollCookie = null;
    }

    if(sjwuic_ScrollCookie != null) { 
        sjwuic_ScrollCookie.restore(); 
    }
    return true; 
}

// This method is invoked from the onunload event handler of the body 
function body_setScrollPosition() {
    if(sjwuic_ScrollCookie != null) { 
        sjwuic_ScrollCookie.set(); 
    }
    return true; 
}


// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// hyperlink functions
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


function hyperlink_submit(hyperlink, formId, params) {
//params are name value pairs but all one big string array
//so params[0] and params[1] form the name and value of the first param

    var theForm = document.getElementById(formId);

    if (params != null) {
        for (var i=0; i<params.length; i++) {
            common_insertHiddenField(params[i], params[i+1], theForm);
            i++;
        }
    }
    common_insertHiddenField(hyperlink.id + "_submittedField", hyperlink.id, theForm);
    
    if (hyperlink.target != null) {
        theForm.target = hyperlink.target;
    }
    theForm.submit();
    return false;
}

/** 
 * Use this function to access the HTML img element that makes up
 * the icon hyperlink. 
 *
 * @param elementId The component id of the JSF component (this id is
 * assigned to the outter most tag enclosing the HTML img element).
 * @return a reference to the img element. 
 */
function hyperlink_getImgElement(elementId) {

    // Image hyperlink is now a naming container and 
    // and the img element id includes the ImageHyperlink parent id.
    //
    if (elementId != null) {
	var parentid = elementId;
	var colon_index = elementId.lastIndexOf(":");
	if (colon_index != -1) {
	    parentid = elementId.substring(colon_index + 1);
	}
	return document.getElementById(elementId + ":" + parentid + "_image");
    }
       
    return document.getElementById(elementId + "_image");
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// button functions
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/**
 * Assign CSS styles for button type.
 */
function StaticButtonStrings() {
    this.classNamePrimary               = "Btn1";
    this.classNamePrimaryDisabled       = "Btn1Dis";
    this.classNamePrimaryMini           = "Btn1Mni";
    this.classNamePrimaryMiniDisabled   = "Btn1MniDis";
    this.classNamePrimaryMiniHov        = "Btn1MniHov";
    this.classNamePrimaryHov            = "Btn1Hov";
    this.classNameSecondary             = "Btn2";
    this.classNameSecondaryDisabled     = "Btn2Dis";
    this.classNameSecondaryMini         = "Btn2Mni";
    this.classNameSecondaryMiniDisabled = "Btn2MniDis";
    this.classNameSecondaryMiniHov      = "Btn2MniHov";
    this.classNameSecondaryHov          = "Btn2Hov";
 
}

/**
 * Assign CSS styles for image type.
 */
function StaticImageButtonStrings() {
    this.classNamePrimary               = "Btn3";
    this.classNamePrimaryDisabled       = "Btn3Dis";
    this.classNamePrimaryMini           = "Btn3Mni";
    this.classNamePrimaryMiniDisabled   = "Btn3MniDis";
    this.classNamePrimaryMiniHov        = "Btn3MniHov";
    this.classNamePrimaryHov            = "Btn3Hov";
    this.classNameSecondary             = ""; //not used
    this.classNameSecondaryDisabled     = ""; //not used
    this.classNameSecondaryMini         = ""; //not used
    this.classNameSecondaryMiniDisabled = ""; //not used
    this.classNameSecondaryMiniHov      = ""; //not used
    this.classNameSecondaryHov          = ""; //not used
 
}

/**
 * Assign default CSS styles.
 */
var defaultButtonStrings = new StaticButtonStrings();
var defaultImgButtonStrings = new StaticImageButtonStrings();

/**
 * Assign button properties.
 */
function sjwuic_assign_button(buttonId, buttonclasses, secondary, mini, disabled) {
    if (buttonId == null) {
        return false;
    }
    var button = document.getElementById(buttonId);
    if (button == null) {
        return false;
    }
    button.isOneOfOurButtons = true;
    button.secondary = secondary; //boolean
    button.mini = mini; //boolean
    button.mydisabled = disabled; //boolean
    button.classes = buttonclasses;
    //take care of button functions
    button.isSecondary=sjwuic_button_isSecondary;
    button.setSecondary=sjwuic_button_setSecondary;
    button.isPrimary=sjwuic_button_isPrimary;
    button.setPrimary=sjwuic_button_setPrimary;
    button.isMini=sjwuic_button_isMini;
    button.setMini=sjwuic_button_setMini;
    button.getDisabled=sjwuic_button_getDisabled;
    button.setDisabled=sjwuic_button_setDisabled;
    button.getVisible=sjwuic_button_getVisible;
    button.setVisible=sjwuic_button_setVisible;
    button.getText=sjwuic_button_getText;
    button.setText=sjwuic_button_setText;
    button.doClick=sjwuic_button_click;
    button.myonblur=sjwuic_button_onblur;
    button.myonfocus=sjwuic_button_onfocus;
    button.myonmouseover=sjwuic_button_onmouseover;
    button.myonmouseout=sjwuic_button_onmouseout;

}

/** 
 * Simulate a mouse click in a button. 
 *
 * @return true if successful; otherwise, false
 */
function sjwuic_button_click() {
    this.click()
    return true;
}

/** 
 * Get the textual label of a button. 
 *
 * @return The element value or null
 */
function sjwuic_button_getText() {
    return this.value;
}

/**
 * Set the textual label of a button. 
 *
 * @param text The element value
 * @return true if successful; otherwise, false
 */
function sjwuic_button_setText(text) {
    if (text==null) {
        return false;
    }

    this.value = text;
    return true;
}

/** 
 * Use this function to show or hide a button. 
 *
 * @param show true to show the element, false to hide the element
 * @return true if successful; otherwise, false
 */
function sjwuic_button_setVisible(show) {
    if (show == null) {
        return false;
    }
    // Get element.
    common_setVisibleElement(this, show);

    return true;
}

/** 
 * Use this function to find whether or not this is visible according to our spec
 *
 * @return true if visible; otherwise, false
 */
function sjwuic_button_getVisible() {
     // Get element.
    styles = common_splitStyleClasses(this);
    if (styles == null) {
        return true;
    }
    return !common_checkStyleClasses(styles, "hidden");
}

/**
 * Test if button is set as "primary".
 *
 * @return true if primary; otherwise, false for secondary
 */
function sjwuic_button_isPrimary() {
    return !this.isSecondary();
}

/**
 * Set button as "primary".
 *
 * @param primary true for primary, false for secondary
 * @return true if successful; otherwise, false
 */
function sjwuic_button_setPrimary(primary) {
    return this.setSecondary(!primary);
}

/**
 * Test if button is set as "secondary".
 *
 * @return true if secondary; otherwise, false for primary
 */
function sjwuic_button_isSecondary() {
    return this.secondary;
}

/**
 * Set button as "secondary".
 *
 * @param secondary true for secondary, false for primary
 * @return true if successful; otherwise, false
 */
function sjwuic_button_setSecondary(secondary) {
    if (secondary == null || this.mydisabled) {
        return false;
    }
    var stripType;
    var stripTypeHov;
    var newType;

    if (this.secondary == false && secondary == true) {
        //change from primary to secondary
        if (this.mini) {
            stripTypeHov = this.classes.classNamePrimaryMiniHov;
            stripType = this.classes.classNamePrimaryMini;
            newType = this.classes.classNameSecondaryMini;
        }
        else {
            stripTypeHov = this.classes.classNamePrimaryHov;
            stripType = this.classes.classNamePrimary;
            hovType = this.classes.classNameSecondaryHov;
            newType = this.classes.classNameSecondary;
        }
    } else if (this.secondary == true && secondary == false) {
        //change from secondary to primary
        if (this.mini) {
            //this is currently a mini button
            stripTypeHov = this.classes.classNameSecondaryMiniHov;
            stripType = this.classes.classNameSecondaryMini;
            newType = this.classes.classNamePrimaryMini;
        }
        else {
            stripTypeHov = this.classes.classNameSecondaryHov;
            stripType = this.classes.classNameSecondary;
            newType = this.classes.classNamePrimary;
        }
    } else {
        // don't need to do anything
        return false;
    }
   
    common_stripStyleClass(this, stripTypeHov);
    common_stripStyleClass(this, stripType);
    common_addStyleClass(this, newType);
    this.secondary=secondary;
    return this.secondary;
}

/**
 * Test if button is set as "mini".
 *
 * @return true if mini; otherwise, false
 */
function sjwuic_button_isMini() {
    return this.mini;
}

/**
 * Set button as "mini".
 *
 * @param mini true for mini, false for standard button
 * @return true if successful; otherwise, false
 */
function sjwuic_button_setMini(mini) {
    if (mini == null || this.mydisabled) {
        return false;
    }
    var stripType;
    var stripTypeHov;
    var newType;
    if (this.mini == true && mini == false) {
        //change from mini to nonmini
        if (!this.secondary) {
            //this is currently a primary button
            stripTypeHov = this.classes.classNamePrimaryMiniHov;
            stripType = this.classes.classNamePrimaryMini;
            newType = this.classes.classNamePrimary;
        }
        else {
            stripTypeHov = this.classes.classNameSecondaryMiniHov;
            stripType = this.classes.classNameSecondaryMini;
            newType = this.classes.classNameSecondary;
        }
    } else if (this.mini == false && mini == true) {
        if (!this.secondary) {
            //this is currently a primary button
            stripTypeHov = this.classes.classNamePrimaryHov;
            stripType = this.classes.classNamePrimary;
            newType = this.classes.classNamePrimaryMini;
        }
        else {
            stripTypeHov = this.classes.classNameSecondaryHov;
            stripType = this.classes.classNameSecondary;
            newType = this.classes.classNameSecondaryMini;
        }
    } else {
        // don't need to do anything
        return false;
    }
    
    common_stripStyleClass(this, stripTypeHov);
    common_stripStyleClass(this, stripType);
    common_addStyleClass(this, newType);
    this.mini=mini;
    return this.mini;
}

/**
 * Test disabled state of button.
 *
 * @return true if disabled; otherwise, false
 */
function sjwuic_button_getDisabled() {
    return this.mydisabled;
}

/**
 * Test disabled state of button.
 *
 * @param disabled true if disabled; otherwise, false
 * @return true if successful; otherwise, false
 */
function sjwuic_button_setDisabled(disabled) {
    if (disabled == null || this.mydisabled == disabled) {
        return false;
    }
    var stripType;
    var stripHovType;
    var newType;
    if (!this.secondary) {
        //this is currently a primary button
        if (this.mini) {
            if (disabled == false) {
                stripType = this.classes.classNamePrimaryMiniDisabled;
                stripHovType = this.classes.classNamePrimaryMiniDisabled;
                newType = this.classes.classNamePrimaryMini;
            }
            else {
                stripType = this.classes.classNamePrimaryMini;
                stripHovType = this.classes.classNamePrimaryMiniHov;
                newType = this.classes.classNamePrimaryMiniDisabled;
            }
        } else { // not mini
            if (disabled == false) {
                stripType = this.classes.classNamePrimaryDisabled;
                stripHovType = this.classes.classNamePrimaryDisabled;
                newType = this.classes.classNamePrimary;
            }
            else {
                stripType = this.classes.classNamePrimary;
                stripHovType = this.classes.classNamePrimaryHov;
                newType = this.classes.classNamePrimaryDisabled;
            }
        }
    }
    else {
        //this is currently a secondary button
        if (this.mini) {
            if (disabled == false) {
                stripType = this.classes.classNameSecondaryMiniDisabled;
                stripHovType = this.classes.classNameSecondaryMiniDisabled;
                newType = this.classes.classNameSecondaryMini;
            }
            else {
                stripType = this.classes.classNameSecondaryMini;
                stripHovType = this.classes.classNameSecondaryMiniHov;
                newType = this.classes.classNameSecondaryMiniDisabled;
            }
        } else { // not mini
            if (disabled == false) {
                stripType = this.classes.classNameSecondaryDisabled;
                stripHovType = this.classes.classNameSecondaryDisabled;
                newType = this.classes.classNameSecondary;
             }
            else {
                stripType = this.classes.classNameSecondary;
                stripHovType = this.classes.classNameSecondaryHov;
                newType = this.classes.classNameSecondaryDisabled;
            }
        }
    } 

    common_stripStyleClass(this, stripHovType);
    common_stripStyleClass(this, stripType);
    common_addStyleClass(this, newType);
    this.mydisabled=disabled;
    this.disabled=disabled;
    return true;
}

/**
 * Set CSS styles for onblur event.
 *
 * @return true if successful; otherwise, false
 */
function sjwuic_button_onblur() {
     if (this.mydisabled == true) {
        return true;
    }
    var stripType;
    var newType;
    if (!this.secondary) {
        if (this.mini) {
            stripType = this.classes.classNamePrimaryMiniHov;
            newType = this.classes.classNamePrimaryMini;        
        } else {
            stripType = this.classes.classNamePrimaryHov;
            newType = this.classes.classNamePrimary;        
        }
    } else { //is secondary 
        if (this.mini) {
            stripType = this.classes.classNameSecondaryMiniHov;
            newType = this.classes.classNameSecondaryMini;        
        } else {
            stripType = this.classes.classNameSecondaryHov;
            newType = this.classes.classNameSecondary;        
        }
    } 

    common_stripStyleClass(this, stripType);
    common_addStyleClass(this, newType);
    return true;
}

/**
 * Set CSS styles for onmouseout event.
 *
 * @return true if successful; otherwise, false
 */
function sjwuic_button_onmouseout() {
    if (this.mydisabled == true) {
        return true;
    }

    var stripType;
    var newType;
    if (!this.secondary) {
        if (this.mini) {
            stripType = this.classes.classNamePrimaryMiniHov;
            newType = this.classes.classNamePrimaryMini;        
        } else {
            stripType = this.classes.classNamePrimaryHov;
            newType = this.classes.classNamePrimary;        
        }
    } else { //is secondary 
        if (this.mini) {
            stripType = this.classes.classNameSecondaryMiniHov;
            newType = this.classes.classNameSecondaryMini;        
        } else {
            stripType = this.classes.classNameSecondaryHov;
            newType = this.classes.classNameSecondary;        
        }
    }
    common_stripStyleClass(this, stripType);
    common_addStyleClass(this, newType);
    return true;
}

/**
 * Set CSS styles for onfocus event.
 *
 * @return true if successful; otherwise, false
 */
function sjwuic_button_onfocus() {
    if (this.mydisabled == true) {
        return true;
    }
    var stripType;
    var newType;
    if (!this.secondary) {
        if (this.mini) {
            stripType = this.classes.classNamePrimaryMini;
            newType = this.classes.classNamePrimaryMiniHov;        
        } else {
            stripType = this.classes.classNamePrimary;
            newType = this.classes.classNamePrimaryHov;        
        }
    } else { //is secondary 
        if (this.mini) {
            stripType = this.classes.classNameSecondaryMini;
            newType = this.classes.classNameSecondaryMiniHov;        
        } else {
            stripType = this.classes.classNameSecondary;
            newType = this.classes.classNameSecondaryHov;        
        }
    }
    common_stripStyleClass(this, stripType);
    common_addStyleClass(this, newType);
    return true;
}

/**
 * Set CSS styles for onmouseover event.
 *
 * @return true if successful; otherwise, false
 */
function sjwuic_button_onmouseover() {
    if (this.mydisabled == true) {
        return false;
    }
    var stripType;
    var newType;
    if (!this.secondary) {
        if (this.mini) {
            stripType = this.classes.classNamePrimaryMini;
            newType = this.classes.classNamePrimaryMiniHov;        
        } else {
 
            stripType = this.classes.classNamePrimary;
            newType = this.classes.classNamePrimaryHov;        
        }
    } else { //is secondary 
        if (this.mini) {
            stripType = this.classes.classNameSecondaryMini;
            newType = this.classes.classNameSecondaryMiniHov;        
        } else {
            stripType = this.classes.classNameSecondary;
            newType = this.classes.classNameSecondaryHov;        
        }
    }

    common_stripStyleClass(this, stripType);
    common_addStyleClass(this, newType);
    return true;
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// listbox functions
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/** 
 * Use this function to access the HTML select element that makes up
 * the list. 
 *
 * @param elementId The component id of the JSF component (this id is
 * assigned to the span tag enclosing the HTML elements that make up
 * the list).
 * @return a reference to the select element. 
 */

function listbox_getSelectElement(elementId) { 

    var element = document.getElementById(elementId); 
    if(element != null) { 
        if(element.tagName == "SELECT") { 
            return element; 
	} 
    } 
    return document.getElementById(elementId + "_list");
}

/** 
 * This function is invoked by the list onselect action to set the selected, 
 * and disabled styles.
 *
 * Page authors should invoke this function if they set the selection
 * using JavaScript.
 *
 * @param elementId The component id of the JSF component (this id is
 * rendered in the div tag enclosing the HTML elements that make up
 * the list).
 * @return true if successful; otherwise, false
 */
function listbox_changed(elementId) { 

    var cntr = 0; 
    var listItem = listbox_getSelectElement(elementId).options;  
    while(cntr < listItem.length) { 

	if(listItem[cntr].selected) {
	    listItem[cntr].className = "MnuStdOptSel"; 
         }
        else if(listItem[cntr].disabled) {
	    listItem[cntr].className = "MnuStdOptDis"; 
        }
	else {
	    // This does not work on Opera 7. There is a bug such that if 
	    // you touch the option at all (even if I explicitly set
	    // selected to false!), it goes back to the original
	    // selection. 
	    listItem[cntr].className = "MnuStdOpt"; 
	}
	++ cntr;
    }
    return true;
}

/** 
 * Invoke this JavaScript function to set the enabled/disabled state
 * of the listbox component. In addition to disabling the list, it
 * also changes the styles used when rendering the component. 
 *
 * Page authors should invoke this function if they dynamically
 * enable or disable a list using JavaScript.
 * 
 * @param elementId The component id of the JSF component (this id is
 * rendered in the div tag enclosing the HTML elements that make up
 * the list).
 * @param disabled true or false
 * @return true if successful; otherwise, false
 */
function listbox_setDisabled(elementId, disabled) {

    var listbox = listbox_getSelectElement(elementId); 
    var regular = "Lst"; 
    var _disabled = "LstDis"; 

    if(listbox.className.indexOf("Mno") > 1) {
        regular = "LstMno"; 
        _disabled = "LstMnoDis"; 
    }
    if(disabled) {
	listbox.disabled = true;
	listbox.className = _disabled;
    } else { 
	listbox.disabled = false;
	listbox.className = regular; 
    }
    return true;
}

/** 
 * Invoke this JavaScript function to get the value of the first
 * selected option on the listbox. If no option is selected, this
 * function returns null. 
 * 
 * @param elementId The component id of the JSF component (this id is
 * rendered in the div tag enclosing the HTML elements that make up
 * the list).
 * @return The value of the selected option, or null if none is
 * selected. 
 */
function listbox_getSelectedValue(elementId) { 

    var listbox = listbox_getSelectElement(elementId); 
    var index = listbox.selectedIndex; 
    if(index == -1) { 
	return null; 
    } 
    else { 
	return listbox.options[index].value; 
    } 
} 

/** 
 * Invoke this JavaScript function to get the label of the first
 * selected option on the listbox. If no option is selected, this
 * function returns null. 
 * 
 * @param elementId The component id of the JSF component (this id is
 * rendered in the div tag enclosing the HTML elements that make up
 * the list).
 * @return The label of the selected option, or null if none is
 * selected. 
 */
function listbox_getSelectedLabel(elementId) { 

    var listbox = listbox_getSelectElement(elementId); 
    var index = listbox.selectedIndex; 
    if(index == -1) { 
	return null; 
    } 
    else { 
	return listbox.options[index].label; 
    } 
} 



// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// dropdown functions
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/** 
 * Use this function to access the HTML select element that makes up
 * the dropDown.
 *
 * @param elementId The component id of the JSF component (this id is
 * assigned to the span tag enclosing the HTML elements that make up
 * the dropDown).
 * @return a reference to the select element. 
 */

function dropDown_getSelectElement(elementId) { 
    var element = document.getElementById(elementId); 
    if(element != null) { 
        if(element.tagName == "SELECT") { 
            return element; 
	} 
    } 
    return document.getElementById(elementId + "_list");
} 

/** 
 * This function is invoked by the choice onselect action to set the selected, 
 * and disabled styles.
 *
 * Page authors should invoke this function if they set the 
 * selection using JavaScript.
 *
 * @param elementId The component id of the JSF component (this id is
 * rendered in the div tag enclosing the HTML elements that make up
 * the list).
 * @return true if successful; otherwise, false
 */
function dropDown_changed(elementId) {         

    var listItem = listbox_getSelectElement(elementId).options;  
    for (var cntr=0; cntr < listItem.length; ++cntr) { 
	if (listItem[cntr].className == "MnuStdOptSep"
	        || listItem[cntr].className == "MnuStdOptGrp") {
	    continue;	
	} else if (listItem[cntr].disabled) {
	    // Regardless if the option is currently selected or not,
            // the disabled option style should be used when the option
            // is disabled. So, check for the disabled item first.
	    // See CR 6317842.
	    listItem[cntr].className = "MnuStdOptDis"; 
        } else if (listItem[cntr].selected) {
	    listItem[cntr].className = "MnuStdOptSel"; 
        } else {
	    // This does not work on Opera 7. There is a bug such that if 
	    // you touch the option at all (even if I explicitly set
	    // selected to false!), it goes back to the original
	    // selection. 
	    listItem[cntr].className = "MnuStdOpt"; 
	}
    }
    return true;
}

/** 
 * Set the disabled state for given dropdown element Id. If the disabled 
 * state is set to true, the element is shown with disabled styles.
 *
 * Page authors should invoke this function if they dynamically
 * enable or disable a dropdown using JavaScript.
 * 
 * @param elementId The component id of the JSF component (this id is
 * rendered in the div tag enclosing the HTML elements that make up
 * the list).
 * @param disabled true or false
 * @return true if successful; otherwise, false
 */    
function dropDown_setDisabled(elementId, disabled) { 

    var choice = dropDown_getSelectElement(elementId); 

    if(disabled) {
	choice.disabled = true;
	choice.className = "MnuStdDis";
    } else { 
	choice.disabled = false;
	choice.className = "MnuStd";
    }
    return true;
}

/** 
 * Invoke this JavaScript function to get the value of the first
 * selected option on the dropDown. If no option is selected, this
 * function returns null. 
 * 
 * @param elementId The component id of the JSF component (this id is
 * rendered in the div tag enclosing the HTML elements that make up
 * the list).
 * @return The value of the selected option, or null if none is
 * selected. 
 */
function dropDown_getSelectedValue(elementId) { 

    var dropDown = dropDown_getSelectElement(elementId); 
    var index = dropDown.selectedIndex; 
    if(index == -1) { 
	return null; 
    } 
    else { 
	return dropDown.options[index].value; 
    } 
} 

/** 
 * Invoke this JavaScript function to get the label of the first
 * selected option on the dropDown. If no option is selected, this
 * function returns null. 
 * 
 * @param elementId The component id of the JSF component (this id is
 * rendered in the div tag enclosing the HTML elements that make up
 * the list).
 * @return The label of the selected option, or null if none is
 * selected. 
 */
function dropDown_getSelectedLabel(elementId) { 

    var dropDown = dropDown_getSelectElement(elementId); 
    var index = dropDown.selectedIndex; 
    if(index == -1) { 
	return null; 
    } 
    else { 
	return dropDown.options[index].label; 
    } 
} 

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// jumpdropdown functions
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/** 
 * This function is invoked by the jumpdropdown onchange action to set the form
 * action and then submit the form.
 *
 * Page authors should invoke this function if they set the selection using 
 * JavaScript.
 *
 * @param elementId The component id of the JSF component (this id is
 * rendered in the div tag enclosing the HTML elements that make up
 * the list).
 * @return true
 */
function jumpDropDown_changed(elementId) {
    
    var jumpDropdown = dropDown_getSelectElement(elementId); 
    var form = jumpDropdown; 
    while(form != null) { 
	form = form.parentNode; 
	if(form.tagName == "FORM") { 
	  break;
	} 
    }
    if(form != null) { 
      var submitterFieldId = elementId + "_submitter"; 
      document.getElementById(submitterFieldId).value = "true"; 

      var listItem = jumpDropdown.options;
      for (var cntr=0; cntr < listItem.length; ++cntr) { 
	  if (listItem[cntr].className == "MnuJmpOptSep"
		  || listItem[cntr].className == "MnuJmpOptGrp") {
	      continue;		
	  } else if (listItem[cntr].disabled) {
	      // Regardless if the option is currently selected or not,
              // the disabled option style should be used when the option
              // is disabled. So, check for the disabled item first.
	      // See CR 6317842.
	      listItem[cntr].className = "MnuJmpOptDis"; 
          } else if (listItem[cntr].selected) {
	      listItem[cntr].className = "MnuJmpOptSel"; 
	  } else { 
              listItem[cntr].className = "MnuJmpOpt";  
          } 
      }

      form.submit();
    } 
    else { 
      alert("The DropDown must be inside a form!"); 
    } 
    return true; 
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// checkbox functions
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/** 
 * Set the disabled state for the given checkbox element Id. If the disabled 
 * state is set to true, the element is shown with disabled styles.
 *
 * @param elementId The element Id
 * @param disabled true or false
 * @return true if successful; otherwise, false
 */
function checkbox_setDisabled(elementId, disabled) {    

    return rbcb_setDisabled(elementId, disabled, "checkbox", "Cb", "CbDis");
}




/** 
 * Set the disabled state for all the checkboxes in the check box
 * group identified by controlName. If disabled
 * is set to true, the check boxes are shown with disabled styles.
 *
 * @param controlName The checkbox group control name
 * @param disabled true or false
 * @return true if successful; otherwise, false
 */
function checkboxGroup_setDisabled(controlName, disabled) {    

    return rbcbGrp_setDisabled(controlName, disabled, "checkbox",
	"Cb", "CbDis");
}

/** 
 * Set the checked property for a checkbox with the given element Id.
 *
 * @param elementId The element Id
 * @param checked true or false
 * @return true if successful; otherwise, false
 */
function checkbox_setChecked(elementId, checked) {

    return rbcb_setChecked(elementId, checked, "checkbox");
}
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// radio button functions
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/** 
 * Set the disabled state for the given radiobutton element Id. If the disabled 
 * state is set to true, the element is shown with disabled styles.
 *
 * @param elementId The element Id
 * @param disabled true or false
 * @return true if successful; otherwise, false
 */
function radioButton_setDisabled(elementId, disabled) {    

    return rbcb_setDisabled(elementId, disabled, "radio", "Rb", "RbDis");
}

/** 
 * Set the disabled state for all the radio buttons in the radio button
 * group identified by controlName. If disabled
 * is set to true, the check boxes are displayed with disabled styles.
 *
 * @param controlName The radio button group control name
 * @param disabled true or false
 * @return true if successful; otherwise, false
 */
function radioButtonGroup_setDisabled(controlName, disabled) {    

    return rbcbGrp_setDisabled(controlName, disabled, "radio",
	"Rb", "RbDis");
}

/** 
 * Set the checked property for a radio button with the given element Id.
 *
 * @param elementId The element Id
 * @param checked true or false
 * @return true if successful; otherwise, false
 */
function radioButton_setChecked(elementId, checked) {

    return rbcb_setChecked(elementId, checked, "radio");
}

//========================================================
// Generic checkbox and radio button functions
//========================================================

/** 
 * Set the disabled state for all radio buttons with the given controlName.
 * If disabled is set to true, the element is shown with disabled styles.
 *
 * @param elementId The element Id
 * @param formName The name of the form containing the element
 * @param disabled true or false
 * @return true if successful; otherwise, false
 */
function rbcbGrp_setDisabled(controlName, disabled, type, 
	enabledStyle, disabledStyle) {

     // Validate params.
    if (controlName == null) {
        // alert("controlName parameter is null.");
        return false;
    }
    if (disabled == null) {
        alert("disabled parameter is null.");
        return false;
    }
    if (type == null) {
        alert("type parameter is null.");
        return false;
    }

    // Get radiobutton group elements.
    var x = document.getElementsByName(controlName)
 
    // Set disabled state.
    for (var i = 0; i < x.length; i++) {
        // Get element.
        var element = x[i];

        if (element == null || element.name != controlName)
            continue;

        // Validate element type.
        if (element.type.toLowerCase() != type) {
            alert("Invalid element type: " + element.type);
            return false;
        }

        // Set disabled state.
        element.disabled = (disabled.toLowerCase() == "true");

        // Set class attribute.
        if (element.disabled) {
	    if (disabledStyle != null) {
		element.className = disabledStyle;
	    }
        } else {
	    if (enabledStyle != null) {
		element.className = enabledStyle;
	    }
        }
    }

    return true;
}

function rbcb_setDisabled(elementId, disabled, type, enabledStyle,
	disabledStyle) {

    if (elementId == null || disabled == null || type == null) {
        // must supply an elementId && state && type
        return false;
    }

    var rbcb = document.getElementById(elementId);

    if (rbcb == null) {
        // specified elementId not found
        return false;
    }

    // wrong type
    //
    if (rbcb.type != type.toLowerCase()) {
	return false;
    }

    rbcb.disabled = (disabled.toLowerCase() == "true");

    if (rbcb.disabled) {
	if (disabledStyle != null) {
	    rbcb.className = disabledStyle;
	}
    } else {
	if (enabledStyle != null) {
	    rbcb.className = enabledStyle;
	}
    }


    return true;
}

function rbcb_setChecked(elementId, checked, type) {

    if (elementId == null || type == null) {
	return false;
    }
    var rbcb = document.getElementById(elementId);

    if (rbcb == null) {
        alert("rbcb_setChecked: element is null");
	return false;
    }

    // wrong type
    //
    if (rbcb.type != type.toLowerCase()) {
	return false;
    }

    // Get boolean value to ensure correct data type.
    rbcb.checked = new Boolean(checked).valueOf();

    return true;

}




// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// field functions
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/** 
 * Use this function to get the HTML input or textarea element
 * associated with a TextField, PasswordField, HiddenField or TextArea
 * component. 
 * @param elementId The element ID of the field 
 * @return the input or text area element associated with the field
 * component 
 */
function field_getInputElement(elementId) { 
    var element = document.getElementById(elementId); 
    if(element != null) { 
        if(element.tagName == "INPUT") { 
            return element; 
	} 
        if(element.tagName == "TEXTAREA") { 
            return element; 
	} 
    } 
    return document.getElementById(elementId + "_field");
} 

/** 
 * Use this function to get the value of the HTML element 
 * corresponding to the Field component
 * @param elementId The element ID of the Field component
 * @return the value of the HTML element corresponding to the 
 * Field component 
 */
function field_getValue(elementId) { 
  return field_getInputElement(elementId).value; 
} 

/** 
 * Use this function to set the value of the HTML element 
 * corresponding to the Field component
 * @param elementId The element ID of the Field component
 * @param newStyle The new value to enter into the input element
 * Field component 
 */
function field_setValue(elementId, newValue) { 
  field_getInputElement(elementId).value = newValue;
}


/** 
 * Use this function to get the style attribute for the field. 
 * The style retrieved will be the style on the span tag that 
 * encloses the (optional) label element and the input element. 
 * @param elementId The element ID of the Field component
 */
function field_getStyle(elementId) { 
  return field_getInputElement(elementId).style; 
}

/** 
 * Use this function to set the style attribute for the field. 
 * The style will be set on the <span> tag that surrounds the field. 
 * @param elementId The element ID of the Field component
 * @param newStyle The new style to apply
 */
function field_setStyle(elementId, newStyle) { 
  field_getInputElement(elementId).style = newStyle; 
}
 

/** 
 * Use this function to disable or enable a field. As a side effect
 * changes the style used to render the field. 
 *
 * @param elementId The element ID of the field 
 * @param show true to disable the field, false to enable the field
 * @return true if successful; otherwise, false
 */
function field_setDisabled(elementId, disabled) {  

    if (elementId == null || disabled == null) {
        // must supply an elementId && state
        return false;
    }

    var textfield = field_getInputElement(elementId); 

    if (textfield == null) {
        return false;
    }

    var newState = new Boolean(disabled).valueOf();    

    var isTextArea = textfield.className.indexOf("TxtAra") > -1; 

    if (newState) { 
	if(isTextArea) {
	    textfield.className = "TxtAraDis";
	} else {
	    textfield.className = "TxtFldDis";
	}
    }
    else {
	if(isTextArea) {
	    textfield.className = "TxtAra";
	} else {
	    textfield.className = "TxtFld";	
	}
    }

    textfield.disabled = newState;
    return true;
}

// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// upload functions
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

/** 
 * Use this function to get the HTML input element associated with the
 * Upload component.  
 * @param elementId The element ID of the Upload
 * @return the input element associated with the Upload component 

 */
function upload_getInputElement(elementId) { 
    var element = document.getElementById(elementId); 
    if(element.tagName == "INPUT") { 
      return element; 
    } 
    return document.getElementById(elementId + "_com.sun.web.ui.upload");
} 

/** 
* Use this function to disable or enable a upload. As a side effect
* changes the style used to render the upload. 
 *
 * @param elementId The element ID of the upload 
 * @param show true to disable the upload, false to enable the upload
 * @return true if successful; otherwise, false
 */
function upload_setDisabled(elementId, disabled) {  

    if (elementId == null || disabled == null) {
        // must supply an elementId && state
        return false;
    }

    var input = upload_getInputElement(elementId); 
    if (input == null) {
        // specified elementId not found
        return false;
    }

    var newState = new Boolean(disabled).valueOf();    
    if (newState) {
	input.className = "TxtFldDis";
    } 
    else {
	input.className = "TxtFld";
    } 

    input.disabled = newState;
    return true;
}

function upload_setEncodingType(elementId) { 
 
    

    var upload = upload_getInputElement(elementId); 

    var form = upload; 
    while(form != null) { 
	form = form.parentNode; 
	if(form.tagName == "FORM") { 
	    break; 
	} 
    }
    if(form != null) {
    	// form.enctype does not work for IE, but works Safari
    	// form.encoding works on both IE and Firefox, but does not work for Safari
    	// form.enctype = "multipart/form-data";
    
        // <RAVE>
        // convert all characters to lowercase to simplify testing
        var agent = navigator.userAgent.toLowerCase();
           
        if( agent.indexOf('safari') != -1) {
            // form.enctype works for Safari
            // form.encoding does not work for Safari
    	    form.enctype = "multipart/form-data"
        } else {
            // form.encoding works for IE, FireFox
            form.encoding = "multipart/form-data"
        }
        // <RAVE>
    }
    return false;
}


// ---------------------------------------------------------------------
//
//  SCROLL POSITION MAINTENANCE
//
// This Javascript code will maintain scroll bar position during a
// page reload.
//
// ---------------------------------------------------------------------

// CONSTRUCTOR: construct a javascript object for maintaining scroll
// position via a cookie 

function sjwuic_ScrollCookie(viewId, path) {

    // All predefined properties of this object begin with '$' because
    // we don't want to store these values in the cookie.
    this.$cookieName = viewId;
    this.$path = path;

    // Default properties.
    this.left = "0";
    this.top  = "0";

    this.get   = sjwuic_GetCookie;
    this.load  = sjwuic_LoadCookie;
    this.reset = sjwuic_ResetCookie;
    this.store = sjwuic_StoreCookie;
    this.restore = sjwuic_RestoreScrollCookie;
    this.set     = sjwuic_SetScrollCookie;
}

// This function will load the cookie and restore scroll position.
function sjwuic_RestoreScrollCookie() {

    // Load cookie value.
    this.load();
    scrollTo(this.left, this.top);
    return true;
}

// This function will set the cookie value.
function sjwuic_SetScrollCookie() {

    var documentElement = window.document.documentElement;

    if (documentElement && documentElement.scrollTop) {
        this.left = documentElement.scrollLeft;
        this.top  = documentElement.scrollTop;
    } 
    else {
        this.left = window.document.body.scrollLeft;
        this.top  = window.document.body.scrollTop;
    }

    // if the left and top scroll values are still null
    // try to extract it assuming the browser is IE

    if (this.left == null && this.top == null) {
        this.left = window.pageXOffset;
        this.top = window.pageYOffset;
    }

    // Store cookie value.
    this.store();

    return true;
}


// ---------------------------------------------------------------------
//
//  GENERIC COOKIE CODE
//
// ---------------------------------------------------------------------


// This function will get the cookie value.
function sjwuic_GetCookie() {
    // Get document cookie.
    var cookie = document.cookie;

    // Parse sjwuic_ScrollCookie value.
    var pos = cookie.indexOf(this.$cookieName + "=");

    if (pos == -1)
        return null;

    var start = pos + this.$cookieName.length + 1;

    var end = cookie.indexOf(";", start);

    if (end == -1)
        end = cookie.length;

    // return cookie value
    return cookie.substring(start, end);
}

// This function will load the cookie value.
function sjwuic_LoadCookie() {

    // Get document cookie.
    var cookieVal = this.get();

    if (cookieVal == null)
        return false;

    // Break cookie into names and values.
    var a = cookieVal.split('&');

    // Break each pair into an array.
    for (var i = 0; i < a.length; i++) {
        a[i] = a[i].split(':');
    }

    // Set name and values for this object.
    for (i = 0; i < a.length; i++) {
        this[a[i][0]] = unescape(a[i][1]);
    }

    return true;
}

// This function will reset the cookie value.
function sjwuic_ResetCookie() {
    // Clear cookie value.
    document.cookie = this.$cookieName + "=";

    return true;
}

// This function will store the cookie value.
function sjwuic_StoreCookie() {
    // Create cookie value by looping through object properties
    var cookieVal = "";

    // Since cookies use the equals and semicolon signs as separators,
    // we'll use colons and ampersands for each variable we store.
    for (var prop in this) {
        // Ignore properties that begin with '$' and methods.
        if (prop.charAt(0) == '$' || typeof this[prop] == 'function')
            continue;
        
        if (cookieVal != "")
            cookieVal += '&';
        
        cookieVal += prop + ':' + escape(this[prop]);
    }

    var cookieString = this.$cookieName + "=" + cookieVal;

    if (this.$path != null) {
        cookieString += ";path=" + this.$path;
    }
    // Store cookie value.
    document.cookie = cookieString;

    return true;
}

// For browser platform specific information
//
function common_browserVersion() {

    // convert all characters to lowercase to simplify testing
    var agent = navigator.userAgent.toLowerCase();

    // *** BROWSER VERSION ***
    // Note: On IE5, these return 4, so use is_ie5up to detect IE5.
    var is_major = parseInt(navigator.appVersion);
    var is_minor = parseFloat(navigator.appVersion);

    // Note: Opera and WebTV spoof Navigator.  
    this.is_nav = ((agent.indexOf('mozilla') != -1) 
	&& (agent.indexOf('spoofer') == -1)
	&& (agent.indexOf('compatible') == -1));
    this.is_nav4 = (this.is_nav && (is_major == 4));
    this.is_nav4up = (this.is_nav && (is_major >= 4));
    this.is_navonly = (this.is_nav && ((agent.indexOf(";nav") != -1) ||
                     (agent.indexOf("; nav") != -1)));
    this.is_nav6 = (this.is_nav && (is_major == 5));
    this.is_nav6up = (this.is_nav && (is_major >= 5));
    this.is_gecko = (agent.indexOf('gecko') != -1);

    this.is_ie = ((agent.indexOf("msie") != -1) &&
                 (agent.indexOf("opera") == -1));
    this.is_ie3 = (this.is_ie && (is_major < 4));
    this.is_ie4 = (this.is_ie && (is_major == 4) && 
                 (agent.indexOf("msie 4") != -1));
    this.is_ie4up = (this.is_ie && (is_major >= 4));
    this.is_ie5 = (this.is_ie && (is_major == 4) && 
                 (agent.indexOf("msie 5.0") != -1));
    this.is_ie5_5 = (this.is_ie && (is_major == 4) && 
                   (agent.indexOf("msie 5.5") != -1));
    this.is_ie5up = (this.is_ie && !this.is_ie3 && !this.is_ie4);

    this.is_ie5_5up =(this.is_ie && !this.is_ie3 && !this.is_ie4 && !this.is_ie5);
    this.is_ie6 = (this.is_ie && (is_major == 4) && 
                 (agent.indexOf("msie 6.") != -1) );
    this.is_ie6up = (this.is_ie && !this.is_ie3 && !this.is_ie4 && !this.is_ie5 && !this.is_ie5_5);

    // *** PLATFORM ***
    this.is_sun = (agent.indexOf("sunos")!= -1);
    this.is_win = ((agent.indexOf("win")!= -1) || (agent.indexOf("16bit")!= -1));
    this.is_linux = (agent.indexOf("inux")!= -1);
}

var public_browserVersion = new common_browserVersion();
//-->
