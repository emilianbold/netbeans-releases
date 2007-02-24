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
import java.awt.Stroke;

import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.ETLifelineCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.LifelineConnectorLocation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;

/**
 * 
 * @author Trey Spiva
 */
public class SuspensionArea extends ConnectorPiece
{
   public static final Color SUSPENSION_BORDER_COLOR = Color.BLACK;
   public static final Color SUSPENSION_BAR_COLOR = Color.WHITE;
   /**
    * @param parent
    * @param parentPiece
    * @param topLeft
    * @param height
    */
   public SuspensionArea(ETLifelineCompartment parent, LifelineCompartmentPiece parentPiece, IETPoint topLeft, int height)
   {
      super(parent, parentPiece, topLeft, height);
   }
   
   /**
    * Draw the piece.  The activation bar is rendendered as a blue bar.
    * 
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.lifelinepieces.ParentPiece#draw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, double)
    */
   public void draw(IDrawInfo pDrawInfo, double zoomLevel)
   {
      if(isAtomicFragment() == false)
      {
         TSEGraphics g = pDrawInfo.getTSEGraphics();
         
         Color curColor = g.getColor();
         g.setColor(SUSPENSION_BAR_COLOR);
         
         IETRect bounds = getAbsoluteDrawRect(zoomLevel);
         TSConstRect tsBounds = new TSConstRect(bounds.getLeft(), 
                                                bounds.getTop(),
                                                bounds.getRight(),
                                                bounds.getBottom());
                                                                                                     
         g.fillRect(tsBounds);   
           
         Stroke curStroke = g.getStroke();
         g.setStroke(GDISupport.getLineStroke(DrawEngineLineKindEnum.DELK_DOT, 1));
         
         g.setColor(SUSPENSION_BORDER_COLOR);
         g.drawRect(tsBounds);
         
         g.setStroke(curStroke);
         g.setColor(curColor);
         super.draw(pDrawInfo, zoomLevel);
      }
   }
   /**
    * Returns the lifeline pieces kind for this piece.
    */
   public int getLifelinePiecesKind()
   {
      return LifelinePiecesKind.LPK_SUSPENSION;
   }
   
   /**
    * Set the Left side for this piece
    */
   public void setLeft(int left)
   {
      // If the height is zero this is an atomic fragment.
      // So, the piece need to be centered over its parent.
      super.setLeft( (getHeight() > 0) ? left : (left - CHILD_OFFSET) );
   }
   
