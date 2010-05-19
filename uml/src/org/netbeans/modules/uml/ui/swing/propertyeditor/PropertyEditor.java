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



package org.netbeans.modules.uml.ui.swing.propertyeditor;

import java.lang.reflect.Method;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.dom4j.Node;
import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.configstringframework.ConfigStringHelper;
import org.netbeans.modules.uml.core.configstringframework.ConfigStringTranslator;
import org.netbeans.modules.uml.core.configstringframework.IConfigStringTranslator;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IReference;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlsupport.ICustomValidator;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.EnumTranslator;
import org.netbeans.modules.uml.core.support.umlutils.IEnumTranslator;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinitionFactory;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinitionFilter;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElementManager;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.core.support.umlutils.PropertyDefinitionFactory;
import org.netbeans.modules.uml.core.support.umlutils.PropertyDefinitionFilter;
import org.netbeans.modules.uml.core.support.umlutils.PropertyElementManager;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperationSignatureChangeContextManager;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameter;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.OperationSignatureChangeContextManager;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.support.applicationmanager.INameCollisionListener;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.NameCollisionListener;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.commonresources.ICommonResourceManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;
import org.netbeans.modules.uml.ui.swing.treetable.JDefaultMutableTreeNode;
import org.netbeans.modules.uml.ui.swing.treetable.JDescribeComboBox;
import org.netbeans.modules.uml.ui.swing.treetable.JPropertyTreeTable;
import org.netbeans.modules.uml.ui.swing.treetable.JTreeTable;
import org.netbeans.modules.uml.ui.swing.treetable.PropertyTreeTableModel;
import org.netbeans.modules.uml.ui.support.drawingproperties.IColorProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IFontProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IPropertyContainer;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.finddialog.FindController;
import org.netbeans.modules.uml.ui.support.finddialog.IFindController;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
//import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;

/**
 * @author sumitabhk
 *
 */
public class PropertyEditor extends JPanel implements IPropertyEditor, ActionListener
{
	public static int EDITOR_ALL = 0;
	public static int EDITOR_DATA = 1;
	public static int EDITOR_PRES = 2;
	public static int EDITOR_SEL = 3;
	private static String[][] m_MenuItems = { {PropertyEditorResources.getString("PropertyEditor.Fill_Down_Menu")}, //$NON-NLS-1$
											  {PropertyEditorResources.getString("PropertyEditor.Create_Menu")}, //$NON-NLS-1$
											  {PropertyEditorResources.getString("PropertyEditor.Create_Menu"), PropertyEditorResources.getString("PropertyEditor.Delete_Menu")}, //$NON-NLS-1$ //$NON-NLS-2$
										  	  {PropertyEditorResources.getString("PropertyEditor.Delete_Menu")}, //$NON-NLS-1$
											  {PropertyEditorResources.getString("PropertyEditor.Associate_Menu")}, //$NON-NLS-1$
											  {PropertyEditorResources.getString("PropertyEditor.Create_Menu"), PropertyEditorResources.getString("PropertyEditor.Delete_Menu")}, //$NON-NLS-1$ //$NON-NLS-2$
									 		  {PropertyEditorResources.getString("PropertyEditor.Create_Menu"), PropertyEditorResources.getString("PropertyEditor.Delete_Menu"), PropertyEditorResources.getString("PropertyEditor.InsertBefore_Menu")}, //$NON-NLS-1$ //$NON-NLS-2$
											  {PropertyEditorResources.getString("PropertyEditor.Delete_Menu"), PropertyEditorResources.getString("PropertyEditor.Navigate_Menu")} //$NON-NLS-1$ //$NON-NLS-2$
											};


	private IPropertyDefinitionFactory m_Factory = null;
	private IPropertyElementManager   m_PropEleMgr = null;
	private IProject  m_Project = null;
	private ICommonResourceManager m_ResourceMgr = null;
	private Vector<IPropertyElement> m_Elements = new Vector<IPropertyElement>();
	private Vector<IPropertyDefinition> m_Definitions = new Vector<IPropertyDefinition>();
	private Vector<IPropertyElement> m_LoadedImages = new Vector<IPropertyElement>();
	private PropertyEditorFilter   m_Filter = null;
	private boolean m_bSinksConnected = false;
	private boolean m_RespondToReload = false;
	private boolean m_InDelayedAction = false;
	private boolean m_IsShowingSelected = false;
	private Object m_PropertyGrid = null;
	/// The event handler for the various controls and the core UML metamodel
	private PropertyEditorEventsSink m_EventsSink = null;
	private INameCollisionListener m_NameCollisionListener = null;
	private IPropertyEditorCollisionHandler m_CollisionHandler = null;
	private JComboBox m_FilterCombo = null;
	private JToolTip m_ToolTip = null;
	private JPopupMenu m_Menu = new JPopupMenu();
	private JDefaultMutableTreeNode m_root = null;
	private PropertyTreeTableModel m_Model = null;
	private JTreeTable m_Tree = null;
	private int m_CurRow = 0;
	private int m_LastRow = 0;
	private boolean m_Reordered = false;
	private String m_ListBuffer = null;
	private Object m_CurLoadedObject = null;
	DispatchHelper m_DispatchHelper = new DispatchHelper();
	
	//private static PropertyEditor m_Instance = null;
	
	private int m_Mode = 0;
	private boolean focusChange = false;
	private boolean m_processingRecord = false;
	private JDefaultMutableTreeNode m_RecordNode = null;
	
	//this variable captures the row where user right clicked.
	private int m_RightClickRow = 0;
	
	private int m_EditableColor = 0;
	private int m_ReadOnlyColor = 0;

	private IOperationSignatureChangeContextManager m_SigChange = null;
	
//	public static PropertyEditor instance()
//	{
//		if (m_Instance == null)
//		{
//			m_Instance = new PropertyEditor();
//		}
//		return m_Instance;
//	}
	/**
	 * 
	 */
	public PropertyEditor()
	{
		super();
		GridBagLayout gbl = new GridBagLayout();
//		double[] rowVals = {0, 1};
//		double[] colVals = {0, 0.5, 0.5};
//		gbl.rowWeights = rowVals;
//		gbl.columnWeights = colVals;
		setLayout(new GridBagLayout());
		init();
		//m_Menu.setLightWeightPopupEnabled(true);
//		PropertyEditorEventsSink eventSink = new PropertyEditorEventsSink(this);
//		m_DispatchHelper.registerProjectTreeEvents(eventSink);
//		m_DispatchHelper.registerDrawingAreaCompartmentEvents(eventSink);
//		m_DispatchHelper.registerDrawingAreaSelectionEvents(eventSink);
//		m_DispatchHelper.registerDrawingAreaEvents(eventSink);

		//register for events
		initialize();
	}

	private void init()
	{
		if (m_Factory == null)
		{
			getPropertyDefinitionFactory();
		}
		
		ICoreProduct prod = CoreProductManager.instance().getCoreProduct();
		IConfigManager conMan = prod != null ? prod.getConfigManager() : null;
		if (conMan != null)
		{
			String home = conMan.getDefaultConfigLocation();
			String file = home;
			file += "PropertyDefinitions.etc"; //$NON-NLS-1$
			m_Factory.setDefinitionFile(file);
			//m_Factory.buildDefinitionsUsingFile();
		}
		
		if (m_PropEleMgr == null)
		{
			getPropertyElementManager();
		}
		m_PropEleMgr.setPDFactory(m_Factory);
		
		m_ResourceMgr = CommonResourceManager.instance();
		
		m_Filter = new PropertyEditorFilter();
		m_Filter.build();
		
		// help box
		initializeHelpBox();
		// buttons
		initializeButtons();
		// grid stuff
		initializeGrid();
		// combo box, must be after the grid, because we use the mode which is determined
		  // in the grid stuff
		initializeComboBox();
		  // fill in the grid
		populateGrid();
	}

	public void prepareToShow()
	{
		removeAll();
		if (m_Tree != null)
		{
			//ETSystem.out.println("Tree is not null so displaying it");

			//JSplitPane sPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			JScrollPane pane = new JScrollPane(m_Tree);
			//sPane.add(m_FilterCombo);
			//sPane.add(pane);
			
			GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints.weightx = 0.1;
			gridBagConstraints.weighty = 0.0;
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
			//add(sPane, gridBagConstraints);
         add(m_FilterCombo, gridBagConstraints);
         
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.weightx = 0.9;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         add(pane, gridBagConstraints);

			refresh();
		}
	}
	

	/**
	 * 
	 */
	private void initializeHelpBox()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	private void initializeButtons()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	private void initializeComboBox()
	{
		if (m_FilterCombo == null)
		{
			m_FilterCombo = new JComboBox();
		}
		if (m_Filter != null)
		{
			//get common map
			Iterator iter = m_Filter.getCommonMapIter();
			if (iter != null)
			{
				while (iter.hasNext())
				{
					Object obj = iter.next();
					if (obj != null)
					{
						String value = obj.toString();
						//ETSystem.out.println("Combo box value " + value);
						m_FilterCombo.addItem(value);
					}
				}
			}
			//Add another item to access filter dialog
			m_FilterCombo.addItem("..."); //$NON-NLS-1$
			setFilterComboBoxValue();

			m_FilterCombo.addActionListener(this);
		}
	}
	
	/**
	 * Set up value in Filter combo box
	 *
	 * @return void
	 */
	private void setFilterComboBoxValue() {
            if (m_FilterCombo != null) {
                
                //kris richards - "DefaultFilter" pref expunged. Set to "PSK_DATA"
                String filter = "PSK_DATA";
                m_FilterCombo.setSelectedItem(filter);
                
                
            }
        }
	

	/**
	 * 
	 */
	private void initializeGrid()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * Initializes the property editor by creating an event sink and then listening to
	 * drawing area select/deselect events
	 *
	 * @return HRESULT
	 *
	 */
	public long initialize()
	{
		// Set up sinks
		connectSinks(true);

	  // register for accelerator keys for navigation within the grid
	  registerAccelerators();
	  registerAcceleratorsNoFocus();
		
	  // Create the name collision listener
		m_NameCollisionListener = new NameCollisionListener();
		m_CollisionHandler = new PropertyEditorCollisionHandler();
		
		m_NameCollisionListener.setHandler(m_CollisionHandler);
		m_CollisionHandler.setPropertyEditor(this);
		
		return 0;
	}

	/**
	 * 
	 */
	private void registerAcceleratorsNoFocus()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	private void registerAccelerators()
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * Gets the property definitions for the editor
	 */
	public Vector<IPropertyDefinition> getPropertyDefinitions()
	{
		return m_Definitions;
	}

	/**
	 * Sets the property definitions for the editor
	 */
	public void setPropertyDefinitions(Vector<IPropertyDefinition> value)
	{
		m_Definitions = value;
	}

	/**
	 * Gets the property elements for the editor
	 */
	public Vector<IPropertyElement> getPropertyElements()
	{
		return m_Elements;
	}

