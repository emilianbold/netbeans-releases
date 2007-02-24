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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;

public interface IConnectorEnd extends IElement
{
	/**
	 * property Part
	*/
	public IConnectableElement getPart();

	/**
	 * property Part
	*/
	public void setPart( IConnectableElement value );

	/**
	 * property Multiplicity
	*/
	public IMultiplicity getMultiplicity();

	/**
	 * property Multiplicity
	*/
	public void setMultiplicity( IMultiplicity value );

	/**
	 * property Port
	*/
	public IPort getPort();

	/**
	 * property Port
	*/
	public void setPort( IPort value );

	/**
	 * property InitialCardinality
	*/
	public int getInitialCardinality();

	/**
	 * property InitialCardinality
	*/
	public void setInitialCardinality( int value );

	/**
	 * property Connector
	*/
	public IConnector getConnector();

	/**
	 * property Connector
	*/
	public void setConnector( IConnector value );

	/**
	 * A derived association referencing the corresponding association end on the association which types the connector owing this connector end.
	*/
	public IAssociationEnd getDefiningEnd();

	/**
	 * A derived association referencing the corresponding association end on the association which types the connector owing this connector end.
	*/
	public void setDefiningEnd( IAssociationEnd value );
    
    public String getRangeAsString();

}
