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
package com.sun.rave.web.ui.component.util.factories;

import com.sun.rave.web.ui.component.util.descriptors.LayoutComponent;
import com.sun.rave.web.ui.component.util.descriptors.LayoutElement;
import com.sun.rave.web.ui.component.util.descriptors.LayoutFacet;
import com.sun.rave.web.ui.util.LogUtil;
import com.sun.rave.web.ui.util.VariableResolver;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;


/**
 *  <p>	This abstract class provides common functionality for UIComponent
 *	factories.</p>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public abstract class ComponentFactoryBase implements ComponentFactory {

    /**
     *	<p> This method iterates through the Map of options.  It looks at each
     *	    one, if it contians an EL expression, it sets a value binding.
     *	    Otherwise, it calls setAttribute() on the component (which in turn
     *	    will invoke the bean setter if there is one).</p>
     *
     *	<p> This method also interates through the child {@link LayoutElement}s
     *	    of the given {@link LayoutComponent} descriptor and adds Facets or
     *	    children as appropriate.</p>
     *
     *	@param	context	The <code>FacesContext</code>
     *	@param	desc    The {@link LayoutComponent} descriptor associated with
     *			the requested <code>UIComponent</code>.
     *	@param	comp	The <code>UIComponent</code>
     */
    protected void setOptions(FacesContext context, LayoutComponent desc, UIComponent comp) {
	// First set the id if supplied, treated special b/c the component
	// used for ${} expressions is the parent and this must be set first
	// so other ${} expressions can use $this{id} and $this{clientId}.
	String compId = (String) desc.getId(context, comp.getParent());
	if ((compId != null) && (!compId.equals(""))) {
	    comp.setId(compId);
	}

	// Loop through all the options and set the values
	Map attributes = comp.getAttributes();
// FIXME: Figure out a way to skip options that should not be set on the Component
	Iterator it = desc.getOptions().keySet().iterator();
	Object value = null;
	String strVal = null;
	String key = null;
	while (it.hasNext()) {
	    // Get next property
	    key = (String)it.next();
	    value = desc.getEvaluatedOption(context, key, comp);

	    // Next check to see if the value contains a JSF ValueBinding
	    strVal = ""+value;
	    if (UIComponentTag.isValueReference(strVal)) {
		ValueBinding vb =
		    context.getApplication().createValueBinding(strVal);
		comp.setValueBinding((String)key, vb);
	    } else {
		// In JSF, you must directly modify the attribute Map
		try {
		    attributes.put(key, value);
		} catch (NullPointerException ex) {
		    // Setting null, assume they want to remove the value
		    attributes.remove(key);
		}
	    }
	}

	// Set the events on the new component
	storeInstanceHandlers(desc, comp);
    }

    /**
     *	<p> This method is responsible for interating over the "instance"
     *	    handlers and applying them to the UIComponent.  An "instance"
     *	    handler is one that is defined <b>outside a renderer</b>, or <b>a
     *	    nested component within a renderer</b>.  In other words, a handler
     *	    that would not get fired by the TemplateRenderer.  By passing this
     *	    in via the UIComponent, code that is aware of events (see
     *	    {@link com.sun.rave.web.ui.component.util.descriptors.LayoutElementBase})
     *	    may find these events and fire them.  These may vary per "instance"
     *	    of a particular component (i.e. <code>TreeNode</code>) unlike the
     *	    handlers defined in a TemplateRender's XML (which are shared and
     *	    therefor should not change dynamically).</p>
     *
     *	@param	desc	The descriptor potentially containing handlers to copy.
     *	@param	comp	The UIComponent instance to store the handlers.
     */
    protected void storeInstanceHandlers(LayoutComponent desc, UIComponent comp) {
	// Iterate over the instance handlers
	Iterator it = desc.getHandlersByTypeMap().keySet().iterator();
	if (it.hasNext()) {
	    String eventType = null;
	    Map compAttrs = comp.getAttributes();
	    while (it.hasNext()) {
		// Assign instance handlers to attribute for retrieval later
		//   (NOTE: retrieval must be explicit, see LayoutElementBase)
		eventType = (String) it.next();
		compAttrs.put(eventType, desc.getHandlers(eventType));
	    }
	}
    }

    /**
     *	<p> This method associates the given child with the given parent.  By
     *	    using this method we centralize the code so that if we decide
     *	    later to add it as a real child it can be done in one place.</p>
     *
     *	@param	context	    The <code>FacesContext</code>
     *	@param	descriptor  The {@link LayoutComponent} descriptor associated
     *			    with the requested <code>UIComponent</code>.
     *	@param	parent	    The parent <code>UIComponent</code>
     *	@param	child	    The child <code>UIComponent</code>
     */
    protected void addChild(FacesContext context, LayoutComponent descriptor, UIComponent parent, UIComponent child) {
	if (descriptor.isFacetChild()) {
	    String name = (String) descriptor.getEvaluatedOption(
		    context, LayoutComponent.FACET_NAME, child);
	    if (name != null) {
		parent.getFacets().put(name, child);
	    } else {
		// Warn the developer that they may have a problem
		if (LogUtil.configEnabled()) {
		    LogUtil.config("Warning: no facet name was supplied for '" +
			    descriptor.getId(context, child) + "'!");
		}

		// Set the parent
		child.setParent(parent);
	    }
	} else {
	    // Add this as an actual child
	    parent.getChildren().add(child);
	}
    }

    /**
     *	<p> This method walks the child {@link LayoutElement}s and adds them as
     *	    <code>Facet</code>s or child <code>UIComponent</code>s as
     *	    appropriate.</p>
     *
     *	@param	context	    The FacesContext
     *	@param	desc	    The {@link LayoutComponent} descriptor that is
     *			    associated with the requested UIComponent.
     *	@param	component   The UIComponent
    private void addChildren(FacesContext context, LayoutComponent desc, UIComponent component) {
	Iterator it = desc.getChildLayoutElements().iterator();
	LayoutElement child = null;
	while (it.hasNext()) {
	    child = (LayoutElement)it.next();
	    // FIXME: Don't use LayoutFacet... use a new type that does the adding in its "encode" method
	    if (child instanceof LayoutFacet) {
		// Found a facet for the UIComponent... add it
		// FIXME: Will need to get the *default* from the LayoutFacet
		// FIXME: (which is it's children)... wrap in a PanelGroup if
		// FIXME: necessary, and add it to "component" as a Facet
		// FIXME: using the LayoutFacet's id.
		// FIXME:
		// FIXME: The problem w/ this is that I need to separate out
		// FIXME: creation from rendering in the "encodeThis"
		// FIXME: section... also, I may need to accurately reflect the
		// FIXME: hierarchy (I knew I should have done this right from
		// FIXME: the start).
	    // FIXME: Don't use LayoutComponent... use a new type that does the adding in its "encode" method
	    } else if (child instanceof LayoutComponent) {
		// Found a child component... add it
		// FIXME: See above comment
	    }
	}
    }
     */


    /**
     *	<p> This method is to be used by subclasses to retrieve a pre-evalated
     *	    option by name.  Since this is in the context of a factory, these
     *	    options usually needed during the creation of the UIComponent.
     *	    Therefor the parent UIComponent is typically passed in because of
     *	    the lack of existence of the child UIComponent.</p>
     *
     *	<p> This method does not currently resolve #{} expressions.</p>
     *
     *	@param	context	    The FacesContext
     *
     *	@param	desc	    The LayoutComponent descriptor holding the options
     *
     *	@param	parent	    Usually the parent UIComponent, only used when
     *			    resolving $...{...} expressions
     *
     *	@param	key	    The option name to pull from <code>desc</code>
     *
     *	@param	required    A flag indicating if a value for the property must
     *			    exist.  If true and the property value is null, an
     *			    IllegalArgumentException will be thrown.
     *
     *	@return The evaluated option, null if not found.
     *
     *	@throws	IllegalArgumentException    Thrown if property is required but
     *			    has a null value (or not found).
     *
     *	@deprecated	I *think* only the LayoutComponent version of this should be used...
     */
    protected Object getEvaluatedOption(FacesContext context, LayoutComponent desc, UIComponent parent, String key, boolean required) {
	// Get the option
	Object val = desc.getOption(key);
	if (val != null) {
	    if (val instanceof List) {
		// Evaluate each item in the List
		Object args[] = ((List)val).toArray();
		for (int count=0; count<args.length; count++) {
		    args[count] = VariableResolver.resolveVariables(
			    context, desc, parent, (String)args[count]);
		}
		val = args;
	    } else {
		// Resolve it...
		val = VariableResolver.resolveVariables(
			context, desc, parent, val);
	    }
	}
	if (required && (val == null)) {
	    // Required but no value!
	    throw new IllegalArgumentException("'"+key+"' is required!");
	}
	return val;
    }
}
