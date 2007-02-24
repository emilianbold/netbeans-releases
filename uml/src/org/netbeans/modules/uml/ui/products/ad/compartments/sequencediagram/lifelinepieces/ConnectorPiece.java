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


package org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IMessageKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.support.Debug;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.controls.drawingarea.UIDiagram;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.ETLifelineCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IADLifelineCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IConnectorsCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.LifelineConnectorLocation;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PresentationHelper;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.ReconnectEdgeContext;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.drawing.TSDEdge;
import com.tomsawyer.drawing.TSDNode;
import com.tomsawyer.editor.TSEConnector;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.graph.TSNode;
//import com.tomsawyer.jnilayout.TSEdgeDirection;
import com.tomsawyer.util.TSObject;

/**
 * 
 * @author Trey Spiva
 */
public abstract class ConnectorPiece extends ParentPiece
{

   public final static int MLF_NONE = 0;
   public final static int MLF_TOPLEFT = 1;
   public final static int MLF_TOPRIGHT = 2;
   public final static int MLF_BOTTOMRIGHT = 4;
   public final static int MLF_BOTTOMLEFT = 8;

   private final static int ACTIVATION_CORNER_COUNT = (LifelineConnectorLocation.LCL_BOTTOMLEFT+1);
   private TSConnector[] m_Connectors = new TSConnector[ACTIVATION_CORNER_COUNT];

   /// Strings maintained to support connectors read in from an archive
   ArrayList< String > m_strSourceEdgeIDs = new ArrayList< String >();
   ArrayList< String > m_strTargetEdgeIDs = new ArrayList< String >();
   private boolean m_bUpdatingConnectors = false; 	// Used to avoid recursion.

   /**
    * Creates and initialize a connector piece.
    * 
    * @param parent
    * @param parentPiece
    * @param topLeft
    * @param height
    */
   public ConnectorPiece(ETLifelineCompartment parent, LifelineCompartmentPiece parentPiece, IETPoint topLeft, int height)
   {
      super(parent, parentPiece, topLeft, height);
      
      for( int i=0; i<ACTIVATION_CORNER_COUNT; i++)
      {
         m_strSourceEdgeIDs.add( "" );
         m_strTargetEdgeIDs.add( "" );
      }
   }

   /**
    * Copies height, and connectors from the input piece
    *
    * @param piece [in] The piece to be copied
    * @return The new connector that has no edges attached.
    */
   public TSConnector copy( ConnectorPiece piece )
   {
      if( null == piece ) throw new IllegalArgumentException();

      TSConnector openConnector = null;

      //  These are assumed to have been set
      ParentPiece parent = (ParentPiece)getParentPiece();

      // Don't copy these:
      // m_Y
      // m_strSourceEdgeIDs
      // m_strTargetEdgeIDs
      // m_cpPresentationHelper

      // Perform a raw height change here, and bump the siblings (below),
      // after the connectors have been moved to this piece
      // This is because we don't want to affect
      // the piece that is currently attached to the edges.
      final int iNewHeight = piece.getHeight();
      final boolean bChangeHeight = (iNewHeight > getHeight());
      if( bChangeHeight )
      {
         parent.setHeight( iNewHeight );
      }

      // Process the corner connectors
      for( int iIndx=LifelineConnectorLocation.LCL_TOPLEFT; iIndx<=LifelineConnectorLocation.LCL_BOTTOMLEFT; iIndx++ )
      {
         boolean bEdgesReconnected = moveConnectorEdges( piece.m_Connectors[iIndx], iIndx );
         if( (!bEdgesReconnected) &&
             (piece.m_Connectors[iIndx] != null) )
         {
            // This connector is the "open" connector
            //assert ( null == openConnector ); // should only have one open connector
            openConnector = createConnector( iIndx );
         }
      }

      // Move the children so the diagram remains consistent
      for (Iterator iter = m_ListPieces.iterator(); iter.hasNext();)
      {
         ConnectorPiece child = (ConnectorPiece)iter.next();
         
         insertPiece( child );
         // the child is removed from the input piece below, outside this loop

         // Setting the height forces the grand parents children to get promoted to this child
         child.setHeight( child.getHeight() ); 

         for( int iIndx=LifelineConnectorLocation.LCL_TOPLEFT; iIndx<=LifelineConnectorLocation.LCL_BOTTOMLEFT; iIndx++ )
         {
            // Remember the connector, but remove it from the piece so we can reattach the connector
            // using the proper parent node.
            TSConnector connector = child.m_Connectors[iIndx];
            child.m_Connectors[iIndx] = null;
            child.moveConnectorEdges( connector, iIndx );
         }
      }

      // Remove the children from the input piece
      m_ListPieces.clear();

      // Now that the connectors have been moved to this piece,
      // it is safe to bump its siblings.
      if( bChangeHeight &&
          (parent != null) )
      {
         parent.bumpSiblingsBelow( this, true );
      }

      // Clean up both nodes
      getParent().postValidateNode();
      piece.getParent().postValidateNode();
      
      return openConnector;
   }

   /** 
    * Returns true when it is ok to finish a message on this piece 
    */
   public abstract boolean canFinishMessage(int iTop);

   /**
    * Test if it is ok to update the input connector location for this piece.
    * 
    * @param corner The corner that is to be updated.  Must be on of the values
    *               in the interface LifelineConnectorLocation 
    * @return <code>true</code> if the corner can be updated.
    * @see LifelineConnectorLocation
    */
   public abstract boolean canUpdateAssociatedPiece(int corner);
   
   /**
    * Returns the lifeline piece that is attached to the connector.
    *
    * @param connector The piece that has the attached piece.
    * @return The attached piece.  <code>null</code> may be returned if 
    *         a piece is not attched to the connector.
    * @see CConnectorPiece#AttachConnector(Lcom.tomsawyer.drawing.TSConnector)
    */
   public static ConnectorPiece getPieceAttachedToConnector(TSConnector connector)
   {
      ConnectorPiece retVal = null;

      assert connector != null : "The connector is null.";
      
      if(connector != null)
      {         
         Object userObject = connector.getUserObject();
         if (userObject instanceof ConnectorPiece)
         {
            retVal = (ConnectorPiece)userObject;
         }
      }
      return retVal;
   }
   
