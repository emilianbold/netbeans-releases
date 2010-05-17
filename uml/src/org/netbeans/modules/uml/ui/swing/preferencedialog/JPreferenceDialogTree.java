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
 * Created on May 22, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.preferencedialog;

import java.awt.BorderLayout;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeDragVerifyImpl;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeExpandingContextImpl;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;


/**
 * @author treys
 *
 */
//public class JProjectTree extends JTree implements IProjectTreeControl
public class JPreferenceDialogTree extends JTree 
{
	private DispatchHelper m_DispatchHelper = new DispatchHelper();
	private PreferenceDialogUI m_PreferenceControl = null;
   private boolean        m_InDragProcess  = false;
	//private JTree          m_Tree           = new JTree();
   
	public JPreferenceDialogTree()
	{
		this(null, null);
	}
	
   /**
    * @param newModel
    */
   public JPreferenceDialogTree(ISwingPreferenceDialogModel newModel, 
   								PreferenceDialogUI control)
   {
      super(newModel);
      m_PreferenceControl = control;
      
      //setModel(newModel);
		
      //CBeckham - changed to allow for larger fonts
      //setFont(new java.awt.Font("Dialog", 0, 11));
      setFont(new java.awt.Font("Dialog", 0, getFont().getSize()));          
      
      setCellRenderer(new PreferenceDialogTreeRenderer());
      setSelectionModel(new DefaultTreeSelectionModel());
      getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
      //setEditable(true);
      setEnabled(true);
      setDragEnabled(true);
      setShowsRootHandles(true);
      //m_Tree.setTransferHandler( new ProjectTreeTransferHandler()  );
      setAutoscrolls(true);
      
      addTreeSelectionListener(new TreeSelectionListener() 
      {
         public void valueChanged(TreeSelectionEvent e) 
         {
            //ETSystem.out.println("I am here.");
            fireSelectionChange(e);
         }
      });
      
      addMouseListener(new ProjectTreeMouseHandler());
      //addTreeWillExpandListener(new ProjectTreeExpandHandler());
      //addControls();
   }
   
   /**
    * 
    */
   protected void addControls()
   {
      setLayout(new BorderLayout());
      //add(m_Tree, BorderLayout.CENTER);
   }

   public void setModel(TreeModel model)
   {
   	if(model instanceof ISwingPreferenceDialogModel)
   	{
         super.setModel(model);
   	}
   }
   
   public ISwingPreferenceDialogModel getProjectModel()
   {
   	return (ISwingPreferenceDialogModel)getModel();
   }

	//**************************************************
   // Fire Events
   //**************************************************
   
   public void fireSelectionChange(TreeSelectionEvent e)
   {
   		TreePath newPath = e.getNewLeadSelectionPath();
   		TreePath oldPath = e.getOldLeadSelectionPath();
   		if (newPath != null)
   		{
   			Object obj = newPath.getLastPathComponent();
   			if (obj != null && obj instanceof DefaultMutableTreeNode)
   			{
   				DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;
   				Object userObj = node.getUserObject();
   				if (userObj != null && userObj instanceof IPropertyElement)
   				{
   					IPropertyElement pEle = (IPropertyElement)userObj;
					updateTable(pEle);
   				}
   			}
   		}
   }
   
   /**
 	* This updates the corresponding table.
 	*/
	private void updateTable(IPropertyElement pEle)
	{
		m_PreferenceControl.loadTable(pEle);
	}

public boolean fireItemExpanding(ITreeItem item)
   {
   	boolean retVal = true;
      
		IProjectTreeEventDispatcher disp = m_DispatchHelper.getProjectTreeDispatcher();
		
		if(disp != null) 
	   {
			IEventPayload payload = disp.createPayload("ProjectTreeItemExpanding");
			IProjectTreeExpandingContext context = new ProjectTreeExpandingContextImpl(item);
			//disp.fireItemExpanding(this, context, payload);
			
			retVal = !context.isCancel();
			
	   }
	  
//	   recursive--;
      return retVal;
   }
   
	public boolean fireItemExpanding(TreePath path)
	{		
      boolean retVal = true;
      
      //addSelectionPath(path);
      
//      ITreeItem item = getProjectModel().getTreeItem(path);
//      if((item != null) && ((item.getChildCount() <= 0) || (item.isInitalized() == false)))
//      {         
//         retVal =  fireItemExpanding(item);
//      }      
      
      return retVal;
	}
   
