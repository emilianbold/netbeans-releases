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

import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.PresentationElementSyncState;
import org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.netbeans.modules.uml.ui.support.umltsconversions.RectConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PointConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PresentationHelper;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.StretchContext;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.StretchContextType;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETHorzDragCursor;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETHorzNoDragCursor;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETVertDragCursor;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETVertNoDragCursor;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSENode;
//import com.tomsawyer.editor.TSEWindowInputState;
import com.tomsawyer.editor.TSEWindowInputTool;
//import com.tomsawyer.editor.TSEWindowState;
import com.tomsawyer.editor.TSEWindowTool;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Iterator;

/**
 * @author KevinM
 *
 */
//public class DragManager extends TSEWindowInputState implements IDragManager {
	public class DragManager extends TSEWindowInputTool implements IDragManager {

	protected int m_orientation = IETZoneDividers.DMO_HORIZONTAL;
	protected ETList < IPresentationElement > m_abovePEs = null;
	IETRect m_rectAbovePEs = new ETRect();

	/// TS logical axis values that limit the movement of the drag operation
	protected int m_topLogical = Integer.MAX_VALUE;
	protected int m_bottomLogical = Integer.MIN_VALUE;

   private ICompartment m_cpStretchCompartment = null;
   private IStretchContext m_stretchContext = null; // created in CreateContext()
	private INodeDrawEngine m_engine = null;           // created in SendStretchContext()

   ETRect m_rectLastMouseMove = new ETRect();

	/// Presentation elements that are "below" the TS logical location
	/// for the vertical movement, "below" is to the left of the cursor
	protected ETList < IPresentationElement > m_belowPEs = null;
	IETRect m_rectBelowPEs = new ETRect();

	/**
	 * 
	 */
	public DragManager(TSEGraphWindow graphWindow)
   {
		super(graphWindow.getCurrentState());
		setGraphWindow(graphWindow);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDragManager#reset()
	 */
	public void reset()
   {
		//super.resetState();
       super.resetTool();
      
      m_orientation = IETZoneDividers.DMO_HORIZONTAL;

      m_topLogical = Integer.MAX_VALUE;
      m_bottomLogical = Integer.MIN_VALUE;

      m_cpStretchCompartment = null;

      m_abovePEs = null;
      m_belowPEs = null;

      m_rectAbovePEs.setRectEmpty();
      m_rectBelowPEs.setRectEmpty();

      m_stretchContext = null;
      m_engine = null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDragManager#getOrientation()
	 */
	public int getOrientation() {
		return m_orientation;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDragManager#setOrientation(int)
	 */
	public void setOrientation(int orientation) {
		m_orientation = orientation;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDragManager#stretchCompartment(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment)
	 */
	public void setStretchCompartment( ICompartment compartment )
   {
      m_cpStretchCompartment = compartment;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDragManager#setTop(int)
	 */
	public void setTop(int topLogical)
   {
      m_topLogical = topLogical;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDragManager#setBottom(int)
	 */
	public void setBottom(int bottomLogical)
   {
      m_bottomLogical = bottomLogical;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDragManager#addElementsAbove()
	 */
	public void addElementsAbove( ETList < IPresentationElement > presentationElements )
   {
      m_abovePEs = presentationElements;
      m_rectAbovePEs = getLogicalBoundingRect( presentationElements );
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDragManager#addElementsBelow()
	 */
	public void addElementsBelow( ETList < IPresentationElement > presentationElements )
   {
      m_belowPEs = presentationElements;
      m_rectBelowPEs = getLogicalBoundingRect( presentationElements );
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEWindowInputState#onMouseClicked(java.awt.event.MouseEvent)
	 */
	public void onMouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		super.onMouseClicked(arg0);
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEWindowInputState#onMouseDragged(java.awt.event.MouseEvent)
	 */
	public void onMouseDragged( MouseEvent event )
   {
      if( validateMouseMove( event ) )
      {
         if( ! sendStretchContext( event, StretchContextType.SCT_STRETCHING ))
         {
            invalidateDrawLine();
         
            m_rectLastMouseMove = new ETRect( getGraphWindow().getBounds() );
         
            switch( m_orientation )
            {
               case IETZoneDividers.DMO_HORIZONTAL:
                  m_rectLastMouseMove.setTop( event.getY() );
                  m_rectLastMouseMove.setBottom( m_rectLastMouseMove.getTop() + 1 );
                  break;

               case IETZoneDividers.DMO_VERTICAL:
                  m_rectLastMouseMove.setLeft( event.getX() );
                  m_rectLastMouseMove.setRight( m_rectLastMouseMove.getLeft() + 1 );
                  break;

               default:
                  break;
            }

            invalidateDrawLine();
         }
      }
      
		// Don't call the super class
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEWindowInputState#onMouseMoved(java.awt.event.MouseEvent)
	 */
	public void onMouseMoved( MouseEvent event )
   {		
      // TODO Auto-generated method stub
      super.onMouseMoved( event );
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEWindowInputState#onMousePressed(java.awt.event.MouseEvent)
	 */
	public void onMousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		super.onMousePressed(arg0);
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEWindowInputState#onMouseReleased(java.awt.event.MouseEvent)
	 */
        public void onMouseReleased( MouseEvent event ) {
            sendStretchContext( event, StretchContextType.SCT_FINISH );
            
            invalidateDrawLine();
            reset();
            
            //finalizeState();
            finalizeTool();
        }

	/// Returns true when the mouse has moved to a valid new location
	protected boolean validateMouseMove(MouseEvent pMouseEvent) {
		boolean bMouseMoveIsValid = false;

		Cursor cursor = null;

		switch (getOrientation()) {
			case IETZoneDividers.DMO_HORIZONTAL :
				{
					// 
					int lMouseLoc = (int)this.getNonalignedWorldY(pMouseEvent);

					if ((lMouseLoc < (m_topLogical - m_rectAbovePEs.getHeight()))
						&& (lMouseLoc > (m_bottomLogical + m_rectBelowPEs.getHeight()))) {
						bMouseMoveIsValid = true;
						cursor = ETHorzDragCursor.getCursor();

						int lDeltaAbove = lMouseLoc - m_rectAbovePEs.getBottom();
						if (lDeltaAbove > 0) {
							movePresentationElements(m_abovePEs, new ETPoint(0, lDeltaAbove));
							m_rectAbovePEs = getLogicalBoundingRect( m_abovePEs );
						}

						int lDeltaBelow = lMouseLoc - m_rectBelowPEs.getTop();
						if (lDeltaBelow < 0) {
							movePresentationElements(m_belowPEs, new ETPoint(0, lDeltaBelow));
							m_rectBelowPEs = getLogicalBoundingRect( m_belowPEs );
						}
					} else {
						cursor = ETHorzNoDragCursor.getCursor();
					}
				}
				break;

			case IETZoneDividers.DMO_VERTICAL :
				{
					int lMouseLoc =(int)this.getNonalignedWorldX(pMouseEvent);

					if ((lMouseLoc < (m_topLogical - m_rectAbovePEs.getWidth()))
						&& (lMouseLoc > (m_bottomLogical + m_rectBelowPEs.getWidth()))) {
						bMouseMoveIsValid = true;
						cursor = ETVertDragCursor.getCursor();

						int lDeltaRight = lMouseLoc - m_rectAbovePEs.getLeft();
						if (lDeltaRight > 0) {
							movePresentationElements(m_abovePEs, new ETPoint(lDeltaRight, 0));
							m_rectAbovePEs = getLogicalBoundingRect( m_abovePEs );
						}

						int lDeltaLeft = lMouseLoc - m_rectBelowPEs.getRight();
						if (lDeltaLeft < 0) {
							movePresentationElements(m_belowPEs, new ETPoint(lDeltaLeft, 0));
							m_rectBelowPEs = getLogicalBoundingRect( m_belowPEs );
						}
					} else {
						cursor = ETVertNoDragCursor.getCursor();
					}
				}
				break;

			default :
				break;
		}

		if (cursor != null)
			setCursor(cursor);

		return bMouseMoveIsValid;
	}

	protected IDrawingAreaControl getDrawingArea() {
		ADGraphWindow graphWindow = getGraphWindow() instanceof ADGraphWindow ? (ADGraphWindow) getGraphWindow() : null;
		return graphWindow != null ? graphWindow.getDrawingArea() : null;
	}
	
	/// invalidates the area containing the draw line
	protected void invalidateDrawLine()
   {
		IDrawingAreaControl drawingArea = getDrawingArea();
		if (drawingArea != null)
		{
			if (this.m_engine != null)
			{
				drawingArea.refreshRect(m_engine.getLogicalBoundingRect(true));
			}
			else
			{
				drawingArea.refreshRect(m_rectLastMouseMove);
			}
				 
		}
	}

	/// Send the context informing the stretch compartment about the stretch.
	protected boolean sendStretchContext(MouseEvent event, int nStretchContextType)
   {
      boolean bStretchContextSent = false;

      try
      {
         if( m_cpStretchCompartment != null )
         {
            final TSConstPoint point = getWorldPoint( event );

            if( m_stretchContext == null )
            {
               m_stretchContext = new StretchContext();
               if( m_stretchContext != null )
               {
                  IETPoint etStartPoint = PointConversions.newETPoint( point );
                  m_stretchContext.setStartPoint( etStartPoint );
               }
            }

            if( m_stretchContext != null )
            {
               m_stretchContext.setType( nStretchContextType );

               IETPoint etFinishPoint = PointConversions.newETPoint( point );
               m_stretchContext.setFinishPoint( etFinishPoint );

               sendStretchContext( m_cpStretchCompartment );

               bStretchContextSent = true;
            }
         }
      }
      catch( Exception e )
      {
         e.printStackTrace();
      }

      return bStretchContextSent;
	}

	/// Send the context informing the stretch compartment about the stretch.
	protected void sendStretchContext(ICompartment compartment)
   {
      // compartment is checked by setCompartment(), below

      if( m_engine == null )
      {
         IDrawEngine engine = compartment.getEngine();
         if (engine instanceof INodeDrawEngine)
         {
            m_engine = (INodeDrawEngine)engine;
         }
      }

      if( m_engine != null )
      {
          m_stretchContext.setCompartment( compartment );
          m_engine.stretch( m_stretchContext );
      }
	}

	/// Moves the presentation elements vertically the specified amount
	protected void movePresentationElements(ETList < IPresentationElement > pPEs, final IETPoint ptLogicalDelta) {
		if (pPEs != null && ptLogicalDelta != null && (ptLogicalDelta.getX() != 0 ||  ptLogicalDelta.getY() != 0))
		{
			Iterator<IPresentationElement> iter = pPEs.iterator();
			while (iter.hasNext())
			{
				IDrawEngine engine = TypeConversions.getDrawEngine(iter.next());
				if (engine != null)
				{
					TSENode node = TypeConversions.getOwnerNode(engine);
					if (node != null)
					{						
						engine.onGraphEvent(IGraphEventKind.GEK_PRE_MOVE);
						node.moveBy((double)ptLogicalDelta.getX(), (double)ptLogicalDelta.getY());
						engine.onGraphEvent(IGraphEventKind.GEK_POST_MOVE);
					}
				}
			}
		}
	}
   
   protected IETRect getLogicalBoundingRect( ETList< IPresentationElement > pes )
   {
      IETRect rect = null;
      
      if( pes != null )
      {
         rect = TypeConversions.getLogicalBoundingRect( pes, false );
      }
      else
      {
         rect = new ETRect();
      }
   
      return rect;
   }
}
