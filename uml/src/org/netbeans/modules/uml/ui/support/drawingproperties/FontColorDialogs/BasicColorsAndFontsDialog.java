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
 * Created on Apr 15, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.netbeans.modules.uml.ui.support.drawingproperties.FontColorDialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.commonresources.ICommonResourceManager;
import org.netbeans.modules.uml.ui.support.drawingproperties.DrawingPropertyResource;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingPropertyProvider;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProps;
import org.netbeans.modules.uml.ui.support.helpers.GUIBlocker;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker;
import org.netbeans.modules.uml.ui.swing.commondialogs.JCenterDialog;
import org.netbeans.modules.uml.ui.swing.treetable.TreeTableModel;
import org.netbeans.modules.uml.ui.support.helpers.IGUIBlocker.GBK;

/**
 * @author jingmingm
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class BasicColorsAndFontsDialog extends JCenterDialog
{
	protected JList m_EngineList = null;
	protected JComboBox m_DiagramSelection = null;
	protected FontColorTreeTable m_Properties = null;
	protected JCheckBox m_AdvancedCheck = null;
	protected JButton m_ApplyButton = null;
        protected JLabel diagramTypeLabel = null;
        protected JSplitPane splitPane = null;
	
	protected TreeTableModel model = null;
	protected String m_SetectedDiagramType = "";
	protected String m_SelectedEngine = "";
	protected int m_nDiagramKindFilterSetting = IDiagramKind.DK_ALL;
	
	protected String m_sConfigLocation = "";
	protected IGUIBlocker m_pDiagramBlocker = null;
	protected ICommonResourceManager m_ResourceMgr = null;
	protected ETList<String> m_pDrawEngines = new ETArrayList<String>();
	protected Hashtable<String, String> m_AllObjectListItems = new Hashtable<String, String>();
	protected Hashtable<Integer, ETList<String> > m_StandardDrawEngines = new Hashtable<Integer, ETList<String> >();
	protected Hashtable<String, String> m_pDrawEngineNames = new Hashtable<String, String>();
	
	protected IDiagram m_pDiagram = null;
	protected ETList<IPresentationElement> m_pPEs = null;
	
	protected ETList<IDrawingProperty> m_ChangedProperties = new ETArrayList<IDrawingProperty>();
	
	public abstract void onObjectListSelected(String sSelection);
	public abstract void onBnClickedApply();
	
	protected IDrawingProperty checkForChange(IDrawingProperty pProperty)
	{
		IDrawingProperty retVal = pProperty;
		if (m_ChangedProperties != null && pProperty != null)
		{
			for (int i = 0; i < m_ChangedProperties.size(); i++)
			{
				IDrawingProperty pDrawingProperty = m_ChangedProperties.get(i);
				if (pDrawingProperty != null && pDrawingProperty.isSame(pProperty))
				{
					retVal = pDrawingProperty;
					break;
				}
			}
		}
		return retVal;
	}
	
	protected void init()
	{
		// Main layout
		getContentPane().setLayout(new BorderLayout());
		//JPanel uiPanel = new JPanel();
		//uiPanel.setLayout(new BorderLayout());
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerLocation(260);
		
		JPanel diagramPanel = new JPanel();
		diagramPanel.setPreferredSize(new Dimension(100, 50));
		diagramPanel.setLayout(new BorderLayout());
		splitPane.add(diagramPanel);
		//uiPanel.add(diagramPanel, BorderLayout.WEST);
		JPanel propertiesPanel = new JPanel();
		propertiesPanel.setLayout(new BorderLayout());
		splitPane.add(propertiesPanel);
		//uiPanel.add(propertiesPanel, BorderLayout.CENTER);
		getContentPane().add(splitPane, BorderLayout.CENTER);
		JPanel actionPanel = new JPanel();
		actionPanel.setLayout(new BorderLayout());
		getContentPane().add(actionPanel, BorderLayout.SOUTH);
		
		// diagram panel
		diagramPanel.setBorder(BorderFactory.createTitledBorder(DrawingPropertyResource.getString("IDS_PRESENTATIONELEMENTTYPE")));
		diagramPanel.setLayout(new BorderLayout());
		m_EngineList = new JList();
		m_EngineList.setBorder(BorderFactory.createEmptyBorder(6,3,3,3));
		JScrollPane scrollPane = new JScrollPane(m_EngineList);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		m_EngineList.setCellRenderer(new ElementListCellRenderer());
		m_EngineList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		diagramPanel.add(scrollPane, BorderLayout.CENTER);
		Box diagramSelectionBox = Box.createHorizontalBox();
		diagramTypeLabel = new JLabel(DrawingPropertyResource.determineText(DrawingPropertyResource.getString("IDS_SHOWTYPESON")));
		DrawingPropertyResource.setMnemonic(diagramTypeLabel, DrawingPropertyResource.getString("IDS_SHOWTYPESON"));
		m_DiagramSelection = new JComboBox();
		diagramTypeLabel.setLabelFor(m_DiagramSelection);
		DrawingPropertyResource.setFocusAccelerator(m_DiagramSelection, DrawingPropertyResource.getString("IDS_SHOWTYPESON"));
		diagramSelectionBox.add(diagramTypeLabel);
		diagramSelectionBox.add(Box.createHorizontalStrut(10));
		diagramSelectionBox.add(m_DiagramSelection);
		diagramPanel.add(diagramSelectionBox, BorderLayout.SOUTH);
		m_EngineList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				JList engines = (JList)e.getSource();
				m_SelectedEngine = getDENameFromDisplayName((String)engines.getSelectedValue());
				onObjectListSelected(m_SelectedEngine);
			}
		});	
		m_DiagramSelection.addItemListener
		(
			new ItemListener()
			{
            public void itemStateChanged(ItemEvent e)
            {
					m_SetectedDiagramType = e.getItem().toString();
					onCbnSelchangeDiagramtype();
            }
			}
		);
				
		// properties panel
		propertiesPanel.setBorder(BorderFactory.createTitledBorder(DrawingPropertyResource.getString("IDS_AVAILABLEPROPERTIES")));
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
		model = new FontColorTreeTableModel(root, null);
		m_Properties = new FontColorTreeTable(model, this);
		//m_Properties.setPreferredSize(new Dimension(50,50));
		//propertiesPanel.add(m_Properties, BorderLayout.CENTER)
		JScrollPane treeScrollPane = new JScrollPane(m_Properties);
		propertiesPanel.add(treeScrollPane, BorderLayout.CENTER);
		m_AdvancedCheck = new JCheckBox();
		m_AdvancedCheck.setSelected(true);
		m_AdvancedCheck.setText(DrawingPropertyResource.determineText(DrawingPropertyResource.getString("IDS_SHOWADVANCED")));
		DrawingPropertyResource.setMnemonic(m_AdvancedCheck, DrawingPropertyResource.getString("IDS_SHOWADVANCED"));
		ActionListener advanceAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JCheckBox check = ((JCheckBox)e.getSource());
				check.requestFocusInWindow();
				check.setSelected(!check.isSelected());
			}
		};
		propertiesPanel.add(m_AdvancedCheck, BorderLayout.SOUTH);
		m_AdvancedCheck.addChangeListener
		(
			new ChangeListener()
			{
            public void stateChanged(ChangeEvent e)
            {
					onObjectListSelected(m_SelectedEngine);
            }
			} 
		);
		
		// Action panel
		Box actionBox = Box.createHorizontalBox();
		//actionBox.add(Box.createHorizontalGlue());
		JButton okButton = new JButton();
		okButton.setText(DrawingPropertyResource.getString("IDS_OK"));
		okButton.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					try
					{
						onBnClickedApply();
					}
					finally
					{
						dispose();
					}
				}
			}
		);
		actionBox.add(okButton);
		actionBox.add(Box.createHorizontalStrut(10));
		m_ApplyButton = new JButton();
		m_ApplyButton.setText(DrawingPropertyResource.determineText(DrawingPropertyResource.getString("IDS_APPLY")));
		DrawingPropertyResource.setMnemonic(m_ApplyButton, DrawingPropertyResource.getString("IDS_APPLY"));
		ActionListener applyAction = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JButton apply = ((JButton)e.getSource());
				apply.requestFocusInWindow();
				onBnClickedApply();
				m_ApplyButton.setEnabled(false);
			}
		};
		m_ApplyButton.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					onBnClickedApply();
					m_ApplyButton.setEnabled(false);
				}
			}
		);
		actionBox.add(m_ApplyButton);
		actionBox.add(Box.createHorizontalStrut(10));
		JButton cancelButton = new JButton();
		cancelButton.setText(DrawingPropertyResource.getString("IDS_CANCEL"));
		cancelButton.addActionListener
		(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					dispose();
				}
			}
		);
		actionBox.add(cancelButton);
		actionPanel.add(actionBox, BorderLayout.EAST);
		
		setSize(600, 400);
		setModal(true);
		
		// Initialize the dialog
		onInitDialog();
	}
	
	public BasicColorsAndFontsDialog()
	{
		init();
	}
	
	public BasicColorsAndFontsDialog(JDialog dia)
	{
		super((Dialog)dia);
		init();
	}

	/**
	 * Initialize the dialogs
	 */
	public boolean onInitDialog()
	{
		// Get the config location
		if (m_sConfigLocation.length() == 0)
		{
			IProduct pProduct = ProductHelper.getProduct();
			if (pProduct != null)
			{
				IConfigManager configMgr = pProduct.getConfigManager();
				if (configMgr != null)
				{
					String dir = configMgr.getDefaultConfigLocation();
					if(dir != null && dir.length() > 0)
					{
						m_sConfigLocation = dir;
						m_sConfigLocation += "ColorProjects\\ColorWorkspace\\ColorProject";
					}
				}
			}
		}
   
		// Create the UserInput Blocker so our little diagram isn't available for
		// the user to muck with.
		if (m_pDiagramBlocker == null)
		{
			m_pDiagramBlocker = new GUIBlocker();
			if( m_pDiagramBlocker != null )
			{
				m_pDiagramBlocker.setKind( GBK.DIAGRAM_KEYBOARD  | GBK.DIAGRAM_SELECTION  |
													GBK.DIAGRAM_MOVEMENT  | GBK.DIAGRAM_RESIZE  |
													GBK.DIAGRAM_DELETION );
			}
		}
   
//		// Create image list
//		m_ImageList.Create(16, 16,
//								 ILC_COLOR32 | ILC_MASK,
//								 5, 5);
//   
//		// Associate image list to list box
//		m_ObjectList.SetImageList(&m_ImageList);

		// Create our list of standard diagram types
		if (m_StandardDrawEngines.size() == 0)
		{
			addStandardDrawEngines(IDiagramKind.DK_ACTIVITY_DIAGRAM, false);
			addStandardDrawEngines(IDiagramKind.DK_CLASS_DIAGRAM, false);
			addStandardDrawEngines(IDiagramKind.DK_COLLABORATION_DIAGRAM, false);
			addStandardDrawEngines(IDiagramKind.DK_COMPONENT_DIAGRAM, false);
			addStandardDrawEngines(IDiagramKind.DK_DEPLOYMENT_DIAGRAM, false);
			addStandardDrawEngines(IDiagramKind.DK_SEQUENCE_DIAGRAM, false);
			addStandardDrawEngines(IDiagramKind.DK_STATE_DIAGRAM, false);
			addStandardDrawEngines(IDiagramKind.DK_USECASE_DIAGRAM, false);
			//addStandardDrawEngines(IDiagramKind.DK_ENTITY_DIAGRAM, true);
		}

                if (m_DiagramSelection != null 
                    && diagramTypeLabel != null
                    && splitPane != null) 
                {
                    int prefferedLeftWidth = Math.min(
                                 Math.max(m_DiagramSelection.getPreferredSize().width
                                          + diagramTypeLabel.getPreferredSize().width
                                          + 30, 
                                          260), 
                                 500);
                    if (prefferedLeftWidth != 260) 
                    {
                        setSize(600 + Math.max(prefferedLeftWidth - 260, 0), 400);
                        splitPane.setDividerLocation(prefferedLeftWidth);
                    }
                }
		return true;  // return TRUE unless you set the focus to a control
	}

	/**
	 * Called when the object list changes
	 */
	public void onObjectListChange()
	{
		String xsSelectedDE = getSelectedDE();

		// Notify our derived classes through this virtual routine
		onObjectListSelected(xsSelectedDE);
	}
	
	/**
	 * Shows the property page for the argument draw engine
	 */
	public void showPropertyPage(String sDE)
	{
            // TODO: meteora
//		IPresentationResourceMgr pMgr = ProductHelper.getPresentationResourceMgr();
//		if (pMgr != null)
//		{
//			ETList<IDrawingProperty> properties = pMgr.getAllDrawingProperties(sDE);
//			int properti = properties.size();
//			if (properties != null)
//			{
//				TreeMap<String, IDrawingProperty> fonts = new TreeMap<String, IDrawingProperty>();
//				TreeMap<String, IDrawingProperty> colors = new TreeMap<String, IDrawingProperty>();
//				for (int i = 0; i < properties.size(); i++)
//				{
//					IDrawingProperty pDrawingProperty = properties.get(i);
//					if (pDrawingProperty != null)
//					{
//						boolean toAdd = true;
//						boolean isAdvanced = pMgr.isAdvanced(sDE, pDrawingProperty.getResourceName());
//						if (!m_AdvancedCheck.isSelected() && isAdvanced)
//						{
//							toAdd = false;
//						}
//						
//						if (toAdd)
//						{
//							pDrawingProperty = checkForChange(pDrawingProperty);
//							ETPairT<String, String> pairStr = pDrawingProperty.getDisplayName();
//							String dispName = pairStr.getParamOne();
//							if (pDrawingProperty.getResourceType().equals("font"))
//							{
//								fonts.put(dispName, pDrawingProperty);
//							}
//							else if (pDrawingProperty.getResourceType().equals("color"))
//							{
//								colors.put(dispName, pDrawingProperty);
//							}
//						}
//					}
//				}
//				
//				DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
//				String fontStr = DrawingPropertyResource.getString("IDS_FONTS");
//				String colorStr = DrawingPropertyResource.getString("IDS_COLORS");
//				DefaultMutableTreeNode fontChild = new DefaultMutableTreeNode(fontStr);
//				DefaultMutableTreeNode colorChild = new DefaultMutableTreeNode(colorStr);
//				if (root != null)
//				{
//					root.removeAllChildren();
//					Collection colColors = colors.values();
//					if (colColors != null && colColors.size() > 0)
//					{
//						Iterator iter = colColors.iterator();
//						for (; iter.hasNext();)
//						{
//							Object obj = iter.next();
//							DefaultMutableTreeNode colorSubChild = new DefaultMutableTreeNode(obj);
//							colorChild.add(colorSubChild);
//						}
//						root.add(colorChild);
//					}
//					Collection colFonts = fonts.values();
//					if (colFonts != null && colFonts.size() > 0)
//					{
//						//java.util.Collections.sort(fonts);
//						Iterator iter = colFonts.iterator();
//						for (; iter.hasNext();)
//						{
//							Object obj = iter.next();
//							DefaultMutableTreeNode fontSubChild = new DefaultMutableTreeNode(obj);
//							fontChild.add(fontSubChild);
//						}
//						root.add(fontChild);
//					}
//				}
//				m_Properties.updateUI();
//				m_Properties.expandFirstLevelNodes();
//			}
//		}
	}
	
