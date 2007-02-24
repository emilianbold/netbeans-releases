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



package org.netbeans.modules.uml.ui.controls.drawingarea;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;

import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.EdgeMapLocation;
import org.netbeans.modules.uml.core.metamodel.diagrams.GraphicExportDetails;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IEdgeMapLocation;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicExportDetails;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicMapLocation;
import org.netbeans.modules.uml.core.metamodel.diagrams.ILabelMapLocation;
import org.netbeans.modules.uml.core.metamodel.diagrams.INodeMapLocation;
import org.netbeans.modules.uml.core.metamodel.diagrams.LabelMapLocation;
import org.netbeans.modules.uml.core.metamodel.diagrams.NodeMapLocation;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETGraph;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETEGraphImageEncoder;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericGraphUI;
import org.netbeans.modules.uml.ui.support.ImageTransferable;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETPointEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import com.tomsawyer.diagramming.TSCutCopyPasteControl;
import com.tomsawyer.drawing.TSDGraph;
import com.tomsawyer.drawing.TSDNode;
import com.tomsawyer.drawing.TSPEdge;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSEGraphManager;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.graph.TSNode;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSExpTransform;
import com.tomsawyer.drawing.geometry.TSExpTransform;
import com.tomsawyer.util.TSObject;
//import com.tomsawyer.util.TSRect;
import com.tomsawyer.drawing.geometry.TSRect;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;
import com.tomsawyer.util.command.TSCommand;
import com.tomsawyer.editor.TSEGraphImageEncoder;
import com.tomsawyer.editor.complexity.TSEHidingManager;
import com.tomsawyer.editor.complexity.command.TSEHideCommand;
import com.tomsawyer.editor.complexity.command.TSEUnhideCommand;
//import com.tomsawyer.editor.state.TSEPasteState;
import com.tomsawyer.editor.tool.TSEPasteTool;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.support.visitors.ETFirstSubjectSameVistor;
import org.netbeans.modules.uml.ui.support.visitors.ETGraphObjectTraversal;
import org.netbeans.modules.uml.ui.support.visitors.ETXMIIDEqualsVisitor;
import org.netbeans.modules.uml.ui.support.visitors.IETGraphObjectVisitor;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.SaveAsGraphicKind;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADPasteState;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.core.support.Debug;

/**
 * @author sumitabhk
 *
 */
public class GetHelper
{

   /**
    * 
    */
   public GetHelper()
   {
      super();
   }

   public static boolean getShowGrid(TSEGraphWindow curEditor)
   {
      return curEditor != null ? curEditor.hasGrid() : false;
   }

   public static boolean saveAsGraphic(TSEGraphWindow curEditor, String sFilename, int nKind)
   {
      ETEGraphImageEncoder imageGenerator = new ETEGraphImageEncoder(curEditor, sFilename, nKind);
      return imageGenerator.save(1);
   }

   public static IGraphicExportDetails saveAsGraphic2(TSEGraphWindow curEditor, String sFilename, int nKind)
   {
	   return saveAsGraphic2(curEditor, sFilename, nKind, 1);
   }

   
   public static IGraphicExportDetails saveAsGraphic2(TSEGraphWindow curEditor, String sFilename, int nKind, double scale)
   {
      ETEGraphImageEncoder imageGenerator = new ETEGraphImageEncoder(curEditor, sFilename, nKind);
      if (imageGenerator.save(scale))
      {
         TSConstRect graphBounds = (TSConstRect) curEditor.getGraph().getBounds();

         return generateExportMap(curEditor, graphBounds != null ? (int) graphBounds.getWidth() : 0, graphBounds != null ? (int) graphBounds.getHeight() : 0, curEditor.getZoomLevel(), imageGenerator.getEncoderTransform());
      }
      return null;
   }
   
   
//   
//   public static IGraphicExportDetails generateImage(TSEGraphWindow curEditor, String sFilename, int nKind, double scale)
//   {
//	  curEditor.setZoomLevel(scale, false);
//      ETEGraphImageEncoder imageGenerator = new ETEGraphImageEncoder(curEditor, sFilename, nKind);
//	  
//      if (imageGenerator.save())
//      {
//         TSConstRect graphBounds = (TSConstRect) curEditor.getGraph().getBounds();		 
//		 TSTransform transform = imageGenerator.getEncoderTransform();
//         return generateExportMap(curEditor, graphBounds != null ? (int) graphBounds.getWidth() : 0, graphBounds != null ? (int) graphBounds.getHeight() : 0, scale, transform);
//      }
//      return null;
//   }
//   
   
   /**
    * This method creates a buffered image of the graph the graph window holds. 
    * If the visibleAreaOnly is true, then the zoomType setting is ignored and 
    * whatever is drawn in the viewport is saved to the image. Otherwise, the 
    * zoomType setting is taken into account.
    * 
    * @return The image of the graph. 
    */
   public static BufferedImage exportAsImage(TSEGraphWindow curEditor, boolean visibleAreaOnly, int zoomType, boolean drawGrid, boolean selectedOnly)
   {
      TSEGraphImageEncoder imageGenerator = new TSEGraphImageEncoder(curEditor);

      return imageGenerator.getBufferedGraphImage(visibleAreaOnly, zoomType, drawGrid, selectedOnly);
   }

   public static void transferImageToClipboard(TSEGraphWindow wnd)
   {
      BufferedImage image = GetHelper.exportAsImage(wnd, false, 0, false, true);
      if (image != null)
      {
         ImageTransferable transferable = new ImageTransferable(image);
         Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
      }
   }

