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



package org.netbeans.modules.uml.ui.swing.trackbar;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;
import javax.swing.event.MouseInputAdapter;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.controls.trackbar.TrackBarResource;
import org.netbeans.modules.uml.ui.support.ProjectTreeHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.MoveToFlags;
import org.netbeans.modules.uml.ui.support.finddialog.FindController;
import org.netbeans.modules.uml.ui.support.finddialog.IFindController;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

/**
 *
 * @author Trey Spiva
 */
public class TrackCar extends TrackItem implements ActionListener
{
   public final static String NAVIGATE_TO_CLASSIFIER = "NAVIGATE_TO_CLASSIFIER";
   public final static String NAVIGATE_TO_INSTANCE   = "NAVIGATE_TO_INSTANCE";
   
   private IPresentationElement m_PresentationElement = null;
   private boolean m_IsOverrideMode = false;
   
   public TrackCar(JTrackBar bar, IPresentationElement element)
   {
      super(bar);
      setPresentationElement(element);
      
      setBackground(Color.WHITE);
      setBorder(new LineBorder(Color.BLACK));
      
      TrackItemMouseListener listener = new TrackItemMouseListener();
      addMouseMotionListener(listener);
      addMouseListener(listener);
   }
   
   
   /**
    * Retrieves the xmi id for a presenation element.
    *
    * @param element The model element.
    * @return The xmi id.
    */
   public String getXMIID(IPresentationElement element)
   {
      String retVal = ""; //$NON-NLS-1$
      
      if(element != null)
      {
         retVal = element.getXMIID();
      }
      
      return retVal;
   }
   
   /**
    * Retrieves the xmi id the element that the track car represents.
    *
    * @return The xmi id.
    */
   public String getXMIID()
   {
      return getXMIID(getPresentationElement());
   }
   
   /**
    * Retrieves the presenation element that the track car represents.
    */
   public IPresentationElement getPresentationElement()
   {
      return m_PresentationElement;
   }
   
   /**
    * Sets the presenation element that the track car represents.
    */
   public void setPresentationElement(IPresentationElement element)
   {
      m_PresentationElement = element;
   }
   
   /**
    * Determines if the object is same track car.  To determine if the track cars
    * are the same the presentation element xmi id is checked.
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      boolean retVal = false;
      
      if (obj instanceof TrackCar)
      {
         TrackCar otherCar = (TrackCar)obj;
         String myXMIID = getXMIID();
         if(myXMIID.equals(otherCar.getXMIID()))
         {
            retVal = true;
         }
      }
      
      return retVal;
   }
   
   public void resizeBasedOnPresentationElement(boolean repaintNow)
   {
      Rectangle bounds = determineBoundingRect();
      setBounds(bounds);
      
      if (repaintNow == true)
      {
         repaint();
      }
   }
   
   public void paint(Graphics g)
   {
      Color curColor = g.getColor();
      
      if(hasFocus() == true)
      {
         g.setColor(SystemColor.textHighlight);
      }
      else
      {
         g.setColor(getBackground());
      }
      
      Rectangle bounds = getBounds();
      g.fillRect(0, 0, bounds.width, bounds.height);
      
      String name = getName();
      if((name != null) && (name.length() > 0))
      {
         FontMetrics metrics = g.getFontMetrics();
         int midX = bounds.width / 2;
         int width = metrics.stringWidth(name);
         int startX = midX - (width / 2);
         
         if(hasFocus() == true)
         {
            g.setColor(SystemColor.textHighlightText);
         }
         else
         {
            g.setColor(SystemColor.textText);
         }
         g.drawString(name, startX, bounds.height - JTrackBar.INSET - metrics.getDescent());
      }
      
      paintBorder(g);
      
      g.setColor(curColor);
   }
   
   //**************************************************
   // Helper Methods
   //**************************************************
   
   /**
    * Determine the size of this item.
    *
    * @param rect The rectangle to update with items width.
    */
   protected void limitBoundingRect(Rectangle rect)
   {
      if( rect != null )
      {
         IETRect bounds = determinePresentationElementBoundingRect();
         if( bounds != null )
         {
            rect.x = bounds.getIntX();
            rect.width = bounds.getIntWidth();
         }
      }
   }
   
