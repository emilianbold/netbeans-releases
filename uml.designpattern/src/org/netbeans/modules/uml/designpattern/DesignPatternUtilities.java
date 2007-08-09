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


package org.netbeans.modules.uml.designpattern;

import org.netbeans.modules.uml.ui.products.ad.applicationcore.IADProduct;
import java.awt.Component;
import java.awt.Cursor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;

import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IDesignCenterManager;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.commondialogs.IErrorDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuSelectionHandler;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingErrorDialog;
import org.netbeans.modules.uml.ui.swing.projecttree.JProjectTree;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager;

public class DesignPatternUtilities
{
	public static boolean m_bWaiting = false;
	/**
	 *
	 */
	public DesignPatternUtilities()
	{
		super();
	}

	/**
	 * Get the projects in the current workspace and populate the list box with the
	 * results.
	 *
	 * @return HRESULT
	 */
	public static void populateProjectListWithUserProjects(JComboBox pCombo, boolean bRemove)
	{
		// get the core product
            ICoreProduct pCoreProduct = ProductHelper.getCoreProduct();
            if (pCoreProduct instanceof  IADProduct)
            {
                IADProduct adProduct = (IADProduct)pCoreProduct;
                IProductProjectManager manager = adProduct.getProjectManager();
                ArrayList < IProject > projects = manager.getOpenProjects();
//                IProject curProject = manager.getCurrentProject();
//                String curName = "";
//                if(curProject != null)
//                {
//                    curName = curProject.getName();
//                }
                
                if (projects != null)
                {
                    if (bRemove)
                    {
                        pCombo.removeAllItems();
                    }
                    DefaultComboBoxModel theModel = (DefaultComboBoxModel)pCombo.getModel();
                    if (theModel == null)
                    {
                        theModel = new DefaultComboBoxModel();
                    }
                    Vector<String> vStrings = new Vector<String>();
                    // loop through
                    int numOfProjects = projects.size();
                    int selectedIndex = -1;
                    for (int x = 0; x < numOfProjects; x++)
                    {
                        IProject project = projects.get(x);
                        if (project != null)
                        {
                            // get their names
                            String name = project.getName();
                            if (name != null && name.length() > 0)
                            {
//                                if(name.equals(curName) == true)
//                                {
//                                    selectedIndex = x;
//                                }
                                
                                // add it to the list
                                vStrings.add(name);
                            }
                        }
                    }
                    sortStringInfo(theModel, vStrings);
                    pCombo.setModel(theModel);
//                    pCombo.setSelectedIndex(selectedIndex);
                }
            }
	}
        
