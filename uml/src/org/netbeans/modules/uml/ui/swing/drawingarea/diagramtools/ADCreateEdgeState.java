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

import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ICombinedFragmentDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import java.util.List;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.drawing.TSPEdge;
//import com.tomsawyer.editor.TSEWindowInputState;
import com.tomsawyer.editor.TSEWindowInputTool;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.TSEPNode;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.editor.ui.TSEEdgeUI;
import org.netbeans.modules.uml.common.generics.IteratorT;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.CreationFactoryHelper;
import org.netbeans.modules.uml.ui.support.relationshipVerification.AddEdgeEventDispatcher;
import org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents;
import org.netbeans.modules.uml.ui.support.relationshipVerification.IEdgeVerification;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADContainerDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
//import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericPathNodeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericEdgeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETUIFactory;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEdgeDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETCreateEdgeInvalid;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETCreateEdgeCursor;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETEdge;

/*
 * 
 * @author KevinM
 * 
*/
//public class ADCreateEdgeState extends TSEWindowInputState implements IAddEdgeEvents {
public class ADCreateEdgeState extends TSEWindowInputTool implements IAddEdgeEvents {

	public ADCreateEdgeState() {
		super();
		m_edgeVerifcation = null;
		m_defaultCursor = ETCreateEdgeCursor.getCursor();
		m_noDropCursor = ETCreateEdgeInvalid.getCursor();

		setDefaultCursor(m_defaultCursor);
	}

