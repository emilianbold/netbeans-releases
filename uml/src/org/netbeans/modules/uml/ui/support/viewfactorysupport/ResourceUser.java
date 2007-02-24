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
 * Created on Feb 5, 2004
 *
 */
package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import java.awt.Font;
import java.awt.Stroke;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Hashtable;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.drawingproperties.ColorProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.FontProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IColorProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingPropertyProvider;
import org.netbeans.modules.uml.ui.support.drawingproperties.IFontProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author jingmingm
 *
 */
public abstract class ResourceUser //implements IDrawingPropertyProvider
{
	public static int UK_BORDERCOLOR = 1;
	public static int UK_FILLCOLOR = 2;
	public static int UK_TEXTCOLOR =4;
	public static int UK_FONT = 8;
	public static int UK_TITLEFONT = 16;

	protected ResourceMgr m_pResourceMgr = null; 
	public Hashtable<Integer, Integer> m_Fonts = new Hashtable<Integer, Integer>();
	public Hashtable<Integer, Integer> m_Colors = new Hashtable<Integer, Integer>();
	protected Hashtable<Integer, Integer> m_ColorsToUpgrade = new Hashtable<Integer, Integer>();
	protected Hashtable<Integer, FontToUpgrade> m_FontsToUpgrade = new Hashtable<Integer, FontToUpgrade>();
	public int m_nDrawEngineStringID = -1;
	
	private WeakReference m_ResourceUserHelper = null;
   
   public ResourceUser( IResourceUserHelper resourceUserHelper )
   {
      m_ResourceUserHelper = new WeakReference( resourceUserHelper );
   }
   
   protected IResourceUserHelper getResourceUserHelper()
   {
      return (m_ResourceUserHelper != null) ? (IResourceUserHelper)m_ResourceUserHelper.get() : null;
   }
	
	/*
	public IDrawingAreaControl getDrawingArea()
	{
		return null;
	}
	
	public int getColorID(int nColorStringID)
	{
		return -1;
	}
	
	public int getFontID(int nFontStringID)
	{
		return -1;
	}
	
	public boolean verifyDrawEngineStringID()
	{
		return false;
	}
	*/
	
	public ResourceMgr getResourceMgr()
	{
		// Watch for deleted/closed diagrams due invokeLater.
		//if(m_pResourceMgr == null)
		{
			IDrawingAreaControl pDrawingArea = getResourceUserHelper().getDrawingArea();
			if (pDrawingArea != null)
			{
				// Get the device manager off the diagram
				if(pDrawingArea != null)
				{
					m_pResourceMgr = ResourceMgr.instance(pDrawingArea);
				}
			}
		}  // m_pResourceMgr == NULL
	
		return m_pResourceMgr;
	}

	public void clearResourceManager()
	{
		m_pResourceMgr = null;
	}

	public int getintForStringID(int nStringID)
	{
		int nFoundColor = 0;
		int nColorID = -1;
	
		if (m_nDrawEngineStringID == -1)
		{
			// We haven't called init resources yet, so return black
			nFoundColor = 0;
		}
		else
		{
			// Get the color
			nColorID = getResourceUserHelper().getColorID(nStringID);
			nFoundColor = getintForColorID(nColorID);
		}
		return nFoundColor;
	}

	public int getintForColorID(int nColorID)
	{
		int nFoundREF = 0;
		nFoundREF = getResourceMgr().getColor(nColorID);
		return nFoundREF;
	}

	public Font getZoomedFontForStringID(int nStringID, double nDrawZoom /*= -1.0*/ )
	{
		int nFontID = getResourceUserHelper().getFontID(nStringID);
		return nFontID != -1 ? getZoomedFontForFontID(nFontID, nDrawZoom) : null;
	}

	public Font getZoomedFontForFontID(int nFontID, double nDrawZoom /*= -1.0*/ )
	{
		return getResourceMgr().getZoomedFont(nFontID, nDrawZoom);
	}

	public int setResourceStringID(int nStringID, String sNameFont)
	{
		int newid = nStringID;
		if (nStringID == -1 && getResourceMgr() != null)
		{
			// We have an uninitialized resource string, initialize it.
			if (sNameFont != null && getResourceMgr() != null )
			 {
				newid = getResourceMgr().getStringID(sNameFont);
			 }
		  }
		return newid;
	}