//	/**
//	 * Shows the argument property page.
//	 */
//	void CBasicColorsAndFontsDialog::ShowPropertyPage2(xstring sDE, CRect& rtPropPage, IPropertyPage* pPage)
//	{
//		HRESULT hr = S_OK;
//
//		try
//		{
//			ATLASSERT(pPage);
//			if (pPage)
//			{
//				m_pCurrentPage = pPage;
//				m_sCurrentPage = sDE.c_str();
//
//				hr = pPage->SetPageSite( NULL );
//				if( FAILED( hr ) ) throw _com_error( hr );
//
//				hr = pPage->SetPageSite( (IPropertyPageSite*) GetInterface( &IID_IPropertyPageSite ) );
//				if( FAILED( hr ) ) throw _com_error( hr );
//
//				InitializePage(pPage);
//				if( FAILED( hr ) ) throw _com_error( hr );
//
//				hr = pPage->Activate( GetSafeHwnd(), rtPropPage, TRUE );
//				if( FAILED( hr ) ) throw _com_error( hr );
//
//				UpdateData(FALSE);
//
//				hr = pPage->Show( SW_SHOW );
//				if( FAILED( hr ) ) throw _com_error( hr );
//			}
//		}
//		catch( _com_error& err )
//		{
//			hr = COMErrorManager::ReportError( err );
//		}
//	}

