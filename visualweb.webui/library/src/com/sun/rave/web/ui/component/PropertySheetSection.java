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

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;

/**
 *
 * @author  Ken Paulsen
 */
public class PropertySheetSection extends PropertySheetSectionBase {

    public PropertySheetSection() {
	super();
    }

    /**
     *	<p> This method calculates the number of visible child
     *	    {@link PropertySheetSection} or {@link Property}
     *	    <code>UIComponent</code>s.  A {@link PropertySheetSection}
     *	    or {@link Property} can be made not visible by setting their
     *	    rendered property to false.</p>
     *
     *	@return The number of visible {@link PropertySheetSection} children.
     */
    public int getSectionChildrenCount() {
	// Set the output value
	return getVisibleSectionChildren().size();
    }

    /**
     *	<p> This method creates a <code>List</code> of visible (rendered=true)
     *	    child {@link PropertySheetSection} or {@link Property}
     *	    components.</p>
     *
     *	@return	<code>List</code> of child {@link PropertySheetSection} or
     *	    {@link Property} <code>UIComponent</code> objects.
     */
    public List getVisibleSectionChildren() {
	int numChildren = getChildCount();

	// See if we've already figured this out
	if ((_visibleChildren != null) && (_childCount == numChildren)) {
	    return _visibleChildren;
	}
	_childCount = numChildren;

	// Make sure we have children
	if (numChildren == 0) {
	    // Avoid creating child UIComponent List by checking for 0 sections
	    _visibleChildren = new ArrayList(0);
	    return _visibleChildren;
	}

	// Add the visible sections to the result List
	UIComponent child = null;
	_visibleChildren = new ArrayList();
	Iterator it = getChildren().iterator();
	while (it.hasNext()) {
	    child = (UIComponent)it.next();
	    if (((child instanceof Property) ||
		    (child instanceof PropertySheetSection)) &&
		    child.isRendered()) {
		_visibleChildren.add(child);
	    }
	}

	// Return the List
	return _visibleChildren;
    }


    /**
     *	<p> Used to cache the visible children.</p>
     */
    private transient List	_visibleChildren = null;
    private transient int	_childCount = -1;

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // UIComponent methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     * If the rendered property is true, render the begining of the current
     * state of this UIComponent to the response contained in the specified
     * FacesContext.
     *
     * If a Renderer is associated with this UIComponent, the actual encoding 
     * will be delegated to Renderer.encodeBegin(FacesContext, UIComponent).
     *
     * @param context FacesContext for the current request.
     *
     * @exception IOException if an input/output error occurs while rendering.
     * @exception NullPointerException if FacesContext is null.
     */
    public void encodeBegin(FacesContext context) throws IOException {
        // Clear cached variables -- bugtraq #6270214.
        _visibleChildren = null;
        _childCount = -1;
        super.encodeBegin(context);
    }
}
