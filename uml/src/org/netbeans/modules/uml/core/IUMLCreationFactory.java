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
import org.netbeans.modules.uml.core.metamodel.core.constructs.IActor;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IDataType;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumerationLiteral;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPrimitiveType;
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

public interface IUMLCreationFactory extends ICreationFactory
{
    /**
     * Creates a new Actor.
     * 
     * @param outer The enclosing (parent) element.
     * @return The created IActor instance.
     */
    public IActor createActor(Object outer);
    
	/**
	 * Create a new Class object.
	*/
	public IClass createClass( Object outer );

	/**
	 * Create a new Generalization object.
	*/
	public IGeneralization createGeneralization( Object outer );

	/**
	 * Create a new Multiplicity object.
	*/
	public IMultiplicity createMultiplicity( Object outer );

	/**
	 * Create a new MultiplicityRange object.
	*/
	public IMultiplicityRange createMultiplicityRange( Object outer );

	/**
	 * Create a new Package object.
	*/
	public IPackage createPackage( Object outer );
    
    /**
     * Creates a new IProtocolStateMachine.
     * @param object The parent object.
     * @return The created IProtocolStateMachine.   
     */
    public IProtocolStateMachine createProtocolStateMachine(Object object);    

	/**
	 * Create a new ElementImport object.
	*/
	public IElementImport createElementImport( Object outer );

	/**
	 * Create a new PackageImport object.
	*/
	public IPackageImport createPackageImport( Object outer );

	/**
	 * Create a new Expression object.
	*/
	public IExpression createExpression( Object outer );

	/**
	 * Create a new Constraint object.
	*/
	public IConstraint createConstraint( Object outer );

	/**
	 * Create a new Parameter object.
	*/
	public IParameter createParameter( Object outer );

	/**
	 * Create a new Operation object.
	*/
	public IOperation createOperation( Object outer );

	/**
	 * Create a new Attribute object.
	*/
	public IAttribute createAttribute( Object outer );

	/**
	 * Create a new DataType object.
	*/
	public IDataType createDataType( Object outer );

	/**
	 * Create a new PrimitiveType object.
	*/
	public IPrimitiveType createPrimitiveType( Object outer );

	/**
	 * Create a new Enumeration object.
	*/
	public IEnumeration createEnumeration( Object outer );

	/**
	 * Create a new EnumerationLiteral object.
	*/
	public IEnumerationLiteral createEnumerationLiteral( Object outer );

	/**
	 * Create a new AssociationEnd object.
	*/
	public IAssociationEnd createAssociationEnd( Object outer );

	/**
	 * Create a new Association object.
	*/
	public IAssociation createAssociation( Object outer );

	/**
	 * Create a new NavigableAssociationEnd object.
	*/
	public INavigableEnd createNavigableEnd( Object outer );

	/**
	 * Create a new Dependency object.
	*/
	public IDependency createDependency( Object outer );

	/**
	 * Create a new Abstraction object.
	*/
	public IAbstraction createAbstraction( Object outer );

	/**
	 * Create a new Usage object.
	*/
	public IUsage createUsage( Object outer );

	/**
	 * Create a new Permission object.
	*/
	public IPermission createPermission( Object outer );

	/**
	 * Create a new Realization object.
	*/
	public IRealization createRealization( Object outer );

	/**
	 * Create a new Collaboration object.
	*/
	public ICollaboration createCollaboration( Object outer );

	/**
	 * Create a new Part object.
	*/
	public IPart createPart( Object outer );

	/**
	 * Create a new ConnectorEnd object.
	*/
	public IConnectorEnd createConnectorEnd( Object outer );

	/**
	 * Create a new Connector object.
	*/
	public IConnector createConnector( Object outer );

	/**
	 * Create a new RoleBinding object.
	*/
	public IRoleBinding createRoleBinding( Object outer );

	/**
	 * Create a new CollaborationOccurrence object.
	*/
	public ICollaborationOccurrence createCollaborationOccurrence( Object outer );