//	/**
//	 * Saves all the changed property sheets
//	 */
//	void CBasicColorsAndFontsDialog::SaveAllChangedPropertySheets()
//	{
//		// Save all the property pages
//		std::map < CComBSTR /*drawengine*/, CComPtr < IPropertyPage > >::iterator iterator;
//
//		for (iterator = m_ChangedPages.begin() ; iterator != m_ChangedPages.end(); ++iterator)
//		{
//			_VH(iterator->second->Apply());
//		}
//		m_ChangedPages.empty();
//   
//		if ( m_pCurrentPage && m_pCurrentPage->IsPageDirty() == S_OK )
//		{
//			// Save the current page
//			_VH(m_pCurrentPage->Apply());
//		}
//	}

	/**
	 * Translates the draw engine display name to the actual draw engine name
	 */
	public String getDENameFromDisplayName(String sDisplayName)
	{
		String retVal = null;

		if (sDisplayName != null && sDisplayName.length() > 0)
		{
			retVal = m_pDrawEngineNames.get(sDisplayName);
		}

		return retVal;
	}
	
	/**
	 * Translates the draw engine name to the actual draw engine display name
	 */
	public String getDisplayNameNameFromDE(String sDEName)
	{
		String retVal =  null;

		if (sDEName != null && sDEName.length() > 0)
		{
			Enumeration enumVal = m_pDrawEngineNames.keys();
			while (enumVal.hasMoreElements())
			{
				String obj = (String)enumVal.nextElement();
				String val = (String)m_pDrawEngineNames.get(obj);
				if (((String)val).equals(sDEName))
				{
					retVal = (String)obj;
				}
			}
		}

		return retVal;
	}
	
	/**
	 * Returns the diagram filename for a diagram kind
	 */
	public String getDiagramFilename(int nDAKind)
	{
		String filename = null;

		// Get the presentation resource manager so we can extract the default filename
                // TODO: meteora
//		IPresentationResourceMgr pMgr = ProductHelper.getPresentationResourceMgr();
//		if (pMgr != null)
//		{
//			filename = pMgr.getSampleDiagramFilename(nDAKind);
//		}

		return filename;
	}
	
