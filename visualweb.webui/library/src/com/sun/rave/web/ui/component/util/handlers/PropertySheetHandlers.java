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
