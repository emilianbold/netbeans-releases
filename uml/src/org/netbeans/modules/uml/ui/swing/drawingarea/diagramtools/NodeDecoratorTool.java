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
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETPointEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETCrossHairCursor;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETInvalidCrossHair;
import com.tomsawyer.editor.TSEObject;
//import com.tomsawyer.editor.TSEWindowInputState;
import com.tomsawyer.editor.TSEWindowInputTool;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;

/**
 * 
 * @author Trey Spiva
 */
//public class NodeDecoratorTool extends TSEWindowInputState
public class NodeDecoratorTool extends TSEWindowInputTool
{
   private Cursor m_DefaultCursor = null;
   private Cursor m_InvalidCrossHair = null;
   private String m_Type = "";

   public NodeDecoratorTool(String type)
   {
      m_DefaultCursor = ETCrossHairCursor.getCursor();
      m_InvalidCrossHair = ETInvalidCrossHair.getCursor();
      setDefaultCursor(m_DefaultCursor);

      setType(type);
   }
   /**
    * Retrieves the type of the decorator being added to a node.
    */
   public String getType()
   {
      return m_Type;
   }

   /**
    * Sets the type of the decorator being added to a node.
    */
   public void setType(String string)
   {
      m_Type = string;
   }

   public void onMousePressed(MouseEvent pEvent)
   {
      if ((pEvent.isPopupTrigger() == false) && (isLeftMouseEvent(pEvent) == true) && (getType().length() > 0))
      {
         IETNode hitNode = getObject(pEvent);
         if (hitNode != null)
         {
            IDrawEngine engine = TypeConversions.getDrawEngine(hitNode);
            if (engine instanceof INodeDrawEngine)
            {
               INodeDrawEngine nodeEngine = (INodeDrawEngine)engine;

               IETPoint worldPt = new ETPointEx(getAlignedWorldPoint(pEvent));
               nodeEngine.addDecoration(getType(), worldPt);
            }
         }
      }

      if (pEvent.isPopupTrigger() == true)
      {
         exitState();
      }
   }

   public void onMouseReleased(MouseEvent pEvent)
   {
      if (pEvent.isPopupTrigger() == true)
      {
         exitState();
      }
   }


   /**
    * This method is called when the mouse is moved over the window.
    * 
    * @see com.tomsawyer.editor.TSEWindowInputState#onMouseMoved(java.awt.event.MouseEvent)
    */
   public void onMouseMoved(MouseEvent event)
   {
       IETNode hitNode = getObject(event);
       if (hitNode != null)
       {
           setCursor(m_DefaultCursor);
       }
       else
       {
           setCursor(m_InvalidCrossHair);
       }
       
       //When the mouse moves over to the diagram editor, set the drawing diagram in focus to listen to key event.
       // Fixing issue 78400
       IDrawingAreaControl cntrl = getDrawingArea();
       boolean focused = cntrl.isFocused();
       
       if (!focused)
       {
           cntrl.setFocus();
       }
       
       // Fix J2202:  The super calls ADDrawinAReaSelectState.onMouseMoved(), which resets the cursor
       //             So, instead we don't call the super.
       // super.onMouseMoved(event);
   }

   //**************************************************
   // Helper Methods
   //**************************************************

   /*
    * Returns the drawingarea control.
    */
   protected IDrawingAreaControl getDrawingArea()
   {
       ADGraphWindow graphWindow = getGraphWindow() instanceof ADGraphWindow ? (ADGraphWindow) getGraphWindow() : null;
       return graphWindow != null ? graphWindow.getDrawingArea() : null;
   }
   /**
    * Test if the mouse event is for a left button event.
    * 
    * @param The mouse event that was recieved.
    * @return <code>true</code> if the mouse event is for a left mouse button.
    */
   protected boolean isLeftMouseEvent(MouseEvent pEvent)
   {
      return SwingUtilities.isLeftMouseButton(pEvent);
   }

   /**
    * Test if the mouse event is for a right button event.
    * 
    * @param The mouse event that was recieved.
    * @return <code>true</code> if the mouse event is for a left mouse button.
    */
   protected boolean isRightMouseEvent(MouseEvent pEvent)
   {
      return !isLeftMouseEvent(pEvent);
   }

   /*
    * Returns the node at the current mouse position.
    */
   protected IETNode getObject(MouseEvent pEvent)
   {
      return getObject(getNonalignedWorldPoint(pEvent));
   }

   /*
    * Returns the node at the point.
    */
   protected IETNode getObject(TSConstPoint worldPt)
   {
      IETNode retVal = null;

      TSEObject obj = getNodeAt(worldPt, null, getGraphWindow().getGraph());
      if (obj instanceof IETNode)
      {
         retVal = (IETNode)obj;
      }

      return retVal;
   }

   /**
    * Returns to the default state.  The default state will most likely be the
    * selection state.
    */
   protected void setDefaultState()
   {
      try
      {
         ((ADGraphWindow)this.getGraphWindow()).getDrawingArea().switchToDefaultState();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   /** 
    * Exists the current states and returns to the default state.
    */
   protected void exitState()
   {
      stopMouseInput();
      setType("");
      setDefaultState();
   }
}
