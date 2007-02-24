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


package org.netbeans.modules.uml.core.metamodel.core.foundation;

import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.support.umlutils.ETList;


public interface IElementLifeTimeEventDispatcher extends IEventDispatcher
{
	/**
	 * Registers an event sink to handle element lifetime events.
	*/
	public void registerForLifeTimeEvents( IElementLifeTimeEventsSink Handler );

	/**
	 * Revokes the sink handler.
	*/
	public void revokeLifeTimeSink( IElementLifeTimeEventsSink Handler );

	/**
	 * Registers an event sink to handle element lifetime events.
	*/
	public void registerForDisposalEvents( IElementDisposalEventsSink Handler );

	/**
	 * Revokes the sink handler.
	*/
	public void revokeDisposalSink( IElementDisposalEventsSink Handler  );

	/**
	 * Registers an event sink to handle element unknown classifier events.
	*/
	public void registerForUnknownClassifierEvents( IUnknownClassifierEventsSink Handler );

	/**
	 * Revokes the sink handler.
	*/
	public void revokeUnknownClassifierSink( IUnknownClassifierEventsSink Handler );

	/**
	 * method FireElementPreCreate
	*/
	public boolean fireElementPreCreate( String ElementType, IEventPayload Payload );

	/**
	 * method FireElementCreated
	*/
	public void fireElementCreated( IVersionableElement element, IEventPayload Payload );

	/**
	 * method FireElementPreDelete
	*/
	public boolean fireElementPreDelete( IVersionableElement ver, IEventPayload Payload );

	/**
	 * method FireElementDeleted
	*/
	public void fireElementDeleted( IVersionableElement element, IEventPayload Payload );

	/**
	 * method FireElementDeleted
	*/
	public boolean firePreDisposeElements( ETList<IVersionableElement> pElements, IEventPayload Payload );

	/**
	 * Fired whenever after an element is created.
	*/
	public void fireDisposedElements( ETList<IVersionableElement> pElements, IEventPayload Payload );

	/**
	 * Fired whenever an element is about to be duplicated.
	*/
	public boolean fireElementPreDuplicated( IVersionableElement element, IEventPayload Payload );

	/**
	 * Fired after an element has been duplicated.
	*/
	public void fireElementDuplicated( IVersionableElement element, IEventPayload Payload );

	/**
	 * Fired when a new classifier is about to be created as specified by the unknown classifier preference.
	*/
	public boolean firePreUnknownCreate( String typeToCreate, IEventPayload Payload );

	/**
	 * Fired when a new classifier has been created as specified by the unknown classifier preference..
	*/
	public void fireUnknownCreated( INamedElement newType, IEventPayload Payload );

}
