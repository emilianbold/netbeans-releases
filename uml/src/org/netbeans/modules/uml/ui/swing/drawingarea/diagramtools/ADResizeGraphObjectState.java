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

import java.awt.Dimension;
import java.awt.event.MouseEvent;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETPointEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.diagramming.TSResizeControl;
import com.tomsawyer.drawing.TSSolidGeometricObject;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.TSESolidObject;
//import com.tomsawyer.editor.TSEWindowInputState;
import com.tomsawyer.editor.TSEWindowInputTool;
//import com.tomsawyer.editor.state.TSEResizeGraphObjectState;
import com.tomsawyer.editor.tool.TSEResizeGraphObjectTool;
import com.tomsawyer.graph.TSNode;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSConstSize;
import com.tomsawyer.drawing.geometry.TSConstSize;
//import com.tomsawyer.util.TSRect;
import com.tomsawyer.editor.TSEInteractiveConstants;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine;

/**
 * @author Embarcadero Technologies Inc.
 *
 */

//public class ADResizeGraphObjectState extends TSEResizeGraphObjectState
public class ADResizeGraphObjectState extends TSEResizeGraphObjectTool
{
   /// During the resize this rectangle is updated to be the final rectangle used in commitResize()
   private TSConstRect m_rectResized = null;
   

//	public ADResizeGraphObjectState(TSEWindowInputState pParentState,TSConstPoint pStartPoint,	boolean pAdjustToGrid) {
	public ADResizeGraphObjectState(TSEWindowInputTool pParentState,TSConstPoint pStartPoint,	boolean pAdjustToGrid) {
		super(pParentState, pStartPoint, pAdjustToGrid);
	}

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEWindowState#init()
    */
   protected void init() {
      super.init();
      
      TSEObject object = (TSEObject) this.getResizeGraphObject();
      IDrawingAreaControl ctrl = getDrawingArea();
      
      if (ctrl != null) {
         if (object instanceof IETGraphObject) {
            handleAspectRatio();
            ETList < IETGraphObject > affectedObjects = new ETArrayList < IETGraphObject > ();
            affectedObjects.add((IETGraphObject) object);

            ctrl.onGraphEvent(
               IGraphEventKind.GEK_PRE_RESIZE,
               new ETPointEx(getStartPoint()),
               null,
               affectedObjects);
         }
      }
   }

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.state.TSEResizeGraphObjectState#commitResize(com.tomsawyer.util.TSConstRect, com.tomsawyer.util.TSConstRect, com.tomsawyer.util.TSConstSize)
	 */
//	public void commitResize(TSConstRect oldBounds,TSConstRect newBounds,TSConstSize oldOriginalSize)
//   {
//      // In C++ there is a call to the TS base Node to determine the resize
//      // In Java we have to ensure that the newBounds are not violating our minimum rectangle
//      
//      TSConstRect newRect = newBounds;
//      if( m_rectResized != null )
//      {
//         newRect = m_rectResized;
//         m_rectResized = null;
//      }
//      
//		// super.commitResize(oldBounds, newRect, oldOriginalSize); //jyothi need to uncomment this
//		
//		TSEObject object = (TSEObject) this.getResizeGraphObject();
//		IDrawingAreaControl ctrl = getDrawingArea();
//		if (ctrl != null) {
//			if (object instanceof IETGraphObject) {
//				ETList < IETGraphObject > affectedObjects =
//					new ETArrayList < IETGraphObject > ();
//				affectedObjects.add((IETGraphObject) object);
//
//				ctrl.onGraphEvent(
//					IGraphEventKind.GEK_POST_RESIZE,
//					new ETPointEx(getStartPoint()),
//					null,
//					affectedObjects);
//			}
//		}
//
//	}
   
