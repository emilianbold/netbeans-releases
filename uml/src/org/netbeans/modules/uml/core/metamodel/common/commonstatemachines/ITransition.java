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
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IEvent;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface ITransition extends INamedElement
{
	/**
	 * property IsInternal
	*/
	public boolean getIsInternal();

	/**
	 * property IsInternal
	*/
	public void setIsInternal( boolean value );

	/**
	 * property Source
	*/
	public IStateVertex getSource();

	/**
	 * property Source
	*/
	public void setSource( IStateVertex value );

	/**
	 * property Target
	*/
	public IStateVertex getTarget();

	/**
	 * property Target
	*/
	public void setTarget( IStateVertex value );

	/**
	 * property Guard
	*/
	public IConstraint getGuard();

	/**
	 * property Guard
	*/
	public void setGuard( IConstraint value );

	/**
	 * property Effect
	*/
	public IProcedure getEffect();

	/**
	 * property Effect
	*/
	public void setEffect( IProcedure value );

	/**
	 * property Trigger
	*/
	public IEvent getTrigger();

	/**
	 * property Trigger
	*/
	public void setTrigger( IEvent value );

	/**
	 * property PreCondition
	*/
	public IConstraint getPreCondition();

	/**
	 * property PreCondition
	*/
	public void setPreCondition( IConstraint value );

	/**
	 * property PostCondition
	*/
	public IConstraint getPostCondition();

	/**
	 * property PostCondition
	*/
	public void setPostCondition( IConstraint value );

	/**
	 * method AddReferredOperation
	*/
	public void addReferredOperation( IOperation pOper );

	/**
	 * method RemoveReferredOperation
	*/
	public void removeReferredOperation( IOperation pOper );

	/**
	 * property ReferredOperations
	*/
	public ETList<IOperation> getReferredOperations();

	/**
	 * property Container
	*/
	public IRegion getContainer();

	/**
	 * property Container
	*/
	public void setContainer( IRegion value );

	/**
	 * method CreatePreCondition
	*/
	public IConstraint createPreCondition( String condition );

	/**
	 * method CreatePostCondition
	*/
	public IConstraint createPostCondition( String condition );
}
