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

import java.awt.Color;
import java.awt.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.ETLifelineCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.LifelineConnectorLocation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PresentationHelper;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.drawing.TSDEdge;
import com.tomsawyer.drawing.TSPEdge;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;

/**
 * 
 * @author Trey Spiva
 */
public class ActivationBar extends ConnectorPiece
{
   public static final Color ACTIVATION_BAR_COLOR = new Color(72, 167, 244);
   
   public static final Color COLOR_MESSAGE_SELF = Color.black;
   
   public static final int   SELF_BEND_CNT      = 3;
   
   private boolean m_IsMessageToSelf = false;
//   private boolean m_UpdateRelexiveBends = false;
   private boolean m_AllowAllUpdatesOfAssocitedPieces = false;
   
   /**
    * @param parent
    * @param parentPiece
    * @param topLeft
    * @param height
    */
   public ActivationBar(ETLifelineCompartment parent, 
                        LifelineCompartmentPiece parentPiece, 
                        IETPoint topLeft, 
                        int height)
   {
      super(parent, parentPiece, topLeft, height);
   }

   /**
    * Move the piece by the specified amount, positive moves the piece down.
    */
   public void moveBy(int delta)
   {
      m_AllowAllUpdatesOfAssocitedPieces = true;
      
      super.moveBy(delta);
      
      m_AllowAllUpdatesOfAssocitedPieces = false;
   }
   
   /**
    * Returns the lifeline pieces kind for this piece.
    */
   public int getLifelinePiecesKind()
   {
      return LifelinePiecesKind.LPK_ACTIVATION;
   }
   
   /**
    * Create a new piece at the specified location.
    * 
    * @param kind The type of piece to create.  Must be one of the 
    *             LifelinePiecesKind values.
    * @param y The y location of the piece.
    * @param height The height of the piece.
    */
   public ParentPiece createNewPiece(int kind, int y, int height)
   {
      ParentPiece retVal = null;
      
      IETPoint topLeft = new ETPoint(getLeft() + getChildOffset(), y);
      
      switch(kind)
      {
         case LifelinePiecesKind.LPK_ACTIVATION:
         case LifelinePiecesKind.LPK_ACTIVATION_FINISH:
            ParentPiece above = getPieceAbove(y);
            if(above == null)
            {
               // The gate connectors were changing the piece,
               // so for now move the SetTo() so it only happens when the 
               // piece is a pure activation
               if(kind == LifelinePiecesKind.LPK_ACTIVATION)
               {
                  setTop(getTop() + y);
               }
               retVal = this;
            }
            break;
            
         case LifelinePiecesKind.LPK_SUSPENSION:
            retVal = new SuspensionArea(getParent(), this, topLeft, height); 
            break;
            
         case LifelinePiecesKind.LPK_ATOMIC_FRAGMENT:
            retVal = new SuspensionArea(getParent(), this, topLeft, 0);
            break;
            
         default: 
            break;                                         
      }
      
      if((retVal != null) && 
         ((kind != LifelinePiecesKind.LPK_ACTIVATION) && 
         (kind != LifelinePiecesKind.LPK_ACTIVATION_FINISH)))
      {
         insertPiece(retVal);
      }
      
      return retVal;
   }

   /**
    * Find all the messages from this piece and all its associated messages.
    */
   public void getPropagatedMessages( ETList< IMessage > messages )
   {
      super.getPropagatedMessages( messages );

      // Attach all the messages for the children as well
      for (Iterator iter = m_ListPieces.iterator(); iter.hasNext();)
      {
         ParentPiece parentPiece = (ParentPiece)iter.next();
         if (parentPiece instanceof ConnectorPiece)
         {
            ConnectorPiece piece = (ConnectorPiece)parentPiece;
            
            piece.getPropagatedMessages( messages );
         }
      }
   }
   
   /**
    * Set the height of the lifeline piece.  The reflexive bends will also
    * be updated  if the activation bar is part of a message to self.
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.lifelinepieces.LifelineCompartmentPiece#setHeight(int)
    */
   public void setHeight(int height)
   {
      super.setHeight(height);
      postUpdateReflexiveBends();
      
      // During CDFS and decorations the bottom of an activation bar wont have
      // the connectors attached yet.  So we have to force the
      // associated piece to update.
      if( containsMessagesOnBottom() == false)
      {  
         ConnectorPiece piece = getAssociatedPiece();
         if (piece instanceof SuspensionArea)
         {
            SuspensionArea suspensionArea = (SuspensionArea)piece;
            if( (suspensionArea != null) &&
                (suspensionArea.isAtomicFragment()== false) )
            {
               suspensionArea.setLogicalBottom( getLogicalBottom() );
            }
         }
      }
   }

