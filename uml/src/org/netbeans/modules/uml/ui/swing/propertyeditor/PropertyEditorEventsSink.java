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
 * Created on Jun 5, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.propertyeditor;

import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.FilteredItemManager;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.EventContextManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspacePreCreateEventPayload;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeHandled;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;

/**
 * @author sumitabhk
 *
 */
public class PropertyEditorEventsSink
   implements
//      IDrawingAreaSelectionEventsSink,
//      IDrawingAreaEventsSink,
      IElementModifiedEventsSink,
      IProjectTreeEventsSink,
      IElementLifeTimeEventsSink,
      IClassifierTransformEventsSink,
//      ICompartmentEventsSink,
      IWorkspaceEventsSink,
      IAttributeEventsSink,
      IOperationEventsSink,
      IWSProjectEventsSink,
      IPreferenceManagerEventsSink,
      ICoreProductInitEventsSink
{

   protected PropertyEditor m_PropertyEditor = null;

   /**
    * 
    */
   public PropertyEditorEventsSink(PropertyEditor pPropertyEditor)
   {
      super();
      m_PropertyEditor = (PropertyEditor)pPropertyEditor;
   }

   public PropertyEditorEventsSink()
   {
      super();
   }

   public void setPropertyEditor(IPropertyEditor pPropertyEditor)
   {
      m_PropertyEditor = (PropertyEditor)pPropertyEditor;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSelectionEventsSink#onSelect(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement[], org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   //TODO
   public void onSelect(final IDiagram pParentDiagram, final ETList < IPresentationElement > selectedItems,/* final ICompartment pCompartment,*/ IResultCell cell)
   {
      try
      {
         if (m_PropertyEditor != null)
         {
            SwingUtilities.invokeLater(new Runnable()
            {
               public void run()
               {
//                  if (pCompartment == null)
//                  {
//                     m_PropertyEditor.onDrawingAreaSelect(pParentDiagram, selectedItems);
//                  }
//                  else
//                  {
//                     m_PropertyEditor.onCompartmentSelect(pCompartment);
//                  }
               }
            });
         }
      }
      catch (Exception err)
      {
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSelectionEventsSink#onUnselect(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onUnselect(IDiagram pParentDiagram, IPresentationElement[] unselectedItems, IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreCreated(org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
//   public void onDrawingAreaPreCreated(IDrawingAreaControl pDiagramControl, IResultCell cell)
//   {
//      //nothing to do
//   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostCreated(org.netbeans.modules.uml.ui.swing.drawingarea.IAxDrawingAreaControl, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
//   public void onDrawingAreaPostCreated(IDrawingAreaControl pDiagramControl, IResultCell cell)
//   {
//      //nothing to do
//   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaOpened(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaOpened(IDiagram pParentDiagram, IResultCell cell)
   {
      try
      {
         if (m_PropertyEditor != null)
         {
            if (PropertyEditorBlocker.inProcess() == false)
            {
               final IDiagram dia = pParentDiagram;
               //while opening the diagram doing double click on the project tree item,
               //we get this event after onTreeItemSelect, but that puts the work in an
               //invoke later - so we need to put this into an invoke later too.
               SwingUtilities.invokeLater(new Runnable()
               {

                  public void run()
                  {
                     m_PropertyEditor.loadElement(dia);
                  }
               });
            }
         }
      }
      catch (Exception err)
      {
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaClosed(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaClosed(IDiagram pParentDiagram, boolean bDiagramIsDirty, IResultCell cell)
   {
      try
      {
         if (m_PropertyEditor != null)
         {
            SwingUtilities.invokeLater(new Runnable()
            {
               public void run()
               {
                  m_PropertyEditor.clear();
                  m_PropertyEditor.populateGrid();
               }
            });

         }
      }
      catch (Exception err)
      {
      }
   }

   public void onDrawingAreaPreSave(IProxyDiagram pParentDiagram, IResultCell cell)
   {
      //nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostSave(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPostSave(IProxyDiagram pParentDiagram, IResultCell cell)
   {
      //nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaKeyDown(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, int, boolean, boolean, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaKeyDown(IDiagram pParentDiagram, int nKeyCode, boolean bControlIsDown, boolean bShiftIsDown, boolean bAltIsDown, IResultCell cell)
   {
      if (bAltIsDown)
      {
         if (nKeyCode == KeyEvent.VK_ENTER)
         {
            if (m_PropertyEditor != null)
            {
               m_PropertyEditor.setFocus();
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPrePropertyChange(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
//   public void onDrawingAreaPrePropertyChange(IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IResultCell cell)
//   {
//      //nothing to do
//   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostPropertyChange(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
//   public void onDrawingAreaPostPropertyChange(IProxyDiagram pProxyDiagram, int nPropertyKindChanged, IResultCell cell)
//   {
//      if (pProxyDiagram != null && m_PropertyEditor != null)
//      {
//         if (nPropertyKindChanged == DiagramAreaEnumerations.DAPK_LAYOUT)
//         {
//            final IProxyDiagram diagram = pProxyDiagram;
//            SwingUtilities.invokeLater(new Runnable()
//            {
//               public void run()
//               {
//                  m_PropertyEditor.reloadElement(diagram);
//
//               }
//            });
//            
//         }
//      }
//   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaTooltipPreDisplay(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement, org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
//   public void onDrawingAreaTooltipPreDisplay(IDiagram pParentDiagram, IPresentationElement pPE, IToolTipData pTooltip, IResultCell cell)
//   {
//      //nothing to do
//   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaActivated(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaActivated(IDiagram pParentDiagram, IResultCell cell)
   {
      //nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreDrop(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
//   public void onDrawingAreaPreDrop(IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IResultCell cell)
//   {
//      //nothing to do
//   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPostDrop(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram, org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
//   public void onDrawingAreaPostDrop(IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IResultCell cell)
//   {
//      //nothing to do
//   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaPreFileRemoved(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaPreFileRemoved(String sFilename, IResultCell cell)
   {
      //nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink#onDrawingAreaFileRemoved(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDrawingAreaFileRemoved(String sFilename, IResultCell cell)
   {
      //nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink#onElementPreModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onElementPreModified(IVersionableElement element, IResultCell cell)
   {
      //nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementModifiedEventsSink#onElementModified(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onElementModified(IVersionableElement element, IResultCell cell)
   {
      if (element != null && m_PropertyEditor != null)
      {
         // Here we make sure that we don't respond to event such as a presentation element
         // being added to a model element.  Who cares!
         EventContextManager mgr = new EventContextManager();
         if (!mgr.isNoEffectModification())
         {
            if (PropertyEditorBlocker.inProcess() == false)
            {
               if (element instanceof IElement)
               {
                  final IElement pEle = (IElement)element;
                  SwingUtilities.invokeLater(new Runnable()
                  {
                     public void run()
                     {
                        m_PropertyEditor.reloadElement(pEle);

                     }
                  });
                  
               }
            }
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeEventsSink#onItemExpanding(org.netbeans.modules.uml.ui.swing.projecttree.IAxProjectTreeControl, org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeExpandingContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onItemExpanding(IProjectTreeControl pParentControl, IProjectTreeExpandingContext pContext, IResultCell cell)
   {
      // nothing to do
   }
   
   public void onItemExpandingWithFilter(IProjectTreeControl pParentControl, 
                                          IProjectTreeExpandingContext pContext, 
                                          FilteredItemManager manager, IResultCell cell)
    {
       onItemExpanding(pParentControl, pContext, cell);
    }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeEventsSink#onBeforeEdit(org.netbeans.modules.uml.ui.swing.projecttree.IAxProjectTreeControl, org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeEditVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onBeforeEdit(IProjectTreeControl pParentControl, IProjectTreeItem pItem, IProjectTreeEditVerify pVerify, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeEventsSink#onAfterEdit(org.netbeans.modules.uml.ui.swing.projecttree.IAxProjectTreeControl, org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeEditVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onAfterEdit(IProjectTreeControl pParentControl, IProjectTreeItem pItem, IProjectTreeEditVerify pVerify, IResultCell cell)
   {
      if (pItem != null && m_PropertyEditor != null)
      {
         Vector < IPropertyDefinition > propDefs = new Vector < IPropertyDefinition > ();
         Vector < IPropertyElement > propEles = new Vector < IPropertyElement > ();
         IElement pModEle = pItem.getModelElement();
         if (pModEle != null && pModEle instanceof INamedElement)
         {
            INamedElement pNamedEle = (INamedElement)pModEle;
            String kind = pNamedEle.getElementType();
            IPropertyElement pEle = m_PropertyEditor.processSelectedItem(kind, propDefs, pNamedEle);
            if (pEle != null)
            {
               propEles.add(pEle);
            }
            m_PropertyEditor.setPropertyDefinitions(propDefs);
            m_PropertyEditor.setPropertyElements(propEles);
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeEventsSink#onDoubleClick(org.netbeans.modules.uml.ui.swing.projecttree.IAxProjectTreeControl, org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeItem, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDoubleClick(IProjectTreeControl pParentControl, IProjectTreeItem pItem, boolean isControl, boolean isShift, boolean isAlt, boolean isMeta, IResultCell cell)
   {
      try
      {
         if (m_PropertyEditor != null)
         {
            boolean isDiag = pItem.isDiagram();
            if (isDiag)
            {
               final IProjectTreeItem[] items = {  pItem };               
               SwingUtilities.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     m_PropertyEditor.onTreeSelect(items);

                  }
               });
               
            }
         }
      }
      catch (Exception err)
      {
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeEventsSink#onSelChanged(org.netbeans.modules.uml.ui.swing.projecttree.IAxProjectTreeControl, org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeItem[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onSelChanged(IProjectTreeControl pParentControl, final IProjectTreeItem[] pItem, IResultCell cell)
   {
      try
      {
         if (m_PropertyEditor != null)
         {
            int count = pItem.length;
            if (count > 0)
            {
               SwingUtilities.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     m_PropertyEditor.onTreeSelect(pItem);

                  }
               });
               
            }
         }
      }
      catch (Exception e)
      {
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeEventsSink#onRightButtonDown(org.netbeans.modules.uml.ui.swing.projecttree.IAxProjectTreeControl, org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeItem, org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeHandled, int, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onRightButtonDown(IProjectTreeControl pParentControl, IProjectTreeItem pItem, IProjectTreeHandled pHandled, int nScreenLocX, int nScreenLocY, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeEventsSink#onBeginDrag(org.netbeans.modules.uml.ui.swing.projecttree.IAxProjectTreeControl, org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeItem[], org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onBeginDrag(IProjectTreeControl pParentControl, IProjectTreeItem[] pItem, IProjectTreeDragVerify pVerify, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeEventsSink#onMoveDrag(org.netbeans.modules.uml.ui.swing.projecttree.IAxProjectTreeControl, org.netbeans.modules.uml.ui.swing.projecttree.IDataObject, org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onMoveDrag(IProjectTreeControl pParentControl, Transferable pItem, IProjectTreeDragVerify pVerify, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeEventsSink#onEndDrag(org.netbeans.modules.uml.ui.swing.projecttree.IAxProjectTreeControl, org.netbeans.modules.uml.ui.swing.projecttree.IDataObject, org.netbeans.modules.uml.ui.swing.projecttree.IProjectTreeDragVerify, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onEndDrag(IProjectTreeControl pParentControl, Transferable pItem, int action, IProjectTreeDragVerify pVerify, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreCreate(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onElementPreCreate(String ElementType, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementCreated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onElementCreated(IVersionableElement element, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDelete(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onElementPreDelete(IVersionableElement element, IResultCell cell)
   {
      if (element != null && m_PropertyEditor != null)
      {
         if (PropertyEditorBlocker.inProcess() == false)
         {
            SwingUtilities.invokeLater(new Runnable()
            {
               public void run()
               {
                  m_PropertyEditor.clear();                  
               }
            });
            
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDeleted(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onElementDeleted(IVersionableElement element, IResultCell cell)
   {
      // Fix for Bug # 5078953 NPE due to editing properties window after the element is deleted
       try
      {
         if (m_PropertyEditor != null)
         {
            m_PropertyEditor.clear();
            m_PropertyEditor.populateGrid();
         }
      }
      catch (Exception err)
      {
      }       
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onElementPreDuplicated(IVersionableElement element, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onElementDuplicated(IVersionableElement element, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink#onPreTransform(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onPreTransform(IClassifier classifier, String newForm, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifierTransformEventsSink#onTransformed(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onTransformed(IClassifier classifier, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.ICompartmentEventsSink#onCompartmentSelected(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
//   public void onCompartmentSelected(final ICompartment pItem, boolean bSelected, IResultCell cell)
//   {
//      if (m_PropertyEditor != null)
//      {
//         if (bSelected)
//         {
//            SwingUtilities.invokeLater(new Runnable()
//            {
//               public void run()
//               {
//                  m_PropertyEditor.onCompartmentSelect(pItem);
//
//               }
//            });
//            
//         }
//      }
//   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.ICompartmentEventsSink#onCompartmentCollapsed(org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
//   public void onCompartmentCollapsed(ICompartment pCompartment, boolean bCollapsed, IResultCell cell)
//   {
//      try
//      {
//         if (m_PropertyEditor != null)
//         {
//            //				 if (pCompartment instanceof IADExtensionPointListCompartment)
//            //				 {
//            //				 }
//            //				 else if (pCompartment instanceof IADOperationListCompartment )
//            //				 {
//            //				 }
//            //				 else
//            //				 {
//            
//            SwingUtilities.invokeLater(new Runnable()
//            {
//               public void run()
//               {
//                  m_PropertyEditor.clear();
//
//               }
//            });
//            
//            //				 }
//         }
//      }
//      catch (Exception err)
//      {
//      }
//   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspacePreCreateEventPayload, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWorkspacePreCreate(IWorkspacePreCreateEventPayload pEvent, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceCreated(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWorkspaceCreated(IWorkspace space, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreOpen(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWorkspacePreOpen(String fileName, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceOpened(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWorkspaceOpened(IWorkspace space, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreSave(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWorkspacePreSave(String fileName, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceSaved(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWorkspaceSaved(IWorkspace space, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspacePreClose(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWorkspacePreClose(IWorkspace space, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)

    * @see org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventsSink#onWorkspaceClosed(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWorkspaceClosed(IWorkspace space, IResultCell cell)
   {
      try
      {
         if (m_PropertyEditor != null)
         {
            m_PropertyEditor.clear();
            m_PropertyEditor.setPropertyElements(null);
         }
      }
      catch (Exception err)
      {
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultPreModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.metamodel.core.foundation.IExpression, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDefaultPreModified(IAttribute attr, IExpression proposedValue, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDefaultModified(IAttribute attr, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPreDefaultBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onPreDefaultBodyModified(IAttribute feature, String bodyValue, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultBodyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDefaultBodyModified(IAttribute feature, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPreDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onPreDefaultLanguageModified(IAttribute feature, String language, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDefaultLanguageModified(IAttribute feature, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPreDerivedModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onPreDerivedModified(IAttribute feature, boolean proposedValue, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onDerivedModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onDerivedModified(IAttribute feature, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPrePrimaryKeyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onPrePrimaryKeyModified(IAttribute feature, boolean proposedValue, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttributeEventsSink#onPrimaryKeyModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onPrimaryKeyModified(IAttribute feature, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onConditionPreAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onConditionPreAdded(IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onConditionAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onConditionAdded(IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onConditionPreRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onConditionPreRemoved(IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onConditionRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onConditionRemoved(IOperation oper, IConstraint condition, boolean isPreCondition, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onPreQueryModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, boolean, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onPreQueryModified(IOperation oper, boolean proposedValue, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onQueryModified(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onQueryModified(IOperation oper, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onRaisedExceptionPreAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onRaisedExceptionPreAdded(IOperation oper, IClassifier pException, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onRaisedExceptionAdded(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onRaisedExceptionAdded(IOperation oper, IClassifier pException, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onRaisedExceptionPreRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onRaisedExceptionPreRemoved(IOperation oper, IClassifier pException, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationEventsSink#onRaisedExceptionRemoved(org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation, org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onRaisedExceptionRemoved(IOperation oper, IClassifier pException, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWSProjectPreCreate(IWorkspace space, String projectName, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectCreated(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWSProjectCreated(IWSProject project, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreOpen(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWSProjectPreOpen(IWorkspace space, String projName, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectOpened(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWSProjectOpened(final IWSProject project, IResultCell cell)
   {
      try
      {
         if (m_PropertyEditor != null)
         {
            if (PropertyEditorBlocker.inProcess() == false)
            {               
               SwingUtilities.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     m_PropertyEditor.reloadElement(project);

                  }
               });
               
            }
         }
      }
      catch (Exception err)
      {
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreRemove(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWSProjectPreRemove(IWSProject project, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectRemoved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWSProjectRemoved(IWSProject project, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreInsert(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWSProjectPreInsert(IWorkspace space, String projectName, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectInserted(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWSProjectInserted(IWSProject project, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreRename(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWSProjectPreRename(IWSProject project, String newName, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectRenamed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWSProjectRenamed(IWSProject project, String oldName, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreClose(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWSProjectPreClose(IWSProject project, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectClosed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWSProjectClosed(final IWSProject project, IResultCell cell)
   {
      try
      {
         if (m_PropertyEditor != null)
         {
            if (PropertyEditorBlocker.inProcess() == false)
            {
               SwingUtilities.invokeLater(new Runnable()
               {
                  public void run()
                  {
                     m_PropertyEditor.reloadElement(project);

                  }
               });
               
            }
         }
      }
      catch (Exception err)
      {
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreSave(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWSProjectPreSave(IWSProject project, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectSaved(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onWSProjectSaved(IWSProject project, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink#onPreferenceChange(java.lang.String, org.netbeans.modules.uml.core.support.umlutils.IPropertyElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onPreferenceChange(String name, IPropertyElement pElement, IResultCell cell)
   {
      // right now, I know that the only preference that I am interested in is the default grid font preference
      // the preference that actually changes in the font scheme of things is its child "FontName", so I am going
      // to need to get the parent of the preference that I have just received and check its name
      if (pElement != null)
      {
         if (m_PropertyEditor != null)
         {
            IPropertyElement parentEle = pElement.getParent();
            if (parentEle != null)
            {
               String parentName = parentEle.getName();
               if (parentName.equals("DefaultGridFont"))
               {
                  m_PropertyEditor.resetGridSettings();
               }
            }
            //kris richards - DefaultFilter pref expunged
         }
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink#onPreferenceAdd(java.lang.String, org.netbeans.modules.uml.core.support.umlutils.IPropertyElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onPreferenceAdd(String Name, IPropertyElement pElement, IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink#onPreferenceRemove(java.lang.String, org.netbeans.modules.uml.core.support.umlutils.IPropertyElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onPreferenceRemove(String Name, IPropertyElement pElement, IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.preferenceframework.IPreferenceManagerEventsSink#onPreferencesChange(org.netbeans.modules.uml.core.support.umlutils.IPropertyElement[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onPreferencesChange(IPropertyElement[] pElements, IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreInit(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onCoreProductPreInit(ICoreProduct pVal, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductInitialized(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onCoreProductInitialized(ICoreProduct newVal, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreQuit(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onCoreProductPreQuit(ICoreProduct pVal, IResultCell cell)
   {
      try
      {
         if (m_PropertyEditor != null)
            m_PropertyEditor.connectSinks(false);
      }
      catch (Exception err)
      {
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreSaved(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onCoreProductPreSaved(ICoreProduct pVal, IResultCell cell)
   {
      // nothing to do
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductSaved(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onCoreProductSaved(ICoreProduct newVal, IResultCell cell)
   {
      // nothing to do
   }

   public void onPreOperationPropertyModified(IOperation oper, int nKind, boolean proposedValue, IResultCell cell)
   {
   }

   public void onOperationPropertyModified(IOperation oper, int nKind, IResultCell cell)
   {
   }

}
