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

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETPointEx;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.diagramming.TSCutCopyPasteControl;
import com.tomsawyer.drawing.TSDGraph;
//import com.tomsawyer.editor.state.TSEPasteState;
import com.tomsawyer.editor.tool.TSEPasteTool;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.util.TSOptionData;
import org.netbeans.modules.uml.core.support.Debug;

/**
 * @author Embarcadero Technologies Inc.
 *
 */

//public class ADPasteState extends TSEPasteState
public class ADPasteState extends TSEPasteTool
{
   private List m_pastedNodeList = new ArrayList();
   private List m_pastedEdgeList = new ArrayList();
   private List m_pastedNodeLabelList = new ArrayList();
   private List m_pastedEdgeLabelList = new ArrayList();
   private List m_pastedConnectorLabelList = new ArrayList();
   private TSGraphObject m_targetGraphObj = null;
   private TSOptionData m_optionData = null;

   public void resetState()
   {
      //super.resetState();
       super.resetTool();
      getDrawingArea().setModelElement(null);
   }

   protected IDrawingAreaControl getDrawingArea()
   {
      ADGraphWindow graphWindow = getGraphWindow() instanceof ADGraphWindow ? (ADGraphWindow)getGraphWindow() : null;
      return graphWindow != null ? graphWindow.getDrawingArea() : null;
   }

   public void onMouseReleased(MouseEvent event)
   {
      TSCutCopyPasteControl clipboardControl = getCutCopyPasteControl();

      if (clipboardControl == null)
      {
         this.cancelAction();
         return;
      }

      if (clipboardControl != null && clipboardControl.canPaste())
      {
         IETPoint location = new ETPointEx(getNonalignedWorldPoint(event));
         TSDGraph pClipboardGraph = clipboardControl.getClipboardGraph();

         if (pClipboardGraph != null)
         {
            m_pastedNodeList = new ArrayList();
            m_pastedEdgeList = new ArrayList();
            m_pastedNodeLabelList = new ArrayList();
            m_pastedEdgeLabelList = new ArrayList();

            m_targetGraphObj = getTargetObject();
            // m_optionData = *** get option data here - jyothi

            try
            {
               //clipboardControl.paste(m_targetGraphObj, location.getX(), location.getY(), m_pastedNodeList, m_pastedEdgeList, m_pastedNodeLabelList, m_pastedEdgeLabelList);
                clipboardControl.paste(
                    m_targetGraphObj, m_optionData, 
                    location.getX(), location.getY(), 
                    m_pastedNodeList, m_pastedEdgeList, 
                    m_pastedNodeLabelList, m_pastedConnectorLabelList, m_pastedEdgeLabelList
                );

               ((ADDrawingAreaControl)getDrawingArea()).onPostPaste(m_targetGraphObj, m_pastedNodeList, m_pastedEdgeList, m_pastedNodeLabelList, m_pastedEdgeLabelList, true);
            }
            catch (IOException e)
            {
               e.printStackTrace();
            }
         }
      }
      this.cancelAction();
   }