   /**
    Creates an export map for use with the HTML report
    *
    */
   public static IGraphicExportDetails generateExportMap(TSEGraphWindow pCurrentEditor, int widthInWorldCoordinates, int heightInWorldCoordinates, double scaleFactor, final TSTransform imageTransform)
   {
      if (pCurrentEditor == null)
         return null;

      IGraphicExportDetails tempDetails = new GraphicExportDetails();
      try
      {
         final double scale = scaleFactor;

         // Get the list of locations
         final ETList < IGraphicMapLocation > pLocations = tempDetails.getMapLocations();

         // Set the rectangle on the output map
         IETRect pBoundingRect = new ETRect();

         if (pBoundingRect != null)
         {
            pBoundingRect.setSides(0, heightInWorldCoordinates, widthInWorldCoordinates, 0);
            tempDetails.setGraphicBoundingRect(pBoundingRect);
			IETRect frameRect = new ETRect();
			frameRect.setSides(0, (int)pCurrentEditor.getGraph().getFrameBounds().getHeight(), (int)pCurrentEditor.getGraph().getFrameBounds().getWidth(), 0);
			tempDetails.setFrameBoundingRect(frameRect);
         }

         TSEGraph pCurrentGraph = pCurrentEditor.getGraph();
         // Get the size of the bounding rectangle
         if (pCurrentGraph != null)
         {
            // Calculate the scale factor for each object
            // determine the bounding rectangle for the diagram
            final IETRect pObjectsBoundingRect = new ETRectEx(pCurrentGraph.getUI().getBounds());
            final int boundingRectLeft;
            final int boundingRectRight;
            final int boundingRectTop;
            final int boundingRectBottom;
            final int boundingRectWidth;
            final int boundingRectHeight;

            // Get the details on the bounding rect
            if (pObjectsBoundingRect != null)
            {
               boundingRectLeft = pObjectsBoundingRect.getLeft();
               boundingRectRight = pObjectsBoundingRect.getRight();
               boundingRectTop = pObjectsBoundingRect.getTop();
               boundingRectBottom = pObjectsBoundingRect.getBottom();
               boundingRectWidth = pObjectsBoundingRect.getIntWidth();
               boundingRectHeight = pObjectsBoundingRect.getIntHeight();
            }
            else
            {
               boundingRectLeft = 0;
               boundingRectRight = 0;
               boundingRectTop = 0;
               boundingRectBottom = 0;
               boundingRectWidth = 0;
               boundingRectHeight = 0;
            }

            if (boundingRectWidth > 0 && boundingRectHeight > 0)
            {
               // Now go through the objects within the diagram
               GetHelper.visit(pCurrentEditor, new IETGraphObjectVisitor()
               {
                  public boolean visit(IETGraphObject cpObject)
                  {
                     IGraphicMapLocation pLocation;
                     if (cpObject.isNode())
                     {
                        pLocation = getNodeMapLocation((IETNode) cpObject, scale, boundingRectLeft, boundingRectBottom, imageTransform);
                     }
                     else if (cpObject.isEdge())
                     {
                        pLocation = getEdgeMapLocation((IETEdge) cpObject, scale, boundingRectLeft, boundingRectBottom, imageTransform);
                     }
                     else if (cpObject.isLabel())
                     {
                        pLocation = getLabelMapLocation((IETLabel) cpObject, scale, boundingRectLeft, boundingRectBottom, imageTransform);
                     }
                     else
                     {
                        pLocation = null;
                     }

                     if (pLocation != null)
                     {
                        pLocations.add(pLocation);
                     }
                     return true;
                  }
               });
            }
         }
      }
      catch (Exception e)
      {
        Log.stackTrace(e);
      }
      return tempDetails;
   }

   /**
    Creates an INodeMapLocation from an argument node
    *
    @param pNode [in] The node to generate a location map for
    @param scaleFactor [in] The current scale factor
    @param boundingRectLeft [in] The bounding rect left side
    @param boundingRectBottom [in] The bounding rect bottom side
    @param pLocation [out] The location of this node
    */
   public static boolean getObjectMapLocation(IETGraphObject node, double scaleFactor, int boundingRectLeft, int boundingRectBottom, IGraphicMapLocation pMapLocation, final TSTransform imageTransform)
   {
      boolean hr = false;
      try
      {
         // Get the coordinate of the node
         IElement pNodeElement = TypeConversions.getElement(node);

         if (pNodeElement != null)
         {
            String sXMIID = pNodeElement.getXMIID();

            String sElementType = pNodeElement.getElementType();

            INamedElement pNamedElement = pNodeElement instanceof INamedElement ? (INamedElement) pNodeElement : null;
            String sName = pNamedElement != null ? pNamedElement.getQualifiedName() : null;

            if (sXMIID != null && sXMIID.length() > 0)
            {               
            	// Convert the world rect into image device.
               IETRect nodeRect = new ETDeviceRect(imageTransform.boundsToDevice(node.getBounds()));

               if (pMapLocation != null)
               {
                  IETRect pRect = (IETRect) nodeRect.clone();

                  if (pMapLocation instanceof INodeMapLocation)
                      ((INodeMapLocation) pMapLocation).setLocation(pRect);
                  else if (pMapLocation instanceof ILabelMapLocation)
                      ((ILabelMapLocation) pMapLocation).setLocation(pRect);

                  hr = populate(pNodeElement, pMapLocation);
               }
            }
            else
            {
               Debug.out.println("Null XMIID");
            }
         }
      }
      catch (Exception e)
      {
        Log.stackTrace(e);
      }
      return hr;
   }

   /**
    Creates an INodeMapLocation from an argument node
    *
    @param pNode [in] The node to generate a location map for
    @param scaleFactor [in] The current scale factor
    @param boundingRectLeft [in] The bounding rect left side
    @param boundingRectBottom [in] The bounding rect bottom side
    @param pLocation [out] The location of this node
    */
   public static INodeMapLocation getNodeMapLocation(IETNode node, double scaleFactor, int boundingRectLeft, int boundingRectBottom, final TSTransform imageTransform)
   {
      if (node == null)
         return null;

      INodeMapLocation pMapLocation = new NodeMapLocation();
      if (getObjectMapLocation(node, scaleFactor, boundingRectLeft, boundingRectBottom, pMapLocation, imageTransform))
         return pMapLocation;
      else
         return null;
   }

   /**
    Creates an ILabelMapLocation from an argument label
    *
    @param pLabel [in] The label to generate a location map for
    @param scaleFactor [in] The current scale factor
    @param boundingRectLeft [in] The bounding rect left side
    @param boundingRectBottom [in] The bounding rect bottom side
    @param pLocation [out] The location of this label
    */
   public static ILabelMapLocation getLabelMapLocation(IETLabel label, double scaleFactor, int boundingRectLeft, int boundingRectBottom, final TSTransform imageTransform)
   {
      if (label == null)
         return null;

      ILabelMapLocation pLocation = new LabelMapLocation();

      if (getObjectMapLocation(label, scaleFactor, boundingRectLeft, boundingRectBottom, pLocation, imageTransform))
         return pLocation;
      else
         return null;
   }

