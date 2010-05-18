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


package org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator;

import java.util.Iterator;
import java.util.Vector;

import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.structure.IArtifact;
import org.netbeans.modules.uml.core.metamodel.structure.IComponent;
import org.netbeans.modules.uml.core.support.umlmessagingcore.ThermCtrl;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
//import org.netbeans.modules.uml.ui.controls.drawingarea.AutoRoutingAction;
//import org.netbeans.modules.uml.ui.controls.drawingarea.DiagramAreaEnumerations;
//import org.netbeans.modules.uml.ui.controls.drawingarea.IAutoRoutingAction;
//import org.netbeans.modules.uml.ui.controls.drawingarea.ITopographyChangeAction;
//import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
//import org.netbeans.modules.uml.ui.controls.drawingarea.TopographyChangeAction;
//import org.netbeans.modules.uml.ui.products.ad.diagramengines.IADRelationshipDiscovery;
//import org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine;
//import org.netbeans.modules.uml.ui.products.ad.drawengines.QuadrantKindEnum;
import org.netbeans.modules.uml.ui.support.IAutoRoutingActionKind;
import org.netbeans.modules.uml.ui.support.ThermProgress;
//import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.helpers.GUIBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker.GBK;
//import org.netbeans.modules.uml.ui.support.umltsconversions.RectConversions;
//import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
//import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author sumitabhk
 *
 */
public class ComponentDiagramCreator implements IComponentDiagramCreator
{

	/**
	 * 
	 */
	public ComponentDiagramCreator()
	{
		super();
	}

	/**
	 * Generate the component diagram.
	 *
	 * @param pSelectedElements [in] The elements that should be used to populate this diagram
	 * @param pExistingDiagram [in] The diagram to populate
	 * @param bHandled [out,retval] TRUE if we've actually generated onto the diagram
	 */
	public boolean generate( ETList<IElement> pSelectedElements, IDiagram pExistingDiagram )
	{
		boolean handled = true;
		if (pSelectedElements != null)
		{
			int count = pSelectedElements.size();
			
			// Create a list of our components
			Vector<IComponent> components = new Vector<IComponent>();
			Vector<IInterface> interfaces = new Vector<IInterface>();
			
			// Remove any invalid objects (ie Ports) and create a list
			// of components and interfaces
			for (int i=0; i<count; i++)
			{
				boolean remove = false;
				IElement elem = pSelectedElements.get(i);
				String elemType = elem.getElementType();
				if (elemType != null)
				{
					if (elemType.equals("Port"))
					{
						remove = true;
					}
					else if (elemType.equals("Component"))
					{
						if (elem instanceof IComponent)
						{
							components.add((IComponent)elem);
							remove = true;
						}
					}
					else if (elemType.equals("Interface"))
					{
						if (elem instanceof IInterface)
						{
							interfaces.add((IInterface)elem);
							remove = true;
						}
					}
				}
				
				if (remove)
				{
					pSelectedElements.remove(i);
					count = pSelectedElements.size();
					i--;
				}
			}
			
			// Create the component diagram
			createComponentDiagram(pExistingDiagram, pSelectedElements, components, interfaces);
		}
		return handled;
	}

