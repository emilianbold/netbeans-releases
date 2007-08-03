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


package org.netbeans.modules.uml.ui.support.commonresources;

import java.util.HashMap;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.diagramsupport.DiagramTypesManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.IDiagramTypesManager;
import java.awt.Image;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;

/**
 * @author sumitabhk
 *
 *
 */
public class CommonResourceManager implements ICommonResourceManager
{
   private static final String BUNDLE_NAME ="org.netbeans.modules.uml.ui.support.commonresources.Bundle";
   
   private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
   
   //stores the name of the element and the corresponding icon location
   private HashMap<String, String> m_ElementNameIconMap = new HashMap<String, String>();
   
   private static CommonResourceManager m_CommonResourceManager = null;
   
   public static CommonResourceManager instance()
   {
      if(m_CommonResourceManager == null)
      {
         m_CommonResourceManager = new CommonResourceManager();
      }
      return m_CommonResourceManager;
   }
   
   /**
    *
    */
   private CommonResourceManager()
   {
      super();
      initializeMap();
   }
   
   /**
    *
    */
   private void initializeMap()
   {
      // No icon
      m_ElementNameIconMap.put("No Icon", RESOURCE_BUNDLE.getString("IDI_NOICON"));
      m_ElementNameIconMap.put("None", RESOURCE_BUNDLE.getString("IDI_NOICON"));
      // The elements
      m_ElementNameIconMap.put("Class", RESOURCE_BUNDLE.getString("IDI_CLASS"));
      m_ElementNameIconMap.put("Abstraction", RESOURCE_BUNDLE.getString("IDI_ABSTRACTION"));
      m_ElementNameIconMap.put("AbortedFinalState", RESOURCE_BUNDLE.getString("IDI_ABORTEDFINALSTATE"));
      m_ElementNameIconMap.put("ActionSequence", RESOURCE_BUNDLE.getString("IDI_ACTIONSEQUENCE"));
      m_ElementNameIconMap.put("Activity", RESOURCE_BUNDLE.getString("IDI_ACTIVITY"));
      m_ElementNameIconMap.put("ActivityEdge", RESOURCE_BUNDLE.getString("IDI_ACTIVITYEDGE"));
      m_ElementNameIconMap.put("ActivityFinalNode", RESOURCE_BUNDLE.getString("IDI_ACTIVITYFINALNODE"));
      m_ElementNameIconMap.put("ActivityNode", RESOURCE_BUNDLE.getString("IDI_ACTIVITYNODE"));
      m_ElementNameIconMap.put("ActivityGroup", RESOURCE_BUNDLE.getString("IDI_ACTIVITYGROUP"));
      m_ElementNameIconMap.put("ActivityInvocation", RESOURCE_BUNDLE.getString("IDI_ACTIVITYINVOCATION"));
      m_ElementNameIconMap.put("ActivityPartition", RESOURCE_BUNDLE.getString("IDI_ACTIVITYPARTITION"));
      m_ElementNameIconMap.put("Actor", RESOURCE_BUNDLE.getString("IDI_ACTOR"));
      m_ElementNameIconMap.put("Aggregation", RESOURCE_BUNDLE.getString("IDI_AGGREGATION"));
      m_ElementNameIconMap.put("Navigable_Aggregation", RESOURCE_BUNDLE.getString("IDI_NAV_AGGREGATION"));
      m_ElementNameIconMap.put("Composition", RESOURCE_BUNDLE.getString("IDI_COMPOSITION"));
      m_ElementNameIconMap.put("Navigable_Composition", RESOURCE_BUNDLE.getString("IDI_NAV_COMPOSITION"));
      m_ElementNameIconMap.put("AliasedType", RESOURCE_BUNDLE.getString("IDI_ALIASEDTYPE"));
      m_ElementNameIconMap.put("Argument", RESOURCE_BUNDLE.getString("IDI_ARGUMENT"));
      m_ElementNameIconMap.put("Artifact", RESOURCE_BUNDLE.getString("IDI_ARTIFACT"));
      m_ElementNameIconMap.put("AssemblyConnector", RESOURCE_BUNDLE.getString("IDI_ASSEMBLYCONNECTOR"));
      m_ElementNameIconMap.put("AssignmentAction", RESOURCE_BUNDLE.getString("IDI_ASSIGNMENTACTION"));
      m_ElementNameIconMap.put("Association", RESOURCE_BUNDLE.getString("IDI_ASSOCIATION"));
      m_ElementNameIconMap.put("Navigable_Association", RESOURCE_BUNDLE.getString("IDI_NAV_ASSOCIATION"));
      m_ElementNameIconMap.put("AssociationClass", RESOURCE_BUNDLE.getString("IDI_ASSOCIATIONCLASS"));
      m_ElementNameIconMap.put("AssociationEnd", RESOURCE_BUNDLE.getString("IDI_ASSOCIATIONEND"));
      m_ElementNameIconMap.put("AtomicFragment", RESOURCE_BUNDLE.getString("IDI_ATOMICFRAGMENT"));
      m_ElementNameIconMap.put("Attribute", RESOURCE_BUNDLE.getString("IDI_ATTRIBUTE"));
      m_ElementNameIconMap.put("Binding", RESOURCE_BUNDLE.getString("IDI_NOICON"));
      m_ElementNameIconMap.put("CallAction", RESOURCE_BUNDLE.getString("IDI_CALLACTION"));
      m_ElementNameIconMap.put("CallEvent", RESOURCE_BUNDLE.getString("IDI_CALLEVENT"));
      m_ElementNameIconMap.put("ChangeEvent", RESOURCE_BUNDLE.getString("IDI_CHANGEEVENT"));
      m_ElementNameIconMap.put("ChangeSignal", RESOURCE_BUNDLE.getString("IDI_CHANGESIGNAL"));
      m_ElementNameIconMap.put("ChoicePseudoState", RESOURCE_BUNDLE.getString("IDI_CHOICEPSEUDOSTATE"));
      m_ElementNameIconMap.put("Collaboration", RESOURCE_BUNDLE.getString("IDI_COLLABORATION"));
      m_ElementNameIconMap.put("CollaborationLifeline", RESOURCE_BUNDLE.getString("IDI_LIFELINE"));
      m_ElementNameIconMap.put("CollaborationOccurrence", RESOURCE_BUNDLE.getString("IDI_COLLABORATIONOCCURRENCE"));
      m_ElementNameIconMap.put("CombinedFragment", RESOURCE_BUNDLE.getString("IDI_COMBINEDFRAGMENT"));
      m_ElementNameIconMap.put("Comment", RESOURCE_BUNDLE.getString("IDI_COMMENT"));
      m_ElementNameIconMap.put("CommentLink", RESOURCE_BUNDLE.getString("IDI_COMMENTLINK"));
      m_ElementNameIconMap.put("ComplexActivityGroup", RESOURCE_BUNDLE.getString("IDI_COMPLEXACTIVITYGROUP"));
      m_ElementNameIconMap.put("Component", RESOURCE_BUNDLE.getString("IDI_COMPONENT"));
      m_ElementNameIconMap.put("CompositeState", RESOURCE_BUNDLE.getString("IDI_COMPOSITESTATE"));
      m_ElementNameIconMap.put("Connector", RESOURCE_BUNDLE.getString("IDI_CONNECTOR"));
      m_ElementNameIconMap.put("ConnectorEnd", RESOURCE_BUNDLE.getString("IDI_CONNECTOREND"));
      m_ElementNameIconMap.put("Constraint", RESOURCE_BUNDLE.getString("IDI_CONSTRAINT"));
      m_ElementNameIconMap.put("Container", RESOURCE_BUNDLE.getString("IDI_CONTAINER"));
      m_ElementNameIconMap.put("ControlFlow", RESOURCE_BUNDLE.getString("IDI_CONTROLFLOW"));
      m_ElementNameIconMap.put("ControlNodes", RESOURCE_BUNDLE.getString("IDI_DIAGRAM_ACTIVITY"));
      m_ElementNameIconMap.put("CreateAction", RESOURCE_BUNDLE.getString("IDI_CREATEACTION"));
      m_ElementNameIconMap.put("DataType", RESOURCE_BUNDLE.getString("IDI_DATATYPE"));
      m_ElementNameIconMap.put("DataStoreNode", RESOURCE_BUNDLE.getString("IDI_DATASTORENODE"));
      m_ElementNameIconMap.put("DecisionNode", RESOURCE_BUNDLE.getString("IDI_DECISIONNODE"));
      m_ElementNameIconMap.put("DecisionMergeNode", RESOURCE_BUNDLE.getString("IDI_DECISIONNODE"));
      m_ElementNameIconMap.put("DeepHistoryState", RESOURCE_BUNDLE.getString("IDI_DEEPHISTORYSTATE"));
      m_ElementNameIconMap.put("Delegation", RESOURCE_BUNDLE.getString("IDI_DELEGATION"));
      m_ElementNameIconMap.put("Delegate", RESOURCE_BUNDLE.getString("IDI_DELEGATE"));
      m_ElementNameIconMap.put("DelegationConnector", RESOURCE_BUNDLE.getString("IDI_DELEGATIONCONNECTOR"));
      m_ElementNameIconMap.put("Dependency", RESOURCE_BUNDLE.getString("IDI_DEPENDENCY"));
      m_ElementNameIconMap.put("Deployment", RESOURCE_BUNDLE.getString("IDI_DEPLOYMENT"));
      m_ElementNameIconMap.put("DeploymentSpecification", RESOURCE_BUNDLE.getString("IDI_DEPLOYMENTSPECIFICATION"));
      m_ElementNameIconMap.put("Derivation", RESOURCE_BUNDLE.getString("IDI_DERIVATION"));
      m_ElementNameIconMap.put("DerivationClassifier", RESOURCE_BUNDLE.getString("IDI_DERIVATIONCLASSIFIER"));
      m_ElementNameIconMap.put("DestroyAction", RESOURCE_BUNDLE.getString("IDI_DESTROYACTION"));
      m_ElementNameIconMap.put("ElementImport", RESOURCE_BUNDLE.getString("IDI_ELEMENTIMPORT"));
      m_ElementNameIconMap.put("EntryPointState", RESOURCE_BUNDLE.getString("IDI_ENTRYPOINTSTATE"));
      m_ElementNameIconMap.put("Enumeration", RESOURCE_BUNDLE.getString("IDI_ENUMERATION"));
      m_ElementNameIconMap.put("EnumerationLiteral", RESOURCE_BUNDLE.getString("IDI_ENUMERATIONLITERAL"));
      m_ElementNameIconMap.put("Exception", RESOURCE_BUNDLE.getString("IDI_EXCEPTION"));
      m_ElementNameIconMap.put("Expression", RESOURCE_BUNDLE.getString("IDI_EXPRESSION"));
      m_ElementNameIconMap.put("Extend", RESOURCE_BUNDLE.getString("IDI_EXTEND"));
      m_ElementNameIconMap.put("ExtensionPoint", RESOURCE_BUNDLE.getString("IDI_EXTENSIONPOINT"));
      m_ElementNameIconMap.put("FinalState", RESOURCE_BUNDLE.getString("IDI_FINALSTATE"));
      m_ElementNameIconMap.put("Flow", RESOURCE_BUNDLE.getString("IDI_FLOW"));
      m_ElementNameIconMap.put("FlowFinalNode", RESOURCE_BUNDLE.getString("IDI_FLOWFINALNODE"));
      m_ElementNameIconMap.put("FlowFinal", RESOURCE_BUNDLE.getString("IDI_FLOWFINALNODE"));
      m_ElementNameIconMap.put("ForkNode", RESOURCE_BUNDLE.getString("IDI_FORKNODE"));
      m_ElementNameIconMap.put("Gate", RESOURCE_BUNDLE.getString("IDI_GATE"));
      m_ElementNameIconMap.put("Generalization", RESOURCE_BUNDLE.getString("IDI_GENERALIZATION"));
      m_ElementNameIconMap.put("Graphic", RESOURCE_BUNDLE.getString("IDI_GRAPHIC"));
      m_ElementNameIconMap.put("Implementation", RESOURCE_BUNDLE.getString("IDI_IMPLEMENTATION"));
      m_ElementNameIconMap.put("Increment", RESOURCE_BUNDLE.getString("IDI_INCREMENT"));
      m_ElementNameIconMap.put("Include", RESOURCE_BUNDLE.getString("IDI_INCLUDE"));
      m_ElementNameIconMap.put("Initial", RESOURCE_BUNDLE.getString("IDI_INITIAL"));
      m_ElementNameIconMap.put("InitialNode", RESOURCE_BUNDLE.getString("IDI_INITIALNODE"));
      m_ElementNameIconMap.put("InitialState", RESOURCE_BUNDLE.getString("IDI_INITIALSTATE"));
      m_ElementNameIconMap.put("Interaction", RESOURCE_BUNDLE.getString("IDI_INTERACTION"));
      m_ElementNameIconMap.put("InteractionConstraint", RESOURCE_BUNDLE.getString("IDI_INTERACTIONCONSTRAINT"));
      m_ElementNameIconMap.put("InteractionFragment", RESOURCE_BUNDLE.getString("IDI_INTERACTIONOCCURRENCE"));
      m_ElementNameIconMap.put("InteractionOccurrence", RESOURCE_BUNDLE.getString("IDI_INTERACTIONOCCURRENCE"));
      m_ElementNameIconMap.put("InteractionOperand", RESOURCE_BUNDLE.getString("IDI_INTERACTIONOPERAND"));
      m_ElementNameIconMap.put("Interface", RESOURCE_BUNDLE.getString("IDI_INTERFACE"));
      m_ElementNameIconMap.put("InterGateConnector", RESOURCE_BUNDLE.getString("IDI_INTERGATECONNECTOR"));
      m_ElementNameIconMap.put("InterLifelineConnector", RESOURCE_BUNDLE.getString("IDI_INTERLIFELINECONNECTOR"));
      m_ElementNameIconMap.put("InterruptibleActivityRegion", RESOURCE_BUNDLE.getString("IDI_INTERRUPTIBLEACTIVITYREGION"));
      m_ElementNameIconMap.put("Invocation", RESOURCE_BUNDLE.getString("IDI_INVOCATIONNODE"));
      m_ElementNameIconMap.put("InvocationNode", RESOURCE_BUNDLE.getString("IDI_INVOCATIONNODE"));
      m_ElementNameIconMap.put("JoinForkNode", RESOURCE_BUNDLE.getString("IDI_JOINNODE"));
      m_ElementNameIconMap.put("JoinNode", RESOURCE_BUNDLE.getString("IDI_JOINNODE"));
      m_ElementNameIconMap.put("JoinorMerge", RESOURCE_BUNDLE.getString("IDI_JOINNODE"));
      m_ElementNameIconMap.put("JoinState", RESOURCE_BUNDLE.getString("IDI_JOINNODE"));
      m_ElementNameIconMap.put("JunctionState", RESOURCE_BUNDLE.getString("IDI_JUNCTIONSTATE"));
      m_ElementNameIconMap.put("Label", RESOURCE_BUNDLE.getString("IDI_LABEL"));
      m_ElementNameIconMap.put("Lifeline", RESOURCE_BUNDLE.getString("IDI_LIFELINE"));
      m_ElementNameIconMap.put("MergeNode", RESOURCE_BUNDLE.getString("IDI_MERGENODE"));
      m_ElementNameIconMap.put("Message", RESOURCE_BUNDLE.getString("IDI_MESSAGE"));
      m_ElementNameIconMap.put("MessageConnector", RESOURCE_BUNDLE.getString("IDI_CONNECTOR"));
      m_ElementNameIconMap.put("Model", RESOURCE_BUNDLE.getString("IDI_MODEL"));
      m_ElementNameIconMap.put("MultiFlow", RESOURCE_BUNDLE.getString("IDI_MULTIFLOW"));
      m_ElementNameIconMap.put("Multiplicity", RESOURCE_BUNDLE.getString("IDI_MULTIPLICITY"));
      m_ElementNameIconMap.put("MultiplicityRange", RESOURCE_BUNDLE.getString("IDI_MULTIPLICITYRANGE"));
      m_ElementNameIconMap.put("NavigableEnd", RESOURCE_BUNDLE.getString("IDI_NAVIGABLEASSOCIATIONEND"));
      m_ElementNameIconMap.put("NestedLink", RESOURCE_BUNDLE.getString("IDI_NESTED"));
      m_ElementNameIconMap.put("Node", RESOURCE_BUNDLE.getString("IDI_NODE"));
      m_ElementNameIconMap.put("ObjectFlow", RESOURCE_BUNDLE.getString("IDI_OBJECTFLOW"));
      m_ElementNameIconMap.put("ObjectNode", RESOURCE_BUNDLE.getString("IDI_OBJECTNODE"));
      m_ElementNameIconMap.put("ObjectNodes", RESOURCE_BUNDLE.getString("IDI_OBJECTNODE"));
      m_ElementNameIconMap.put("Operation", RESOURCE_BUNDLE.getString("IDI_OPERATION"));
      m_ElementNameIconMap.put("Package", RESOURCE_BUNDLE.getString("IDI_PACKAGE"));
      m_ElementNameIconMap.put("PackageImport", RESOURCE_BUNDLE.getString("IDI_PACKAGEIMPORT"));
      m_ElementNameIconMap.put("Parameter", RESOURCE_BUNDLE.getString("IDI_PARAMETER"));
      m_ElementNameIconMap.put("ParameterUsageNode", RESOURCE_BUNDLE.getString("IDI_PARAMETERUSAGENODE"));
      m_ElementNameIconMap.put("Part", RESOURCE_BUNDLE.getString("IDI_PART"));
      m_ElementNameIconMap.put("PartDecomposition", RESOURCE_BUNDLE.getString("IDI_PARTDECOMPOSITION"));
      m_ElementNameIconMap.put("PartFacade", RESOURCE_BUNDLE.getString("IDI_CLASSIFIERROLE"));
      m_ElementNameIconMap.put("Partition", RESOURCE_BUNDLE.getString("IDI_PARTITION"));
      m_ElementNameIconMap.put("Permission", RESOURCE_BUNDLE.getString("IDI_PERMISSION"));
      m_ElementNameIconMap.put("Port", RESOURCE_BUNDLE.getString("IDI_PORT"));
      m_ElementNameIconMap.put("PortProvidedInterface", RESOURCE_BUNDLE.getString("IDI_PORTPROVIDEDEDGE"));
      m_ElementNameIconMap.put("Ports", RESOURCE_BUNDLE.getString("IDI_PORTS"));
      m_ElementNameIconMap.put("Presentation", RESOURCE_BUNDLE.getString("IDI_NOICON"));
      m_ElementNameIconMap.put("PrimitiveType", RESOURCE_BUNDLE.getString("IDI_PRIMITIVETYPE"));
      m_ElementNameIconMap.put("Procedure", RESOURCE_BUNDLE.getString("IDI_PROCEDURE"));
      m_ElementNameIconMap.put("Profile", RESOURCE_BUNDLE.getString("IDI_PROFILE"));
      m_ElementNameIconMap.put("ProtocolConformance", RESOURCE_BUNDLE.getString("IDI_PROTOCOLCONFORMANCE"));
      m_ElementNameIconMap.put("ProtocolTransition", RESOURCE_BUNDLE.getString("IDI_PROTOCOLTRANSITION"));
      m_ElementNameIconMap.put("PseudoState", RESOURCE_BUNDLE.getString("IDI_PSEUDOSTATE"));
      m_ElementNameIconMap.put("Qualifier", RESOURCE_BUNDLE.getString("IDI_QUALIFIER"));
      m_ElementNameIconMap.put("Realization", RESOURCE_BUNDLE.getString("IDI_REALIZATION"));
      m_ElementNameIconMap.put("Reception", RESOURCE_BUNDLE.getString("IDI_RECEPTION"));
      m_ElementNameIconMap.put("Reference", RESOURCE_BUNDLE.getString("IDI_REFERENCE"));
      m_ElementNameIconMap.put("ReferencedLibrary", RESOURCE_BUNDLE.getString("IDI_REFERENCEDLIBRARY"));
      m_ElementNameIconMap.put("Region", RESOURCE_BUNDLE.getString("IDI_REGION"));
      m_ElementNameIconMap.put("Regions", RESOURCE_BUNDLE.getString("IDI_REGIONS"));
      m_ElementNameIconMap.put("ReturnAction", RESOURCE_BUNDLE.getString("IDI_RETURNACTION"));
      m_ElementNameIconMap.put("RobustnessClass", RESOURCE_BUNDLE.getString("IDI_CONTROLCLASS"));
      m_ElementNameIconMap.put("RoleBinding", RESOURCE_BUNDLE.getString("IDI_ROLEBINDING"));
      m_ElementNameIconMap.put("SendAction", RESOURCE_BUNDLE.getString("IDI_SENDACTION"));
      m_ElementNameIconMap.put("Signal", RESOURCE_BUNDLE.getString("IDI_SIGNAL"));
      m_ElementNameIconMap.put("SignalNode", RESOURCE_BUNDLE.getString("IDI_SIGNALNODE"));
      m_ElementNameIconMap.put("SignalEvent", RESOURCE_BUNDLE.getString("IDI_SIGNALEVENT"));
      m_ElementNameIconMap.put("SimpleState", RESOURCE_BUNDLE.getString("IDI_SIMPLESTATE"));
      m_ElementNameIconMap.put("ShallowHistoryState", RESOURCE_BUNDLE.getString("IDI_SHALLOWHISTORYSTATE"));
      m_ElementNameIconMap.put("SourceFileArtifact", RESOURCE_BUNDLE.getString("IDI_SOURCEFILEARTIFACT"));
      m_ElementNameIconMap.put("State", RESOURCE_BUNDLE.getString("IDI_STATE"));
      m_ElementNameIconMap.put("StateNodes", RESOURCE_BUNDLE.getString("IDI_DIAGRAM_STATE"));
      m_ElementNameIconMap.put("StateGroup", RESOURCE_BUNDLE.getString("IDI_STATEGROUP"));
      m_ElementNameIconMap.put("StateMachine", RESOURCE_BUNDLE.getString("IDI_STATEMACHINE"));
      m_ElementNameIconMap.put("Stereotype", RESOURCE_BUNDLE.getString("IDI_STEREOTYPE"));
      m_ElementNameIconMap.put("SubmachineState", RESOURCE_BUNDLE.getString("IDI_SUBMACHINESTATE"));
      m_ElementNameIconMap.put("Subsystem", RESOURCE_BUNDLE.getString("IDI_SUBSYSTEM"));
      m_ElementNameIconMap.put("SubDetail", RESOURCE_BUNDLE.getString("IDI_USECASESUBDETAIL"));
      m_ElementNameIconMap.put("TaggedValue", RESOURCE_BUNDLE.getString("IDI_TAGGEDVALUE"));
      m_ElementNameIconMap.put("TemplateArgument", RESOURCE_BUNDLE.getString("IDI_TEMPLATEARGUMENT"));
      m_ElementNameIconMap.put("TemplateBinding", RESOURCE_BUNDLE.getString("IDI_TEMPLATEBINDING"));
      m_ElementNameIconMap.put("TemplateParameter", RESOURCE_BUNDLE.getString("IDI_TEMPLATEPARAMETER"));
      m_ElementNameIconMap.put("TerminateAction", RESOURCE_BUNDLE.getString("IDI_TERMINATEACTION"));
      m_ElementNameIconMap.put("TimeEvent", RESOURCE_BUNDLE.getString("IDI_TIMEEVENT"));
      m_ElementNameIconMap.put("TimeSignal", RESOURCE_BUNDLE.getString("IDI_TIMESIGNAL"));
      m_ElementNameIconMap.put("Transition", RESOURCE_BUNDLE.getString("IDI_TRANSITION"));
      m_ElementNameIconMap.put("UMLConnectionPoint", RESOURCE_BUNDLE.getString("IDI_UMLCONNECTIONPOINT"));
      m_ElementNameIconMap.put("UninterpretedAction", RESOURCE_BUNDLE.getString("IDI_UNINTERPRETEDACTION"));
      m_ElementNameIconMap.put("Usage", RESOURCE_BUNDLE.getString("IDI_USAGE"));
      m_ElementNameIconMap.put("UseCase", RESOURCE_BUNDLE.getString("IDI_USECASE"));
      m_ElementNameIconMap.put("UseCaseDetail", RESOURCE_BUNDLE.getString("IDI_USECASEDETAIL"));
      m_ElementNameIconMap.put("VBAModule", RESOURCE_BUNDLE.getString("IDI_VBAMODULE"));
      m_ElementNameIconMap.put("VBAProject", RESOURCE_BUNDLE.getString("IDI_VBAPROJECT"));
      m_ElementNameIconMap.put("Diagram", RESOURCE_BUNDLE.getString("IDI_DIAGRAM"));
      m_ElementNameIconMap.put("ActivityDiagram", RESOURCE_BUNDLE.getString("IDI_DIAGRAM_ACTIVITY"));
      m_ElementNameIconMap.put("ClassDiagram", RESOURCE_BUNDLE.getString("IDI_DIAGRAM_CLASS"));
      m_ElementNameIconMap.put("CollaborationDiagram", RESOURCE_BUNDLE.getString("IDI_DIAGRAM_COLLABORATION"));
      m_ElementNameIconMap.put("ComponentDiagram", RESOURCE_BUNDLE.getString("IDI_DIAGRAM_COMPONENT"));
      m_ElementNameIconMap.put("DeploymentDiagram", RESOURCE_BUNDLE.getString("IDI_DIAGRAM_DEPLOYMENT"));
      m_ElementNameIconMap.put("ImplementationDiagram", RESOURCE_BUNDLE.getString("IDI_DIAGRAM_IMPLEMENTATION"));
      m_ElementNameIconMap.put("RobustnessDiagram", RESOURCE_BUNDLE.getString("IDI_DIAGRAM_ROBUSTNESS"));
      m_ElementNameIconMap.put("SequenceDiagram", RESOURCE_BUNDLE.getString("IDI_DIAGRAM_SEQUENCE"));
      m_ElementNameIconMap.put("StateDiagram", RESOURCE_BUNDLE.getString("IDI_DIAGRAM_STATE"));
      m_ElementNameIconMap.put("SummaryDiagram", RESOURCE_BUNDLE.getString("IDI_DIAGRAM_SUMMARY"));
      m_ElementNameIconMap.put("UseCaseDiagram", RESOURCE_BUNDLE.getString("IDI_DIAGRAM_USECASE"));

      // Folder icons
      m_ElementNameIconMap.put("ConstraintFolder", RESOURCE_BUNDLE.getString("IDI_CONSTRAINTSPACKAGEICON"));
      m_ElementNameIconMap.put("TagDefinitionFolder", RESOURCE_BUNDLE.getString("IDI_TAGDEFINITIONPACKAGEICON"));
      m_ElementNameIconMap.put("DataTypeFolder", RESOURCE_BUNDLE.getString("IDI_DATATYPEPACKAGEICON"));
      m_ElementNameIconMap.put("Model Elements", RESOURCE_BUNDLE.getString("IDI_MODEL_ELEMENTS"));
      m_ElementNameIconMap.put("Diagrams", RESOURCE_BUNDLE.getString("IDI_DIAGRAMS"));
      
      // Other
      m_ElementNameIconMap.put("Project", RESOURCE_BUNDLE.getString("IDI_PROJECT"));
      m_ElementNameIconMap.put("WSProject", RESOURCE_BUNDLE.getString("IDI_PROJECT"));
      m_ElementNameIconMap.put("NewProperty", RESOURCE_BUNDLE.getString("IDI_NEWPROPERTY"));
      
      // Collections
      m_ElementNameIconMap.put("ActivityPartitions", RESOURCE_BUNDLE.getString("IDI_ACTIVITYPARTITION"));
      m_ElementNameIconMap.put("Aggregations", RESOURCE_BUNDLE.getString("IDI_AGGREGATION"));
      m_ElementNameIconMap.put("AnnotatedElements", RESOURCE_BUNDLE.getString("IDI_ANNOTATEDELEMENT"));
      m_ElementNameIconMap.put("Artifacts", RESOURCE_BUNDLE.getString("IDI_ARTIFACT"));
      m_ElementNameIconMap.put("AssociatedArtifacts", RESOURCE_BUNDLE.getString("IDI_ARTIFACT"));
      m_ElementNameIconMap.put("AssociatedDiagrams", RESOURCE_BUNDLE.getString("IDI_DIAGRAM"));
      m_ElementNameIconMap.put("AssociatedElements", RESOURCE_BUNDLE.getString("IDI_REFERRED"));
      m_ElementNameIconMap.put("Associations", RESOURCE_BUNDLE.getString("IDI_ASSOCIATION"));
      m_ElementNameIconMap.put("Attributes", RESOURCE_BUNDLE.getString("IDI_ATTRIBUTE"));
      m_ElementNameIconMap.put("Bindings", RESOURCE_BUNDLE.getString("IDI_NOICON"));
      m_ElementNameIconMap.put("ClientDependencies", RESOURCE_BUNDLE.getString("IDI_DEPENDENCY"));
      m_ElementNameIconMap.put("Constraints", RESOURCE_BUNDLE.getString("IDI_CONSTRAINT"));
      m_ElementNameIconMap.put("Contents", RESOURCE_BUNDLE.getString("IDI_SUBMACHINECONTENTS"));
      m_ElementNameIconMap.put("Dependencies", RESOURCE_BUNDLE.getString("IDI_DEPENDENCY"));
      m_ElementNameIconMap.put("DeploymentDescriptors", RESOURCE_BUNDLE.getString("IDI_NOICON"));
      m_ElementNameIconMap.put("Ends", RESOURCE_BUNDLE.getString("IDI_ASSOCIATIONEND"));
      m_ElementNameIconMap.put("EnumerationLiterals", RESOURCE_BUNDLE.getString("IDI_ENUMERATIONLITERALS"));
      m_ElementNameIconMap.put("Extends", RESOURCE_BUNDLE.getString("IDI_EXTEND"));
      m_ElementNameIconMap.put("ExtensionPoints", RESOURCE_BUNDLE.getString("IDI_EXTENSIONPOINT"));
      m_ElementNameIconMap.put("ExternalInterfaces", RESOURCE_BUNDLE.getString("IDI_INTERFACE"));
      m_ElementNameIconMap.put("Generalizations", RESOURCE_BUNDLE.getString("IDI_GENERALIZATION"));
      m_ElementNameIconMap.put("Groups", RESOURCE_BUNDLE.getString("IDI_COMPLEXACTIVITYGROUP"));
      m_ElementNameIconMap.put("Implementations", RESOURCE_BUNDLE.getString("IDI_IMPLEMENTATION"));
      m_ElementNameIconMap.put("ImportedPackages", RESOURCE_BUNDLE.getString("IDI_IMPORTEDPACKAGES"));
      m_ElementNameIconMap.put("ImportedElements", RESOURCE_BUNDLE.getString("IDI_IMPORTEDELEMENTS"));
      m_ElementNameIconMap.put("Includes", RESOURCE_BUNDLE.getString("IDI_INCLUDE"));
      m_ElementNameIconMap.put("IncomingEdges", RESOURCE_BUNDLE.getString("IDI_MULTIFLOW"));
      m_ElementNameIconMap.put("IncomingTransitions", RESOURCE_BUNDLE.getString("IDI_TRANSITION"));
      m_ElementNameIconMap.put("Literals", RESOURCE_BUNDLE.getString("IDI_ENUMERATIONLITERAL"));
      m_ElementNameIconMap.put("Messages", RESOURCE_BUNDLE.getString("IDI_MESSAGE"));
      m_ElementNameIconMap.put("MultiplicityRanges", RESOURCE_BUNDLE.getString("IDI_MULTIPLICITYRANGE"));
      m_ElementNameIconMap.put("NodeContents", RESOURCE_BUNDLE.getString("IDI_INVOCATIONNODE"));
      m_ElementNameIconMap.put("Operands", RESOURCE_BUNDLE.getString("IDI_INTERACTIONOPERAND"));
      m_ElementNameIconMap.put("Operations", RESOURCE_BUNDLE.getString("IDI_OPERATION"));
      m_ElementNameIconMap.put("OutgoingEdges", RESOURCE_BUNDLE.getString("IDI_MULTIFLOW"));
      m_ElementNameIconMap.put("OutgoingTransitions", RESOURCE_BUNDLE.getString("IDI_TRANSITION"));
      m_ElementNameIconMap.put("OwnedElements", RESOURCE_BUNDLE.getString("IDI_OWNEDELEMENTS"));
      m_ElementNameIconMap.put("Parameters", RESOURCE_BUNDLE.getString("IDI_PARAMETER"));
      m_ElementNameIconMap.put("PostConditions", RESOURCE_BUNDLE.getString("IDI_POSTCONDITION"));
      m_ElementNameIconMap.put("PreConditions", RESOURCE_BUNDLE.getString("IDI_PRECONDITION"));
      m_ElementNameIconMap.put("ProvidedInterfaces", RESOURCE_BUNDLE.getString("IDI_INTERFACE"));
      m_ElementNameIconMap.put("Qualifiers", RESOURCE_BUNDLE.getString("IDI_QUALIFIER"));
      m_ElementNameIconMap.put("RaisedExceptions", RESOURCE_BUNDLE.getString("IDI_RAISEDEXCEPTION"));
      m_ElementNameIconMap.put("Realizations", RESOURCE_BUNDLE.getString("IDI_REALIZATION"));
      m_ElementNameIconMap.put("RedefiningAttributes", RESOURCE_BUNDLE.getString("IDI_ATTRIBUTE"));
      m_ElementNameIconMap.put("RedefiningOperations", RESOURCE_BUNDLE.getString("IDI_OPERATION"));
      m_ElementNameIconMap.put("ReferencingReferences", RESOURCE_BUNDLE.getString("IDI_REFERENCING"));
      m_ElementNameIconMap.put("ReferredReferences", RESOURCE_BUNDLE.getString("IDI_REFERRED"));
      m_ElementNameIconMap.put("ReferencedLibraries", RESOURCE_BUNDLE.getString("IDI_REFERENCEDLIBRARY"));
      m_ElementNameIconMap.put("Relationships", RESOURCE_BUNDLE.getString("IDI_RELATIONSHIPS"));
      m_ElementNameIconMap.put("RequiredInterfaces", RESOURCE_BUNDLE.getString("IDI_INTERFACE"));
      m_ElementNameIconMap.put("Specializations", RESOURCE_BUNDLE.getString("IDI_GENERALIZATION"));
      m_ElementNameIconMap.put("Stereotypes", RESOURCE_BUNDLE.getString("IDI_STEREOTYPE"));
      m_ElementNameIconMap.put("SubDetails", RESOURCE_BUNDLE.getString("IDI_USECASESUBDETAIL"));
      m_ElementNameIconMap.put("SubPartitions", RESOURCE_BUNDLE.getString("IDI_ACTIVITYPARTITION"));
      m_ElementNameIconMap.put("SupplierDependencies", RESOURCE_BUNDLE.getString("IDI_DEPENDENCY"));
      m_ElementNameIconMap.put("TaggedValues", RESOURCE_BUNDLE.getString("IDI_TAGGEDVALUE"));
      m_ElementNameIconMap.put("TemplateParameters", RESOURCE_BUNDLE.getString("IDI_TEMPLATEPARAMETER"));
      m_ElementNameIconMap.put("Transitions", RESOURCE_BUNDLE.getString("IDI_TRANSITION"));
      m_ElementNameIconMap.put("UseCaseDetails", RESOURCE_BUNDLE.getString("IDI_USECASEDETAIL"));
      
      // Application icons
      m_ElementNameIconMap.put("Describe", RESOURCE_BUNDLE.getString("IDI_DESCRIBE"));
      
      // Design center stuff
      m_ElementNameIconMap.put("DesignCenter", RESOURCE_BUNDLE.getString("IDI_DESIGNCENTER"));
      m_ElementNameIconMap.put("DesignPatternCatalog", RESOURCE_BUNDLE.getString("IDI_DESIGNCENTER"));
      
      // Requiremnts Stuff.
      m_ElementNameIconMap.put("ReqCategory", RESOURCE_BUNDLE.getString("IDI_REQCATEGORY"));
      m_ElementNameIconMap.put("ReqModelElement", RESOURCE_BUNDLE.getString("IDI_REQMODELELEMENT"));
      m_ElementNameIconMap.put("ReqModelElementFolder", RESOURCE_BUNDLE.getString("IDI_REQMODELELEMENTFOLDER"));
      m_ElementNameIconMap.put("ReqProject", RESOURCE_BUNDLE.getString("IDI_REQPROJECT"));
      m_ElementNameIconMap.put("ReqRequirement", RESOURCE_BUNDLE.getString("IDI_REQREQUIREMENT"));
      m_ElementNameIconMap.put("RequirementArtifact", RESOURCE_BUNDLE.getString("IDI_REQREQUIREMENT"));
      m_ElementNameIconMap.put("Requirement", RESOURCE_BUNDLE.getString("IDI_REQREQUIREMENT"));
      
      // These are the expanded element types
      m_ElementNameIconMap.put("FinalState_Aborted", RESOURCE_BUNDLE.getString("IDI_ABORTEDFINALSTATE"));
      m_ElementNameIconMap.put("AbortedFinalState", RESOURCE_BUNDLE.getString("IDI_ABORTEDFINALSTATE"));
      m_ElementNameIconMap.put("PseudoState_Choice", RESOURCE_BUNDLE.getString("IDI_CHOICEPSEUDOSTATE"));
      m_ElementNameIconMap.put("PseudoState_DeepHistory", RESOURCE_BUNDLE.getString("IDI_DEEPHISTORYSTATE"));
      m_ElementNameIconMap.put("PseudoState_Fork", RESOURCE_BUNDLE.getString("IDI_FORKNODE"));
      m_ElementNameIconMap.put("ForkState", RESOURCE_BUNDLE.getString("IDI_FORKNODE"));
      m_ElementNameIconMap.put("PseudoState_Initial", RESOURCE_BUNDLE.getString("IDI_INITIALSTATE"));
      m_ElementNameIconMap.put("InitialState", RESOURCE_BUNDLE.getString("IDI_INITIALSTATE"));
      m_ElementNameIconMap.put("PseudoState_Join", RESOURCE_BUNDLE.getString("IDI_JOINNODE"));
      m_ElementNameIconMap.put("PseudoState_Junction", RESOURCE_BUNDLE.getString("IDI_JUNCTIONSTATE"));
      m_ElementNameIconMap.put("PseudoState_ShallowHistory", RESOURCE_BUNDLE.getString("IDI_SHALLOWHISTORYSTATE"));
      m_ElementNameIconMap.put("PseudoState_EntryPoint", RESOURCE_BUNDLE.getString("IDI_ENTRYPOINTSTATE"));
      m_ElementNameIconMap.put("PseudoState_Stop", RESOURCE_BUNDLE.getString("IDI_STOPSTATE"));
      m_ElementNameIconMap.put("StopState", RESOURCE_BUNDLE.getString("IDI_STOPSTATE"));
      m_ElementNameIconMap.put("State_Composite", RESOURCE_BUNDLE.getString("IDI_COMPOSITESTATE"));
      m_ElementNameIconMap.put("CompositeState", RESOURCE_BUNDLE.getString("IDI_COMPOSITESTATE"));
      m_ElementNameIconMap.put("State_SubMachine", RESOURCE_BUNDLE.getString("IDI_SUBMACHINESTATE"));
      m_ElementNameIconMap.put("DesignPattern", RESOURCE_BUNDLE.getString("IDI_DESIGNPATTERN"));
      m_ElementNameIconMap.put("PartFacade_Actor", RESOURCE_BUNDLE.getString("IDI_ACTORROLE"));
      m_ElementNameIconMap.put("PartFacade_Class", RESOURCE_BUNDLE.getString("IDI_CLASSROLE"));
      m_ElementNameIconMap.put("PartFacade_Interface", RESOURCE_BUNDLE.getString("IDI_INTERFACEROLE"));
      m_ElementNameIconMap.put("PartFacade_UseCase", RESOURCE_BUNDLE.getString("IDI_USECASEROLE"));
      // needed this both ways - for the project tree and property definitions
      m_ElementNameIconMap.put("PartFacadeActor", RESOURCE_BUNDLE.getString("IDI_ACTORROLE"));
      m_ElementNameIconMap.put("PartFacadeClass", RESOURCE_BUNDLE.getString("IDI_CLASSROLE"));
      m_ElementNameIconMap.put("PartFacadeInterface", RESOURCE_BUNDLE.getString("IDI_INTERFACEROLE"));
      m_ElementNameIconMap.put("PartFacadeUseCase", RESOURCE_BUNDLE.getString("IDI_USECASEROLE"));
      
      // SCM Icons
      m_ElementNameIconMap.put("CHECKEDOUT", RESOURCE_BUNDLE.getString("IDI_CHECKEDOUT"));
      m_ElementNameIconMap.put("VERSION_CONTROLLED", RESOURCE_BUNDLE.getString("IDI_VERSION_CONTROLLED"));
      
   }
   
