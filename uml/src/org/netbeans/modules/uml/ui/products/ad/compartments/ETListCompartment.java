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


//	 $Date$

package org.netbeans.modules.uml.ui.products.ad.compartments;

import java.awt.Font;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADContainerDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.DiagramEngineResources;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ContainmentTypeEnum;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.UIFactory;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;
import org.netbeans.modules.uml.ui.support.helpers.UserInputBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker.GBK;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETTransform;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartments;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.TSEFont;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.graph.TSGraphObject;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;

/**
 * @author Embarcadero Technologies Inc
 *
 *
 */
public abstract class ETListCompartment extends ETSimpleListCompartment implements IListCompartment {

	protected final static int BORDER_INDENT = 4;

	private TSEFont m_staticTextFont = new TSEFont("SansSerif-italic-11");
	private TSEColor m_defaultTextColor = new TSEColor(TSEColor.black);

	//	height of all compartments
	int m_maxHeight;

	//	widest of all compartments
	int m_maxWidth;

	//	width of widest visible compartment
	int m_widestVisibleCompartment;

	ETList < ICompartment > m_VisibleCompartments = null;

	public ETListCompartment() {
		super();
	}

	public ETListCompartment(IDrawEngine pDrawEngine) {
		super(pDrawEngine);
	}

	public void deleteSelectedCompartments(boolean bPrompt) {

		String sTitle = DiagramEngineResources.getString("IDS_POPUP_DELETE_COMPARTMENT");
		String sText = DiagramEngineResources.getString("IDS_DELETE_COMPARTMENT");
		deleteSelectedCompartments(sTitle, sText, bPrompt);

	}

	/**
	 * Delete all currently selected compartments
	 * @param bPrompt Instructs the operation to prompt the user with a messagebox first
	 * @param sTitle Title for messagebox, if bPrompt == true
	 * @param sText Text for the messageboc, if bPrompt == true
	 */
	protected void deleteSelectedCompartments(String sTitle, String sText, boolean bPrompt) {

		// Fix W4253:  Don't delete if the diagram is read-only
		if (!this.isParentDiagramReadOnly()) {

			ETList < ICompartment > pCompartments = new ETArrayList();
			pCompartments.addAll(getSelectedCompartments());

			int nCount = this.getEngine() != null ? pCompartments.size() : 0;

			if (nCount > 0) {
				String bstrText = sText;

				//				UpdateTextIfMessagesWillBeEffected( pCompartments, bstrText );
				//
				//				SimpleQuestionDialogResultKind nResult = SQDRK_RESULT_YES;
				int nResult = 0;

				if (bPrompt) {
					IQuestionDialog pDlg = UIFactory.createQuestionDialog();
					QuestionResponse result = pDlg.displaySimpleQuestionDialogWithCheckbox(MessageDialogKindEnum.SQDK_YESNO, MessageIconKindEnum.EDIK_ICONWARNING, sText, "", sTitle, MessageResultKindEnum.SQDRK_RESULT_NO, true);

					nResult = result.getResult();
				}

				if (nResult == MessageResultKindEnum.SQDRK_RESULT_YES) {
					// we want the batch mode to destruct before the invalidate happens

					//				   {
					//					  CEnterBatchMode batchMode ( CComBSTR("Multiple Deletes"), nCount > 1 ? true : false );

					// tell compartment to update

					Iterator < ICompartment > iterator = pCompartments.iterator();
					while (iterator.hasNext()) {

						ICompartment pCompartment = iterator.next();

						if (pCompartment != null) {
							removeCompartment(pCompartment, true);
						}
					}

					// if this is a node resize to fit
					resizeDrawEngineToFitThisCompartment();
					//				   }

					//					if (hr == S_OK) {
					this.getEngine().setAnchoredCompartment(null);
					//					}

					this.getEngine().invalidate();
				}
			}
		}
	}