   /**
    Creates an IEdgeMapLocation from an argument node
    *
    @param pEdge [in] The edge to generate a location map for
    @param scaleFactor [in] The current scale factor
    @param boundingRectLeft [in] The bounding rect left side
    @param boundingRectBottom [in] The bounding rect bottom side
    @param pLocation [out] The location of this edge
    */
   public static IEdgeMapLocation getEdgeMapLocation(IETEdge pEdge, double scaleFactor, int boundingRectLeft, int boundingRectBottom, final TSTransform imageTransform)
   {
      if (pEdge == null)
         return null;

      IEdgeMapLocation pMapLocation = null;
      try
      {
         // Get the coordinate of the node
         IElement pEdgeElement = TypeConversions.getElement(pEdge);

         if (pEdgeElement != null)
         {
            Iterator < TSPEdge > pathEdgeIter = ((TSEEdge) pEdge).pathIterator();
            ETList < IETPoint > pointList = new ETArrayList < IETPoint > ();
            IETPoint lastPoint = null;
            boolean bFirst = true;

            while (pathEdgeIter.hasNext())
            {
               TSPEdge pPathEdge = (TSPEdge) pathEdgeIter.next();
               TSConstPoint tsSourcePoint =  pPathEdge.getLocalSourcePoint();
               TSConstPoint tsTargetPoint =  pPathEdge.getLocalTargetPoint();
               IETPoint tempSourcePoint = new ETPoint(imageTransform.pointToDevice(tsSourcePoint));
					IETPoint tempTargetPoint = new ETPoint(imageTransform.pointToDevice(tsTargetPoint));

               // Add the point, but make sure we don't duplicate
               if (bFirst)
               {
                  lastPoint = tempSourcePoint;
                  pointList.add(tempSourcePoint);
                  bFirst = false;
               }

               if (lastPoint != tempSourcePoint)
               {
                  lastPoint = tempSourcePoint;
                  pointList.add(tempSourcePoint);
               }

               if (lastPoint != tempTargetPoint)
               {
                  lastPoint = tempTargetPoint;
                  pointList.add(tempTargetPoint);
               }
            }

            // Create an object to return
            pMapLocation = new EdgeMapLocation();
            if (pMapLocation != null)
            {
               populate(pEdgeElement, pMapLocation);

               // Go over the point vector and add to the map
               ETList < IETPoint > pPointList = new ETArrayList < IETPoint > ();
               if (pPointList != null)
               {
                  IteratorT < IETPoint > iterator = new IteratorT < IETPoint > (pointList);

                  while (iterator.hasNext())
                  {
                     IETPoint tempPoint = iterator.next();

                     if (tempPoint != null)
                     {
                        pPointList.add(tempPoint);
                     }
                  }
               }
               pMapLocation.setPoints(pPointList);
            }
         }
      }
      catch (Exception e)
      {
        Log.stackTrace(e);
      }
      return pMapLocation;
   }

   /**
    Populates the basic location information
    *
    @param pElement [in] The element that controls this edge or node
    @param pLocation [in] The location object that is to be populated
    */
   public static boolean populate(IElement pElement, IGraphicMapLocation pLocation)
   {
      if (pElement == null || pLocation == null)
         return false;

      boolean hr = true;
      try
      {
         String sXMIID = pElement.getXMIID();
         String sName = null;
         String sElementType = pElement.getElementType();

         // Get the information

         INamedElement pNamedElement = pElement instanceof INamedElement ? (INamedElement) pElement : null;
         if (pNamedElement != null)
         {
            sName = pNamedElement.getQualifiedName();
         }

         // Populate the information
         pLocation.setName(sName);
         pLocation.setElementXMIID(sXMIID);
         pLocation.setElementType(sElementType);
		 pLocation.setElement(pElement);
      }
      catch (Exception e)
      {
        Log.stackTrace(e);
      }
      return hr;
   }

   /**
    * Reverses the incoming list
    *
    * @param pCurrentEditor [in] The current graph editor
    * @param pList [in] The list to reverse.
    */
   public static boolean reverseList(TSEGraphWindow curEditor, List pList)
   {
      if (pList == null)
         return false;

      Stack stack = new Stack();
      Iterator iter = pList.iterator();
      while (iter.hasNext())
      {
         stack.push(iter.next());
      }

      pList.clear();

      while (stack.size() > 0)
      {
         pList.add(stack.pop());
      }
      return true;
   }

   /*
    * Returns the presentation element on the drawing area control with the specified xml id
    *
    * @param pCurrentEditor [in] The editor this GETHelper is reponsible for
    * @param pDiagram [in] The diagram that is our parent
    * @param sXMLID [in] The presentation element to search for on the diagram.
    * @param pPresentationElement [out,retval] The found presentation element.
    */
   public static IPresentationElement findPresentationElement(TSEGraphWindow curEditor, IDiagram pDia, String xmiid)
   {
      ETGraph graph = curEditor.getGraph() instanceof ETGraph ? (ETGraph) curEditor.getGraph() : null;
      ETGraphObjectTraversal traversal = new ETGraphObjectTraversal(graph);
      ETXMIIDEqualsVisitor visitor = new ETXMIIDEqualsVisitor(xmiid);
      traversal.addVisitor(visitor);
      return !traversal.traverse() ? visitor.getFoundPresentation() : null;
      /*   	
            IPresentationElement retObj = null;
            ETList < IPresentationElement > elems = getAllItems(curEditor, pDia);
            if (elems != null)
            {
               int count = elems.size();
               for (int i = 0; i < count; i++)
               {
                  IPresentationElement pEle = elems.get(i);
                  String eleXMIID = pEle.getXMIID();
                  if (eleXMIID != null && eleXMIID.equals(xmiid))
                  {
                     retObj = pEle;
                     break;
                  }
               }
            }
            return retObj;
      */
   }

   /**
    * Returns a list of all the items
    */
   public static ETList < IPresentationElement > getAllItems(TSEGraphWindow curEditor, IDiagram pDia)
   {
      return getAllItems2(curEditor, pDia, null);
   }

   /**
    * Returns a list of all the items that represent the IElement
    */
   public static ETList < IPresentationElement > getAllItems2(TSEGraphWindow curEditor, IDiagram pDia, IElement modelEle)
   {
      final ETList < IPresentationElement > retObj = new ETArrayList < IPresentationElement > ();
      if (modelEle != null && pDia != null)
      {
         IteratorT < IPresentationElement > iter = new IteratorT < IPresentationElement > (modelEle.getPresentationElements());
         while (iter.hasNext())
         {
            IPresentationElement pEle = iter.next();
            if (pEle instanceof IGraphPresentation && ((IGraphPresentation) pEle).getIsOnDiagram(pDia))
            {
               retObj.add(pEle);
            }
         }
      }
      else if (curEditor != null)
      {
         ETGraph graph = curEditor.getGraph() instanceof ETGraph ? (ETGraph) curEditor.getGraph() : null;
         if (modelEle != null && graph != null)
         {
            ETGraphObjectTraversal traversal = new ETGraphObjectTraversal(graph);
            traversal.addVisitor(new ETFirstSubjectSameVistor(retObj, modelEle));
            traversal.traverse();
         }
         else if (graph != null)
         {
            ETGraphObjectTraversal traversal = new ETGraphObjectTraversal(graph);
            traversal.addVisitor(new IETGraphObjectVisitor()
            {
               public boolean visit(IETGraphObject object)
               {
                  IPresentationElement presItem = object.getPresentationElement();
                  if (presItem != null)
                  {
                     // Add it to the list
                     retObj.add(presItem);
                  }
                  return true;
               }
            });
            traversal.traverse();
         }

      }
      return retObj;
   }