//	/**
//	 * Is this the diagram HWND?
//		*/
//	bool CBasicColorsAndFontsDialog::IsDrawingAreaHWND(HWND hWnd)
//	{
//		bool bIsHWND = false;
//   
//		if (hWnd == m_DrawingArea->m_hWnd)
//		{
//			bIsHWND = true;
//		}
//		else if (m_DrawingArea && m_DrawingArea->m_hWnd)
//		{
//			CComPtr < IAxDrawingAreaControl > pDA;
//			m_DrawingArea->GetDrawingAreaControl(&pDA);
//			if (pDA)
//			{
//				HWND hWindowHandle = 0;
//				HWND hDiagramWindowHandle = 0;
//
//				_VH(pDA->GetWindowHandle( (OLE_HANDLE*)(&hWindowHandle)));
//				_VH(pDA->GetDiagramWindowHandle( (OLE_HANDLE*)(&hDiagramWindowHandle)));
//
//				if (hWnd == hWindowHandle ||
//					 hWnd == hDiagramWindowHandle)
//				{
//					bIsHWND = true;
//				}
//			}
//		}
//
//		return bIsHWND;
//	}
//	/**
//	 * Creates the DA control
//	 */
//	bool CBasicColorsAndFontsDialog::CreateDrawingArea(DiagramKind nDAKind)
//	{
//		AFX_MANAGE_STATE(AfxGetStaticModuleState())
//		bool bCreated = false;
//
//		// See if the one we have is fine
//		if (m_DrawingArea && m_DrawingArea->m_hWnd)
//		{
//			CComPtr < IAxDrawingAreaControl > pDA;
//			m_DrawingArea->GetDrawingAreaControl(&pDA);
//			if (pDA)
//			{
//				DiagramKind nKind = DK_UNKNOWN;
//				_VH(pDA->get_DiagramKind(&nKind));
//
//				if (nKind == nDAKind)
//				{
//					bCreated = true;
//				}
//			}
//		}
//
//		// Now create it
//		if (!bCreated)
//		{
//			::LockWindowUpdate(m_hWnd);
//			// Get the default filename
//			xstring sDiagramFilename = GetDiagramFilename(nDAKind);
//			if (sDiagramFilename.size())
//			{
//				// Destroy the existing if there is one
//				DestroyDrawingArea(true);
//			}
//
//			if (m_sConfigLocation.Length() &&
//				 m_DrawingArea == NULL) // not subclassed yet
//			{
//				// Create a new drawing area
//				m_DrawingArea = new CAxDrawingAreaControlWrapper();
//
//				if (m_DrawingArea && m_DALocation.Width())
//				{
//					// Install our controller
//					InstallOurEventDispatchController();
//
//					// Create the new control
//					m_DrawingArea->Create(NULL, WS_VISIBLE | WS_TABSTOP, m_DALocation, this, m_DrawingAreaControlID);
//
//					if (m_DrawingArea->m_hWnd)
//					{
//						CComPtr < IApplication > pApp;
//						CComPtr < IAxDrawingAreaControl > pDA;
//
//						CProductHelper::Instance()->GetApplication(&pApp);
//						m_DrawingArea->GetDrawingAreaControl(&pDA);
//						if (pApp && pDA)
//						{
//							CComBSTR sProjectLocation(m_sConfigLocation);
//							CComBSTR sDiagramLocation(m_sConfigLocation);
//
//							sProjectLocation += _T("\\ColorProject.etd");
//							sDiagramLocation += _T("\\");
//							sDiagramLocation += sDiagramFilename.c_str();
//
//							if (m_OpenedProject == 0)
//							{
//								_VH(pApp->OpenProject(sProjectLocation, &m_OpenedProject));
//							}
//							if (m_OpenedProject)
//							{
//								DrawingFileCode nFileCode;
//								_VH(pDA->Load(sDiagramLocation, &nFileCode));
//								_VH(pDA->put_EnableScrollBars(VARIANT_FALSE));
//
//								// Now send the diagram a sizing message
//								CRect rect;
//								m_DrawingArea->GetWindowRect(&rect);
//								DWORD dw = MAKELONG(rect.Width(), rect.Height());
//
//								::SendMessage(m_DrawingArea->m_hWnd, WM_SIZE, 0, (LPARAM)dw);
//							}
//						}
//					}
//					bCreated = true;
//				}
//			}
//			::LockWindowUpdate(NULL);
//		}
//
//		return bCreated;
//	}
//	/**
//	 * Called when the dialog is destroyed.
//	 */
//	void CBasicColorsAndFontsDialog::OnDestroy()
//	{
//		DestroyDrawingArea(false);
//
//		CDispatcherHelper dispatcherHelper;
//		_VH(dispatcherHelper.RevokeDrawingAreaSelectionSink(m_DrawingAreaSelectionEventsEventsCookie));
//		m_DrawingAreaSelectionEventsEventsCookie = -1;
//
//		InstallOriginalEventDispatchController();
//		m_OriginalEDController = 0 ;
//	}
//	/**
//	 * Destroys the drawing area window.  Use bNicely to hide the window and give the user feedback
//	 */
//	void CBasicColorsAndFontsDialog::DestroyDrawingArea(bool bNicely)
//	{
//		if (m_DrawingArea && m_DrawingArea->m_hWnd)
//		{
//			CComPtr < IAxDrawingAreaControl > pDA;
//			m_DrawingArea->GetDrawingAreaControl(&pDA);
//			if (pDA)
//			{
//				if (bNicely)
//				{
//					::ShowWindow(m_DrawingArea->m_hWnd,SW_HIDE);
//					ShowNoPreviewImageText(VARIANT_TRUE);
//					UpdateWindow();
//				}
//
//				SetReadOnly(VARIANT_FALSE);
//				::DestroyWindow(m_DrawingArea->m_hWnd);
//				m_DrawingArea->m_hWnd = 0;
//			}
//		}
//
//		if (m_DrawingArea)
//		{
//			delete m_DrawingArea;
//			m_DrawingArea = 0;
//		}
//	}
//	/**
//	 * Sets the drawing area control to readonly, dirty,... depending on the bool
//	 */
//	HRESULT CBasicColorsAndFontsDialog::SetReadOnly(VARIANT_BOOL bReadOnly)
//	{
//		HRESULT hr = S_OK;
//
//		try
//		{
//			if (m_DrawingArea && m_DrawingArea->m_hWnd)
//			{
//				CComPtr < IAxDrawingAreaControl > pDA;
//				m_DrawingArea->GetDrawingAreaControl(&pDA);
//				if (pDA)
//				{
//					_VH(pDA->put_ReadOnly(bReadOnly));
//					_VH(pDA->put_IsDirty(VARIANT_FALSE));
//				}
//			}
//		}
//		catch( _com_error& err )
//		{
//			hr = COMErrorManager::ReportError( err );
//		}
//
//		return S_OK;
//	}
//	/**
//	 * Sends a message to the diagram about the sizing
//	 */
//	void CBasicColorsAndFontsDialog::OnSize(UINT nType, int cx, int cy)
//	{
//		if (m_DrawingArea && m_DrawingArea->m_hWnd)
//		{
//			DWORD dw = MAKELONG(cx, cy);
//
//			::SendMessage(m_DrawingArea->m_hWnd, WM_SIZE, 0, (LPARAM)dw);
//		}
//	}
//	void CBasicColorsAndFontsDialog::InstallOriginalEventDispatchController()
//	{
//		if (m_OriginalEDController)
//		{
//			CComPtr < ICoreProduct > pCoreProduct;
//
//			CProductHelper::Instance()->GetCoreProduct(&pCoreProduct);
//			if (pCoreProduct)
//			{
//				pCoreProduct->put_EventDispatchController(m_OriginalEDController);
//				ModifyEventHelper::Instance()->ReestablishEventDispatcher();
//			}
//		}
//	}
//	/**
//	 * Installs our own event dispatch controller so all the folks listening to diagrams
//	 * don't get the events from our, private dialog diagram.  As the dialog is destroyed
//	 * the original event dispatch controller is re-installed.
//	 */
//	void CBasicColorsAndFontsDialog::InstallOurEventDispatchController()
//	{
//		HRESULT hr = S_OK;
//
//		try
//		{
//			CComPtr < ICoreProduct > pCoreProduct;
//
//			CProductHelper::Instance()->GetCoreProduct(&pCoreProduct);
//			if (pCoreProduct)
//			{
//				if (m_OriginalEDController == 0)
//				{
//					// Get the current controller
//					pCoreProduct->get_EventDispatchController(&m_OriginalEDController);
//				}
//
//				if (m_OriginalEDController)
//				{
//					if (m_OurController == 0)
//					{
//						// create our controller
//						m_OurController.CoCreateInstance( __uuidof( EventDispatchController ));
//               
//						if (m_OurController)
//						{
//							// Add a new drawing area dispatcher
//							{
//								CComPtr < IDrawingAreaEventDispatcher > pDrawDisp;
//								_VH( pDrawDisp.CoCreateInstance( __uuidof( DrawingAreaEventDispatcher )));
//								_VH( m_OurController->AddDispatcher( EventDispatchNameKeeper::DrawingAreaName(), pDrawDisp ));
//							}
//
//							// Add a new structure dispatcher for when the project opens
//							{
//								CComPtr < IStructureEventDispatcher > pStructDisp;
//								_VH( pStructDisp.CoCreateInstance( __uuidof( StructureEventDispatcher )));
//								_VH( m_OurController->AddDispatcher( EventDispatchNameKeeper::Structure(), pStructDisp ));
//							}
//
//							// Add the workspace events dispatcher
//							{
//								CComPtr < IWorkspaceEventDispatcher > pWSDispatcher;
//								_VH( pWSDispatcher.CoCreateInstance( __uuidof( WorkspaceEventDispatcher )));
//								_VH( m_OurController->AddDispatcher( EventDispatchNameKeeper::WorkspaceName(), pWSDispatcher ));
//							}
//
//							// Add the scm events dispatcher
//							{
//								CComPtr < ISCMEventDispatcher > pSCMDispatcher;
//								_VH( pSCMDispatcher.CoCreateInstance( __uuidof( SCMEventDispatcher )));
//								_VH( m_OurController->AddDispatcher( EventDispatchNameKeeper::SCM(), pSCMDispatcher ));
//							}
//
//							// add the dynamics dispatcher
//							{
//								CComPtr < IDynamicsEventDispatcher > pDynamicsDispatcher;
//								_VH( pDynamicsDispatcher.CoCreateInstance( __uuidof( DynamicsEventDispatcher )));
//								_VH( m_OurController->AddDispatcher( EventDispatchNameKeeper::Dynamics(), pDynamicsDispatcher ));
//							}
//
//							// Add the core product dispatcher
//							{
//								CComPtr < ICoreProductEventDispatcher > pCoreProdDispatcher;
//								_VH( pCoreProdDispatcher.CoCreateInstance( __uuidof( CoreProductEventDispatcher )));
//								_VH( m_OurController->AddDispatcher( EventDispatchNameKeeper::CoreProduct(), pCoreProdDispatcher ));
//							}
//
//							// Add the activity event dispatcher
//							{
//								CComPtr < IActivityEventDispatcher > pActDispatcher;
//								_VH( pActDispatcher.CoCreateInstance( __uuidof( ActivityEventDispatcher )));
//								_VH( m_OurController->AddDispatcher( EventDispatchNameKeeper::Activities(), pActDispatcher ));
//							}
//
//							// Add the edit control event dispatcher
//							{
//								CComPtr < IAxEditCtrlEventDispatcher > pEditCtrlDispatcher;
//								_VH( pEditCtrlDispatcher.CoCreateInstance( __uuidof( AxEditCtrlEventDispatcher )));
//								_VH( m_OurController->AddDispatcher( EventDispatchNameKeeper::EditCtrlName(), pEditCtrlDispatcher ));
//							}
//
//							// Add an elements modified dispatcher
//							{
//								CComPtr < IElementChangeEventDispatcher > pElementChangeEventDispatcher;
//								_VH( pElementChangeEventDispatcher.CoCreateInstance( __uuidof( ElementChangeEventDispatcher )));
//								_VH( m_OurController->AddDispatcher( EventDispatchNameKeeper::ModifiedName(), pElementChangeEventDispatcher ));
//							}
//
//							// Add an element lifetime dispatcher
//							{
//								CComPtr < IElementLifeTimeEventDispatcher > pElementLifeTimeEventDispatcher;
//								_VH( pElementLifeTimeEventDispatcher.CoCreateInstance( __uuidof( ElementLifeTimeEventDispatcher )));
//								_VH( m_OurController->AddDispatcher( EventDispatchNameKeeper::LifeTime(), pElementLifeTimeEventDispatcher ));
//							}
//
//							// Add an classifier dispatcher
//							{
//								CComPtr < IClassifierEventDispatcher > pClassifierEventDispatcher;
//								_VH( pClassifierEventDispatcher.CoCreateInstance( __uuidof( ClassifierEventDispatcher )));
//								_VH( m_OurController->AddDispatcher( EventDispatchNameKeeper::Classifier(), pClassifierEventDispatcher ));
//							}
//                  
//							if ( m_EventsSink == 0)
//							{
//								m_EventsSink = new CComObject<CDrawingPropEventSink>;
//								m_EventsSink->AddRef();
//								m_EventsSink->SetParent(this);
//							}
//						}
//					}
//
//					if (m_OurController)
//					{
//						pCoreProduct->put_EventDispatchController(m_OurController);
//                  
//						// Register our sink
//						if (m_EventsSink &&
//							 m_DrawingAreaSelectionEventsEventsCookie == -1)
//						{
//							CDispatcherHelper dispatcherHelper;
//							CComPtr < IDispatch > pMySink;
//							_VH(m_EventsSink->QueryInterface(__uuidof(IDispatch), (void **)(&pMySink)));
//
//							_VH(dispatcherHelper.RegisterDrawingAreaSelectionEvents(pMySink, m_DrawingAreaSelectionEventsEventsCookie));
//						}
//					}
//				}
//			}
//		}
//		catch( _com_error& err )
//		{
//			hr = COMErrorManager::ReportError( err );
//		}
//	}
//	/**
//	 * Finds the first presentation element with this draw engine and 
//	 * displays it in the diagram.
//	 */
//	bool CBasicColorsAndFontsDialog::ShowInDrawingArea(xstring& sDE, bool bShowIfNecessary)
//	{
//		bool bShown = false;
//		HRESULT hr = S_OK;
//
//		try
//		{
//			if (m_DrawingArea && m_DrawingArea->m_hWnd)
//			{
//				CComPtr < IAxDrawingAreaControl > pDA;
//				m_DrawingArea->GetDrawingAreaControl(&pDA);
//				if (pDA)
//				{
//					// Deselect all
//					_VH(pDA->SelectAll(VARIANT_FALSE));
//
//					// Find the first PE with this draw engine
//					CComPtr < IPresentationElements > pPEs;
//					long count = 0;
//
//					_VH(pDA->GetAllElementsByDrawEngineType(CComBSTR(sDE.c_str()), &pPEs));
//					if (pPEs)
//					{
//						pPEs->get_Count(&count);
//					}
//
//					if (count >= 1)
//					{
//						CComPtr < IPresentationElement > pPE;
//
//						pPEs->Item(0, &pPE);
//						if (pPE)
//						{
//							// Selection is blocked so make the 2nd argument false
//							_VH(pDA->CenterPresentationElement(pPE, VARIANT_FALSE, VARIANT_TRUE));
//							bShown = true;
//                  
//							// The sqd diagram is too big for this dialog so we resize it
//							DiagramKind nKind = DK_UNKNOWN;
//							_VH(pDA->get_DiagramKind(&nKind));
//							if (nKind == DK_SEQUENCE_DIAGRAM)
//							{
//								// Zoom to 50%
//								_VH(pDA->Zoom(0.5));
//							}
//						}
//					}
//				}
//			}
//
//			// If we couldn't find the draw engine and we're told to swap out draw engines then
//			// ask the presentation resource manager where this draw engine is and bring up that 
//			// diagram.
//			if (!bShown && bShowIfNecessary)
//			{
//				// See if this type of draw engine has a preferred type of diagram
//				CComPtr < IPresentationResourceMgr > pMgr;
//         
//				CProductHelper::Instance()->GetPresentationResourceMgr(&pMgr);
//
//				ATLASSERT(pMgr);
//				if (pMgr)
//				{
//					DiagramKind nKind = DK_UNKNOWN;
//
//					_VH(pMgr->GetDrawEngineDisplayDetails(CComBSTR(sDE.c_str()), NULL, NULL, &nKind));
//					if (nKind != DK_UNKNOWN)
//					{
//						_VH(CreateDrawingArea(nKind));
//						bShown = ShowInDrawingArea(sDE, false);
//					}
//				}
//			}
//
//			if (m_DrawingArea && m_DrawingArea->m_hWnd)
//			{
//				::ShowWindow(m_DrawingArea->m_hWnd,bShown?SW_SHOW:SW_HIDE);
//			}
//			ShowNoPreviewImageText(bShown?VARIANT_FALSE:VARIANT_TRUE);
//		}
//		catch( _com_error& err )
//		{
//			hr = COMErrorManager::ReportError( err );
//		}
//		return bShown;
//	}
//	/**
//	 * Finds the first presentation element with this draw engine and 
//	 * displays it in the diagram.  Shows the drawing area if necessary
//	 */
//	bool CBasicColorsAndFontsDialog::ShowInDrawingArea(xstring& sDE)
//	{
//		return ShowInDrawingArea(sDE, true);
//	}
//	/**
//	 * Event coming from the sink about a select on the diagram.
//	 */
//	HRESULT CBasicColorsAndFontsDialog::OnSelect(IDiagram* pParentDiagram, 
//																IPresentationElements* pSelectedItems,
//																ICompartment* pCompartment)
//	{
//		AFX_MANAGE_STATE(AfxGetStaticModuleState())
//		HRESULT hr = S_OK;
//
//		try
//		{
//			long count = 0;
//			if (pSelectedItems)
//			{
//				pSelectedItems->get_Count(&count);
//			}
//
//			if (count)
//			{
//				CComPtr < IPresentationElement > pPE;
//				pSelectedItems->Item(0, &pPE);
//
//				CComQIPtr < IGraphPresentation > pGraphPE(pPE);
//				if (pGraphPE)
//				{
//					CComPtr < IDrawEngine > pDrawEngine;
//					CComBSTR sID;
//					USES_CONVERSION;
//
//					pGraphPE->get_DrawEngine(&pDrawEngine);
//					pDrawEngine->GetDrawEngineID(&sID);
//
//					// Now convert to the friendly name and select in
//					// the list and show the property page
//					xstring xsDEName(SAFEW2T(sID));
//					xstring xsDisplayName = GetDisplayNameNameFromDE(xsDEName);
//
//					// Now select in the object list and show the page
//					if (xsDisplayName.size())
//					{
//						CString pageName(xsDisplayName.c_str());
//
//						if (m_ObjectList.m_hWnd)
//						{
//							m_ObjectList.SelectString(-1, pageName);
//							// OnLbnSelchangeObjectlist();
//						}
//
//						ShowPropertyPage(xsDEName);
//					}
//				}
//			}
//		}
//		catch( _com_error& err )
//		{
//			hr = COMErrorManager::ReportError( err );
//		}
//
//		return hr;
//	}
//	/**
//	 * Event coming from the sink about an unselect on the diagram.
//	 */
//	HRESULT CBasicColorsAndFontsDialog::OnUnselect(IDiagram* pParentDiagram, 
//																  IPresentationElements* unselectedItems)
//	{
//		return S_OK;
//	}

	/**
	 * Returns all the draw engines that make up the presentation elements
	 */
	public ETList<String> getPEDrawEngines(ETList<IPresentationElement> pPEs)
	{
		ETList<String> pDEStrings = new ETArrayList<String>();

		if (pPEs != null)
		{
			long count = pPEs.size();
			for (int i = 0 ; i < count ; i++)
			{
				IPresentationElement pThisPE = pPEs.get(i);
                                // TODO: meteora
//				if (pThisPE instanceof IGraphPresentation)
//				{
//					IGraphPresentation pGraphPE = (IGraphPresentation)pThisPE;
//					IDrawEngine pEngine = pGraphPE.getDrawEngine();
//					if (pEngine != null)
//					{
//						String sID = pEngine.getDrawEngineID();
//						if (sID != null && sID.length() > 0)
//						{
//							boolean bExist = false;
//							for (int j = 0; j < pDEStrings.size(); j++)
//							{
//								String s = pDEStrings.get(j);
//								if (s != null && s.equals(sID))
//								{
//									bExist = true;
//									break;
//								}
//							}
//							if (!bExist)
//							{
//								pDEStrings.add(sID);
//							}
//						}
//					}
//				}
			}
		}
		return pDEStrings;
	}
	
	/**
	 * Returns the presentation elements of this type
	 */
	public ETPairT<ETList<IPresentationElement>, ETList<IDrawingProperty> > getPEs(ETList<IPresentationElement> pSourcePEs,	String sDrawEngine)
	{
		ETList<IPresentationElement> pFoundPEs = new ETArrayList<IPresentationElement>();
		ETList<IDrawingProperty> pFoundProperties = new ETArrayList<IDrawingProperty>();
		
		boolean bFoundFirstPE = false;
		if (pSourcePEs != null)
		{
			int count = pSourcePEs.size();
			for (int i = 0 ; i < count ; i++)
			{
				IPresentationElement pThisPE = pSourcePEs.get(i);
                                // TODO: meteora
//				if (pThisPE instanceof IGraphPresentation)
//				{
//					IGraphPresentation pGraphPE = (IGraphPresentation)pThisPE;
//					IDrawEngine pEngine = pGraphPE.getDrawEngine();
//					if (pEngine != null)
//					{
//						String sID = pEngine.getDrawEngineID();
//						if (sID != null && sID.equals(sDrawEngine))
//						{
//							pFoundPEs.add(pThisPE);
//
//							if (bFoundFirstPE == false)
//							{
//								// Get the properties for this draw engine
//								bFoundFirstPE = true;
//
//								if (pEngine instanceof IDrawingPropertyProvider)
//								{
//									IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)pEngine;
//									pFoundProperties = pProvider.getDrawingProperties();
//								}
//							}
//						}
//					}
//				}
			}
		}

		ETPairT<ETList<IPresentationElement>, ETList<IDrawingProperty> > retVal = new ETPairT<ETList<IPresentationElement>, ETList<IDrawingProperty> >();
		retVal.setParamOne(pFoundPEs);
		retVal.setParamTwo(pFoundProperties);

		return retVal;
	}
	
