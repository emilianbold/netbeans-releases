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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.text.MessageFormat;

import javax.accessibility.AccessibleContext;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;
import javax.swing.event.MouseInputAdapter;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.ui.controls.drawingarea.GetHelper;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.controls.drawingarea.UIDiagram;
import org.netbeans.modules.uml.ui.controls.trackbar.TrackBarResource;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.MoveToFlags;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.DiagramEngine;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.TSEGraphWindow;

/**
 * 
 * @author Trey Spiva
 */
public class JTrackBar extends JPanel implements ITrackBar, FocusListener
{
   public final static int INSET = 3;

   //   private IDiagram m_Diagram = null;
   private IDiagramEngine m_DiagramEngine = null;

   private HashMap < String, TrackCar > m_TrackCars = new HashMap < String, TrackCar > ();
   private String currentFocusedCar = "";
   
   public JTrackBar(IDiagramEngine engine)
   {
      setLayout(null);
      setDiagramEngine(engine);
      setFocusable(true);
      addFocusListener(this);
      
      InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
      inputMap.put(KeyStroke.getKeyStroke("control T"),
                   "SWITCH_TRACKBAR_FOCUS");
      getActionMap().put("SWITCH_TRACKBAR_FOCUS", new AbstractAction()
      {
          public void actionPerformed(ActionEvent e)
          {              
           
              boolean switchToBar = true;
              if(hasFocus() == true)
              {
                  switchToBar = false;
              }
              else
              {
                int size = getComponentCount();
                for(int i = 0; i < size; i++)
                {
                    if(getComponent(i).hasFocus() == true)
                    {
                        switchToBar = false;
                        break;
                    }
                }
              }
              
              if(switchToBar == true)
              {
                  requestFocus();
//                  if(getComponentCount() > 0)
//                  {
//                      getComponent(0).requestFocus();
//                  }
              }
              else
              {
                 m_DiagramEngine.getDrawingArea().setFocus();
              }
          }
      });

   }

   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#setBackStyle(long)
    */
   public void setBackStyle(long style)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#getBackStyle()
    */
   public long getBackStyle()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#setDrawMode(long)
    */
   public void setDrawMode(long mode)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#getDrawMode()
    */
   public long getDrawMode()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#setDrawStyle(long)
    */
   public void setDrawStyle(long style)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#getDrawStyle()
    */
   public long getDrawStyle()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#setDrawWidth(long)
    */
   public void setDrawWidth(long width)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#getDrawWidth()
    */
   public long getDrawWidth()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#setFillStyle(long)
    */
   public void setFillStyle(long style)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#getFillStyle()
    */
   public long getFillStyle()
   {
      // TODO Auto-generated method stub
      return 0;
   }

//   /* (non-Javadoc)
//    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#setValid(boolean)
//    */
//   public void setValid(boolean bValid)
//   {
//      // TODO Auto-generated method stub
//
//   }
//
//   /* (non-Javadoc)
//    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#getValid()
//    */
//   public boolean getValid()
//   {
//      // TODO Auto-generated method stub
//      return false;
//   }

