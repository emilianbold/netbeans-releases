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

package org.netbeans.modules.uml.core;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IActor;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IDataType;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPrimitiveType;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductManager;
import org.netbeans.modules.uml.core.metamodel.basic.basicactions.IReturnAction;
import org.netbeans.modules.uml.core.metamodel.behavior.IActionSequence;
import org.netbeans.modules.uml.core.metamodel.behavior.IAssignmentAction;
import org.netbeans.modules.uml.core.metamodel.behavior.ICallAction;
import org.netbeans.modules.uml.core.metamodel.behavior.ICallEvent;
import org.netbeans.modules.uml.core.metamodel.behavior.IChangeEvent;
import org.netbeans.modules.uml.core.metamodel.behavior.ICreateAction;
import org.netbeans.modules.uml.core.metamodel.behavior.IDestroyAction;
import org.netbeans.modules.uml.core.metamodel.behavior.ISendAction;
import org.netbeans.modules.uml.core.metamodel.behavior.ISignalEvent;
import org.netbeans.modules.uml.core.metamodel.behavior.ITerminateAction;
import org.netbeans.modules.uml.core.metamodel.behavior.ITimeEvent;
import org.netbeans.modules.uml.core.metamodel.behavior.IUninterpretedAction;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityFinalNode;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivityPartition;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IControlFlow;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IDecisionNode;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IFlowFinalNode;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IForkNode;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInitialNode;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IInterruptibleActivityRegion;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IJoinNode;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IMergeNode;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectFlow;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IObjectNode;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IFinalState;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IProtocolConformance;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IProtocolStateMachine;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IPseudoState;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateMachine;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IUMLConnectionPoint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.CreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IAbstraction;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IFlow;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicityRange;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPermission;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IUsage;
import org.netbeans.modules.uml.core.metamodel.dynamics.IAtomicFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IChangeSignal;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IGate;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInterGateConnector;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionConstraint;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.dynamics.IPartDecomposition;
import org.netbeans.modules.uml.core.metamodel.dynamics.ITimeSignal;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IAssemblyConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnectorEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IDelegationConnector;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPart;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPort;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IUMLException;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAggregation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IArgument;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ICollaborationOccurrence;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IIncrement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IRealization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IReception;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IRoleBinding;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ISignal;
import org.netbeans.modules.uml.core.metamodel.structure.IArtifact;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.core.metamodel.structure.IComponent;
import org.netbeans.modules.uml.core.metamodel.structure.IDeployment;
import org.netbeans.modules.uml.core.metamodel.structure.IDeploymentSpecification;
import org.netbeans.modules.uml.core.metamodel.structure.IModel;
import org.netbeans.modules.uml.core.metamodel.structure.INode;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.ISubsystem;

/**
 * @author sumitabhk
 *
 */
public class UMLCreationFactory extends CreationFactory implements IUMLCreationFactory
{
   private Object create(String typeName, Object outer)
   {
      return retrieveMetaType(typeName, outer);
   }

