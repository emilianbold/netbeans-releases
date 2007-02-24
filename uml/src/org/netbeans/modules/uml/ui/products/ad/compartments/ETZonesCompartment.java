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

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISetCursorEvent;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PointConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.StretchContextType;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;
import com.tomsawyer.editor.TSENode;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;

/*
 * 
 * @author KevinM
 *
*/
public abstract class ETZonesCompartment extends ETSimpleListCompartment implements IADZonesCompartment
{
   private final int DIVIDER_SELECT_WIDTH = 5;

   private static final ResourceBundle messages = ResourceBundle.getBundle("org.netbeans.modules.uml.ui.products.ad.compartments.Bundle");

   /// Dividers between the zones, either horizontal or vertical, using dashed lines.
   protected IETZoneDividers m_zonedividers = new ETZoneDividers(this, DrawEngineLineKindEnum.DELK_DASH);

   /// The minimum number of compartments, normally 0
   protected int m_minNumCompartments = 0;

   /// VARIANT_TRUE to draw a line at the top of the compartment
   protected boolean m_DrawTopLine;

   /// When a new compartment is created, it is of this type
   protected String m_strCompartmentID = "ADZoneCompartment";
   protected boolean m_bCollapsible;
   protected boolean m_bResizeable;

   public static final String ORIENTATION_STRING = "orientation";

   /**
    * 
    */
   public ETZonesCompartment()
   {
      super();
      setShowName(false);
      m_bCollapsible = false;
      m_bResizeable = false;
      m_DrawTopLine = true;
   }

   // ICompartment stuff that we override.

