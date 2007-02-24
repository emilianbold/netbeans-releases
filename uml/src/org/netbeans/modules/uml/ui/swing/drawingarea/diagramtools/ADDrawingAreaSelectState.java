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


package org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.SwingUtilities;

import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.tool.TSESelectRegionTool;
import com.tomsawyer.editor.tool.TSESelectTool;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.SetCursorEvent;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETGraph;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import com.tomsawyer.editor.TSEHitTesting;
import com.tomsawyer.editor.TSEInteractiveConstants;

//public class ADDrawingAreaSelectState extends TSESelectState
public class ADDrawingAreaSelectState extends TSESelectTool
{

   private IETGraphObjectUI dropSourceUI;
   private boolean isMouseDragged;
   private IPresentationElement m_SelectedPresElement = null;
   private ICompartment m_SelectedCompartment = null;
   private IDrawingAreaControl m_SelectedDia = null;

   public ADDrawingAreaSelectState()
   {
      super();
   }

   protected IDrawingAreaControl getDrawingArea()
   {
      return ((ADGraphWindow)this.getGraphWindow()).getDrawingArea();
   }

   /**
    * This method is called when the user "drops" the object onto the drop target.
    */
   public void onDrop(DropTargetDropEvent pEvent)
   {  
      TSEObject obj = null;
      if(pEvent.getLocation() != null)
      {
         Point dropPoint = pEvent.getLocation();
         TSConstPoint pt = new TSConstPoint(getGraphWindow().getNonalignedWorldPoint(dropPoint));
//         obj = getObjectAt(pt);
         obj = this.getHitTesting().getGraphObjectAt(pt, this.getGraph(), true);
      }
      
      super.onDrop(pEvent);
      //getDrawingArea().onDrop(pEvent, obj, m_DropPoint);
      getDrawingArea().onDrop(pEvent, obj);
   }

   
   
//   public void onDragEnter(DropTargetDragEvent  event)
//   {
//      m_DropPoint = null;
//   }
//   
//   public void onDragExit(DropTargetDragEvent  event)
//   {
//      m_DropPoint = null;
//   }
//   
//   public void onDragOver(DropTargetDragEvent event)
//   {
//      m_DropPoint = event.getLocation();
//   }
   
   /**
    * This method is invoked when a mouse button is released on the graph window canvas
    */
   public void onMouseReleased(MouseEvent pEvent)
   {
      boolean eventHandled = false;

      IDrawingAreaControl drawingArea = getDrawingArea();
      IETGraphObjectUI objectUI = getUI(pEvent);
      if (objectUI != null && isLeftMouseEvent(pEvent))
      {
         if (this.dropSourceUI != null && this.isMouseDragged)
         {
            IDrawEngine engine = objectUI.getDrawEngine();

            if (engine != null)
            {
               if (pEvent.isControlDown())
               {
                  eventHandled = engine.handleLeftMouseDrop(getETPoint(pEvent), dropSourceUI.getDrawEngine().getSelectedCompartments(), false);
               }
               else
               {
                  eventHandled = engine.handleLeftMouseDrop(getETPoint(pEvent), dropSourceUI.getDrawEngine().getSelectedCompartments(), true);
               }
            }

            drawingArea.refresh(true);

            if (eventHandled)
            {
               dropSourceUI = null;
            }
         }
      }
      super.onMouseReleased(pEvent);
      this.isMouseDragged = false;
   }

   protected boolean isLeftMouseEvent(MouseEvent pEvent)
   {
      return SwingUtilities.isLeftMouseButton(pEvent);
   }

   protected boolean isRightMouseEvent(MouseEvent pEvent)
   {
      //Catch any button that is not the left button
      return !isLeftMouseEvent(pEvent);
   }

