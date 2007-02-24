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

package org.netbeans.modules.uml.core.metamodel.infrastructure;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IDirectedRelationship;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IStructuralFeature;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IConnector extends IDirectedRelationship, IStructuralFeature
{
	/**
	 * method AddBehavior
	*/
	public void addBehavior( IBehavior behavior );

	/**
	 * method RemoveBehavior
	*/
	public void removeBehavior( IBehavior behavior );

	/**
	 * property Behaviors
	*/
	public ETList<IBehavior> getBehaviors();

	/**
	 * method AddEnd
	*/
	public void addEnd( IConnectorEnd end );

	/**
	 * method RemoveEnd
	*/
	public void removeEnd( IConnectorEnd end );

	/**
	 * property Ends
	*/
	public ETList<IConnectorEnd> getEnds();

	/**
	 * property From
	*/
	public IConnectorEnd getFrom();

	/**
	 * property From
	*/
	public void setFrom( IConnectorEnd value );

	/**
	 * property To
	*/
	public IConnectorEnd getTo();

	/**
	 * property To
	*/
	public void setTo( IConnectorEnd value );
}