	public int setResourceStringID(int nStringID, 
									String sStringInResourceMgrTable,
									int nDefaultColor)
	{
		int newid = nStringID;
		if (nStringID == -1)
		{
			// We have an uninitialized resource string, initialize it.

			// Set the resource string
			newid = setResourceStringID(nStringID, sStringInResourceMgrTable);

			// Set the default color
			setDefaultColor(sStringInResourceMgrTable, nDefaultColor);
		}
		
		return newid;
	}

	public void setDefaultColor(int nResourceStringID, int nColor)
	{
		if (nResourceStringID != -1)
		{
			/// Returns the string the corresponds to the string id.
			if (getResourceMgr() != null)
			{
				String sStringName = getResourceMgr().getString(nResourceStringID);
				// Set the default color
				setDefaultColor(sStringName, nColor);
			}
		}
	}

	public void setDefaultColor(String sResourceString, int nColor)
	{
		if (getResourceUserHelper().verifyDrawEngineStringID())
		{
			if (sResourceString != null)
			{
				if (getResourceMgr() != null)
				{
					int nID = getResourceMgr().getStringID(sResourceString);
					getResourceMgr().setDefaultColor(m_nDrawEngineStringID, nID, nColor);
				}
			}
		}
	}

	public Stroke getBrush(int nStringID)
	{
		Stroke pBrush = null;

		if (nStringID != -1)
		{
			int nColor = getintForStringID(nStringID);
			//pBrush = new Stroke(nColor);
		}

		return pBrush;
	}

	public void writeResourcesToArchive(IProductArchive pProductArchive, 
										IProductArchiveElement pParentElement)
	{
		ResourceMgr pResourceManager = getResourceMgr();

		if (pResourceManager != null)
		{
			// Create a resource element to hold the font/color information
			IProductArchiveElement pResourcesElement = pParentElement.createElement(IProductArchiveDefinitions.RESOURCES_STRING);
			
			// Populate it with our color/font tables (m_Fonts and m_Colors)
			if(pResourcesElement != null)
			{
				Enumeration enumVal1 = m_Fonts.keys();
				// Go from the back to the front so if the list changes we don't pick up the change
				while (enumVal1.hasMoreElements())
				{
					Integer obj = (Integer)enumVal1.nextElement();
					Integer val = m_Fonts.get(obj);
					// Use GetFontID to verify that the FONTID we've cached is the correct one
					int nStringID = Integer.parseInt(obj.toString());
					int nID = getResourceUserHelper().getFontID(nStringID);
					if (nID != -1)
					{
						IProductArchiveElement pFontElement = pResourcesElement.createElement(IProductArchiveDefinitions.RESOURCE_STRING);
						if (pFontElement != null)
						{
							pFontElement.addAttributeString(IProductArchiveDefinitions.TYPE_STRING, IProductArchiveDefinitions.FONT_STRING);
							pFontElement.addAttributeLong(IProductArchiveDefinitions.RESOURCESTRINGID_STRING, nStringID);
							pFontElement.addAttributeLong(IProductArchiveDefinitions.FONTID_STRING, nID);
						}
					}
				}
				Enumeration enumVal2 = m_Colors.keys();
				while (enumVal2.hasMoreElements())
				{
					Integer obj = (Integer)enumVal2.nextElement();
					Integer val = m_Colors.get(obj);
					
					// Use GetColorID to verify that the COLORID we've cached is the correct one
					int nStringID = Integer.parseInt(obj.toString());
					int nID = getResourceUserHelper().getColorID(nStringID);
					if (nID != -1)
					{
						IProductArchiveElement pColorElement = pResourcesElement.createElement(IProductArchiveDefinitions.RESOURCE_STRING);
						if (pColorElement != null)
						{
							pColorElement.addAttributeString(IProductArchiveDefinitions.TYPE_STRING, IProductArchiveDefinitions.COLOR_STRING);
							pColorElement.addAttributeLong(IProductArchiveDefinitions.RESOURCESTRINGID_STRING, nStringID);
							pColorElement.addAttributeLong(IProductArchiveDefinitions.COLORID_STRING, nID);
						}
					}
				}
			}
		}
	}