	/**
	 * Create a new Implementation object.
	*/
	public IImplementation createImplementation( Object outer );

	/**
	 * Create a new Interface object.
	*/
	public IInterface createInterface( Object outer );

	/**
	 * Create a new Port object.
	*/
	public IPort createPort( Object outer );

	/**
	 * Create a new Component object.
	*/
	public IComponent createComponent( Object outer );

	/**
	 * Create a new StateMachine object.
	*/
	public IStateMachine createStateMachine( Object outer );

	/**
	 * Create a new Model object.
	*/
	public IModel createModel( Object outer );

	/**
	 * Create a new Project object.
	*/
	public IProject createProject( Object outer );

	/**
	 * Create a new Flow object.
	*/
	public IFlow createFlow( Object outer );

	/**
	 * Create a new AssociationClass object.
	*/
	public IAssociationClass createAssociationClass( Object outer );

	/**
	 * Create a new Comment object.
	*/
	public IComment createComment( Object outer );

	/**
	 * Create a new Signal object.
	*/
	public ISignal createSignal( Object outer );

	/**
	 * Create a new Exception object.
	*/
	public IUMLException createException( Object outer );

	/**
	 * Create a new Reception object.
	*/
	public IReception createReception( Object outer );

	/**
	 * Create a new Argument object.
	*/
	public IArgument createArgument( Object outer );

	/**
	 * Create a new SignalEvent object.
	*/
	public ISignalEvent createSignalEvent( Object outer );

	/**
	 * Create a new CallEvent object.
	*/
	public ICallEvent createCallEvent( Object outer );

	/**
	 * Create a new TimeEvent object.
	*/
	public ITimeEvent createTimeEvent( Object outer );

	/**
	 * Create a new ChangeEvent object.
	*/
	public IChangeEvent createChangeEvent( Object outer );

	/**
	 * Create a new ActionSequence object.
	*/
	public IActionSequence createActionSequence( Object outer );

	/**
	 * Create a new UninterpretedAction object.
	*/
	public IUninterpretedAction createUninterpretedAction( Object outer );

	/**
	 * Create a new TerminateAction object.
	*/
	public ITerminateAction createTerminateAction( Object outer );

	/**
	 * Create a new CreateAction object.
	*/
	public ICreateAction createCreateAction( Object outer );

	/**
	 * Create a new ReturnAction object.
	*/
	public IReturnAction createReturnAction( Object outer );

	/**
	 * Create a new CallAction object.
	*/
	public ICallAction createCallAction( Object outer );

	/**
	 * Create a new DestroyAction object.
	*/
	public IDestroyAction createDestroyAction( Object outer );

	/**
	 * Create a new SendAction object.
	*/
	public ISendAction createSendAction( Object outer );

	/**
	 * Create a new AssignmentAction object.
	*/
	public IAssignmentAction createAssignmentAction( Object outer );

	/**
	 * Create a new Activity object.
	*/
	public IActivity createActivity( Object outer );

	/**
	 * Create a new ObjectNode object.
	*/
	public IObjectNode createObjectNode( Object outer );

	/**
	 * Create a new ControlFlow object.
	*/
	public IControlFlow createControlFlow( Object outer );

	/**
	 * Create a new ObjectFlow object.
	*/
	public IObjectFlow createObjectFlow( Object outer );

	/**
	 * Create a new InitialNode object.
	*/
	public IInitialNode createInitialNode( Object outer );

	/**
	 * Create a new ForkNode object.
	*/
	public IForkNode createForkNode( Object outer );

	/**
	 * Create a new DecisionNode object.
	*/
	public IDecisionNode createDecisionNode( Object outer );

	/**
	 * Create a new ActivityFinalNode object.
	*/
	public IActivityFinalNode createActivityFinalNode( Object outer );

