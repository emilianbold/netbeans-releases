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

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.common.Util;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETGraph;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETPointEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSEObject;
//import com.tomsawyer.editor.TSEWindowState;
import com.tomsawyer.editor.TSEWindowTool;
//import com.tomsawyer.editor.state.TSEMoveSelectedState;
import com.tomsawyer.editor.tool.TSEMoveSelectedTool;
import com.tomsawyer.graph.TSGraphObject;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;

//public class ADMoveSelectedState extends TSEMoveSelectedState
public class ADMoveSelectedState extends TSEMoveSelectedTool
{	
	protected long initalMemoryUsed = 0;	
//   public ADMoveSelectedState(TSEWindowState pParentState, TSConstPoint pStartPoint, boolean pAdjustToGrid)
   public ADMoveSelectedState(TSEWindowTool pParentState, TSConstPoint pStartPoint, boolean pAdjustToGrid)
   {
      super(pParentState, pStartPoint, pAdjustToGrid);
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.state.TSEMoveSelectedState#onMouseDragged(java.awt.event.MouseEvent)
    */
   public void onMouseDragged(MouseEvent e)
   {
      // Fix J1075:  TS will sometimes throw here with a null pointer exception
      //             This was seen when moving the combined fragment interaction constraint labels.
      //             This is also what causes the label to not be drawn while the user is moving it.
      //             see also ADDrawingAreaSelectState.onMouseDragged(), and
      //                      ETGenericNodeLabelUI.drawCalloutOutline()
      try
      {
      	if (initalMemoryUsed == 0)
      	{
				initalMemoryUsed= Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
      	}
      	
        super.onMouseDragged(e);
      }
      catch (NullPointerException error)
      {
         // ETSystem.out.println("NullPointerException in ADMoveSelectedState.onMouseDragged");
      }
   }

   public void onMouseReleased(MouseEvent pEvent)
   {
      super.onMouseReleased(pEvent);

      ETList < IETGraphObject > affectedObjects = this.getSelectedObjects();

      IDrawingAreaControl drawingArea = ((ADGraphWindow) this.getGraphWindow()).getDrawingArea();

      if (drawingArea != null)
      {
         // Send event to drawing area control
         drawingArea.onGraphEvent(IGraphEventKind.GEK_POST_MOVE, new ETPointEx(this.getStartPoint()), getETPoint(pEvent), affectedObjects);
      }
      affectedObjects.clear();

//		// Run clean up tests.
//		if (shouldRunGC())
//		{
//			runBackgroundGC();
//		}
//		// Reset this puppy.
//		initalMemoryUsed = 0;
   }
   
   /*
    * returns true if we should run the Garbage collector after the dragging process completes.
    */
	protected boolean shouldRunGC()
   {
		long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		
		if (used != 0)	// This should be impossible but better to be safe.
		{
			double percentageGrowth = ((double)initalMemoryUsed / (double)used) * 10.0;				
			return percentageGrowth > 9.0;
		}
		return false;   	
   }

	protected void runBackgroundGC()
	{			
		SwingUtilities.invokeLater(new Runnable() {
			 public void run() {
				Util.runBackgroundGC();
			 }
		});	
	}
	
   protected IETPoint getETPoint(MouseEvent pEvent)
   {
      Point mousePos = pEvent.getPoint();
      return new ETPoint(mousePos.x, mousePos.y);
   }

   /*
    * Returns the select graph Objects.
    */
   public ETList < IETGraphObject > getSelectedObjects()
   {
      ETGraph graph = (ETGraph) this.getGraphWindow().getGraph();
      //return graph != null ? graph.getSelectedObjects(false, false) : null;

      // IN C++ it seems we only care about nodes during MOVE events
      ETList < IETGraphObject > selectedObjects = new ETArrayList < IETGraphObject > ();

      selectedObjects.addAll(graph.selectedNodes());

      return graph != null ? selectedObjects : null;
   }

}
