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


package org.netbeans.modules.uml.ui.products.ad.applicationcore;

import org.dom4j.Element;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
//import org.netbeans.modules.uml.ui.support.applicationmanager.PresentationTypesMgrImpl;
//import org.netbeans.modules.uml.ui.support.applicationmanager.TSGraphObjectKind;

/**
 *
 * @author Trey Spiva
 */ //TODO
public class ADPresentationTypesMgrImpl //TODO extends PresentationTypesMgrImpl
{

   /**
    * Create the Invalid DrawEngines Section.
    *
    * This provides the draw engines that are invalid on the various diagrams.
    *
    * @param pParent[in] The parent DOM node for this section
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.PresentationTypesMgrImpl#createInvalidDrawEnginesOnDiagramsSection(org.dom4j.Element)
    */
   protected void createInvalidDrawEnginesOnDiagramsSection(Element rootElement)
   {
      if(rootElement != null)
      {         
//         Element node = XMLManip.createElement(rootElement, getInvalidDrawEnginesSectionName());
//         if(node != null)
//         {     
//            createInvalidDrawEnginesEntry(node,
//                                          CLASS_DIAGRAM,
//                                          "CombinedFragmentDrawEngine");     
//            createInvalidDrawEnginesEntry(node,
//                                          CLASS_DIAGRAM,
//                                          "LifelineDrawEngine");     
//            createInvalidDrawEnginesEntry(node,
//                                          CLASS_DIAGRAM,
//                                          "MessageEdgeDrawEngine"); 
//            createInvalidDrawEnginesEntry(node,
//                                          CLASS_DIAGRAM,
//                                          "NodeDrawEngine"); 
//            createInvalidDrawEnginesEntry(node,
//                                          SEQUENCE_DIAGRAM,
//                                          "InterfaceDrawEngine"); 
//            createInvalidDrawEnginesEntry(node,
//                                          SEQUENCE_DIAGRAM,
//                                          "ClassDrawEngine"); 
//            createInvalidDrawEnginesEntry(node,
//                                          SEQUENCE_DIAGRAM,
//                                          "PackageDrawEngine"); 
//            createInvalidDrawEnginesEntry(node,
//                                          SEQUENCE_DIAGRAM,
//                                          "NodeDrawEngine");        
//         }
      }
   }