//	/**
//	 * This one initializes the drawing area by resetting it's fonts/colors to application 
//	 * defaults. Use it when changing application defaults and the diagram will show
//	 * the current application default colors after the reset.
//	 */
//	HRESULT CBasicColorsAndFontsDialog::InitDrawingAreaToApplicationDefaults()
//	{
//		HRESULT hr = S_OK;
//
//		try
//		{
//			CComPtr < IAxDrawingAreaControl > pDA;
//
//			if (m_DrawingArea)
//			{
//				m_DrawingArea->GetDrawingAreaControl(&pDA);
//				if (pDA)
//				{
//					_VH(SetReadOnly(VARIANT_FALSE));
//
//					CComQIPtr < IDrawingPropertyProvider > pProvider(pDA);
//					if (pProvider)
//					{
//						_VH(pProvider->ResetToDefaultResources());
//					}
//
//					_VH(SetReadOnly(VARIANT_TRUE));
//				}
//			}
//		}
//		catch( _com_error& err )
//		{
//			hr = COMErrorManager::ReportError( err );
//		}
//
//		return hr;
//	}
//	/**
//	 * This one initializes the drawing area by copying the open diagram's properties
//	 * to the diagram on this dialog.  That means that all the presentation elements on
//	 * this dialog's diagram will appear as they do on the current diagram.
//	 */
//	HRESULT CBasicColorsAndFontsDialog::InitDrawingAreaToDiagramDefaults(IAxDrawingAreaControl* pActiveDiagram)
//	{
//		HRESULT hr = S_OK;
//
//		try
//		{
//			CComPtr < IAxDrawingAreaControl > pDA;
//
//			if (m_DrawingArea)
//			{
//				m_DrawingArea->GetDrawingAreaControl(&pDA);
//
//				if (pDA)
//				{
//					_VH(SetReadOnly(VARIANT_FALSE));
//
//					CComQIPtr < IDrawingPropertyProvider > pProvider(pDA);
//					CComQIPtr < IDrawingPropertyProvider > pActiveProvider(pActiveDiagram);
//					if (pProvider && pActiveProvider)
//					{
//						CComPtr < IDrawingProperties > pActiveProperties;
//						long count = 0;
//
//						_VH(pProvider->ResetToDefaultResources());
//						_VH(pActiveProvider->get_DrawingProperties(&pActiveProperties));
//
//						ATLASSERT(pActiveProperties);
//						if (pActiveProperties)
//						{
//							pActiveProperties->get_Count(&count);
//						}
//
//						for (long i = 0 ; i < count ; i++)
//						{
//							CComPtr < IDrawingProperty > pThisProperty;
//
//							_VH(pActiveProperties->Item(i, &pThisProperty));
//                  
//							CComQIPtr < IFontProperty > pFontProperty(pThisProperty);
//							CComQIPtr < IColorProperty > pColorProperty(pThisProperty);
//
//							if (pFontProperty)
//							{
//								_VH(pProvider->SaveFont2(pFontProperty));
//							}
//							else if (pColorProperty)
//							{
//								_VH(pProvider->SaveColor2(pColorProperty));
//							}
//						}
//					}
//
//					_VH(SetReadOnly(VARIANT_TRUE));
//				}
//			}
//		}
//		catch( _com_error& err )
//		{
//			hr = COMErrorManager::ReportError( err );
//		}
//
//		return hr;
//	}
//	/**
//	 * This one initializes the drawing area by copying the open diagram's properties
//	 * to the diagram on this dialog, and then going through the presentation elements and
//	 * copying the properties off those presentation elements to the PE's on the diagram.
//	 */
//	HRESULT CBasicColorsAndFontsDialog::InitDrawingAreaToPEDefaults(IPresentationElements* pPEs)
//	{
//		HRESULT hr = S_OK;
//
//		try
//		{
//			CComPtr < IAxDrawingAreaControl > pDA;
//
//			if (m_DrawingArea)
//			{
//				m_DrawingArea->GetDrawingAreaControl(&pDA);
//				if (pDA)
//				{
//					CComPtr < IAxDrawingAreaControl> pActiveDiagram;
//
//					long numPEs = 0;
//					CComPtr < IPresentationElement > pFirstPE;
//
//					if (pPEs)
//					{
//						_VH(pPEs->get_Count(&numPEs));
//						if (numPEs)
//						{
//							_VH(pPEs->Item(0, &pFirstPE));
//							if (pFirstPE)
//							{
//								// get the active diagram
//								CComQIPtr < IGraphPresentation > pGraphPE(pFirstPE);
//								if (pGraphPE)
//								{
//									CComPtr < IDiagram > pDiagram;
//
//									pGraphPE->get_Diagram(&pDiagram);
//
//									CComQIPtr < IAxDiagram > pAxDiagram(pDiagram);
//									if (pAxDiagram)
//									{
//										_VH(pAxDiagram->get_DrawingArea(&pActiveDiagram));
//									}
//								}
//							}
//						}
//					}
//
//					CComQIPtr < IDrawingPropertyProvider > pDialogProvider(pDA);
//					CComQIPtr < IDrawingPropertyProvider > pProvider(pActiveDiagram);
//					if (pDialogProvider && pProvider && pFirstPE)
//					{
//						// Reset the diagram to the current diagram defaults
//						_VH(InitDrawingAreaToDiagramDefaults(pActiveDiagram));
//                                                                   
//						// Allow the DA to change
//						_VH(SetReadOnly(VARIANT_FALSE));
//
//						// Get a list of all the draw engines for the presentation elements
//						// this dialog was initialized with
//						CComPtr < IStrings > pAllDrawEngines;
//						long numDrawEngines = 0;
//
//						_VH(GetPEDrawEngines(pPEs, &pAllDrawEngines));
//						if (pAllDrawEngines)
//						{
//							_VH(pAllDrawEngines->get_Count(&numDrawEngines));
//						}
//
//						// Now for each draw engine type grab the first pe's property definitions
//						// and apply them to the dialogs diagram.
//						for (long i = 0 ; i < numDrawEngines ; i++)
//						{
//							CComBSTR sDrawEngine;
//							_VH(pAllDrawEngines->Item(i, &sDrawEngine));
//
//							if (sDrawEngine.Length())
//							{
//								CComPtr < IPresentationElements > pThisDEPEs;
//								CComPtr < IDrawingProperties > pPropertiesForTheFirstProvider;
//
//								// Get this presentation element and apply to the current object on the diagram
//								_VH(GetPEs(pPEs,
//											sDrawEngine, 
//											&pThisDEPEs,
//											&pPropertiesForTheFirstProvider));
//								if (pPropertiesForTheFirstProvider)
//								{
//									long numPropertiesForFirstProvider = 0;
//
//									_VH(pPropertiesForTheFirstProvider->get_Count(&numPropertiesForFirstProvider));
//
//									for (long j = 0 ; j < numPropertiesForFirstProvider ; j++)
//									{
//										CComPtr < IDrawingProperty > pThisProperty;
//
//										_VH(pPropertiesForTheFirstProvider->Item(j, &pThisProperty));
//                           
//										CComQIPtr < IFontProperty > pFontProperty(pThisProperty);
//										CComQIPtr < IColorProperty > pColorProperty(pThisProperty);
//
//										if (pFontProperty)
//										{
//											_VH(pDialogProvider->SaveFont2(pFontProperty));
//										}
//										else if (pColorProperty)
//										{
//											_VH(pDialogProvider->SaveColor2(pColorProperty));
//										}
//									}
//								}
//							}
//						}
//					}
//
//					_VH(SetReadOnly(VARIANT_TRUE));
//				}
//			}
//		}
//		catch( _com_error& err )
//		{
//			hr = COMErrorManager::ReportError( err );
//		}
//
//		return hr;
//	}
//	/**
//	 * Notification that something on the page changed
//	 */
//	HRESULT CBasicColorsAndFontsDialog::OnDrawingPropertyChanged(IDrawingProperty* pProperty)
//	{
//		HRESULT hr = S_OK;
//
//		try
//		{
//			if (pProperty && m_DrawingArea)
//			{
//				CComPtr < IAxDrawingAreaControl > pControl;
//				m_DrawingArea->GetDrawingAreaControl(&pControl);
//				if (pProperty && pControl)
//				{
//					// Set this drawing property on the dialogs diagram
//					CComQIPtr < IFontProperty > pFontProperty(pProperty);
//					CComQIPtr < IColorProperty > pColorProperty(pProperty);
//					CComQIPtr < IDrawingPropertyProvider > pProvider(pControl);
//
//					_VH(SetReadOnly(VARIANT_FALSE));
//					if (pProvider && pFontProperty)
//					{
//						_VH(pProvider->SaveFont2(pFontProperty));
//					}
//					else if (pProvider && pColorProperty)
//					{
//						_VH(pProvider->SaveColor2(pColorProperty));
//					}
//					_VH(pProvider->InvalidateProvider());
//					_VH(SetReadOnly(VARIANT_TRUE));
//				}
//			}
//		}
//		catch( _com_error& err )
//		{
//			hr = COMErrorManager::ReportError( err );
//		}
//
//		return hr;
//	}
//	/**
//	 * This routine shows/hides the "No Preview Image Available" text
//	 */
//	void CBasicColorsAndFontsDialog::ShowNoPreviewImageText(VARIANT_BOOL bShow)
//	{
//		CWnd* pWnd = GetDlgItem(IDC_PREVIEWIMAGE);
//		if (pWnd)
//		{
//			pWnd->ShowWindow(bShow?SW_SHOW:SW_HIDE);
//		}
//	}

	/**
	 * Populates our map of draw engine display names to draw engine ids
	 */
	public void populateDrawEngineNameMap()
	{
		if (m_pDrawEngines != null)
		{
			//	Populate the list
			int numDEs = m_pDrawEngines.size();
      
			if (numDEs > 0)
			{
                            // TODO: meteora
//				IPresentationResourceMgr pMgr = ProductHelper.getPresentationResourceMgr();
//				if (pMgr != null)
//				{
//					// Create a list of strings to add to the object list.  We
//					// sort the list so we also have to have a looking from the
//					// display name and the actual draw engine type
//					for (int i = 0 ; i < numDEs ; i++)
//					{
//						String sDE = m_pDrawEngines.get(i);
//						if (sDE != null && sDE.length() > 0)
//						{
//							String sDisplayName = "";
//							String sDescription = "";
//
//							// Convert that name to something more reasonable
//							ETPairT<String, String> val = pMgr.getDisplayName(sDE, null);
//							if (val != null)
//							{
//								sDisplayName = val.getParamOne();
//								sDescription = val.getParamTwo();
//							}
//
//							if (sDisplayName == null || sDisplayName.length() == 0)
//							{
//								sDisplayName = sDE;
//							}
//
//							m_pDrawEngineNames.put(sDisplayName, sDE);
//						}
//					}
//				}
			}
		}
	}
	