   private < T > T createType(String typeName, Object outer)
   {
      return (T) create(typeName, outer);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createClass(java.lang.Object)
    */
   public IClass createClass(Object outer)
   {
      Object obj = create("Class", outer);
      return obj instanceof IClass ? (IClass) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createGeneralization(java.lang.Object)
    */
   public IGeneralization createGeneralization(Object outer)
   {
      Object obj = create("Generalization", outer);
      return obj instanceof IGeneralization ? (IGeneralization) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createMultiplicity(java.lang.Object)
    */
   public IMultiplicity createMultiplicity(Object outer)
   {
      Object obj = create("Multiplicity", outer);
      return obj instanceof IMultiplicity ? (IMultiplicity) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createMultiplicityRange(java.lang.Object)
    */
   public IMultiplicityRange createMultiplicityRange(Object outer)
   {
      Object obj = create("MultiplicityRange", outer);
      return obj instanceof IMultiplicityRange ? (IMultiplicityRange) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createPackage(java.lang.Object)
    */
   public IPackage createPackage(Object outer)
   {
      Object obj = create("Package", outer);
      return obj instanceof IPackage ? (IPackage) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createElementImport(java.lang.Object)
    */
   public IElementImport createElementImport(Object outer)
   {
      Object obj = create("ElementImport", outer);
      return obj instanceof IElementImport ? (IElementImport) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createPackageImport(java.lang.Object)
    */
   public IPackageImport createPackageImport(Object outer)
   {
      Object obj = create("PackageImport", outer);
      return obj instanceof IPackageImport ? (IPackageImport) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createExpression(java.lang.Object)
    */
   public IExpression createExpression(Object outer)
   {
      Object obj = create("Expression", outer);
      return obj instanceof IExpression ? (IExpression) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createConstraint(java.lang.Object)
    */
   public IConstraint createConstraint(Object outer)
   {
      Object obj = create("Constraint", outer);
      return obj instanceof IConstraint ? (IConstraint) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createParameter(java.lang.Object)
    */
   public IParameter createParameter(Object outer)
   {
      Object obj = create("Parameter", outer);
      return obj instanceof IParameter ? (IParameter) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createOperation(java.lang.Object)
    */
   public IOperation createOperation(Object outer)
   {
      Object obj = create("Operation", outer);
      return obj instanceof IOperation ? (IOperation) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createAttribute(java.lang.Object)
    */
   public IAttribute createAttribute(Object outer)
   {
      Object obj = create("Attribute", outer);
      return obj instanceof IAttribute ? (IAttribute) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createDataType(java.lang.Object)
    */
   public IDataType createDataType(Object outer)
   {
      Object obj = create("DataType", outer);
      return obj instanceof IDataType ? (IDataType) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createPrimitiveType(java.lang.Object)
    */
   public IPrimitiveType createPrimitiveType(Object outer)
   {
      Object obj = create("PrimitiveType", outer);
      return obj instanceof IPrimitiveType ? (IPrimitiveType) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createEnumeration(java.lang.Object)
    */
   public IEnumeration createEnumeration(Object outer)
   {
      Object obj = create("Enumeration", outer);
      return obj instanceof IEnumeration ? (IEnumeration) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createEnumerationLiteral(java.lang.Object)
    */
   public IEnumerationLiteral createEnumerationLiteral(Object outer)
   {
      Object obj = create("EnumerationLiteral", outer);
      return obj instanceof IEnumerationLiteral ? (IEnumerationLiteral) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createAssociationEnd(java.lang.Object)
    */
   public IAssociationEnd createAssociationEnd(Object outer)
   {
      Object obj = create("AssociationEnd", outer);
      return obj instanceof IAssociationEnd ? (IAssociationEnd) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createAssociation(java.lang.Object)
    */
   public IAssociation createAssociation(Object outer)
   {
      Object obj = create("Association", outer);
      return obj instanceof IAssociation ? (IAssociation) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createNavigableEnd(java.lang.Object)
    */
   public INavigableEnd createNavigableEnd(Object outer)
   {
      Object obj = create("NavigableEnd", outer);
      return obj instanceof INavigableEnd ? (INavigableEnd) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createDependency(java.lang.Object)
    */
   public IDependency createDependency(Object outer)
   {
      Object obj = create("Dependency", outer);
      return obj instanceof IDependency ? (IDependency) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createAbstraction(java.lang.Object)
    */
   public IAbstraction createAbstraction(Object outer)
   {
      Object obj = create("Abstraction", outer);
      return obj instanceof IAbstraction ? (IAbstraction) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createUsage(java.lang.Object)
    */
   public IUsage createUsage(Object outer)
   {
      Object obj = create("Usage", outer);
      return obj instanceof IUsage ? (IUsage) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createPermission(java.lang.Object)
    */
   public IPermission createPermission(Object outer)
   {
      Object obj = create("Permission", outer);
      return obj instanceof IPermission ? (IPermission) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createRealization(java.lang.Object)
    */
   public IRealization createRealization(Object outer)
   {
      Object obj = create("Realization", outer);
      return obj instanceof IRealization ? (IRealization) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createCollaboration(java.lang.Object)
    */
   public ICollaboration createCollaboration(Object outer)
   {
      Object obj = create("Collaboration", outer);
      return obj instanceof ICollaboration ? (ICollaboration) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createPart(java.lang.Object)
    */
   public IPart createPart(Object outer)
   {
      Object obj = create("Part", outer);
      return obj instanceof IPart ? (IPart) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createConnectorEnd(java.lang.Object)
    */
   public IConnectorEnd createConnectorEnd(Object outer)
   {
      Object obj = create("ConnectorEnd", outer);
      return obj instanceof IConnectorEnd ? (IConnectorEnd) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createConnector(java.lang.Object)
    */
   public IConnector createConnector(Object outer)
   {
      Object obj = create("Connector", outer);
      return obj instanceof IConnector ? (IConnector) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createRoleBinding(java.lang.Object)
    */
   public IRoleBinding createRoleBinding(Object outer)
   {
      Object obj = create("RoleBinding", outer);
      return obj instanceof IRoleBinding ? (IRoleBinding) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createCollaborationOccurrence(java.lang.Object)
    */
   public ICollaborationOccurrence createCollaborationOccurrence(Object outer)
   {
      Object obj = create("CollaborationOccurrence", outer);
      return obj instanceof ICollaborationOccurrence ? (ICollaborationOccurrence) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createImplementation(java.lang.Object)
    */
   public IImplementation createImplementation(Object outer)
   {
      Object obj = create("Implementation", outer);
      return obj instanceof IImplementation ? (IImplementation) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createInterface(java.lang.Object)
    */
   public IInterface createInterface(Object outer)
   {
      Object obj = create("Interface", outer);
      return obj instanceof IInterface ? (IInterface) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createPort(java.lang.Object)
    */
   public IPort createPort(Object outer)
   {
      Object obj = create("Port", outer);
      return obj instanceof IPort ? (IPort) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createComponent(java.lang.Object)
    */
   public IComponent createComponent(Object outer)
   {
      Object obj = create("Component", outer);
      return obj instanceof IComponent ? (IComponent) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createStateMachine(java.lang.Object)
    */
   public IStateMachine createStateMachine(Object outer)
   {
      Object obj = create("StateMachine", outer);
      return obj instanceof IStateMachine ? (IStateMachine) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createModel(java.lang.Object)
    */
   public IModel createModel(Object outer)
   {
      Object obj = create("Model", outer);
      return obj instanceof IModel ? (IModel) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createProject(java.lang.Object)
    */
   public IProject createProject(Object outer)
   {
      Object obj = create("Project", outer);
      return obj instanceof IProject ? (IProject) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createFlow(java.lang.Object)
    */
   public IFlow createFlow(Object outer)
   {
      Object obj = create("Flow", outer);
      return obj instanceof IFlow ? (IFlow) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createAssociationClass(java.lang.Object)
    */
   public IAssociationClass createAssociationClass(Object outer)
   {
      Object obj = create("AssociationClass", outer);
      return obj instanceof IAssociationClass ? (IAssociationClass) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createComment(java.lang.Object)
    */
   public IComment createComment(Object outer)
   {
      Object obj = create("Comment", outer);
      return obj instanceof IComment ? (IComment) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createSignal(java.lang.Object)
    */
   public ISignal createSignal(Object outer)
   {
      Object obj = create("Signal", outer);
      return obj instanceof ISignal ? (ISignal) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createException(java.lang.Object)
    */
   public IUMLException createException(Object outer)
   {
      Object obj = create("UMLException", outer);
      return obj instanceof IUMLException ? (IUMLException) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createReception(java.lang.Object)
    */
   public IReception createReception(Object outer)
   {
      Object obj = create("Reception", outer);
      return obj instanceof IReception ? (IReception) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createArgument(java.lang.Object)
    */
   public IArgument createArgument(Object outer)
   {
      Object obj = create("Argument", outer);
      return obj instanceof IArgument ? (IArgument) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createSignalEvent(java.lang.Object)
    */
   public ISignalEvent createSignalEvent(Object outer)
   {
      Object obj = create("SignalEvent", outer);
      return obj instanceof ISignalEvent ? (ISignalEvent) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createCallEvent(java.lang.Object)
    */
   public ICallEvent createCallEvent(Object outer)
   {
      Object obj = create("CallEvent", outer);
      return obj instanceof ICallEvent ? (ICallEvent) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createTimeEvent(java.lang.Object)
    */
   public ITimeEvent createTimeEvent(Object outer)
   {
      Object obj = create("TimeEvent", outer);
      return obj instanceof ITimeEvent ? (ITimeEvent) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createChangeEvent(java.lang.Object)
    */
   public IChangeEvent createChangeEvent(Object outer)
   {
      Object obj = create("ChangeEvent", outer);
      return obj instanceof IChangeEvent ? (IChangeEvent) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createActionSequence(java.lang.Object)
    */
   public IActionSequence createActionSequence(Object outer)
   {
      Object obj = create("ActionSequence", outer);
      return obj instanceof IActionSequence ? (IActionSequence) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createUninterpretedAction(java.lang.Object)
    */
   public IUninterpretedAction createUninterpretedAction(Object outer)
   {
      Object obj = create("UninterpretedAction", outer);
      return obj instanceof IUninterpretedAction ? (IUninterpretedAction) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createTerminateAction(java.lang.Object)
    */
   public ITerminateAction createTerminateAction(Object outer)
   {
      Object obj = create("TerminateAction", outer);
      return obj instanceof ITerminateAction ? (ITerminateAction) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createCreateAction(java.lang.Object)
    */
   public ICreateAction createCreateAction(Object outer)
   {
      Object obj = create("CreateAction", outer);
      return obj instanceof ICreateAction ? (ICreateAction) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createReturnAction(java.lang.Object)
    */
   public IReturnAction createReturnAction(Object outer)
   {
      Object obj = create("ReturnAction", outer);
      return obj instanceof IReturnAction ? (IReturnAction) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createCallAction(java.lang.Object)
    */
   public ICallAction createCallAction(Object outer)
   {
      Object obj = create("CallAction", outer);
      return obj instanceof ICallAction ? (ICallAction) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createDestroyAction(java.lang.Object)
    */
   public IDestroyAction createDestroyAction(Object outer)
   {
      Object obj = create("DestroyAction", outer);
      return obj instanceof IDestroyAction ? (IDestroyAction) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createSendAction(java.lang.Object)
    */
   public ISendAction createSendAction(Object outer)
   {
      Object obj = create("SendAction", outer);
      return obj instanceof ISendAction ? (ISendAction) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createAssignmentAction(java.lang.Object)
    */
   public IAssignmentAction createAssignmentAction(Object outer)
   {
      Object obj = create("AssignmentAction", outer);
      return obj instanceof IAssignmentAction ? (IAssignmentAction) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createActivity(java.lang.Object)
    */
   public IActivity createActivity(Object outer)
   {
      Object obj = create("Activity", outer);
      return obj instanceof IActivity ? (IActivity) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createObjectNode(java.lang.Object)
    */
   public IObjectNode createObjectNode(Object outer)
   {
      Object obj = create("ObjectNode", outer);
      return obj instanceof IObjectNode ? (IObjectNode) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createControlFlow(java.lang.Object)
    */
   public IControlFlow createControlFlow(Object outer)
   {
      Object obj = create("ControlFlow", outer);
      return obj instanceof IControlFlow ? (IControlFlow) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createObjectFlow(java.lang.Object)
    */
   public IObjectFlow createObjectFlow(Object outer)
   {
      Object obj = create("ObjectFlow", outer);
      return obj instanceof IObjectFlow ? (IObjectFlow) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createInitialNode(java.lang.Object)
    */
   public IInitialNode createInitialNode(Object outer)
   {
      Object obj = create("InitialNode", outer);
      return obj instanceof IInitialNode ? (IInitialNode) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createForkNode(java.lang.Object)
    */
   public IForkNode createForkNode(Object outer)
   {
      Object obj = create("ForkNode", outer);
      return obj instanceof IForkNode ? (IForkNode) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createDecisionNode(java.lang.Object)
    */
   public IDecisionNode createDecisionNode(Object outer)
   {
      Object obj = create("DecisionNode", outer);
      return obj instanceof IDecisionNode ? (IDecisionNode) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createActivityFinalNode(java.lang.Object)
    */
   public IActivityFinalNode createActivityFinalNode(Object outer)
   {
      Object obj = create("ActivityFinalNode", outer);
      return obj instanceof IActivityFinalNode ? (IActivityFinalNode) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createFlowFinalNode(java.lang.Object)
    */
   public IFlowFinalNode createFlowFinalNode(Object outer)
   {
      Object obj = create("FlowFinalNode", outer);
      return obj instanceof IFlowFinalNode ? (IFlowFinalNode) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createJoinNode(java.lang.Object)
    */
   public IJoinNode createJoinNode(Object outer)
   {
      Object obj = create("JoinNode", outer);
      return obj instanceof IJoinNode ? (IJoinNode) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createMergeNode(java.lang.Object)
    */
   public IMergeNode createMergeNode(Object outer)
   {
      Object obj = create("MergeNode", outer);
      return obj instanceof IMergeNode ? (IMergeNode) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createActivityPartition(java.lang.Object)
    */
   public IActivityPartition createActivityPartition(Object outer)
   {
      Object obj = create("ActivityPartition", outer);
      return obj instanceof IActivityPartition ? (IActivityPartition) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createInterruptibleActivityRegion(java.lang.Object)
    */
   public IInterruptibleActivityRegion createInterruptibleActivityRegion(Object outer)
   {
      Object obj = create("InterruptibleActivityRegion", outer);
      return obj instanceof IInterruptibleActivityRegion ? (IInterruptibleActivityRegion) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createState(java.lang.Object)
    */
   public IState createState(Object outer)
   {
      Object obj = create("State", outer);
      return obj instanceof IState ? (IState) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createTimeSignal(java.lang.Object)
    */
   public ITimeSignal createTimeSignal(Object outer)
   {
      Object obj = create("TimeSignal", outer);
      return obj instanceof ITimeSignal ? (ITimeSignal) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createChangeSignal(java.lang.Object)
    */
   public IChangeSignal createChangeSignal(Object outer)
   {
      Object obj = create("ChangeSignal", outer);
      return obj instanceof IChangeSignal ? (IChangeSignal) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createInteractionOperand(java.lang.Object)
    */
   public IInteractionOperand createInteractionOperand(Object outer)
   {
      Object obj = create("InteractionOperand", outer);
      return obj instanceof IInteractionOperand ? (IInteractionOperand) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createCombinedFragment(java.lang.Object)
    */
   public ICombinedFragment createCombinedFragment(Object outer)
   {
      Object obj = create("CombinedFragment", outer);
      return obj instanceof ICombinedFragment ? (ICombinedFragment) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createInteractionOccurrence(java.lang.Object)
    */
   public IInteractionOccurrence createInteractionOccurrence(Object outer)
   {
      Object obj = create("InteractionOccurrence", outer);
      return obj instanceof IInteractionOccurrence ? (IInteractionOccurrence) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createAtomicFragment(java.lang.Object)
    */
   public IAtomicFragment createAtomicFragment(Object outer)
   {
      Object obj = create("AtomicFragment", outer);
      return obj instanceof IAtomicFragment ? (IAtomicFragment) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createInteractionConstraint(java.lang.Object)
    */
   public IInteractionConstraint createInteractionConstraint(Object outer)
   {
      Object obj = create("InteractionConstraint", outer);
      return obj instanceof IInteractionConstraint ? (IInteractionConstraint) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createInteraction(java.lang.Object)
    */
   public IInteraction createInteraction(Object outer)
   {
      Object obj = create("Interaction", outer);
      return obj instanceof IInteraction ? (IInteraction) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createMessage(java.lang.Object)
    */
   public IMessage createMessage(Object outer)
   {
      Object obj = create("Message", outer);
      return obj instanceof IMessage ? (IMessage) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createPartDecomposition(java.lang.Object)
    */
   public IPartDecomposition createPartDecomposition(Object outer)
   {
      Object obj = create("PartDecomposition", outer);
      return obj instanceof IPartDecomposition ? (IPartDecomposition) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createLifeline(java.lang.Object)
    */
   public ILifeline createLifeline(Object outer)
   {
      Object obj = create("Lifeline", outer);
      return obj instanceof ILifeline ? (ILifeline) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createGate(java.lang.Object)
    */
   public IGate createGate(Object outer)
   {
      Object obj = create("Gate", outer);
      return obj instanceof IGate ? (IGate) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createInterGateConnector(java.lang.Object)
    */
   public IInterGateConnector createInterGateConnector(Object outer)
   {
      Object obj = create("InterGateConnector", outer);
      return obj instanceof IInterGateConnector ? (IInterGateConnector) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createTransition(java.lang.Object)
    */
   public ITransition createTransition(Object outer)
   {
      Object obj = create("Transition", outer);
      return obj instanceof ITransition ? (ITransition) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createConnectionPoint(java.lang.Object)
    */
   public IUMLConnectionPoint createConnectionPoint(Object outer)
   {
      Object obj = create("UMLConnectionPoint", outer);
      return obj instanceof IUMLConnectionPoint ? (IUMLConnectionPoint) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createPseudoState(java.lang.Object)
    */
   public IPseudoState createPseudoState(Object outer)
   {
      Object obj = create("PseudoState", outer);
      return obj instanceof IPseudoState ? (IPseudoState) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createFinalState(java.lang.Object)
    */
   public IFinalState createFinalState(Object outer)
   {
      Object obj = create("FinalState", outer);
      return obj instanceof IFinalState ? (IFinalState) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createProtocolConformance(java.lang.Object)
    */
   public IProtocolConformance createProtocolConformance(Object outer)
   {
      Object obj = create("ProtocolConformance", outer);
      return obj instanceof IProtocolConformance ? (IProtocolConformance) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createAggregation(java.lang.Object)
    */
   public IAggregation createAggregation(Object outer)
   {
      Object obj = create("Aggregation", outer);
      return obj instanceof IAggregation ? (IAggregation) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createIncrement(java.lang.Object)
    */
   public IIncrement createIncrement(Object outer)
   {
      Object obj = create("Increment", outer);
      return obj instanceof IIncrement ? (IIncrement) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createDelegationConnector(java.lang.Object)
    */
   public IDelegationConnector createDelegationConnector(Object outer)
   {
      Object obj = create("DelegationConnector", outer);
      return obj instanceof IDelegationConnector ? (IDelegationConnector) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createAssemblyConnector(java.lang.Object)
    */
   public IAssemblyConnector createAssemblyConnector(Object outer)
   {
      Object obj = create("AssemblyConnector", outer);
      return obj instanceof IAssemblyConnector ? (IAssemblyConnector) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createNode(java.lang.Object)
    */
   public INode createNode(Object outer)
   {
      Object obj = create("Node", outer);
      return obj instanceof INode ? (INode) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createArtifact(java.lang.Object)
    */
   public IArtifact createArtifact(Object outer)
   {
      Object obj = create("Artifact", outer);
      return obj instanceof IArtifact ? (IArtifact) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createDeployment(java.lang.Object)
    */
   public IDeployment createDeployment(Object outer)
   {
      Object obj = create("Deployment", outer);
      return obj instanceof IDeployment ? (IDeployment) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createDeploymentSpecification(java.lang.Object)
    */
   public IDeploymentSpecification createDeploymentSpecification(Object outer)
   {
      Object obj = create("DeploymentSpecification", outer);
      return obj instanceof IDeploymentSpecification ? (IDeploymentSpecification) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createSubsystem(java.lang.Object)
    */
   public ISubsystem createSubsystem(Object outer)
   {
      Object obj = create("Subsystem", outer);
      return obj instanceof ISubsystem ? (ISubsystem) obj : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createTaggedValue(java.lang.Object)
    */
   public ITaggedValue createTaggedValue(Object outer)
   {
      Object obj = create("TaggedValue", outer);
      return obj instanceof ITaggedValue ? (ITaggedValue) obj : null;
   }

   /**
    *
    * Retrieves an IUMLCreationFactory interface off the current product
    *
    * @param pVal[out] the found IUMLCreationFactory.
    *
    * @return Typical HRESULTs. 
    *
    */
   public ICreationFactory getCreationFactory()
   {
      ICoreProduct prod = CoreProductManager.instance().getCoreProduct();
      return prod != null ? prod.getCreationFactory() : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createActor(java.lang.Object)
    */
   public IActor createActor(Object outer)
   {
      return createType("Actor", outer);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.IUMLCreationFactory#createProtocolStateMachine(java.lang.Object)
    */
   public IProtocolStateMachine createProtocolStateMachine(Object outer)
   {
      return createType("ProtocolStateMachine", outer);
   }
}
