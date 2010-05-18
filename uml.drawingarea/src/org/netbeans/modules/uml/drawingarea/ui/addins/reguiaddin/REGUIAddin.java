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

package org.netbeans.modules.uml.drawingarea.ui.addins.reguiaddin;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.SwingUtilities;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.reverseengineering.reintegration.IUMLParsingIntegrator;
import org.netbeans.modules.uml.core.reverseengineering.reintegration.UMLParsingIntegrator;
import org.netbeans.modules.uml.core.roundtripframework.codegeneration.CodeGenerator;
import org.netbeans.modules.uml.core.roundtripframework.codegeneration.ICodeGenerator;
import org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.IRPMethodSelection;
import org.netbeans.modules.uml.core.roundtripframework.requestprocessors.javarpcomponent.RPMethodSelection;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator.DiagCreatorAddIn;
import org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator.IDiagCreatorAddIn;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.products.ad.application.ApplicationView;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager;
import org.netbeans.modules.uml.ui.support.helpers.ETSmartWaitCursor;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel;

public class REGUIAddin extends Thread //implements IAddIn, IAddInButtonSupport, IViewActionDelegate
{
	public static String RE_OPER_BTN_SOURCE =
			REGUIResources.getString("IDS_RE_OPERATION"); // NOI18N
	public static String CODEGEN_BTN_SOURCE =
			REGUIResources.getString("IDS_GENERATE_PULLRIGHT"); // NOI18N
	public static String REDEFINE_OPS_BTN_SOURCE =
			REGUIResources.getString("IDS_REDEFING_OPERATIONS_MENU"); // NOI18N
	
	/// The event handler for the various controls and the core UML metamodel
	private AddinEventsSink m_EventsSink = null;
	private ApplicationView m_View = null;
	
	/// The operations associated with the context menu selection
	private static ETList<IElement> m_OperationElements = null;
	
	/// Elements that can be code generated
	private static ETList<IElement> m_CodeGenElements = null;
	
	/// Elements that will be the operation of redefing operations.
	private static ETList<IClassifier> m_RedefiningElements = null;
	
	/// UML parsing integrator, only access via GetUMLParsingIntegrator()
	private IUMLParsingIntegrator m_UMLParsingIntegrator = null;
	
        private String action = null;
        private ETArrayList <IElement> elements = null ;
        
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddIn#initialize(java.lang.Object)
	 */
	public long initialize(Object context)
	{
		if (m_EventsSink == null)
		{
			m_EventsSink = new AddinEventsSink();
			m_EventsSink.setParent(this);
			
			DispatchHelper helper = new DispatchHelper();
			helper.registerForInitEvents(m_EventsSink);
			helper.registerForProjectEvents(m_EventsSink);
		}
		return 0;
	}
	
