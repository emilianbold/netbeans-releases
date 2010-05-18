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
import com.sun.rave.web.ui.component.util.event.AfterEncodeEvent;
import com.sun.rave.web.ui.component.util.event.BeforeEncodeEvent;
import com.sun.rave.web.ui.component.util.event.EncodeEvent;
import com.sun.rave.web.ui.component.util.event.Handler;
import com.sun.rave.web.ui.component.util.event.HandlerContext;
import com.sun.rave.web.ui.component.util.event.HandlerContextImpl;
import com.sun.rave.web.ui.util.RenderingUtilities;
import com.sun.rave.web.ui.util.VariableResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;


/**
 *  <p>This class provides some common functionality between the various types
 *  of LayoutElements.  It is the base class of most implementations (perhaps
 *  all).</p>
 *
 *  @author Ken Paulsen (ken.paulsen@sun.com)
 */
public abstract class LayoutElementBase implements LayoutElement {

    /**
     *	Constructor
     *
     *	@param	parent	The parent LayoutElement
     *	@param	id	Identifier for this LayoutElement
     */
    protected LayoutElementBase(LayoutElement parent, String id) {
	setParent(parent);
	_id = id;
    }


    /**
     *	This method is used to add a LayoutElement.  LayoutElements should be
     *	added sequentially in the order in which they are to be rendered.
     *
     *	@param	element	The LayoutElement to add as a child
     */
    public void addChildLayoutElement(LayoutElement element) {
	_layoutElements.add(element);
    }


    /**
     *	This method returns the LayoutElements as a List.
     *
     *	@return List of LayoutElements
     */
    public List getChildLayoutElements() {
	return _layoutElements;
    }


    /**
     *	This method walks to the top-most LayoutElement, which should be a
     *	LayoutDefinition.  If not, it will throw an Exception.
     *
     *	@return	The LayoutDefinition
     */
    public LayoutDefinition getLayoutDefinition() {
	// Find the top-most LayoutElement
	LayoutElement cur = this;
	while (cur.getParent() != null) {
	    cur = cur.getParent();
	}

	// This should be the LayoutDefinition, return it
	return (LayoutDefinition)cur;
    }


    /**
     *	This method returns the parent LayoutElement.
     *
     *	@return	parent LayoutElement
     */
    public LayoutElement getParent() {
	return _parent;
    }


    /**
     *	This method sets the parent LayoutElement.
     *
     *	@param	parent	Parent LayoutElement
     */
    protected void setParent(LayoutElement parent) {
	_parent = parent;
    }


    /**
     *	<p> Accessor method for id.  This returns a non-null value, it may
     *	    return "" if id is not set or does not apply.</p>
     *
     *	<p> This method will also NOT resolve EL strings.</p>
     *
     *	@return a non-null id
     */
    private String getId() {
	if (_id == null) {
	    return "";
	}
	return _id;
    }

    /**
     *	<p> This method generally should not be used.  It does not resolve
     *	    expressions.  Instead use
     *	    {@link #getId(FacesContext, UIComponent)}.</p>
     *
     *	@return	The unevaluated id.
     */
    public String getUnevaluatedId() {
	return _id;
    }

    /**
     *	<p> Accessor method for id.  This returns a non-null value, it may
     *	    return "" if id is not set or does not apply.</p>
     *
     *	<p> This method will also attempt to resolve EL strings.</p>
     *
     *	@param	context	    The <code>FacesContext</code>
     *	@param	parent	    The parent <code>UIComponent</code>.  This is used
     *			    because the current UIComponent is typically
     *			    unknown (or not even created yet).
     *
     *	@return a non-null id
     */
    public String getId(FacesContext context, UIComponent parent) {
	// Evaluate the id...
	Object value = resolveValue(context, parent, getId());

	// Return the result
	return (value == null) ? "" : value.toString();
    }

    /**
     *	<p> This method will attempt to resolve EL strings in the given
     *	    value.</p>
     *
     *	@param	context	    The <code>FacesContext</code>
     *	@param	parent	    The parent <code>UIComponent</code>.  This is used
     *			    because the current UIComponent is typically
     *			    unknown (or not even created yet).
     *	@param	value	    The String to resolve
     *
     *	@return The evaluated value (may be null).
     */
    public Object resolveValue(FacesContext context, UIComponent parent, String value) {
	return Util.resolveValue(context, this, parent, value);
    }

