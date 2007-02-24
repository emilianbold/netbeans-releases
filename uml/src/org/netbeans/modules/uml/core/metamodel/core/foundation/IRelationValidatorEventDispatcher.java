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

public interface IRelationValidatorEventDispatcher extends IEventDispatcher
{
	/**
	 * Registers an event sink to handle relation validation events.
	*/
	public void registerForRelationValidatorEvents( IRelationValidatorEventsSink Handler );

	/**
	 * Registers an event sink to handle relation events.
	*/
	public void registerForRelationEvents( IRelationEventsSink Handler );

	/**
	 * Removes a sink listening for relation validation events.
	*/
	public void revokeRelationValidatorSink( IRelationValidatorEventsSink Handler );

	/**
	 * Removes a sink listening for relation events.
	*/
	public void revokeRelationSink( IRelationEventsSink Handler );

	/**
	 * Calling this method will result in the firing of any listeners who register for element modified events.
	*/
	public boolean firePreRelationValidate( IRelationProxy proxy, IEventPayload Payload );

	/**
	 * Calling this method will result in the firing of any listeners who register for element modified events.
	*/
	public void fireRelationValidated( IRelationProxy proxy, IEventPayload Payload );

	/**
	 * Fired before a relation meta type is modified. This includes Dependency, Generalization, and Associations.
	*/
	public boolean firePreRelationEndModified( IRelationProxy proxy, IEventPayload Payload );

	/**
	 * Fired after a relation meta type has been modified. This includes Dependency, Generalization, and Associations.
	*/
	public void fireRelationEndModified( IRelationProxy proxy, IEventPayload Payload );

	/**
	 * Fired before a relation meta type is added to. This includes Dependency, Generalization, and Associations.
	*/
	public boolean firePreRelationEndAdded( IRelationProxy proxy, IEventPayload Payload );

	/**
	 * Fired after a relation meta type has been added to. This includes Dependency, Generalization, and Associations.
	*/
	public void fireRelationEndAdded( IRelationProxy proxy, IEventPayload Payload );

	/**
	 * Fired before a relation meta type is removed from. This includes Dependency, Generalization, and Associations.
	*/
	public boolean firePreRelationEndRemoved( IRelationProxy proxy, IEventPayload Payload );

	/**
	 * Fired after a relation meta type has been removed from. This includes Dependency, Generalization, and Associations.
	*/
	public void fireRelationEndRemoved( IRelationProxy proxy, IEventPayload Payload );

	/**
	 * Fired before a relation meta type is created.
	*/
	public boolean firePreRelationCreated( IRelationProxy proxy, IEventPayload Payload );

	/**
	 * Fired after a relation meta type has been created.
	*/
	public void fireRelationCreated( IRelationProxy proxy, IEventPayload Payload );

	/**
	 * Fired before a relation meta type is deleted.
	*/
	public boolean firePreRelationDeleted( IRelationProxy proxy, IEventPayload Payload );

	/**
	 * Fired after a relation meta type has been deleted.
	*/
	public void fireRelationDeleted( IRelationProxy proxy, IEventPayload Payload );

}