	public void readResourcesFromArchive(IProductArchive pProductArchive,  
										 IProductArchiveElement pParentElement)
	{
		IProductArchiveElement pResourcesElement = pParentElement.getElement(IProductArchiveDefinitions.RESOURCES_STRING);
		if (pResourcesElement != null)
		{
			IProductArchiveElement[] pChildElements = pResourcesElement.getElements();
			if( pChildElements != null)
			{
				for( int i = 0; i < pChildElements.length; i++ )
				{
					IProductArchiveElement pElement = pChildElements[i];
					if( pElement != null)
					{
						String sType = pElement.getAttributeString(IProductArchiveDefinitions.TYPE_STRING);
						if (sType.equals(IProductArchiveDefinitions.COLOR_STRING))
						{
							int nResourceStringID = (int)pElement.getAttributeLong(IProductArchiveDefinitions.RESOURCESTRINGID_STRING);
							int nColorIDString = (int)pElement.getAttributeLong(IProductArchiveDefinitions.COLORID_STRING);
							m_Colors.put(new Integer(nResourceStringID), new Integer(nColorIDString));
						}
						else if (sType.equals(IProductArchiveDefinitions.FONT_STRING))
						{
							int nResourceStringID = (int)pElement.getAttributeLong(IProductArchiveDefinitions.RESOURCESTRINGID_STRING);
							int nFontIDString = (int)pElement.getAttributeLong(IProductArchiveDefinitions.FONTID_STRING);
							m_Fonts.put(new Integer(nResourceStringID), new Integer(nFontIDString));
						}
					}
				}
			}
		}
		else
		{
			// We have an upgrade situation
			handleUpgrade(pProductArchive, pParentElement);
		}
	}

