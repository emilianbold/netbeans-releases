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



package org.netbeans.modules.uml.ui.products.ad.drawEngineManagers;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelPlacementKind;

public class SimpleStereotypeAndNameLabelManager extends ADLabelManager implements ISimpleStereotypeAndNameLabelManager
{

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#createInitialLabels()
    */
   public void createInitialLabels()
   {
      boolean bCurrentlyShown = false;
      String sName = "";

      bCurrentlyShown = this.isDisplayed(TSLabelKind.TSLK_STEREOTYPE);

      if (!bCurrentlyShown)
      {
         // Get the text to be displayed
         sName = this.getStereotypeText();

         if (sName != null && sName.length() > 0)
         {
            this.createLabelIfNotEmpty(sName, TSLabelKind.TSLK_STEREOTYPE, TSLabelPlacementKind.TSLPK_CENTER_BELOW, null);
         }
      }

      bCurrentlyShown = this.isDisplayed(TSLabelKind.TSLK_NAME);
      if (!bCurrentlyShown)
      {
         // Get the name text to be displayed
         sName = this.getNameText(false);
         if (sName != null && sName.length() > 0)
         {
            this.createLabelIfNotEmpty(sName, TSLabelKind.TSLK_NAME, TSLabelPlacementKind.TSLPK_CENTER_ABOVE, null);
         }
      }

      // Make sure the text is ok
      this.resetLabelsText();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#isValidLabelKind(int)
    */
   public boolean isValidLabelKind(int nLabelKind)
   {
      //		return super.isValidLabelKind(nLabelKind);

      boolean retValue = false;

      if (nLabelKind == TSLabelKind.TSLK_STEREOTYPE || nLabelKind == TSLabelKind.TSLK_NAME)
      {
         retValue = true;
      }
      return retValue;

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#showLabel(int, boolean)
    */
   public void showLabel(int nLabelKind, boolean bShow)
   {
      //		super.showLabel(nLabelKind, bShow);

      // See if it's already shown
      boolean bCurrentlyShown = false;

      bCurrentlyShown = this.isDisplayed(nLabelKind);

      if ((bCurrentlyShown && bShow) || (!bCurrentlyShown && !bShow))
      {
         // We have nothing to do!
      }
      else
      {
         if (nLabelKind == TSLabelKind.TSLK_NAME)
         {
            if (bShow)
            {
               this.createNameLabel(true);
            }
            else
            {
               this.discardLabel(nLabelKind);
               this.invalidate();
            }
         }
         else if (nLabelKind == TSLabelKind.TSLK_STEREOTYPE)
         {
            if (bShow)
            {
               this.showStereotypeLabel();
            }
            else
            {
               this.discardLabel(nLabelKind);
               this.invalidate();
            }
         }
      }
   }

   public void createNameLabel(boolean bAssignDefaultName)
   {
      String sName = this.getNameText(bAssignDefaultName);

      if (sName != null && sName.length() > 0)
      {
         this.createLabelIfNotEmpty(sName, TSLabelKind.TSLK_NAME, TSLabelPlacementKind.TSLPK_CENTER_ABOVE, null);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#resetLabelsText()
    */
   public void resetLabelsText()
   {
      boolean done = false;
      int index = 0;
      boolean bDoLayout = false;

      IElement pElement = getModelElement();

      // Go through all the product labels and re-get their text.
      while (!done)
      {
         IETLabel pETLabel = this.getETLabelbyIndex(index);

         if (pETLabel != null)
         {
            String sText = "";
            int nLabelKind = TSLabelKind.TSLK_UNKNOWN;

            nLabelKind = pETLabel.getLabelKind();

            if (nLabelKind == TSLabelKind.TSLK_NAME)
            {
               // Get the name
               sText = getNameText(false);
            }
            else if (nLabelKind == TSLabelKind.TSLK_STEREOTYPE)
            {
               sText = getStereotypeText();
            }

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
               this.removeETLabel(index);
            }
         }
         else
         {
            done = true;
         }
         index++;
      }

      if (bDoLayout)
      {
         // Relayout the labels
         this.relayoutLabels();
      }

      this.invalidate();

   }

}
