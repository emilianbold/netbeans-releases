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

import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.component.util.descriptors.LayoutComponent;
import com.sun.rave.web.ui.component.util.descriptors.LayoutDefinition;
import com.sun.rave.web.ui.component.util.descriptors.LayoutElement;
import com.sun.rave.web.ui.renderer.template.LayoutDefinitionManager;

import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;


/**
 *  <P>	This abstract class provides base functionality for components that
 *	work in conjunction with the
 *	{@link com.sun.rave.web.ui.renderer.template.TemplateRenderer}.  It
 *	provides a default implementation of the {@link com.sun.rave.web.ui.component.TemplateComponent}
 *	interface.</P>
 *
 *  @see    com.sun.rave.web.ui.renderer.template.TemplateRenderer
 *  @see    com.sun.rave.web.ui.component.TemplateComponent
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public abstract class TemplateComponentBase extends UIComponentBase implements TemplateComponent {

    /**
     *	This method will find the request child UIComponent by id.  If it is
     *	not found, it will attempt to create it if it can find a LayoutElement
     *	describing it.
     *
     *	@param	context	    The FacesContext
     *	@param	id	    The UIComponent id to search for
     *
     *	@return	The requested UIComponent
     */
    public UIComponent getChild(FacesContext context, String id) {
	if ((id == null) || (id.trim().equals("")))  {
	    // No id, no LayoutComponent, nothing we can do.
	    return null;
	}

	// We have an id, use it to search for an already-created child
// FIXME: I am doing this 2x if it falls through to create the child... think about optimizing this
	UIComponent childComponent = Util.findChild(this, id, id);
	if (childComponent != null) {
	    return childComponent;
	}

	// If we're still here, then we need to create it... hopefully we have
	// a LayoutComponent to tell us how to do this!
	LayoutDefinition ld = getLayoutDefinition(context);
	if (ld == null) {
	    // No LayoutDefinition to tell us how to create it... return null
	    return null;
	}

	// Attempt to find a LayoutComponent matching the id
	LayoutElement elt = LayoutDefinition.getChildLayoutElementById(context, id, ld, this);

	// Create the child from the LayoutComponent
	return getChild(context, (LayoutComponent)elt);
    }


    /**
     *	This method will find the request child UIComponent by id (the id is
     *	obtained from the given LayoutComponent).  If it is not found, it will
     *	attempt to create it from the supplied LayoutElement.
     *
     *	@param	descriptor  The LayoutElement describing the UIComponent
     *
     *	@return	The requested UIComponent
     */
    public UIComponent getChild(FacesContext context, LayoutComponent descriptor) {
	UIComponent childComponent = null;

	// Sanity check
	if (descriptor == null) {
	    throw new IllegalArgumentException("The LayoutComponent is null!");
	}

	// First pull off the id from the descriptor
	String id = descriptor.getId(context, this);
	if ((id != null) && !(id.trim().equals(""))) {
	    // We have an id, use it to search for an already-created child
	    childComponent = Util.findChild(this, id, id);
	    if (childComponent != null) {
		return childComponent;
	    }
	}

	// No id, or the component hasn't been created.  In either case, we
	// create a new component (moral: always have an id)

	// Invoke "beforeCreate" handlers
	descriptor.beforeCreate(context, this);

	// Create UIComponent
	childComponent = Util.createChildComponent(context, descriptor, this);

	// Invoke "afterCreate" handlers
	descriptor.afterCreate(context, childComponent);

	// Add the new child (perhaps we shouldn't add it if it doesn't have
	// an id... what would this mean?)
// FIXME: I added this to the factory... this information is needed at create
// FIXME: time.
//	getChildren().add(childComponent);

	// Return the newly created UIComponent
	return childComponent;
    }


    /**
     *	This method returns the LayoutDefinition associated with this component.
     *
     *	@param	context	The FacesContext
     *
     *	@return	LayoutDefinition associated with this component.
     */
    public LayoutDefinition getLayoutDefinition(FacesContext context) {
	// Make sure we don't already have it...
	if (_layoutDefinition != null) {
	    return _layoutDefinition;
	}

	// Get the LayoutDefinitionManager key
	String key = getLayoutDefinitionKey();
	if (key == null) {
	    throw new NullPointerException("LayoutDefinition key is null!");
	}

	// Get the LayoutDefinitionManager
	LayoutDefinitionManager ldm =
	    LayoutDefinitionManager.getManager(context);

	// Save the LayoutDefinition for future calls to this method
	_layoutDefinition = ldm.getLayoutDefinition(key);

	// Return the LayoutDefinition (if found)
	return _layoutDefinition;
    }

    /**
     *	This method saves the state for this component.  It relies on the
     *	super class to save its own sate, this method will invoke
     *	super.saveState().
     *
     *	@param	context	The FacesContext
     *
     *	@return The serialized State
     */
    public Object saveState(FacesContext context) {
	Object values[] = new Object[2];
	values[0] = super.saveState(context);
	values[1] = _ldmKey;
	return values;
    }

    /**
     *	This method restores the state for this component.  It will invoke the
     *	super class to restore its state.
     *
     *	@param	context	The FacesContext
     *	@param	state	The serialized State
     *
     */
    public void restoreState(FacesContext context, Object state) {
	Object values[] = (Object[]) state;
	super.restoreState(context, values[0]);
	_ldmKey = (java.lang.String) values[1];
    }

    /**
     *	This method returns the LayoutDefinitionKey for this component.
     *
     *	@return	key	The key to use in the LayoutDefinitionManager
     */
    public String getLayoutDefinitionKey() {
	return _ldmKey;
    }


    /**
     *	This method sets the LayoutDefinition key for this component.
     *
     *	@param	key The key to use in the LayoutDefinitionManager
     */
    public void setLayoutDefinitionKey(String key) {
	_ldmKey = key;
    }

    /**
     *	This is the LayoutDefinition key for this component.  This is
     *	typically set by the Tag.  The Component may also provide a default
     *	by setting it in its constructor.
     */
    private String _ldmKey = null;


    /**
     *	This is a cached reference to the LayoutDefinition used by this
     *	UIComponent.
     */
    private transient LayoutDefinition _layoutDefinition = null;
}