   /**
    * Returns all the graph objects
    * The stacking order of the output list is from bottom to top.
    */
   public static ETList < IETGraphObject > getAllGraphObjects(TSEGraphWindow curEditor)
   {
      ETGraph graph = curEditor != null && curEditor.getGraph() instanceof ETGraph ? (ETGraph) curEditor.getGraph() : null;
      return graph != null ? graph.getAllETGraphObjects() : null;
   }

   /**
     * Return all the objects on the diagram that are of the indicated type
     *
     * @param editor The editor this GETHelper is reponsible for
     * @param type The element type for the query
     * @return The presentation elements of this type
     */
   public static ETList < IPresentationElement > getAllByType(TSEGraphWindow editor, String type)
   {
      ETList < IPresentationElement > retVal = new ETArrayList < IPresentationElement > ();

      if (editor != null && type != null)
      {
         ETGraphObjectTraversal traversal = new ETGraphObjectTraversal((ETGraph) editor.getGraph());
         traversal.addVisitor(new ETElementTypesEqualVisitor(retVal, type));
         traversal.traverse();
      }

      return retVal;
   }

   public static ETList < IPresentationElement > getSelected(ETGraph etGraph)
   {
      if (etGraph != null)
      {
         ETList < IPresentationElement > selectdPes = new ETArrayList < IPresentationElement > ();
         ETList < TSGraphObject > selected = etGraph.getSelectedObjects(false, false);
         if (selected != null)
         {
            Iterator < TSGraphObject > iter = selected.iterator();
            while (iter.hasNext())
            {
               TSGraphObject tsObj = iter.next();
               IETGraphObject obj = tsObj instanceof IETGraphObject ? (IETGraphObject) tsObj : null;
               IPresentationElement pe = obj != null ? obj.getPresentationElement() : null;
               if (pe != null)
                  selectdPes.add(pe);
            }
         }
         return selectdPes.size() > 0 ? selectdPes : null;
      }
      return null;
   }

   /**
    * Returns a list of the selected items (nodes and edges).
    */
   public static ETList < IETGraphObject > getSelected2(ETGraph etGraph)
   {
      if (etGraph != null)
      {
         ETList < IETGraphObject > selected = new ETArrayList < IETGraphObject > ();
         List selectedNodes = etGraph.selectedNodes();
         if (selectedNodes != null)
            selected.addAll(selectedNodes);

         List selectedEdges = etGraph.selectedEdges();
         if (selectedEdges != null)
            selected.addAll(selectedEdges);

         return selected.size() > 0 ? selected : null;
      }
      return null;
   }

   /**
    * Returns a list of the selected items (nodes, edges and nodelabels and edgeLabels).
    */
   public static ETList < IETGraphObject > getSelected3(ETGraph etGraph)
   {
      if (etGraph != null)
      {
         // Get the selected nodes and edges.
         ETList < IETGraphObject > selected = getSelected2(etGraph);
         selected = selected != null ? selected : new ETArrayList < IETGraphObject > ();

         // Now tack on the labels.
         List selectedNodeLabels = etGraph.selectedNodeLabels();
         if (selectedNodeLabels != null)
            selected.addAll(selectedNodeLabels);

         List selectedEdgeLabels = etGraph.selectedEdgeLabels();
         if (selectedEdgeLabels != null)
            selected.addAll(selectedEdgeLabels);

         return selected.size() > 0 ? selected : null;
      }
      return null;
   }

   /**
    * Calculates the rectangle that encompasses all the specified graph objects' veiw bounds rectangles
    * This call is equivalent to the C++ TSGraph::getBoundingRectOfObjects
    * 
    * @param rvecTSGraphObjects [in] The vector of Tom Sawyer graph objects used in the calculation
    */
   public static TSRect calculateTSGraphObjectsRect(ETList < TSGraphObject > tsGraphObjects)
   {
      TSRect retVal = new TSRect(0, 0, 0, 0);

      for (Iterator < TSGraphObject > iter = tsGraphObjects.iterator(); iter.hasNext();)
      {
         TSGraphObject curObject = iter.next();

         TSConstRect bounds = null;
         if (curObject instanceof TSENode)
         {
            TSENode node = (TSENode) curObject;
            bounds = (TSConstRect) node.getBounds();
         }
         else if (curObject instanceof TSEEdge)
         {
            TSEEdge edge = (TSEEdge) curObject;
            bounds = (TSConstRect) edge.getBounds();
         }

         if (bounds != null)
         {
            retVal.merge(bounds);
         }
      }

      return retVal;
   }

   /**
   	* Does this node have children
   	*/
   public static boolean hasChildren(TSEGraphWindow curEditor, TSNode prodNode)
   {
      boolean hasChildren = false;
      if (curEditor != null && prodNode != null)
      {
         List newNodeList = new Vector();
         prodNode.findChildren(newNodeList, null, 1);

         if (!newNodeList.isEmpty())
         {
            hasChildren = true;
         }
      }
      return hasChildren;
   }

   /**
   	* Does this node have parents
   	*/
   public static boolean hasParents(TSEGraphWindow curEditor, TSNode prodNode)
   {
      boolean hasParents = false;
      if (curEditor != null && prodNode != null)
      {
         List newNodeList = new Vector();
         prodNode.findParents(newNodeList, null, 1);
         if (newNodeList != null)
         {
            if (!newNodeList.isEmpty())
            {
               hasParents = true;
            }
         }
      }
      return hasParents;
   }

    /**
     * Get all children upto specified level
     */
    public static void findChildren(TSENode prodNode, List children, long levels) {
        if (levels>=1 && prodNode != null) {
            if (children == null) {
                children = new Vector();
            }
            List myChildren = new Vector();
            prodNode.findChildren(myChildren, null, 1);
            if(myChildren != null) { 
                myChildren.removeAll(children);
                children.addAll(myChildren);
                if (levels>1) {
                    int ctr =0;
                    while (ctr<myChildren.size()) {
                        TSENode pNode = (TSENode)myChildren.get(ctr++);
                        findChildren(pNode,children,levels-1);
                    }
                }
            }
        }
    }

    /**
     * Get all parents upto specified level
     */
    public static void findParents(TSENode prodNode, List parents, long levels) {
        if (levels>=1 && prodNode != null) {
            if (parents == null) {
                parents = new Vector();
            }
            List myParents = new Vector();
            prodNode.findParents(myParents, null, 1);
            if (myParents != null) {
                myParents.removeAll(parents);
                parents.addAll(myParents);
                if (levels>1) {
                    int ctr =0;
                    while (ctr<myParents.size()) {
                        TSENode pNode = (TSENode)myParents.get(ctr++);
                        findParents(pNode,parents,levels-1);
                    }
                }
            }
        }
    }
    