	/**
	 * Sets the property elements for the editor
	 */
	public void setPropertyElements(Vector<IPropertyElement> value)
	{
		m_Elements = value;
		m_root = null;
		m_Model = null;
		populateGrid();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditor#clear()
	 */
	public long clear()
	{
		if (m_Elements != null){
			m_Elements.clear();
		}
		if (m_LoadedImages != null){
	      m_LoadedImages.clear();
		}
		if (m_Definitions != null){
    	  m_Definitions.clear();
		}
      
		m_Model = null;
		m_root = null;
		m_Tree = null;
		doLayout();
		m_ListBuffer = ""; //$NON-NLS-1$
		m_IsShowingSelected = false;
		m_LastRow = 0;
		m_CurRow = 0;
		endSignatureChange();
		
		if (m_PropEleMgr != null)
		{
			// The manager can hold onto elements so when
			// the workspace closes make sure to clear out 
			// it.
			m_PropEleMgr.setModelElement(null);
			m_PropEleMgr.setPresentationElement(null);
		}
      
      m_CurLoadedObject = null;
      
		//populateGrid();
		return 0;
	}

	/**
	 * Gets the property element manager for the editor.  Creates one if necessary.
	 */
	public IPropertyElementManager getPropertyElementManager()
	{
		if (m_PropEleMgr == null)
		{
			m_PropEleMgr = new PropertyElementManager();
		}
		return m_PropEleMgr;
	}

	/**
	 * Begin the process of saving the information in the property editor
	 *
	 * @return HRESULT
	 *
	 */
	public long save()
	{
		if (m_Tree != null)
		{
			int count = m_Tree.getRowCount();

			// loop through the information in the grid by row
			for (int i=0; i<count; i++)
			{
				// get the property element at the currently processed row
				IPropertyElement pEle = getElementAtGridRow(i);
				if (pEle != null)
				{
					// has this property element been marked as modified
					boolean isMod = pEle.getModified();
					if (isMod)
					{
						// get the corresponding definition
						IPropertyDefinition pDef = pEle.getPropertyDefinition();
						if (pDef != null)
						{
							// just another check to see if we should set some data
							String cType = pDef.getControlType();
							if (cType != null && cType.length() > 0)
							{
								setRowData(i, pDef, pEle);
							}
						}
					}
				}
				else
				{
					IColorProperty pColorProperty = getColorPropertyAtGridRow(i);
					IFontProperty pFontProperty = getFontPropertyAtGridRow(i);
					
					if (pColorProperty != null)
					{
//						boolean isMod = pColorProperty.isModified();
//						if (isMod)
						{
//							pColorProperty.save();
						}
					}
					else if (pFontProperty != null)
					{
//						boolean isMod = pFontProperty.isModified();
//						if (isMod)
						{
//							pFontProperty.save();
						}
					}
				}
			}
		}
		return 0;
	}

	/**
	 * Returns the font property at this grid row
	 *
	 * @param[in] row          The row to retrieve the property element from
	 * @param[out] pProperty   The found font property
	 *
	 * @return HRESULT
	 *
	 */
	private IFontProperty getFontPropertyAtGridRow(int row)
	{
		IFontProperty retProp = null;
		
		if (row > 0 && m_Model != null)
		{
			JDefaultMutableTreeNode node = getNodeAtGridRow(row);
			if (node != null)
			{
				Object obj = node.getUserObject();
				if (obj instanceof IFontProperty)
				{
					retProp = (IFontProperty)obj;
				}
			}
		}
		
		return retProp;
	}

	/**
	 * Returns the color property at this grid row
	 *
	 * @param[in] row          The row to retrieve the property element from
	 * @param[out] pProperty   The found color property
	 *
	 * @return HRESULT
	 *
	 */
	private IColorProperty getColorPropertyAtGridRow(int row)
	{
		IColorProperty retProp = null;
		
		if (row > 0 && m_Model != null)
		{
			JDefaultMutableTreeNode node = getNodeAtGridRow(row);
			if (node != null)
			{
				Object obj = node.getUserObject();
				if (obj instanceof IColorProperty)
				{
					retProp = (IColorProperty)obj;
				}
			}
		}
		
		return retProp;
	}

	/**
	 * Begin to store the data in the property element
	 *
	 * @param[in] row    The row in the grid that we are currently setting the data on
	 * @param[in] pDef   The property definition associated with the element at this row
	 * @param[in] pEle   The property element at this row
	 *
	 * @return HRESULT
	 *
	 */
	private void setRowData(int i, IPropertyDefinition pDef, IPropertyElement pEle)
	{
		beginEditContext();
		if (m_Model != null)
		{
			// Block other events
			PropertyEditorBlocker.block();
			try
			{
				// get the model element associated with the property element
				Object pDisp = pEle.getElement();
				if (pDisp != null)
				{
					// if we are in an operation or any subelement of an operation
					// we need to start a signature change on it.  RT needs all operation
					// changes to be at once, not one at a time
					IOperation pTempOp = getOperationFromPropertyElement(pEle);
					if (pTempOp != null)
					{
						if (m_SigChange == null)
						{
							m_SigChange = new OperationSignatureChangeContextManager();
							m_SigChange.startSignatureChange(pTempOp);
						}
					}
					// if there is a model element, we will just need to do set datas
					m_PropEleMgr.processData(pDisp, pDef, pEle);

					// the actual processing of the data would have done a set, but someone else may
					// have cancelled the put and unfortunately the put_ is successful, so we don't have
					// any way of knowing that it failed
					// After we did a put_, we did a get and refilled the element, so we need to refresh the
					// grid with the element's value
					m_PropEleMgr.interpretElementValue(pEle);
					String value = pEle.getValue();
					//refresh the element value in the grid.
				}
				else
				{
					// no model element, so we will need to create one
					// this also handles the setting of the data of the newly created model element
					// go up the property element chain and find the first model element that you come to
					Object pDisp2 = getModelElement(pEle);
					if (pDisp2 != null)
					{
						// go up the property element chain and find the first one that has the ability to insert
						IPropertyElement pInsertEle = getInsertElement(pEle);
						if (pInsertEle != null)
						{
							// get the corresponding definition for the insert element
							IPropertyDefinition pInsertDef = pInsertEle.getPropertyDefinition();
							if (pInsertDef != null)
							{
								// process the data for the found model element and the insert information
								// ie.  This will do a IClass::CreateAttribute
								m_PropEleMgr.processData(pDisp2, pInsertDef, pInsertEle);
							
								// so now we have the newly created IDispatch
								Object pNewDisp = pInsertEle.getElement();

								// we are having problems in RT when dealing with the sets of an operation
								// the change events cannot handle single changes to the signature
								// a context was created to handle this and was being used by everything
								// but the property editor, so now we are special casing an operation
								
								IOperation pTempOp = getOperationFromPropertyElement(pInsertEle);
								if (pTempOp != null)
								{
									if (m_SigChange == null)
									{
										// create and start the signature context
										m_SigChange = new OperationSignatureChangeContextManager();
										m_SigChange.startSignatureChange(pTempOp);
									}
								}
//								if (pNewDisp instanceof IOperation)
//								{
//									IOperation op1 = (IOperation)pNewDisp;
//									// create and start the signature context
//									IOperationSignatureChangeContextManager sigChange = new OperationSignatureChangeContextManager();
//									//sigChange.startSignatureChange(op1);
//								}
//								else if (pNewDisp instanceof IParameter)
//								{
//									IParameter parm1 = (IParameter)pNewDisp;
//									// create and start the signature context
//									IOperationSignatureChangeContextManager sigChange = new OperationSignatureChangeContextManager();
//									IBehavioralFeature pOp2 = parm1.getBehavioralFeature();
//									if (pOp2 instanceof IOperation)
//									{
//										IOperation pOp3 = (IOperation)pOp2;
//										sigChange.startSignatureChange(pOp3);
//									}
//								}

								// this was originally kicked off
								// because information was entered into the property editor, so we still need
								// to process that
								m_PropEleMgr.processData(pNewDisp, pDef, pEle);
							}
						}
					}
				}
			}
			finally
			{
				PropertyEditorBlocker.unblock();
			}
		}
		endEditContext();
	}

	/**
	 * Method to navigate up the property element chain to retrieve the property element that represents
	 * the one that should be inserted into (the one with an insert method)
	 *
	 * @param[in] pEle          The property element in which to get the insert element that it belongs to 
	 * @param[out] pInsertEle   The property element that is the element in which to perform the insert
	 *
	 * @return HRESULT
	 *
	 */
	private IPropertyElement getInsertElement(IPropertyElement pEle)
	{
		IPropertyElement retEle = null;
		
		if (pEle != null)
		{
			// get the corresponding definition
			IPropertyDefinition pDef = pEle.getPropertyDefinition();
			if (pDef != null)
			{
				// get the insert method
				String insMethod = pDef.getCreateMethod();
				if (insMethod == null || insMethod.length() == 0)
				{
					IPropertyElement parentEle = pEle.getParent();
					if (parentEle != null)
					{
						retEle = getInsertElement(parentEle);
					}
				}
				else
				{
					retEle = pEle;
				}
			}
		}
		return retEle;
	}

	/**
	 * Method to navigate up the property element chain to retrieve the property element that represents
	 * the one that should be deleted from (the one with a delete method)
	 *
	 * @param[in] pEle          The property element in which to get the insert element that it belongs to 
	 * @param[out] pInsertEle   The property element that is the element in which to perform the insert
	 *
	 * @return HRESULT
	 *
	 */
	private IPropertyElement getDeleteElement(IPropertyElement pEle)
	{
		IPropertyElement retEle = null;
		
		if (pEle != null)
		{
			// get the corresponding definition
			IPropertyDefinition pDef = pEle.getPropertyDefinition();
			if (pDef != null)
			{
				// get the delete method
				String delMethod = pDef.getDeleteMethod();
				if (delMethod == null || delMethod.length() == 0)
				{
					IPropertyElement parentEle = pEle.getParent();
					if (parentEle != null)
					{
						retEle = getDeleteElement(parentEle);
					}
				}
				else
				{
					retEle = pEle;
				}
			}
		}
		return retEle;
	}
	/**
	 * Method to navigate up the property element chain to retrieve the first model element in the chain
	 *
	 * @param[in] pEle       The property element in which to get the model element 
	 * @param[out] pModEle   The model element
	 *
	 * @return HRESULT
	 *
	 */
	private Object getModelElement(IPropertyElement pEle)
	{
		Object retObj = null;
		if (pEle != null)
		{
			// get the model element
			Object pDisp = pEle.getElement();
			if (pDisp == null)
			{
				IPropertyElement parentEle = pEle.getParent();
				if (parentEle != null)
				{
					retObj = getModelElement(parentEle);
				}
			}
			else
			{
				retObj = pDisp;
			}
		}
		return retObj;
	}

	/**
	 * Gets the property definition factory for the editor.  Creates one if necessary.
	 */
	public IPropertyDefinitionFactory getPropertyDefinitionFactory()
	{
		if (m_Factory == null)
		{
			m_Factory = new PropertyDefinitionFactory();
		}
		if (m_Factory != null)
		{
			ICoreProduct prod = CoreProductManager.instance().getCoreProduct();
			IConfigManager conMan = prod != null ? prod.getConfigManager() : null;
			if (conMan != null)
			{
				String home = conMan.getDefaultConfigLocation();
				String file = home + "PropertyDefinitions.etc"; //$NON-NLS-1$
				m_Factory.setDefinitionFile(file);
			}
		}
		return m_Factory;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditor#setProject(org.netbeans.modules.uml.core.metamodel.structure.IProject)
	 */
	public void setProject(IProject value)
	{
		m_Project = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditor#setFocus()
	 */
	public long setFocus()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditor#populateGrid()
	 */
	public long populateGrid()
	{
		if (m_Elements != null && m_Elements.size() > 0)
		{
			int count = m_Elements.size();
			m_root = new JDefaultMutableTreeNode("Root"); //$NON-NLS-1$
			for (int i=0; i<count; i++)
			{
				IPropertyElement pEle = m_Elements.elementAt(i);
				String topName = calculateTopElementName(pEle);
				pEle.getPropertyDefinition().setPropertyEditorShowName(topName);
				JDefaultMutableTreeNode node = new JDefaultMutableTreeNode(pEle, true);
				node.setIsRoot(true);
				buildSubElementsThatNeedToDisplay(pEle, node);
				node.setExpanded(true);
				m_root.add(node);
				//m_Model.expand(i, true);
			}
			//ETSystem.out.println("Going to show the property Element " + pEle.getName());
			//m_root = new JDefaultMutableTreeNode(pEle, true);
			m_Model = null;
			m_Model = new PropertyTreeTableModel(m_root, this);
			//Vector<Object> subEles = m_Elements;
			//subEles.remove(0); 
			//m_Model.setChildren(subEles);
			m_Tree = null;
			m_Tree = new JPropertyTreeTable(m_Model, this);
			m_Tree.getTree().setRootVisible(false);
			m_Model.setTreeTable(m_Tree);
			for (int i=count-1; i>=0; i--)
			{
				m_Model.expand(i, true);
			}
		}
		else
		{
			m_root = new JDefaultMutableTreeNode();
			m_Model = new PropertyTreeTableModel(m_root, this);
			m_Tree = new JPropertyTreeTable(m_Model, this);
			m_Model.setTreeTable(m_Tree);
		}
		prepareToShow();
		return 0;
	}

	/**
	 * Add a top level property element to the grid
	 *
	 * @param[in] pElement   The property element to add
	 *
	 * @return HRESULT
	 *
	 */
	public JDefaultMutableTreeNode addTopElementToGrid(IPropertyElement pEle)
	{
		JDefaultMutableTreeNode retNode = null;
		
		if (m_Model != null)
		{
			// for the state diagram, we ran into the case where even though the elements
			// were of the same element type, we wanted to display their kind (initial, final)
			// so do some special processing before determining the main display name
			// add the main element row to the grid
			String name = calculateTopElementName(pEle);
			String value = pEle.getValue();
			retNode = new JDefaultMutableTreeNode(pEle);
		}
		
		return retNode;
	}

	/**
	 * Reload the particular property element that is associated with the passed in
	 * model element
	 *
	 * @param[in] pDisp  The model element that needs to be reloaded
	 *
	 * @return HRESULT
	 *
	 */
	public long reloadElement(Object pDisp)
	{
		// check this first to determine if we should even try to reload
		if (m_RespondToReload)
		{
			// We were just casting the information to an IElement, but this did not work
			// in the case of an IProject, so had to add an extra check here so that we
			// know what to cast it too
			if (pDisp instanceof IPresentationElement)
			{
				IPresentationElement pres = (IPresentationElement)pDisp;
				IElement pEle = pres.getFirstSubject();
				if (pEle != null)
				{
					reloadElement2(pEle);
				}
			}
			else if (pDisp instanceof IElement)
			{
				IElement pEle = (IElement)pDisp;
				reloadElement2(pEle);
			}
			else if (pDisp instanceof IWSProject)
			{
				IWSProject proj = (IWSProject)pDisp;
				reloadProject(proj);
			}
			else if (pDisp instanceof IProxyDiagram)
			{
				IProxyDiagram diag = (IProxyDiagram)pDisp;
				reloadDiagram(diag);
			}
		}
		m_InDelayedAction = false;
		return 0;
	}

	/**
	 * Reload the particular property element that is associated with the passed in IElement
	 *
	 * @param[in] pElement  The IElement that needs to be reloaded
	 *
	 * @return HRESULT
	 *
	 */
	private void reloadElement2(IElement pEle)
	{
		PropertyEditorBlocker.block();
		try
		{
			// loop through the elements currently in the property editor
			if (m_Elements != null)
			{
				int count = m_Elements.size();
				for (int i=0; i<count; i++)
				{
					IPropertyElement propEle = m_Elements.get(i);
					boolean bReload = false;

					// get the model elements from the property element
					Object pDisp = propEle.getElement();
					IElement pToReload = null;

					// is the dispatch on the property element a presentation element
					// if so, need to get its model element before continuing
					if (pDisp instanceof IPresentationElement)
					{
						IPresentationElement presEle = (IPresentationElement)pDisp;
						IElement pModelEle = presEle.getFirstSubject();
						if (pModelEle != null)
						{
							bReload = shouldReloadElement(pEle, pModelEle);
							pToReload = pModelEle;
						}
					}
					else
					{
						if (pDisp instanceof IElement)
						{
							IElement element = (IElement)pDisp;
							bReload = shouldReloadElement(pEle, element);
							pToReload = element;
						}
					}

					// we have now determined whether or not the property editor should
					// be reloaded because an element that is displayed in it
					if (bReload && !m_InDelayedAction)
					{
						IPropertyDefinition pDef = propEle.getPropertyDefinition();
						// build a new property element (refresh its data)
						if (pToReload != null)
						{
							IPropertyElement propEle2 = m_PropEleMgr.buildElement(pToReload, pDef, propEle);
							filterPropertyElement(propEle2);

							// remove the old one
							m_Elements.removeElementAt(i);
							// add the new one
							m_Elements.add(propEle2);
							// refresh the grid
							setPropertyElements(m_Elements);
							break;
						}
					}
				}
			}
		}
		finally
		{
			PropertyEditorBlocker.unblock();
		}
	}

	/**
	 * Determines whether or not the property editor should reload its data
	 * 
	 *
	 * @param pCurr[in]					The element that has changed as a result of some user action in the tool
	 * @param pCurrInEditor[in]		The element currently displayed in the editor
	 * @param bReload[in]				Whether or not the property editor should reload its data
	 *
	 * @return HRESULT
	 *
	 */
	private boolean shouldReloadElement(IElement pEle, IElement pModelEle)
	{
		boolean bReload = false;
		
		// does this property element have the same model element as the one passed in
		boolean isSame = pEle.isSame(pModelEle);
		if (isSame)
		{
			bReload = true;
		}
		else
		{
			// may not be the same, but if it is a child of what is in the property editor, we
			// still want to reload it
			boolean isOwned = pModelEle.isOwnedElement(pEle);
			if (isOwned)
			{
				bReload = true;
			}
			else
			{
				// one more check to see if the element coming in for the reload
				// owns what is in the property editor.  if it does then reload
				// it
				boolean isChild = isElementChildElement(pEle, pModelEle);
				if (isChild)
				{
					bReload = true;
				}
			}
		}
		
		return bReload;
	}

	/**
	 * Checks to see if the passed in parent element contains the passed in child element.  It does
	 * not have to be an owned element, it checks by dom node containment (whether it is under the
	 * structure or not)
	 * 
	 *
	 * @param pParent[in]		Element that is the parent element
	 * @param pChild[in]			Element that is the child element
	 * @param bIsChild[in]		Whether or not it is a child of the parent
	 *
	 * @return HRESULT
	 *
	 */
	private boolean isElementChildElement(IElement parent, IElement child)
	{
		boolean bIsChild = false;
		Node parNode = parent.getNode();
		if (parNode != null)
		{
			String id = child.getXMIID();
			if (id != null && id.length() > 0)
			{
				String pattern = ".//*[@xmi.id=\'"; //$NON-NLS-1$
				pattern += id;
				pattern += "\']"; //$NON-NLS-1$
				Node foundNode = parNode.selectSingleNode(pattern);
				if (foundNode != null)
				{
					bIsChild = true;
				}
			}
		}
		return bIsChild;
	}

	/**
	 * Reload the particular property element that is associated with the passed in project
	 *
	 * @param[in] pProject  The IWSProject that needs to be reloaded
	 *
	 * @return HRESULT
	 *
	 */
	private void reloadProject(IWSProject proj)
	{
		m_PropEleMgr.setModelElement(proj);
		// remove everything in the property editor first because
		// this was causing memory leaks if the project was closed
		// and stuff still was being displayed in the editor
		if (m_Elements != null)
		{
			m_Elements.removeAllElements();
			m_Elements = null;
			m_Elements = new Vector<IPropertyElement>();
			
			// is what is being passed in an open or closed project
			// this will be determined by asking the application for the project with
			// this name, if we get one back it is open
			IApplication app = ProductHelper.getApplication();
			if (app != null)
			{
				String name = proj.getName();
				IProject pProj = app.getProjectByName(name);
				if (pProj != null)
				{
					// but now we actually want to build the information for an IProject, not a IWSProject
					// which is what we have
					// so get the definition for an IProject
					IPropertyDefinition pDef = m_Factory.getPropertyDefinitionForElement("Project", pProj); //$NON-NLS-1$
					if (pDef != null)
					{
						// build a new property element (refresh its data)
						m_PropEleMgr.setModelElement(pProj);
						IPropertyElement propEle2 = m_PropEleMgr.buildElement(pProj, pDef, null);
						filterPropertyElement(propEle2);
						// add the new one
						m_Elements.add(propEle2);
						// refresh the grid
						setPropertyElements(m_Elements);
					}
				}
				else
				{
					IPropertyDefinition pDef = m_Factory.getPropertyDefinitionForElement("WSProject", proj); //$NON-NLS-1$
					if (pDef != null)
					{
                  if(m_PropEleMgr != null)
                  {
   						// build a new property element (refresh its data)
   						IPropertyElement propEle2 = m_PropEleMgr.buildElement(proj, pDef, null);
   						filterPropertyElement(propEle2);
                     
                     if(m_Elements == null)
                     {
                        m_Elements = new Vector<IPropertyElement>();
                     }
                     
   						// add the new one
   						m_Elements.add(propEle2);
   						// refresh the grid
   						setPropertyElements(m_Elements);
                  }
					}
				}
			}
		}
	}

	/**
	 * Reload the particular property element that is associated with the passed in diagram
	 *
	 * @param[in] pElement  The diagram that needs to be reloaded
	 *
	 * @return HRESULT
	 */
	private void reloadDiagram(IProxyDiagram diag)
	{
		PropertyEditorBlocker.block();
		try
		{
			// loop through the elements currently in the property editor
			if (m_Elements != null)
			{
				int count = m_Elements.size();
				for (int i=0; i<count; i++)
				{
					IPropertyElement propElement = m_Elements.get(i);

					// get the model elements from the property element
					Object pDisp = propElement.getElement();
					if (pDisp instanceof IDiagram)
					{
						IDiagram pEle = (IDiagram)pDisp;
						IDiagram dia1 = diag.getDiagram();
						if (dia1 != null)
						{
							// does this property element have the same model element as the one passed in
							boolean isSame = dia1.isSame(pEle);
							if (isSame)
							{
								IPropertyDefinition pDef = propElement.getPropertyDefinition();
								// build a new property element (refresh its data)
								IPropertyElement propEle = m_PropEleMgr.buildElement(pDisp, pDef, propElement);
								filterPropertyElement(propEle);
							
								// remove the old one
								m_Elements.removeElementAt(i);
								// add the new one
								m_Elements.add(propEle);
								// refresh the grid
								setPropertyElements(m_Elements);
							}
						}
					}
				}
			}
		}
		finally
		{
			PropertyEditorBlocker.unblock();
		}
	}

	/**
	 *	Registers or revokes event sinks.
	 *
	 * @param bConnect
	 *
	 * @return HRESULT
	 *
	 */
	public long connectSinks(boolean bConnect)
	{
		DispatchHelper helper = new DispatchHelper();
		if (bConnect && !m_bSinksConnected)
		{
			if (m_EventsSink == null)
			{
				m_EventsSink = new PropertyEditorEventsSink();
				m_EventsSink.setPropertyEditor(this);
			}
			
			if (m_EventsSink != null)
			{
				//Register for events
				helper.registerForWorkspaceEvents(m_EventsSink);
				helper.registerForLifeTimeEvents( m_EventsSink );
//				helper.registerDrawingAreaSelectionEvents(m_EventsSink);
//				helper.registerDrawingAreaEvents(m_EventsSink);
//				helper.registerDrawingAreaCompartmentEvents(m_EventsSink);
				helper.registerProjectTreeEvents(m_EventsSink);
				helper.registerForTransformEvents(m_EventsSink);
				helper.registerForAttributeEvents(m_EventsSink);
				helper.registerForOperationEvents(m_EventsSink);
				helper.registerForElementModifiedEvents(m_EventsSink);
				helper.registerForWSProjectEvents(m_EventsSink);
				helper.registerForPreferenceManagerEvents(m_EventsSink);
				helper.registerForInitEvents(m_EventsSink);

				m_bSinksConnected = true;

				m_RespondToReload = true;
			}
		}
		else if (!bConnect && m_bSinksConnected)
		{
			try
			{
//				helper.revokeDrawingAreaSelectionSink(m_EventsSink);
//				helper.revokeDrawingAreaSink(m_EventsSink);
//				helper.revokeDrawingAreaCompartmentSink(m_EventsSink);
				helper.revokeProjectTreeSink(m_EventsSink);
				helper.revokeLifeTimeSink( m_EventsSink );
				helper.revokeTransformSink(m_EventsSink);
				helper.revokeWorkspaceSink(m_EventsSink);
				helper.revokeAttributeSink(m_EventsSink);
				helper.revokeOperationSink(m_EventsSink);
				helper.revokeElementModifiedSink(m_EventsSink);
				helper.revokeWSProjectSink(m_EventsSink);
				helper.revokePreferenceManagerSink(m_EventsSink);
				helper.revokeInitSink(m_EventsSink);
			}
			catch (InvalidArguments e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			m_bSinksConnected = false;
		}
		return 0;
	}

	/** 
	 * returns the TreeTable used in this property editor.
	 */
	public Object getGrid()
	{
		return m_Tree;
	}

	/**
	 * Load the passed in IElement into the property editor
	 *
	 * @param pElement[in]		The element to put into the editor
	 *
	 * @return HRESULT
	 */
	public long loadElement(IElement pElement)
	{
		if (PropertyEditorBlocker.inProcess() == false /*&& m_Model != null*/)
		{
			processLastCell(true);
			clear();
			//
			// the property editor is loaded by creating property definitions(which
			// represent the structure of the data) and property elements(which represent
			// the actual data)
			//
			String kind = ""; //$NON-NLS-1$
			if (pElement instanceof IDiagram)
			{
				kind = "Diagram"; //$NON-NLS-1$
			}
			
			Vector<IPropertyElement> propElems = new Vector<IPropertyElement>();
			Vector<IPropertyDefinition> propDefs = new Vector<IPropertyDefinition>();
			IPropertyElement pEle = processSelectedItem(kind, propDefs, pElement);
			if (pEle != null)
			{
				// since we are coming from the drawing area, we will also want the
				// capability of showing the presentation information, so store the
				// presentation element on the property element
				pEle.setElement(pElement);
				propElems.add(pEle);
			}
			setPropertyDefinitions(propDefs);
			setPropertyElements(propElems);
			m_CurLoadedObject = pElement;
		}
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Load the passed in IElements into the property editor
	 *
	 * @param pElements[in]		The elements to put into the editor
	 *
	 * @return HRESULT
	 */
	public long loadElements(IElement[] pElements)
	{
		//might want to use m_Tree instead of m_PropertyGrid
		if (PropertyEditorBlocker.inProcess() == false && m_PropertyGrid != null)
		{
			processLastCell(true);
			clear();
			//
			// the property editor is loaded by creating property definitions(which
			// represent the structure of the data) and property elements(which represent
			// the actual data)
			//
			int count = pElements.length;
			
			// get how many should be displayed from preferences
			IPreferenceAccessor pref = PreferenceAccessor.instance();
			int max = pref.getDefaultEditorSelect();
			if (count < max)
			{
				max = count;
			}
			
			Vector<IPropertyElement> propElems = new Vector<IPropertyElement>();
			Vector<IPropertyDefinition> propDefs = new Vector<IPropertyDefinition>();
			// loop through the items selected and build the corresponding definitions and elements
			for (int i=0; i<max; i++)
			{
				// what we have selected from the project tree is a project tree item
				IElement pElement = pElements[i];

				// more processing to create the property element
				IPropertyElement pEle = processSelectedItem("", propDefs, pElement); //$NON-NLS-1$
				if (pEle != null)
				{
					propElems.add(pEle);
				}
			}
			setPropertyDefinitions(propDefs);
			setPropertyElements(propElems);
		}
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Create the property element that represents whatever is selected from the drawing area or the project tree
	 *
	 * @param[in] kind            The element type       
	 * @param[in] pDefs           The array of property definitions associated with the property editor
	 * @param[in] pModelElement   The model element that is selected
	 * @param[out] pEle           The property element that has been built based on a property definition
	 *
	 * @return HRESULT
	 *
	 */
	public IPropertyElement processSelectedItem(String kind, Vector<IPropertyDefinition> propDefs, Object pElement)
	{
		IPropertyElement retEle = null;
		if (m_Factory != null)
		{
			// get the property definition that represents this kind of model element
			// this will create it from the xml file if it hasn't already been created
			IPropertyDefinition propDef = m_Factory.getPropertyDefinitionForElement(kind, pElement);
			
			if (propDef != null)
			{
				IElement modelElement = null;
				if (pElement instanceof IElement)
				{
					modelElement = (IElement)pElement;
				} 

				// add this to the property editor's property definitions
				propDefs.add(propDef);
				if (m_PropEleMgr != null)
				{
					// build the property element
					m_PropEleMgr.setModelElement(pElement);
					m_PropEleMgr.setCreateSubs(true);
					IPropertyElement propEle = m_PropEleMgr.buildTopPropertyElement(propDef);
					if (propEle != null)
					{
						propEle.setElement(pElement);
						// now that we have built the property element structure
						// check the definition filter to see if any of the elements need to
						// change
						filterPropertyElement(propEle);
						retEle = propEle;
						m_CurLoadedObject = modelElement;
					}
				}
			}
		}
		return retEle;
	}
	
	/**
	 * Filter the property element based on an xml file that has been predefined
	 * by the user
	 * 
	 *
	 * @param pEle[in]		The property element to filter
	 *
	 * @return HRESULT
	 *
	 */
	private void filterPropertyElement(IPropertyElement pEle) {
            // check the preference to see if we should filter or not
            //kris richards - "LanguageFilter" pref expunged. Set to true. Condition removed.
            
            // this guy does the filtering
            IPropertyDefinitionFilter pFilter = new PropertyDefinitionFilter();
            // get the element from the property element
            Object pDisp = pEle.getElement();
            if (pDisp instanceof IElement) {
                IElement pModEle = (IElement)pDisp;
                // from the element we should be able to tell what language it is
                ILanguage lang = getActiveLanguage(pModEle);
                if (lang != null) {
                    pFilter.filterPropertyElement(pEle);
                } else {
                    // if the element is new, it may not have a language just yet, so
                    // get the top most property element's model element and use that
                    // as the guy to figure out the language filter
                    Object pTop = getTopModelElement(pEle);
                    if (pTop instanceof IElement) {
                        IElement parentEle = (IElement)pTop;
                        pFilter.filterPropertyElementBasedOnModelElement(pEle, parentEle);
                    }
                }
            } else {
                // there is not a model element on the property element, so
                // get the top most property element's model element and use that
                // as the guy to figure out the language filter
                Object pTop = getTopModelElement(pEle);
                if (pTop instanceof IElement) {
                    IElement parentEle = (IElement)pTop;
                    pFilter.filterPropertyElementBasedOnModelElement(pEle, parentEle);
                }
            }
            
        }

	/**
	 * Retrieves a model elements associated language.  If the model element
	 * is associated to more than one language then the first language is the 
	 * active language.
	 *
	 * @param pElement [in] The element being processed.
	 * @param pVal [out] The active language for the element.
	 */
	private ILanguage getActiveLanguage(IElement pElement)
	{
		ILanguage retLang = null;
		// Find the first language that is supported by the element.  If the 
		// is not supporting any languages (Should never happen) get the
		// default langauge.
		ETList<ILanguage> langs = pElement.getLanguages();
//		if (langs != null && langs.length > 0)
		if (langs != null && langs.size() > 0)
		{
			retLang = langs.get(0);
		}
		return retLang;
	}

	/**
	 * Method to navigate up the property element chain to retrieve the top model element in the chain
	 *
	 * @param[in] pEle       The property element in which to get the model element 
	 * @param[out] pModEle   The model element
	 *
	 * @return HRESULT
	 *
	 */
	private Object getTopModelElement(IPropertyElement pEle)
	{
		Object retObj = null;
		if (pEle != null)
		{
			// get the model element
			Object pDisp = pEle.getElement();
			// get the parent of this property element
			IPropertyElement parentEle = pEle.getParent();
			if (parentEle != null)
			{
				retObj = getTopModelElement(parentEle);
			}
			else
			{
				retObj = pDisp;
			}
		}
		return retObj;
	}


	/**
	 * Because we have changed the way that we do a save - save now on leaving a field
	 * or if in a new property (attribute/operation), save when we leave that particular
	 * property, we need to determine whether or not to save the last cell that we were on
	 *
	 * I think it needs to be called only when a new element is selected and I need to save
	 * the record.
	 *
	 * @return HRESULT
	 *
	 */
	public void processLastCell(boolean elementChanged)
	{
		processLastCell(elementChanged, m_LastRow);
	}
	public void processLastCell(boolean elementChanged, int row)
	{
		if (m_Model != null && row != 0)
		{
			int lastRow = row;

			// get the element at the previous row
			IColorProperty pColorProperty = getColorPropertyAtGridRow(lastRow);
			IFontProperty pFontProperty = getFontPropertyAtGridRow(lastRow);
			JDefaultMutableTreeNode node = getNodeAtGridRow(lastRow);
			IPropertyElement nodeEle = getElementAtGridRow(lastRow);
			
			if (node != null)
			{
				if (node != null)
				{
					//
					// Update the information in the elements, grid, and xml
					// This may cause both a create and a set, or just a set
					//
					boolean mod = nodeEle.getModified();
					if (mod)
					{
						// get the model element at the previous row
						Object pLastDisp = nodeEle.getElement();
				
						// get the definition of the element on the last row
						IPropertyDefinition pDef = nodeEle.getPropertyDefinition();
				
						// Block events from coming in.
						PropertyEditorBlocker.block();
						try
						{
							setRowData(lastRow, pDef, nodeEle);
				
							// update the grid nodes
							if (node != null)
							{
								updateNodeAndParentNodes(node, nodeEle);
							}
							nodeEle.setModified(false);
						}
						finally
						{
							PropertyEditorBlocker.unblock();
						}
					}
				}

				//
				// Now we need to determine if we need to do an insert of the information that
				// was just created or set
				//
				if (node != null)
				{
					JDefaultMutableTreeNode lastRecNode = getGridNodeMarkedAsRecord(node);
					
					//I want the current node to pass to the insertNecessary
					node = getNodeAtGridRow(m_CurRow);
					
					while (lastRecNode != null)
					{
						//if the element selection has changed or 
						if (elementChanged || insertNecessary(node, lastRecNode))
						{
							insert(lastRecNode);
						}
						JDefaultMutableTreeNode parentNode = (JDefaultMutableTreeNode)lastRecNode.getParent();
						lastRecNode = getGridNodeMarkedAsRecord(parentNode);
					}
					
					if (m_Reordered)
					{
						reorderFeatures();
						m_Reordered = false;
					}
					
					if (row > 0)
					{
						// Block events from coming in.
						PropertyEditorBlocker.block();
						try
						{
							// Now we need to see if we need to pop the operation signature context
							IPropertyElement pCurPropEle = getElementAtGridRow(row);
							if (pCurPropEle != null)
							{
								if (m_SigChange != null)
								{
									IOperation pOldOp = m_SigChange.getOperation();
									IOperation pNewOp = getOperationFromPropertyElement(pCurPropEle);
									if (pOldOp != null && pNewOp != null)
									{
										boolean isSame = pOldOp.isSame(pNewOp);
										if (!isSame)
										{
											endSignatureChange();
										}
									}
									else
									{
										endSignatureChange();
									}
								}
							}
							else
							{
								endSignatureChange();
							}
						}
						finally
						{
							PropertyEditorBlocker.unblock();
						}
					}
				}
				m_processingRecord = false;
			}
			else if (pColorProperty != null)
			{
//				boolean modified = pColorProperty.isModified();
//				if (modified)
//				{
//					pColorProperty.save();
//				}
//			}
//			else if (pFontProperty != null)
//			{
//				boolean modified = pFontProperty.isModified();
//				if (modified)
//				{
//					pFontProperty.save();
//				}
			}
		}
	}

	public void saveCellValueAt(int row, String newVal)
	{
		if (m_Model != null && row != 0)
		{
			// get the element at the previous row
			IPropertyElement lastPropEle = getElementAtGridRow(row);
			
			if (lastPropEle != null)
			{
				//
				// Update the information in the elements, grid, and xml
				// This may cause both a create and a set, or just a set
				//
				boolean mod = lastPropEle.getModified();
				if (mod)
				{
					// get the model element at the previous row
					Object pLastDisp = lastPropEle.getElement();
					
					// update the property element
					updatePropertyElementValue(row, lastPropEle, newVal);
					
					// get the definition of the element on the last row
					IPropertyDefinition pDef = lastPropEle.getPropertyDefinition();
					
					// Block events from coming in.
					//PropertyEditorBlocker.block();
					
					setRowData(row, pDef, lastPropEle);
					
					// update the grid nodes
					JDefaultMutableTreeNode node = getNodeAtGridRow(row);
					if (node != null)
					{
						updateNodeAndParentNodes(node, lastPropEle);
					}
					lastPropEle.setModified(false);
				}

			}
		}
	}

	/**
	 * Update the value on the property element
	 *
	 * @param[in] Row  The row that was just edited
	 * @param[in] pEle The property element to update
	 *
	 * @return HRESULT
	 *
	 */
	private void updatePropertyElementValue(int row, IPropertyElement pEle)
	{
		// get the corresponding definition
      if (m_Tree != null)
      {
   		IPropertyDefinition pDef = pEle.getPropertyDefinition();
   		if (pDef != null)
   		{
   			String value = (String)m_Tree.getValueAt(row, 2);
   			
   		 	//
   		 	// if the user has chosen something from a list box or a combo box, get its index
   		 	// because most things in the listbox are represented by an enumeration, we need to
   		 	// determine what that enumeration is, so that we can set the property element value
   		 	// to the proper thing
   		 	//
   			String values = pDef.getValidValues();
   			if (values != null && values.length() > 0)
   			{
   				// there could be an enumeration, so further check
   				int pos = values.indexOf("//"); //$NON-NLS-1$
   				if (pos >= 0)
   				{
   					// xpath string
   					pEle.setValue(value);
   				}
   				else
   				{
   					pos = values.indexOf("#DataTypeList"); //$NON-NLS-1$
   					if (pos >= 0)
   					{
   						// we now have to be smart and if the user has set this preference
   						// to use fully qualified names in the picklist, we actually want
   						// to be setting the type to the qualified name, not just int, pack1::pack1a::int
   						// so this needs to get the information from the listbox
   						boolean useQName = getDataTypePreference();
   						if (useQName)
   						{
   							// use fully qualified name, so get the index of the selected item in the
   							// combo box
   							
   							//This is complicated, need to understand and implement
   						}
   					}
   					else
   					{
   						// enumeration, so store the index in the property element
   						IEnumTranslator enumTranslator = new EnumTranslator();
   						String enumValues = pDef.getFromAttrMap("enumValues"); //$NON-NLS-1$
   						int num = enumTranslator.translateToEnum(value, values, enumValues);
   						if (num != -999)
   						{
   							pEle.setValue(Integer.toString(num));
   						}
   						else
   						{
   							pEle.setValue(value);
   						}
   					}
   				}
   			}
   			else
   			{
   				pEle.setValue(value);
   			}
   		}
   		pEle.setModified(true);
      }
	}

	private void updatePropertyElementValue(int row, IPropertyElement pEle,
											String newVal)
	{
		if (newVal == null)
		{
			updatePropertyElementValue(row, pEle);
		}
		// get the corresponding definition
		IPropertyDefinition pDef = pEle.getPropertyDefinition();
		if (pDef != null)
		{
			String value = newVal;
			
			//
			// if the user has chosen something from a list box or a combo box, get its index
			// because most things in the listbox are represented by an enumeration, we need to
			// determine what that enumeration is, so that we can set the property element value
			// to the proper thing
			//
			String values = pDef.getValidValues();
			if (values != null && values.length() > 0)
			{
				// there could be an enumeration, so further check
				int pos = values.indexOf("//"); //$NON-NLS-1$
				if (pos >= 0)
				{
					// xpath string
					pEle.setValue(value);
				}
				else
				{
					pos = values.indexOf("#DataTypeList"); //$NON-NLS-1$
					if (pos >= 0)
					{
						// we now have to be smart and if the user has set this preference
						// to use fully qualified names in the picklist, we actually want
						// to be setting the type to the qualified name, not just int, pack1::pack1a::int
						// so this needs to get the information from the listbox
						boolean useQName = getDataTypePreference();
						if (useQName)
						{
							// use fully qualified name, so get the index of the selected item in the
							// combo box
							
							//This is complicated, need to understand and implement
						}
					}
					else
					{
						// enumeration, so store the index in the property element
						IEnumTranslator enumTranslator = new EnumTranslator();
						String enumValues = pDef.getFromAttrMap("enumValues"); //$NON-NLS-1$
						int num = enumTranslator.translateToEnum(value, values, enumValues);
						if (num != -999)
						{
							pEle.setValue(Integer.toString(num));
						}
						else
						{
							pEle.setValue(value);
						}
					}
				}
			}
			else
			{
				pEle.setValue(value);
			}
		}
		pEle.setModified(true);
	}

	/**
	 * Retrieves the datatype preference from the preference manager
	 * 
	 *
	 * @param pref[out]		Whether or not to display the fully qualified name in the datatype picklist
	 *
	 * @return HRESULT
	 *
	 */
	private boolean getDataTypePreference()
	{
		//kris richards - "DisplayTypeFSN" pref removed. Set to true
            return true;
	}

	/**
	 * Special processing to reorder parameters
	 * @return HRESULT
	 *
	 */
	private void reorderFeatures()
	{
		// only doing this for parameters at this time
		// we are not set up to do it generically for "features"
		ETList<IParameter> pFeatures = new ETArrayList<IParameter>();

		// get the grid node that is at the last row we just processed
		JDefaultMutableTreeNode pNode = getNodeAtGridRow(m_LastRow);
		if (pNode != null)
		{
			// get the collection grid node for the last row we just processed
			JDefaultMutableTreeNode colNode = getCollectionGridNode(pNode, 0);
			if (colNode != null)
			{
				// get the property element at the row of the collection node
				IPropertyElement colEle = (IPropertyElement)colNode.getUserObject();
				if (colEle != null)
				{
					// loop through the grid nodes under the collection
					DefaultMutableTreeNode child = (DefaultMutableTreeNode)colNode.getFirstChild();
					while (child != null)
					{
						IPropertyElement pEle = (IPropertyElement)child.getUserObject();
						if (pEle != null)
						{
							// if the property element contains a parameter, add it to
							// our temporary list
							Object pDisp = pEle.getElement();
							if (pDisp instanceof IParameter)
							{
								IParameter pFeat = (IParameter)pDisp;
								String temp = pFeat.getName();
								pFeatures.add(pFeat);
							}
							child = child.getNextSibling();
						}
						else
						{
							child = null;
						}
					}
					//
					// now we should have a new array of parameters in the correct order
					// according to the user
					//
					Object pDisp2 = colEle.getElement();
					if (pDisp2 instanceof IOperation)
					{
						IOperation pOp = (IOperation)pDisp2;
						// so set it on the operation
						pOp.setFormalParameters(pFeatures);
					}
				}
			}
		}
	}

	/**
	 * Method to navigate up the grid node chain to retrieve the grid node that represents
	 * the record that the passed in grid node is in.
	 *
	 * @param[in] pNode            The grid node in which to get the record grid node that it belongs to 
	 * @param[out] pCollectionNode The grid node that is the record node
	 *
	 * @return HRESULT
	 *
	 */
	private JDefaultMutableTreeNode getGridNodeMarkedAsRecord(JDefaultMutableTreeNode node)
	{
		JDefaultMutableTreeNode retNode = null;
		if (node != null)
		{
			String key = node.getKey();
			if (key != null && key.equals("RecordNew")) //$NON-NLS-1$
			{
				retNode = node;
			}
			else
			{
				JDefaultMutableTreeNode parent = (JDefaultMutableTreeNode)node.getParent();
				if (parent != null)
				{
					retNode = getGridNodeMarkedAsRecord(parent);
				}
			}
		}
		
		return retNode;
	}

	/**
	 * Calls the insert method of the property element
	 * 
	 *
	 * @param row[in]		The grid row that we are dealing with
	 *
	 * @return HRESULT
	 */
	private void insert(JDefaultMutableTreeNode lastRecNode)
	{
		if (m_Model != null)
		{
			int lastRow = m_LastRow;//m_Tree.getRowCount();
			IPropertyElement lastPropEle = getElementAtGridRow(lastRow);
			JDefaultMutableTreeNode lastNode = getNodeAtGridRow(lastRow);
			if (lastPropEle != null && lastRecNode != null)
			{
				IPropertyElement pEle = (IPropertyElement)lastRecNode.getUserObject();
				if (pEle != null)
				{
					IPropertyElement colEle = getCollectionElement(pEle);
					if (colEle != null)
					{
						IPropertyDefinition pDef = pEle.getPropertyDefinition();
						Object pDisp2 = colEle.getElement();
						if (pDisp2 != null)
						{
							// continue with the add
							PropertyEditorBlocker.block();
							try
							{
								m_PropEleMgr.insertData(pDisp2, pDef, pEle);
							
								// we are having problems in RT when dealing with the sets of an operation
								// the change events cannot handle single changes to the signature
								// a context was created to handle this and was being used by everything
								// but the property editor, so now we are special casing an operation
								// and have a context set up, so now before we "Add" the operation
								// we need to pop the context
								Object pDisp3 = pEle.getElement();
								if (lastNode != null)
								{
									updateNodeAndParentNodes(lastNode,lastPropEle);
									JDefaultMutableTreeNode pLastRecNode = getGridNodeMarkedAsRecord(lastNode);
									if (pLastRecNode != null)
									{
										pLastRecNode.setKey("Record"); //$NON-NLS-1$
										pLastRecNode.setExpanded(false);
									}
								}
								pEle.setModified(false);
							}
							finally
							{
								PropertyEditorBlocker.unblock();
							}
						}
						else
						{
							Object pDisp = getModelElement(colEle);
							PropertyEditorBlocker.block();
							try
							{
								m_PropEleMgr.insertData(pDisp, pDef, pEle);
							
								if (lastNode != null)
								{
									updateNodeAndParentNodes(lastNode,lastPropEle);
									//JDefaultMutableTreeNode pLastRecNode = getGridNodeMarkedAsRecord(lastNode);
									if (lastRecNode != null)
									{
										lastRecNode.setKey("Record"); //$NON-NLS-1$
										lastRecNode.setExpanded(false);
									}
								}
								pEle.setModified(false);
							}
							finally
							{
								PropertyEditorBlocker.unblock();
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Determines whether or not we need to call the insert function for a particular element
	 * 
	 *
	 * @param pCurrNode[in]			The current grid node
	 * @param pLastRecNode[in]		The last grid node that we were in
	 *
	 * @return bool
	 */
	private boolean insertNecessary(JDefaultMutableTreeNode node, JDefaultMutableTreeNode lastRecNode)
	{
		boolean retVal = false;
		if (node != null && lastRecNode != null)
		{
			JDefaultMutableTreeNode curRecNode = getGridNodeMarkedAsRecord(node);
			if (curRecNode == null)
			{
				retVal = true;
			}
			else
			{
				int curLevel = curRecNode.getLevel();
				int lastLevel = lastRecNode.getLevel();
				if (curLevel > lastLevel)
				{
				}
				else if (curLevel == lastLevel)
				{
					int curRow = m_CurRow;//curRecNode.getRow();
					int lastRow = m_LastRow;//lastRecNode.getRow();
					if (curRow != lastRow)
					{
						//in our case since we do not know where the user clicked next, we cannot say
						//that we need to insert. I will decide that when I know where the user clicked
						//next in TreeTableCellEditor
						//retVal = true;
					}
				}
				else
				{
					retVal = true;
				}
			}
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditor#resetGridSettings()
	 */
	public long resetGridSettings()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditor#resetGridFilter()
	 */
	public long resetGridFilter()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditor#getWindowHandle()
	 */
	public int getWindowHandle()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Ask the user what to do about a name collision
	 *
	 * @param pElement [in] The element being renamed
	 * @param sProposedName [in] The new name
	 * @param pFirstCollidingElement [in] The first colliding element
	 * @param pCell [in] The result cell.  Used to cancel the rename.
	 */
	public long questionUserAboutNameCollision(INamedElement pElement, String sProposedName, INamedElement pFirstCollidingElement, IResultCell pCell)
	{
		if (pFirstCollidingElement != null && pElement != null && pCell != null)
		{
			// Ask the user if he wants to reconnect the presentation element to a different model element
			IQuestionDialog pDiag = new SwingQuestionDialogImpl();
			if ( pDiag != null )
			{
				String title = PropertyEditorResources.getString("PropertyEditor.NameCollisionTitle");
				String msg = PropertyEditorResources.getString("PropertyEditor.NameCollision");
				QuestionResponse result = pDiag.displaySimpleQuestionDialog(SimpleQuestionDialogKind.SQDK_YESNO, MessageIconKindEnum.EDIK_ICONWARNING, msg, 0, null, title);
				if (result.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_YES)
				{
					// User wants to allow the name collision.
				}
				else if (result.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_NO)
				{
					// Cancel the editing to abort the name collision
					pCell.setContinue(false);
				}
			}
		}
		return 0;
	}

	/**
	 * Tells the listener we've got as a member that it was us that began editing
	 */
	public long beginEditContext()
	{
		if (m_NameCollisionListener != null)
		{
			m_NameCollisionListener.setEnabled(true);
		}
		return 0;
	}
	/**
	 * Tells the listener we've got as a member that we're done editing
	 */
	public void endEditContext()
	{
		if (m_NameCollisionListener != null)
		{
			m_NameCollisionListener.setEnabled(false);
		}
	}
	/**
	 * Whether or not the property editor should respond to an event and reload its information.
	 * We were running into performance issues from within some of the processes (ie. ERStudioIntegration
	 * and DesignPattern Apply) where the property editor is receiving events, but we really don't want it
	 * to try and figure out if it should reload, so this flag will be checked and short circuit the check.
	 *
	 * @param[out] pVal		The flag for whether or not a diagram should be created
	 *
	 * @return HRESULT
	 */
	public boolean getRespondToReload()
	{
		return m_RespondToReload;
	}

	public void setRespondToReload(boolean value)
	{
		m_RespondToReload = value;
	}

	/**
	 * Figure out how we should present the property element to the user as far as whether the grid
	 * node is read-only or not
	 *
	 * @param[in] pDef      The property definition associated with the property element
	 * @param[in] pEle      The property element to be used
	 *
	 * @return BOOL         If it is read only or not
	 *
	 */
	private boolean isReadOnlyStatus(IPropertyDefinition pDef, IPropertyElement pEle)
	{
		boolean isReadOnly = false;
		String controlType = pDef.getControlType();
		long mult = pDef.getMultiplicity();
		if (mult > 1)
		{
			// collections are read only
			if (controlType != null && controlType.equals("read-only")) //$NON-NLS-1$
			{
				isReadOnly = true;
			}
		}
		else
		{
			// property definitions that have not told us what their type is or that they are read only
			// should be marked as read only
			if (controlType == null || controlType.length() == 0 || controlType.equals("read-only")) //$NON-NLS-1$
			{
				isReadOnly = true;
			}
			else
			{
				boolean bEdit = isElementEditable(pEle, pDef);
				if (!bEdit)
				{
					isReadOnly = true;
				}
			}
		}
		return isReadOnly;
	}

	/**
	 * Figure out how we should present the property element to the user as far as whether the grid
	 * node is bold or not
	 *
	 * @param[in] pDef      The property definition associated with the property element
	 * @param[in] pEle      The property element to be used
	 *
	 * @return BOOL         If it is bold or not
	 *
	 */
	private boolean isBold(IPropertyDefinition pDef, IPropertyElement pEle)
	{
		boolean bold = false;
		long mult = pDef.getMultiplicity();
		if (mult > 1)
		{
			// collections are bold
			bold = true;
		}
		else
		{
			String values = pDef.getValidValues();
			if (values != null && values.length() > 0)
			{
				int pos = values.indexOf("FormatString"); //$NON-NLS-1$
				if (pos >= 0)
				{
					bold = true;
				}
			}
		}
		return bold;
	}

	/**
	 * Determines whether or not this property element should be editable.  There are some cases
	 * that we know going in that it is not editable (ie. versioned file name).  There are other
	 * cases that we want it to not be editable if it is a certain value.  In these cases, we
	 * need to mark the definition, so that we know who to ask.
	 * 
	 *
	 * @param pEle[in]		The property element in question
	 * @param pDef[in]		The property definition of the property element in question
	 * @param bEdit[out]		Whether or not this property element is editable		
	 *
	 * @return HRESULT
	 *
	 */
	private boolean isElementEditable(IPropertyElement pEle, IPropertyDefinition pDef)
	{
		boolean bEdit = true;
		String value = pEle.getValue();
		if (value != null && value.length() > 0)
		{
			String cType = pDef.getControlType();
			if (cType != null && cType.equals("read-only")) //$NON-NLS-1$
			{
				String pdName = pDef.getName();
				String parentName = ""; //$NON-NLS-1$
				IPropertyDefinition parentDef = pDef.getParent();
				if (parentDef != null)
				{
					parentName = parentDef.getName();
				}
				if ( pdName.equals("ReferredElement") || pdName.equals("ReferencingElement") ) //$NON-NLS-1$ //$NON-NLS-2$
				{
				}
				else if ( parentName.equals("AssociatedDiagrams") || parentName.equals("AssociatedElements") ) //$NON-NLS-1$ //$NON-NLS-2$
				{
				}
				else
				{
					bEdit = false;
				}
			}
			else
			{
				// get the validate information from the definition
				String validM = pDef.getFromAttrMap("validate"); //$NON-NLS-1$
				if (validM != null && validM.length() > 0)
				{
					// if the string in the validate is not a progID (Foundation.Project)
					// then check to see if it is a GUID ({123-456})
					// there are two ways to invoke the validate, if it is a progID
					// then we cocreate it, cast it to a ICustomValidator, and call Validate
					// if it is a GUID, we cast the IDispatch that we have on the property element
					// to a ICustomValidator, and call Validate
					try {
						Class progIDClass = Class.forName(validM);
						Object progIDObj = progIDClass.newInstance();
						if (progIDObj instanceof ICustomValidator)
						{
							ICustomValidator pValidator = (ICustomValidator)progIDObj;
							String name = pDef.getName();
							bEdit = pValidator.validate(pEle, name, value);
						}
					} catch (ClassNotFoundException cExp)
					{
						Object obj = pEle.getElement();
						if (obj != null)
						{
							if (obj instanceof ICustomValidator)
							{
								ICustomValidator pValidator = (ICustomValidator)obj;
								String name = pDef.getName();
								bEdit = pValidator.validate(pEle, name, value);
							}
						}
					}
					catch (Exception e)
					{}
				}
			}
		}
		return bEdit;
	}
	
	/**
	 * Display the proper menu in the grid based on where the user has clicked
	 *
	 * @param[in] pDef   The property definition used to help determine what menu to display
	 * @param[in] pEle   The property element used to help determine what menu to display
	 *
	 * @return HRESULT
	 *
	 */
	public String[] showMenuBasedOnDefinition(IPropertyDefinition pDef, IPropertyElement pEle)
	{
		String[] retMenuItems = null;
		int menuNum = 0;
		//To Do
		if (pDef != null)
		{
			long mult = pDef.getMultiplicity();
			String type = pDef.getControlType();
			String pdName = pDef.getName();
			String parentName = ""; //$NON-NLS-1$
			IPropertyDefinition parentDef = pDef.getParent();
			if (parentDef != null)
			{
				parentName = parentDef.getName();
			}
			if ( pdName.equals("ReferredElement") ||  //$NON-NLS-1$
				  pdName.equals("ReferencingElement")) //$NON-NLS-1$
			{
				// totally special processing for referred/referencing elements
				// couldn't get it to work the generic way because we only wanted to show the opposite
				// element of the reference, and we wanted its value to be placed on the top level (read-only)
				// and values string in propDef, but we wanted to only have a right-click delete, no create
				if (parentDef != null)
				{
					String delMeth = parentDef.getDeleteMethod();
					if (delMeth != null && delMeth.length() > 0)
					{
						menuNum = 7;
					}
				}
			}
			else if (pdName.equals("SourceFileArtifact"))  //$NON-NLS-1$
			{
				menuNum = 3;
			}
			else if ( parentName.equals("AssociatedDiagrams") ||  //$NON-NLS-1$
					  parentName.equals("AssociatedElements") ) //$NON-NLS-1$
			{
				menuNum = 7;
			}
			else
			{
				// if it isn't marked as read only
				if (type == null || !type.equals("read-only")) //$NON-NLS-1$
				{
					// if the node that we are on is a collection, may want to display the create menu
					if (mult > 1)
					{
						String putM = pDef.getSetMethod();
						Vector<IPropertyElement> subEles = pEle.getSubElements();
						long count = subEles.size();
						if ( (putM == null) || (putM.length() == 0) || ( (putM.length() > 0) && (count == 0) ) )
						{
							menuNum = 1;
						}
					}
					else
					{
						// if it isn't a multiple definition(cannot have records), the user may have clicked in one of the
						// subs, which we still want them to be able to create from within there
						IPropertyElement collectionEle = getCollectionElement(pEle);
						if (collectionEle != null)
						{
							// show the create/delete menu
							// only show delete if it is editable
							boolean bEdit = isElementEditable(pEle, pDef);
							if (bEdit)
							{
								// now if we are on a attribute/operation/parameter
								// then show the insert menu button as well
								String insertBefore = pDef.getFromAttrMap("insertBefore"); //$NON-NLS-1$
								if (insertBefore != null && insertBefore.length() > 0)
								{
									menuNum = 6;
								}
								else
								{
									menuNum = 2;
								}
							}
							else
							{
								menuNum = 5;
							}
						}
						else
						{
							// not a collection, and not a member of a collection
							// so may want to show the fill down menu (if in that mode)
							boolean show = showFilldownMenuItem();
							if (show)
							{
								menuNum = 0;
							}
						}
					}
				}
			}
		}
		IProxyUserInterface pUI = ProductHelper.getProxyUserInterface();
		
		//ETSystem.out.println("Going to show menu for " + menuNum);
		// have figured out what menu, so display it
		if (menuNum > -1)
		{
			if (pUI != null)
				pUI.setDisableContextMenu(true);

			if (m_MenuItems != null && m_MenuItems.length > menuNum)
			{
				String[] menuItems = m_MenuItems[menuNum];
				if (menuItems != null)
				{
					retMenuItems = menuItems;
				}
			}
			
			if (pUI != null)
				pUI.setDisableContextMenu(false);
		}
		else
		{
			if (pUI != null)
				pUI.setDisableContextMenu(true);
		}
		return retMenuItems;
	}
	
	/**
	 * Method to navigate up the property element chain to retrieve the element that represents
	 * the collection that the passed in property element is in.
	 *
	 * @param[in] pEle          The property element in which to get the collection element that it belongs to 
	 * @param[out] pInsertEle   The property element that is the collection element
	 *
	 * @return HRESULT
	 *
	 */
	private IPropertyElement getCollectionElement(IPropertyElement pEle)
	{
		IPropertyElement retEle = null;
		IPropertyDefinition pDef = pEle.getPropertyDefinition();
		if (pDef != null)
		{
			// if the element has a property definition that is multiple, then we have found it
			long mult = pDef.getMultiplicity();
			String controlType = pDef.getControlType();
			if (mult > 1)
			{
				if (controlType == null || !controlType.equals("read-only")) //$NON-NLS-1$
				{
					retEle = pEle;
				}
			}
			else
			{
				IPropertyElement parentEle = pEle.getParent();
				if (parentEle != null)
				{
					retEle = getCollectionElement(parentEle);
				}
			}
		}
		return retEle;
	}
	
	/**
	 * Determines whether or not the Fill menu should be displayed
	 *
	 */
	private boolean showFilldownMenuItem()
	{
		boolean show = false;
		if (m_Mode == EDITOR_SEL)
		{
			show = true;
		}
		return show;
	}

	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();
		if (source instanceof JComboBox)
		{
			JComboBox box = (JComboBox)source;
			onSelChangeFilterCombo();
			Object selItem = box.getSelectedItem();
		}
		
	}

	/**
	 * Called when the user changes the selection in the listbox
	 *
	 * @param[in] wNotifyCode
	 * @param[in] wID
	 * @param[in] hWndCtrl
	 * @param[out] bHandled
	 *
	 * @return LRESULT
	 */
	public void onSelChangeFilterCombo()
	{
		if (m_FilterCombo != null && m_Tree != null)
		{
			// set up the mode that the property editor is in
			String str = m_FilterCombo.getSelectedItem().toString();
			setModeBasedOnCombo();
			if (str.equals("...")) //$NON-NLS-1$
			{
				m_Filter.showFilterDialog();
				String str1 = m_Filter.getCurrentSelection();
				if (str1 != null && !str1.equals("")) //$NON-NLS-1$
				{
					//need to make sure that duplicate items are not added
					boolean bFound = false;
					int size = m_FilterCombo.getItemCount();
					for (int j = 0; j < size; j++)
					{
						String tempStr = m_FilterCombo.getItemAt(j).toString();
						if (str1.equals(tempStr))
						{
							bFound = true;
							break;
						}
					}
					if (!bFound)
					{
						m_FilterCombo.addItem(str1);
					}
					m_FilterCombo.setSelectedItem(str1);
				}
			}
			else
			{
				m_Filter.setCurrentSelection(str);
			}
			setModeBasedOnCombo();
			Object obj = m_Tree.getTree().getModel().getRoot();
			if (obj != null && obj instanceof JDefaultMutableTreeNode)
			{
				//We want to get all the child nodes of this root node and 
				//refresh them.
				JDefaultMutableTreeNode rootNode = (JDefaultMutableTreeNode)obj;
				int count = rootNode.getChildCount();
				for (int i=0; i<count; i++)
				{
					JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)rootNode.getChildAt(i);
					Object obj1 = node.getUserObject();
					if (obj1 != null && obj1 instanceof IPropertyElement)
					{
						IPropertyElement ele = (IPropertyElement)obj1;
						refreshRootNode(ele, node);
					}
				}
			}
		}
	}

	private void setModeBasedOnCombo()
	{
		if (m_FilterCombo != null)
		{
			// set up the mode that the property editor is in
			String str = m_FilterCombo.getSelectedItem().toString();
			ConfigStringTranslator trans = new ConfigStringTranslator();
			String all = trans.translate(null, "PSK_ALL"); //$NON-NLS-1$
			String pres = trans.translate(null, "PSK_PRESENTATION"); //$NON-NLS-1$
			String data = trans.translate(null, "PSK_DATA"); //$NON-NLS-1$
			if (str.equals(all))
			{
				m_Mode = EDITOR_ALL;
			}
			else if (str.equals(pres))
			{
				m_Mode = EDITOR_PRES;
			}
			else if (str.equals(data))
			{
				m_Mode = EDITOR_DATA;
			}
			else
			{
				m_Mode = EDITOR_SEL;
			}
		}
	}

	public Vector<Icon> loadImages(JDefaultMutableTreeNode root)
	{
		Vector<Icon> retImages = new Vector();
		m_LoadedImages.removeAllElements();
		if (root != null)
		{
			Object obj = root.getUserObject();
			if (obj != null && obj instanceof IPropertyElement)
			{
				IPropertyElement rootEle = (IPropertyElement)obj;
				IPropertyDefinition rootDef = rootEle.getPropertyDefinition();
				Icon icon0 = getImage(rootEle);
				retImages.add(icon0);
				m_LoadedImages.add(rootEle);
				Vector<IPropertyElement> elems = rootEle.getSubElements();
				if (elems != null)
				{
					int count = elems.size();
					for (int i=0; i<count; i++)
					{
						IPropertyElement pEle = elems.elementAt(i);
						IPropertyDefinition pDef = pEle.getPropertyDefinition();
						String name = pDef.getName();
		
						// determine if the element is a collection from its definition
						long mult = pDef.getMultiplicity();
						if (mult > 1)
						{
							String cType = pDef.getControlType();
							Vector <IPropertyElement> subEles = pEle.getSubElements();
							int count2 = subEles.size();
							if ( (cType != null && cType.equals("read-only") && (count2 == 0)) ||  //$NON-NLS-1$
								 name.equals("ReturnType") ) //$NON-NLS-1$
							{
							}
							else if ( (m_Mode == EDITOR_DATA) && (name.equals("Presentation")) ) //$NON-NLS-1$
							{
							}
							else if ( name.equals("FontProperties") ||  //$NON-NLS-1$
									  name.equals("ColorProperties") || //$NON-NLS-1$
									  name.equals("FontProperty") ||  //$NON-NLS-1$
									  name.equals("ColorProperty") ||  //$NON-NLS-1$
									  name.equals("PropertyContainer") ||  //$NON-NLS-1$
									  name.equals("ChildProperties")  //$NON-NLS-1$
									 )
							{
							}
							else
							{
								Icon icon = getImage(pEle);
								if (icon != null)
								{
									retImages.add(icon);
									m_LoadedImages.add(pEle);
								}
							}
						}
					}
				}
			}
		}
		return retImages;
	}

	/**
	 * Set the icon of the grid cell
	 * 
	 *
	 * @param pDef[in]		The current property definition
	 * @param pEle[in]		The current property element
	 * @param row[in]			The current row
	 *
	 * @return HRESULT
	 *
	 */
	public Icon getImage(IPropertyElement pEle)
	{
		Icon icon = null;
		if (pEle != null)
		{
			IPropertyDefinition pDef = pEle.getPropertyDefinition();
			if (pDef != null)
			{
				String name = pDef.getName();
				if (m_ResourceMgr != null)
				{
					if (name != null && name.equals("ProxyDiagram")) //$NON-NLS-1$
					{
						Object pDisp = pEle.getElement();
						if (pDisp != null && pDisp instanceof IProxyDiagram)
						{
							IProxyDiagram pDiag = (IProxyDiagram)pDisp;
							String kind = pDiag.getDiagramKindName();
							if (kind != null)
							{
								name = kind.trim();
							}
						}
					}
					else if (name.equals("Diagram")) //$NON-NLS-1$
					{
						Object pDisp = pEle.getElement();
						if (pDisp != null && pDisp instanceof IDiagram)
						{
							IDiagram pDiag = (IDiagram)pDisp;
							String kind = pDiag.getDiagramKindAsString();
							if (kind != null)
							{
								name = kind.trim();
							}
						}
					}
					else if (name.equals("State") //$NON-NLS-1$
					|| name.equals("PseudoState") //$NON-NLS-1$
					|| name.equals("FinalState")) //$NON-NLS-1$
					{
						String temp = calculateTopElementName(pEle);
						if (temp != null)
						{
							name = temp.trim();
						}
					}
					icon = m_ResourceMgr.getIconForElementType(name);
				}
			}
		}
		return icon;
	}

	/**
	 * Determines the name to display in the property editor based on the IElement
	 * type.  Couldn't just use the element type because some of the elements were
	 * the same element type, but had qualities that were different, so we wanted a
	 * different name (psuedostate, for example)
	 * 
	 *
	 * @param pEle[in]		The current property element
	 * @param sName[out]		The display name of the property element
	 *
	 * @return HRESULT
	 *
	 */
	public String calculateTopElementName(IPropertyElement pEle)
	{
		String topName = ""; //$NON-NLS-1$
		IPropertyDefinition pDef = pEle.getPropertyDefinition();
		if (pDef != null)
		{
			Object pDisp = pEle.getElement();
			if (pDisp != null)
			{
				IElement pElement = null;
				if (pDisp instanceof IPresentationElement)
				{
					IPresentationElement pPres = (IPresentationElement)pDisp;
					pElement = pPres.getFirstSubject();
				}
				else if (pDisp instanceof IElement)
				{
					pElement = (IElement)pDisp;
				}
				
				if (pElement != null)
				{
					IConfigStringTranslator trans = ConfigStringHelper.instance().getTranslator();
					if (trans != null)
					{
						String expandedEleType = pElement.getExpandedElementType();

						if (expandedEleType.equals("AbortedFinalState") ) //$NON-NLS-1$
						{
							topName = trans.translate(null, "PSK_ABORTEDFINALSTATE"); //$NON-NLS-1$
						}
						else if (expandedEleType.equals("FinalState") ) //$NON-NLS-1$
						{
							topName = trans.translate(null, "PSK_FINALSTATE"); //$NON-NLS-1$
						} 
						else if (expandedEleType.equals("ChoicePseudoState") ) //$NON-NLS-1$
						{
							topName = trans.translate(null, "PSK_CHOICEPSUEDOSTATE"); //$NON-NLS-1$
						} 
						else if (expandedEleType.equals("DeepHistoryState") ) //$NON-NLS-1$
						{
							topName = trans.translate(null, "PSK_DEEPHISTORY"); //$NON-NLS-1$
						} 
						else if (expandedEleType.equals("ForkState") ) //$NON-NLS-1$
						{
							topName = trans.translate(null, "PSKPSUEDOSTATE"); //$NON-NLS-1$
						} 
						else if (expandedEleType.equals("InitialState") ) //$NON-NLS-1$
						{
							topName = trans.translate(null, "PSK_INITIALSTATE"); //$NON-NLS-1$
						} 
						else if (expandedEleType.equals(PropertyEditorResources.getString("PropertyEditor.JoinState_91")) ) //$NON-NLS-1$
						{
							topName = trans.translate(null, "PSK_JOINSTATE"); //$NON-NLS-1$
						} 
						else if (expandedEleType.equals("JunctionState") ) //$NON-NLS-1$
						{
							topName = trans.translate(null, "PSK_JUNCTION"); //$NON-NLS-1$
						} 
						else if (expandedEleType.equals("ShallowHistoryState") ) //$NON-NLS-1$
						{
							topName = trans.translate(null, "PSK_SHALLOWHISTORY"); //$NON-NLS-1$
						} 
						else if (expandedEleType.equals("EntryPointState") ) //$NON-NLS-1$
						{
							topName = trans.translate(null, "PSK_ENTRYPOINT"); //$NON-NLS-1$
						} 
						else if (expandedEleType.equals("StopState") ) //$NON-NLS-1$
						{
							topName = trans.translate(null, "PSK_STOPSTATE"); //$NON-NLS-1$
						} 
						else if (expandedEleType.equals("CompositeState") ) //$NON-NLS-1$
						{
							topName = trans.translate(null, "PSK_COMPOSITESTATE"); //$NON-NLS-1$
						} 
						else if (expandedEleType.equals("SubmachineState") ) //$NON-NLS-1$
						{
							topName = trans.translate(null, "PSK_SUBMACHINESTATE"); //$NON-NLS-1$
						} 
						else if (expandedEleType.equals("DesignPattern") ) //$NON-NLS-1$
						{
							topName = trans.translate(null, "PSK_DESIGNPATTERN"); //$NON-NLS-1$
						} 
						else 
						{
							topName = pDef.getDisplayName();
						} 
					}
				}
				else
				{
					topName = pDef.getDisplayName();
				}
			}
		}
		return topName;
	}

	/**
	 * Called when a compartment on the drawing area is selected
	 *
	 * @param[in] selectedItems   The compartments that are selected in the drawing area
	 *
	 * @return HRESULT
	 *
	 */
        // TODO: meteora
//	public void onCompartmentSelect(final ICompartment selectedItem)
//	{
//		//we want the focusLost to do the work, but we need to set the lastCell
//		//to be the m_CurRow
//		m_LastRow = m_CurRow;
//		
//		try {
//			if (PropertyEditorBlocker.inProcess() == false)
//			{
//				//When the last cell was being edited and user clicked on some other component which
//				//makes property editor to refresh, we need to process last row.
//				handleSave(false, null);
//				clear();
//				
//				//
//				// the property editor is loaded by creating property definitions(which
//				// represent the structure of the data) and property elements(which represent
//				// the actual data)
//				//
//				Vector<IPropertyDefinition> propDefs = new Vector<IPropertyDefinition>();
//				Vector<IPropertyElement> propEles = new Vector<IPropertyElement>();
//				
//				if (selectedItem != null)
//				{
//					// what we have selected from the drawing area is a compartment
//					// so ask it for its model element
//					IElement pModelElement = selectedItem.getModelElement();
//					if (pModelElement != null)
//					{
//						// more processing to create the property element
//						IPropertyElement pEle = processSelectedItem("", propDefs, pModelElement); //$NON-NLS-1$
//						if (pEle != null)
//						{
//							// since we are coming from the drawing area, we will also want the
//							// capability of showing the presentation information, so store the
//							// presentation element on the property element
//							//_VH(pEle->put_Element(pPresentationElement));
//							// we also want to present the presentation section
//							processSelectedPresentationItem(pEle, selectedItem);
//							propEles.add(pEle);
//						}
//					}
//				}
//				setPropertyDefinitions(propDefs);
//				setPropertyElements(propEles);
//			}
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//		}
//	}

	/**
	 * Called when something is selected on the drawing area
	 *
	 * @param[in] selectedItems   The items that are currently selected in the drawing area
	 *
	 * @return HRESULT
	 *
	 */
	public void onDrawingAreaSelect(final IDiagram pParentDiagram, final ETList<IPresentationElement> selectedItems)
	{
		//we want the focusLost to do the work, but we need to set the lastCell
		//to be the m_CurRow
		m_LastRow = m_CurRow;
		
		try {
			int count = 0;
			if (selectedItems != null)
			{
				count = selectedItems.size();
			}
			
			if (count == 0)
			{
				if (PropertyEditorBlocker.inProcess() == false)
				{
					//When the last cell was being edited and user clicked on some other component which
					//makes property editor to refresh, we need to process last row.
					handleSave(false, null);
					clear();
	
					// if there is nothing selected and a diagram is open, we want to show the information
					// about the diagram

					onDrawingAreaSelectSpecial(pParentDiagram);
					
				}
			}
			else
			{
				// The blocker was put in because we were getting multiple "selects" when a
				// user was selecting a compartment.  The compartment would issue a select, which would
				// then get processed by the property editor, but since the drawing area was also selected
				// it would also fire, nullifying what we did in the compartment select
				if (PropertyEditorBlocker.inProcess() == false)
				{
					//When the last cell was being edited and user clicked on some other component which
					//makes property editor to refresh, we need to process last row.
					handleSave(false, null);
					clear();
	
					//
					// the property editor is loaded by creating property definitions(which
					// represent the structure of the data) and property elements(which represent
					// the actual data)
					//
					Vector<IPropertyDefinition> propDefs = new Vector<IPropertyDefinition>();
					Vector<IPropertyElement> propEles = new Vector<IPropertyElement>();
					
					// get how many should be displayed from preferences
					IPreferenceAccessor pref = PreferenceAccessor.instance();
					int max = pref.getDefaultEditorSelect();
	
					if (count < max)
					{
					   max = count;
					}
	
					// loop through the items selected and build the corresponding definitions and elements
					for (int i=0; i<max; i++)
					{
						// what we have selected from the drawing area is a presentation element
						IPresentationElement presEle = selectedItems.get(i);
						if (presEle != null)
						{
							// we need the IElement from the presentation element
							IElement modelEle = presEle.getFirstSubject();
							if (modelEle != null)
							{
								// more processing to create the property element
								IPropertyElement pEle = processSelectedItem("", propDefs, modelEle); //$NON-NLS-1$
								if (pEle != null)
								{
									// since we are coming from the drawing area, we will also want the
									// capability of showing the presentation information, so store the
									// presentation element on the property element
									pEle.setElement(presEle);
								
									// we also want to present the presentation section
									processSelectedPresentationItem(pEle, presEle);
									propEles.add(pEle);
								}
							}
						}
					}
					setPropertyDefinitions(propDefs);
					setPropertyElements(propEles);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
	}

	/**
	 * Called when something is selected on the project tree
	 *
	 * @param[in] selectedItems   The items that are selected in the project tree
	 *
	 * @return HRESULT
	 *
	 */
	public void onTreeSelect(final IProjectTreeItem[] selectedItems)
	{
		//we want the focusLost to do the work, but we need to set the lastCell
		//to be the m_CurRow
		m_LastRow = m_CurRow;
		
		try {
			if (PropertyEditorBlocker.inProcess() == false)
			{
				//When the last cell was being edited and user clicked on some other component which
				//makes property editor to refresh, we need to process last row.
				handleSave(false, null);
				clear();
		
				//
				// the property editor is loaded by creating property definitions(which
				// represent the structure of the data) and property elements(which represent
				// the actual data)
				//
				Vector<IPropertyDefinition> propDefs = new Vector<IPropertyDefinition>();
				Vector<IPropertyElement> propEles = new Vector<IPropertyElement>();
		
				int count = selectedItems.length;
				IPreferenceAccessor pref = PreferenceAccessor.instance();
				int max = pref.getDefaultEditorSelect();
				if (count < max)
				{
					max = count;
				}
		
				// loop through the items selected and build the corresponding definitions and elements
				for (int i = 0; i < max; i++)
				{
					// what we have selected from the project tree is a project tree item
					IProjectTreeItem item = selectedItems[i];
					
					//if the support item for this ProjectTreeItem is a IFolderItem, we do not show its properties.
					ITreeItem suppItem = item.getProjectTreeSupportTreeItem();
					if (suppItem instanceof ITreeFolder)
					{
						//do nothing
					}
					else
					{
						// and then ask it for its model element
						IElement modelEle = item.getModelElement();
						if (modelEle != null)
						{
							//modelEle = new IElementProxy(modelEle);
							IPropertyElement pEle = processSelectedItem("", propDefs, modelEle); //$NON-NLS-1$
							if (pEle != null)
							{
								propEles.add(pEle);
							}
						}
						else
						{
							// there are items in the project tree that do not have model elements behind them
							// ie. Workspace, Project, Diagram
							// this is the additional processing if one of those items are selected
							IPropertyElement pEle = onTreeSelectSpecial(item, propDefs);
							if (pEle != null)
							{
								propEles.add(pEle);
							}
						}
					}
				}
				setPropertyDefinitions(propDefs);
				setPropertyElements(propEles);
				//populateGrid();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Called when something is selected on the project tree and we cannot get a model element for it
	 * ie. a workspace, a unopened project, a diagram
	 *
	 * @param[in] selectedItem   The item that is selected in the project tree
	 * @param[in] pDefs          The array of property definitions for this property editor
	 * @param[out] pEle          The property element that has been created
	 *
	 * @return HRESULT
	 *
	 */
	public void onDrawingAreaSelectSpecial(IDiagram pDiagram)
	{
		Vector<IPropertyDefinition> propDefs = new Vector<IPropertyDefinition>();
		Vector<IPropertyElement> propEles = new Vector<IPropertyElement>();
		
		IPropertyElement pEle = processSelectedItem("Diagram", propDefs, pDiagram); //$NON-NLS-1$
		if (pEle != null)
		{
			propEles.add(pEle);
		}
		setPropertyDefinitions(propDefs);
		setPropertyElements(propEles);
	}

	/**
	 * Build the presentation section of the property element
	 * 
	 *
	 * @param pEle[in]			The top level property element which will house the presentation section
	 * @param pDisp[in]			The IDispatch from which we will obtain its presentation information
	 *
	 * @return HRESULT
	 */
	private void processSelectedPresentationItem(IPropertyElement pEle, Object pDisp)
	{
//		if (m_Factory != null)
//		{
//			// if the dispatch passed in can have a presentation section, it must be a DrawingPropertyProvider
//			if (pDisp instanceof IDrawingPropertyProvider)
//			{
//				IDrawingPropertyProvider provider = (IDrawingPropertyProvider)pDisp;
//
//				// the information is actually stored in the container of the drawing property provider
//				IPropertyContainer container = provider.getPropertyContainer();
//				if (container != null)
//				{
//					// get the property definition that represents this kind of model element
//					// this will create it from the xml file if it hasn't already been created
//					IPropertyDefinition pDef = m_Factory.getPropertyDefinitionByName("Presentation");
//					if (pDef != null)
//					{
//						if (m_PropEleMgr != null)
//						{
//							// build the property element
//							IPropertyElement newEle = m_PropEleMgr.buildElement(container, pDef, pEle);
//							if (newEle != null)
//							{
//								filterPropertyElement(newEle);
//								// determine if there is presentation information present
//								Vector<IPropertyElement> subEles = newEle.getSubElements();
//								if (subEles != null)
//								{
//									int fontCount = 0;
//									int colorCount = 0;
//									int childCount = 0;
//									int count = subEles.size();
//									for (int i=0; i<count; i++)
//									{
//										IPropertyElement elem = subEles.get(i);
//										if (elem.getName().equals("FontProperties"))
//										{
//											Vector<IPropertyElement> subFonts = elem.getSubElements();
//											if (subFonts != null)
//											{
//												fontCount = subFonts.size();
//											}
//										}
//										if (elem.getName().equals("ColorProperties"))
//										{
//											Vector<IPropertyElement> subCols = elem.getSubElements();
//											if (subCols != null)
//											{
//												fontCount = subCols.size();
//											}
//										}
//										if (elem.getName().equals("ChildProperties"))
//										{
//											Vector<IPropertyElement> subChilds = elem.getSubElements();
//											if (subChilds != null)
//											{
//												fontCount = subChilds.size();
//											}
//										}
//									}
//
//									if ( (fontCount > 0) || (colorCount > 0) || (childCount > 0) )
//									{
//										newEle.setElement(pDisp);
//										pEle.addSubElement(newEle);
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}
	}

	/**
	 * Called when something is selected on the project tree and we cannot get a model element for it
	 * ie. a workspace, a unopened project, a diagram
	 *
	 * @param[in] selectedItem   The item that is selected in the project tree
	 * @param[in] pDefs          The array of property definitions for this property editor
	 * @param[out] pEle          The property element that has been created
	 *
	 * @return HRESULT
	 *
	 */
	private IPropertyElement onTreeSelectSpecial(IProjectTreeItem selectedItem, 
							Vector<IPropertyDefinition> pDefs)
	{
		IPropertyElement retEle = null;
		boolean isWork = selectedItem.isWorkspace();
		boolean isProj = selectedItem.isProject();
		if (isWork)
		{
			// get the workspace that is selected
			IWorkspace space = ProductHelper.getWorkspace();
			retEle = processSelectedItem("Workspace", pDefs, space); //$NON-NLS-1$
		}
		else if (isProj)
		{
			// have a closed project selected
			// if the project is open, we will have a model element and will not be
			// in this processing
			String projName = selectedItem.getItemText();
			if (projName.length() > 0)
			{
				IWorkspace space = ProductHelper.getWorkspace();
				if (space != null)
				{
					IWSProject wsproj = space.getWSProjectByName(projName);
					if (wsproj != null)
					{
						retEle = processSelectedItem("WSProject", pDefs, wsproj); //$NON-NLS-1$
					}
				}
			}
		}
		else
		{
			// might have a diagram selected
			String description = selectedItem.getDescription();
			if (description.length() > 0)
			{
				if (description.endsWith(".etld")) //$NON-NLS-1$
				{
					IProxyDiagramManager diaMgr = ProxyDiagramManager.instance();
					IProxyDiagram proxyDiag = diaMgr.getDiagram(description);
					if (proxyDiag != null)
					{
						IDiagram diag = proxyDiag.getDiagram();
						if (diag != null)
						{
							retEle = processSelectedItem("Diagram", pDefs, diag); //$NON-NLS-1$
						}
						else
						{
							retEle = processSelectedItem("ProxyDiagram", pDefs, proxyDiag); //$NON-NLS-1$
						}
					}
				}
			}
		}
		return retEle;
	}

	public Vector<Object> buildSubElementsThatNeedToDisplay(IPropertyElement pEle, 
												JDefaultMutableTreeNode node)
	{
		Vector<Object> retObj = new Vector<Object>();

		if (pEle != null)
		{		
			IPropertyDefinition pDef = pEle.getPropertyDefinition();
			Vector<IPropertyElement> elems = pEle.getSubElements();
			
			if (elems != null)
			{
				int count = elems.size();
				for (int i=0; i<count; i++)
				{
					JDefaultMutableTreeNode mNode = null;
					IPropertyElement subEle = elems.elementAt(i);
					IPropertyDefinition subDef = subEle.getPropertyDefinition();
					if (subDef != null)
					{
						// we may not want to build the property elements just yet
						// we got into a performance hit by building all of the property elements up front
						boolean onDem = subDef.isOnDemand();
						if (onDem && i < 1)
						{
							// the property definition says that this is an on demand one, therefore the
							// property element hasn't been built yet
							Object pDisp = subEle.getElement();
							
							// get the property definition for the element
							IPropertyDefinition pd2 = getPropertyDefinition(subEle);
							if (pd2 != null)
							{
								pd2.setParent(pDef);
								boolean onDem2 = pd2.isOnDemand();
	
								// everything is ready to be built now, so build it
								if (m_Mode == EDITOR_SEL)
								{
									String value = (String)m_FilterCombo.getSelectedItem();
	
									// set the value on the property element
									IConfigStringTranslator trans = ConfigStringHelper.instance().getTranslator();
									if (trans != null)
									{
										String transValue = trans.translateIntoPSK(null, value);
										String displayValue = subDef.getFromAttrMap("displayName"); //$NON-NLS-1$
										if (displayValue.equals(transValue))
										{
											mNode = addSubElementToTable(node, pd2, subEle, onDem2);
										}
									}
								}
								else
								{
									mNode = addSubElementToTable(node, pd2, subEle, onDem2);
								}
							}
						}
						else
						{
							if (m_Mode == EDITOR_SEL)
							{
								String value = (String)m_FilterCombo.getSelectedItem();
	
								// set the value on the property element
								IConfigStringTranslator trans = ConfigStringHelper.instance().getTranslator();
								if (trans != null)
								{
									String transValue = trans.translateIntoPSK(null, value);
									String displayValue = subDef.getName();//subDef.getFromAttrMap("displayName");
									if (displayValue != null && displayValue.equals(value))
									{
										mNode = addSubElementToTable(node, subDef, subEle, false);
									}
								}
							}
							else
							{
								mNode = addSubElementToTable(node, subDef, subEle, false);
							}
						}
					}
					if (mNode != null)
					{
						retObj.add(mNode); 
					}
				}
			}
		}
		return retObj;
	}

	/**
	 * Add the sub element of the passed in property element to the grid
	 *
	 * @param[in] pNode     The grid node to add element to
	 * @param[in] pDef      The property definition associated with the property element
	 * @param[in] pEle      The property element to be added at this time
	 * @param[in] onDemand  Whether or not the property element should be built now or later
	 *
	 * @return HRESULT
	 *
	 */
	private JDefaultMutableTreeNode addSubElementToTable(JDefaultMutableTreeNode pNode,
									  IPropertyDefinition pDef,
									  IPropertyElement pEle,
									  boolean onDemand)
	{
		JDefaultMutableTreeNode retNode = null;
		// need the parent of the definition so that we can do special processing for fonts/colors
		IPropertyDefinition parentDef = pDef.getParent();
		String parentName = ""; //$NON-NLS-1$
		if (parentDef != null)
		{
			parentName = parentDef.getName();
		}
		
		String name = pDef.getName();
		
		// get the name to be displayed in the name field of the property editor
		String displayName = pDef.getDisplayName();

		// get the value
		m_PropEleMgr.interpretElementValue(pEle);
		String value = pEle.getValue();
		
		// now determine if there are subelements by getting the sub element count
		  // we will also need to do a check on these sub elements to see if the sub element
		  // is on demand, if it is, it is a placeholder and may not cause us to add this
		Vector<IPropertyElement> subEles = pEle.getSubElements();
		
		// get the control type of the definition
		String cType = pDef.getControlType();
		
		// get the multiplicity of the definition
		long mult = pDef.getMultiplicity();
		
		int count = 0;
		if (subEles != null)
		{
			count = subEles.size();
		}

		// Now we have the information that we need, so should we add this node
		// But first, we will want to do several rule checks to see if it should be added
		//
		if ( (cType != null && cType.equals("read-only")) && (mult > 1)  //$NON-NLS-1$
				&& (count == 0) && (!onDemand))
		{
			//ETSystem.out.println("Ignoring in first loop " + name + cType + mult + count + onDemand);
		   // Can't remember why this is here but it is needed
			  // I think it is to not show the collections that are for display purposes (can't be
			  // added to through the property editor - relationships).  We didn't want to display
			  // the node if it was empty - Associations, Generalizations if there weren't any
		}
		else if ( (cType == null || cType.equals("read-only")) &&  //$NON-NLS-1$
					(mult == 1) && (count == 0) && 
					(!onDemand) && (value == null || value.length() == 0) &&
					(!parentName.equals("ChildProperties")) //$NON-NLS-1$
				   )
		{
			//ETSystem.out.println("Ignoring in second loop " + name);
			  // VersionedFileName : We don't want to display a read only single field that has no value
		}
		else if ( (m_Mode == EDITOR_DATA) && 
				  ( name.equals("Presentation")  //$NON-NLS-1$
				  	|| name.equals("FontProperty")  //$NON-NLS-1$
				  	|| name.equals("ColorProperty") ) ) //$NON-NLS-1$
		{
			//ETSystem.out.println("Ignoring in third loop " + name);
			// if we are in the mode where the user only wants to see data information, the property element
			// could have property elements representing the presentation section, so filter these out
		}
		else if ( (m_Mode == EDITOR_PRES) && 
				  (!name.equals("Presentation")) && //$NON-NLS-1$
				  (!name.equals("PropertyContainer")) &&  //$NON-NLS-1$
				  (!name.equals("FontProperties")) &&  //$NON-NLS-1$
				  (!name.equals("ColorProperties")) &&  //$NON-NLS-1$
				  (!name.equals("FontProperty")) &&  //$NON-NLS-1$
				  (!name.equals("ColorProperty")) &&  //$NON-NLS-1$
				  (!name.equals("ChildProperties"))  //$NON-NLS-1$
				)
		{
			//ETSystem.out.println("Ignoring in fourth loop " + name);
			// if we are in the mode where the user only wants to see only presentation information, 
			// the property element could actually have both data and presentation sections. So we
			// filter out the data stuff
		}
		else if (parentName.equals("FontProperty")) //$NON-NLS-1$
		{
			//ETSystem.out.println("Ignoring in fifth loop " + name);
			// we don't want to add sub elements of a font property because fonts appear as one line, so the
			// sub elements have been taken care of in special processing
		}
		else if (parentName.equals(PropertyEditorResources.getString("PropertyEditor.ColorProperty_132"))) //$NON-NLS-1$
		{
			//ETSystem.out.println("Ignoring in sixth loop " + name);
			// we don't want to add sub elements of a color property because colors appear as one line, so the
			// sub elements have been taken care of in special processing
		}
	  	else
		{
			//ETSystem.out.println("Finally adding " + name);
			// add the node to the grid
			retNode = new JDefaultMutableTreeNode(pEle, true);
			pNode.add(retNode);
//			if (m_Tree != null)
//			{
//				m_Tree.updateUI();
//			}
			
			// special processing to mark this grid node as a record in case we need to process
			// it as a whole.  This is due to the changing of the way we save to do it by record
			// rather than by cell
			String recd = pDef.getFromAttrMap("pseudoRecord"); //$NON-NLS-1$
			if ( (count > 0 && mult == 1) || 
				 name.equals("Multiplicity") ||  //$NON-NLS-1$
				 (recd != null && recd.equals("true")) ) //$NON-NLS-1$
			{
				retNode.setKey("Record"); //$NON-NLS-1$
			}
			int newRow = retNode.getRow();
			
			// do not expand the node
			retNode.setExpanded(false);
			
			// Now check the name for fonts/colors so that we can do special processing
			if (name.equals("FontProperty")) //$NON-NLS-1$
			{
				specialFontProcessing(newRow, pDef, pEle);
			}
			else if (name.equals("ColorProperty")) //$NON-NLS-1$
			{
				specialColorProcessing(newRow, pDef, pEle);
			}
			else if (name.equals("PropertyContainer")) //$NON-NLS-1$
			{
				specialPropertyProcessing(newRow, pDef, pEle);
			}
			else
			{
				// set the value
				
			}
			
			if (!name.equals("ColorProperty")) //$NON-NLS-1$
			{
				// set the color of the grid cell
				int color = m_EditableColor;
				boolean readOnly = isReadOnlyStatus(pDef, pEle);
				if (readOnly)
				{
					color = m_ReadOnlyColor;
				}
				
				boolean boldness = isBold(pDef, pEle);
				
				//set the color of the row and set the boldness of the row.
			}
			
			if (!onDemand)
			{
				// process further sub elements if necessary
				buildSubElementsThatNeedToDisplay(pEle, retNode);
			}
		}
		return retNode;
	}
	
	/**
	 * Font property elements are displayed differently than other preference elements.  The font
	 * definition looks like:
	 * 
	 *	<PropertyDefinition id="{6C77C083-7008-4CE0-893E-D4C198D5E8C2}" name="FontProperty" controlType="multiedit">
	 *		<aDefinition name="FontDisplayName" get="DisplayNameID"/>
	 *		<aDefinition name="FontColor" get="Color" set="Color"/>
	 *	</PropertyDefinition>
	 *
	 * but we only want to display one line in the grid and have that one line represent all of the font values
	 * (ie. Have the name "Arial" displayed, but have it bolded and the right size
	 *
	 * We will use the FontDisplayName for the left hand side of the property editor
	 * and for the other properties, we will ask the FontProperty for its information
	 *
	 * @param pDef[in]		The property definition associated with the preference element
	 * @param pEle[in]		The property element representing the font
	 *
	 * @return HRESULT
	 */
	private void specialFontProcessing(int row, IPropertyDefinition pDef, IPropertyElement pEle)
	{
		Object pDisp = pEle.getElement();
		if (pDisp != null && pDisp instanceof IFontProperty)
		{
			IFontProperty fontProp = (IFontProperty)pDisp;
			specialFontProcessing2(row, fontProp, pEle);
		}
	}
	
	/**
	 * Font property elements are displayed differently than other preference elements.  The font
	 * definition looks like:
	 * 
	 *	<PropertyDefinition id="{6C77C083-7008-4CE0-893E-D4C198D5E8C2}" name="FontProperty" controlType="multiedit">
	 *		<aDefinition name="FontDisplayName" get="DisplayNameID"/>
	 *		<aDefinition name="FontColor" get="Color" set="Color"/>
	 *	</PropertyDefinition>
	 *
	 * but we only want to display one line in the grid and have that one line represent all of the font values
	 * (ie. Have the name "Arial" displayed, but have it bolded and the right size
	 *
	 * We will use the FontDisplayName for the left hand side of the property editor
	 * and for the other properties, we will ask the FontProperty for its information
	 *
	 * @param row[in]					The current row
	 * @param pFontProperty[in]	The font property to load into the grid
	 * @param pEle[in]		The property element representing the font
	 *
	 * @return HRESULT
	 */
	private void specialFontProcessing2(int row, IFontProperty fontProp, IPropertyElement pEle)
	{
		// This method builds the left hand side of the font in the grid
		Vector<IPropertyElement> subEles = pEle.getSubElements();
		if (subEles != null)
		{
			String name, displayName = null;
			for (int i=0; i<subEles.size(); i++)
			{
				IPropertyElement subEle = subEles.elementAt(i);
				if (subEle.getName().equals("FontDisplayName")) //$NON-NLS-1$
				{
					name = subEle.getValue();
					IConfigStringTranslator translator = ConfigStringHelper.instance().getTranslator();
					if (translator != null)
					{
						displayName = translator.translate(null, name);
					}
				}
			}
			
			if (displayName != null && displayName.length() > 0)
			{
				//set the display name for this element
				//_VH(m_PropertyGrid->put_Cell(flexcpText, CComVariant(row), CComVariant(m_HeadingCol), vtMissing, vtMissing, CComVariant(dispName)));
				specialFontProcessing3(row, fontProp);
				
				// set the behind the scenes data to the property element
				//_VH(m_PropertyGrid->put_Cell(flexcpData, CComVariant(row), CComVariant(m_DataCol), vtMissing, vtMissing, CComVariant(pEle)));
				
			}
		}
	}

	/**
	 * Font property elements are displayed differently than other preference elements.  The font
	 * definition looks like:
	 * 
	 *	<PropertyDefinition id="{6C77C083-7008-4CE0-893E-D4C198D5E8C2}" name="FontProperty" controlType="multiedit">
	 *		<aDefinition name="FontDisplayName" get="DisplayNameID"/>
	 *		<aDefinition name="FontColor" get="Color" set="Color"/>
	 *	</PropertyDefinition>
	 *
	 * but we only want to display one line in the grid and have that one line represent all of the font values
	 * (ie. Have the name "Arial" displayed, but have it bolded and the right size
	 *
	 * We will use the FontDisplayName for the left hand side of the property editor
	 * and for the other properties, we will ask the FontProperty for its information
	 *
	 * @param row[in]					The current row
	 * @param pFontProperty[in]	The font property to load into the grid
	 *
	 * @return HRESULT
	 */
	private void specialFontProcessing3(int row, IFontProperty pFontProperty)
	{
		
	}

	/**
	 * Color property elements are displayed differently than other preference elements.  The color
	 * definition looks like:
	 * 
	 *	<PropertyDefinition id="{6C77C083-7008-4CE0-893E-D4C198D5E8C2}" name="ColorProperty" controlType="multiedit">
	 *		<aDefinition name="ColorName" get="DisplayNameID"/>
	 *		<aDefinition name="Color" get="Color" set="Color"/>
	 *	</PropertyDefinition>
	 *
	 * but we only want to display one line in the grid and have that one line represent all of the color values
	 * (ie. Have the color stored in the property editor
	 *
	 * We will use the ColorName for the left hand side of the property editor
	 * and for the other properties, we will ask the ColorProperty for its information
	 *
	 * @param row[in]				The current row
	 * @param pProperty[in]		The color property to load into the grid
	 * @param pEle[in]			The property element representing the color
	 *
	 * @return HRESULT
	 */
	private void specialColorProcessing(int row, IPropertyDefinition pDef, IPropertyElement pEle)
	{
		Object pDisp = pEle.getElement();
		if (pDisp != null && pDisp instanceof IColorProperty)
		{
			IColorProperty colorProp = (IColorProperty)pDisp;
			specialColorProcessing2(row, colorProp, pEle);
		}
	}

	/**
	 * Color property elements are displayed differently than other preference elements.  The color
	 * definition looks like:
	 * 
	 *	<PropertyDefinition id="{6C77C083-7008-4CE0-893E-D4C198D5E8C2}" name="ColorProperty" controlType="multiedit">
	 *		<aDefinition name="ColorName" get="DisplayNameID"/>
	 *		<aDefinition name="Color" get="Color" set="Color"/>
	 *	</PropertyDefinition>
	 *
	 * but we only want to display one line in the grid and have that one line represent all of the color values
	 * (ie. Have the color stored in the property editor
	 *
	 * We will use the ColorName for the left hand side of the property editor
	 * and for the other properties, we will ask the ColorProperty for its information
	 *
	 * @param row[in]				The current row
	 * @param pProperty[in]		The color property to load into the grid
	 * @param pEle[in]			The property element representing the color
	 *
	 * @return HRESULT
	 */
	private void specialColorProcessing2(int row, IColorProperty colorProp, IPropertyElement pEle)
	{
		// get the display name of the color property
		String name, displayName;
		int color = 0;
		
		Vector<IPropertyElement> subEles = pEle.getSubElements();
		if (subEles != null)
		{
			for (int i=0; i<subEles.size(); i++)
			{
				IPropertyElement subEle = subEles.elementAt(i);
				if (subEle.getName().equals("ColorName")) //$NON-NLS-1$
				{
					name = subEle.getValue();
					IConfigStringTranslator translator = ConfigStringHelper.instance().getTranslator();
					if (translator != null)
					{
						displayName = translator.translate(null, name);
					}
				}
			}
		}
			
		// if there is no value for this, keep the color at white
		// if there is a value, and it is black ("0"), need to set the grid color to one, per flex grid
		/*
		Remarks    
		Setting this property to zero (black) causes the control to paint the cell using the standard 
		colors (set by the BackColor and BackColorAlternate properties). 
		Therefore, to set this property to black, use RGB(1,1,1) instead of RGB(0,0,0).
		*/
		int cref = colorProp.getColor();
//		BYTE rvalue = GetRValue(cref);
//		BYTE gvalue = GetGValue(cref);
//		BYTE bvalue = GetBValue(cref);
//		if (rvalue == 0 && gvalue == 0 && bvalue == 0)
//		{
//			color = 1;
//		}
//		else
//		{
//			color = cref;
//		}
		
		if (m_Model != null)
		{
			// now set the information in the grid
			//_VH(m_PropertyGrid->put_Cell(flexcpText, CComVariant(row), CComVariant(m_HeadingCol), vtMissing, vtMissing, CComVariant(dispName)));
			//_VH(m_PropertyGrid->put_Cell(flexcpBackColor, CComVariant(row), CComVariant(m_DataCol), vtMissing, vtMissing, CComVariant(color)));
			// set the behind the scenes data to the property element
			//_VH(m_PropertyGrid->put_Cell(flexcpData, CComVariant(row), CComVariant(m_DataCol), vtMissing, vtMissing, CComVariant(pEle)));
			//_VH(m_PropertyGrid->put_Cell(flexcpRefresh, CComVariant(row), CComVariant(m_DataCol), vtMissing, vtMissing, CComVariant(TRUE)));
		}
	}

	/**
	 * Child property elements are displayed differently than other preference elements.  
	 *
	 * @param row[in]				The current row
	 * @param pProperty[in]		The child property to load into the grid
	 * @param pEle[in]			The property element representing the color
	 *
	 * @return HRESULT
	 */
	private void specialPropertyProcessing(int row, IPropertyDefinition pDef, IPropertyElement pEle)
	{
		Object pDisp = pEle.getElement();
		if (pDisp != null && m_Model != null && pDisp instanceof IPropertyContainer)
		{
			IPropertyContainer pContainer = (IPropertyContainer)pDisp;
			String displayName = pContainer.getDisplayName();
			
			int color = m_EditableColor;
			boolean readOnly = isReadOnlyStatus(pDef, pEle);
			if (readOnly)
			{
				color = m_ReadOnlyColor;
			}
			boolean boldness = isBold(pDef, pEle);
			
//			_VH(m_PropertyGrid->put_Cell(flexcpText, CComVariant(row), CComVariant(m_HeadingCol), vtMissing, vtMissing, CComVariant(displayName)));
//			_VH(m_PropertyGrid->put_Cell(flexcpText, CComVariant(row), CComVariant(m_DataCol), vtMissing, vtMissing, CComVariant(value)));
//			// set the behind the scenes data to the property element
//			_VH(m_PropertyGrid->put_Cell(flexcpData, CComVariant(row), CComVariant(m_DataCol), vtMissing, vtMissing, CComVariant(pEle)));
//
//			_VH(m_PropertyGrid->put_Cell(flexcpBackColor, CComVariant(row), CComVariant(m_DataCol), vtMissing, vtMissing, CComVariant(color)));
//			// set the boldness of the font of the grid cell
//			_VH(m_PropertyGrid->put_Cell(flexcpFontBold, CComVariant(row), CComVariant(m_HeadingCol), vtMissing, vtMissing, CComVariant(boldness)));
//			_VH(m_PropertyGrid->put_Cell(flexcpFontBold, CComVariant(row), CComVariant(m_DataCol), vtMissing, vtMissing, CComVariant(boldness)));
			
		}
	}

	/**
	 * Retrieve the property definition from the definition factory
	 * 
	 *
	 * @param pEle[in]			The current property element
	 * @param pDef[out]			The current property definition
	 *
	 * @return HRESULT
	 *
	 */
	private IPropertyDefinition getPropertyDefinition(IPropertyElement pEle)
	{
		IPropertyDefinition retDef = null;
		
		// get the property definition for the element on the property element
		Object pDisp = pEle.getElement();
		IPropertyDefinition pDef = null;
		if (pDisp != null)
		{
			if (pDisp instanceof IElement)
			{
				// can reuse a method if it is an IElement
				pDef = m_Factory.getPropertyDefinitionForElement("", pDisp); //$NON-NLS-1$
			}
			else if (pDisp instanceof IFontProperty)
			{
				// need to specifically retrieve this element, because can't get its "type"
				pDef = m_Factory.getPropertyDefinitionByName("FontProperty"); //$NON-NLS-1$
			}
			else if (pDisp instanceof IColorProperty)
			{
				// need to specifically retrieve this element, because can't get its "type"
				pDef = m_Factory.getPropertyDefinitionByName("ColorProperty"); //$NON-NLS-1$
			}
			else if (pDisp instanceof IPropertyContainer)
			{
				// need to specifically retrieve this element, because can't get its "type"
				pDef = m_Factory.getPropertyDefinitionByName("PropertyContainer"); //$NON-NLS-1$
			}
		}
		else
		{
			String name = pEle.getName();
			// need to specifically retrieve this element, because can't get its "type"
			pDef = m_Factory.getPropertyDefinitionByName(name);
			if (pDef != null)
			{
				String value = pDef.getFromAttrMap("pseudoRecord"); //$NON-NLS-1$
				if (value != null && !value.equals("true")) //$NON-NLS-1$
				{
					pDef = null;
				}
			}
		}
		if (pDef != null)
		{
			retDef = pDef;
		}
		return retDef;
	}

	/**
	 * Called when the create popup menu is clicked upon
	 *
	 * @param[in] wNotifyCode

	 * @param[in] wID
	 * @param[in] hWndCtrl
	 * @param[out] bHandled
	 *
	 * @return LRESULT
	 *
	 */
	public void onPopupCreate(int row, IPropertyElement element)
	{
      if (m_Tree != null && m_Model != null)
      {
   		onNodeExpanding(row);
   		IPropertyElement pEle = createEmptyPropertyElement(row, true);
   		JDefaultMutableTreeNode node = null;
   		if (pEle != null)
   		{
   			// now expand and set up the node that will be added
   			node = addNodeToGrid(row, pEle);
   			
   			//Now go through the sub elements of pEle and add those to the tree
   			//buildSubElementsThatNeedToDisplay(pEle, node);
   		}
   		//refreshElementInGrid(m_CurRow, pEle);
   		m_Tree.getTree().updateUI();
   		m_Model.expand(row, true);
   		if (node != null)
   		{
   			node.setExpanded(true);
   			JDefaultMutableTreeNode pNode = getNodeAtGridRow(row);
   			int iRow = pNode.getIndex(node);
   			
   			//expand the node that is just created.
   			m_Model.expand(row+iRow+1, true);
   			//m_Tree.expandNode(node, true);
   			
   			//I need to start editing the first node in this newly created node.
   			final int editRow = row+iRow+2;
   			SwingUtilities.invokeLater(new Runnable() {
   
   				public void run() {
   					startEditingRowAt(editRow);
   				}
   			});
   			
   		}
      }
	}
	
	/**
	 * Called when the user clicks on the delete popup menu 
	 *
	 * @param[in] wNotifyCode
	 * @param[in] wID
	 * @param[in] hWndCtrl
	 * @param[out] bHandled
	 *
	 * @return LRESULT
	 */
	public void onPopupDelete()
	{
		if (m_Model != null && m_Tree != null)
		{
			// we will try to delete any rows that are selected
			// I had a terrible time with the grid and processing the rows that are selected
			// I finally had to break it up into sections, and this seems to work, so I didn't
			// want to mess with it anymore
			//
			// First get the rows that are selected that are marked as records
			// this just stores the row number in an array
			int count = m_Tree.getSelectedRowCount();
			int[] rows = m_Tree.getSelectedRows();
			Vector tempRows = new Vector();
			for (int i=0; i<count; i++)
			{
				int rowNum = rows[i];
				if (count == 1)
				{
					//only one row is selected, user may not have selected
					//this row and right clicked, if that is the case, use the 
					//saved row value
					if (rowNum != m_RightClickRow)
					{
						rowNum = m_RightClickRow;
					}
				}
				JDefaultMutableTreeNode node = getNodeAtGridRow(rowNum);
				if (node != null)
				{
					String key = node.getKey();
					if (key != null && (key.equals("Record") || key.equals("RecordNew")) ) //$NON-NLS-1$ //$NON-NLS-2$
					{
						tempRows.addElement(node);
					}
				}
			}
			
			// now loop through that array of selected "record" rows and store the property elements
			// in a temporary array to be removed in the next step.  I couldn't remove them as I went
			// because then the grid data got all messed up and I couldn't do any further processing
			// if more than one row was selected
			int rowCount = tempRows.size();
			for (int i=rowCount-1; i>=0; i--)
			{
				JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)tempRows.elementAt(i);
				IPropertyElement pEle = (IPropertyElement)node.getUserObject();
				if (pEle != null)
				{
					JDefaultMutableTreeNode parentNode = (JDefaultMutableTreeNode)node.getParent();
					IPropertyDefinition pDef = pEle.getPropertyDefinition();
					boolean bEdit = isElementEditable(pEle, pDef);
					if (bEdit)
					{
						//DefaultMutableTreeNode parentNode = node.getParent();
						node.removeFromParent();
						
						//now remove it from the xml file
						pEle.setModified(true);
						IPropertyElement delEle = getDeleteElement(pEle);
						if (delEle != null)
						{
							String delName = delEle.getName();
							if (delName.equals("DiagramName") || delName.equals("ElementName"))
							{
								deleteDiagram(delEle);
							}
							else if (delName.equals("ReferencingReferences") || delName.equals("ReferencedReferences"))
							{
								deleteReference(delEle, pEle);
							}
							else
							{
								// delete, make sure to create a blocker to block other events!
								// only want to call delete if there is an element to delete
								PropertyEditorBlocker.block();
								try
								{
									// Because of Issue 7295 (where there is a new attribute
									// and we try and delete it, the class gets deleted)
									// we need to check to see if there is an element to delete
									// this introduces a problem with the ability to delete a reference
									// library (because there is not an element on it)
									// so we are special casing the reference library
									String parentName = ""; //$NON-NLS-1$
									IPropertyElement parentEle = pEle.getParent();
									if (parentEle != null)
									{
										parentName = parentEle.getName();
									}
									Object pDisp = delEle.getElement();
									if (pDisp != null || parentName.equals("ReferencedLibraries")) //$NON-NLS-1$
									{
										m_PropEleMgr.deleteData(delEle, pEle);
									}
								}
								finally
								{
									PropertyEditorBlocker.unblock();
								}
							}
						}
						updateNodeAndParentNodes(node, pEle);
						
						//since the node is now removed, I want to refresh the parent
						if (parentNode != null)
						{
							boolean expanded = parentNode.isExpanded();
							if (expanded)
							{
								parentNode.setExpanded(false);
								parentNode.setExpanded(true);
							}
						}
					}
				}
			}
			
			// now that both the grid and the xml have been updated, process the parent nodes to see if their
			// format strings need to be updated
			refresh();
		}
	}
	
	/**
	 * @param node
	 * @param pEle
	 */
	private void updateNodeAndParentNodes(JDefaultMutableTreeNode node, IPropertyElement pEle)
	{
		if (m_Model != null)
		{
			String name = pEle.getName();
			String value = pEle.getValue();
			
			IPropertyDefinition pDef = pEle.getPropertyDefinition();
			if (pDef != null)
			{
				String validVals = pDef.getValidValues();
				if (validVals != null && validVals.equals("FormatString")) //$NON-NLS-1$
				{
					m_PropEleMgr.interpretElementValue(pEle);
					String val2 = pEle.getValue();
					if (val2 == null || val2.length() == 0)
					{
						val2 = value;
					}
					//update this value in the data grid.
				}
				else if (name.equals("Name")) //$NON-NLS-1$
				{
					// if the element that we are editing/updating is the name value
					// then we also want to update the alias field because there were
					// some bugs entered where they were not in synch
					//
					// so, get the element at the next row (should be the alias)
					int aliasRow = m_LastRow + 1;
					IPropertyElement aliasEle = getElementAtGridRow(aliasRow);
					if (aliasEle != null)
					{
						String tempName = aliasEle.getName();
						if (tempName != null && tempName.equals("Alias")) //$NON-NLS-1$
						{
							// now rebuild the value because by setting the name
							// the alias could have changed
							IPropertyDefinition tempDef = aliasEle.getPropertyDefinition();
							Object tempMod = aliasEle.getElement();
							IPropertyElement tempEle = m_PropEleMgr.buildElement(tempMod, tempDef, null);
							String val2 = tempEle.getValue();
							refresh();
						}
					}
				}
			}
			updateNecessaryParents(node, pEle);
		}
	}

	/**
	 * Some of the updates that occur in the grid may also warrant a change to a node above it
	 * 
	 *
	 * @param pNode[in]		The current grid node
	 * @param pEle[in]		The current property element
	 *
	 * @return HRESULT
	 *
	 */
	private void updateNecessaryParents(JDefaultMutableTreeNode node, 
										IPropertyElement pEle)
	{
		if (m_Model != null)
		{
			IPropertyDefinition pDef = pEle.getPropertyDefinition();
			if (pDef != null)
			{
				IPropertyElement parentEle = pEle.getParent();
				if (parentEle != null)
				{
					IPropertyDefinition parentDef = parentEle.getPropertyDefinition();
					if (parentDef != null)
					{
						JDefaultMutableTreeNode parentNode = (JDefaultMutableTreeNode)node.getParent();
						if (parentNode != null)
						{
							String validVals = parentDef.getValidValues();
							boolean alreadyProcessed = false;
							if (validVals != null && validVals.equals("FormatString")) //$NON-NLS-1$
							{
								Object obj = parentNode.getUserObject();
								if (obj != null && obj instanceof IPropertyElement)
								{
									IPropertyElement propEle = (IPropertyElement)obj;
									m_PropEleMgr.interpretElementValue(parentEle);
									alreadyProcessed = true;
								}
							}
							updateNecessaryParents(parentNode, parentEle);
						}
					}
				}
			}
		}
	}

	/**
	 * Special processing for associated diagrams on diagram and element
	 * 
	 *
	 * @param pEle[in]		The property element that houses the diagram info that is being deleted
	 *
	 * @return HRESULT
	 *
	 */
	private void deleteDiagram(IPropertyElement pEle)
	{
		if (pEle != null)
		{
			// first ask the user about deleting this association
			boolean answer = false;
			IQuestionDialog pQuestionDiag = new SwingQuestionDialogImpl();
			if (pQuestionDiag != null)
			{
				String text = PropertyEditorResources.getString("PropertyEditor.DeleteQuestion");
				QuestionResponse result = pQuestionDiag.displaySimpleQuestionDialog(SimpleQuestionDialogKind.SQDK_YESNO, MessageIconKindEnum.EDIK_ICONQUESTION, text, 0, null, "");
				if (result.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_YES)
				{
					Object pTopDisp = getTopModelElement(pEle);
					if (pTopDisp instanceof IElement)
					{
						IElement pTopElement = (IElement)pTopDisp;
						// the top guy is an element or an open diagram
						if (pTopElement instanceof IDiagram)
						{
							IDiagram pDiagram = (IDiagram)pTopElement;
							// the top guy is an open diagram
							//	get the element that we are deleting
							Object pDisp = pEle.getElement();
							// figure out if it is a closed diagram or an element (maybe an open diagram)
							if (pDisp instanceof IProxyDiagram)
							{
								IProxyDiagram pDiag = (IProxyDiagram)pDisp;
								// deleting a closed diagram from an open diagram's properties
								String xmiID = pDiag.getXMIID();
								String xmiID2 = pDiagram.getXMIID();
								pDiagram.removeAssociatedDiagram(xmiID);
								pDiag.removeAssociatedDiagram(xmiID2);
							}
							else if (pDisp instanceof IElement)
							{
								IElement pElement = (IElement)pDisp;
								if (pElement instanceof IDiagram)
								{
									IDiagram pDiagram2 = (IDiagram)pElement;
									// deleting an open diagram from an open diagram's properties
									String xmiID = pDiagram2.getXMIID();
									String xmiID2 = pDiagram.getXMIID();
									pDiagram.removeAssociatedDiagram(xmiID);
									pDiagram2.removeAssociatedDiagram(xmiID2);
								}
								else
								{
									// deleting an element from an open diagram's properties
									pDiagram.removeAssociatedElement2(pElement);
								}
							}
						}
						else
						{
							// the top guy is an element
							// get the element that we are deleting
							Object pDisp = pEle.getElement();
							// figure out if it is a closed diagram or an element (maybe an open diagram)
							if (pDisp instanceof IProxyDiagram)
							{
								IProxyDiagram pDiag = (IProxyDiagram)pDisp;
								// deleting a closed diagram from an element's properties
								pDiag.removeAssociatedElement(pTopElement);
							}
							else if (pDisp instanceof IElement)
							{
								IElement pElement = (IElement)pDisp;
								if (pElement instanceof IDiagram)
								{
									IDiagram pDiagram2 = (IDiagram)pElement;
									// deleting an open diagram from an element's properties
									pDiagram2.removeAssociatedElement2(pTopElement);
								}
							}
						}
					}
					else if (pTopDisp instanceof IProxyDiagram)
					{
						IProxyDiagram pTopDiag = (IProxyDiagram)pTopDisp;
						// the top guy is a closed diagram
						// so get what we are deleting
						Object pDisp = pEle.getElement();
						if (pDisp instanceof IProxyDiagram)
						{
							IProxyDiagram pDiag2 = (IProxyDiagram)pDisp;
							// deleting a closed diagram from a closed diagram's properties
							pDiag2.removeDualAssociatedDiagrams(pTopDiag, pDiag2);
						}
						// could be deleting an element or an open diagram from a closed diagram's properties
						else if (pDisp instanceof IElement)
						{
							IElement pElement = (IElement)pDisp;
							if (pElement instanceof IDiagram)
							{
								IDiagram pDiagram2 = (IDiagram)pElement;
								String xmiID = pDiagram2.getXMIID();
								// deleting an open diagram from a closed diagram's properties
								pDiagram2.removeAssociatedDiagram2(pTopDiag);
								pTopDiag.removeAssociatedDiagram(xmiID);
							}
							else
							{
								// deleting an element from a closed diagram's properties
								pTopDiag.removeAssociatedElement(pElement);
							}
						}
					}
					
				}
			}
		}
	}
	/**
	 * Special processing for associated diagrams on diagram and element
	 * 
	 *
	 * @param pEle[in]		The property element that houses the diagram info that is being deleted
	 *
	 * @return HRESULT
	 *
	 */
	private void deleteReference(IPropertyElement delEle, IPropertyElement pEle)
	{
		if (pEle != null && delEle != null)
		{
			// first ask the user about deleting this reference
			boolean answer = false;
			IQuestionDialog pQuestionDiag = new SwingQuestionDialogImpl();
			if (pQuestionDiag != null)
			{
				String text = PropertyEditorResources.getString("PropertyEditor.DeleteQuestion");
				QuestionResponse result = pQuestionDiag.displaySimpleQuestionDialog(SimpleQuestionDialogKind.SQDK_YESNO, MessageIconKindEnum.EDIK_ICONQUESTION, text, 0, null, "");
				if (result.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_YES)
				{
					// delete, make sure to create a blocker to block other events!
					// only want to call delete if there is an element to delete
					PropertyEditorBlocker.block();
					try
					{
						Object pDisp = delEle.getElement();
						if (pDisp != null)
						{
							m_PropEleMgr.deleteData(delEle, pEle);
						}
					}
					finally
					{
						PropertyEditorBlocker.unblock();
					}
				}
			}
		}
	}

	/**
	 * Refresh the grid node in the grid.  This is only used to add a node to a collection.  It adds
	 * the proper property elements and then expands the proper node and places you in edit mode on the
	 * added node
	 *
	 * @param[in] row    The row to refresh
	 * @param[in] pEle   The property element associated with the row to refresh
	 *
	 * @return HRESULT
	 *
	 */
	private JDefaultMutableTreeNode addNodeToGrid(int row, IPropertyElement pEle)
	{
		JDefaultMutableTreeNode retObj = null;
		if (m_Tree != null)
		{
			// get the grid node at the passed in row
			JDefaultMutableTreeNode curNode = getNodeAtGridRow(row);
			if (curNode != null)
			{
				// get the grid node that is this node's collection node
				JDefaultMutableTreeNode colNode = getCollectionGridNode(curNode, row);
				if (colNode != null)
				{
					// refresh the property elements for this grid node
					pEle.setModified(true);
					IPropertyDefinition pDef = pEle.getPropertyDefinition();
					JDefaultMutableTreeNode addedNode = addSubElementToTable(colNode, pDef, pEle, false);
					
					// now deal with the grid node
					// we want to collapse any children and then expand the node that we just
					// created (this will be the last node of the collection)
					if (colNode.getChildCount() > 0)
					{
						DefaultMutableTreeNode child = (DefaultMutableTreeNode)colNode.getFirstChild();
						int i=1;
						while (child != null)
						{
							if (child instanceof JDefaultMutableTreeNode)
							{
								((JDefaultMutableTreeNode)child).setExpanded(false);
								//m_Tree.expandNode((JDefaultMutableTreeNode)child, false);
								m_Tree.getTree().expandNode(row+i, false);
								m_Model.expand(row+i, false);
							}
							child = child.getNextSibling();
							i++;
						}
						
						//now expand the last node
						child = (DefaultMutableTreeNode)colNode.getLastChild();
						if (child instanceof JDefaultMutableTreeNode)
						{
							retObj = (JDefaultMutableTreeNode)child;
							((JDefaultMutableTreeNode)child).setExpanded(true);
							int childLevel = colNode.getIndex(child);
							//m_Tree.expandNode((JDefaultMutableTreeNode)child, true);
							m_Tree.getTree().expandNode(row+childLevel+1, true);
							m_Model.expand(row+childLevel+1, true);
							// this is ugly, but I cannot figure out another way to do this
							// in order for the create/set/insert problems to go away, nodes need to be marked
							// so that we know when to do a create, a set, or an insert
							//
							// when a node is first added to the grid, if it is a record, it is marked as "Record"
							// this code is invoked when something new is added to the grid
							//	if there is no model element associated with it, we don't want to mark it as "Record",
							// but we do need to mark it so that we can get to it when doing a save
							Object pDisp2 = pEle.getElement();
							String temp = pEle.getName();
							if (pDisp2 == null || (temp != null && temp.equals("Multiplicity"))) //$NON-NLS-1$
							{
								addedNode.setKey("RecordNew"); //$NON-NLS-1$
							}
							
							// expand this last child
							addedNode.setExpanded(true);
							
							// get the first child of this last one
							JDefaultMutableTreeNode firstNode = (JDefaultMutableTreeNode)addedNode.getFirstChild();
							
							if (firstNode != null)
							{
								// set focus to this field
								int firstRow = firstNode.getRow();
								
								//set focus on this row, edit it.
							}
						}
					}
				}
			}
		}
		return retObj;
	}
	
	/**
	 * Method to navigate up the grid node chain to retrieve the grid node that represents
	 * the collection that the passed in grid node is in.
	 *
	 * @param[in] pNode            The grid node in which to get the collection grid node that it belongs to 
	 * @param[out] pCollectionNode The grid node that is the collection node
	 *
	 * @return HRESULT
	 *
	 */
	private JDefaultMutableTreeNode getCollectionGridNode(JDefaultMutableTreeNode curNode, int curRow)
	{
		JDefaultMutableTreeNode retCol = null;

		// get the property element at the row where user clicked
		IPropertyElement pEle = getElementAtGridRow(curRow);
		if (pEle != null)
		{
			// get the corresponding definition
			IPropertyDefinition pDef = pEle.getPropertyDefinition();
			if (pDef != null)
			{
				// if the element has a property definition that is multiple, then we have found it
				long mult = pDef.getMultiplicity();
				if (mult == 1)
				{
					JDefaultMutableTreeNode parentNode = (JDefaultMutableTreeNode)curNode.getParent();
					if (parentNode != null)
					{
						int loc = findNodeRowInTreeTable(parentNode);
						retCol = getCollectionGridNode(parentNode, loc);
					}
				}
				else
				{
					retCol = curNode;
				}
			}
		}

		return retCol;
	}
	/**
	 * @param parentNode
	 * @return
	 */
	private int findNodeRowInTreeTable(JDefaultMutableTreeNode pNode)
	{
		// TODO Auto-generated method stub
		return 0;
	}
	/**
	 * Called when the user has clicked on a row and is expanding the grid node
	 *
	 * @param[in] row  The row that is being expanded
	 *
	 * @return HRESULT
	 *
	 */
	public void onNodeExpanding(int row)
	{
		// Block other events from coming in when we don't want them.
		PropertyEditorBlocker.block();
		try
		{
			// get the property element at the row where user clicked
			IPropertyElement pEle = getElementAtGridRow(row);
			if (pEle != null)
			{
				JDefaultMutableTreeNode node = getNodeAtGridRow(row);
				int countChild = node.getChildCount();
				if (countChild == 0)
				{
					Vector<IPropertyElement> subElems = pEle.getSubElements();
					if (subElems != null && subElems.size() > 0)
					{
						//if this property element was build earlier, we want to rebuild it,
						//since we are rebuilding the default node.
						pEle.setSubElements(new Vector<IPropertyElement>());
					}
				
					buildAndExpandNode(row);
				}
			
				pEle = getElementAtGridRow(row);
				String name = pEle.getName();
				if (name.equals("dummy")) //$NON-NLS-1$
				{
					// get the corresponding definition
					IPropertyDefinition pd = pEle.getPropertyDefinition();
					if (pd != null)
					{
						// get the model element on the property element
						Object curModEle = pEle.getElement();
					
						// get the property definition representing this model element
						IPropertyDefinition pd2 = getPropertyDefinition(pEle);
					
						if (pd2 != null)
						{
							m_PropEleMgr.reloadElement(curModEle, pd2, pEle);
							filterPropertyElement(pEle);
							refreshElementInGrid(row, pEle);
						}
					}
				}
				else
				{
					boolean refresh = false;

					// get its sub elements
					Vector<IPropertyElement> subEles = pEle.getSubElements();
					if (subEles != null)
					{
						int count = subEles.size();
						for (int i=0; i<count; i++)
						{
							IPropertyElement subEle = subEles.elementAt(i);

							// get the corresponding definition
							IPropertyDefinition subDef = subEle.getPropertyDefinition();
							if (subDef != null)
							{
								// has the definition been marked as on demand
								boolean onDem = subDef.isOnDemand();
								if (onDem)
								{
									Object modEle = subEle.getElement();
									IPropertyDefinition pd2 = getPropertyDefinition(subEle);
									if (pd2 != null)
									{
										m_PropEleMgr.reloadElementWithDummy(modEle, pd2, subEle);
										refresh = true;
									}
								}
							}
						}
						if (refresh)
						{
							refreshElementInGrid(row, pEle);
						}
					}
				}
			}
		}
		finally
		{
			PropertyEditorBlocker.unblock();
		}
	}
	
	private IPropertyElement createEmptyPropertyElement(int row, boolean filter)
	{
		IPropertyElement newEle = null;

		// get the property element at the row that we are on
		IPropertyElement pEle = getElementAtGridRow(row);
		if (pEle != null)
		{
			// get the element that represents the collection element
			// If the user has gotten this far, they have right clicked either on a collection or on a node
			// that is under the collection.  We need to get the collection element.
			IPropertyElement colEle = getCollectionElement(pEle);
			if (colEle != null)
			{
				// get the property definition associated with the collection element
				IPropertyDefinition colDef = colEle.getPropertyDefinition();
				if (colDef != null)
				{
					// We are not actually concerned with the collection definition, we are concerned with the
					// sub definition because that will tell us what we need to build
					IPropertyDefinition indivDef = colDef.getSubDefinition(0);
					if (indivDef != null)
					{
						// have the definition that needs to be built, so get its name
						String indDefName = indivDef.getName();

						// get the current model element and save off for later use
						Object mainEle = m_PropEleMgr.getModelElement();
						
						// determine if the definition with this name has been built, if not build it
						// this will be the definition "Attribute" (whatever we are creating)
						IPropertyDefinition newDef = m_Factory.getPropertyDefinitionForElement(indDefName, mainEle);
						if (newDef != null)
						{
							// have the definition, so set up its parent, because it may have just been built
							newDef.setParent(colDef);

							// now that we have the definition, we need to build the corresponding empty property element
							IPropertyElement newPropEle = null;
							// but we were having problems building property elements for something that
							// did not have a model element with it
							// we were getting collections that we didn't want to show (references)
							// and then when we implemented the filter, we didn't know what filter to
							// use because there was no model element
							FactoryRetriever ret = FactoryRetriever.instance();
							// so we are going to create a fake model element to go on this property
							// element structure so that the build and the filter work right
							// but then we will remove it so that the save will also work ("create")
							// but first see if it is a valid metatype
							ICoreProduct cProd = CoreProductManager.instance().getCoreProduct();
							//IProduct prod = ProductHelper.getProduct();
							Object tempDisp = null;
							if (cProd != null)
							{
								//ICreationFactory fact = cProd.getCreationFactory();
//								ICreationFactory fact = new CreationFactory();//cProd.getCreationFactory();
//								ConfigManager conMan = new ConfigManager();
//								fact.setConfigManager(conMan);
								//if (fact != null)
								{
									try {
										//fact = new ICreationFactoryProxy((Dispatch)fact);
										//Object disp = fact.retrieveMetaType(indDefName, null);
										tempDisp = ret.createType(indDefName, null);
										
										//using the newDef, find the right class for this object and create it
										if (tempDisp instanceof INamedElement)
										{
											String id = newDef.getID();
											if (id != null && id.length() > 0)
											{
												try
												{
													Class clazz = Class.forName(id);
													//tempDisp = (clazz)tempDisp;
												}
												catch (Exception e1)
												{
													// TODO Auto-generated catch block
													e1.printStackTrace();
												}
											}
										}
										//tempDisp = disp;
									}
									catch (Exception e)
									{
										e.printStackTrace();
									}
								}
							}

							// blanking out the model element that the manager knows about will allow us to
							// create a dummy structure
							m_PropEleMgr.setModelElement(null);
							m_PropEleMgr.setCreateSubs(true);
							newPropEle = m_PropEleMgr.buildTopPropertyElement(newDef);
							if (newPropEle != null)
							{
								// now temporarily store the fake new model element on this new property
								// element so that the filter will work
								newPropEle.setElement(tempDisp);
								//newPropEle = m_PropEleMgr.buildElement(tempDisp, newDef, null);
								//newPropEle = m_PropEleMgr.buildElement(null, newDef, null);

								// have built the property element, so set it as modified and add it to the collection
								// of property elements
								newPropEle.setModified(true);
								
								if (filter)
								{
									// now add this new one to the collection
									colEle.addSubElement(newPropEle);

									// now that we have built the property element structure
									// check the definition filter to see if any of the elements need to
									// change
									filterPropertyElement(newPropEle);
								}

								// done with the filtering, so set its model element back to 0 so that
								// the saves, creates will work properly
								newPropEle.setElement(null);
								newEle = newPropEle;
							}
						}
						// reset the model element
						m_PropEleMgr.setModelElement(mainEle);
					}
				}
			}
		}
		
		return newEle;
	}

	/**
	 * Shortcut method to retrieve the data from the grid cell which is in the form of a IPropertyElement
	 *
	 * @param[in] row     The row to retrieve the property element from
	 * @param[out] pEle   The found property element
	 *
	 * @return HRESULT
	 *
	 */
	public IPropertyElement getElementAtGridRow(int row)
	{
		IPropertyElement retEle = null;
		
		if (row > 0 && m_Tree != null)
		{
			JTree tree = m_Tree.getTree();
			TreePath path = tree.getPathForRow(row);
			if (path != null)
			{
				Object obj = path.getLastPathComponent();
				if (obj instanceof JDefaultMutableTreeNode)
				{
					JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)obj;
					retEle = (IPropertyElement)node.getUserObject();
				}
			}
		}
		return retEle;
	}
	
	public JDefaultMutableTreeNode getNodeAtGridRow(int row)
	{
		JDefaultMutableTreeNode retNode = null;
		
		if (row >= 0 && m_Tree != null)
		{
			JTree tree = m_Tree.getTree();
			TreePath path = tree.getPathForRow(row);
			if (path != null)
			{
				Object obj = path.getLastPathComponent();
				if (obj instanceof JDefaultMutableTreeNode)
				{
					retNode = (JDefaultMutableTreeNode)obj;
				}
			}
		}
		return retNode;
	}
	
	public void setCurrentRow(int row)
	{
		//save the last selected row as the last row and store the new value
		if (m_CurRow == 0)
		{
			m_LastRow = row;
		}
		else
		{
			m_LastRow = m_CurRow;
		}
		
		m_CurRow = row;
	}
	
	/**
	 * Refresh the element in the grid.  This will remove any unwanted nodes and then add any others.
	 *
	 * @param[in] row        The row to refresh
	 * @param[in] propEle    The property element associated with the row to be refreshed
	 *
	 * @return HRESULT
	 *
	 */
	public void refreshElementInGrid(int row, IPropertyElement pEle)
	{
		if (m_Tree != null)
		{
			// get the grid node at the passed in row
			JDefaultMutableTreeNode node = getNodeAtGridRow(row);
			if (node != null)
			{
				// get all of the children of this grid node and remove them from the bottom up
				node.removeAllChildren();
				
				// now process the property element
				// get the corresponding definition
				buildSubElementsThatNeedToDisplay(pEle, node);
			}
		}
	}
	
	public void onKeyDownGrid(KeyEvent e)
	{
      if (m_Tree != null)
      {
   		int row = m_CurRow;
   		int keyCode = e.getKeyCode();
   		if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_ESCAPE)
   		{
     			m_Tree.editingCanceled(null);
   			//keep the focus on this row.
   			startEditingRowAt(row);
   		}
   		else if (keyCode == KeyEvent.VK_TAB)
   		{
   			try
   			{
   				if (e.isShiftDown())
   				{
   					startEditingRowAt(row-1);
   				}
   				else
   				{
   					startEditingRowAt(row+1);
   				}
   			}
   			catch (Exception exc)
   			{
   			}
   		}
   		else if (e.getKeyCode() == KeyEvent.VK_UP)
   		{
   			try
   			{
   				startEditingRowAt(row-1);
   			}
   			catch (Exception exc)
   			{
   			}
   		}
   		else if (e.getKeyCode() == KeyEvent.VK_DOWN)
   		{
   			try
   			{
   				startEditingRowAt(row+1);
   			}
   			catch (Exception exc)
   			{
   			}
   		}
   		else if (keyCode == KeyEvent.VK_LEFT)
   		{
   			IPropertyElement pEle = getElementAtGridRow(row);
   			if (pEle != null)
   			{
   				IPropertyDefinition pDef = pEle.getPropertyDefinition();
   				if (pDef != null)
   				{
   					String cType = pDef.getControlType();
   					if (cType == null || (cType != null && cType.equals("read-only")) )
   					{
     						m_Tree.editingCanceled(null);
   						//collapse the row
   						JDefaultMutableTreeNode node = getNodeAtGridRow(row);
   						if (node != null)
   						{
   							node.setExpanded(false);
   							m_Model.expand(row, false);
   						}
   						// tried this, but it made the property editor totally lose focus on the current cell
   						// so editCellAt is not ideal, but is better than what we got (you lose the highlight
   						// of the current cell, but if you arrow around, you get it back
   						//e.consume();
   						//startEditingRowAt(row);
   						m_Tree.editCellAt(row, 2, e);
   					}
   				}
   			}
   		}
   		else if (keyCode == KeyEvent.VK_RIGHT)
   		{
   			IPropertyElement pEle = getElementAtGridRow(row);
   			if (pEle != null)
   			{
   				IPropertyDefinition pDef = pEle.getPropertyDefinition();
   				if (pDef != null)
   				{
   					String cType = pDef.getControlType();
   					if (cType == null || (cType != null && cType.equals("read-only")) )
   					{
   						m_Tree.editingCanceled(null);
   			
   						//expand the row
   						JDefaultMutableTreeNode node = getNodeAtGridRow(row);
   						if (node != null)
   						{
   							onNodeExpanding(row);
   							node.setExpanded(false);
   							m_Model.expand(row, true);
   						}
   						// tried this, but it made the property editor totally lose focus on the current cell
   						// so editCellAt is not ideal, but is better than what we got (you lose the highlight
   						// of the current cell, but if you arrow around, you get it back
   						//e.consume();
   						//startEditingRowAt(row);
   						m_Tree.editCellAt(row, 2, e);
   					}
   				}
   			}
   		}
   		else if (keyCode == KeyEvent.VK_INSERT)
   		{
   			IPropertyElement pEle = getElementAtGridRow(row);
   			onPopupCreate(row, pEle);
   		}
   		else if (keyCode == KeyEvent.VK_DELETE)
   		{
   			IPropertyElement pEle = getElementAtGridRow(row);
   			
   			//set the rightClick row to this row number
   			m_RightClickRow = row;
   			onPopupDelete();
   		}
      }
	}
	
	public void editNextRow()
	{
		startEditingRowAt(m_CurRow+1);
	}
	
	public void startEditingRowAt(int row)
	{
		try
		{
         if (m_Tree != null)
         {
   			m_Tree.editingCanceled(null);
   			m_Tree.editCellAt(row, 2);
   			TableCellEditor editor = m_Tree.getCellEditor(row, 2);
   			if (editor != null)
   			{
   				Object obj = getEditingComponent();
   				if (obj != null && obj instanceof Component)
   				{
   					((Component)obj).requestFocus();
   					if (obj instanceof JTextField)
   					{
   						((JTextField)obj).selectAll();
   					}
   					else if (obj instanceof JComboBox)
   					{
   						Component comp = ((JComboBox)obj).getEditor().getEditorComponent();
   						if (comp != null && comp instanceof JTextField)
   						{
   							((JTextField)comp).requestFocus();
   							((JTextField)comp).selectAll();
   						}
   					}
   				}
   			}
   			setCurrentRow(row);
   			m_Tree.getSelectionModel().setSelectionInterval(row, row);
   		}
      }
		catch (Exception exc)
		{
		}
	}

	private void refreshRootNode(IPropertyElement pEle, JDefaultMutableTreeNode root)
	{
		if (m_Tree != null)
		{
			if (root != null)
			{
				// get all of the children of this grid node and remove them from the bottom up
				root.removeAllChildren();
				
				// now process the property element
				// get the corresponding definition
				buildSubElementsThatNeedToDisplay(pEle, root);
				populateGrid();
			}
		}
	}

	private void refreshRootNodeAfterFilter(IPropertyElement pEle)
	{
		if (m_Tree != null)
		{
			if (m_root != null)
			{
				//We can reach here only if there is only one element being shown
				JDefaultMutableTreeNode rootNode = (JDefaultMutableTreeNode)m_root.getChildAt(0);
				// get all of the children of this grid node and remove them from the bottom up
				rootNode.removeAllChildren();
				
				Object obj = rootNode.getUserObject();
				if (obj != null && obj instanceof IPropertyElement)
				{
					IPropertyElement rootEle = (IPropertyElement)obj;

					//remove all existing subElements
					rootEle.setSubElements(new Vector<IPropertyElement>());

					Vector<IPropertyElement> propElems = new Vector<IPropertyElement>();
					Vector<IPropertyDefinition> propDefs = new Vector<IPropertyDefinition>();
					
					// now process the property element
					// get the corresponding definition
					//buildSubElementsThatNeedToDisplay(pEle, m_root);
					
					if (rootEle.equals(pEle))
					{
						if (m_CurLoadedObject != null && 
							m_CurLoadedObject instanceof IElement)
						{
							loadElement((IElement)m_CurLoadedObject);
						}
					}
					else
					{
						//we have just built a small collection node
						rootEle.addSubElement(pEle);
						propElems.add(rootEle);
						setPropertyElements(propElems);
						//populateGrid();

						//at this point I want to build the node and expand it.
						Vector subElems = pEle.getSubElements();

						//we want to expand the child node - this will build it if the
						//property element is not built already.
						onNodeExpanding(1);

						m_Model.expand(1, true);
						refresh();
					}
				}
			}
		}
	}

	/**
	 * 
	 */
	public void refresh()
	{
		Graphics g = this.getGraphics();
		if (g != null)
		{
			this.paintAll(g);
		}
	}
	
	/**
	 * Called when dialog is initialized
	 *
	 * @param[in] uMsg
	 * @param[in] wParam
	 * @param[in] lParam
	 * @param[out] bHandled
	 *
	 * @return LRESULT
	 *
	 */
	public void onInitDialog()
	{
		if (m_Factory == null)
		{
			m_Factory = new PropertyDefinitionFactory();
		}
		
		if (m_Factory != null)
		{
			ICoreProduct prod = CoreProductManager.instance().getCoreProduct();
			IConfigManager conMan = prod.getConfigManager();
			if (conMan != null)
			{
				String home = conMan.getDefaultConfigLocation();
				String file = home + "PropertyDefinitions.etc"; //$NON-NLS-1$
				m_Factory.setDefinitionFile(file);
			}
		}
		if (m_PropEleMgr == null)
		{
			m_PropEleMgr = getPropertyElementManager();
		}
		
		m_PropEleMgr.setPDFactory(m_Factory);
		m_ResourceMgr = CommonResourceManager.instance();
		
//		m_ReadOnlyColor = RGB(232, 228, 232);
//		m_ReadOnlyColor2 = RGB(192, 192, 192);
//		m_EditableColor = GetSysColor(COLOR_WINDOW);
		
		m_Filter = new PropertyEditorFilter();
		m_Filter.build();
		
		// help box
		initializeHelpBox();
		
		// buttons
		initializeButtons();

		// grid stuff
		initializeGrid();
		// combo box, must be after the grid, because we use the mode which is determined
		  // in the grid stuff
		initializeComboBox();
		  // fill in the grid
		populateGrid();
		
		// Initialize tooltips for grid
		m_ToolTip = createToolTip();
		
	}
	
	/**
	 * Called when the navigate popup menu is clicked upon
	 *
	 * @param[in] wNotifyCode
	 * @param[in] wID
	 * @param[in] hWndCtrl
	 * @param[out] bHandled
	 *
	 * @return LRESULT
	 *
	 */
	public void onPopupNavigate()
	{
		if (m_Model != null)
		{
			IPropertyElement pEle = getElementAtGridRow(m_CurRow);
			if (pEle != null)
			{
				Object pDisp = pEle.getElement();
				if (pDisp != null)
				{
					if (pDisp instanceof IReference)
					{
						IReference pReference = (IReference)pDisp;
						String name = pEle.getName();
						IElement pElement2 = null;
						if (name.equals("ReferredElement")) //$NON-NLS-1$
						{
							pElement2 = pReference.getReferredElement();
						}
						else if (name.equals("ReferencingElement")) //$NON-NLS-1$
						{
							pElement2 = pReference.getReferencingElement();
						}
						
						if (pElement2 != null)
						{
							IFindController controller = new FindController();
							controller.navigateToElement(pElement2);
						}
					}
					else if (pDisp instanceof IElement)
					{
						IElement pElement = (IElement)pDisp;
						IFindController controller = new FindController();
						controller.navigateToElement(pElement);
					}
					else if (pDisp instanceof IProxyDiagram)
					{
						IProxyDiagram pDiagram = (IProxyDiagram)pDisp;
						IProductDiagramManager pManager = ProductHelper.getProductDiagramManager();
						if (pManager != null)
						{
							IDiagram dia = pManager.openDiagram2(pDiagram, true, null);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Called when the fill down popup menu is clicked upon.  This takes the value in the first
	 * selected row and changes the information of all of the other rows that are selected to
	 * the value in the first row.
	 *
	 * @param[in] wNotifyCode
	 * @param[in] wID
	 * @param[in] hWndCtrl
	 * @param[out] bHandled
	 *
	 * @return LRESULT
	 *
	 */
	public void onPopupFill()
	{
		if (m_Model != null && m_Tree != null)
		{
			PropertyEditorBlocker.block();
			try
			{
				// need a temporary array of property elements
				Vector<IPropertyElement> temp = new Vector<IPropertyElement>();
			
				boolean haveValue = false;
				// having problems with the GetSelectedRows method, so we are just going
				// to loop through all of the rows in the grid and see if they are selected
				int countRows = m_Tree.getRowCount();
				String eleValue = null;
				String textValue = null;
			
				for (int i=1; i<countRows; i++)
				{
					boolean selected = true;//m_Tree.isRowSelected(i);
					if (selected)
					{
						IPropertyElement pEle = getElementAtGridRow(i);
						if (pEle != null)
						{
							if (!haveValue)
							{
								// if we don't have a value yet, this must be the first one selected, so
								// we want to get its information to use for the value to set the rest of the
								// selected rows to
								// get the information from both the element and the grid
								// because they could be different (stored vs displayed values)
								eleValue = pEle.getValue();
								textValue = (String)m_Tree.getValueAt(i, 2);
								haveValue = true;
							}
							else
							{
								// if we already have a value, and the item is selected, then this is an
								// element that we want to "fill" or set its value

								// we don't want to process a top level node, if it is selected, so check
								IPropertyElement parEle = pEle.getParent();
								if (parEle != null)
								{
									// set that it is being modified, set its value, and set the grid text
									m_Tree.setValueAt(textValue, i, 2);
									updatePropertyElementValue(i, pEle, textValue);
									temp.add(pEle);
								}
							}
						}
					}
				}
				// we have set the information in the grid, now need to set it in the xml
				int count = temp.size();
				for (int j=0; j<count; j++)
				{
					IPropertyElement ele = temp.get(j);
					IPropertyDefinition def = ele.getPropertyDefinition();
					if (def != null)
					{
						setRowData(j, def, ele);
					}
				}
				m_Tree.updateUI();
			}
			finally
			{
				PropertyEditorBlocker.unblock();
			}
		}
	}
	
	/**
	 * Called before a grid cell is left - grid event
	 *
	 * @return VOID
	 *
	 */
	public void onLeaveCellGrid()
	{
		if (m_Model != null && m_Tree != null)
		{
			m_LastRow = m_Tree.getSelectedRow();
		}
	}
	
	/**
	 * Called when the associate popup menu is clicked upon
	 *
	 * @param[in] wNotifyCode
	 * @param[in] wID
	 * @param[in] hWndCtrl
	 * @param[out] bHandled
	 *
	 * @return LRESULT
	 *
	 */
	public void onPopupAssociate()
	{
		Vector<IElement> tempElements = new Vector<IElement>();
		IPropertyElement pEle = getElementAtGridRow(m_CurRow);
		if (pEle != null)
		{
			Object pDisp = pEle.getElement();
			if (pDisp != null && pDisp instanceof IElement)
			{
				IElement pElement = (IElement)pDisp;
				tempElements.add(pElement);
				
				//AssociateDialog dialog = new AssociateDialog();
				//dialog.setElements(tempElements);
				//dialog.doModal();
			}
		}
	}
	
	/**
	 * Some of the picklists used by the property editor are common and are built
	 * from xpath queries.  This routine executes the xpath query and then gets the 
	 * name from each element and stores it in a list buffer.
	 * 
	 *
	 * @param pEle[in]		The current property element
	 *
	 * @return HRESULT
	 */
	public String buildDataTypeList(IPropertyElement pEle)
	{
		if (m_ListBuffer == null || m_ListBuffer.length() == 0)
		{
			boolean useQName = getDataTypePreference();
	
			// need to block events here because we may be opening up projects in here
			// to populate the datatype lists
			PropertyEditorBlocker.block();
			try
			{
				IPropertyDefinition pDef = pEle.getPropertyDefinition();
				String str = null;
				if (pDef != null)
				{
					// get_validvalues3 uses the picklist manager
					IStrings strs = pDef.getValidValue(pEle);
					if (strs != null)
					{
						TreeMap <String, TreeMap<String, String> > theMap = new TreeMap<String, TreeMap<String, String> >(); 
						String empty = ""; //$NON-NLS-1$
						String full = null;
						int count = strs.getCount();
						for (int i=0; i<count; i++)
						{
							String temp = strs.item(i);
							if (useQName && temp.length() > 0)
							{
								full = temp;
								int pos = full.lastIndexOf(':');
								if (pos >= 0)
								{
									temp = full.substring(pos + 1);
									empty = full.substring(0, pos-1);
								}
								else
								{
									empty = ""; //$NON-NLS-1$
								}
							}
							String lower = temp.toLowerCase();
							
							TreeMap inner = theMap.get(lower);
							if (inner != null)
							{
								String found = (String)inner.get(full);
								if (found == null || found.length() == 0)
								{
									inner.put(full, temp);
								}
							}
							else
							{
								inner = new TreeMap<String, String>();
								inner.put(full, temp);
								theMap.put(lower, inner);
							}
						}
						
						Collection col = theMap.values();
						Object[] objs = col.toArray();
						int cnt = objs.length;
						for (int x = 0; x < cnt; x++)
						{
							Object obj = objs[x];
							if (obj instanceof TreeMap)
							{
								TreeMap hm = (TreeMap)obj;
								Collection col2 = hm.values();
								Object[] objs2 = col2.toArray();
								int cnt2 = objs2.length;
								for (int x2 = 0; x2 < cnt2; x2++)
								{
									Object obj2 = objs2[x2];
									if (obj2 instanceof String)
									{
										String temp = (String)obj2;
										if (str == null)
										{
											str = ""; //$NON-NLS-1$
										}
										if (str.length() > 0)
										{
											str += "|"; //$NON-NLS-1$
										}
										str += temp;
										if (useQName)
										{
											//I am commenting out right now, 
											//need to uncomment later when I can handle
											//popup display with table format.
											//str += "\t";
											//str += temp;
										}
									}
								}
							}
						}
					}
				}
			
				if (str != null)
				{
					m_ListBuffer = "|"; //$NON-NLS-1$
					m_ListBuffer += str;
				}
			}
			finally
			{
				PropertyEditorBlocker.unblock();
			}
		}
		return m_ListBuffer;
	}

	/**
	 * Called when the insert popup menu is clicked upon
	 *
	 * @param[in] wNotifyCode
	 * @param[in] wID
	 * @param[in] hWndCtrl
	 * @param[out] bHandled
	 *
	 * @return LRESULT
	 *
	 */
	public void onPopupInsert()
	{
		JDefaultMutableTreeNode pBeforeNode = getNodeAtGridRow(m_CurRow);
		if (pBeforeNode != null)
		{
			// create an empty property element that is being inserted
			IPropertyElement pNewPropEle = createEmptyPropertyElement(m_CurRow, false);
			if (pNewPropEle != null)
			{
				pNewPropEle.setModified(true);

				// get the property element at the row that we are on, because we have right clicked
				// and said to insert before, we need the element that we are on to get the collection
				// element, so that we can rebuild the collection elements
				IPropertyElement pPropEle = (IPropertyElement)pBeforeNode.getUserObject();
				if (pPropEle != null)
				{
					// get the collection element
					IPropertyElement colEle = getCollectionElement(pPropEle);
					if (colEle != null)
					{
						//
						// reorder the elements in the collection element
						//
						Vector<IPropertyElement> temp = new Vector<IPropertyElement>();
						Vector<IPropertyElement> subEles = colEle.getSubElements();
						int count = subEles.size();
						for (int i=count-1; i>=0; i--)
						{
							IPropertyElement subEle = subEles.elementAt(i);
							// add it to our temp array
							temp.add(subEle);
							// remove it from the collection element
							subEles.removeElementAt(i);
						}
						
						// now loop through our temp array
						count = temp.size();
						for (int i=0; i<count; i++)
						{
							// if the current sub element in our temp array is the same
							// as the new element we just created, then we need to insert
							// the new one before the current one
							// otherwise add the current one
							IPropertyElement subEle = temp.elementAt(i);
							if (subEle.equals(pPropEle))
							{
								colEle.addSubElement(pNewPropEle);
								colEle.addSubElement(subEle);
							}
							else
							{
								colEle.addSubElement(subEle);
							}
						}
					}
				}
				
				//
				// Now we should have the sub elements in the right order
				//
				IPropertyDefinition pDef = pNewPropEle.getPropertyDefinition();

				// the following code is from AddSubElementToGrid
				// but that routine was doing more than we wanted, so it should
				// be refactored, but for proof of concept we duplicated it here
				// add the node to the grid
				String displayName = pDef.getDisplayName();
				m_PropEleMgr.interpretElementValue(pNewPropEle);
				String value = pNewPropEle.getValue();
				
				JDefaultMutableTreeNode newNode = new JDefaultMutableTreeNode();
				newNode.setKey("RecordNew"); //$NON-NLS-1$
				int row = newNode.getRow();
				pBeforeNode.add(newNode);
				
				//set the value
				m_Tree.setValueAt(displayName, row-1, 2);
				m_Tree.setValueAt(value, row, 2);
				
				// set the behind the scenes data to the property element
				updatePropertyElementValue(row, pNewPropEle);
				
				// set the color of the grid cell
				boolean readOnlyStatus = isReadOnlyStatus(pDef, pNewPropEle);
				
				// set the boldness of the font of the grid cell
				boolean boldness = isBold(pDef, pNewPropEle);
				
				addSubElementToTable(newNode, pDef, pNewPropEle, false);
				
				// expand this last child
				JTreeTable.TreeTableCellRenderer tree = m_Tree.getTree();
				tree.expandNode(row, true);
				newNode.setExpanded(true);
				
				//set focus to the editable cell on the first node.
			}
		}
	}

	/**
	 * Used to hide/show the property editor element's subElements.
	 */
	public void sortPropertyEditorElements(int row)
	{
		//ETSystem.out.println("In sortPropertyElements for the row " + row);
		//We are builing root and not displaying it, so I need to user
		// the first child of the root.
		if (m_root == null)
		{
			return;
		}
		
		//We do not want to do anything if there are more than one items selected.
		int childCount = m_root.getChildCount();
		if (childCount != 1)
		{
			JDefaultMutableTreeNode node = getNodeAtGridRow(row);
			
			//just make sure we have it expanded, if it was expanded
			if (node.isExpanded())
			{
				m_Model.expand(row, true);
			}
			return;
		}
		
		JDefaultMutableTreeNode node = (JDefaultMutableTreeNode)m_root.getChildAt(0);
		Object obj = node.getUserObject();
		if (obj != null && obj instanceof IPropertyElement)
		{
			IPropertyElement ele = (IPropertyElement)obj;
			if (row == 0)
			{
				//we user has clicked onto the icon to toggle show all vs show selected
				//so check which mode we are in right now and then toggle to the other one
				if (m_IsShowingSelected)
				{
					m_IsShowingSelected = false;
				
					//show all elements
					refreshRootNodeAfterFilter(ele);
				}
				else
				{
					//do nothing as we are already showing all the elements.
					//just make sure we have it expanded, if it was expanded
					m_Model.expand(row, true);
				}
			}
			else
			{
				if (m_IsShowingSelected)
				{
					//do nothing
				}
				else
				{
					
					//show the selected element now. First need to find which
					Vector<Icon> icons = m_Model.getIcons();
					if (icons != null && icons.size() > 0 )
					{
						int count = icons.size();
						if (count > row)
						{
							m_IsShowingSelected = true;
							IPropertyElement selEle = m_LoadedImages.elementAt(row);
							if (selEle != null)
							{
								//ETSystem.out.println("Going to sort for the element " + selEle.getName());
								refreshRootNodeAfterFilter(selEle);
							}
						}
					}
				}
			}
		}
	}
	
	public boolean isShowingFilteredOnIcons()
	{
		return m_IsShowingSelected;
	}
	
	public boolean isShowingComboFilteredList()
	{
		return m_Mode == EDITOR_SEL ? true : false;
	}

	/**
	 * This method should save the value when it gets changed.
	 * @param arg0
	 */
	public void columnValueChanged(PropertyChangeEvent e)
	{
		try {
			Object obj = e.getSource();
			String str = e.getPropertyName();
			if (str.equals("ancestor")) //$NON-NLS-1$
			{
				if (!focusChange)
				{
					JDefaultMutableTreeNode node = getNodeAtGridRow(m_CurRow);
					IPropertyElement ele = getElementAtGridRow(m_CurRow);
					
					JDefaultMutableTreeNode recNode = getGridNodeMarkedAsRecord(node);
					
					String text = null;
					if (obj instanceof JTextField)
					{
						JTextField field = (JTextField)obj;
						text = field.getText();
					}
					else if (obj instanceof JComboBox)
					{
						JComboBox field = (JComboBox)obj;
						text = (String)field.getSelectedItem();
					}
					ETSystem.out.println("New value = " + text); //$NON-NLS-1$
					if (ele != null)
					{
						String value = ele.getValue();
						if (value == null && (text == null || text.length() == 0))
						{
							//do nothing
						}
						else if ( (value == null && text != null && text.length() > 0) 
								|| !value.equals(text))
						{
							ele.setModified(true);
							if (obj instanceof JComboBox)
							{
								ConfigStringTranslator trans = new ConfigStringTranslator();
								IPropertyDefinition pDef = ele.getPropertyDefinition();
								text = trans.translateIntoPSK(pDef, text);
							}
							ele.setValue(text);

							if (recNode != null)
							{
								//just set this node as modified
								Object userObj = recNode.getUserObject();
								if (userObj != null && userObj instanceof IPropertyElement)
								{
									((IPropertyElement)userObj).setModified(true);
								}
								m_processingRecord = true;
							}
							else
							{
								if (m_processingRecord)
								{
									//need to save the old record
									insert(recNode);
									m_processingRecord = false;
								}
								else
								{
									saveCellValueAt(m_CurRow, text);
								}
							}
						}
					}
				}
				else
				{
					focusChange = false;
				}
			}
			else if (str.equals("nextFocus")) //$NON-NLS-1$
			{
				focusChange = true;
				
				//if we were working on a new record till now, need to save it.
				if (m_processingRecord)
				{
					JDefaultMutableTreeNode lastNode = getNodeAtGridRow(m_LastRow);
					JDefaultMutableTreeNode curNode = getNodeAtGridRow(m_CurRow);
					
					JDefaultMutableTreeNode recLastNode = getGridNodeMarkedAsRecord(lastNode);
					JDefaultMutableTreeNode recCurNode = getGridNodeMarkedAsRecord(curNode);
					if (recCurNode != null && recLastNode.equals(recCurNode))
					{
						//we are still working on this node.
					}
					else
					{
						insert(recLastNode);
						m_processingRecord = false;
					}
				}
			}
			
		}catch (Exception ep)
		{
			ep.printStackTrace();
		}
	}
	
	public void handleSave(boolean stopCellEditing, Object obj)
	{
		try 
		{
			if (obj == null)
			{
				if (m_Model != null)
				{
					obj = m_Model.getEditingComponent();
				}
				if (obj == null)
					return;
			}
			//I need to figure out if I have lost focus to a 
			//different component, in which case I need to use the
			//m_CurRow and not m_LastRow, I can do this using the 
			//other component on focus event
			int rowToUse = m_CurRow;
			if (!stopCellEditing)
			{
				//we lost focus to some other component.
				rowToUse = m_LastRow;
			}

			JDefaultMutableTreeNode node = getNodeAtGridRow(rowToUse);
			IPropertyElement ele = getElementAtGridRow(rowToUse);
			
			JDefaultMutableTreeNode recNode = getGridNodeMarkedAsRecord(node);
			
			String text = null;
			if (obj instanceof JTextField)
			{
				JTextField field = (JTextField)obj;
				text = field.getText();
			}
			else if (obj instanceof JComboBox)
			{
				JComboBox field = (JComboBox)obj;
				boolean editable = field.isEditable();
				if (editable)
				{
					text = (String)field.getEditor().getItem();
				}
				else
				{
					text = (String)field.getSelectedItem();
				}
			}
			
			if (ele != null)
			{
				String value = ele.getValue();
                                //Samaresh: fix for #5078895.
                                //Somehow I feel that the if condition 8 lines below
                                //if (value == null && (text == null || text.length() == 0) )
                                //should instead be
                                //if (value != null && (text == null || text.length() == 0) )
                                if ( (value != null) && (text == null || text.length() == 0) ) {
                                    return;
                                }
                                
				if (value == null && (text == null || text.length() == 0))
				{
					//do nothing
					// we have a bug 2023 where if you enter an operation name and arrow down to alias
					// and then switch elements in the project tree, the operation was not getting saved
					// because we were going into this if statement
					// so now we are going to check if we are in a record node (just like down below)
					// this is pretty touchy code, so we just copied it rather than add to the if stmt
					// so as not to break other stuff
					if (recNode != null)
					{
						//the property element at last row was not modified 
						//but if we were in a record new mode, we should process last node.
						node = getNodeAtGridRow(m_CurRow);
						while (recNode != null)
						{
							//if the element selection has changed or some other element is being loaded.
							if (insertNecessary(node, recNode) || stopCellEditing == false)
							{
								insert(recNode);
							}
							JDefaultMutableTreeNode parentNode = (JDefaultMutableTreeNode)recNode.getParent();
							recNode = getGridNodeMarkedAsRecord(parentNode);
						}
					}
					
				}
				else if ( (value == null && text != null && text.length() > 0) 
						|| (value != null && text != null && !value.equals(text)) )
				{
					ele.setModified(true);
					if (obj instanceof JComboBox)
					{
						if (!((JComboBox)obj).isEditable())
						{
							ConfigStringTranslator trans = new ConfigStringTranslator();
							IPropertyDefinition pDef = ele.getPropertyDefinition();
							text = trans.translateIntoPSK(pDef, text);
						}
					}
					ele.setValue(text);

					if (recNode != null)
					{
						//just set this node as modified
						Object userObj = recNode.getUserObject();
						if (userObj != null && userObj instanceof IPropertyElement)
						{
							((IPropertyElement)userObj).setModified(true);
						}
						m_processingRecord = true;
						m_RecordNode = recNode;
					}
					
					//we want to save the cell value at this last row - it will create
					//an element if the record element is not yet created.
					//saveCellValueAt(m_LastRow, text);
					if (stopCellEditing)
					{
						processLastCell(false, rowToUse);
					}
					else
					{
						//the focus has been lost to some other component so need to insert.
						processLastCell(true, rowToUse);
					}
				}
				else if (recNode != null)
				{
					//the property element at last row was not modified 
					//but if we were in a record new mode, we should process last node.
					node = getNodeAtGridRow(m_CurRow);
					while (recNode != null)
					{
						//if the element selection has changed or some other element is being loaded.
						if (insertNecessary(node, recNode) || stopCellEditing == false)
						{
							insert(recNode);
						}
						JDefaultMutableTreeNode parentNode = (JDefaultMutableTreeNode)recNode.getParent();
						recNode = getGridNodeMarkedAsRecord(parentNode);
					}
				}
			}
	
		}catch (Exception ep)
		{
			ep.printStackTrace();
		}
	}

	public void handleFocusLostOnCellEvent(FocusEvent e)
	{
		try {
			Object obj = e.getSource();

			//I need to figure out if I have lost focus to a 
			//different component, in which case I need to use the
			//m_CurRow and not m_LastRow, I can do this using the 
			//other component on focus event
			Component oComp = e.getOppositeComponent();
			int rowToUse = m_LastRow;
			if (oComp instanceof JPropertyTreeTable)
			{
				//we are still in the property editor, so no probs.
			}
			else
			{
				rowToUse = m_CurRow;
			}

			JDefaultMutableTreeNode node = getNodeAtGridRow(rowToUse);
			IPropertyElement ele = getElementAtGridRow(rowToUse);
			
			JDefaultMutableTreeNode recNode = getGridNodeMarkedAsRecord(node);
			
			
			String text = null;
			if (obj instanceof JTextField)
			{
				JTextField field = (JTextField)obj;
				text = field.getText();
			}
			else if (obj instanceof JComboBox)
			{
				JComboBox field = (JComboBox)obj;
				text = (String)field.getSelectedItem();
			}
			if (ele != null)
			{
				String value = ele.getValue();
				if (value == null && (text == null || text.length() == 0))
				{
					//do nothing
				}
				else if ( (value == null && text != null && text.length() > 0) 
						|| (value != null && text != null && !value.equals(text)) )
				{
					ele.setModified(true);
					if (obj instanceof JDescribeComboBox)
					{
						//I do not want to translate this value
					}
					else if (obj instanceof JComboBox)
					{
						ConfigStringTranslator trans = new ConfigStringTranslator();
						IPropertyDefinition pDef = ele.getPropertyDefinition();
						text = trans.translateIntoPSK(pDef, text);
					}
					ele.setValue(text);

					if (recNode != null)
					{
						//just set this node as modified
						Object userObj = recNode.getUserObject();
						if (userObj != null && userObj instanceof IPropertyElement)
						{
							((IPropertyElement)userObj).setModified(true);
						}
						m_processingRecord = true;
						m_RecordNode = recNode;
					}
					
					//we want to save the cell value at this last row - it will create
					//an element if the record element is not yet created.
					//saveCellValueAt(m_LastRow, text);
					if (oComp instanceof JPropertyTreeTable)
					{
						processLastCell(false, rowToUse);
					}
					else
					{
						//the focus has been lost to some other component so need to insert.
						processLastCell(true, rowToUse);
					}
				}
				else if (recNode != null)
				{
					//the property element at last row was not modified 
					//but if we were in a record new mode, we should process last node.
					node = getNodeAtGridRow(m_CurRow);
					while (recNode != null)
					{
						//if the element selection has changed or 
						if (insertNecessary(node, recNode))
						{
							insert(recNode);
						}
						JDefaultMutableTreeNode parentNode = (JDefaultMutableTreeNode)recNode.getParent();
						recNode = getGridNodeMarkedAsRecord(parentNode);
					}
					
				}
			}
	
		}catch (Exception ep)
		{
			ep.printStackTrace();
		}
	}
	
	public void handleFocusGainedOnCellEvent(FocusEvent e)
	{
		try {
			ETSystem.out.println("Focus Gained m_CurRow = " + m_CurRow + " m_LastRow = " + m_LastRow); //$NON-NLS-1$ //$NON-NLS-2$
//			Object obj = e.getSource();
//			JDefaultMutableTreeNode node = getNodeAtGridRow(m_CurRow);
//			IPropertyElement ele = getElementAtGridRow(m_CurRow);
//			
//			JDefaultMutableTreeNode recNode = getGridNodeMarkedAsRecord(node);
//			
//			if (ele != null)
//			{
//				if (recNode == null && m_processingRecord)
//				{
//					node = getNodeAtGridRow(m_LastRow);
//					recNode = getGridNodeMarkedAsRecord(node);
//					if (recNode != null)
//					{
//						IPropertyElement pEle = (IPropertyElement)recNode.getUserObject();
//						ETSystem.out.println("I need to save the record for " + pEle.getName());
//						//saveCellValueAt(m_LastRow, text);
//						processLastCell(false);
//					}
//					m_processingRecord = false;
//				}
//			}
		}catch (Exception ep)
		{
			ep.printStackTrace();
		}
	}
	
	/*
	 * This method is called when a tree node in the property editor is 
	 * double clicked. It will expand the node or collapse it.
	 * If the node's property element is not built yet and the multiplicity
	 * is > 1, this method builds the property element and expands it.
	 */
	public void handleDoubleClick()
	{
      if (m_Tree != null)
      {
   		int row = m_Tree.getSelectedRow();
   		handleDoubleClick(row, null);
      }
	}
	public void handleDoubleClick(int row, TreePath selPath)
	{
		JDefaultMutableTreeNode node = getNodeAtGridRow(row);
		if (node == null)
			return;
			
		if (node.isExpanded() )//|| m_Tree.getTree().isExpanded(selPath))
		{
			node.setExpanded(false);
			m_Model.expand(row, false);
		}
		else 
		{
			int count = node.getChildCount();
			
			//if the child count is 0, we need to check if the element is
			// not built yet - like for attributes, operations etc.
//			if (count == 0)
			{
            	onNodeExpanding(row);
			}
         	m_Model.expand(row, true);
			node.setExpanded(true);
		}
	}
	
	/*
	 * Nodes with onDemand true or with multiplicity > 1 may not be built
	 * at first load of an element. This method builds those.
	 */
	private void buildAndExpandNode(int row)
	{
		IPropertyElement pEle = getElementAtGridRow(row);
		JDefaultMutableTreeNode node = getNodeAtGridRow(row);
		if (pEle != null)
		{
			IPropertyDefinition pDef = pEle.getPropertyDefinition();
			if (pDef != null)
			{
				long mult = pDef.getMultiplicity();
				boolean isOnDemand = pDef.isOnDemand();
				if (mult > 1)
				{
					buildForMutiplicityNode(pEle, node);
				}
				else
				{
					buildOnDemandNode(pEle, node);
				}
			}
		}
	}
	
	private void buildForMutiplicityNode(IPropertyElement pEle, JDefaultMutableTreeNode node)
	{
		if (pEle != null)
		{
			IPropertyDefinition pDef = pEle.getPropertyDefinition();
			if (pDef == null)
				return;
				
			IPropertyDefinition def = pDef.getSubDefinition(0);
			//This element might not have built yet, so build it
			String getMeth = pDef.getGetMethod();
			Object pDisp = pEle.getElement();
			//onNodeExpanding(row);
			if (getMeth != null && getMeth.length() > 0 && pDisp != null)
			{
				Class clazz = pDisp.getClass();
				if (pDisp instanceof IElement)
				{
					try
					{
						java.lang.reflect.Method method = clazz.getMethod(getMeth, null);
						Object obj = method.invoke(pDisp, null);
						if (obj != null)
						{
							//if this returned object has a getCount method, then this is a 
							//collection object and I want to build property element for each of
							//these collection objects.
	
							Method countMethod = obj.getClass().getMethod("getCount", null); //$NON-NLS-1$
							if (countMethod != null)
							{
								Object countResult = countMethod.invoke(obj, null);
								if (countResult != null && countResult instanceof Integer)
								{
									 int counter = ((Integer)countResult).intValue();
									 if(counter > 0)
									 {
										//Now I want to get the item method so that I can invoke 
										//it on the collections object.
										Class[] parms = new Class[1];
										parms[0] = int.class;
										Method itemMethod = obj.getClass().getMethod("item", parms); //$NON-NLS-1$
										if (itemMethod != null)
										{
											boolean onDemand = def.isOnDemand();
											for (int i=0; i<counter; i++)
											{
												Object[] itemCount = new Object[1];
												itemCount[0] = new Integer(i);
												Object itemResult = itemMethod.invoke(obj, itemCount);
																	
												if (itemResult != null)
												{
													m_PropEleMgr.setCreateSubs(true);
													IPropertyElement newPropEle = m_PropEleMgr.buildTopPropertyElement(def);
													if (newPropEle != null)
													{
														 newPropEle.setElement(itemResult);
														 //newPropEle.setPropertyDefinition(newDef);
														 
														 //I want to build this property element only if its not on demand
														 if (onDemand)
														 {
														 	//here I just want to set its name as dummy.
														 	newPropEle.setName("dummy"); //$NON-NLS-1$
														 }
														 else
														 {
															//newPropEle = m_PropEleMgr.buildElement(itemResult, newDef, null);
														 }
			
														 // now add this new one to the collection
														 pEle.addSubElement(newPropEle);
			
														 // now that we have built the property element structure
														 // check the definition filter to see if any of the elements need to
														 // change
														 filterPropertyElement(newPropEle);
													}
	
													if (newPropEle != null)
													{
														//JDefaultMutableTreeNode newNode = new JDefaultMutableTreeNode(newPropEle);
														JDefaultMutableTreeNode newNode = null;
	                                      
														// now expand and set up the node that will be added
														newNode = addSubElementToTable(node, def, newPropEle, false);
			
														//Now go through the sub elements of pEle and add those to the tree
														//buildSubElementsThatNeedToDisplay(newPropEle, newNode);
													}
												}
											}
										}
									 }
								}
							}
						}
					}
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void buildOnDemandNode(IPropertyElement pEle, JDefaultMutableTreeNode node)
	{
		Object pDisp = pEle.getElement();
		IPropertyDefinition pDef = pEle.getPropertyDefinition();
		if (pDef != null)
		{
			//set the name of the property element to proper name
			pEle.setName(pDef.getName());
			pEle = m_PropEleMgr.buildElement(pDisp, pDef, null);
			if (pEle != null)
			{
				// now that we have built the property element structure
				// check the definition filter to see if any of the elements need to
				// change
				filterPropertyElement(pEle);
				node.setUserObject(pEle);				
				JDefaultMutableTreeNode newNode = null;
				// now expand and set up the node that will be added
				newNode = addSubElementToTable(node, pDef, pEle, false);
			}
		}
	}
	
	public void setRightClickRow(int newRow)
	{
		m_RightClickRow = newRow;
	}

	/** 
	 * In order to protect this object from a reentrancy issue, we make
	 * sure that m_SigChange is set to NULL before it is destroyed
	 * 
	 */
	private void endSignatureChange()
	{
		if (m_SigChange != null)
		{
			m_SigChange.endSignatureChange();
			m_SigChange = null;
		}
	}
	
	private IOperation getOperationFromPropertyElement(IPropertyElement pEle)
	{
		IOperation retOper = null;
		if (pEle != null)
		{
			// Get the PropertyElement's IElement 
			Object pDisp = pEle.getElement();
			if (pDisp == null)
			{
				pDisp = getModelElement(pEle);
			}
			
			if (pDisp instanceof IOperation)
			{
				retOper = (IOperation)pDisp;
			}
			else if (pDisp instanceof IParameter)
			{
				IBehavioralFeature pFeature = ((IParameter)pDisp).getBehavioralFeature();
				if (pFeature != null && pFeature instanceof IOperation)
				{
					retOper = (IOperation)pFeature;
				}
			}
			else
			{
				IPropertyElement parentEle = pEle.getParent();
				if (parentEle != null)
				{
					retOper = getOperationFromPropertyElement(parentEle);
				}
			}
		}
		return retOper;
	}
	
	public void setEditingComponent(Object obj)
	{
		if (m_Model != null)
		{
			m_Model.setEditingComponent(obj);
		}
		if (obj == null && m_Tree != null)
		{
			m_Tree.editingCanceled(null);
		}
	}
	
	public Object getEditingComponent()
	{
		Object retVal = null;
		if (m_Model != null)
		{
			retVal = m_Model.getEditingComponent();
		}
		return retVal;
	}
	
	public void onFocus()
	{
      if (m_Tree != null)
      {
   		m_Tree.requestFocus();
      }
	}
}