   /**
    * This method is invoked when the mouse is being dragged
    */
   public void onMouseDragged(MouseEvent pEvent)
   {      
      
      // Fix J1075:  TS will sometimes throw here with a null pointer exception
      //             This was seen when moving the combined fragment interaction constraint labels.
      //             This is also what causes the label to not be drawn while the user is moving it.
      //             see also ADMoveSelectedState.onMouseDragged(), and
      //                      ETGenericNodeLabelUI.drawCalloutOutline()
      try
      {
         boolean eventHandled = false;

         IDrawingAreaControl drawingArea = getDrawingArea();
         
         // Fix J2803:  Elements are not allowed to be dragged when the diagram is read only.
         if( (drawingArea != null) &&
             (!drawingArea.getReadOnly()) )
         {
            IETPoint pt = getETPoint(pEvent);
            IETGraphObjectUI objectUI = getUI(pEvent);

            if (objectUI != null)
            {
               IDrawEngine engine = objectUI.getDrawEngine();
               if (engine != null)
               {
                  eventHandled = engine.handleLeftMouseDrag(pt, pt);
               }
               drawingArea.refreshRect(new ETRectEx(objectUI.getOwner().getBounds()));
            }

            // fire the event once per drag.
            if (!this.isMouseDragged)
            {
               // We don't consider drags on graph window only if we hit an object 
               this.isMouseDragged = objectUI != null;
               // if (objectUI != null) { 
                   if (checkIfNeedToFireEvent(objectUI)) { // objectUI could be null if mouse move fast 
                       drawingArea.fireSelectEvent(getSelectedObjects());
                   }
               // }
            }

            if (this.dropSourceUI != null && !eventHandled)
            {
               this.setCursor(pEvent.isControlDown() ? DragSource.DefaultCopyDrop : DragSource.DefaultMoveDrop);
            }
            else if (!eventHandled)
            {
               this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }

            if (!eventHandled)
            {
               super.onMouseDragged(pEvent);
            }
         }
      }
      catch (NullPointerException e)
      {
         // ETSystem.out.println("NullPointerException in ADDrawingAreaSelectState.onMouseDragged");
      }
   }

   /**
    * This method is invoked when the mouse cursor is being moved
    * In C++ this event comes via the call in CGenericNode::onSetCursor()
    */
   public void onMouseMoved(MouseEvent event)
   {
      this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

      // Let the super go first. It might activate a child state.
      super.onMouseMoved(event);

      IETGraphObjectUI objectUI = getUI(event);

      if (objectUI != null) {
          //JM: Fix for #6364120
          TSEHitTesting hitTesting = this.getGraphWindow().getHitTesting();
          TSConstPoint point = this.getNonalignedWorldPoint(event);
          int grapple = hitTesting.getGrappleAt(point, this.getGraph(), true);
          
          // verify that the grapple is valid
          if (grapple != TSEInteractiveConstants.GRAPPLE_INVALID) {
//              ErrorManager.getDefault().log(ErrorManager.WARNING, "Grapple is not invalid... so DO nothing.. do not change the cursor");
          }
          
          else {              
              //end JM
              
              this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
              
              IDrawEngine engine = objectUI.getDrawEngine();
              
              if (engine != null) {
                  engine.handleSetCursor(new SetCursorEvent(event, this));
              }
          }
      }
   }

   /*
    * Returns the select graph Objects.
    */
   public ETList < TSGraphObject > getSelectedObjects()
   {
      ETGraph graph = (ETGraph)this.getGraphWindow().getGraph();
      return graph != null ? graph.getSelectedObjects(false, false) : null;
   }

   /**
    * This method is invoked when the mouse button is clicked on the graph window canvas.
    */
   public void onMouseClicked(MouseEvent pEvent)
   {
      boolean eventHandled = false;

      IDrawingAreaControl drawingArea = getDrawingArea();

      IETGraphObjectUI objectUI = getUI(pEvent);

      //Debug Code
      TSEObject debugObj = getObject(pEvent);
      if (debugObj != null)
      {/* JM
         System.err.println("Printing tag: " + debugObj.getTag());
         System.err.println("Printing text: " + debugObj.getText());
         System.err.println("Printing bounds: " + debugObj.getBounds());
     */
      }
      //End of debug Code

      if (objectUI != null)
      {
         IDrawEngine engine = objectUI.getDrawEngine();
         if (isLeftMouseEvent(pEvent) && engine != null)
         {
            eventHandled = pEvent.getClickCount() == 2 ? engine.handleLeftMouseButtonDoubleClick(pEvent) : engine.handleLeftMouseButton(pEvent);
         }
      }

      if (!eventHandled)
      {
         super.onMouseClicked(pEvent);
      }

      drawingArea.onGraphEvent(IGraphEventKind.GEK_POST_SELECT, null, null, null);
      drawingArea.refresh(true);

      if (checkIfNeedToFireEvent(objectUI))
      {
         drawingArea.fireSelectEvent(getSelectedObjects());
      }
   }

