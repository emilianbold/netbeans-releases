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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.Debug;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.ui.support.EdgeKindEnum;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.drawing.TSDEdge;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.graph.TSNode;

/**
 * This class contains various interfaces helpful in navigating and manipulating
 * the presentation layer.
 * 
 * @author josephg
 *
 */
public class PresentationHelper
{

   public static class LollypopsAndEdges
   {
      private ETList < IETGraphObject > m_lollypops = new ETArrayList < IETGraphObject > ();
      private ETList < IETGraphObject > m_edges = new ETArrayList < IETGraphObject > ();

      public void addLollypop(IETGraphObject lollypop)
      {
         if (lollypop != null)
         {
            m_lollypops.add(lollypop);
         }
      }
      public void addEdge(IETGraphObject edge)
      {
         if (edge != null)
         {
            m_edges.add(edge);
         }
      }
      public ETList < IETGraphObject > getLollypops()
      {
         return m_lollypops;
      }
      public ETList < IETGraphObject > getEdges()
      {
         return m_edges;
      }
   }

   public static LollypopsAndEdges getLollypopsWithOneControllingEdge(IETGraphObject etGraphObject)
   {
      LollypopsAndEdges returnValue = new LollypopsAndEdges();

      INodePresentation nodePresentation = TypeConversions.getNodePresentation(etGraphObject);

      ETList < IETGraphObject > edges = nodePresentation.getEdgesByType(EdgeKindEnum.EK_IMPLEMENTATION, true, true);
      ETList < IETGraphObject > usageEdges = nodePresentation.getEdgesByType(EdgeKindEnum.EK_USAGE, true, true);
      ETList < IETGraphObject > interfaceEdges = nodePresentation.getEdgesByType(EdgeKindEnum.EK_INTERFACE, true, true);

      if (usageEdges != null)
         edges.addAll(usageEdges);
      if (interfaceEdges != null)
         edges.addAll(interfaceEdges);

      try
      {
         IteratorT < IETGraphObject > iterator = new IteratorT < IETGraphObject > (edges);
         while (iterator.hasNext())
         {
            IETGraphObject edge = iterator.next();

            if (doesLollypopHaveOneControllingEdge(etGraphObject, edge))
            {
               TSEEdge controllingEdge = TypeConversions.getOwnerEdge(edge);
               TSENode thisNode = TypeConversions.getOwnerNode(etGraphObject);

               TSENode interfaceNode = null;
               if (thisNode != null && controllingEdge != null)
               {
                  interfaceNode = getOtherNode(controllingEdge, thisNode);
               }

               IETGraphObject lollypop = TypeConversions.getETGraphObject(interfaceNode);

               if (lollypop != null)
               {
                  returnValue.addLollypop(lollypop);
                  returnValue.addEdge(edge);
               }
            }
         }
      }
      catch (InvalidArguments e)
      {
         e.printStackTrace();
      }

      return returnValue;
   }

   public static boolean isOwnerNodeInterfaceDrawnAsLollypopWithOneControllingEdge(IETGraphObject graphObject)
   {
      IDrawEngine nodeEngine = TypeConversions.getDrawEngine(graphObject);

      if (nodeEngine == null || !nodeEngine.getDrawEngineID().equals("InterfaceDrawEngine"))
      {
         return false;
      }

      INodePresentation nodePresentation = TypeConversions.getNodePresentation(graphObject);

      if (nodePresentation == null)
      {
         return false;
      }

      ETList < IETGraphObject > allControllingEdges = getControllingEdges(nodePresentation);

      if (allControllingEdges == null)
      {
         return false;
      }

      if (allControllingEdges.size() != 1)
      {
         return false;
      }

      return true;
   }

   public static IEdgePresentation getLollipopControllingEdge(IETGraphObject graphObject)
   {
      INodePresentation nodePresentation = TypeConversions.getNodePresentation(graphObject);

      if (nodePresentation == null)
      {
         return null;
      }

      ETList < IETGraphObject > allControllingEdges = getControllingEdges(nodePresentation);

      if (allControllingEdges.size() != 1)
      {
         return null;
      }

      IETGraphObject edge = allControllingEdges.get(0);

      if (!(edge instanceof IETEdge))
      {
         return null;
      }

      return TypeConversions.getEdgePresentation((IETEdge)edge);
   }

   private static ETList < IETGraphObject > getControllingEdges(INodePresentation nodePresentation)
   {
      ETList < IETGraphObject > allControllingEdges = nodePresentation.getEdgesByType(EdgeKindEnum.EK_USAGE, true, true);

      ETList < IETGraphObject > someControllingEdges = nodePresentation.getEdgesByType(EdgeKindEnum.EK_IMPLEMENTATION, true, true);

      if (someControllingEdges != null)
      {
         if (allControllingEdges == null)
         {
            allControllingEdges = someControllingEdges;
         }
         else
         {
            allControllingEdges.addAll(someControllingEdges);
         }
      }

      someControllingEdges = nodePresentation.getEdgesByType(EdgeKindEnum.EK_INTERFACE, true, true);

      if (someControllingEdges != null)
      {
         if (allControllingEdges == null)
         {
            allControllingEdges = someControllingEdges;
         }
         else
         {
            allControllingEdges.addAll(someControllingEdges);
         }
      }

      return allControllingEdges;
   }