	/**
	 * Creates the component diagram based on the elements in the arguments
	 *
	 * @param pExistingDiagram [in] The component diagram to populate
	 * @param pSelectedElements [in] Misc Elements - not ports, components or interfaces
	 * @param components [in] A list of components to add
	 * @param interfaces [in] A list of interfaces to add
	 */
	private void createComponentDiagram(IDiagram pExistingDiagram,
										ETList<IElement> pSelectedElements,
										Vector<IComponent> components,
										Vector<IInterface> interfaces)
	{
      if ( null == pExistingDiagram ) throw new IllegalArgumentException();
      if ( null == pSelectedElements ) throw new IllegalArgumentException();
      
		// Get the relationship discovery object so we can create presentation elements
//		ICoreRelationshipDiscovery relDisco = TypeConversions.getRelationshipDiscovery(pExistingDiagram);
//		if ( relDisco instanceof IADRelationshipDiscovery )
//		{
//			IADRelationshipDiscovery adrelDis = (IADRelationshipDiscovery)relDisco;
//
//			// Block all label layouts to speed up the creation of this diagram
//			IGUIBlocker blocker = new GUIBlocker( GBK.DIAGRAM_LABEL_LAYOUT );
//         try
//         {
//            // Create the misc presentation elements (ie not components or interfaces)
//            int count = pSelectedElements.size();
//            if (count > 0)
//            {
//               // Create this object on the stack to verify that we'll end progress even
//               // if we get an exception
//               ThermProgress ensureEnd = new ThermProgress();
//
//               // If we have a message string then begin the progress
//               String message = loadString("IDS_CREATING_PES");
//               ensureEnd.beginProgress(message, 0, count, 0);
//               for (int i=0; i<count; i++)
//               {
//                  IElement elem = pSelectedElements.get(i);
//                  IPresentationElement cpPE = adrelDis.createPresentationElement(elem);
//                  ensureEnd.setPos(i);
//               }
//            }
//         
//            // Create the components
//            {
//               boolean origBlock = EventBlocker.startBlocking();
//               try
//               {
//                  if (components != null)
//                  {
//                     for (Iterator iter = components.iterator(); iter.hasNext();)
//                     {
//                        IComponent component = (IComponent)iter.next();
//                        
//                        createComponentPresentationElement(pExistingDiagram, adrelDis, component, interfaces);
//                     }
//                  }
//               }
//               finally
//               {
//                  EventBlocker.stopBlocking(origBlock);
//               }
//            }
//         
//            // Now create the interfaces that weren't created during the component creation
//            {
//               boolean origBlock = EventBlocker.startBlocking();
//               try
//               {
//                  if (interfaces != null)
//                  {
//                     for (Iterator iter = interfaces.iterator(); iter.hasNext();)
//                     {
//                        IInterface iFace = (IInterface)iter.next();
//                        
//                        adrelDis.createPresentationElement( iFace );
//                     }
//                  }
//               }
//               finally
//               {
//                  EventBlocker.stopBlocking(origBlock);
//               }
//            }
//         
//            // process post drop handling which will discover relationships
//            IDiagramEngine diaEngine = TypeConversions.getDiagramEngine(pExistingDiagram);
//            if (diaEngine != null)
//            {
//               ETList<IElement> elemsToDoRelDiscoOn = pExistingDiagram.getAllItems3();
//               ETList<IPresentationElement> pPEs = adrelDis.discoverCommonRelations(false, elemsToDoRelDiscoOn, null);
//            }
//         }
//         finally
//         {
//            if( blocker != null )
//            {
//               blocker.clearBlockers();
//            }
//         }
//		}
	}

	/**
	 * Creates a component presentation element
	 *
	 * @param pExistingDiagram [in] The diagram to create the component on
	 * @param pADRelationshipDiscovery [in] The relationship discovery object on that diagram
	 * @param pComponent [in] The component to place on the diagram
	 * @param interfaces [in] The interfaces to display
	 */
//	private void createComponentPresentationElement(IDiagram pExistingDiagram,
//													IADRelationshipDiscovery pADRelationshipDiscovery,
//													IComponent pComponent,
//													Vector<IInterface> interfaces)
//	{
//		IPresentationElement pComponentPE = pADRelationshipDiscovery.createPresentationElement(pComponent);
//		if (pComponentPE != null)
//		{
//			ETList<IPresentationElement> portPEs = pADRelationshipDiscovery.createPortPresentationElements(pComponentPE);
//			if (portPEs != null)
//			{
//				int count = portPEs.size();
//				
//				// Go over all the ports and add the port provided and required interfaces
//				for (int i=0; i<count; i++)
//				{
//					IPresentationElement portPE = portPEs.get(i);
//					createPortInterfaces(pADRelationshipDiscovery, portPE, interfaces);
//				}
//			}
//		}
//	}