   /**
    * From the given product element, which is an edge, determine the associated
    * return edge.
    *
    * @param pETGraphObject The product element, which is an edge, used to 
    *                       determine the return edge
    *
    * @return The return edge, or <code>null</code> if none is available
    */
   public static TSEEdge getReturnEdge( IETGraphObject pETGraphObject )
   {
      TSEEdge pEdge = null;

      if( pETGraphObject != null )
      {
         IElement element = TypeConversions.getElement( pETGraphObject );
         if( element instanceof IMessage )
         {
            IMessage message = (IMessage)element;
            int kind = message.getKind();
            switch( kind )
            {
               case IMessageKind.MK_SYNCHRONOUS:
                  {
                     TSEEdge pEdgeMessage = TypeConversions.getOwnerEdge( pETGraphObject );
                     if( pEdgeMessage != null )
                     {
                        TSConnector pConnector = pEdgeMessage.getTargetConnector();
                        if( pConnector != null )
                        {
                           ConnectorPiece piece = (ConnectorPiece)pConnector.getUserObject();
                           if( piece != null )
                           {
                              pEdge = piece.getReturnEdge();
                           }
                        }
                     }
                  }
                  break;
   
               case IMessageKind.MK_RESULT:
                  pEdge = TypeConversions.getOwnerEdge( pETGraphObject );
                  break;
   
               default:
                  break;
            }
         }
      }

      return pEdge;
   }
   
   /**
    * Select all the edges associated with the product element, which is also an edge.
    *
    * @param pETGraphObject[in] The product element that must represent a TS edge
    */
   public static void selectAssociatedEdges( IETGraphObject etGraphObject )
   {
      if( etGraphObject != null )
      {
         TSEEdge edge = TypeConversions.getOwnerEdge( etGraphObject );
         if( edge != null )
         {
            TSConnector connector = edge.getTargetConnector();
            selectAssociatedEdges( connector );
         }
      }
   }

   /**
    * Select all the edges associated with the specified connector.
    *
    * @param pConnector[in]
    */
   protected static void selectAssociatedEdges( TSConnector connector )
   {
      if( connector != null )
      {
         ConnectorPiece piece = getPieceAttachedToConnector( connector );
         if( piece != null )
         {
            piece.selectAssociatedEdges();
         }
      }
   }

   /**
    * Select all the edges associated with this piece.
    */
   public void selectAssociatedEdges()
   {
      for( int iIndx=LifelineConnectorLocation.LCL_TOPLEFT; iIndx<=LifelineConnectorLocation.LCL_TOPRIGHT; iIndx++ )
      {
         //selectEdges( m_Connectors[iIndx], true, TSEdgeDirection.TS_OUT_EDGES, true );
      }

      for( int iIndx=LifelineConnectorLocation.LCL_BOTTOMRIGHT; iIndx<=LifelineConnectorLocation.LCL_BOTTOMLEFT; iIndx++ )
      {
         // Don't allow return messages to select their target piece's edges
         //selectEdges( m_Connectors[iIndx], false, TSEdgeDirection.TS_OUT_EDGES, true );
      }

      // Select all the edges for the children as well
      for (Iterator iter = m_ListPieces.iterator(); iter.hasNext();)
      {
         ParentPiece piece = (ParentPiece)iter.next();
         if (piece instanceof ConnectorPiece)
         {
            ConnectorPiece connectorPiece = (ConnectorPiece)piece;
            
            connectorPiece.selectAssociatedEdges();
         }         
      }
   }

   /**
    * Select all edges that are going in the specified direction from this connector.
    *
    * @param pConnector[in] The connector used to determine the out going edges to select
    * @param bSelectAssociatedPieces[in] When true the pieces on the target end of the edge are used to select more edges
    * @param edgeDirection[in] The direction the edges must be going from the connector to be selected
    * @param bInvalidateEdges[in] When true, the edges will be invalidated
    */
   protected static void selectEdges( TSConnector connector,
                                      boolean bSelectAssociatedPieces,
                                      int edgeDirection, /* TSEdgeDirection.TS_OUT_EDGES */
                                      boolean bInvalidateEdges /* true */ )
   {
      if( connector != null  && connector.degree() > 0)
      {
         List listEdges = null;
         
         // Select all the edges going in/out to/from this connector
         switch( edgeDirection )
         {/*
            case TSEdgeDirection.TS_OUT_EDGES:
            listEdges = connector.outEdges();
            break;
            */
            // TODO implement the TSEdgeDirection types
         }
         
         if( listEdges != null )
         {
            // iterate through the list of edges
				Iterator iter = listEdges.iterator();
           	while (iter.hasNext())
            {
            	// Make sure we don't get a path edge.
            	Object pObject = iter.next();
            	
               TSEEdge edge = pObject instanceof TSEEdge ? (TSEEdge) pObject : null;
               if (edge == null)
               {
               	continue;
               }
               
               // select the edge, to be deleted later
                edge.setSelected( true );

               if( bInvalidateEdges )
               {
                  // invalidate the edge so that the user can see what is to be deleted
                  IETGraphObject etGraphObject = TypeConversions.getETGraphObject( edge );
                  if( etGraphObject != null )
                  {
                      etGraphObject.invalidate() ;
                  }
               }

               if( bSelectAssociatedPieces )
               {
                  // Determine the target connector, and select its pieces
                  TSConnector targetConnector = edge.getTargetConnector();
                  if( targetConnector != null )
                  {
                     selectAssociatedEdges( targetConnector );
                  }
               }
            }
         }
      }
   }
   
   /**
    * Return the return edge which is connected to the bottom of this piece.
    */
   public TSEEdge getReturnEdge()
   {
      TSEEdge pEdge = null;

      // Search the bottom corners for any connectors
      TSConnector connector = null;
      for( int iIndx=LifelineConnectorLocation.LCL_BOTTOMRIGHT; 
           iIndx<=LifelineConnectorLocation.LCL_BOTTOMLEFT; iIndx++ )
      {
         if( m_Connectors[iIndx] != null )
         {
            connector = m_Connectors[iIndx];
            break;
         }
      }

      if( connector != null)
      {
          /* jyothi
         List outList = connector.outEdges();
         if( (outList != null ) && (outList.size() > 0))
         {
            long lCnt = outList.size();
            if( lCnt > 0 )
            {
               Debug.assertFalse( 1 == lCnt ); // the following code assumes the count is 1
               Object object = outList.get(0);
               
               // Fix J1427:  For some reason in the Java side it is possible to get here with
               //             a TSPEdge that has the TSEEdge as its owner.  This happens for example
               //             when the user deletes an operation from the project tree that is a
               //             label for a message on the SQD.
               {
                  if( ! (object instanceof TSEEdge ) &&
                        (object instanceof TSEdge) )
                  {
                     object = ((TSEdge)object).getOwner();
                  }
               }
               
               if (object instanceof TSEEdge)
               {
                  pEdge = (TSEEdge)object;
               }
            }
         }
           */
      }

      return pEdge;
   }
   
