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
package com.sun.rave.web.ui.component.util;

import com.sun.rave.web.ui.component.ChildManager;
import com.sun.rave.web.ui.component.ComplexComponent;
import com.sun.rave.web.ui.component.util.descriptors.ComponentType;
import com.sun.rave.web.ui.component.util.descriptors.LayoutComponent;
import com.sun.rave.web.ui.component.util.descriptors.LayoutElement;
import com.sun.rave.web.ui.util.VariableResolver;


import java.beans.Beans;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.faces.FactoryFinder;
import javax.faces.component.UIComponent;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIForm;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.ValueBinding;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.faces.webapp.UIComponentTag;
import javax.servlet.http.HttpServletRequest;

/**
 * Utility class that contains helper methods for components.
 *
 * @author  Ken Paulsen
 */
public class Util {

    /**
     *	<p> This constructor is here to prevent this class from being
     *	    instantiated.  It only contains static methods.</p>
     */
    private Util() {
    }

    /**
     *	<p> Return a child with the specified component id from the specified
     *	    component. If not found, return <code>null</code>.</p>
     *
     *	<p> This method will NOT create a new <code>UIComponent</code>.</p>
     *
     *	@param	parent	<code>UIComponent</code> to be searched
     *	@param	id	Component id (or facet name) to search for
     *
     *	@return	The child <code>UIComponent</code> if it exists, null otherwise.
     */
    public static UIComponent getChild(UIComponent parent, String id) {
	return findChild(parent, id, id);
    }

    /**
     *	<p> Return a child with the specified component id (or facetName) from
     *	    the specified component. If not found, return <code>null</code>.
     *	    <code>facetName</code> or <code>id</code> may be null to avoid
     *	    searching the facet Map or the <code>parent</code>'s children.</p>
     *
     *	<p> This method will NOT create a new <code>UIComponent</code>.</p>
     *
     *	@param	parent	    <code>UIComponent</code> to be searched
     *	@param	id	    id to search for
     *	@param	facetName   Facet name to search for
     *
     *	@return	The child <code>UIComponent</code> if it exists, null otherwise.
     */
    public static UIComponent findChild(UIComponent parent, String id, String facetName) {
	// Sanity Check
	if (parent == null) {
	    return null;
	}

	// First search for facet
	UIComponent child = null;
	if (facetName != null) {
	    child = (UIComponent)parent.getFacets().get(facetName);
	    if (child != null) {
		return child;
	    }
	}

	// Search for component by id
	if (id != null) {
	    Iterator it = parent.getChildren().iterator();
	    while (it.hasNext()) {
		child = (UIComponent)it.next();
		if (id.equals(child.getId())) {
		    return (child);
		}
	    }
	}

	// Not found, return null
	return null;
    }

    /**
     *	<p> This method finds or creates a child <code>UIComponent</code>
     *	    identified by the given id.  If the child is not found, it will
     *	    attempt to create it using the provided {@link com.sun.rave.web.ui.component.util.factories.ComponentFactory}
     *	    (<code>factoryClass</code>).</p>
     *
     *	<p> If there are <code>Properties</code> to be set on the UIComponent,
     *	    this method should generally be avoided.  It is preferable to use
     *	    the {@link #getChild(UIComponent, String, String, Properties)}
     *	    form of <code>getChild</code>.</p>
     *
     *	<p> <code>
     *		// Example (no properties):<br>
     *		UIComponent child = Util.getChild(component, "jklLabel", "{@link com.sun.rave.web.ui.component.util.factories.LabelFactory com.sun.rave.web.ui.component.util.factories.LabelFactory}");<br>
     *		((Label)child).setText("JKL Label:");<br>
     *		((Label)child).setFor("jkl");<br>
     *		<br>
     *		{@link LayoutComponent#encodeChild(FacesContext, UIComponent) LayoutComponent.encodeChild}(context, child);
     *	    </code></p>
     *
     *	@param	parent		Parent <code>UIComponent</code>
     *	@param	id		Identifier for the child <code>UIComponent</code>
     *	@param	factoryClass	Full {@link com.sun.rave.web.ui.component.util.factories.ComponentFactory} class name
     *
     *	@return	The child UIComponent that was found or created.
     *
     *	@see	#getChild(UIComponent, String, String, Properties)
     */
    public static UIComponent getChild(UIComponent parent, String id, String factoryClass) {
	return getChild(parent, id, factoryClass, id);
    }

