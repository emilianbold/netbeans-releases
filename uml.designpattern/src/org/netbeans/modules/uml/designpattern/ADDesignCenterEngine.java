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
 * Created on Mar 3, 2004
 *
 */
package org.netbeans.modules.uml.designpattern;

import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.FilteredItemManager;
import java.util.ResourceBundle;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IDesignCenterManager;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel;
import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.ADProjectTreeEngine;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.ui.swing.projecttree.ISwingProjectTreeModel;
import org.netbeans.modules.uml.ui.swing.projecttree.JProjectTree;
import org.netbeans.modules.uml.core.coreapplication.IDesignCenterSupport;

/**
 * @author sumitabhk
 *
 */
public class ADDesignCenterEngine extends ADProjectTreeEngine implements IADDesignCenterEngine
{
	private IDesignCenterManager m_DesignCenterMgr = null;
	private IProjectTreeControl m_ProjectTree = null;

	private static final String BUNDLE_NAME = "org.netbeans.modules.uml.designpattern.Bundle"; //$NON-NLS-1$
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	public long initialize(IProjectTreeControl pParentControl)
	{
		m_ProjectTree = pParentControl;
		ICoreProduct prod = ProductHelper.getCoreProduct();
		if (prod != null)
		{
			m_DesignCenterMgr = null;
			m_DesignCenterMgr = prod.getDesignCenterManager();
			if (m_DesignCenterMgr == null)
			{
				m_DesignCenterMgr = new ADDesignCenterManager();
				prod.setDesignCenterManager(m_DesignCenterMgr);
			}
		}
		return 0;
	}
	/**
	 * Sets the parent tree control for this engine.  This is the backpointer to the tree control
	 * which this engine is controlling.
	 *
	 * @param pParentControl [in] The parent tree control this engine is controlling.
	 */
	public void initialize(IProjectTreeModel pParentControl)
	{
		// Let the base class initialize.  
		super.initializeTreeBuilder(pParentControl);
                // Fixed IZ=119513,119944
                // InitalizeSink() should be executed after initializeFillEditableAndDisplayList()
                initializeFillEditableAndDisplayList();
                initializeFilteredManager();
                initializeSinks();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.IADProjectTreeEngine#showElementType(java.lang.String, boolean)
	 */
	public long showElementType(String sElementTypeName, boolean bShow) {
		// TODO Auto-generated method stub
		return 0;
	}

    public void onNodeExpanding(IProjectTreeControl pParentControl,
                                IProjectTreeExpandingContext pContext,
                                FilteredItemManager manager)
    {
        //handleItemExpanding(pParentControl, pContext);
        if (isDesignCenterTree(pParentControl))
        {
            if (manager == null)
            {
                handleItemExpanding(pParentControl, pContext);
            } else
            {
                handleItemExpanding(pParentControl, pContext, manager);
            }
        }
    }

	public void onNodeDoubleClick(IProjectTreeControl pParentControl,
							  IProjectTreeItem pItem,
							  boolean             isControl,
							  boolean             isShift,
							  boolean             isAlt,
							  boolean             isMeta)
	{
		 if (isDesignCenterTree(pParentControl))
		 {
			// Pass to the base engine, in case it needs to handle it
			super.handleDoubleClick(pParentControl, pItem, isControl, isShift, isAlt, isMeta);
		 }
	}

	/**
	 * This will look for any loaded addins that have been registered with the Design Center
	 * (ie. an entry under the design center registry) and add them to the project tree.
	 *
	 *
	 * @param pParent[in]		The tree item that is to be added to
	 *
	 * @return HRESULT
	 */
	public void addDesignCenterNodes(IProjectTreeItem pParent)
	{
            if (m_DesignCenterMgr != null)
            {
                ISwingProjectTreeModel model = null;
                ITreeItem root = null;
                if (m_ProjectTree instanceof JProjectTree)
                {
                    model = ((JProjectTree)m_ProjectTree).getProjectModel();
                    root = model.getRootItem();
                    root.removeAllChildren();
                }
                
                // get the addin descriptors under the design center key
                //IAddInDescriptor[] pDescriptors = m_DesignCenterMgr.getAddInDescriptors();
                IDesignCenterSupport[] addins = m_DesignCenterMgr.getAddIns();
//			if (pDescriptors != null)
//			{
//				int count = pDescriptors.length;
//				for(int i=0; i<count; i++)
                for(IDesignCenterSupport pAddin : addins)
                {
//					IAddInDescriptor desc = pDescriptors[i];
//					if (desc != null)
//					{
//						// get the actual addin for this descriptor
//						IAddIn pAddin = m_DesignCenterMgr.retrieveAddIn2(desc);
//						if (pAddin != null)
//						{
                    //
                    // Check to see if the addin that we have found supports the DesignCenterSupportGUI
                    // interface.  If it does than it is okay for the design center to add it as a tree
                    // item.
                    //
                    if (pAddin instanceof IDesignCenterSupportGUI)
                    {
                        IDesignCenterSupportGUI pDesignCenterAddin = (IDesignCenterSupportGUI)pAddin;
                        String name = pAddin.getName();
                        String progID = pAddin.getID();
                        IDesignPatternCatalog pCatalog = null;
                        if (pAddin instanceof IDesignPatternCatalog)
                        {
                            pCatalog = (IDesignPatternCatalog)pAddin;
                        }
                        IProjectTreeItem pAdded = null;
                        
                        if (pCatalog != null)
                        {
                            // if we have a catalog
                            // get its workspace
                            // add add a node to the tree
                            IWorkspace pDisp = pCatalog.getWorkspace();
                            if (pDisp != null)
                            {
                                if (model != null)
                                {
                                    pAdded = model.addWorkspace(root, pDisp);
                                }
                            }
                        }
                        else
                        {
                            // regular design center item, so add it
                            if (model != null)
                            {
                                pAdded = model.addItem(pParent, "DesignCenter", name, 1, null, pAddin, progID);
                                pAdded.setAsAddinNode(true);
                                pAdded.setData(pAddin);
                                
                                if(pAddin instanceof IActionProvider)
                                {
                                    IActionProvider provider = (IActionProvider)pAddin;
                                    pAdded.setActions(provider.getActions());
                                }
                            }
                            //pAdded = m_ProjectTree.addItem2(pParent,
//																name, // sText
//																1,    // Sort Priority
//																0, // IElement*
//																progID // Description
//																);
                        }
                        pDesignCenterAddin.setProjectTree(m_ProjectTree);
                        if (pAdded != null)
                        {
                            pAdded.setSecondaryDescription(progID);
                            //m_ProjectTree.setSecondaryDescription(pAdded, progID);
                            //setImage(pAdded, "DesignCenter");
                        }
                    }
//						}
//					}
                }
//			}
            }
        }

	/**
	 *	Have each of the addins take care of populating the passed in tree item based on what
	 * the node is
	 *
	 * @param pParent[in]	The project tree item to add to
	 * @param pAdded[out]	The returned project tree item
	 *
	 * @return HRESULT
	 *
	 */
	public IProjectTreeItem addNode( IProjectTreeItem pParent)
	{
		IProjectTreeItem pAdded = null;
		if (m_DesignCenterMgr != null)
		{
			//
			// Need to get the node in the tree that has the addin information stored in its description
			// so that we know which addin to call
			//
			IProjectTreeItem pAddinNode = getAddInNode(pParent);
			if (pAddinNode != null)
			{
				// Now get the addin that matches the description
				String progId = pAddinNode.getSecondaryDescription();
				IDesignCenterSupport pAddin = m_DesignCenterMgr.getDesignCenterAddIn(progId);
				if (pAddin != null)
				{
					// Does the found addin support either of these two interfaces
					// and if so, ask the addin to populate the tree item
					if (pAddin instanceof IDesignCenterSupportGUI)
					{
						IDesignCenterSupportGUI pDesignCenterAddin = (IDesignCenterSupportGUI)pAddin;
						pDesignCenterAddin.setProjectTree(m_ProjectTree);
						pDesignCenterAddin.populateTreeItem(pParent);
					}
					else if (pAddin instanceof IDesignPatternSupport)
					{
						IDesignPatternSupport pDesignPatternAddin = (IDesignPatternSupport)pAddin;
						pDesignPatternAddin.setProjectTree(m_ProjectTree);
						pDesignPatternAddin.populateTreeItem(pParent);
					}
				}
			}
		}
		return pAdded;
	}

	/**
	 * Given a particular tree item, go up its tree structure until the node is found that represents
	 * the addin.
	 *
	 *
	 * @param pNode[in]				The current node
	 * @param pAddInNode[out]		The node that represents the addin
	 *
	 * @return HRESULT
	 *
	 */
	public IProjectTreeItem getAddInNode( IProjectTreeItem pNode)
	{
		IProjectTreeItem pAddInNode = null;
		IProjectTreeItem pTemp = pNode;
		if (pNode != null)
		{
			String progId = pNode.getSecondaryDescription();
			if (pNode.isAddinNode())
			{
				pAddInNode = pTemp;
			}
			else
			{
				IProjectTreeItem pParent = m_ProjectTree.getParent(pNode);
				pAddInNode = getAddInNode(pParent);
			}
		}
		return pAddInNode;
	}

	/**
	 * Gets the workspace from the product if we are in the project tree
	 * or asks the user defined addin for its workspace if we are in the design center.
	 *
	 *
	 * @param pWorkspace[out]		The workspace that the tree knows about
	 *
	 * @return HRESULT
	 */
	public IWorkspace getWorkspace()
	{
		IWorkspace pWorkspace = null;
		if (m_ProjectTree != null)
		{
//			IProjectTreeItem pWorkItem = m_ProjectTree.getWorkspaceTreeItem();
//			if (pWorkItem != null)
			{
//				String progId = pWorkItem.getSecondaryDescription();
				ICoreProduct pProd = ProductHelper.getCoreProduct();
				if (pProd != null)
				{
					IDesignCenterManager pManager = pProd.getDesignCenterManager();
					if (pManager != null && pManager instanceof IADDesignCenterManager)
					{
						IADDesignCenterManager pADManager = (IADDesignCenterManager)pManager;
						IDesignPatternCatalog pCatalog = pADManager.getDesignPatternCatalog();
						if (pCatalog != null)
						{
							IWorkspace pWork = pCatalog.getWorkspace();
							if (pWork != null)
							{
								pWorkspace = pWork;
							}
						}
					}
				}
			}
		}
		return pWorkspace;
	}

//	public void callAddIn(IProductContextMenu pContextMenu, String progID, boolean bRecurse, boolean bPre)
//	{
//		if (m_DesignCenterMgr != null)
//		{
//			IAddIn pAddin = m_DesignCenterMgr.getDesignCenterAddIn(progID);
//			if (pAddin != null)
//			{
//				if (pAddin instanceof IAddInManager)
//				{
//					IAddInManager pManager = (IAddInManager)pAddin;
//					if (pAddin instanceof IDesignCenterSupportGUI)
//					{
//						IDesignCenterSupportGUI pDesignCenterAddin = (IDesignCenterSupportGUI)pAddin;
//						if (bPre)
//						{
//
//						}
//					}
//					if (bRecurse)
//					{
//						IAddInDescriptor[] pDescriptors = pManager.getAddInDescriptors();
//						if (pDescriptors != null)
//						{
//							for (int i=0; i<pDescriptors.length; i++)
//							{
//								IAddInDescriptor pDesc = pDescriptors[i];
//								String progId2 = pDesc.getProgID();
//								callAddIn(pContextMenu, progId2, bRecurse, bPre);
//							}
//						}
//					}
//				}
//				else
//				{
//					if (pAddin instanceof IDesignCenterSupportGUI)
//					{
//						IDesignCenterSupportGUI pDesignCenterAddin = (IDesignCenterSupportGUI)pAddin;
//						if (bPre)
//						{
//						}
//						else
//						{
//						}
//					}
//					else
//					{
//						// We want Requirements-derived AddIns to keep going up the tree until a Manager is found.
////						if (pAddin instanceof IRequirementsProvider)
////						{
////							return false;
////						}
//					}
//				}
//			}
//		}
//	}

	/**
	 * Handles the after edit event.
	   *
	   * @param pParentControl [in] The tree that caused the event
	   * @param pItem [in] The item being edited
	   * @param pVerify [in] The status after this connection point has finished
	 */
	public void handleItemExpanding(IProjectTreeControl pParentControl,
									   IProjectTreeExpandingContext pContext)
	{
		IProjectTreeItem pItem = pContext.getProjectTreeItem();
		if (pItem != null && isDesignCenterTree(pParentControl))
		{
			//
			// Special processing if the item being expanded is the top level node in the tree
			// "Design Center"
			//
			String name = pItem.getItemText();
			String topNode = RESOURCE_BUNDLE.getString("IDS_DESIGNCENTER2");
			if (name.equals(topNode))
			{
				initialize(pParentControl);
				addDesignCenterNodes(pItem);
			}
			else
			{
				//
				// Not the top level node, so just add it
				//
				IProjectTreeItem pAdded = addNode(pItem);

				//
				// now that we have added the node, set its icon
				//
				if (pAdded != null)
				{
					String sName = pAdded.getItemText();
					//setImage(pAdded, sName);
				}

				// Pass to the base engine, in case it needs to handle it
				super.handleItemExpanding(pParentControl, pContext);
			}
		}
	}
}



