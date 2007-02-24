/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.uml.core.eventframework;

import org.dom4j.Node;

public interface IEventContext
{
	/**
	 * Sets / Gets the EventFilter for this Context.
	*/
	public IEventFilter getFilter();

	/**
	 * Sets / Gets the EventFilter for this Context.
	*/
	public void setFilter( IEventFilter value );

	/**
	 * Sets / Gets the XML node this context represents.
	*/
	public Node getNode();
	public org.dom4j.Node getDom4JNode();

	/**
	 * Sets / Gets the XML node this context represents.
	*/
	public void setNode( Node value );
	public void setDom4JNode( org.dom4j.Node value );

	/**
	 * Validates the trigger and payload about to be dispatched.
	*/
	public boolean validateEvent( String triggerName, Object payLoad );

	/**
	 * The name of this EventContext.
	*/
	public String getName();

	/**
	 * The name of this EventContext.
	*/
	public void setName( String value );

	/**
	 * User-defined data associated with this Context.
	*/
	public Object getData();

	/**
	 * User-defined data associated with this Context.
	*/
	public void setData( Object value );

}