   //   public void onMouseReleased(MouseEvent event)
   //   {
   //
   //		m_pastedNodeList = new ArrayList();
   //		m_pastedEdgeList = new ArrayList();
   //		m_pastedNodeLabelList = new ArrayList();
   //		m_pastedEdgeLabelList = new ArrayList();
   //
   //      IDrawingAreaControl drawingArea = getDrawingArea();
   //
   //      TSCutCopyPasteControl control = this.getCutCopyPasteControl();
   //      m_targetGraphObj = this.getTargetObject();
   //      TSDGraph clipGraph = control.getClipboardGraph();
   //
   //      ETList < IETGraphObject > affectedObjects = new ETArrayList < IETGraphObject > ();
   //
   //      affectedObjects.addAll(clipGraph.nodes());
   //      //affectedObjects.addAll(clipGraph.edges());
   //
   //      IteratorT < IETGraphObject > objIter = new IteratorT < IETGraphObject > (affectedObjects);
   //
   //      try
   //      {
   //         while (objIter.hasNext())
   //         {
   //            IETGraphObject obj = objIter.next();
   //
   //            IETGraphObjectUI objUI = obj.getETUI();
   //
   //            if (objUI != null)
   //            {
   //               String initString = objUI.getInitStringValue();
   //               String drawEngineClass = objUI.getDrawEngineClass();
   //
   //               objUI.setDrawEngine(null);
   //					
   //               if (obj.isNode())
   //               {
   //                  //TODO This should not be necessary. Just trying to see if I can reset the UI/drawengine here
   //                  //ETGenericNodeUI nodeUI = ETUIFactory.createNodeUI("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI", initString, drawEngineClass, drawingArea);
   //						((ETGenericNodeUI)objUI).setOwner((TSENode)obj);
   //						((ETNode)obj).setUI((TSEObjectUI)objUI);
   //						((GraphPresentation)((ETNode)obj).getPresentationElement()).setUI(objUI);
   //
   //                  m_pastedNodeList.add(obj);
   //               }
   //               else if (obj.isEdge())
   //               {
   //                  //ETGenericEdgeUI edgeUI = ETUIFactory.createEdgeUI("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericEdgeUI", initString, drawEngineClass, drawingArea);
   //                  //obj.setObjectView(edgeUI);
   //                  m_pastedEdgeList.add(obj);
   //               }
   //            }
   //
   //         }
   //      }
   //      catch (Exception e)
   //      {
   //         e.printStackTrace();
   //      }
   //
   //		super.onMouseReleased(event);
   //
   //      ((ADDrawingAreaControl)getDrawingArea()).onPostPaste(m_targetGraphObj, m_pastedNodeList, m_pastedEdgeList, m_pastedNodeLabelList, m_pastedEdgeLabelList, true);
   //
   //   }

   //
   //   public void onMousePressed(MouseEvent event)
   //   {
   //      // TODO Auto-generated method stub
   //      //super.onMousePressed(arg0);
   //
   //      TSCutCopyPasteControl clipboardControl = getCutCopyPasteControl();
   //
   //      if (clipboardControl == null)
   //      {
   //         Debug.out.println("ADPasteState - Something is wrong... the CutCopypastControl is null");
   //         this.cancelAction();
   //         return;
   //      }
   //
   //      boolean allowPaste = clipboardControl.canPaste();
   //
   //      if (!allowPaste)
   //      {
   //         this.cancelAction();
   //      }
   //
   //      if (clipboardControl != null && allowPaste)
   //      {
   //         Point location = event.getPoint();
   //         TSDGraph pClipboardGraph = clipboardControl.getClipboardGraph();
   //
   //         if (pClipboardGraph != null)
   //         {
   //            m_pastedNodeList = new ArrayList();
   //            m_pastedEdgeList = new ArrayList();
   //            m_pastedNodeLabelList = new ArrayList();
   //            m_pastedEdgeLabelList = new ArrayList();
   //
   //            m_targetGraphObj = getTargetObject();
   //
   //            try
   //            {
   //               clipboardControl.paste(m_targetGraphObj, location.x, location.y, m_pastedNodeList, m_pastedEdgeList, m_pastedNodeLabelList, m_pastedEdgeLabelList);
   //            }
   //            catch (IOException e)
   //            {
   //               e.printStackTrace();
   //            }
   //         }
   //      }
   //   }