    /**
     *	<p> Same as {@link #getChild(UIComponent, String, String)} except that
     *	    it allows you to specify a facetName different than the id.  If
     *	    null is supplied, it won't save the component as a facet.</p>
     *
     *	@param	parent		Parent <code>UIComponent</code>
     *	@param	id		Identifier for the child <code>UIComponent</code>
     *	@param	factoryClass	Full {@link com.sun.rave.web.ui.component.util.factories.ComponentFactory} class name
     *	@param	facetName	The facet name (null means don't store it)
     *
     *	@return	The child UIComponent that was found or created.
     *
     *	@see	#getChild(UIComponent, String, String)
     */
    public static UIComponent getChild(UIComponent parent, String id, String factoryClass, String facetName) {
	return getChild(parent, id, getComponentType(factoryClass), null, facetName);
    }

    /**
     *	<p> This method finds or creates a child <code>UIComponent</code>
     *	    identified by the given id.  If the child is not found, it will
     *	    attempt to create it using the provided {@link com.sun.rave.web.ui.component.util.factories.ComponentFactory}
     *	    (<code>factoryClass</code>).  It will also initialize the
     *	    <code>UIComponent</code> using the provided set of
     *	    <code>Properties</code>.</p>
     *
     *	<p> <code>
     *		// Example (with properties):<br>
     *		Properties props = new Properties();<br>
     *		props.setProperty("text", "ABC Label:");<br>
     *		props.setProperty("for", "abc");<br>
     *		UIComponent child = Util.getChild(component, "abcLabel", "{@link com.sun.rave.web.ui.component.util.factories.LabelFactory com.sun.rave.web.ui.component.util.factories.LabelFactory}", props);<br>
     *		<br>
     *		{@link LayoutComponent#encodeChild(FacesContext, UIComponent) LayoutComponent.encodeChild}(context, child);
     *	    </code></p>
     *
     *	@param	parent		Parent <code>UIComponent</code>
     *	@param	id		Identifier for the child <code>UIComponent</code>
     *	@param	factoryClass	Full {@link com.sun.rave.web.ui.component.util.factories.ComponentFactory} class name
     *	@param	properties	<code>java.util.Properties</code> needed to
     *				create and/or initialize the
     *				<code>UIComponent</code>
     *
     *	@return	The child UIComponent that was found or created.
     */
    public static UIComponent getChild(UIComponent parent, String id, String factoryClass, Properties properties) {
	return getChild(parent, id, factoryClass, properties, id);
    }

    /**
     *	<p> Same as {@link #getChild(UIComponent, String, String, Properties)}
     *	    except that it allows you to specify a facetName different than the
     *	    id.  If null is supplied, it won't save the component as a
     *	    facet.</p>
     *
     *	@param	parent		Parent <code>UIComponent</code>
     *	@param	id		Identifier for the child <code>UIComponent</code>
     *	@param	factoryClass	Full {@link com.sun.rave.web.ui.component.util.factories.ComponentFactory} class name
     *	@param	properties	<code>java.util.Properties</code> needed to
     *				create and/or initialize the
     *				<code>UIComponent</code>
     *	@param	facetName	The facet name (null means don't store it)
     *
     *	@return	The child UIComponent that was found or created.
     */
    public static UIComponent getChild(UIComponent parent, String id, String factoryClass, Properties properties, String facetName) {
	return getChild(parent, id, getComponentType(factoryClass), properties, facetName);
    }

    /**
     *	<p> This method finds or creates a child <code>UIComponent</code>
     *	    identified by the given id.  If the child is not found, it will
     *	    attempt to create it using the provided {@link ComponentType}
     *	    (<code>type</code>).  It will also initialize the
     *	    <code>UIComponent</code> using the provided set of
     *	    <code>properties</code>.</p>
     *
     *	@param	parent		Parent <code>UIComponent</code>
     *	@param	id		Identifier for the child
     *				<code>UIComponent</code>
     *	@param	type		The <code>ComponentType</code> class name
     *	@param	properties	Properties needed to create and/or initialize
     *				the <code>UIComponent</code>
     *	@param	facetName	The facet name (null means don't store it)
     *
     *	@return	The child <code>UIComponent</code> that was found or created.
     */
    private static UIComponent getChild(UIComponent parent, String id, ComponentType type, Properties properties, String facetName) {
	LayoutComponent desc = new LayoutComponent(null, id, type);
	if (properties != null) {
	    desc.setOptions(properties);
	}
	if (facetName != null) {
	    // Add the facetName to use
// FIXME: Decide if this should have its own method
	    desc.addOption(LayoutComponent.FACET_NAME, facetName);
	}

	return getChild(parent, desc);
    }

