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