   /**
    * Returns an HICON (as a long) for the name.  sKeyname may be an element type ie Class.    The caller manages
    * the HICON destruction.
    *
    * @param sKeyname [in] The key to be used to find the icon
    * @param pIcon [out,retval] The HICON that represents this dispatch type.
    */
   public Icon getIconForElementType(String sKeyname)
   {
      String iconLocation = getIconDetailsForElementType(sKeyname);
      Image img = ProductHelper.getProxyUserInterface().getResource(iconLocation);
      //URL url = this.getClass().getClassLoader().getResource(iconLocation);
      //URL url = ClassLoader.getSystemClassLoader().getResource(iconLocation);
      //URL url = ClassLoader.getSystemClassLoader().getResource("Class.png");
      
      //File f = new File(iconLocation);
      if (img != null)
      {
         ImageIcon icon = new ImageIcon(img);
         //ImageIcon icon = new ImageIcon(iconLocation);
         return icon;
      }
      return null;
   }
   
   public Icon getIconForFile(String iconLocation)
   {
       
      Image img = ProductHelper.getProxyUserInterface().getResource(iconLocation);
      if (img != null)
      {
         ImageIcon icon = new ImageIcon(img);
         //ImageIcon icon = new ImageIcon(iconLocation);
         return icon;
      }
//		URL url = ClassLoader.getSystemClassLoader().getResource(iconLocation);
//
//		File f = new File(iconLocation);
//	  	if (url != null)
//	  	{
//			ImageIcon icon = new ImageIcon(url);
//			return icon;
//	  	}
      return null;
   }
   