   /**
    * @param selPath
    */
   public void fireDoubleClick(ITreeItem item)
   {
      boolean retVal = true;
      
      IProjectTreeEventDispatcher disp = m_DispatchHelper.getProjectTreeDispatcher();
      
      if(disp != null)
      {
         IEventPayload payload = disp.createPayload("ProjectTreeDoubleClick");
         //disp.fireDoubleClick(this, item.getData(), payload);
      }
   }   
   
   /**
    * @param selPath
    */
   public void fireDoubleClick(TreePath path)
   {
//      ITreeItem item = getProjectModel().getTreeItem(path);
//      if(item != null)
//      {
//         fireDoubleClick(item);
//      }
   }
   
   public boolean fireBeginDrag(IProjectTreeItem[] items)
   {      
      boolean retVal = true;
      
      IProjectTreeEventDispatcher disp = m_DispatchHelper.getProjectTreeDispatcher();
      
      if(disp != null)
      {
         IEventPayload payload = disp.createPayload("ProjectTreeBeginDrag");
         IProjectTreeDragVerify context = new ProjectTreeDragVerifyImpl();
        
         //disp.fireBeginDrag(this, items, context, payload);
         
         retVal = !context.isCancel();
        
         m_InDragProcess = retVal;
         
      }
      return retVal;
   }
   
   private TreePath[] m_DragPaths = null;
   
   public boolean fireBeginDrag(TreePath[] paths)
   {
      boolean retVal = true;
      
      IProjectTreeItem[] items = new IProjectTreeItem[paths.length];
      
//      for (int index = 0; index < paths.length; index++)
//      {
//         ITreeItem curItem = getProjectModel().getTreeItem(paths[index]);
//         
//         if(curItem != null)
//         {
//            items[index] = curItem.getData();
//         }
//      }
      
      m_DragPaths = paths;
      if((items != null) && (items.length > 0))
      {
         fireBeginDrag(items);
      }
      
      return retVal;
   }
   
   public boolean fireEndDrag(Transferable data, int action)
   {
      boolean retVal = true;
      m_InDragProcess = false;
      
      IProjectTreeEventDispatcher disp = m_DispatchHelper.getProjectTreeDispatcher();
      
      if(disp != null)
      {
         IEventPayload payload = disp.createPayload("ProjectTreeBeginDrag");
         IProjectTreeDragVerify context = new ProjectTreeDragVerifyImpl();
        
         //disp.fireEndDrag(this, data, action, context, payload);
         
         retVal = !context.isCancel();
         m_DragPaths = null;
         m_InDragProcess = retVal;
         
      }
      return retVal;
   }
   
   public boolean fireMoveDrag(Transferable           data, 
                               IProjectTreeDragVerify context)
   {
      boolean retVal = true;
      m_InDragProcess = false;
   
      IProjectTreeEventDispatcher disp = m_DispatchHelper.getProjectTreeDispatcher();
   
      if(disp != null)
      {
         IEventPayload payload = disp.createPayload("ProjectTreeBeginDrag");
         //disp.fireMoveDrag(this, data, context, payload);
      
         retVal = !context.isCancel();
     
         m_InDragProcess = retVal;
      
      }
      return retVal;

   
   }
   
	//**************************************************
   // JTree Override Methods
   //**************************************************
   
	
	/* (non-Javadoc)
	 * @see javax.swing.JTree#fireTreeWillExpand(javax.swing.tree.TreePath)
	 */
//	public void fireTreeWillExpand(TreePath path)
//	  throws ExpandVetoException
//	{
//		if(fireItemExpanding(path) == false)
//		{
//			throw new ExpandVetoException(null);
//		}
//		else
//		{
//         m_Tree.fireTreeWillExpand(path);
//		}
//	}
	