   //Jyothi: Fix for Bug#6420545
   public void commitResize(double x, double y)
   {   
       super.commitResize(x,y);
       TSEObject object = (TSEObject) this.getResizeGraphObject();
       IDrawingAreaControl ctrl = getDrawingArea();
       if (ctrl != null)
       {
           if (object instanceof IETGraphObject)
           {
               ETList < IETGraphObject > affectedObjects =
                       new ETArrayList < IETGraphObject > ();
               affectedObjects.add((IETGraphObject) object);
               
               ctrl.onGraphEvent(
                       IGraphEventKind.GEK_POST_RESIZE,
                       new ETPointEx(getStartPoint()),
                       null,
                       affectedObjects);
           }
       }
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEWindowInputState#onMouseDragged(java.awt.event.MouseEvent)
    */
   public void onMouseDragged(MouseEvent arg0)
   {
      // In C++ there is a call to the TS base Node to determine the resize
      // In Java we can not override the similar method, adjustSize().
      // Instead we calculate the necessary parameters here and pass the event accordingly.
      
      TSSolidGeometricObject solidObject = getResizeGraphObject();
      if (solidObject instanceof TSNode)
      {
         TSENode tsNode = (TSENode)solidObject;
         
         IDrawEngine drawEngine = TypeConversions.getDrawEngine( tsNode );
         if (drawEngine instanceof INodeDrawEngine)
         {
            INodeDrawEngine nodeEngine = (INodeDrawEngine)drawEngine;
            
            IETRect rectBounding = drawEngine.getLogicalBoundingRect( false );
         
            final int grapple = getGrapple();
           //JM
            int width = rectBounding.getIntWidth();
            int height = rectBounding.getIntHeight();
            
            if (drawEngine instanceof ILifelineDrawEngine) {
                if (grapple == TSEInteractiveConstants.GRAPPLE_N 
                        || grapple == TSEInteractiveConstants.GRAPPLE_NE 
                        || grapple == TSEInteractiveConstants.GRAPPLE_NW) {                    
                    
                    this.cancelAction();
                }
            }
            else {            
               TSConstPoint pt = getAlignedWorldPoint( arg0 );
               
               if( TSResizeControl.MODE_RIGHT == (grapple & TSResizeControl.MODE_RIGHT) )
               {
                  width = (int)Math.round( pt.getX() - rectBounding.getLeft() );
               }
               else if( TSResizeControl.MODE_LEFT == (grapple & TSResizeControl.MODE_LEFT) )
               {
                  width = (int)Math.round( rectBounding.getRight() - pt.getX() );
               }

               if( TSResizeControl.MODE_TOP == (grapple & TSResizeControl.MODE_TOP) )
               {
                  height = (int)Math.round( pt.getY() - rectBounding.getBottom() );
               }
               else if( TSResizeControl.MODE_BOTTOM == (grapple & TSResizeControl.MODE_BOTTOM) )
               {
                  height = (int)Math.round( rectBounding.getTop() - pt.getY() );
               }            
            }

            // Validate the resize, values
            Dimension validDim = drawEngine.validateResize( width, height );
            if ( validDim != null )
            {
               int iLeft = rectBounding.getLeft();
               int iTop = rectBounding.getTop();
               int iRight = rectBounding.getRight();
               int iBottom = rectBounding.getBottom();
               //JM:
               if (drawEngine instanceof ILifelineDrawEngine) {
                if (grapple == TSEInteractiveConstants.GRAPPLE_N 
                        || grapple == TSEInteractiveConstants.GRAPPLE_NE 
                        || grapple == TSEInteractiveConstants.GRAPPLE_NW) {
                    
                    m_rectResized = null;
                    this.cancelAction();
                }
            }
            else {
               
               if( TSResizeControl.MODE_RIGHT == (grapple & TSResizeControl.MODE_RIGHT) )
               {
                  iRight = (int)Math.round( rectBounding.getLeft() + validDim.width );
               }
               else if( TSResizeControl.MODE_LEFT == (grapple & TSResizeControl.MODE_LEFT) )
               {
                  iLeft = (int)Math.round( rectBounding.getRight() - validDim.width );
               }

               if( TSResizeControl.MODE_TOP == (grapple & TSResizeControl.MODE_TOP) )
               {
                  iTop = (int)Math.round( rectBounding.getBottom() + validDim.height );
               }
               else if( TSResizeControl.MODE_BOTTOM == (grapple & TSResizeControl.MODE_BOTTOM) )
               {
                  iBottom = (int)Math.round( rectBounding.getTop() - validDim.height );
               }

               tsNode.setBounds( iLeft, iTop, iRight, iBottom );
               
               //Tell the drawengine that is was resized interactively
               drawEngine.setLastResizeOriginator(ETDrawEngine.TSE_NODE_RESIZE_ORIG_INTERACTIVE);

               m_rectResized = new TSConstRect( iLeft, iTop, iRight, iBottom );
            }
            }

            getGraphWindow().drawGraph();
            getGraphWindow().fastRepaint();
         }
      }
   }
   
	//**************************************************
	// Helper Methods
	//**************************************************

	protected IDrawingAreaControl getDrawingArea() {
		ADGraphWindow graphWindow =
			getGraphWindow() instanceof ADGraphWindow
				? (ADGraphWindow) getGraphWindow()
				: null;
		return graphWindow != null ? graphWindow.getDrawingArea() : null;
	}

	protected IDiagram getDiagram() {
		IDrawingAreaControl drawingArea = getDrawingArea();
		return drawingArea != null ? drawingArea.getDiagram() : null;
	}

	protected int handleAspectRatio()
	{
		if (shouldPreserverAspectRatio())
		{
			IETGraphObject obj = getETGraphObject();
			if (obj instanceof TSESolidObject)
			{
				TSESolidObject node = (TSESolidObject)obj.getObject();
				
				//node.setAspectRatio(node.getWidth() / node.getHeight());
				node.setResizability(TSESolidObject.RESIZABILITY_PRESERVE_ASPECT);
				return 1;
			}			
		}
			
		return 0;
	}
	
	protected IETGraphObject getETGraphObject()
	{
		TSSolidGeometricObject obj = getResizeGraphObject();
		return obj instanceof IETGraphObject ? (IETGraphObject)obj : null;
	}
	
	protected boolean shouldPreserverAspectRatio()
	{
		//return  getDrawEngine() instanceof ETActorDrawEngine;
		return false;  
	}
	
	protected IDrawEngine getDrawEngine()
	{
		return TypeConversions.getDrawEngine((IETGraphObject)getResizeGraphObject());
	}
	
	// Expose this api.
	public void setGraphWindow(TSEGraphWindow window)
	{
		super.setGraphWindow(window);
	}
}