   /**
    * Returns an HICON (as a long) for the IDispatch.  The pDisp can be an IElement or an IDiagram.
    * The caller manages the HICON destruction.
    *
    * @param pDisp [in] The dispatch that is an element or diagram.  The element name or diagram type is
    * used as the keyname into the map.
    * @param pIcon [out,retval] The HICON that represents this dispatch type.
    */
   public Icon getIconForDisp(Object pDisp)
   {
      Icon icon = null;
      String searchStr = getSearchString(pDisp);
      if (searchStr != null && searchStr.length() > 0)
      {
         icon = getIconForElementType(searchStr);
      }
      return icon;
   }
   
   /**
    * Returns an HICON (as a long) for the diagram kind (of type DiagramKind).   The
    * caller manages the HICON destruction.
    *
    * @param nDiagramKind [in] The DiagramKind of this diagram
    * @param pIcon [out,retval] The HICON that represents this dispatch type.
    */
   public Icon getIconForDiagramKind(int nDiagramKind)
   {
      Icon icon = null;
      IDiagramTypesManager pManager = DiagramTypesManager.instance();
      String displayName = pManager.getDiagramTypeNameNoSpaces(nDiagramKind);
      if (displayName.length() > 0)
      {
         icon = getIconForElementType(displayName);
      }
      return icon;
   }
   
