package org.netbeans.modules.uml.core;

import org.netbeans.modules.uml.core.AbstractUMLTestCase;


/**
 * Test cases for UMLCreationFactory.
 */
public class UMLCreationFactoryTestCase extends AbstractUMLTestCase
{
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(UMLCreationFactoryTestCase.class);
    }
    
    public void testCreateAbstraction()
    {
        assertNotNull(factory.createAbstraction(null));
    }
    public void testCreateActionSequence()
    {
        assertNotNull(factory.createActionSequence(null));
    }
    public void testCreateActivity()
    {
        assertNotNull(factory.createActivity(null));
    }
    public void testCreateActivityFinalNode()
    {
        assertNotNull(factory.createActivityFinalNode(null));
    }
    public void testCreateActivityPartition()
    {
        assertNotNull(factory.createActivityPartition(null));
    }
    public void testCreateAggregation()
    {
        assertNotNull(factory.createAggregation(null));
    }
    public void testCreateArgument()
    {
        assertNotNull(factory.createArgument(null));
    }
    public void testCreateArtifact()
    {
        assertNotNull(factory.createArtifact(null));
    }
    public void testCreateAssemblyConnector()
    {
        assertNotNull(factory.createAssemblyConnector(null));
    }
    public void testCreateAssignmentAction()
    {
        assertNotNull(factory.createAssignmentAction(null));
    }
    public void testCreateAssociation()
    {
        assertNotNull(factory.createAssociation(null));
    }
    public void testCreateAssociationClass()
    {
        assertNotNull(factory.createAssociationClass(null));
    }
    public void testCreateAssociationEnd()
    {
        assertNotNull(factory.createAssociationEnd(null));
    }
    public void testCreateAtomicFragment()
    {
        assertNotNull(factory.createAtomicFragment(null));
    }
    public void testCreateAttribute()
    {
        assertNotNull(factory.createAttribute(null));
    }
    public void testCreateCallAction()
    {
        assertNotNull(factory.createCallAction(null));
    }
    public void testCreateCallEvent()
    {
        assertNotNull(factory.createCallEvent(null));
    }
    public void testCreateChangeEvent()
    {
        assertNotNull(factory.createChangeEvent(null));
    }
    public void testCreateChangeSignal()
    {
        assertNotNull(factory.createChangeSignal(null));
    }
    public void testCreateClass()
    {
        assertNotNull(factory.createClass(null));
    }
    public void testCreateCollaboration()
    {
        assertNotNull(factory.createCollaboration(null));
    }
    public void testCreateCollaborationOccurrence()
    {
        assertNotNull(factory.createCollaborationOccurrence(null));
    }
    public void testCreateCombinedFragment()
    {
        assertNotNull(factory.createCombinedFragment(null));
    }
    public void testCreateComment()
    {
        assertNotNull(factory.createComment(null));
    }
    public void testCreateComponent()
    {
        assertNotNull(factory.createComponent(null));
    }
    public void testCreateConnectionPoint()
    {
        assertNotNull(factory.createConnectionPoint(null));
    }
    public void testCreateConnector()
    {
        assertNotNull(factory.createConnector(null));
    }
    public void testCreateConnectorEnd()
    {
        assertNotNull(factory.createConnectorEnd(null));
    }
    public void testCreateConstraint()
    {
        assertNotNull(factory.createConstraint(null));
    }
    public void testCreateControlFlow()
    {
        assertNotNull(factory.createControlFlow(null));
    }
    public void testCreateCreateAction()
    {
        assertNotNull(factory.createCreateAction(null));
    }
    public void testCreateDataType()
    {
        assertNotNull(factory.createDataType(null));
    }
    public void testCreateDecisionNode()
    {
        assertNotNull(factory.createDecisionNode(null));
    }
    public void testCreateDelegationConnector()
    {
        assertNotNull(factory.createDelegationConnector(null));
    }
    public void testCreateDependency()
    {
        assertNotNull(factory.createDependency(null));
    }
    public void testCreateDeployment()
    {
        assertNotNull(factory.createDeployment(null));
    }
    public void testCreateDeploymentSpecification()
    {
        assertNotNull(factory.createDeploymentSpecification(null));
    }
    public void testCreateDestroyAction()
    {
        assertNotNull(factory.createDestroyAction(null));
    }
    public void testCreateElementImport()
    {
        assertNotNull(factory.createElementImport(null));
    }
    public void testCreateEnumeration()
    {
        assertNotNull(factory.createEnumeration(null));
    }
    public void testCreateEnumerationLiteral()
    {
        assertNotNull(factory.createEnumerationLiteral(null));
    }
    public void testCreateException()
    {
        assertNotNull(factory.createException(null));
    }
    public void testCreateExpression()
    {
        assertNotNull(factory.createExpression(null));
    }
    public void testCreateFinalState()
    {
        assertNotNull(factory.createFinalState(null));
    }
    public void testCreateFlow()
    {
        assertNotNull(factory.createFlow(null));
    }
    public void testCreateFlowFinalNode()
    {
        assertNotNull(factory.createFlowFinalNode(null));
    }
    public void testCreateForkNode()
    {
        assertNotNull(factory.createForkNode(null));
    }
    public void testCreateGate()
    {
        assertNotNull(factory.createGate(null));
    }
    public void testCreateGeneralization()
    {
        assertNotNull(factory.createGeneralization(null));
    }
    public void testCreateImplementation()
    {
        assertNotNull(factory.createImplementation(null));
    }
    public void testCreateIncrement()
    {
        assertNotNull(factory.createIncrement(null));
    }
    public void testCreateInitialNode()
    {
        assertNotNull(factory.createInitialNode(null));
    }
    public void testCreateInterGateConnector()
    {
        assertNotNull(factory.createInterGateConnector(null));
    }
    public void testCreateInteraction()
    {
        assertNotNull(factory.createInteraction(null));
    }
    public void testCreateInteractionConstraint()
    {
        assertNotNull(factory.createInteractionConstraint(null));
    }
    public void testCreateInteractionOccurrence()
    {
        assertNotNull(factory.createInteractionOccurrence(null));
    }
    public void testCreateInteractionOperand()
    {
        assertNotNull(factory.createInteractionOperand(null));
    }
    public void testCreateInterface()
    {
        assertNotNull(factory.createInterface(null));
    }
    public void testCreateInterruptibleActivityRegion()
    {
        assertNotNull(factory.createInterruptibleActivityRegion(null));
    }
    public void testCreateJoinNode()
    {
        assertNotNull(factory.createJoinNode(null));
    }
    public void testCreateLifeline()
    {
        assertNotNull(factory.createLifeline(null));
    }
    public void testCreateMergeNode()
    {
        assertNotNull(factory.createMergeNode(null));
    }
    public void testCreateMessage()
    {
        assertNotNull(factory.createMessage(null));
    }
    public void testCreateModel()
    {
        assertNotNull(factory.createModel(null));
    }
    public void testCreateMultiplicity()
    {
        assertNotNull(factory.createMultiplicity(null));
    }
    public void testCreateMultiplicityRange()
    {
        assertNotNull(factory.createMultiplicityRange(null));
    }
    public void testCreateNavigableEnd()
    {
        assertNotNull(factory.createNavigableEnd(null));
    }
    public void testCreateNode()
    {
        assertNotNull(factory.createNode(null));
    }
    public void testCreateObjectFlow()
    {
        assertNotNull(factory.createObjectFlow(null));
    }
    public void testCreateObjectNode()
    {
        assertNotNull(factory.createObjectNode(null));
    }
    public void testCreateOperation()
    {
        assertNotNull(factory.createOperation(null));
    }
    public void testCreatePackage()
    {
        assertNotNull(factory.createPackage(null));
    }
    public void testCreatePackageImport()
    {
        assertNotNull(factory.createPackageImport(null));
    }
    public void testCreateParameter()
    {
        assertNotNull(factory.createParameter(null));
    }
    public void testCreatePart()
    {
        assertNotNull(factory.createPart(null));
    }
    public void testCreatePartDecomposition()
    {
        assertNotNull(factory.createPartDecomposition(null));
    }
    public void testCreatePermission()
    {
        assertNotNull(factory.createPermission(null));
    }
    public void testCreatePort()
    {
        assertNotNull(factory.createPort(null));
    }
    public void testCreatePrimitiveType()
    {
        assertNotNull(factory.createPrimitiveType(null));
    }
    public void testCreateProject()
    {
        assertNotNull(factory.createProject(null));
    }
    public void testCreateProtocolConformance()
    {
        assertNotNull(factory.createProtocolConformance(null));
    }
    public void testCreatePseudoState()
    {
        assertNotNull(factory.createPseudoState(null));
    }
    public void testCreateRealization()
    {
        assertNotNull(factory.createRealization(null));
    }
    public void testCreateReception()
    {
        assertNotNull(factory.createReception(null));
    }
    public void testCreateReturnAction()
    {
        assertNotNull(factory.createReturnAction(null));
    }
    public void testCreateRoleBinding()
    {
        assertNotNull(factory.createRoleBinding(null));
    }
    public void testCreateSendAction()
    {
        assertNotNull(factory.createSendAction(null));
    }
    public void testCreateSignal()
    {
        assertNotNull(factory.createSignal(null));
    }
    public void testCreateSignalEvent()
    {
        assertNotNull(factory.createSignalEvent(null));
    }
    public void testCreateState()
    {
        assertNotNull(factory.createState(null));
    }
    public void testCreateStateMachine()
    {
        assertNotNull(factory.createStateMachine(null));
    }
    public void testCreateSubsystem()
    {
        assertNotNull(factory.createSubsystem(null));
    }
    public void testCreateTaggedValue()
    {
        assertNotNull(factory.createTaggedValue(null));
    }
    public void testCreateTerminateAction()
    {
        assertNotNull(factory.createTerminateAction(null));
    }
    public void testCreateTimeEvent()
    {
        assertNotNull(factory.createTimeEvent(null));
    }
    public void testCreateTimeSignal()
    {
        assertNotNull(factory.createTimeSignal(null));
    }
    public void testCreateTransition()
    {
        assertNotNull(factory.createTransition(null));
    }
    public void testCreateUninterpretedAction()
    {
        assertNotNull(factory.createUninterpretedAction(null));
    }
    public void testCreateUsage()
    {
        assertNotNull(factory.createUsage(null));
    }
}