	public void handleUpgrade(IProductArchive pProductArchive,  
							  IProductArchiveElement pParentElement)
	{
//		/** Here's an example draw engine saved with Describe 6.1
//		  <engine name="ClassDrawEngine" editLocked="0">
//				<resourceID internalName="11" kind="3"/>
//				<compartment name="1" MEID="DCE.79A28E61-6929-4BA0-AA16-59EE467E58B2" value="ClassNames" textStyle="34853" DefaultCompartment="ADClassNameCompartment" staticText="">
//					<resourceID inherited="1" kind="0"/>
//					<resourceID inherited="1" kind="3"/>
//					<resourceID internalName="26" kind="5"/>
//					<resourceID internalName="27" kind="6"/>
//					<compartment name="2" MEID="DCE.79A28E61-6929-4BA0-AA16-59EE467E58B2" value="Unnamed" textStyle="34853" nameCompartmentBorderKind="0">
//						<resourceID inherited="1" kind="0"/>
//						<resourceID inherited="1" kind="3"/>
//						<resourceID internalName="28" kind="5"/>
//					</compartment>
//				</compartment>
//				<compartment name="3" value="Attributes" textStyle="34853">
//					<resourceID inherited="1" kind="0"/>
//					<resourceID inherited="1" kind="3"/>
//					<resourceID inherited="1" kind="5"/>
//					<resourceID internalName="27" kind="6"/>
//				</compartment>
//				<compartment name="4" value="Operations" textStyle="34853">
//					<resourceID inherited="1" kind="0"/>
//					<resourceID inherited="1" kind="3"/>
//					<resourceID inherited="1" kind="5"/>
//					<resourceID internalName="27" kind="6"/>
//				</compartment>
//			</engine>
//		  **/
//
//		  // now read the stored elements describing our textfield's overridden resources
//		  < IProductArchiveElements > pFieldElements;
//		  _VH( pParentElement->get_Elements( &pFieldElements ));
//		  if( pFieldElements )
//		  {
//			 USES_CONVERSION;
//			 int nCount = 0;
//			 _VH( pFieldElements->get_Count( &nCount ));
//			 for( int i = 0; i < nCount; i++ )
//			 {
//				< IProductArchiveElement > pElement;
//				_VH( pFieldElements->Item( i, &pElement ));
//				if( pElement )
//				{
//				// get name or type
//				String strValue;
//
//				_VH( pElement->get_ID( &strValue ));
//				if( strValue == RESOURCEELEMENT_STRING )
//				{
//					  // We found a "resourceID" element.  Get kind and 
//					  // whether or not its inherited
//					  enum ResourceIDKind
//					  {
//						 CK_FIRSTCOLOR,
//						 CK_TEXTCOLOR = CK_FIRSTCOLOR,
//						 CK_BACKCOLOR,
//						 CK_BORDERCOLOR,
//						 CK_FILLCOLOR,
//						 // add colors above this line
//						 CK_LASTCOLOR,
//						 CK_FIRSTFONT,
//						 CK_FONT = CK_FIRSTFONT,
//						 CK_TITLEFONT,
//						 // add fonts above this line
//						 CK_LASTFONT
//					  };
//
//					  ResourceIDKind nKind;
//					  int nInheritValue = 0;
//					   < IProductArchiveAttribute > pInheritedAttr;
//
//					  strValue.Empty();
//					  _VH(pElement->GetAttributeint(String(RESOURCEKIND_STRING), 
//													 (int*)(&nKind), 
//													 0));
//					  _VH(pElement->GetAttributeString(String(RESOURCENAME_STRING), 
//													&strValue, 
//													0));
//					  _VH(pElement->GetAttributeint(String(RESOURCEKIND_INHERITED_STRING), 
//													&nInheritValue, 
//													&pInheritedAttr));
//
//					  if (pInheritedAttr)
//					  {
//						 // Do nothing
//					  }
//					  else if (strValue.Length())
//					  {
//						 xstring sTableName;
//						 bool bIsColor = false;
//						 bool bIsFont  = false;
//
//						 if( nKind >= CK_FIRSTCOLOR && nKind < CK_LASTCOLOR )
//						 {
//							sTableName = RESOURCECOLORTABLE_STRING;
//							bIsColor = true;
//						 }
//						 else if( nKind >= CK_FIRSTFONT && nKind < CK_LASTFONT )
//						 {
//							sTableName = RESOURCEFONTTABLE_STRING;
//							bIsFont = true;
//						 }
//
//						  < IProductArchiveElement > pFoundTableElement;
//						 if( sTableName.length() )
//						 {
//							strValue.Empty();
//							_VH(pProductArchive->GetTableEntry3(pElement, 
//																String(RESOURCENAME_STRING),
//																String(sTableName.c_str()),
//																&pFoundTableElement,
//																&strValue));
//						 }
//
//						 if (pFoundTableElement)
//						 {
//							// Handle colors here
//							if (bIsColor)
//							{
//							xstring xsTemp(SAFEW2T(strValue));
//
//							xstring::size_type pos = xsTemp.find(_T("CLR"));
//							if( pos != xstring::npos )
//							{
//								  int lColor = 0;
//								   < IProductArchiveAttribute > pColorAttribute;
//
//								  _VH(pFoundTableElement->GetAttributeint(String(RESOURCECOLORVALUE_STRING), 
//																		&lColor, 
//																		&pColorAttribute));
//								  // We have a changed color
//								  if (pColorAttribute)
//								  {
//									 if (nKind == CK_BORDERCOLOR)
//									 {
//										m_ColorsToUpgrade[UK_BORDERCOLOR] = (int)(lColor);
//									 }
//									 else if (nKind == CK_FILLCOLOR)
//									 {
//										m_ColorsToUpgrade[UK_FILLCOLOR] = (int)(lColor);
//									 }
//									 else if (nKind == CK_TEXTCOLOR)
//									 {
//										m_ColorsToUpgrade[UK_TEXTCOLOR] = (int)(lColor);
//									 }
//								  }
//							}
//							}
//							else if (bIsFont)
//							{
//							xstring xsTemp(SAFEW2T(strValue));
//
//							xstring::size_type posCLR = xsTemp.find(_T("CLR"));
//							xstring::size_type posFont = xsTemp.find(_T("Font"));
//							// Make sure Font and CLR are not in the string
//							if (posCLR  == xstring::npos &&
//									 posFont == xstring::npos)
//							{
//								   < IProductArchiveAttribute > pNameAttribute;
//								   < IProductArchiveAttribute > pHeightAttribute;
//								   < IProductArchiveAttribute > pWeightAttribute;
//								   < IProductArchiveAttribute > pItalicAttribute;
//
//								  String sFontName;
//								  int nCharset = 0;
//								  int nHeight  = 0;
//								  int nItalic  = 0;
//								  int nStrikeout  = 0;
//								  int nUnderline  = 0;
//								  int nWeight  = 0;
//								  int nColor= 0;
//
//								  _VH(pFoundTableElement->GetAttributeString( String( RESOURCEFONTNAME_STRING), &sFontName , &pNameAttribute));
//								  _VH(pFoundTableElement->GetAttributeint(String( RESOURCEFONTCHARSET_STRING), &nCharset  , 0  ));
//								  _VH(pFoundTableElement->GetAttributeint(String( RESOURCEFONTHEIGHT_STRING ), &nHeight, &pHeightAttribute ));
//								  _VH(pFoundTableElement->GetAttributeint(String( RESOURCEFONTITALIC_STRING ), &nItalic, &pItalicAttribute ));
//								  _VH(pFoundTableElement->GetAttributeint(String( RESOURCEFONTSTRIKEOUT_STRING ), &nStrikeout, 0  ));
//								  _VH(pFoundTableElement->GetAttributeint(String( RESOURCEFONTUNDERLINE_STRING ), &nUnderline, 0  ));
//								  _VH(pFoundTableElement->GetAttributeint(String( RESOURCEFONTWEIGHT_STRING ), &nWeight, &pWeightAttribute ));
//								  _VH(pFoundTableElement->GetAttributeint(String( RESOURCEFONTCOLOR_STRING  ), &nColor , 0  ));
//
//								  if (pNameAttribute && pHeightAttribute && pWeightAttribute && pItalicAttribute)
//								  {
//									 USES_CONVERSION;
//
//									 // hack for older systems that stored height in logical units instead of points
//									 if( nHeight < 0 )
//									 {
//										nHeight = CGDISupport::LogicalUnitsToPoints( nHeight );
//									 }
//
//									 // Now load our table
//									 CResourceMgr::CFontToUpgrade* pNewFont = new CResourceMgr::CFontToUpgrade;
//
//									 pNewFont->m_sFontName = sFontName;
//									 pNewFont->m_nCharset = nCharset;
//									 pNewFont->m_nHeight = nHeight;
//									 pNewFont->m_nItalic = nItalic;
//									 pNewFont->m_nStrikeout = nStrikeout;
//									 pNewFont->m_nUnderline = nUnderline;
//									 pNewFont->m_nWeight = nWeight;
//									 pNewFont->m_nColor = nColor;
//									 if (nKind == CK_FONT)
//									 {
//										m_FontsToUpgrade[UK_FONT] = pNewFont;
//									 }
//									 else if (nKind == CK_TITLEFONT)
//									 {
//										m_FontsToUpgrade[UK_TITLEFONT] = pNewFont;
//									 }
//								  }
//							}
//							}
//						 }
//					  }
//				}
//				}
//			 }
//		  }
//	}
//	catch( _com_error& err )
//	{
//		  hr = COMErrorManager::ReportError( err );
//	}
//
//	return hr;
	}