   /**
    * Create the InitStrings Section.
    *
    * This maps an initialization string to the metatype, engine and type 
    * (ie node or edge) the TSObjectView should create.
    *
    * @param rootElement The parent DOM node for this section
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.PresentationTypesMgrImpl#createInitStringsSection(org.dom4j.Element)
    */
   protected void createInitStringsSection(Element rootElement)
   {
      if(rootElement != null)
      {         //TODO
//         Element node = XMLManip.createElement(rootElement, getInitStringsSectionName());
//         if(node != null)
//         {  
//            ///////////////////////////////
//            // Pure Presentation Elements
//            ///////////////////////////////
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_RECTANGLE", 
//                                   "Graphic",
//                                   "GraphicDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_ROUNDED_RECTANGLE", 
//                                   "Graphic",
//                                   "GraphicDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_ELLIPSE", 
//                                   "Graphic",
//                                   "GraphicDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_PENTAGON", 
//                                   "Graphic",
//                                   "GraphicDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_HEXAGON1", 
//                                   "Graphic",
//                                   "GraphicDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_HEXAGON2", 
//                                   "Graphic",
//                                   "GraphicDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_OCTAGON", 
//                                   "Graphic",
//                                   "GraphicDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_TRIANGLE", 
//                                   "Graphic",
//                                   "GraphicDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_TRIANGLE_DOWN", 
//                                   "Graphic",
//                                   "GraphicDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_TRIANGLE_LEFT", 
//                                   "Graphic",
//                                   "GraphicDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_TRIANGLE_RIGHT", 
//                                   "Graphic",
//                                   "GraphicDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_DIAMOND", 
//                                   "Graphic",
//                                   "GraphicDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_PARALLELOGRAM", 
//                                   "Graphic",
//                                   "GraphicDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_STAR", 
//                                   "Graphic",
//                                   "GraphicDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_CROSS", 
//                                   "Graphic",
//                                   "GraphicDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//
//             // Nested Link
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge NestedLink", 
//                                   "",
//                                   "NestedLinkDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//
//             // Comment Link
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge CommentEdge", 
//                                   "",
//                                   "CommentEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//
//             // Qualifiers
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Qualifier", 
//                                   "AssociationEnd",
//                                   "QualifierDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//
//             ///////////////////////////////
//             // Bridges
//             ///////////////////////////////
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge AssociationClassInitialEdge", 
//                                   "",
//                                   "BridgeEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge AssemblyConnectorInitialEdge", 
//                                   "",
//                                   "BridgeEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge", 
//                                   "",
//                                   "BridgeEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI", 
//                                   "",
//                                   "BridgeNodeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//
//             ///////////////////////////////
//             // NodeDecorators
//             ///////////////////////////////
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI self", 
//                                   "SelfMessage",
//                                   "LifelineDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_DECORATOR,
//                                        SEQUENCE_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI destroy", 
//                                   "Destroy",
//                                   "LifelineDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_DECORATOR,
//                                        SEQUENCE_DIAGRAM );
//
//             ///////////////////////////////
//             // Comment Nodes
//             ///////////////////////////////
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Comment", 
//                                   "Comment",
//                                   "CommentDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//
//             ///////////////////////////////
//             // Classifier Nodes
//             ///////////////////////////////
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI AssociationClass", 
//                                   "AssociationClass",
//                                   "ClassDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI AssociationClassConnectorDrawEngine", 
//                                   "AssociationClass",
//                                   "AssociationClassConnectorDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Class", 
//                                   "Class",
//                                   "ClassDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI TemplateClass", 
//                                   "Class",
//                                   "ClassDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Class", 
//                                   "Lifeline",
//                                   "LifelineDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE,
//                                        SEQUENCE_DIAGRAM);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Actor", 
//                                   "Actor",
//                                   "ActorDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Actor", 
//                                   "Lifeline",
//                                   "LifelineDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE,
//                                        SEQUENCE_DIAGRAM);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Actor", 
//                                   "Lifeline",
//                                   "CollaborationLifelineDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE,
//                                        COLLABORATION_DIAGRAM);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI BoundaryControllerOrEntity", 
//                                   "Class",
//                                   "ClassRobustnessDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Boundary", 
//                                   "Class",
//                                   "ClassRobustnessDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Controller", 
//                                   "Class",
//                                   "ClassRobustnessDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Entity", 
//                                   "Class",
//                                   "ClassRobustnessDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface LollypopNotation", 
//                                   "Interface",
//                                   "InterfaceDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface ClassNotation", 
//                                   "Interface",
//                                   "ClassDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI DataType", 
//                                   "DataType",
//                                   "DataTypeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI AliasedType", 
//                                   "AliasedType",
//                                   "DataTypeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI UtilityClass", 
//                                   "Class",
//                                   "ClassDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Enumeration", 
//                                   "Enumeration",
//                                   "EnumerationDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI DerivationClassifier", 
//                                   "DerivationClassifier",
//                                   "DerivationClassifierDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Derivation", 
//                                   "Derivation",
//                                   "DerivationEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE );
//             ///////////////////////////////
//             // Package Nodes
//             ///////////////////////////////
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.Package", 
//                                   "Package",
//                                   "PackageDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//             ///////////////////////////////
//             // Interaction Fragment Nodes
//             ///////////////////////////////
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI CombinedFragment", 
//                                   "CombinedFragment",
//                                   "CombinedFragmentDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE,
//                                        SEQUENCE_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI InteractionOccurrence", 
//                                   "InteractionOccurrence",
//                                   "InteractionFragmentDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE,
//                                        SEQUENCE_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI InteractionOccurrence", 
//                                   "InteractionOccurrence",
//                                   "InteractionFragmentDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE,
//                                        COLLABORATION_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI InteractionOccurrence", 
//                                   "InteractionOccurrence",
//                                   "InteractionFragmentDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interaction", 
//                                   "Interaction",
//                                   "InteractionFragmentDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE,
//                                        SEQUENCE_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interaction", 
//                                   "Interaction",
//                                   "InteractionFragmentDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE,
//                                        COLLABORATION_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interaction", 
//                                   "Interaction",
//                                   "InteractionFragmentDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE,
//                                        ACTIVITY_DIAGRAM );
//             ///////////////////////////////
//             // Activity Diagram Nodes & Edges
//             ///////////////////////////////
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ObjectNode ParameterUsageNode", 
//                                   "ParameterUsageNode",
//                                   "ObjectNodeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ObjectNode DataStoreNode", 
//                                   "DataStoreNode",
//                                   "ObjectNodeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ObjectNode Signal", 
//                                   "SignalNode",
//                                   "ObjectNodeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode InitialNode", 
//                                   "InitialNode",
//                                   "ControlNodeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode ActivityFinalNode", 
//                                   "ActivityFinalNode",
//                                   "ControlNodeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode FlowFinalNode", 
//                                   "FlowFinalNode",
//                                   "ControlNodeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode DecisionNode", 
//                                   "DecisionMergeNode",
//                                   "ControlNodeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode MergeNode", 
//                                   "DecisionMergeNode",
//                                   "ControlNodeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode ForkNode", 
//                                   "JoinForkNode",
//                                   "ControlNodeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode ForkNode Horizontal", 
//                                   "JoinForkNode",
//                                   "ControlNodeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode JoinNode", 
//                                   "JoinForkNode",
//                                   "ControlNodeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI InvocationNode", 
//                                   "InvocationNode",
//                                   "InvocationNodeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge ActivityEdge", 
//                                   "ActivityEdge",
//                                   "ActivityEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI InterruptibleActivityRegion", 
//                                   "InterruptibleActivityRegion",
//                                   "ActivityGroupDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI IterationActivityGroup", 
//                                   "IterationActivityGroup",
//                                   "ActivityGroupDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI StructuredActivityGroup", 
//                                   "StructuredActivityGroup",
//                                   "ActivityGroupDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ComplexActivityGroup", 
//                                   "ComplexActivityGroup",
//                                   "ActivityGroupDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Activity", 
//                                   "Activity",
//                                   "ActivityNodeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE,
//                                        ACTIVITY_DIAGRAM );
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Partition", 
//                                   "ActivityPartition",
//                                   "PartitionDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//
//             ///////////////////////////////
//             // UseCase Diagram Nodes
//             ///////////////////////////////
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI UseCase", 
//                                   "UseCase",
//                                   "UseCaseDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Include", 
//                                   "Include",
//                                   "IncludeEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Extend", 
//                                   "Extend",
//                                   "ExtendEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//
//             ///////////////////////////////
//             // Deployment Diagram Nodes
//             ///////////////////////////////
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Artifact", 
//                                   "Artifact",
//                                   "ArtifactDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Node", 
//                                   "Node",
//                                   "ClassNodeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI DeploymentSpecification", 
//                                   "DeploymentSpecification",
//                                   "DeploymentSpecDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//
//             ///////////////////////////////
//             // Component Diagram Nodes & Edges
//             ///////////////////////////////
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Component", 
//                                   "Component",
//                                   "ComponentDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Port", 
//                                   "Port",
//                                   "PortDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge PortProvidedInterface", 
//                                   "Interface",
//                                   "PortProvidedInterfaceEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//
//             ///////////////////////////////
//             // State Diagram Nodes & Edges
//             ///////////////////////////////
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState", 
//                                   "PseudoState",
//                                   "PseudoStateDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState Choice", 
//                                   "PseudoState",
//                                   "PseudoStateDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState EntryPoint", 
//                                   "PseudoState",
//                                   "PseudoStateDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState DeepHistory", 
//                                   "PseudoState",
//                                   "PseudoStateDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState ShallowHistory", 
//                                   "PseudoState",
//                                   "PseudoStateDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState Initial", 
//                                   "PseudoState",
//                                   "PseudoStateDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState Junction", 
//                                   "PseudoState",
//                                   "PseudoStateDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState Join", 
//                                   "PseudoState",
//                                   "PseudoStateDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState Join Horizontal", 
//                                   "PseudoState",
//                                   "PseudoStateDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Transition", 
//                                   "Transition",
//                                   "TransitionEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI CompositeState", 
//                                   "State",
//                                   "StateDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI SimpleState", 
//                                   "State",
//                                   "StateDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI SubmachineState", 
//                                   "State",
//                                   "StateDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI FinalState", 
//                                   "FinalState",
//                                   "FinalStateDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI FinalState Aborted", 
//                                   "FinalState",
//                                   "FinalStateDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI StateMachine", 
//                                   "StateMachine",
//                                   "StateMachineDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//
//             ///////////////////////////////
//             // Edges
//             ///////////////////////////////
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Association", 
//                                   "Association",
//                                   "AssociationEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge AssociationEnd", 
//                                   "AssociationEnd",
//                                   "AssociationEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Aggregation", 
//                                   "Aggregation",
//                                   "AssociationEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Association NN NN", 
//                                   "Association",
//                                   "AssociationEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Aggregation CO NN", 
//                                   "Aggregation",
//                                   "AssociationEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Aggregation AG NN", 
//                                   "Aggregation",
//                                   "AssociationEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Association NN NA", 
//                                   "Association",
//                                   "AssociationEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Aggregation CO NA", 
//                                   "Aggregation",
//                                   "AssociationEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Aggregation AG NA", 
//                                   "Aggregation",
//                                   "AssociationEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Generalization", 
//                                   "Generalization",
//                                   "GeneralizationEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message", 
//                                   "Message",
//                                   "MessageEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message asynchronous", 
//                                   "Message",
//                                   "MessageEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message create", 
//                                   "Message",
//                                   "MessageEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message result", 
//                                   "Message",
//                                   "MessageEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Dependency", 
//                                   "Dependency",
//                                   "DependencyEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge BinaryAssociation", 
//                                   "BinaryAssociation",
//                                   "BinaryAssociationEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Implementation", 
//                                   "Implementation",
//                                   "ImplementationEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Realization", 
//                                   "Realization",
//                                   "DependencyEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Usage", 
//                                   "Usage",
//                                   "DependencyEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Delegate", 
//                                   "Delegate",
//                                   "DependencyEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Permission", 
//                                   "Permission",
//                                   "DependencyEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Abstraction", 
//                                   "Abstraction",
//                                   "DependencyEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//
//             ///////////////////////////////
//             // Collaboration Diagram Nodes & Edges
//             ///////////////////////////////
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI CollaborationLifeline", 
//                                   "Lifeline",
//                                   "CollaborationLifelineDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Collaboration", 
//                                   "Collaboration",
//                                   "CollaborationDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE_RESIZE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Classifier", 
//                                   "PartFacade",
//                                   "ClassDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Interface", 
//                                   "PartFacade",
//                                   "ClassDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade UseCase", 
//                                   "PartFacade",
//                                   "UseCaseDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Actor", 
//                                   "PartFacade",
//                                   "ActorDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Class", 
//                                   "PartFacade",
//                                   "ClassDrawEngine",
//                                   TSGraphObjectKind.TSGOK_NODE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge PartFacade", 
//                                   "PartFacade",
//                                   "PartFacadeEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE);
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Connector", 
//                                   "Connector",
//                                   "ConnectorEdgeDrawEngine",
//                                   TSGraphObjectKind.TSGOK_EDGE,
//                                        COLLABORATION_DIAGRAM);
//
//             ///////////////////////////////
//             // Labels
//             ///////////////////////////////
//            createInitStringsEntry(node,
//                                   "org.netbeans.modules.uml.ui.products.ad.viewfactory.LabelView", 
//                                   "",
//                                   "ADLabelDrawEngine",
//                                   TSGraphObjectKind.TSGOK_LABEL);
//         }
      }
   }