   /**
    * Creates a connector at the indicated location on this piece.
    *
    * @param lclCorner Corner of the piece where the new connector will be attached
    * @return The created connector
    */
   public TSConnector createConnector(int lcl)
   {
      TSConnector retVal = null;

      TSGraphObject gObject = getParent().getOwnerGraphObject();
      if (gObject instanceof TSDNode)
      {
         TSDNode node = (TSDNode)gObject;
         
         // Create and add the new connector to the node
         IETPoint pt = getConnectorTopOffset(lcl);
         if(pt != null)
         {
            TSConnector connector = node.addConnector();
            if (connector instanceof TSConnector)
            {
               retVal = connector;
               retVal.setProportionalYOffset(0.5);
               retVal.setConstantXOffset(pt.getX());
               retVal.setConstantYOffset(pt.getY());
               retVal.setVisible(false);
               
               attachConnector( retVal, lcl, true );               
            }
         }
      }

      return retVal;
   }

   /**
    * Returns the edge attached to this lifeline piece's corner.
    * 
    * @param lclCorner The corner that has the attached edge.  The valid 
    *                  values are one of the LifelineConnectorLocation values.
    * @return The attached edge.  If an edge is not attached to the specified
    *         corner then <code>null</code> will be returned.
    * @see LifelineConnectorLocation
    */
   public TSEdge getAttachedEdge(int lclCorner)
   {
      TSEdge retVal = null;

      TSConnector connector = getConnector(lclCorner);
      if(connector != null)
      {
         retVal = PresentationHelper.getConnectedEdge(connector, false);
      }

      return retVal;
   }
   
   /**
    * Finds the associated piece via the TS connector
    */
   public ConnectorPiece getAssociatedPiece(boolean invalidateEdge)
   {
      ConnectorPiece retVal = null;

      TSConnector connector = getAssociatedConnector(invalidateEdge);
      if(connector != null)
      {
         retVal = getPieceAttachedToConnector(connector);
      }

      return retVal;
   }
   
   /**
    * Finds the associated piece via the TS connector
    */
   ConnectorPiece getAssociatedPiece(  ) 
   {
      return getAssociatedPiece(false);
   }
   
   /**
    * Finds the associated piece via the TS connector
    */
   public static ConnectorPiece getAssociatedPiece( TSConnector connector, boolean invalidateEdge )
   {
      ConnectorPiece retVal = null;

      TSConnector assocConnector = getAssociatedConnector( connector, invalidateEdge );
      if(assocConnector != null)
      {
         retVal = getPieceAttachedToConnector( assocConnector );
      }

      return retVal;
   }

   /**
    * Finds the associated piece via the TS connector
    */
   public TSConnector getAssociatedConnector(boolean invalidateEdge)
   {
      TSConnector retVal = null;
      
      TSConnector foundConnector = null;
      for (int index = 0; index < m_Connectors.length; index++)
      {
         if(m_Connectors[index] != null)
         {
            foundConnector = m_Connectors[index];
            break;
         }
      }

      if(foundConnector != null)
      {
         retVal = getAssociatedConnector(foundConnector, invalidateEdge);
      }
      
      return retVal;
   }
   
   /**
    * Finds the associated piece via the TS connector
    */
   public static TSConnector getAssociatedConnector( TSConnector connector, boolean invalidateEdge )
   {        
      TSConnector retVal = null;
      
      if(connector != null)
      {
         retVal = PresentationHelper.getConnectorOnOtherEndOfEdge(connector, invalidateEdge);
      }
      
      return retVal;
   }

   /**
    * Archive functions
    */
   public IProductArchiveElement writeToArchive( IProductArchiveElement compartmentElement )
   {
      IProductArchiveElement pieceElement = super.writeToArchive( compartmentElement );
      if( pieceElement != null )
      {
         writeConnectorsToArchive( pieceElement );
      }
      
      return pieceElement;
   }

   /**
    * Archive functions
    */
   public void readFromArchive( IProductArchiveElement pieceElement )
   {
      super.readFromArchive( pieceElement );

      readConnectorsFromArchive( pieceElement );
   }

   /**
    * Attach the connectors determined during readConnectorsFromArchive().
    */
   public void attachConnectors()
   {
      IDiagram diagram = getDiagram();
      if( diagram != null )
      {
         for( int iIndx=LifelineConnectorLocation.LCL_TOPLEFT; iIndx<=LifelineConnectorLocation.LCL_BOTTOMLEFT; iIndx++ )
         {
            TSConnector connector = null;
            IPresentationElement presentationElement = null;

            final String strSourceEdgeID = m_strSourceEdgeIDs.get( iIndx );
            final String strTargetEdgeID = m_strTargetEdgeIDs.get( iIndx );
            if( strSourceEdgeID.length() > 0 )
            {
               presentationElement = diagram.findPresentationElement( strSourceEdgeID );
               if (presentationElement instanceof IEdgePresentation)
               {
                  IEdgePresentation edgePE = (IEdgePresentation)presentationElement;
                  
                  TSDEdge tsEdge = edgePE.getTSEdge();
                  if( tsEdge != null )
                  {
                     connector = tsEdge.getSourceConnector();
                  }
               }
            }
            else if( strTargetEdgeID.length() > 0 )
            {
               presentationElement = diagram.findPresentationElement( strTargetEdgeID );
               if (presentationElement instanceof IEdgePresentation)
               {
                  IEdgePresentation edgePE = (IEdgePresentation)presentationElement;
                  
                  TSDEdge tsEdge = edgePE.getTSEdge();
                  if( tsEdge != null )
                  {
                     connector = tsEdge.getTargetConnector();
                  }
               }
            }

            if( (presentationElement != null) &&
                (connector != null) )
            {
               // Fix J1149:  When reading from the archive,
               //             we do not want the connector to update their location.
               attachConnector( connector, iIndx, false );

               // We no longer want to set the user field to null
            }
         }
      }

      // Attach all the connectors for the children as well
      for (Iterator iter = m_ListPieces.iterator(); iter.hasNext();)
      {
         ParentPiece piece = (ParentPiece)iter.next();
         if (piece instanceof ConnectorPiece)
         {
            ConnectorPiece connectorPiece = (ConnectorPiece)piece;
            
            connectorPiece.attachConnectors();
         }
      }
   }
   