	public String getPreferenceValue( String sLocID, String sID)
	{
		String sRetVal = "";
		
		// Get the preference off the drawing area which can cache it up.
		ResourceMgr pResourceManager = getResourceMgr();
		if (pResourceManager != null)
		{
			IDrawingAreaControl pControl = pResourceManager.getDrawingArea();
			if (pControl!= null)
			{
				String sData = "";
				String sPath = "";
				String sName = "";

				if (sLocID.length() > 0)
				{
					sPath = sLocID;
				}
				if (sID.length() >0)
				{
					sName = sID;
				}

				sData = pControl.getPreferenceValue( sPath, sName);
				if (sData.length() >0)
				{
					sRetVal = sData;
				}
			}
		}
		return sRetVal;
	}

	public void setPreferenceValue( String sLocID, String sID, String sValue )
	{
//		IPreferenceManager2 pMgr = ProductHelper.instance().getPreferenceManager();
//		if (pMgr!= null)
//		{
//			String sPath;
//			String sName;
//
//			if (sLocID.length()>0)
//			{
//				sPath = sLocID;
//			}
//			if (sID.length()>0)
//			{
//				sName = sID;
//			}
//
//			pMgr.setPreferenceValue( sPath, sName, sValue);
//		}
	}

	public ETList<IDrawingProperty> getDrawingProperties(IDrawingPropertyProvider pProvider,
													  String sDrawEngineID)
	{
		ETList <IDrawingProperty> pProperties = new ETArrayList<IDrawingProperty>();

		ResourceMgr pResourceManager = getResourceMgr();

		if (pResourceManager != null)
		{
			// Populate our list of properties
			Enumeration enumVal1 = m_Fonts.keys();
			while (enumVal1.hasMoreElements())
			{
				Integer obj = (Integer)enumVal1.nextElement();
				Integer val = m_Fonts.get(obj);
				int nStringID = Integer.parseInt(obj.toString());
				String sString = pResourceManager.getString(nStringID);

				if (sString != null)
				{
					int nColor = getintForStringID(nStringID);
					Font pFont = getZoomedFontForStringID(nStringID, 1.0);

					if (pFont != null)
					{
						IFontProperty pFontProperty = new FontProperty();

						pFontProperty.initialize2(pProvider,sDrawEngineID, sString, pFont, nColor);
						pProperties.add(pFontProperty);
					}
				}
			}
			Enumeration enumVal2 = m_Colors.keys();
			while (enumVal2.hasMoreElements())
			{
				Integer obj = (Integer)enumVal2.nextElement();
				Integer val = m_Colors.get(obj);
				int nStringID = Integer.parseInt(obj.toString());
				String sString = pResourceManager.getString(nStringID);

				if (sString != null)
				{
					int nColor = getintForStringID(nStringID);

					IColorProperty pColorProperty = new ColorProperty();

					pColorProperty.initialize(pProvider,sDrawEngineID, sString, nColor);
					pProperties.add(pColorProperty);
				}
			}
		}
		return pProperties;
	}