    /**
     * Get all hidden children upto specified level
     */
    public static void findHiddenChildren(TSENode prodNode, List hiddenChildren, long levels, TSEHidingManager hidingManager) {
        if ((levels>=1) && prodNode != null) {
            if (hiddenChildren == null) {
                hiddenChildren = new Vector();
            }
            List myChildren = new Vector();
            hidingManager.findHiddenChildren(prodNode,1);
            List myHiddenChildren = hidingManager.getResultNodeList();
            if (myHiddenChildren != null)  {
                myHiddenChildren.removeAll(hiddenChildren);
                hiddenChildren.addAll(myHiddenChildren);
                myChildren = myHiddenChildren;
            }
            if (levels>1) {
                findChildren(prodNode,myChildren,1);
                if(myChildren != null && !myChildren.isEmpty()) {
                    int ctr =0;
                    while (ctr<myChildren.size()) {
                        TSENode pNode = (TSENode)myChildren.get(ctr++);
                        findHiddenChildren(pNode,hiddenChildren,levels-1,hidingManager);
                    }
                }
            }
        }
    }

    /**
     * Get all hidden parents upto specified level
     */
    public static void findHiddenParents(TSENode prodNode, List hiddenParents, long levels, TSEHidingManager hidingManager) {
        if ((levels>=1) && prodNode != null) {
            if (hiddenParents == null) {
                hiddenParents = new Vector();
            }
            List myParents = new Vector();
            hidingManager.findHiddenParents(prodNode,1);
            List myHiddenParents = hidingManager.getResultNodeList();
            if (myHiddenParents != null)  {
                myHiddenParents.removeAll(hiddenParents);
                hiddenParents.addAll(myHiddenParents);
                myParents = myHiddenParents;
            }
            if (levels>1) {
                findParents(prodNode, myParents, 1);
                if(myParents != null)  {
                    int ctr =0;
                    while (ctr<myParents.size()) {
                        TSENode pNode = (TSENode)myParents.get(ctr++);
                        findHiddenParents(pNode,hiddenParents,levels-1,hidingManager);
                    }
                }
            }
        }
    }
    
   /**
   	* Does this node have hidden children
   	*/
   public static boolean hasHiddenChildren(TSEGraphWindow curEditor, TSNode prodNode)
   {
      boolean hasChildren = false;
      if (curEditor != null && prodNode != null)
      {
         TSEHidingManager hidingManager = (TSEHidingManager) TSEHidingManager.getManager(curEditor.getGraphManager());
         hasChildren = hidingManager.hasHiddenChildren((TSDNode) prodNode);
      }
      return hasChildren;
   }

   /**
   	* Does this node have hidden parents
   	*/
   public static boolean hasHiddenParents(TSEGraphWindow curEditor, TSNode prodNode)
   {
      boolean hasChildren = false;
      if (curEditor != null && prodNode != null)
      {
         TSEHidingManager hidingManager = (TSEHidingManager) TSEHidingManager.getManager(curEditor.getGraphManager());
         hasChildren = hidingManager.hasHiddenParents((TSDNode) prodNode);
      }
      return hasChildren;
   }

   /**
   	* Does this node have folded items
   	*/
   public static boolean hasFoldedItems(TSEGraphWindow curEditor, TSNode prodNode)
   {
      boolean hasChildren = false;
      if (curEditor != null && prodNode != null)
      {
         hasChildren = prodNode.isExpanded();
      }
      return hasChildren;
   }

   public static void fold(TSEGraphWindow curEditor, TSNode pNode, int depth, boolean hideChildren)
   {
      //to do implement
   }

   /**
    * Edge navigation
    */
   public static void gotoNodeEnd(TSEGraphWindow curEditor, TSEdge pNode, boolean bSourceNode)
   {
      TSNode pFoundNode = null;
      if (bSourceNode)
      {
         pFoundNode = pNode.getSourceNode();
      }
      else
      {
         pFoundNode = pNode.getTargetNode();
      }
      if (pFoundNode != null)
      {
         IETGraphObject tsObject = TypeConversions.getETGraphObject(pFoundNode);
         if (tsObject != null)
         {
            curEditor.deselectAll(false);
            curEditor.selectObject(tsObject.getObject(), true);
         }
         ETNode node = (ETNode) pFoundNode;
         TSConstPoint goToPoint = (TSConstPoint) node.getCenter();
         if (goToPoint != null)
         {
            curEditor.centerPointInWindow(goToPoint, true);
         }
      }
   }

   /**
   	* Removes this presentation element from its associated IElement.
   	*
   	* @param pPresentationElement [in] The presentation element to whack.
   	*/
   public static void removePresentationElement(IPresentationElement pEle)
   {
      // Go over all the model elements associated with this presentation element and remove them
      if (pEle != null)
      {
         IteratorT < IElement > iter = new IteratorT < IElement > (pEle.getSubjects());
         while (iter.hasNext())
         {
            iter.next().removePresentationElement(pEle);
         }
      }
   }

   /**
    Returns the mid point of the link
    *
    @param pEdge [in] The edge to return the mid point for
    @param pPoint [out,retval] The centerpoint for the edge (mode of target point and source point)
    */
   public static IETPoint getMidPoint(IETEdge pEdge)
   {
      if (pEdge == null)
         return null;

      try
      {
         // Get the from and to points
         TSConstPoint sourcePoint = (TSConstPoint) ((TSEEdge) pEdge).getSourcePoint();
         TSConstPoint targetPoint = (TSConstPoint) ((TSEEdge) pEdge).getTargetPoint();

         return new ETPoint(
            (int) (Math.min(sourcePoint.getX(), targetPoint.getX()) + Math.abs(sourcePoint.getX() - targetPoint.getX()) / 2),
            (int) (Math.min(sourcePoint.getY(), targetPoint.getY()) + Math.abs(sourcePoint.getY() - targetPoint.getY()) / 2));
      }
      catch (Exception e)
      {
        Log.stackTrace(e);
      }
      return null;
   }

   public static boolean hasGraphObjects(TSEGraphWindow wnd)
   {
      boolean retVal = false;

      TSDGraph graph = getCurrentGraph(wnd);
      if (graph != null)
      {
         retVal = true;
         List nodes = graph.nodes();

         // You can't have edges without nodes, or connectors.
         // This looks fishy. (Kevin)
         if ((nodes == null) || (nodes.size() <= 0))
         {
            List edges = graph.edges();
            if ((edges == null) || (edges.size() <= 0))
            {
               if (graph.numberOfLabels() <= 0)
               {
                  retVal = false;
               }
            }
         }
      }

      return retVal;
   }

   /**
    * @param wnd
    * @return
    */
   public static TSDGraph getCurrentGraph(TSEGraphWindow wnd)
   {
      TSDGraph retVal = null;

      if (wnd != null)
      {
         TSEGraphManager manager = wnd.getGraphManager();
         if (manager != null)
         {
            retVal = manager.getMainDisplayGraph();
         }
      }

      return retVal;
   }

