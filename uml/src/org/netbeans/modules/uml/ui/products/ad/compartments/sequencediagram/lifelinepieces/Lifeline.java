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

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.Iterator;

import sun.rmi.transport.LiveRef;

import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.ETLifelineCompartment;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.jnilayout.TSColor;
import com.tomsawyer.editor.TSEColor;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;

/**
 * 
 * @author Trey Spiva
 */
public class Lifeline extends ParentPiece
{
   public static final int DESTROY_SIZE = PIECE_WIDTH;
   public static final float[] LIFELINE_DASH = {20, 5};
   public static final BasicStroke LIFELINE_STROKE = new BasicStroke(1.0f, 
                                                                     BasicStroke.CAP_BUTT, 
                                                                     BasicStroke.JOIN_MITER, 
                                                                     1.0f, 
                                                                     LIFELINE_DASH, 
                                                                     0.0f);
   public static final TSEColor LIFELINE_COLOR = new TSEColor(0,128,0);
   
   /** Specifies that a destroy action is associated with the lifeline */
   private boolean m_IsDestroyed = false;
   
   public Lifeline(ETLifelineCompartment parent)
   {
      super(parent, null, new ETPoint(0, 0), 470);
   }
   
   
   
   /**
    * Calculate the minimum height for this lifeline
    * 
    * @return The minimum height.
    */
   public long getMinimumHeight()
   {
      long retVal = 40;
      
      ETList < ParentPiece > pieces = getPieces();
      if((pieces != null) && (pieces.size() > 0))
      {
         ParentPiece lastPiece = pieces.get(pieces.size() - 1);
         if(lastPiece != null)
         {
            retVal = lastPiece.getBottom() + MIN_SIBLING_SPACE;
         }
      }
      
      return retVal;
   }

   /**
    * Calculate the minimum height for this lifeline's compartment
    */
   public int getCompartmentMinimumHeight()
   {
      int iMinHeight = (int)getMinimumHeight();

   /* UPDATE, This code supports limiting the movement of the top of the lifeline.
              However, it breaks the creation of the destroy "element"
      if( m_listPieces.size() )
      {
         final IETRect rect( getCompartmentLogicalBoundingRect() );
         final long lMinForTop = (m_listPieces.front().getLogicalTop() - rect.getBottom()) + getParent().getLogicalOffsetInDrawEngineRect().y;
         iMinHeight = Math.max( lMinHeight, lMinForTop );
      }
   */
      return iMinHeight;
   }

   /**
    * Sets the y offset from the parent of the piece, without changing the height
    */
   public void setY( int iY )
   {
      super.setY( 0 );
   }

   /**
    * Set the height of this piece
    */
   public void setHeight( int iHeight )
   {
      final boolean bResizeNode = ( (iHeight != getHeight()) &&
                                    (iHeight != -getCompartmentLogicalBoundingRect().getIntHeight() ) );

      super.setHeight( iHeight );

      if( bResizeNode )
      {
         resizeNode( false );
      }
   }
   
   /**
    * Create a new piece at the specified location
    *
    * @param kind The type of lifeline piece.
    * @param top The offset from the top of the compartment
    */
   public ParentPiece createPiece(int kind, int top)
   {
      ParentPiece retVal = null;
      
      int pieceTop = top;
      if(kind == LifelinePiecesKind.LPK_DESTROY)
      {
         pieceTop = (int)Math.max(pieceTop, getMinimumHeight());
         
         setDestroyedAt(pieceTop);
         retVal = this;
      }
      else
      {
         // LPK_ACTIVATION_FINISH is used by ADLifelineCompartmentImpl::ConnectMessage()
         // to indicate that the FindActivationBarNear() call should be used.
         ParentPiece parentPiece = null;
         if(kind == LifelinePiecesKind.LPK_ACTIVATION_FINISH)
         {
            parentPiece = findActivationBarNear(pieceTop);            
         }
         else
         {
            parentPiece = findPieceAt(top);
         }
         
         if(parentPiece == null)
         {
            parentPiece = this;
         }
         
         int newPieceY = pieceTop - parentPiece.getTop();
         retVal = parentPiece.createNewPiece(kind, newPieceY, PIECE_HEIGHT);
      }
      
      return retVal;
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
             retVal = new ActivationBar(getParent(), this, topLeft, height);
             if(retVal != null)
             {
                insertPiece(retVal);
             }
             break;
            
          case LifelinePiecesKind.LPK_ATOMIC_FRAGMENT:
             retVal = createActivationPiece(kind, y, 0);
             break;
          
          case LifelinePiecesKind.LPK_SUSPENSION:
             retVal = createActivationPiece(kind, y, height);
             break;
            
          default: 
             break;                                         
       }
      
//       if(retVal != null)
//       {
//          insertPiece(retVal);
//       }
      
