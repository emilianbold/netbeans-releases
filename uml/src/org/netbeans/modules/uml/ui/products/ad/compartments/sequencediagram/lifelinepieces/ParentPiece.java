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

import java.util.Iterator;
import java.util.ListIterator;

import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.ETLifelineCompartment;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;

/**
 * 
 * @author Trey Spiva
 */
public abstract class ParentPiece extends LifelineCompartmentPiece
{
   public final static String SUSPENSION_AREA_ID = "SuspensionArea";
   public final static String LIFELINE_ID        = "Lifeline";
   public final static String DESTORY_ELEMENT_ID = "DestroyElement";
   public final static String ACTIVATION_BAR_ID  = "ActivationBar";
   
   protected ETList < ParentPiece > m_ListPieces = new ETArrayList < ParentPiece > ();

   public ParentPiece(ETLifelineCompartment parent, LifelineCompartmentPiece parentPiece, IETPoint topLeft, int height)
   {
      super(parent, parentPiece, topLeft, height);
   }
   
   /**
    * Create a new piece at the specified location.
    * 
    * @param kind The type of piece to create.  Must be one of the 
    *             LifelinePiecesKind values.
    * @param y The y location of the piece.
    * @param height The height of the piece.
    */
   public abstract ParentPiece createNewPiece(int kind, int y, int height); 
   
   /**
    * Returns the piece above the input logical vertical location.
    *
    * @param lCompartmentY[in]
    *
    * @return the piece above the input logical vertical location
    */
   public ParentPiece getPieceAbove(int y)
   {
      ParentPiece retVal = null;
      
      for (int index = m_ListPieces.size() - 1; index >= 0 ; index--)
      {
         ParentPiece curPiece = m_ListPieces.get(index);
         if(curPiece != null)
         {
            int pieceBottom = curPiece.getBottom();
            if(y > pieceBottom)
            {
               retVal = curPiece;
               break;
            }
         }
      }
      
      return retVal;
   }
   
   /**
    * Returns the piece below the input logical vertical location.
    *
    * @param lCompartmentY[in]
    *
    * @return the piece above the input logical vertical location
    */
   public ParentPiece getPieceBelow(int y)
   {
      ParentPiece retVal = null;
      
      for (int index = m_ListPieces.size() - 1; index >= 0 ; index--)
      {
         ParentPiece curPiece = m_ListPieces.get(index);
         if(curPiece != null)
         {
            int pieceTop = curPiece.getTop();
            if(y < pieceTop)
            {
               retVal = curPiece;
               break;
            }
         }
      }
      
      return retVal;
   }
   
   /**
    * Returns the piece above the input logical vertical location.
    *
    * @param y The position to check.
    * @return the piece above the input logical vertical location
    */
   public ParentPiece getPieceAt(int y)
   {
      ParentPiece retVal = null;
      
      for (int index = m_ListPieces.size() - 1; index >= 0 ; index--)
      {
         ParentPiece curPiece = m_ListPieces.get(index);
         if(curPiece != null)
         {
            int pieceBottom = curPiece.getLogicalBottom();
            int pieceTop = curPiece.getLogicalTop();
            if((y <= pieceTop) && (y >= pieceBottom))
            {
                retVal = curPiece;
                
                ParentPiece parentPiece = (ParentPiece)curPiece;
                curPiece = parentPiece.getPieceAt(y);

                // If a child piece is under the mouse, return the child
                // piece instead.
                if(curPiece != null)
                {
                    retVal = curPiece;
                }                
                break;
            }
         }
      }
      
      return retVal;
   }
   
   /**
    * @return
    */
   public ETList < ParentPiece > getPieces()
   {
      return m_ListPieces;
   }

   /**
    * @param list
    */
   public void setPieces(ETList < ParentPiece > list)
   {
      m_ListPieces = list;
   }
   
