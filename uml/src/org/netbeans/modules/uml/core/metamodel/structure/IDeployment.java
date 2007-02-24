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

package org.netbeans.modules.uml.core.metamodel.structure;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDirectedRelationship;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IDeployment extends IDirectedRelationship
{
	/**
	 * property Location
	*/
	public INode getLocation();

	/**
	 * property Location
	*/
	public void setLocation( INode value );

	/**
	 * property AddDeployedArtifact
	*/
	public void addDeployedArtifact( IArtifact pVal );

	/**
	 * property RemoveDeployedArtifact
	*/
	public void removeDeployedArtifact( IArtifact pVal );

	/**
	 * property DeployedArtifacts
	*/
	public ETList<IArtifact> getDeployedArtifacts();

	/**
	 * property Specification
	*/
	public IDeploymentSpecification getSpecification();

	/**
	 * property Specification
	*/
	public void setSpecification( IDeploymentSpecification value );

}
