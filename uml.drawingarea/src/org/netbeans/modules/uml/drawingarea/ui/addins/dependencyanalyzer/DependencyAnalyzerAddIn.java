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


package org.netbeans.modules.uml.drawingarea.ui.addins.dependencyanalyzer;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.INavigableEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator.IDiagCreatorAddIn;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.products.ad.application.ApplicationView;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.helpers.ETSmartWaitCursor;
import org.netbeans.modules.uml.ui.support.helpers.ProgressBarHelper;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator.DiagCreatorAddIn;

/**
 * @author sumitabhk
 *
 */
public class DependencyAnalyzerAddIn // implements IAddIn, IAddInButtonSupport,	IViewActionDelegate
{
	private static final String BUNDLE_NAME = "org.netbeans.modules.uml.drawingarea.ui.addins.dependencyanalyzer.Bundle"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	/// The event handler for the various controls and the core UML metamodel
	private AddInEventSink m_EventsSink = null;
	private String m_Version = "1"; // NOI18N
	// The classes associated with the context menu selection
	private ETList<IElement> m_ClassElements = null;
	private ApplicationView m_View = null;

	public static String CLASS_DEPENDENCY_BTN_SOURCE = 
			"MBK_CLASSIFIER_DEPENDENCY"; // NOI18N

	/**
	 * 
	 */
	public DependencyAnalyzerAddIn()
	{
		super();
	}

	/**
	 * Called when the addin is initialized.
	 */
	public long initialize(Object context)
	{
		if (m_EventsSink == null)
		{
			m_EventsSink = new AddInEventSink();
			m_EventsSink.setParent(this);
			
			DispatchHelper helper = new DispatchHelper();
			helper.registerProjectTreeContextMenuEvents(m_EventsSink);
                        // TODO: meteora
//			helper.registerDrawingAreaContextMenuEvents(m_EventsSink);
		}
		return 0;
	}

	/**
	 * Called when the addin is deinitialized.
	 */
	public long deInitialize(Object context)
	{
		if (m_EventsSink != null)
		{
			DispatchHelper helper = new DispatchHelper();
//			helper.revokeDrawingAreaContextMenuSink(m_EventsSink);
			helper.revokeProjectTreeContextMenuSink(m_EventsSink);
			m_EventsSink = null;
		}
		return 0;
	}

	/**
	 * Called when the addin is unloaded.
	 */
	public long unLoad(Object context)
	{
		return 0;
	}

