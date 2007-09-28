/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
