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
package com.sun.rave.web.ui.component.util.event;

import com.sun.rave.web.ui.component.util.descriptors.LayoutElement;

import java.util.EventObject;

import javax.faces.context.FacesContext;


/**
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class HandlerContextImpl implements HandlerContext {

    /**
     *	Constructor
     */
    public HandlerContextImpl(FacesContext context, LayoutElement layoutDesc, EventObject event, String eventType) {
	_facesContext = context;
	_layoutDesc = layoutDesc;
	_event = event;
	_eventType = eventType;
    }

    /**
     *	<P> Constructor that gets all its values from the given
     *	    HandlerContext.</P>
     *
     *	@param	context	The HandlerContext to clone.
     */
    public HandlerContextImpl(HandlerContext context) {
	_facesContext = context.getFacesContext();
	_layoutDesc = context.getLayoutElement();
	_event = context.getEventObject();
	_eventType = context.getEventType();
	_handler = context.getHandler();
    }

    /**
     *	<P> Accessor for the FacesContext.</P>
     *
     *	@return FacesContext
     */
    public FacesContext getFacesContext() {
	return _facesContext;
    }

    /**
     *	<P> Accessor for the LayoutElement associated with this Handler.</P>
     */
    public LayoutElement getLayoutElement() {
	return _layoutDesc;
    }

    /**
     *	<P> Accessor for the EventObject associated with this Handler.  This
     *	    may be null if an EventObject was not created for this handler.
     *	    An EventObject, if it does exist, may provide additional details
     *	    describing the context in which this Event is invoked.</P>
     */
    public EventObject getEventObject() {
	return _event;
    }

    /**
     *	<P> This method provides access to the EventType.  This is mostly
     *	    helpful for diagnostics, but may be used in a handler to determine
     *	    more information about the context in which the code is
     *	    executing.</P>
     */
    public String getEventType() {
	return _eventType;
    }

    /**
     *	<P> Accessor for the Handler descriptor for this Handler.  The Handler
     *	    descriptor object contains specific meta information describing
     *	    the invocation of this handler.  This includes details such as
     *	    input values, and where output values are to be set.</P>
     */
    public Handler getHandler() {
	return _handler;
    }

    /**
     *	<P> Setter for the Handler descriptor for this Handler.</P>
     */
    public void setHandler(Handler handler) {
	_handler = handler;
    }

    /**
     *	<P> Accessor for the Handler descriptor for this Handler.  The
     *	    HandlerDefinition descriptor contains meta information about the
     *	    actual Java handler that will handle the processing.  This
     *	    includes the inputs required, outputs produces, and the types for
     *	    both.</P>
     */
    public HandlerDefinition getHandlerDefinition() {
	return _handler.getHandlerDefinition();
    }

    /**
     *	<P> This method returns the value for the named input.  Input values
     *	    are not stored in this HandlerContext itself, but in the Handler.
     *	    If you are trying to set input values for a handler, you must
     *	    create a new Handler object and set its input values.</P>
     *
     *	<P> This method attempts to resolve $...{...} expressions.  It also
     *	    will return the default value if the value is null.  If you don't
     *	    want these things to happen, look at
     *	    Handler.getInputValue(String).</P>
     *
     *	@param	name	    The input name
     *
     *	@return	The value of the input (null if not found)
     */
    public Object getInputValue(String name) {
	return getHandler().getInputValue(this, name);
    }

    /**
     *	<P> This method retrieves an Output value. Output values must not be
     *	    stored in this Context itself (remember HandlerContext objects
     *	    are shared).  Output values are stored according to what is
     *	    specified in the HandlerDefintion.</P>
     *
     *	@param	name	    The output name
     *
     *	@return	The value of the output (null if not found)
     */
    public Object getOutputValue(String name) {
	return getHandler().getOutputValue(this, name);
    }

    /**
     *	<P> This method sets an Output value. Output values must not be
     *	    stored in this Context itself (remember HandlerContext objects
     *	    are shared).  Output values are stored according to what is
     *	    specified in the HandlerDefintion.</P>
     */
    public void setOutputValue(String name, Object value) {
	getHandler().setOutputValue(this, name, value);
    }

    private String		_eventType	= null;
    private FacesContext	_facesContext   = null;
    private LayoutElement	_layoutDesc	= null;
    private EventObject		_event		= null;
    private Handler		_handler	= null;
}