    /**
     *	<p> This method creates a {@link ComponentType} instance from the given
     *	    <code>factoryClass</code>.  It will first check its cache to see if
     *	    one has already been created.  If not, it will create one and add
     *	    to the cache for future use.</p>
     *
     *	<p> This method sets <code>factoryClass</code> for the
     *	    {@link ComponentType} id.</p>
     *
     *	@param	facatoryClass	The full classname of the
     *	    {@link com.sun.rave.web.ui.component.util.factories.ComponentFactory}.
     *
     *	@return	A ComponentType instance for <code>factoryClass</code>.
     */
    private static ComponentType getComponentType(String factoryClass) {
	// Check the cache
	ComponentType type = (ComponentType)_types.get(factoryClass);
	if (type == null) {
	    // Not in the cache... add it...
	    type = new ComponentType(factoryClass, factoryClass);
	    Map newMap = new HashMap(_types);
	    newMap.put(factoryClass, type);
	    _types = newMap;
	}

	// Return the ComponentType
	return type;
    }

    /**
     *	<p> This method finds or creates a child <code>UIComponent</code>
     *	    identified by the given id.  If the child is not found, it will
     *	    attempt to create it using the provided {@link LayoutComponent}
     *	    (<code>descriptor</code>).  It will also initialize the
     *	    <code>UIComponent</code> using the options set on the
     *	    {@link LayoutComponent}.</p>
     *
     *	<p> If <code>parent</code> implements {@link ChildManager}, then the
     *	    responsibility of finding and creating the child will be delegated
     *	    to the {@link ChildManager} <code>UIComponent</code>.</p>
     *
     *	<p> If you are constructing and populating a LayoutComponent before
     *	    calling this method, there are a few features that should be noted.
     *	    Besides <code>id</code> and <code>type</code> which can be set in
     *	    the LayoutComponent constructor, you can also set
     *	    <code>options</code>, and
     *	    {@link com.sun.rave.web.ui.component.util.event.Handler}'s.</p>
     *
     *	<p> <code>Options</code> may be set via
     *	    {@link LayoutComponent#setOptions(Map)}.  These options will be
     *	    applied to the <code>UIComponent</code> and may also be used by the
     *	    {@link com.sun.rave.web.ui.component.util.factories.ComponentFactory} while instantiating the
     *	    <code>UIComponent</code>.</p>
     *
     *	<p> {@link com.sun.rave.web.ui.component.util.event.Handler}'s can be
     *	    supplied by calling
     *	    {@link LayoutComponent#setHandlers(String, List)}.  The
     *	    <code>type</code> must match the event name which invokes the
     *	    <code>List</code> of handlers you provide.  The
     *	    <code>Renderer</code> for this <code>UIComponent</code> is
     *	    responsible for declaring and dispatching events.
     *	    {@link com.sun.rave.web.ui.renderer.template.TemplateRenderer} will
     *	    invoke <code>beforeCreate</code> and <code>afterCreate</code>
     *	    events for each child it creates (such as the one being requested
     *	    here).</p>
     *
     *	<p> <code>
     *		// Example (with LayoutComponent):<br>
     *		{@link ComponentType} type = new {@link ComponentType#ComponentType(String, String) ComponentType}("LabelFactory", "{@link com.sun.rave.web.ui.component.util.factories.LabelFactory com.sun.rave.web.ui.component.util.factories.LabelFactory}");<br>
     *		{@link LayoutComponent} descriptor = new {@link LayoutComponent#LayoutComponent(LayoutElement, String, ComponentType) LayoutComponent}(null, "abcLabel", type);<br>
     *		{@link LayoutComponent#addOption(String, Object) descriptor.addOption}("text", "ABC Label:");<br>
     *		{@link LayoutComponent#addOption(String, Object) descriptor.addOption}("for", "abc");<br>
     *		UIComponent child = Util.getChild(component, descriptor);<br>
     *		<br>
     *		{@link LayoutComponent#encodeChild(FacesContext, UIComponent) LayoutComponent.encodeChild}(context, child);
     *	    </code></p>
     *
     *	@param	parent		Parent <code>UIComponent</code>
     *	@param	descriptor	The {@link LayoutComponent} describing the
     *				<code>UIComponent</code>
     *
     *	@return	The child <code>UIComponent</code> that was found or created.
     */
    public static UIComponent getChild(UIComponent parent, LayoutComponent descriptor) {
	FacesContext context = FacesContext.getCurrentInstance();
	// First check to see if the UIComponent can create its own children
	if (parent instanceof ChildManager) {
	    return ((ChildManager)parent).getChild(
		context, descriptor);
	}

	// Make sure it doesn't already exist
	String childId = descriptor.getId(context, parent);
	UIComponent childComponent = findChild(parent, childId,
	    (String) descriptor.getEvaluatedOption(
		context, LayoutComponent.FACET_NAME, null));
	if (childComponent != null) {
	    return childComponent;
	}

	// Not found, create a new UIComponent
	return createChildComponent(context, descriptor, parent);
    }

