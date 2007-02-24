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

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADContainerDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.IPackageDrawEngine;
import org.netbeans.modules.uml.ui.support.CreationFactoryHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.relationshipVerification.INodeVerification;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETDragResizeCursor;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.NoDropCursor;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSEObjectUI;
//import com.tomsawyer.editor.state.TSECreateNodeState;
import com.tomsawyer.editor.tool.TSECreateNodeTool;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;

/*
 * 
 * Creates nodes on the diagram using the mouse. 
 */
//public class ADCreateNodeState extends TSECreateNodeState {
	public class ADCreateNodeState extends TSECreateNodeTool {

	// Data
	protected TSConstPoint m_createTopLeftPoint = null;
	protected TSENode node = null;
	protected INodeVerification m_verifcation;
	protected IETRect m_minRect = null;
	protected boolean m_bStateSwitched = false;

	public ADCreateNodeState() {
		super();
	}

	/*
	 *  (non-Javadoc)
	 * @see com.tomsawyer.editor.state.TSECreateNodeState#createNode(com.tomsawyer.util.TSConstPoint)
	 */
	public void createNode(TSConstPoint point) {
		
		if (m_bStateSwitched == false && onVerifyMouseMove(point)) {
			this.getGraphWindow().deselectAll(false);
			super.createNode(point);
			node = getVirtualNode();
			if (node == null) {
				TSEGraph graph = getSelectedGraph();
				node = (TSENode) graph.nodes().get(graph.nodes().size() - 1);
			}

			if (node != null)
			{
            postCreateObj();
				TSConstRect bounds = node.getBounds();
				m_createTopLeftPoint = new TSConstPoint(bounds.getLeft(), bounds.getTop());
			} 		
		}
	}

	/*
	 * Changes to the default drawing area control state, generaly select.
	 */
	protected void setDefaultState() {
		try {
			ETSystem.out.println("ADCreateNodeState setDefaultState");
			m_bStateSwitched = true;
			this.getGraphWindow().setCurrentNodeUI(null);
			
			getDrawingArea().switchToDefaultState();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Notifies the drawing area control that a new object has interactivity been added to diagram.
	 */
	public void postCreateObj(IETNode pIETNode, boolean isInteractive) {
		try {
			IDrawingAreaControl drawingArea = getDrawingArea();
			if (drawingArea == null || pIETNode == null)
				return;

			TSEObjectUI ui = (TSEObjectUI) pIETNode.getETUI();
			if ((drawingArea != null) && (isInteractive == true)) {
				drawingArea.onInteractiveObjCreated(ui);
			}

			// Resize the new node to its contents.
			IDrawEngine engine = TypeConversions.getDrawEngine(pIETNode);
			if (engine != null) {
				engine.sizeToContents();
			}
         
   		// Repaint
			pIETNode.invalidate();
			drawingArea.refresh(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Called by the framework after a node has been added to diagram.
	 */
	protected void postCreateObj() {
		this.postCreateObj((IETNode) getCreatedNode(), true);
	}

	/*
	 * Notifies the Drawing area, 
	 */
	protected void firePostCreateEvent(IETNode pIETNode)
	{
		ETList < IETGraphObject > affectedObjects = new ETArrayList < IETGraphObject > ();
		affectedObjects.add(pIETNode);
		
		// Send event to drawing area control
		getDrawingArea().onGraphEvent(IGraphEventKind.GEK_POST_CREATE, null, null, affectedObjects);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEWindowInputState#onMousePressed(java.awt.event.MouseEvent)
	 */
	public void onMousePressed(MouseEvent pEvent) {
		// Change the state switched flag before the base is called.
		if (m_bStateSwitched == false && (pEvent.isPopupTrigger() || !isLeftMouseEvent(pEvent)))
		{
			m_bStateSwitched = true;
		}
		
		if (onVerifyMouseMove(getAlignedWorldPoint(pEvent))) {
			super.onMousePressed(pEvent);
		}
		
		if (pEvent.isPopupTrigger()) {
			setDefaultState();
		}			
	}

	/*
         *  (non-Javadoc)
         * @see com.tomsawyer.editor.TSEWindowInputState#onMouseReleased(java.awt.event.MouseEvent)
         */
        public void onMouseReleased(MouseEvent pEvent)
        {
            if (node == null)
            {
                super.onMouseReleased(pEvent);
            }
            
            if (pEvent.isPopupTrigger())
            {
                setDefaultState();
            }	      
            
            if(node != null)
            {
                firePostCreateEvent((IETNode)node);
            }
            
            // Double check that the new dragged resized node is big enough for its contents.
            if (m_minRect != null && node != null)
            {
                INodeDrawEngine nodeDrawEngine = (INodeDrawEngine) TypeConversions.getDrawEngine(node);
                if (nodeDrawEngine != null)
                {
                    // resize with min,
                    IETRect newBounds = nodeDrawEngine.getLogicalBoundingRect(false);
                    if (newBounds.getWidth() < m_minRect.getWidth() || newBounds.getHeight() < m_minRect.getHeight())
                    {
                        // Use the presentation interface becuase it uses logical units.
                        INodePresentation presentation = (INodePresentation)nodeDrawEngine.getPresentation();
                        presentation.resize( Math.max(newBounds.getWidth(), m_minRect.getWidth()), Math.max(newBounds.getHeight(), m_minRect.getHeight()), true);
                        refresh();
                    }
                }
            }
            
            
            // reset our data the mouse has been released.
            m_createTopLeftPoint = null;
            node = null;
            m_verifcation = null;
            m_minRect = null;
        }

	/*
	 * Returns the new node added to the diagram.
	 */
	public TSENode getCreatedNode() {
		return node;
	}

	/*
	 * Changes the Cursor to the no drop cursor.
	 */
	protected void showNoDropCursor() {
		setCursor(getNoDropCursor());
	}

	/*
	 * Returns the static no drop (GhostBusters) Cursor. 
	 */
	protected Cursor getNoDropCursor() {
		return NoDropCursor.getCursor();
	}

	/*
	 * 
	 */
	protected void showCreateRelationCursor() {
		setCursor(getDefaultCursor());
	}

	/*
	 *  (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEWindowInputState#onMouseMoved(java.awt.event.MouseEvent)
	 */
	public void onMouseMoved(MouseEvent event) {
            
            onVerifyMouseMove(getAlignedWorldPoint(event));
            super.onMouseMoved(event);      
            
            //When the mouse moves over to the diagram editor, set the drawing diagram in focus to listen to key event.
            // Fixing issue 78400
            IDrawingAreaControl cntrl = this.getDrawingArea();
            boolean focused = cntrl.isFocused();
            if (!focused) {
                cntrl.setFocus();
            }
        }

	/*
	 * onVerifyMouseMove(TSConstPoint worldPt)
	 */
	protected boolean onVerifyMouseMove(TSConstPoint worldPt) {
		boolean rc = false;
		if (getVerification() != null && getVerification().verifyCreationLocation(getDiagram(), new ETPoint((int) worldPt.getX(), (int) worldPt.getY()))) {
			showCreateRelationCursor();
			rc = true;
		} else
			showNoDropCursor();

		return rc;
	}
	
	/*
	 * Returns the drawingarea control.
	 */
	protected IDrawingAreaControl getDrawingArea() {
		ADGraphWindow graphWindow = getGraphWindow() instanceof ADGraphWindow ? (ADGraphWindow) getGraphWindow() : null;
		return graphWindow != null ? graphWindow.getDrawingArea() : null;
	}

	/*
	 * Returns the diagram.
	 */
	protected IDiagram getDiagram() {
		IDrawingAreaControl drawingArea = getDrawingArea();
		return drawingArea != null ? drawingArea.getDiagram() : null;
	}

	/*
	 * Demand load accessor to the INodeVerification interface.
	 */
	protected INodeVerification getVerification() {
		if (m_verifcation == null)
			m_verifcation = createVerification();
		return m_verifcation;
	}

	/*
	 * Factory function that creates an implementation of the INodeVerification interface.
	 */
	protected INodeVerification createVerification() {
		CreationFactoryHelper factory = new CreationFactoryHelper();
		return factory.getNodeVerification();
	}

	/*
	 * Expose the setGraphWindow api, its protected by the super class.
	 */
	public void setGraphWindow(TSEGraphWindow window) {
		super.setGraphWindow(window);
	}

	/*
	 * Returns true if we should resize on mouse drag.
	 */
	protected boolean shouldResizeOnDrag() {
		IDrawEngine drawEngine = TypeConversions.getDrawEngine(this.getVirtualNode());
		
		return m_bStateSwitched == false && getGraphWindow().getCurrentNodeUI() != null &&
			(drawEngine instanceof IPackageDrawEngine || drawEngine instanceof IComponentDrawEngine || drawEngine instanceof IADContainerDrawEngine);
	}
	
	protected boolean isLeftMouseEvent(MouseEvent pEvent) {
		return SwingUtilities.isLeftMouseButton(pEvent);
	}

	/*
	 * Only gets called if shouldResizeOnDrag returns true, if the node hasn't
	 * been created it will demand create it, then resize the node and refresh the window.
	 */
	protected void dragResize(MouseEvent event) {
		IDrawEngine drawEngine = TypeConversions.getDrawEngine(getVirtualNode());
		if (drawEngine instanceof INodeDrawEngine) {
			// Change the cursor
			setCursor(ETDragResizeCursor.getCursor());

			TSConstPoint pt = getAlignedWorldPoint(event);

			// Create the node then resize it.
			if (m_createTopLeftPoint == null && node == null && this.getVirtualNode() != null) {
				this.createNode(pt);

				// Store the size to Contents rect, so we can compare against the mouse drag resize. 
				drawEngine = TypeConversions.getDrawEngine(node);
				m_minRect = drawEngine != null && drawEngine.isInitialized() ? drawEngine.getLogicalBoundingRect(false) : null;
			}

			if (node != null && m_createTopLeftPoint != null) {
				INodeDrawEngine nodeDrawEngine = (INodeDrawEngine) drawEngine;

				double sizeX = Math.abs(pt.getX() - m_createTopLeftPoint.getX());
				double sizeY = Math.abs(pt.getY() - m_createTopLeftPoint.getY());

				//	Use the presentation interface becuase it uses logical units.
				// Keep the Topleft,
				INodePresentation presentation = (INodePresentation)nodeDrawEngine.getPresentation();
				presentation.resize(sizeX, sizeY, true);

				refresh();
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEWindowInputState#onMouseDragged(java.awt.event.MouseEvent)
	 */
	public void onMouseDragged(MouseEvent event) {
		boolean isLeft = isLeftMouseEvent(event) || event.isPopupTrigger();
		if (isLeft && shouldResizeOnDrag()) {
			dragResize(event);
		} 
		else if (isLeft)
		{
			super.onMouseDragged(event);
		}
		else
		{
			this.setDefaultState();
			return;
		}
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEWindowState#resetState()
	 */
	public void resetState() {
		ETSystem.out.println("CreateNodeState reset state");
		m_createTopLeftPoint = null;
		node = null;
		m_verifcation = null;
		m_minRect = null;
		//super.resetState();
                super.resetTool();
		getDrawingArea().setModelElement(null);
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEWindowState#cancelAction()
	 */
	public void cancelAction() {
		ETSystem.out.println("CreateNodeState cancelAction");
		refresh();
		m_createTopLeftPoint = null;
		node = null;
		m_verifcation = null;
		m_minRect = null;
		//super.cancelAction();
	}
	
	protected void refresh()
	{
		if (node != null)
		{
			getDrawingArea().getGraphWindow().addInvalidRegion(node);
		}
		getDrawingArea().refresh(true);
	}

}