   /**
    * Set the y of the lifeline piece.  The reflexive bends will also
    * be updated  if the activation bar is part of a message to self.
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.lifelinepieces.LifelineCompartmentPiece#setY(long)
    */
   public void setY(long y)
   {
      if( isMessageToSelf() )
      {
         y = ACTIVATION_BAR_BUFFER;
      }
      
      super.setY(y);
      postUpdateReflexiveBends();
   }

   /**
    * Set the left side of the lifeline piece.  The reflexive bends will also
    * be updated  if the activation bar is part of a message to self.
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.lifelinepieces.LifelineCompartmentPiece#setLeft(int)
    */
   public void setLeft(int left)
   {
      super.setLeft(left);
      postUpdateReflexiveBends();
   }
   
   /**
    * Draw the piece.  The activation bar is rendendered as a blue bar.
    * 
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.lifelinepieces.ParentPiece#draw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, double)
    */
   public void draw(IDrawInfo pDrawInfo, double zoomLevel)
   {
      TSEGraphics g = pDrawInfo.getTSEGraphics();
      
      Color curColor = g.getColor();
      g.setColor(ACTIVATION_BAR_COLOR);
      
      IETRect bounds = getAbsoluteDrawRect(zoomLevel);
      TSConstRect tsBounds = new TSConstRect(bounds.getLeft(), 
                                             bounds.getTop(),
                                             bounds.getRight(),
                                             bounds.getBottom());
      g.fillRect(tsBounds);
      
      g.setColor(curColor);
      super.draw(pDrawInfo, zoomLevel);
   }

   /**
    * Write ourselves to archive, returns the compartment element.
    */
   public IProductArchiveElement writeToArchive( IProductArchiveElement parentElement )
   {
      if( null == parentElement )  throw new IllegalArgumentException();

      IProductArchiveElement element = super.writeToArchive( parentElement );

      if( element != null )
      {
         element.addAttributeBool(
            IProductArchiveDefinitions.MESSAGEEDGEENGINE_ISSELFTOMESSAGE_BOOL, m_IsMessageToSelf );
      }
      
      return element;
   }
   
   /**
    * Update from archive.
    */
   public void readFromArchive( IProductArchiveElement element )
   {
      if( null == element )  throw new IllegalArgumentException();

      super.readFromArchive( element );

      m_IsMessageToSelf = element.getAttributeBool(
         IProductArchiveDefinitions.MESSAGEEDGEENGINE_ISSELFTOMESSAGE_BOOL, false );
   }
   
   /**
    * Post an event to update the reflexive bends
    */
   protected void postUpdateReflexiveBends()
   {
      if(isMessageToSelf() == true)
      {
         getParent().postValidateNode();
      }
   }
   
   /**
    * A message can finish on an activation bar if no other message is finishing 
    * on the activation bar.
    * 
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.lifelinepieces.ConnectorPiece#canFinishMessage(int)
    */
   public boolean canFinishMessage(int iTop)
   {
      boolean retVal = false;
      
      if((getConnector(LifelineConnectorLocation.LCL_TOPLEFT) == null) &&
         (getConnector(LifelineConnectorLocation.LCL_TOPRIGHT) == null) )
      {
         ParentPiece abovePiece = getPieceAbove( iTop );
         
         if (abovePiece == null)
         {
            retVal = true;
         }
      }
   
      return retVal;
   }
   
   /**
    * Test if it is ok to update the input connector location for this piece.
    * 
    * @param corner The corner that is to be updated.  Must be on of the values
    *               in the interface LifelineConnectorLocation 
    * @return <code>true</code> if the corner can be updated.
    * @see LifelineConnectorLocation
    */
   public boolean canUpdateAssociatedPiece(int corner)
   {
      boolean retVal = false;

      if(isMessageToSelf() == false)
      {
         if(m_AllowAllUpdatesOfAssocitedPieces == true)
         {
            retVal = true;
         }
         else if((corner == LifelineConnectorLocation.LCL_BOTTOMRIGHT) ||
                 (corner == LifelineConnectorLocation.LCL_BOTTOMLEFT))
         {
            retVal = true;
         }
      }
      return retVal;
   }
   
   /**
    * Update the bends of the reflexive messages. 
    */
   public void updateReflexiveBends()
   {
      super.updateReflexiveBends();
      
      if(isMessageToSelf() == true)
      {
         TSConnector topRight = getConnector(LifelineConnectorLocation.LCL_TOPRIGHT); 
         TSConnector bottomRight = getConnector(LifelineConnectorLocation.LCL_BOTTOMRIGHT);
         TSDEdge edge = PresentationHelper.getConnectedEdge(topRight,
                                                            true);
         updateReflexiveBends(edge);
         
         edge = null;
         edge = PresentationHelper.getConnectedEdge(bottomRight,
                                                    true);
         updateReflexiveBends(edge);
      }
   }

   /**
    * Forces this activation bar to shrink when possible.
    */
   public void cleanUpActivationBars()
   {
      cleanUpChildrenActivationBars();
      
      shrinkToFitChildren();
   }
   
