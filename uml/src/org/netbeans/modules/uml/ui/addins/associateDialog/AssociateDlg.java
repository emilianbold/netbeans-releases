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

/*
 * Created on Mar 5, 2004
 *
 */
package org.netbeans.modules.uml.ui.addins.associateDialog;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.application.action.IETContextMenuHandler;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.finddialog.FindResults;
import org.netbeans.modules.uml.ui.support.finddialog.IFindResults;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.openide.util.NbBundle;

/**
 * @author jingmingm
 *
 */
public class AssociateDlg implements IETContextMenuHandler
{
	protected boolean m_FromDiagram = false;
	protected IProjectTreeControl m_ProjectTree = null;
	protected Object m_AssociateContext = null;
	
	/**
	 * Message from the project tree that a context menu is about to be displayed
	 */
	public void onProjectTreeContextMenuPrepare(IProjectTreeControl pControl, IMenuManager pContextMenu)
	{
		// keep track that we are not clicking on a diagram, because this will aid in how the items
		// are retrieved to be associated with
		m_FromDiagram = false;
		m_AssociateContext = pControl;
		
		// add the menu item
		addAssociateMenuItem(pContextMenu);
	}
	
	/**
	 * Message from the drawing area that a context menu is about to be displayed.
	 *
	 * @param pParentDiagram [in] The diagram where the context menu is about to appear.
	 * @param contextMenu [in] The context menu to add or remove buttons from.
	 */
	public void onDrawingAreaContextMenuPrepare(IDiagram pDiagram, IMenuManager pContextMenu)
	{
		// do not display the associate with for a read only diagram
		boolean bReadOnly = pDiagram.getReadOnly();
		if (!bReadOnly)
		{
			// keep track that we are clicking on a diagram, because this will aid in how the items
			// are retrieved to be associated with
			m_FromDiagram = true;
			m_AssociateContext = pDiagram;
			
			// add the menu item
			addAssociateMenuItem(pContextMenu);
		}
	}
	
	/**
	 * Add the associate menu to the project tree menu
	 *
	 * @param[in]	pContextMenu	The current context menu
	 *
	 * @return HRESULT
	 */
	protected void addAssociateMenuItem(IMenuManager pContextMenu)
	{
		// Fix W6157:  Do not add the menu item if the item is a label
//		Object cpItemClickedOn = pContextMenu.getContextObject();
//		if( !(cpItemClickedOn instanceof TSLabel))
//		{
//			pContextMenu.add(createMenuAction(LabelManager.loadString("IDS_POPUP_ASSOCIATE"), "MBK_ASSOCIATEDLG_ASSOCIATEWITH"));
//		}
                pContextMenu.add(createMenuAction(NbBundle.getMessage(
                                AssociateDlg.class, "IDS_POPUP_ASSOCIATE"),
                                "MBK_ASSOCIATEDLG_ASSOCIATEWITH"));
	}

	public ContextMenuActionClass createMenuAction(String text, String menuID)
	{
		return new ContextMenuActionClass(this, text, menuID);
	}
	