	/**
	 * Creates the interfaces and assembly connectors on the port
	 *
	 * @param pADRelationshipDiscovery [in] The relationship discovery object on that diagram
	 * @param pPortPE [in] The presentation element for that port
	 * @param interfaces [in] The interfaces to display
	 */
//	private void createPortInterfaces(IADRelationshipDiscovery pADRelationshipDiscovery,
//									  IPresentationElement pPortPE,
//									  Vector<IInterface> interfaces)
//	{
//		ETList<IPresentationElement> createdInterfaces = pADRelationshipDiscovery.createPortProvidedAndRequiredInterfaces(pPortPE);
//		if ( (createdInterfaces != null) &&
//           (interfaces != null) )
//		{
//			int count = createdInterfaces.size();
//			
//			// Remove these interfaces from the list the user originally selected
//			for (int i=0; i<count; i++)
//			{
//				IPresentationElement pEle = createdInterfaces.get(i);
//				IElement elem = TypeConversions.getElement(pEle);
//				if (elem != null)
//				{
//					int numFaces = interfaces.size();
//					for (int j=0; j<numFaces; j++)
//					{
//						IInterface iFace = interfaces.get(j);
//						if (iFace.isSame(elem))
//						{
//							interfaces.remove(j);
//							break;
//						}
//					}
//				}
//			}
//		}
//	}

	/**
	 * Called after the layout is performed, this moves nodes around so they get
	 * contained in their parents.
	 *
	 * @param pDiagram [in] The diagram to look for containment on.
	 */
	public long performLayout(IDiagram pDiagram)
	{ //TODO
//		if (pDiagram != null)
//		{
//			// Get the current layout kind of the diagram
//			int layoutKind = pDiagram.getLayoutStyle();
//			
//			if (pDiagram instanceof IUIDiagram)
//			{
//				IDrawingAreaControl control = ((IUIDiagram)pDiagram).getDrawingArea();
//				if (control != null)
//				{
//					// Get all the elements on the diagram
//					ETList<IPresentationElement> allPEs = control.getAllItems();
//					
//					if (allPEs != null)
//					{
//						// Autorouting is extreemly slow unless it's straight.  So we force the 
//						// autorouting for this diagram to STRAIGHT no matter what anyone else says.
//						IAutoRoutingAction tempAction = new AutoRoutingAction();
//						tempAction.forceAutoRoutingStyle(IAutoRoutingActionKind.ARAK_STRAIGHT);
//
//						// We're about to turn on containment and perform layout.  We don't want anything
//						// contained at this point so move nodes around so that no containment is possible.
//						moveNodesSoNothingIsContained(control, allPEs);
//						
//						// Go through all the components on the diagram and construct a genealogy tree.
//						GenealogyTree tree = new GenealogyTree();
//						{
//							{
//								int count = allPEs.size();
//								String message = loadString("IDS_PERFORMING_COMPONENT_LAYOUT");
//								ThermCtrl thermState = new ThermCtrl(message, count);
//								for (int i=0; i<count; i++)
//								{
//									IPresentationElement pPE = allPEs.get(i);
//									if (pPE instanceof INodePresentation)
//									{
//										INodePresentation pNodePE = (INodePresentation)pPE;
//										IElement elem = TypeConversions.getElement(pNodePE);
//										
//										// Components are added
//										if (elem != null)
//										{
//											if (elem instanceof IComponent)
//											{
//												// Tell the component to move all the ports to the right
//												IDrawEngine drawEngine = TypeConversions.getDrawEngine(pNodePE);
//												if (drawEngine != null && drawEngine instanceof IComponentDrawEngine)
//												{
//													((IComponentDrawEngine)drawEngine).movePortsToSide(QuadrantKindEnum.QK_RIGHT);
//												}
//											}
//
//											// So are those things that the components can contain
//											if (elem instanceof IComponent || 
//												elem instanceof IStructuredClassifier ||
//												elem instanceof IArtifact)
//											{
//												// Add the component or anything the component may contain (structured classifiers
//												// or artifacts).
//												tree.addUndeterminedNode(pNodePE);
//											}
//										}
//										thermState.update(message, i);
//									}
//								}
//							}
//
//							// This will hide the ports on the components so they don't need to be moved
//							// as well.
//							tree.onGraphEvent(IGraphEventKind.GEK_PRE_RESIZE, loadString("IDS_HIDING_PORTS"));
//						}
//						
//						//IDS_BUILDING_GENEALOGY
//						// Now tell the tree to create its list.  We're done adding nodes
//						tree.buildGenealogy(control);
//						
//						//IDS_PERFORMING_CONTAINMENT_PROCESSING
//						// Now do the containment processing
//						processContainerGenealogy(control, tree);
//						
//						//IDS_PERFORMING_STACKING
//						tree.setStackingOrder();
//						
//						// Here's where the ports get reattached to the side of the components
//						tree.onGraphEvent(IGraphEventKind.GEK_POST_RESIZE, loadString("IDS_SHOWING_PORTS"));
//						
//						{
////							IGUIBlocker blocker = new GUIBlocker();
////							blocker.setKind(GBK_DIAGRAM_STACKING_COMMANDS);
//						
//							// Relayout, this will get rid of the empty space and distribute all the lollypops again
//							
//							// This looks goofy, but the first chunk of code gives each node a chance to hide its children
//							// then relayout them in a better spot.  For assembly connectors and interfaces this means
//							// moving them closer to the component.  Now that they've been moved...
//							tree.distributeAllComponentPorts();
//							
//							// ... we need to layout again to reduce the amount of whitespace around the components.
//							{
//								ITopographyChangeAction cAction = new TopographyChangeAction();
//								cAction.setKind(DiagramAreaEnumerations.TAK_LAYOUTCHANGE_SILENT);
//								cAction.setLayoutStyle(true, true, layoutKind);
//								control.postDelayedAction(cAction);
//							}
//
//							// Pump messages to cause the layout.  We do it here so that the blocker
//							// the stops stacking is active.  When we lose this block the stacking blocker
//							// goes away
//							control.pumpMessages(false);
//						}
//						
//						// End the forcing of the autoroute
//						tempAction.endForce();
//					}
//				}
//			}
//		}
		return 0;
	}

