/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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


package org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.structure.IComponent;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
//import org.netbeans.modules.uml.ui.products.ad.drawengines.IComponentDrawEngine;
//import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.MoveToFlags;
//import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
//import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
//import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
//import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author sumitabhk
 *
 */
public class GenealogyTree //TODO implements IGenealogyTree
{
   private static final int SPACING = 80;


   // List of nodes that need to be placed into a genealogy
   private ETList < IPresentationElement > m_UndeterminedNodes = null;

   // List of all our nodes
   private ETList < IPresentationElement > m_AllNodes = null;

   // This is our doubly linked list of elements
   private ArrayList< GenealogyNode > m_RootElements = new ArrayList< GenealogyNode >();

   // The diagram we're building a genealogy for
//   private IDrawingAreaControl m_Diagram = null;

   /// Here's a list of our components and their children
   private ComponentParentFinder.ParentChildCollection m_ParentChildPairs;


	/**
	 * 
	 */
	public GenealogyTree()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.addins.diagramcreator.IGenealogyTree#addUndeterminedNode(org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation)
	 */ //TODO
//	public void addUndeterminedNode(INodePresentation nodeToAdd)
//	{
//		if( null == m_UndeterminedNodes )
//      {
//         m_UndeterminedNodes = new ETArrayList< IPresentationElement >();
//         m_AllNodes = new ETArrayList< IPresentationElement >();
//      }
//		
//      if( (m_UndeterminedNodes != null) &&
//          (m_AllNodes != null) )
//      {
//         m_UndeterminedNodes.add( nodeToAdd );
//         m_AllNodes.add( nodeToAdd );
//      }
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.addins.diagramcreator.IGenealogyTree#buildGenealogy(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl)
	 */
//	public void buildGenealogy(IDrawingAreaControl diagram)
//	{
//      // Remember our diagram
//      m_Diagram = diagram;
//
//      if (m_Diagram != null)
//      {
//         // Find the parent/child relationships
//         determineParentChildRelationships();
//
//         // Populates our root elements with roots found in the list of undetermined nodes
//         populateRootElementsFromUndeterminedNodes();
//
//         // Go through the remaining list of undetermined nodes and find
//         // their location within the list of parents
//         int numUndetermined = (m_UndeterminedNodes != null) ? m_UndeterminedNodes.getCount() : 0;
//
//         // Go through the list of undetermined nodes and place each of the children
//         // until there are no children
//         if (numUndetermined > 0)
//         {
//            boolean bDidPlacement = true;
//            while (bDidPlacement)
//            {
//               bDidPlacement = false;
//
//               numUndetermined = m_UndeterminedNodes.getCount();
//               if (numUndetermined > 0)
//               {
//                  for (int i = 0 ; i < numUndetermined ; i++)
//                  {
//                     IPresentationElement pe = m_UndeterminedNodes.item( i );
//                     if (pe != null)
//                     {
//                        if (addToParent( pe ))
//                        {
//                           bDidPlacement = true;
//
//                           // Remove from our list of undetermined nodes
//                           m_UndeterminedNodes.removeItem( pe );
//                           numUndetermined = m_UndeterminedNodes.getCount();
//                           i--;
//                        }
//                     }
//                  }
//
//                  if (!bDidPlacement)
//                  {
//                     assert ( false ); // "Error : found a child that had no parent"
//                  }
//               }
//            }
//         }
//      }
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.addins.diagramcreator.IGenealogyTree#determineParentChildRelationships()
	 */
	public void determineParentChildRelationships()
	{
      // Divide our list into components and other elements, then use the ComponentParentFinder
      // to calculate the component owners of all the elements.
      long numUndetermined = (m_UndeterminedNodes != null) ? m_UndeterminedNodes.getCount() : 0;

      ETList< IComponent > components = new ETArrayList< IComponent >();
      ETList< IClassifier > classifiers = new ETArrayList< IClassifier >();

      for (int i = 0 ; i < numUndetermined ; i++)
      {
         IPresentationElement pe = m_UndeterminedNodes.item( i );
//         IElement firstSubject = TypeConversions.getElement( pe );
//         assert ( firstSubject != null );
//         
//         if ( firstSubject instanceof IComponent )
//         {
//            components.add( (IComponent)firstSubject );
//
//            if ( firstSubject instanceof IClassifier )
//            {
//               classifiers.add( (IClassifier)firstSubject );
//            }
//         }
//         else if ( firstSubject instanceof IClassifier )
//         {
//            classifiers.add( (IClassifier)firstSubject );
//         }
      }

      // Now find the pairs of components and classifiers
      m_ParentChildPairs = ComponentParentFinder.resolveParents( components, classifiers );
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.addins.diagramcreator.IGenealogyTree#calculateRectangles()
	 */
	public void calculateRectangles( IETRect rootGraphRect )
	{
      // If the root has children then we have to deal with it.  If there
      // are no children then it can sit in place
      for (Iterator iter = m_RootElements.iterator(); iter.hasNext();)
      {
         GenealogyNode geneNode = (GenealogyNode)iter.next();
         
         if ( geneNode.m_Children.size() > 0 )
         {
            // We have a root geneNode with children.  Move the rectangle of the root element
            // to the right most side of the root graph
            IETRect rect = geneNode.getDesiredRect();

            rect.inflate( rootGraphRect.getIntWidth(), 0 );
            geneNode.setDesiredRect( rect );

            geneNode.computeRectangleSizes();
            geneNode.setRectToContainChildren( geneNode.getDesiredRect().getTopLeft() );

            rootGraphRect.setRight( geneNode.getDesiredRect().getRight() + SPACING );
         }
      }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.addins.diagramcreator.IGenealogyTree#placeNodes()
	 */
	public void placeNodes()
	{
//      if ( m_Diagram != null )
//      {
//         // Tell each node to place itself now that the rectangle has been computed
//         for (Iterator iter = m_RootElements.iterator(); iter.hasNext();)
//         {
//            GenealogyNode geneNode = (GenealogyNode)iter.next();
//            
//            geneNode.placeNode(m_Diagram);
//         }
//      }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.addins.diagramcreator.IGenealogyTree#onGraphEvent(int, long)
	 */
	public void onGraphEvent(int nKind, String message)
	{
      int count = (m_AllNodes != null) ? m_AllNodes.getCount() : 0;
      
      // TODO CThermCtrlProxy thermState( message, count);

      // Autorouting takes forever and that's what the component draw engine does
      // in the generic pre/post resize/layout.  So if it's a component draw engine 
      // then handle differently
      for (int i = 0 ; i < count ; i++)
      {
         IPresentationElement pe = m_AllNodes.item( i );
         if ( pe != null )
         {
//            IETGraphObject etGraphObject = TypeConversions.getETGraphObject( pe );
//            IDrawEngine drawEngine = TypeConversions.getDrawEngine( pe );
            
//            IComponentDrawEngine componentDE = null;
//            if (drawEngine instanceof IComponentDrawEngine)
//            {
//               componentDE = (IComponentDrawEngine)drawEngine;
//               
//               componentDE.setAllowAutoRouteEdges(false);
//            }
//
//            if ( etGraphObject != null )
//            {
//               etGraphObject.onGraphEvent( nKind );
//            }
//
//            if ( componentDE != null )
//            {
//               componentDE.setAllowAutoRouteEdges(true);
//            }
         }

         // TODO thermState.update(sMessage, i);
      }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.addins.diagramcreator.IGenealogyTree#setStackingOrder()
	 */
	public void setStackingOrder()
	{
//      if ( m_Diagram != null )
//      {
//         // Tell each child to set its stacking order
//         for (Iterator iter = m_RootElements.iterator(); iter.hasNext();)
//         {
//            GenealogyNode geneNode = (GenealogyNode)iter.next();
//            geneNode.setStackingOrder( m_Diagram );
//         }
//      }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.addins.diagramcreator.IGenealogyTree#printTree()
	 */
	public void printTree()
	{
//      if ( m_Diagram != null )
//      {
//         // Tell each child to set its stacking order
//         for (Iterator iter = m_RootElements.iterator(); iter.hasNext();)
//         {
//            GenealogyNode geneNode = (GenealogyNode)iter.next();
//            geneNode.printTree( 0 );
//         }
//      }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.addins.diagramcreator.IGenealogyTree#distributeAllComponentPorts()
	 */
	public void distributeAllComponentPorts()
	{
      int count = (m_AllNodes != null) ? m_AllNodes.getCount() : 0;
      
/* TODO
      String sMessage;
      vERIFY(sMessage.loadString(IDS_LAYING_OUT_PORTS));
      CThermCtrlProxy thermState( sMessage, count);
*/      

      // Autorouting takes forever and that's what the component draw engine does
      // in the generic pre/post resize/layout.  So if it's a component draw engine 
      // then handle differently
      for ( int i = 0 ; i < count ; i++ )
      {
         IPresentationElement pe = m_AllNodes.item( i );
         if ( pe != null )
         {
//            IETGraphObject etGraphObject = TypeConversions.getETGraphObject( pe );
//            IDrawEngine drawEngine = TypeConversions.getDrawEngine( pe );
            
//            if (drawEngine instanceof IComponentDrawEngine)
//            {
//               IComponentDrawEngine componentDE = (IComponentDrawEngine)drawEngine;
//               
//               componentDE.setAllowAutoRouteEdges(false);
//               componentDE.distributeInterfacesOnAllPorts(false);
//               componentDE.setAllowAutoRouteEdges(true);
//            }
         }

         // TODO thermState.update(sMessage, i);
      }
	}
   
   
   //////////////////////////////////////////////////////////////////////////////////////////////
   //
   // GenealogyNode
   // The node that composes our linked list
   //
   //////////////////////////////////////////////////////////////////////////////////////////////

   // Builds a new node
   protected GenealogyNode buildGenealogyNode( IPresentationElement item )
   {
      return buildGenealogyNode( item, null );
   }
   protected GenealogyNode buildGenealogyNode( IPresentationElement item, GenealogyNode pParent )
   {
      GenealogyNode returnNode = null;

      if (item != null)
      {
         returnNode = new GenealogyNode();

         returnNode.m_PE = item;
         returnNode.m_Parent = pParent;

//         IDrawEngine thisDrawEngine = TypeConversions.getDrawEngine( item );
//         if ( thisDrawEngine != null )
//         {
//            IETRect thisComponentSize = thisDrawEngine.getLogicalBoundingRect( false );
//
//            returnNode.setDesiredRect( thisComponentSize );
//         }
      }

      return returnNode;
   }
   
   protected class GenealogyNode
   {
      private IPresentationElement m_PE = null;
      private GenealogyNode m_Parent = null;
      private ArrayList< GenealogyNode > m_Children = new ArrayList< GenealogyNode >();
      private String m_sName = "";

      private IETRect m_DesiredRect = new ETRect();

      public GenealogyNode()
      {
      }

      /**
       * Finds the parent in our list and adds it
       *
       * @param item [in] The presentation element to add to the tree
       * @param parentElement [in] The parent for item
       */
      boolean addToParent( IPresentationElement item, IElement parentElement)
      {
         boolean bDidAdd = false;
         
         if ( (m_PE != null) &&
              (item != null) &&
              (parentElement != null) )
         {
            // First see if we are the parent
            boolean bIsSubject = m_PE.isSubject( parentElement );
            if (bIsSubject)
            {
               // Found it.  Add it to our list of children
               GenealogyNode newNode = buildGenealogyNode( item );
               m_Children.add( newNode );
               bDidAdd = true;
            }
            else
            {
               // Now see if any of our children act as a parent for the argument item
               for (Iterator iter = m_Children.iterator(); iter.hasNext();)
               {
                  GenealogyNode geneNode = (GenealogyNode)iter.next();
                  
                  bDidAdd = geneNode.addToParent( item, parentElement );
                  if (bDidAdd)
                  {
                     break;
                  }
               }
            }
         }
         
         return bDidAdd;
      }

      /**
       * Computes the sizes of all the rectangles
       */
      void computeRectangleSizes()
      {
         // First go through all the children and tell them to set their size to their children
         for (Iterator iter = m_Children.iterator(); iter.hasNext();)
         {
            GenealogyNode geneNode = (GenealogyNode)iter.next();
            
            geneNode.computeRectangleSizes();
         }

         // Now calculate the sum of all our children and orient them left to right
         int numChildren = m_Children.size();

         if (numChildren > 0)
         {
            int nWidth = 0;
            int nHeight = 0;

            // Allow for spacing on the top and bottom
            nHeight = (2 * SPACING);

            // Allow for spacing on the left and right, and between each child
            nWidth = (2 * SPACING);
   
            if ( numChildren > 0 )
            {
               nWidth += (numChildren-1) * SPACING;
            }

            // Now see what the Math.max height is and total width
            int nTotalWidth = 0;
            int nMaxHeight = 0;
            for (Iterator iter = m_Children.iterator(); iter.hasNext();)
            {
               GenealogyNode geneNode = (GenealogyNode)iter.next();
               
               IETRect origRect = geneNode.getRectWithChildren();

               nTotalWidth += origRect.getIntWidth();
               if (nMaxHeight < Math.abs(origRect.getIntHeight()) )
               {
                  nMaxHeight = Math.abs(origRect.getIntHeight());
               }
            }

            nHeight = Math.max(nHeight, nMaxHeight + (2 * SPACING) );
            nWidth += nTotalWidth;

            // Set our rectangle to match the childrens
            IETRect thisDesiredRect = getDesiredRect();
            thisDesiredRect.setRight( Math.max( thisDesiredRect.getRight(), thisDesiredRect.getLeft() + nWidth) );
            thisDesiredRect.setBottom( Math.min( thisDesiredRect.getBottom(), thisDesiredRect.getTop() - nHeight) );
            setDesiredRect(thisDesiredRect);
         }
      }

      /**
       * Sets the rectangle of this object to contain all of the children
       *
       * @param upperLeft [in] The upper left point for the beginning of the childrens list
       */
      void setRectToContainChildren( Point upperLeft )
      {
         upperLeft.x += SPACING;
         upperLeft.y -= SPACING;

         // Move the children left to right
         Point lastTopLeft = (Point)upperLeft.clone();
         for (Iterator iter = m_Children.iterator(); iter.hasNext();)
         {
            GenealogyNode geneNode = (GenealogyNode)iter.next();
            
            IETRect origRect = geneNode.getDesiredRect();
            IETRect newRect = new ETRect();

            newRect.setLeft(   lastTopLeft.x );
            newRect.setRight(  newRect.getLeft() + Math.abs(origRect.getIntWidth()) );
            newRect.setTop(    lastTopLeft.y );
            newRect.setBottom( newRect.getTop() - Math.abs(origRect.getIntHeight()) );

            geneNode.setDesiredRect(newRect);

            // Set the starting point for the next child
            lastTopLeft.x = newRect.getRight() + SPACING;
         }

         // Tell each child to move its children
         for (Iterator iter = m_Children.iterator(); iter.hasNext();)
         {
            GenealogyNode geneNode = (GenealogyNode)iter.next();
            
            geneNode.setRectToContainChildren( geneNode.getDesiredRect().getTopLeft() );
         }
      }

      /**
       * Places the node in the diagram
       */
//      void placeNode( IDrawingAreaControl diagram )
//      {
//         if( null == diagram ) throw new IllegalArgumentException();
//   
//         // Tell each child to place itself
//         for (Iterator iter = m_Children.iterator(); iter.hasNext();)
//         {
//            GenealogyNode geneNode = (GenealogyNode)iter.next();
//            geneNode.placeNode( diagram );
//         }
//
//         // Now place us
//         if (m_PE instanceof INodePresentation)
//         {
//            INodePresentation nodePE = (INodePresentation)m_PE;
//            
//            nodePE.moveTo(m_DesiredRect.getCenterPoint().x, m_DesiredRect.getCenterPoint().y, MoveToFlags.MTF_MOVEX | MoveToFlags.MTF_MOVEY | MoveToFlags.MTF_LOGICALCOORD );
//            nodePE.resize(Math.abs(m_DesiredRect.getIntWidth()), Math.abs(m_DesiredRect.getIntHeight()), false);
//         }
//      }
      
      /**
       * Prints the tree.  Used in debug
       *
       * @param increment [in] The child level.  Used to offset the print.
       */
      void printTree( int increment )
      {
         // Print us
         String  message = "";

         // Offset the message by the increment
         for (long i = 0 ; i < increment ; i++)
         {
            message += " ";
         }
         message += " name=" + m_sName + ", ";
         message += " top=" + m_DesiredRect.getTop() + ", ";
         message += " bottom=" + m_DesiredRect.getBottom() + ", ";
         message += " left=" + m_DesiredRect.getLeft() + ", ";
         message += " right=" + m_DesiredRect.getRight() + "\n";
         ETSystem.out.println( message );

         // Now print all our children
         for (Iterator iter = m_Children.iterator(); iter.hasNext();)
         {
            GenealogyNode geneNode = (GenealogyNode)iter.next();

            geneNode.printTree( increment + 1 );
         }
      }

      /**
       * Sets the desired rect and moves all the children
       */
      void setDesiredRectAndMoveChildren( IETRect newRect )
      {
         int xOffset = newRect.getLeft() - m_DesiredRect.getLeft();
         int yOffset = newRect.getTop()  - m_DesiredRect.getTop();

         m_DesiredRect = (IETRect)newRect.clone();

         // Now offset all the children
         for (Iterator iter = m_Children.iterator(); iter.hasNext();)
         {
            GenealogyNode geneNode = (GenealogyNode)iter.next();

            IETRect newestRect = (IETRect)geneNode.m_DesiredRect.clone();

            newestRect.offsetRect(xOffset, yOffset);
            geneNode.setDesiredRectAndMoveChildren( newestRect );
         }
      }

      /**
       * Sets the stacking order
       */
//      void setStackingOrder( IDrawingAreaControl diagram )
//      {
//         if( null == diagram ) throw new IllegalArgumentException();
//   
//         // Move us to the front
//         diagram.executeStackingCommand( m_PE, IDrawingAreaControl.SOK_MOVETOFRONT, false );
//
//         for (Iterator iter = m_Children.iterator(); iter.hasNext();)
//         {
//            GenealogyNode geneNode = (GenealogyNode)iter.next();
//            
//            geneNode.setStackingOrder(diagram);
//         }
//      }

      /**
       * Sets the desired rect
       */
      void setDesiredRect( IETRect rect )
      {
         m_DesiredRect = rect;
      }
      
      /**
       * Gets the desired rect
       */
      IETRect getDesiredRect()
      {
         return m_DesiredRect;
      }

      /**
       * Returns the size of this node + any dependant children.  For instance, the
       * component has lollypops and assembly connectors that make the size of any
       arent bigger.
       */
      IETRect getRectWithChildren()
      {
         IETRect returnRect = (IETRect)m_DesiredRect.clone();

         if ( m_PE != null )
         {
//            IDrawEngine drawEngine = TypeConversions.getDrawEngine( m_PE );
//            if (drawEngine instanceof IComponentDrawEngine)
//            {
//               IComponentDrawEngine compDE = (IComponentDrawEngine)drawEngine;
//               
//               ETPairT< IETRect, IETRect > retval = compDE.getBoundingRectWithLollypops();
//               IETRect boundingRect = retval.getParamTwo();
//               if ( boundingRect != null )
//               {
//                  returnRect.setRight(  Math.max( m_DesiredRect.getRight(),  m_DesiredRect.getLeft() + boundingRect.getIntWidth()) );
//                  returnRect.setBottom( Math.min( m_DesiredRect.getBottom(), m_DesiredRect.getTop() - Math.abs(boundingRect.getIntHeight())) );
//               }
//            }
         }

         return returnRect;
      }
   };
   
   /**
    * Returns the parent for this presentation element
    *
    * @param childPE [in] The child whose parent is to be determined within our list of nodes
    * we're placing on a diagram.
    * @param parentElement [out] The parent for this child.  This could be null.  That just means
    * that childPE's parent is not being placed onto this diagram.
    */
   protected IElement getParentElement( IPresentationElement childPE )
   {
      if( null == childPE ) throw new IllegalArgumentException();
      
      IElement parentElement = null;

      // Go through our list of pairs and find the child and its parent
      IElement childElement = childPE.getFirstSubject();
      assert ( childElement != null );
      if ( childElement != null )
      {
         for (Iterator iter = m_ParentChildPairs.iterator(); iter.hasNext();)
         {
            ComponentParentFinder.ParentChildPair pair = (ComponentParentFinder.ParentChildPair)iter.next();
            
            IClassifier classifier = pair.getParamTwo();
            if (classifier != null)
            {
               boolean bIsSame = classifier.isSame( childElement );
               if ( bIsSame )
               {
                  parentElement = pair.getParamOne();
                  break;
               }
            }
         }
      }

      return parentElement;
   }

   /**
    * Is the childElement a child of component parent
    */
   protected boolean getIsInternalClassifier( IComponent component, IElement childElement )
   {
      if( null == component ) throw new IllegalArgumentException();
      if( null == childElement ) throw new IllegalArgumentException();
      
      boolean bIsInternalClassifier = false;
      
      if( childElement instanceof IClassifier )
      {
         bIsInternalClassifier = component.getIsInternalClassifier( (IClassifier)childElement );
      }

      return bIsInternalClassifier;
   }
   
   /**
    * Populates our root elements with roots found in the list of undetermined nodes
    */
   protected void populateRootElementsFromUndeterminedNodes()
   {
//      if ( m_Diagram != null )
//      {
//         int numUndetermined = (m_UndeterminedNodes != null) ? m_UndeterminedNodes.getCount() : 0;
//         if ( numUndetermined > 0 )
//         {
//            ETList< IPresentationElement > foundRoots = new ETArrayList< IPresentationElement >();
//
//            for (int i = 0 ; i < numUndetermined ; i++)
//            {
//               // Get all the root nodes by going through the list
//               // and extracting those presentation elements whose
//               // parents are not represented in the list
//               IPresentationElement pe = m_UndeterminedNodes.item( i );
//               if ( pe != null )
//               {
//                  IElement parentElement = getParentElement( pe );
//                  if ( null == parentElement )
//                  {
//                     // We have a presentation element with it's parent unrepresented
//                     // in our genealogy
//                     foundRoots.add( pe );
//                  }
//               }
//            }
//
//            // Remove these roots from our list of undetermined nodes
//            m_UndeterminedNodes.removeThese( foundRoots );
//
//            // Add them to our list of root nodes
//            int numRoots = foundRoots.getCount();
//
//            for ( int i = 0 ; i < numRoots ; i++ )
//            {
//               // Get all the root nodes by going through the list
//               // and extracting those presentation elements whose
//               // parents are not represented in the list
//               IPresentationElement pe = foundRoots.item( i );
//               if ( pe != null )
//               {
//                  m_RootElements.add( buildGenealogyNode( pe ) );
//               }
//            }
//         }
//      }
   }

   /**
    * Finds the parent of this item in the tree and add it
    */
   protected boolean addToParent( IPresentationElement item )
   {
      boolean bDidAdd = false;
   
      if ( item != null )
      {
         IElement parentElement = getParentElement( item );
      
         assert ( parentElement != null );
         if ( parentElement != null )
         {
            // Find this parent in the list
            for (Iterator iter = m_RootElements.iterator(); iter.hasNext();)
            {
               GenealogyNode geneNode = (GenealogyNode)iter.next();

               bDidAdd = geneNode.addToParent( item, parentElement );
               if ( bDidAdd )
               {
                  break;
               }
            }
         }
      }

      return bDidAdd;
   }
}



