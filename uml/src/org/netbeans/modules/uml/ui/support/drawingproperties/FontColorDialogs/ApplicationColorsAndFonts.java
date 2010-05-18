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

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JDialog;

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.drawingproperties.DrawingPropertyResource;
import org.netbeans.modules.uml.ui.support.drawingproperties.ETFontType;
import org.netbeans.modules.uml.ui.support.drawingproperties.IColorProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingPropertyProvider;
import org.netbeans.modules.uml.ui.support.drawingproperties.IFontProperty;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;

/**
 * @author jingmingm
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ApplicationColorsAndFonts extends BasicColorsAndFontsDialog
{
   public ApplicationColorsAndFonts()
	{
		super();
	}
        
	public ApplicationColorsAndFonts(JDialog dia)
	{
		super(dia);
	}
	
	public void onObjectListSelected(String sSelection)
	{
		showPropertyPage(sSelection);

		// Initialize to application defaults
		//InitDrawingAreaToApplicationDefaults();

		// Now apply the changes the user has made
		//ApplyChangedPropertiesToCurrentDiagram();
	}
	
	/**
	 * Initialize the dialogs
	 */
	public boolean onInitDialog()
	{
		super.onInitDialog();
		setTitle(DrawingPropertyResource.getString("IDS_TITLE"));
		populateObjectListBox();
		return true;
	}
	
	/**
	 * Populates the object list box
	 */
	public void populateObjectListBox()
	{
		// Go through the presentation resource manager and display all the draw engines
		if (m_EngineList != null)
		{
			// Clear the current content in case we call this twice
			resetObjectList();
			m_pDrawEngines.clear();
                        // TODO: meteora

//			IPresentationResourceMgr pMgr = ProductHelper.getPresentationResourceMgr();
//			if (pMgr != null)
//			{
//				m_pDrawEngines = pMgr.getAllDrawEngineNames();

				// Populate our map of draw engine name to draw engine id
				populateDrawEngineNameMap();

				// Now populate the windows control
				Vector elements = new Vector();
				Enumeration enumVal = m_pDrawEngineNames.keys();
				while (enumVal.hasMoreElements())
				{
					String xsDisplayName = (String)enumVal.nextElement();
					String xsDrawEngineID = (String)m_pDrawEngineNames.get(xsDisplayName);

					//addObjectListString(xsDisplayName, xsDrawEngineID);
					elements.add(xsDisplayName);
				}
				java.util.Collections.sort(elements);
				m_EngineList.setListData(elements);

				// Select the first item automatically
				selectInList(0);
//			}
		}
	}
	
	/**
	 * Apply button was clicked.
	 */
	public void onBnClickedApply()
	{
		if (m_ChangedProperties != null && m_ChangedProperties.size() > 0)
		{
			// Save all the property pages
			//saveAllChangedPropertySheets();
			saveChangedProperties();
			
			// Now save the presentation types files
                        // TODO: meteora
//			IPresentationResourceMgr pMgr = ProductHelper.getPresentationResourceMgr();
//			if (pMgr != null)
//			{
//				pMgr.saveOverriddenResources();
//	
//				// Now ask if the user wishes to reset any open diagrams
//				IProductDiagramManager pDiagramManager = ProductHelper.getProductDiagramManager();
//				if (pDiagramManager != null)
//				{
//					ETList<IProxyDiagram> pOpenDiagrams = pDiagramManager.getOpenDiagrams();
//					int count = 0;
//					if (pOpenDiagrams != null)
//					{
//						count = pOpenDiagrams.size();
//					}
//	
//					if (count > 0)
//					{
//						String title = DrawingPropertyResource.getString("IDS_RESETDIAGRAMS");
//						String msg = DrawingPropertyResource.getString("IDS_RESETDIAGRAMS_MSG");
//						SwingQuestionDialogImpl dialog = new SwingQuestionDialogImpl();
//						QuestionResponse response = dialog.displaySimpleQuestionDialogWithCheckbox(SimpleQuestionDialogKind.SQDK_YESNO, 0, msg, "", title, 0, false);
//						if (response != null)
//						{
//							int result = response.getResult();
//							if (result == SimpleQuestionDialogResultKind.SQDRK_RESULT_YES)
//							{
//								for (int i = 0 ; i < count ; i++)
//								{
//									IProxyDiagram pThisDiagram = pOpenDiagrams.get(i);
//									if (pThisDiagram != null)
//									{
//										IDiagram pDiagram = pThisDiagram.getDiagram();
//										if (pDiagram instanceof IUIDiagram)
//										{
//											IUIDiagram pAxDiagram = (IUIDiagram)pDiagram;
//											IDrawingAreaControl pControl = pAxDiagram.getDrawingArea();
//											if (pControl != null && pControl instanceof IDrawingPropertyProvider)
//											{
//												IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider)pControl;
//												
//												// Reset the drawing area
//												pProvider.resetToDefaultResources();
//												pProvider.invalidateProvider();
//											}
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
	   
			// Clear our list of changed properties
			clearChangedProperties();
		}
	}