   //   public void onMouseReleased(MouseEvent event)
   //   {
   //      //super.onMouseReleased(event);
   //
   //      TSCutCopyPasteControl clipboardControl = getCutCopyPasteControl();
   //
   //      if (clipboardControl == null)
   //      {
   //         Debug.out.println("ADPasteState - Something is wrong... the CutCopypastControl is null");
   //         this.cancelAction();
   //         return;
   //      }
   //
   //      boolean allowPaste = clipboardControl.canPaste();
   //
   //      if (!allowPaste)
   //      {
   //         this.cancelAction();
   //      }
   //
   //      if (clipboardControl != null && allowPaste)
   //      {
   //         Point location = event.getPoint();
   //         TSDGraph pClipboardGraph = clipboardControl.getClipboardGraph();
   //
   //         if (pClipboardGraph != null)
   //         {
   //            m_pastedNodeList = new ArrayList();
   //            m_pastedEdgeList = new ArrayList();
   //            m_pastedNodeLabelList = new ArrayList();
   //            m_pastedEdgeLabelList = new ArrayList();
   //
   //            m_targetGraphObj = getTargetObject();
   //
   //            try
   //            {
   //               clipboardControl.paste(m_targetGraphObj, location.x, location.y, m_pastedNodeList, m_pastedEdgeList, m_pastedNodeLabelList, m_pastedEdgeLabelList);
   //               ((ADDrawingAreaControl)getDrawingArea()).onPostPaste(m_targetGraphObj, m_pastedNodeList, m_pastedEdgeList, m_pastedNodeLabelList, m_pastedEdgeLabelList, true);
   //               this.commitPasteOperation();
   //            }
   //            catch (IOException e)
   //            {
   //               e.printStackTrace();
   //            }
   //         }
   //      }
   //      this.cancelAction();
   //   }

   //   public void onMouseReleased(MouseEvent event)
   //   {
   //		super.onMouseReleased(event);
   //
   //      IDrawingAreaControl drawingArea = getDrawingArea();
   //
   //      TSCutCopyPasteControl control = this.getCutCopyPasteControl();
   //      TSGraphObject targetGraphObj = this.getTargetObject();
   //      TSDGraph clipGraph = control.getClipboardGraph();
   //
   //      List nodeList = clipGraph.nodes();
   //      List edgeList = new ArrayList();
   //      List nodeLabelList = new ArrayList();
   //      List edgeLabelList = new ArrayList();
   //
   //      ETList < IETGraphObject > affectedObjects = new ETArrayList < IETGraphObject > ()
   //      {
   //         public boolean addAll(List c)
   //         {
   //            return c != null ? super.addAll(c) : false;
   //         }
   //      };
   //
   //      affectedObjects.addAll(clipGraph.nodes());
   //      affectedObjects.addAll(clipGraph.edges());
   //
   //      IteratorT < IETGraphObject > objIter = new IteratorT < IETGraphObject > (affectedObjects);
   //
   //      try
   //      {
   //         while (objIter.hasNext())
   //         {
   //            IETGraphObject obj = objIter.next();
   //
   //            IETGraphObjectUI objUI = obj.getETUI();
   //
   //            if (objUI != null)
   //            {
   //               String initString = objUI.getInitStringValue();
   //               String drawEngineClass = objUI.getDrawEngineClass();
   //               
   //               //objUI.setDrawEngine(null);
   //
   //               if (obj.isNode())
   //               {
   //                  //TODO This should not be necessary. Just trying to see if I can reset the UI/drawengine here
   //                  //ETGenericNodeUI nodeUI = ETUIFactory.createNodeUI("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI", initString, drawEngineClass, drawingArea);
   //                  //obj.setObjectView(nodeUI);
   //                  nodeList.add(obj);
   //               }
   //               else if (obj.isEdge())
   //               {
   //                  //ETGenericEdgeUI edgeUI = ETUIFactory.createEdgeUI("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericEdgeUI", initString, drawEngineClass, drawingArea);
   //                  //obj.setObjectView(edgeUI);
   //                  edgeList.add(obj);
   //               }
   //            }
   //
   //         }
   //      }
   //      catch (Exception e)
   //      {
   //         e.printStackTrace();
   //      }
   //
   //      ((ADDrawingAreaControl)getDrawingArea()).onPostPaste(targetGraphObj, nodeList, edgeList, nodeLabelList, edgeLabelList, true);
   //
   //   }

}
