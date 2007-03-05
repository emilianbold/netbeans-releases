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
package com.sun.rave.web.ui.component.util.handlers;

import com.sun.rave.web.ui.component.Property;
import com.sun.rave.web.ui.component.PropertySheet;
import com.sun.rave.web.ui.component.PropertySheetSection;
import com.sun.rave.web.ui.component.util.event.HandlerContext;


/**
 *  <P>	This class contains {@link com.sun.rave.web.ui.component.util.event.Handler}
 *	methods that perform PropertySheet specific functions.</P>
 *
 *  @author  Ken Paulsen (ken.paulsen@sun.com)
 */
public class PropertySheetHandlers {

    /**
     *	<P> Default Constructor.</P>
     */
    public PropertySheetHandlers() {
    }

    /**
     *	<p> This method calculates the number of visible
     *	    {@link PropertySheetSection}s.  A {@link PropertySheetSection} can
     *	    be made not visible by setting its rendered propety to false.</p>
     *
     *	<p> This method expects the {@link PropertySheet} to be passed in as
     *	    an input value ({@link #PROP_SHEET}).  It returns "numSections" as
     *	    the number of visible {@link PropertySheetSection}s.</p>
     *
     *	@param	context	    The {@link HandlerContext}
     */
    public void getSectionCount(HandlerContext context) {
	// Get the PropertySheet
	PropertySheet propSheet =
	    (PropertySheet)context.getInputValue(PROP_SHEET);

	// Set the output value
	context.setOutputValue("numSections",
	    new Integer(propSheet.getSectionCount()));
    }

    /**
     *	<p> This method calculates the number of visible child
     *	    {@link PropertySheetSection} or {@link Property}
     *	    <code>UIComponent</code>s.  A {@link PropertySheetSection} or
     *	    {@link Property} can be made not visible by setting its rendered
     *	    propety to false.</p>
     *
     *	<p> This method expects the {@link PropertySheetSection} to be passed
     *	    in as an input value ({@link #SECTION}).  It returns the result in
     *	    "numChildren".</p>
     *
     *	@param	context	    The {@link HandlerContext}
     */
    public void getSectionChildrenCount(HandlerContext context) {
	// Get the Section
	PropertySheetSection section = (PropertySheetSection)context.getInputValue(SECTION);

	// Set the output value
	context.setOutputValue("numChildren",
	    new Integer(section.getSectionChildrenCount()));
    }

    /**
     *	<p> This {@link com.sun.rave.web.ui.component.util.event.Handler} creates a
     *	    <code>List</code> of visible (rendered=true) child
     *	    {@link PropertySheetSection} or {@link Property} components.</p>
     *
     *	@param	context	    The {@link HandlerContext}.
     */
    public void getVisibleSectionChildren(HandlerContext context) {
	// Get the Section
	PropertySheetSection section = (PropertySheetSection)context.getInputValue(SECTION);

	// Set the output value
	context.setOutputValue("children", section.getVisibleSectionChildren());
    }

    /**
     *	<p> This {@link com.sun.rave.web.ui.component.util.event.Handler} creates a
     *	    <code>List</code> of visible (rendered=true)
     *	    {@link PropertySheetSection} components.</p>
     *
     *	@param	context	    The {@link HandlerContext}.
     */
    public void getVisibleSections(HandlerContext context) {
	// Get the PropertySheet
	PropertySheet propSheet = (PropertySheet)context.getInputValue(PROP_SHEET);

	// Set the output value
	context.setOutputValue("sections", propSheet.getVisibleSections());
    }

    /**
     *	<p> This {@link com.sun.rave.web.ui.component.util.event.Handler} creates a
     *	    <code>List</code> of visible (rendered=true)
     *	    {@link PropertySheetSection} components.</p>
     *
     *	@param	context	    The {@link HandlerContext}.
     */
    public void getLabelTarget(HandlerContext context) {
	// Get the Property
	Property prop = (Property)context.getInputValue(PROPERTY);

	// Delegate to the Property UIComponent
	context.setOutputValue("target",
		prop.getPrimaryElementID(context.getFacesContext()));
    }

    /**
     *	<p> This constant defines the input parameter key used to pass in the
     *	    {@link PropertySheet}. ("propSheet")</p>
     */
    public static final String PROP_SHEET =	"propSheet"; // NO18N

    /**
     *	<p> This constant defines the input parameter key used to pass in the
     *	    {@link PropertySheetSection}. ("section")</p>
     */
    public static final String SECTION =	"section"; // NO18N

    /**
     *	<p> This constant defines the input parameter key used to pass in the
     *	    {@link Property}. ("property")</p>
     */
    public static final String PROPERTY =	"property"; // NO18N
}