   /**
    * Validate the lifeline and all its child pieces
    */
   public boolean validate()
   {
      boolean bIsValid = super.validate();

      if( bIsValid )
      {
         if( isMessageToSelf() == true)
         {
            if( getY() != LifelineCompartmentPiece.ACTIVATION_BAR_BUFFER )
            {
               setY( LifelineCompartmentPiece.ACTIVATION_BAR_BUFFER );
            }
         }
         else
         {
            shrinkToFitChildren();
         }
      }

      return bIsValid;
   }
   
   /**
    * Shrinks the activation bar to fit the bounds set by the children pieces
    */
   public void shrinkToFitChildren()
   {
      if(getNumChildren() > 0)
      {
         if(containsMessageOnTop() == false)
         {
            ParentPiece topPiece = getTopPiece();            
            int newTop = topPiece.getTop() - MIN_SIBLING_SPACE;
            resizeTop(newTop);
         }
         
         if(containsMessageOnBottom() == false)
         {
            ParentPiece bottomPiece = getBottomPiece();
            if(bottomPiece != null)
            {
               super.setBottom(bottomPiece.getBottom() + MIN_SIBLING_SPACE);
            }
         }
      }
   }
   
   /**
    * Determines if the ActivationBar is a message to self activation.
    */
   public boolean isMessageToSelf()
   {
      return m_IsMessageToSelf;
   }

   /**
    * Makes the ActivationBar a message to self activation.
    */
   public void setMessageToSelf(boolean b)
   {
      m_IsMessageToSelf = b;
      updateReflexiveBends();
   }


   /**
    * Retreives the first non NULL parent piece.
    * 
    * @return The first non null parent piece. 
    */
   protected ParentPiece getTopPiece()
   {
      ParentPiece topPiece =  null;
      for (int index = 0; index < getPieces().size(); index++)
      {
         topPiece = getPieces().get(index);
         if(topPiece != null)
         {
            break;
         }
      }
      return topPiece;
   }

   /**
    * Retreives the first non NULL parent piece.
    * 
    * @return The first non null parent piece. 
    */
   protected ParentPiece getBottomPiece()
   {
      ParentPiece topPiece =  null;
      for (int index = getPieces().size() - 1; index >= 0; index++)
      {
         topPiece = getPieces().get(index);
         if(topPiece != null)
         {
            break;
         }
      }
      return topPiece;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.lifelinepieces.LifelineCompartmentPiece#getID()
    */
   protected String getID()
   {
      return "ActivationBar";
   }
   
   /**
    * Update the bends of the reflexive messages.
    * @param edge The edge that is to be updated.
    */
   protected void updateReflexiveBends(TSDEdge edge)
   {
      assert isMessageToSelf() : "We must only bend message to self messages.";
      
      RecursiveHelper helper = null;
      try
      {
         helper = new RecursiveHelper( "UpdateReflexiveBends" );
         if( RecursiveHelper.isOkToUsePiece( "UpdateReflexiveBends", this ) &&
             (edge != null) )
         {
            // In Java we have a reroute command, so use that.
            
            // Make sure the connectors are updated before updating the bends
            // This was added because we saw a problem:  sync 2-1, create message-to-self
            // under this move the sync message down far enough so that the message-to-self is
            // below the original lifeline.  On the mouse up the message-to-self edges
            // were drawn in the wrong location.
         
            LifelineCompartmentPiece parent = getParentPiece();
            if (parent instanceof ConnectorPiece)
            {
               ConnectorPiece connector = (ConnectorPiece)parent;
               connector.updateConnectorsViaTopCenter();
            }         
         
            TSConnector sourceConnector = edge.getSourceConnector();
            TSConnector targetConnector = edge.getTargetConnector();
         
            if((sourceConnector != null) && (targetConnector != null))
            {
               double bottom = Math.max(sourceConnector.getCenterY(), 
                                        targetConnector.getCenterY());
               double top    = Math.min(sourceConnector.getCenterY(), 
                                        targetConnector.getCenterY());
               double right  = Math.max(sourceConnector.getCenterX(), 
                                        targetConnector.getCenterX());
                                        
               ArrayList< TSConstPoint > ptsMessages = new ArrayList< TSConstPoint >();
               double adjustedRight = right + PIECE_WIDTH;
               ptsMessages.add( new TSConstPoint( adjustedRight, bottom ));
               ptsMessages.add( new TSConstPoint( adjustedRight + PIECE_WIDTH, (top + bottom) / 2 ));
               ptsMessages.add( new TSConstPoint( adjustedRight, top ));
               
               edge.reroute( ptsMessages );
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
   
   public void moveConnectorsBy(int delta, boolean updateAssociatedPieces)
   {
       m_AllowAllUpdatesOfAssocitedPieces = updateAssociatedPieces;
       
       super.moveConnectorsBy(delta, updateAssociatedPieces);
       
       m_AllowAllUpdatesOfAssocitedPieces = false;
   }
}