    /**
     *	<p> This method creates a child <code>UIComponent</code> by using the
     *	    provided {@link LayoutComponent} (<code>descriptor</code>).  It
     *	    will associate the parent and the newly created
     *	    <code>UIComponent</code>.</p>
     *
     *	<p> It is recommended that this method NOT be called from a Renderer.
     *	    It should not be called if you have not yet checked to see if a
     *	    child UIComponent with the requested ID already exists.</p>
     *
     *	@param	context	    The <code>FacesContext</code> object.
     *	@param	descriptor  The {@link LayoutComponent} describing the
     *			    <code>UIComponent</code> to be created.
     *	@param	parent	    Parent <code>UIComponent</code>.
     *
     *	@return	A new <code>UIComponent</code> based on the provided
     *		{@link LayoutComponent}.
     *
     *	@throws	IllegalArgumentException    Thrown if descriptor equals null.
     *
     *	@see	#getChild(UIComponent, LayoutComponent)
     *	@see	#getChild(UIComponent, String, String, Properties)
     *	@see	LayoutComponent#getType()
     *	@see	ComponentType#getFactory()
     *	@see	com.sun.rave.web.ui.component.util.factories.ComponentFactory#create(FacesContext, LayoutComponent, UIComponent)
     */
    public static UIComponent createChildComponent(FacesContext context, LayoutComponent descriptor, UIComponent parent) {
	//  Make sure a LayoutComponent was provided.
	if (descriptor == null) {
	    throw new IllegalArgumentException("'descriptor' cannot be null!");
	}

	// Create & return the child UIComponent
	return descriptor.getType().getFactory().create(
		    context, descriptor, parent);
    }

    /**
     *	<p> This util method will set the given key/value on the
     *	    <code>UIComponent</code>.  It will resolve all $...{...}
     *	    expressions, and convert the String into a
     *	    <code>ValueBinding</code> if a <code>ValueBinding</code> is
     *	    detected.  The return value will be a <code>ValueBinding</code> or
     *	    the value.</p>
     *
     *	@param	context	    <code>FacesContext</code>
     *	@param	key	    The Property name to set
     *	@param	value	    The Property value to set
     *	@param	desc	    The {@link LayoutElement} associated with the
     *			    <code>UIComponent</code>
     *	@param	component   The <code>UIComponent</code>
     *
     *	@return A ValueBinding, or the evaulated value (if no value binding is
     *		present).
     */
    public static Object setOption(FacesContext context, String key, Object value, LayoutElement desc, UIComponent component) {
	// Invoke our own EL.  This is needed b/c JSF's EL is designed for
	// Bean getters only.  It does not get CONSTANTS or pull data from
	// other sources (such as session, request attributes, etc., etc.)
	// Resolve our variables now because we cannot depend on the
	// individual components to do this.  We may want to find a way to
	// make this work as a regular ValueBinding expression... but for
	// now, we'll just resolve it here.
	value = VariableResolver.resolveVariables(
		context, desc, component, value);
	if (value == null) {
	    // It is possible to resolve an expression to null
	    return null;
	}

	// Next check to see if the value contains a JSF ValueBinding
	String strVal = value.toString();
	if (UIComponentTag.isValueReference(strVal)) {
	    ValueBinding vb =
		context.getApplication().createValueBinding(strVal);
	    if (component != null) {
		component.setValueBinding(key, vb);
	    }
	    value = vb;
	} else {
	    // In JSF, you must directly modify the attribute Map
	    if (component != null) {
		component.getAttributes().put(key, value);
	    }
	}
	return value;
    }

    /**
     *	<p>Helper method to obtain containing <code>UIForm</code>.</p>
     *
     *	@param	context	    <code>FacesContext</code> for the request we are
     *			    processing.
     *	@param	component   <code>UIComponent</code> to find form from.
     *
     *	@return	Returns the <code>UIForm</code> component that contains this element
     */
    public static UIComponent getForm(FacesContext context, UIComponent component) {
	//make sure component is not null
	if (component != null) {
	    //make sure we don't already have a form
	    if (component instanceof UIForm) {
		return component;
	    }
	    
	    UIComponent form = component;
	    do {
		form = form.getParent();
		if (form != null && form instanceof UIForm) {
		    return form;
		}
	    } while (form != null);
	}
	return null;
    }