   /**
    * @return True when there is a message connected to either of the top corners.
    */
   public boolean containsMessagesOnTop()
   {
      boolean retVal = false;

      if((m_Connectors[LifelineConnectorLocation.LCL_TOPLEFT] != null) || 
         (m_Connectors[LifelineConnectorLocation.LCL_TOPRIGHT] != null))
      {
         retVal = true;
      }

      return retVal;
   }

   /**
    * @return True when there is a message connected to either of the bottom corners.
    */
   public boolean containsMessagesOnBottom()
   {
      boolean retVal = false;

      if((m_Connectors[LifelineConnectorLocation.LCL_BOTTOMLEFT] != null) || 
         (m_Connectors[LifelineConnectorLocation.LCL_BOTTOMRIGHT] != null))
      {
         retVal = true;
      }

      return retVal;
   }
    
   /**
    * Attach a connector to the piece at the specified location.
    *
    * @param pConnector The connector to attach to the piece
    * @param lclocation The specific location on the piece to attach connector
    * @param bMoveConnector (added for java) indicates that the connector's location
    *                       should be move appropriately.  The default behavior from should be true.
    *
    * @see ConnectorPiece#getPieceAttachedToConnector
    */
   public void attachConnector( TSConnector pConnector, int lpcCorner, boolean bMoveConnector )
   {

      if (pConnector != null)
      {
         if (m_Connectors[lpcCorner] == null)
         {
            m_Connectors[lpcCorner] = pConnector;

            // tell the connector the piece to which it is attached
            pConnector.setUserObject(this);
            
            if( bMoveConnector )
            {
               //
               // Move the connector now so that we don't have to rely on the draw to update its location
               IETPoint ptOffset = getConnectorTopOffset(lpcCorner);
               pConnector.setProportionalYOffset(0.5);
               pConnector.setProportionalXOffset(0.0);
            
               pConnector.setConstantXOffset(ptOffset.getX());
               pConnector.setConstantYOffset(ptOffset.getY());
            }
         }
         else
         {
            // TODO send message to event logger.
         }

      }
   }

   public boolean containsMessageOnTop()
   {
      boolean retVal = false;

      if ((m_Connectors[LifelineConnectorLocation.LCL_TOPLEFT] != null) || (m_Connectors[LifelineConnectorLocation.LCL_TOPRIGHT] != null))
      {
         retVal = true;
      }

      return retVal;
   }

   public boolean containsMessageOnBottom()
   {
      boolean retVal = false;

      if ((m_Connectors[LifelineConnectorLocation.LCL_BOTTOMLEFT] != null) || (m_Connectors[LifelineConnectorLocation.LCL_BOTTOMRIGHT] != null))
      {
         retVal = true;
      }

      return retVal;
   }


   /**
    * Return the location of the input connector on this piece.
    * 
    * @param connector The reference connector.
    * @return The location of the connector.  Valid values are one of the 
    *         LifelineConnectorLocation values.  If the connector is not
    *         managed by the ConnectorPiece instance then 
    *         LifelineConnectorLocation.LCL_UNKNOWN is returned.
    * @see LifelineConnectorLocation
    */
   public int getLocation(TSConnector connector)
   {
      int retVal = LifelineConnectorLocation.LCL_UNKNOWN;

      double xCenter = connector.getCenterX();
      double yCenter = connector.getCenterY();
      
      for (int index = 0; index < m_Connectors.length; index++)
      {
         TSConnector curConnector = m_Connectors[index];
         if(curConnector != null)
         {
            if((xCenter == curConnector.getCenterX()) && 
               (yCenter == curConnector.getCenterY()))
            {
               retVal = index;
               break;
            }
         }
      }

      return retVal;
   }
   
   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelineCompartmentPiece#setY(long)
    */
   public void setY(long y)
   {
       if (this.getY() != (int)y)
       {
           super.setY(y);
           
           // Only update the connectors if y isn't the same.
           updateConnectors();
           updateChildrenConnectors();
       }
   }
   
   /**
    * Set the height of this piece.  All siblings below this piece will also
    * be adjusted.
    */
   public void setHeight(int height)
   {
      int delta = height - getHeight();
      
      super.setHeight(height);
      
      updateConnectors(LifelineConnectorLocation.LCL_BOTTOMRIGHT,
                       LifelineConnectorLocation.LCL_BOTTOMLEFT);
                       
      // We were updating children connectors here, but this was causing problems
      // when we had messages coming back to the activation bar.
      // Example problem:
      // 3 lifelines: sync msg 1-2, sync msg 2-3, syn msg from 3-1
      // Draw a sync msg just above the 1-2 msg, so that the 1-2 moves down.
      // The problem seen was that the result msg from 3-1 was slanted.
      
      if(delta > 0)
      {
         LifelineCompartmentPiece piece = getParentPiece();
         if (piece instanceof ParentPiece)
         {
            ParentPiece parentPiece = (ParentPiece)piece;
            parentPiece.bumpSiblingsBelow(this, true);
         }
      }
   }
   
   /**
    * Special function to indicate this piece's height changed because siblings 
    * have changed
    * 
    * @param bottom The new location of the bottom part of the piece.
    */
   public void grow(int bottom)
   {
      // We need to make sure we only grow each piece once
      // A problem occured on a State Farm SQD where we could not move a 
      // message a large distance      
      // Issue:  W10426
      
      RecursiveHelper helper = null;
      try
      {
         helper = new RecursiveHelper( "Grow" );
         if( RecursiveHelper.isOkToUsePiece( "Grow", this ) )
         {
            super.grow(bottom);
            updateConnectors(LifelineConnectorLocation.LCL_BOTTOMRIGHT, 
                             LifelineConnectorLocation.LCL_BOTTOMLEFT);
         }
      }
      finally
      {
         if( helper != null )
         {
            helper.done();
         } 
      }
   }
   /**
    * Updates the TS connectors moving them to their proper location within the 
    * parent node.
    * 
    * @see LifelineConnectorLocation
    */
   public void updateConnectors()
   {
      updateConnectors(LifelineConnectorLocation.LCL_TOPLEFT, 
                       LifelineConnectorLocation.LCL_BOTTOMLEFT);   
   }
   
