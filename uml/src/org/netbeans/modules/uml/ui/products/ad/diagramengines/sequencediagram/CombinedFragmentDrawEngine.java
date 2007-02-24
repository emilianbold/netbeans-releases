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

package org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;

import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IInteractionOperator;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionConstraint;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.structure.INode;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.DiagramAreaEnumerations;
import org.netbeans.modules.uml.ui.controls.drawingarea.ITopographyChangeAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.TopographyChangeAction;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.ConvertRectToPercent;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADZonesCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.ETInteractionOperandsCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IADInteractionOperandCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IADInteractionOperandsCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.ICornerLabelCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IGateCompartment;
import org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.ICombinedFragmentLabelManager;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETContainerDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.helpers.UserInputBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker.GBK;
import org.netbeans.modules.uml.ui.support.umltsconversions.RectConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartments;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IMouseEvent;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.SmartDragTool;
import org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.SmartDragTool.DR;
import com.tomsawyer.drawing.TSPolygonShape;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import java.awt.GradientPaint;
import com.tomsawyer.editor.graphics.TSEGraphics;


/**
 * @author brettb
 *
 */
public class CombinedFragmentDrawEngine extends ETContainerDrawEngine implements ICombinedFragmentDrawEngine
{
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources() 
	{
                setFillColor("combinedfragmentfill", 211, 227, 244);
                setLightGradientFillColor("combinedfragmentlightgradientfill", 255, 255, 255);
		setBorderColor("combinedfragmentborder", Color.BLACK);
		super.initResources();
	}
	