	public void saveColor(String sDrawEngineType,
						  String sResourceName,
						  int nColor)
	{
		// Make sure we've got the correct draw engine
		if (m_nDrawEngineStringID == getResourceMgr().getStringID(sDrawEngineType))
		{
			int nResourceNameStringID = getResourceMgr().getStringID(sResourceName);
			if (nResourceNameStringID > 0)
			{
				int nAllocatedColorID = 0;
				// The true says that the default color is not being changed, rather this resource user
				// (draw engine or compartment) is being changed.
				nAllocatedColorID = getResourceMgr().saveColor(m_nDrawEngineStringID, nResourceNameStringID, nColor, false);
 
				// Resets all the child resources called sResourceName so that they re-get 
				// from their parent during the next draw.
				resetToDefaultResource(sDrawEngineType, sResourceName, "color");

				m_Colors.put(new Integer(nResourceNameStringID), new Integer(nAllocatedColorID));
			 }
		}
	}

	public void saveColor2(IColorProperty pProperty)
	{
		String sDrawEngine = pProperty.getDrawEngineName();
		String sResourceName = pProperty.getResourceName();
		int nColor = pProperty.getColor();
		saveColor(sDrawEngine, sResourceName, nColor);
	}

	public void saveFont(String sDrawEngineName,
						 String sResourceName,
						 String sFaceName,
						 int nHeight,
						 int nWeight,
						 boolean bItalic,
						 int nColor)
	{
		// Make sure we've got the correct draw engine
		if (m_nDrawEngineStringID == getResourceMgr().getStringID(sDrawEngineName))
		{
			int nResourceNameStringID = getResourceMgr().getStringID(sResourceName);
			if (nResourceNameStringID > 0)
			{
				// Save the color for the font first
				saveColor(sDrawEngineName, sResourceName, nColor);

				// The true says that the default font is not being changed, rather this resource user
				// (draw engine or compartment) is being changed.
				int nAllocatedFontID = 0;
				int nAllocatedColorID = 0;

				// The true says that the default color is not being changed, rather this resource user
				// (draw engine or compartment) is being changed.
				ETPairT<Integer, Integer> ids = getResourceMgr().saveFont(m_nDrawEngineStringID, nResourceNameStringID, sFaceName,
										nHeight, nWeight, bItalic, nColor, false);
				//nAllocatedFontID = (ids.getParamOne()).intValue();
				//nAllocatedColorID = (ids.getParamTwo()).intValue();
				Integer id1 = ids.getParamOne();
				nAllocatedFontID = id1.intValue();
				Integer id2 = ids.getParamTwo();
				nAllocatedColorID = id2.intValue();

				// Resets all the child resources called sResourceName so that they re-get 
				// from their parent during the next draw.
				resetToDefaultResource(sDrawEngineName, sResourceName, "font");

				m_Fonts.put(new Integer(nResourceNameStringID), new Integer(nAllocatedFontID));
				m_Colors.put(new Integer(nResourceNameStringID), new Integer(nAllocatedColorID));
			}
		}
	}

	public void saveFont2(IFontProperty pProperty)
	{
		String sDrawEngine = pProperty.getDrawEngineName();
		String sResourceName = pProperty.getResourceName();
		String sFaceName = pProperty.getFaceName();
		int nSize = pProperty.getSize();
		int nWeight = pProperty.getWeight();
		boolean bItalic = pProperty.getItalic();
		int nColor = pProperty.getColor();

		saveFont(sDrawEngine,sResourceName, sFaceName, nSize,nWeight,bItalic,nColor);
	}

