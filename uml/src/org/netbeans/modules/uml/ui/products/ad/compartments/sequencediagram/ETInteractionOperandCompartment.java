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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Iterator;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETCompartment;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.sequencediagram.ICombinedFragmentDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author sumitabhk
 *
 */
public class ETInteractionOperandCompartment extends ETCompartment implements IADInteractionOperandCompartment
{

	/**
	 * 
	 */
	public ETInteractionOperandCompartment()
	{
		super();
	}


   // ICompartment

	/**
	 * This is the name of the drawengine used when storing and reading from the product archive.
	 *
	 * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
	 * product archive (etlp file).
	 */
	public String getCompartmentID()
	{
		return "ADInteractionOperandCompartment";
	}
   
   /**
    * Called when the context menu is about to be displayed.  The compartment should add whatever buttons
    * it might need.
    *
    * @param pContextMenu [in] The context menu about to be displayed
    * @param logicalX [in] The logical x location of the context menu event
    * @param logicalY [in] The logical y location of the context menu event
    */
   public void onContextMenu( IMenuManager manager)
   {
      if (getEnableContextMenu())
      {
          Point point = manager.getLocation();
          
          // (LLS) Adding the buildContext logic to support A11Y issues.  The
          // user should be able to use the CTRL-F10 keystroke to activate
          // the context menu.  In the case of the keystroke the location
          // will not be valid.  Therefore, we have to just check if the
          // compartment is selected.
          boolean buildContext = false;
          if(point != null)
          {
              buildContext = containsPoint(point);
          }
          else
          {
              buildContext = isSelected();
          }
          
          if(buildContext == true)
          {
              addInteractionOperandButtons(manager);
          }
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
      return isParentDiagramReadOnly() ? false : true;
   }

   public boolean onHandleButton(ActionEvent event, String id)
   {
      if ( id.equals("MBK_CF_EDIT_INTERACTION_CONSTRAINT") )
      {
         if( m_engine instanceof ICombinedFragmentDrawEngine )
         {
            ICombinedFragmentDrawEngine engine = (ICombinedFragmentDrawEngine)m_engine;
            IInteractionOperand operand = getInteractionOperand();
            if( (engine != null) &&
                (operand != null) )
            {
               engine.editConstraint( operand );
            }
         }
      }
      else
      {
         return super.onHandleButton(event, id);
      }
      
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getCompartmentHasNonRectangularShape()
    */
   public boolean getCompartmentHasNonRectangularShape()
   {
      return true;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getCompartmentShape()
    */
   public ETList< IETPoint > getCompartmentShape()
   {
      ETList< IETPoint > listPoints = new ETArrayList< IETPoint >();
      if( listPoints != null )
      {
         // Note the paths must be in counter-clockwise order, to make the area hollow
         IETRect rectInner = getWinScaledOwnerRect();
         rectInner.deflateRect( HOLLOW_EDGE_WIDTH, HOLLOW_EDGE_WIDTH );

         // TEST listPoints.add( new ETPoint( 0, rectInner.getTop() ));
         listPoints.add( new ETPoint( rectInner.getTopLeft() ));
         listPoints.add( new ETPoint( rectInner.getTopRight() ));
         listPoints.add( new ETPoint( rectInner.getBottomRight() ));
         listPoints.add( new ETPoint( rectInner.getBottomLeft() ));
         listPoints.add( new ETPoint( rectInner.getTopLeft() ));
         // TEST listPoints.add( new ETPoint( 0, rectInner.getTop() ));

/* TEST
         // Move to the inside of the compartment
         listPoints.add( new ETPoint( 0, rectInner.getTop() ));
         listPoints.add( new ETPoint( rectInner.getRight(),  rectInner.getTop() ));
         listPoints.add( new ETPoint( rectInner.getRight(),  rectInner.getBottom() ));
         listPoints.add( new ETPoint( rectInner.getLeft(),   rectInner.getBottom() ));
         listPoints.add( new ETPoint( rectInner.getLeft(),   rectInner.getTop() ));
         listPoints.add( new ETPoint( 0, rectInner.getTop() ));
*/
      }

      return listPoints;
   }
   
   
   // IADInteractionOperandCompartment

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADInteractionOperandCompartment#setInteractionOperand(org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand)
    */
   public void setInteractionOperand(IInteractionOperand pInteractionOperand)
   {
      addModelElement( pInteractionOperand, -1 );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADInteractionOperandCompartment#getInteractionOperand()
    */
   public IInteractionOperand getInteractionOperand()
   {
      IInteractionOperand interactionOperand = null;
      {
         IElement element = getModelElement();
         if( element instanceof IInteractionOperand )
         {
            interactionOperand = (IInteractionOperand)element;
         }
      }
      
      return interactionOperand;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADInteractionOperandCompartment#getMinimumEncompassingRect(int)
    */
   public IETRect getMinimumEncompassingRect(int lPreviousCompartmentsBottom)
   {
      return calculateMinimumEncompassingRect();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADInteractionOperandCompartment#expandToIncludeCoveredItems()
    */
   public void expandToIncludeCoveredItems()
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.compartments.IADInteractionOperandCompartment#selectAllCoveredItems()
    */
   public int selectAllCoveredItems()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * Returns the rectangle that minimally surrounds the items contained by the compartment
    */
   IETRect calculateMinimumEncompassingRect()
   {
      IETRect rectInteractionOperand = new ETRect(0,0,0,0);

      IInteractionOperand interactionOperand = getInteractionOperand();
      if( interactionOperand != null )
      {
         IDiagram diagram = TypeConversions.getDiagram( m_engine );
         if( diagram != null )
         {
            // Determine the size of the combined fragment

            // ... from the messages
            long lMessageCnt = 0;
            ETList< IMessage > messages = interactionOperand.getCoveredMessages();
            for (Iterator iter = messages.iterator(); iter.hasNext();)
            {
               IMessage message = (IMessage)iter.next();
               
               IETRect rectEncompassing = getEncompassingRect( diagram, message );
               rectEncompassing.inflate( 5 );
               rectInteractionOperand.unionWith( rectEncompassing );
            }

            // ... from the contained combined fragments
            ETList< IInteractionFragment > interactionFragments = interactionOperand.getFragments();
            for (Iterator iterator = interactionFragments.iterator(); iterator.hasNext();)
            {
               IInteractionFragment interactionFragment = (IInteractionFragment)iterator.next();
               
               if (interactionFragment instanceof ICombinedFragment)
               {
                  ICombinedFragment combinedFragment = (ICombinedFragment)interactionFragment;
                  
                  IETRect rectEncompassing = getEncompassingRect( diagram, combinedFragment );
                  rectEncompassing.inflate( 5 );
                  rectInteractionOperand.unionWith( rectEncompassing );
               }
            }

            // ... from the meta data covered lifelines
            ETList< ILifeline > lifelines = interactionOperand.getCoveredLifelines();
            for (Iterator iterator = lifelines.iterator(); iterator.hasNext();)
            {
               ILifeline lifeline = (ILifeline)iterator.next();
               
               IETRect rectEncompassingLifeline = getEncompassingRect( diagram, lifeline );
               rectInteractionOperand.setTop( Math.min( rectInteractionOperand.getTop(), rectEncompassingLifeline.getTop() ));
               rectInteractionOperand.setBottom( Math.max( rectInteractionOperand.getBottom(), rectEncompassingLifeline.getBottom() ));
            }
         }
      }

      return rectInteractionOperand;
   }

   /**
    * For all the presentation elements on the diagram that represent the model element,
    * find the smallest rectangle that contains them.
    *
    * @param pDiagram
    * @param pElement
    */
   protected IETRect getEncompassingRect( IDiagram diagram, IElement element )
   {
      IETRect rectEncompassing = new ETRect( 0, 0, 0, 0 );

      if( (diagram != null) &&
          (element != null) )
      {
         ETList< IPresentationElement > pes = diagram.getAllItems2( element );
         if( pes != null )
         {
            rectEncompassing = TypeConversions.getLogicalBoundingRect( pes, true );
            // CLEAN do we need this? rectEncompassing.normalizeRect();
         }
      }

      return rectEncompassing;
   }
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#onGraphEvent(int)
    */
   public long onGraphEvent(int nKind)
   {
      long retVal = super.onGraphEvent(nKind);
      
      switch(nKind)
      {
         case IGraphEventKind.GEK_POST_CREATE:
            takeOwnershipOfContainedMessages();
            break;
         default:
            // do nothing
            break;
      }
      
      return retVal;
   }
   
   protected void takeOwnershipOfContainedMessages()
   {
      IInteractionOperand interactionOperand = getInteractionOperand();
      
      if(interactionOperand != null)
      {
         IDrawingAreaControl dac = getDrawingArea();
         
         if(dac != null)
         {
            IETRect etRect = getLogicalBoundingRect();
            
            if(etRect != null)
            {
               ETList<IPresentationElement> pes = dac.getAllEdgesViaRect(etRect,false);
               
               if(pes != null)
               {
                  int count = pes.getCount();
                  
                  for(int index = 0;index < count;index++)
                  {
                     IPresentationElement pe = pes.item(index);
                     
                     if(pe!= null)
                     {
                        IElement element = TypeConversions.getElement(pe);
                        
                        if(element instanceof IMessage)
                        {
                           ((IMessage)element).setInteractionOperand(interactionOperand);
                        }
                     }
                  }
               }
            }
         }
      }
   }
}

