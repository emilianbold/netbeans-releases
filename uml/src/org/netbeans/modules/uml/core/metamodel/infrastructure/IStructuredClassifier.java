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

import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IStructuredClassifier extends IClassifier
{
	/**
	 * Adds a Role to this classifier.
	*/
	public void addRole( IConnectableElement pElement );

	/**
	 * Removes a role from this Classifier.
	*/
	public void removeRole( IConnectableElement pElement );

	/**
	 * Retrieves all the roles associated with this Classifier 
	*/
	public ETList<IConnectableElement> getRoles();

	/**
	 * Adds a Part to this Classifier.
	*/
	public void addPart( IPart pPart );

	/**
	 * Removes a Part from this Classifier.
	*/
	public void removePart( IPart pPart );

	/**
	 * Retrieves all the Parts contained by this Classifier.
	*/
	public ETList<IPart> getParts();

	/**
	 * Adds a connector to this Classifier.
	*/
	public void addConnector( IConnector pConnector );

	/**
	 * Removes a connector from this Classifier.
	*/
	public void removeConnector( IConnector pConnector );

	/**
	 * Retrieves all the connectors that this Classifier owns.
	*/
	public ETList<IConnector> getConnectors();

}