    /**
     *	<p> This method allows each LayoutElement to provide it's own encode
     *	    functionality.  If the <code>LayoutElement</code> should render its
     *	    children, this method should return true.  Otherwise, this method
     *	    should return false.</p>
     *
     *	@param	context	    The FacesContext
     *	@param	component   The UIComponent
     *
     *	@return	true if children are to be rendered, false otherwise.
     */
    protected abstract boolean encodeThis(FacesContext context, UIComponent component) throws IOException;

    /**
     *	<p> This is the base implementation for encode.  Typically each type of
     *	    LayoutElement wants to do something specific then conditionally have
     *	    its children rendered.  This method invokes the abstract method
     *	    "encodeThis" to do specific functionality, it the walks the children
     *	    and renders them, if encodeThis returns true.  It skips the children
     *	    if encodeThis returns false.</p>
     *
     *	@param	context	    The <code>FacesContext</code>
     *	@param	component   The <code>UIComponent</code>
     */
    public void encode(FacesContext context, UIComponent component) throws IOException {
	// Invoke "before" handlers
// FIXME: Consider true/false for skipping component
	Object result = dispatchHandlers(context, BEFORE_ENCODE,
	    new BeforeEncodeEvent(component));

	// Do LayoutElement specific stuff...
	boolean renderChildren = encodeThis(context, component);

// FIXME: Consider buffering HTML and passing to "endDisplay" handlers...
// FIXME: Storing in the EventObject may be useful if we go this route.
	// Perhaps we want our own Response writer to buffer children?
	//ResponseWriter out = context.getResponseWriter();

	// Conditionally render children...
	if (renderChildren) {
	    result = dispatchHandlers(context, ENCODE,
		new EncodeEvent(component));

	    // Iterate over children
	    LayoutElement childElt = null;
	    Iterator it = getChildLayoutElements().iterator();
	    while (it.hasNext()) {
		childElt = (LayoutElement)it.next();
		childElt.encode(context, component);
	    }
	}

	// Invoke "after" handlers
	result = dispatchHandlers(context, AFTER_ENCODE,
	    new AfterEncodeEvent(component));
    }


    /**
     *	<p> This method iterates over the handlers and executes each one.  A
     *	    HandlerContext will be created to pass to each Handler.  The
     *	    HandlerContext object is reused across all Handlers that are
     *	    invoked; the setHandler(Handler) method is invoked with the
     *	    correct Handler descriptor before the handler is executed.</p>
     *
     *	@param	context	    The FacesContext
     *	@param	eventType   The event type which is being fired
     *	@param	event	    An optional EventObject providing more detail
     *
     *	@return	By default, (null) is returned.  However, if any of the
     *		handlers produce a non-null return value, then the value from
     *		the last handler to produces a non-null return value is
     *		returned.
     */
    public Object dispatchHandlers(FacesContext context, String eventType, EventObject event) {
	// Get the handlers for this eventType
	Object eventObj = event.getSource();
	if (!(eventObj instanceof UIComponent)) {
	    eventObj = null;
	}
	List handlers = getHandlers(eventType, (UIComponent) eventObj);

	// Make sure we have something to do...
	if (handlers == null) {
	    return null;
	}

	// Create a HandlerContext
	HandlerContext handlerContext =
	    createHandlerContext(context, event, eventType);

	// This method is broken down so that recursion is easier
	return dispatchHandlers(handlerContext, handlers);
    }

    /**
     *	<p> As currently implemented, this method is essentially a utility
     *	    method.  May want to rethink this.</p>
     */
    public Object dispatchHandlers(HandlerContext handlerCtx, List handlers) {
	Object retVal = null;
	Object result = null;
	Handler handler = null;
	Iterator it = handlers.iterator();
	while (it.hasNext()) {
	    try {
		// Get the Handler
		handler = (Handler) it.next();
		handlerCtx.setHandler(handler);

		// Delegate to the Handler to perform invocation
		retVal = handler.invoke(handlerCtx);

		// Check for return value
		if (retVal != null) {
		    result = retVal;
		}
	    } catch (Exception ex) {
		throw new RuntimeException(
		    ex.getClass().getName() + " while attempting to " +
		    "process a '" + handlerCtx.getEventType() + "' event for '" +
		    getId() + "'.", ex);
	    }
	}

	// Return the return value (null by default)
	return result;
    }

