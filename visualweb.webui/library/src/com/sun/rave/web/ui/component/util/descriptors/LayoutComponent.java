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
package com.sun.rave.web.ui.component.util.descriptors;

import com.sun.rave.web.ui.component.util.Util;
import com.sun.rave.web.ui.component.ChildManager;
import com.sun.rave.web.ui.component.util.event.AfterCreateEvent;
import com.sun.rave.web.ui.component.util.event.BeforeCreateEvent;
import com.sun.rave.web.ui.util.VariableResolver;

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
 *  <p>	This class defines a LayoutComponent.  A LayoutComponent describes a
 *	UIComponent to be instantiated.  The method {@link #getType()} provides
 *	a {@link ComponentType} descriptor that is capable of providing a
 *	{@link com.sun.rave.web.ui.component.util.factories.ComponentFactory} to
 *	perform the actual instantiation.  This class also stores properties
 *	and facets (children) to be set on a newly instantiated instance.</p>
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutComponent extends LayoutElementBase implements LayoutElement {

    /**
     *	<p> Constructor.</p>
     */
    public LayoutComponent(LayoutElement parent, String id, ComponentType type) {
	super(parent, id);
	_type = type;
    }

    /**
     *	<p> Accessor for type.</p>
     */
    public ComponentType getType() {
	return _type;
    }

    /**
     *	<p> Determines if this component should be created even if there is
     *	    already an existing <code>UIComponent</code>.  It will "overwrite"
     *	    the existing component if this property is true.</p>
     */
    public void setOverwrite(boolean value) {
	_overwrite = value;
    }

    /**
     *	<p> Determines if this component should be created even if there is
     *	    already an existing <code>UIComponent</code>.  It will "overwrite"
     *	    the existing component if this property is true.</p>
     */
    public boolean isOverwrite() {
	return _overwrite;
    }

    /**
     *	<p> This method overrides LayoutElementBase.addChildLayoutElement().
     *	    Child LayoutElements for LayoutComponent are limited to LayoutFacet
     *	    objects.  This method ensures that only LayoutFacet objects are
     *	    added.  If any other types are added, an IllegalArgumentException
     *	    will be thrown.</p>
     *
     *	@param	element	    The LayoutElement to add.
     *
     *	@throws	IllegalArgumentException Thrown if LayoutElement is not a
     *	    LayoutFacet
     */
    public void addChildLayoutElement(LayoutElement element) {
	if (!(element instanceof LayoutComponent) &&
		!(element instanceof LayoutFacet)) {
	    throw new IllegalArgumentException("Only LayoutComponent and "
		    + "LayoutFacet LayoutElements may be added as children to "
		    + "a LayoutComponent!");
	}
	super.addChildLayoutElement(element);
    }

    /**
     *	<p> This method adds an option to the LayoutComponent.  Options may be
     *	    useful in constructing the LayoutComponent.</p>
     *
     *	@param	name	The name of the option
     *	@param	value	The value of the option (may be List or String)
     */
    public void addOption(String name, Object value) {
	_options.put(name, value);
    }

    /**
     *	<p> This method adds all the options in the given Map to the
     *	    LayoutComponent.  Options may be useful in constructing the
     *	    LayoutComponent.</p>
     *
     *	@param	map	The map of options to add.
     */
    public void addOptions(Map map) {
	_options.putAll(map);
    }

    /**
     *	<p> Accessor method for an option.  This method does not evaluate
     *	    expressions.</p>
     *
     *	@param	name	The option name to retrieve.
     *
     *	@return	The option value (List or String), or null if not found.
     *
     *	@see #getEvaluatedOption(FacesContext, String, UIComponent)
     */
    public Object getOption(String name) {
	return _options.get(name);
    }

    /**
     *	<p> Accessor method for an option.  This method evaluates our own
     *	    expressions (not JSF expressions).</p>
     *
     *	@param	name	    The option name to retrieve
     *	@param	ctx	    The FacesContext
     *	@param	component   The UIComponent (may be null)
     *
     *	@return	The option value (List or String), or null if not found.
     *
     *	@see #getOption(String)
     */
    public Object getEvaluatedOption(FacesContext ctx, String name, UIComponent component) {
	// Get the option value
	Object value = getOption(name);

	// Invoke our own EL.  This is needed b/c JSF's EL is designed for
	// Bean getters only.  It does not get CONSTANTS or pull data from
	// other sources (such as session, request attributes, etc., etc.)
	// Resolve our variables now because we cannot depend on the
	// individual components to do this.  We may want to find a way to
	// make this work as a regular ValueBinding expression... but for
	// now, we'll just resolve it here.
	return VariableResolver.resolveVariables(ctx, this, component, value);
    }

    /**
     *	<p> This method returns true/false based on whether the given option
     *	    name has been set.</p>
     *
     *	@param	name	The option name to look for.
     *
     *	@return	true/false depending on whether the options exists.
     */
    public boolean containsOption(String name) {
	return _options.containsKey(name);
    }

    /**
     *	<p> This method sets the Map of options.</p>
     *
     *	@param	options	    Map of options.
     */
    public void setOptions(Map options) {
	_options = options;
    }

    /**
     *	<p> This method returns the options as a Map.  This method does not
     *	    evaluate expressions.</p>
     *
     *	@return Map of options.
     */
    public Map getOptions() {
	return _options;
    }

    /**
     *	<p> This method allows each LayoutElement to provide it's own encode
     *	    functionality.  If the LayoutComponent should render its children,
     *	    this method should return true.  Otherwise, this method should
     *	    return false.</p>
     *
     *	@param	context	    The FacesContext
     *	@param	component   The UIComponent
     *
     *	@return	true if children are to be rendered, false otherwise.
     */
    protected boolean encodeThis(FacesContext context, UIComponent component) throws IOException {
	// If overwrite...
	if (isOverwrite()) {
	    String id = getId(context, component);
	    if (component.getFacets().remove(id) == null) {
		UIComponent child = Util.findChild(component, id, null);
		if (child != null) {
		    // Not a facet, try child...
		    component.getChildren().remove(child);
		}
	    }
	}

	// Display this UIComponent
	// First find the UIComponent
	UIComponent childComponent = null;
	if (component instanceof ChildManager) {
	    // If we have a ChildManager, take advantage of it...
	    childComponent = ((ChildManager) component).getChild(context, this);
	} else {
	    // Use local util method for finding / creating child component...
	    childComponent = getChild(context, component);
	}

	// Render the child UIComponent
	encodeChild(context, childComponent);

	// We return false here b/c it is up to the renderer above to decide
	// how to deal w/ children.  The renderer that called this method is
	// not responsible for rendering the children any further than this.
	return false;
    }

    /**
     *	<p> This method will find or create a <code>UIComponent</code> as
     *	    described by this <code>LayoutComponent</code> descriptor.  If the
     *	    component already exists as a child or facet, it will be returned.
     *	    If it creates a new <code>UIComponent</code>, it will typically be
     *	    added to the given parent <code>UIComponent</code> as a facet (this
     *	    actually depends on the factory that instantiates the
     *	    <code>UIComponent</code>).</p>
     *
     *	@param	context	The <code>FacesContext</code>
     *	@param	parent	The <code>UIComponent</code> to serve as the parent to
     *			search and to store the new <code>UIComponent</code>.
     *
     *	@return	The <code>UIComponent</code> requested (found or newly created)
     */
    public UIComponent getChild(FacesContext context, UIComponent parent) throws IOException {
	UIComponent childComponent = null;

	// First pull off the id from the descriptor
	String id = this.getId(context, parent);
	if ((id != null) && !(id.trim().equals(""))) {
	    // We have an id, use it to search for an already-created child
	    childComponent = Util.findChild(parent, id, id);
	    if (childComponent != null) {
		return childComponent;
	    }
	}

	// No id, or the component hasn't been created.  In either case, we
	// create a new component (moral: always have an id)

	// Invoke "beforeCreate" handlers
	this.beforeCreate(context, parent);

	// Create UIComponent
	childComponent = Util.createChildComponent(context, this, parent);

	// Invoke "afterCreate" handlers
	this.afterCreate(context, childComponent);

	// Add the new child (perhaps we shouldn't add it if it doesn't have
	// an id... what would this mean?)
// FIXME: I added this to the factory... this information is needed at create
// FIXME: time.
//	getChildren().add(childComponent);

	// Return the newly created UIComponent
	return childComponent;
    }

    /**
     *	<p> This method is invoked before the Component described by this
     *	    LayoutComponent is created.  This allows handlers registered for
     *	    "beforeCreate" functionality to be invoked.</p>
     *
     *	@param	context	The FacesContext
     *
     *	@return	The result of invoking the handlers (null by default)
     */
    public Object beforeCreate(FacesContext context, UIComponent parent) {
	// Invoke "beforeCreate" handlers
	return dispatchHandlers(context, BEFORE_CREATE, new BeforeCreateEvent(parent));
    }

    /**
     *	<p> This method is invoked after the Component described by this
     *	    LayoutComponent is created.  This allows handlers registered for
     *	    "afterCreate" functionality to be invoked.</p>
     *
     *	@param	context	The FacesContext
     *
     *	@return	The result of invoking the handlers (null by default)
     */
    public Object afterCreate(FacesContext context, UIComponent component) {
	// Invoke "afterCreate" handlers
	return dispatchHandlers(context, AFTER_CREATE, new AfterCreateEvent(component));
    }

    /**
     *	<p> This method returns true if the child should be added to the parent
     *	    component as a facet.  Otherwise, it returns false indicating that
     *	    it should exist as a real child.  The default is true.</p>
     *
     *	@return	True if the child UIComponent should be added as a facet.
     */
    public boolean isFacetChild() {
	return _isFacetChild;
    }

    /**
     *	<p> This method sets whether the child <code>UIComponent</code> should
     *	    be set as a facet or a real child.</p>
     *
     *	@param	facetChild  True if the child <code>UIComponent</code> should
     *			    be added as a facet.
     */
    public void setFacetChild(boolean facetChild) {
	_isFacetChild = facetChild;
    }


    /**
     *	<p> Component type</p>
     */
    private ComponentType _type	= null;

    /**
     *	<p> Determines if this component should be created even if there is
     *	    already an existing <code>UIComponent</code>.  It will "overwrite"
     *	    the existing component if this property is true.</p>
     */
    private boolean _overwrite	= false;

    /**
     *	<p> Map of options.</p>
     */
    private Map	_options	= new HashMap();

    /**
     *
     */
    private boolean _isFacetChild = true;

    /**
     *	<p> This is the "type" for handlers to be invoked to handle
     *	    "afterCreate" functionality for this element.</p>
     */
    public static final String AFTER_CREATE =	"afterCreate";

    /**
     *	<p> This is the "type" for handlers to be invoked to handle
     *	    "beforeCreate" functionality for this element.</p>
     */
    public static final String BEFORE_CREATE =	"beforeCreate";

    /**
     *	<p> This defines the property key for specifying the facet name in
     *	    which the component should be stored under in its parent
     *	    UIComponent.</p>
     */
    public static final String FACET_NAME =	"_facetName";
}