	public ADCreateEdgeState(int edgeType) {
		super();
		m_edgeVerifcation = null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents#getParentDiagram()
	 */
	public IDiagram getParentDiagram() {
		return getEventDispatcher().getParentDiagram();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents#setParentDiagram(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
	 */
	public void setParentDiagram(IDiagram pParentDiagram) {
		if (getEventDispatcher() != null)
			getEventDispatcher().setParentDiagram(pParentDiagram);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents#getViewDescription()
	 */
	public String getViewDescription() {
		return getEventDispatcher().getViewDescription();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents#setViewDescription(java.lang.String)
	 */
	public void setViewDescription(String sViewDescription) {
		getEventDispatcher().setViewDescription(sViewDescription);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents#fireStartingEdgeEvent(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode, com.tomsawyer.util.TSConstPoint)
	 */
	public ETTripleT < TSConnector, Integer, IETPoint > fireStartingEdgeEvent(IETNode pNode, TSConstPoint point) {
		return getEventDispatcher().fireStartingEdgeEvent(pNode, point);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents#fireEdgeMouseMoveEvent(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode, org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode, com.tomsawyer.util.TSConstPoint)
	 */
	public boolean fireEdgeMouseMoveEvent(IETNode pStartNode, IETNode pNodeUnderMouse, TSConstPoint point) { // TODO Auto-generated method stub
		return getEventDispatcher().fireEdgeMouseMoveEvent(pStartNode, pNodeUnderMouse, point);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IAddEdgeEvents#fireFinishEdgeEvent(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode, org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode, com.tomsawyer.editor.TSEConnector, com.tomsawyer.util.TSConstPoint)
	 */
	public ETPairT < TSConnector, Integer > fireFinishEdgeEvent(IETNode pStartNode, IETNode pFinishNode, TSConnector pStartConnector, TSConstPoint point) {
		return getEventDispatcher().fireFinishEdgeEvent(pStartNode, pFinishNode, pStartConnector, point);
	}

	/*
	 * Demand load accessor to the IEdgeVerification interface.
	 */
	IEdgeVerification getVerification() {
		if (m_edgeVerifcation == null)
			m_edgeVerifcation = creatEdgeVerification();
		return m_edgeVerifcation;
	}

	/*
	 * Factory function that creates an implementation of the IEdgeVerification interface.
	 */
	IEdgeVerification creatEdgeVerification() {
		return CreationFactoryHelper.getEdgeVerification();
	}

	public void setCursor(Cursor cursor) {
		super.setCursor(cursor);
	}

	/*
	 * Returns true if we can connect an edge at this logical window point, it only gets called
	 * after the user has clicked for the second time, durning interactive mouse moves onVerifyMouseMove is used
	 */
	public ETPairT < TSConnector, Boolean > canConnectEdge(TSConstPoint point) {
		ETPairT < TSConnector, Boolean > retVal = new ETPairT < TSConnector, Boolean > ();
		if (getSourceNode() != null && getTargetNode() != null && createdEdge == null) {
			TSENode targetNode = getTargetNode();
			TSENode sourceNode = getSourceNode();

			// Now fire the connect Edge Event.
			ETPairT < TSConnector, Integer > retcode = getEventDispatcher().fireFinishEdgeEvent((IETNode) sourceNode, (IETNode) targetNode, interactiveEdge.getSourceConnector(), point);

			if (retcode != null) {
				retVal.setParamOne(retcode.getParamOne());
				retVal.setParamTwo(new Boolean(retcode.getParamTwo().intValue() == 0));
			}
		}
		return retVal;
	}

	/*
	 * Delete the node and removes the interactive edge if it hasn't already been reconnected to the target node or connector.
	 */
	protected void deleteHiddenNode() {
		if (m_hiddenNode != null) {
			try{
				getGraphWindow().getGraph().discard(m_hiddenNode);
				m_hiddenNode = null;
			}
			catch(Exception e)
			{
				m_hiddenNode = null;
			}
		}
	}

	
	/*
	 * Model element creation driver,
	 */
	public void connectEdge() {
		connectEdge(null);
	}

	/*
	 * Model element creation driver,
	 */
	public void connectEdge(TSConnector connector) {
		TSENode target = getTargetNode();
		TSENode source = getSourceNode();

		ETPairT<List,List> bendPoints = getReconnectEdgeBendPoints();
		
		try {
			/*
			 * Reconnect the interactive Edge from the hidden node to the target node.
			 */			 
			 interactiveEdge.getOwnerGraph().remove(interactiveEdge); //JM: adding this to fix the problem of events being lost while creating an edge
			createdEdge = (ETEdge) getGraphWindow().reconnectEdge(interactiveEdge, target, null, false, bendPoints.getParamOne(), bendPoints.getParamTwo());		
		
		} catch (Exception e) {
			// Just it ignore this exception its just an assertion.
		}

		createdEdge = (ETEdge) interactiveEdge;
		if (connector != null) {
			createdEdge.setTargetConnector(connector);
		}

		if (createdEdge != null) {
			this.postConnectEdge();
		}
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEWindowState#resetState()
	 */
	public void resetState() {
		//super.resetState();
            super.resetTool();

		getDrawingArea().setModelElement(null);
		createdEdge = null;
		interactiveEdge = null;
		m_fromNode = null;
		m_toNode = null;

		m_edgeUI = null;
		m_hiddenNode = null;
		m_lastMousePt = null;
		m_edgeVerifcation = null;
		m_eventDispatcher = null;
	}

	public void cancelAction() {
		m_edgeUI = null;
		deleteHiddenNode();
		super.cancelAction();

		getGraphWindow().drawGraph();
		getGraphWindow().fastRepaint();
	}

	protected void setDefaultState() {
		try {
			((ADGraphWindow) this.getGraphWindow()).getDrawingArea().switchToDefaultState();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Sets the target node if it checks out the interactive edge is added to the drawing area.
	 */
	protected boolean setTargetNode(TSENode node) {
		if (m_toNode == null)
			m_toNode = (ETNode) node;

		ETPairT < TSConnector, Boolean > value = canConnectEdge(m_lastMousePt);
		if (value.getParamTwo().booleanValue() == true) {
			connectEdge(value.getParamOne());

			// Remove our temp node and edge.
			deleteHiddenNode();
			resetState();
			return true;
		} else {
			m_toNode = null;
			return false;
		}
	}

	/*
	 * Sets the source node of the edge, return true if it correctness is verified for this drawengine type..
	 */
	protected boolean setSourceNode(TSENode node) {
		if (m_fromNode == null) {
			m_fromNode = (ETNode) node;

			// We have enough information to create our interactive object.
			createInteractiveObjects();

			if (getVerification() != null) {
				return getVerification().verifyStartNode(getNodeElement(node), getEdgeMetaType());
			} else
				return false;
		}
		return false;
	}

	/*
	 * Returns the nodes model element.
	 */
	protected IElement getNodeElement(TSENode node) {
		return TypeConversions.getElement(node);
	}

	/*
	 * Create our hidden node, interactive edge, and our new Edge UI.
	 */
	protected void createInteractiveObjects() {
		this.m_hiddenNode = createHiddenNode();
		this.interactiveEdge = createinteractiveEdge();
		m_edgeUI = createEdgeUI();
		interactiveEdge.setUI(m_edgeUI);
		
		if (m_edgeUI.getDrawEngine() != null)
			m_edgeUI.getDrawEngine().setParent(m_edgeUI);
			
		interactiveEdge.setSelected(false);
	}

	/*
	 * If the start node isn't compatible with this type we need to reset.
	 */
	protected void resetInteractiveObjects() {
		m_edgeUI = null;
		deleteHiddenNode();
		m_fromNode = null;
	}

	/*
	 * Returns the initial node hit by the mouse, can be null.
	 */
	public TSENode getSourceNode() {
		return m_fromNode;
	}

	/*
	 * Returns the target node hit by the mouse, can be null.
	 */
	public TSENode getTargetNode() {
		return m_toNode;
	}

	/*
	 * Creates the UI drawing for the interactive edge creation.
	 */
	protected ETGenericEdgeUI createEdgeUI() {
		TSEGraphWindow window = getGraphWindow();
		if (window != null) {
			ETGenericEdgeUI currentUI = (ETGenericEdgeUI) window.getCurrentEdgeUI();
			if (currentUI != null) {
				ETGenericEdgeUI ui = (ETGenericEdgeUI) currentUI.clone();
				ui.setOwner(interactiveEdge);
				return ui;
			}
		}
		return null;
	}

	/*
	 * Creates the temp node used by the edge tool for interactive dragging, its gets deleted when the tool
	 * completes or cancels.
	 */
	protected TSENode createHiddenNode() {
		TSENode theHiddenNode = (TSENode) getGraphWindow().getGraph().addNode();
		theHiddenNode.setWidth(.01);
		theHiddenNode.setHeight(.01);
		theHiddenNode.setVisible(false);
		return theHiddenNode;
	}

	// Expose the hidden node
	protected IETNode getHiddenNode() {
		return (IETNode) m_hiddenNode;
	}

	/*
	 * Creates the edge used durning the drag operation.
	 */
	protected ETEdge createinteractiveEdge() {
		if (m_fromNode != null && m_hiddenNode != null) {
			return (ETEdge) getGraphWindow().getGraph().addEdge(m_fromNode, m_hiddenNode);                        
                }                    
		else
			return null;
	}

	/*
	 * Returns a pointer to the Edge the was created by this state object.
	 */
	public TSEEdge getCreatedEdge() {
		return createdEdge;
	}

	public TSConstRect getSourceNodeBounds() {
		return getSourceNode() != null ? getSourceNode().getBounds() : null;
	}

	public TSConstRect getTargetNodeBounds() {
		return getTargetNode() != null ? getTargetNode().getBounds() : null;
	}

	/*
	 * Gets called after the new edge has been reconnected to the target node.
	 */
	protected void postConnectEdge() {	
		onPreDrawingAreaNofityObjCreated();
		notifyDrawingAreaObjCreated();
		onPostDrawingAreaNotifyedObjCreated();
	}
	
	/*
	 * Gets called just before the diagram gets notified that an edge has been 
	 * created. The default implementation does nothing.
	 */
	protected void onPreDrawingAreaNofityObjCreated()
	{
	}
	
	protected void notifyDrawingAreaObjCreated() {
		TSEEdgeUI edgeUI = getCreatedEdge().getEdgeUI();
		if (edgeUI != null) {
			getDrawingArea().onInteractiveObjCreated(edgeUI);
			return;
		} else {
			try {
				ETGenericEdgeUI ui = (ETGenericEdgeUI) getGraphWindow().getCurrentEdgeUI();
				ETGenericEdgeUI newUI = ETUIFactory.createEdgeUI(ui.getClass().getName(), ui.getInitStringValue(), ui.getDrawEngine().getClass().getName(), ui.getDrawingArea());

				newUI.setDrawEngineClass(ui.getDrawEngine().getClass().getName());
				getCreatedEdge().setUI(newUI);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * Make sure the only object selected it the new edge just created. 
	 * It gets called after the drawing area has been notified that an edge has
	 * been created.
	 */
	protected void onPostDrawingAreaNotifyedObjCreated()
	{
		TSEGraphWindow diagramWindow = getGraphWindow();
		if (diagramWindow != null && getCreatedEdge() != null)
		{
			diagramWindow.deselectAll(false);
			getCreatedEdge().setSelected(true);
			// Repaint the window.
			diagramWindow.drawGraph();
			diagramWindow.fastRepaint();		
		}
	}	

	protected String getMetaTypeInitString() {
		IEdgeDrawEngine de = m_edgeUI.getDrawEngine() instanceof IEdgeDrawEngine ? (IEdgeDrawEngine) m_edgeUI.getDrawEngine() : null;
		if (de != null) {
			// Used for the Association Ends.
			String metaTypeExtention = de.getMetaTypeInitString();
			String metaType = getEdgeMetaType();
			return metaTypeExtention != null && metaType != null ? metaType.concat(" ").concat(metaTypeExtention) : metaType;
		}
		return null;
	}

	protected IElement verifyAndCreateEdgeRelation() {
		return getVerification().verifyAndCreateEdgeRelation((IETEdge) getCreatedEdge(), getDrawingArea().getNamespace(), getEdgeMetaType(), getMetaTypeInitString());
	}

	protected void showNoDropCursor() {
		this.setCursor(m_noDropCursor);
	}

	protected void showCreateRelationCursor() {
		this.setCursor(m_defaultCursor);
	}

	protected boolean onMousePressAddPathNode(MouseEvent pEvent) {
		boolean callBase = true;
		// Add the path nodes,
		TSEPNode pathNode = addPathNode(m_lastMousePt);

		if (pathNode != null)
			pathNode.setSelected(true);

		getGraphWindow().drawGraph();
		getGraphWindow().fastRepaint();
		return callBase;

	}

	protected boolean doMouseClickedNode(ETNode hitNode, TSConstPoint mousePoint) {
		boolean callBase = true;

		if (m_fromNode == null) {
			if (this.setSourceNode(hitNode)) {
				// Move the hidden node to the current mouse pos.

				m_hiddenNode.setCenter(mousePoint.getX(), mousePoint.getY());

				if (!connectStartingEdgeEvent(mousePoint)) {
					cancelAction(); // Abort
					callBase = false;
				}
			} else {
				/*
				 * We can't connect to this type of node, GhostBusters..
				 */
				resetInteractiveObjects();
				showNoDropCursor();
			}
		} else if (m_toNode == null) {
			this.setTargetNode(hitNode);
		}
		return callBase;
	}

	/*
	 * Returns the node at the current mouse position.
	 */
	protected IETNode getNodeAt(MouseEvent pEvent) {
		return (IETNode) getObject(getNonalignedWorldPoint(pEvent));
	}

	/*
	 * Returns the node at the point.
	 */
	protected IETNode getNodeAt(TSConstPoint worldPt) {
		return (IETNode) getObject(worldPt);
	}

	protected boolean onMousedClickNode(ETNode hitNode, TSConstPoint mousePoint) {
		return doMouseClickedNode(hitNode, mousePoint);
	}

	protected boolean onMousePressOnGraph(MouseEvent pEvent) {
		return true; // call the base 		
	}

	protected boolean shouldAddContainerPathNode(ETNode hitNode)
	{
		if (m_fromNode != null && hitNode != null && hitNode.getEngine() instanceof IADContainerDrawEngine)
		{
			if (interactiveEdge != null)
			{
				//	Make sure both nodes are contained with in the same container.
				IADContainerDrawEngine containerDrawEngine = (IADContainerDrawEngine)hitNode.getEngine();
                                //Fix for Bug#6327072
//				ETList <IPresentationElement> contained = containerDrawEngine.getContained();
                                ETList <IPresentationElement> contained = containerDrawEngine.getContained(false, true, false);
                                
				IPresentationElement fromPE = TypeConversions.getPresentationElement((IETGraphObject)m_fromNode);
				if (contained != null && fromPE != null && contained.find(fromPE))
				{					
					return true;
				}
			}
		}
		return false;
	}
	
	public void onMousePressed(MouseEvent pEvent) {
            // J1851-Double-clicking when adding link puts it inside node
            if (pEvent.getClickCount() == 2) {
                return;
            }
            
            boolean callBase = false;
            if (isLeftMouseEvent(pEvent)) {
                ETNode hitNode = getObject(pEvent);
                boolean shouldAddContainerPathNode = false;
                if (hitNode != null && hitNode.getEngine() instanceof IADContainerDrawEngine) {
                    shouldAddContainerPathNode = shouldAddContainerPathNode(hitNode);
                }
                
                if (hitNode != null && shouldAddContainerPathNode == false) {
                    callBase = onMousedClickNode(hitNode, getAlignedWorldPoint(pEvent));
                } else if (interactiveEdge != null || shouldAddContainerPathNode) {
                    // Add the path nodes,
                    callBase = onMousePressAddPathNode(pEvent);
                } else
                    callBase = onMousePressOnGraph(pEvent);
            }
            
            // Never call the base class it screws up the selection state.
            // The the selection state still gets the mouse event after we connect the node
            // which isn't what we want.  Only the edge should be selected after the
            // the relation is created.
            
//		if (callBase)
//			super.onMousePressed(pEvent);
            
            if (pEvent.isPopupTrigger() == true) {
                stopMouseInput();
                setDefaultState();
            }
        }

	public void onMouseReleased(MouseEvent pEvent) {
            if (pEvent.isPopupTrigger() == true) {
                stopMouseInput();
                setDefaultState();
                
//         SwingUtilities.invokeLater(new Runnable()
//         {
//            public void run()
//            {
//               Util.forceGC();
//            }
//         });
            }
            
            super.onMouseReleased(pEvent);
            
        }

	/*
	 * Adds a path node to the edge at the world point.
	 */
	protected TSEPNode addPathNode(TSConstPoint point) {
		if (interactiveEdge == null || point == null || fireShouldCreateBendEvent(point) == false)
			return null;

		TSPEdge pathEdge = interactiveEdge.getLastDrawablePEdge();
		if (pathEdge == null)
			pathEdge = interactiveEdge.getSourceEdge();

		return (TSEPNode) interactiveEdge.addPathNode(pathEdge, point);
	}

	/*
	 * Adds a path node to the edge at the current mouse postion.
	 */
	protected TSEPNode addPathNode(MouseEvent pEvent) {
		return addPathNode(getAlignedWorldPoint(pEvent));
	}

	/*
	 * EdgeUI from drawing.
	 */
	public void paint(TSEGraphics graphics) {
		if (m_edgeUI != null)
			m_edgeUI.draw(graphics);

		super.paint(graphics);
	}

	/*
	 *  Override the default node hit testing so it doesn't return our hidden node.
	 */
	public TSENode getNodeAt(TSConstPoint point, TSEObject referenceNode, TSEGraph onGraph) {
		TSENode node = super.getNodeAt(point, referenceNode, onGraph);
		if (node != null && node == m_hiddenNode)
			node = super.getNodeAt(point, m_hiddenNode, onGraph);
		return node;
	}

	/*
	 * Returns the node at the current mouse position.
	 */
	protected ETNode getObject(MouseEvent pEvent) {
		return getObject(getNonalignedWorldPoint(pEvent));
	}

	/*
	 * Returns the node at the point.
	 */
	protected ETNode getObject(TSConstPoint worldPt) {
		TSEObject obj = this.getNodeAt(worldPt, null, this.getGraphWindow().getGraph());
//        ETNode node = (ETNode)obj;
//        node.getETUI().getDrawEngine();
//		return obj != null && obj instanceof ETNode && obj != m_hiddenNode ? (ETNode) obj : null;
        
        ETNode retVal = null;
        if((obj instanceof ETNode) && (obj != m_hiddenNode))
        {
            retVal = getNodeUnderCombinedFragement(worldPt, (ETNode)obj);
        }
        return retVal;
	}

    protected ETNode getNodeUnderCombinedFragement(TSConstPoint worldPt,
                                                   ETNode node)
    {
        ETNode retVal = node;
            
        IDrawEngine engine = retVal.getETUI().getDrawEngine();
        if(engine instanceof ICombinedFragmentDrawEngine)
        {
            TSENode obj = this.getNodeAt(worldPt, node, this.getGraphWindow().getGraph());
            if((obj instanceof ETNode) && (obj != m_hiddenNode))
            {
                retVal = getNodeUnderCombinedFragement(worldPt, (ETNode)obj);
            }
            
        }
        
        return retVal;
    }
    
	/*
	 * Used by the autoscroller.
	 */
	protected TSConstPoint getUpdateVisiblePt(MouseEvent event) {
		TSConstPoint fromPt = m_lastMousePt;
		TSConstPoint toPt = getAlignedWorldPoint(event);

		int mvBy = 20;
		double deltaX = fromPt.getX() - toPt.getX() < 0 ? mvBy : -1 * mvBy;
		double deltaY = fromPt.getY() - toPt.getY() < 0 ? mvBy : -1 * mvBy;
		return new TSConstPoint(toPt.getX() + deltaX, toPt.getY() + deltaY);
	}

	/*
	 * Move the hidden node to the current mouse position.
	 */
	public void onMouseMoved(MouseEvent event) {

		TSConstPoint mousePoint = getAlignedWorldPoint(event);

                //Bug 6176245 Start
                //make sure the starting node really exists if its not null
                if (m_fromNode != null) {
                    IElement elem = getNodeElement(m_fromNode);
                    if (elem == null || elem.isDeleted()) {
                        resetState();
                    }
                }
                //End
		//	super.onMouseMoved(event);
		if (m_hiddenNode != null) {
                    if(interactiveEdge != null)
			interactiveEdge.setSelected(false);                     
                    
			// Make sure the edge isn't selected.		
			m_hiddenNode.setCenter(mousePoint.getX(), mousePoint.getY());

			if (m_lastMousePt == null)
				m_lastMousePt = mousePoint;

			TSConstPoint movePoint = getUpdateVisiblePt(event);
			updateVisibleArea(movePoint, true);
			if (movePoint.getX() != m_lastMousePt.getX() || movePoint.getY() != m_lastMousePt.getY())
				m_lastMousePt = mousePoint;

			onVerifyMouseMove(mousePoint);
		} else {
//			onMouseMovedShowCreateRelationCursor(mousePoint, this.getObjectAt(mousePoint) != null);
                    onMouseMovedShowCreateRelationCursor(mousePoint, 
                        this.getHitTesting().getGraphObjectAt(mousePoint, this.getGraph(), true) != null);
                }
                
                //When the mouse moves over to the diagram editor, set the drawing diagram in focus to listen to key event.
                // Fixing issue 78400
                IDrawingAreaControl cntrl = this.getDrawingArea();
                boolean focused = cntrl.isFocused();
                if (!focused) {
                    cntrl.setFocus();
                }
	}

	protected void onMouseMovedShowCreateRelationCursor(TSConstPoint mousePoint, boolean overNode) {
		showCreateRelationCursor();
	}

	/*
	 * onVerifyMouseMove(TSConstPoint worldPt)
	 */
	protected boolean onVerifyMouseMove(TSConstPoint worldPt) {
		boolean rc = false;
		ETNode hitNode = getObject(worldPt);
		if (hitNode != null && getVerification() != null) {
			if (getVerification().verifyFinishNode(getNodeElement(getSourceNode()), getNodeElement(hitNode), getEdgeMetaType())) {
				onMouseMovedShowCreateRelationCursor(worldPt, true);
				rc = this.fireEdgeMouseMoveEvent(hitNode, worldPt);
			} else
				showNoDropCursor();
		} else
			showNoDropCursor();

		return rc;
	}

	public String getEdgeMetaType() {
		return m_edgeUI != null ? m_edgeUI.getDrawEngine().getElementType() : null;
	}

	/*
	 * Auto scrolls the window, and repaints the graphWindow
	 */
	public boolean updateVisibleArea(TSConstPoint point, boolean redraw) {
            if (interactiveEdge != null) {
		addDirtyRegion(interactiveEdge);
            }
		boolean willScroll = super.updateVisibleArea(point, false);
		if (redraw) {
			getGraphWindow().drawGraph();
			getGraphWindow().fastRepaint();
		}

		return willScroll;
	}

	protected IDrawingAreaControl getDrawingArea() {
		ADGraphWindow graphWindow = getGraphWindow() instanceof ADGraphWindow ? (ADGraphWindow) getGraphWindow() : null;
		return graphWindow != null ? graphWindow.getDrawingArea() : null;
	}

	/**
	 * Connects the edge to the node.  Sends out the onStartingEdgeEvent to 
	 * determine if the is able to be created.
	 * 
	 * @param pt The location that the user started the edge.
	 * @return <code>true</code> if the edge can be created.
	 */
	protected boolean connectStartingEdgeEvent(TSConstPoint pt) {
		boolean retVal = true;

		IAddEdgeEvents eventDispatcher = getEventDispatcher();
		if (eventDispatcher != null) {
			ETTripleT < TSConnector, Integer, IETPoint > data = eventDispatcher.fireStartingEdgeEvent(m_fromNode, pt);
			if (data != null) {
				Integer canceled = data.getParamTwo();
				if (canceled != null && canceled.longValue() > 0) {
					retVal = false;
				} else if ((interactiveEdge != null) && (data.getParamOne() != null)) {
					interactiveEdge.setSourceConnector(data.getParamOne());
				}
			}
		}
		return retVal;
	}

	/*
	 * Returns false if we shouldn't create path nodes along the edge.
	 */
	public boolean fireShouldCreateBendEvent(TSConstPoint pt) {
		return getEventDispatcher() != null ? getEventDispatcher().fireShouldCreateBendEvent(pt) : false;
	}

	/*
	 * Returns the AddEdge Event Dispatcher.
	 */
	protected IAddEdgeEvents getEventDispatcher() {
		if (m_eventDispatcher == null)
			m_eventDispatcher = new AddEdgeEventDispatcher(getDrawingArea().getDrawingAreaDispatcher(), getDrawingArea().getDiagram(), m_edgeUI != null ? m_edgeUI.getInitStringValue() : "");

		return m_eventDispatcher;
	}

	/*
	 * Returns false if the pNodeUnderMouse isn't valid.
	 */
	protected boolean fireEdgeMouseMoveEvent(IETNode pNodeUnderMouse, TSConstPoint point) {
		IAddEdgeEvents eventDispatcher = getEventDispatcher();
		if (eventDispatcher != null) {
			return eventDispatcher.fireEdgeMouseMoveEvent(m_fromNode, pNodeUnderMouse, point);
		}
		return false;
	}

	protected boolean isLeftMouseEvent(MouseEvent pEvent) {
		return SwingUtilities.isLeftMouseButton(pEvent);
	}

	protected boolean isRightMouseEvent(MouseEvent pEvent) {
		return !isLeftMouseEvent(pEvent);
	}

	protected ETPairT<List,List> getReconnectEdgeBendPoints()
	{
		java.util.List bendPoints = interactiveEdge.bendPoints();
		java.util.List newBendPoints = new ETArrayList<TSConstPoint>();
		IteratorT<TSConstPoint> iter = new IteratorT<TSConstPoint>(bendPoints);
		while (iter.hasNext())
		{
			newBendPoints.add((TSConstPoint)iter.next().clone());
		}
		return new  ETPairT<List,List>(bendPoints,newBendPoints);
	}
	
	/*
	 * Helper function that prints the pathdiagraph to the ETSystem.out.
	 */
	public static void printBendPoints(TSEEdge pEdge, final String prepend)
	{
		IteratorT<TSConstPoint> iter = new IteratorT<TSConstPoint>(pEdge.bendPoints());
		ETSystem.out.print(prepend);
		while (iter.hasNext())
		{
			ETSystem.out.print(iter.next().toString());
			ETSystem.out.print(", ");
		}
		ETSystem.out.println("");
	}

	// Data
	protected ETEdge createdEdge = null;
	protected ETEdge interactiveEdge = null;
	protected ETNode m_fromNode = null;
	protected ETNode m_toNode = null;

	protected ETGenericEdgeUI m_edgeUI = null;
	protected TSENode m_hiddenNode = null;
	protected TSConstPoint m_lastMousePt = null;
	protected IEdgeVerification m_edgeVerifcation;
	protected IAddEdgeEvents m_eventDispatcher = null;

	private Cursor m_defaultCursor;
	private Cursor m_noDropCursor;
}
