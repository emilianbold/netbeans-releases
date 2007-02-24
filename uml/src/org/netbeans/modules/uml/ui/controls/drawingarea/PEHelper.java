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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETGraph;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation;
import org.netbeans.modules.uml.ui.support.umltsconversions.RectConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.drawing.TSDGraph;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.graph.TSGraphObject;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
import com.tomsawyer.util.TSObject;
//import com.tomsawyer.util.TSRect;
import com.tomsawyer.drawing.geometry.TSRect;

/**
 * @author brettb
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PEHelper
{
   TSEGraphWindow m_graphWindow;
   IDrawingAreaControl m_drawingArea;

   boolean m_bCalculateBoundingRect;
   IETRect m_rectBounding = new ETRect();

   HashSet< Long > m_setPEIDs = new HashSet< Long > ();

   ETList < IPresentationElement > m_presentationElements;

   /**
    * Constructor
    */
   public PEHelper(TSEGraphWindow graphEditor, IDrawingAreaControl control, boolean bCalculateBoundingRect)
   {
      m_graphWindow = graphEditor;
      m_bCalculateBoundingRect = bCalculateBoundingRect;
      m_drawingArea = control;
   }

   /**
    * Gets the TS object owned by the presentation element and adds its TS object 
    * IDs to the member vector of PE IDs
    *
    * @param presentationElement [in] The object this pe helper is to act upon
    */
   public void add( IPresentationElement presentationElement )
   {
      if (presentationElement != null )
      {
         if (m_bCalculateBoundingRect &&
            (presentationElement instanceof IProductGraphPresentation))
         {
            IProductGraphPresentation graphPresentation = (IProductGraphPresentation)presentationElement;
            if ( graphPresentation != null )
            {
               IETRect rectBounding = TypeConversions.getLogicalBoundingRect( graphPresentation );
               m_rectBounding.unionWith( rectBounding );
            }
         }

         // Get the id of the object
         TSGraphObject graphObject = (TSGraphObject)TypeConversions.getTSObject( presentationElement );
         add( graphObject );

         if ( null == m_presentationElements )
         {
            m_presentationElements = new ETArrayList< IPresentationElement >();
         }
         if ( m_presentationElements != null )
         {
            m_presentationElements.add( presentationElement );
         }
      }
   }

   /**
    * Gets the TS objects owned by the presentation elements and adds their TS object's 
    * IDs to the member vector of PE IDs
    *
    * @param pPresentationElements [in] The objects this pe helper is to act upon
    */
   public void add( ETList< IPresentationElement > presentationElements )
   {
      if ( presentationElements != null )
      {
         for (Iterator iter = presentationElements.iterator(); iter.hasNext();)
         {
            IPresentationElement pe = (IPresentationElement)iter.next();
            
            add( pe );
         }
      }
   }

   /**
    * Add the selected presentation elements to the list of selected items by ID
    */
   public void addSelected()
   {
      if (null == m_graphWindow) throw new IllegalArgumentException();
      
      // In C++ we were using the TSGraph & TSObjectFactory.
      // Since the TSObjectFactory is not available in C++,
      // I (BDB) am just using the already created helper functions from GetHelper

      ETGraph graph = (ETGraph)GetHelper.getCurrentGraph( m_graphWindow );
      ETList< IPresentationElement > pesSelected = GetHelper.getSelected( graph );
      add( pesSelected ); 
   }
   
   /**
    * Returns the bounding rect for the objects this helper contains
    *
    * @param ppBoundingRect [out] The bounding rect made up of all our presentation elements.
    */
   public IETRect getBoundingRect()
   {
      return (IETRect)m_rectBounding.clone();
   }

   /**
    * Routine to perform the stacking commands
    *
    * @param nStackingCommand [in] A IDrawingAreaControl.StackingOrderKind which is the stacking command to perform
    * @param bRedraw [in] Should we redraw after doing the stacking?
    */
   void executeStackingCommand( int nStackingCommand, boolean bRedraw )
   {
      if (null == m_graphWindow)  throw new IllegalArgumentException();

      if ( nStackingCommand == IDrawingAreaControl.SOK_MOVETOFRONT )
      {
         TSDGraph graph = GetHelper.getCurrentGraph( m_graphWindow );
         long count = 0;

         if ( null == m_presentationElements )
         {
            m_presentationElements = m_drawingArea.getSelected();
         }

         if ( (m_presentationElements != null) &&
              (graph != null) )
         {
            count = m_presentationElements.getCount();
         }
         
         for (Iterator iter = m_presentationElements.iterator(); iter.hasNext();)
         {
            IPresentationElement pe = (IPresentationElement)iter.next();
            
            TSENode tsNode = TypeConversions.getOwnerNode( pe );

            if ( tsNode != null )
            {
               boolean bIsSelected = tsNode.isSelected();
               
               graph.remove( tsNode );
               graph.insert( tsNode );
               if ( bIsSelected )
               {
                  tsNode.setSelected(bIsSelected);
               }
            }
         }

         if (bRedraw)
         {
            m_drawingArea.refresh( false );
         }
         return;
      }

      TSDGraph graph = GetHelper.getCurrentGraph( m_graphWindow );
      TSRect rectInvalidate = new TSRect();

      if ( (graph != null) &&
           (m_graphWindow != null) )
      {
         // Get all the graph objects that overlap or partially overlap the given rectangle.
         // The stacking order of this list is from bottom to top.

         // This is the original TS code.  It had a bug when nodes were on layered :
         //
         //  |----------------------------------------------|
         //  |                               A              |
         //  |    |-------------------|                     |
         //  |    |             B     |                     |
         //  |    |                   |                     |
         //  |    |  |---------|      |                     |
         //  |    |  |   C     |      |   |------------|    |
         //  |    |  |         |      |   |     D      |    |
         //  |    |  |---------|      |   |            |    |
         //  |    |                   |   |------------|    |
         //  |    |-------------------|                     |
         //  |                                              |
         //  |----------------------------------------------|
         //
         // If "C" is selected and MOVE TO BACK is the command then B and A get added to the
         // list by buildListTouchingRect.  The problem is that our stacking code implements stacking
         // by removing A, B and C and then inserting them onto the list of nodes in the correct order
         // That leaves D still on the list and it actually is now behind C in the stacking order.
         // To correctly implement stacking we need to call buildListTouchingRect and compute a bounding
         // rectangle then get the objects touching that rect and call buildListTouchingRect again...keep
         // going until overlappingObjectList doesn't change.
         //
         //            TSCOM
         //               .TSDList
         //               > overlappingObjectList;
         //               m_graphWindow.buildListTouchingRect(rectInvalidate, graph, & overlappingObjectList);
         
         ETPairT< TSRect, List > retVal = buildListTouchingRect();
         if( null == retVal )
         {
            return;
         }
         rectInvalidate = retVal.getParamOne();
         
         // Extract only the nodes in the object list.
         // In java the list coming from buildListTouchingRect() only contains nodes
         // So, we only need to loop to find the nodes that are selected.
         List overlappingNodeList = retVal.getParamTwo();

         // Keep a list of the nodes we need to select when the operation is complete.
         List selectedNodeList = new ArrayList();

         //
         // This loop goes through the list of all the touching rects and extracts all
         // the nodes and appends them into 'overlappingNodeList', if the node is selected
         // it also puts the node into 'selectedNodeList'.
         for (Iterator iter = overlappingNodeList.iterator(); iter.hasNext();)
         {
            TSENode node = (TSENode)iter.next();
            
            if( node.isSelected() )
            {
               selectedNodeList.add( node );
            }
         }

         // Now that the overlappingNodeList contains all the nodes that overlap or
         // partially overlap the given node, including the node itself. Further, they are
         // in the order of from back to front. You can now remove these nodes from
         // the graph, and then insert them in your desired order, from back to front.

         if ( (nStackingCommand == IDrawingAreaControl.SOK_MOVETOFRONT) ||
              (nStackingCommand == IDrawingAreaControl.SOK_MOVETOBACK) )
         {
            for (Iterator iterator = selectedNodeList.iterator(); iterator.hasNext();)
            {
               TSGraphObject graphObject = (TSGraphObject)iterator.next();
               
               long thisID = graphObject.getID();

               boolean bIsInLocalList = m_setPEIDs.contains( new Long( thisID ));
               if ( ((nStackingCommand == IDrawingAreaControl.SOK_MOVETOFRONT) && bIsInLocalList) ||
                    ((nStackingCommand == IDrawingAreaControl.SOK_MOVETOBACK)  && !bIsInLocalList) )
               {
                  TSENode tsNode = (TSENode)graphObject;

                  if ( tsNode != null )
                  {
                     graph.remove( tsNode );
                     graph.insert( tsNode );

                        // CLEAN, this code seems redundent 
//                        TSCOM.TSRect > pThisBoundingRect;
//                        pTSDNode.getBoundingRect(_variant_t(true), & pThisBoundingRect);
//                        rectInvalidate.createUnion(pThisBoundingRect);
                  }
               }
            }
         }
         else if ( (nStackingCommand == IDrawingAreaControl.SOK_MOVEBACKWARD) ||
                   (nStackingCommand == IDrawingAreaControl.SOK_MOVEFORWARD) )
         {
            List desiredStackingOrderList = new ArrayList();
            if ( nStackingCommand == IDrawingAreaControl.SOK_MOVEBACKWARD )
            {
               //
               // Moving an item back is the same as reversing the list and then moving forwards.  So we
               // reverse the list here and then reverse it again before removing/inserting nodes.
               GetHelper.reverseList( m_graphWindow, overlappingNodeList );
            }

            // Now perform the move forward logic
            //
            // This is a little complicated, but here's the explination.  We first create a list of objects in the
            // order we want them - desiredStackingOrderList.  We then loop over the node list.  Whenever a selected 
            // item is found we put that onto a temporary list called moveUpOneNodeList.  If a non-selected item 
            // is encountered then the moveUpOneNodeList is first transfered to desiredStackingOrderList and then the
            // non-selected item is added to desiredStackingOrderList.  This will move all the non-selected items down and
            // selected items up.
            //                         Move Up One               Move Down One
            //    1                         1                          2 (selected)
            //    2 (selected)              4                          3 (selected)
            //    3 (selected)              2 (selected)               1
            //    4                         3 (selected)               4
            //    5                         5                          6 (selected)
            //    6 (selected)              6 (selected)               5
            //
            List moveUpOneNodeList = new ArrayList();

            boolean bFoundFirstSelectedItem = false;
            boolean bLastItemWasSelected = false;

            boolean bIsWithinList = false;
            for (Iterator iterator = overlappingNodeList.iterator(); iterator.hasNext();)
            {
               TSGraphObject graphObject = (TSGraphObject)iterator.next();
               
               long thisID = graphObject.getID();
               boolean bAdded = false;
               
               final boolean bFoundInList = m_setPEIDs.contains( new Long( thisID ));

               if ( bFoundInList )
               {
                  // Found a selected item
                  bFoundFirstSelectedItem = true;
               }

               if (bFoundFirstSelectedItem)
               {
                  if ( bFoundInList )
                  {
                     // Move the current item to move up one list
                     moveUpOneNodeList.add( graphObject );
                     bAdded = true;
                  }
                  else if ( !bLastItemWasSelected && !bFoundInList )
                  {
                     // Move the items from the move up one list to the current list and then add this item
                     // make sure that selected items are not the first ones on the list
                     int length = desiredStackingOrderList.size();
                     if ( length > 0 )
                     {
                        desiredStackingOrderList.addAll( moveUpOneNodeList );
                        moveUpOneNodeList.clear();
                     }
                  }
               }

               if ( !bAdded )
               {
                  desiredStackingOrderList.add( graphObject );
               }

               bLastItemWasSelected = bFoundInList;
            }

            desiredStackingOrderList.addAll( moveUpOneNodeList );
            moveUpOneNodeList.clear();
            // End of move forward logic

            if (nStackingCommand == IDrawingAreaControl.SOK_MOVEBACKWARD)
            {
               //
               // Now reverse the list back
               //
               GetHelper.reverseList( m_graphWindow, desiredStackingOrderList );
            }

            // Now we have a list of items in their desired stacking order.  Lets go through that
            // list and add/remove the nodes thus ending up with nodes in the right order.
            for (Iterator iterator = desiredStackingOrderList.iterator(); iterator.hasNext();)
            {
               TSENode tsNode = (TSENode)iterator.next();
               
               graph.remove( tsNode );
               graph.insert( tsNode );
            }
         }

         // Now reselect the items
         // This code only reselects nodes that were selected before this operation,
         // which avoids selecting contained nodes after a resize of an IADContainerDrawEngine
         selectByID( selectedNodeList );

         if ( bRedraw )
         {
            // Refresh the affected area
            m_graphWindow.addInvalidRegion( rectInvalidate );
         }
      }
   }

   /**
    * Select by id.  Look into these list items and, if it's in the map, select it
    *
    * @param list [in] A list of graphical objects
    */
   protected void selectByID( List list )
   {
      if ( (list != null) &&
           (m_drawingArea != null) )
      {
         for (Iterator iter = list.iterator(); iter.hasNext();)
         {
            TSGraphObject graphObject = (TSGraphObject)iter.next();
            
            long thisID = graphObject.getID();

            if ( m_setPEIDs.contains( new Long( thisID )) )
            {
               IPresentationElement pe = TypeConversions.getPresentationElement( graphObject );
               if ( pe != null )
               {
                  m_drawingArea.postSimplePresentationDelayedAction( pe, DiagramAreaEnumerations.SPAK_SELECT);
               }
            }
         }
      }
   }

   /**
    * Adds the ID's of the items in the list to the member vector of PE IDs
    *
    * @param list [in] The objects whose ids we need to extract and put into selectedObjectIDs
    */
   protected void addToIDList( List list )
   {
      if (list != null )
      {
         for (Iterator iter = list.iterator(); iter.hasNext();)
         {
            TSGraphObject graphObject = (TSGraphObject)iter.next();
            
            add( graphObject );
         }
      }
   }

   /**
    * Adds the TS object's ID to the member vector of PE IDs
    *
    * @param pTSObject [in] The object this pe helper is to act upon
    */
   protected void add( TSGraphObject tsObject )
   {
      if ( tsObject != null )
      {
         long id = tsObject.getID();

         m_setPEIDs.add( new Long( id ));
      }
   }
   /**
    * Calls buildListTouchingRect in a loop until the returned list doesn't change
    *
    * @param pOutInvalidateRect [out] The bounding rectangle for the objects
    * @param overlappingObjectList [out] The returned list of overlapping objects
    */
   protected ETPairT< TSRect, List > buildListTouchingRect()
   {
      ETPairT< TSRect, List > retVal = null;
      
      ETList< TSGraphObject > overlappingObjectList = new ETArrayList< TSGraphObject >();

      TSRect rectInvalidate = RectConversions.etRectToTSRect( m_rectBounding );
      TSDGraph graph = GetHelper.getCurrentGraph( m_graphWindow );

      if ( (rectInvalidate != null) &&
           (graph != null) )
      {
         List objectList = graph.getNodesTouchingBounds( rectInvalidate, null );
         if ( objectList != null )
         {
            long nOldLength = objectList.size();
            long nCurrentLength = 0;

            // Get the rectangle size from the object list and re-get the
            // object list, then requery for the object list.  Do this until
            // the object list size doesn't change
            while (nOldLength != nCurrentLength)
            {
               // build a node list
               List overlappingNodeList = new ArrayList();
               for (Iterator iter = objectList.iterator(); iter.hasNext();)
               {
                  TSGraphObject graphObject = (TSGraphObject)iter.next();
                  
                  if( graphObject instanceof TSENode )
                  {
                     overlappingObjectList.add( graphObject );
                  }
               }

               // Get the bounding rect of these nodes
               nOldLength = objectList.size();
               
               rectInvalidate = GetHelper.calculateTSGraphObjectsRect( overlappingObjectList );
               objectList = graph.getNodesTouchingBounds( rectInvalidate, null );
               nCurrentLength = objectList.size();
            }
            
            retVal = new ETPairT< TSRect, List >( rectInvalidate, objectList );
         }
      }
      
      return retVal;
   }
}