   /**
    * Create the MetaTypes Section.
    *
    * This maps a metatype to a specific initialization string.
    *
    * @param rootElement The parent DOM node for this section
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.PresentationTypesMgrImpl#createPresentationSection(org.dom4j.Element)
    */
   protected void createPresentationSection(Element rootElement)
   {
      if(rootElement != null)
      {         
//         Element node = XMLManip.createElement(rootElement, getPresentationSectionName());
//         if(node != null)
//         {            
//            createPresentationEntry(node,"AssociationEnd","AssociationEdgePresentation");
//            createPresentationEntry(node,"AssociationClass","AssociationEdgePresentation");
//            createPresentationEntry(node,"Association","AssociationEdgePresentation");
//            createPresentationEntry(node,"Aggregation","AggregationEdgePresentation");
//            createPresentationEntry(node,"Implementation","ImplementationEdgePresentation");
//            createPresentationEntry(node,"Dependency","DependencyEdgePresentation");
//            createPresentationEntry(node,"Abstraction","DependencyEdgePresentation");
//            createPresentationEntry(node,"Usage","DependencyEdgePresentation");
//            createPresentationEntry(node,"Permission","DependencyEdgePresentation");
//            createPresentationEntry(node,"Realization","DependencyEdgePresentation");
//            createPresentationEntry(node,"Generalization","GeneralizationEdgePresentation");
//            createPresentationEntry(node,"Message","MessageEdgePresentation");
//            createPresentationEntry(node,"Comment","CommentEdgePresentation");
//            createPresentationEntry(node,"ActivityEdge","ActivityEdgePresentation");
//            createPresentationEntry(node,"ControlFlow","ActivityEdgePresentation");
//            createPresentationEntry(node,"ObjectFlow","ActivityEdgePresentation");
//            createPresentationEntry(node,"MultiFlow","ActivityEdgePresentation");
//            createPresentationEntry(node,"Transition","TransitionEdgePresentation");
//            createPresentationEntry(node,"Include","IncludeEdgePresentation");
//            createPresentationEntry(node,"Extend","ExtendEdgePresentation");
//            createPresentationEntry(node,"Derivation","DerivationEdgePresentation");
//            createPresentationEntry(node,"Delegate","DependencyEdgePresentation");
//            createPresentationEntry(node,"Connector","ConnectorEdgePresentation");
//            createPresentationEntry(node,"Interface","InterfaceEdgePresentation");
//            createPresentationEntry(node,"AssociationEnd","AssociationEdgePresentation");
//         }
      }
   }

