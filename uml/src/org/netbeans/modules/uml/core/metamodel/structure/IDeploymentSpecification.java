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
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IDeploymentSpecification extends INamedElement
{
	/**
	 * method AddDeploymentDescriptor
	*/
	public void addDeploymentDescriptor( IArtifact art );

	/**
	 * method RemoveDeploymentDescriptor
	*/
	public void removeDeploymentDescriptor( IArtifact art );

	/**
	 * property DeploymentDescriptors
	*/
	public ETList<IArtifact> getDeploymentDescriptors();

	/**
	 * property Container
	*/
	public INode getContainer();

	/**
	 * property Container
	*/
	public void setContainer( INode value );

	/**
	 * property DeploymentLocation
	*/
	public String getDeploymentLocation();

	/**
	 * property DeploymentLocation
	*/
	public void setDeploymentLocation( String value );

	/**
	 * property ExecutionLocation
	*/
	public String getExecutionLocation();

	/**
	 * property ExecutionLocation
	*/
	public void setExecutionLocation( String value );

	/**
	 * method AddDeployment
	*/
	public void addDeployment( IDeployment dep );

	/**
	 * method RemoveDeployment
	*/
	public void removeDeployment( IDeployment dep );

	/**
	 * property Deployments
	*/
	public ETList<IDeployment> getDeployments();

	/**
	 * property ConfiguredComponents
	*/
	public IComponent getConfiguredComponent();

	/**
	 * property ConfiguredComponents
	*/
	public void setConfiguredComponent( IComponent value );

	/**
	 * property ConfiguredAssembly
	*/
	public IComponentAssembly getConfiguredAssembly();

	/**
	 * property ConfiguredAssembly
	*/
	public void setConfiguredAssembly( IComponentAssembly value );

}