//	/**
//	 * Returns what is selected in the list (localized DE name, not the DE ID)
//	 */
//	xstring CBasicColorsAndFontsDialog::GetSelectedDisplayName()
//	{
//		xstring xsDE;
//
//		int nCurSel = m_ObjectList.GetCurSel();
//		if (nCurSel != LB_ERR)
//		{
//			CString sText;
//			m_ObjectList.GetText(nCurSel, sText);
//
//			if (sText.GetLength())
//			{
//				xsDE = sText;
//			}
//		}
//		return xsDE;
//	}

	/**
	 * Returns the selected draw engine (not the display name)
	 */
	public String getSelectedDE()
	{
		String xsDE = "";
		if (m_EngineList != null)
		{
			String sText = (String)m_EngineList.getSelectedValue();
			if (sText != null && sText.length() > 0)
			{
				xsDE = getDENameFromDisplayName(sText);
			}
		}
		return xsDE;
	}

	/**
	 * Resets the content on the object list
	 */
	public void resetObjectList()
	{      
		if (m_EngineList != null)
		{
			// Clear the current content in case we call this twice
			m_EngineList.removeAll();
		}
	}

//	/**
//	 * Returns true if we have an active object list
//	 */
//	bool CBasicColorsAndFontsDialog::HaveObjectList()
//	{
//		bool bHave = false;
//
//		if (m_ObjectList.m_hWnd)
//		{
//			bHave = true;
//		}
//
//		return bHave;
//	}

	/**
	 * Adds a string to the object list
	 */
	public void addObjectListString(String xsString, String xsIconType)
	{
		if (xsString != null)
		{
			boolean bAdded = false;
			Icon nIcon = null;

			if (xsIconType != null)
			{
				if (m_ResourceMgr == null)
				{
					m_ResourceMgr = CommonResourceManager.instance();
				}

				if (m_ResourceMgr != null)
				{
					nIcon = m_ResourceMgr.getIconForElementType(xsIconType);

					if (nIcon != null)
					{
						if (isFilteredFromObjectList(xsString) == false)
						{
							JLabel engineEntry = new JLabel(xsString);
							engineEntry.setIcon(nIcon);
							m_EngineList.add(engineEntry);
						}
						bAdded = true;
					}
				}
			}

			if (!bAdded)
			{
				if (isFilteredFromObjectList(xsString) == false)
				{
					JLabel engineEntry = new JLabel(xsString);
					m_EngineList.add(engineEntry);
				}
			}
		}
	}
	
	/** 
	 * Indicates that the filter has changed for the object list
	 */
	public void objectListFilterChanged(String sText)
	{
		if (sText != null && sText.length() > 0)
		{
			if (sText.equals(IDrawingProps.IDS_ALL))
				m_nDiagramKindFilterSetting = IDiagramKind.DK_ALL;
			else if (sText.equals(IDrawingProps.IDS_ACTIVITY_DIAGRAM))
				m_nDiagramKindFilterSetting = IDiagramKind.DK_ACTIVITY_DIAGRAM;
			else if (sText.equals(IDrawingProps.IDS_CLASS_DIAGRAM))
				m_nDiagramKindFilterSetting = IDiagramKind.DK_CLASS_DIAGRAM;
			else if (sText.equals(IDrawingProps.IDS_COLLABORATION_DIAGRAM))
				m_nDiagramKindFilterSetting = IDiagramKind.DK_COLLABORATION_DIAGRAM;
			else if (sText.equals(IDrawingProps.IDS_COMPONENT_DIAGRAM))
				m_nDiagramKindFilterSetting = IDiagramKind.DK_COMPONENT_DIAGRAM;
			else if (sText.equals(IDrawingProps.IDS_DEPLOYMENT_DIAGRAM))
				m_nDiagramKindFilterSetting = IDiagramKind.DK_DEPLOYMENT_DIAGRAM;
			else if (sText.equals(IDrawingProps.IDS_SEQUENCE_DIAGRAM))
				m_nDiagramKindFilterSetting = IDiagramKind.DK_SEQUENCE_DIAGRAM;
			else if (sText.equals(IDrawingProps.IDS_STATE_DIAGRAM))
				m_nDiagramKindFilterSetting = IDiagramKind.DK_STATE_DIAGRAM;
			else if (sText.equals(IDrawingProps.IDS_USECASE_DIAGRAM))
				m_nDiagramKindFilterSetting = IDiagramKind.DK_USECASE_DIAGRAM;
			//else if (sText.equals(IDrawingProps.IDS_ENTITY_DIAGRAM))
			//	m_nDiagramKindFilterSetting = IDiagramKind.DK_ENTITY_DIAGRAM;

			// Now add all our items
			resetObjectList();
			Vector elements = new Vector();
			Enumeration enumVal = m_pDrawEngineNames.keys();
			while (enumVal.hasMoreElements())
			{
				String xsDisplayName = (String)enumVal.nextElement();
				String xsIconType = m_pDrawEngineNames.get(xsDisplayName);

				//addObjectListString(xsDisplayName, xsIconType);
				if (!isFilteredFromObjectList(xsDisplayName))
				{
					elements.add(xsDisplayName);
				}
			}
			java.util.Collections.sort(elements);
			m_EngineList.setListData(elements);

			// Select the first item automatically
			selectInList(0);
		}
	}

	/**
	 * Selects the element in the list
	 */
	public void selectInList(int nIndex)
	{
		if (m_EngineList != null)
		{
			// Select the first item automatically
			if (nIndex >= 0 && nIndex < m_EngineList.getModel().getSize())
			{
				m_EngineList.setSelectedIndex(nIndex);
				onObjectListChange();
			}
		}
	}
	