   public static ETList < TSDNode > getAllNodes(TSEGraphWindow currentWindow)
   {
      if (currentWindow == null)
         return null;

      ETList < TSDNode > listNodes = null;

      TSDGraph currentGraph = getCurrentGraph(currentWindow);
      if (currentGraph != null)
      {
         listNodes = new ETArrayList < TSDNode > ();

         IteratorT < IETGraphObject > itrGO = new IteratorT < IETGraphObject > (getAllGraphObjects(currentWindow));

         while (itrGO.hasNext())
         {
            IETGraphObject tsObject = itrGO.next();

            if (tsObject instanceof TSDNode)
               listNodes.add((TSDNode) tsObject);
         }
      }

      return listNodes != null && listNodes.size() > 0 ? listNodes : null;
   }

   public static ETList < TSDNode > getAllNodesByMatch(TSEGraphWindow currentWindow, String elementType, Comparator compare)
   {
      ETList < TSDNode > listNodes = null;

      ETList < TSDNode > listAllNodes = getAllNodes(currentWindow);

      if (listAllNodes != null)
      {
         TSDGraph currentGraph = getCurrentGraph(currentWindow);

         if (currentGraph != null)
         {
            listNodes = new ETArrayList < TSDNode > ();
            IteratorT < TSDNode > itrNode = new IteratorT < TSDNode > (listAllNodes);

            while (itrNode.hasNext())
            {
               TSDNode node = itrNode.next();

               if (compare.compare(node, elementType) == 0)
                  listNodes.add(node);
            }
         }
      }

      return listNodes != null && listNodes.size() > 0 ? listNodes : null;
   }

   private static class MatchElementType implements Comparator
   {
      public int compare(Object left, Object right)
      {
         String elementType = (String) right;
         IElement element = TypeConversions.getElement((TSGraphObject) left);

         // Return 0 when the compared elements are equal, see Comparator.compare()
         return element != null && element.getElementType().equals(elementType) ? 0 : 1;
      }
   }

   public static ETList < TSDNode > getAllNodesByElementType(TSEGraphWindow currentWindow, String elementType)
   {
      return getAllNodesByMatch(currentWindow, elementType, new MatchElementType());
   }

   public static ETList < TSDNode > sortObjectsLeftToRight(TSEGraphWindow currentWindow, ETList < TSDNode > listOriginalNodes)
   {
      ETList < TSDNode > listNodes = null;

      if (listOriginalNodes != null)
      {
         ETList < TSDNode > list = new ETArrayList < TSDNode > ();

         TSDNode nodeOnLeft = null;

         double minX = Double.MAX_VALUE;
         int count = listOriginalNodes.getCount();

         while (count > 0)
         {
            Iterator < TSDNode > itrNode = listOriginalNodes.iterator();

            while (itrNode.hasNext())
            {
               TSDNode node = itrNode.next();

               if (node != null)
               {
                  if (nodeOnLeft == null)
                  {
                     nodeOnLeft = node;
                     minX = nodeOnLeft.getCenter().getX();
                  }
                  else
                  {
                     double x = node.getCenter().getX();
                     if (x < minX)
                     {
                        minX = x;
                        nodeOnLeft = node;
                     }
                  }
               }
            }

            list.add(nodeOnLeft);
            listOriginalNodes.remove(nodeOnLeft);
            nodeOnLeft = null;

            count = listOriginalNodes.getCount();
         }

         listNodes = list;
      }

      return listNodes;
   }

   public static ETList < IPresentationElement > sortNodesLeftToRight(ETList < IPresentationElement > unsortedList)
   {
      ETList < IPresentationElement > sortedList = new ETArrayList < IPresentationElement > ();

      if ((unsortedList != null) && (unsortedList.getCount() > 0))
      {
         TreeMap < Integer, IPresentationElement > mapPEs = new TreeMap < Integer, IPresentationElement > ();

         for (Iterator iter = unsortedList.iterator(); iter.hasNext();)
         {
            IPresentationElement pe = (IPresentationElement) iter.next();
            if (pe instanceof INodePresentation)
            {
               INodePresentation nodePE = (INodePresentation) pe;

               mapPEs.put(new Integer(nodePE.getCenter().getX()), pe);
            }
         }

         for (Iterator iterator = mapPEs.values().iterator(); iterator.hasNext();)
         {
            IPresentationElement pe = (IPresentationElement) iterator.next();
            sortedList.add(pe);
         }
      }

      return sortedList;
   }

   public static ETList < TSEEdge > getAllEdges(TSEGraphWindow graphWindow)
   {
      ETList < TSEEdge > listEdges = null;

      TSDGraph currentGraph = getCurrentGraph(graphWindow);
      if (currentGraph != null)
      {
         listEdges = new ETArrayList < TSEEdge > ();

         IteratorT < TSEEdge > iter = new IteratorT < TSEEdge > (currentGraph.edges());
         while (iter.hasNext())
         {
            listEdges.add(iter.next());
         }
      }

      return listEdges != null && listEdges.size() > 0 ? listEdges : null;
   }

   public static ETList < TSEEdge > getAllEdgesByMatch(TSEGraphWindow graphWindow, String elementType, Comparator compare)
   {
      ETList < TSEEdge > listEdges = null;
      ETList < TSEEdge > listAllEdges = getAllEdges(graphWindow);

      if (listAllEdges != null)
      {
         TSDGraph currentGraph = getCurrentGraph(graphWindow);

         if (currentGraph != null)
         {
            listEdges = new ETArrayList < TSEEdge > ();

            Iterator < TSEEdge > itrEdge = listAllEdges.iterator();

            while (itrEdge.hasNext())
            {
               TSEEdge edge = itrEdge.next();

               if (compare.compare(edge, elementType) == 0)
                  listEdges.add(edge);
            }
         }
      }
      return listEdges != null && listEdges.size() > 0 ? listEdges : null;
   }

   public static ETList < TSEEdge > getAllEdgesByElementType(TSEGraphWindow graphWindow, String elementType)
   {
      return getAllEdgesByMatch(graphWindow, elementType, new MatchElementType());
   }

   public static boolean handleAccelerator(IDrawingAreaControl pCurrentEditor, String accelerator, boolean bSingleNodeOnly)
   {
      boolean bRetVal = false;

      ETList < IPresentationElement > pPresentationElements = pCurrentEditor.getSelected();
      if (pPresentationElements != null)
      {
         // fetch selected node count
         int count = pPresentationElements.size();

         // if we're to forward this accelerator to just a single node, return if none or 2+ nodes are selected
         if (bSingleNodeOnly && count != 1)
         {
            return bRetVal;
         }

         // either we have 1 node or OK for multiple nodes
         for (int i = 0; i < count; i++)
         {
            IPresentationElement pPresentationElement = pPresentationElements.get(i);

            if (pPresentationElement != null)
            {
               IETGraphObject pETGraphObject = TypeConversions.getETGraphObject(pPresentationElement);

               if (pETGraphObject != null)
               {
                  bRetVal = pETGraphObject.handleAccelerator(accelerator);
               }
            }
         }
      }
      return bRetVal;
   }

