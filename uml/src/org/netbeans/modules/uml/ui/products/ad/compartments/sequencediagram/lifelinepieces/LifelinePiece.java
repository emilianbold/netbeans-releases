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

import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.ETLifelineCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IADLifelineCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.LifelineConnectorLocation;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.graph.TSEdge;

/**
 * Lifeline Piece is helper class that provides convience methods used to 
 * manipulate connector pieces.
 * 
 * @author Trey Spiva
 */
public class LifelinePiece
{
   private ConnectorPiece m_Piece = null;
   private TSConnector   m_AssociatedConnector = null;
   
   /**
    * Initializes the LifelinePiece instance.  The default constructor is 
    * protected and therefore can only be used by LifelinePieces.
    */
   protected LifelinePiece()
   {
   }
   
   /**
    * Initialze the lifeline piece with a connector piece.
    * 
    * @param piece The piece that the LifelinePiece will reference.
    */
   public LifelinePiece(ConnectorPiece piece)
   {
      setPiece(piece);
   }
   
   /**
    * Initializes the LifelinePiece by specifing the connector that is 
    * manipulated.  The associated ConnectorPiece is retrieved from the
    * connector.
    * 
    * @param connector The connector to use to retrieve the ConnectorPiece.
    */
   public LifelinePiece(TSConnector connector)
   {      
      ConnectorPiece piece = ConnectorPiece.getPieceAttachedToConnector(connector);
      setPiece(piece);
      setAssociatedConnector(connector);  
   }
   
   /**
    * Retrieves the location of the connector that is associated with the 
    * LifelinePiece instance.
    * 
    * @return The location of the connector.  The value will be one of the
    *         LifelineConnectorLocation values.
    * 
    * @see LifelineConnectorLocation
    */
   public int getConnectorLocation()
   {
      return getConnectorLocation(getAssociatedConnector());
   }
   
   public int getConnectorLocation(TSConnector connector)
   {     
      int retVal = LifelineConnectorLocation.LCL_UNKNOWN;

      if (connector instanceof TSConnector)
      {
         TSConnector tsConnector = (TSConnector)connector;
         ConnectorPiece piece = getPiece();
         if (piece != null)
         {
            retVal = piece.getLocation(tsConnector);
         }
      }

      return retVal;
   }
   
   /**
    * Indicates wether this interface is associated with a valid lifeline piece
    * 
    * @return <code>true</code> if the piece is valid.
    */
   public boolean isValid()
   {
      return getPiece() != null;
   }
   
   /**
    * Get the diagram logical location for the top of the piece.
    *
    * @return The vertical location for the top of the piece on the diagram
    */
   public int getLogicalTop()
   {
      int retVal = 0;

      ConnectorPiece piece = getPiece();
      if(piece != null)
      {
         ETLifelineCompartment compartment = piece.getParent();
         if(compartment != null)
         {
            IETRect rect = compartment.getLogicalBoundingRect();
            retVal = rect.getTop() - piece.getTop();
         }
      }

      return retVal;
   }
   
   public void setLogicalTop(int top)
   {
      ConnectorPiece piece = getPiece();
      if(piece != null)
      {
         piece.setLogicalTop(top);  
      }
   }
   
   public int getLogicalBottom()
   {
      int retVal = 0;

      ConnectorPiece piece = getPiece();
      if(piece != null)
      {
         retVal = piece.getLogicalBottom();
      }

      return retVal;
   }
   
   public void setLogicalBottom(int value)
   {
      ConnectorPiece piece = getPiece();
      if(piece != null)
      {
         piece.setLogicalBottom(value);  
      }
   }
   
   public TSConnector createConnector(int lclCorner)
   {
      TSConnector retVal = null;

      ConnectorPiece piece = getPiece();
      if(piece != null)
      {
         retVal = piece.createConnector(lclCorner);
      }

      return retVal;
   }
   
   public void attachConnector( int lclCorner, TSConnector connector )
   {   
      ConnectorPiece piece = getPiece();
      if (piece instanceof ActivationBar)
      {
         piece.attachConnector(connector, lclCorner, true);
      }                          
   }
   
   public boolean isPartOfMessageToSelf()
   {
      boolean retVal = false;

      ConnectorPiece piece = getPiece();
      if (piece instanceof ActivationBar)
      {
         ActivationBar bar = (ActivationBar)piece;
         retVal = bar.isMessageToSelf();
      }

      return retVal;
   }
   