   /**
    * Create the MetaTypes Section.
    *
    * This maps a metatype to a specific initialization string.
    * This list is used during Create Diagram from Selected (CDFS) to determine what initialization string to use
    * for the specific element on the specific diagram.  The first section below contains the "generic" initialization
    * string, which is only used if a "specific" string is not found.  The "specific" strings are created by adding
    * the diagram type to the CreateMetaTypesEntry() call.
    *
    * @param pParent[in] The parent DOM node for this section
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.PresentationTypesMgrImpl#createMetaTypesSection(org.dom4j.Element)
    */
   protected void createMetaTypesSection(Element rootElement)
   {
      if(rootElement != null)
      {         
//         Element node = XMLManip.createElement(rootElement, getMetaTypesSectionName());
//         if(node != null)
//         {
//            ////////////////////////////////////////////////////////////////////////////////
//            // Generic initialization strings (maintained alphabetical by element)
//            // These are used if there is not a specific initialization string for the diagram.
//            ////////////////////////////////////////////////////////////////////////////////
//
//            createMetaTypesEntry(node,"Abstraction","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Abstraction" );
//            createMetaTypesEntry(node,"Actor","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Actor");
//            createMetaTypesEntry(node,"Artifact","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Artifact" );
//            createMetaTypesEntry(node,"Aggregation","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Aggregation CO NN");
//            createMetaTypesEntry(node,"Association","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Association NN NN");
//            createMetaTypesEntry(node,"AssociationClass","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI AssociationClass");
//            createMetaTypesEntry(node,"BinaryAssociation","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge BinaryAssociation" );
//            createMetaTypesEntry(node,"Class","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Class");
//            createMetaTypesEntry(node,"DerivationClassifier","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI DerivationClassifier");
//            createMetaTypesEntry(node,"Comment","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Comment");
//            createMetaTypesEntry(node,"Component","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Component" );
//            createMetaTypesEntry(node,"DataType","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI DataType" );
//            createMetaTypesEntry(node,"AliasedType","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI AliasedType" );
//            createMetaTypesEntry(node,"Delegate","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Delegate" );
//            createMetaTypesEntry(node,"Dependency","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Dependency" );
//            createMetaTypesEntry(node,"DeploymentSpecification","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI DeploymentSpecification" );
//            createMetaTypesEntry(node,"Enumeration","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Enumeration" );
//            createMetaTypesEntry(node,"Extend","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Extend" );
//            createMetaTypesEntry(node,"Generalization","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Generalization" );
//            createMetaTypesEntry(node,"Implementation","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Implementation" );
//            createMetaTypesEntry(node,"Include","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Include" );
//            createMetaTypesEntry(node,"Interface","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface ClassNotation" );
//            createMetaTypesEntry(node,"Message","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message" );
//            createMetaTypesEntry(node,"Node","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Node" );
//            createMetaTypesEntry(node,"Package","org.netbeans.modules.uml.ui.products.ad.viewfactory.Package" );
//            createMetaTypesEntry(node,"PartFacade","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Classifier");
//            createMetaTypesEntry(node,"PartFacadeInterface","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Interface");
//            createMetaTypesEntry(node,"PartFacadeClass","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Class");
//            createMetaTypesEntry(node,"PartFacadeUseCase","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade UseCase");
//            createMetaTypesEntry(node,"PartFacadeActor","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Actor");
//            createMetaTypesEntry(node,"Permission","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Permission" );
//            createMetaTypesEntry(node,"Port","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Port" );
//            createMetaTypesEntry(node,"Realization","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Realization" );
//            createMetaTypesEntry(node,"Usage","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Usage" );
//            createMetaTypesEntry(node,"UseCase","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI UseCase" );
//            createMetaTypesEntry(node,"AssociationEnd","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Qualifier" );
//            createMetaTypesEntry(node,"Derivation","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Derivation" );
//
//             ////////////////////////////////////////////////////////////////////////////////
//             // Specific initialization strings (maintained alphabetical by diagram then element)
//             // These are used first, if none is found for the element,
//             // then the generic string (from above) is used.
//             ////////////////////////////////////////////////////////////////////////////////
//
//             // Activity Diagram Metatypes
//            createMetaTypesEntry(node,"Activity","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Activity", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"ActivityEdge","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge ActivityEdge", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"ControlFlow","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge ActivityEdge", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"ObjectFlow","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge ActivityEdge", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"MultiFlow","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge ActivityEdge", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"ActivityFinalNode","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode ActivityFinalNode", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"ActivityPartition","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Partition", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"ComplexActivityGroup","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ComplexActivityGroup", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"DataStoreNode","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ObjectNode DataStoreNode", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"DecisionMergeNode","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode DecisionNode", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"DecisionNode","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode DecisionNode", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"FlowFinalNode","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode FlowFinalNode", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"ForkNode","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode ForkNode", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"InitialNode","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode InitialNode", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"Interaction","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interaction", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"InteractionOccurrence","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI InteractionOccurrence", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"InvocationNode","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI InvocationNode", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"JoinForkNode","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode ForkNode Horizontal", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"JoinNode","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode JoinNode", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"MergeNode","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode MergeNode", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"ParameterUsageNode","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ObjectNode ParameterUsageNode", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"Partition","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Partition", ACTIVITY_DIAGRAM );
//            createMetaTypesEntry(node,"SignalNode","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ObjectNode Signal", ACTIVITY_DIAGRAM );
//
//             // Collaboration Diagram Metatypes
//            createMetaTypesEntry(node,"Actor","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Actor", COLLABORATION_DIAGRAM);
//            createMetaTypesEntry(node,"Collaboration","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Collaboration" );
//            createMetaTypesEntry(node,"Interaction","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interaction", COLLABORATION_DIAGRAM );
//            createMetaTypesEntry(node,"InteractionOccurrence","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI InteractionOccurrence", COLLABORATION_DIAGRAM );
//            createMetaTypesEntry(node,"Lifeline","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI CollaborationLifeline", COLLABORATION_DIAGRAM );
//            createMetaTypesEntry(node,"Connector","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Connector", COLLABORATION_DIAGRAM );
//            createMetaTypesEntry(node,"MessageConnector","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Connector", COLLABORATION_DIAGRAM );
//
//             // Component and Class diagram only Metatypes
//            createMetaTypesEntry(node,"Lifeline","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI CollaborationLifeline", COMPONENT_DIAGRAM );
//            createMetaTypesEntry(node,"Lifeline","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI CollaborationLifeline", CLASS_DIAGRAM );
//
//             // Sequence diagram only Metatypes
//            createMetaTypesEntry(node,"Actor","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Actor", SEQUENCE_DIAGRAM);
//            createMetaTypesEntry(node,"CombinedFragment","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI CombinedFragment", SEQUENCE_DIAGRAM );
//            createMetaTypesEntry(node,"Lifeline","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Class", SEQUENCE_DIAGRAM );
//            createMetaTypesEntry(node,"Interaction","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interaction", SEQUENCE_DIAGRAM );
//            createMetaTypesEntry(node,"InteractionOccurrence","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI InteractionOccurrence", SEQUENCE_DIAGRAM );
//
//             // State Diagram Metatypes
//            createMetaTypesEntry(node,"CompositeState","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI CompositeState", STATE_DIAGRAM );
//            createMetaTypesEntry(node,"FinalState","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI FinalState", STATE_DIAGRAM );
//            createMetaTypesEntry(node,"Initial","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState Initial", STATE_DIAGRAM );
//            createMetaTypesEntry(node,"PseudoState","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState", STATE_DIAGRAM );
//            createMetaTypesEntry(node,"SimpleState","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI SimpleState", STATE_DIAGRAM );
//            createMetaTypesEntry(node,"State","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI CompositeState", STATE_DIAGRAM );
//            createMetaTypesEntry(node,"StateMachine","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI StateMachine", STATE_DIAGRAM );
//            createMetaTypesEntry(node,"SubmachineState","org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI SubmachineState", STATE_DIAGRAM );
//            createMetaTypesEntry(node,"Transition","org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Transition", STATE_DIAGRAM );
//         }
      }
   }

