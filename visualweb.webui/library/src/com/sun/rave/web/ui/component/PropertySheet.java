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
public class PropertySheet extends PropertySheetBase {

    /**
     *	Constructor.
     */
    public PropertySheet() {
	super();
    }

    /**
     *	<p> This method calculates the number of visible
     *	    {@link PropertySheetSection}s.  A {@link PropertySheetSection} can
     *	    be made not visible by setting its rendered propety to false.  It
     *	    is also considered not visible if it contains no children
     *	    (sub-sections or properties).</p>
     *
     *	@return	The number of visible sections.
     */
    public int getSectionCount() {
	// Return the answer
	return getVisibleSections().size();
    }

    /**
     *	<p> This method creates a <code>List</code> of visible (rendered=true)
     *	    {@link PropertySheetSection} components.
     *	    {@link PropertySheetSection}s must also contain some content to be
     *	    considered visible.</p>
     *
     *	@return	A <code>List</code> of visible {@link PropertySheetSection}
     *		objects.
     */
    public List getVisibleSections() {
	int numChildren = getChildCount();

	// See if we've already figured this out
	if ((_visibleSections != null) && (_childCount == numChildren)) {
	    return _visibleSections;
	}
	_childCount = numChildren;

	// Make sure we have children
	if (numChildren == 0) {
	    _visibleSections = new ArrayList(0);
	    return _visibleSections;
	}

	// Add the visible sections to the result List
	UIComponent child = null;
	List visibleSections = new ArrayList();
	Iterator it = getChildren().iterator();
	while (it.hasNext()) {
	    child = (UIComponent)it.next();
	    if ((child instanceof PropertySheetSection) && child.isRendered()) {
		if (((PropertySheetSection)child).getVisibleSectionChildren().size() > 0) {
		    visibleSections.add(child);
		}
	    }
	}

	// Return the visible PropertySheetSections
	_visibleSections = visibleSections;
	return _visibleSections;
    }


    /**
     *	<p> Used to cache the visible sections.</p>
     */
    private transient List	_visibleSections = null;
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
        _visibleSections = null;
        _childCount = -1;
        super.encodeBegin(context);
    }
}