   /**
    * Cut
    */
   public static void cut(ADGraphWindow pCurrentEditor)
   {
      transferImageToClipboard(pCurrentEditor);

      TSEGraphWindow pGraphEditor = pCurrentEditor;
      TSEGraph pGraph = pCurrentEditor.getGraph();

      if (pGraph != null && pGraphEditor != null)
      {
         //         TSCutCopyPasteControl pPasteControl = pGraphEditor.getCutCopyPasteControl();

         //         if (pPasteControl != null)
         //         {
         //            CComPtr < TSCOM : : TSCommand > pCommand;
         //
         //            _VH(CreateCommand(pCurrentEditor, xstring(_T("TSCutCmd")), & pCommand));
         //            
         //            if (pCommand)
         //            {
         //               CComQIPtr < TSCOM : : TSCutCmd > pCutCmd(pCommand);
         //               if (pCutCmd)
         //               {
         // These objects are about to be transfered to the clipboard
         // Remove the PE from the model

         //         IDrawingAreaControl drawingArea = pCurrentEditor.getDrawingArea();
         //
         //         if (drawingArea != null)
         //         {
         //            ETList < IPresentationElement > pPresentationElements = drawingArea.getSelected();
         //
         //            Iterator < IPresentationElement > iter = pPresentationElements.iterator();
         //            while (iter.hasNext())
         //            {
         //               IPresentationElement pPresentationElement = iter.next();
         //               removePresentationElement(pPresentationElement);
         //            }
         //         }

         try
         {
            pGraphEditor.cut();
         }
         catch (Exception e)
         {
           Log.stackTrace(e);
         }

         //            long count = 0;
         //            count = pPresentationElements.size();
         //
         //            for (long i = 0; i < count; i++)
         //            {
         //               CComPtr < IPresentationElement > pPresentationElement;
         //
         //               _VH(pPresentationElements - > Item(i, & pPresentationElement));
         //               if (pPresentationElement)
         //               {
         //                  _VH(RemovePresentationElement(pPresentationElement));
         //               }
         //            }

         //
         //                  _VH(pCutCmd - > init(pGraph, pPasteControl, 0, 0, 0, 0, 0));
         //                  _VH(ExecuteCommand(pCurrentEditor, pCutCmd));
         //               }
         //            }
         //         }
      }

   }

   /**
    * Copy
    *
    * @param pCurrentEditor [in] The current editor
    */
   public static void copy(ADGraphWindow pCurrentEditor)
   {
      // First copy to the clipboard
      //boolean bSuccess = false;
      //_VH(pCurrentEditor->copyToClipboard(0,0,0,true, true, false, &bSuccess));

      transferImageToClipboard(pCurrentEditor);

      // Now copy to the TS clipboard manager
      TSEGraphWindow pGraphEditor = pCurrentEditor;
      TSEGraph pGraph = pCurrentEditor.getGraph();

      if (pGraph != null && pGraphEditor != null)
      {
         TSCutCopyPasteControl pPasteControl = pGraphEditor.getCutCopyPasteControl();

         if (pPasteControl != null)
         {
            pPasteControl.reset();

            try
            {
               pGraphEditor.copy();
            }
            catch (Exception e)
            {
              Log.stackTrace(e);
            }
         }
      }
   }

   /**
    * Paste
    */
   public static boolean paste(ADGraphWindow pCurrentEditor)
   {
      boolean bDidPaste = false;

      TSEGraphManager pGraphManager = pCurrentEditor.getGraphManager();
      TSEGraphWindow pGraphEditor = pCurrentEditor;
      TSEGraph pGraph = pCurrentEditor.getGraph();

      if (pGraphManager != null && pGraphEditor != null && pGraph != null)
      {
         TSCutCopyPasteControl pPasteControl = pGraphEditor.getCutCopyPasteControl();

         if (pPasteControl != null)
         {
				boolean bCanPaste  = pPasteControl.canPaste();

            if (bCanPaste)
            {
               //if (!(pCurrentEditor.getCurrentState() instanceof TSEPasteState))
			   if (!(pCurrentEditor.getCurrentState() instanceof TSEPasteTool))
               {
//                  pCurrentEditor.switchState(new ADPasteState());
                  pCurrentEditor.switchTool(new ADPasteState());
               }
               bDidPaste = true;
            }
         }
      }

      return bDidPaste;
   }

   /**
    * ClearClipboard
    */
   public static void clearClipboard(ADGraphWindow pCurrentEditor)
   {
      TSEGraphManager pGraphManager = pCurrentEditor.getGraphManager();
      TSEGraphWindow pGraphEditor = pCurrentEditor;
      TSEGraph pGraph = pCurrentEditor.getGraph();

      if (pGraphManager != null && pGraphEditor != null && pGraph != null)
      {
         TSCutCopyPasteControl pPasteControl = pGraphEditor.getCutCopyPasteControl();

         if (pPasteControl != null)
         {
            pPasteControl.reset();

            // This added code is similar to what that C++ code is doing.
            // We are not sure that the code below is necessary, but its working as is.
                        
            //pPasteControl.copy(pGraphManager, new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList());
         }
      }
   }

   /**
    * ItemsOnClipboard
    */
   public static boolean itemsOnClipboard(ADGraphWindow pCurrentEditor)
   {
      return pCurrentEditor.canPaste();
   }

