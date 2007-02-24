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



package org.netbeans.modules.uml.ui.products.ad.compartments;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.InvalidPointerException;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADContainerDrawEngine;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETPointEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETTransform;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IMouseEvent;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISetCursorEvent;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PointConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETHorzDragCursor;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETVertDragCursor;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.DragManager;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.IDragManager;
//import com.tomsawyer.editor.TSEWindowInputState;
import com.tomsawyer.editor.TSEWindowInputTool;
//import com.tomsawyer.editor.TSEWindowState;
import com.tomsawyer.editor.TSEWindowTool;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Iterator;

/**
 * @author KevinM
 *
 */
public class ETZoneDividers implements IETZoneDividers
{
	class DividerInfo {
		public DividerInfo(int offset) {
			m_lOffset = offset;
			m_rectDivider = new ETRect();
		};

		public void setOffset(int offset) {
			m_lOffset = offset;
		}

		public int getOffset() {
			return m_lOffset;
		}

		public IETRect getRectDivider() {
			return m_rectDivider;
		}

		public void setRectDivider(IETRect rect) {
			m_rectDivider = rect;
		}

		protected int m_lOffset;
		protected IETRect m_rectDivider;
	}

	protected static final int NEW_ZONE_SIZE = 40;
	protected int m_orientation;
	protected ETList < ETZoneDividers.DividerInfo > m_dividers = new ETArrayList < DividerInfo > ();
	protected int m_nLineStyle = DrawEngineLineKindEnum.DELK_DASH;
	protected IADZonesCompartment m_parentCompartment;
	protected int m_ulCurrentDivider = 0;
	protected IETRect m_rectPreResize = null;

	public static final String ZD_DIVIDERS = "Dividers";
	public static final String ZD_DIVIDER = "Divider";
	public static final String ZD_OFFSET = "offset";
	
	public ETZoneDividers(final IADZonesCompartment pCompartmentImpl, int lineStyle, int orientation) {
		m_parentCompartment = pCompartmentImpl;
		setLineStyle(lineStyle);
		setOrientation(orientation);
	}

	public ETZoneDividers(final IADZonesCompartment pCompartmentImpl, int lineStyle) {
		this(pCompartmentImpl, lineStyle, DMO_UNKNOWN);
	}