   public static boolean doesLollypopHaveOneControllingEdge(IETGraphObject classETGraphObject, IETGraphObject controllingEdge)
   {
      TSENode classNode = TypeConversions.getOwnerNode(classETGraphObject);
      TSEEdge edge = TypeConversions.getOwnerEdge(controllingEdge);
      TSENode lollypopNode = getOtherNode(edge, classNode);

      if (classNode == null || edge == null || lollypopNode == null)
      {
         return false;
      }

      IETGraphObject interfaceGraphObject = TypeConversions.getETGraphObject(lollypopNode);

      if (interfaceGraphObject == null)
      {
         return false;
      }

      INodePresentation nodePresentation = TypeConversions.getNodePresentation(interfaceGraphObject);

      if (nodePresentation == null)
      {
         return false;
      }

      ETList < IETGraphObject > edges = getControllingEdges(nodePresentation);

      if (edges.size() != 1)
      {
         return false;
      }

      IDrawEngine drawEngine = interfaceGraphObject.getEngine();
      if (drawEngine == null)
      {
         return false;
      }

      if (!drawEngine.getDrawEngineID().equals("InterfaceDrawEngine"))
      {
         return false;
      }

      return true;
   }

   public static TSENode getOtherNode(TSEEdge edge, TSENode node)
   {
      if (edge != null && node != null)
      {
         if (edge.getSourceNode() == node)
         {
            return (TSENode)edge.getTargetNode();
         }
         else if (edge.getTargetNode() == node)
         {
            return (TSENode)edge.getSourceNode();
         }
      }

      return null;
   }

   /**
    * Finds the connector at the other end of an edge connected to the first 
    * connector
    * 
    * @param pConnector Starting connector
    * @param invalidateEdge Whether or not to invalidate the edge.
    * @return Connector found at the other end of the edge.
    */
   public static TSConnector getConnectorOnOtherEndOfEdge(TSConnector connector, boolean invalidateEdge)
   {
      TSConnector retVal = null;

      TSDEdge edge = getConnectedEdge(connector, invalidateEdge);
      if (edge != null)
      {
         TSConnector targetConnector = edge.getTargetConnector();
         //if (connector.getID() == targetConnector.getID())         
         if ( (connector != null && targetConnector != null) && (connector.getID() == targetConnector.getID()) )  //Jyothi: fix for bug#6253726
         {
            retVal = edge.getSourceConnector();
         }
         else
         {
            retVal = targetConnector;
         }
      }

      return retVal;
   }

   /**
    * Finds the edge connected to the input connector
    */
   public static TSDEdge getConnectedEdge(TSConnector connector, boolean invalidateEdge)
   {
      TSDEdge retVal = null;

      if (connector != null)
      {
         retVal = getConnectorEdge(connector);
         if ((retVal != null) && (invalidateEdge == true))
         {
            IGraphPresentation presentation = TypeConversions.getETElement(retVal);
            if(presentation != null)
            {
               presentation.invalidate();
            }
         }
      }

      return retVal;
   }

   /**
    * Retrieves an edge from the connector.  It is assumed that there will only
    * be one edge connected to the connector.
    * 
    * @param connector
    * @return
    */
   private static TSDEdge getConnectorEdge(TSConnector connector)
   {
      TSDEdge retVal = null;

      if (connector != null)
      {
         // (TS 4/1/05) Tom Sawyer's API has changed between 5.x and 6.0.  They have removed
         // the inEdges and the outEdges and added the incidentEdges methods.
         // The incidentEdges will return all of the edges connected to the connector.
         List inEdgeList = connector.incidentEdges();
         if (inEdgeList.size() > 0)
         {
            //assert inEdgeList.size() == 1 : "The following code assumes the count is 1";
            Debug.assertTrue(inEdgeList.size() == 1, "The following code assumes the count is 1");

            if (inEdgeList.get(0) instanceof TSDEdge)
            {
               retVal = (TSDEdge)inEdgeList.get(0);
               
               // (TS 4/1/05) The below code now returns the graph that ownes the edge.  
               // They use to have two constructs one for layout and the one everyone
               // used.  Now there is only one edge type.
//               TSGraphObject gObject = ((TSEdge)inEdgeList.get(0)).getOwner();
//               if (gObject instanceof TSDEdge)
//               {
//                  retVal = (TSDEdge)gObject;
//               }
            }
         }
      }

      return retVal;
   }

   public static boolean haveIntersections(ETList < IPresentationElement > PEs)
   {
      boolean foundIntersection = false;

      if (PEs == null)
         return foundIntersection;

      for (int outerIndex = 0; outerIndex < PEs.getCount(); ++outerIndex)
      {
         IPresentationElement outerElement = PEs.item(outerIndex);

         IProductGraphPresentation outerGraphPE = null;
         if (outerElement instanceof IProductGraphPresentation)
            outerGraphPE = (IProductGraphPresentation)outerElement;

         IETRect outerRect = null;
         if (outerGraphPE != null)
         {
            outerRect = outerGraphPE.getBoundingRect();
         }

         if (outerRect != null)
         {
            for (int innerIndex = outerIndex + 1; innerIndex < PEs.getCount() && !foundIntersection; ++innerIndex)
            {
               if (outerIndex != innerIndex)
               {
                  IPresentationElement innerElement = PEs.item(innerIndex);

                  IProductGraphPresentation innerGraphPE = null;
                  if (innerElement instanceof IProductGraphPresentation)
                     innerGraphPE = (IProductGraphPresentation)innerElement;

                  if (outerGraphPE != null && innerGraphPE != null)
                  {
                     IETRect innerRect = innerGraphPE.getBoundingRect();

                     if (outerRect != null && innerRect != null)
                     {
                        foundIntersection = outerRect.doesIntersect(innerRect);
                     }
                  }
               }
            }
         }
      }

      return foundIntersection;
   }
}


