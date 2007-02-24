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

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphObjectValidation;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.application.action.IETContextMenuHandler;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETUIFactory;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.umltsconversions.RectConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelPlacementKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.drawing.TSEdgeLabel;
import com.tomsawyer.drawing.TSLabel;
import com.tomsawyer.drawing.TSNodeLabel;
import com.tomsawyer.drawing.command.TSDeleteEdgeLabelCommand;
import com.tomsawyer.drawing.command.TSDeleteNodeLabelCommand;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEEdgeLabel;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSENodeLabel;
import com.tomsawyer.editor.ui.TSELabelUI;
import com.tomsawyer.editor.ui.TSELabelUI;
import com.tomsawyer.util.command.TSCommand;
import com.tomsawyer.util.command.TSGroupCommand;

public class LabelManager implements ILabelManager, IETContextMenuHandler
{

   // This string is a stereotype that is forced upon the stereotype string no matter what
   String m_forcedStereotypeString = "";

   IETGraphObject m_rawParentETGraphObject;

   private static final String BUNDLE_NAME = "org.netbeans.modules.uml.ui.products.ad.diagramengines.Bundle"; //$NON-NLS-1$
   private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

   public static String loadString(String key)
   {
      try
      {
         return RESOURCE_BUNDLE.getString(key);
      }
      catch (MissingResourceException e)
      {
         return '!' + key + '!';
      }
   }

   public LabelManager()
   {
      super();

   }

   public ILabelManager getLabelManagerInterface()
   {
      return this;
   }

   // Returns the model element for this label manager
   public IElement getModelElement()
   {

      IElement retValue = null;

      // Get the IElement from the parent IETGraphObject (the node or edge view)
      if (m_rawParentETGraphObject != null)
      {

         // Get the parent model element
         IPresentationElement pPresElement = m_rawParentETGraphObject.getPresentationElement();
         if (pPresElement != null)
         {
            retValue = pPresElement.getFirstSubject();
         }

         //			retValue = m_rawParentETGraphObject.getETUI().getModelElement();
      }
      return retValue;
   }

   // Creates the initial labels on a newly created node or edge
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#createInitialLabels()
    */
   public void createInitialLabels()
   {

   }

   // Most edges support stereotypes so we've moved the core functionality for showing them here
   public void showStereotypeLabel()
   {
      IElement pElement = this.getModelElement();
      if (pElement != null)
      {
         String sStereotypeText = this.getStereotypeText();

         if (sStereotypeText != null && sStereotypeText.length() > 0)
         {
            this.createLabel(sStereotypeText, TSLabelKind.TSLK_STEREOTYPE, TSLabelPlacementKind.TSLPK_CENTER_BELOW, null);
         }

         // Relayout the labels
         this.relayoutLabels();
      }
   }

   // Gets the stereotype text
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#getStereotypeText()
    */
   public String getStereotypeText()
   {

      String retValue = null;
      IElement pElement = this.getModelElement();

      if (pElement != null)
      {
         String sTempStereotype = pElement.getAppliedStereotypesAsString(true);

         if (m_forcedStereotypeString != null && m_forcedStereotypeString.length() > 0)
         {

            // See if this stereotype string is already in the text, if not then add it.
            if (sTempStereotype != null && sTempStereotype.length() > 0)
            {
               String tempStereotype = sTempStereotype;

               int pos = tempStereotype.indexOf(m_forcedStereotypeString);

               if (pos == -1)
               {
                  String replaceString = "<<";

                  replaceString += m_forcedStereotypeString;
                  replaceString += ",";

                  // The forced stereotype string was not found so add it
                  String newString = tempStereotype.replaceFirst("<<", replaceString);
                  sTempStereotype = newString;
               }
            }
            else
            {
               String replaceString = "<<";
               replaceString += m_forcedStereotypeString;
               replaceString += ">>";
               sTempStereotype = replaceString;
            }
         }

         retValue = sTempStereotype;

      }
      return retValue;
   }

   // Removes the product label at this index (0 relative index)
   public void removeETLabel(int index)
   {
      TSENode pTSENode = this.getOwnerNode();
      TSEEdge pTSEEdge = this.getOwnerEdge();

      if (pTSENode != null && pTSENode.labels() != null)
      {
         // Get the label
         TSNodeLabel pLabel = (TSNodeLabel)pTSENode.labels().get(index);
         if (pLabel != null)
         {
            pTSENode.remove(pLabel);
         }
      }
      else if (pTSEEdge != null && pTSEEdge.labels() != null)
      {
         // Get the label
         TSEdgeLabel pLabel = (TSEdgeLabel)pTSEEdge.labels().get(index);
         if (pLabel != null)
         {
            pTSEEdge.remove(pLabel);
         }
      }

      // Set the diagram as dirty
      IDiagram pDiagram;
      if (m_rawParentETGraphObject != null)
      {
         // Get the parent drawing area
         pDiagram = m_rawParentETGraphObject.getDiagram();
         if (pDiagram != null)
         {
            pDiagram.setDirty(true);
         }
      }
   }