//	/**
//	 * Notification that one of the properties (font or color) in the BCG list
//	 * has changed.
//	 */
//	LRESULT CMFCApplicationColorsAndFonts::OnDrawingPropertyChanged(WPARAM wParam, LPARAM lParam)
//	{
//		HRESULT hr = S_OK;
//
//		try
//		{
//			CComQIPtr < IDrawingProperty > pDrawingProperty = (IDrawingProperty*)(lParam);
//
//			if (pDrawingProperty)
//			{
//				// Add to our list
//				_VH(AddChangedProperty(pDrawingProperty));
//
//				_VH(CBasicColorsAndFontsDialog::OnDrawingPropertyChanged(pDrawingProperty));
//			}
//
//			// Since this dialog has the capability of switching between the various
//			// diagram types, we need to keep track of the changes and when the
//			// diagram comes up apply those changes to the current diagram, otherwise
//			// changes that took place before the diagram was displayed would not
//			// take affect.
//		}
//		catch( _com_error& err )
//		{
//			hr = COMErrorManager::ReportError( err );
//		}
//
//		return 0;
//	}

	/**
	 * We keep track of all changes between ok/apply/cancel
	 */
	public void addChangedProperty(IDrawingProperty pProperty)
	{
		if (m_ChangedProperties != null && pProperty != null)
		{
			boolean bFound = false;
			for (int i = 0; i < m_ChangedProperties.size(); i++)
			{
				IDrawingProperty pDrawingProperty = m_ChangedProperties.get(i);
				if (pDrawingProperty != null && pDrawingProperty.equals(pProperty))
				{
					bFound = true;
					break;
				}
			}
			if (!bFound)
			{
				m_ChangedProperties.add(pProperty);
			}
		}
	}

	/**
	 * Save changed properties
	 */
	public void saveChangedProperties()
	{
		if (m_ChangedProperties != null)
		{
			int count = m_ChangedProperties.size();
			for (int i = 0 ; i < count ; i++)
			{
				IDrawingProperty pProperty = m_ChangedProperties.get(i);
				String sEngineName = pProperty.getDrawEngineName();
				if (pProperty instanceof IColorProperty)
				{
					IColorProperty pColorProperty = (IColorProperty)pProperty;					
					String sResourceName = pColorProperty.getResourceName();
					int nColor = pColorProperty.getColor();
					if (sResourceName != null && sResourceName.length() > 0)
					{
						saveColorProperty(sEngineName, sResourceName,	nColor);
					}
				}
				else if (pProperty instanceof IFontProperty)
				{
					IFontProperty pFontProperty = (IFontProperty)pProperty;
					String sResourceName = pFontProperty.getResourceName();
					if (sResourceName != null && sResourceName.length() > 0)
					{
						saveFontProperty(sEngineName, sResourceName, pFontProperty);
					}
				}
			}
		}
	}
		
	/**
	 * Clear changed properties
	 */
	public void clearChangedProperties()
	{
		m_ChangedProperties.clear();
	}

