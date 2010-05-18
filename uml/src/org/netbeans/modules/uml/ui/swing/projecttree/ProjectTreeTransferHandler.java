/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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


/*
 *
 * Created on Jun 25, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.swing.projecttree;

import java.awt.Container;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.event.MouseInputAdapter;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeDragVerifyImpl;
import org.netbeans.modules.uml.ui.support.ADTransferable;

/**
 * The TransferHandler used to support project tree drag and drop functionality.
 * The class ADTransferable is used to populate a ADTransferable.ADTransferData
 * Transferable object.  The Transferable object can then be used to retrieve
 * the project tree items that are being dragged.
 * 
 * To research the Java Drag and Drop functionallity check out the 
 * <a href="http://java.sun.com/docs/books/tutorial/uiswing/misc/dnd.html"> Drag
 * and Drop Tutorial</a>.
 * 
 * @see ADTransferable
 * @author Trey Spiva
 */
public class ProjectTreeTransferHandler extends TransferHandler 
  implements FileExtensions
{
   private ProjectTreeMouseHandler m_DragMoveListener = new ProjectTreeMouseHandler();
   private int                     m_MoveAction       = DnDConstants.ACTION_NONE;
   
   public ProjectTreeTransferHandler()
   {
   }
   /** 
    * Overridden to check for the presence of a Describe data flavor.
    * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
    */
   public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
   {
      boolean retVal = false;
      
      if(comp != null)
      {
         for (int index = 0; index < transferFlavors.length; index++)
         {
            if(transferFlavors[index].equals(ADTransferable.ADDataFlavor) == true)
            {
               retVal = true;
            }
            else if(transferFlavors[index].equals(DataFlavor.stringFlavor) == true)
            {
               retVal = true;
            }   
         }
      }
      else
      {
      }
      
      return retVal; 
   }

   /* (non-Javadoc)
    * @see javax.swing.TransferHandler#exportAsDrag(javax.swing.JComponent, java.awt.event.InputEvent, int)
    */
   public void exportAsDrag(JComponent comp, InputEvent e, int action)
   {
      m_MoveAction = action;
      
      super.exportAsDrag(comp, e, action);
      
      JProjectTree tree = getProjectTree(comp);
      if (tree != null)
      {
         tree.resetDragState();
      }
   }

   /* (non-Javadoc)
    * @see javax.swing.TransferHandler#exportDone(javax.swing.JComponent, java.awt.datatransfer.Transferable, int)
    */
   protected void exportDone(JComponent source, Transferable data, int action)
   {
      JProjectTree tree = getProjectTree(source);
      if (tree != null)
      {
         m_DragMoveListener.setDataObject(null);
         tree.removeMouseMotionListener(m_DragMoveListener);         
      }
      super.exportDone(source, data, action);
   }

   /* (non-Javadoc)
    * @see javax.swing.TransferHandler#exportToClipboard(javax.swing.JComponent, java.awt.datatransfer.Clipboard, int)
    */
   public void exportToClipboard(JComponent comp, Clipboard clip, int action)
   {
      // TODO Auto-generated method stub
      super.exportToClipboard(comp, clip, action);
   }

   /* (non-Javadoc)
    * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
    */
   public boolean importData(JComponent comp, Transferable t)
   {
      boolean retVal = false;
      
      JProjectTree tree = getProjectTree(comp);
      if (tree != null)
      {
         retVal = tree.fireEndDrag(t, m_MoveAction);   
      }
      
      return retVal; 
   }

   /* (non-Javadoc)
    * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
    */
   public int getSourceActions(JComponent c)
   {
      return COPY_OR_MOVE;
   }

   /* (non-Javadoc)
    * @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent)
    */
   protected Transferable createTransferable(JComponent c)
   {
      Transferable retVal = null;      
      
      JProjectTree tree = getProjectTree(c);
      if (tree != null)
      {
         if(tree.fireBeginDrag(tree.getSelected()) == true)
         {
            // Create an XML Document to hold the information for
            // what is being dragged.
            retVal = populateDragSource(tree);
         }
      }
      return retVal;
   }
   
   protected JProjectTree getProjectTree(Container c)
   {
      JProjectTree retVal = null;
      
      Container parent = c.getParent();
      if(parent instanceof JProjectTree)
      {
         retVal = (JProjectTree)parent;
      }
      else if(parent != null)
      {
         retVal = getProjectTree(parent);
      }
      
      return retVal;
   }
   
   /**
    * Creates and populates a Transferable object with selected items from the tree
    *
    * @return The transferable object to continue the drag and drop operation.
    */
   private Transferable populateDragSource(JProjectTree tree)
   {
      ADTransferable retVal = null;
      
      IProjectTreeItem[] selItems = tree.getSelected();
      if((selItems != null) && (selItems.length > 0))
      {
         retVal = new ADTransferable("DRAGGEDITEMS");
         for (int index = 0; index < selItems.length; index++)
         {
            if((selItems[index].isModelElement() == true) &&
               (selItems[index].isProject() == false))
            {
               IElement modelElement = selItems[index].getModelElement();
               retVal.addModelElement(modelElement);
            }
            else if(selItems[index].isProject() == false)
            {
               String desc = selItems[index].getDescription();
               if(StringUtilities.hasExtension(desc, DIAGRAM_LAYOUT_EXT) == true)
               {
                  retVal.addDiagramLocation(desc);
               }
               else if(desc.length() > 0)
               {
                  retVal.addGenericElement(desc);
               }
            }
         }
         
      }      
      
      if(retVal != null)
      {
         m_DragMoveListener.setDataObject(retVal);
         tree.addMouseMotionListener(m_DragMoveListener);
      }
      
      return retVal;
   }
      
   public class ProjectTreeMouseHandler extends MouseInputAdapter
   {
      private Transferable m_DataObject = null;
      
      public void mouseDragged(MouseEvent e) 
      {
         JProjectTree tree = getProjectTree((Container)e.getComponent());
         if(tree != null)
         {            
            // Tell the project tree engines the we've moved into a region.
            // The engine is responsible for telling us if it is ok to drop
            // the item.
            
            IProjectTreeDragVerify verify = new ProjectTreeDragVerifyImpl();
            
            if(tree.fireMoveDrag(getDataObject(), verify) == true)
            {
               // Do Something with the drop effect.
            }
         }         
       }
      
      /**
       * Retrieve the Transferable object that specifies the data that is 
       * being dragged.
       * 
       * @return The tranfer data.
       */
      public Transferable getDataObject()
      {
         return m_DataObject;
      }

      /**
       * Retrieve the Transferable object that specifies the data that is 
       * being dragged.
       * 
       * @parm data The tranfer data.
       */
      public void setDataObject(Transferable data)
      {
         m_DataObject = data;
      }
   }
}
