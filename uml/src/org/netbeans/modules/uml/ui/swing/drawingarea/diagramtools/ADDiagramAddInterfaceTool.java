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



package org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools;

import java.awt.event.MouseEvent;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPort;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.structure.IComponent;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETPointEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PointConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.drawing.TSDNode;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.command.TSEAddNodeCommand;
import com.tomsawyer.editor.ui.TSENodeUI;
import com.tomsawyer.graph.TSNode;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSPoint;
import com.tomsawyer.drawing.geometry.TSPoint;


/**
 * @author josephg
 */
public class ADDiagramAddInterfaceTool extends ADAddNodeEdgeTool  {

	protected String getExpectedElementType() { 
		return "Interface";
	}
	
	private TSENode m_createdPortNode = null;
	private boolean connectedEdge = false;
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADCreateEdgeState#getObject(java.awt.event.MouseEvent)
	 */
	protected ETNode getObject(MouseEvent pEvent) {
		TSENode foundNode = super.getObject(pEvent);
		try {
			TSConstPoint point = getNonalignedWorldPoint(pEvent);

			if(foundNode != null) {
				IDrawEngine foundDrawEngine = TypeConversions.getDrawEngine((IETGraphObject)foundNode);
				
				IElement foundElement = TypeConversions.getElement(foundDrawEngine);
				
				if(m_createPortIfNecessary) {
					if(interactiveEdge==null && foundElement instanceof IComponent) {
						IDrawingAreaControl control = getDrawingArea();
						
						if(control != null) {
							TSPoint nonConstPoint = new TSPoint(point);
							IETPoint etLocation = PointConversions.newETPoint(nonConstPoint);
							
							TSNode createdNode = control.addNode("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Port", etLocation, true,true);
							
							if(createdNode != null) {
								control.postAddObject((IETGraphObject)createdNode,true);

								foundElement = TypeConversions.getElement(createdNode);
								m_createdPortNode = (TSENode)createdNode;
								foundNode = TypeConversions.getOwnerNode(createdNode);
							}
						}
					}
					
					if( foundElement instanceof IInterface ) {
						if(interactiveEdge == null) { // first click from an interface, ignore it
							foundNode = null;
						}
					}
					else if( !(foundElement instanceof IPort) &&
						     !(foundElement instanceof IClass) ) {
						foundNode = null;
					}
               else if( foundElement instanceof IComponent &&
                        interactiveEdge != null) {
                  foundNode = null;
               }
               else {
						IDrawingAreaControl control = getDrawingArea();
						
						if(control != null) {
							if(foundElement instanceof IPort) {
								control.changeEdgeDescription("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge PortProvidedInterface");
								setViewDescription("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface ClassNotation");
								setSingleClickNodeDescription("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface ClassNotation");								
							} 
							else if(foundElement instanceof IClass) {
								control.changeEdgeDescription("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Implementation");
								setViewDescription("org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge Implementation");
								setSingleClickNodeDescription("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI Interface ClassNotation");
							}
						}
					}
				}
				else {
					if(! (foundElement instanceof IInterface) )
						foundNode = null;
				}
			}
		} catch(ETException e) {
			e.printStackTrace();
		}
		
		return (ETNode)TypeConversions.getETNode(foundNode);
	}
	
	boolean m_createPortIfNecessary = false;

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEWindowInputState#onMousePressed(java.awt.event.MouseEvent)
	 */
	public void onMousePressed(MouseEvent pEvent) {
		m_createPortIfNecessary = true;
		super.onMousePressed(pEvent);
		m_createPortIfNecessary = false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADAddNodeEdgeTool#createNode(com.tomsawyer.util.TSConstPoint)
	 */
	protected IETNode createNode(TSConstPoint pt) {
		try {
			if(m_fromNode == null) {
                            this.getGraphWindow().deselectAll(false);
				IETNode interfaceNode = (IETNode) this.getDrawingArea().addNode(getSingleClickNodeDescription(), new ETPointEx(pt), interactiveEdge != null ? this.interactiveEdge.isSelected() : true, false);
	
				if (interfaceNode != null) {
					// Fire the Events.
					try { 
						ADCreateNodeState createNodeState = new ADCreateNodeState();
	
						createNodeState.setGraphWindow(this.getGraphWindow());
						createNodeState.postCreateObj(interfaceNode, true);
                  createNodeState.firePostCreateEvent(interfaceNode);
						createNodeState.cancelAction();
						createNodeState.stopMouseInput();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
	
				return interfaceNode;
			}
			else {
				if(m_fromNode.getOwnerGraph() instanceof TSEGraph) {
					TSEGraph graph = (TSEGraph)m_fromNode.getOwnerGraph();

					TSEAddNodeCommand cmd = new TSEAddNodeCommand(graph,pt.getX(),pt.getY(),graph.getGraphWindow().getCurrentNodeUI(), true);
					
					cmd.execute();
					TSDNode createdNode = cmd.getNode();
					IETNode etNode = TypeConversions.getETNode(createdNode);
					getDrawingArea().postAddObject(etNode,true);
					return etNode;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Sets the source node of the edge, return true if it correctness is verified for this drawengine type..
	 */
	protected boolean setSourceNode(TSENode node)
	{
	   if (m_fromNode == null)
	   {
		  m_fromNode = (ETNode)node;

		  // We have enough information to create our interactive object.
		  createInteractiveObjects();
		  
		  return true;
	   }

	   return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADCreateEdgeState#connectEdge(com.tomsawyer.drawing.TSConnector)
	 */
	public void connectEdge(TSConnector connector) {
		IDrawingAreaControl control = getDrawingArea();
		IElement sourceElement = TypeConversions.getElement((IETGraphObject)m_fromNode);
		IElement targetElement = TypeConversions.getElement((IETGraphObject)m_toNode);
		
		if(control != null && sourceElement  != null) {
			boolean sourceIsClass = sourceElement instanceof IClass;
			boolean targetIsInterface = targetElement instanceof IInterface;
			if(!sourceIsClass && targetIsInterface) {
				control.setModelElement(targetElement);
			}
		}
		
		super.connectEdge(connector);
    
      addProvidedInterface(sourceElement,targetElement);  
		
		if(control!= null)
			control.setModelElement(null);
	}

   private void addProvidedInterface(IElement sourceElement, IElement targetElement)
   {
      IInterface anInterface = null;
      IPort aPort = null;
      
      if(targetElement instanceof IInterface &&
         sourceElement instanceof IPort)
      {
         anInterface = (IInterface)targetElement;
         aPort = (IPort)sourceElement;
      }
      else if( sourceElement instanceof IInterface &&
               targetElement instanceof IPort)
      {  
         anInterface = (IInterface)sourceElement;
         aPort = (IPort)targetElement;
      }
      
      if(anInterface != null && aPort != null)
         aPort.addProvidedInterface(anInterface);
   }
   
   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEWindowState#cancelAction()
    */
   public void cancelAction()
   {
      if (m_createdPortNode != null && connectedEdge == false &&
		m_createdPortNode instanceof IETNode)
      {
			IETNode etNode = (IETNode)m_createdPortNode;
			etNode.invalidate();
			etNode.delete();
			m_createdPortNode = null;
      }
      
		connectedEdge = false;
      super.cancelAction();
   }

	protected void postConnectEdge()
	{
		super.postConnectEdge();
		connectedEdge = true;
	}


   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEWindowState#resetState()
    */
   public void resetState()
   {	
      super.resetState();
   }

}