	public ETZoneDividers(final IADZonesCompartment pCompartmentImpl) {
		this(pCompartmentImpl, DrawEngineLineKindEnum.DELK_SOLID);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#setLineStyle(int)
	 */
	public void setLineStyle(int DrawEngineLineKindEnum) {
		m_nLineStyle = DrawEngineLineKindEnum;

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#setOrientation(int)
	 */
	public void setOrientation(int orientation) {
		m_orientation = orientation;

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#getOrientation()
	 */
	public int getOrientation() {
		return m_orientation;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#insertDivider(int)
	 */
	public void insertDivider(int nBefore) {
		if (nBefore != -1) {
			shiftDividers(NEW_ZONE_SIZE, nBefore);
		}

		IETRect rectCompartment = getTransform().getWinAbsoluteOwnerRect();
		int lBelowEdge = (DMO_VERTICAL == m_orientation) ? rectCompartment.getIntWidth() : rectCompartment.getIntHeight();

		m_dividers.add(new DividerInfo(lBelowEdge));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#addDividers(long)
	 */
	public void addDividers(int lCnt) {
		if (lCnt > 0) {
			IETRect rectCompartment = getTransform().getWinAbsoluteOwnerRect();
			int sizeTotal = (DMO_VERTICAL == m_orientation) ? rectCompartment.getIntWidth() : rectCompartment.getIntHeight();
			int sizePerDivider = sizeTotal / (lCnt + 1);

			for (int indx = 0, lDividerLoc = sizePerDivider; indx < lCnt; indx++, lDividerLoc += sizePerDivider) {
				m_dividers.add(new DividerInfo(lDividerLoc));
			}
		}
	}

	public void shiftDividers(int offset)
	{
		shiftDividers(offset, 0);
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#shiftDividers(int, int)
	 */
	public void shiftDividers(int offset, int ulStartIndex) {
		for (int ulIndex = ulStartIndex; ulIndex < m_dividers.size(); ulIndex++) {
			DividerInfo pInfo = (DividerInfo) m_dividers.get(ulIndex);
			pInfo.setOffset(pInfo.getOffset() + offset);
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#deleteDivider(int)
	 */
	public void deleteDivider(int nIndex)
   {
      final int dividerCnt = m_dividers.getCount();
      if( dividerCnt > 0 )
      {
         while( nIndex >= dividerCnt )
         {
            nIndex--;
         }
         
         m_dividers.remove(nIndex);
      }
	}

   /// Removes all the dividers, and recreates new dividers
   public void resetDividers()
   {
      m_dividers.clear();

      if( m_parentCompartment != null )
      {
         // Create rectangles at their proper offset based on the size of
         // the parent container's contained compartments.
         // The size of the rectangles will be calculated in the draw()

         int offset = 0;

         // Number of dividers to be created
         final int iCnt = (m_parentCompartment.getNumCompartments() - 1);
         boolean bAllCompartmentsZeroSize = (iCnt > 0);

         for ( int indx = 0; indx < iCnt; indx++ )
         {
            ICompartment compartment = m_parentCompartment.getCompartment( indx );
            assert (compartment != null);
            if( compartment != null )
            {
               IETRect rectBounding = TypeConversions.getLogicalBoundingRect( compartment );

               switch( m_orientation )
               {
               case DMO_HORIZONTAL:
                  {
                     final int height = (int)rectBounding.getHeight();
                     if( height > 0 ) bAllCompartmentsZeroSize = false;
                     offset += Math.max( 40, height );
                  }
                  break;

               case DMO_VERTICAL:
                  {
                     final int width = (int)rectBounding.getWidth();
                     if( width > 0 ) bAllCompartmentsZeroSize = false;
                     offset += Math.max( 40, width );
                  }
                 break;

               default:
                  assert ( false );  // did we add another type?
                  // fall through

               case DMO_UNKNOWN:
                  break;
               }
            }
         
            DividerInfo info = new DividerInfo( offset );
            m_dividers.add( info );
         }

         // All compartments are zero size when a new draw engine is created.
         if( bAllCompartmentsZeroSize )
         {
            // Since all the compartments were zero size resize evenly within the parent
            final IETRect rectCompartment = getTransform().getWinAbsoluteOwnerRect();
            final int sizeTotal = (int)((DMO_VERTICAL == m_orientation)
               ? rectCompartment.getWidth()
               : rectCompartment.getHeight());
            final int sizePerDivider = sizeTotal / (iCnt + 1);

            for( int indx=0, dividerLoc=sizePerDivider;
                 indx<iCnt;
                 indx++, dividerLoc+=sizePerDivider )
            {
               m_dividers.item( indx ).m_lOffset = dividerLoc;
            }
         }
      }
   }

	public int getDividerOffset(int index) {
		DividerInfo info = m_dividers.get(index);
		return info != null ? info.getOffset() : 0;
	}

	protected IETRect getDivider(int index) {
		DividerInfo info = m_dividers.get(index);
		return info != null ? info.getRectDivider() : null;
	}

	protected void setDividerRect(int index, IETRect dividerRect)
   {
		DividerInfo info = m_dividers.get(index);
		if (info != null)
      {
         info.setRectDivider( (IETRect)dividerRect.clone() );
      }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#draw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, org.netbeans.modules.uml.core.support.umlsupport.IETRect, java.awt.Color, int)
	 */
	public void draw(IDrawInfo pInfo, IETRect rectBounding, Color crDivider, int nDividerWidth) {
		double dZoom = pInfo.getOnDrawZoom(); // getTransform().getZoomLevel();

      // Pen width is not used right now for drawing - just calculations.  This should be moved 
      // into a preference when we get around to it.  It's a general problem in DevTrack right now.
		int nPenWidth = Math.max( 1, (int) (nDividerWidth * dZoom));

      // copy the input rectangle and make sure we have a device rectangle
      ETDeviceRect rectDivider = ETDeviceRect.ensureDeviceRect( (IETRect)rectBounding.clone() );

		// Durring a node resize, to origin of the offset calculations must remain fixed
		IETPoint ptOffset = getDrawOffset(rectBounding);

		switch (m_orientation) {
			case DMO_HORIZONTAL :
				{
					Point ptFrom = new Point(rectBounding.getLeft(), 0);
					Point ptTo = new Point(rectBounding.getRight(), 0);

					Graphics2D graphics = pInfo.getTSEGraphics().getGraphics();

					for (int ulRowIndx = 0; ulRowIndx < m_dividers.size(); ulRowIndx++) {
						int lY = (int) (getDividerOffset(ulRowIndx) * dZoom + ptOffset.getY());
						ptFrom.y = ptTo.y = lY;

						//GDISupport.drawDashedLine(graphics,ptFrom, ptTo, crDivider);
                  GDISupport.drawLine(graphics, ptFrom, ptTo, crDivider, 1,m_nLineStyle);

						rectDivider.setTop(lY - (int) nPenWidth / 2);
						rectDivider.setBottom(lY + (int) nPenWidth / 2);
						setDividerRect(ulRowIndx, rectDivider);
					}
				}
				break;

			case DMO_VERTICAL :
				{
					Point ptFrom = new Point(0, rectBounding.getTop());
					Point ptTo = new Point(0, rectBounding.getBottom());

					Graphics2D graphics = pInfo.getTSEGraphics().getGraphics();

					for (int ulCoindx = 0; ulCoindx < m_dividers.size(); ulCoindx++) {
						int lX = (int) (getDividerOffset(ulCoindx) * dZoom + ptOffset.getX());
						ptFrom.x = ptTo.x = lX;

						//GDISupport.drawDashedLine(graphics, ptFrom, ptTo, crDivider);
						GDISupport.drawLine(graphics, ptFrom, ptTo, crDivider, 1, m_nLineStyle);

						rectDivider.setLeft(lX - (int) nPenWidth / 2);
						rectDivider.setRight(lX + (int) nPenWidth / 2);
						setDividerRect(ulCoindx, rectDivider);
					}
				}
				break;

			default :
				break;
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#isMouseOnDivider(org.netbeans.modules.uml.ui.support.viewfactorysupport.IMouseEvent)
	 */
	public boolean isMouseOnDivider(MouseEvent pMouseEvent) {
		boolean bMouseOnDivider = false;

		if (m_dividers.size() > 0 && m_parentCompartment != null) {
			// this is the relative position within the node (topleft = 0,0)
//			TSEWindowState state = (TSEWindowInputState) getTransform().getGraphWindow().getCurrentState();
			TSEWindowTool state = (TSEWindowInputTool) getTransform().getGraphWindow().getCurrentState();
//			if (state instanceof TSEWindowInputState) {
			if (state instanceof TSEWindowInputTool) {
//				TSConstPoint ptMouseLoc = ((TSEWindowInputState) state).getNonalignedWorldPoint(pMouseEvent);
				TSConstPoint ptMouseLoc = ((TSEWindowInputTool) state).getNonalignedWorldPoint(pMouseEvent);
				if (ptMouseLoc != null) {
					bMouseOnDivider =
						getIndexFromLocation(new Point((int) ptMouseLoc.getX(), (int) ptMouseLoc.getY())) < m_dividers.size();
				}
			}
		}

		return bMouseOnDivider;

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#handleSetCursor(org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
	 */
	public boolean handleSetCursor( IETPoint ptMouseLoc, ISetCursorEvent event )
   {
		// this is the relative position within the node (topleft = 0,0)
      Cursor cursor = null;

      if (m_dividers.size() > 0)
      {
         // Retain the current divider for use by HandleLeftMouseBeginDrag()
         m_ulCurrentDivider = getIndexFromLocation( event.getWinClientLocation() );
         if (m_ulCurrentDivider < m_dividers.size())
         {
            switch (m_orientation)
            {
               case DMO_HORIZONTAL :
                  cursor = ETHorzDragCursor.getCursor();
                  break;

               case DMO_VERTICAL :
                  cursor = ETVertDragCursor.getCursor();
                  break;

               default :
                  break;
            }

            // yes, set cursor
            if (cursor != null)
            {
               event.setCursor(cursor);
            }
         }
      }

      return (cursor != null);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#handleLeftMouseBeginDrag(org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
	 */
	public boolean handleLeftMouseBeginDrag(IETPoint pptETStartPos)
   {
		boolean bHandled = false;

		if ( (m_parentCompartment != null) &&
           (m_dividers.size() > 0) &&
           (m_ulCurrentDivider < m_dividers.size()) &&
           (getIndexFromLocation( pptETStartPos.asPoint() ) < m_dividers.size()) )
      {
			bHandled = true;

			IDragManager cpTool = createDragManagerTool();

			if (cpTool != null)
         {
				cpTool.setOrientation(m_orientation);

				ICompartment cpCompartment = m_parentCompartment;
				cpTool.setStretchCompartment(cpCompartment);

				// Determine the max & min height for the drag operation
				IETRect rectBounds = getTransform().getTSAbsoluteRect();

				int lAboveIndex = -1;
				int lBelowIndex = -1;

				switch (m_orientation) {
					case DMO_HORIZONTAL :
						{
							int lTop =
								(m_ulCurrentDivider > 0)
									? (rectBounds.getTop() - getDividerOffset(m_ulCurrentDivider - 1))
									: rectBounds.getTop();
							cpTool.setTop(lTop);

							int lBottom =
								(m_ulCurrentDivider < (m_dividers.size() - 1))
									? (rectBounds.getTop() - getDividerOffset(m_ulCurrentDivider + 1))
									: rectBounds.getBottom();
							cpTool.setBottom(lBottom);

							lAboveIndex = m_ulCurrentDivider;
							lBelowIndex = m_ulCurrentDivider + 1;
						}
						break;

					case DMO_VERTICAL :
						{
							int lBottom =
								(m_ulCurrentDivider > 0)
									? rectBounds.getLeft() + getDividerOffset(m_ulCurrentDivider - 1)
									: rectBounds.getLeft();
							cpTool.setBottom(lBottom);

							int lTop =
								(m_ulCurrentDivider < (m_dividers.size() - 1))
									? rectBounds.getLeft() + getDividerOffset(m_ulCurrentDivider + 1)
									: rectBounds.getRight();
							cpTool.setTop(lTop);

							lAboveIndex = m_ulCurrentDivider + 1;
							lBelowIndex = m_ulCurrentDivider;
						}
						break;

					default :
						break;
				}

				// Add the contained presentation elements, if available

				IADZonesCompartment cpZones =
					cpCompartment instanceof IADZonesCompartment ? (IADZonesCompartment) cpCompartment : null;
				ETList < ICompartment > cpCompartments = cpZones != null ? cpZones.getCompartments() : null;

				if (cpCompartments != null) {
					cpCompartment = cpCompartments.get(lAboveIndex);
					if (cpCompartment != null)
					{
						IADContainerDrawEngine parentEngine =
							cpCompartment.getEngine() instanceof IADContainerDrawEngine
								? (IADContainerDrawEngine) m_parentCompartment.getEngine()
								: null;
						if (parentEngine != null)
						{					
							cpTool.addElementsAbove(cpCompartment.getContained());
						}
					}

					cpCompartment = cpCompartments.get(lBelowIndex);
					if (cpCompartment != null) {
						IADContainerDrawEngine parentEngine =
							cpCompartment.getEngine() instanceof IADContainerDrawEngine
								? (IADContainerDrawEngine) m_parentCompartment.getEngine()
								: null;
						if (parentEngine != null) {
							cpTool.addElementsBelow(cpCompartment.getContained());
						}
					}
				}
			}
		}

		// turn off TS handling of the mouse, the node tool will take it from here.
		return bHandled;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#getDividerCnt()
	 */
	public int getDividerCnt() {
		return  m_dividers.size();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#getZoneIndex(int)
	 */
	public int getZoneIndex(int offset)
   {
		// Search for the zone containing the specified offset
		 int ulCnt = getDividerCnt();
		 for(int ulZoneIndex=0; ulZoneIndex<ulCnt; ulZoneIndex++ )
		 {
			if( offset <= this.getDividerOffset(ulZoneIndex))
			{
			   return ulZoneIndex;
			}
		 }
   
      // Returning this value indicates that the offset was beyond the dividers.
      // So, for example in ShiftDividers() none of the dividers will be shifted.
      return ulCnt;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
	 */
   public IETSize calculateOptimumSize(IDrawInfo pInfo)
   {
      int rlX = NEW_ZONE_SIZE;
      int rlY = NEW_ZONE_SIZE;

      switch (m_orientation)
      {
         case DMO_HORIZONTAL :
            rlY = Math.max(rlY, getMinimumSize());
            break;

         case DMO_VERTICAL :
            rlX = Math.max(rlX, getMinimumSize());
            break;

         default :
            break;
      }

      return new ETSize(rlX, rlY);
   }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#minimumSize()
	 */
	public int getMinimumSize() {
		int lDividerCnt = m_dividers.size();
		return ((lDividerCnt > 0) ? this.getDividerOffset(lDividerCnt-1) : 0) + NEW_ZONE_SIZE;

	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#updateCurrentDivider(org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
	 */
	public void updateCurrentDivider(IETPoint ptMoveTo)
   {
      int offset = calculateOffset( ptMoveTo );
      
		DividerInfo pInfo = (DividerInfo) m_dividers.get(m_ulCurrentDivider);
		if (pInfo != null)
			pInfo.setOffset(offset);
	}
   
   /**
    * Returns the logical offset within the containing node, based on the orientation
    */
   protected int calculateOffset( IETPoint ptTSLogical )
   { 
      IETPoint ptClientMoveTo = getTransform().getTSAbsoluteToWinAbsoluteOwner( ptTSLogical );
      
      final int offset = (DMO_HORIZONTAL == m_orientation)
         ? ptClientMoveTo.getY()
         : ptClientMoveTo.getX();
         
      return offset;
   }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#writeToArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
	 */
	public void writeToArchive(IProductArchiveElement pCompartmentElement) throws ETException {
		if( (pCompartmentElement != null) &&
			(getDividerCnt() > 0) )
		{
		   IProductArchiveElement cpDividers = pCompartmentElement.createElement(ZD_DIVIDERS);		   
		   if( cpDividers != null )
		   {
			  for(int ulIndex=0; ulIndex < getDividerCnt(); ulIndex++ )
			  {
				 IProductArchiveElement cpDivider = cpDividers.createElement(ZD_DIVIDER);
				 if( cpDivider != null )
				 {
					cpDivider.addAttributeLong(ZD_OFFSET, this.getDividerOffset(ulIndex));
				 }
			  }
		   }
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#readFromArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
	 */
	public void readFromArchive(IProductArchiveElement pCompartmentElement) throws ETException {
		if ( pCompartmentElement != null )
		{
		  IProductArchiveElement  cpDividers = pCompartmentElement.getElement(ZD_DIVIDERS);
		   if ( cpDividers != null )
		   {
			  // Retrieve all the divider offsets, and create the new dividers
			  IProductArchiveElement[]  cpDividerElements = cpDividers.getElements();
			  if ( cpDividerElements != null)
			  {
				 long lCnt = cpDividerElements.length;
				 for( int indx=0; indx < lCnt; indx++ )
				 {
					IProductArchiveElement cpElement = cpDividerElements[indx];
					if( cpElement != null )
					{
					   int offset = (int)cpElement.getAttributeLong(ZD_OFFSET);
					   m_dividers.add(new DividerInfo( offset ));
					}
				 }
			  }
		   }
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#startNodeResize()
	 */
	public void startNodeResize()
   {
		m_rectPreResize = getTransform().getTSAbsoluteRect();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#finishNodeResize()
	 */
	public void finishNodeResize()
   {
		if(m_rectPreResize != null &&  !m_rectPreResize.isZero() )
		 {
			// Shift all the dividers, so that they look to the user like they haven't moved
			Point ptPreResizeOrigin = m_rectPreResize.getTopLeft();
			Point ptPostResizeOrigin = getTransform().getTSAbsoluteRect().getTopLeft();

			int lNewOffset = 0;
			switch( m_orientation )
			{
			case DMO_HORIZONTAL:
			   lNewOffset = ptPostResizeOrigin.y - ptPreResizeOrigin.y;
			   break;

			case DMO_VERTICAL:
			   lNewOffset = ptPreResizeOrigin.x - ptPostResizeOrigin.x;
			   break;

			default:
			   break;
			}

			shiftDividers(lNewOffset);

			// Indicate we are no longer resizing
			m_rectPreResize.setSides(0,0,0,0);
		 }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers#getDrawOffset(org.netbeans.modules.uml.core.support.umlsupport.IETRect)
	 */
	public IETPoint getDrawOffset(IETRect rectBounding) {
		if (rectBounding == null)
			return null;
			
		// Durring a node resize, to origin of the offset calculations must remain fixed
		 IETPoint ptOffset = PointConversions.newETPoint( rectBounding.getTopLeft() );
/* In java the passed in rectangle already contains the proper offset
		 if (m_rectPreResize != null && !m_rectPreResize.isZero())
		 {
          ptOffset = getTransform().getTSAbsoluteToWinScaledOwner( ptOffset );
		 }
*/
		 return ptOffset;
	}

	protected ETTransform getTransform() throws InvalidPointerException {
		if (m_parentCompartment == null) {
			throw new InvalidPointerException();
		}

		return m_parentCompartment instanceof ETTransform ? (ETTransform) m_parentCompartment : null;
	}

	/**
	 * Find the divider list iterator associated with the divider bar located under the mouse location
    * In C++ this mouse location was passed in as a win client owner coordinate
    * In Java this mouse location is a win client coordinate.
	 */
	protected int getIndexFromLocation(Point ptClientMouseLocation)
   {
		Iterator < DividerInfo > iter = m_dividers.iterator();
		int ulIndex = 0;
		while (iter.hasNext())
      {
			DividerInfo pInfo = iter.next();
			IETRect rect = pInfo.getRectDivider();
			if (rect.contains(ptClientMouseLocation))
         {
				break;
			}
			ulIndex++;
		}

		return ulIndex;
	}

	/// Sets the specific divider offset
	public void setDividerOffset(int nIndex, int offset)
   {
		if( nIndex < m_dividers.size() && nIndex >= 0)
      {
			DividerInfo info = m_dividers.get(nIndex);
			if (info != null)
         {
            info.setOffset(offset);
         }
		}
	}

	protected IDragManager  createDragManagerTool()
	{
		DragManager dragMgr = new DragManager(getTransform().getGraphWindow());
		if (dragMgr != null)
		{
         //getTransform().getGraphWindow().getCurrentState().setState(dragMgr);
                    getTransform().getGraphWindow().getCurrentTool().setTool(dragMgr);
		}
		return dragMgr;
	}
	
	/**
	 * Find the divider located "below" the TS logical location
	 */
	public int getIndexFromTSLogical( final IETPoint ptTSLogical )
	{
		final int lOffset = calculateOffset( ptTSLogical );
		return getZoneIndex( lOffset );
	}
	
}
