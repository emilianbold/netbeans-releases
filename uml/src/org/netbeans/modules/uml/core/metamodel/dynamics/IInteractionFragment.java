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

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IInteractionFragment extends INamedElement
{
	/**
	 * Adds the connector to the internal collection.
	*/
	public void addGateConnector( IInterGateConnector connector );

	/**
	 * Removes the specified connector from this fragment.
	*/
	public void removeGateConnector( IInterGateConnector connector );

	/**
	 * Retrieves all the connectors this fragment owns.
	*/
	public ETList<IInterGateConnector> getGateConnectors();

	/**
	 * Adds a lifeline to the collection of lines this fragment covers.
	*/
	public void addCoveredLifeline( ILifeline line );

	/**
	 * Removes the specified lifeline from the collection of lines this fragment covers.
	*/
	public void removeCoveredLifeline( ILifeline line );

	/**
	 * Retrieves the collection of life lines this fragment covers.
	*/
	public ETList<ILifeline> getCoveredLifelines();

	/**
	 * The enclosing Operand, immediately encapsulating this fragment.
	*/
	public IInteractionOperand getEnclosingOperand();
}