   //   /* (non-Javadoc)
   //    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#setDiagram(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
   //    */
   //   public void setDiagram(IDiagram pDiagram)
   //   {
   //      m_Diagram = pDiagram;
   //      
   //   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#getDiagram()
    */
   public IDiagram getDiagram()
   {
      IDiagram retVal = null;
      IDiagramEngine engine = getDiagramEngine();
      if (engine != null)
      {
         IDrawingAreaControl ctrl = engine.getDrawingArea();
         if(ctrl != null)
         {
            retVal = ctrl.getDiagram();
         }
      }

      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#initialize()
    */
   public void initialize()
   {
      IDiagram diagram = getDiagram();

      Component[] curComponents = getComponents();
      clear();

      if (diagram != null)
      {
         ETList < IPresentationElement > presentationElements = diagram.getAllItems();
         if ((presentationElements != null) && (presentationElements.size() > 0))
         {
            for (Iterator < IPresentationElement > iter = presentationElements.iterator(); iter.hasNext();)
            {
               IPresentationElement curElement = iter.next();
               addPresentationElement(curElement);
            }
         }
      }

      // Keep the previous coupling information
      for (int index = 0; index < curComponents.length; index++)
      {
         if (curComponents[index] instanceof TrackCoupling)
         {
            TrackCoupling curCoupling = (TrackCoupling)curComponents[index];

            // search for the new matching
            // couplings match if their previous & next items match
            int max = getComponentCount();
            boolean foundIt = false;
            for (int i = 0;(i < max) && (foundIt == false); i++)
            {
               Component curComponent = getComponent(i);
               if (curComponent instanceof TrackCoupling)
               {
                  TrackCoupling testCoupling = (TrackCoupling)curComponent;
                  if (curCoupling.equals(testCoupling) == true)
                  {
                     curCoupling.copyAttributes(testCoupling);
                  }
               }
            }
         }
      }
      
      repaint();
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#invalidate(boolean)
    */
   public void invalidate(boolean bErase)
   {
      invalidate(bErase);
   }

   /**
    * Restores the track bar from the product archive (etlp) file.
    *
    * @param pProductArchive [in] The product archive being read in.
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#load(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive)
    */
   public void load(IProductArchive pProductArchive)
   {
      if (pProductArchive != null)
      {
         IProductArchiveElement aElement = pProductArchive.getElement(IProductArchiveDefinitions.ELEMENT_TRACKBAR);
         if (aElement != null)
         {
            int max = getComponentCount();
            for (int index = 0; index < max; index++)
            {
               TrackItem curItem = (TrackItem)getComponent(index);
               if (curItem != null)
               {
                  curItem.load(aElement);
               }
            }
         }
      }

      repaint();
   }

   /**
    * Saves the track bar to the product archive (etlp) file.
    *
    * @param pProductArchive [in] The product archive being created.
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#save(org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive)
    */
   public void save(IProductArchive pProductArchive)
   {
      if (pProductArchive != null)
      {
         IProductArchiveElement aElement = pProductArchive.createElement(IProductArchiveDefinitions.ELEMENT_TRACKBAR);
         if (aElement != null)
         {
            int max = getComponentCount();
            for (int index = 0; index < max; index++)
            {
               TrackItem curItem = (TrackItem)getComponent(index);
               if (curItem != null)
               {
                  curItem.save(aElement);
               }
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#addPresentationElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
    */
   public void addPresentationElement(IPresentationElement pPresentationElement)
   {
      if (pPresentationElement != null)
      {
         TrackCar curCar = getTrackCar(pPresentationElement);
         if(curCar == null)
         {
            // Make sure the presentation element represents a lifeline
            IElement element = TypeConversions.getElement(pPresentationElement);
            if (element instanceof ILifeline)
            {
               TrackCar car = new TrackCar(this, pPresentationElement);
               car.setName(getCaption(pPresentationElement));
   
               addCar(car);
					updateCarLocation(car);
            }
         }
         else
         {
            addCar(curCar, false);
				updateCarLocation(curCar);
         }
      }

   }

   protected String getCaption(IPresentationElement pPresentationElement)
   {
      String retVal = " : ";
      
      if(pPresentationElement != null)
      {
         IElement element = TypeConversions.getElement(pPresentationElement);
         if(element != null)
         {
            IDataFormatter formatter = ProductHelper.getDataFormatter();
            if(formatter != null)
            {
               retVal = formatter.formatElement(element);   
            }
         }
         
      }
      
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#removePresentationElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
    */
   public void removePresentationElement(IPresentationElement pPresentationElement)
   {
      if (pPresentationElement != null)
      {
         TrackCar car = getTrackCar(pPresentationElement);
         if (car != null)
         {
            TrackCoupling coupling = removeCar(car);
            coupling = null;
         }

         car = null;
      }
      repaint();
   }

//   /* (non-Javadoc)
//    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#moveObjects()
//    */
//   public boolean moveObjects()
//   {
//      // TODO Auto-generated method stub
//      return false;
//   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#preResize(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
    */
   public void preResize(IPresentationElement pPresentationElement)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#resize(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
    */
   public boolean resize(IPresentationElement pPresentationElement)
   {
      boolean retVal = false;
      
      TrackCar car = getTrackCar(pPresentationElement);
      if(car != null)
      {
         int origX = car.getX();
         int origRight = origX + car.getWidth();
         
         car.resizeBasedOnPresentationElement(true);
         
         int afterX = car.getX();
         int afterRight = afterX + car.getWidth();
         
         TrackItem prevItem = car.getPreviousItem();
         if(prevItem != null)
         {
            int xDelta = afterX - origX;
            if(xDelta < 0)
            {
               prevItem.push(xDelta);
            }
            else
            {
               prevItem.updateContraints(xDelta);
            }
         }
         
         TrackItem nextItem = car.getNextItem();
         if(nextItem != null)
         {
            int xDelta = afterRight - origRight;
            if(xDelta > 0)
            {
               nextItem.push(xDelta);
            }
            else
            {
               nextItem.updateContraints(xDelta);
            }
          }
      }
      
      return retVal;
   }

   /**
    * Update the name of the car associated with the presentation element
    *
    * @param pPresentationElement The presentation element connected to the
    *                             track bar car to be updated, and containing 
    *                             the name
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#updateName(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
    */
   public void updateName(IPresentationElement pPresentationElement)
   {
      if(pPresentationElement != null)
      {
         TrackCar car = getTrackCar(pPresentationElement);
         updateName(car);
      }
   }

   /**
     * Update the name of the car by using the cars associated presentation 
     * element to retrieve the cars name.
     *
     * @param car The track bar car to be updated
     */
   public void updateName(TrackCar car)
   {
      if(car != null)
      {
         String caption = getCaption(car.getPresentationElement());
         car.setName(caption);
         car.invalidate();
      }
   }
   
   /**
    * Update all the names of all the track bar cars
    * 
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#updateAllCarNames()
    */
   public void updateAllCarNames()
   {
      // The sorted list if cars is maintained in the trackbar components
      // The C++ code eroneously uses its map which is not sorted by location
      final int nCount = getComponentCount();
      for( int index=0; index<nCount; index++ )
      {
         Component component = (Component)getComponent( index );
         if (component instanceof TrackCar)
         {
            TrackCar car = (TrackCar)component;
            updateName( car );
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#expandAssociatedCoupling(org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation)
    */
   public void expandAssociatedCoupling(ILabelPresentation pLabelPresentation)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#expandCouplings()
    */
   public void expandCouplings()
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#onPostScrollZoom()
    */
   public boolean onPostScrollZoom()
   {
      // Fix J1896:  This operation was being called when the diagram was already closed
      //             So, we put a check to see if the diagram is still valid before
      //             laying out the trackbar.
       
      IDiagram diagram = getDiagram();
      if( diagram != null )
      {
         layoutTrack();
      }

      return true;
   }

   /**
    * Layout all the cars by resizing the cars to the size of the 
    * presentation elements
    */
   protected void layoutTrack()
   {
      // Uncomment the code below to display the order of the cars
      // TEST debugPrintOrder();
      
      // The sorted list if cars is maintained in the trackbar components
      // The C++ code eroneously uses its map which is not sorted by location

      // Move all the cars
      final int nCount = getComponentCount();
      for( int index=0; index<nCount; index++ )
      {
         Component component = (Component)getComponent( index );
         if (component instanceof TrackCar)
         {
            TrackCar curCar = (TrackCar)component;
            curCar.resizeBasedOnPresentationElement(false);
         }
      }

      // Resize all the couplings
      for( int index=0; index<nCount; index++ )
      {
         Component component = (Component)getComponent( index );
         if (component instanceof TrackCoupling)
         {
            TrackCoupling curCoupling = (TrackCoupling)component;
            curCoupling.resizeToFitNeighbors();
         }
      }
   }
   
   // print out all the cars and couplings along with their x locations
   private void debugPrintOrder()
   {
      ETSystem.out.println("Begin Track Location Trace..." );

      final int nCount = getComponentCount();
      for( int index=0; index<nCount; index++ )
      {
         Component component = (Component)getComponent( index );
         if (component instanceof TrackCar)
         {
            TrackCar car = (TrackCar)component;

            String strThisCaption = car.getName();
            
            Rectangle windRect = car.getBounds();
   
            ETSystem.out.println( "Car '" + strThisCaption + "' at " + windRect.getX() + " to " + (windRect.getX() + windRect.getWidth()) );
         
            TrackCoupling coupling = (TrackCoupling)car.getPreviousItem();
            if( coupling != null )
            {
               windRect = coupling.getBounds();
   
               ETSystem.out.println( "Coupling at " + windRect.getX() + " to " + (windRect.getX() + windRect.getWidth()) );
            }
         }
      }

      ETSystem.out.println("End Track Location Trace" );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#onKeyDown(short, short)
    */
   public boolean onKeyDown(KeyEvent e)
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#onKeyUp(short, short)
    */
   public boolean onKeyUp(KeyEvent e)
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.trackbar.ITrackBar#postLayoutSequenceDiagram()
    */
   public void postLayoutSequenceDiagram() 
   {
      HashMap < String, TrackCar > oldTrackCars = new HashMap <String, TrackCar>(m_TrackCars);
      this.clear();
      
      m_TrackCars.clear();
      
      IDiagram diagram = getDiagram();
      
      if(diagram != null)
      {
         IUIDiagram uiDiagram = null;
         if(diagram instanceof IUIDiagram)
            uiDiagram = (IUIDiagram)diagram;
         
         IDrawingAreaControl da = null;
         
         if(uiDiagram != null)
         {
            da = uiDiagram.getDrawingArea();
            
            ETList<IPresentationElement> presentationElements = diagram.getAllItems();
           
            ETList<IPresentationElement> sortedPresentationElements = null;
            if(da != null)
               sortedPresentationElements = GetHelper.sortNodesLeftToRight(presentationElements);
               
            if(sortedPresentationElements != null)
            {
               for (Iterator iter = sortedPresentationElements.iterator(); iter.hasNext();)
               {
                  IPresentationElement pe = (IPresentationElement)iter.next();
               }
               int count = sortedPresentationElements.getCount();
               for(int index = 0; index<count; index++)
               {
                  IPresentationElement presentationElement  = sortedPresentationElements.item(index);
                  IPresentationElement previousPresentationElement = null;
                  
                  if(index > 0)
                     previousPresentationElement = sortedPresentationElements.item(index-1);
                  
                  INodePresentation nodePE = null;
                  if(presentationElement instanceof INodePresentation)
                     nodePE = (INodePresentation)presentationElement;
                     
                  INodePresentation previousNodePE = null;
                  if(previousPresentationElement instanceof INodePresentation)
                     previousNodePE = (INodePresentation)previousPresentationElement;
                     
                  if(nodePE != null)
                  {
                     if(previousNodePE == null)
                     {
                        addPresentationElement(presentationElement);                    
                     }
                     else
                     {
                        IETRect nodeLocation = nodePE.getLocation();
                        IETRect previousLocation = previousNodePE.getLocation();
                        
                        if(nodeLocation.getCenterX() < previousLocation.getCenterX())
                        {
                           int moveToFlags = MoveToFlags.MTF_MOVEX | MoveToFlags.MTF_LOGICALCOORD | 
                              MoveToFlags.MTF_INVALIDATE;
                           double newX = previousLocation.getCenterX() + previousLocation.getWidth()/2 + 5;
                           nodePE.moveTo((int)newX, 0, moveToFlags);
                        }
                        addPresentationElement(presentationElement);
                     }
                  }
               }
            }
         }
      }
   }

   /* (non-Javadoc)
    */
   public void removePresentationElements(ETList < IPresentationElement > pPresentationElements)
   {
      for (Iterator < IPresentationElement > iter = pPresentationElements.iterator(); iter.hasNext();)
      {
         removePresentationElement(iter.next());
      }
   }

   public boolean moveObjects(ETList < IPresentationElement > pPresentationElements, long lDelta)
   {
      boolean retVal = false;

      for (Iterator < IPresentationElement > iter = pPresentationElements.iterator(); iter.hasNext();)
      {
         TrackCar curCar = getTrackCar(iter.next());
         if (curCar != null)
         {
            updateCarLocation(curCar);
            retVal = true;
         }

      }

      return retVal;
   }

   protected void updateCarLocation(TrackCar car)
   {
      TrackItem prevBeforeItem = car.getPreviousItem();
      TrackItem nextBeforeItem = car.getNextItem();

      TrackCoupling removeCoupling = removeCar(car);
      if(removeCoupling != null)
      {
         remove(removeCoupling);
      }
       
      addCar(car);

      TrackItem prevAfterItem = car.getPreviousItem();
      TrackItem nextAfterItem = car.getNextItem();

      if ((prevBeforeItem != null) && (prevAfterItem != null) && (prevBeforeItem.isBetweenSameNeighbors(prevAfterItem) == true))
      {
         prevAfterItem.copyAttributes(prevBeforeItem);
      }
      else if ((prevBeforeItem != null) && (nextAfterItem != null) && (prevBeforeItem.isBetweenSameNeighbors(nextAfterItem) == true))
      {
         nextAfterItem.copyAttributes(prevBeforeItem);
      }

      if ((nextBeforeItem != null) && (prevAfterItem != null) && (nextBeforeItem.isBetweenSameNeighbors(prevAfterItem) == true))
      {
         prevAfterItem.copyAttributes(prevBeforeItem);
      }
      else if ((nextBeforeItem != null) && (nextAfterItem != null) && (nextBeforeItem.isBetweenSameNeighbors(nextAfterItem) == true))
      {
         nextAfterItem.copyAttributes(nextBeforeItem);
      }

      if (removeCoupling != null)
      {
         remove(removeCoupling);
      }
   }

   public void clear()
   {
      removeAll();
      m_TrackCars.clear();
   }
   
   public TrackCoupling removeCar(TrackCar car)
   {
      TrackCoupling retVal = null;

      if (car != null)
      {
         TrackItem prevItem = car.getPreviousItem();
         TrackItem nextItem = car.getNextItem();

         if (prevItem == null)
         {
            if (nextItem != null)
            {
               if (nextItem instanceof TrackCoupling)
               {
                  retVal = (TrackCoupling)nextItem;

               }
               nextItem = nextItem.getNextItem();
            }
         }
         else
         {
            if (prevItem instanceof TrackCoupling)
            {
               retVal = (TrackCoupling)prevItem;
            }
            prevItem = prevItem.getPreviousItem();
         }

         // Update the previous car's next car
         if (prevItem != null)
         {
            prevItem.setNextItem(nextItem);
         }

         // Update the next car's next car
         if (nextItem != null)
         {
            nextItem.setPreviousItem(prevItem);
         }

         car.setPreviousItem(null);
         car.setNextItem(null);

         m_TrackCars.remove(car.getXMIID());
         remove(car);

         layoutTrack();
      }

      return retVal;
   }

   /* (non-Javadoc)
    * @see java.awt.Component#getPreferredSize()
    */
   public Dimension getPreferredSize()
   {
      //Dimension retVal = super.getPreferredSize();

      Dimension value = null;
      
      FontMetrics metrics = getFontMetrics(getFont());
      int height = metrics.getHeight() + metrics.getDescent() + (3 * INSET);

      IDiagramEngine engine = getDiagramEngine();
      if(engine != null)
      {
         IDrawingAreaControl control = engine.getDrawingArea();
         if(control != null)
         {
            TSEGraphWindow window = control.getGraphWindow();
            
            if(window != null)
            {
               value = window.getPreferredSize();
            }
         }
      }
      
      Dimension retVal = null;
      if(value != null)
      {
         retVal = new Dimension(value.width, height);
      }
      else
      {
         retVal = new Dimension(0, height);
      }
      
      return retVal;
   }
   
   public void paintComponent(Graphics g)
   {
      Color curColor = g.getColor();
      g.setColor(getBackground());

      g.fillRect(0, 0, getWidth(), getHeight());

      g.setColor(curColor);
   }

   //**************************************************
   // Helper Methods
   //**************************************************

   public void addCar(TrackCar car)
   {
      addCar(car, true);
   }

   /**
    * @param car
    */
   public void addCar(TrackCar car, boolean isNewCar)
   {
      car.resizeBasedOnPresentationElement(false);

      if (isNewCar == false)
      {
         remove(car);
      }

      if (getComponentCount() == 0)
      {
         add(car);
      }
      else
      {
         int index = findPreviousItem(car);
         insertCar(car, index);
      }

      if (isNewCar == true)
      {
         m_TrackCars.put(car.getXMIID(), car);
      }

      repaint();
      
      reportCars();
   }

   protected void reportCars()
   {
//      ETSystem.out.println("****************************************************");
//      ETSystem.out.println("********************** Cars ************************");
//      
//      Component[] cars = getComponents();
//      for (int index = 0; index < cars.length; index++)
//      {
//         ETSystem.out.println(cars[index].getClass().getName());
//      }
//      
//      ETSystem.out.println("****************************************************");
   }
   
   /**
    * @param car
    * @param index
    */
   protected void insertCar(TrackCar car, int index)
   {
      add(car, index);
      int maxItems = getComponentCount();

      TrackItem nextItem = null;
      if ((index + 1) < maxItems)
      {
         nextItem = (TrackItem)getComponent(index + 1);
      }

      TrackItem prevItem = null;
      if ((index - 1) >= 0)
      {
         prevItem = (TrackItem)getComponent(index - 1);
      }

      if (nextItem != null)
      {
         if (nextItem instanceof TrackCoupling)
         {
            TrackCoupling coupling = (TrackCoupling)nextItem;
            coupling.setPreviousItem(car);            
            car.setNextItem(coupling);
//            coupling.setNextItem(car);            
//            car.setPreviousItem(coupling);
            
            coupling.resizeToFitNeighbors(car);
         }
         else
         {
            TrackCoupling spacer = new TrackCoupling(this, car, nextItem);
            car.setNextItem(spacer);
            nextItem.setPreviousItem(spacer);
            
            spacer.resizeToFitNeighbors(car);
            add(spacer, index + 1);
         }
      }

      if (prevItem != null)
      {
         if (prevItem instanceof TrackCoupling)
         {
            TrackCoupling coupling = (TrackCoupling)prevItem;
            coupling.setNextItem(car);            
            car.setPreviousItem(coupling);
//            coupling.setPreviousItem(car);            
//            car.setNextItem(coupling);
            
            coupling.resizeToFitNeighbors(car);
         }
         else
         {
            TrackCoupling spacer = new TrackCoupling(this, prevItem, car);
            car.setPreviousItem(spacer);
            prevItem.setNextItem(spacer);
            
            spacer.resizeToFitNeighbors(car);  
            ETSystem.out.println(spacer.getBounds());          
            add(spacer, index);
         }
      }
      repaint();
   }

   /**
    * @param car
    * @return
    */
   protected int findPreviousItem(TrackCar car)
   {
      int retVal = 0;
      
      final int iCarCenter = car.getX() + car.getWidth()/2;

      Component[] componentList = getComponents();
      for (int index = 0; index < componentList.length; index++)
      {
         Component component = componentList[index];

         final int iTestCenter = component.getX() + component.getWidth()/2;
         if ( iTestCenter < iCarCenter )
         {
            retVal = index + 1;
         }
         else
         {
            break;
         }
      }
      return retVal;
   }

   /**
    * @return
    */
   public IDiagramEngine getDiagramEngine()
   {
      return m_DiagramEngine;
   }

   /**
    * @param engine
    */
   public void setDiagramEngine(IDiagramEngine engine)
   {
      m_DiagramEngine = engine;
   }

   protected TrackCar getTrackCar(IPresentationElement element)
   {
      TrackCar retVal = null;

      if (element != null)
      {
         retVal = getTrackCar(element.getXMIID());
      }

      return retVal;
   }

   protected TrackCar getTrackCar(String id)
   {
      TrackCar retVal = null;

      if ((id != null) && (id.length() > 0))
      {
         retVal = m_TrackCars.get(id);
      }

      return retVal;
   }

   protected void showContextMenu(MouseEvent e)
   {
   }

   ////////////////////////////////////////////////////////////////////////////
   // Focus Chagne Listener Methods
   
    public void focusGained(FocusEvent e)
    {
        if(getComponentCount() > 0)
        {
            getComponent(0).requestFocus();
        }
    }

    public void focusLost(FocusEvent e)
    {
        // Nothing to do.
    }

   public class TrackBarMouseListener extends MouseInputAdapter
   {
      /* (non-Javadoc)
       * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
       */
      public void mousePressed(MouseEvent e)
      {
         if (e.isPopupTrigger() == true)
         {
            showContextMenu(e);
         }
      }

      /* (non-Javadoc)
      * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
      */
      public void mouseReleased(MouseEvent e)
      {
         if (e.isPopupTrigger() == true)
         {
            showContextMenu(e);
         }
      }
   }


    /////////////
    // Accessible
    /////////////

    AccessibleContext accessibleContext;
    
    public AccessibleContext getAccessibleContext() {
	if (accessibleContext == null) {
	    accessibleContext = new AccessibleJTrackBar();
	}
	return accessibleContext;
    }
    
    
    public class AccessibleJTrackBar extends AccessibleJPanel {	

	public String getAccessibleName(){
	    return TrackBarResource.getString("ACSN_TrackBar");
	}

	public String getAccessibleDescription(){
	    String diagramName = null;
	    if (getDiagramEngine() != null && getDiagramEngine().getDrawingArea() != null) {
		diagramName = getDiagramEngine().getDrawingArea().getName();
	    }
	    return MessageFormat.format(TrackBarResource.getString("ACSD_TrackBar"), diagramName);
	}

    }


}