	/**
	 * Deinitialize the addin - specifically it revokes from the sinks
	 */
	public long deInitialize(Object context)
	{
		if (m_EventsSink != null)
		{
			DispatchHelper helper = new DispatchHelper();
			helper.revokeInitSink(m_EventsSink);
			helper.revokeProjectSink(m_EventsSink);
			
			m_EventsSink = null;
		}
		
		// Release our hold on any model elements
		m_OperationElements = null;
		m_CodeGenElements = null;
		m_RedefiningElements = null;
		
		// Release the parsing integrator
		m_UMLParsingIntegrator = null;
		
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddIn#unLoad(java.lang.Object)
	 */
	public long unLoad(Object context)
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getVersion()
	 */
	public String getVersion()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getName()
	 */
	public String getName1()
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
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddInButtonSupport#getButtons()
	 */
//	public ETList<IAddInButton> getButtons()
//	{
//		// TODO Auto-generated method stub
//		return null;
//	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddInButtonSupport#execute(org.netbeans.modules.uml.core.addinframework.IAddInButton, int)
	 */
//	public long execute(IAddInButton pButton, Frame nParentHWND)
//	{
//		// TODO Auto-generated method stub
//		return 0;
//	}
	
	/**
	 * Retrieve the current project
	 */
	private IProject getCurrentProject()
	{
		IProject retProj = null;
		IProductProjectManager mgr = ProductHelper.getProductProjectManager();
		if (mgr != null)
		{
			retProj = mgr.getCurrentProject();
		}
		return retProj;
	}
	
	/**
	 * Get the addin for creating the diagrams
	 */
	private IDiagCreatorAddIn getDiagCreatorAddin()
	{
		IDiagCreatorAddIn retAddin = null;
		
		// IAddIn addin = ProductHelper.retrieveAddIn(
		//		"org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator"); // NOI18N
		
		// cvc - the diagram creator addin has been moved out of Core
//		IAddIn addin = ProductHelper.retrieveAddIn(
//				"org.netbeans.modules.uml.diagramcreator"); // NOI18N
                

//                IDiagCreatorAddIn addin = ProductHelper.getDiagCreatorAddIn();

                IDiagCreatorAddIn addin = new DiagCreatorAddIn(); //Incompatible types trying to use ProductHelper
		if (addin instanceof IDiagCreatorAddIn)
		{
			retAddin = (IDiagCreatorAddIn)addin;
		}
		return retAddin;
	}
	
	/**
	 * Handles the onupdate coming from the gui.
	 * The addin can set the sensitivity or checked status of the button.
	 */
//	public long update(IAddInButton pButton, Frame nParentHWND)
//	{
//		pButton.setSensitive(true);
//		return 0;
//	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.addinframework.IAddInButtonSupport#getProgID()
	 */
	public String getProgID()
	{
		return "org.netbeans.modules.uml.drawingarea.ui.addins.reguiaddin.REGUIAddin"; // NOI18N
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.application.action.IViewActionDelegate#init(org.netbeans.modules.uml.ui.products.ad.application.ApplicationView)
	 */
	public void init(ApplicationView view)
	{
		m_View = view;
	}
	
	/**
	 * This method decides whether this button should be shown for the selected item.
	 */
	public boolean validate(
			ApplicationView view,
			IMenuManager item,
			IMenuManager pContextMenu)
	{
		boolean valid = false;
		String label = item.getLabel();
		
		if(m_OperationElements != null)
		{
			m_OperationElements.clear();
			m_OperationElements = null;
		}
		
		if (view instanceof IProjectTreeControl)
		{
			IProjectTreeControl control = (IProjectTreeControl)view;
			
			boolean isFolder = isFolderSelected(control);
			if (!isFolder)
			{
				// See if we have a model element selected
				IElement firstSelModEle =
						control.getFirstSelectedModelElement();
				
				// Only add the RE operation button if the selected
				// element is an operation
				if (label.equals(RE_OPER_BTN_SOURCE))
				{
					if (firstSelModEle != null &&
							firstSelModEle instanceof IOperation)
					{
						if (addOperation((IOperation)firstSelModEle))
						{
							valid = true;
						}
					}
				}
				
				if (label.equals(CODEGEN_BTN_SOURCE))
				{
					//add code generation button
					valid = addCodeGenerationButton(control, pContextMenu);
				}
				
				if (label.equals(REDEFINE_OPS_BTN_SOURCE))
				{
					//add redefine operations button
					valid = addRedefiningOpsButton(control, pContextMenu);
				}
			}
		}
                // TODO: meteora
//		else if (view instanceof IDrawingAreaControl)
//		{
//			if (label.equals(RE_OPER_BTN_SOURCE))
//			{
//				// Determine if an operation is under the current context menu
//				boolean atleastOneOperAdded =
//						getAssociatedOperations(pContextMenu);
//				
//				if (atleastOneOperAdded)
//				{
//					valid = true;
//				}
//			}
//			
//			IDiagram dia = ((IDrawingAreaControl)view).getDiagram();
//			
//			if (label.equals(CODEGEN_BTN_SOURCE))
//			{
//				//add code generation button
//				valid = addCodeGenerationButton(dia, pContextMenu);
//			}
//			
//			if (label.equals(REDEFINE_OPS_BTN_SOURCE))
//			{
//				//add redefine operations button
//				valid = addRedefiningOpsButton(dia, pContextMenu);
//			}
//		}
		return valid;
	}
	
	/**
	 * Determines whether or not a folder node is selected in the tree
	 *
	 * @param pControl[in] The tree control
	 * @param bSel[out]	Whether or not a folder is selected in the tree
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
	
	/**
	 *
	 * Called when the context menu for the ProjectTree needs to be built
	 *
	 * @param pParentControl[in]  The tree
	 * @param pContextMenu[in]    The menu
	 *
	 * @return HRESULT
	 *
	 */
	private boolean addCodeGenerationButton(
			IProjectTreeControl pParentControl,
			IMenuManager pContextMenu)
	{
		boolean retVal = false;
		IProjectTreeItem[] items = pParentControl.getSelected();
		if (items != null)
		{
			int count = items.length;
			ETList<IElement> elems = new ETArrayList<IElement>();
			for (int i=0; i<count; i++)
			{
				IProjectTreeItem item = items[i];
				IElement pEle = item.getModelElement();
				if (pEle != null)
				{
					elems.add(pEle);
				}
			}
			
			if (elems.size() > 0)
			{
				m_CodeGenElements = null;
				m_CodeGenElements = filterElementsForCodeGen(elems);
				if (m_CodeGenElements != null && m_CodeGenElements.size() > 0)
				{
					retVal = true;
				}
			}
		}
		return retVal;
	}
	
	/**
	 *
	 * Called when the diagram is about to show its context menu.
	 *
	 * @param pParentDiagram[in]  The diagram showing the menu
	 * @param pContextMenu[in]    The actual menu being shown
	 *
	 * @return HRESULT
	 *
	 */
	private boolean addCodeGenerationButton(
			IDiagram pParentDiagram, IMenuManager pContextMenu)
	{
		boolean retVal = false;
                // TODO: meteora
//		if (pParentDiagram != null && pParentDiagram instanceof IUIDiagram)
//		{
//			IDrawingAreaControl control =
//					((IUIDiagram)pParentDiagram).getDrawingArea();
//			
//			if (control != null)
//			{
//				ETList<IElement> selItems = control.getSelected4();
//				
//				if (selItems != null)
//				{
//					m_CodeGenElements = null;
//					m_CodeGenElements = filterElementsForCodeGen(selItems);
//					
//					if (m_CodeGenElements != null &&
//							m_CodeGenElements.size() > 0)
//					{
//						retVal = true;
//					}
//				}
//			}
//		}
		return retVal;
	}
	
	/**
	 *
	 * Filters the passed in collection of Elements based on their element type. Currently,
	 * we are only looking for Class and Interface elements, so any other element will be
	 * filtered out.
	 *
	 * @param pSelected[in] The currently selected elements
	 * @param pResult[out]  The collection of elements that can be code gened.
	 *
	 * @return HRESULT
	 *
	 */
	private ETList<IElement> filterElementsForCodeGen(ETList<IElement> pSelected)
	{
		ETList<IElement> retElems = null;
		if (pSelected != null)
		{
			int count = pSelected.size();
			for (int i=0; i<count; i++)
			{
				IElement pEle = pSelected.get(i);
				String elemType = pEle.getElementType();
				if (elemType.equals("Class") || // NOI18N
						elemType.equals("Interface")) // NOI18N
				{
					if (retElems == null)
					{
						retElems = new ETArrayList<IElement>();
					}
					retElems.add(pEle);
				}
			}
		}
		return retElems;
	}
	
	/**
	 *
	 * Filters the passed in collection of Classifiers based on their element type. Currently,
	 * we are only looking for Class and Interface elements, so any other element will be
	 * filtered out.
	 *
	 * @param pSelected[in] The currently selected elements
	 * @param pResult[out]  The collection of elements that can be code gened.
	 *
	 * @return HRESULT
	 *
	 */
	private ETList<IClassifier> filterClassifiersForRedefinition(
			ETList<IElement> pSelected)
	{
		ETList<IClassifier> retElems = null;
		if (pSelected != null)
		{
			int count = pSelected.size();
			for (int i=0; i<count; i++)
			{
				IElement pEle = pSelected.get(i);
				String elemType = pEle.getElementType();
				
				if (elemType.equals("Class") || // NOI18N
						elemType.equals("Interface")) // NOI18N
				{
					if (retElems == null)
					{
						retElems = new ETArrayList<IClassifier>();
					}
					
					if (pEle instanceof IClassifier)
					{
						retElems.add((IClassifier)pEle);
					}
				}
			}
		}
		return retElems;
	}
	
	/**
	 *
	 * Called when the context menu for the ProjectTree needs to be built
	 *
	 * @param pParentControl[in]  The tree
	 * @param pContextMenu[in]    The menu
	 *
	 * @return HRESULT
	 *
	 */
	private boolean addRedefiningOpsButton(
			IProjectTreeControl pParentControl, IMenuManager pContextMenu)
	{
		boolean retVal = false;
		IProjectTreeItem[] items = pParentControl.getSelected();
		if (items != null)
		{
			int count = items.length;
			if (count == 1)
			{
				ETList<IElement> pElements = new ETArrayList<IElement>();
				IProjectTreeItem item = items[0];
				IElement pEle = item.getModelElement();
				
				if (pEle != null)
				{
					pElements.add(pEle);
				}
				
				m_RedefiningElements = null;
				m_RedefiningElements =
						filterClassifiersForRedefinition(pElements);
				
				if (m_RedefiningElements != null &&
						m_RedefiningElements.size() > 0)
				{
					retVal = true;
				}
			}
		}
		return retVal;
	}
	
	/**
	 *
	 * Called when the diagram is about to show its context menu.
	 *
	 * @param pParentDiagram[in]  The diagram showing the menu
	 * @param pContextMenu[in]    The actual menu being shown
	 *
	 * @return HRESULT
	 *
	 */
	private boolean addRedefiningOpsButton(
			IDiagram pParentDiagram, IMenuManager pContextMenu)
	{
		boolean retVal = false;
                // TODO: meteora
//		if (pParentDiagram != null && pParentDiagram instanceof IUIDiagram)
//		{
//			IDrawingAreaControl control =
//					((IUIDiagram)pParentDiagram).getDrawingArea();
//			
//			if (control != null)
//			{
//				ETList<IElement> selItems = control.getSelected4();
//				
//				if (selItems != null)
//				{
//					m_RedefiningElements = null;
//					m_RedefiningElements =
//							filterClassifiersForRedefinition(selItems);
//					
//					if (m_RedefiningElements != null &&
//							m_RedefiningElements.size() > 0)
//					{
//						retVal = true;
//					}
//				}
//			}
//		}
		return retVal;
	}
	
	/**
	 * This handles the menu item selection.
	 */
	public void run(ActionEvent e)
	{
		if (m_View != null)
		{
			String id = m_View.getId();
			String btnSource = e.getActionCommand();
			
			if (btnSource.equals(RE_OPER_BTN_SOURCE))
			{
				if (m_OperationElements != null)
				{
					// Reverse Engineer the operation, creating an interaction
					IProject proj = getCurrentProject();
					if (proj != null)
					{
						ETSmartWaitCursor waitCursor = new ETSmartWaitCursor();
						try
						{
							IUMLParsingIntegrator integrator =
									getUMLParsingIntegrator();
							
							integrator.reverseEngineerOperations(
									proj, m_OperationElements);
						}
						finally
						{
							waitCursor.stop();
						}
					}
					
					// Ask the user if they want to create a diagram for the interactions
					ETList<IElement> pElements = new ETArrayList<IElement>();
					IDiagCreatorAddIn diaCreator = getDiagCreatorAddin();
					
					if (diaCreator != null)
					{
						int count = m_OperationElements.size();
						for (int i=0; i<count; i++)
						{
							IElement pEle = m_OperationElements.get(i);
							IInteraction pInteraction =
									getLastInteraction(pEle);
							
							IProjectTreeControl pControl =
									ProductHelper.getProjectTree();
							
							if (pInteraction != null)
							{
								diaCreator.guiCreateDiagramFromElements(
										null, pInteraction, pControl);
							}
						}
					}
					
					else
					{
						//inform user that IDiagCreatorAddIn is not loaded.
					}
				}
				
				m_OperationElements = null;
			}
			
			else if (btnSource.equals(CODEGEN_BTN_SOURCE))
			{
				if (m_CodeGenElements != null)
				{
					generateCode();
				}
			}
			
			else if (btnSource.equals(REDEFINE_OPS_BTN_SOURCE))
			{
				if (m_RedefiningElements != null &&
						m_RedefiningElements.size() > 0)
				{
					IClassifier pClass = m_RedefiningElements.get(0);
					handleRedefineOperations(pClass);
				}
			}
		}
	}
        
	//kris - this method was added in order to set these default values outside
        // the run method. This is in response to issue 95928.
        public void prepareForRun(String action, ETArrayList <IElement> elements) {
                
            this.action = action ;
            this.elements = elements ;
        }
        
        //kris - added this since this class now extends Thread
        public void run() {
            run (action, elements);
        }
	
        /*
	 * cvc - New version of method for Buzz release
	 */
	public void run(String action, ETArrayList <IElement> elements)
	{
            
            
		// generate code action
		if (action.equals(CODEGEN_BTN_SOURCE))
		{
			if (elements != null)
			{
				generateCode(elements);
			}
		}
		
		// redefine operations (override methods) action
		else if (action.equals(REDEFINE_OPS_BTN_SOURCE))
		{
			m_RedefiningElements = filterClassifiersForRedefinition(elements);
			
			if (m_RedefiningElements != null && m_RedefiningElements.size() > 0)
			{
				IClassifier pClass = m_RedefiningElements.get(0);
				handleRedefineOperations(pClass);
			}
		}
		
		// reverser engineer an operation action (sequence or collab diagram)
		else if (action.equals(RE_OPER_BTN_SOURCE))
		{
			if (elements != null)
			{
				// Reverse Engineer the operation, creating an interaction
				IProject proj = elements.get(0).getProject();

				if (proj != null)
				{
					ETSmartWaitCursor waitCursor = new ETSmartWaitCursor();
					ProgressHandle handler = ProgressHandleFactory.createHandle("RE Operation");      
                                        handler.start() ;
                                        
					try {
                                            
                                            IUMLParsingIntegrator integrator =
                                                    getUMLParsingIntegrator();
                                            
                                            integrator.reverseEngineerOperations(proj, elements);
                                            
                                        } finally {
                                            waitCursor.stop();
                                            handler.finish() ;
                                        }
				}
                                
                                //kris - made local copy because it is needed in this inner class
                                final ETArrayList <IElement> elementsLocal = elements ;
                                
                                //kris - the diaCreator.guiCreateDiagramFromElements call below
                                // creates a new top component. This must happen in the event thread.
                                // So it is bring forced into the event thread.
                                SwingUtilities.invokeLater(new Runnable() {
                                    
                                    public void run() {
                                        IDiagCreatorAddIn diaCreator = new DiagCreatorAddIn();
                                        
                                        //fix concurrentModification exception
                                        IElement[] elemArray=(IElement[])elementsLocal.toArray(new IElement[0]);
                                        for (IElement pEle: elemArray) {
                                            // IElement pEle = elements.get(i);
                                            final IInteraction pInteraction = getLastInteraction(pEle);
                                            
                                            // Fixed 114922. 
                                            // ProjectTreeControl seems to be obsolete.
                                            // It's not initialized any where; hence always null.
                                            // Use IProjectTreeModel instead.
//                                            final IProjectTreeControl pControl =
//                                                    ProductHelper.getProjectTree();
                                            
                                            final IProjectTreeModel treeModel = 
                                                    ProductHelper.getProjectTreeModel();
                                            
                                            if (pInteraction != null) {                                                
                                                diaCreator.guiCreateDiagramFromElements(
                                                        elementsLocal, pInteraction, treeModel);
                                            }
                                            
                                            
                                        }
                                    }
                                });
                                
			}
			elements = null;
		}	
            
	}
	
	
	private void handleRedefineOperations(IClassifier pElement)
	{
		IRPMethodSelection methSel = new RPMethodSelection();
		methSel.overrideMethods(pElement);
	}
	
	private void generateCode()
	{
		ICodeGenerator codeGen = new CodeGenerator();
		codeGen.generateCode("Java", m_CodeGenElements);
	}
	
	/*
	 * cvc - New version of method for Buzz release
	 */
	private void generateCode(ETArrayList <IElement> elements)
	{
		ICodeGenerator codeGen = new CodeGenerator();
		codeGen.generateCode("Java", elements);
	}
	
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#selectionChanged(org.netbeans.modules.uml.ui.products.ad.application.action.PluginAction, org.netbeans.modules.uml.ui.products.ad.application.selection.ISelection)
	 */
//	public void selectionChanged(PluginAction action, ISelection selection)
//	{
//		// TODO Auto-generated method stub
//	}
	
	/**
	 * This routine responds to the close of the project
	 */
	public void onProjectClosed()
	{
		// We don't care what project closed, use this event to clear our all the
		// model elements.  We'll repopulate with the next context menu events from the
		// tree or drawing area.
		m_OperationElements = null;
		m_CodeGenElements = null;
		m_RedefiningElements = null;
	}
	
	/**
	 * Retrieves the elements associated with the context menu's graph object
	 *
	 * @param pContextMenu[in] The context menu assoicated with some graph object
	 * @param bAtLeastOneOperationAdded [out] true if we added at least one menu
	 */
        // TODO: meteora
//	private boolean getAssociatedOperations( IMenuManager pContextMenu)
//	{
//		boolean retVal = false;
//		Object obj = pContextMenu.getContextObject();
//		
//		if (obj != null && obj instanceof IETGraphObject)
//		{
//			IDrawEngine pEngine =
//					TypeConversions.getDrawEngine((IETGraphObject)obj);
//			
//			if (pEngine != null)
//			{
//				List pCompartments = pEngine.getSelectedCompartments();
//				if (pCompartments != null)
//				{
//					int count = pCompartments.size();
//					ETList<IOperation> opers = null;
//					
//					for (int i=0; i<count; i++)
//					{
//						ICompartment pComp = (ICompartment)pCompartments.get(i);
//						if (pComp != null)
//						{
//							IElement pEle = pComp.getModelElement();
//							retVal |= addOperation(pEle);
//						}
//					}
//				}
//			}
//		}
//		
//		return retVal;
//	}
	
	/**
	 * Adds an operation to m_cpOperationElements, @see m_cpOperationElements
	 *
	 * @param pElement[in] If the element is an operation, it is added to m_cpOperationElements
	 */
	private boolean addOperation( IElement pElement )
	{
		boolean opAdded = false;
		if ( (pElement != null) && (pElement instanceof IOperation) &&
				(canOperationBeREed((IOperation)pElement)) )
		{
			if (m_OperationElements == null)
			{
				m_OperationElements = new ETArrayList<IElement>();
			}
			m_OperationElements.add(pElement);
			opAdded = true;
		}
		return opAdded;
	}
	
	/**
	 * Verifies that the input operation can be processed by reverse engineering
	 * Currently, this operation just checks to see if any of the IOperation's
	 * languages supports the "Operation Reverse Engineer" feature.
	 */
	private boolean canOperationBeREed( IOperation pOperation )
	{
		IUMLParsingIntegrator integrator = getUMLParsingIntegrator();
		boolean canRE = integrator.canOperationBeREed(pOperation);
		return (canRE != false);
	}
	
	/**
	 * Access to the UML parsing integrator member variable
	 */
	private IUMLParsingIntegrator getUMLParsingIntegrator()
	{
		if (m_UMLParsingIntegrator == null)
		{
			m_UMLParsingIntegrator = new UMLParsingIntegrator();
		}
		return m_UMLParsingIntegrator;
	}
	
	/**
	 * Finds the last interaction in the owned elements of the input element
	 */
	private IInteraction getLastInteraction( IElement pElement )
	{
		IInteraction pInteraction = null;
		if (pElement != null && pElement instanceof INamespace)
		{
			ETList<INamedElement> pElems =
					((INamespace)pElement).getOwnedElements();
			
			if (pElems != null)
			{
				int count = pElems.size();
				for (int i=count-1; i>=0; i--)
				{
					INamedElement pEle = pElems.get(i);
					if (pEle != null && pEle instanceof IInteraction)
					{
						pInteraction = (IInteraction)pEle;
						break;
					}
				}
			}
		}
		return pInteraction;
	}
}