   /**
    * Set the height of this piece.
    */
   public void setHeight(int height)
   {
      int delta = height - getHeight();
      
      super.setHeight(height);
      
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
    * Returns the number of children stored in the member list
    */
   public int getNumChildren()
   {
      return m_ListPieces.size();
   }

   /**
    * Grows this piece and all it parrents, sibblings are moved first to support the move even it not bumped
    */
   public void stretch( int iStretchFromY, int iStretchDelta )
   {
      if( iStretchDelta != 0 )
      {
         LifelineCompartmentPiece piece = getParentPiece();
         if (piece instanceof ParentPiece)
         {
            ((ParentPiece)piece).stretch( iStretchFromY, iStretchDelta );
         }

         if( iStretchDelta > 0 )
         {
            moveSiblingsBelow( iStretchFromY, iStretchDelta );

            final int iNewHeight = getHeight() + iStretchDelta;
            setHeight( iNewHeight );
         }
         else
         {
            final int iNewHeight = getHeight() + iStretchDelta;
            setHeight( iNewHeight );

            moveSiblingsBelow( iStretchFromY, iStretchDelta );
         }
      }
   }
   
   /**
    * Find the piece, possibly a descendent, that exists at the input location.
    * 
    * @param top The location to find.
    * @return The piece at the location or this if none is found.
    */
   public ParentPiece findPieceAt(int top)
   {
      ParentPiece retVal = this;
      
      for (Iterator < ParentPiece > iter = m_ListPieces.iterator(); iter.hasNext();)
      {
         ParentPiece curPiece = iter.next();
         if(curPiece != null)
         {
            ETSystem.out.println("top(" + top + ") >= curPiece.getTop()(" + curPiece.getTop() + ") = " + (top >= curPiece.getTop()));
            ETSystem.out.println("top(" + top + ") <= curPiece.getBottom()(" + curPiece.getBottom() + ") = " + (top <= curPiece.getBottom()));
            if((top >= curPiece.getTop()) && 
               (top <= curPiece.getBottom()))
            {
               retVal = curPiece.findPieceAt(top);
               break;
            }
         }
      }
      
      return retVal;
   }

   /**
    * Find the piece, possibly a descendent, that exists nearest to the input compartment logical location
    * The assumption here is that this piece was found using FindPieceAt(), and now we want to find
    * the closest sibling to the input location.
    *
    * If there are not any child pieces then NULL is returned.
    */
   public ParentPiece findNearestChildPiece(int top)
   {
      ParentPiece retVal = null;
      
      if(m_ListPieces.size() > 0)
      {
         int prevBottom = 0;
         
         for (Iterator < ParentPiece > iter = m_ListPieces.iterator(); iter.hasNext();)
         {
            ParentPiece curPiece = iter.next();
            if(curPiece != null)
            {
               int pieceBottom = curPiece.getBottom();
               if(top <= pieceBottom)
               {
                  // The default behavior here will be to use the previous piece
                  // as the found piece, unless the logic below determines that the
                  // current piece is closer.
                  if(retVal != null)
                  {
                     retVal = curPiece;
                  }
                  else
                  {
                     int prevDelta = top - prevBottom;
                     int curDelta = curPiece.getTop() - top;
                     if(curDelta < prevDelta)
                     {
                        retVal = curPiece;
                     }
                  }
                  
                  break;
               }
               
               retVal = curPiece;
               prevBottom = pieceBottom;
            }
         }   
      }
      
      return retVal;
   }
   
   /**
    * Pass the clean activation bars call to the children
    */
   public void cleanUpChildrenActivationBars()
   {
      for (Iterator < ParentPiece > iter = m_ListPieces.iterator(); iter.hasNext();)
      {
         ParentPiece curPiece = iter.next();
         if(curPiece != null)
         {
            curPiece.cleanUpActivationBars();
            curPiece.cleanUpChildrenActivationBars();         
         }
      }
   }
   
   public void cleanUpActivationBars()
   {
      if(m_ListPieces.size() > 1)
      {
//         Iterator < ParentPiece > iter = m_ListPieces.iterator();
//         Iterator < ParentPiece > prevIter = iter;
//         
//         ParentPiece abovePiece = null;
//         ParentPiece belowPiece = iter.next();
//         
//         while (iter.hasNext())
//         {
//            abovePiece = belowPiece;
//            belowPiece = iter.next();
//            
//            if(mergeActivationBars(abovePiece, belowPiece) == true)
//            {
//               // Start over because the list has changed;
//               iter = prevIter;
//               
//               // I have to first reset the iterator otherwise I will get a 
//               // ConcurrentModificationException when calling
//               // belowPiece = iter.next();
//               belowPiece = null;
//               belowPiece = iter.next();
//            }
//            
//            prevIter = iter;            
//         }

         ParentPiece abovePiece = null;
         ParentPiece belowPiece = m_ListPieces.get(0);
         for(int index = 1, prev = 0; index < m_ListPieces.size(); index++)
         {
            abovePiece = belowPiece;
            belowPiece = m_ListPieces.get(index);
            
            if(mergeActivationBars(abovePiece, belowPiece) == true)
            {
               // Start over because the list has changed.
               index = prev;
               belowPiece = m_ListPieces.get(index);               
            }
            prev = index;
         }
         
         removeVoids();
      }
   }
   
   /**
    * Merge the two pieces if they meet the proper criteria
    */
   public boolean mergeActivationBars(ParentPiece above, ParentPiece below)
   {
      boolean retVal = false;
      
      if((above != null) && (below != null))
      {
         // Fix W1861:  Activation bars merge when moved together
         // Fix W3235:  Ensure there are no messages attached to either activation bar
         
         if((above instanceof ActivationBar) &&
            (below instanceof ActivationBar))  
         {
            ActivationBar aboveBar = (ActivationBar)above;
            ActivationBar belowBar = (ActivationBar)below;
            if((aboveBar.containsMessageOnBottom() == false) && 
               (belowBar.containsMessageOnTop() == false))
            {
               above.merge(below);
               retVal = true;
            }
         }
      }
      
      return retVal;
   }

   /**
    * Moves this piece's children to the input piece
    */
   protected void moveChildrenTo( ParentPiece newParent )
   {
      if( null == newParent )  throw new IllegalArgumentException();
      
      for (Iterator iter = m_ListPieces.iterator(); iter.hasNext();)
      {
         ParentPiece piece = (ParentPiece)iter.next();
         newParent.insertPiece( piece );
      }

      m_ListPieces.clear();
   }
   
   /**
    * Merge the other piece into this piece by copying all the pieces to this 
    * piece
    * 
    * @param otherPiece The piece to merge into this instance.
    */
   public void merge(ParentPiece otherPiece)
   {
      if(otherPiece != null)
      {
         //assert getParentPiece() == otherPiece.getParentPiece();
         
         // Set the correct height of this piece before adding children
         // This prevents an error in the pNewChild->SetLogicalTop() call below
         int logicalBottom = otherPiece.getLogicalBottom();
         
         ParentPiece otherParent = (ParentPiece)otherPiece.getParentPiece();
         if(otherParent != null)
         {
            otherParent.voidPiece(otherPiece);
         }
         
         ETSystem.out.println("Before getHeight() = " + getHeight());
         setLogicalBottom(logicalBottom);
         ETSystem.out.println("After getHeight() = " + getHeight());
         
         for (Iterator < ParentPiece > iter = otherPiece.getPieces().iterator(); iter.hasNext();)
         {
            ParentPiece curPiece = iter.next();
            if(curPiece != null)
            {
               curPiece.setParentPiece(this);
               m_ListPieces.add(curPiece);
            }
         }
      }
   }

   /**
    * Remove a piece from the list of pieces
    */
   protected void removePiece( ParentPiece newPiece )
   {
      m_ListPieces.removeItem( newPiece );
   }
   
   /**
    * Removes this piece from its parent, but moves any child pieces to the parent
    */
   public void removeSelfFromStack()
   {
      LifelineCompartmentPiece parentPiece = getParentPiece();
      if (parentPiece instanceof ParentPiece)
      {
         ParentPiece parent = (ParentPiece)parentPiece;
         
         parent.removePiece( this );
         
         for (Iterator iter = m_ListPieces.iterator(); iter.hasNext();)
         {
            LifelineCompartmentPiece piece = (LifelineCompartmentPiece)iter.next();
            if (piece instanceof ParentPiece)
            {
               ((ParentPiece)piece).moveChildrenTo( parent );
            }
         }

         setParentPiece( null );
      }
   }
   
   /**
    * Removes all void values within the member list of pieces
    *
    * @see #voidPiece()
    */
   protected void removeVoids()
   {
      for (ListIterator < ParentPiece > iter = m_ListPieces.listIterator(); iter.hasNext();)
      {
         ParentPiece curPiece = iter.next();
         if(curPiece == null)
         {
            iter.remove();         
         }
      }
   }
   
   /**
    * Sets the piece to <code>null</code> in the piece list.
    * This is so the iterator that this call contains remains valid
    *
    * @see removeVoids();
    */
   public void voidPiece( ParentPiece pNewPiece )
   {
      int pos = m_ListPieces.indexOf( pNewPiece );
      if( pos > -1 )
      {
         try
         {
            m_ListPieces.set(pos, null);
         }
         catch (UnsupportedOperationException e)
         {
            assert false : "Unable to replace the piece (We should never be here.";
         }
      }
   }
   
   /**
    * Draw the contianed pieces.  The pieces are drawn according to the current
    * zoom level.
    */
   public void draw(IDrawInfo pDrawInfo, double zoomLevel)
   {
      for (Iterator < ParentPiece > iter = m_ListPieces.iterator(); iter.hasNext();)
      {
         ParentPiece curPiece = iter.next();
         if (curPiece != null)
         {
            curPiece.draw(pDrawInfo, zoomLevel);
         }
      }
   }
   
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.lifelinepieces.LifelineCompartmentPiece#readFromArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
    */
   public void readFromArchive(IProductArchiveElement pParentElement)
   {
      super.readFromArchive(pParentElement);
      
      IProductArchiveElement[] subElements = pParentElement.getElements();
      if(subElements != null)
      {
         for (int index = 0; index < subElements.length; index++)
         {
            IProductArchiveElement curElement = subElements[index];
            if(curElement != null)
            {
               String id = curElement.getID();
               if(id.equals(IProductArchiveDefinitions.ADLIFELINECONNECTORLIST_STRING) == false)
               {
                  ParentPiece piece = null;
                  
                  if(id.equals(ACTIVATION_BAR_ID) == true)
                  {
                     piece = new ActivationBar( getParent(), this, new ETPoint( 0, getChildOffset() ), PIECE_HEIGHT );
                  }
                  else if(id.equals(DESTORY_ELEMENT_ID) == true)
                  {
                     // In the Java code we should never see a destroy element, 
                     // because this information has been moved to an attribute on the lifeline.
                  }
                  else if(id.equals(LIFELINE_ID) == true)
                  {
                     piece = new Lifeline(getParent());
                  }
                  else if(id.equals(SUSPENSION_AREA_ID) == true)
                  {
                     piece = new SuspensionArea( getParent(), this, new ETPoint( 0, getChildOffset() ), PIECE_HEIGHT );
                  }               
                  
                  if(piece != null)
                  {
                     piece.readFromArchive(curElement);
                     m_ListPieces.add(piece);
                  }
               }
            }
         }
      }
   }

   /**
    * Use ResizeToFitCompartment() to fit the node to the lifeline compartment.
    */
   public void resizeNode(boolean doItNow)
   {
      if(doItNow == true)
      {
         IDrawEngine engine = getDrawEngine();
         ICompartment compartment = getParent();
         if((engine instanceof INodeDrawEngine) && (compartment != null))
         {
            INodeDrawEngine nodeEngine = (INodeDrawEngine)engine;
            nodeEngine.resizeToFitCompartment(compartment, true, false);        
         }
      }
      else
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               IDrawEngine engine = getDrawEngine();
               engine.sizeToContents();
            }
         });
      }
   }
   
   /**
    * Validate the lifeline and all its child pieces
    */
   public boolean validate()
   {
      boolean retVal = false;

      for (int index = m_ListPieces.size() - 1; index >= 0; index--)
      {
         ParentPiece curPiece = m_ListPieces.get(index);
         if(curPiece != null)
         {
            if(curPiece.validate() == false)
            {
               m_ListPieces.remove(index);
            }
         }
      }
      
      return !m_ListPieces.isEmpty();
   }
   
   /**
    * Update the bends of the reflexive messages.
    */
   public void updateReflexiveBends()
   {
      for (Iterator < ParentPiece > iter = m_ListPieces.iterator(); iter.hasNext();)
      {
         ParentPiece curPiece = iter.next();
         if(curPiece != null)
         {
            curPiece.updateReflexiveBends();
         }
      }
   }
   
   /**
    * Changes the top location of this piece without moving its children, or 
    * its bottom
    */
   public void resizeTop(int top)
   {
      int delta = top - getTop();
      
      if(delta != 0)
      {
         super.setY(getY() + delta);
         super.setHeight(getHeight() - delta);
         
         for (Iterator < ParentPiece > iter = m_ListPieces.iterator(); iter.hasNext();)
         {
            ParentPiece curPiece = (ParentPiece)iter.next();
            if(curPiece != null)
            {
               curPiece.setY(curPiece.getY() - delta);
            }
         }
      }
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.lifelinepieces.LifelineCompartmentPiece#writeToArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
    */
   public IProductArchiveElement writeToArchive(IProductArchiveElement pParentElement)
   {
      IProductArchiveElement retVal = super.writeToArchive(pParentElement);
      
      if(retVal != null)
      {
         for (Iterator < ParentPiece > iter = m_ListPieces.iterator(); iter.hasNext();)
         {
            ParentPiece curPiece = iter.next();
            if(curPiece != null)
            {
               curPiece.writeToArchive(retVal);
            }
         }
      }
      
      return retVal;
   }
   
   /**
    * Insert a new piece into the list of pieces.  The insertion will always
    * affect siblings.
    */
   public void insertPiece(ParentPiece newPiece)
   {
      insertPiece(newPiece, true);
   }
   
   /**
    * Insert a new piece into the list of pieces.  
    */
   public void insertPiece(ParentPiece newPiece, boolean affectSiblings)
   {
      if(newPiece != null)
      {
         // Update the new piece with this piece's information
         newPiece.setParentPiece(this);
         
         int newTop = newPiece.getTop();
         
         boolean addPieceToEnd = true;

         // Retain the previous bottom value so we don't overlap pieces
         int prevBottom = 0;
         
         for (int index = 0; index < m_ListPieces.size(); index++)
         {
            ParentPiece curPiece = m_ListPieces.get(index);
            
            if(newTop <= curPiece.getTop())
            {
               // Insert the piece before the found piece.
               m_ListPieces.add(index, newPiece);
               addPieceToEnd = false;
               break;
            }
            
            prevBottom = curPiece.getBottom();
         }
         
         if(addPieceToEnd == true)
         {
            // Insert the piece at the end of the list of children pieces
            m_ListPieces.add(newPiece);
         }
         
         int minTop = prevBottom + MIN_SIBLING_SPACE;
         if(minTop > newTop)
         {
            newPiece.setY(minTop - getParentTop());
         }
         
         // Determine if the siblings need to be moved down
         if( affectSiblings )
         {
            bumpSiblingsBelow(newPiece, false);
         }
         
         // Grow the compartment, if necessary
         resizeNode(false);
      }
   }
   
   /**
    * Moves the pieces under the grandchild location to the grandchild
    */
   public int promotePiecesTo(ParentPiece grandChild, int newHeight)
   {
      int retVal = newHeight;
      
      assert grandChild != null : "Grand Child is NULL.";
      assert this != grandChild : "I am my own grandchild :-(";
      
      if(grandChild != null)
      {
         int top = grandChild.getTop();
         int bottom = top + newHeight;
         
         int limitBelow = 0;
         for (Iterator < ParentPiece > iter = m_ListPieces.iterator(); iter.hasNext();)
         {
            ParentPiece curPiece = iter.next();
            if(curPiece != null)
            {
               assert curPiece instanceof ActivationBar : "We can only promote activation bars";
               
               int pieceTop = curPiece.getTop();
               int pieceBottom = curPiece.getBottom();
               
               if((pieceTop >= top) && (pieceBottom <= bottom))
               {
                  if(m_ListPieces.contains(curPiece) == true)
                  {
                     grandChild.insertPiece(curPiece, false);                     
                     
                     limitBelow = curPiece.getBottom() + ACTIVATION_BAR_BUFFER;
                     voidPiece(curPiece);
                  }
               }
            }
         }
         
         // Update the grand parent piece, if necessary
         if(bottom < limitBelow)
         {
            retVal = limitBelow - top;
         }
      } 
      
      return retVal;     
   }
   
   /**
    * Move siblings above the input child piece, if necessary
    */
   public void bumpSiblingsAbove(ParentPiece child)
   {
      // There are instances when bumbing the siblings recurses back and tells
      // this piece to bump again.  This wastes time and sometimes crashes.
      // So, we use the recursive helper here to avoid bumping the same piece
      // more than once.  This same helper is used in BumpSiblingsAbove(),
      // which keeps us from bumping the same piece in either direction up/down.
      
      RecursiveHelper helper = null;
      try
      {
         helper = new RecursiveHelper( "Bump" );
         if( RecursiveHelper.isOkToUsePiece( "Bump", this ) &&
             (child != null))
         {   
            assert this == child.getParentPiece();
         
            int limitAbove = child.getTop() - ACTIVATION_BAR_BUFFER;
         
            if(m_ListPieces.size() > 1)
            {
               // Make sure each piece bumps its sibling, if necessary
               int startPos = m_ListPieces.lastIndexOf(child);
               if(startPos > -1)
               {
                  ParentPiece prevPiece = m_ListPieces.get(startPos);
                  ParentPiece curPiece = null;
               
                  if((startPos - 1) >= 0)
                  {
                     for(int index = startPos - 1; index >= 0; index--)
                     {
                        if(prevPiece != null)
                        {
                           limitAbove = prevPiece.getTop() - ACTIVATION_BAR_BUFFER;
                        }
                     
                        curPiece = m_ListPieces.get(index);
                        if(curPiece != null)
                        {
                           int delta = limitAbove - curPiece.getBottomBound();
                           
                           if(delta < 0)
                           {
                              if(mergeActivationBars(curPiece, prevPiece) == true)
                              {
                                 if(m_ListPieces.size() < 2)
                                 {
                                    break;  
                                 }
                              
                                 // Start over because the list has changed
                                 index = m_ListPieces.size() - 1;
                                 curPiece = m_ListPieces.get(index);                           
                              }
                              else
                              {
                                 curPiece.moveBy(delta);
                              }
                           }
                        
                           prevPiece = curPiece;
                        }
                     }
                  }
               
                  removeVoids();
               }
            }
         
            // Update the Parent piece, if necessary
            if(getTop() > limitAbove)
            {
               setTop(limitAbove);
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
    * Move siblings below the input child piece, if necessary
    * 
    * @param child All pieces below this piece are bumped, null indicates all pieces are bumped
    * @param allowMerge When true activation bars are allowed to merge, if possible
    */
   public void bumpSiblingsBelow(ParentPiece child, boolean allowMerge)
   {
      // There are instances when bumbing the siblings recurses back and tells
      // this piece to bump again.  This wastes time and sometimes crashes.
      // So, we use the recursive helper here to avoid bumping the same piece
      // more than once.  This same helper is used in BumpSiblingsAbove(),
      // which keeps us from bumping the same piece in either direction up/down.
      
      RecursiveHelper helper = null;
      try
      {
         helper = new RecursiveHelper( "Bump" );
         if( ! RecursiveHelper.isOkToUsePiece( "Bump", this ) )
         {   
            return;
         }
         
         int limitBelow = ACTIVATION_BAR_BUFFER;
         if(child != null)
         {
            assert this == child.getParentPiece();
            
            // If the message is a create message, we want to use the lifeline
            // head as the limit below, not the bottom of the piece (since the
            // piece is 0 height).
            
            limitBelow = child.getBottomBound() + ACTIVATION_BAR_BUFFER;
            //limitBelow = child.getBottom() + ACTIVATION_BAR_BUFFER;
         }
         
         if(m_ListPieces.size() > 0)
         {
            int startPos = m_ListPieces.indexOf(child);            
         
            if(startPos > -1)
            {
               ParentPiece prevPiece = m_ListPieces.get(startPos);
               ParentPiece curPiece = null;
            
               if((startPos + 1 ) < m_ListPieces.size())
               {
                  for(int index = startPos + 1; index <  m_ListPieces.size(); index++)
                  {
                     if(prevPiece != null)
                     {
                         limitBelow = prevPiece.getBottomBound() + ACTIVATION_BAR_BUFFER;
                     }
                  
                     curPiece = m_ListPieces.get(index);
                     if(curPiece != null)
                     {
                        int delta = limitBelow - curPiece.getTop();
                        if(delta > 0)
                        {
                           if((allowMerge == true) && 
                              (mergeActivationBars(prevPiece, curPiece) == true))
                           {
                              if(m_ListPieces.size() < 2)
                              {
                                 break;  
                              }
                           
                              // start from the previous iterator because list
                              // has changed
                              index -= 1;
                              curPiece = m_ListPieces.get(index);                           
                           }
                           else
                           {
                              curPiece.moveBy(delta);
                           }
                        }
                        prevPiece = curPiece;
                     }
                  }
               }
               
               if(prevPiece != null)
               {
                  limitBelow = prevPiece.getBottom() + ACTIVATION_BAR_BUFFER;
               }
               removeVoids();
            }
         }
      
         // Update the Parent piece, if necessary
         if(getBottom() < limitBelow)
         {
            grow(limitBelow);
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
    * Move siblings below the input child piece by the specified delta
    */
   protected void moveSiblingsBelow( int iY, int iDelta )
   {
      if( m_ListPieces.size() > 0 )
      {
         int iNewBottom = Integer.MIN_VALUE;

         ParentPiece belowPiece = null;
         
         // Search for the piece in the list of pieces, from the top
         Iterator iter = m_ListPieces.iterator();
         for (; iter.hasNext();)
         {
            ParentPiece currentPiece = (ParentPiece)iter.next();
            
            if( currentPiece.getTop() > iY )
            {
               belowPiece = currentPiece;
               break;
            }
         }

//         for(; iter.hasNext();)
         while(belowPiece != null)
         {
            belowPiece.moveBy( iDelta );

            iNewBottom = belowPiece.getBottom() + ACTIVATION_BAR_BUFFER;
            
            belowPiece = null;
            if(iter.hasNext() == true)
            {
                belowPiece = (ParentPiece)iter.next();
            }
         }

         // Update this piece's height, if necessary
         if( getBottom() < iNewBottom )
         {
            grow( iNewBottom );
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
       super.setBottom(bottom);
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelineCompartmentPiece#getLifelinePiecesKind()
         */
   public int getLifelinePiecesKind()
   {
       // TODO Auto-generated method stub
       return 0;
   }
   
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelineCompartmentPiece#setY(long)
         */
   public void setY(long value)
   {
       final long originalY = getY();
       super.setY(value);
       
       bumpSiblingsBelow(null,true);
       
       ParentPiece parent = (ParentPiece)getParentPiece();
       
       if(parent != null)
       {
           final long delta = getY() - originalY;
           if(delta < 0)
               parent.bumpSiblingsAbove(this);
           else if(delta > 0)
               parent.bumpSiblingsBelow(this,true);
       }
   }


    protected int getBottomBound()
    {
        return getBottom();
    }

}