	/**
	 * Create a new FlowFinalNode object.
	*/
	public IFlowFinalNode createFlowFinalNode( Object outer );

	/**
	 * Create a new JoinNode object.
	*/
	public IJoinNode createJoinNode( Object outer );

	/**
	 * Create a new MergeNode object.
	*/
	public IMergeNode createMergeNode( Object outer );

	/**
	 * Create a new ActivityPartition object.
	*/
	public IActivityPartition createActivityPartition( Object outer );

	/**
	 * Create a new InterruptibleActivityRegion object.
	*/
	public IInterruptibleActivityRegion createInterruptibleActivityRegion( Object outer );

	/**
	 * Create a new State object.
	*/
	public IState createState( Object outer );

	/**
	 * Create a new TimeSignal object.
	*/
	public ITimeSignal createTimeSignal( Object outer );

	/**
	 * Create a new ChangeSignal object.
	*/
	public IChangeSignal createChangeSignal( Object outer );

	/**
	 * Create a new InteractionOperand object.
	*/
	public IInteractionOperand createInteractionOperand( Object outer );

	/**
	 * Create a new CombinedFragment object.
	*/
	public ICombinedFragment createCombinedFragment( Object outer );

	/**
	 * Create a new InteractionOccurrence object.
	*/
	public IInteractionOccurrence createInteractionOccurrence( Object outer );

	/**
	 * Create a new AtomicFragment object.
	*/
	public IAtomicFragment createAtomicFragment( Object outer );

	/**
	 * Create a new InteractionConstraint object.
	*/
	public IInteractionConstraint createInteractionConstraint( Object outer );

	/**
	 * Create a new Interaction object.
	*/
	public IInteraction createInteraction( Object outer );

	/**
	 * Create a new Message object.
	*/
	public IMessage createMessage( Object outer );

	/**
	 * Create a new PartDecomposition object.
	*/
	public IPartDecomposition createPartDecomposition( Object outer );

	/**
	 * Create a new Lifeline object.
	*/
	public ILifeline createLifeline( Object outer );

	/**
	 * Create a new Gate object.
	*/
	public IGate createGate( Object outer );

	/**
	 * Create a new InterGateConnector object.
	*/
	public IInterGateConnector createInterGateConnector( Object outer );

	/**
	 * Create a new Transition object.
	*/
	public ITransition createTransition( Object outer );

	/**
	 * Create a new ConnectionPoint object.
	*/
	public IUMLConnectionPoint createConnectionPoint( Object outer );

	/**
	 * Create a new PseudoState object.
	*/
	public IPseudoState createPseudoState( Object outer );

	/**
	 * Create a new FinalState object.
	*/
	public IFinalState createFinalState( Object outer );

	/**
	 * Create a new ProtocolConformance object.
	*/
	public IProtocolConformance createProtocolConformance( Object outer );

	/**
	 * Create a new Aggregation object.
	*/
	public IAggregation createAggregation( Object outer );

	/**
	 * Create a new Increment object.
	*/
	public IIncrement createIncrement( Object outer );

	/**
	 * Create a new DelegationConnector object.
	*/
	public IDelegationConnector createDelegationConnector( Object outer );

	/**
	 * Create a new AssemblyConnector object.
	*/
	public IAssemblyConnector createAssemblyConnector( Object outer );

	/**
	 * Create a new Node object.
	*/
	public INode createNode( Object outer );

	/**
	 * Create a new Artifact object.
	*/
	public IArtifact createArtifact( Object outer );

	/**
	 * Create a new Deployment object.
	*/
	public IDeployment createDeployment( Object outer );

	/**
	 * Create a new DeploymentSpecification object.
	*/
	public IDeploymentSpecification createDeploymentSpecification( Object outer );

	/**
	 * Create a new Subsystem object.
	*/
	public ISubsystem createSubsystem( Object outer );

	/**
	 * Create a new TaggedValue object.
	*/
	public ITaggedValue createTaggedValue( Object outer );
}
