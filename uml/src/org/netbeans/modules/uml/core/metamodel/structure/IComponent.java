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
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateMachine;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPart;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPort;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IComponent extends IClass
{
	/**
	 * property Instantiation
	*/
	public int getInstantiation();

	/**
	 * property Instantiation
	*/
	public void setInstantiation( /* InstantiationKind */ int value );

	/**
	 * method AddExternalInterface
	*/
	public void addExternalInterface( IPort ext );

	/**
	 * method RemoveExternalInterface
	*/
	public void removeExternalInterface( IPort ext );

	/**
	 * property ExternalInterfaces
	*/
	public ETList<IPort> getExternalInterfaces();

	/**
	 * property SpecifyingStateMachine
	*/
	public IStateMachine getSpecifyingStateMachine();

	/**
	 * property SpecifyingStateMachine
	*/
	public void setSpecifyingStateMachine( IStateMachine value );

	/**
	 * method AddElementImport
	*/
	public void addElementImport( IElementImport element );

	/**
	 * method RemoveElementImport
	*/
	public void removeElementImport( IElementImport element );

	/**
	 * property ElementImports
	*/
	public ETList<IElementImport> getElementImports();

	/**
	 * method AddInternalConnector
	*/
	public void addInternalConnector( IConnector connect );

	/**
	 * method RemoveInternalConnector
	*/
	public void removeInternalConnector( IConnector connect );

	/**
	 * property InternalConnectors
	*/
	public ETList<IConnector> getInternalConnectors();

	/**
	 * method AddInternalClassifier
	*/
	public void addInternalClassifier( IPart internal );

	/**
	 * method RemoveInternalClassifier
	*/
	public void removeInternalClassifier( IPart internal );

	/**
	 * property InternalClassifiers
	*/
	public ETList<IPart> getInternalClassifiers();

	/**
	 * method AddNode
	*/
	public void addNode( INode node );

	/**
	 * method RemoveNode
	*/
	public void removeNode( INode node );

	/**
	 * property Nodes
	*/
	public ETList<INode> getNodes();

	/**
	 * method AddArtifact
	*/
	public void addArtifact( IArtifact art );

	/**
	 * method RemoveArtifact
	*/
	public void removeArtifact( IArtifact art );

	/**
	 * property Artifacts
	*/
	public ETList<IArtifact> getArtifacts();

	/**
	 * method AddDeploymentSpecification
	*/
	public void addDeploymentSpecification( IDeploymentSpecification pSpec );

	/**
	 * method RemoveDeploymentSpecification
	*/
	public void removeDeploymentSpecification( IDeploymentSpecification pSpec );

	/**
	 * property DeploymentSpecifications
	*/
	public ETList<IDeploymentSpecification> getDeploymentSpecifications();

	/**
	 * method AddAssembly
	*/
	public void addAssembly( IComponentAssembly pAssembly );

	/**
	 * method RemoveAssembly
	*/
	public void removeAssembly( IComponentAssembly pAssembly );

	/**
	 * property Assemblies
	*/
	public ETList<IComponentAssembly> getAssemblies();
   
   /**
    * Determines whether or not the passed in Classifier is internal to the Component.
    * 
    * @param classifier[in]  The classifier to check
    * @return True if it is internal, else false
    */ 
   public boolean getIsInternalClassifier( IClassifier classifier );
}
