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

import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.event.ActionEvent;

//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.ui.TSERectangularUI;
import com.tomsawyer.editor.TSEColor;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADContainerDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETArrowHead;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/*
 * 
 * @author Kevinm

 */
public class ETNestedLinkDrawEngine extends ETEdgeDrawEngine
{
   public String getElementType()
   {
      String type = super.getElementType();
      if (type == null)
      {
         type = new String("NestedLink");
      }
      return type;
   }


	private void erasePathSegment(IDrawInfo pInfo, IETArrowHead arrowHead, ETPairT < TSConstPoint, TSConstPoint > segment, TSENode node)
	{
		if (pInfo == null || arrowHead == null || segment == null || node == null)
			return;

		// We need to erase from the centerpoint of the arrow to the clipping point on the node
		// to remove the little line segment, becuase we have circle.

		Shape arrowShape = arrowHead.getShape(pInfo, segment.getParamOne(), segment.getParamTwo());

		if (arrowShape != null)
		{
			TSEGraphics dc = pInfo.getTSEGraphics();
			Stroke prevPen = dc.getStroke();
			// Widen the stroke becuase it will not cover it overwise, it tends to bleed through.
			dc.setStroke(this.getLineStroke(DrawEngineLineKindEnum.DELK_SOLID, getPenWidth() + 2));
			Rectangle arrowBounds = arrowShape.getBounds();

			// Get the line segment to erase.
			int centerX = arrowBounds.x + arrowBounds.width / 2;
			int centerY = arrowBounds.y + arrowBounds.height / 2;
			TSTransform transform = dc.getTSTransform();
			int lineToX = transform.xToDevice(segment.getParamTwo().getX());
			int lineToY = transform.yToDevice(segment.getParamTwo().getY());

			Color prevColor = pInfo.getTSEGraphics().getColor();
			dc.setColor(pInfo.getGraphDisplay().getBackground());
			dc.drawLine(centerX, centerY, lineToX, lineToY);
			dc.setStroke(prevPen);


			TSERectangularUI ui = node.getUI() instanceof TSERectangularUI ? (TSERectangularUI)node.getUI() : null;
 
			IDrawEngine nodeEngine = TypeConversions.getDrawEngine(node);
			if (nodeEngine instanceof IADContainerDrawEngine)
			{            
				// We need to redraw the arrow head, when its a container to respect its stacking order.
				arrowHead.draw(pInfo, segment.getParamOne(), segment.getParamTwo(), this.getStateColor());
			}

			// Redraw the point on the boarder.
			if (ui != null)
			{								
				dc.setColor(ui.getBorderColor());
				dc.drawLine(segment.getParamTwo(),segment.getParamTwo());
			}
			
			dc.setColor(prevColor);

		}
	}

   protected void drawPathDigraph(IDrawInfo pInfo, IETArrowHead pFromArrow, IETArrowHead pToArrow)
   {
      boolean isFromCirclePlus = pFromArrow != null && pFromArrow.getKind() == DrawEngineArrowheadKindEnum.DEAK_CIRCLE_WITH_PLUS ? true : false;
      boolean isToCirclePlus = pToArrow != null && pToArrow.getKind() == DrawEngineArrowheadKindEnum.DEAK_CIRCLE_WITH_PLUS ? true : false;

      super.drawPathDigraph(pInfo, pFromArrow, pToArrow);

      if (isToCirclePlus)
      {
         erasePathSegment(pInfo, pToArrow, this.getToLineSegment(), getTargetNode());
      }
      else if (isFromCirclePlus)
      {
         erasePathSegment(pInfo, pFromArrow, this.getFromLineSegment(), getSourceNode());
      }
   }

   protected int getStartArrowKind()
   {
      return displayArrowAtSource() ? DrawEngineArrowheadKindEnum.DEAK_CIRCLE_WITH_PLUS : DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
   }

   protected int getEndArrowKind()
   {
      return displayArrowAtSource() ? DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD : DrawEngineArrowheadKindEnum.DEAK_CIRCLE_WITH_PLUS;
   }

   public void initCompartments()
   {

   }