   // Posts a refresh to the drawing area
   public void invalidate()
   {
      if (m_rawParentETGraphObject != null)
      {
         // Refresh the drawing area
         IDiagram pDiagram = m_rawParentETGraphObject.getDiagram();
         if (pDiagram != null)
         {
            pDiagram.refresh(true); // Don't use this its crashing durning creation.
            //				if (this.getDrawingArea() != null && this.getDrawingArea().getGraphWindow() != null)
            //				{
            //					this.getDrawingArea().getGraphWindow().drawGraph();
            //					this.getDrawingArea().getGraphWindow().fastRepaint();
            //				}
         }
      }

   }

   // IGraphObjectManager
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IGraphObjectManager#setParentETGraphObject(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject)
    */
   public void setParentETGraphObject(IETGraphObject value)
   {
      this.m_rawParentETGraphObject = value;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IGraphObjectManager#getParentETGraphObject()
    */
   public IETGraphObject getParentETGraphObject()
   {
      return this.m_rawParentETGraphObject;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IGraphObjectManager#onGraphEvent(int)
    */
   public void onGraphEvent(int nKind)
   {
      if (nKind == IGraphEventKind.GEK_POST_CREATE)
      {
         // Create the labels if there's something to display
         this.createInitialLabels();
      }
      else if (nKind == IGraphEventKind.GEK_POST_SELECT)
      {

         IETLabel etLabel = null;

         TSENode pTSENode = this.getOwnerNode();
         TSEEdge pTSEEdge = this.getOwnerEdge();

         if (pTSENode != null && pTSENode.labels() != null)
         {

            int length = pTSENode.labels().size();
            for (int i = 0; i < length; i++)
            {

               // Get the label
               TSNodeLabel pLabel = (TSNodeLabel)pTSENode.labels().get(i);
               if (pLabel != null)
               {

                  etLabel = TypeConversions.getETLabel(pLabel);

                  if (etLabel != null)
                  {
                     etLabel.onGraphEvent(nKind);
                  }

               }
            }
         }
         else if (pTSEEdge != null && pTSEEdge.labels() != null)
         {
            int length = pTSEEdge.labels().size();
            for (int i = 0; i < length; i++)
            {

               // Get the label
               TSEdgeLabel pLabel = (TSEdgeLabel)pTSEEdge.labels().get(i);
               if (pLabel != null)
               {

                  etLabel = TypeConversions.getETLabel(pLabel);

                  if (etLabel != null)
                  {
                     etLabel.onGraphEvent(nKind);
                  }
               }
            }
         }

      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IGraphObjectManager#validate(org.netbeans.modules.uml.core.metamodel.diagrams.IGraphObjectValidation)
    */
   public long validate(IGraphObjectValidation pValidationKind)
   {
      // Validate is called when friendly names is switched on and off.  We just reset
      // the text.
      this.resetLabelsText();
      return 0;
   }

   // ILabelManager
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#onContextMenu(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, int, int)
    */
   public void onContextMenu(IProductContextMenu pContextMenu, int logicalX, int logicalY)
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#onContextMenu(org.netbeans.modules.uml.ui.products.ad.application.IMenuManager)
    */
   public void onContextMenu(IMenuManager manager)
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#onContextMenuHandleSelection(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem)
    */
   public void onContextMenuHandleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem)
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#setSensitivityAndCheck(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem, int)
    */
   public void setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind)
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#resetLabels()
    */
   public void resetLabels()
   {
      // Just delete all the existing labels and recreate.
      this.discardAllLabels();
      this.createInitialLabels();
      this.invalidate();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#resetLabelsText()
    */
   public void resetLabelsText()
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#relayoutLabels()
    */
   public void relayoutLabels()
   {

      TSEEdge pTSEEdge = this.getOwnerEdge();
      if (pTSEEdge != null)
      {
         //NL TODO pTSEEdge->layoutLabelsInList(pTSEEdge->pLabelList());
      }
      else
      {
         TSENode pTSENode = this.getOwnerNode();
         if (pTSENode != null)
         {
            //NL TODO pTSENode->layoutLabelsInList(pTSENode->pLabelList());
         }
      }

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
    */
   public void modelElementHasChanged(INotificationTargets pTargets)
   {
      // Default implementation just resets the text of all the labels.
      this.resetLabelsText();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#handleEditChange(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel, java.lang.String)
    */
   public void handleEditChange(IETLabel pLabel, String sNewString)
   {

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#handleEditNoChange(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel, java.lang.String)
    */
   public void handleEditNoChange(IETLabel pLabel, String sNewString)
   {

   }

   // Resets the text for an individual label type
   public void resetLabelText(int nLabelKind)
   {
      if (nLabelKind == TSLabelKind.TSLK_STEREOTYPE)
      {
         // Reset the name text
         String sText = this.getStereotypeText();
         IETLabel pETLabel = this.getETLabelbyKind(nLabelKind);
         if (pETLabel != null)
         {
            if (sText != null && sText.length() > 0)
            {
               this.setLabelString(pETLabel, sText);
            }
            else
            {
               this.showLabel(nLabelKind, false);
            }
         }
      }
   }

   // Sets the label string, resizes and invalidates to redraw
   public void setLabelString(IETLabel pETLabel, String sNewString)
   {

      String sOldText = pETLabel.getText();
      if (sOldText == null || !(sNewString.equals(sOldText)))
      {
         IDrawEngine pDrawEngine = pETLabel.getEngine();

         ILabelDrawEngine pLabelDrawEngine = (ILabelDrawEngine)pDrawEngine;
         if (pLabelDrawEngine != null)
         {
            pLabelDrawEngine.setText(sNewString);
            pLabelDrawEngine.reposition();
            pLabelDrawEngine.sizeToContents();
            pLabelDrawEngine.invalidate();
         }
      }
   }

   // Handles the change of a stereotype - this will display it if necessary
   public void handleStereotypeChange(INotificationTargets pTargets)
   {

      IElement pChangedElement = pTargets.getChangedModelElement();
      int nKind = pTargets.getKind();
      IElement pThisElement = this.getModelElement();

      boolean bIsSame = pChangedElement != null ? pChangedElement.isSame(pThisElement) : false;
      // If this model element changed and it's a stereotype then show the new stereo label.
      if (bIsSame && (nKind == ModelElementChangedKind.MECK_STEREOTYPEAPPLIED || nKind == ModelElementChangedKind.MECK_STEREOTYPEDELETED))
      {

         boolean bCurrentlyShown = this.isDisplayed(TSLabelKind.TSLK_STEREOTYPE);
         if (!bCurrentlyShown)
         {
            this.showStereotypeLabel();
         }
         else
         {
            // Reset the stereotype text
            this.resetLabelText(TSLabelKind.TSLK_STEREOTYPE);
         }
      }
   }

   // Returns the owner node
   public TSENode getOwnerNode()
   {
      TSENode retValue = null;
      if (this.m_rawParentETGraphObject != null && this.m_rawParentETGraphObject.isNode())
      {
         retValue = (TSENode)this.m_rawParentETGraphObject.getObject();
      }
      return retValue;
   }

   // Returns the owner edge
   public TSEEdge getOwnerEdge()
   {
      TSEEdge retValue = null;
      if (this.m_rawParentETGraphObject != null && this.m_rawParentETGraphObject.isEdge())
      {
         retValue = (TSEEdge)this.m_rawParentETGraphObject.getObject();
      }
      return retValue;

   }

   // Deletes all the labels
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#discardAllLabels()
    */
   public void discardAllLabels()
   {
      TSENode pTSENode = this.getOwnerNode();

      if (pTSENode != null)
      {
         if (pTSENode.labels() != null && pTSENode.labels().size() > 0)
         {
            // Use the cmd so that a pre delete message goes to the drawing area

            IDrawingAreaControl drawingArea = this.getDrawingArea();
            if (drawingArea != null)
            {

               TSGroupCommand groupCommand = new TSGroupCommand();

               Iterator iter = pTSENode.labels().iterator();

               while (iter.hasNext())
               {
                  TSCommand command = new TSDeleteNodeLabelCommand((TSNodeLabel)iter.next());
                  groupCommand.add(command);
               }

               drawingArea.getGraphWindow().transmit(groupCommand);
            }
         }
      }
      else
      {

         TSEEdge pTSEEdge = this.getOwnerEdge();
         if (pTSEEdge != null)
         {
            if (pTSEEdge.labels() != null && pTSEEdge.labels().size() > 0)
            {

               // Use the cmd so that a pre delete message goes to the drawing area
               IDrawingAreaControl drawingArea = this.getDrawingArea();
               if (drawingArea != null)
               {

                  TSGroupCommand groupCommand = new TSGroupCommand();

                  Iterator iter = pTSEEdge.labels().iterator();

                  while (iter.hasNext())
                  {
                     TSCommand command = new TSDeleteEdgeLabelCommand((TSEdgeLabel)iter.next());
                     groupCommand.add(command);
                  }

                  drawingArea.getGraphWindow().transmit(groupCommand);

               }
            }
         }
      }
   }

   // Deletes the label by type
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#discardLabel(int)
    */
   public void discardLabel(int nLabelKind)
   {
      boolean done = false;
      int index = 0;

      IDrawingAreaControl drawingArea = this.getDrawingArea();

      while (!done)
      {
         IETLabel pETLabel = this.getETLabelbyIndex(index);

         if (pETLabel != null)
         {

            int nThisLabelKind = pETLabel.getLabelKind();

            if (nThisLabelKind == nLabelKind)
            {
               done = true;

               TSEEdge pTSEEdge = this.getOwnerEdge();

               if (pTSEEdge != null)
               {
                  //pETLabel = 0; // Do this or we crash!

                  TSEdgeLabel pLabel = (TSEdgeLabel)pTSEEdge.labels().get(index);

                  if (drawingArea != null)
                  {
                     drawingArea.getGraphWindow().transmit(new TSDeleteEdgeLabelCommand(pLabel));
                  }

               }
               else
               {
                  TSENode pTSENode = this.getOwnerNode();
                  if (pTSENode != null)
                  {
                     //pETLabel = 0; // Do this or we crash!
                     TSNodeLabel pLabel = (TSNodeLabel)pTSENode.labels().get(index);

                     // Use the cmd so that a pre delete message goes to the drawing area
                     if (drawingArea != null)
                     {
                        drawingArea.getGraphWindow().transmit(new TSDeleteNodeLabelCommand(pLabel));
                     }
                  }
               }
            }
         }
         else
         {
            done = true;
         }
         index++;
      }
      this.invalidate();
   }

   // Deletes the product label
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#discardETLabel(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel)
    */
   public void discardETLabel(IETLabel pETLabel)
   {

      boolean hr = false;
      TSLabel pInLabel = (TSLabel)pETLabel;

      if (pInLabel != null)
      {
         TSENode pTSENode = this.getOwnerNode();
         TSEEdge pTSEEdge = this.getOwnerEdge();

         for (int iIndx = 0; hr != true; iIndx++)
         {
            if (pTSENode != null && pTSENode.labels() != null)
            {
               if (iIndx > pTSENode.labels().size())
               {
                  break;
               }

               TSNodeLabel pLabel = (TSNodeLabel)pTSENode.labels().get(iIndx);
               if (pInLabel == pLabel)
               {
                  pTSENode.discard(pLabel);

                  hr = true; // found it, and deleted it
               }
            }
            else if (pTSEEdge != null && pTSEEdge.labels() != null)
            {
               if (iIndx > pTSEEdge.labels().size())
               {
                  break;
               }

               TSEdgeLabel pLabel = (TSEdgeLabel)pTSEEdge.labels().get(iIndx);
               if (pInLabel == pLabel)
               {
                  pTSEEdge.discard(pLabel);

                  hr = true; // found it, and deleted it
               }
            }
         }
      }

      if (hr)
      {
         this.invalidate();
      }
   }

   // Shows this label
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#showLabel(int, boolean)
    */
   public void showLabel(int nLabelKind, boolean bShow)
   {

   }

   // Does this label manager know how to display this label?
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#isValidLabelKind(int)
    */
   public boolean isValidLabelKind(int nLabelKind)
   {
      return true;
   }

   // Get the presentation element for this label
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#getLabel(int)
    */
   public IPresentationElement getLabel(int nLabelKind)
   {
      IPresentationElement retValue = null;
      boolean done = false;
      int index = 0;
      while (!done)
      {
         IETLabel pETLabel = this.getETLabelbyIndex(index);
         if (pETLabel != null)
         {
            int nThisLabelKind = TSLabelKind.TSLK_UNKNOWN;
            nThisLabelKind = pETLabel.getLabelKind();
            if (nThisLabelKind == nLabelKind)
            {
               retValue = TypeConversions.getPresentationElement(pETLabel);
               done = true;
            }
         }
         else
         {
            done = true;
         }
         index++;
      }
      return retValue;
   }

   // Edits this label
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#editLabel(int)
    */
   public void editLabel(int nLabelKind)
   {
      IPresentationElement pPE = this.getLabel(nLabelKind);

      ILabelPresentation pLabelPE = pPE instanceof ILabelPresentation ? (ILabelPresentation)pPE : null;
      if (pLabelPE != null)
      {
         pLabelPE.beginEdit();
      }
   }

   // The rectangle encompassing all the labels, in logical coordinates
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#getLogicalBoundingRectForAllLabels()
    */
   public IETRect getLogicalBoundingRectForAllLabels()
   {
      IETRect retValue = null;

      IETRect cpRectOut = new ETRect();

      // loop through all the labels collecting the union of all their bounding rectangles
      TSENode pTSENode = this.getOwnerNode();
      TSEEdge pTSEEdge = this.getOwnerEdge();

      if (pTSENode != null && pTSENode.labels() != null)
      {

         // TS indexes are 1 relative
         for (int lIndx = 0; lIndx < pTSENode.labels().size(); lIndx++)
         {

            // Get the label
            TSNodeLabel pLabel = (TSNodeLabel)pTSENode.labels().get(lIndx);
            this.unionLabelsLogicalBoundingRect(pLabel, cpRectOut);
         }
      }
      else if (pTSEEdge != null && pTSEEdge.labels() != null)
      {

         // TS indexes are 1 relative
         for (int lIndx = 0; lIndx < pTSEEdge.labels().size(); lIndx++)
         {

            // Get the label
            TSEdgeLabel pLabel = (TSEdgeLabel)pTSEEdge.labels().get(lIndx);
            this.unionLabelsLogicalBoundingRect(pLabel, cpRectOut);
         }
      }

      retValue = cpRectOut;
      return retValue;
   }

   // Is this label displayed?
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#isDisplayed(int)
    */
   public boolean isDisplayed(int nLabelKind)
   {
      boolean retValue = false;
      boolean done = false;
      int index = 0;

      while (!done)
      {
         IETLabel pETLabel = this.getETLabelbyIndex(index);
         if (pETLabel != null)
         {
            int nThisLabelKind = TSLabelKind.TSLK_UNKNOWN;
            nThisLabelKind = pETLabel.getLabelKind();
            if (nThisLabelKind == nLabelKind)
            {
               done = true;
               retValue = true;
            }
         }
         else
         {
            done = true;
         }
         index++;
      }
      return retValue;
   }

   // Retrieve a product label by its index (0 relative index)
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#getLabelByIndex(int)
    */
   public IETLabel getLabelByIndex(int lIndex)
   {
      return this.getETLabelbyIndex(lIndex);
   }

   // Retrieve a product label by its id (get_ID from the tsgraphobject)
   public IETLabel getLabelByID(int nID)
   {

      IETLabel retValue = null;

      TSENode pTSENode = this.getOwnerNode();
      TSEEdge pTSEEdge = this.getOwnerEdge();

      if (pTSENode != null && pTSENode.labels() != null)
      {

         int length = pTSENode.labels().size();
         for (int i = 0; i < length; i++)
         {
            // Get the label
            TSNodeLabel pLabel = (TSNodeLabel)pTSENode.labels().get(i);
            if (pLabel != null)
            {
               long thisID = 0;

               thisID = pLabel.getID();

               if (thisID == nID)
               {
                  retValue = TypeConversions.getETLabel(pLabel);
               }
            }
         }
      }
      else if (pTSEEdge != null && pTSEEdge.labels() != null)
      {
         int length = pTSEEdge.labels().size();
         for (int i = 0; i < length; i++)
         {
            // Get the label
            TSEdgeLabel pLabel = (TSEdgeLabel)pTSEEdge.labels().get(i);
            if (pLabel != null)
            {

               long thisID = 0;

               thisID = pLabel.getID();

               if (thisID == nID)
               {
                  retValue = TypeConversions.getETLabel(pLabel);
               }
            }
         }
      }

      return retValue;
   }

   // Creates a label if the string is not ""
   public IETLabel createLabelIfNotEmpty(String sName, int nLabelKind, int nPlacementKind, IElement pParentElement)
   {

      IETLabel retValue = null;

      if (sName != null && sName.length() > 0)
      {
         retValue = this.createLabel(sName, nLabelKind, nPlacementKind, pParentElement);
      }

      return retValue;
   }

   // Creates a label if the string is not ""
   public IETLabel createLabelIfNotEmpty(String sName, int nLabelKind, IETPoint pLogicalOffset, IElement pParentElement)
   {
      IETLabel retValue = null;

      if (sName != null && sName.length() > 0)
      {
         retValue = this.createLabel(sName, nLabelKind, pLogicalOffset, pParentElement);
      }

      return retValue;
   }

   // Creates a label
   public IETLabel createLabel(String sName, int nLabelKind, int nPlacementKind, IElement pParentElement)
   {

      IETLabel retValue = null;

      IElement pElementForLabel = pParentElement;

      IETGraphObjectUI labelUI = null;
      IDiagram cpDiagram = null;
      IDrawingAreaControl cpDA = null;

      if (m_rawParentETGraphObject != null)
      {
         // Get the parent drawing area
         cpDiagram = m_rawParentETGraphObject.getDiagram();
         if (cpDiagram != null && cpDiagram instanceof IUIDiagram)
         {
            cpDA = ((IUIDiagram)cpDiagram).getDrawingArea();
         }
      }

      if (cpDiagram != null && cpDA != null)
      {

         IETLabel pTempETLabel = null;

         TSEEdge pTSEEdge = this.getOwnerEdge();
         TSENode pTSENode = this.getOwnerNode();

         if (pTSEEdge != null)
         {

            TSEdgeLabel pCreatedLabel = null;

            // Add the label to the edge
            if (pElementForLabel == null)
            {
               // Use the parent as the target element
               pElementForLabel = TypeConversions.getElement(pTSEEdge);
            }

            if (pElementForLabel != null)
            {

               cpDA.setModelElement(pParentElement);

               try
               {
                  labelUI =
                     ETUIFactory.createEdgeLabelUI(
                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericEdgeLabelUI",
                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericEdgeLabelUI EdgeLabel",
                        "ADLabelDrawEngine",
                        cpDA);

                  pCreatedLabel = pTSEEdge.addLabel();

                  if (pCreatedLabel != null)
                  {
                     pCreatedLabel.setText(sName);
                     pTempETLabel = TypeConversions.getETLabel(pCreatedLabel);
                     pTempETLabel.setLabelView((TSELabelUI)labelUI);
                     cpDA.postAddObject(pTempETLabel, false);
                  }

                  cpDA.setModelElement(null);

               }
               catch (ETException e)
               {
                  e.printStackTrace();
               }

            }

         }
         else if (pTSENode != null)
         {
            TSNodeLabel pCreatedLabel = null;

            // Add the label to the node
            if (pElementForLabel == null)
            {
               // Use the parent as the target element
               pElementForLabel = TypeConversions.getElement(pTSENode);
            }

            if (pElementForLabel != null)
            {

               cpDA.setModelElement(pParentElement);

               try
               {

                  labelUI =
                     ETUIFactory.createNodeLabelUI(
                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeLabelUI",
                        "org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeLabelUI NodeLabel",
                        "ADLabelDrawEngine",
                        cpDA);

                  pCreatedLabel = pTSENode.addLabel();

                  if (pCreatedLabel != null)
                  {
                     pCreatedLabel.setText(sName);
                     pTempETLabel = TypeConversions.getETLabel(pCreatedLabel);
                     pTempETLabel.setLabelView((TSELabelUI)labelUI);
                     cpDA.postAddObject(pTempETLabel, false);
                  }

                  cpDA.setModelElement(null);

               }
               catch (ETException e)
               {
                  e.printStackTrace();
               }

            }

         }

         if (pTempETLabel != null)
         {

            pTempETLabel.setDiagram(cpDiagram);
            pTempETLabel.setLabelKind(nLabelKind);
            pTempETLabel.setLabelPlacement(nPlacementKind);

            this.setLabelsModelElement(pTempETLabel, pParentElement);

            //				pTempETLabel.setText("");
            //
            //				if (sName != null && sName.length() > 0) {
            //					pTempETLabel.setText(sName);
            //				}

            //				pTempETLabel.sizeToContents();

            if (nPlacementKind != TSLabelPlacementKind.TSLPK_SPECIFIED_XY)
            {
               pTempETLabel.reposition();
               this.relayoutThisLabel(pTempETLabel);
            }

            // Tell the draw engine to redraw
            IDrawEngine pDrawEngine = pTempETLabel.getEngine();

            ILabelDrawEngine pLabelDrawEngine = (ILabelDrawEngine)pDrawEngine;
            if (pLabelDrawEngine != null)
            {
               IPresentationElement pPE = pTempETLabel.getPresentationElement();
               pLabelDrawEngine.initCompartments(pPE);
               pLabelDrawEngine.initResources();
               pLabelDrawEngine.sizeToContents();
               pLabelDrawEngine.invalidate();
            }

            invalidate();
            retValue = pTempETLabel;
         }
      }

      return retValue;
   }

   // Creates a label
   public IETLabel createLabel(String sName, int nLabelKind, IETPoint pLogicalOffset, IElement pParentElement)
   {

      IETLabel pTempETLabel = this.createLabel(sName, nLabelKind, TSLabelPlacementKind.TSLPK_SPECIFIED_XY, pParentElement);
      if (pTempETLabel != null)
      {
         if (pLogicalOffset != null)
         {
            pTempETLabel.setSpecifiedXY(pLogicalOffset);
            pTempETLabel.reposition();
         }

         // Tell the draw engine to redraw
         IDrawEngine pDrawEngine = pTempETLabel.getEngine();

         ILabelDrawEngine pLabelDrawEngine = (ILabelDrawEngine)pDrawEngine;
         if (pLabelDrawEngine != null)
         {
            pLabelDrawEngine.invalidate();
         }
         invalidate();
      }

      return pTempETLabel;
   }

   // Returns the default name from the preference manager
   public String retrieveDefaultName()
   {
      String retValue = null;

      String bstrName;
      IPreferenceAccessor pPref = PreferenceAccessor.instance();
      if (pPref != null)
      {
         retValue = pPref.getDefaultElementName();
      }

      return retValue;
   }

   // Is the parent diagram readonly
   public boolean isParentDiagramReadOnly()
   {
      boolean bParentIsReadonly = true;

      if (m_rawParentETGraphObject != null)
      {
         // Refresh the drawing area
         IDiagram pDiagram = this.m_rawParentETGraphObject.getDiagram();

         if (pDiagram != null)
         {
            bParentIsReadonly = pDiagram.getReadOnly();
         }
      }

      return bParentIsReadonly;
   }

   // Post a message to the drawing area control to edit the product label
   public void postEditLabel(ILabelPresentation pLabelPresentation)
   {
      if (pLabelPresentation != null)
      {
         // Fix W1001:  Edit the operation label
         IDrawEngine cpEngine = TypeConversions.getDrawEngine(pLabelPresentation);
         if (cpEngine != null)
         {
            IDiagram cpDiagram = cpEngine.getDiagram();
            if (cpDiagram != null && cpDiagram instanceof IUIDiagram)
            {
               IDrawingAreaControl cpDA = ((IUIDiagram)cpDiagram).getDrawingArea();
               if (cpDA != null)
               {
                  cpDA.postEditLabel(pLabelPresentation);
               }
            }
         }
      }

   }

   public void postEditLabel(IETLabel pETLabel)
   {
      IPresentationElement cpPresentationElement = TypeConversions.getPresentationElement(pETLabel);
      ILabelPresentation cpLabelPresentation = (ILabelPresentation)cpPresentationElement;

      this.postEditLabel(cpLabelPresentation);
   }

   // Sets the forced stereotype string used to show a stereotype label when no actual stereotype is involved
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager#setForcedStereotypeString(java.lang.String)
    */
   public void setForcedStereotypeString(String sForcedString)
   {
      if (sForcedString != null && sForcedString.length() > 0)
      {
         this.m_forcedStereotypeString = sForcedString;
      }
      else
         this.m_forcedStereotypeString = "";
   }

   // Returns the parent drawing area control
   public IDiagram getDiagram()
   {
      IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(m_rawParentETGraphObject);
      return pDrawEngine != null ? pDrawEngine.getDiagram() : null;
   }

   // Returns the IAxDrawingAreaControl that matches our IDiagram
   public IDrawingAreaControl getDrawingArea()
   {

      IDrawingAreaControl retValue = null;
      IDiagram pDiagram = (IUIDiagram)this.getDiagram();

      if (pDiagram != null && pDiagram instanceof IUIDiagram)
      {
         retValue = ((IUIDiagram)pDiagram).getDrawingArea();
      }

      return retValue;
   }

   // Layouts out just this layout
   protected void relayoutThisLabel(IETLabel pETLabel)
   {
      //		// Fix W5650:  Had to make this operation virtual so that
      //		//             MessageLabelManagerImpl::RelayoutThisLabel() could be called.
      //
      //		HR_PARM_CHECK(pETLabel);
      //   
      //		HRESULT hr = S_OK;
      //		try
      //		{
      //		   if (CUserInputBlocker::GetIsDisabled(GBK_DIAGRAM_LABEL_LAYOUT))
      //		   {
      //			  // We are blocked, don't layout
      //			  return S_OK;
      //		   }
      //
      //		   TSEEdge* pTSEEdge = pOwnerEdge();
      //		   if (pTSEEdge)
      //		   {
      //			  TSEdgeLabelSList edgeList;
      //			  TSEEdgeLabel* pTSEEdgeLabel = CTypeConversions::pEdgeLabel(pETLabel);
      //
      //			  if (pTSEEdgeLabel)
      //			  {
      //				 edgeList.appendLabel(pTSEEdgeLabel);
      //				 pTSEEdge->layoutLabelsInList(&edgeList);
      //				 edgeList.deleteAllCells();
      //			  }
      //		   }
      //		   else
      //		   {
      //			  TSENode* pTSENode = pOwnerNode();
      //			  if (pTSENode)
      //			  {
      //				 pTSENode->layoutLabelsInList(pTSENode->pLabelList());
      //			  }
      //		   }
      //		}
      //		catch ( _com_error& err )
      //		{
      //		   hr = COMErrorManager::ReportError( err );
      //		}
      //		return hr;
   }

   // Returns the product label at this index (0 relative index)
   protected IETLabel getETLabelbyIndex(int index)
   {

      IETLabel retValue = null;

      TSENode pTSENode = this.getOwnerNode();
      TSEEdge pTSEEdge = this.getOwnerEdge();

      if (pTSENode != null && pTSENode.labels() != null && index < pTSENode.labels().size())
      {

         // Get the label
         TSENodeLabel pNodeLabel = (TSENodeLabel)pTSENode.labels().get(index);

         if (pNodeLabel != null)
         {
            retValue = TypeConversions.getETLabel(pNodeLabel);
         }

      }
      else if (pTSEEdge != null && pTSEEdge.labels() != null && index < pTSEEdge.labels().size())
      {

         // Get the label
         TSEEdgeLabel pEdgeLabel = (TSEEdgeLabel)pTSEEdge.labels().get(index);

         if (pEdgeLabel != null)
         {
            retValue = TypeConversions.getETLabel(pEdgeLabel);
         }
      }

      return retValue;
   }

   protected IETLabel getETLabelbyKind(int eKind)
   {

      IETLabel retValue = null;

      IETLabel cpETLabel = null;
         for (int lIndx = 0; /* break below */; lIndx++)
      {
         cpETLabel = null;
         cpETLabel = this.getETLabelbyIndex(lIndx);
         if (cpETLabel == null)
         {
            break;
         }

         int eCurrentKind = TSLabelKind.TSLK_UNKNOWN;
         eCurrentKind = cpETLabel.getLabelKind();
         if (eCurrentKind == eKind)
         {
            retValue = cpETLabel;
            break;
         }
      }
      return retValue;
   }

   // Updates the model element for the product label
   protected void setLabelsModelElement(IETLabel pETLabel, IElement pParentElement)
   {

      if ((pETLabel != null && pParentElement != null))
      {
         // By default the label is attached to the edge's model element.
         // If the user wants it attached to something else then get the presentation
         // element, detach and attach it to the new one
         IPresentationElement cpPE = pETLabel.getPresentationElement();

         ILabelPresentation pLabelPE = (ILabelPresentation)cpPE;
         if (pLabelPE != null)
         {
            pLabelPE.setModelElement(pParentElement);
         }

         // Re-put the presentation element so the compartments get reinitialized with the
         // correct model elements.
         pETLabel.setPresentationElement(cpPE);

      }

   }

   // Union the label's logical rectangle with the in/out rectangle
   protected IETRect unionLabelsLogicalBoundingRect(TSLabel pLabel, IETRect pRect)
   {
      IETRect retValue = null;
      if (pLabel != null)
      {
         IETRect rectTS = new ETRectEx(pLabel.getBounds());

         retValue = RectConversions.unionTSCoordinates(pRect, rectTS);

         pRect = null;
      }
      return retValue;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.application.action.IETContextMenuHandler#setSensitivityAndCheck(java.lang.String, org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass)
    */
   public boolean setSensitivityAndCheck(String menuID, ContextMenuActionClass pMenuAction)
   {
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.application.action.IETContextMenuHandler#onHandleButton(java.awt.event.ActionEvent, java.lang.String)
    */
   public boolean onHandleButton(ActionEvent e, String menuID)
   {
      return false;
   }

   /*
    * Searches through all the compartments looking for the specified compartment 
    */
   public < Type > Type getCompartmentByKind(IDrawEngine engine, Class interfacetype)
   {
      try
      {
         IteratorT < ICompartment > iter = new IteratorT < ICompartment > (engine.getCompartments());
         while (iter.hasNext())
         {
            ICompartment comp = iter.next();
            if (interfacetype.isAssignableFrom(comp.getClass()))
            {
               return (Type)comp;
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      return null;
   }

}
