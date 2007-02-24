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


package org.netbeans.modules.uml.core.metamodel.common.commonstatemachines;

import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IProcedure;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IEvent;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IState extends IStateVertex
{
	/**
	 * property IsComposite
	*/
	public boolean getIsComposite();

	/**
	 * property IsComposite
	*/
	public void setIsComposite( boolean value );

	/**
	 * property IsOrthogonal
	*/
	public boolean getIsOrthogonal();

	/**
	 * property IsOrthogonal
	*/
	public void setIsOrthogonal( boolean value );

	/**
	 * property IsSimple
	*/
	public boolean getIsSimple();

	/**
	 * property IsSimple
	*/
	public void setIsSimple( boolean value );

	/**
	 * property IsSubmachineState
	*/
	public boolean getIsSubmachineState();

	/**
	 * property IsSubmachineState
	*/
	public void setIsSubmachineState( boolean value );

	/**
	 * method AddContent
	*/
	public void addContent( IRegion pReg );

	/**
	 * method RemoveContent
	*/
	public void removeContent( IRegion pReg );

	/**
	 * property Contents
	*/
	public ETList<IRegion> getContents();

	/**
	 * Returns the first content
	*/
	public IRegion getFirstContent();

	/**
	 * property Entry
	*/
	public IProcedure getEntry();

	/**
	 * property Entry
	*/
	public void setEntry( IProcedure value );

	/**
	 * property Exit
	*/
	public IProcedure getExit();

	/**
	 * property Exit
	*/
	public void setExit( IProcedure value );

	/**
	 * property DoActivity
	*/
	public IProcedure getDoActivity();

	/**
	 * property DoActivity
	*/
	public void setDoActivity( IProcedure value );

	/**
	 * method AddDefferableEvent
	*/
	public void addDefferableEvent( IEvent pEvent );

	/**
	 * method RemoveDeferrableEvent
	*/
	public void removeDeferrableEvent( IEvent pEvent );

	/**
	 * property DeferrableEvents
	*/
	public ETList<IEvent> getDeferrableEvents();

	/**
	 * property StateInvariant
	*/
	public IConstraint getStateInvariant();

	/**
	 * property StateInvariant
	*/
	public void setStateInvariant( IConstraint value );

	/**
	 * property Submachine
	*/
	public IStateMachine getSubmachine();

	/**
	 * property Submachine
	*/
	public void setSubmachine( IStateMachine value );
}
