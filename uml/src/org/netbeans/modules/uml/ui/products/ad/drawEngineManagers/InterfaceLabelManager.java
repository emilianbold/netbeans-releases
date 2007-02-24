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


//	 Author:: nickl
//	   Date:: Dec 22, 2003 4:46:52 PM
//	Modtime:: 5/31/2004 2:24:14 PM 4:46:52 PM

package org.netbeans.modules.uml.ui.products.ad.drawEngineManagers;

import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelPlacementKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import com.tomsawyer.graph.TSNode;

public class InterfaceLabelManager extends ADLabelManager implements IInterfaceLabelManager
{

   public void createInitialLabels()
   {

      String sName = getNameText(false);

      if (sName != null && sName.length() > 0)
      {
         boolean bIsDisplayed = false;

         bIsDisplayed = isDisplayed(TSLabelKind.TSLK_INTERFACE);

         if (!bIsDisplayed)
         {
            createLabelIfNotEmpty(sName, TSLabelKind.TSLK_INTERFACE, TSLabelPlacementKind.TSLPK_CENTER_BELOW, null);
         }
      }

      // Make sure the text is ok
      resetLabelsText();

   }

   public void resetLabelsText()
   {

      IETLabel pETLabel = null;
      boolean bDoLayout = false;

      pETLabel = getETLabelbyIndex(0);
      if (pETLabel != null)
      {
         String sText = getNameText(false);

         // Here's where we set the text of the label
         String sOldText = pETLabel.getText();

         if (sText != null && sText.length() > 0)
         {
            if (!(sText.equals(sOldText)))
            {
               pETLabel.setText(sText);
               pETLabel.reposition();
               bDoLayout = true;
            }
            pETLabel.sizeToContents();
         }
         else
         {
            // If there is no text then remove the label
            removeETLabel(0);
         }
      }

      if (bDoLayout)
      {
         // Relayout the labels
         relayoutLabels();
      }

      invalidate();
   }

   public void showLabel(int nLabelKind, boolean bShow)
   {

      // See if it's already shown
      boolean bCurrentlyShown = false;

      bCurrentlyShown = isDisplayed(nLabelKind);

      if ((bCurrentlyShown && bShow) || (!bCurrentlyShown && !bShow))
      {
         // We have nothing to do!
      }
      else
      {
         if (bShow)
         {
            discardAllLabels();
            createInitialLabels();
         }
         else
         {
            discardAllLabels();
            invalidate();
         }
      }

   }

   public boolean isValidLabelKind(int nLabelKind)
   {

      boolean bIsValid = false;

      if (nLabelKind == TSLabelKind.TSLK_INTERFACE)
      {
         bIsValid = true;
      }
      return bIsValid;
   }

   public void modelElementHasChanged(INotificationTargets pTargets)
   {
      resetLabelsText();
   }

   public void onContextMenu(IProductContextMenu pContextMenu, int logicalX, int logicalY)
   {
      // Call the base class.  This creates the button handler
      super.onContextMenu(pContextMenu, logicalX, logicalY);

      String tempString;

      IProductContextMenuItem pMenuItem;
      ETList < IProductContextMenuItem > pMenuItems = null;
      ETList < IProductContextMenuItem > pSubMenuItems = null;

      pMenuItems = pContextMenu.getSubMenus();

      pMenuItem = m_ButtonHandler.createOrGetPullright(pMenuItems, "IDS_LABELS_TITLE", "IDS_LABELS_TITLE");

      if (pMenuItem != null)
      {
         // Get the sub menu items for the popup
         pSubMenuItems = pMenuItem.getSubMenus();

         if (pSubMenuItems != null)
         {
            // Add our button
            m_ButtonHandler.addMenuItem(
               pContextMenu,
               pSubMenuItems,
               MenuButtonKind.MBK_RESET_LABELS,
               m_ButtonHandler.loadString("IDS_RESETLABELS"),
               m_ButtonHandler.loadString("IDS_RESETLABELS_DESCRIPTION"),
               "MBK_RESET_LABELS",
               true,
               null);
         }
         pMenuItems.add(pMenuItem);
      }
   }

   public void onContextMenuHandleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem)
   {

      int menuSelected = 0;

      menuSelected = m_ButtonHandler.getMenuButtonClicked(pMenuItem);


      TSNode pNodeClickedOn = (TSNode)pContextMenu.getItemClickedOn();

		IETGraphObject pETElement = TypeConversions.getETGraphObject(pNodeClickedOn);

      switch (menuSelected)
      {
         case MenuButtonKind.MBK_RESET_LABELS :
            {
               if (pETElement != null)
               {
                  IDrawEngine pEngine = pETElement.getEngine();

                  if (pEngine != null)
                  {
                     ILabelManager pLabelManager = pEngine.getLabelManager();

                     if (pLabelManager != null)
                     {
                        pLabelManager.resetLabels();
                     }
                  }
               }
            }
      }
   }

   public void setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind)
   {
      // Don't sensitize for readonly diagrams
      pMenuItem.setSensitive(this.isParentDiagramReadOnly() ? false : true);

   }

}