	public void resetToDefaultResource(String sDrawEngineName,
										String sResourceName,
										String sResourceType)
	{
		if (getResourceMgr() != null)
		{
			int nResourceNameStringID = getResourceMgr().getStringID(sResourceName);

			if (sResourceType.equals("font"))
			{
				if (m_Fonts.get(new Integer(nResourceNameStringID)) != null)
				{
					m_Fonts.put(new Integer(nResourceNameStringID),new Integer(-1));
				}
			 }
			 else if (sResourceType.equals("color"))
			 {
				if (m_Colors.get(new Integer(nResourceNameStringID)) != null)
				{
					m_Colors.put(new Integer(nResourceNameStringID), new Integer(-1));
				}
			}
		}
	}

	public void resetToDefaultResources()
	{
		Enumeration enumVal1 = m_Fonts.keys();
		while (enumVal1.hasMoreElements())
		{
			Integer obj = (Integer)enumVal1.nextElement();
			m_Fonts.put(obj, new Integer(-1));
		}

		Enumeration enumVal2 = m_Colors.keys();
		while (enumVal2.hasMoreElements())
		{
			Integer obj = (Integer)enumVal2.nextElement();
			m_Colors.put(obj, new Integer(-1));
		}
	}

	public void setDrawEngineStringID(String sDrawEngine)
	{
		if (sDrawEngine != null && getResourceMgr() != null)
		{
			m_nDrawEngineStringID = setResourceStringID(m_nDrawEngineStringID, sDrawEngine);
		}
	}

	public void dumpToFile(String sFile, boolean bAppendToExistingFile)
	{
//		HRESULT hr = S_OK;
//	
//		try
//		{
//			  CStdioFile file;
//			  String tempFilename(_T("C:\\ResourceMgr.txt"));
//			  USES_CONVERSION;
//	
//			  if (sFile)
//			  {
//				 tempFilename = W2T(sFile);
//			  }
//	
//			  UINT nOpenFlags = CFile::modeCreate | CFile::modeWrite | CFile::typeText;
//			  if (bAppendToExistingFile)
//			  {
//				 // Don't delete the existing file
//				 nOpenFlags = CFile::modeWrite | CFile::typeText;
//			  }
//	
//			  if (file.Open(tempFilename.c_str(), nOpenFlags ) )
//			  {
//				 String outputString;
//				 String sTitleString;
//				 String sCreditsString;
//	
//				 sTitleString = StringUtilities::Format(_T("*** Begin Dump of CResourceUser %x ***\n"), 
//														(int)this);
//				 sCreditsString = StringUtilities::Format(_T("*** End Dump of CResourceUser %x ***\n"), 
//														(int)this);
//	
//				 file.WriteString( sTitleString.c_str() );
//				 file.WriteString( sCreditsString.c_str() );
//	
//				 // Dump the colors
//				 {
//					std::map < RESOURCESTRINGID /*stringid*/, COLORID /*nColorID*/ >::iterator iterator;
//	
//					file.WriteString( _T("m_Colors (RESOURCESTRINGID, COLORID) : \n") );
//					for (iterator = m_Colors.begin() ; iterator != m_Colors.end() ; ++iterator)
//					{
//					RESOURCESTRINGID nResourceID = iterator->first;
//					COLORID nColorID = iterator->second;
//	
//					outputString = StringUtilities::Format(_T("[%d] = %d\n"), 
//															  nResourceID,
//															  nColorID);
//					file.WriteString(outputString.c_str());
//					}
//				 }
//	
//				 // Dump the fonts
//				 {
//					std::map < RESOURCESTRINGID /*stringid*/, FONTID /*nFontID*/ >::iterator iterator;
//	
//					file.WriteString( _T("m_Fonts (RESOURCESTRINGID, FONTID) : \n") );
//					for (iterator = m_Fonts.begin() ; iterator != m_Fonts.end() ; ++iterator)
//					{
//					RESOURCESTRINGID nResourceID = iterator->first;
//					FONTID nFontID = iterator->second;
//	
//					outputString = StringUtilities::Format(_T("[%d] = %d\n"), 
//															  nResourceID,
//															  nFontID);
//					file.WriteString(outputString.c_str());
//					}
//				 }
//	
//				 file.Close();
//			  }
//		}
//		catch( _com_error& err )
//		{
//			  hr = COMErrorManager::ReportError( err );
//		}
//	
//		return hr;
	}

	public boolean displayFontDialog(IFontProperty pProperty)
	{
		if (getResourceMgr() != null)
		{
			return getResourceMgr().displayFontDialog(pProperty);
		}
		else
		{
			return false;
		}
	}