   private boolean checkIfNeedToFireEvent(IETGraphObjectUI ui)
   {
      return true;
   }

   /**
    * This method is invoked when a mouse button is pressed on the graph window canvas.
    */
   public void onMousePressed(MouseEvent pEvent)
   {
      boolean eventHandled = false;

      ADGraphWindow window = (ADGraphWindow)getGraphWindow();
      IDrawingAreaControl drawingArea = window.getDrawingArea();
      IETGraphObjectUI objectUI = getUI(pEvent);

      boolean isCtrlDown = pEvent.isControlDown();
      boolean isShiftDown = pEvent.isShiftDown();

      if (objectUI != null && isLeftMouseEvent(pEvent))
      {
         IDrawEngine engine = objectUI.getDrawEngine();

         if (engine != null)
         {
            eventHandled = engine.handleLeftMouseBeginDrag(getETPoint(pEvent), getETPoint(pEvent));
         }

         if (objectUI.getTSObject().isNode())
         {
            this.dropSourceUI = eventHandled ? objectUI : null;
         }

         if (engine != null && !eventHandled)
         {
            eventHandled = engine.handleLeftMouseButtonPressed(pEvent);
         }

         if (eventHandled)
         {
            if (!isCtrlDown && !isShiftDown)
            {
               window.deselectAll(true);
            }

            ITSGraphObject grObj = objectUI.getTSObject();
            if (!grObj.isSelected())
            {
               window.selectObject(grObj.getObject(), true);
            }
         }
      }
      // Need different implementation for the right mouse button because of the way context menus work
      else if (objectUI != null && isRightMouseEvent(pEvent))
      {
         IDrawEngine engine = objectUI.getDrawEngine();

         if (engine != null)
         {

            ITSGraphObject grObj = objectUI.getTSObject();
            ETList < TSGraphObject > selectedObjects = getSelectedObjects();

            if (selectedObjects != null)
            {
               if (selectedObjects.size() == 1)
               {
                  window.deselectAll(true);
                  window.selectObject(grObj.getObject(), true);
               }
               else if (selectedObjects.size() > 1)
               {
                  if (!grObj.isSelected())
                  {
                     window.deselectAll(true);
                     window.selectObject(grObj.getObject(), true);
                  }
               }
            }
            else if (!grObj.isSelected())
            {
               window.selectObject(grObj.getObject(), true);
            }

            eventHandled = engine.handleRightMouseButton(pEvent);

            drawingArea.onGraphEvent(IGraphEventKind.GEK_POST_SELECT, null, null, null);
            drawingArea.refresh(true);

            selectedObjects = getSelectedObjects();
            if (selectedObjects != null && selectedObjects.size() > 0)
            {
               drawingArea.fireSelectEvent(selectedObjects);
            }
         }
      }
      else if (objectUI == null && isRightMouseEvent(pEvent))
      {
         window.deselectAll(true);
         drawingArea.onGraphEvent(IGraphEventKind.GEK_POST_SELECT, null, null, null);
         drawingArea.refresh(true);
         eventHandled = true;
      }

      if (!eventHandled)
      {
         super.onMousePressed(pEvent);
      }
   }

   /*
    * Returns the object under the point.
    */
   protected TSEObject getObject(TSConstPoint worldPoint)
   {
//      return this.getObjectAt(worldPoint, null, this.getGraphWindow().getGraph());
       return this.getHitTesting().getGraphObjectAt(worldPoint, this.getGraphWindow().getGraph(), true );
   }

   /*
    * Returns the object under the event.
    */
   protected TSEObject getObject(MouseEvent pEvent)
   {
       TSEObject retVal = null;
       
       TSEHitTesting hitTest = new TSEHitTesting(getGraphWindow());
       
       TSConstPoint pt = getNonalignedWorldPoint(pEvent);
              
       retVal = hitTest.getEdgeAt(pt, getGraphWindow().getGraph(), true);
       if(retVal == null)
       {
           retVal = hitTest.getNodeLabelAt(pt, getGraphWindow().getGraph(), true);
           if(retVal == null)
           {
               retVal = hitTest.getEdgeLabelAt(pt, getGraphWindow().getGraph(), true);
               if(retVal == null)
               {
                   retVal = hitTest.getConnectorAt(pt, getGraphWindow().getGraph(), true);
                   if(retVal == null)
                   {
                       retVal = hitTest.getConnectorLabelAt(pt, getGraphWindow().getGraph(), true);
                       if(retVal == null)
                       {
                           // If we perform the node it test before we test the other 
                           // graph elements, the node will always win if the nodes surronds
                           // other graph nodes.  By checking for the node last we are 
                           // basically saying that the node is behind the other graph
                           // elements.
                           retVal = hitTest.getNodeAt(pt, getGraphWindow().getGraph(), true);
                       }
                   }
               }
           }
       } 
       
       return retVal;
   }