   /**
    * Returns the information about a specific icon.  Use to load the icon yourself - for
    * instance if you need to put into an image list.
    *
    * @param sKeyname [in] The key to be used to find the icon
    * @param sIconLibrary [out] The dll where this icon lives (this one!)
    * @param nIconID [out] The id of the icon
    */
   public String getIconDetailsForElementType(String sKeyname)
   {
      // First look for the string as the user passed it to us, if not found
      // then strip spaces and look again just in case the user of this routine
      // is a little sloppy.
      String iconLocation ="";
      if (m_ElementNameIconMap.containsKey(sKeyname))
      {
         iconLocation = m_ElementNameIconMap.get(sKeyname);
      }
      else
      {
         String newKey = StringUtilities.replaceAllSubstrings(sKeyname," ","");
         if (m_ElementNameIconMap.containsKey(newKey))
         {
            iconLocation = m_ElementNameIconMap.get(newKey);
         }
      }
      return iconLocation;
   }
   
   /**
    * Returns the information about a specific icon for the IDispatch.  The pDisp can be an
    * IElement, IDiagram or IProxyDiagram..  Use to load the icon yourself - for instance if you
    * need to put into an image list.
    *
    * @param pDisp [in] The dispatch that is an element or diagram.  The element name or diagram type is
    * @param sIconLibrary [out] The dll where this icon lives (this one!)
    * @param nIconID [out] The id of the icon
    */
   public String getIconDetailsForDisp(Object pDisp)
   {
      String iconLib = null;
      String searchStr = getSearchString(pDisp);
      if (searchStr.length() > 0)
      {
         // Fixed issue 82208, 78848
         // Take care a special case of lifeline elemenent
         // if a lifeline element has an attribute indicating that
         // it is an actor lifeline, then get an Actor icon instead.
         if (pDisp instanceof ILifeline)
         {
            boolean isActorLifeline = ((ILifeline)pDisp).getIsActorLifeline();
            searchStr = (isActorLifeline ? "Actor" : searchStr);
            
         }
         iconLib = getIconDetailsForElementType(searchStr);
      }
      return iconLib;
   }
   