	public boolean displayColorDialog(IColorProperty pProperty)
	{
		if (getResourceMgr() != null)
		{
			return getResourceMgr().displayColorDialog(pProperty);
		}
		else
		{
			return false;
		}
	}

	public boolean IsUpgraded(int nKind)
	{
		boolean bUpgraded = false;
	
//		std::map < UpgradeKind , int >::iterator iterator1;
//		std::map < UpgradeKind, CResourceMgr::CFontToUpgrade* >::iterator iterator2;
//	
//		iterator1 = m_ColorsToUpgrade.find(nKind);
//		if ( iterator1 != m_ColorsToUpgrade.end() )
//		{
//			  bUpgraded = true;
//		}
//	
//		if (bUpgraded == false)
//		{
//			  iterator2 = m_FontsToUpgrade.find(nKind);
//			  if ( iterator2 != m_FontsToUpgrade.end() )
//			  {
//				 bUpgraded = true;
//			  }
//		}
	
		return bUpgraded;
	}


	public boolean upgradeStringID(int nID, int nKind)
	{
		boolean bUpgraded = false;
	
//		if (nID != -1 && VerifyDrawEngineStringID())
//		{
//			  String sDrawEngineString;
//	
//			  GetResourceMgr()->GetString(m_nDrawEngineStringID, &sDrawEngineString);
//	
//			  if (sDrawEngineString.Length())
//			  {
//				 std::map < UpgradeKind , int >::iterator iterator1;
//				 std::map < UpgradeKind, CResourceMgr::CFontToUpgrade* >::iterator iterator2;
//	
//				 // Upgrade the colors
//				 iterator1 = m_ColorsToUpgrade.find(nKind);
//				 if ( iterator1 != m_ColorsToUpgrade.end() )
//				 {
//					int nColor = iterator1->second;
//					String sResourceString;
//	
//					GetResourceMgr()->GetString(nID, &sResourceString);
//					if (sResourceString.Length() )
//					{
//					// Set the default color
//					_VH(SaveColor(sDrawEngineString, 
//									 sResourceString, 
//									 nColor ));
//					}
//	
//					bUpgraded = true;
//				 }
//	
//				 // Upgrade the fonts
//				 iterator2 = m_FontsToUpgrade.find(nKind);
//				 if ( iterator2 != m_FontsToUpgrade.end() )
//				 {
//					CResourceMgr::CFontToUpgrade* pFont = iterator2->second;
//	
//					if (pFont && GetResourceMgr() )
//					{
//					// Upgrade the font
//					String sResourceString;
//	
//					GetResourceMgr()->GetString(nID, &sResourceString);
//					if (sResourceString.Length() )
//					{
//						  // Color was saved separately in Describe 6.1 so see if the
//						  // color is in our color upgrade table
//						  iterator1 = m_ColorsToUpgrade.find(UK_TEXTCOLOR);
//						  if ( iterator1 != m_ColorsToUpgrade.end() )
//						  {
//							 pFont->m_nColor = iterator1->second;
//						  }
//	
//						  SaveFont(sDrawEngineString,
//								sResourceString,
//								pFont->m_sFontName,
//								pFont->m_nHeight,
//								pFont->m_nWeight,
//								pFont->m_nItalic?true:false,
//								pFont->m_nColor);
//					}
//	
//					// Now delete this font
//					delete pFont;
//					iterator2->second = 0;
//	
//					bUpgraded = true;
//					}
//				 }
//			  }
//		}
//	
//		if (bUpgraded)
//		{
//			   < IAxDrawingAreaControl > pControl;
//			  get_DrawingArea(&pControl);
//			  if (pControl)
//			  {
//				 pControl->put_IsDirty(true);
//			  }
//		}
	
		return bUpgraded;
	}
	
	public int getCOLORREFForStringID(int nStringID)
	{
		int nFoundColor = 0;
		int nColorID = -1;

		if (m_nDrawEngineStringID == -1)
		{
			// We haven't called init resources yet, so return black
			nFoundColor = 0;
		}
		else
		{
			// Get the color
			nColorID = getResourceUserHelper().getColorID(nStringID);
			nFoundColor = getCOLORREFForColorID(nColorID);
		 }
		 return nFoundColor;
	}
	
	public int getCOLORREFForColorID(int nColorID)
	{
	   int nFoundREF = 0;
	   nFoundREF = getResourceMgr().getColor(nColorID);
	   return nFoundREF;
	}
}