   /**
    * loads data from a model element
    * Determine if any regions already exist, and add compartments as necessary
    */
   public void addModelElement(IElement element, int nIndex /*-1*/
   ) throws RuntimeException
   {
      if (null == element)
         throw new IllegalArgumentException();

      super.addModelElement(element, nIndex);
      validateZoneCompartments(true);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#draw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, org.netbeans.modules.uml.core.support.umlsupport.IETRect)
    */
   public void draw(IDrawInfo pDrawInfo, IETRect pBoundingRect)
   {
      try
      {
         super.draw(pDrawInfo, pBoundingRect);

         if (pDrawInfo != null)
         {
            Color crBorder = Color.BLACK; // GetColorDefaultText( CK_BORDERCOLOR, pTSEDrawInfo->dc() );

            // Draw the compartments then the dividers
            int orientation = m_zonedividers.getOrientation();
            switch (orientation)
            {
               case IETZoneDividers.DMO_HORIZONTAL :
                  {
                     drawHorizontalCompartments(pDrawInfo, pBoundingRect);
                     m_zonedividers.draw(pDrawInfo, pBoundingRect, crBorder, DIVIDER_SELECT_WIDTH);
                  }
                  break;

               case IETZoneDividers.DMO_VERTICAL :
                  {
                     drawVerticalCompartments(pDrawInfo, pBoundingRect);
                     m_zonedividers.draw(pDrawInfo, pBoundingRect, crBorder, DIVIDER_SELECT_WIDTH);
                  }
                  break;

               default :
                  break;
            }

            // Draw a line at the top of the zones compartment, once the orientation is known
            if (IETZoneDividers.DMO_UNKNOWN != m_zonedividers.getOrientation() && getDrawTopLine())
            {
               GDISupport.drawLine(pDrawInfo.getTSEGraphics().getGraphics(), pBoundingRect.getTopLeft(), pBoundingRect.getTopRight(), crBorder, 1);
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#writeToArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive, org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
    */
   public IProductArchiveElement writeToArchive(IProductArchive pProductArchive, IProductArchiveElement pEngineElement)
   {

      try
      {
         IProductArchiveElement pElement = super.writeToArchive(pProductArchive, pEngineElement);

         if (pElement != null)
         {
            // Write out our stuff
            pElement.addAttributeLong(ORIENTATION_STRING, m_zonedividers.getOrientation());

            m_zonedividers.writeToArchive(pElement);
         }
         return pElement;
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#readFromArchive(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive, org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement)
    */
   public void readFromArchive(IProductArchive pProductArchive, IProductArchiveElement pCompartmentElement)
   {
      try
      {
         if (pProductArchive != null && pCompartmentElement != null)
         {
            super.readFromArchive(pProductArchive, pCompartmentElement);

            // read in our stuff
            if (pCompartmentElement != null)
            {
               int lOrientation = IETZoneDividers.DMO_UNKNOWN;
               lOrientation = (int)pCompartmentElement.getAttributeLong(ORIENTATION_STRING);
               m_zonedividers.setOrientation(lOrientation);
               m_zonedividers.readFromArchive(pCompartmentElement);
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Called when the context menu is about to be displayed.  The compartment should add whatever buttons
    * it might need.
    *
    * @param pContextMenu [in] The context menu about to be displayed
    * @param logicalX [in] The logical x location of the context menu event
    * @param logicalY [in] The logical y location of the context menu event
    */
   public void onContextMenu(IMenuManager manager)
   {
      if (getEnableContextMenu())
      {
         Point point = manager.getLocation();
         
         // (LLS) Adding the buildContext logic to support A11Y issues.  The
         // user should be able to use the CTRL-F10 keystroke to activate
         // the context menu.  In the case of the keystroke the location
         // will not be valid.  Therefore, we have to just check if the
         // compartment is selected.
         //
         // A list compartment can not be selected.  Therefore, when
         // CTRL-F10 is pressed, we must always show the list compartment
         // menu items.
         boolean buildMenu = true;
         if(point != null)
         {
             buildMenu = containsPoint(point);
         }
         
         if (buildMenu == true)
         {
            int count = getNumCompartments();
            for (int i = 0; i < count; i++)
            {
               ICompartment pComp = getCompartment(i);
               pComp.onContextMenu(manager);
            }            
         }
         
         // Fix W7573:  Always display these buttons, not just when the cursor is inside this compartment
         // ... add any buttons this compartment needs
         createZonesButtons(manager);
      }
   }

   /**
    * Sets the sensitivity and check state of the buttons created and owned by this implementor.  By default the
    * buttons are created so they are not checked.
    *
    * @param id The string id of the button whose sensitivity we are checking
    * @param pClass The button class
    * 
    * @return True have the button be enabled.
    */
   public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
   {
      boolean bIsSensitive = isParentDiagramReadOnly() ? false : true;
      if (bIsSensitive)
      {
         if (id.equals("MBK_Z_DELETE_COLUMN") || id.equals("MBK_Z_DELETE_ROW") || id.equals("MBK_POPULATE_ALL_ZS"))
         {
            if (getNumCompartments() <= m_minNumCompartments)
            {
               bIsSensitive = false;
            }
         }
      }

      return bIsSensitive;
   }

   public boolean onHandleButton(ActionEvent event, String id)
   {
      boolean bInvalidate = true;

      if (id.equals("MBK_Z_ADD_COLUMN"))
      {
         m_zonedividers.setOrientation(IETZoneDividers.DMO_VERTICAL);
         insertColumn(-1);
      }
      else if (id.equals("MBK_Z_DELETE_COLUMN"))
      {
         final int nColumn = getCompartmentColumnIndex(event);

         if (askRemoveZoneCompartment(nColumn))
         {
            m_zonedividers.deleteDivider(nColumn);
			//Added by Smitha- Fix for bug # 6267565         
			m_zonedividers.resetDividers();
         }
      }
      else if (id.equals("MBK_Z_ADD_ROW"))
      {
         m_zonedividers.setOrientation(IETZoneDividers.DMO_HORIZONTAL);
         insertRow(-1);
      }
      else if (id.equals("MBK_Z_DELETE_ROW"))
      {
         final int nRow = getCompartmentRowIndex(event);

         if (askRemoveZoneCompartment(nRow))
         {
            m_zonedividers.deleteDivider(nRow);
			//Added by Smitha - Fix for bug # 6267565
			m_zonedividers.resetDividers();
			
         }
      }
  else if (id.equals("MBK_POPULATE_THIS_Z"))
      {
         if (IETZoneDividers.DMO_UNKNOWN == m_zonedividers.getOrientation())
         {
            populateWithChildren(this);
         }
         else
         {
            final int nIndex = (IETZoneDividers.DMO_HORIZONTAL == m_zonedividers.getOrientation()) ? getCompartmentRowIndex(event) : getCompartmentColumnIndex(event);

            populateWithChildren(nIndex);
         }
         bInvalidate = false; // already handled in PopulateWithChildren()
      }
      else if (id.equals("MBK_POPULATE_ALL_ZS"))
      {
         for (int nIndx = 0; nIndx < getNumCompartments(); nIndx++)
         {
            populateWithChildren(nIndx);
         }
         bInvalidate = false; // already handled in PopulateWithChildren()
      }
      else
      {
         return super.onHandleButton(event, id);
      }

      if (getNumCompartments() <= 0)
      {
         m_zonedividers.setOrientation(IETZoneDividers.DMO_UNKNOWN);
      }

	  if (bInvalidate && (m_engine != null))	 
      {
         m_engine.sizeToContents();
         m_engine.invalidate();	
      }

      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#onContextMenu(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, int, int)
    */
   public long onContextMenu(IProductContextMenu pContextMenu, int logicalX, int logicalY)
   {
      // TODO Auto-generated method stub
      return super.onContextMenu(pContextMenu, logicalX, logicalY);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#onContextMenuHandleSelection(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem)
    */
   public long onContextMenuHandleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem)
   {
      // TODO Auto-generated method stub
      return super.onContextMenuHandleSelection(pContextMenu, pMenuItem);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#setSensitivityAndCheck(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem, int)
    */
   public boolean setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind)
   {
      return super.setSensitivityAndCheck(pContextMenu, pMenuItem, buttonKind);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
    */
   public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
   {
      try
      {
         IETSize size = m_zonedividers.calculateOptimumSize(pDrawInfo);
         //
         // If the orientation is specified then consider size of subcompatments.
         if (m_zonedividers.getOrientation() != IETZoneDividers.DMO_UNKNOWN) {
             if (m_zonedividers.getDividerCnt() < 1) {
                 size = getCompartments().get(0).calculateOptimumSize(pDrawInfo, true);
             } else {
                 boolean bHorizontalOrientation = (IETZoneDividers.DMO_HORIZONTAL == m_zonedividers.getOrientation());
                 //
                 int maxSize = 0;
                 IETSize subCompSize = null;
                 //
                 // Calculate maximum width.
                 ETList < ICompartment > subCompatments = getCompartments();
                 for (int index = 0; index < subCompatments.size(); index++) {
                     ICompartment compatment = subCompatments.get(index);
                     subCompSize = compatment.calculateOptimumSize(pDrawInfo, true);
                     //
                     if (bHorizontalOrientation) {
                         maxSize = Math.max(maxSize, subCompSize.getWidth());
                     } else {
                         maxSize = Math.max(maxSize, subCompSize.getHeight());
                     }
                 }
                 //
                 int lastDividerOffset = m_zonedividers.getDividerOffset(m_zonedividers.getDividerCnt() - 1);
                 //
                 // Correct size
                 if (bHorizontalOrientation) {
                     size.setWidth(Math.max(maxSize, size.getWidth()));
                     size.setHeight(lastDividerOffset + subCompSize.getHeight() + 2);
                 } else {
                     size.setHeight(Math.max(maxSize, size.getHeight()));
                     size.setWidth(lastDividerOffset + subCompSize.getWidth() + 2);
                 }
             }
         }
         //
         if (!bAt100Pct)
         {
            double dZoom = pDrawInfo.getOnDrawZoom();
            size.setWidth((int) (size.getWidth() * dZoom));
            size.setHeight((int) (size.getHeight() * dZoom));
         }
         return size;
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#handleLeftMouseBeginDrag(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, org.netbeans.modules.uml.core.support.umlsupport.IETPoint, boolean)
    */
   public boolean handleLeftMouseBeginDrag(IETPoint startPos, IETPoint currentPos, boolean bCancel)
   {
      // first we look for the mouse being over a horizontal divider or scrollbar
      boolean bHandled = m_zonedividers.handleLeftMouseBeginDrag(startPos);

      // if divider bars didn't handle it, let the standard draw engine implementation handle it
      if (bHandled)
      {
         // Return false if dragging should be stopped.
         bHandled = false;
      }
      else
      {
         bHandled = super.handleLeftMouseBeginDrag(startPos, currentPos, bCancel);
      }

      return bHandled;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#handleLeftMouseButton(java.awt.event.MouseEvent)
    */
   public boolean handleLeftMouseButton(MouseEvent event)
   {
      if (null == event)
         throw new IllegalArgumentException();

      boolean bHandled = false;

      // Allow the dividers to process the event
      bHandled = m_zonedividers.isMouseOnDivider(event);

      // if divider bars didn't handle it, dispatch to the compartments
      if (bHandled == false)
      {
         bHandled = super.handleLeftMouseButton(event);
      }

      return bHandled;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#handleLeftMouseButtonDoubleClick(java.awt.event.MouseEvent)
    */
   public boolean handleLeftMouseButtonDoubleClick(MouseEvent event)
   {
      boolean bHandled = false;

      ETList < ICompartment > compartments = getCompartments();
      for (Iterator iter = compartments.iterator(); iter.hasNext();)
      {
         ICompartment compartment = (ICompartment)iter.next();

         bHandled = compartment.handleLeftMouseButtonDoubleClick(event);
         if (bHandled)
         {
            break;
         }
      }

      return bHandled;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#handleLeftMouseButtonPressed(java.awt.event.MouseEvent)
    */
   public boolean handleLeftMouseButtonPressed(MouseEvent event)
   {
      if (null == event)
         throw new IllegalArgumentException();

      boolean bHandled = false;

      // Allow the dividers to process the event
      bHandled = m_zonedividers.isMouseOnDivider(event);

      // if divider bars didn't handle it, dispatch to the compartments
      if (bHandled == false)
      {
         bHandled = super.handleLeftMouseButton(event);
      }

      return bHandled;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#handleLeftMouseDrag(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
    * 
    * Implemented from ADZonesCompartmentImpl::HandleLeftMouseBeginDrag()
    */
   public boolean handleLeftMouseBeginDrag(IETPoint startPos, IETPoint currentPos)
   {
      // first we look for the mouse being over a horizontal divider or scrollbar
      boolean bHandled = m_zonedividers.handleLeftMouseBeginDrag(startPos);

      // if divider bars didn't handle it, let the standard draw engine implementation handle it
      if (bHandled)
      {
         // Return false if dragging should be stopped.
         bHandled = false;
      }
      else
      {
         bHandled = super.handleLeftMouseDrag(startPos, currentPos);
      }

      return bHandled;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#handleLeftMouseDrop(org.netbeans.modules.uml.core.support.umlsupport.IETPoint, java.util.List, boolean)
    */
   public boolean handleLeftMouseDrop(IETPoint pCurrentPos, List pElements, boolean bMoving)
   {
      // TODO Auto-generated method stub
      return super.handleLeftMouseDrop(pCurrentPos, pElements, bMoving);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#handleRightMouseButton(java.awt.event.MouseEvent)
    */
   public boolean handleRightMouseButton(MouseEvent pEvent)
   {
      // TODO Auto-generated method stub
      return super.handleRightMouseButton(pEvent);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#handleSetCursor(org.netbeans.modules.uml.core.support.umlsupport.IETPoint)
    */
   public boolean handleSetCursor(IETPoint point, ISetCursorEvent event)
   {
      // Allow the horizontal dividers to process the event
      boolean bHandled = m_zonedividers.handleSetCursor(point, event);

      // if divider bars didn't handle it, dispatch to the compartments
      if (!bHandled)
      {
         bHandled = super.handleSetCursor(point, event);
      }

      return bHandled;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#stretch(org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext)
    */
   public long stretch(IStretchContext stretchContext)
   {
      if (null == stretchContext)
         throw new IllegalArgumentException();

      int type = stretchContext.getType();
      if ((StretchContextType.SCT_STRETCHING == type) || (StretchContextType.SCT_FINISH == type))
      {
         IETSize sizeStretch = stretchContext.getStretchSize();
         if ((sizeStretch.getWidth() != 0) || (sizeStretch.getHeight() != 0))
         {
            IETPoint ptFinish = stretchContext.getFinishPoint();
            if (ptFinish != null)
            {
               m_zonedividers.updateCurrentDivider(ptFinish);

               if (m_engine != null)
               {
                  if (StretchContextType.SCT_FINISH == type)
                  {
                     // Make sure any associated labels are also updated
                     ILabelManager labelManager = m_engine.getLabelManager();
                     if (labelManager != null)
                     {
                        labelManager.relayoutLabels();
                     }
                  }

                  m_engine.invalidate();
                  getGraphWindow().updateInvalidRegions(true);
               }
            }
         }
      }

      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#clearStretch()
    */
   public long clearStretch(IDrawInfo drawInfo) {
       //
       // Discard caches
       m_cachedOptimumSize.setSize(-1,-1);
       m_cachedUserSize.setSize(-1,-1);
       m_cachedVisibleSize.setSize(-1,-1);
       //
       //
       // If the orientation is specified then consider size of subcompatments.
       if (m_zonedividers.getOrientation() != IETZoneDividers.DMO_UNKNOWN) {
           if (m_zonedividers.getDividerCnt() > 0) {
               boolean bHorizontalOrientation = (IETZoneDividers.DMO_HORIZONTAL == m_zonedividers.getOrientation());
               //
               int interDividersGap = 1;
               int currOffset = interDividersGap;
               //
               // Correct dividers location according to subcompatments' optimize size.
               ETList < ICompartment > subCompatments = getCompartments();
               for (int index = 0; index < (subCompatments.size() - 1); index++) {
                   // -1 because of the last compartment doesn't effects to dividers
                   ICompartment compatment = subCompatments.get(index);
                   IETSize subCompSize = compatment.calculateOptimumSize(drawInfo, true);
                   //
                   if (bHorizontalOrientation) {
                       currOffset += subCompSize.getHeight();
                   } else {
                       currOffset += subCompSize.getWidth();
                   }
                   //
                   currOffset += interDividersGap;
                   m_zonedividers.setDividerOffset(index, currOffset);
                   currOffset += interDividersGap;
               }
           }
       }
       //
       return 0;
   }
   
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#onGraphEvent(int)
    */
   public long onGraphEvent(int nKind)
   {
      long retVal = super.onGraphEvent(nKind);

      switch (nKind)
      {
         case IGraphEventKind.GEK_PRE_RESIZE :
            m_zonedividers.startNodeResize();
            break;

         case IGraphEventKind.GEK_POST_RESIZE :
            m_zonedividers.finishNodeResize();
            break;
      }

      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
    */
   public long modelElementHasChanged(INotificationTargets pTargets)
   {
      try
      {
         IElement pModelElement = null;
         int nKind = ModelElementChangedKind.MECK_UNKNOWN;
         if (pTargets != null)
         {
            pModelElement = pTargets.getChangedModelElement();
            nKind = pTargets.getKind();

            // See if the model element that changed was an ITaggedValue
            ITaggedValue pTaggedValue = pModelElement instanceof ITaggedValue ? (ITaggedValue)pModelElement : null;
            if ((nKind == ModelElementChangedKind.MECK_STEREOTYPEDELETED || nKind == ModelElementChangedKind.MECK_STEREOTYPEAPPLIED || nKind == ModelElementChangedKind.MECK_ELEMENTADDEDTONAMESPACE)
               || pTaggedValue != null)
            {
               // Update the optional compartments, including stereotype
               int lCnt = getNumCompartments();

               for (int lIndx = 0; lIndx < lCnt; lIndx++)
               {
                  ICompartment cpCompartment = getCompartment(lIndx);

                  INameListCompartment cpNameList = cpCompartment instanceof INameListCompartment ? (INameListCompartment)cpCompartment : null;
                  if (cpNameList != null)
                  {
                     boolean bAddedOrRemovedCompartment = cpNameList.updateAllOptionalCompartments(null);
                     if (bAddedOrRemovedCompartment)
                     {
                        setIsDirty();
                     }
                  }
               }
            }
            else if (nKind == ModelElementChangedKind.MECK_NAMEMODIFIED)
            {
               int lCnt = getNumCompartments();

               for (int lIndx = 0; lIndx < lCnt; lIndx++)
               {
                  ICompartment cpCompartment = getCompartment(lIndx);
                  INameListCompartment cpNameList = cpCompartment instanceof INameListCompartment ? (INameListCompartment)cpCompartment : null;
                  if (cpNameList != null)
                  {
                     cpNameList.modelElementHasChanged(pTargets);
                  }
               }
            } // element modified

            if (getEngine() != null)
            {
               getEngine().invalidate();
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return 1;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#modelElementDeleted(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
    */
   public long modelElementDeleted(INotificationTargets pTargets)
   {
      validateZoneCompartments(false);
      
      return 0;
   }

   // IADZonesCompartment

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADZonesCompartment#getMinimumRect()
    */
   public IETRect getMinimumRect()
   {
      IETRect rectMin = new ETRect();

      try
      {
         IETRect rectCurrent = getTSAbsoluteRect();

         int ulCnt = m_zonedividers.getDividerCnt();

         if (ulCnt > 0)
         {
            // Since we can't just set the top & bottom and be sure they are set properly
            // we get all the side dimensions, update the values and set them all at once.

            switch (m_zonedividers.getOrientation())
            {
               case IETZoneDividers.DMO_HORIZONTAL :
                  int iTop = rectCurrent.getTop() - m_zonedividers.getDividerOffset(0);
                  int iBottom = rectCurrent.getTop() - m_zonedividers.getDividerOffset(ulCnt - 1);
                  rectMin.setSides(0, iTop, 0, iBottom);
                  break;

               case IETZoneDividers.DMO_VERTICAL :
                  int iLeft = rectCurrent.getLeft() + m_zonedividers.getDividerOffset(0);
                  int iRight = rectCurrent.getLeft() + m_zonedividers.getDividerOffset(ulCnt - 1);
                  rectMin.setSides(iLeft, 0, iRight, 0);
                  break;

               default :
                  break;
            }

         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      return rectMin;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADZonesCompartment#getDrawTopLine()
    */
   public boolean getDrawTopLine()
   {
      return m_DrawTopLine;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADZonesCompartment#setDrawTopLine(boolean)
    */
   public void setDrawTopLine(boolean bDoDraw)
   {
      m_DrawTopLine = bDoDraw;
   }

   // protected member operations

   protected abstract void createZonesButtons(IMenuManager manager);

   /**
    * Determine the compartment index via the input parameter
    *
    * @param pContextMenu [in] The upper left corner of the context menu
    *
    * @return Index found, or -1 to indicate invalid input.
    */
   int getCompartmentRowIndex(ActionEvent event)
   {
      if (null == event)
         throw new IllegalArgumentException();

      ensureProperDividerCount();

      final TSConstPoint ptLogical = getLogicalMouseLocation(event);

      // Remember, TS vertical axis points up
      int lRowOffset = getLogicalBoundingRect().getTop() - (int)ptLogical.getY();

      int iIndex = m_zonedividers.getZoneIndex(lRowOffset);
      if ((-1 == iIndex) && (getNumCompartments() > 0))
      {
         iIndex = 0;
      }

      return iIndex;
   }

   /**
    * Determine the compartment index via the input parameter
    *
    * @param pContextMenu [in] The upper left corner of the context menu
    *
    * @return Index found, or -1 to indicate invalid input.
    */
   protected int getCompartmentColumnIndex(ActionEvent event)
   {
      if (null == event)
         throw new IllegalArgumentException();

      // Make sure we haven't messed up our vectors
      ensureProperDividerCount();

      final TSConstPoint ptLogical = getLogicalMouseLocation(event);

      int lColumnOffset = (int)ptLogical.getX() - getLogicalBoundingRect().getLeft();

      int iIndex = m_zonedividers.getZoneIndex(lColumnOffset);
      if (-1 == iIndex && getNumCompartments() > 0)
      {
         iIndex = 0;
      }

      return iIndex;
   }

   /**
    * Determine the compartment index via the input parameter
    *
    * @param pContextMenu [in] The upper left corner of the context menu
    *
    * @return Index found, or -1 to indicate invalid input.
    */
   protected int getCompartmentRowIndex(IMenuManager pContextMenu)
   {
      Point ptLogical = pContextMenu.getLocation();

      // Remember, TS vertical axis points up
      int lRowOffset = getLogicalBoundingRect().getTop() - ptLogical.y;

      int sIndex = m_zonedividers.getZoneIndex(lRowOffset);
      if (-1 == sIndex && getNumCompartments() > 0)
      {
         sIndex = 0;
      }

      return sIndex;
   }

   /**
    * Insert a row of zones above the input row
    */
   protected void insertRow(int nAboveRow)
   {
      // Make sure there are either zero, two, or more sub-compartments
      if (getNumCompartments() == 0)
      {
         insertZoneCompartment(-1);
      }

      m_zonedividers.setOrientation(IETZoneDividers.DMO_HORIZONTAL);
      insertZoneCompartment(nAboveRow);

      // Now insert the graphical row size
      m_zonedividers.insertDivider(nAboveRow);
   }

   /**
    * Insert a column of zones to the left of the input column
    */
   protected void insertColumn(int nToLeftOfColumn)
   {
      // Make sure there are either zero, two, or more sub-compartments
      if (getNumCompartments() == 0)
      {
         insertZoneCompartment(-1);
      }

      m_zonedividers.setOrientation(IETZoneDividers.DMO_VERTICAL);
      insertZoneCompartment(nToLeftOfColumn);

      // Now insert the graphical column size
      m_zonedividers.insertDivider(nToLeftOfColumn);
   }

   /**
    * Ensure that the zone model elements and the zone compartments match up
    */
   protected abstract void validateZoneCompartments(boolean attachElement);
   protected void validateZoneCompartments(ETList < IElement > elements, 
                                           int /* Orientation */ defaultOrientation,
                                           boolean attachElement) 
    throws RuntimeException
   {
      if (null == elements)
         throw new IllegalArgumentException();
      if (null == m_engine)
         throw new IllegalStateException();

      boolean bAddedZoneCompartment = false;

      HashSet < ICompartment > setCompartments = new HashSet < ICompartment > ();

      // Add zone compartments for elements not
      // currently associated with other zone compartments.
//      for (Iterator iter = elements.iterator(); iter.hasNext();)
      for(IElement element : elements)
      {
//         IElement element = (IElement)iter.next();

         // Fix J2818:  For some reason an IRegion during a model element deleted is still in the list of elements.
         //             So, for now the fix is the check to see if the element has been deleted.
         if( ! element.isDeleted() )
         {
            ICompartment compartment = findCompartmentContainingElement(element);
            if (compartment == null)
            {
               compartment = addZoneCompartment(element);
               bAddedZoneCompartment = true;
            }

            if (compartment != null)
            {
               setCompartments.add(compartment);
               if(attachElement == true)
               {
                   if(compartment instanceof ETZoneCompartment)
                   {
                       ((ETZoneCompartment)compartment).attach(element);
                   }
                   else
                   {
                       compartment.addModelElement(element, -1);
                   }
                    
               }
            }
         }
      }

      // Check to see if we need to delete some compartments
      int compartmentCnt = getNumCompartments();
      for (int indx = compartmentCnt - 1; indx >= 0; indx--)
      {
         ICompartment compartment = getCompartment(indx);
         if (compartment != null)
         {
            if (!setCompartments.contains(compartment))
            {
               removeCompartmentAt(indx, false);
            }
         }
      }

      // Make sure there is an orientation for the compartments, and the dividers
      if ((getNumCompartments() > 1) && (IETZoneDividers.DMO_UNKNOWN == m_zonedividers.getOrientation()))
      {
         m_zonedividers.setOrientation(defaultOrientation);
      }

      m_zonedividers.resetDividers();

      if (bAddedZoneCompartment)
      {
         m_engine.delayedSizeToContents();
      }
      else
      {
         m_engine.invalidate();
      }
   }

   /**
   * Adds a new zone compartment for the specified element before the indicated compartment
   *
   * @param pElement[in] Element for which the new compartment is being created
   */
   protected ICompartment addZoneCompartment(IElement pElement)
   {
      return addZoneCompartment(pElement, -1);
   }

   /**
   * Adds a new zone compartment for the specified element before the indicated compartment
   *
   * @param pElement[in] Element for which the new compartment is being created
   * @param nPos[in] The position to create the new compartment in the list compartments
   */
   protected ICompartment addZoneCompartment(IElement pElement, int nPos) throws InvalidArguments
   {
      ICompartment cpCompartment = null;

      if (pElement != null)
      {
         cpCompartment = createAndAddCompartment(m_strCompartmentID, nPos, false);

         IADZoneCompartment cpNameList = cpCompartment instanceof IADZoneCompartment ? (IADZoneCompartment)cpCompartment : null;
         if (cpNameList != null)
         {
            cpNameList.attach(pElement);
         }
         else if (cpCompartment != null)
         {
            cpCompartment.addModelElement(pElement, -1);
         }
      }

      return cpCompartment;
   }

   /**
    * Insert a new zone compartment before the indicated compartment
    *
    * @param nPos[in] The position to create the new compartment in the list compartments
    */
   protected void insertZoneCompartment(int nPos) throws InvalidArguments
   {
      if (nPos < -1 || nPos >= getNumCompartments())
      {
         throw new InvalidArguments();
      }

      IElement cpElement = createNewElement();
      addZoneCompartment(cpElement);
   }

   protected abstract IElement createNewElement() throws RuntimeException;

   /**
    * Initialize the compartments
    */
   public void initCompartments(IPresentationElement pElement)
   
   {
      try
      {
         nodeResized(-1);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * If the divider count is off for some reason, the dividers are thrown away and recalculated
    */
   protected void ensureProperDividerCount()
   {
      if (m_zonedividers.getDividerCnt() != getNumCompartments() - 1)
      {
         m_zonedividers.resetDividers();
      }
   }
   /**
    * Asks the user if they want to remove the compartment
    */
   boolean askRemoveZoneCompartment(int nPos)
   {
      boolean bCompartmentRemoved = false;

      if ((nPos >= 0) && (nPos < getNumCompartments()))
      {
         QuestionResponse nResult = null;

         IQuestionDialog dlg = new SwingQuestionDialogImpl();
         if (dlg != null)
         {
            String strText = messages.getString("IDS_REMOVE_ZONE");
            String strTitle = messages.getString("IDS_REMOVE_ZONE_TITLE");

            // TODO pass in the proper window handle
            nResult = dlg.displaySimpleQuestionDialog(MessageDialogKindEnum.SQDK_YESNO, MessageIconKindEnum.EDIK_ICONWARNING, strText, SimpleQuestionDialogResultKind.SQDRK_RESULT_NO, null, strTitle);
         }

         if ((nResult != null) && (SimpleQuestionDialogResultKind.SQDRK_RESULT_YES == nResult.getResult()))
         {
            removeCompartmentAt(nPos, true);

            bCompartmentRemoved = true;
         }
      }

      return bCompartmentRemoved;
   }

   /**
    * Draw the compartments using the horizontal dividers to determine their size.
    *
    * @param pInfo[in] Information about where to draw
    * @param rectBounding[in] Bounding rectangle for this zones compartment
    */
   protected void drawHorizontalCompartments(IDrawInfo pDrawInfo, IETRect pBoundingRect)
   {
      int compartmentCnt = getNumCompartments();
      if (compartmentCnt > 0)
      {
         ensureProperDividerCount();

         // Use the input rectangle as the total bounds for all the zone compartments
         // Create a local rectangle to pass into the zone compartments' draw() routine

         IETRect localRectBounding = (IETRect)pBoundingRect.clone();
         double dZoom = pDrawInfo.getOnDrawZoom();

         IETPoint ptDrawOffset = m_zonedividers.getDrawOffset(pBoundingRect);
         final int iOffset = ptDrawOffset.getY() - pBoundingRect.getTop();
         int previousBottom = pBoundingRect.getTop();
         for (int compartmentIndx = 0; compartmentIndx < compartmentCnt; compartmentIndx++)
         {
            localRectBounding.setTop(previousBottom);

            previousBottom =
               (compartmentIndx < m_zonedividers.getDividerCnt()) ? pBoundingRect.getTop() + iOffset + (int) (m_zonedividers.getDividerOffset(compartmentIndx) * dZoom) : pBoundingRect.getBottom();

            localRectBounding.setBottom(previousBottom);

            ICompartment cpCompartment = getCompartment(compartmentIndx);
            if (cpCompartment != null)
            {
               cpCompartment.draw(pDrawInfo, (IETRect)localRectBounding.clone());
            }
         }
      }
   }

   /**
    * Draw the compartments using the vertical dividers to determine their size.
    *
    * @param pInfo[in] Information about where to draw
    * @param rectBounding[in] Bounding rectangle for this zones compartment
    */
   protected void drawVerticalCompartments(IDrawInfo pDrawInfo, IETRect pBoundingRect)
   {
      final int compartmentCnt = getNumCompartments();
      if (compartmentCnt > 0)
      {
         ensureProperDividerCount();

         // Use the input rectangle as the total bounds for all the zone compartments
         // Create a local rectangle to pass into the zone compartments' draw() routine

         IETRect rectLocalBounding = (IETRect)pBoundingRect.clone();
         final double dZoom = pDrawInfo.getOnDrawZoom();

         final int iOffset = m_zonedividers.getDrawOffset(pBoundingRect).getX() - pBoundingRect.getLeft();
         int previousRight = pBoundingRect.getLeft();
         for (int compartmentIndx = 0; compartmentIndx < compartmentCnt; compartmentIndx++)
         {
            rectLocalBounding.setLeft(previousRight);

            previousRight =
               (compartmentIndx < (int) (m_zonedividers.getDividerCnt()))
                  ? pBoundingRect.getLeft() + (int) (iOffset + m_zonedividers.getDividerOffset(compartmentIndx) * dZoom)
                  : pBoundingRect.getRight();

            rectLocalBounding.setRight(previousRight);

            ICompartment compartment = getCompartment(compartmentIndx);
            if (compartment != null)
            {
               compartment.draw(pDrawInfo, (IETRect)rectLocalBounding.clone());
            }
         }
      }
   }

   /**
    * Ensure that all the metadata children have presentation elements
    * contained within the indexed zone compartment.
    */
   protected boolean populateWithChildren(int nIndex)
   {
      boolean bItemsAdded = false;

      ICompartment compartment = getCompartment(nIndex);
      if (compartment != null)
      {
         bItemsAdded = populateWithChildren(compartment);
      }

      return bItemsAdded;
   }

   /**
    * Ensure that all the metadata children have presentation elements
    * contained within the indexed zone compartment.
    */
   protected boolean populateWithChildren(ICompartment compartment)
   {
      // The default behavior is to do nothing.  See derived classes for implementation
      return false;
   }

   /**
    * Resize this compartment to contain the input rectangle
    */
   protected void resizeToContain(ICompartment pCompartment, final IETRect rect)
   {
      if (null == pCompartment)
         throw new IllegalArgumentException();

      if (null == this.m_engine)
         throw new IllegalArgumentException();

      if (rect != null)
      {
         final IETRect rectCompartment = TypeConversions.getLogicalBoundingRect(pCompartment);

         final int lOffsetLeft = (rect.getLeft() - rectCompartment.getLeft());
         final int lOffsetRight = (rect.getRight() - rectCompartment.getRight());
         final int lOffsetBottom = (rect.getBottom() - rectCompartment.getBottom());
         final int lOffsetTop = (rect.getTop() - rectCompartment.getTop());

         final boolean bExpandLeft = (lOffsetLeft < 0);
         final boolean bExpandRight = (lOffsetRight > 0);
         final boolean bExpandBottom = (lOffsetBottom < 0);
         final boolean bExpandTop = (lOffsetTop > 0);

         final boolean bHorizontalOrientation = (IETZoneDividers.DMO_HORIZONTAL == m_zonedividers.getOrientation());
         final boolean bMoveOtherPEs = bHorizontalOrientation ? (bExpandBottom || bExpandTop) : (bExpandLeft || bExpandRight);

         // move the dividers
         if (bExpandRight || bExpandBottom)
         {
            final IETPoint ptInCompartment = new ETPoint(rectCompartment.getTopLeft().x + 5,  + rectCompartment.getTopLeft().y + 5);

            int ulDividerIndx = m_zonedividers.getIndexFromTSLogical(ptInCompartment);

            if (IETZoneDividers.DMO_UNKNOWN != m_zonedividers.getOrientation())
            {
               final int lOffset = bHorizontalOrientation ? lOffsetBottom : lOffsetRight;

               //ATLASSERT(lOffset > 0);

               m_zonedividers.shiftDividers(lOffset, ulDividerIndx);
            }

            // Make sure the draw engine is updated
            INodePresentation cpNodePresentation = TypeConversions.getNodePresentation(m_engine);

            if (cpNodePresentation != null)
            {
               IETSize sizeDividers = m_zonedividers.calculateOptimumSize(null);

               final IETRect rectNode = TypeConversions.getLogicalBoundingRect(m_engine);
              
               final IETSize sizeRequired = new ETSize((int)(Math.max(sizeDividers.getWidth(), rect.getRight() - rectNode.getLeft())),
                                                       (int)(Math.max(sizeDividers.getHeight(), rectNode.getTop() - rect.getBottom())));
               
               final IETSize sizeResize = new ETSize((int)(Math.max(sizeRequired.getWidth(), rectNode.getWidth())),
                                                     (int)(Math.max(sizeRequired.getHeight(), -rectNode.getHeight())));

               cpNodePresentation.resize(sizeResize.getWidth(), sizeResize.getHeight(), true);
            }
         }

         if (bMoveOtherPEs)
         {
            // Find all the presentation elements for the compartments seperating
            // the list into presentation elements before, and after the input compartment.
            ETList < IPresentationElement > cpBeforePEs = new ETArrayList < IPresentationElement > ();
            ETList < IPresentationElement > cpAfterPEs = new ETArrayList < IPresentationElement > ();
            ETList < IPresentationElement > cpCurrentPEs = cpBeforePEs;

            int lCnt = getNumCompartments();

            for (int lIndx = 0; lIndx < lCnt; lIndx++)
            {
               ICompartment cpCompartment = getCompartment(lIndx);

               //ATLASSERT(cpCompartment);

               if (pCompartment == cpCompartment)
               {
                  cpCurrentPEs = cpAfterPEs;
                  continue;
               }

               ETList < IPresentationElement > cpPresentationElements = cpCompartment.getContained();

               if (cpPresentationElements != null)
               {
                  cpCurrentPEs.addThese(cpPresentationElements);
               }
            }

            // Calculate the offset for the presentation elements
            IETPoint ptBefore = new ETPoint(0, 0);
            IETPoint ptAfter = new ETPoint(0, 0);

            if (bHorizontalOrientation)
            {
               if (bExpandTop)
               {
                  ptBefore.setY(lOffsetTop);
               }
               if (bExpandBottom)
               {
                  ptAfter.setY(lOffsetBottom);
               }
            }
            else
            {
               if (bExpandLeft)
               {
                  ptBefore.setX(lOffsetLeft);
               }
               if (bExpandRight)
               {
                  ptAfter.setX(lOffsetRight);
               }
            }

            movePEs(cpBeforePEs, ptBefore);
            movePEs(cpAfterPEs, ptAfter);
         }
      }
   }

   /**
    * Move the presentation elements the specified amount
    */
   protected void movePEs(ETList < IPresentationElement > pPEs, final IETPoint ptOffset)
   {
      if (null == pPEs)
         throw new IllegalArgumentException();

      int lCnt = pPEs.size();

      for (int lIndx = 0; lIndx < lCnt; lIndx++)
      {
         IPresentationElement cpPE = (IPresentationElement)pPEs.get(lIndx);

         TSENode pNode = TypeConversions.getOwnerNode(cpPE);

         if (pNode != null)
         {
            pNode.setCenterX(ptOffset.getX());
            pNode.setCenterY(ptOffset.getY());
         }
      }
   }
   
   //Jyothi: a11y work - traverse thru partitions/states
    public void setSelected(boolean pValue)
    {       
        ETList <ICompartment> zoneCompList = getCompartments();
        if (zoneCompList != null && zoneCompList.size() > 0) {
            for (int i=0; i<zoneCompList.size(); i++) {
                ICompartment comp = zoneCompList.get(i);
                if (comp != null && comp instanceof IADZoneCompartment && !comp.isSelected()) {
                    comp.setSelected(pValue);
//                    comp.editCompartment(false, 0, 0, -1);
                    break;
                }
            }
        }
//        super.setSelected(pValue);
    }
    
    public boolean handleKeyDown(int keyCode, int Shift) {
        boolean handled = super.handleKeyDown(keyCode,Shift);
        
        return handled;
    }
    
}
