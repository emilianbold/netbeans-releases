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

import com.sun.rave.web.ui.component.util.event.DecodeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.component.UIComponent;


/**
 *  <P>	This represents the top-level {@link LayoutElement}, it is the
 *	container for every other {@link LayoutElement}.  By itself, it has no
 *	functionality.  Its purpose in life is to group all top-level child
 *	{@link LayoutElement}s.  LayoutDefintion objects can be registered
 *	with the {@link com.sun.rave.web.ui.renderer.template.LayoutDefinitionManager}.
 *	This class does provide a useful method
 *	{@link #getChildLayoutElementById(FacesContext, String, LayoutElement, UIComponent)}
 *	which will search recursively for the given child id.</P>
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class LayoutDefinition extends LayoutElementBase {

    /**
     *	This is a hard-coded LayoutComponent type.  By default it corresponds
     *	to com.sun.rave.web.ui.component.util.factories.StaticTextFactory.
     */
    public static final String STATIC_TEXT_TYPE			=
	"staticText";

    /**
     *	This is the full class name of the default StaticTextFactory
     */
    public static final String STATIC_TEXT_FACTORY_CLASS_NAME	=
	"com.sun.rave.web.ui.component.util.factories.StaticTextFactory";

    /**
     *	This is a list of Resource objects.  These Resources are to be added
     *	to the Request scope when this LayoutDefinition is used.
     */
    private List _resources = new ArrayList();

    /**
     *	Map of types.  This information is needed to instantiate UIComponents.
     */
    private Map _types = new HashMap();

    /**
     *	Map of attributes.  Attributes can be used to store extra information
     *	about the LayoutDefinition.
     */
    private Map _attributes = new HashMap();


    /**
     *	Constructor
     */
    public LayoutDefinition(String id) {
	// LayoutDefinition objects do not have a parent or an id
	super(null, id);

	// Set the default StaticText ComponentType
	addComponentType(new ComponentType(
	    STATIC_TEXT_TYPE, STATIC_TEXT_FACTORY_CLASS_NAME));
    }


    /**
     *	Retrieve a ComponentType by typeID
     *
     *	@param	typeID	The key used to retrieve the ComponentType
     *
     *	@return	The requested ComponentType or null
     */
    public ComponentType getComponentType(String typeID) {
	return (ComponentType)_types.get(typeID);
    }


    /**
     *  This will add the given ComponentType to the map of registered
     *	ComponentTypes.  It will use the ComponentType ID as the key to the
     *	Map.  This means that if a ComponentType with the same ID had
     *	previously been registered, it will be replaced with the ComponentType
     *	passed in.
     *
     *	@param	type	The ComponentType.
     */
    public void addComponentType(ComponentType type) {
	_types.put(type.getId(), type);
    }

    /**
     *	This method adds a Resource.  These resources should be added to the
     *	request scope when this component is used.  This is mainly used for
     *	ResourceBundles (at this time).
     *
     *	@param	res The Resource to associate with the LayoutDefinition
     */
    public void addResource(Resource res) {
	_resources.add(res);
    }

    /**
     *	This method returns a List of Resource objects.
     *
     *	@return This method returns a List of Resource objects.
     */
    public List getResources() {
	return _resources;
    }

    /**
     *	This method searches for the requested LayoutComponent by id.
     *
     *	@param	context		<code>FacesContext</code>
     *	@param	id		id to look for
     *	@param	parent		Search starts from this
     *				<code>LayoutElement</code>
     *	@param	parentComponent	Parent <code>UIComponent</code>
     *	
     *	@return	The matching LayoutElement if found, null otherwise.
     */
    public static LayoutElement getChildLayoutElementById(FacesContext context, String id, LayoutElement parent, UIComponent parentComponent) {
	// NOTE: I may want to optimize this by putting all values in a Map so
	// NOTE: that I don't have to do this search.

	// Make sure this isn't what we're looking for
	if (parent.getId(context, parentComponent).equals(id)) {
	    return parent;
	}

	// Not 'this' so lets check the children
	Iterator it = parent.getChildLayoutElements().iterator();
	LayoutElement elt = null;
	while (it.hasNext()) {
	    elt = getChildLayoutElementById(context, id, (LayoutElement)it.next(), parentComponent);
	    if (elt != null) {
		// Found it!
		return elt;
	    }
	}

	// Not found...
	return null;
    }


    /**
     *	Retrieve an attribute by key
     *
     *	@param	key	The key used to retrieve the attribute
     *
     *	@return	The requested attribute or null
     */
    public Object getAttribute(String key) {
	return _attributes.get(key);
    }


    /**
     *	Associate the given key with the given Object as an attribute.
     *
     *	@param	key	The key associated with the given object (if this key
     *	    is already in use, it will replace the previously set attribute
     *	    object).
     *
     *	@param	value	The Object to store.
     */
    public void setAttribute(String key, Object value) {
	_attributes.put(key, value);
    }


    /**
     *	<P> The LayoutDefinition does not encode anything for itself, this
     *	    method simply returns true.</P>
     *
     *	@param	context	    The FacesContext
     *	@param	component   The UIComponent
     *
     *	@return	true
     */
    protected boolean encodeThis(FacesContext context, UIComponent component) {
	return true;
    }

    /**
     *	<P> This decode method invokes any registered DECODE handlers.</P>
     *
     *	@param	context	    The FacesContext
     *	@param	component   The Template
     */
    public void decode(FacesContext context, UIComponent component) {
	// Invoke "decode" handlers
	dispatchHandlers(context, DECODE, new DecodeEvent(component));
    }


    /**
     *	<P> This is the "type" for handlers to be invoked to handle "decode"
     *	    functionality for this element.</P>
     */
     public static final String DECODE =	"decode";
}