   /**
    * Returns the information about a specific icon for the diagram kind (of type DiagramKind).  Use
    * to load the icon yourself - for instance if you need to put into an image list.
    *
    * @param nDiagramKind [in] The DiagramKind of this diagram
    * @param sIconLibrary [out] The dll where this icon lives (this one!)
    * @param nIconID [out] The id of the icon
    */
   public String getIconDetailsDiagrmaKind(int nDiagramKind)
   {
      String iconLib = null;
      IDiagramTypesManager pManager = DiagramTypesManager.instance();
      String displayName = pManager.getDiagramTypeNameNoSpaces(nDiagramKind);
      if (displayName.length() > 0)
      {
         iconLib = getIconDetailsForElementType(displayName);
      }
      return iconLib;
   }
   
   /**
    * Returns the search string for this disp
    *
    * @param pDisp [in] The dispatch that is an element or diagram.  The element name or diagram type is
    * @param sSearchString [out] The search string that should be used as a key into the map.
    */
   private String getSearchString(Object pDisp)
   {
      String searchStr ="";
      if (pDisp != null)
      {
         if (pDisp instanceof IDiagram)
         {
            IDiagram pDiagram = (IDiagram)pDisp;
            IDiagramTypesManager pManager = DiagramTypesManager.instance();
            String displayName = pManager.getDiagramTypeNameNoSpaces(pDiagram);
            if (displayName != null && displayName.length() > 0)
            {
               searchStr = displayName;
            }
         }
         else if (pDisp instanceof IElement)
         {
            IElement pElement = (IElement)pDisp;
            searchStr = pElement.getExpandedElementType();
         }
         else if (pDisp instanceof IWorkspace)
         {
            searchStr ="Workspace";
         }
         else if (pDisp instanceof IWSProject)
         {
            searchStr ="WSProject";
         }
         else if (pDisp instanceof IProxyDiagram)
         {
            IProxyDiagram pProxyDiagram = (IProxyDiagram)pDisp;
            int diaKind = IDiagramKind.DK_DIAGRAM;
            boolean isOpen = false;
            boolean isValid = false;
            
            diaKind = pProxyDiagram.getDiagramKind();
            isOpen = pProxyDiagram.isOpen();
            isValid = pProxyDiagram.isValidDiagram();
            
            IDiagramTypesManager pManager = DiagramTypesManager.instance();
            String displayName = pManager.getDiagramTypeNameNoSpaces(diaKind);
            if (displayName != null && displayName.length() > 0)
            {
               searchStr = displayName;
            }
          
            if (!isValid && searchStr.length() > 0)
            {
                // searchStr = pManager.getBrokenIcon(diaKind);
                searchStr = pManager.getOpenIcon(diaKind);
            }
            
            else if (!isOpen && searchStr.length() > 0)
            {
                // searchStr = pManager.getClosedIcon(diaKind);
                searchStr = pManager.getOpenIcon(diaKind);
            }
         }
      }
      
      return searchStr;
   }
   
}