   /**
    * Create the Buttons Section.
    *
    * This maps a button to a specific initialization string.
    *
    * @param pParent[in] The parent DOM node for this section
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.PresentationTypesMgrImpl#createButtonsSection(org.dom4j.Element)
    */
   protected void createButtonsSection(Element rootElement)
   {
      if(rootElement != null)
      {         
//         Element node = XMLManip.createElement(rootElement, getButtonSectionName());
//         if(node != null)
//         {
//            
//            ////////////////////////
//            // Pure Graphic buttons
//            ////////////////////////
//   
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_RECTANGLE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_RECTANGLE");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_ROUNDED_RECTANGLE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_ROUNDED_RECTANGLE");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_ELLIPSE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_ELLIPSE");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PENTAGON", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_PENTAGON");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_HEXAGON1", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_HEXAGON1");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_HEXAGON2", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_HEXAGON2");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_OCTAGON", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_OCTAGON");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_TRIANGLE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_TRIANGLE");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_TRIANGLE_DOWN", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_TRIANGLE_DOWN");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_TRIANGLE_LEFT", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_TRIANGLE_LEFT");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_TRIANGLE_RIGHT", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_TRIANGLE_RIGHT");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_DIAMOND", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_DIAMOND");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PARALLELOGRAM", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_PARALLELOGRAM");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_STAR", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_STAR");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_CROSS", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_CROSS");
//
//             ////////////////////////
//             // Classifier buttons
//             ////////////////////////
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_CLASS", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Class",
//                              SEQUENCE_DIAGRAM);
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_CLASS", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Class");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_TEMPLATECLASS", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI TemplateClass");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_ACTOR",          
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Actor",
//                               SEQUENCE_DIAGRAM);
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_ACTOR",          
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Actor");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_CLASS_BOUNDARYCONTROLLERORENTITY", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI BoundaryControllerOrEntity");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_CLASS_BOUNDARY", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Boundary");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_CLASS_CONTROL",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Controller");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_CLASS_ENTITY",   
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Entity");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_INTERFACE",      
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface LollypopNotation");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_INTERFACE_AS_CLASS",      
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface ClassNotation");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_DATATYPE",       
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI DataType");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_ALIASEDTYPE",       
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI AliasedType");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_UTILITYCLASS",   
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI UtilityClass");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_ENUMERATION",    
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Enumeration");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_MESSAGE_SELF",       
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI self");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_DESTROY_LIFELINE",       
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI destroy");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_DERIVATIONCLASSIFIER",       
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI DerivationClassifier");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_DERIVATIONEDGE",       
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Derivation");
//   
//             // Nested Link
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_NESTEDLINK", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge NestedLink");
//   
//             // Comment Link
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_COMMENTLINK", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge CommentEdge");
//   
//             ////////////////////////
//             // Bridge buttons
//             ////////////////////////
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_ASSOCIATIONCLASS", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge AssociationClassInitialEdge");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_ASSEMBLYCONNECTOR_INITIALEDGE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge AssemblyConnectorInitialEdge");
//   
//             ////////////////////////
//             // Edge buttons
//             ////////////////////////
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_ASSOCIATION_NN_NN", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Association NN NN");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_AGGREGATION_CO_NN", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Aggregation CO NN");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_AGGREGATION_AG_NN", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Aggregation AG NN");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_ASSOCIATION_NN_NA", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Association NN NA");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_AGGREGATION_CO_NA", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Aggregation CO NA");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_AGGREGATION_AG_NA", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Aggregation AG NA");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_GENERALIZATION", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Generalization");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_MESSAGE",       
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_MESSAGE_ASYNCHRONOUS",       
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message asynchronous");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_MESSAGE_CREATE",       
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message create");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_MESSAGE_CREATE",       
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message result");
//   
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_DEPENDENCY", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Dependency");
//   
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_IMPLEMENTATION", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Implementation");
//   
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_REALIZATION", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Realization");
//   
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_USAGE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Usage");
//   
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_DELEGATE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Delegate");
//   
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PERMISSION", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Permission");
//   
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_ABSTRACTION", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Abstraction");
//   
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_BINARYASSOCIATION", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge BinaryAssociation");
//
//             ////////////////////////
//             // Comment buttons - this one is the same as the link
//             ////////////////////////
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_COMMENT", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge CommentEdge");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_COMMENTNODE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Comment");
//   
//             ////////////////////////
//             // Combined Fragment buttons
//             ////////////////////////
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_COMBINED_FRAGMENT", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI CombinedFragment");
//   
//             ////////////////////////
//             // Package buttons
//             ////////////////////////
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PACKAGE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.Package");
//   
//             ////////////////////////
//             // Activity Diagram buttons
//             ////////////////////////
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PARAMETERUSAGENODE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ObjectNode ParameterUsageNode");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_DATASTORENODE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ObjectNode DataStoreNode");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_SIGNALNODE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ObjectNode Signal");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_INITIALNODE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode InitialNode");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_ACTIVITYFINALNODE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode ActivityFinalNode");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_FLOWFINALNODE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode FlowFinalNode");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_DECISIONNODE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode DecisionNode");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_MERGENODE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode MergeNode");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_FORKNODE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode ForkNode");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_HORIZONTAL_FORKNODE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode ForkNode Horizontal");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_JOINNODE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode JoinNode");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_INVOCATIONNODE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI InvocationNode");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_ACTIVITYEDGE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge ActivityEdge");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_INTERRUPTIBLEACTIVITYREGIONNODE", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI InterruptibleActivityRegion");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_ITERATIONACTIVITYGROUP", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI IterationActivityGroup");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_STRUCTUREDACTIVITYGROUP", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI StructuredActivityGroup");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_COMPLEXACTIVITYGROUP", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ComplexActivityGroup");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_ACTIVITY", 
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Activity");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PARTITION",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Partition");
//         
//             ////////////////////////
//             // Component Diagram buttons
//             ////////////////////////
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_COMPONENT",      
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Component");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_COMPONENTPORT",      
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Port");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PORTINTERFACE_INITIALEDGE",      
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge PortProvidedInterface");
//   
//             ////////////////////////
//             // Use Case Diagram buttons
//             ////////////////////////
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_USECASE",        
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI UseCase");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_INCLUDE",        
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Include");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_EXTEND",        
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Extend");
//   
//             ////////////////////////
//             // Deployment Diagram buttons
//             ////////////////////////
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_NODE",           
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Node");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_ARTIFACT",       
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Artifact");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_DEPLOYMENTSPEC",       
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI DeploymentSpecification");
//   
//             ////////////////////////
//             // State Diagram buttons
//             ////////////////////////
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PSEUDOSTATE_CHOICE",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState Choice");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PSEUDOSTATE_ENTRYPOINT",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState EntryPoint");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PSEUDOSTATE_DEEPHISTORY",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState DeepHistory");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PSEUDOSTATE_SHALLOWHISTORY",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState ShallowHistory");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PSEUDOSTATE_INITIAL",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState Initial");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PSEUDOSTATE_JUNCTION",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState Junction");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PSEUDOSTATE_JOIN",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState Join");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PSEUDOSTATE_JOIN_HORIZONTAL",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState Join Horizontal");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_STATE_TRANSITION",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Transition");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_COMPOSITE_STATE",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI CompositeState");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_SIMPLE_STATE",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI SimpleState");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_SUBMACHINE_STATE",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI SubmachineState");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_FINALSTATE",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI FinalState");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_FINALSTATE_ABORTED",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI FinalState Aborted");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_STATEMACHINE",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI StateMachine");
//             ////////////////////////
//             // Collaboration Diagram buttons
//             ////////////////////////
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_COLLABORATIONLIFELINE",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI CollaborationLifeline");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_COLLABORATION",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Collaboration");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PARTFACADE",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Classifier");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PARTFACADE_INTERFACE",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Interface");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PARTFACADE_USECASE",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade UseCase");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PARTFACADE_ACTOR",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Actor");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PARTFACADE_CLASS",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Class");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_PARTFACADELINK",  
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge PartFacade");
//            createButtonEntry(node,
//                              "ID_VIEWNODE_UML_CONNECTOR",
//                              "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Connector");
//         }
      }
      
   }

   /**
    * Create the DiagramTables Section.
    *
    * This maps a diagram to an id so the diagram name isn't repeated all 
    * over the file.
    *
    * @param rootElement The parent DOM node for this table
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.PresentationTypesMgrImpl#createInitStringsTableSection(org.dom4j.Element)
    */
   protected void createInitStringsTableSection(Element rootElement)
   {      
//      if(rootElement != null)
//      {         
//         Element node = XMLManip.createElement(rootElement, getInitStringsTableSectionName());
//         if(node != null)
//         {
//            int index = 1;
//            clearInitStringsTable();
//            
//            ///////////////////////////////
//            // Pure Presentation Elements
//            ///////////////////////////////
//            createInitStringsTableEntry( node, 
//                                         "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_RECTANGLE", 
//                                         index++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_ROUNDED_RECTANGLE",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_ELLIPSE",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_PENTAGON",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_HEXAGON1",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_HEXAGON2",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_OCTAGON",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_TRIANGLE",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_TRIANGLE_DOWN",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_TRIANGLE_LEFT",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_TRIANGLE_RIGHT",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_DIAMOND",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_PARALLELOGRAM",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_STAR",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Graphic GST_CROSS",
//                                        index ++);
//
//            // Nested Link
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge NestedLink",
//                                        index ++);
//
//            // Comment Link
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge CommentEdge",
//                                        index ++);
//
//            // Qualifiers
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Qualifier",
//                                        index ++);
//
//            ///////////////////////////////
//            // Bridges
//            ///////////////////////////////
//
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge AssociationClassInitialEdge",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge AssemblyConnectorInitialEdge",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI",
//                                        index ++);
//
//            ///////////////////////////////
//            // Comment Node View
//            ///////////////////////////////
//
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Comment",
//                                        index ++);
//
//            ///////////////////////////////
//            // Classifier Node View
//            ///////////////////////////////
//
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI AssociationClass",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI AssociationClassConnectorDrawEngine",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Class",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI TemplateClass",
//                                        index ++);
//            createInitStringsTableEntry(node,        
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Actor",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI BoundaryControllerOrEntity",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Boundary",
//                                        index ++);
//            createInitStringsTableEntry(node, 
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Controller",
//                                        index ++);
//            createInitStringsTableEntry(node, 
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Entity",
//                                        index ++);
//            createInitStringsTableEntry(node,   
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface LollypopNotation",
//                                        index ++);
//            createInitStringsTableEntry(node,   
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface ClassNotation",
//                                        index ++);
//            createInitStringsTableEntry(node,     
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI DataType",
//                                        index ++);
//            createInitStringsTableEntry(node,     
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI AliasedType",
//                                        index ++);
//            createInitStringsTableEntry(node,   
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI UtilityClass",
//                                        index ++);
//            createInitStringsTableEntry(node,   
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Enumeration",
//                                        index ++);
//            createInitStringsTableEntry(node, 
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI self",
//                                        index ++);
//            createInitStringsTableEntry(node, 
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI destroy",
//                                        index ++);
//            createInitStringsTableEntry(node, 
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI DerivationClassifier",
//                                        index ++);
//            createInitStringsTableEntry(node, 
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Derivation",
//                                        index ++);
//         
//            ///////////////////////////////
//            // Class Diagram & Sequence DiagramRelation EdgeView
//            ///////////////////////////////
//
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Association",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge AssociationEnd",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Aggregation",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Association NN NN",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Aggregation CO NN",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Aggregation AG NN",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Association NN NA",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Aggregation CO NA",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Aggregation AG NA",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Generalization",
//                                        index ++);
//            createInitStringsTableEntry(node, 
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message",
//                                        index ++);
//            createInitStringsTableEntry(node, 
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message asynchronous",
//                                        index ++);
//            createInitStringsTableEntry(node, 
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message create",
//                                        index ++);
//            createInitStringsTableEntry(node, 
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Message result",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Dependency",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge BinaryAssociation",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Implementation",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Realization",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Usage",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Delegate",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Permission",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Abstraction",
//                                        index ++);
//
//            ///////////////////////////////
//            // Package Node View
//            ///////////////////////////////
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.Package",
//                                        index ++);
//
//            ///////////////////////////////
//            // Interaction Fragment Node Views
//            ///////////////////////////////
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI CombinedFragment",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI InteractionOccurrence",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interaction",
//                                        index ++);
//
//            ///////////////////////////////
//            // Label Label View
//            ///////////////////////////////
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.LabelView",
//                                        index ++);
//
//            ///////////////////////////////
//            // Activity Diagram Nodes & Edges
//            ///////////////////////////////
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ObjectNode ParameterUsageNode",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ObjectNode DataStoreNode",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ObjectNode Signal",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode InitialNode",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode ActivityFinalNode",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode FlowFinalNode",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode DecisionNode",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode MergeNode",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode ForkNode",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode ForkNode Horizontal",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ControllerNode JoinNode",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI InvocationNode",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge ActivityEdge",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI InterruptibleActivityRegion",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI IterationActivityGroup",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI StructuredActivityGroup",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI ComplexActivityGroup",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Activity",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Partition",
//                                        index ++);
//            ///////////////////////////////
//            // Use Case Diagram Nodes & Edges
//            ///////////////////////////////
//            createInitStringsTableEntry(node,      
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI UseCase",
//                                        index ++);
//            createInitStringsTableEntry(node,      
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Include",
//                                        index ++);
//            createInitStringsTableEntry(node,      
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Extend",
//                                        index ++);
//            ////////////////////////
//            // Component Diagram Nodes & Edges
//            ////////////////////////
//            createInitStringsTableEntry(node,    
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Component",
//                                        index ++);
//            createInitStringsTableEntry(node,    
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Port",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge PortProvidedInterface",
//                                        index ++);
//
//            ///////////////////////////////
//            // Deployment Diagram Nodes & Edges
//            ///////////////////////////////
//            createInitStringsTableEntry(node,    
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Node",
//                                        index ++);
//            createInitStringsTableEntry(node,     
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Artifact",
//                                        index ++);
//            createInitStringsTableEntry(node,     
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI DeploymentSpecification",
//                                        index ++);
//
//            ///////////////////////////////
//            // State Diagram Nodes & Edges
//            ///////////////////////////////
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState Choice",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState EntryPoint",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState DeepHistory",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState ShallowHistory",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState Junction",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState Initial",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState Join",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PseudoState Join Horizontal",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Transition",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI CompositeState",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI SimpleState",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI SubmachineState",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI FinalState",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI FinalState Aborted",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI StateMachine",
//                                        index ++);
//            ////////////////////////
//            // Collaboration Diagram Nodes & Edges
//            ////////////////////////
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI CollaborationLifeline",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Collaboration",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Classifier",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Interface",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade UseCase",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Actor",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI PartFacade Class",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge PartFacade",
//                                        index ++);
//            createInitStringsTableEntry(node,
//                                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Connector",
//                                        index ++);
//         }
//      }
   }

   /**
    * Create the DiagramTables Section.
    *
    * This maps a diagram to an id so the diagram name isn't repeated all
    * over the file.
    *
    * @param rootElement The parent DOM node for this table
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.PresentationTypesMgrImpl#createDiagramsTableSection(org.dom4j.Element)
    */
   protected void createDiagramsTableSection(Element rootElement)
   {
      if(rootElement != null)
      {
//         Element node = XMLManip.createElement(rootElement, getDiagramsTableSectionName());
//         if(node != null)
//         {
//            int index = 1;
//            clearDiagramTable();
//            createDiagramsTableEntry( node, "Diagram", IDiagramKind.DK_DIAGRAM);
//            createDiagramsTableEntry( node, ACTIVITY_DIAGRAM, IDiagramKind.DK_ACTIVITY_DIAGRAM);
//            createDiagramsTableEntry( node, CLASS_DIAGRAM, IDiagramKind.DK_CLASS_DIAGRAM);
//            createDiagramsTableEntry( node, COLLABORATION_DIAGRAM, IDiagramKind.DK_COLLABORATION_DIAGRAM);
//            createDiagramsTableEntry( node, COMPONENT_DIAGRAM, IDiagramKind.DK_COMPONENT_DIAGRAM);
//            createDiagramsTableEntry( node, DEPLOYMENT_DIAGRAM, IDiagramKind.DK_DEPLOYMENT_DIAGRAM);
//            createDiagramsTableEntry( node, SEQUENCE_DIAGRAM, IDiagramKind.DK_SEQUENCE_DIAGRAM);
//            createDiagramsTableEntry( node, STATE_DIAGRAM, IDiagramKind.DK_STATE_DIAGRAM);
//            createDiagramsTableEntry( node, USECASE_DIAGRAM, IDiagramKind.DK_USECASE_DIAGRAM);
//         }
      }
   }

   /**
    * Returns the diagram id for the diagramKind.
    *
    * @param nDiagramKind The kind of diagram to convert to an index into the 
    *                     diagram table.  Must be one of the values defined
    *                     int IDiagramKind.
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.PresentationTypesMgrImpl#getDiagramID(int)
    * @see IDiagramKind
    */
   protected long getDiagramID(int nDiagramKind)
   {
      String xDiagramKind ="Diagram";
//
//      switch (nDiagramKind)
//      {
//         case IDiagramKind.DK_ACTIVITY_DIAGRAM : 
//            xDiagramKind = ACTIVITY_DIAGRAM;
//            break;
//         case IDiagramKind.DK_CLASS_DIAGRAM : 
//            xDiagramKind = CLASS_DIAGRAM;
//            break;
//         case IDiagramKind.DK_COLLABORATION_DIAGRAM : 
//            xDiagramKind = COLLABORATION_DIAGRAM;
//            break;
//         case IDiagramKind.DK_COMPONENT_DIAGRAM : 
//            xDiagramKind = COMPONENT_DIAGRAM;
//            break;
//         case IDiagramKind.DK_DEPLOYMENT_DIAGRAM : 
//            xDiagramKind = DEPLOYMENT_DIAGRAM;
//            break;
//         case IDiagramKind.DK_SEQUENCE_DIAGRAM : 
//            xDiagramKind = SEQUENCE_DIAGRAM;
//            break;
//         case IDiagramKind.DK_STATE_DIAGRAM : 
//            xDiagramKind = STATE_DIAGRAM;
//            break;
//         case IDiagramKind.DK_USECASE_DIAGRAM : 
//            xDiagramKind = USECASE_DIAGRAM;
//            break;
//      }
//      return super.getDiagramID(xDiagramKind);
      return 0;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr#getDefaultConnectorView()
    */
   public String getDefaultConnectorView()
   {
      // TODO Auto-generated method stub
//      return super.getDefaultConnectorView();
       return "";
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr#getDefaultLabelView()
    */
   public String getDefaultLabelView()
   {
      // TODO Auto-generated method stub
//      return super.getDefaultLabelView();
       return "";
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr#getPresentationElementMetaType(java.lang.String, java.lang.String)
    */
   public String getPresentationElementMetaType(String sElementType, 
                                                String sInitializationString)
   {
      String retVal = "";
      
      if(sElementType != null)
      {
//         retVal = super.getPresentationElementMetaType(sElementType, sInitializationString);
         
         if (sInitializationString != null && sInitializationString.endsWith("NestedLink"))
         {
            retVal = "NestedLinkPresentation";            
         }
         else if((sInitializationString != null && (sInitializationString.endsWith("PartFacade")) || 
                 (sInitializationString.endsWith("PartFacade Interface"))))
         {
            retVal = "PartFacadeEdgePresentation";            
         }
      }
      else
      {
		retVal = "";
      }
      
      return retVal;
   }

}