	public boolean setSensitivityAndCheck(String menuID, ContextMenuActionClass pMenuAction)
	{
		boolean bEnable = false;
		
		// Is it the project tree
		if (m_FromDiagram == false && m_AssociateContext instanceof IProjectTreeControl)
		{
			IProjectTreeControl pControl = (IProjectTreeControl)m_AssociateContext;
			// we are in the project tree, but there are two project trees, so if we are in the
			// design center, we don't want to enable this button if we are in a shipped project
			// vs. a user project, so all this code is checking to see if any of the selected
			// items in the tree belong to an etpat project
			boolean continueFlag = true;
			String mgrName = pControl.getConfigMgrName();
			if (mgrName.equals("DesignCenter")) // NOI18N
			{
				boolean isSel = isProjectTreeItemSelected(pControl);
				if (isSel)
				{
					IProjectTreeItem[] pTreeItems = pControl.getSelected();
					if (pTreeItems != null)
					{
						int count = pTreeItems.length;
						for (int x = 0; x < count; x++)
						{
							IProjectTreeItem pTreeItem = pTreeItems[x];
							if (pTreeItem != null)
							{
								IProject pProject = pControl.retrieveProjectFromItem(pTreeItem);
								if (pProject != null)
								{
									boolean bMember = isMemberOfDesignCenterProject(pProject);
									if (bMember)
									{
										// it is a member of an etpat file so bail and do not
										// enable the associate with button
										continueFlag = false;
										break;
									}
								}
							}						
						}
					}
				}
				else
				{
					// the tree item is in the design center, but is not a designated project
					// tree item, so it is probably a macro catalog item or a requirement
					// bail
					continueFlag = false;
				}
			}
			if (continueFlag)
			{
				// not enabling if workspace is selected
				boolean isWork = isWorkspaceSelected(pControl);
				if (!isWork)
				{
					// not enabling if a folder is selected
					boolean isFolder = isFolderSelected(pControl);
					if (!isFolder)
					{
						// not enabling if a closed project is selected
						String sFirstSelectedClosedProject = pControl.getFirstSelectedClosedProject();
						if (sFirstSelectedClosedProject == null || sFirstSelectedClosedProject.length() == 0)
						{
							IProjectTreeItem[] pTreeItems = pControl.getSelected();
							if (pTreeItems != null)
							{
								int count = pTreeItems.length;
								if (count > 0)
								{
									bEnable = true;
								}
							}
						}
					}
				}
			}
		}
		else
		{
			// in diagram area, not enabling if nothing selected on diagram
			IProduct pProduct = ProductHelper.getProduct();
			if (pProduct != null)
			{
				// need the diagram manager to get the current diagram
				IProductDiagramManager pDiagramMgr = pProduct.getDiagramManager();
				if (pDiagramMgr != null)
				{
					// get the current diagram
					IDiagram pDiagram = pDiagramMgr.getCurrentDiagram();
					if (pDiagram != null)
					{
						// get the items selected on the diagram
						ETList<IPresentationElement> pSelected = pDiagram.getSelected();
						if (pSelected != null)
						{
							// get what is selected
							int count = pSelected.size();
							if (count > 0)
							{
								bEnable = true;
							}
						}
					}
				}
			}
		}
				 
		return bEnable;
	}

	public boolean onHandleButton(ActionEvent e, String menuID)
	{
		boolean bHandled = false; 
		
		if (menuID.equals("MBK_ASSOCIATEDLG_ASSOCIATEWITH")) // NOI18N
		{
			handleAssociate();
			bHandled = true;
		}
		
		return bHandled;
	}
	
	protected void handleAssociate()
	{
		// get the items that will become the referencing element in the reference relationship
		// this will be what was selected in the tree or on the diagram
		IFindResults pResults = getReferencingItems();
		if (pResults != null)
		{
			// Display the GUI to the user
			Frame parent = ProductHelper.getProxyUserInterface().getWindowHandle();
			AssociateDialogUI pGUI = new AssociateDialogUI(parent, true);
			if (pGUI != null)
			{
				pGUI.setResults(pResults);
				
				// try and determine what project the stuff was selected is in because
				// that is needed by the "find" controller to do the search
				// if there isn't one (or it can't be determined), tell the user that we
				// are unable to do the associate
				IProject pProject = getProject(pResults);
				if (pProject != null)
				{
					pGUI.setProject(pProject);
					pGUI.doLayout();
					pGUI.setModal(true);
					pGUI.setVisible(true);
				}
				else
				{
//					String 	CComBSTR str;
//					VERIFY(str.LoadString(IDS_MULTIPLEPROJECTS));
//					CComBSTR str2;
//					VERIFY(str2.LoadString(IDS_PROJNAME2));
//					CComPtr < IErrorDialog > pTemp;
//					_VH(pTemp.CoCreateInstance(__uuidof(ErrorDialog)));
//					if (pTemp)
//					{
//						_VH(pTemp->Display3(str, EDIK_ICONINFORMATION, long(0), str2));
//					}
				}
			}
		}
	}
	