	/**
	 * The version of the addin.
	 */
	public String getVersion()
	{
		return m_Version;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getName()
	 */
	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getID()
	 */
	public String getID()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getLocation()
	 */
	public String getLocation()
	{
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * Returns the clsid of this addin.
	 */
	public String getProgID()
	{
		return "org.netbeans.modules.uml.ui.addins.dependencyanalyzer.DependencyAnalyzerAddIn"; // NOI18N
	}

	/**
	 * If an external interface handles the display of the popup menu,
	 * then this is called to handle the selection event
	 */
	public void handleSelection( IProductContextMenu pContextMenu,
											IProductContextMenuItem pSelectedItem )
	{
		if (pContextMenu != null && pSelectedItem != null)
		{
			String btnSource = pSelectedItem.getButtonSource();
			if (m_ClassElements != null)
			{
				if (btnSource.equals(CLASS_DEPENDENCY_BTN_SOURCE))
				{
//					IAddIn addin = ProductHelper.retrieveAddIn(
//							"org.netbeans.modules.uml.diagramcreator"); // NOI18N
//                                    IDiagCreatorAddIn addin = ProductHelper.getDiagCreatorAddIn(); 
                                    IDiagCreatorAddIn addin =  new DiagCreatorAddIn();
					if (addin != null)
					{
                                            ETSmartWaitCursor waitCursor = null;
                                            ProgressBarHelper progress = null;
                                            try {
						waitCursor = new ETSmartWaitCursor();
                                                String descr = loadString("IDS_PROGRESS_DESCRIPTION"); //NOI18N
                                                progress = new ProgressBarHelper(descr, 0);

						IDiagCreatorAddIn diaCreator = (IDiagCreatorAddIn)addin;
						int count = m_ClassElements.size();
						for (int i=0; i<count; i++)
						{
							ETList<IElement> dependentElems = new ETArrayList<IElement>();
							IElement curEle = m_ClassElements.get(i);
							dependentElems.add(curEle);
							
							if (curEle instanceof IClassifier)
							{
								IClassifier curClassifier = (IClassifier)curEle;
								Hashtable<String, IClassifier> classifierMap = new Hashtable<String, IClassifier>();
								classifierMap = gatherFromGeneralizations(curClassifier, classifierMap);
								classifierMap = gatherFromImplementations(curClassifier, classifierMap);
								classifierMap = gatherFromAssociations(curClassifier, classifierMap);
								classifierMap = gatherFromOperations(curClassifier, classifierMap);
								
								Collection colElems = classifierMap.values();
								dependentElems.addAll(colElems);
								
								String postFix = loadString(
										"IDS_DIAGRAM_POSTFIX"); // NOI18N
								String diaName = curClassifier.getName();
								if (diaName != null && diaName.length() > 0)
								{
									diaName += postFix;
									diaCreator.createDiagramForElements(
											IDiagramKind.DK_CLASS_DIAGRAM, 
											curClassifier, diaName, 
											dependentElems, null);
								}
							}
						}
                                            }
                                            finally
                                            {
						waitCursor.stop();
                                                progress.stop();
                                            }
					}
					m_ClassElements = null;
				}
			}
		}
	}

	/**
	 * Fired when the context menu is about to be displayed.
	 */
	public void onProjectTreeContextMenuPrepare( 
			IProjectTreeControl pParentControl,
			IProductContextMenu    pContextMenu,
			IResultCell        pCell )
	{
//		if (pParentControl != null)
//		{
//			m_ClassElements = null;
//			
//			// See if we have a model element selected
//			IElement firstSelModEle = pParentControl.getFirstSelectedModelElement();
//
//			// Only add the dependency button if the selected element is a classifier
//			if (firstSelModEle != null && firstSelModEle instanceof IClassifier)
//			{
//				addClass((IClassifier)firstSelModEle);
//				addClassDependencyButton(pContextMenu);
//			}
//		}
	}

	/**
	 * Fired when the context menu is about to be displayed.
	 */
	public void onDrawingAreaContextMenuPrepare( 
			IDiagram pParentDiagram,
			IProductContextMenu pContextMenu,
			IResultCell pCell )
	{
//		m_ClassElements = null;
//
//		// Determine if an operation is under the current context menu
//		boolean atleastOneOperAdded = getAssociatedClassifiers(pContextMenu);
//		if (atleastOneOperAdded)
//		{
//			addClassDependencyButton(pContextMenu);
//		}
	}
	
	/**
	 * Adds a class to m_cpClassElements, 
	 *
	 * @see m_cpClassElements
	 * @param pElement[in] If the element is an classifier, it is added to m_cpClassElements 
	 */
	private boolean addClass( IElement pElement )
	{
		boolean retVal = false;
		if (pElement != null && pElement instanceof IClassifier)
		{
			if (m_ClassElements == null)
			{
				m_ClassElements = new ETArrayList<IElement>();
			}
			if (m_ClassElements != null)
			{
				m_ClassElements.add(pElement);
				retVal = true;
			}
		}
		return retVal;
	}
	
	/**
	 * Retrieves the elements associated with the context menu's graph object
	 *
	 * @param pContextMenu[in] The context menu assoicated with some graph object
	 * @param bAtLeastOneOperationAdded [out] true if we added at least one menu
	 */
//	private boolean getAssociatedClassifiers( IProductContextMenu pContextMenu)
//	{
//		boolean operAdded = false;
//		if (pContextMenu != null)
//		{
//			Object obj = pContextMenu.getItemClickedOn();
//			if (obj != null && obj instanceof TSGraphObject)
//			{
//				IElement elem = TypeConversions.getElement((TSGraphObject)obj);
//				if (elem != null && elem instanceof IClassifier)
//				{
//					addClass((IClassifier)elem);
//					operAdded = true;
//				}
//			}
//		}
//		return operAdded;
//	}
	
	//Adds the Reverse Engineering Operation button to the specified context menu
  	private void addClassDependencyButton( IProductContextMenu pContextMenu )
  	{
		
//  		if (pContextMenu != null)
//  		{
//  			ETList<IProductContextMenuItem> items = pContextMenu.getSubMenus();
//  			if (items != null)
//  			{
//  				IProductContextMenuItem selItem = new ProductContextMenuItem();
//  				selItem.setSensitive(true);
//  				
//  				String strTemp = loadString("IDS_CLASSIFIER_DEPENDENCY"); // NOI18N
//  				selItem.setMenuString(strTemp);
//  				
//  				strTemp = loadString("IDS_CLASSIFIER_DEPENDENCY_DSCR"); // NOI18N
//  				selItem.setDescription(strTemp);
//  				
//  				selItem.setButtonSource(CLASS_DEPENDENCY_BTN_SOURCE);
//  				selItem.setSelectionHandler(m_EventsSink);
//  				items.add(selItem);
//  			}
//  		}
		
		
  	}
	
	/**
	 * Gather all of the generalizations that the specified classifer plays the sub class role.
	 * If the class has already been discovered it will not be added to the classifier map again.
	 *
	 * @param pClassifier [in] The classifier used to retrieve the relationships.
	 * @param elementMap [in] A map of classifiers that have been discovered.
	 */
	private Hashtable<String, IClassifier> gatherFromGeneralizations(IClassifier   pClassifier, 
																Hashtable<String, IClassifier> elementMap)
	{
		if (pClassifier != null)
		{
			ETList<IGeneralization> gens = pClassifier.getGeneralizations();
			if (gens != null)
			{
				int count = gens.size();
				for (int i=0; i<count; i++)
				{
					IGeneralization curGen = gens.get(i);
					IClassifier superClass = curGen.getGeneral();
					if (superClass != null)
					{
						String name = superClass.getName();
						if (name != null && name.length() > 0)
						{
							Object obj = elementMap.get(name);
							if (obj == null)
							{
								elementMap.put(name, superClass);
							}
						}
					}
				}
			}
		}
		return elementMap;
	}

	/**
	 * Gather all of the implementations that the specified classifer plays the contract role.
	 * If the class has already been discovered it will not be added to the classifier map again.
	 *
	 * @param pClassifier [in] The classifier used to retrieve the relationships.
	 * @param elementMap [in] A map of classifiers that have been discovered.
	 */
	private Hashtable<String,IClassifier> gatherFromImplementations(
			IClassifier  pClassifier, 
			Hashtable<String,IClassifier> elementMap)
	{
		if (pClassifier != null)
		{
			ETList<IImplementation> impls = pClassifier.getImplementations();
			if (impls != null)
			{
				int count = impls.size();
				for (int i=0; i<count; i++)
				{
					IImplementation curRelation = impls.get(i);
					IClassifier contractClass = curRelation.getContract();
					if (contractClass != null)
					{
						String name = contractClass.getName();
						if (name != null && name.length() > 0)
						{
							Object obj = elementMap.get(name);
							if (obj == null)
							{
								elementMap.put(name, contractClass);
							}
						}
					}
				}
			}
		}
		return elementMap;
	}
	
	/**
	 * Gather all of the navigable associations that the specified classifer is not the naviagable end.
	 * If the class has already been discovered it will not be added to the classifier map again.
	 *
	 * @param pClassifier [in] The classifier used to retrieve the relationships.
	 * @param elementMap [in] A map of classifiers that have been discovered.
	 */
	private Hashtable<String,IClassifier> gatherFromAssociations(IClassifier   pClassifier, 
												Hashtable<String,IClassifier> elementMap)
	{
		if (pClassifier != null)
		{
			ETList<INavigableEnd> ends = pClassifier.getNavigableEnds();
			if (ends != null)
			{
				int count = ends.size();
				for (int i=0; i<count; i++)
				{
					INavigableEnd nEnd = ends.get(i);
					IClassifier curClass = nEnd.getFeaturingClassifier();
					IClassifier curClass2 = nEnd.getReferencingClassifier();
					if (curClass != null)
					{
						boolean isSame = pClassifier.isSame(curClass);
						if (!isSame)
						{
							String name = curClass.getName();
							if (name != null && name.length() > 0)
							{
								Object obj = elementMap.get(name);
								if (obj == null)
								{
									elementMap.put(name, curClass);
								}
							}
						}
					}
				}
			}
		}
		return elementMap;
	}
	
	/**
	 * Gather all of the classifiers that are specified as paramters.  If the class 
	 * has already been discovered it will not be added to the classifier map again.
	 *
	 * @param pClassifier [in] The classifier used to retrieve the operations.
	 * @param elementMap [in] A map of classifiers that have been discovered.
	 */
	private Hashtable<String,IClassifier> gatherFromOperations(IClassifier   pClassifier, 
											Hashtable<String,IClassifier> elementMap)
	{
		if (pClassifier != null)
		{
			ETList<IOperation> opers = pClassifier.getOperations();
			
			// The first language wins.  The first language that is specified 
			// is the language that gets to determine if the paramter type is a
			// primitive type.
			ETList<ILanguage> pLanguages = pClassifier.getLanguages();
			ILanguage lang = null;
			if (pLanguages != null && pLanguages.size() > 0)
			{
				lang = pLanguages.get(0);
			}
			
			if (opers != null)
			{
				int count = opers.size();
				for (int i=0; i<count; i++)
				{
					IOperation curOper = opers.get(i);
					ETList<IParameter> parameters = curOper.getParameters();
					if (parameters != null)
					{
						int parmCount = parameters.size();
						for (int index = 0; index<parmCount; index++)
						{
							IParameter curParm = parameters.get(index);
							IClassifier pType = curParm.getType();
							if (pType != null)
							{
								String typeName = pType.getName();
								if (typeName != null && typeName.length() > 0)
								{
									boolean isPrimitive = lang.isPrimitive(typeName);
									boolean isSame = pClassifier.isSame(pType);
									if (!isPrimitive && !isSame)
									{
										Object obj = elementMap.get(typeName);
										if (obj == null)
										{
											elementMap.put(typeName, pType);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return elementMap;
	}

	private String loadString(String key)
	{
		try
		{
		   return RESOURCE_BUNDLE.getString(key);
		}
		catch (MissingResourceException e)
		{
		   return '!' + key + '!';
		}
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.ui.action.IViewActionDelegate#init(org.netbeans.modules.uml.core.addinframework.ui.application.ApplicationView)
	 */
	public void init(ApplicationView view)
	{
		m_View = view;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.ui.action.IPlugginAction#run()
	 */
	public void run(ActionEvent e)
	{
//		if (m_View != null)
//		{
//			String id = m_View.getId();
//			if (id.equals("org.netbeans.modules.uml.view.projecttree"))
//			{
//				JProjectTree projTree = (JProjectTree)m_View;
//
//				//First initialize the m_ClassElements
//				m_ClassElements = null;
//			
//				// See if we have a model element selected
//				IElement firstSelModEle = projTree.getFirstSelectedModelElement();
//
//				// Only add the dependency button if the selected element is a classifier
//				if (firstSelModEle != null && firstSelModEle instanceof IClassifier)
//				{
//					addClass((IClassifier)firstSelModEle);
//				}
//
//				handleProjectTreeItemSelected();
//			}
//			else if (id.equals("org.netbeans.modules.uml.view.drawingarea"))
//			{
//				IDrawingAreaControl drawControl = (IDrawingAreaControl)m_View;
//
//				//First initialize the m_ClassElements
//				m_ClassElements = null;
//			
//				// See if we have a model element selected
//				ETList<IPresentationElement> selItems = drawControl.getSelected();
//				if (selItems != null && selItems.size() > 0)
//				{
//					IPresentationElement presEle = selItems.get(0);
//
//					IElement elem = TypeConversions.getElement(presEle);
//					if (elem != null && elem instanceof IClassifier)
//					{
//						addClass(elem);
//					}
//				}
//
//				handleProjectTreeItemSelected();
//			}
//		}
	}
	
	
	public void run(ETList<IElement> elements)
	{
		//First initialize the m_ClassElements
		m_ClassElements = null;

		// See if we have a model element selected
		IElement element = elements.get(0);

		// Only add the dependency button if the selected element is a classifier
		if (element != null && element instanceof IClassifier)
		{
			addClass((IClassifier)element);
		}

		handleProjectTreeItemSelected();
	}
	
	private void handleProjectTreeItemSelected()
	{
		//now I need to handle the generate dependency diagram.
		if (m_ClassElements != null)
		{
			IDiagCreatorAddIn diaCreator = new DiagCreatorAddIn();
                        ETSmartWaitCursor waitCursor = null;
                        ProgressBarHelper progress = null;
                        try {
                            waitCursor = new ETSmartWaitCursor();
                            String descr = loadString("IDS_PROGRESS_DESCRIPTION"); //NOI18N
                            progress = new ProgressBarHelper(descr, 0);
			
                            int count = m_ClassElements.size();
                            for (int i=0; i<count; i++)
                            {
				ETList<IElement> dependentElems = new ETArrayList<IElement>();
				IElement curEle = m_ClassElements.get(i);
				dependentElems.add(curEle);

				if (curEle instanceof IClassifier)
				{
					IClassifier curClassifier = (IClassifier)curEle;
					
					Hashtable<String, IClassifier> classifierMap = 
							new Hashtable<String, IClassifier>();
					
					classifierMap = gatherFromGeneralizations(
							curClassifier, classifierMap);
					
					classifierMap = gatherFromImplementations(
							curClassifier, classifierMap);
					
					classifierMap = gatherFromAssociations(
							curClassifier, classifierMap);
					
					classifierMap = gatherFromOperations(
							curClassifier, classifierMap);

					Collection colElems = classifierMap.values();
					dependentElems.addAll(colElems);

					String postFix = loadString(
							"IDS_DIAGRAM_POSTFIX"); // NOI18N
					
					String diaName = curClassifier.getName();
					
					if (diaName != null && diaName.length() > 0)
					{
						diaName += postFix;
						diaCreator.createDiagramForElements(
								IDiagramKind.DK_CLASS_DIAGRAM, 
								curClassifier, 
								diaName, 
								dependentElems, 
								null);
					}
				}
                            }
                        }
                        finally
                        {
                            waitCursor.stop();
                            progress.stop();
                        }			
			m_ClassElements = null;
		}
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
					if (pDisp != null && pDisp instanceof ITreeFolder)
					{
						bSel = true;
						break;
					}
				}
			}
		}

		return bSel;
	}

        // not used
//	private boolean getAssociatedClassifiers( IMenuManager pContextMenu)
//	{
//		boolean operAdded = false;
//		if (pContextMenu != null)
//		{
//			Object obj = pContextMenu.getContextObject();
//			if (obj != null && obj instanceof TSGraphObject)
//			{
//				IElement elem = TypeConversions.getElement((TSGraphObject)obj);
//				if (elem != null && elem instanceof IClassifier)
//				{
//					addClass(elem);
//					operAdded = true;
//				}
//			}
//		}
//		return operAdded;
//	}
	
}



