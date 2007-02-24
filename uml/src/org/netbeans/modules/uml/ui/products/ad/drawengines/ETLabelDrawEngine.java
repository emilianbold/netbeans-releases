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


package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleRelation;
import javax.accessibility.AccessibleRelationSet;
import javax.accessibility.AccessibleRole;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IMultiplicity;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.ADDrawEngineButtonHandler;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADLabelNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IMessageConnectorLabelListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.MessageConnectorLabelListCompartment;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISupportEnums;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.LabelDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PointConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelPlacementKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.UIResources;
import com.tomsawyer.drawing.TSEdgeLabel;
import com.tomsawyer.drawing.TSLabel;
import com.tomsawyer.drawing.TSNodeLabel;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEEdgeLabel;
import com.tomsawyer.editor.TSEFont;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSENodeLabel;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.editor.ui.TSEEdgeUI;
import com.tomsawyer.editor.ui.TSELabelUI;
import com.tomsawyer.editor.ui.TSENodeUI;
import com.tomsawyer.graph.TSGraphObject;
//import com.tomsawyer.layout.java.property.TSIntLayoutProperty;
//import com.tomsawyer.layout.java.property.TSLayoutPropertyEnums;
//import com.tomsawyer.layout.java.property.TSTailorProperties;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

public class ETLabelDrawEngine extends LabelDrawEngine implements IADLabelDrawEngine
{

   public static final String CURRENTTEXT_STRING = "labelText";

   protected final int WIDTH = 75;
   protected final int HEIGHT = 20;

   public static final int ICON_LABEL_SIZE = 20;

   protected int m_nNameFontStringID = -1;

   // The button handler that handles the context menu
   ADDrawEngineButtonHandler m_ButtonHandler;

   // Did we do the upgrade check?  See ADLabelDrawEngineImpl::Draw
   boolean m_bDidUpgradeCheck;

   public ETLabelDrawEngine()
   {
      super();
   }

   public String getElementType()
   {
      String type = super.getElementType();
      if (type == null)
      {
         type = new String("LabelPresentation");
      }
      return type;
   }

   // Creates the button handler for this label
   public void createButtonHandler()
   {
      m_ButtonHandler = new ADDrawEngineButtonHandler(this);
   }

   // This is the name of the drawengine used when storing and reading from the product archive
   public String getDrawEngineID()
   {

      return "ADLabelDrawEngine";
   }

   public void initCompartments(IPresentationElement pElement)
   {
      // We may get here with no compartments.  This happens if we've been created
      // by the user.  If we read from a file then the compartments have been pre-created and
      // we just need to initialize them.
      int numCompartments = 0;

      numCompartments = this.getNumCompartments();
      if (numCompartments == 0)
      {
         createCompartments();
      }

      if (pElement != null)
      {
         IElement pModelElement = pElement != null ? pElement.getFirstSubject() : null;

         if (pModelElement != null)
         {
            ICompartment pCompartment = this.getCompartment(0);

            if (pCompartment != null)
            {
               pCompartment.addModelElement(pModelElement, -1);

               IADLabelNameCompartment pNameCompartment = this.getLabelNameCompartment();

               if (pNameCompartment != null)
               {
                  this.setDefaultCompartment(pNameCompartment);
               }
            }
         }
      }
   }

   public void createCompartments()
   {

      clearCompartments();

      if (isMessageConnectorLabel())
      {
         createAndAddCompartment("MessageConnectorLabelListCompartment", 0);
      }
      else
      {
         createAndAddCompartment("ADLabelNameCompartment", 0);
      }
   }

   // This is the name of the drawengine used when storing and reading from the product archive
   public void initResources()
   {
      m_nBorderStringID = m_ResourceUser.setResourceStringID(m_nBorderStringID, "labeliconborder", (new Color(255, 255, 255)).getRGB());
      m_nNameFontStringID = m_ResourceUser.setResourceStringID(m_nNameFontStringID, "label", (Color.BLACK).getRGB());

      super.initResources();
   }

