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
package com.sun.rave.web.ui.component.util.descriptors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.component.UIColumn;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIViewRoot;
import javax.faces.render.Renderer;

/**
 *  <P>	This class defines the descriptor for LayoutFacet.  A LayoutFacet
 *	descriptor provides information needed to attempt to obtain a Facet
 *	from the UIComponent.  If the Facet doesn't exist, it also has the
 *	opportunity to provide a "default" in place of the facet.</P>
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutFacet extends LayoutElementBase implements LayoutElement {

    /**
     *	Constructor
     */
    public LayoutFacet(LayoutElement parent, String id) {
	super(parent, id);
    }

    /**
     *	<p> Returns whether this LayoutFacet should be rendered.  When this
     *	    component is used to specify an actual facet (i.e. specifies a
     *	    <code>UIComponent</code>), it should not be rendred.  When it
     *	    defines a place holder for a facet, then it should be rendered.</p>
     *
     *	@return	true if {@link #encodeThis(FacesContext, UIComponent)} should
     *		execute
     */
    public boolean isRendered() {
	return _rendered;
    }

    /**
     *
     */
    public void setRendered(boolean render) {
	_rendered = render;
    }

    /**
     *	<P>This method looks for the facet on the component.  If found, it
     *	renders it and returns false (so children will not be rendered).  If
     *	not found, it returns true so that children will be rendered.
     *	Children of a LayoutFacet represent the default value for the
     *	Facet.</P>
     *
     *	@param	context	    The FacesContext
     *	@param	parent	    The parent UIComponent
     *
     *	@return	true if children are to be rendered, false otherwise.
     */
    protected boolean encodeThis(FacesContext context, UIComponent component) throws IOException {
	// Make sure we are supposed to render facets
	if (!isRendered()) {
	    return false;
	}

	// Look for Facet
	component = (UIComponent)component.getFacets().
	    get(getId(context, component));
	if (component != null) {
	    // Found Facet, Display UIComponent
	    encodeChild(context, component);

	    // Return false so the default won't be rendered
	    return false;
	}

	// Return true so that the default will be rendered
	return true;
    }

    private boolean _rendered = true;
}