   /**
    * Hide 'num' number of children levels for the input node.  If -1 then we ask the user
    *
    * @param pCurrentEditor [in] The current graph editor
    * @param pPE [in] The presentation element whose children (or parents) we'll be hiding.
    * @param numLevels [in] The number of levels we should hide.
    * @param bChildren [in] Set to VARIANT_TRUE to hide children.
    */
   public static void hide(TSEGraphWindow pCurrentEditor, IPresentationElement pPE, long numLevels, boolean bChildren)
   {
      TSENode pTSNode = TypeConversions.getOwnerNode(pPE);

      if (pTSNode != null)
      {
         if (numLevels == -1)
         {
            // TODO
            //					CComPtr < IGraphObjectsHidingDialog > pGraphObjectsHidingDialog;
            //
            //					_VH(pGraphObjectsHidingDialog.CoCreateInstance(__uuidof(GraphObjectsHidingDialog)));
            //					ATLASSERT(pGraphObjectsHidingDialog);
            //					if (pGraphObjectsHidingDialog)
            //					{
            //						long nResult  = 1L;
            //						long nDefault = 1L;
            //						VARIANT_BOOL bUserHitOK = VARIANT_FALSE;
            //
            //						if (bChildren)
            //						{
            //							_VH(pGraphObjectsHidingDialog->DisplayHideChildrenDialog(nDefault,
            //																										&nResult,
            //																										&bUserHitOK));
            //						}
            //						else
            //						{
            //							_VH(pGraphObjectsHidingDialog->DisplayHideParentsDialog(nDefault,
            //																									  &nResult,
            //																										&bUserHitOK));
            //						}
            //
            //						if (bUserHitOK)
            //						{
            //							_VH(Hide(pCurrentEditor, pPE, nResult, bChildren));
            //						}
            //					}

            //TODO remove once  the above is implemented
            hide(pCurrentEditor, pPE, 999, bChildren);
         }
         else
         {

            List pDList = new Vector();

            if (bChildren)
            {
               findChildren(pTSNode, pDList, numLevels);
            }
            else
            {
               findParents(pTSNode, pDList, numLevels);
            }

            if (!pDList.isEmpty())
            {
               // make sure this node is not in the list
               pDList.remove(pTSNode);
               TSEHidingManager hidingManager = (TSEHidingManager) TSEHidingManager.getManager(pCurrentEditor.getGraphManager());
               pCurrentEditor.transmit(new TSEHideCommand(pDList, null));

            }
         }
      }
   }

   /**
    * Hides the parents of the specified node 'numLevels' number of levels
    *
    * @param pCurrentEditor [in] The current graph editor
    * @param pPE [in] The presentation element whose children (or parents) we'll be showing.
    * @param numLevels [in] The number of levels we should showing.
    * @param bChildren [in] Set to VARIANT_TRUE to show children.
    */
   public static void unhide(TSEGraphWindow pCurrentEditor, IPresentationElement pPE, long numLevels, boolean bChildren)
   {
      TSENode pTSNode = TypeConversions.getOwnerNode(pPE);

      if (pTSNode != null)
      {
         if (numLevels == -1)
         {
            // TODO
            //					CComPtr < IGraphObjectsHidingDialog > pGraphObjectsHidingDialog;
            //
            //					_VH(pGraphObjectsHidingDialog.CoCreateInstance(__uuidof(GraphObjectsHidingDialog)));
            //					ATLASSERT(pGraphObjectsHidingDialog);
            //					if (pGraphObjectsHidingDialog)
            //					{
            //						long nResult  = 1L;
            //						long nDefault = 1L;
            //						VARIANT_BOOL bUserHitOK = VARIANT_FALSE;
            //
            //						if (bChildren)
            //						{
            //							_VH(pGraphObjectsHidingDialog->DisplayUnhideChildrenDialog(nDefault,
            //																										  &nResult,
            //																										  &bUserHitOK));
            //						}
            //						else
            //						{
            //							_VH(pGraphObjectsHidingDialog->DisplayUnhideParentsDialog(nDefault,
            //																										 &nResult,
            //																										 &bUserHitOK));
            //						}
            //						if (bUserHitOK)
            //						{
            //							_VH(Unhide(pCurrentEditor, pPE, nResult, bChildren));
            //						}
            //					}

            //TODO remove once  the above is implemented
            unhide(pCurrentEditor, pPE, 999, bChildren);

         }
         else
         {

            List pDList = new Vector();

            TSEHidingManager hidingManager = (TSEHidingManager) TSEHidingManager.getManager(pCurrentEditor.getGraphManager());

            if (bChildren)
            {
               findHiddenChildren(pTSNode, pDList, numLevels, hidingManager);
            }
            else
            {
                findHiddenParents(pTSNode, pDList, numLevels, hidingManager);
            }

            if (!pDList.isEmpty())
            {
               // make sure this node is not in the list
               pDList.remove(pTSNode);
               pCurrentEditor.transmit(new TSEUnhideCommand(pDList, null));
            }
         }
      }
   }

   /** 
    * Reconnects the link.
    *
    * @param pLink [in] The link to reconnect
    * @param pOldNode [in] The old source or target node.
    * @param pNewNode [in] The new source or target node.
    * @Returns true if the reconnection was successful.
    */
   public static boolean reconnectLink(IPresentationElement pLink, IPresentationElement pOldNode, IPresentationElement pNewNode)
   {
      TSEEdge pTSEEdge = TypeConversions.getOwnerEdge(pLink, false);
      TSENode pTSEFromNode = TypeConversions.getOwnerNode(pOldNode);
      TSENode pTSEToNode = TypeConversions.getOwnerNode(pNewNode);

      if (pTSEEdge != null && pTSEFromNode != null && pTSEToNode != null)
      {
         pTSEEdge.setSourceNode(pTSEFromNode);
         pTSEEdge.setTargetNode(pTSEToNode);
         return true;
      }
      return false;
   }

   /**
    * Adds the TSCOM::TSGraphObjects in pList to the items list.
    *
    * @param pList [in] The list to which to extract presentation elements from
    * @param items [in,out] All presentation elements in pList are added to this list.
    */
   public static ETList < IPresentationElement > addToPresentationElements(ETList < IETGraphObject > objects, ETList < IPresentationElement > items)
   {

      ETArrayList < IPresentationElement > retVal = new ETArrayList < IPresentationElement > ();

      retVal.addAll(items);

      if (objects != null)
      {
         for (Iterator < IETGraphObject > iter = objects.iterator(); iter.hasNext();)
         {
            IETGraphObject curObject = iter.next();
            retVal.add(curObject.getPresentationElement());
         }
      }

      return retVal;
   }

   /**
    * Creates IPresentationElements list from the TS lists
    */
   public static ETList < IPresentationElement > createPEList(ETList < IETGraphObject > objects)
   {

      ETArrayList < IPresentationElement > retVal = new ETArrayList < IPresentationElement > ();

      for (Iterator < IETGraphObject > iter = objects.iterator(); iter.hasNext();)
      {
         IETGraphObject curObject = iter.next();
         retVal.add(curObject.getPresentationElement());
      }

      return retVal;
   }

   /*
    * Visits all objects on the graph in stacking order from top to bottom
    * return true of all objects were visited.
    */
   private static boolean visit(TSEGraphWindow curEditor, IETGraphObjectVisitor visitor)
   {
      ETGraph graph = curEditor != null && curEditor.getGraph() instanceof ETGraph ? (ETGraph) curEditor.getGraph() : null;
      if (graph != null && visitor != null)
      {
         ETGraphObjectTraversal traversal = new ETGraphObjectTraversal(graph);
         traversal.addVisitor(visitor);
         return traversal.traverseInReverseOrder();
      }
      return false;
   }
}
