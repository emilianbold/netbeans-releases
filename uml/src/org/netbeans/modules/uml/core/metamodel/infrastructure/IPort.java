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

import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IProtocolStateMachine;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IPort extends IFeature
{
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
	 * property Protocol
	*/
	public IProtocolStateMachine getProtocol();

	/**
	 * property Protocol
	*/
	public void setProtocol( IProtocolStateMachine value );

	/**
	 * 
	*/
	public boolean getIsService();

	/**
	 * 
	*/
	public void setIsService( boolean value );

	/**
	 * 
	*/
	public boolean getIsSignal();

	/**
	 * 
	*/
	public void setIsSignal( boolean value );

	/**
	 * 
	*/
	public void addRequiredInterface( IInterface pInter );

	/**
	 * 
	*/
	public void removeRequiredInterface( IInterface end );

	/**
	 * 
	*/
	public ETList<IInterface> getRequiredInterfaces();

	/**
	 * Checks the list of required interfaces to see if pInter is in the list
	*/
	public boolean getIsRequiredInterface( IInterface pInter );

	/**
	 * 
	*/
	public void addProvidedInterface( IInterface pInter );

	/**
	 * 
	*/
	public void removeProvidedInterface( IInterface end );

	/**
	 * 
	*/
	public ETList<IInterface> getProvidedInterfaces();

	/**
	 * Checks the list of provided interfaces to see if pInter is in the list
	*/
	public boolean getIsProvidedInterface( IInterface pInter );

}