	/**
	 * Builds a list of selected elements either from the project tree or the diagram
	 * 
	 *
	 * @param pContextMenu[in]
	 * @param pElements[out]
	 *
	 * @return HRESULT
	 *
	 */
	protected IFindResults getReferencingItems()
	{
		IFindResults pResults = new FindResults();
		ETList<IElement> pElements = pResults.getElements();
		ETList<IProxyDiagram> pDiagrams = pResults.getDiagrams();
		if (pElements != null && pDiagrams != null)
		{
			// see if we are in the project tree
			if (m_FromDiagram == false && m_AssociateContext instanceof IProjectTreeControl)
			{
				IProjectTreeControl pTree = (IProjectTreeControl)m_AssociateContext;
				// came from the project tree, so that which is selected will be
				// what is selected in the tree
				IProjectTreeItem[] pTreeItems = pTree.getSelected();
				if (pTreeItems != null)
				{
					int count = pTreeItems.length;
					for (int x = 0; x < count; x++)
					{
						IProjectTreeItem pTreeItem = pTreeItems[x];
						if (pTreeItem != null)
						{
							IProxyDiagram pProxyDiagram = pTreeItem.getDiagram();
							if (pProxyDiagram != null)
							{
								pDiagrams.add(pProxyDiagram);
							}
							else
							{
								IElement pElement = pTreeItem.getModelElement();
								if (pElement != null)
								{
									pElements.add(pElement);
								}
							}
						}
					}
				}
			}
			else
			{
				// came from the drawing area, so what is selected is either what is
				// selected on the diagram, or the diagram itself
				IProduct pProduct = ProductHelper.getProduct();
				if (pProduct != null)
				{
					// need the diagram manager to get the current diagram
					IProductDiagramManager pDiagramMgr = pProduct.getDiagramManager();
					if (pDiagramMgr != null)
					{
						// get the current diagram
						IDiagram pDiagram = pDiagramMgr.getCurrentDiagram();
						if (pDiagram != null)
						{
							// get the items selected on the diagram
							ETList<IPresentationElement> pSelected = pDiagram.getSelected();
							if (pSelected != null)
							{
								// loop through what is selected
								int count = pSelected.size();
								for (int x = 0; x < count; x++)
								{
									IPresentationElement pSelected2 = pSelected.get(x);
									if (pSelected2 != null)
									{
										// get the element from the presentation
										IElement pSubject = pSelected2.getFirstSubject();
										if (pSubject != null)
										{
											pElements.add(pSubject);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return pResults;
	}
	
	/**
	 * Get the project of what is selected in the tree.  If multiple projects
	 * are selected, we are just getting the first one
	 *
	 * @param pElements[in]			The elements that are selected
	 * @param pProject[out]			The found project
	 *
	 * return HRESULT
	 */
	protected IProject getProject(IFindResults pResults)
	{
		IProject pProject =  null;
		
		ETList<IElement> pElements = pResults.getElements();
		ETList<IProxyDiagram> pDiagrams = pResults.getDiagrams();
		if (pElements != null && pDiagrams != null)
		{
			int count = pElements.size();
			for (int x = 0; x < count; x++)
			{
				IElement pElement = pElements.get(x);
				if (pElement != null)
				{
					IProject pProj = pElement.getProject();
					if (pProj != null)
					{
						if (pProject == null)
						{
							pProject = pProj;
						}
						else if (pProject.equals(pProj))
						{
							//pProject = pProj;
							continue;
						}
						else
						{
							pProject = null;
							break;
						}
					}
				}
			}
			if ( (pProject == null) && (count == 0) )
			{
				// if the elements logic has not already figured out that we have mixed
				// projects or there weren't any elements then process for the diagrams
				int count1 = pDiagrams.size();
				for (int x = 0; x < count1; x++)
				{
					IProxyDiagram pDiagram = pDiagrams.get(x);
					if (pDiagram != null)
					{
						IProject pProj = pDiagram.getProject();
						if (pProj != null)
						{
							if (pProject == null)
							{
								pProject = pProj;
							}
							else if (pProject.equals(pProj))
							{
								//pTemp = pProj;
								continue;
							}
							else
							{
								pProject = null;
								break;
							}
						}
					}
				}
			}
		}

		return pProject;
	}
	
	/**
	 * Determines whether or not a project tree item is selected in the tree
	 *
	 * @param pControl[in]			The tree control
	 * @param bSel[out]				Whether or not a project tree item is selected in the tree
	 *
	 * return HRESULT
	 */
	protected boolean isProjectTreeItemSelected(IProjectTreeControl pControl)
	{
		boolean bSel = true;

		// get what is selected in the tree
		IProjectTreeItem[] pTreeItems = pControl.getSelected();
		if (pTreeItems != null)
		{
			int count = pTreeItems.length;
			for (int x = 0; x < count; x++)
			{
				// get the selected item
				IProjectTreeItem pTreeItem = pTreeItems[x];
				if (pTreeItem != null)
				{
					ITreeItem pDisp = pTreeItem.getProjectTreeSupportTreeItem();
					if (pDisp == null)
					{
						bSel = false;
						break;
					}
				}
			}
		}

		return bSel;
	}
	
	/**
	 * Determines whether the passed in element is a member of an etpat project file
	 * 
	 *
	 * @param pElement[in]			The element in question
	 * @param bMember[out]			Whether or not it is a member of an etpat project file
	 *
	 * @return HRESULT
	 *
	 */
	protected boolean isMemberOfDesignCenterProject(IElement pElement)
	{
		boolean bMember = false;
		IProject pProject = null;

		if (pElement instanceof IProject)
		{
			pProject = (IProject)pElement;
		}
		else
		{
			pProject = pElement.getProject();
		}
		
		if (pProject != null)
		{
			String fileName = pProject.getFileName();
			if(fileName != null && fileName.length() > 0)
			{
				String xsExtension = FileSysManip.getExtension(fileName);
				if (xsExtension.equals(FileExtensions.PATTERN_EXT_NODOT))
				{
					bMember = true;
				}
			}
		}

		return bMember;
	}
	
	/**
	 * Determines whether or not the workspace is selected in the tree
	 *
	 * @param pControl[in]			The tree control
	 * @param bSel[out]				Whether or not the workspace is selected in the tree
	 *
	 * return HRESULT
	 */
	protected boolean isWorkspaceSelected(IProjectTreeControl pControl)
	{
		boolean bSel = false;

		// get what is selected in the tree
		IProjectTreeItem[] pTreeItems = pControl.getSelected();
		if (pTreeItems != null)
		{
			int count = pTreeItems.length;
			for (int x = 0; x < count; x++)
			{
				// get the selected item
				IProjectTreeItem pTreeItem = pTreeItems[x];
				if (pTreeItem != null)
				{
					boolean isWork = pTreeItem.isWorkspace();
					if (isWork)
					{
						bSel = true;
						break;
					}
				}
			}
		}
		return bSel;
	}
	
	/**
	 * Determines whether or not a folder node is selected in the tree
	 *
	 * @param pControl[in]			The tree control
	 * @param bSel[out]				Whether or not a folder is selected in the tree
	 *
	 * return HRESULT
	 */
	protected boolean isFolderSelected(IProjectTreeControl pControl)
	{
		boolean bSel = false;

		// get what is selected in the tree
		IProjectTreeItem[] pTreeItems = pControl.getSelected();
		if (pTreeItems != null)
		{
			int count = pTreeItems.length;
			for (int x = 0; x < count; x++)
			{
				// get the selected item
				IProjectTreeItem pTreeItem = pTreeItems[x];
				if (pTreeItem != null)
				{
					ITreeItem pDisp = pTreeItem.getProjectTreeSupportTreeItem();
					if (pDisp != null)
					{
						bSel = true;
						break;
					}
				}
			}
		}

		return bSel;
	}
}