	/**
	 * Takes the geneology information
	 *
	 * @param pDiagram [in] The diagram to look for containment on.
	 * @param tree [in] The genealogy tree constructed for components
	 */
//	private void processContainerGenealogy(IDrawingAreaControl pDiagram, GenealogyTree tree)
//	{
//		if (pDiagram != null)
//		{
//			// Get the view graph rect.  The components are going to be placed 
//			TSGraphManager graphMgr = pDiagram.getCurrentGraphManager();
//			if (graphMgr != null)
//			{
//				TSGraph rootGraph = graphMgr.getOwnerGraph();
//            if (rootGraph instanceof TSEGraph)
//            {
//               TSEGraph tseRootGraph = (TSEGraph)rootGraph;
//               
//               TSConstRect viewRect = tseRootGraph.getUI().getBounds();   //rootGraph.getViewRect(false);
//					if (viewRect != null)
//					{
//                  IETRect graphRect = RectConversions.newETRect( viewRect );
//						tree.calculateRectangles(graphRect);
//						tree.placeNodes();
//					}
//				}
//			}
//		}
//	}

	/**
	 * Moves all the nodes on the diagram so that nothing is contained
	 *
	 * @param pAxControl [in] The diagram
	 * @param pAllPEs [in] All the presentation elements on the diagram (previously calculated)
	 */
//	private void moveNodesSoNothingIsContained(IDrawingAreaControl control,
//												ETList<IPresentationElement> pAllPEs)
//	{
//		if (control != null && pAllPEs != null)
//		{
//			// Pump messages to make sure all the nodes process any resize messages properly
//			control.pumpMessages(false);
//			int count = pAllPEs.size();
//			int currentTop = 0;
//			int slop = 10;
//			for (int i=0; i<count; i++)
//			{
//				IPresentationElement pEle = pAllPEs.get(i);
//				if (pEle instanceof INodePresentation)
//				{
//					INodePresentation pNodePE = (INodePresentation)pEle;
//					IETRect rect = TypeConversions.getLogicalBoundingRect(pNodePE);
//
//					currentTop += rect.getHeight() / 2 + slop;
//					rect.setLeft(0);
//               rect.setTop((int)(currentTop - rect.getCenterY()));
//					
//               // Take the default move flags.
//               pNodePE.moveTo(rect.getIntX(), rect.getIntY(), 0);
//					currentTop += rect.getHeight() / 2 + slop;
//				}
//			}
//		}
//	}

	private String loadString(String key)
	{
		return DiagCreatorAddIn.loadString(key);
	}

}