    /**
     *	<p> This method is responsible for creating a new HandlerContext.  It
     *	    does not set the Handler descriptor.  This is done right before a
     *	    Handler is invoked.  This allows the HandlerContext object to be
     *	    reused.</p>
     *
     *	@param	context	    The FacesContext
     */
    protected HandlerContext createHandlerContext(FacesContext context, EventObject event, String eventType) {
	return new HandlerContextImpl(context, this, event, eventType);
    }

    /**
     *	<p> This method retrieves the Handlers for the requested type.</p>
     *
     *	@param	type	The type of Handlers to retrieve.
     *
     *	@return	A List of Handlers.
     */
    public List getHandlers(String type) {
	return (List) _handlersByType.get(type);
    }

    /**
     *	<p> This method provides access to the "handlersByType"
     *	    <code>Map</code>.</p>
     */
    public Map getHandlersByTypeMap() {
	return _handlersByType;
    }

    /**
     *	<p> This method provides a means to set the "handlersByType" Map.
     *	    Normally this is done for each type individually via
     *	    {@link #setHandlers(String, List)}.  This Map may not be null (null
     *	    will be ignored) and should contain entries that map to
     *	    <code>List</code>s of {@link Handler}s.
     */
    public void setHandlersByTypeMap(Map map) {
	if (map != null) {
	    _handlersByType = map;
	}
    }

    /**
     *	<p> This method retrieves the Handlers for the requested type.  But
     *	    also includes any handlers that are associated with the instance
     *	    (i.e. the UIComponent).</p>
     *
     *	@param	type	The type of <code>Handler</code>s to retrieve.
     *	@param	event	The associated <code>UIComponent</code> (or null).
     *
     *	@return	A List of Handlers.
     */
    public List getHandlers(String type, UIComponent comp) {
	// 1st get list of handlers for definition of this LayoutElement
	List handlers = null;

	// Now check to see if there are any on the UIComponent
	if (comp != null) {
	    List instHandlers = (List) comp.getAttributes().get(type);
	    if ((instHandlers != null) && (instHandlers.size() > 0)) {
		// NOTE: Copy b/c this is <i>instance</i> + static
		// Add the UIComponent instance handlers
		handlers = new ArrayList(instHandlers);

		List defHandlers = getHandlers(type);
		if (defHandlers != null) {
		    // Add the LayoutElement "definition" handlers, if any
		    handlers.addAll(getHandlers(type));
		}
	    }
	}
	if (handlers == null) {
	    handlers = getHandlers(type);
	}

	return handlers;
    }

    /**
     *	<p> This method associates 'type' with the given list of Handlers.</p>
     *
     *	@param	type	    The String type for the List of Handlers
     *	@param	handlers    The List of Handlers
     */
    public void setHandlers(String type, List handlers) {
	_handlersByType.put(type, handlers);
    }

    /**
     *	<p> This method is a convenience method for encoding the given
     *	    <code>UIComponent</code>.  It calls the appropriate encoding
     *	    methods on the component and calls itself recursively for all
     *	    <code>UIComponent</code> children that do not render their own
     *	    children.</p>
     *
     *	@param	context	    <code>FacesContext</code>
     *	@param	component   <code>UIComponent</code> to encode
     */
    public static void encodeChild(FacesContext context, UIComponent component) throws IOException {
       RenderingUtilities.renderComponent(component, context);
    }


    /**
     *	List of renderable elements (if, facet, UIComponents)
     */
    private List _layoutElements = new ArrayList();


    /**
     *	The parent LayoutElement.  This will be null for the LayoutDefinition.
     */
    private LayoutElement _parent = null;

    /**
     *	Map containing Lists of Handlers
     */
    private Map _handlersByType = new HashMap();

    /**
     *	This stores the id for the LayoutElement
     */
    private String  _id	    = null;

    /**
     *	<p> This is the "type" for handlers to be invoked after the encoding
     *	    of this element.</p>
     */
     public static final String AFTER_ENCODE =	"afterEncode";

    /**
     *	<p> This is the "type" for handlers to be invoked before the encoding
     *	    of this element.</p>
     */
     public static final String BEFORE_ENCODE =	"beforeEncode";

    /**
     *	<p> This is the "type" for handlers to be invoked during the encoding
     *	    of this element.  This occurs before any child LayoutElements are
     *	    invoked and only if child Elements are to be invoked.</p>
     */
     public static final String ENCODE =	"encode";
}