   /**
    * Set the height of this piece.  The grandparents of the SuspensionArea
    * will also have thier height updated.
    */
   public void setHeight(int value)
   {
      // Commented out to fix issue 78491.
      // The below commented code only takes care of the case when the message
      // connected to the piece is being moved down (i.e, new height is > than its current height); 
      // hence causing the issue 78491.
      
//      if(value > getHeight())
//      {
//         value = promoteGrandParentsActivationBars(value);
//      }
      value = promoteGrandParentsActivationBars(value);
      
      super.setHeight(value);
      
      // This code makes sure the message-to-self activation bar
      // resizes when the user is moving the result message.
      ActivationBar bar = getMessageToSelfActivationBar();
      if(bar != null)
      {
         bar.setHeight(value - (2 * ACTIVATION_BAR_BUFFER));
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
      super.grow(bottom);
      
      // The bottom connector wont have been updated
      // since canUpdateAssociatedPiece() will return false
      // However, we want to make sure the associated piece is update when this 
      // piece grows.  However, if this suspension area is part of a message-to-
      // self the resizing of the activation bar takes place in 
      // CSuspensionArea::setHeight(), so don't do it here.
      ParentPiece piece = getAssociatedPiece(true);
      if (piece instanceof ActivationBar)
      {
         ActivationBar bar = (ActivationBar)piece;
         if(bar.isMessageToSelf() == false)
         {
            bar.setLogicalBottom(getLogicalBottom());
         }
      }
   }
   
   /**
    * An atomic fragment is the description of the piece used to start an async message
    * @return <code>true</code> if the suspension bar is a atomic fragment.
    */
   public boolean isAtomicFragment()
   {  
      return (getHeight() == 0);  
   }
   
   /**
    * Returns true when it is ok to finish a message on this piece
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.lifelinepieces.ConnectorPiece#canFinishMessage(int)
    */
   public boolean canFinishMessage(int iTop)
   {
      return !isPartOfMessageToSelf();
   }

   /**
    * Returns true if this suspension area contains an activation bar that is a 
    * message-to-self.
    * 
    * @return <code>true</code> 
    */
   protected boolean isPartOfMessageToSelf()
   {
      boolean retVal = false;

      ETList < ParentPiece > pieces = getPieces();
      if(pieces.size() == 1)
      {
         ParentPiece firstPiece = pieces.get(0);
         if (firstPiece instanceof ActivationBar)
         {
            ActivationBar bar = (ActivationBar)firstPiece;
            retVal = bar.isMessageToSelf();
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
      return ((LifelineConnectorLocation.LCL_TOPRIGHT == corner) || 
              (LifelineConnectorLocation.LCL_TOPLEFT == corner));
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.lifelinepieces.ParentPiece#createNewPiece(int, int, int)
    */
   public ParentPiece createNewPiece(int kind, int y, int height)
   {
      ParentPiece retVal = null;

      IETPoint topLeft = new ETPoint(getLeft() + getChildOffset(), y);

      switch (kind)
      {
         case LifelinePiecesKind.LPK_ACTIVATION :
         case LifelinePiecesKind.LPK_ACTIVATION_FINISH :
            retVal = new ActivationBar(getParent(), this, topLeft, height);
            if (retVal != null)
            {
               insertPiece(retVal);
            }
            
//            if (retVal != null)
//            {
//               insertPiece(retVal);
//            }
            break;

         case LifelinePiecesKind.LPK_ATOMIC_FRAGMENT :
            createActivationPiece(kind, y, 0);
            // no break

         case LifelinePiecesKind.LPK_SUSPENSION :
            retVal = createActivationPiece(kind, y, height);
            break;

            default : 
               break;
      }

//      if (retVal != null)
//      {
//         insertPiece(retVal);
//      }

      return retVal;
   }

   /**
    * Find all the messages from this piece and all its associated messages.
    */
   public void getPropagatedMessages( ETList< IMessage > messages )
   {
      super.getPropagatedMessages( messages );

      // Also get the associated piece's messages
      ConnectorPiece associatedPiece = getAssociatedPiece();
      if( associatedPiece != null )
      {
         associatedPiece.getPropagatedMessages( messages );
      }
   }
   
   /**
    * Create a new piece.  First an ActivationBar is created then the specified 
    * pieced is added to the activation bar.
    * 
    * @param kind The type of piece to create.  Must be one of the 
    *             LifelinePiecesKind values.
    * @param y The y location of the piece.
    * @param height The height of the piece.
    * @return The new piece.
    */
   protected ParentPiece createActivationPiece(int kind, int y, int height)
   {
      ParentPiece retVal = null;
      
      ParentPiece activation = createNewPiece(LifelinePiecesKind.LPK_ACTIVATION,
                                               y + ACTIVATION_BAR_BUFFER, 
                                               height + (2 * ACTIVATION_BAR_BUFFER));
       if(activation != null)
       {
          retVal = activation.createNewPiece(kind, ACTIVATION_BAR_BUFFER, height);                                       
       }
      return retVal;
   }

   /**
    * Moves the activation bars owned by the parent, but under this suspension 
    * area, to this suspension area.
    * 
    * @param newHeigth The height of the piece.
    * @return The height after the promotions are completed.
    */
   protected int promoteGrandParentsActivationBars( int newHeight )
   {
      int retVal = newHeight;
      
      if(getParentPiece() != null)
      {
         LifelineCompartmentPiece piece = getParentPiece().getParentPiece();
         if (piece instanceof ParentPiece)
         {
            ParentPiece grandParent = (ParentPiece)piece;
            newHeight = grandParent.promotePiecesTo(this, newHeight);
            getParent().postValidateNode();
         }
      }
      
      return newHeight;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.lifelinepieces.LifelineCompartmentPiece#getID()
    */
   protected String getID()
   {
      return "SuspensionArea";
   }

   /**
    * If this suspension area is part of a message-to-self
    * then this function returns the child activation bar.
    */
   protected ActivationBar getMessageToSelfActivationBar()
   {
      ActivationBar retVal = null;

      ETList < ParentPiece > pieces = getPieces();
      if((pieces != null) && (pieces.size() > 0))
      {
         if (pieces.get(0) instanceof ActivationBar)
         {
            ActivationBar bar = (ActivationBar)pieces.get(0);
            if(bar.isMessageToSelf() == true)
            {
               retVal = bar;
            }
         }
      }

      return retVal;
   }
}