    /**
     *	<p>Gets the form id from the containing <code>UIForm</code>.</p>
     *
     *	@param	context	    FacesContext for the request we are processing.
     *	@param	component   UIComponent to find form from.
     *
     *	@return Returns the id of the <code>UIForm</code> that contains this
     *		element.
     */
    public static String getFormName(FacesContext context, UIComponent component) {
	UIComponent form = getForm(context, component);
	if (form != null) {
	    return form.getClientId(context);
	}
	return null;
    }

    /**
     *	<p> This method will attempt to resolve EL strings in the given
     *	    value.</p>
     *
     *	@param	context	    The <code>FacesContext</code>
     *	@param	elt	    The LayoutElement associated w/ the expression
     *	@param	parent	    The parent <code>UIComponent</code>.  This is used
     *			    because the current UIComponent is typically
     *			    unknown (or not even created yet).
     *	@param	value	    The String to resolve
     *
     *	@return The evaluated value (may be null).
     */
    public static Object resolveValue(FacesContext context, LayoutElement elt, UIComponent parent, String value) {
	// Invoke our own EL.  This is needed b/c JSF's EL is designed for
	// Bean getters only.  It does not get CONSTANTS or pull data from
	// other sources without adding a custom VariableResolver and/or
	// PropertyResolver.  Eventually we may want to find a good way to
	// make this work as a regular ValueBinding expression... but for
	// now, we'll just resolve it this way.
	Object result = VariableResolver.resolveVariables(
	    context, elt, parent, value);

	// Next check to see if the value contains a JSF ValueBinding
	if (result != null) {
	    String strVal = result.toString();
	    if (UIComponentTag.isValueReference(strVal)) {
		ValueBinding vb = context.getApplication().createValueBinding(strVal);
		result = vb.getValue(context);
	    }
	}

	// Return the result
	return result;
    }

   /**
     * <p>Return the base URI for the view identifier of the current view.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    public static String getBase(FacesContext context) {

        return getContext(context) + context.getViewRoot().getViewId();

    }


    /**
     * <p>Return an absolute URL to our server and context path.</p>
     *
     * @param context <code>FacesContext</code> for the current request
     */
    public static String getContext(FacesContext context) {

        // FIXME - design time FacesContext needs better instrumentaiton?
        if (Beans.isDesignTime()) {
            return "http://localhost:18080/myapp";
        }

        // FIXME - portlet environment variation?
        HttpServletRequest request = (HttpServletRequest)
            context.getExternalContext().getRequest();
        StringBuffer sb = new StringBuffer(request.getScheme());
        sb.append("://"); //NOI18N
        sb.append(request.getServerName());
        if ("http".equals(request.getScheme()) && //NOI18N
            (80 == request.getServerPort())) {
            ;
        } else if ("https".equals(request.getScheme()) && //NOI18N
                   (443 == request.getServerPort())) {
            ;
        } else {
            sb.append(":" + request.getServerPort()); //NOI18N
        }
        sb.append(request.getContextPath());
        return sb.toString();

    }
 
       
    public static String getActionURL(FacesContext context, String url) {
        return context.getApplication().getViewHandler()
                    .getActionURL(context, url);
    }

    /**
     *	<p> Return whether the given <code>UIComponent</code> is "visible".
     *	    If the property is null, it will return true.  Otherwise the value
     *	    of the property is returned.</p>
     *
     * @param	component   The <code>UIComponent</code> to check
     *
     * @return	True if the property is null or true, false otherwise.
     */
     public static boolean isVisible(UIComponent component) {
        Object visible = component.getAttributes().get("visible"); //NOI18N
        if (visible == null)
            return true;
        else 
            return ((Boolean) visible).booleanValue(); 
    }

    /**
     * Add a PhaseListener.
     *
     * @param phaseListener PhaseListener instance.
     */
    public static void addPhaseListener(PhaseListener phaseListener) {

        LifecycleFactory factory = (LifecycleFactory)
            FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle =
            factory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
	lifecycle.addPhaseListener(phaseListener);
    }

    /**
     * Remove a PhaseListener.
     *
     * @param phaseListener PhaseListener instance.
     */
    public static void removePhaseListener(PhaseListener phaseListener) {

        LifecycleFactory factory = (LifecycleFactory)
            FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        Lifecycle lifecycle =
            factory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
	lifecycle.removePhaseListener(phaseListener);
    }

    /**
     *
     *	<p>This Map caches ComponentTypes by their factoryClass name.</p>
     */
    private static Map	_types	= new HashMap();

}