   public boolean displayArrowAtTarget()
   {
      return !displayArrowAtSource();
   }

   public boolean displayArrowAtSource()
   {
      boolean bPlusOnSource = false;
      boolean bPlusOnTarget = false;
      try
      {
         IEdgePresentation thisEdgePresentation = this.getIEdgePresentation();
         if (thisEdgePresentation != null)
         {
            IElement pSourceModelElement = thisEdgePresentation.getEdgeFromElement(false);
            IElement pTargetModelElement = thisEdgePresentation.getEdgeToElement(false);

            INamedElement pSourceNamedElement = pSourceModelElement instanceof INamedElement ? (INamedElement)pSourceModelElement : null;
            INamedElement pTargetNamedElement = pTargetModelElement instanceof INamedElement ? (INamedElement)pTargetModelElement : null;
            if (pSourceNamedElement != null && pTargetNamedElement != null)
            {
               // One should be a namespace of the other
               INamespace pSourceNamespace = pSourceNamedElement.getNamespace();
               INamespace pTargetNamespace = pTargetNamedElement.getNamespace();
               boolean bIsSame = false;

               if (pSourceNamespace != null && pTargetNamespace != null)
               {
                  bIsSame = pSourceNamespace.isSame(pTargetModelElement);
                  if (bIsSame)
                  {
                     bPlusOnTarget = true;
                  }
                  else
                  {
                     bIsSame = pTargetNamespace.isSame(pSourceModelElement);
                     bPlusOnSource = true;
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return true;
      }
      return bPlusOnSource;
   }

   public void onContextMenu(IMenuManager manager)
   {
      // Add the stereotype label pullright
      addStandardLabelsToPullright(StandardLabelKind.SLK_STEREOTYPE, manager);
      super.onContextMenu(manager);
   }
   /**
    * Notifier that the model element has changed, if available the changed IFeature is passed along.
    *
    * @param targets [in] Information about what has changed.
    */
   public long modelElementHasChanged(INotificationTargets targets) 
   {
      if( null == targets ) throw new IllegalArgumentException();

      if ( targets != null )
      {
         int nKind = targets.getKind();
         if (nKind == ModelElementChangedKind.MECK_ELEMENTADDEDTONAMESPACE)
         {
            IEdgePresentation thisEdgePresentation = getEdgePresentationElement();
            if ( thisEdgePresentation != null )
            {
               boolean bIsValid = thisEdgePresentation.validateLinkEnds();
               if ( !bIsValid )
               {
                  // Post a delete presentation element
                  IDrawingAreaControl control = getDrawingArea();
                  if( control != null )
                  {
                     control.postDeletePresentationElement( thisEdgePresentation );
                  }
               }
            }
         }
      }
         
      return 0;
   }

   public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
   {
      boolean retVal = handleStandardLabelSensitivityAndCheck(id, pClass);
      if (!retVal)
      {
         super.setSensitivityAndCheck(id, pClass);
      }
      return retVal;
   }

   public boolean onHandleButton(ActionEvent e, String id)
   {
      boolean handled = handleStandardLabelSelection(e, id);
      if (!handled)
      {
         handled = super.onHandleButton(e, id);
      }
      return handled;
   }

   public String getDrawEngineID()
   {
      return "NestedLinkDrawEngine";
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
    */
   public void initResources()
   {
      this.setLineColor("nestedlinkcolor", Color.BLACK);
      super.initResources();
   }

   /**
    * When a presentation element is selected and VK_DELETE is selected, the user is
    * asked if the data model should be affected as well.  For nested links we want to remove the
    * namespace relationship.
    */
   public void affectModelElementDeletion()
   {
      try
      {
         INamedElement pOwningNamespace = null;
         INamedElement pChildElement = null;
         IETGraphObject pOwningNamespaceETElement = null;
         IETGraphObject pChildETElement = null;

         IEdgePresentation thisEdgePresentation = this.getEdgePresentationElement();

         if (thisEdgePresentation != null)
         {
            IElement pSourceModelElement = null;
            IElement pTargetModelElement = null;
            IETGraphObject pSourceETElement = null;
            IETGraphObject pTargetETElement = null;

            // Get the ends of the link and break the namespace relationship
            ETPairT < IElement, IElement > result1 = thisEdgePresentation.getEdgeFromAndToElement(false);
            pSourceModelElement = result1.getParamOne();
            pTargetModelElement = result1.getParamTwo();

            ETPairT < IETGraphObject, IETGraphObject > result2 = thisEdgePresentation.getEdgeFromAndToNode();
            pSourceETElement = result2.getParamOne();
            pTargetETElement = result2.getParamTwo();

            INamedElement pSourceNamedElement = (pSourceModelElement instanceof INamedElement) ? (INamedElement)pSourceModelElement : null;
            INamedElement pTargetNamedElement = (pTargetModelElement instanceof INamedElement) ? (INamedElement)pTargetModelElement : null;

            if (pSourceNamedElement != null && pTargetNamedElement != null)
            {
               // One should be a namespace of the other
               INamespace pSourceNamespace = null;
               INamespace pTargetNamespace = null;
               boolean bIsSame = false;

               pSourceNamespace = pSourceNamedElement.getNamespace();
               pTargetNamespace = pTargetNamedElement.getNamespace();

               if (pSourceNamespace != null || pTargetNamespace != null)
               {
                  if (pSourceNamespace != null)
                  {
                     bIsSame = pSourceNamespace.isSame(pTargetModelElement);
                  }
                  if (bIsSame)
                  {
                     pOwningNamespace = (INamedElement)pTargetModelElement;
                     pChildElement = (INamedElement)pSourceModelElement;
                     pOwningNamespaceETElement = pTargetETElement;
                     pChildETElement = pSourceETElement;
                  }
                  else if (pTargetNamespace != null)
                  {
                     bIsSame = pTargetNamespace.isSame(pSourceModelElement);

                     if (bIsSame)
                     {
                        pOwningNamespace = (INamedElement)pSourceModelElement;
                        pChildElement = (INamedElement)pTargetModelElement;
                        pOwningNamespaceETElement = pSourceETElement;
                        pChildETElement = pTargetETElement;
                     }
                  }
               }
            }
         }

         // If we've got a child and parent break the namespace relationship and restore with
         // 1.  If it's sitting on a namespace container then us that
         // 2.  The namespace of the diagram
         if (pOwningNamespace != null && pChildElement != null && pChildETElement != null)
         {
            boolean bChangedNamespace = false;

            // See if we've got a container
            IPresentationElement pPresentationElement = pChildETElement.getPresentationElement();

            INodePresentation pNodePE = (pPresentationElement instanceof INodePresentation) ? (INodePresentation)pPresentationElement : null;

            if (pNodePE != null)
            {
               INodePresentation pGraphicalContainer = pNodePE.getGraphicalContainer();

               if (pGraphicalContainer != null)
               {
                  // Get the draw engine to make sure it's a namespace container
                  IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(pGraphicalContainer);

                  IADContainerDrawEngine pContainerDrawEngine = (pDrawEngine instanceof IADContainerDrawEngine) ? (IADContainerDrawEngine)pDrawEngine : null;

                  if (pContainerDrawEngine != null)
                  {
                     INamespace pBeforeNamespace = pChildElement.getNamespace();
                     pContainerDrawEngine.beginContainment(null, pPresentationElement);

                     INamespace pAfterNamespace = pChildElement.getNamespace();

                     // See if they've changed
                     boolean bIsSame = false;
                     if (pBeforeNamespace != null && pAfterNamespace != null)
                     {
                        bIsSame = pBeforeNamespace.isSame(pAfterNamespace);

                        if (!bIsSame)
                        {
                           bChangedNamespace = true;
                        }
                     }
                  }
               }
            }

            if (!bChangedNamespace)
            {
               // Here's where we set the namespace to that of the diagram
               IDiagram pDiagram = this.getDiagram();
               INamespace pDiagramNamespace = null;
               if (pDiagram != null)
               {
                  pDiagramNamespace = pDiagram.getNamespace();

                  if (pDiagramNamespace != null)
                  {
                     pChildElement.setNamespace(pDiagramNamespace);
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

}
