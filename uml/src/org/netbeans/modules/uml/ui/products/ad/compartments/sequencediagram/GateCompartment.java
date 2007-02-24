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


package org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram;

import java.awt.Point;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.ConnectorPiece;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.MoveToFlags;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PointConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PresentationHelper;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.drawing.TSDEdge;
import com.tomsawyer.editor.TSEConnector;
import com.tomsawyer.editor.TSENode;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;

/**
 * @author brettb
 *
 */
public class GateCompartment extends ETCompartment implements IGateCompartment
{

   /**
    * 
    */
   public GateCompartment()
   {
      super();
   }

   /**
    * @param pDrawEngine
    */
   public GateCompartment(IDrawEngine pDrawEngine)
   {
      super(pDrawEngine);
   }
   
   
   // ICompartment methods

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getCompartmentID()
    */
   public String getCompartmentID()
   {
      return "GateCompartment";
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#clone(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine, org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment)
    */
   public ICompartment clone( IDrawEngine parentDrawEngine )
   {
      IGateCompartment newCompartment = new GateCompartment();
      if( newCompartment != null )
      {
         newCompartment.setEngine( parentDrawEngine );
      }

      return newCompartment;
   }
 
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getCompartmentShape()
    */
   public ETList< IETPoint > getCompartmentShape()
   {
      ETList< IETPoint > tempPointList = null; /* TODO new PointList();

      // Note the paths must be in counter-clockwise order, to make the area hollow
      ETRect rectInner = new ETRect( getTransform().getWinScaledOwnerRect() );
      rectInner.deflateRect( HOLLOW_EDGE_WIDTH, HOLLOW_EDGE_WIDTH );

      // Move to the inside of the compartment
      appendPoint( tempPointList, cPoint( rectInner.left-1, rectInner.top ));
      appendPoint( tempPointList, cPoint( rectInner.left,   rectInner.top ));
      appendPoint( tempPointList, cPoint( rectInner.left,   rectInner.bottom ));
      appendPoint( tempPointList, cPoint( rectInner.right,  rectInner.bottom ));
      appendPoint( tempPointList, cPoint( rectInner.right,  rectInner.top ));
      appendPoint( tempPointList, cPoint( rectInner.left-1, rectInner.top ));
*/
      return tempPointList;
   }
   
   public void draw( IDrawInfo pDrawInfo, IETRect pBoundingRec )
   {
      // Expand the rectangle the sides by one pixel to make sure that connectors attach properly
      IETRect rectBounding = (IETRect)pBoundingRec.clone();

      rectBounding.setLeft( rectBounding.getLeft() - 1 );
      rectBounding.setRight( rectBounding.getRight() + 1 );

      // Call the base class first to update the member bounding rectangle
      super.draw( pDrawInfo, rectBounding );
   }
   
   /**
    * Called when a node is resized.  nodeResizeOriginator is a TSENodeResizeOriginator.
    *
    * @param nodeResizeOriginator[in] The TS enumeration detailing who resized this object
    */
   void nodeResized(long nodeResizeOriginator)
   {
      updateConnectors( (IDrawInfo)(null) );
   }


   // IConnectorsCompartment
   /**
    * Indicates that a message edge can be started from the current logical location.
    *
    * @param  ptLogical[in] Logical view coordinates to test
    * @return TRUE if the location is a place where a message can be started
    */
   public boolean canStartMessage( IETPoint ptLogical )
   {
      return isValidEdgeStartFinish( ptLogical );
   }
   
   /**
    * Indicates that a message edge can be finished from the current logical location.
    *
    * @param ptLogical[in] Logical view coordinates to test
    * @return TRUE if the location is a place where a message can be finished
    */
   public boolean canFinishMessage( IETPoint ptLogical )
   {
      return isValidEdgeStartFinish( ptLogical );
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IConnectorsCompartment#connectMessage(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, int, int)
    */
   public TSEConnector connectMessage(IETPoint point, int kind, int connectMessageKind, TSEConnector connector )
   {
      if( connector == null )
      {
         if( (m_engine != null) &&
             (m_engine instanceof INodeDrawEngine) )
         {
            INodeDrawEngine engine = (INodeDrawEngine)m_engine;
            if( engine != null )
            {
               connector = engine.addConnector();
            }
         }
      }

      if( connector != null )
      {
         // Calculate, and set the offset for the connector
         IETRect rectBounding = getLogicalBoundingRect();

         Point ptConnector = PointConversions.ETPointToPoint( point );

         final double perMilX = (ptConnector.x < rectBounding.getCenterPoint().x) ? -0.5 : 0.5;
         connector.setProportionalXOffset( perMilX );
         connector.setProportionalYOffset( 0.5 );

         setConnectorOffset( connector, ptConnector.y, rectBounding );
      }
      
      return connector;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IConnectorsCompartment#updateConnectors(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
    */
   public void updateConnectors(IDrawInfo drawInfo)
   {
      boolean bDrawingToMainDrawingArea = true;
      if ( drawInfo != null )
      {
         // If we're drawing to the overview window don't update the connectors
         bDrawingToMainDrawingArea = drawInfo.getDrawingToMainDrawingArea();
      }

      if( bDrawingToMainDrawingArea &&
          (m_engine != null) &&
          (m_engine instanceof INodeDrawEngine) )
      {
         INodeDrawEngine engine = (INodeDrawEngine)m_engine;
         if( engine != null )
         {
            TSENode node = engine.getNode();
            if( node != null )
            {
               List list = node.connectors();
               if( list != null )
               {
                  updateConnectors( list );
               }
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IConnectorsCompartment#moveConnector(com.tomsawyer.drawing.TSConnector, double, boolean, boolean)
    */
   public void moveConnector(TSConnector connector, double dY, boolean bDoItNow, boolean bSetYOfAssociatedPiece)
   {
      TSConstPoint ptNew = connector.getCenter();
      final int lDeltaY = (int)(dY - ptNew.getY());
      if( lDeltaY != 0 )
      {
         if( bSetYOfAssociatedPiece )
         {
            // Find the "other" connector
            TSConnector otherConnector = PresentationHelper.getConnectorOnOtherEndOfEdge( connector, false );
            // Find the piece that is attached to the connector
            ConnectorPiece piece = ConnectorPiece.getPieceAttachedToConnector( otherConnector );

            if( piece != null )
            {
               int lclOther = piece.getLocation( otherConnector );
               if( (LifelineConnectorLocation.LCL_BOTTOMRIGHT == lclOther) || 
                   (LifelineConnectorLocation.LCL_BOTTOMLEFT == lclOther) )
               {
                  piece.setLogicalBottom( (int)ptNew.getY() );
               }
               else
               {
                  piece.setLogicalTop( (int)ptNew.getY() );
               }
            }
         }

         // Get the logical bounding rectangle for this compartment,
         // which should also be the bounding rectangle for the draw engine.
         final IETRect rectBounding = getLogicalBoundingRect();
         validateConnectorMove( connector, (int)dY, rectBounding, bDoItNow );
      }
   }
   
   
   // protected member operations

   /**
    * Get the Interaction from the associated model element.
    * 
    * @param ppInteraction[out]
    */
   protected IInteraction getInteraction()
   {
      IInteraction interaction = null;

      IElement element = getModelElement();
      if( (element != null) &&
          (element instanceof IInteraction) )
      {
         interaction = (IInteraction)element;
      }

      return interaction;
   }

   /**
    * Ensure that the movement of a connector either shifts the connector's location or moves this node
    */
   void validateConnectorMove( TSConnector connector,
                               long lY,
                               final IETRect rectBounding,
                               boolean bDoItNow )
   {
      // TODO_THROW if( !pConnector )  E_INVALIDARG ;

      // if the connector would move outside the compartment,
      // move the draw engine
      if( (lY > rectBounding.getTop()) ||
          (lY < rectBounding.getBottom()) )
      {
         if( m_engine != null )
         {
            IPresentationElement presentationElement = TypeConversions.getPresentationElement( m_engine );
            if( presentationElement instanceof INodePresentation )
            {
               INodePresentation nodePresentation = (INodePresentation)presentationElement;
               
               TSConstPoint ptNew = connector.getCenter();
               final int lDeltaY = (int)(lY - ptNew.getY());
               final int lNewY = rectBounding.getCenterPoint().y + lDeltaY;

                nodePresentation.moveTo( 0, lNewY, (int)(MoveToFlags.MTF_MOVEY | MoveToFlags.MTF_LOGICALCOORD));

               if( bDoItNow )
               {
                  // Invalidate the draw engine
                  m_engine.invalidate() ;
                  // TODO_MFC pumpMessages( WM_PAINT, WM_PAINT );   // forces the invalidate now
               }
            }
         }
      }
      else
      {
         setConnectorOffset( connector, lY, rectBounding );
      }
   }

   /**
    * Returns true when the input point is a valid location for starting/finishing an edge
    */
   boolean isValidEdgeStartFinish( IETPoint ptLogical )
   {
      boolean bIsValidEdgeStartFinish = false;

      final IETRect  rectBounding = getLogicalBoundingRect();
      if( rectBounding.contains( ptLogical ) )
      {
         final long lInnerLeft = rectBounding.getLeft() + HOLLOW_EDGE_WIDTH;
         final long lInnerRight = rectBounding.getRight() - HOLLOW_EDGE_WIDTH;
         if( (ptLogical.getX() <= lInnerLeft) ||
             (ptLogical.getX() >= lInnerRight) )
         {
            bIsValidEdgeStartFinish = true;
         }
      }

      return bIsValidEdgeStartFinish;
   }

   void setConnectorOffset( TSConnector connector,
                            long lY,
                            final IETRect rectBounding )
   {
      if( connector != null )
      {
         // don't change the ProportionalXOffset
         connector.setProportionalYOffset( 0.5 );

         // The offset should always be inside the gate's bounding rectangle
         // Fix J862:  In java the height value from the logical bounding rect is negative.
         final int iOffsetY = (int)Math.max( -rectBounding.getHeight(), Math.min( 0, (lY - rectBounding.getTop()) ));
         connector.setConstantYOffset( iOffsetY );
      }
   }

   /**
    * Ensures that the connected messages are horizontal.
    *
    * @param pItr[in] The iterator containing the list of connectors
    */
   void updateConnectors( List list )
   {
      if( list != null )
      {
         // Get the logical bounding rectangle for this compartment,
         // which should also be the bounding rectangle for the draw engine.
         IETRect rectBounding = getLogicalBoundingRect();
         
         for (Iterator iter = list.iterator(); iter.hasNext();)
         {
            TSConnector connector = (TSConnector)iter.next();
            
            // Make sure the connector does not point to bad data
            connector.setUserObject( null );

            // Fix W10421:  Ensure that the connected edge is for a message
            TSDEdge edge = PresentationHelper.getConnectedEdge( connector, false );
            if( edge != null )
            {
               IDrawEngine drawEngine = TypeConversions.getDrawEngine( edge );
               if( (drawEngine != null) &&
                   (drawEngine instanceof IDrawEngine) )
               {
                  // Determine the connector on the other end of the edge
                  TSConnector otherConnector =
                  PresentationHelper.getConnectorOnOtherEndOfEdge( connector, true );
                  if( otherConnector != null )
                  {
                     // Get the center of the connector on the other end of the edge
                     TSConstPoint ptOther = otherConnector.getCenter();
                     setConnectorOffset( connector, (long)ptOther.getY(), rectBounding );
                  }
               }
            }
         }
      }
   }
}


