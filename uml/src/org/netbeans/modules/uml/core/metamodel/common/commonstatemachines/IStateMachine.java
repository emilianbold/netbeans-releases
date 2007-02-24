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
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavior;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IStateMachine extends IBehavior
{
	/**
	 * method AddSubmachineState
	*/
	public void addSubmachineState( IState pState );

	/**
	 * method RemoveSubmachineState
	*/
	public void removeSubmachineState( IState pState );

	/**
	 * property SubmachinesStates
	*/
	public ETList<IState> getSubmachinesStates();

	/**
	 * method AddConnectionPoint
	*/
	public void addConnectionPoint( IUMLConnectionPoint pPoint );

	/**
	 * method RemoveConnectionPoint
	*/
	public void removeConnectionPoint( IUMLConnectionPoint pPoint );

	/**
	 * property ConnectionPoints
	*/
	public ETList<IUMLConnectionPoint> getConnectionPoints();

	/**
	 * method AddRegion
	*/
	public void addRegion( IRegion pRegion );

	/**
	 * method RemoveRegion
	*/
	public void removeRegion( IRegion pRegion );

	/**
	 * property Regions
	*/
	public ETList<IRegion> getRegions();

	/**
	 * Returns the first in our set of regions.
	*/
	public IRegion getFirstRegion();

	/**
	 * method AddConformance
	*/
	public void addConformance( IProtocolConformance pProt );

	/**
	 * method RemoveConformance
	*/
	public void removeConformance( IProtocolConformance pProt );

	/**
	 * property Conformances
	*/
	public ETList<IProtocolConformance> getConformances();

	/**
	 * property ContainedElements
	*/
	public ETList<INamedElement> getContainedElements();
}
