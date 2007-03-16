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


package org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram;

import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSENode;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.IAtomicFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IEventOccurrence;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.CollectionTranslator;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETZonesCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IETZoneDividers;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ILifelineDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.ICombinedFragmentLabelManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

/**
 * @author brettb
 *
 */
public class ETInteractionOperandsCompartment extends ETZonesCompartment implements IADInteractionOperandsCompartment
{

   /**
    * 
    */
   public ETInteractionOperandsCompartment()
   {
      super();
      
      m_zonedividers.setLineStyle( DrawEngineLineKindEnum.DELK_DOT );
      m_zonedividers.setOrientation( IETZoneDividers.DMO_HORIZONTAL );
      m_minNumCompartments = 1;
      m_strCompartmentID = "ADInteractionOperandCompartment";
   }
   
   
   // ICompartment

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getCompartmentID()
    */
   public String getCompartmentID()
   {
      return "ADInteractionOperandsCompartment";
   }
   
   
   // ISimpleListCompartment
   /**
    * Remove this compartment to this list, optionally deletes its model element.
   */
   public void removeCompartmentAt( int lIndex, boolean bDeleteElement )
   {
      if( m_engine != null )
      {
         // Make sure any associated labels are also deleted
         ILabelManager labelManager = m_engine.getLabelManager();
         if( labelManager instanceof ICombinedFragmentLabelManager )
         {
            ICombinedFragmentLabelManager combinedFragmentLM = (ICombinedFragmentLabelManager)labelManager;
            assert ( combinedFragmentLM != null );

            ICompartment compartment = getCompartment( lIndex );
            if( compartment != null )
            {
               IElement element = compartment.getModelElement();
               if( element instanceof IInteractionOperand )
               {
                  IInteractionOperand interactionOperand = (IInteractionOperand)element;
                  combinedFragmentLM.discardOperandsLabel( interactionOperand );
               }
            }
         }

          super.removeCompartmentAt( lIndex, bDeleteElement );
      }
   }


   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.ETZonesCompartment#createZonesButtons(org.netbeans.modules.uml.ui.products.ad.application.IMenuManager)
    */
   public void createZonesButtons(IMenuManager manager)
   {
      addInteractionOperandsButtons( manager );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.ETZonesCompartment#createNewElement()
    */
   protected IElement createNewElement() throws RuntimeException
   {
      return createNewInteractionOperand();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IADInteractionOperandsCompartment#calculateEncompassingRect(boolean)
    */
   public IETRect calculateEncompassingRect(boolean bAllowToShrink)
   {
      IETRect rect = null;

      // Using this default size allows the calculated rectangle to shrink
      // J2099 - Rectangle removed here because width & height made for too many messy rollover issues
      int operandLeft = m_rectDefault.getLeft();
      int operandRight = m_rectDefault.getRight();
      int operandTop = m_rectDefault.getTop();
      int operandBottom = m_rectDefault.getBottom();
      
      int iCnt = 0;

      ETList< ICompartment > compartments = getCompartments();
      if( compartments != null )
      {
         if( !bAllowToShrink )
         {
            // Use the current bounding rectangle as the starting point
            IETRect bounds = getLogicalBoundingRect();
            operandLeft = bounds.getLeft();
            operandRight = bounds.getRight();
            operandTop = bounds.getTop();
            operandBottom = bounds.getBottom();
         }

         int iPreviousLogicalBottom = Integer.MIN_VALUE;

         iCnt = compartments.size();
         
         int iIndex = 0;
         for (Iterator iter = compartments.iterator(); iter.hasNext(); iIndex++)
         {
            ICompartment compartment = (ICompartment)iter.next();
            assert ( compartment != null );
            
            IETRect rectInteractionOperand = (IETRect)m_rectDefault.clone();

            if( compartment instanceof IADInteractionOperandCompartment )
            {
               IADInteractionOperandCompartment operandCompartment = (IADInteractionOperandCompartment)compartment;

               rectInteractionOperand = operandCompartment.getMinimumEncompassingRect( iPreviousLogicalBottom );

               // Fix W6461:  Give the interaction operand's rectangle some size
               if( rectInteractionOperand.isZero() &&
                   ( m_rectDefault.getLeft() != operandLeft ||
                     m_rectDefault.getTop() != operandTop ||
                     m_rectDefault.getRight() != operandRight ||
                     m_rectDefault.getBottom() != operandBottom) )
               {
                  // Fix W10735:  The interaction operands rect should never shrink here.
                  if( !bAllowToShrink )
                  {
                     rectInteractionOperand = TypeConversions.getLogicalBoundingRect( operandCompartment );
                  }

                  if( rectInteractionOperand.isZero() )
                  {
                     if( Integer.MIN_VALUE == iPreviousLogicalBottom )
                     {
                        rectInteractionOperand.setTop(    operandTop );
                        rectInteractionOperand.setBottom( operandTop - 20 );
                     }
                     else
                     {
                        rectInteractionOperand.setTop(    iPreviousLogicalBottom );
                        rectInteractionOperand.setBottom( iPreviousLogicalBottom - 20 );
                     }
                  }

                  rectInteractionOperand.setLeft(  operandLeft );
                  rectInteractionOperand.setRight( operandRight );
               }

               // Update the encompassing rectangle
               if( !rectInteractionOperand.isZero() )
               {
                  operandLeft   = Math.min( operandLeft,   rectInteractionOperand.getLeft() );
                  operandRight  = Math.max( operandRight,  rectInteractionOperand.getRight() );
                  operandTop    = Math.max( operandTop,    rectInteractionOperand.getTop() );
                  operandBottom = Math.min( operandBottom, rectInteractionOperand.getBottom() );
               }

               // update the divider locations
               if( (iPreviousLogicalBottom != Integer.MIN_VALUE) &&
                   (operandTop != m_rectDefault.getTop()) )
               {
                  final int iOffset = operandTop -
                     (iPreviousLogicalBottom + rectInteractionOperand.getTop()) / 2;
                  m_zonedividers.setDividerOffset( iIndex-1, iOffset );
               }
            }

            if( rectInteractionOperand.getBottom() != m_rectDefault.getBottom() )
            {
               iPreviousLogicalBottom = rectInteractionOperand.getBottom();
            }
         }
      }

      // No messages are contained by any of the interaction operands
      if( operandTop == m_rectDefault.getTop() &&
          operandBottom == m_rectDefault.getBottom() &&
          operandLeft == m_rectDefault.getLeft() &&
          operandRight == m_rectDefault.getRight() )
      {
         IETRect bounds = getLogicalBoundingRect();
         operandTop = findBestTopForEmptyCombinedFragment();
         operandBottom = operandTop - 20;
         operandLeft = bounds.getLeft();
         operandRight = bounds.getRight();
         
         updateElementsBelow(operandTop - operandBottom);
      }

      IETRect rectOperands = new ETRect();
      rectOperands.setTop(operandTop);
      rectOperands.setBottom(operandBottom);
      rectOperands.setLeft(operandLeft);
      rectOperands.setRight(operandRight);
      
      return rectOperands;
   }
   
   
   // protected methods

   /**
    * Creates an IActivityPartition for the inserting into the table
    */
   protected IInteractionOperand createNewInteractionOperand()
   {
      IInteractionOperand operand = null;
      {
         IElement element = getModelElement();
         if( element instanceof ICombinedFragment )
         {
            ICombinedFragment parentElement = (ICombinedFragment)getModelElement();
            if( parentElement != null )
            {
               operand = parentElement.createOperand();
            }
         }
      }
      
      return operand;
   }

   /**
    * Ensure that the zone model elements and the zone compartments match up
    */
   protected void validateZoneCompartments(boolean attachElements)
   {
      IElement element = getModelElement();
      if( element instanceof ICombinedFragment )
      {
         ICombinedFragment parentElement = (ICombinedFragment)element;
               
         ETList< IInteractionOperand > operands = parentElement.getOperands();
         if( operands != null )
         {
            // Copy the partitions to the elements
            ETList< IElement > elements = null;
            elements = (new CollectionTranslator<IInteractionOperand, IElement>()).copyCollection(operands);
      
            super.validateZoneCompartments( elements, IETZoneDividers.DMO_VERTICAL, attachElements );
         }
      }
   }

   public static boolean isDefaultRect( IETRect rect )
   {
      return m_rectDefault.equals( rect );
   }

    private int findBestTopForEmptyCombinedFragment()
    {
        // Set up the default value first
        IETRect bounds = getLogicalBoundingRect();
        int iCnt = getCompartments().size();
        int retVal = bounds.getBottom()- (iCnt + 1) * 40;
        
        // Find first message before the combined fragment
        ICombinedFragment fragment = (ICombinedFragment) getModelElement();
        IElement owner = fragment.getOwner();
        
        IMessage prevMsg = findPreviousMessage(fragment);
        
        if(prevMsg != null)
        {
            // get location of message and adjust for the combined fragment.
            List < IPresentationElement > presentations = prevMsg.getPresentationElements();
            if((presentations != null) && presentations.size() > 0)
            {
                IEdgePresentation p = (IEdgePresentation)presentations.get(0);
                TSEEdge edge = p.getTSEdge();
                retVal = (int) edge.getSourceY() - 30;
            }
        }
            
        return retVal;
    }
    
    private void updateElementsBelow(int delta)
    {
        double yDelta = ((TSENode)getEngine().getUI().getTSObject()).getSize().getHeight();
        
        ICombinedFragment fragment = (ICombinedFragment) getModelElement();
        IMessage nextMsg = findNextMessages(fragment);
        
        // get location of message and adjust for the combined fragment.
        List < IPresentationElement > presentations = null ;
        if (nextMsg != null)
            presentations = nextMsg.getPresentationElements();
        
        if((presentations != null) && (presentations.size() > 0))
        {
            IEdgePresentation p = (IEdgePresentation)presentations.get(0);
            TSEEdge edge = p.getTSEdge();
            double newPos = edge.getSourceY() - yDelta;

            TSConnector sourceConnector = edge.getSourceConnector();
            ILifelineDrawEngine engine = (ILifelineDrawEngine)TypeConversions.getDrawEngine(sourceConnector);
            ICompartment compartment = engine.getLifelineCompartment();
            if(compartment instanceof IConnectorsCompartment)
            {
                IConnectorsCompartment connectorsComp = 
                        (IConnectorsCompartment)compartment;
                connectorsComp.moveConnector(edge.getSourceConnector(), newPos, false, true);
            }
        }
        
        pushSiblingFragments(fragment, yDelta);
    }

    private IMessage findNextMessages(IElement reference)
    {
        return findRelativeMessage(reference, false);
    }

    private IMessage findPreviousMessage(final IElement reference)
    {
        return findRelativeMessage(reference, true);
    }
    
    private IMessage findRelativeMessage(final IElement reference, boolean before)
    {   
        IElement owner = reference.getOwner();
        List < ? extends INamedElement > children = null;
        
        IMessage retVal = null;
        if(owner instanceof IInteraction)
        {
            IInteraction interaction = (IInteraction) owner;
            children = interaction.getOwnedElements();
        }
        else if (owner instanceof IInteractionOperand)
        {
            IInteractionOperand operand = (IInteractionOperand)owner;
            children = operand.getFragments();
        }
        
        if(children != null)
        {
            int myIndex = children.indexOf(reference);
            
            if(before == true)
            {
                for(int index = myIndex - 1; index >= 0; index--)
                {
                    INamedElement sibling = children.get(index);
                    if(sibling instanceof IAtomicFragment)
                    {
                        IAtomicFragment atomicFrag = (IAtomicFragment)sibling;
                        IEventOccurrence event = atomicFrag.getEvent();
                        retVal = event.getReceiveMessage();
                        if(retVal == null)
                        {
                            retVal = event.getSendMessage();
                        }
                        break;
                    }
                }
            }
            else
            {
                for(int index = myIndex + 1; index < children.size(); index++)
                {
                    INamedElement sibling = children.get(index);
                    if(sibling instanceof IAtomicFragment)
                    {
                        IAtomicFragment atomicFrag = (IAtomicFragment)sibling;
                        IEventOccurrence event = atomicFrag.getEvent();
                        retVal = event.getReceiveMessage();
                        if(retVal == null)
                        {
                            retVal = event.getSendMessage();
                        }
                        break;
                    }
                }
            }
            
            if(retVal == null)
            {
                if (owner instanceof IInteractionOperand)
                {
                    retVal = findRelativeMessage(owner.getOwner(), before);
                }
            }
        }
        
        return retVal;
    }

    private void pushSiblingFragments(IElement reference, double yDelta)
    {
        IElement owner = reference.getOwner();
        List < ? extends INamedElement > children = null;
        
        IMessage retVal = null;
        if(owner instanceof IInteraction)
        {
            IInteraction interaction = (IInteraction) owner;
            children = interaction.getOwnedElements();
        }
        else if (owner instanceof IInteractionOperand)
        {
            IInteractionOperand operand = (IInteractionOperand)owner;
            children = operand.getFragments();
        }
        
        if(children != null)
        {
            int myIndex = children.indexOf(reference);
            if(myIndex == -1)
            {
                myIndex = children.size();
            }
            
            // For some reason, there needs to be an additional space added
            // when two combined fragments are siblings.  Since we are starting
            // with a combined fragment (since this compartment is owned by
            // a combined fragment), assume that we are already in one.
            boolean prevElementWasCF = true;
            
            for(int index = myIndex + 1; index < children.size(); index++)
            {
                INamedElement sibling = children.get(index);
                if(sibling instanceof ICombinedFragment)
                {   
                    // get location of message and adjust for the combined fragment.
                    List < IPresentationElement > presentations = sibling.getPresentationElements();
                    if((presentations != null) && presentations.size() > 0)
                    {
                        INodePresentation p = (INodePresentation)presentations.get(0);
                        double center = p.getTSNode().getCenterY() - yDelta;
                        
                        if(prevElementWasCF == true)
                        {
                            center -= 30;
                        }
                        
                        p.getTSNode().setCenterY(center);
                        prevElementWasCF = true;
                    }
                    
                    pushChildFragments((ICombinedFragment)sibling, yDelta);
                }
            }
        }
    }

    private void pushChildFragments(ICombinedFragment fragment, double yDelta)
    {
        List<IInteractionOperand> operands = fragment.getOperands();
        for(IInteractionOperand operand : operands)
        {
            pushSiblingFragments(operand, yDelta);
        }
    }
    
   public static final IETRect m_rectDefault = new ETRect( Integer.MAX_VALUE/2, Integer.MIN_VALUE/2, 
      Integer.MIN_VALUE, Integer.MIN_VALUE );
}