   //	**************************************************
	// IProjectTreeControl methods
	// **************************************************

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getRootNodes()
	 */
	public IProjectTreeItem[] getRootNodes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getStandardDescription(int)
	 */
	public String getStandardDescription(int nKind)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getUserCalledShowWorkspaceNode()
	 */
	public boolean getUserCalledShowWorkspaceNode()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getShowWorkspaceNode()
	 */
	public boolean getShowWorkspaceNode()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setShowWorkspaceNode(boolean)
	 */
	public void setShowWorkspaceNode(boolean value)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#addWorkspace()
	 */
	public long addWorkspace()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getUnfilteredProjects()
	 */
	public IStrings getUnfilteredProjects()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setUnfilteredProjects(org.netbeans.modules.uml.core.support.umlsupport.IStrings)
	 */
	public void setUnfilteredProjects(IStrings value)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#refresh(boolean)
	 */
	public void refresh(boolean bPostEvent)
	{
      ISwingPreferenceDialogModel model = getProjectModel();
      
      if(bPostEvent == true)
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               refresh(false);
            }
         });
      }
      else
      {
//         PreserveTreeState preserveState = new PreserveTreeState(m_Tree);
//         preserveState.perserveState();
//         
//         model.clear();
         
         updateUI();
         treeDidChange();
         
         //preserveState.restoreState();
      }
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#close()
	 */
	public void close()
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#sortChildNodes(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
	 */
	public void sortChildNodes(IProjectTreeItem pParent)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#sortThisNode(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
	 */
	public void sortThisNode(IProjectTreeItem pNode)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setImage(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.String, int)
	 */
	public void setImage(IProjectTreeItem pItem, String sIconLibrary, int nIconID)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setImage2(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, int)
	 */
	public void setImage2(IProjectTreeItem pItem, int hIcon)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setDescription(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.String)
	 */
	public void setDescription(IProjectTreeItem pItem, String sDesc)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setSecondaryDescription(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.String)
	 */
	public void setSecondaryDescription(IProjectTreeItem pItem, String sDesc)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setDispatch(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.Object)
	 */
	public void setDispatch(IProjectTreeItem pItem, Object pDisp)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setModelElement(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
	 */
	public void setModelElement(IProjectTreeItem pItem, IElement pEle)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#addItem(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.String, int, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
	 */
	public void addItem( IProjectTreeItem pParent, 
								String sText, 
								int nSortPriority, 
								IElement pElement, 
								IProjectTreeItem pCreatedItem )
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#addItem2(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.String, int, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, java.lang.String, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
	 */
	public void addItem( IProjectTreeItem pParent, 
								String sText, 
								int nSortPriority, 
								IElement pElement, 
								String sDescription, 
								IProjectTreeItem pCreatedItem )
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#addItem3(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.String, int, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, java.lang.Object, java.lang.String, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
	 */
	public void addItem( IProjectTreeItem pParent, 
								String sText,
								int nSortPriority, 
								IElement pElement, 
								Object pProjectTreeSupportTreeItem, 
								String sDescription, 
								IProjectTreeItem pCreatedItem )
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#beginEditFirstSelected()
	 */
	public void beginEditFirstSelected()
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#beginEditThisItem(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
	 */
	public void beginEditThisItem(IProjectTreeItem pItem)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getSelected()
	 */
	public IProjectTreeItem[] getSelected()
	{
      IProjectTreeItem[] retVal = null;
      
      TreePath[] paths = getSelectionPaths();
      if((paths != null) && (paths.length > 0))
      {
         retVal = new IProjectTreeItem[paths.length];
         
//         for (int index = 0; index < paths.length; index++)
//         {
//            ITreeItem curItem = getProjectModel().getTreeItem(paths[index]);
//            retVal[index] = curItem.getData();
//         }
      }
      
      return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#deselectAll()
	 */
	public void deselectAll()
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#endEditing(boolean)
	 */
	public void endEditing(boolean bSaveChanges)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#deleteSelectedItems()
	 */
	public void deleteSelectedItems()
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setText(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.String)
	 */
	public void setText(IProjectTreeItem pItem, String sText)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getParent(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
	 */
	public IProjectTreeItem getParent(IProjectTreeItem pItem)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#retrieveProjectFromItem(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
	 */
	public IProject retrieveProjectFromItem(IProjectTreeItem pProjItem)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getProjects()
	 */
	public IProjectTreeItem[] getProjects()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getWorkspaceTreeItem()
	 */
	public IProjectTreeItem getWorkspaceTreeItem()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getChildren(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
	 */
	public IProjectTreeItem[] getChildren(IProjectTreeItem pItem)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getChildDiagrams(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
	 */
	public IProjectTreeItem[] getChildDiagrams(IProjectTreeItem pItem)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#findNode(java.lang.String, java.lang.String)
	 */
	public IProjectTreeItem[] findNode(String sTopLevelXMIID, String itemXMIID)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#findNode2(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
	 */
	public IProjectTreeItem[] findNode2(IElement pElement)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#findNodeWithDescription(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.String)
	 */
	public IProjectTreeItem[] findNodeWithDescription(IProjectTreeItem pParent, String sDescription)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#findNodesRepresentingMetaType(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, java.lang.String)
	 */
	public IProjectTreeItem[] findNodesRepresentingMetaType(IProjectTreeItem pParent, String sMetaType)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#findDiagramNode(java.lang.String)
	 */
	public IProjectTreeItem[] findDiagramNode(String sTOMFilename)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#removeDiagramNode(java.lang.String, boolean)
	 */
	public void removeDiagramNode(String sTOMFilename, boolean bPostEvent)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#removeFromTree(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem[])
	 */
	public void removeFromTree(IProjectTreeItem[] pRemovedItems)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#removeFromTree2(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
	 */
	public void removeFromTree2(IElement pElementToRemove)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#selectProject(org.netbeans.modules.uml.core.metamodel.structure.IProject)
	 */
	public void selectProject(IProject pProject)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#lockWindowUpdate(boolean)
	 */
	public void lockWindowUpdate(boolean bLock)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getDropTargetItem()
	 */
	public IProjectTreeItem getDropTargetItem()
	{
      IProjectTreeItem retVal = null;
      IProjectTreeItem[] curSelected = getSelected();
      
      // During the drag the tree selects the items as they are passed over.
      // Therefore, there should only be one selected item.  The item that
      // is the drop target.
      if((curSelected != null) && (curSelected.length > 0))
      {
         retVal = curSelected[0];
      }
      
      return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#closeProject(org.netbeans.modules.uml.core.metamodel.structure.IProject)
	 */
	public void closeProject(IProject pProject)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#openProject(org.netbeans.modules.uml.core.metamodel.structure.IProject)
	 */
	public void openProject(IProject pProject)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#areJustTheseSelected(int)
	 */
	public boolean areJustTheseSelected(int items)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#atLeastOneOfTheseSelected(int)
	 */
	public boolean atLeastOneOfTheseSelected(int items)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getFirstSelectedModelElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, boolean)
	 */
	public void getFirstSelectedModelElement(IElement pItem, boolean bIsTopItem)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getFirstSelectedModelElement2(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, boolean)
	 */
	public void getFirstSelectedModelElement2(IElement pItem, IProjectTreeItem pTreeItem, boolean bIsTopItem)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getFirstSelectedDiagram(java.lang.StringBuffer, boolean)
	 */
	public void getFirstSelectedDiagram(StringBuffer sLocation, boolean bIsTopItem)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getFirstSelectedOpenDiagram(java.lang.StringBuffer, boolean)
	 */
	public void getFirstSelectedOpenDiagram(StringBuffer sLocation, boolean bIsTopItem)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getFirstSelectedClosedDiagram(java.lang.StringBuffer, boolean)
	 */
	public void getFirstSelectedClosedDiagram(StringBuffer sLocation, boolean bIsTopItem)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getFirstSelectedOpenProject(java.lang.StringBuffer, boolean)
	 */
	public void getFirstSelectedOpenProject(StringBuffer sProjectName, boolean bIsTopItem)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getFirstSelectedClosedProject(java.lang.StringBuffer, boolean)
	 */
	public void getFirstSelectedClosedProject(StringBuffer sLocation, boolean bIsTopItem)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getWindowHandle()
	 */
	public int getWindowHandle()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#findAndSelectInTree(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
	 */
	public void findAndSelectInTree(IElement pModelElement)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getHasBeenExpanded(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
	 */
	public boolean getHasBeenExpanded(IProjectTreeItem pItem)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getIsExpanded(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
	 */
	public boolean getIsExpanded(IProjectTreeItem pItem)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setIsExpanded(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, boolean)
	 */
	public void setIsExpanded(IProjectTreeItem pItem, boolean value)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getIsSelected(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem)
	 */
	public boolean getIsSelected(IProjectTreeItem pItem)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#setIsSelected(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem, boolean)
	 */
	public void setIsSelected(IProjectTreeItem pItem, boolean value)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#rememberTreeState()
	 */
	public void rememberTreeState()
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#restoreTreeState()
	 */
	public void restoreTreeState()	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#openSelectedDiagrams()
	 */
	public IProxyDiagram[] openSelectedDiagrams()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#closeSelectedDiagrams()
	 */
	public IProxyDiagram[] closeSelectedDiagrams()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#saveSelectedDiagrams()
	 */
	public IProxyDiagram[] saveSelectedDiagrams()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#openSelectedProjects()
	 */
	public void openSelectedProjects()
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#closeSelectedProjects()
	 */
	public void closeSelectedProjects()
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#saveSelectedProjects()
	 */
	public void saveSelectedProjects()
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#handleLostProject(org.netbeans.modules.uml.core.workspacemanagement.IWSProject)
	 */
	public void handleLostProject(IWSProject wsProject)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#questionUserAboutNameCollision(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement[], org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void questionUserAboutNameCollision(INamedElement pElement, 
	                                           String sProposedName, 
	                                           INamedElement[] pCollidingElements, 
	                                           IResultCell pCell)
	{
		// TODO Auto-generated method stub
      
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#getNumOpenProjects()
	 */
	public int getNumOpenProjects()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#postDeleteModelElement(java.lang.String, java.lang.String)
	 */
	public void postDeleteModelElement(String sTopLevelXMIID, String sXMIID)
	{
		// TODO Auto-generated method stub
      
	}
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl#isEditing()
    */
   public boolean isEditing()
   {
      // TODO Auto-generated method stub
      return false;
   }
   
   //**************************************************
   // Support Inner Classes
   //**************************************************
   
   public class ProjectTreeMouseHandler extends MouseInputAdapter
   {
      public void mousePressed(MouseEvent e) 
      {
         if(e.getClickCount() == 2)
         {
            int selRow = getRowForLocation(e.getX(), e.getY());
            TreePath selPath = getPathForRow(selRow);
            if(selRow != -1)
            {
               fireDoubleClick(selPath);             
            }
         }
      }
   }
   
   public class ProjectTreeExpandHandler implements TreeWillExpandListener
   {

      /* (non-Javadoc)
       * @see javax.swing.event.TreeWillExpandListener#treeWillExpand(javax.swing.event.TreeExpansionEvent)
       */
      public void treeWillExpand(TreeExpansionEvent e) 
        throws ExpandVetoException
      {
         if(fireItemExpanding(e.getPath()) == false)
         {
            throw new ExpandVetoException(e);
         }     
      }

      /* (non-Javadoc)
       * @see javax.swing.event.TreeWillExpandListener#treeWillCollapse(javax.swing.event.TreeExpansionEvent)
       */
      public void treeWillCollapse(TreeExpansionEvent event) 
         throws ExpandVetoException
      {
      }
   }

	/**
	 * This is called when the tree needs to be reloaded because of
	 * advanced button clicked.
	 */
	public void reload()
	{
		//We want to save the current selection, so that we can select it after reload.
		TreePath currSelection = getSelectionPath();
		int currRow = getRowForPath(currSelection);
		Object obj = getLastSelectedPathComponent();
		String name = "";
		DefaultMutableTreeNode node = null;
		if (obj instanceof DefaultMutableTreeNode)
		{
			node = (DefaultMutableTreeNode)obj;
			Object userObj = node.getUserObject();
			if (userObj != null && userObj instanceof IPropertyElement)
			{
				IPropertyElement elem = (IPropertyElement)userObj;
				name = elem.getName();
			}
		}
		
		TreeModel model = getModel();
		if (model instanceof PreferenceDialogTreeModel)
		{
			PreferenceDialogTreeModel treeModel = (PreferenceDialogTreeModel)model;
			treeModel.reinitialize();
			this.updateUI();
		}
		
		//reselect the earlier selected path
		TreePath root = getPathForRow(0);
		Object rootObj = root.getPathComponent(0);
		if (rootObj != null && rootObj instanceof DefaultMutableTreeNode)
		{
			DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)rootObj;
			Object userObj = rootNode.getUserObject();
			if (userObj != null && userObj instanceof IPropertyElement)
			{
				IPropertyElement elem = (IPropertyElement)userObj;
				String eleName = elem.getName();
				if (eleName != null && eleName.equals(name))
				{
					setSelectionRow(0);
					return;
				}
				else
				{
					int count = rootNode.getChildCount();
					boolean found = false;
					for (int i=0; i<count; i++)
					{
						DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)rootNode.getChildAt(i);
						Object childObj = childNode.getUserObject();
						if (childObj != null && childObj instanceof IPropertyElement)
						{
							IPropertyElement childEle = (IPropertyElement)childObj;
							String childName = childEle.getName();
							if (childName != null && childName.equals(name))
							{
								setSelectionRow(i+1);
								found = true;
								break;
							}
						}
					}
					
					if (!found)
					{
						//If we reach here, we should select the first row.
						setSelectionRow(0);
					}
				}
			}
		}
	}   
}