   public void setIsPartOfMessageToSelf(boolean value)
   {
      ConnectorPiece piece = getPiece();
      if (piece instanceof ActivationBar)
      {
         ActivationBar bar = (ActivationBar)piece;
         bar.setMessageToSelf(value);
      }
   }
   
   /**
    * Returns the LifelinePiece that is associated with this piece.
    * 
    * @return The LifelinePiece that represents the associated Piece.  
    *         <code>null</code> may be returned if 
    */
   public LifelinePiece getAssociatedPiece()
   {
      LifelinePiece retVal = new LifelinePiece();

      ConnectorPiece piece = getPiece();
      if(piece != null)
      {
         ConnectorPiece associatedPiece = piece.getAssociatedPiece(false);
         if(associatedPiece != null)
         {
            retVal.setPiece(associatedPiece);
         }
      }

      return retVal;
   }
   
   /**
    * Returns the LifelinePiece that is this piece's parent.
    *
    * @return The parent piece LifelinePiece instance.
    */
   public LifelinePiece getParentPiece()
   {
      LifelinePiece retVal = null;

      ConnectorPiece piece = getPiece();
      if(piece != null)
      {
         LifelineCompartmentPiece parentPiece = piece.getParentPiece();
         if (parentPiece instanceof ConnectorPiece)
         {
            retVal = new LifelinePiece((ConnectorPiece)parentPiece);
         }
      }

      return retVal;
   }
   
   /**
    * Returns the compartment that contains this lifeline piece
    */
   public IADLifelineCompartment getParentCompartment()
   {
      IADLifelineCompartment retVal = null;

      ConnectorPiece piece = getPiece();
      if(piece != null)
      {
         retVal = piece.getParent();
      }

      return retVal;
   }
   
   /**
    * Returns the edge attached to this lifeline piece's corner
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

      ConnectorPiece piece = getPiece();
      if(piece != null)
      {
         retVal = piece.getAttachedEdge(lclCorner);
      }

      return retVal;
   }
   
   //**************************************************
   // Helper Methods
   //**************************************************
   
   /**
    * Retieves the connector piece associated with the lifeline piece.
    *  
    * @return The connector piece.
    */
   protected ConnectorPiece getPiece()
   {
      return m_Piece;
   }

   /**
    * Sets the connector piece associated with the lifeline piece.
    *  
    * @param piece The connector piece.
    */
   protected void setPiece(ConnectorPiece piece)
   {
      m_Piece = piece;
   }

   /**
    * Retrieves the connector that was used to initalize the LifelinePiece.
    * If the LifelinePiece was not initialize via a connector then null will
    * be returned.
    * 
    * @return The associated connector.
    */
   public TSConnector getAssociatedConnector()
   {
      return m_AssociatedConnector;
   }

   /**
    * Sets the connector is associated with the Lifeline Connector.
    * 
    * @param connector The connector to assocate with the LifelinePiece.
    */
   public void setAssociatedConnector(TSConnector connector)
   {
      m_AssociatedConnector = connector;
   }

   /**
    * Makes a copy of the piece on the input lifeline
    *
    * @param pCompartment [in] The compartment where a new piece is created using this piece as a model
    * @param openConnector [in] The new connector that has no edges attached.
    *
    * @return void
    */
   public TSConnector copyTo( ETLifelineCompartment compartment )
   {
      if( null == compartment ) throw new IllegalArgumentException();
      if( null == m_Piece )   throw new IllegalStateException();

      TSConnector openConnector = null;

      // Fix W10441:  The problem here was create sync 1-2, nest sync 2-1, then move the arrow head
      //              from 2.3, and then back from 3.2.  The result message would cross over the
      //              original nested messages.
      //              The solution is to inform the create code that this is a finish piece, so
      //              that the proper parenting logic is kicking in.
      // UPDATE:  we should probabaly have the activation bar remember what type it is.

      ConnectorPiece connectorPiece = null;

      int lpk = m_Piece.getLifelinePiecesKind();
      // we should not be finding a piece, but always creating a new one

      connectorPiece = (ConnectorPiece)compartment.createElement( m_Piece.getLogicalTop(), lpk );

      if( connectorPiece != null )
      {
         m_Piece.removeSelfFromStack();
         openConnector = connectorPiece.copy( m_Piece );

         m_Piece = null;
      }
      
      return openConnector;
   }

}