//	/**
//	 * Selects a string in the list
//	 */
//	void CBasicColorsAndFontsDialog::SetSelectedDisplayName(xstring& xsDisplayName)
//	{
//		if (HaveObjectList() && xsDisplayName.c_str())
//		{
//			m_ObjectList.SelectString(-1, xsDisplayName.c_str());
//			OnObjectListChange();
//		}
//	}
//	/**
//	 * Given a dll and icon id, this routine loads the correct HICON.
//	 *
//	 * @param sIconLibrary [in] The dll loocation where the icon exists (in the resource file)
//	 * @param nIconID [in] The icon that exists in the dll location
//	 * @return The index into the image list for this icon
//	 */
//	int CBasicColorsAndFontsDialog::GetIcon(BSTR sIconLibrary, long nIconID)
//	{
//		USES_CONVERSION;
//		HICON hIcon  = 0;
//		TCHAR buffer[10];
//		int iconIndex = -1; // The default
//
//		_stprintf(buffer, _T("%d"), nIconID);
//
//		xstring key;
//
//		key = W2T(sIconLibrary);
//		key += _T("__");
//		key += buffer;
//
//		if (m_IconMap.find(key) == m_IconMap.end())
//		{
//			HINSTANCE hLib = LoadLibrary( W2T(sIconLibrary) );
//			if (hLib)
//			{
//				int     iCurrent = nIconID ;
//
//				hIcon = LoadIcon (hLib, 
//										MAKEINTRESOURCE (iCurrent)) ;
//
//				int newImage = m_ImageList.Add( hIcon );
//				if (newImage != -1)
//				{
//					iconIndex = newImage;
//				}
//				m_IconMap[ key ] = IconImageIndex(hIcon, (int)newImage);
//			}
//		}
//		else
//		{
//			iconIndex = m_IconMap[key].second;
//		}
//
//		return iconIndex;
//	}
	/**
	 * Adds a bunch of standard draw engines to m_StandardDrawEngines
	 */
	public void addStandardDrawEngines(int nKind, boolean bSelectFirstElement)
	{

		if (m_DiagramSelection.getItemCount() == 0)
		{
			// Add the list of diagrams to the combo box
			m_DiagramSelection.addItem(IDrawingProps.IDS_ALL);
			m_DiagramSelection.addItem(IDrawingProps.IDS_ACTIVITY_DIAGRAM);
			m_DiagramSelection.addItem(IDrawingProps.IDS_CLASS_DIAGRAM);
			m_DiagramSelection.addItem(IDrawingProps.IDS_COLLABORATION_DIAGRAM);
			m_DiagramSelection.addItem(IDrawingProps.IDS_COMPONENT_DIAGRAM);
			m_DiagramSelection.addItem(IDrawingProps.IDS_DEPLOYMENT_DIAGRAM);
			//m_DiagramSelection.addItem(IDrawingProps.IDS_ENTITY_DIAGRAM);
			m_DiagramSelection.addItem(IDrawingProps.IDS_SEQUENCE_DIAGRAM);
			m_DiagramSelection.addItem(IDrawingProps.IDS_STATE_DIAGRAM);
			m_DiagramSelection.addItem(IDrawingProps.IDS_USECASE_DIAGRAM);
		}
      
		if (bSelectFirstElement)
		{
			m_DiagramSelection.setSelectedIndex(0);
		}

		// Now create a list of the default draw engines for each diagram type
		ETList<String> iterator = m_StandardDrawEngines.get(new Integer(nKind));
		if (iterator != null)
		{
			m_StandardDrawEngines.remove(iterator);
		}

		// Get from the presentation resource manager and add to our list
                // TODO: meteora
//		IPresentationResourceMgr pMgr = ProductHelper.getPresentationResourceMgr();
//		if (pMgr != null)
//		{
//			ETList<String> pStandardDrawEngines = pMgr.getStandardDrawEngines(nKind);
//			if (pStandardDrawEngines != null)
//			{
//				m_StandardDrawEngines.put(new Integer(nKind), pStandardDrawEngines);
//			}
//		}
	}
	
	/**
	 * Is this draw engine name filtered from our list by the diagrams combo box?
	 */
	public boolean isFilteredFromObjectList(String xsDisplayName)
	{
		boolean bFiltered = false;

		if (m_nDiagramKindFilterSetting != IDiagramKind.DK_ALL &&
			 m_nDiagramKindFilterSetting != IDiagramKind.DK_UNKNOWN)
		{
			bFiltered = true;
			String xsDEName = getDENameFromDisplayName(xsDisplayName);
			if (xsDEName != null && xsDEName.length() > 0)
			{
				ETList<String> iterator = m_StandardDrawEngines.get(new Integer(m_nDiagramKindFilterSetting));
				if (iterator != null)
				{
					for (int i = 0; i < iterator.size(); i++)
					{
						String val = iterator.get(i);
						if (val != null && val.equals(xsDEName))
						{
							bFiltered = false;
							break;
						}
					}
				}
			}
		}

		return bFiltered;
	}
	
	public void onCbnSelchangeDiagramtype()
	{
		String sText = m_DiagramSelection.getSelectedItem().toString();
		objectListFilterChanged(sText);
	}
	
	class ElementListCellRenderer extends JLabel implements ListCellRenderer 
	{
		public Icon getImageIcon(String iconType)
		{
			Icon retIcon = null;
			if (iconType != null)
			{
				if (m_ResourceMgr == null)
				{
					m_ResourceMgr = CommonResourceManager.instance();
				}

				if (m_ResourceMgr != null)
				{
					retIcon = m_ResourceMgr.getIconForElementType(iconType);
				}
			}

			return retIcon;
		}

		public Component getListCellRendererComponent(
			JList list,
			Object value,            // value to display
			int index,               // cell index
			boolean isSelected,      // is the cell selected
			boolean cellHasFocus)    // the list and the cell have the focus
		 {
			 String s = value.toString();
			 setText(s);
			 setIcon(getImageIcon(s));
			 if (isSelected) 
			 {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			 }
			 else 
			 {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			 }
			 setEnabled(list.isEnabled());
			 setFont(list.getFont());
			 setOpaque(true);
			 return this;
		 }
	 }
}
