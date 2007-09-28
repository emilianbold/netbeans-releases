/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import com.tomsawyer.editor.TSEWindowInputTool;
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
