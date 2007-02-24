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

import java.util.HashMap;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IValueSpecification;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionConstraint;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IADInteractionOperandCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IADInteractionOperandsCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.ConnectorPiece;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import com.tomsawyer.drawing.TSLabel;
import com.tomsawyer.util.TSObject;

public class CombinedFragmentLabelManager extends ADLabelManager implements ICombinedFragmentLabelManager
{

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
    */
   public void modelElementHasChanged(INotificationTargets pTargets)
   {
      resetLabelsText();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#createInitialLabels()
    */
   public void createInitialLabels()
   {

      boolean bIsDisplayed = false;

      bIsDisplayed = isDisplayed(TSLabelKind.TSLK_INTERACTION_CONSTRAINT);

      if (!bIsDisplayed)
      {
         m_mapOperandToLabel.clear();

         ETList < ICompartment > cpCompartments = getCompartments();

         if (cpCompartments != null)
         {
            int lCnt = 0;
            lCnt = cpCompartments.size();

            // Create the labels in their proper locations
            for (int lIndx = 0; lIndx < lCnt; lIndx++)
            {
               ICompartment cpCompartment = cpCompartments.get(lIndx);

               IInteractionOperand cpInteractionOperand = getInteractionOperand(cpCompartment);
               attemptToCreateLabelForInteractionConstrant(cpInteractionOperand, null);
            }
         }
      }

      // Make sure the text is ok
      resetLabelsText();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#discardAllLabels()
    */
   public void discardAllLabels()
   {

      // Must clear our map before calling the parent
      m_mapOperandToLabel.clear();

      super.discardAllLabels();

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#handleEditChange(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel, java.lang.String)
    */
   public void handleEditChange(IETLabel pLabel, String sNewString)
   {

      int nLabelKind = TSLabelKind.TSLK_UNKNOWN;

      nLabelKind = pLabel.getLabelKind();

      if (nLabelKind == TSLabelKind.TSLK_INTERACTION_CONSTRAINT)
      {
         // Fix W7376:  Need to reset the text properly after an edit has changed the label's text.
         resetLabelsText();
      }

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#isValidLabelKind(int)
    */
   public boolean isValidLabelKind(int nLabelKind)
   {

      boolean bIsValid = false;

      if (nLabelKind == TSLabelKind.TSLK_INTERACTION_CONSTRAINT)
      {
         bIsValid = true;
      }
      return bIsValid;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IGraphObjectManager#onGraphEvent(int)
    */
   public void onGraphEvent(int nKind)
   {

      switch (nKind)
      {
         case IGraphEventKind.GEK_PRE_DELETEGATHERSELECTED :
            onPreDeleteGatherSelected();
            break;

         case IGraphEventKind.GEK_PRE_DELETE :
            onPreDelete();
            break;

         case IGraphEventKind.GEK_POST_RESIZE :
            relayoutLabels();
            break;

         default :
            break;
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#relayoutLabels()
    */
   public void relayoutLabels()
   {

      ETList < ICompartment > cpCompartments = getCompartments();

      if (cpCompartments != null)
      {
         LabelOffsetHelper helper = new LabelOffsetHelper(this);

         int lCnt = 0;

         lCnt = cpCompartments.size();

         // Create the labels in their proper locations
         for (int lIndx = 0; lIndx < lCnt; lIndx++)
         {
            ICompartment cpCompartment = cpCompartments.get(lIndx);

            if (cpCompartment != null)
            {
               IInteractionOperand cpInteractionOperand = getInteractionOperand(cpCompartment);

               if (cpInteractionOperand != null)
               {
                  Integer iLabelID = m_mapOperandToLabel.get( cpInteractionOperand );
                  if( iLabelID != null )
                  {
                     IETLabel etLabel = getLabelByID( iLabelID.intValue() );
                     if (etLabel != null)
                     {
                        helper.relayoutLabel(cpCompartment, etLabel);
                     }
                  }
               }
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#resetLabelsText()
    */
   public void resetLabelsText()
   {

      // This function can be called when the combined fragment is being read back in from archive.
      // In this situation, the labels exist but have not been put in the member map,
      // so we must hook them back up, see below

      // Loop through the compartments,
      // looking for compartments that contain interaction operands

      boolean bDoLayout = false;

      ETList < ICompartment > cpCompartments = getCompartments();

      if (cpCompartments != null)
      {
         int lCnt = 0;
         int nLabelIndx = 0;

         lCnt = cpCompartments.size();

         for (int lIndx = 0; lIndx < lCnt; lIndx++)
         {
            ICompartment cpCompartment = cpCompartments.get(lIndx);

            // See if the compartment has an associated interaction operand, and product label

            IInteractionOperand cpInteractionOperand = getInteractionOperand(cpCompartment);

            if (cpInteractionOperand != null)
            {
               // Refresh the product label's text

               IInteractionConstraint cpInteractionConstraint = cpInteractionOperand.getGuard();

               if (cpInteractionConstraint != null)
               {
                  String bstrInteractionConstraint = formatInteractionConstraint(cpInteractionConstraint);

                  // Find the product label associated with this constraint
                  IETLabel etLabel = null;

                  Integer id = m_mapOperandToLabel.get(cpInteractionOperand);
                  if ( id != null )
                  {
                     etLabel = getLabelByID( id.intValue() );
                  }
                  else
                  {
                     // Handle the case were the labels exist, but are not in the map
                     // This happens when the diagram is being read back in from archive

                     etLabel = getETLabelbyIndex(nLabelIndx++);
                     if (etLabel != null)
                     {
                        TSLabel tsLabel = (TSLabel)etLabel.getObject();
                        if (tsLabel != null)
                        {
                           Integer nID = new Integer( (int)tsLabel.getID() );
                           m_mapOperandToLabel.put(cpInteractionOperand, nID);
                        }
                     }
                  }

                  if (etLabel != null)
                  {
                     String bsOldText = etLabel.getText();

                     if (!(bsOldText.equals(bstrInteractionConstraint)))
                     {
                        etLabel.setText(bstrInteractionConstraint);
                        etLabel.sizeToContents();
                        bDoLayout = true;
                     }
                  }
               }
            }
         }
      }

      // Relayout the labels
      if (bDoLayout)
      {
         relayoutLabels();
      }

      invalidate();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.ICombinedFragmentLabelManager#discardOperandsLabel(org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand)
    */
   public boolean discardOperandsLabel(IInteractionOperand operand)
   {
      boolean bDiscarded = false;

      Integer id = m_mapOperandToLabel.get( operand );
      if (id != null)
      {
         // Make sure we don't hold onto the last reference
         IETLabel etLabel = getLabelByID( id.intValue() );
         if ( etLabel != null )
         {
            discardETLabel( etLabel );
         }

         m_mapOperandToLabel.remove(operand);

         bDiscarded = true;
      }

      return bDiscarded;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.ICombinedFragmentLabelManager#LabelFromOperand(org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand)
    */
   public IETLabel getLabelFromOperand(IInteractionOperand operand)
   {
      IETLabel etLabel = null;

      Integer id = m_mapOperandToLabel.get( operand );
      if ( id != null )
      {
         etLabel = getLabelByID( id.intValue() );
      }

      if ( null == etLabel )
      {
         attemptToCreateLabelForInteractionConstrant( operand, etLabel );

         // Make sure the label is deleted if the user does not edit it,
         // moved to CombinedFragmentDrawEngine.EditConstraint()
      }
      return null;
   }

   // Convert the parent product element to a draw engine
   public IDrawEngine getEngine()
   {
      IDrawEngine ppEngine = null;

      IETGraphObject cpParentETElement = this.getParentETGraphObject();

      if (cpParentETElement != null)
      {
         IDrawEngine cpEngine = TypeConversions.getDrawEngine(cpParentETElement);

         if (cpEngine != null)
         {
            ppEngine = cpEngine;
         }
      }

      return ppEngine;
   }

   // Handles the various dispatchers
   protected void onPreDeleteGatherSelected()
   {
      IETGraphObject cpParentETElement = this.getParentETGraphObject();
      if (cpParentETElement != null)
      {
         //TODO			  ConnectorPiece.selectAssociatedEdges( cpParentETElement );
      }
   }

   protected void onPreDelete()
   {
      IETGraphObject cpParentETElement = this.getParentETGraphObject();
      if (cpParentETElement != null)
      {
         //TODO			  ConnectorPiece.deleteEdge( cpParentETElement );
      }
   }

   // Format an interaction constraint into its display string.
   protected String formatInteractionConstraint(IInteractionConstraint pInteractionConstraint)
   {
      String bstrFormatedInteractionConstraint = "";

      if (pInteractionConstraint != null)
      {
         // Get the data formatter off the product where it caches up
         // the various factories per language
         IDataFormatter pFormatter = ProductHelper.getDataFormatter();

         if (pFormatter != null)
         {
            String bcsInteractionConstraint = pFormatter.formatElement(pInteractionConstraint);
            bstrFormatedInteractionConstraint = bcsInteractionConstraint;
         }
      }

      return bstrFormatedInteractionConstraint;
   }

   // Convert the parent product element to a combined fragment
   protected ICombinedFragment getCombinedFragment()
   {
      ICombinedFragment ppCombinedFragment = null;

      IETGraphObject cpParentETElement = this.getParentETGraphObject();
      if (cpParentETElement != null)
      {
         IElement cpElement = TypeConversions.getElement(cpParentETElement);

         ICombinedFragment cpCombinedFragment = (ICombinedFragment)cpElement;
         if (cpCombinedFragment != null)
         {
            ppCombinedFragment = cpCombinedFragment;
         }
      }
      return ppCombinedFragment;

   }

   // Get the compartments from the draw engine associated with the parent product element
   protected ETList < ICompartment > getCompartments()
   {
      ETList < ICompartment > compartments = null;

      IDrawEngine engine = getEngine();
      if (engine != null)
      {
         IADInteractionOperandsCompartment operandsCompartment =
            getCompartmentByKind( engine, IADInteractionOperandsCompartment.class );

         if (operandsCompartment != null)
         {
            compartments = operandsCompartment.getCompartments();
         }
      }

      return compartments;
   }

   // Get the interaction operand associated with the compartment
   protected IInteractionOperand getInteractionOperand(ICompartment pCompartment)
   {

      IInteractionOperand ppOperand = null;

      IADInteractionOperandCompartment cpInteractionOperandCompartment = (IADInteractionOperandCompartment)pCompartment;

      if (cpInteractionOperandCompartment != null)
      {
         IInteractionOperand cpInteractionOperand = cpInteractionOperandCompartment.getInteractionOperand();

         if (cpInteractionOperand != null)
         {
            ppOperand = cpInteractionOperand;
         }
      }

      return ppOperand;
   }

   // Find the location for new interaction constraint labels
   protected IETPoint calculateLabelOffset()
   {

      return null;

   }

   // Try to create a label for the interaction operand's interaction constraint, if it exists
   protected void attemptToCreateLabelForInteractionConstrant(IInteractionOperand pOperand, IETLabel ppETLabel)
   {
      if (ppETLabel != null)
      {
         ppETLabel = null;
      }

      if (pOperand != null)
      {
         IInteractionConstraint cpInteractionConstraint = pOperand.getGuard();

         if (cpInteractionConstraint != null)
         {
            String bstrInteractionConstraint = formatInteractionConstraint(cpInteractionConstraint);

            IValueSpecification spec = cpInteractionConstraint.getSpecification();

            IExpression cpExpression = (IExpression)spec;

            if (cpExpression != null)
            {
               String bsBody = cpExpression.getBody();

               IETLabel etLabel = createLabel(bsBody, TSLabelKind.TSLK_INTERACTION_CONSTRAINT, 0, cpExpression);

               if (etLabel != null)
               {
                  if (ppETLabel != null)
                  {
                     ppETLabel = etLabel;
                  }

                  TSLabel tsLabel = (TSLabel)etLabel.getObject();

                  if (tsLabel != null)
                  {
                     Integer nID = new Integer( (int)tsLabel.getID() );

                     m_mapOperandToLabel.put(pOperand, nID);
                  }

                  // Make sure the label is layed out properly
                  LabelOffsetHelper helper = new LabelOffsetHelper(this);
                  helper.relayoutLabel(pOperand, etLabel);
               }
            }
         }
      }
   }

   // Relayout the label associated with this compartment
   protected void relayoutLabel(ICompartment pCompartment, IETLabel pETLabel)
   {

   }

   // Map the interaction operand to TS ids
   private HashMap < IInteractionOperand, Integer > m_mapOperandToLabel = new HashMap < IInteractionOperand, Integer >();
}
