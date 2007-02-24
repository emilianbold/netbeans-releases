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

/*
 ReqEventsSink.java
 *
 Created on June 25, 2004, 6:39 AM
 */

package org.netbeans.modules.uml.requirements;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.requirementsframework.IRequirement;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.ui.controls.projecttree.IDataObject;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeHandled;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.FilteredItemManager;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuSelectionHandler;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink;
import java.awt.datatransfer.Transferable;

/**
 *
 * @author  Trey Spiva
 */
public class ReqEventsSink implements IProjectTreeEventsSink,
                                      ICoreProductInitEventsSink,
                                      IProductContextMenuSelectionHandler,
                                      IDrawingAreaEventsSink,
                                      IReqEventsSink
{
   private ADRequirementsManager m_Manager = null;
   
   /*Creates a new instance of ReqEventsSink */
   public ReqEventsSink(ADRequirementsManager manager)
   {
      m_Manager = manager;
   }
   
   /*Retrieve the requirments manager associated with the event handler. */
   public ADRequirementsManager getManager()
   {
      return m_Manager;
   }
   
   ////////////////////////////////////////////////////////////////////////////
   // Event Handlers
   
   /**
    *
    * IDrawingAreaEventsSink event, Fired right before items are
    * dropped onto the diagram. This event sink calls the same
    * method name in the ReqProxyManger to handle the event.
    *
    * @param pParentDiagram[in] The diagram where the drop occured
    * @param pContext[in] Deatails of what has been dropped onto the diagram
    * @param cell[in] The result cell from the original event.
    *
    * @return void
    *
    */
   public void onDrawingAreaPreDrop(IDiagram pParentDiagram,
                                    IDrawingAreaDropContext pContext,
                                    IResultCell cell)
   {
      if( (null == pParentDiagram) ||
          (pContext == null))
      {
         throw new IllegalArgumentException();
      }
      
      
      if( m_Manager != null)
      {
         // Forward to the requriments proxy manager if interested.
         m_Manager.onDrawingAreaPreDrop( pParentDiagram, pContext, cell );
      }
   }
   
   /**
    *
 IDrawingAreaEventsSink event, Fired after items are are
 dropped onto the diagram.  This event sink calls the same
 method name in the ReqProxyManger to handle the event.
    *
 @param pParentDiagram[in] The diagram where the drop occured
 @param pContext[in] Deatails of what has been dropped onto the diagram
 @param cell[in] The result cell from the original event.
    *
 @return void
    *
    */
   public void onDrawingAreaPostDrop( IDiagram pParentDiagram,
                                      IDrawingAreaDropContext pContext,
                                      IResultCell cell )
   {
      if( (null == pParentDiagram) ||
      (pContext == null))
      {
         throw new IllegalArgumentException();
      }
      
      if( m_Manager != null)
      {
         // Forward to the requriments proxy manager if interested.
         m_Manager.onDrawingAreaPostDrop( pParentDiagram, pContext, cell );
      }
   }
   
   // Ignored by CReqEventsSink
   public void onDrawingAreaPreCreated(IDrawingAreaControl pDiagramControl,
   IResultCell cell)
   {
      
   }
   
   // Ignored by CReqEventsSink
   public void onDrawingAreaPostCreated(IDrawingAreaControl pDiagramControl,
   IResultCell cell)
   {
      
   }
   
   // Ignored by CReqEventsSink
   public void onDrawingAreaOpened(IDiagram parentDiagram,
   IResultCell cell)
   {
      
   }
   
   // Ignored by CReqEventsSink
   public void onDrawingAreaClosed(IDiagram parentDiagram,
   boolean bDiagramIsDirty,
   IResultCell cell)
   {
      
   }
   
   // Ignored by CReqEventsSink
   public void onDrawingAreaPreSave(IProxyDiagram parentDiagram,
   IResultCell cell)
   {
      
   }
   
   // Ignored by CReqEventsSink
   public void onDrawingAreaPostSave(IProxyDiagram parentDiagram,
   IResultCell cell)
   {
      
   }
   
   // Ignored by CReqEventsSink
   public void onDrawingAreaKeyDown( IDiagram pParentDiagram, 
                                     int nKeyCode, 
                                     boolean bControlIsDown, 
                                     boolean bShiftIsDown, 
                                     boolean bAltIsDown, 
                                     IResultCell cell)
   {
      
   }
   
   // Ignored by CReqEventsSink
   public void onDrawingAreaPrePropertyChange(IProxyDiagram parentDiagram,
                                              int nPropertyKindChanged,
                                              IResultCell cell)
   {
      
   }
   public void onDrawingAreaPreFileRemoved(String sFilename,  IResultCell cell)
   {
      
   }
   public void onDrawingAreaFileRemoved(String sFilename,IResultCell cell)
   {
      
   }
//   public void onDrawingAreaPresentationElementPreAction(IDiagram pParentDiagram,
//                                                         IPresentationElement pPE,
//                                                         PresentationElementPreAction nAction,
//                                                         IResultCell cell)
//   {
//      
//   }
//   public void onDrawingAreaPresentationElementAction(IDiagram pParentDiagram,
//   IPresentationElement pPE,
//   PresentationElementAction nAction,
//   IResultCell cell)
//   {
//      
//   }
   
   // Ignored by CReqEventsSink
   public void onDrawingAreaPostPropertyChange(IProxyDiagram parentDiagram,
                                               int nPropertyKindChanged,
                                               IResultCell cell)
   {
   }
   
   // Ignored by CReqEventsSink
   public void onDrawingAreaTooltipPreDisplay(IDiagram parentDiagram,
   IPresentationElement pE,
   IToolTipData tooltip,
   IResultCell cell)
   {
   }
   
   // Ignored by CReqEventsSink
   public void onDrawingAreaActivated(IDiagram parentDiagram,
   IResultCell cell)
   {
      
   }
   
   /////////////////////////////////////////////////////////////////////////////////////
   // IProjectTreeEventsSink
   /**
 Expand Nodes containing a Requirement - Get their SubRequirements.
    */
   public void onItemExpanding( IProjectTreeControl pControl,
                                IProjectTreeExpandingContext pContext,
                                IResultCell cell )
   {
      if( isDesignCenterTree(pControl) )
      {
         IProjectTreeItem cpProjectTreeItem  = pContext.getProjectTreeItem();
         if( cpProjectTreeItem != null)
         {
            Object cpDispatch = cpProjectTreeItem.getData();
            
            if( cpDispatch instanceof IRequirement)
            {
               IRequirement cpRequirement = (IRequirement)cpDispatch;
               if( m_Manager != null)
               {
                  // Forward to the requirements proxy manager if interested.
                  m_Manager.onItemExpanding( cpProjectTreeItem, cpRequirement ) ;
               }
            }
         }
      }
   }
   /**
 Routes this message to the GoFPatternAddIn
    */
   public void onBeforeEdit( IProjectTreeControl pParentControl,IProjectTreeItem item, IProjectTreeEditVerify verify,IResultCell cell)
   {
      
   }
   /**
 Routes this message to the GoFPatternAddIn
    */
   public void onAfterEdit(IProjectTreeControl pParentControl,IProjectTreeItem pItem, IProjectTreeEditVerify pVerify,IResultCell cell)
   {
      
   }
   /**
 Routes this message to the GoFPatternAddIn
    */
   public void onDoubleClick(IProjectTreeControl pParentControl, 
	                         IProjectTreeItem    pItem, 
                             boolean             isControl, 
                             boolean             isShift, 
                             boolean             isAlt, 
                             boolean             isMeta, 
	                         IResultCell         cell)
   {
      
   }
   /**
 Routes this message to the GoFPatternAddIn
    */
   public void onSelChanged(IProjectTreeControl pParentControl, 
	                        IProjectTreeItem[] pItem, 
	                        IResultCell cell)
   {
      
   }
   /**
 Routes this message to the GoFPatternAddIn
    */
   public void onRightButtonDown(IProjectTreeControl pParentControl, 
	                               IProjectTreeItem pItem, 
	                               IProjectTreeHandled pHandled, 
	                               int nScreenLocX, 
	                               int nScreenLocY, 
	                               IResultCell cell)
   {
      
   }
   /**
 Routes this message to the GoFPatternAddIn
    */
   public void onBeginDrag( IProjectTreeControl pParentControl, 
	                         IProjectTreeItem[] pItem, 
	                         IProjectTreeDragVerify pVerify, 
	                         IResultCell cell)
   {
      if( (null == pParentControl) ||
          (pItem == null) ||
          (pVerify == null) )
      {
         throw new IllegalArgumentException();
      }
      
      
      
      if( isDesignCenterTree(pParentControl) )
      {
         // Do not allow dragging of the DOORS modules ( TreeItem contains an IRequirement with IRequirement type.equals("Category") )
         boolean allow = true;
         
         for( int x = 0; x < pItem.length; x++ )
         {
            IProjectTreeItem  cpTreeItem = pItem[x];
            if( cpTreeItem != null)
            {
               Object cpDispatch = cpTreeItem.getData();
               
               if( cpDispatch instanceof IRequirement)
               {
                  IRequirement cpRequirement = (IRequirement)cpDispatch;   
                  allow = cpRequirement.isAllowedToDrag();
//                  String strProviderID = cpRequirement.getProviderID();
//                  String strType = cpRequirement.getType();
//                  if( (strProviderID.equals("ADRequirements.DoorsReqProvider") == true) &&
//                  (strType.equals("Category") == true) )
//                  {
//                     allow = false;
//                     break;
//                  }
               }
               
               if(allow == true)
               {
                  // Do not allow dragging of Requirement Source nodes.
                  String strDescription = cpTreeItem.getDescription( );
                  String strSecondaryDescription = cpTreeItem.getSecondaryDescription();

                  // This will be true for the top-level Requirement Source nodes, they cannot be dragged.
                  if( strDescription.equals(strSecondaryDescription ) == true)
                  {
                     allow = false;
                     break;
                  }
               }
               else
               {
                  break;
               }
            }
         }
         if( !allow )
         {
            pVerify.setCancel( true );
            pVerify.setHandled( true );
         }
      }
      
   }
   
   /**
 Routes this message to the GoFPatternAddIn
    */
   public void onMoveDrag(IProjectTreeControl pParentControl, 
	                      Transferable pItem, 
	                      IProjectTreeDragVerify pVerify, 
	                      IResultCell cell)
   {
      
   }
   
   /**
 Routes this message to the GoFPatternAddIn
    */
   public void onEndDrag(IProjectTreeControl    pParentControl, 
                         Transferable           pItem, 
                         int                    action,
	                     IProjectTreeDragVerify pVerify, 
	                     IResultCell            cell)
   {
      
   }
   
   /**
 The tree has been hidden or shown.  Engines should not update the tree if hidden.
    */
   public void onHideTree(IProjectTreeControl pParentControl,boolean bHidden,IResultCell cell)
   {
      
   }
   
   
   // IProductContextMenuSelectionHandler
   /**
 If an external interface handles the display of the popup menu then this is called to handle the selection event
    */
   public void handleSelection(IProductContextMenu pContextMenu,IProductContextMenuItem pSelectedItem )
   {
      if( (null == pContextMenu) && (pSelectedItem != null))
      {
         throw new IllegalArgumentException();
      }
      
      if( m_Manager != null)
      {
         // Forward to the parent if interested.
         m_Manager.handleSelection( pContextMenu, pSelectedItem );
      }
      
   }
   
   // ICoreProductInitEventsSink by CReqEventsSink
   public void onCoreProductPreInit( ICoreProduct pVal, IResultCell cell )
   {
      
      
   }
   
   // This ICoreProductInitEventsSink Ignored by CReqEventsSink
   public void onCoreProductInitialized( ICoreProduct newVal, IResultCell cell )
   {
      
   }
   
/*
 This ICoreProductInitEventsSink event indicates that the  CoreProduct is going away.
 
 @param pVal[in] the core product that is going away.
 @param cell[in] result cell
 
 @return void
 */
   public void onCoreProductPreQuit( ICoreProduct pVal, IResultCell cell )
   {
      if( (null == pVal) || (cell == null) ) throw new IllegalArgumentException();
      
      if( m_Manager != null)
      {
         // Forward to the requriments manager if interested.
         m_Manager.deInitialize( null );
         
         // Clear out the static com ptr.
         m_Manager = null;
      }
      
   }
   
   public void onCoreProductPreSaved( ICoreProduct pVal, IResultCell cell )
   {
      
   }
   
   public void onCoreProductSaved( ICoreProduct newVal, IResultCell cell )
   {
      
   }
   /**
    * Is the argument tree control the design center tree?
    */
   protected boolean isDesignCenterTree(IProjectTreeControl pParentControl)
   {
      boolean retVal = false;
      
      if (pParentControl != null)
      {
         String sConfigName = "DesignCenter";
         String mgrName = pParentControl.getConfigMgrName() ;
         if( mgrName.equals(sConfigName) == true )
         {
            retVal = true;
         }
         //      pParentControl.get
         //      IProjectTreeModel thisModel = getTreeModel();
         //      if (thisModel instanceof DesignCenterSwingModel)
         //      {
         //          if (pParentControl instanceof JProjectTree)
         //          {
         //              ISwingProjectTreeModel model = ((JProjectTree)pParentControl).getProjectModel();
         //              retVal = model.getProjectTreeName().equals(ProjectTreeResources.getString("DesignCenterSwingModel.Design_Center_Description"));
         //          }
         //      }
      }
      
      return retVal;
   }

    public void onItemExpandingWithFilter(IProjectTreeControl pParentControl, 
                                          IProjectTreeExpandingContext pContext, 
                                          FilteredItemManager manager, IResultCell cell)
    {
       onItemExpanding(pParentControl, pContext, cell);
    }
}