   /*
    * Converts from and interface pointer to representing graph object.
    */
   protected TSEObject getObject(IETGraphObjectUI objectUI)
   {
      return objectUI != null ? (TSEObject)objectUI.getTSObject() : null;
   }

   /*
    * Returns the UI under the mouse event.
    */
   protected IETGraphObjectUI getUI(MouseEvent pEvent)
   {
      TSEObject object = getObject(pEvent);
      return (object != null && object.getUI() instanceof IETGraphObjectUI) ? (IETGraphObjectUI)object.getUI() : null;
   }

   /*
    * Returns the UI under the logical world point.
    */
   protected IETGraphObjectUI getUI(TSConstPoint worldPoint)
   {
      TSEObject object = getObject(worldPoint);
      return (object != null && object.getUI() instanceof IETGraphObjectUI) ? (IETGraphObjectUI)object.getUI() : null;
   }

   /*
    * Returns the Device point for the mouse event.
    */
   protected IETPoint getETPoint(MouseEvent pEvent)
   {
      Point mousePos = pEvent.getPoint();
      return new ETPoint(mousePos.x, mousePos.y);
   }

   public void moveSelected(TSConstPoint pStartPoint)
   {
      IETGraphObjectUI objectUI = this.getUI(pStartPoint);
      if (objectUI != null && objectUI.getTSObject() != null && !objectUI.getTSObject().isSelected())
      {
         objectUI.getTSObject().setSelected(true);
      }

      ETList < IETGraphObject > selectedNodes = new ETArrayList < IETGraphObject > ();
      List selected = getGraph().selectedNodes();
      if (selected != null)
      {
         selectedNodes.addAll(selected);
         Point devicePoint = this.getGraphWindow().getTransform().pointToDevice(pStartPoint);

         this.getDrawingArea().onGraphEvent(IGraphEventKind.GEK_PRE_MOVE, new ETPoint(devicePoint), new ETPoint(devicePoint), selectedNodes);
      }

      //this.setState(new ADMoveSelectedState(this, pStartPoint, false));
      this.setTool(new ADMoveSelectedState(this, pStartPoint, false));
   }

   public void resizeGraphObject(TSConstPoint pStartPoint)
   {
      this.isMouseDragged = false;
      //this.setState(new ADResizeGraphObjectState(this, pStartPoint, false));
      this.setTool(new ADResizeGraphObjectState(this, pStartPoint, false));
   }

   /*
    * Called when the user releases the mouse after a marguee rectangle has finished changing the selection.
    */
   protected void onMarqueeSelectNotify()
   {
      // Marquee selection notify.
      getDrawingArea().fireSelectEvent(getSelectedObjects());
   }

   /*
    * 
    * @author KevinM
    *  This class handles when the user draws a retectangle around objects to select them.
    *	
    */
//   class ETSelectRegionState extends TSESelectRegionState
   class ETSelectRegionState extends TSESelectRegionTool
   {
      public ETSelectRegionState(TSConstPoint startPoint)
      {
         super(ADDrawingAreaSelectState.this, startPoint, ADDrawingAreaSelectState.this.getGraphWindow().getGraph());
      }

      public void onMouseReleased(MouseEvent pEvent)
      {
         super.onMouseReleased(pEvent);
         // Marquee selection notify.
         ADDrawingAreaSelectState.this.onMarqueeSelectNotify();
      }
   }

   /*
    *  (non-Javadoc)
    * @see com.tomsawyer.editor.state.TSESelectState#selectRegion(com.tomsawyer.util.TSConstPoint)
    */
   public void selectRegion(TSConstPoint startPoint)
   {
      //this.setState(new ETSelectRegionState(startPoint));
       this.setTool(new ETSelectRegionState(startPoint));
   }

}