   /**
    * @return
    */
   protected IETRect determinePresentationElementBoundingRect()
   {
      IETRect retVal = null;
      
      IPresentationElement presElement = getPresentationElement();
      if(presElement != null)
      {
         IETRect logical = TypeConversions.getLogicalBoundingRect(presElement, false);
         if(logical != null)
         {
            IDiagram diagram = getTrackBar().getDiagram();
            if(diagram != null)
            {
               retVal = diagram.logicalToDeviceRect(logical);
            }
         }
      }
      
      return retVal;
   }
   
   public void push(int xDelta)
   {
      if(IsOverrideConstraintsMode() == false)
      {
         super.push(xDelta);
      }
      else
      {
         setLocation(getX() + xDelta, getY());
      }
      movePresentation(xDelta);
   }
   
   protected void movePresentation(int xDelta)
   {
      IPresentationElement element = getPresentationElement();
      if (element instanceof INodePresentation)
      {
         INodePresentation nodePres = (INodePresentation)element;
         //nodePres.moveTo(xDelta, 0, 0);
         IETRect bounds = nodePres.getBoundingRect();
         if(bounds != null)
         {
            //nodePres.moveTo(bounds.getLeft() + xDelta, bounds.getTop(), true, true, false);
            int moveFlags = MoveToFlags.MTF_DEVICECOORD | MoveToFlags.MTF_MOVEX | MoveToFlags.MTF_INVALIDATE;
            
            int centerX = getX() + (getWidth() / 2);
            nodePres.moveTo(centerX + xDelta - 2, getY(), moveFlags);
         }
      }
   }
   
   protected void fixConstraints()
   {
      TrackItem prevItem = getPreviousItem();
      TrackItem nextItem = getNextItem();
      
      nextItem = readjustNieghbors(prevItem, nextItem);
//      prevItem = readj ustNieghbors(nextItem, prevItem);
      updateContraints(0);
      
      setPreviousItem(null);
      setNextItem(null);
      
      printPreviousItems(prevItem);
      printNextItems(nextItem);
      
      getTrackBar().addCar(this, false);
      
      printPreviousItems(getPreviousItem());
      printNextItems(getNextItem());
   }
   
   protected void printNextItems(TrackItem nextItem)
   {
      TrackItem curItem = nextItem;
      while(curItem != null)
      {
         ETSystem.out.println(nextItem);
         curItem = curItem.getNextItem();
      }
   }
   
   protected void printPreviousItems(TrackItem prevItem)
   {
      TrackItem curItem = prevItem;
      while(curItem != null)
      {
         ETSystem.out.println(curItem);
         curItem = curItem.getPreviousItem();
      }
   }
   
   protected TrackItem readjustNieghbors(TrackItem prevItem,
           TrackItem nextItem)
   {
      if(prevItem != null)
      {
         if(nextItem instanceof TrackCoupling)
         {
            TrackItem oldNextItem = nextItem;
            nextItem = nextItem.getNextItem();
            if(nextItem != null)
            {
               nextItem.setPreviousItem(this);
            }
            prevItem.setNextItem(this);
            
            oldNextItem.setNextItem(null);
            oldNextItem.setPreviousItem(null);
            getTrackBar().remove(oldNextItem);
         }
         
         if(nextItem != null)
         {
            prevItem.setNextItem(nextItem);
            nextItem.setPreviousItem(prevItem);
         }
         else
         {
            prevItem.setNextItem(null);
            prevItem.setPreviousItem(null);
            getTrackBar().remove(prevItem);
         }
         
      }
      else if(nextItem != null)
      {
         TrackItem myNextItem = nextItem.getNextItem();
         if(myNextItem != null)
         {
            myNextItem.setPreviousItem(null);
         }
         
         nextItem.setNextItem(null);
         nextItem.setPreviousItem(null);
         getTrackBar().remove(nextItem);
      }
      
      if(prevItem != null)
      {
         prevItem.setBounds(prevItem.determineBoundingRect());
      }
      
      if(nextItem != null)
      {
         nextItem.setBounds(nextItem.determineBoundingRect());
      }
      return nextItem;
   }
   