   /**
    * Updates the TS connectors moving them to their proper location within the 
    * parent node.  The connectors set to the location of the pieces.
    * 
    * @param lclBegin The start corner to update.  Will be one of the values 
    *                 of LifelineConnectorLocation.
    * @param lclEnd The end corner to update.Will be one of the values 
    *               of LifelineConnectorLocation.
    * 
    * @see LifelineConnectorLocation
    */
   public void updateConnectors(int lclBegin, int lclEnd)
   {
       updateConnectors(LifelineConnectorLocation.LCL_TOPLEFT, 
                        LifelineConnectorLocation.LCL_BOTTOMLEFT,
                        0, 0);
   }
   
   /**
    * Updates the TS connectors moving them to their proper location within the 
    * parent node.  The connectors are offset by the xDelta, and yDelta 
    * values.  The additional offset is needed for when the top of a lifeline 
    * moves.  Since the entire node moves, the pieces are in the same relative
    * positions, however the connectors on the opposite end of the message
    * need to be updated.    
    * 
    * @param lclBegin The start corner to update.  Will be one of the values 
    *                 of LifelineConnectorLocation.
    * @param lclEnd The end corner to update.Will be one of the values 
    *               of LifelineConnectorLocation.
    * @param xDelta The x offset.
    * @param yDelta the y offset.
    * 
    * @see LifelineConnectorLocation
    */
   public void updateConnectors(int lclBegin, int lclEnd, int xDelta, int yDelta)
   {
      // Check if we actually have connectors.  This will happen when setting
      // the height in the constructor.
      if(m_Connectors != null && !m_bUpdatingConnectors)
      {
			m_bUpdatingConnectors = true;

         for (int index = lclBegin; index <= lclEnd; index++)
         {
            TSConnector curConnector = m_Connectors[index];
            if(curConnector != null)
            {
               double xOrigOffset = curConnector.getConstantXOffset();
               double yOrigOffset = curConnector.getConstantYOffset();

               IETPoint offset = getConnectorTopOffset(index);
               if(((offset.getY() + yDelta) != yOrigOffset) ||
                  ((offset.getX() + xDelta) != xOrigOffset))
               {
                  curConnector.setProportionalYOffset(0.5);
                  curConnector.setConstantXOffset((offset.getX() + xDelta));
                  curConnector.setConstantYOffset(offset.getY() + yDelta);

                  if(canUpdateAssociatedPiece(index) == true)
                  {
                     // UPDATE, if we get the tool working in CSmartDragTool::UpdateDraggingGraphObjects()
                     // where the invalidate does not have to be the whole diagram.
                     // Then we will have to invalidate the edge here.  So far
                     // there is no other reason to invalidate.

                     ConnectorPiece associatedPiece = getAssociatedPiece(curConnector, false);
                     if(associatedPiece != null)
                     {
                        if((index == LifelineConnectorLocation.LCL_TOPLEFT) ||
                           (index == LifelineConnectorLocation.LCL_TOPRIGHT))
                        {
                           associatedPiece.setLogicalTop(getLogicalTop());
                        }
                        else
                        {
                           associatedPiece.setLogicalBottom(getLogicalBottom());
                        }
                     }
                     else
                     {
                        // Since there is no associated piece, the assume is that
                        // the connectors are to a gate compartment
                        TSConnector assocConnector = getAssociatedConnector(false);
                        if(assocConnector != null)
                        {
                           IDrawEngine engine = TypeConversions.getDrawEngine(assocConnector);

                           ICompartment compartment = TypeConversions.getCompartment(assocConnector,
                                                                                     IConnectorsCompartment.class);

                           // If there is no piece, we must be on a create message
                           // So, we need the ILifelineNameCompatment
                           compartment = engine.findCompartmentByCompartmentID("LifelineNameCompartment");


                           if(compartment instanceof IConnectorsCompartment)
                           {
                              double origPosition = assocConnector.getCenterY();
                              
                              IConnectorsCompartment connectors = (IConnectorsCompartment)compartment;
                              connectors.updateConnectors(null);
                              
                              ParentPiece parent = (ParentPiece)getParentPiece();
                              
                              
                              IADLifelineCompartment lifelineComp = (IADLifelineCompartment) 
                                  engine.findCompartmentByCompartmentID("ADLifelineCompartment");
                              double delta = origPosition - assocConnector.getCenterY();
                              lifelineComp.movingCreate((int)delta);
                              
                              if(origPosition >= assocConnector.getCenterY())
                              {
                                  parent.bumpSiblingsBelow(this, true);
                              }
                              else
                              {
                                  parent.bumpSiblingsAbove(this);   
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      m_bUpdatingConnectors = false;

   }
   
   /**
    * Updates the TS connectors moving them to their proper location within the parent node
    * This routine is only used during a resize, so associated pieces are not updated.
    */
   public void updateConnectorsViaTopCenter()
   {
      RecursiveHelper helper = null;
      try
      {
         helper = new RecursiveHelper( "UpdateConnectorsViaTopCenter" );
         if( RecursiveHelper.isOkToUsePiece( "UpdateConnectorsViaTopCenter", this ) )
         {
            for(int index = LifelineConnectorLocation.LCL_TOPLEFT; index <= LifelineConnectorLocation.LCL_BOTTOMLEFT; index++)
            {
               TSConnector curConnector = getConnector(index);
               if(curConnector != null)
               {
                  // Move the connector now so that we don't have to rely on the 
                  // draw to update its location
                  IETPoint ptOffset = getConnectorTopOffset(index);
                  curConnector.setProportionalYOffset(0.5);
                  curConnector.setProportionalXOffset(0);
               
                  curConnector.setConstantXOffset(ptOffset.getX());
                  curConnector.setConstantYOffset(ptOffset.getY());
               }
            }
         
            ETList < ParentPiece > pieces = getPieces();
            for (Iterator < ParentPiece > iter = pieces.iterator(); iter.hasNext();)
            {
               ParentPiece curPiece = iter.next();
               if (curPiece instanceof ConnectorPiece)
               {
                  ConnectorPiece connector = (ConnectorPiece)curPiece;
                  connector.updateConnectorsViaTopCenter();
               }
            }
         }
      }
      finally
      {
         if( helper != null )
         {
            helper.done();
         } 
      }
   }
   
   /**
    * Updates the TS connectors moving them to their proper location within the parent node
    * This routine is only used during a resize, so associated pieces are not updated.
    */
   public void updateConnectorsViaBottomCenter()
   {
      RecursiveHelper helper = null;
      try
      {
         helper = new RecursiveHelper( "UpdateConnectorsViaBottomCenter" );
         if( RecursiveHelper.isOkToUsePiece( "UpdateConnectorsViaBottomCenter", this ) )
         {
            for(int index = LifelineConnectorLocation.LCL_TOPLEFT; index <= LifelineConnectorLocation.LCL_BOTTOMLEFT; index++)
            {
               TSConnector curConnector = getConnector(index);
               if(curConnector != null)
               {
                  // Move the connector now so that we don't have to rely on the 
                  // draw to update its location
                  IETPoint ptOffset = getConnectorBottomOffset(index);
                  curConnector.setProportionalYOffset(-0.5);
                  curConnector.setProportionalXOffset(0);
               
                  curConnector.setConstantXOffset(ptOffset.getX());
                  curConnector.setConstantYOffset(ptOffset.getY());
               }
            }
         
            ETList < ParentPiece > pieces = getPieces();
            for (Iterator < ParentPiece > iter = pieces.iterator(); iter.hasNext();)
            {
               ParentPiece curPiece = iter.next();
               if (curPiece instanceof ConnectorPiece)
               {
                  ConnectorPiece connector = (ConnectorPiece)curPiece;
                  connector.updateConnectorsViaTopCenter();
               }
            }
         }
      }
      finally
      {
         if( helper != null )
         {
            helper.done();
         } 
      }
   }
   
   /**
    * Updates the TS connectors of all the children
    */
   public void updateChildrenConnectors()
   {
      // Attach all the connectors for the children as well
      ETList < ParentPiece > pieces = getPieces();
      for (Iterator < ParentPiece > iter = pieces.iterator(); iter.hasNext();)
      {
         ParentPiece curPiece = iter.next();
         if (curPiece instanceof ConnectorPiece)
         {
            ConnectorPiece cPiece = (ConnectorPiece)curPiece;
            
            cPiece.updateConnectors();
            cPiece.updateChildrenConnectors();
         }
      }
   }
   
   /**
    * Validate the lifeline and all its child pieces
    */
   public boolean validate()
   {
      // Ensure that the y value is not too small
      super.setY( getY() );   
      
      for( int indx = LifelineConnectorLocation.LCL_TOPLEFT; 
               indx <= LifelineConnectorLocation.LCL_BOTTOMLEFT; indx++ )
      {
         if( m_Connectors[indx] != null )
         {
            // Determine if there is an edge connected to this connector
            TSEdge edge = PresentationHelper.getConnectedEdge( m_Connectors[indx], false);
            if( edge == null )
            {
               IDrawEngine drawEngine = getDrawEngine();
               if (drawEngine instanceof INodeDrawEngine)
               {
                  INodeDrawEngine nodeEngine = (INodeDrawEngine)drawEngine;
               
                  TSENode node = nodeEngine.getNode( );
                  if( node != null )
                  {
                     try
                     {
                        node.remove( m_Connectors[indx]);
                     }
                     catch(RuntimeException exception)
                     {
                        // Tom Sawyer will throw a RuntimeException if the 
                        // node does not actually contain the connector.
                        // This can happen during a delete i.e. The connector
                        // has already been removed from the node before the
                        // validate was called.
                     }
                  }
               }

               m_Connectors[indx] = null;
            }
         }
      }
   
      // Why do we need to update connectors here?
      // TEST updateConnectors();
   
      boolean hasChildren = super.validate();
   
      return (hasChildren || hasMessagesAttached());
   }
   
   /**
    * Delete an edge, its connectors, and any pieces attached to the target of
    * the edge.
    *
    * @param edge The product element that must represent a TS edge to be 
    *             deleted.
    */
   public static void deleteEdge(IETGraphObject edge)
   {
      if(edge != null)
      {
         TSEdge tsEdge = TypeConversions.getOwnerEdge(edge);
         deleteEdge(tsEdge);
      }
   }
   
   /**
    * Delete an edge, its connectors, and any pieces attached to the target of 
    * the edge.
    *
    * @param pEdge The TS edge to be deleted
    */
   public static void deleteEdge(TSEdge edge)
   {
      if( edge instanceof TSDEdge )
      {
         TSDEdge connectorEdge = (TSDEdge)edge;
         
         // Delete both the source and target pieces
         TSConnector connector = connectorEdge.getSourceConnector();
         deletePieceAttachedToConnector( connector );         
         deleteConnectorFromNode( connector );

         connector = connectorEdge.getTargetConnector();
         deletePieceAttachedToConnector( connector );         
         deleteConnectorFromNode( connector );
      }
   }

   /**
    * Find all the messages from this piece and all its associated messages.
    */
   public void getPropagatedMessages( ETList< IMessage > messages )
   {
      if( null == messages )  throw new IllegalArgumentException();
      
      TSEEdge topEdge = findTopEdge();
      if( topEdge != null )
      {
         IElement element = TypeConversions.getElement( topEdge );
         if( element instanceof IMessage )
         {
             messages.add( (IMessage) element );
         }
      }

      // The activation bar and suspension area classes get the rest of the messagess
   }
   
   //**************************************************
   // Helper Methods
   //**************************************************

   /**
    * Deletes the connector from its owner node.
    *
    * @param connector The connector to be deleted from its owner node
    */
   protected static void deleteConnectorFromNode(TSConnector connector)
   {
      if( connector != null )
      {
         TSGraphObject gObject = connector.getOwner();
         if (gObject instanceof TSENode)
         {
            TSENode node = (TSENode)gObject;
            if ((node != null) && (connector.getOwner() == node))
            {
               try
               {
                  node.remove(connector);
               }
               catch(RuntimeException exception)
               {
                  // Tom Sawyer will throw a RuntimeException if the 
                  // node does not actually contain the connector.
                  // This can happen during a delete i.e. The connector
                  // has already been removed from the node before the
                  // validate was called.
                  //
                  // We should never get here because of the owner check.
                  Debug.assertFalse( false, exception.getLocalizedMessage());
               }
            }
         }
      } 
   }

   /**
    * Delete the piece associated with the connector.
    * 
    * @param connector The connector that is about to be deleted.
    */
   protected static void deletePieceAttachedToConnector(TSConnector connector)
   {
      if( connector != null )
      {
         Object userObject = connector.getUserObject();
         if(userObject instanceof LifelineCompartmentPiece)
         {
            LifelineCompartmentPiece piece = (LifelineCompartmentPiece)userObject;
            if( piece != null )
            {
               // Fix W4003:  Just post a validate message to clean up the lifeline
               piece.getParent().postValidateNode();
            }
         }
      }
   }

   /**
    * Determines if there are any messages attached to the piece.
    *
    * @return <code>true</code> indicating that messages are attached.
    */
   protected boolean hasMessagesAttached()
   {
      int hasMessagesAttached = MLF_NONE;

      int[] convertIndxToMLF = { MLF_TOPLEFT, MLF_TOPRIGHT, MLF_BOTTOMRIGHT, MLF_BOTTOMLEFT };
   
      for( int iIndx = 0; iIndx < ACTIVATION_CORNER_COUNT; iIndx++ )
      {
         if( m_Connectors[iIndx] != null )
         {
            hasMessagesAttached |= convertIndxToMLF[iIndx];
         }
      }
   
      return hasMessagesAttached != MLF_NONE;
   }

   /**
    * Finds the top most edge on this piece or any of its children
    */
   public TSEEdge findTopEdge()
   {
      // Search the bottom corners for any connectors
      TSConnector connector = null;
      for( int iIndx=LifelineConnectorLocation.LCL_TOPLEFT; iIndx<=LifelineConnectorLocation.LCL_TOPRIGHT; iIndx++ )
      {
         if( m_Connectors[iIndx] != null )
         {
            connector = m_Connectors[iIndx];
            break;
         }
      }

      return getConnectorsEdge( connector );
   }

	/*
	 * Returns true if the connector has edges connected to it, in or out.
	 */
	private boolean isConnected(TSConnector originalConnector)
	{
		return originalConnector != null && originalConnector.degree() > 0;
	}
	
   /**
    * Move all the edges from the original connector to a new connector on this piece
    * 
    * @param originalConnector containing possibly edges
    * @param lclCorner LifelineConnectorLocation to which the edges are to be connected
    * @return true when edges were reconnected
    */
   protected boolean moveConnectorEdges( TSConnector originalConnector, int lclCorner )
   {
      // originalConnector is allowed to be null
      
      boolean bEdgesReconnected = false;

		if (!isConnected(originalConnector))
			return false;
			
      if( originalConnector != null )
      {
         // For the following code there should be only one edge connected to the connector
         // So, we process accordingly
         TSConnector connector = null;
         TSDEdge tsEdge = null;
         TSNode oldNode = null;
         TSNode newNode = null;

//         List inEdgeList = originalConnector.inEdges();
         
         // (TS 7/12/05) Tom Sawyer's API has changed between 5.x and 6.0.  They have removed
         // the inEdges and the outEdges and added the incidentEdges methods.
         // The incidentEdges will return all of the edges connected to the connector.
         List inEdgeList = originalConnector.incidentEdges();
         if (inEdgeList.size() > 0)
         {
             // Debug.assertFalse(inEdgeList.size() == 1, "The following code assumes the count is 1");
             
             // Only create the new connector when there is an edge associated with the old connector
             connector = createConnector( lclCorner );
             if( connector != null )
             {
                 newNode = (TSNode)connector.getOwner();
                 
                 if (inEdgeList.get(0) instanceof TSDEdge)
                 {
                     // (TS 4/1/05) The below code now returns the graph that ownes the edge.  
                     // They use to have two constructs one for layout and the one everyone
                     // used.  Now there is only one edge type.
//                     TSGraphObject gObject = ((TSEdge)inEdgeList.get(0)).getOwner();
//                     if (gObject instanceof TSDEdge)
                     {
//                         tsEdge = (TSDEdge)gObject;    
                         tsEdge = (TSDEdge)inEdgeList.get(0);
                         if(originalConnector.equals(tsEdge.getTargetConnector()) == true)
                         {
                             oldNode = tsEdge.getTargetNode();                         
                             tsEdge.setTargetConnector( connector );
                         }
                         else
                         {
                             oldNode = tsEdge.getSourceNode();
                             tsEdge.setSourceNode(newNode);
                             tsEdge.setSourceConnector( connector );
                         }
                     }
                 }
             }
         }
//         else if (originalConnector.outEdges().size() > 0)
//         {
//             connector = createConnector( lclCorner );
//             if( connector != null )
//             {
//                 newNode = (TSNode)connector.getOwner();
//                 
//                 if (originalConnector.outEdges().get(0) instanceof TSEdge)
//                 {
//                     TSEdge pEdge = (TSEdge)originalConnector.outEdges().get(0);
//                     TSGraphObject gObject = ((TSEdge)originalConnector.outEdges().get(0)).getOwner();
//                     if (gObject instanceof TSDEdge)
//                     {
//                         tsEdge = (TSDEdge)gObject;
//                         oldNode = pEdge.getSourceNode();
//                         tsEdge.setSourceNode(newNode);
//                         tsEdge.setSourceConnector( connector );
//                     }
//                 }
//             }
//         }

         if( (connector != null) &&
             (tsEdge != null) &&
             (oldNode != null) &&
             (newNode != null) )
         {
            // Now that the TS information is all updated, process the meta data
            IEdgePresentation edgePE = TypeConversions.getEdgePresentation( tsEdge );
            if( edgePE != null )
            {
               IReconnectEdgeContext context = new ReconnectEdgeContext();

               context.setPreConnectNode( TypeConversions.getETNode( oldNode ));
               context.setProposedEndNode( TypeConversions.getETNode( newNode ));

               edgePE.reconnectLink( context );
               
               bEdgesReconnected = true;
            }
         }
      }
      
      return bEdgesReconnected;
   }

   /**
    * Returns the point where a connector should be attached, for the specified location on the piece.
    * This offset is calculated from the top center of the TS node in TS absolute logical units.
    *
    * @param lclocation[in] location to calculate offset from
    *
    * @return 
    */
   protected IETPoint getConnectorTopOffset( int lclocation )
   {
      IETRect rectEngine = getEngineLogicalBoundingRect();
      double horzCenter = rectEngine.getCenterX();
      IETRect rectDraw = getDrawRect(1.0);

      ETPoint ptOffset = new ETPoint(0, 0);
      switch( lclocation )
      {
         case LifelineConnectorLocation.LCL_TOPLEFT:         
            ptOffset.setX((int)(rectDraw.getLeft() - horzCenter));
            ptOffset.setY(-rectDraw.getTop());
            break;
   
         case LifelineConnectorLocation.LCL_TOPRIGHT:
            ptOffset.setX((int)(rectDraw.getRight() - horzCenter));
            ptOffset.setY(-rectDraw.getTop());
            break;
   
         case LifelineConnectorLocation.LCL_BOTTOMRIGHT:
            ptOffset.setX((int)(rectDraw.getRight() - horzCenter));
            ptOffset.setY(-(rectDraw.getIntY() + rectDraw.getIntHeight()));
            break;
   
         case LifelineConnectorLocation.LCL_BOTTOMLEFT:
            ptOffset.setX((int)(rectDraw.getLeft() - horzCenter));
            ptOffset.setY(-(rectDraw.getIntY() + rectDraw.getIntHeight()));
            break;
   
         default:
            // did we add another location?
            break;
      }

      return ptOffset;
   }
   
   /**
    * Returns the point where a connector should be attached, for the specified location on the piece.
    * This offset is calculated from the bottom center of the TS node in TS absolute logical units.
    *
    * @param lclocation location to calculate offset from
    * @return The connecotr.
    *
    * @see CConnectorPiece::GetConnectorTopOffset()
    */
   protected IETPoint getConnectorBottomOffset( int lclocation )
   {
      IETPoint retVal = getConnectorTopOffset(lclocation);
      IETRect rectEngine = getEngineLogicalBoundingRect();
      retVal.setY( rectEngine.getIntHeight() - (-retVal.getY()) );      

      return retVal;
   }
   
   /**
    * Retreieves the connector at a specified location.  The location of the 
    * connector is one of the LifelineConnectorLocation values.
    * 
    * @param location The location of the connector to retrieve.
    * @return The connector at the location or <code>null</code> if a connector
    *         is not at the location.
    * 
    * @see LifelineConnectorLocation
    */
   public TSConnector getConnector(int location)
   {
      return m_Connectors[location];  
   }   

   /**
    * Save the information about the connectors to the archive.
    */
   protected void writeConnectorsToArchive( IProductArchiveElement parentElement )
   {
      if( parentElement != null )
      {
         IProductArchiveElement connectorListElement = null;

         for( int iIndx=LifelineConnectorLocation.LCL_TOPLEFT; iIndx<=LifelineConnectorLocation.LCL_BOTTOMLEFT; iIndx++ )
         {
            if( m_Connectors[iIndx] != null )
            {
               // Determine if there is an edge connected to this connector
               TSDEdge tsEdge = PresentationHelper.getConnectedEdge( m_Connectors[iIndx], false );
               if( tsEdge != null )
               {
                  IPresentationElement pe = TypeConversions.getPresentationElement( tsEdge );
                  if( pe != null )
                  {
                     String strXMIID = pe.getXMIID();
                     if( strXMIID.length() > 0 )
                     {
                        if( null == connectorListElement )
                        {
                           connectorListElement = parentElement.createElement( IProductArchiveDefinitions.ADLIFELINECONNECTORLIST_STRING );
                        }

                        if( connectorListElement != null )
                        {
                           IProductArchiveElement connectorElement = 
                              connectorListElement.createElement( IProductArchiveDefinitions.ADLIFELINECONNECTOR_STRING );
                           if( connectorElement != null )
                           {
                              connectorElement.addAttributeLong(
                                 IProductArchiveDefinitions.ADLIFELINECONNECTOR_LOCATION_STRING, iIndx );

                              TSConnector source = tsEdge.getSourceConnector();
                              final String strEnd = TypeConversions.areSameTSObjects( m_Connectors[iIndx], source )
                                 ? IProductArchiveDefinitions.ADLIFELINECONNECTOR_SOURCE_STRING
                                 : IProductArchiveDefinitions.ADLIFELINECONNECTOR_TARGET_STRING;

                              connectorElement.addAttributeString( strEnd, strXMIID );
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   /**
    * Retrieve the information about the connectors from the archive.
    *
    * @param parentElement
    */
   protected void readConnectorsFromArchive( IProductArchiveElement parentElement )
   {
      if( parentElement != null )
      {
         IProductArchiveElement compartmentListElement =
            parentElement.getElement( IProductArchiveDefinitions.ADLIFELINECONNECTORLIST_STRING );
         if( compartmentListElement != null )
         {
            IProductArchiveElement[] compartmentElements = compartmentListElement.getElements();
            int nCnt = compartmentElements.length;
            for ( int nIndx=0; nIndx<nCnt; nIndx++ )
            {
               IProductArchiveElement element = compartmentElements[nIndx];
               
               if( element != null )
               {
                  int location = (int)element.getAttributeLong( IProductArchiveDefinitions.ADLIFELINECONNECTOR_LOCATION_STRING );
                  if( (location >= LifelineConnectorLocation.LCL_TOPLEFT) && (location <= LifelineConnectorLocation.LCL_BOTTOMLEFT) )
                  {
                     String strSource = element.getAttributeString( IProductArchiveDefinitions.ADLIFELINECONNECTOR_SOURCE_STRING );
                     if( strSource.length() > 0 )
                     {
                        m_strSourceEdgeIDs.set( location, strSource );
                     }
                     else
                     {
                        String strTarget = element.getAttributeString( IProductArchiveDefinitions.ADLIFELINECONNECTOR_TARGET_STRING );
                        if( strTarget.length() > 0 )
                        {
                           m_strTargetEdgeIDs.set( location, strTarget );
                        }
                     }
                  }
               }
            }
         }
      }
   }

   /**
    * Return the edge associated with this connector
    */
   protected TSEEdge getConnectorsEdge( TSConnector connector )
   {
      TSEEdge edge = null;

      // I (BDB) was not able to determine how dot use buildInOutEdges()
      TSDEdge tsdEdge = PresentationHelper.getConnectedEdge( connector, false );
      if (tsdEdge instanceof TSEEdge)
      {
         edge = (TSEEdge)tsdEdge;
      }
      
      return edge;
   }
   
   protected int getBottomBound()
   {
       int retVal = super.getBottomBound();
       
       //getDrawTop
       if((getHeight() == 0) &&
          (getLifelinePiecesKind() == LifelinePiecesKind.LPK_SUSPENSION))
       {
           TSConnector connector = getAssociatedConnector(false);
           if((connector !=  null) && 
               (getPieceAttachedToConnector(connector) == null))
           {
               // Since we do not have an associated piece, the assumption is 
               // that we are connected to a gate.  Therefore you the draw engine
               // head instead.
               retVal = getDETop();
           }
       }
       
       return retVal;
   }
   
   public void moveConnectorsBy(int delta, boolean updateAssociatedPieces)
   {
       
       updateConnectors(LifelineConnectorLocation.LCL_TOPLEFT,
                    LifelineConnectorLocation.LCL_BOTTOMLEFT,
                    0, delta);
       
       for(ParentPiece child : getPieces())
       {
          if(child instanceof ConnectorPiece)
          {
              ConnectorPiece connectorPiece = (ConnectorPiece)child;
              connectorPiece.moveConnectorsBy(delta, updateAssociatedPieces);
          }
       }
   }
}
