/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