   // IDrawEngine methods

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
    */
   public String getDrawEngineID()
   { 
      return "CombinedFragmentDrawEngine";
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IGraphObjectManager#onGraphEvent(int)
    */
   public void onGraphEvent(/* IGraphEventKind */ int nKind)
   {
      super.onGraphEvent( nKind );

      switch( nKind )
      {
      case IGraphEventKind.GEK_POST_CREATE:
/* TODO      
         if( !UserInputBlocker.getIsDisabled( GBK.DIAGRAM_MOVEMENT ) &&
             DlgCombinedFragmentWizard.getShowCombinedFragmentWizard() )
         {
            DlgCombinedFragmentWizard wizard( this, AfxGetMainWnd() );
            wizard.DoModal();
         }
*/
         break;

      case IGraphEventKind.GEK_POST_MOVE:
         {
            if ( m_gateCompartment != null )
            {
               m_gateCompartment.updateConnectors( null );
            }
         }
         break;

      case IGraphEventKind.GEK_PRE_DELETE:
         // Fix W5253:  Clean up any left over pieces, i.e. from message-to-self
         validateNodesInRect();
         break;

      default:
         // do nothing
         break;
      }
   }
   
   /**
    * Create the compartments for this node.
    */
   public void createCompartments()
   {
       clearCompartments();

      // Create the compartments in the order to be drawn
      // The ADInteractionOperandsCompartment must be 1st so that
      // the dividers work properly.
      ICompartment compartment = createAndAddCompartment( "ADInteractionOperandsCompartment" );
      if( compartment instanceof IADInteractionOperandsCompartment )
      {
         m_interactionOperandsCompartment = (IADInteractionOperandsCompartment)compartment;
      }
      compartment = createAndAddCompartment( "GateCompartment" );
      if( compartment instanceof IGateCompartment )
      {
         m_gateCompartment = (IGateCompartment)compartment;
      }
      compartment = createAndAddCompartment( "CornerLabelCompartment" );
      if( compartment instanceof ICornerLabelCompartment )
      {
         m_cornerLabelCompartment = (ICornerLabelCompartment)compartment;
      }
   }
   
   /**
    * Initializes our compartments.
    *
    * @param element [in] The presentation element we are representing
    */
   public void initCompartments( IPresentationElement element )
   {
      // We may get here with no compartments.  This happens if we've been created
      // by the user.  If we read from a file then the compartments have been pre-created and
      // we just need to initialize them.
      if (getNumCompartments() == 0)
      {
          createCompartments() ;
      }

      ICombinedFragment combinedFragment = getCombinedFragment( element );
      if ( combinedFragment != null )
      {
         // Make sure there is at least one interaction operand for the combined fragment
         ETList < IInteractionOperand > interactionOperands = getInteractionOperands( combinedFragment );
         if( interactionOperands != null )
         {
            if( interactionOperands.size() <= 0 )
            {
               createInteractionOperand( combinedFragment );
            }
         }
    
	
	// The zone value is calculated dynamically each time.

	// IADZonesCompartment zones = getADInteractionOperandsCompartment();
       IADZonesCompartment zones = getCompartmentByKind(IADInteractionOperandsCompartment.class);
         assert ( zones != null );
         if ( zones != null )
         {
            zones.addModelElement( combinedFragment, -1 );
         }

         // Initialize the corner label


	 // The cornerLabel value is calculated dynamically each time.
         
	 //ICornerLabelCompartment cornerLabel = getCornerLabelCompartment();
	   ICornerLabelCompartment cornerLabel = getCompartmentByKind(ICornerLabelCompartment.class);

		 if( cornerLabel != null )
         {
            cornerLabel.addModelElement( combinedFragment, -1 );
         }

         // Show all the operand constraint labels that have data
         getCombinedFragmentLabelManager().createInitialLabels();
      }
   }

   /**
    * Resize as normal.
    * 
    * @param mouseEvent
    * @param pTool
    * @param bHandled
    */
   public boolean handleLeftMouseButton( MouseEvent mouseEvent )
   {
      boolean bHandled = super.handleLeftMouseButton( mouseEvent );

      if ( !bHandled )
      {
     	// Determine if there are any messages attached to the lifeline
        final IETRect rectMessages = calculateEncompassedMessageRect();
        if ( !rectMessages.isZero() )
        {
           // Create a smart drag tool
           SmartDragTool dragTool = createSmartDragTool(mouseEvent);
           if ( dragTool != null )
           {
              dragTool.setDragRestrictionType(DR.HORIZONTAL_MOVE_ONLY);
              
              IETRect rect;
              double x = getTransform().xToWorld( mouseEvent.getX() );
              double y = getTransform().yToWorld( mouseEvent.getY() );
              final IETRect rectRestricted = new ETRect( x, y, x, y );

              dragTool.setRestrictedArea( rectRestricted );
              
              bHandled = true;
           }
         }
      }
      return bHandled;
   }

   /**
    * Returns the optimum size for an item.  This is used when an item is created from the toolbar.
    */
   public void sizeToContents()
   {
      m_bRelayoutLabels = true;

      // Size but keep the current size if possible
      sizeToContentsWithMin( MIN_NODE_WIDTH,
                             MIN_NODE_HEIGHT,
                             true,   // Since this is a container, only grow the right, and bottom
                             true );
   }

   /**
    * Notifier that the model element has changed.
    *
    * @param targets[in] Information about what has changed
    */
   public long modelElementHasChanged( INotificationTargets targets )
   {
      // Fix W2859:  Make sure the combined fragment is showing the proper number of interaction operands.
      if ( targets != null )
      {
         final int nKind = targets.getKind();
         if( ModelElementChangedKind.MECK_ELEMENTMODIFIED == nKind )
         {
            IElement modelElement = targets.getChangedModelElement();
            if( (modelElement != null) &&
                (modelElement instanceof ICombinedFragment) )
            {
               ICombinedFragment combinedFragment = (ICombinedFragment)modelElement;
               
               IPresentationElement presentationElement = getPresentationElement();
               if( presentationElement != null )
               {
                   initCompartments( presentationElement );
                   sizeToContents() ;
               }
            }
         }
      }
      
      return 1;
   }

   /**
    * Draws each of the individual compartments.
    *
    * @param drawIinfo [in] Information about the draw event (ie the DC, are we printing...)
    */
   public void doDraw( IDrawInfo drawInfo )
   {
      // Get the bounding rectangle of the node.
      IETRect rectBounding = drawInfo.getDeviceBounds();
      setWinClientRectangle( rectBounding );
      

      // draw our frame
      float centerX = (float) rectBounding.getCenterX();
      GDISupport.drawRectangle( drawInfo.getTSEGraphics().getGraphics(), rectBounding.getRectangle(), getBorderColor(), null);
                

      // Draw the compartments in the order they were created in createCompartments()
      dispatchDrawToCompartments( drawInfo, rectBounding );

      // This call must come after the draw call, because
      // the shape is determined within the draw of the compartments.
      
      //Jyothi:
      TSEGraphics graphics = drawInfo.getTSEGraphics();
      graphics.getGraphWindow().getGraph().setFireEvents(false);
      graphics.getGraphWindow().getGraphManager().getEventManager().setCoalescingPermanentlyDisabled(true);
         
         
      setCombinedFragmentShape();
      
      graphics.getGraphWindow().getGraph().setFireEvents(true);
      graphics.getGraphWindow().getGraphManager().getEventManager().setCoalescingPermanentlyDisabled(false); 
      //Jyothi : end

      if( m_bRelayoutLabels )
      {
         getLabelManager().relayoutLabels() ;

         m_bRelayoutLabels = false;
      }
      // Give the container a chance to draw
//      super.doDraw(drawInfo);
   }

   /**
    * Is this draw engine valid for the element it is representing?
    *
    * @param bIsValid[in] true if this draw engine can correctly represent the attached model element
    */
   public boolean isDrawEngineValidForModelElement()
   {
      boolean bIsValid = false;

      String currentMetaType = getMetaTypeOfElement();
      if (currentMetaType.equals("CombinedFragment"))
      {
         bIsValid = true;
      }
      
      return bIsValid;
   }


   // ICombinedFragmentDrawEngine methods
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ICombinedFragmentDrawEngine#setOperator(org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IInteractionOperator)
    */
   public void setOperator( int newVal )
   {
      ICombinedFragment combinedFragment = getCombinedFragment();
      if( combinedFragment != null )
      {
         combinedFragment.setOperator( newVal );

         // Fix 1795:  Make sure the interaction operand labels are relayed out
         m_bRelayoutLabels = true;
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ICombinedFragmentDrawEngine#editConstraint(org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand)
    */
   public void editConstraint( IInteractionOperand operand )
   {
      IInteractionConstraint interactionConstraint = operand.getGuard();
      if( interactionConstraint == null )
      {
         interactionConstraint = operand.createGuard();
      }

      IETLabel etLabel = getCombinedFragmentLabelManager().getLabelFromOperand( operand );
      if( etLabel != null )
      {
         ILabelPresentation labelPresentation = TypeConversions.getLabelPresentation( etLabel );
         if( labelPresentation != null )
         {
            // A new label will be deleted if it is not edited.
            labelPresentation.setDeleteIfNotEdited( true );

            IDrawingAreaControl control = getDrawingArea();
            if( control != null )
            {
               // Fix W1778:  Need to put label into edit mode
               control.postEditLabel( labelPresentation );
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ICombinedFragmentDrawEngine#getEdgesInteractionOperand(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge)
    */
   public ETPairT < IInteractionOperand, ICompartment > getEdgesInteractionOperand( IETEdge edge )
   {
      IInteractionOperand operand = null;
      ICompartment edgesCompartment = null;

      // Get the bounding rectangle for the edge
      IETRect rectEdge = RectConversions.newETRect( edge.getBounds() );

      // Fix W2710:  Need to make sure that mesages connected to the combined fragment
      // from the outside are not concidered to be inside the combined fragment
      rectEdge.setLeft( rectEdge.getLeft()+1 );
      rectEdge.setRight( rectEdge.getRight()-1 );

      ETList< ICompartment > compartments = getInteractionOperandCompartments();
      if( compartments != null )
      {
         for (Iterator iter = compartments.iterator(); iter.hasNext();)
         {
            ICompartment compartment = (ICompartment)iter.next();
            IETRect rectCompartment = TypeConversions.getLogicalBoundingRect( compartment );
   
            // Fix W10507:  The message must be completely contained by the compartment
            //              to be owned by the interaction operand.
            if( rectCompartment.contains( rectEdge ) &&
                (compartment instanceof IADInteractionOperandCompartment) )
            {
               IADInteractionOperandCompartment operandCompartment = (IADInteractionOperandCompartment)compartment;
               if( operandCompartment != null )
               {
                  operand = operandCompartment.getInteractionOperand();
                  break;
               }
            }
         }
      }
      
      return new ETPairT < IInteractionOperand, ICompartment >( operand, edgesCompartment );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ICombinedFragmentDrawEngine#expandToIncludeInteractionOperands(boolean)
    */
   public void expandToIncludeInteractionOperands( boolean bAllowToShrink )
   {
      // Make sure we have a presentation element, and a diagram for posting the delay actions
      IPresentationElement presentationElement = getPresentationElement();
      IDiagram diagram = getDiagram();

      if( (presentationElement != null) &&
          (diagram != null) )
      {
         final IETRect rectCombinedFragment = calculateEncompassingRect( bAllowToShrink );

         // Move, and resize based on the rectangle just calculated
         if( ! ETInteractionOperandsCompartment.isDefaultRect( rectCombinedFragment ) )
         {
            IETRect rectBounding = getLogicalBoundingRect();

            if( !rectBounding.equals( rectCombinedFragment ) )
            {
               final Point ptCenter = rectCombinedFragment.getCenterPoint();

               // Post a resize and moveto action because we need to move after other moves.
               ITopographyChangeAction action = new TopographyChangeAction();
               assert ( action != null );
               if ( action != null )
               {
                   ICornerLabelCompartment labelComp = getCompartmentByKind(ICornerLabelCompartment.class);
                   int halfH = 10;
                   
                   action.setKind( DiagramAreaEnumerations.TAK_RESIZETO );
                   action.setX( ptCenter.x );
                   action.setY( ptCenter.y + halfH );
                   action.setWidth( (int)rectCombinedFragment.getWidth() );
                   action.setHeight( (rectCombinedFragment.getTop() - rectCombinedFragment.getBottom()) + halfH );
                   action.setPresentationElement( presentationElement );

                   diagram.postDelayedAction( action );
               }
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ICombinedFragmentDrawEngine#getSelectAllCoveredItems()
    */
   public int getSelectAllCoveredItems()
   {
      int iItemsSelectedCnt = 0;

      // Loop through all the compartments
      final int iCompartmentCnt = getNumCompartments();
      for ( int iCompartmentIndx = 0; iCompartmentIndx < iCompartmentCnt; iCompartmentIndx++ )
      {
         ICompartment compartment = getCompartment( iCompartmentIndx );
         if( compartment instanceof IADInteractionOperandCompartment )
         {
            IADInteractionOperandCompartment operandCompartment = (IADInteractionOperandCompartment)compartment;
            iItemsSelectedCnt += operandCompartment.selectAllCoveredItems();
         }
      }
      
      return iItemsSelectedCnt;
   }
   
   
   // protected member operations

   /**
    * Get the list combined fragment associated with the presentation element.
    *
    * @param pe The presentation element that may represent a combined fragment
    * @return The combined fragment model element
    */
   protected static ICombinedFragment getCombinedFragment( IPresentationElement pe )
   {
      ICombinedFragment combinedFragment = null;
      
      if( pe != null )
      {
         IElement element = pe.getFirstSubject();
         if ( element instanceof ICombinedFragment )
         {
             combinedFragment = (ICombinedFragment)element;
         }
      }
      
      return combinedFragment;
   }

   /**
    * Get the combined fragment from the parent product element.
    *
    * @return The combined fragment model element
    */
   protected ICombinedFragment getCombinedFragment()
   {
      ICombinedFragment combinedFragment = null;

      IElement element = TypeConversions.getElement( this );
      if( element instanceof ICombinedFragment )
      {
         combinedFragment = (ICombinedFragment)element;
      }
      
      return combinedFragment;
   }

   /**
    * Get the list of interaction operands.
    *
    * @param pe
    * @param ppInteractionOperands[out]
    */
   protected ETList< IInteractionOperand > getInteractionOperands( ICombinedFragment combinedFragment )
   {
      ETList< IInteractionOperand > interactionOperands = null;
      
      if( combinedFragment != null )
      {
         interactionOperands = combinedFragment.getOperands();
      }
      
      return interactionOperands;
   }

   /**
    * Get the compartments representing the interaction operands
    */
   protected ETList< ICompartment > getInteractionOperandCompartments()
   {
      ETList< ICompartment > compartments = null;


		 

// The below code has been modified to get the value of the opearndsCompartment dynamically.

  
// IADInteractionOperandsCompartment operandsCompartment = getADInteractionOperandsCompartment();
   IADInteractionOperandsCompartment operandsCompartment = getCompartmentByKind(IADInteractionOperandCompartment.class);
      if( operandsCompartment != null )
      {
         compartments = operandsCompartment.getCompartments();
      }
      
      return compartments;
   }

   /**
    * Create a new interaction operand for the combined fragment.
    *
    * @param pe[in]
    * @param ppOperand[out]
    */
   protected void createInteractionOperand( ICombinedFragment combinedFragment )
   {
      if( combinedFragment != null )
      {
         combinedFragment.createOperand();
      }
   }

   /**
    * Calculates the rectangle that encompasses all the interaction operands.
    *
    * @param bAllowToShrink
    */
   protected IETRect calculateEncompassingRect( boolean bAllowToShrink )
   {
      // Using this default size allows the calculated rectangle to shrink
      IETRect rectCombinedFragment = (IETRect)ETInteractionOperandsCompartment.m_rectDefault.clone();


	  
	// The below code has been modified to get the value of the operandsCompartment dynamically. 


	//IADInteractionOperandsCompartment operandsCompartment = getADInteractionOperandsCompartment();
	  IADInteractionOperandsCompartment operandsCompartment = getCompartmentByKind(IADInteractionOperandsCompartment.class);
      if( operandsCompartment != null )   
      {
         IETRect rect = operandsCompartment.calculateEncompassingRect( bAllowToShrink );
         if( rect != null )
         {
            rectCombinedFragment = rect;

            // Find the lifeline nodes under the combined fragment, and
            // increase the width of our rectangle to include the lifelines
            IDrawingAreaControl control = getDrawingArea();
            if( control != null )
            {
               ETList< IPresentationElement > presentationElements =
                  control.getAllNodesViaRect( rectCombinedFragment, true );
               if( presentationElements != null )
               {
                  for (Iterator iter = presentationElements.iterator(); iter.hasNext();)
                  {
                     IPresentationElement pe = (IPresentationElement)iter.next();
                     assert ( pe != null );

                     if( shouldContain( pe ) )
                     {
                        IETRect rectBounding = TypeConversions.getLogicalBoundingRect( pe );

                        // When creating the interaction operand compartment via automation,
                        // resize the combined fragment by a "default" amount.
                        rectCombinedFragment.setLeft(  Math.min( rectCombinedFragment.getLeft(),  rectBounding.getLeft() - 50 ));
                        rectCombinedFragment.setRight( Math.max( rectCombinedFragment.getRight(), rectBounding.getRight() + 20 ));
                     }
                  }
               }
            }
         }
      }

      return rectCombinedFragment;
   }

   /**
    * Returns true if this combined fragment should contain the input presentation element
    */
   protected boolean shouldContain( IPresentationElement pe )
   {
      boolean bShouldContain = false;

      if( pe != null )
      {
         IElement element = pe.getFirstSubject();
         if( element != null )
         {
            String strType = element.getElementType( );
            if( strType.equals("Lifeline") )
            {
               bShouldContain = true;
            }
            else if( strType.equals("CombinedFragment") )
            {
               String strCFXMIID = element.getXMIID();
               if( strCFXMIID.length() > 0 )
               {
                  IElement thisElement = getFirstModelElement();
                  if( thisElement != null )
                  {
                     Node thisNode = thisElement.getNode();
                     if( thisNode != null )
                     {
                        try   // using descendant in the XPath query can sometimes throw
                        {
                           final String strQuery = "descendant::*[@xmi.id=\""  +  strCFXMIID  + "\"]";
                           Node cfNode = thisNode.selectSingleNode( strQuery );
                           bShouldContain = (cfNode != null);
                        }
                        catch( Exception e )
                        {
                        }
                     }
                  }
               }
            }
         }
      }

      return bShouldContain;
   }

   /**
    * Tell Tom Sawyer the active shape for node selection.
    * Note:  In C++ TS uses mils (0-1000 from center), in java TS uses percent (0-100 from center)
    */
   void setCombinedFragmentShape()
   {
      // @todo Move this code to the resize area

      TSENode tseNode = getOwnerNode();
      if ( tseNode != null )
      {
         // Here's our list of shapes that we're sending to tomsawyer
         /* jyothi .. There is no addPoint in TS6.0; 
          TSPolygonShape shape = new TSPolygonShape();
          
         // Loop around the outside of the drawing area
         // We must start and end on the same point.
         // In this case, the upper left corner is expected by the compartment(s)
         shape.addPoint( 0, 100 );
         shape.addPoint( 100, 100 );
         shape.addPoint( 100, 0 );
         shape.addPoint( 0, 0 );
         shape.addPoint( 0, 100 );
         */
          List ptList = new Vector();
          
          //outside boundary
          ptList.add(new TSConstPoint(0, 0));
          ptList.add(new TSConstPoint(100, 0));
          ptList.add(new TSConstPoint(100, 100));
          ptList.add(new TSConstPoint(0, 100));
          
          //make turn to inside boundary
          ptList.add(new TSConstPoint(0, 6));
          ptList.add(new TSConstPoint(5, 6));
          
          //inside boundary
          ptList.add(new TSConstPoint(5, 95));
          ptList.add(new TSConstPoint(95, 95));
          ptList.add(new TSConstPoint(95, 5));
          ptList.add(new TSConstPoint(0, 5));
          
          //close up the shape
          ptList.add(new TSConstPoint(0, 0));
         
         IETRect rectInner = getWinScaledOwnerRect();

         // Set up the coordinate conversion parameters
         ConvertRectToPercent converter = new ConvertRectToPercent( this );
         Point point = null;
         if( converter != null )
         {
            // Add the compartment information
            ETList< ICompartment > compartments = getInteractionOperandCompartments();
            if( compartments != null )
            {
               for (Iterator iter = compartments.iterator(); iter.hasNext();)
               {
                  ICompartment compartment = (ICompartment)iter.next();
               
                  // Get the shape of the compartment
                  // The compartment should assume that the point before its list of points
                  // will be in the upper left corner of its bounding rect
                  ETList< IETPoint > pointList = compartment.getCompartmentShape();
                  assert ( pointList != null );
   
                  // Process the list of points
                  if ( pointList != null )
                  {
                     for (Iterator iterPoints = pointList.iterator(); iterPoints.hasNext();)
                     {
                        IETPoint ptCompartment = (IETPoint)iterPoints.next();
                     
                        point = converter.ConvertToPercent( ptCompartment.asPoint() );
                        // shape.addPoint( point.x, point.y ); //jyothi
                         ptList.add(new TSConstPoint(point.x, point.y));
                     }
                  }
               }
            }
         }

         TSPolygonShape shape = new TSPolygonShape(ptList);  //jyothi: moved it from above to accomodate the point.x, point.y..
         
         
         
         // Give TS our new shape list
         tseNode.setShape( shape );
         
         
      }
   }

   /**
    * Returns the metatype of the manager we should use.  This implementation 
    * always returns an empty string.
    *
    * @return The metatype in essentialconfig.etc that defines the label manager
    * @param managerType The type of manager.
    */
   public String getManagerMetaType(int nManagerKind)
   {
      return (MK_LABELMANAGER == nManagerKind) ? "CombinedFragmentLabelManager" : "";
   }

   /**
    * Calculates the rectangle that minimally encompasses all the graphically contained messages
    */
   protected IETRect calculateEncompassedMessageRect()
   {
      boolean bNoMessagesFound = true;
      IETRect rectMessages = new ETRect( Long.MAX_VALUE, Long.MIN_VALUE, Long.MIN_VALUE, Long.MAX_VALUE );

      IDrawingAreaControl control = getDrawingArea();
      if( control != null )
      {
         IETRect etRect = getLogicalBoundingRect();
         if( etRect != null )
         {
            ETList< IPresentationElement > pes = control.getAllEdgesViaRect( etRect, false );
            if( pes != null )
            {
               for (Iterator iter = pes.iterator(); iter.hasNext();)
               {
                  IPresentationElement pe = (IPresentationElement)iter.next();
                  
                  IElement element = TypeConversions.getElement( pe );
                  if( element instanceof IMessage )
                  {
                     final IETRect rectMessage = TypeConversions.getLogicalBoundingRect( pe );

                     // Update the encompassing rectangle
                     if( !rectMessage.isZero() )
                     {
                        rectMessages.setLeft(   Math.min( rectMessages.getLeft(),   rectMessage.getLeft() ));
                        rectMessages.setRight(  Math.max( rectMessages.getRight(),  rectMessage.getRight() ));
                        rectMessages.setTop(    Math.max( rectMessages.getTop(),    rectMessage.getTop() ));
                        rectMessages.setBottom( Math.min( rectMessages.getBottom(), rectMessage.getBottom() ));

                        bNoMessagesFound = false;
                     }
                  }
               }
            }
         }
      }

      if( bNoMessagesFound )
      {
         rectMessages.setRectEmpty();
      }

      return rectMessages;
   }

   /**
    * Validate all the nodes contained in the rectangle
    */
   protected void validateNodesInRect()
   {
      // Find the lifeline nodes under the combined fragment, and
      // tell them to validate
      final IETRect rectCombinedFragment = getLogicalBoundingRect();
      ETList< IPresentationElement > presentationElements =
         getPresentationElementsInRect( rectCombinedFragment );
      if( (presentationElements != null) &&
          (presentationElements.size() > 0) )
      {
         IDrawingAreaControl control = getDrawingArea();
         if( control != null )
         {
/* TODO            
            control.postSimplePresentationDelayedAction2( presentationElements,
                                                          SPAK_VALIDATENODE);
*/
         }
      }
   }

   /**
    * Retrieves all the presentation elements touching the input rectangle
    */
   protected ETList< IPresentationElement > getPresentationElementsInRect( IETRect rect )
   {
      ETList< IPresentationElement > pes = null;

      // Find the lifeline nodes under the rectangle
      IDrawingAreaControl control = getDrawingArea();
      if( control != null )
      {
         pes = control.getAllNodesViaRect( rect, true );
      }
      
      return pes;
   }

   /**
    * Get the label manager for this draw engine
    */
   protected ICombinedFragmentLabelManager getCombinedFragmentLabelManager()
   {
      ICombinedFragmentLabelManager combinedFragmentLM = null;
      {
         ILabelManager labelManager = getLabelManager();
         if( labelManager instanceof ICombinedFragmentLabelManager )
         {
            combinedFragmentLM = (ICombinedFragmentLabelManager)labelManager;
         }
      }

      // TODO_THROW if( !cpCombinedFragmentLM )    E_POINTER ;
      return combinedFragmentLM;
   }


   
//This getter method is no longer needed as we are getting the compartments value dynamically each time.

/*
   protected IADInteractionOperandsCompartment getADInteractionOperandsCompartment()
   {
      return m_interactionOperandsCompartment;
}*/
   
   protected IGateCompartment getGateCompartment()
   {
      return m_gateCompartment;
   }
   

//  This getter method is no longer needed as we are getting the cornerLabel value dynamically each time.

/*   protected ICornerLabelCompartment getCornerLabelCompartment()
   {
      return m_cornerLabelCompartment;
   }*/

   private IADInteractionOperandsCompartment m_interactionOperandsCompartment = null;
   private IGateCompartment m_gateCompartment = null;
   private ICornerLabelCompartment m_cornerLabelCompartment = null;
   
   // TODO private CollapsibleCompartmentDividers m_compartmentDividers;
   private boolean m_bRelayoutLabels; // indicates that the labels for the InteractionConstraints need to be layed out
}