	/**
	 * Calculates a new size based on the current state of the scrollbar. If 
	 * scrolling is not in effect, the return height is the total of all 
	 * compartment heights, plus the list title (if allowed by preferences). 
	 * If scrolling is in effect the return height will be the current height.  
	 * The return width is always the optimum width.  
	 * <br>
	 * <b>RETURN SIZE IS CALCULATED AT THE 100% ZOOM LEVEL.</b>
	 *
	 * @return The desired size.  The desired size will be equal to the optimum 
	 *         size if scrolling is not in effect, otherwise it will be equal to 
	 *         the optimum width and the current height.
	 */
	public IETSize getDesiredSizeToFit() {
		// recalc all compartment sizes, ALL CALCS BELOW ARE DONE AT 100% ZOOM
		IETSize retVal = calculateOptimumSize(null, true);

		int top = 0;
		int bottom = 0;

		// find width of widest visible compartment
		long numCompartments = getNumCompartments();
		m_widestVisibleCompartment = 0;

		for (int index = 0; index < numCompartments; index++) {

			ICompartment compartment = getCompartment(index);

			if (compartment != null) {
				// compartments should have been sized already
				IETSize size = compartment.getOptimumSize(true);

				if (size != null) {
					bottom = top + size.getHeight();

					// visible compartments have positive height
					if (size.getHeight() > 0) {
						// compartment is visible if part of it falls within viewport area
						// Since the Java version does not support scroll bars the 
						// viewport is not completly suppported yet.  This needs to be
						// commented out after the scroll bar support is added.
						// if((bottom > m_viewport.getTop()) && (top < m_viewport.getBottom()))
						{
							m_widestVisibleCompartment = Math.max(m_widestVisibleCompartment, size.getWidth());
						}
					}

					top += size.getHeight();
				}

				// if(bottom >= m_viewport.getBottom())
				// {
				//    break;
				// }
			}

			// The C++ version does some scaling calculations.  I do not thing this
			// is needed for the Java version.
		}

		if (retVal != null) {
			retVal.setWidth(m_widestVisibleCompartment + (BORDER_INDENT * 2));
		}
		// When scroll bars are added we will need to set the height.

		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment#getDeleteIfEmpty()
	 */
	public boolean getDeleteIfEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean getHasSelectedCompartments() {
		return getSelectedCompartments().size() > 0;
	}

	/**
	 * Returns a collection of selected compartments.
	*/
	public ETList < ICompartment > getSelectedCompartments() {

		ETList < ICompartment > selectedCompartments = new ETArrayList < ICompartment > ();

		Iterator < ICompartment > iterator = this.getCompartments().iterator();
		while (iterator.hasNext()) {

			ICompartment curCompartment = iterator.next();

			if (curCompartment instanceof IListCompartment) {
				IListCompartment listCompartment = (IListCompartment) curCompartment;

				Iterator < ICompartment > compartmentIterator = listCompartment.getCompartments().iterator();
				while (compartmentIterator.hasNext()) {
					ICompartment foundCompartment = compartmentIterator.next();
					if (foundCompartment.isSelected()) {
						selectedCompartments.add(foundCompartment);
					}
				}
			} else {
				if (curCompartment.isSelected()) {
					selectedCompartments.add(curCompartment);
				}
			}
		}
		return selectedCompartments;
	}

	/**
	 *
	 * Adds any selected compartments contained by this list to the collection.
	 *
	 * @param pCompartments[in] A Compartments list. Any selected compartments will be 
	 * added to the end of the list.
	 *
	 */
	public void getSelectedCompartments2(ETList < ICompartment > pCompartments) {
		ETList < ICompartment > compartments = getSelectedCompartments();
		for (Iterator < ICompartment > iter = compartments.iterator(); iter.hasNext();) 
		{			
			pCompartments.add(iter.next());
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment#getVisibleCompartments()
	 */
	public ETList < ICompartment > getVisibleCompartments() {
		return m_VisibleCompartments != null ? m_VisibleCompartments : this.getCompartments();
	}

	public void clearVisibleCompartments() {
		if (m_VisibleCompartments != null) {
			m_VisibleCompartments.clear();
		}
	}

	/**
	 * Moves selection down to the next visible compartment
	 */
	public boolean lineDown() 
	{
            boolean retVal = false;
		IDrawEngine pEngine = getEngine();
		if (pEngine != null)
		{
			ICompartment pCompartment = pEngine.getAnchoredCompartment();
			
			// unselect everything
			pEngine.selectAllCompartments(false);
			
			if (pCompartment != null)
			{
				ICompartment pNewComp = getNextCompartment(pCompartment);
				if (pNewComp != null)
				{
					pNewComp.setSelected(true);
					ensureVisible(pNewComp, false);
					pEngine.setAnchoredCompartment(pNewComp);
                                        pEngine.invalidate();
                                        retVal = true;
				}
			}
		}
		return retVal;
	}

	/**
	 * Moves selection up to the previous visible compartment
	 */
	public boolean lineUp() 
	{
            boolean retVal = false;
		IDrawEngine pEngine = getEngine();
		if (pEngine != null)
		{
			ICompartment pCompartment = pEngine.getAnchoredCompartment();
			
			// unselect everything
			pEngine.selectAllCompartments(false);
			
			if (pCompartment != null)
			{
				ICompartment pNewComp = getPreviousCompartment(pCompartment);
				if (pNewComp != null)
				{
					pNewComp.setSelected(true);
					ensureVisible(pNewComp, false);
					pEngine.setAnchoredCompartment(pNewComp);
                                        pEngine.invalidate();
                                        retVal = true;
				}
			}
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment#setDeleteIfEmpty(boolean)
	 */
	public void setDeleteIfEmpty(boolean value) {
		// TODO Auto-generated method stub

	}

	public void setSelectedCompartments(ETList < ICompartment > pCompartments) 
	{
		IteratorT<ICompartment> iter = new IteratorT<ICompartment>(pCompartments);
		while (iter.hasNext())
		{
			iter.next().setSelected(true);
		}
	}

	public boolean handleLeftMouseButton(MouseEvent pEvent) {
		boolean eventHandled = false;
		Iterator < ICompartment > iterator = this.getCompartments().iterator();
		while (iterator.hasNext() && !eventHandled) {
			eventHandled = iterator.next().handleLeftMouseButton(pEvent);
		}
		return eventHandled;
	}

	public boolean handleLeftMouseBeginDrag(IETPoint pStartPos, IETPoint pCurrentPos, boolean bCancel) {
		boolean eventHandled = false;
		Iterator < ICompartment > iterator = this.getCompartments().iterator();
		while (iterator.hasNext() && !eventHandled) {
			eventHandled = iterator.next().handleLeftMouseBeginDrag(pStartPos, pCurrentPos, bCancel);
		}
		return eventHandled;
	}

	public boolean handleLeftMouseButtonDoubleClick(MouseEvent pEvent) {
		boolean eventHandled = false;
		Iterator < ICompartment > iterator = this.getCompartments().iterator();
		while (iterator.hasNext() && !eventHandled) {
			eventHandled = iterator.next().handleLeftMouseButtonDoubleClick(pEvent);
		}
		return eventHandled;
	}

	public boolean handleLeftMouseDrag(IETPoint pStartPos, IETPoint pCurrentPos) {
		boolean eventHandled = false;
		Iterator < ICompartment > iterator = this.getCompartments().iterator();
		while (iterator.hasNext()) {
			eventHandled = iterator.next().handleLeftMouseDrag(pStartPos, pCurrentPos);
		}
		return eventHandled;
	}

	/**
	 * Handle dropping via the left mouse.
	*/
	public boolean handleLeftMouseDrop(IETPoint pCurrentPos, List pElements, boolean bMoving)
   {
      // TODO implement this code from C++
		return false;
	}

   /**
    * Invokes the in=place editor for the 1st editable compartment in the list of compartments.
    *
    * @param bNew[in] - Flag indicating that this is a new compartment and should be destroyed if the edit is cancelled.
    * Default is FALSE.
    * @param KeyCode[in] - The key pressed that invoked editing, null if none.  Default is NULL.
    * @param nPos[in] - The horizontal position for the cursor, used if editing was activated via the mouse. The position value 
    * is in pixels in client coordinates, e.g. the left edge of the control is position 0.  Default is -1 which does not position
    * the cursor (some translators may select a field by default).
    */
   public long editCompartment(boolean bNew, int nKeyCode, int nShift, int nPos) 
   {
      // The default is to use the 1st editable compartment
      IADEditableCompartment editableCompartment = getCompartmentByKind( IADEditableCompartment.class );
      if( editableCompartment != null )
      {
         editableCompartment.editCompartment( bNew, nKeyCode, nShift, nPos );
      }

      return 0;
   }

	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct) {

		IETSize retValue = null;
		int minWidth = 0;
		int minHeight = 0;
		int nNameHeight = 0;

		TSEGraphics graphics = getGraphics(pDrawInfo);
		
		if (graphics != null) {
			TSTransform transform =graphics.getTSTransform();
			
			// special handling if the compartment is not collapsed but is empty
			boolean bDisplay = false;

			ICoreProduct prod = ProductRetriever.retrieveProduct();
			IPreferenceManager2 prefMan = prod.getPreferenceManager();

			if (prefMan != null) {
				String sShowList = prefMan.getPreferenceValue("Diagrams", "ShowEmptyLists");
				if (sShowList != null && sShowList.equals("PSK_YES")) {
					bDisplay = true;
				}
			}

			// don't display if empty, determine if empty
			if (!bDisplay) {
				bDisplay = this.getNumCompartments() > 0;
			}

			if (bDisplay) 
			{
				// get preference for showing list titles
				String sShowTitles = prefMan.getPreferenceValue("Diagrams", "ShowCompartmentTitles");

				if (sShowTitles.equals("PSK_ALWAYS") || sShowTitles.equals("PSK_SELECTED")) {
					// get height of title if present use list compartment title font for the title
					IETSize titleSize = super.calculateOptimumSize(pDrawInfo, true);

					// don't use the return value from above, if this compartment is collapsed they will always return 0
					// use the internal stored value instead.
					IETSize cachedOptimumSize = getCachedOptimumSize();
					if (cachedOptimumSize != null) {
						nNameHeight = cachedOptimumSize.getHeight();
						minHeight = cachedOptimumSize.getHeight();
						minWidth = cachedOptimumSize.getWidth();
					}
				}

				// get height of visible compartments into m_maxHeight
				calcLogicalHeight( pDrawInfo );
            
            minHeight += m_maxHeight;
            minWidth = Math.max( minWidth, m_maxWidth );

				// add border indentation
				int nIndent = BORDER_INDENT;

				// add border spacing if there's anything there
				if (minHeight > 0) 
				{
					// both sides are indented
					minWidth += nIndent * 2;

					// bottom is indented
					minHeight += nIndent;
				}

				// if no title is shown, add some indent from the top of the compartment,
				// otherwise the compartment list will be drawn immediately beneath the title
				if (nNameHeight == 0) {
					minHeight += nIndent;
				}

				m_maxHeight = Math.max(minHeight, 10);
				m_maxWidth = minWidth;
			}

			// sizes calc'd above are at no zoom (100%)
			internalSetOptimumSize(minWidth, minHeight);

			// return zoomed size
			retValue = bAt100Pct ? this.getOptimumSize(bAt100Pct) : this.scaleSize(m_cachedOptimumSize, transform);
		}
		return retValue;
	}
   
   /**
    * Calculates the scrollable height of all potentially visible compartments.  Call this 
    * method to update the m_maxScrollPos and m_maxHeight members.  On m_maxScrollPos, m_maxHeight
    * and m_maxWidth are always set to the 100% zoomed amounts.
    *
    * @param dc The drawing area's device context, used to calculate based on 
    * the compartment's font and zoom level.
    */
   protected void calcLogicalHeight( IDrawInfo drawInfo )
   {
      // Math.max scrollable distance (from top of 1st compart to top of last compart)
      // we need to know this so the scrollthumb will be on the bottom of the 
      // scrollbar when the last compartment is visible, if we used the total
      // height (m_maxHeight) we would never reach the bottom of the scrollbar
      //m_maxScrollPos    = 0;
   
      // total height of all scrollable comparts (m_maxScrollPos + height of last compart)
      m_maxHeight = 0;
      m_maxWidth  = 0;
     
     int nHeight =0;
      // Maximum scrollable height is defined as the distance between the top of the first
      // visible element and the top of last visible element
      ETList< ICompartment > compartments = getCompartments();
      for (Iterator< ICompartment > iter = compartments.iterator(); iter.hasNext();)
      {
         ICompartment compartment = iter.next();
         
         // last compartment's height doesn't get added here
         //m_maxScrollPos += nHeight;
      
         // always calc the zoomed size, 
         IETSize size = compartment.calculateOptimumSize( drawInfo, true );
      
         // last compartment's height does get added here
			nHeight = size.getHeight();
         m_maxHeight += nHeight;
         m_maxWidth = Math.max( m_maxWidth, size.getWidth() );
      }
   }

	protected int getBorderIndent(IDrawInfo pDrawInfo)
	{
		return pDrawInfo != null ? pDrawInfo.getTSTransform().widthToDevice((double)BORDER_INDENT) : BORDER_INDENT;
	}
	
   public void draw(IDrawInfo pDrawInfo, IETRect pBoundingRect)
   {
      super.draw(pDrawInfo, pBoundingRect);

      TSEGraphics graphics = pDrawInfo.getTSEGraphics();
      TSTransform transform = graphics.getTSTransform();

      IETRect boundingRect = (IETRect)this.getBoundingRect().clone();
      int lastDrawPointY = boundingRect.getIntY() + drawName(pDrawInfo, boundingRect);

      
      Iterator < ICompartment > iterator = this.getCompartments().iterator();
      int borderIndent = getBorderIndent(pDrawInfo);
      
      while (iterator.hasNext())
      {
         // Draw the compartment
         ICompartment compartment = iterator.next();

         if (compartment.getCollapsed() == false)
         {
            IETSize compartmentSize = compartment.calculateOptimumSize(pDrawInfo, false);

            int height;
            if (compartment.getTextWrapping())
            {
               height = Math.abs(lastDrawPointY - boundingRect.getBottom()); // Give it the rest of the bounding rect.				 
            }
            else
               height = compartmentSize.getHeight();

            IETRect compartmentDrawRect = new ETRect(boundingRect.getIntX() + borderIndent, lastDrawPointY, boundingRect.getIntWidth(), height);

            compartment.draw(pDrawInfo, compartmentDrawRect);

            // advance to the next line 
            lastDrawPointY = lastDrawPointY + height;
         }
      }

      this.drawScroll(pDrawInfo);
   }

	private int drawName(IDrawInfo pDrawInfo, IETRect pBoundingRect) {
		int retValue = 0;
		TSEGraphics pGraphics = pDrawInfo.getTSEGraphics();
		if (!this.m_collapsed && pGraphics != null) {
			// check flag for drawing name
			boolean bShowName = this.getShowName();

			if (bShowName) {
				ICoreProduct prod = ProductRetriever.retrieveProduct();
				IPreferenceManager2 prefMan = prod.getPreferenceManager();

				// get preference for showing list titles
				String sShowTitles = prefMan != null ? prefMan.getPreferenceValue("Diagrams", "ShowCompartmentTitles") : "";

				IETGraphObject graphObj = (getOwnerGraphObject() instanceof IETGraphObject)? (IETGraphObject)getOwnerGraphObject():null;
				bShowName = sShowTitles.equals("PSK_ALWAYS") || 
				(sShowTitles.equals("PSK_SELECTED") && (graphObj != null && graphObj.isSelected()));

				if (bShowName) {
					String sCompartmentName = getName();

					if (sCompartmentName != null && sCompartmentName.length() > 0) {
						Font originalFont = pGraphics.getFont();
						pGraphics.setFont(m_staticTextFont.getScaledFont(pDrawInfo.getFontScaleFactor()));
						pGraphics.setColor(this.m_defaultTextColor);

						int left = (pBoundingRect.getIntX() + pBoundingRect.getIntWidth() / 2) - (pGraphics.getFontMetrics().stringWidth(sCompartmentName) / 2);
						int top = pBoundingRect.getIntY() + pGraphics.getFontMetrics().getHeight();

                  // draw the static text
						pGraphics.drawString(sCompartmentName, left, top);
                  
						retValue = pGraphics.getFontMetrics().getHeight();
						pGraphics.setFont(originalFont);
					}
				}
			}
		}

		return retValue;
	}
	
   // Uncomment if you want to show the UI changes.
//   private int calculateName(IDrawInfo pDrawInfo, IETRect pBoundingRect) {
//		int retValue = 0;
//		TSEGraphics pGraphics = pDrawInfo.getTSEGraphics();
//		if (!this.m_collapsed && pGraphics != null) {
//			// check flag for drawing name
//			boolean bShowName = this.getShowName();
//
//			if (bShowName) {
//				ICoreProduct prod = ProductRetriever.retrieveProduct();
//				IPreferenceManager2 prefMan = prod.getPreferenceManager();
//
//				// get preference for showing list titles
//				String sShowTitles = prefMan != null ? prefMan.getPreferenceValue("Diagrams", "ShowCompartmentTitles") : "";
//
//				IETGraphObject graphObj = (getOwnerGraphObject() instanceof IETGraphObject)? (IETGraphObject)getOwnerGraphObject():null;
//				bShowName = sShowTitles.equals("PSK_ALWAYS") || 
//				(sShowTitles.equals("PSK_SELECTED") && (graphObj != null && graphObj.isSelected()));
//
//				if (bShowName) {
//					String sCompartmentName = getName();
//
//					if (sCompartmentName != null && sCompartmentName.length() > 0) {
//						Font originalFont = pGraphics.getFont();
//						pGraphics.setFont(m_staticTextFont.getScaledFont(pDrawInfo.getFontScaleFactor()));
//						pGraphics.setColor(this.m_defaultTextColor);
//
//						int left = (pBoundingRect.getIntX() + pBoundingRect.getIntWidth() / 2) - (pGraphics.getFontMetrics().stringWidth(sCompartmentName) / 2);
//						int top = pBoundingRect.getIntY() + pGraphics.getFontMetrics().getHeight();
//
//                  // CHANGED CHANGED
//						// draw the static text
//						//pGraphics.drawString(sCompartmentName, left, top);
//                  // CHANGED CHANGED
//                  
//						retValue = pGraphics.getFontMetrics().getHeight();
//						pGraphics.setFont(originalFont);
//					}
//				}
//			}
//		}
//
//		return retValue;
//	}
   
	/**
	 * Draws the entire scrollbar.
	 */
	private boolean drawScroll(IDrawInfo pDrawInfo) {
		boolean bRetVal = false;
		//m_scrollBarVisible = false;

		long nCount = this.getNumCompartments();

		//		double zoomLevel = GetZoomLevel(pInfo);
		//
		//		if (m_bEnableScroll && nCount > 1 && (int (m_maxHeight * zoomLevel) > m_rcScrollBar.Height())) {
		//			if (pOwnerNode() && pOwnerNodeView() && pOwnerNode() - > isSelected() && pOwnerNodeView() - > resizable()) {
		//				// fetch draw area colors
		//				ATLASSERT(m_Engine);
		//
		//				double nPoints = zoomLevel * SCROLLWIDTH;
		//
		//				// calc scrollbar's rect
		//				CRect rcBar(m_rcScrollBar);
		//				rcBar.left = rcBar.right - int (nPoints);
		//				m_rcScrollBar.left = rcBar.left;
		//
		//				// calc rect for buttons
		//				nPoints = zoomLevel * SCROLLBUTTONHEIGHT;
		//
		//				m_rcScrollDown = rcBar;
		//				m_rcScrollDown.bottom = m_rcScrollDown.top + int (nPoints);
		//
		//				m_rcScrollUp = rcBar;
		//				m_rcScrollUp.top = m_rcScrollUp.bottom - int (nPoints);
		//
		//				// adjust button heights if we're overlapping
		//				if (m_rcScrollDown.bottom > m_rcScrollUp.top) {
		//					// buttons overlap, squish them together
		//					if ((m_rcScrollUp.bottom - m_rcScrollDown.top) >= nPoints) {
		//						m_rcScrollDown.bottom = (m_rcScrollDown.top + m_rcScrollUp.bottom) / 2;
		//						m_rcScrollUp.top = m_rcScrollDown.bottom;
		//						DrawButton(pInfo, m_rcScrollUp, true);
		//						DrawButton(pInfo, m_rcScrollDown, false);
		//					}
		//				} else {
		//					// paint the track         
		//					CBrush * pBrushH = pTSEDrawInfo - > dc().GetHalftoneBrush();
		//					pTSEDrawInfo - > dc().FillRect(rcBar, pBrushH);
		//
		//					// draw up/down buttons
		//					DrawButton(pInfo, m_rcScrollUp, true);
		//					DrawButton(pInfo, m_rcScrollDown, false);
		//
		//					DrawThumb(pInfo);
		//				}
		//				bRetVal = true;
		//				m_bScrollBarVisible = true;
		//			}
		//		}
		return bRetVal;
	}

	/**
	 * Draws the button in the list compartment scrollbar
	 */
	boolean drawButton(IDrawInfo pDrawInfo, ETRect rect, boolean bDirection) {
		boolean bRetVal = true;

		// grab colors off the dc
		//		COLORREF clrBk = pTSEDrawInfo - > dc().GetBkColor();
		//		COLORREF clrText = pTSEDrawInfo - > dc().GetTextColor();

		// create background brush
		//		CBrush br(clrBk);
		//		CBrush * pOldBrush = (CBrush *) pTSEDrawInfo - > dc().SelectObject(& br);

		//		double zoomLevel = GetZoomLevel(pInfo) * 2;
		//		int nPoints = (int) zoomLevel;
		//
		//		// draw button outline
		//		pTSEDrawInfo - > dc().RoundRect(rect, CPoint(nPoints, nPoints));
		//
		//		// shrink rect slightly for the arrowhead
		//		nPoints = (int) zoomLevel;
		//		rect.InflateRect(-nPoints, -nPoints);
		//
		//		CPoint pts[3];
		//
		//		// create pen and brush for drawing arrowhead
		//		CPen pen(PS_SOLID, 1, clrText);
		//		CPen * pOldPen = pTSEDrawInfo - > dc().SelectObject(& pen);
		//
		//		br.DeleteObject();
		//		br.CreateSolidBrush(clrText);
		//		pTSEDrawInfo - > dc().SelectObject(& br);
		//
		//		// set points of the arrow
		//		pts[0].x = rect.left;
		//		pts[0].y = rect.top;
		//
		//		pts[1].x = rect.right;
		//		pts[1].y = rect.top;
		//
		//		pts[2].x = rect.left + rect.Width() / 2;
		//		pts[2].y = rect.bottom;
		//
		//		if (!bDirection) {
		//			// invert the arrow
		//			pts[0].y = rect.bottom;
		//			pts[1].y = rect.bottom;
		//			pts[2].y = rect.top;
		//		}
		//
		//		// draw arrowhead
		//		pTSEDrawInfo - > dc().Polygon(pts, 3);
		//
		//		// clean up
		//		pTSEDrawInfo - > dc().SelectObject(pOldPen);
		//		pTSEDrawInfo - > dc().SelectObject(pOldBrush);

		return bRetVal;
	}

	/**
	 * Draws the thumb in the list compartment
	 */
	boolean drawThumb(IDrawInfo pDrawInfo) {
		boolean bRetVal = true;

		//		ATLASSERT(m_maxScrollPos > 0);
		//		ATLASSERT(m_maxHeight > 0);
		//
		//		if (m_maxScrollPos > 0 && m_maxHeight > 0) {
		//			// calc height of thumbbuttom
		//			CRect rect(m_rcScrollBar);
		//			rect.top = m_rcScrollDown.bottom;
		//			rect.bottom = m_rcScrollUp.top;
		//
		//			// height of thumb is the percent of displayed area
		//			double nZoomLevel = GetZoomLevel(pInfo);
		//			double percent = double (m_rcViewport.Height()) / int (double (m_maxHeight) * nZoomLevel);
		//			int nThumbSize = (int) (double (rect.Height()) * percent);
		//
		//			// position of thumb is percent of top hidden area
		//			percent = double (m_nTopLogicalPos) / int (double (m_maxScrollPos) * nZoomLevel);
		//			int nThumbPos = (int) (double (rect.Height()) * percent);
		//
		//			rect.top += nThumbPos;
		//			rect.bottom = min(rect.top + nThumbSize, rect.bottom);
		//
		//			// grab colors off the dc
		//			COLORREF clrBk = pTSEDrawInfo - > dc().GetBkColor();
		//
		//			// create background brush
		//			CBrush br(clrBk);
		//			CBrush * pOldBrush = (CBrush *) pTSEDrawInfo - > dc().SelectObject(& br);
		//
		//			// calc radius of corners
		//			int nPoints = (int) nZoomLevel * 2;
		//
		//			// draw button outline
		//			pTSEDrawInfo - > dc().RoundRect(rect, CPoint(nPoints, nPoints));
		//			m_rcScrollThumb = rect;
		//
		//			// clean up
		//			pTSEDrawInfo - > dc().SelectObject(pOldBrush);
		//		}

		return bRetVal;
	}

	/*
	 * Sets the logical viewport to make sure that on the next redraw this compartment is visible.
	 *
	 * @param compartment - The compartment element to make visible.  If the compartment element is not currently
	 * visible or is partially visible, the list compartment is scrolled, up or down.  Thus if the compartment
	 * element is currently above the top-most visible compartment element, it will be scrolled into the 
	 * top-most position, likewise if the compartment element is below the last-most visible compartment element
	 * it will be scrolled into the bottom position.
	 */
	public void ensureVisible(ICompartment pCompartment, boolean bGrow)
	{
		if (pCompartment != null)
		{
			pCompartment.setVisible(true);
		}
		
		if (bGrow) 
		{
			// This needs to be at 100 % or we have trouble in the NodeDrawEngine, (kevin)
			IETSize size = calculateOptimumSize(null, true);


			// set our optimum width to that of the compartment
			IETSize optSize = getCachedOptimumSize();
			if (optSize != null && size != null) 
			{
				size.setWidth(Math.max(optSize.getWidth(), size.getWidth()));
			}
			
			// restore width to whatever we figured above
			this.internalSetOptimumSize(size);

			// call drawengine to resize us
			resizeDrawEngineToFitThisCompartment();
		}
	}

	/**
	 * Resizes the owning drawengine to fit this compartmenst
	 */

	private void resizeDrawEngineToFitThisCompartment() {
		// call drawengine to resize us
		if (this.getEngine() instanceof INodeDrawEngine)
		{
			INodeDrawEngine pNodeDrawEngine = (INodeDrawEngine) this.getEngine();
			pNodeDrawEngine.resizeToFitCompartment((ICompartment) this, true, false);
		}
	}

	/**
	 * Support for derived classes, if the model element exists in a compartment it is handed the change
	 * notification, otherwise a new compartment is created and the list compartment is grown
	 *
	 * @param pTargets [in] Information about what has changed.
	 */
	public void modelElementHasChanged2(INotificationTargets pTargets) {
		IElement changedEle = pTargets != null ? pTargets.getSecondaryChangedModelElement() : null;

		// find our compartment
		ICompartment pComp = findCompartmentContainingElement(changedEle);
		if (pComp != null) {
			// tell compartment to update
			pComp.modelElementHasChanged(pTargets);

			// scroll but do not grow
			ensureVisible(pComp, true);
		} else {
			// not found, must be a new one, grow the compartment
			addModelElement(changedEle, -1);
			pComp = findCompartmentContainingElement(changedEle);
			ensureVisible(pComp, true);

			this.m_hasOptimumSizeBeenSet = false;
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment#getMaxSize()
	 */
	public IETSize getMaxSize() {
		return new ETSize(this.m_maxWidth, this.m_maxHeight);
	}

	public boolean handleKeyDown(int nKeyCode, int nShift) {
		boolean handled = false;
		int index = -1;
		IDrawEngine engine = getEngine();
      //if the compartment is read only we do not want to handle key down on that.
		if (engine != null && !getReadOnly()) 
      {
			ETList < ICompartment > pSelected = getSelectedCompartments();
			if (pSelected != null) 
			{
				boolean changedSelection = false;
				ICompartment pCompartment = engine.getAnchoredCompartment();

				//get the first selected compartment if anchored compartment is null
				if (pCompartment == null)
				{
					if (pSelected.size() > 0) 
					{
						pCompartment = (ICompartment) pSelected.get(0);
					}
					else if (this.getDefaultCompartment() != null) {
						ICompartment pDefaultCompartment = getDefaultCompartment();
						if (!pDefaultCompartment.isSelected()) {
							pDefaultCompartment.setSelected(true);
							changedSelection = true;
						}
						pCompartment = pDefaultCompartment;
					}
				}

				if (pCompartment != null) {
					handled = pCompartment.handleKeyDown(nKeyCode, nShift);

					// save anchored compartment's index incase we need it below
					index = getCompartmentIndex(pCompartment);
				}

				if (!handled && pCompartment != null) {
					// compartment didn't handle it, check for a navigation key
                                    //Jyothi: skip handling VK_UP and VK_DOWN events if the shift key is pressed.. 
                                    //we want to use the shift_up/down combination for keyboard edge traversal
					if ((nKeyCode == KeyEvent.VK_UP) && (nShift != 0)) {
						handled = lineUp();
					} else if ((nKeyCode == KeyEvent.VK_DOWN) && (nShift != 0)) {
						handled = lineDown();
					} else if (nKeyCode == KeyEvent.VK_INSERT && index != -1) {
						// insert below the selected compartment (or end of list if none selected)
						addCompartment(null, index + 1, true);
						handled = true;
					} else if (nKeyCode == KeyEvent.VK_DELETE) {
						
						// insert below the selected compartment (or end of list if none selected)
						if (pSelected.size() > 0)
						{
							deleteSelectedCompartments(true);
							handled = true;
						}
					}
				}
				
				if (pCompartment != null && changedSelection)
				{
					pCompartment.setSelected(false); 
					engine.setAnchoredCompartment(null);
				}
			}
		}
		return handled;
	}

	public boolean handleCharTyped(char ch) {
		boolean handled = false;
		int index = -1;
		IDrawEngine engine = getEngine();
      //if the compartment is read only we do not want to handle key down on that.
		if (engine != null && !getReadOnly()) 
      {
			ETList < ICompartment > pSelected = getSelectedCompartments();
			if (pSelected != null) 
			{
				boolean changedSelection = false;
				ICompartment pCompartment = engine.getAnchoredCompartment();

				//get the first selected compartment if anchored compartment is null
				if (pCompartment == null)
				{
					if (pSelected.size() > 0) 
					{
						pCompartment = (ICompartment) pSelected.get(0);
					}
					else if (this.getDefaultCompartment() != null) {
						ICompartment pDefaultCompartment = getDefaultCompartment();
						if (!pDefaultCompartment.isSelected()) {
							pDefaultCompartment.setSelected(true);
							changedSelection = true;
						}
						pCompartment = pDefaultCompartment;
					}
				}

				if (pCompartment != null) {
					handled = pCompartment.handleCharTyped(ch);

					// save anchored compartment's index incase we need it below
					index = getCompartmentIndex(pCompartment);
				}
				
				if (pCompartment != null && changedSelection)
				{
					pCompartment.setSelected(false); 
				}
			}
		}
		return handled;
	}

	protected String getPackageImportText(IElement element) {
		String sPackageImportText = "";

		if (element != null) {
			INamespace containerNamespace = null;

			if (m_engine != null) {
				// Fix W9299:  Added this check because during a CDFS the "{ From ..." string
				// would get displayed.  This is because (I think) CDFS lays all the elements
				// on top of each other so the element would think it was graphically contained.

				if (!UserInputBlocker.getIsDisabled(GBK.DIAGRAM_CONTAINMENT)) {
					INodePresentation containerNodePE = TypeConversions.getGraphicalContainer(m_engine);
					if (containerNodePE != null) {
						// Make sure this container is a namespace container, and
						// get the model element of the compartment containing this engine's presentation element
						IDrawEngine containerDrawEngine = TypeConversions.getDrawEngine(containerNodePE);

						if (containerDrawEngine instanceof IADContainerDrawEngine) {
							IADContainerDrawEngine drawEngine = (IADContainerDrawEngine) containerDrawEngine;

							// Changed the check for CT_NAMESPACE becuase it broke the states
							// Fix W6539:  Graphical containers do NOT use the containing model element
							long nType = drawEngine.getContainmentType();
							if (nType != ContainmentTypeEnum.CT_GRAPHICAL) {
								IPresentationElement thisPE = TypeConversions.getPresentationElement(m_engine);
								if (thisPE != null) {
									IElement containingModelElement = drawEngine.getContainingModelElement(thisPE);
									if (containingModelElement instanceof INamespace) {
										containerNamespace = (INamespace) containingModelElement;
									}
								}
							}
						}
					}
				}
			}

			// When a node is not in a graphical container, its namespace is that of the diagram
			if (containerNamespace == null) {
				IDrawingAreaControl control = getDrawingArea();
				if (control != null) {
					containerNamespace = control.getNamespaceForCreatedElements();
				}
			}

			INamespace ownerNamespace = (INamespace) element.getOwner();

			// If the ownerNamespace is null that's ok, it just means that the owner is either the
			// diagram or the container we're currently in
			if ((ownerNamespace != null) && (containerNamespace != null)) {
				String sFormat = null;
				String sName = null;

				// Check to see if the namespaces are differenct
				if (!ownerNamespace.isSame(containerNamespace)) {
					sFormat = DiagramEngineResources.getString("IDS_FROM");
					sName = ownerNamespace.getName();
					sFormat = StringUtilities.replaceSubString(sFormat, "%s", sName);

					// Fix W6141:  Check to see if the projects are different

					IProject containerProject = containerNamespace.getProject();
					IProject ownerProject = ownerNamespace.getProject();

					if ((containerProject != null) && (ownerProject != null)) 
					{
						if (!ownerProject.isSame(containerProject)) 
						{
                                                        sName = ownerProject.getName();
							sFormat = DiagramEngineResources.getString("IDS_IMPORTED_FROM");
							sFormat = StringUtilities.replaceSubString(sFormat, "%s", sName);
							// CLEAN  ownerProject.getName( &sName );
						}
					}
				}

				if (sFormat != null) {
					sPackageImportText = sFormat;
				}
			}
		}

		return sPackageImportText;
	}

	/**
	 * Initializes the compartments.
	 *
	 * @param pElement[in] The presentation element used for this compartment's drawengine
	 * The default implementation only clears all contained compartments
	 * 
	 * @return HRESULT
	 */
	protected void initCompartments(IPresentationElement pElement) {
		clearCompartments();
	}

   public boolean handleLeftMouseButtonPressed(MouseEvent pEvent)
   {
		boolean eventHandled = false;
		Iterator < ICompartment > iterator = this.getCompartments().iterator();
		while (iterator.hasNext() && !eventHandled) {
			eventHandled = iterator.next().handleLeftMouseButtonPressed(pEvent);
		}
		return eventHandled;
   }

}