//	/**
//	 * Apply the changed properties to the current diagram
//	 */
//	HRESULT CMFCApplicationColorsAndFonts::ApplyChangedPropertiesToCurrentDiagram()
//	{
//		HRESULT hr = S_OK;
//
//		try
//		{
//			if (m_ChangedProperties &&
//				 m_DrawingArea &&
//				 m_DrawingArea->m_hWnd)
//			{
//				CComPtr < IAxDrawingAreaControl > pControl;
//
//				_VH(m_DrawingArea->GetDrawingAreaControl(&pControl));
//         
//				CComQIPtr < IDrawingPropertyProvider > pProvider(pControl);
//				if (pProvider)
//				{
//					long count = 0;
//
//					_VH(m_ChangedProperties->get_Count(&count));
//
//					_VH(SetReadOnly(VARIANT_FALSE));
//					for (long i = 0 ; i < count ; i++)
//					{
//						CComPtr < IDrawingProperty > pProperty;
//						_VH(m_ChangedProperties->Item(i, &pProperty));
//               
//						CComQIPtr < IFontProperty >  pFontProperty(pProperty);
//						CComQIPtr < IColorProperty > pColorProperty(pProperty);
//						if (pFontProperty)
//						{
//							_VH(pProvider->SaveFont2(pFontProperty));
//						}
//						else if (pColorProperty)
//						{
//							_VH(pProvider->SaveColor2(pColorProperty));
//						}
//					}
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

	public void onCbnSelchangeDiagramtype()
	{
		super.onCbnSelchangeDiagramtype();
	}
	
	/**
	 * Saves a color property
	 */
	public void saveColorProperty(String sEngineName, String sResource, int nNewColor)
	{
			//if (m_pDiagram == 0 && m_pPEs == 0)
			{
				// Make the change
                            // TODO: meteora
//				IPresentationResourceMgr pMgr = ProductHelper.getPresentationResourceMgr();
//				if (pMgr != null)
//				{
//					pMgr.saveOverriddenColorResource(sEngineName, sResource, nNewColor);
//				}
			}
//			else if (m_pDiagram)
//			{
//				// We're controlling the diagram
//				CComPtr < IAxDrawingAreaControl > pControl;
//				CComQIPtr < IAxDiagram > pAxDiagram(m_pDiagram);
//				if (pAxDiagram)
//				{
//					_VH(pAxDiagram->get_DrawingArea(&pControl));
//				}
//
//				ATLASSERT(pControl);
//				if (pControl)
//				{
//					CComQIPtr < IDrawingPropertyProvider > pProvider(pControl);
//					if (pProvider)
//					{
//						_VH(pProvider->SaveColor(m_sDrawEngineName, 
//														 sResource,
//														 nNewColor));
//					}
//
//					_VH(pControl->Refresh(VARIANT_TRUE));
//				}
//			}
//			else if (m_pPEs)
//			{
//				// Apply the changed colors to the presentation elements
//				_VH(ApplyToPresentationElements(sResource,
//														  nNewColor));
//			}
	}
	
	/**
	 * Saves a font property
	 */
	public void saveFontProperty(String sEngineName, String sResource, IFontProperty pFontProperty)
	{
		//if (m_pDiagram == 0 && m_pPEs == 0)
		{
			// Make the change
                    // TODO: meteora
//			IPresentationResourceMgr pMgr = ProductHelper.getPresentationResourceMgr();
//			if (pMgr != null)
//			{
//				String sFaceName = pFontProperty.getFaceName();
//				int nSize = pFontProperty.getSize();
//				int nColor = pFontProperty.getColor();
//				boolean bItalic = pFontProperty.getItalic();
//				int nWeight = pFontProperty.getWeight();
//				
//				ETFontType val = new ETFontType();
//				val.setName(sFaceName);
//				val.setHeight(nSize);
//				val.setWeight(nWeight);
//				val.setItalic(bItalic);	
//				val.setColor(nColor);
//				
//				pMgr.saveOverriddenFontResource(sEngineName, sResource, val);
//			}
		}
//		else if (m_pDiagram)
//		{
//			// We're controlling the diagram
//			CComPtr < IAxDrawingAreaControl > pControl;
//			CComQIPtr < IAxDiagram > pAxDiagram(m_pDiagram);
//			if (pAxDiagram)
//			{
//				_VH(pAxDiagram->get_DrawingArea(&pControl));
//			}
//
//			ATLASSERT(pControl);
//			if (pControl)
//			{
//				CComQIPtr < IDrawingPropertyProvider > pProvider(pControl);
//				if (pProvider)
//				{
//					_VH(pProvider->SaveFont2(pFontProperty));
//				}
//			}
//		}
//		else if (m_pPEs)
//		{
//			// Apply the changed fonts to the presentation elements
//			_VH(ApplyToPresentationElements(sResource,
//													  pFontProperty));
//		}
	}
}