	/**
	 * Get the projects in the current workspace and populate the list box with the
	 * results.
	 *
	 * @return HRESULT
	 */
	public static void populateProjectListWithDesignCenterProjects(JComboBox pCombo, boolean bPromote)
	{
		// also get any projects that the user defined addin workspace has in it
		IWorkspace pWorkspace = getDesignPatternCatalogWorkspace();
		if (pWorkspace != null)
		{
			ETList <IWSProject> pProjects = pWorkspace.getWSProjects();
			if (pProjects != null)
			{
				DefaultComboBoxModel theModel = new DefaultComboBoxModel();
				Vector<String> vStrings = new Vector<String>();
				// loop through
				int numOfProjects = pProjects.size();
				for (int x = 0; x < numOfProjects; x++)
				{
					IWSProject pProject = pProjects.get(x);
					if (pProject != null)
					{
						// get their names, but only if they are user defined, not our shipped
						String name = pProject.getName();
					   if (bPromote)
					   {
						  // if we are promoting, then we only want to display user defined
						  String file = pProject.getLocation();
						  String xsExtension = FileSysManip.getExtension( file );
						  if (xsExtension != null && (!(xsExtension.equals(FileExtensions.PATTERN_EXT_NODOT))))
						  {
							  if (name != null && name.length() > 0)
							  {
								  // add it to the list
								  vStrings.add(name);
							  }
					  	  }
				   		}
				   		else
				   		{
							// add it to the list
							vStrings.add(name);
				   		}
					}
				}
				sortStringInfo(theModel, vStrings);
				pCombo.setModel(theModel);
				pCombo.setSelectedIndex(-1);
			}
		}
	}
	private static void sortStringInfo(DefaultComboBoxModel theModel, Vector<String> v)
	{
		if (theModel != null)
		{
			TreeMap map = new TreeMap();
			int cnt = v.size();
			for (int x = 0; x < cnt; x++)
			{
				String str = v.get(x);
                                if (str != null)
                                    map.put(str, str);
			}
			Set s = map.keySet();
			Object[] o = s.toArray();
			int cnt2 = o.length;
			for (int y = 0; y < cnt2; y++)
			{
				Object obj = o[y];
				theModel.addElement(obj);
			}
		}
	}
	/**
	 * Event called when an entry in the project list box changes
	 *
	 * @return HRESULT
	 */
	public static IProject onSelChangeProjectList(String sName, JDialog pDialog)
	{
		IProject pProject = null;
		if (sName != null)
		{
			// get our application
			IApplication pApp = ProductHelper.getApplication();
			if (pApp != null)
			{
				if (pApp != null)
				{
					// open the project matching the list entry
					// get the current workspace
					IWorkspace pWorkspace = ProductHelper.getWorkspace();
					if (pWorkspace != null)
					{
						startWaitCursor(pDialog);
						EventBlocker.startBlocking();
						pProject = pApp.openProject(pWorkspace, sName);
						EventBlocker.stopBlocking(false);
						endWaitCursor(pDialog);
					}
					if (pProject == null)
					{
						// if we couldn't find the project in the application (GoF)
						// then look in user defined workspace
						IWorkspace pUserWork = getDesignPatternCatalogWorkspace();
						if (pUserWork != null)
						{
							startWaitCursor(pDialog);
							EventBlocker.startBlocking();
							pProject = pApp.openProject(pUserWork, sName);
							if (pProject == null)
							{
								// we were unable to open the project so tell the user
								IErrorDialog pTemp = new SwingErrorDialog(pDialog);
								if (pTemp != null)
								{
									String str = translateString("IDS_OPENPROJECTFAILED");
									str = StringUtilities.replaceAllSubstrings(str, "%s", sName);
									pTemp.display(str, MessageIconKindEnum.EDIK_ICONERROR, "");
								}
							}
							endWaitCursor(pDialog);
							EventBlocker.stopBlocking(false);
						}
					}
				}
			}
		}
	   return pProject;
	}
	/**
	 * Get the packages in the current project and populates the list box with the
	 * results.
	 *
	 * @return HRESULT
	 */
	public static void populateNamespaceList(JComboBox pCombo, IProject pProject)
	{
		if (pProject != null)
		{
			// remove any previous information in the list
			pCombo.removeAllItems();
			DefaultComboBoxModel theModel = new DefaultComboBoxModel();
			Vector<String> vStrings = new Vector<String>();
			// get the package element names
			String pattern = "//UML:Element.ownedElement/UML:Package";
			ETList<String> pStrings = getElementNames(pProject, pattern, false);
			if (pStrings != null)
			{
				// add a blank to the combo so that the user can blank it out if
				// necessary, since we will be defaulting it for them
				vStrings.add(" ");
				// loop through the results and add them to the list box
				int count = pStrings.size();
				for (int x = 0; x < count; x++)
				{
					String str = pStrings.get(x);
					vStrings.add(str);
				}
				sortStringInfo(theModel, vStrings);
				pCombo.setModel(theModel);
			}
		}
	}
	/**
	 * Based on the selected namespace in the list box, retrieve the corresponding
	 * COM Object.  Right now, we are only looking for packages.
	 *
	 * @param[out] pNamespace		The found package
	 *
	 * @return HRESULT
	 */
	public static INamespace getSelectedNamespace(JComboBox pCombo, IProject pProject)
	{
		INamespace pNamespace = null;
		if (pProject != null)
		{
			// what is in the list box
			String selText = (String)pCombo.getSelectedItem();
			if (selText != null && selText.length() > 0)
			{
				// find a corresponding com object
				String pattern = "//UML:Package";
				// get a element locator object
				IElementLocator pLocator = new ElementLocator();
				if (pLocator != null)
				{
					// ask it for the elements
					ETList <IElement> pElements = pLocator.findElementsByDeepQuery(pProject, pattern);
					if (pElements != null)
					{
						// loop through the found elements
						int count = pElements.size();
						for (int x = 0; x < count; x++)
						{
							IElement pElement = pElements.get(x);
							if (pElement != null)
							{
								// only concerned with the first found element that is a
								// namespace
								if (pElement instanceof INamespace)
								{
									INamespace pNamed = (INamespace)pElement;
									String fullName = pNamed.getQualifiedName();
									if (fullName.equals(selText))
									{
										pNamespace = pNamed;
										break;
									}
								}
							}
						}
					}
				}
			}
		}
	   return pNamespace;
	}
	/**
	 * Use the ElementLocator to retrieve a list of elements(where we can get their names)
	 * based on the passed in project and xpath query
	 *
	 * @param[in] pProject		The project to query
	 * @param[in] pattern		The xpath query
	 * @param[out] pStrings		A list of element names that matched the query
	 * @param[out] pStrings2	A list of element ids that matched the query
	 *
	 * @return HRESULT
	 */
	public static ETList<String> getElementNames(IProject pProject, String pattern, boolean bGetIDs)
	{
		ETList<String> pStrings = new ETArrayList<String>();
		if (pProject != null && pattern != null)
		{
			// create a locator object
			IElementLocator pLocator = new ElementLocator();
			if (pLocator != null)
			{
				// ask it for the matching elements
				ETList <IElement> pElements = pLocator.findElementsByDeepQuery(pProject, pattern);
				if (pElements != null)
				{
					// loop through the found elements
					int count = pElements.size();
					for (int x = 0; x < count; x++)
					{
						IElement pElement = pElements.get(x);
						if (pElement != null)
						{
							// only want those that are NamedElements
							if (pElement instanceof INamedElement)
							{
								INamedElement pNamed = (INamedElement)pElement;
								String id = pNamed.getXMIID();
								String name = pNamed.getQualifiedName();
								if (bGetIDs){
									pStrings.add(id);
								}
								else{
									pStrings.add(name);
								}
							}
						}
					}
				}
			}
		}
	   return pStrings;
	}
	/**
	 *
	 * Retrieves the collaboration object by the passed in name
	 *
	 * @param id[in]				The id of the pattern to retrieve
	 * @param pProject[in]		The project to look in
	 * @param pCollab[out]		The pattern
	 *
	 * @return HRESULT
	 *
	 */
	public static ICollaboration getPatternByID(String id, IProject pProject)
	{
		ICollaboration pCollab = null;
		if (pProject != null && id != null)
		{
			// create a locator object
			IElementLocator pLocator = new ElementLocator();
			if (pLocator != null)
			{
				// ask it for the matching elements
				String pattern = "//UML:Collaboration[@xmi.id=\'" + id + "\']";
				ETList <IElement> pElements = pLocator.findElementsByDeepQuery(pProject, pattern);
				if (pElements != null)
				{
					// loop through the found elements
					int count = pElements.size();
					if (count > 0)
					{
						IElement pElement = pElements.get(0);
						if (pElement instanceof ICollaboration)
						{
							pCollab = (ICollaboration)pElement;
						}
					}
				}
			}
	   }
	   return pCollab;
	}
	/**
	 * Determines whether or not a menu item should be displayed based on whether
	 * or not a collaboration is selected (and is the only thing selected)
	 *
	 *
	 * @param pContextMenu[in]	The menu that needs the menu item
	 * @param bDisplay[out]		Whether or not a collaboration is selected
	 *
	 * @return HRESULT
	 *
	 */
	public static boolean onlyCollaborationSelected(IProductContextMenu pContextMenu)
	{
		boolean bDisplay = false;
		if (pContextMenu != null)
		{
			Object pDisp = pContextMenu.getParentControl();
			if (pDisp != null)
			{
				// do we come from a tree control
				if (pDisp instanceof IProjectTreeControl)
				{
					IProjectTreeControl pTree = (IProjectTreeControl)pDisp;
					IProjectTreeItem[] pTreeItems = pTree.getSelected();
					if (pTreeItems != null)
					{
						// if only one thing is selected
						int count = pTreeItems.length;
						if (count == 1)
						{
							// get the tree item
							IProjectTreeItem pTreeItem = pTreeItems[0];
							if (pTreeItem != null)
							{
								// get the model element from the tree item
								IElement pElement = pTreeItem.getModelElement();
								if (pElement != null)
								{
									// is the model element a collaboration
									if (pElement instanceof ICollaboration)
									{
										ICollaboration pCollab = (ICollaboration)pElement;
										// now do one more check to see if the collaboration has template parameters
										// because it is a pattern if it does
										ETList<IParameterableElement> pParams = pCollab.getTemplateParameters();
										if (pParams != null)
										{
											int pcount = pParams.size();
											if (pcount > 0)
											{
												bDisplay = true;
											}
										}
									}
								}
							}
						}
					}
				}
				else
				{
					// came from the diagram, so do the same check but check what is selected
					// on the diagram
					ETList <IPresentationElement> pSelecteds = getSelectedOnCurrentDiagram();
					if (pSelecteds != null)
					{
						int count = pSelecteds.size();
						for (int x = 0; x < count; x++)
						{
							// get the tree item
							IPresentationElement pPres = pSelecteds.get(x);
							if (pPres != null)
							{
								// get the model element from the tree item
								IElement pElement = pPres.getFirstSubject();
								if (pElement != null)
								{
									// is the model element a classifier
									// because if it is not, then we are not going to enable the menu
									// because as of right now the only thing that can have patterns
									// applied to it are classifiers
									if (pElement instanceof ICollaboration)
									{
										bDisplay = true;
										break;
									}
								}
							}
						}
					}
				}
			}
	   }
	   return bDisplay;
	}
	/**
	 * Determines whether or not a menu item should be displayed based on whether
	 * or not classifiers are selected (and are the only thing selected).
	 *
	 * Also allowing packages to be selected.
	 *
	 *
	 * @param pContextMenu[in]	The menu that needs the menu item
	 * @param bDisplay[out]		Whether or not classifiers are selected
	 *
	 * @return HRESULT
	 *
	 */
	public static boolean onlyClassifiersSelected(IProductContextMenu pContextMenu)
	{
		boolean bDisplay = false;
		if (pContextMenu != null)
		{
			Object pDisp = pContextMenu.getParentControl();
			if (pDisp != null)
			{
				// do we come from a tree control
				if (pDisp instanceof IProjectTreeControl)
				{
					IProjectTreeControl pTree = (IProjectTreeControl)pDisp;
					IProjectTreeItem[] pTreeItems = pTree.getSelected();
					if (pTreeItems != null)
					{
						int count = pTreeItems.length;
						for (int x = 0; x < count; x++)
						{
							// get the tree item
							IProjectTreeItem pTreeItem = pTreeItems[x];
							if (pTreeItem != null)
							{
								// get the model element type from the tree item.  We don't get the
						 		// model element itself because loading the actual IElement is way
						 		// slow compared to just getting the type.
								String sElementType = pTreeItem.getModelElementMetaType();
								if (sElementType != null && sElementType.length() > 0)
								{
									// is the model element a classifier
									// because if it is not, then we are not going to enable the menu
									// because as of right now the only thing that can have patterns
									// applied to it are classifiers
									if (sElementType.equals("PartFacade"))
									{
										bDisplay = false;
										break;
									}
									else
									{
										if (sElementType.equals("Class") ||
											sElementType.equals("Actor") ||
											sElementType.equals("Artifact") ||
											sElementType.equals("DataType") ||
											sElementType.equals("DerivationClassifier") ||
											sElementType.equals("Interface") ||
											sElementType.equals("Behavior") ||
											sElementType.equals("Signal") ||
											sElementType.equals("Association") ||
											sElementType.equals("Aggregation") ||
											sElementType.equals("AssociationClass") ||
											sElementType.equals("UseCase") ||
											sElementType.equals("Enumeration") ||
											sElementType.equals("AliasedType") ||
											sElementType.equals("Node") ||
											sElementType.equals("Activity") ||
											sElementType.equals("StateMachine") ||
											sElementType.equals("Collaboration") ) // IClassifiers we can draw
										{
											bDisplay = true;
										}
										else if ( sElementType.equals("Package") )
										{
											bDisplay = true;
										}
										else
										{
											bDisplay = false;
											break;
										}
									}
								}
								else
								{
									bDisplay = false;
									break;
								}
							}
						}
					}
				}
				else
				{
					// came from the diagram, so do the same check but check what is selected
					// on the diagram
					ETList<IPresentationElement> pSelecteds = getSelectedOnCurrentDiagram();
					if (pSelecteds != null)
					{
						int count = pSelecteds.size();
						for (int x = 0; x < count; x++)
						{
							// get the tree item
							IPresentationElement pPres = pSelecteds.get(x);
							if (pPres != null)
							{
								// get the model element from the tree item
								IElement pElement = pPres.getFirstSubject();
								if (pElement != null)
								{
									// is the model element a classifier
									// because if it is not, then we are not going to enable the menu
									// because as of right now the only thing that can have patterns
									// applied to it are classifiers
									if (pElement instanceof IPartFacade)
									{
										bDisplay = false;
										break;
									}
									else
									{
										if (pElement instanceof IClassifier)
										{
											bDisplay = true;
										}
										else if (pElement instanceof IPackage)
										{
											bDisplay = true;
										}
										else
										{
											bDisplay = false;
											break;
										}
									}
								}
								else
								{
									bDisplay = false;
									break;
								}
							}
						}
					}
				}
			}
	   }
	   return bDisplay;
	}
	/**
	 * Helper function to get what is selected on the current diagram
	 *
	 *
	 * @param pElements[out]
	 *
	 * @return HRESULT
	 *
	 */
	public static ETList<IPresentationElement> getSelectedOnCurrentDiagram()
	{
		ETList < IPresentationElement > pElements = new ETArrayList<IPresentationElement>();
		// came from the diagram, so do the same check but check what is selected
		// on the diagram
		IProduct pProduct      = ProductHelper.getProduct();
		if (pProduct != null)
		{
			 IProductDiagramManager pDiagramMgr = pProduct.getDiagramManager();
			 if (pDiagramMgr != null)
			 {
				IDiagram pDiagram = pDiagramMgr.getCurrentDiagram();
				if (pDiagram != null)
				{
					pElements = pDiagram.getSelected();
				}
			}
	   }
	   return pElements;
	}
	/**
	 * Get the selected collaboration in the tree or diagram associated with the context menu
	 *
	 * @param pContextMenu[in]			The current context menu
	 * @param pCollab[out]				The pattern that is selected in the tree
	 *
	 * return HRESULT
	 */
	public static ICollaboration getSelectedCollaboration(Object pDisp)
	{
		ICollaboration pCollab = null;
		if (pDisp != null)
		{
			// do we come from a tree control
			if (pDisp instanceof IProjectTreeControl)
			{
				IProjectTreeControl pTree = (IProjectTreeControl)pDisp;
				IProjectTreeItem[] pTreeItems = pTree.getSelected();
				if (pTreeItems != null)
				{
					int count = pTreeItems.length;
					// we are only going to process this if there is one thing selected
					if (count == 1)
					{
						// get the selected item
						IProjectTreeItem pTreeItem = pTreeItems[0];
						if (pTreeItem != null)
						{
							// get the model element associated with the tree item
							IElement pElement = pTreeItem.getModelElement();
							if (pElement != null)
							{
								// is the model element a collaboration
								if (pElement instanceof ICollaboration)
								{
									pCollab = (ICollaboration)pElement;
								}
							}
						}
					}
				}
			}
			else
			{
				// came from the diagram, so do the same check but check what is selected
				// on the diagram
				ETList<IPresentationElement> pSelecteds = getSelectedOnCurrentDiagram();
				if (pSelecteds != null)
				{
					int count = pSelecteds.size();
					if (count == 1)
					{
						// get the tree item
						IPresentationElement pPres = pSelecteds.get(0);
						if (pPres != null)
						{
							// get the model element from the tree item
							IElement pElement = pPres.getFirstSubject();
							if (pElement != null)
							{
								// is the model element a collaboration
								if (pElement instanceof ICollaboration)
								{
									pCollab = (ICollaboration)pElement;
								}
							}
						}
					}
				}
			}
	   }
	   return pCollab;
	}
	/**
	 * Helper method to retrieve the workspace that is housed in the user defined
	 * pattern addin
	 *
	 *
	 * @param pWorkspace[out]		The workspace that the user defined pattern addin manages
	 *
	 * @return HRESULT
	 *
	 */
	public static IWorkspace getDesignPatternCatalogWorkspace()
	{
		IWorkspace pWorkspace = null;
		ICoreProduct pCore = ProductHelper.getCoreProduct();
		if (pCore != null)
		{
			IDesignCenterManager pManager = pCore.getDesignCenterManager();
                        if(pManager == null)
                        {
                           IDesignCenterManager m_DesignCenterMgr = new ADDesignCenterManager();
                           pCore.setDesignCenterManager(m_DesignCenterMgr);
                           pManager = m_DesignCenterMgr;
                        }


			if (pManager != null)
			{
				if (pManager instanceof IADDesignCenterManager)
				{
					IADDesignCenterManager pADManager = (IADDesignCenterManager)pManager;
					IDesignPatternCatalog pCatalog = pADManager.getDesignPatternCatalog();
					if (pCatalog != null)
					{
						Object pDisp = pCatalog.getWorkspace();
						if (pDisp instanceof IWorkspace)
						{
							pWorkspace = (IWorkspace)pDisp;
						}
					}
				}
			}
	   }
	   return pWorkspace;
	}
	/**
	 * Adds the "Apply..." menu item to the right click menu in the design center
	 * tree
	 *
	 * @param pContextMenu[in]			The current context menu
	 *
	 * return HRESULT
	 */
	public static void addApplyMenuItem(IProductContextMenu pContextMenu, IProductContextMenuSelectionHandler pHandler)
	{
		if (pContextMenu != null && pHandler != null)
		{
			// get the text for the apply button
			String name = translateString("IDS_POPUP_APPLY");
			// determine whether or not this button should be greyed out or not
			boolean bSensitive = true;
			// create the menu item that we will be adding
			IProductContextMenuItem pApplyMenuItem = null;
			// I'm creating a fake MBK value here so the the menu sorter
		  	// can sort the menu.
			

			pApplyMenuItem = DesignCenterUtilities.createMenuItemOnMain(pContextMenu,
							   name,
							   "MBK_DESIGN_PATTERN_APPLY",
							   bSensitive,
							   pHandler);
		  	if (pApplyMenuItem != null)
		  	{
				IProductContextMenuItem pSubMenuItem = null;
				String name2 = translateString("IDS_POPUP_DESIGNPATTERN");
				pSubMenuItem = DesignCenterUtilities.createMenuItemOnSub(pApplyMenuItem,
											name2,
											"MBK_DESIGN_PATTERN_APPLY2",
											bSensitive, pHandler);
		  	}
		}
	}
	/**
	 * Get the packages in the current project and populates the list box with the
	 * results.
	 *
	 * @return HRESULT
	 */
	public static void populatePatternList(JComboBox pPatternCombo, IProject pProject)
	{
		if (pProject != null)
		{
			// remove any previous information in the list
			pPatternCombo.removeAllItems();
			DefaultComboBoxModel theModel = new DefaultComboBoxModel();
			Vector<String> vStrings = new Vector<String>();
			// populate the namespace list box now that we have the new project
			String pattern = "//UML:Collaboration";
			ETList <String> pStrings = getElementNames(pProject, pattern, false);
			if (pStrings != null)
			{
				// loop through the results and add them to the list box
				int count = pStrings.size();
				for (int x = 0; x < count; x++)
				{
					String str = pStrings.get(x);
					vStrings.add(str);
				}
			}
			sortStringInfo(theModel, vStrings);
			pPatternCombo.setModel(theModel);
		}
	}
	/**
	 * Display an error dialog box
	 *
	 *
	 * @param errList[in]	The array of strings to place into the error message box
	 *
	 * @return HRESULT
	 *
	 */
	public static void displayErrorMessage( JDialog pDialog, String err )
	{
		if (err != null && err.length() > 0)
		{
			IErrorDialog pTemp = new SwingErrorDialog(pDialog);
			if (pTemp != null)
			{
				pTemp.display(err, MessageIconKindEnum.EDIK_ICONERROR, "");
			}
		}
	}
	/**
	 * Format the array of strings into one string for error display
	 *
	 *
	 * @param errList[in]	The array of strings to format
	 * @param err[out]		The newly built string
	 *
	 * @return HRESULT
	 *
	 */
	public static String formatErrorMessage( ETList<String> errList )
	{
		String msg = "";
		if (errList != null)
		{
			int cnt = errList.size();
			for (int x = 0; x < cnt; x++)
			{
				String err = errList.get(x);
				if (msg != null && msg.length() > 0)
				{
					msg += "\n";
				}
				msg += err;
			}
		}
		return msg;
	}
	public static String translateString(String inStr)
	{
		return DefaultDesignPatternResource.getString(inStr);
	}
	public static void startWaitCursor(Component c)
	{
		if (!m_bWaiting && c != null)
		{
			c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			m_bWaiting = true;
		}
	}
	public static void endWaitCursor(Component c)
	{
		if (m_bWaiting && c != null)
		{
			m_bWaiting = false;
			c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}
}