   public boolean IsOverrideConstraintsMode()
   {
      return m_IsOverrideMode;
   }
   
   public void setIsOverrideConstraintsMode(boolean value)
   {
      m_IsOverrideMode = value;
   }
   
   /**
    * Retrieves the context menu.  The method showContextMenu uses
    * buildContextMenu to determine the menu to be displayed.  If null is returned
    * the a menu will not be displayed.
    *
    * @return The menu to be displayed.
    */
   public void buildContextMenu(JPopupMenu menu)
   {
      menu.add(getNavigateToInstance());
      menu.add(getNavigateToClassifier());
   }
   
   
   /**
    *
    */
   protected JMenuItem getNavigateToClassifier()
   {
      JMenuItem retVal = new JMenuItem(TrackBarResource.getString("TrackCar.Navigate_to_Classifier"), 'C'); //$NON-NLS-1$
      retVal.setActionCommand(NAVIGATE_TO_CLASSIFIER);
      retVal.addActionListener(this);
      return retVal;
      
   }
   
   /**
    *
    */
   protected JMenuItem getNavigateToInstance()
   {
      JMenuItem retVal = new JMenuItem(TrackBarResource.getString("TrackCar.Navigate_to_Instance"), 'I'); //$NON-NLS-1$
      retVal.setActionCommand(NAVIGATE_TO_INSTANCE);
      retVal.addActionListener(this);
      return retVal;
   }
   
   /* (non-Javadoc)
    * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    */
   public void actionPerformed(ActionEvent e)
   {
      String cmd = e.getActionCommand();
      
      if(cmd.equals(NAVIGATE_TO_INSTANCE) == true)
      {
         JTrackBar bar = getTrackBar();
         if(bar != null)
         {
            IDiagram diagram = bar.getDiagram();
            if(diagram != null)
            {
               diagram.centerPresentationElement(getPresentationElement(), true, true);
            }
         }
      }
      else if(cmd.equals(NAVIGATE_TO_CLASSIFIER) == true)
      {
         IElement cpElement = TypeConversions.getElement(m_PresentationElement);
         if (cpElement != null && cpElement instanceof ILifeline)
         {
            ILifeline cpLifeline = (ILifeline)cpElement;
            IClassifier cpClassifier = cpLifeline.getRepresentingClassifier();
            if (cpClassifier != null)
            {
                 // Fixed issue 80923- 'Navigate to classifer does not work
                 // The call to cpFindController.navigateToElement bacause the method can
                 // not access to the IProjectTreeControl, so using 
                 // ProjectTreeHelper.findElementInProjectTree(cpClassifier) instead.
               
                 ProjectTreeHelper.findElementInProjectTree(cpClassifier);
//               IFindController cpFindController = new FindController();
//               if(cpFindController != null)
//               {
//                  cpFindController.navigateToElement(cpClassifier);
//               }
            }
         }
      }
   }
   
   public class TrackItemMouseListener extends MouseInputAdapter
   {
      private final static int UNINIAILIZED = -1;
      private int xOffset = UNINIAILIZED;
      
         /* (non-Javadoc)
          * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
          */
      public void mouseDragged(MouseEvent e)
      {
         if(xOffset > UNINIAILIZED)
         {
            int curX = getX();
            int xDelta = e.getX() - xOffset;
            
            push(xDelta);
         }
      }
      
      /* (non-Javadoc)
       * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
       */
      public void mousePressed(MouseEvent e)
      {
         xOffset = e.getX();
         
         if((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK)
         {
            setIsOverrideConstraintsMode(true);
         }
      }
      
      /* (non-Javadoc)
       * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
       */
      public void mouseReleased(MouseEvent e)
      {
         xOffset = UNINIAILIZED;
         
         if(IsOverrideConstraintsMode() == true)
         {
            fixConstraints();
            setIsOverrideConstraintsMode(false);
         }
      }
      
   }
}