       return retVal;
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
                                               y - ACTIVATION_BAR_BUFFER, 
                                               height + (2 * ACTIVATION_BAR_BUFFER));
       if(activation != null)
       {
          retVal = activation.createNewPiece(kind, ACTIVATION_BAR_BUFFER, height);                                       
       }
      return retVal;
   }
    
   /**
    * Draws the lifeline piece.  The lifeline children are also drawn.
    */
   public void draw(IDrawInfo pDrawInfo, double zoomLevel)
   {
      final IETRect rectEngine = getEngineLogicalBoundingRect(); // pDrawInfo.getBoundingRect(); 
      final IETRect rectCompartment = getCompartmentLogicalBoundingRect();
      
      final double left = getLeftRelativeTo(rectEngine);
      final double centerX = rectEngine.getCenterX();
      final double top  = rectCompartment.getTop();
      double bottom = Math.round(rectEngine.getBottom());
      
      TSEGraphics g = pDrawInfo.getTSEGraphics();
            
      if(isDestroyed() == true)
      {
         
         final double destroySize = DESTROY_SIZE;
       
         double destroyHalf = (destroySize / 2);
         double destroyLeft = centerX - destroyHalf;
         double destroyTop  = bottom + destroySize;
         double destroyRight = centerX + destroyHalf;
         
         TSConstPoint topLeft = new TSConstPoint(destroyLeft, destroyTop);
         TSConstPoint bottomRight = new TSConstPoint(destroyRight, bottom);
                 
         g.setColor(TSEColor.red);
         g.drawLine(topLeft, bottomRight);
         
         TSConstPoint rightTop = new TSConstPoint(destroyRight, destroyTop);
         TSConstPoint leftBottom = new TSConstPoint(destroyLeft, bottom);
         g.drawLine(rightTop, leftBottom);
         
         // Adjust the bottom of the lifeline.  We want the lifeline to end 
         // in the center of the destoy symbol.
         bottom = (bottom + destroyHalf);
      }
      
      TSConstPoint lifeLineTop = new TSConstPoint(centerX, top);
      TSConstPoint lifeLineBottom = new TSConstPoint(centerX, bottom);
      
      Stroke curStroke = g.getStroke();
      
      g.setStroke(LIFELINE_STROKE);
      g.setColor(LIFELINE_COLOR);
      g.drawLine(lifeLineTop, lifeLineBottom);
      
      g.setStroke(curStroke);
      
      super.draw(pDrawInfo, zoomLevel);
      
   }
   
   /**
    * Update from archive.
    * 
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.lifelinepieces.LifelineCompartmentPiece#readFromArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
    */
   public void readFromArchive(IProductArchiveElement pParentElement)
   {
      if(pParentElement != null)
      {
         super.readFromArchive(pParentElement);
         setDestroyed(pParentElement.getAttributeBool(IProductArchiveDefinitions.MESSAGEEDGEENGINE_ISDESTROYED_BOOL));
      }
   }
   
   /**
    * Write ourselves to archive, returns the compartment element.
    * 
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.lifelinepieces.LifelineCompartmentPiece#writeToArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
    */
   public IProductArchiveElement writeToArchive(IProductArchiveElement pParentElement)
   {
      IProductArchiveElement retVal = null;
      
      if(pParentElement != null)
      {
         retVal = super.writeToArchive(pParentElement);
         if(retVal != null)
         {
            retVal.addAttributeBool(IProductArchiveDefinitions.MESSAGEEDGEENGINE_ISDESTROYED_BOOL, isDestroyed());
         }
      }
      
      return retVal;
   }

   /**
    * Attach the connectors determined during ReadConnectorsFromArchive()
    */
   public void attachConnectors()
   {
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
    * Returns the lifeline pieces kind for this piece.
    */
   public int getLifelinePiecesKind()
   {
      return LifelinePiecesKind.LPK_LIFELINE;
   }
   
   
   public ParentPiece findActivationBarNear(long top)
   {
      ParentPiece retVal = null;
      
      // cleanUpActivationBars();
            
      ParentPiece piece = getPieceUnderY(top);
      
      boolean keepProcessing = true;
      while((piece != null) && (keepProcessing == true))
      {
         switch(piece.getLifelinePiecesKind())
         {
            default:
            case LifelinePiecesKind.LPK_LIFELINE:
               keepProcessing = false;
               
            case LifelinePiecesKind.LPK_ACTIVATION:
            case LifelinePiecesKind.LPK_ACTIVATION_FINISH:
            
               if ( piece instanceof ActivationBar )
               {
               ActivationBar bar = (ActivationBar)piece;
               if(bar.containsMessageOnBottom() == true)
               {
                  piece = (ParentPiece)piece.getParentPiece();
               }
               else
               {
                  retVal = piece;
                  keepProcessing = false;
               }
               }
               break;
               
            case LifelinePiecesKind.LPK_SUSPENSION:
            case LifelinePiecesKind.LPK_ATOMIC_FRAGMENT:
               piece = (ParentPiece)piece.getParentPiece();
               break;
         }
      }
      
      return retVal;
   }
   
   public void resizeTopBy(int delta)
   {
      if(delta != 0)
      {
         // Attach all the connectors for the children as well
         ETList < ParentPiece > pieces = getPieces();
         for (Iterator < ParentPiece > iter = pieces.iterator(); iter.hasNext();)
         {
            ParentPiece curPiece = (ParentPiece)iter.next();
            if(curPiece != null)
            {
               curPiece.setY(curPiece.getY() - delta);
            }
         }
      }
   }
   
   //**************************************************
   // Data Access Methods
   //**************************************************
   
   /**
    * Find the piece that is exactly under the specified location.
    * 
    * @param top The Y location.
    * @return The piece under the Y location.  <code>null</code> will be 
    *         retruned if a piece is not found.
    */
   protected ParentPiece getPieceUnderY(long top)
   {
      ParentPiece piece = findPieceAt((int)top);
      if(piece != null)
      {
         switch(piece.getLifelinePiecesKind())
         {
            case LifelinePiecesKind.LPK_ACTIVATION:
            case LifelinePiecesKind.LPK_ACTIVATION_FINISH:
               // do nothing
               break;
              
            case LifelinePiecesKind.LPK_SUSPENSION:
            case LifelinePiecesKind.LPK_ATOMIC_FRAGMENT:
            case LifelinePiecesKind.LPK_LIFELINE:        
            default:    
               piece = piece.findNearestChildPiece((int)top);
               break;                        
         }
      }
      return piece;
   }
   
   /**
    * Retreives the archive ID for the lifeline piece.
    * 
    * @return The name of the lifeline piece.
    */
   public String getID()
   {
      return "Lifeline";
   }

   /**
    * Determines if there is a destroy action associated with the lifeline.
    * 
    * @return <code>true</code> when the lifeline has destroyed a destroy action
    *         associated with the lifeline. <code>false</code> when the lifeline
    *         does not have a destroyed action associted with the lifeline.
    */
   public boolean isDestroyed()
   {
      return m_IsDestroyed;
   }

   /**
    * Determines if there is a destroy action associated with the lifeline.
    * 
    * @param b <code>true</code> when the lifeline has destroyed a destroy action
    *          associated with the lifeline. <code>false</code> when the lifeline
    *          does not have a destroyed action associted with the lifeline.
    */
   public void setDestroyed(boolean b)
   {
      m_IsDestroyed = b;
   }
   
   /**
    * Creates a destroy "element" at the specified location.
    */
   protected void setDestroyedAt(int y)
   {
      setDestroyedAt(y, true);
   }
   
   /**
    * Ensure that any connectors that are attached to pieces are updated.
    * This routine references the connector from the top center of the node.
    */
   public void updateConnectorsViaTopCenter()
   {
      ETList < ParentPiece > pieces = getPieces();
      for (Iterator < ParentPiece > iter = pieces.iterator(); iter.hasNext();)
      {
         ParentPiece curPiece = iter.next();
         if (curPiece instanceof ConnectorPiece)
         {
            ConnectorPiece curConnectorPiece = (ConnectorPiece)curPiece;
            curConnectorPiece.updateConnectorsViaTopCenter();
         }
      
      }
   }
   
   /**
    * Ensure that any connectors that are attached to pieces are updated.
    * This routine references the connector from the bottom center of the node.
    */
   public void updateConnectorsViaBottomCenter()
   {
      ETList < ParentPiece > pieces = getPieces();
      for (Iterator < ParentPiece > iter = pieces.iterator(); iter.hasNext();)
      {
         ParentPiece curPiece = iter.next();
         if (curPiece instanceof ConnectorPiece)
         {
            ConnectorPiece curConnectorPiece = (ConnectorPiece)curPiece;
            curConnectorPiece.updateConnectorsViaBottomCenter();
         }
      
      }
   }
   
   /**
    * Creates a destroy "element" at the specified location.
    */      
   protected void setDestroyedAt(int y, boolean resizeNode)
   {
      setHeight(y + (DESTROY_SIZE / 2));
      setDestroyed(true);
      
      if(resizeNode == true)
      {
         resizeNode(true);
      }
   }

   /**
    * The hroizontal offset for the children of this piece.  The piece will be
    * centered around the lifeline.
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.lifelinepieces.LifelineCompartmentPiece#getChildOffset()
    */
   public int getChildOffset()
   {
      return -(PIECE_WIDTH/2) + 1;
   }

}