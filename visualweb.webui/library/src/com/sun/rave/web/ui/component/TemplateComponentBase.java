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