   protected String getDefaultResourceID(int nKind)
   {
      String retValue = null;

      switch (nKind)
      {
         case UIResources.CK_FONT :
            retValue = "LabelFont";
            break;
         case UIResources.CK_FILLCOLOR :
            retValue = "LabelFillColor";
            break;
         case UIResources.CK_TEXTCOLOR :
            retValue = "LabelTextColor";
            break;
         case UIResources.CK_BORDERCOLOR :
            retValue = "LabelBorderColor";
            break;
      }
      return retValue;
   }


    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getResourceName(int)
     */
    public String getResourceName(int nKind) {
	
	String retValue = "";
	
	switch (nKind)
	    {
	    case UIResources.CK_FONT :
		retValue = "label";
		break;
	    case UIResources.CK_TEXTCOLOR :
		retValue = "label";
		break;
	    }
	return retValue;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setFontResource()
     */
    public void setFontResource(int resourceKind, Font font) {
	// code refactored from font menu handling in ADCoreEngine
	super.setFontResource(resourceKind, font);
	sizeToContents();
	invalidate();	
    }


   public void doDraw(IDrawInfo pInfo)
   {

      TSEGraphics graphics = pInfo.getTSEGraphics();
      IETGraphObjectUI parentUI = this.getParent();

      if (parentUI.getOwner() == null)
      {
         return;
      }

      IETRect boundingRect = pInfo.getBoundingRect();

      if (isIconLabel())
      {

         // Draw a lightning bold
         IDrawInfo pTSEDrawInfo = pInfo;

         if (pTSEDrawInfo != null)
         {

            // perform color overrides
            //				COLORREF crBorderColor = GetColorDefaultText(CK_TEXTCOLOR, pTSEDrawInfo - > dc());

            long arrowSize = 5;

            TSConstPoint topLeft = new TSConstPoint(boundingRect.getTopLeft().getX(), boundingRect.getTopLeft().getY());
            TSConstPoint topRight = new TSConstPoint(boundingRect.getRight(), boundingRect.getTop());
            TSConstPoint bottomLeft = new TSConstPoint(boundingRect.getLeft(), boundingRect.getBottom() - arrowSize);
            TSConstPoint bottomRight = new TSConstPoint(boundingRect.getRight(), boundingRect.getBottom() - arrowSize);
            TSConstPoint arrowTop = new TSConstPoint(bottomRight.getX() - arrowSize, bottomRight.getY() - arrowSize);
            TSConstPoint arrowBottom = new TSConstPoint(arrowTop.getX(), boundingRect.getBottom());

            graphics.drawLine(topLeft, topRight);
            graphics.drawLine(topRight, bottomLeft);
            graphics.drawLine(bottomLeft, bottomRight);
            graphics.drawLine(bottomRight, arrowTop);
            graphics.drawLine(bottomRight, arrowBottom);
         }

         // Put the selection handles
         //			CGDISupport : : DrawSelectionHandles(pInfo);
      }
      //		 else {
      //			// UPGRADE ISSUE : Developer released with multiplicity labels attached to the 
      //			// IAssociationEnd rather then the IMultiplicity.  We account for that change here.
      //			if (m_bDidUpgradeCheck == false) {
      //				m_bDidUpgradeCheck = true;
      //
      //				if (isMultiplicityLabel()) {
      //
      //					IElement pElement = this.getFirstModelElement();
      //
      //					IAssociationEnd pAssEnd = (IAssociationEnd) pElement;
      //
      //					if (pAssEnd != null) {
      //
      //						IPresentationElement pPE = this.getPresentationElement();
      //
      //						IMultiplicity pMult = pAssEnd.getMultiplicity();
      //
      //						if (pMult != null) {
      //
      //							IETLabel pETLabel = TypeConversions.getETLabel(pPE);
      //
      //							if (pETLabel != null) {
      //
      //								// Connect the PE to a new model element
      //								IPresentationElement cpPE = pETLabel.getPresentationElement();
      //
      //								ILabelPresentation pLabelPE = (ILabelPresentation) cpPE;
      //
      //								if (pLabelPE != null) {
      //									pLabelPE.setModelElement(pMult);
      //								}
      //
      //								// Re-put the presentation element so the compartments get reinitialized with the
      //								// correct model elements.
      //								pETLabel.setPresentationElement(cpPE);
      //							}
      //						}
      //					}
      //				}
      //			}
      //		}

      // Verify the compartments
      verifyCompartments();

      //TODO Force the stereotype label to show the stereotype text instead of nothing. There must be a missing
      // link somewhere in order to handle stereotype labels. When the compartment is created with the presentation
      // element it overwrites the stereotype text. This problem also appears in the C++ codebase when editing stereotype labels
      setText(getText());

      Iterator < ICompartment > iterator = this.getCompartments().iterator();
      while (iterator.hasNext())
      {
         iterator.next().draw(pInfo, pInfo.getDeviceBounds());
      }

      // Put the selection handles
      //		   CGDISupport::DrawSelectionHandles(pInfo);

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelDrawEngine#reposition()
    */
   // Move the label to the correct position.
   public void reposition()
   {

      IETLabel pETLabel = this.getParentETLabel();

      if (pETLabel != null)
      {
         int nPlacement = pETLabel.getLabelPlacement();

         TSLabel pLabel = this.getParentLabel();

         // Now do the positioning
         if (this.getOwnerNode() != null)
         {
            TSNodeLabel pNodeLabel = (TSNodeLabel)pLabel;

            if (pNodeLabel != null)
            {
               // Get the bound box for the label and its owner (an edge or a node)
               IETRect logicalBounds = this.getOwnerBoundingRect();

               IETRect rectLogicalOwner = logicalBounds;

               IETRect logicalLabelRect = this.getLogicalBoundingRect();

               switch (nPlacement)
               {
                  case TSLabelPlacementKind.TSLPK_CENTER_ABOVE :
                  case TSLabelPlacementKind.TSLPK_TO_NODE_ABOVE :
                  case TSLabelPlacementKind.TSLPK_FROM_NODE_ABOVE :
                     {
                        ETSystem.out.println("TSLabelPlacementKind.TSLPK_FROM_NODE_ABOVE");

                        this.moveTo(rectLogicalOwner.getCenterX(), rectLogicalOwner.getTop() + logicalLabelRect.getHeight());
/* commented by jyothi..
                        TSIntLayoutProperty property1 = new TSIntLayoutProperty(TSTailorProperties.NODE_LABEL_ORIENTATION);
                        property1.setCurrentValue(TSLayoutPropertyEnums.NODE_LABEL_ORIENTATION_OUTSIDE);
                        pNodeLabel.setTailorProperty(property1);

                        TSIntLayoutProperty property2 = new TSIntLayoutProperty(TSTailorProperties.NODE_LABEL_REGION);
                        property2.setCurrentValue(TSLayoutPropertyEnums.NODE_LABEL_REGION_ABOVE);
                        pNodeLabel.setTailorProperty(property2);
 */
                     }
                     break;
                  case TSLabelPlacementKind.TSLPK_CENTER_BELOW :
                  case TSLabelPlacementKind.TSLPK_FROM_NODE_BELOW :
                  case TSLabelPlacementKind.TSLPK_TO_NODE_BELOW :
                     {
                        ETSystem.out.println("TSLabelPlacementKind.TSLPK_TO_NODE_BELOW");

                        this.moveTo(rectLogicalOwner.getCenterX(), rectLogicalOwner.getBottom() - logicalLabelRect.getHeight());
/* commented by jyothi
                        TSIntLayoutProperty property1 = new TSIntLayoutProperty(TSTailorProperties.NODE_LABEL_ORIENTATION);
                        property1.setCurrentValue(TSLayoutPropertyEnums.NODE_LABEL_ORIENTATION_OUTSIDE);
                        pNodeLabel.setTailorProperty(property1);

                        TSIntLayoutProperty property2 = new TSIntLayoutProperty(TSTailorProperties.NODE_LABEL_REGION);
                        property2.setCurrentValue(TSLayoutPropertyEnums.NODE_LABEL_REGION_BELOW);
                        pNodeLabel.setTailorProperty(property2);
 */
                     }
                     break;

                  case TSLabelPlacementKind.TSLPK_SPECIFIED_XY :
                     {
                        ETSystem.out.println("TSLabelPlacementKind.TSLPK_SPECIFIED_XY");

                        Point ptSpecifiedXY = null;
                        {
                           IETPoint cpSpecifiedXY = pETLabel.getSpecifiedXY();
                           ptSpecifiedXY = PointConversions.ETPointToPoint(cpSpecifiedXY);
                        }

                        Point ptOwnerCenter = rectLogicalOwner.getCenterPoint();

                        moveTo(ptOwnerCenter.x + ptSpecifiedXY.x, ptOwnerCenter.y + ptSpecifiedXY.y);
/* commented by jyothi
                        TSIntLayoutProperty property1 = new TSIntLayoutProperty(TSTailorProperties.NODE_LABEL_ORIENTATION);
                        property1.setCurrentValue(TSLayoutPropertyEnums.NODE_LABEL_ORIENTATION_INSIDE);
                        pNodeLabel.setTailorProperty(property1);

                        TSIntLayoutProperty property2 = new TSIntLayoutProperty(TSTailorProperties.NODE_LABEL_REGION);
                        property2.setCurrentValue(TSLayoutPropertyEnums.NODE_LABEL_REGION_DONT_CARE);
                        pNodeLabel.setTailorProperty(property2);
*/
                     }
                     break;
               }
            }
         }
         else if (this.getOwnerEdge() != null && pLabel != null)
         {
            TSEdgeLabel pEdgeLabel = (TSEdgeLabel)pLabel;

            if (pEdgeLabel != null)
            {

               switch (nPlacement)
               {
                  case TSLabelPlacementKind.TSLPK_FROM_NODE_ABOVE :
                     {
                        ETSystem.out.println("TSLabelPlacementKind.TSLPK_FROM_NODE_ABOVE");

                        pEdgeLabel.setDistanceFromSource(0.10);
                        pEdgeLabel.setOffset(0.0, 10.0);
/* commented by jyothi
                        TSIntLayoutProperty property1 = new TSIntLayoutProperty(TSTailorProperties.EDGE_LABEL_REGION);
                        property1.setCurrentValue(TSLayoutPropertyEnums.EDGE_LABEL_REGION_ABOVE);
                        pEdgeLabel.setTailorProperty(property1);

                        TSIntLayoutProperty property2 = new TSIntLayoutProperty(TSTailorProperties.EDGE_LABEL_ASSOCIATION);
                        property2.setCurrentValue(TSLayoutPropertyEnums.EDGE_LABEL_ASSOCIATION_SOURCE);
                        pEdgeLabel.setTailorProperty(property2);
 */
                     }
                     break;
                  case TSLabelPlacementKind.TSLPK_FROM_NODE_BELOW :
                     {
                        ETSystem.out.println("TSLabelPlacementKind.TSLPK_FROM_NODE_BELOW");

                        pEdgeLabel.setDistanceFromSource(0.10);
                        pEdgeLabel.setOffset(0.0, -10.0);
/*
                        TSIntLayoutProperty property1 = new TSIntLayoutProperty(TSTailorProperties.EDGE_LABEL_REGION);
                        property1.setCurrentValue(TSLayoutPropertyEnums.EDGE_LABEL_REGION_BELOW);
                        pEdgeLabel.setTailorProperty(property1);

                        TSIntLayoutProperty property2 = new TSIntLayoutProperty(TSTailorProperties.EDGE_LABEL_ASSOCIATION);
                        property2.setCurrentValue(TSLayoutPropertyEnums.EDGE_LABEL_ASSOCIATION_SOURCE);
                        pEdgeLabel.setTailorProperty(property2);
 */
                     }
                     break;
                  case TSLabelPlacementKind.TSLPK_CENTER_ABOVE :
                     {
                        ETSystem.out.println("TSLabelPlacementKind.TSLPK_CENTER_ABOVE");

                        pEdgeLabel.setDistanceFromSource(0.5);
                        pEdgeLabel.setOffset(0.0, 10.0);
/* commented by jyothi
                        TSIntLayoutProperty property1 = new TSIntLayoutProperty(TSTailorProperties.EDGE_LABEL_REGION);
                        property1.setCurrentValue(TSLayoutPropertyEnums.EDGE_LABEL_REGION_ABOVE);
                        pEdgeLabel.setTailorProperty(property1);

                        TSIntLayoutProperty property2 = new TSIntLayoutProperty(TSTailorProperties.EDGE_LABEL_ASSOCIATION);
                        property2.setCurrentValue(TSLayoutPropertyEnums.EDGE_LABEL_ASSOCIATION_CENTER);
                        pEdgeLabel.setTailorProperty(property2);
 */
                     }
                     break;
                  case TSLabelPlacementKind.TSLPK_CENTER_BELOW :
                     {
                        ETSystem.out.println("TSLabelPlacementKind.TSLPK_CENTER_BELOW");

                        pEdgeLabel.setDistanceFromSource(0.5);
                        pEdgeLabel.setOffset(0.0, -10.0);
/* commented by jyothi
                        TSIntLayoutProperty property1 = new TSIntLayoutProperty(TSTailorProperties.EDGE_LABEL_REGION);
                        property1.setCurrentValue(TSLayoutPropertyEnums.EDGE_LABEL_REGION_BELOW);
                        pEdgeLabel.setTailorProperty(property1);

                        TSIntLayoutProperty property2 = new TSIntLayoutProperty(TSTailorProperties.EDGE_LABEL_ASSOCIATION);
                        property2.setCurrentValue(TSLayoutPropertyEnums.EDGE_LABEL_ASSOCIATION_CENTER);
                        pEdgeLabel.setTailorProperty(property2);
 */
                     }
                     break;
                  case TSLabelPlacementKind.TSLPK_TO_NODE_ABOVE :
                     {
                        ETSystem.out.println("TSLabelPlacementKind.TSLPK_TO_NODE_ABOVE");

                        pEdgeLabel.setDistanceFromSource(0.90);
                        pEdgeLabel.setOffset(0.0, 10.0);
/* commented by jyothi
                        TSIntLayoutProperty property1 = new TSIntLayoutProperty(TSTailorProperties.EDGE_LABEL_REGION);
                        property1.setCurrentValue(TSLayoutPropertyEnums.EDGE_LABEL_REGION_ABOVE);
                        pEdgeLabel.setTailorProperty(property1);

                        TSIntLayoutProperty property2 = new TSIntLayoutProperty(TSTailorProperties.EDGE_LABEL_ASSOCIATION);
                        property2.setCurrentValue(TSLayoutPropertyEnums.EDGE_LABEL_ASSOCIATION_TARGET);
                        pEdgeLabel.setTailorProperty(property2);
 */
                     }
                     break;
                  case TSLabelPlacementKind.TSLPK_TO_NODE_BELOW :
                     {
                        ETSystem.out.println("TSLabelPlacementKind.TSLPK_TO_NODE_BELOW");

                        pEdgeLabel.setDistanceFromSource(0.90);
                        pEdgeLabel.setOffset(0.0, -10.0);
/* commented by jyothi
                        TSIntLayoutProperty property1 = new TSIntLayoutProperty(TSTailorProperties.EDGE_LABEL_REGION);
                        property1.setCurrentValue(TSLayoutPropertyEnums.EDGE_LABEL_REGION_BELOW);
                        pEdgeLabel.setTailorProperty(property1);

                        TSIntLayoutProperty property2 = new TSIntLayoutProperty(TSTailorProperties.EDGE_LABEL_ASSOCIATION);
                        property2.setCurrentValue(TSLayoutPropertyEnums.EDGE_LABEL_ASSOCIATION_TARGET);
                        pEdgeLabel.setTailorProperty(property2);
 */
                     }
                     break;

                  case TSLabelPlacementKind.TSLPK_SPECIFIED_XY :
                     // This kind is used for nodes only, for now
                     break;
               }
            }
         }
      }
   }

   public void sizeToContents()
   {
      if (this.isIconLabel())
      {
         IETSize size = new ETSize(ICON_LABEL_SIZE, ICON_LABEL_SIZE);
         this.resize(size);
      }
      else
      {

         IDrawInfo info = getParent().getDrawInfo();
         IETSize size = calculateOptimumSize(info, true);

         if (size != null)
         {

            // Put some boundary on the resize
            size.setWidth(Math.max(WIDTH, size.getWidth() + 6));
            size.setHeight(Math.max(HEIGHT, size.getHeight() + 2));

            resize(size);
         }
      }
   }

   // Gets/Sets the text.  This should be overridden by the derived classes
   public String getText()
   {

      String retValue = null;

      IETLabel parent = this.getParentETLabel();

      if (parent == null)
      {
         return retValue;
      }

      if (parent.getLabelKind() != TSLabelKind.TSLK_STEREOTYPE)
      {
         IADLabelNameCompartment pNameCompartment = this.getLabelNameCompartment();
         if (pNameCompartment != null)
         {
            retValue = pNameCompartment.getName();
         }
      }
      else
      {
         // Break the circle of death.
         if (parent.getObject() != null && parent.getObject().getTag() != null)
         {
            retValue = parent.getObject().getTag().toString();
         }
      }

      return retValue;
   }

   public void setText(String pNewValue)
   {

      // Set the TSLabel text
      IETLabel parent = this.getParentETLabel();
      if (parent == null && parent instanceof TSLabel)
      {
         ((TSLabel)parent).setText(pNewValue);
      }

      IADLabelNameCompartment pNameCompartment = this.getLabelNameCompartment();
      if (pNameCompartment != null)
      {
         pNameCompartment.setName(pNewValue);
      }
   }

   /**
    * Tells the draw engine to write its data to the IProductArchive
    *
    * @param pProductArchive [in] The archive we're saving to
    * @param pParentElement [in] The current element, or parent for any new attributes or elements.
    */
   public long writeToArchive(IProductArchive pProductArchive, IProductArchiveElement pElement)
   {

      super.writeToArchive(pProductArchive, pElement);
      IProductArchiveElement engineEle = pElement.getElement(IProductArchiveDefinitions.ENGINENAMEELEMENT_STRING);
      if (engineEle != null)
      {
         String text = getText();
         engineEle.addAttributeString(CURRENTTEXT_STRING, text);
      }
      return 0;
   }

   /**
    * Tells the draw engine to read its data to the IProductArchive
    *
    * @param pProductArchive [in] The archive we're reading from
    * @param pEngineElement [in] The element where this draw engine's information should exist.
    */
   public long readFromArchive(IProductArchive pProductArchive, IProductArchiveElement pParentElement)
   {
      super.readFromArchive(pProductArchive, pParentElement);
      String text = pParentElement.getAttributeString(CURRENTTEXT_STRING);

      // Labels should be synced up right away since their text is related to the model element
      // and the label kind rather then just the model element.  Otherwise the dataformatter will
      // be used during the compartment reattach and the label text will change to whatever the
      // dataformatter returns

      performDeepSynch();

      setText(text);

      // Restore the default compartment
      IADLabelNameCompartment nameComp = getLabelNameCompartment();
      if (nameComp != null)
      {
         setDefaultCompartment(nameComp);
      }
      return 0;
   }

   // Are we a message connector label
   protected boolean isMessageConnectorLabel()
   {

      boolean retValue = false;

      int nLabelKind = TSLabelKind.TSLK_UNKNOWN;

      IETLabel pParentETLabel = this.getParentETLabel();

      if (pParentETLabel != null)
      {
         nLabelKind = pParentETLabel.getLabelKind();
      }

      if (nLabelKind == TSLabelKind.TSLK_MESSAGECONNECTOR_OPERATION_NAME)
      {
         retValue = true;
      }

      return retValue;
   }

   // Are we a multiplicity label
   protected boolean isMultiplicityLabel()
   {

      boolean retValue = false;

      int nLabelKind = TSLabelKind.TSLK_UNKNOWN;
      IETLabel pParentETLabel = this.getParentETLabel();
      if (pParentETLabel != null)
      {
         nLabelKind = pParentETLabel.getLabelKind();
      }

      if (nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END0_MULTIPLICITY || nLabelKind == TSLabelKind.TSLK_ASSOCIATION_END1_MULTIPLICITY)
      {
         retValue = true;
      }

      return retValue;
   }

   // This verifies that we've created the correct compartmenst
   protected void verifyCompartments()
   {

      // Make sure we have some compartments.  If not then create some
      int numCompartments = 0;

      numCompartments = this.getNumCompartments();

      if (numCompartments == 0)
      {
         this.createCompartments();
      }

      ICompartment pCompartment = this.getCompartment(0);
      IMessageConnectorLabelListCompartment pMessageConnectorListCompartment = null;

      if (pCompartment instanceof IMessageConnectorLabelListCompartment)
      {

         pMessageConnectorListCompartment = (IMessageConnectorLabelListCompartment)pCompartment;
      }

      if (this.isMessageConnectorLabel())
      {
         if (pMessageConnectorListCompartment == null)
         {
            pCompartment = null;

            // Recreate the compartments 'cause they are wrong for some reason.
            this.createCompartments();
         }
      }
      else
      {
         if (pMessageConnectorListCompartment != null)
         {
            pCompartment = null;
            //pMessageConnectorListCompartment.Release();

            // We're not a message connector so whack it.  This should never happen
            this.createCompartments();
         }
      }
   }

   // Returns the label name compartment
   protected IADLabelNameCompartment getLabelNameCompartment()
   {

      IADLabelNameCompartment retValue = null;

      ICompartment pCompartment = this.getCompartment(0);

      if (pCompartment != null && pCompartment instanceof IADLabelNameCompartment)
      {

         retValue = (IADLabelNameCompartment)pCompartment;

      }
      else if (pCompartment != null && pCompartment instanceof IListCompartment)
      {

         IListCompartment pListCompartment = (IListCompartment)pCompartment;

         ETList < ICompartment > pCompartments = pListCompartment.getCompartments();

         if (pCompartments != null)
         {

            Iterator < ICompartment > iterator = pCompartments.iterator();

            while (iterator.hasNext())
            {

               ICompartment curCompartment = iterator.next();

               if (curCompartment != null && curCompartment instanceof IADLabelNameCompartment)
               {

                  retValue = (IADLabelNameCompartment)curCompartment;

               }
            }

         }
      }
      return retValue;
   }

   protected TSLabel getOwnerLabel()
   {
      TSLabel retVal = null;
      IETGraphObjectUI ui = getParent();
      if (ui != null)
      {
         TSEObject owner = ui.getOwner();
         if (owner instanceof TSLabel)
         {
            retVal = (TSLabel)owner;
         }
      }
      return retVal;
   }

   public boolean handleLeftMouseButtonDoubleClick(MouseEvent pEvent)
   {
      boolean eventHandled = false;
      Point mousePos = pEvent.getPoint();

      if (!eventHandled)
      {
         Iterator < ICompartment > iterator = this.getCompartments().iterator();
         while (iterator.hasNext() && !eventHandled)
         {
            eventHandled = iterator.next().handleLeftMouseButtonDoubleClick(pEvent);
         }
      }

      return eventHandled;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#handleLeftMouseButton(java.awt.event.MouseEvent)
    */
   public boolean handleLeftMouseButton(MouseEvent pEvent)
   {
      boolean eventHandled = false;
      Iterator iterator = this.getCompartments().iterator();
      while (iterator.hasNext() && !eventHandled)
      {
         Object curCompartment = iterator.next();
         if (curCompartment instanceof IListCompartment)
         {
            IListCompartment listCompartment = (IListCompartment)curCompartment;
            eventHandled = listCompartment.handleLeftMouseButton(pEvent);
         }
      }

      if (!eventHandled && this.hasSelectedCompartments())
      {
         this.selectAllCompartments(false);
      }

      return eventHandled;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#init()
    */
   public void init() throws ETException
   {

      // DO NOT do anything here. The label manager inits the engine as in C++
      //		super.init();
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onGraphEvent(int)
    */
   public void onGraphEvent(int nKind)
   {
      super.onGraphEvent(nKind);

      switch (nKind)
      {
         case IGraphEventKind.GEK_POST_SELECT :
            {
               this.handlePostSelect();
            }
            break;
            /*				
            			case IGraphEventKind.GEK_POST_MOVE :
            				{
            				}
            				break;
            
            			case IGraphEventKind.GEK_PRE_MOVE :
            				{
            				}
            				break;
            			case IGraphEventKind.GEK_POST_SMARTDRAW_MOVE :
            				{
            				}
            				break;
            			case IGraphEventKind.GEK_PRE_RESIZE :
            				{
            				}
            				break;
            			case IGraphEventKind.GEK_POST_RESIZE :
            				{
            				}
            				break;
            			case IGraphEventKind.GEK_PRE_LAYOUT :
            				{
            				}
            				break;
            			case IGraphEventKind.GEK_POST_LAYOUT :
            				{
            				}
            				break;
            			case IGraphEventKind.GEK_PRE_DELETEGATHERSELECTED :
            				{
            				}
            				break;
            */
      }
   }

   private void handlePostSelect()
   {
	   if(this.getParentETElement()==null)		  
		   return;
	   
	   
      if (!this.getParentETElement().isSelected() && this.hasSelectedCompartments())
      {
         this.selectAllCompartments(false);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#invalidate()
    */
   public long invalidate()
   {
      long rc = super.invalidate();
      ETRect rect = new ETRect(getBoundingRect().getRectangle());
      // The arrow head draws outside the border, include that now.
      rect.inflate(5, 5);
      this.invalidateRect(rect);
      
      this.getDrawingArea().refreshRect(rect);
      return rc;
   }

   
    /////////////
    // Accessible
    /////////////
    
    AccessibleContext accessibleContext;
    
    public AccessibleContext getAccessibleContext() {
	if (accessibleContext == null) {
	    accessibleContext = new AccessibleETLabelDrawEngine();
	} 
	return accessibleContext;
    }


    public class AccessibleETLabelDrawEngine extends AccessibleETDrawEngine {
 
	
	public AccessibleRelationSet getAccessibleRelationSet() {
	    AccessibleRelationSet relSet = new AccessibleRelationSet();
	    AccessibleRelation relation = 
		new AccessibleRelation(AccessibleRelation.LABEL_FOR,
				       getParentDrawEngine());
	    relSet.add(relation);
	    return relSet;
	}
	
	public AccessibleRole getAccessibleRole() {
	    return AccessibleRole.LABEL;
	}

    }


}
