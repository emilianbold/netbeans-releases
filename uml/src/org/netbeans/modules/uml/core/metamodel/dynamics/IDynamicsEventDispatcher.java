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


package org.netbeans.modules.uml.core.metamodel.dynamics;

import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement;

public interface IDynamicsEventDispatcher extends IEventDispatcher
{
	/**
	 * Registers an event sink to handle lifeline modified events.
	*/
	public void registerForLifelineModifiedEvents( ILifelineModifiedEventsSink handler );

	/**
	 * Removes a sink listening for lifeline modified events.
	*/
	public void revokeLifelineModifiedSink( ILifelineModifiedEventsSink handler );

	/**
	 * Calling this method will result in the firing of any listeners who register for lifeline modified events.
	*/
	public boolean firePreChangeRepresentingClassifier( ILifeline pLifeline, ITypedElement pRepresents, IEventPayload payload );

	/**
	 * Calling this method will result in the firing of any listeners who register for lifeline modified events.
	*/
	public void fireChangeRepresentingClassifier( ILifeline pLifeline, ITypedElement pRepresents, IEventPayload payload );
}
