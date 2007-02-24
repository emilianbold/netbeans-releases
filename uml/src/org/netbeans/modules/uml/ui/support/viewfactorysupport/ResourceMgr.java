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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import java.awt.Color;
import java.awt.Font;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.WeakHashMap;

import javax.swing.JColorChooser;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETEditableCompartment;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationResourceMgr;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveAttribute;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.drawingproperties.ColorProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.ETFontType;
import org.netbeans.modules.uml.ui.support.drawingproperties.FontChooser;
import org.netbeans.modules.uml.ui.support.drawingproperties.FontProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IColorProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingPropertyProvider;
import org.netbeans.modules.uml.ui.support.drawingproperties.IFontProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.openide.util.NbBundle;

/**
 * @author jingmingm
 *
 */
public class ResourceMgr //implements IDrawingPropertyProvider
{
	//public static Hashtable<IDrawingAreaControl, ResourceMgr> m_Mgrs = new Hashtable<IDrawingAreaControl, ResourceMgr>();
   public static WeakHashMap<IDrawingAreaControl, ResourceMgr> m_Mgrs = new WeakHashMap<IDrawingAreaControl, ResourceMgr>();
	//protected IDrawingAreaControl m_RawDrawingAreaControl = null;
   protected WeakReference m_RawDrawingAreaControl = null;
	protected boolean  m_bIsDirty = false;
	protected Hashtable<String, Integer> m_StringsTable = new Hashtable<String, Integer>();
	protected int m_nLastStringID = 0;
	protected Hashtable<Integer, Integer> m_ColorIDs = new Hashtable<Integer, Integer>();
	protected Hashtable<Integer , ETPairT<Integer, Integer> > m_ColorTable = new Hashtable<Integer , ETPairT<Integer, Integer> >();
	protected int m_nLastColorID = 0;
	protected Hashtable<Integer , Integer> m_FontIDs = new Hashtable<Integer , Integer>();
	protected Hashtable<Integer , ETPairT<Integer, Integer> > m_FontHolderIDs = new Hashtable<Integer , ETPairT<Integer, Integer> >();
	protected int m_nLastFontID = 0;

	// Here's where the actual fonts are stored
	protected Hashtable<Integer , ERFontHolder> m_FontHolderTable = new Hashtable<Integer , ERFontHolder>();
	protected int m_nLastFontHolderID = 0;

	// Here's a list of upgraded colors
	protected Hashtable<String , Integer> m_ColorsToUpgrade = new Hashtable<String , Integer>();
	protected Hashtable<String, FontToUpgrade> m_FontsToUpgrade = new Hashtable<String, FontToUpgrade>();

	ERFontHolder m_pDefaultFontHolder = null;
	

	/*
	 * Default Constructor.
	 */
	protected ResourceMgr()
	{
		m_pDefaultFontHolder = null;
		m_nLastStringID = 0;
		m_nLastColorID = 0;
		m_nLastFontID = 0;
		m_nLastFontHolderID = 0;
		// fall back to default font
		m_pDefaultFontHolder = new ERFontHolder();
		
	}
	
	public ResourceMgr(IDrawingAreaControl pIDrawingAreaControl)
	{
		this();
		m_RawDrawingAreaControl = new WeakReference(pIDrawingAreaControl);

		loadPreferences(true);
	}
	
	public ResourceMgr(IDrawingAreaControl pIDrawingAreaControl, IProductArchive pArchive)
	{
		this();
		m_RawDrawingAreaControl = new WeakReference(pIDrawingAreaControl);
		
		loadPreferences(true);
	}
	
	protected ETPairT<Integer, Integer> createIntPair(int i, int j)
	{
		return new ETPairT<Integer, Integer>(new Integer(i), new Integer(j));
	}
	
	protected int getID1(int ID)
	{
		return ID >> 16;
	}
	
	protected int getID2(int ID)
	{
		return ID - (getID1(ID) << 16);
	}
	
	protected int combineIDs(int ID1, int ID2)
	{
		int ret = ID1 << 16;
		ret += ID2;
		return ret;
	}
	
	public void resetAllTables()
	{
	   Enumeration enumVal = m_FontHolderTable.keys();
	   while (enumVal.hasMoreElements())
	   {
	   		Object obj = enumVal.nextElement();
	   		if (obj instanceof ERFontHolder)
	   		{
					ERFontHolder obj1 = (ERFontHolder)obj;
					obj1 = null;
					obj = null;
	   		}
	   }

	   m_ColorIDs.clear();
	   m_ColorTable.clear();
	   m_FontIDs.clear();
	   m_FontHolderIDs.clear();
	   m_FontHolderTable.clear();
	   m_nLastColorID = 0;
	   m_nLastFontID = 0;
	   m_nLastFontHolderID = 0;
	}
	
	public void readFromArchive(IProductArchive pArchive)
	{
		if(pArchive != null)
		{
			IProductArchiveElement pResourceElement = pArchive.getElement(IProductArchiveDefinitions.RESOURCES_STRING);
			if (pResourceElement != null)
			{
				IProductArchiveElement pStringsTable = pResourceElement.getElement(IProductArchiveDefinitions.STRINGS_TABLE);
				IProductArchiveElement pColorIDsTable = pResourceElement.getElement(IProductArchiveDefinitions.COLORIDS_TABLE);
				IProductArchiveElement pColorTable = pResourceElement.getElement(IProductArchiveDefinitions.COLOR_TABLE);;
				IProductArchiveElement pFontIDsTable = pResourceElement.getElement(IProductArchiveDefinitions.FONTIDS_TABLE);;
				IProductArchiveElement pFontHolderIDsTable = pResourceElement.getElement(IProductArchiveDefinitions.FONTHOLDERIDS_TABLE);;
				IProductArchiveElement pFontHolderTable = pResourceElement.getElement(IProductArchiveDefinitions.FONTHOLDER_TABLE);;

				if ( pStringsTable != null && pColorIDsTable != null && pColorTable != null && 
					 pFontIDsTable != null && pFontHolderIDsTable != null && pFontHolderTable != null )
				{
					// Get rid of our current tables - they should be 0 anyway.  At the
					// end we overlay the preferences so that we get a merge in case any new
					// colors or fonts have been added.
					resetAllTables();

					// Load the Tables - StringTable
					{
						// Now get all the child elements and populate the table
						IProductArchiveElement[] pChildElements = pStringsTable.getElements();
						if( pChildElements != null)
						{
							for( int i = 0; i < pChildElements.length; i++ )
						 	{
								IProductArchiveElement pElement = pChildElements[i];
								if( pElement != null)
								{
							   		String sString = pElement.getAttributeString(IProductArchiveDefinitions.TYPE_STRING);
							   		int nID = (int)pElement.getAttributeLong(IProductArchiveDefinitions.STRINGID_ENTRY);

							   		if (sString.length() > 0)
							   		{
								  			m_StringsTable.put(sString, new Integer(nID));
							   		}
							   		if (nID > m_nLastStringID)
							   		{
								  			m_nLastStringID = nID;
							   		}
								}
						 	}
					  	}
				   	}
				   	// ColorIDs
				   	{
					  	// Now get all the child elements and populate the table
					  	IProductArchiveElement[] pChildElements =	pColorIDsTable.getElements();
					  	if( pChildElements != null)
					  	{
						 	for( int i = 0; i < pChildElements.length; i++ )
						 	{
								IProductArchiveElement pElement = pChildElements[i];
								if( pElement != null)
								{
							   		long nID1 = readCOMBINEDSTRINGID(pElement, IProductArchiveDefinitions.COMBINEDSTRINGID_ATTR);
							   		long nID2 = pElement.getAttributeLong(IProductArchiveDefinitions.COLORID_ENTRY);
							   		m_ColorIDs.put(new Integer((int)nID1), new Integer((int)(nID2)));
								}
						 	}
					  	}
				   	}
				   	// ColorTable
				   	{
					  	// Now get all the child elements and populate the table
					  	IProductArchiveElement[] pChildElements = pColorTable.getElements();
					  	if( pChildElements != null)
					  	{
						 	for( int i = 0; i < pChildElements.length; i++ )
						 	{
								IProductArchiveElement pElement = pChildElements[i];
								if( pElement != null)
								{
							   		long nColorID = pElement.getAttributeLong(IProductArchiveDefinitions.COLORID_ENTRY);
							   		int nColorRef = readCOLORREF(pElement, IProductArchiveDefinitions.COLORREF_ENTRY);
							   		int  nStringID = (int)readCOMBINEDSTRINGID(pElement,IProductArchiveDefinitions.COMBINEDSTRINGID_ATTR);

							   		m_ColorTable.put(new Integer((int)nColorID), createIntPair(nColorRef, nStringID));
							   		
							   		if (nColorID > m_nLastColorID)
							   		{
								  			m_nLastColorID = (int)nColorID;
							   		}
								}
						 	}
					  	}
				   	}
				   	// FontIDs
				   	{
					  	// Now get all the child elements and populate the table
					  	IProductArchiveElement[] pChildElements = pFontIDsTable.getElements();
					  	if( pChildElements != null)
					  	{
						 	for( int i = 0; i < pChildElements.length; i++ )
						 	{
								IProductArchiveElement pElement = pChildElements[i];
								if( pElement != null)
								{
							   		int nID1 = (int)readCOMBINEDSTRINGID(pElement,IProductArchiveDefinitions.COMBINEDSTRINGID_ATTR);
							   		int nID2 = (int)pElement.getAttributeLong(IProductArchiveDefinitions.FONTID_ENTRY);
							   		m_FontIDs.put(new Integer(nID1), new Integer(nID2));
								}
						 	}
					  	}
				   	}
				   	// FontHolderIDs
				   	{
					  	// Now get all the child elements and populate the table
					  	IProductArchiveElement[] pChildElements = pFontHolderIDsTable.getElements();
					  	if( pChildElements != null)
					  	{
						 	for( int i = 0; i < pChildElements.length; i++ )
						 	{
								IProductArchiveElement pElement = pChildElements[i];
								if( pElement != null)
								{
							   		long nFontID = pElement.getAttributeLong(IProductArchiveDefinitions.FONTID_ENTRY);
							   		long nFontHolderID = pElement.getAttributeLong(IProductArchiveDefinitions.FONTHOLDERID_ENTRY);
							   		long nStringID = readCOMBINEDSTRINGID(pElement,IProductArchiveDefinitions.COMBINEDSTRINGID_ATTR);
							   		m_FontHolderIDs.put(new Integer((int)nFontID),  createIntPair((int)nFontHolderID, (int)nStringID));
							   		if (nFontID > m_nLastFontID)
							   		{
								  		m_nLastFontID = (int)nFontID;
							   		}
								}
						 	}
					  	}
				   	}
				   	// FontHolderTable
				   	{
					  	// Now get all the child elements and populate the table
					  	IProductArchiveElement[] pChildElements = pFontHolderTable.getElements();
					  	if( pChildElements != null)
					  	{
						 	for( int i = 0; i < pChildElements.length; i++ )
						 	{
								IProductArchiveElement pElement = pChildElements[i];
								if( pElement != null)
								{
									long nFontHolderID = pElement.getAttributeLong(IProductArchiveDefinitions.FONTHOLDERID_ENTRY);
									String sFontName = pElement.getAttributeString(IProductArchiveDefinitions.RESOURCEFONTNAME_STRING);
									long nCharset = pElement.getAttributeLong(IProductArchiveDefinitions.RESOURCEFONTCHARSET_STRING);
									long nHeight = pElement.getAttributeLong(IProductArchiveDefinitions.RESOURCEFONTHEIGHT_STRING);
									boolean bItalic = pElement.getAttributeBool(IProductArchiveDefinitions.RESOURCEFONTITALIC_STRING);
									boolean bStrikeout = pElement.getAttributeBool(IProductArchiveDefinitions.RESOURCEFONTSTRIKEOUT_STRING);
									boolean bUnderline = pElement.getAttributeBool(IProductArchiveDefinitions.RESOURCEFONTUNDERLINE_STRING);
									long nWeight = pElement.getAttributeLong(IProductArchiveDefinitions.RESOURCEFONTWEIGHT_STRING);
//								   IProductArchiveAttribute pNameAttribute = pElement.getAttribute(IProductArchiveDefinitions.RESOURCEFONTNAME_STRING);
//								   IProductArchiveAttribute pHeightAttribute = pElement.getAttribute(IProductArchiveDefinitions.RESOURCEFONTHEIGHT_STRING);;
//								   IProductArchiveAttribute pWeightAttribute = pElement.getAttribute(IProductArchiveDefinitions.RESOURCEFONTWEIGHT_STRING);;
//								   IProductArchiveAttribute pItalicAttribute = pElement.getAttribute(IProductArchiveDefinitions.RESOURCEFONTITALIC_STRING);;                           
							   		//if (pNameAttribute!= null && pHeightAttribute!= null && pWeightAttribute!= null && pItalicAttribute!= null && sFontName.length()>0)
							   		//{
//									  m_FontHolderTable[nFontHolderID] = new ERFontHolder(sFontName,
//																						   (int)nHeight,
//																						   (int)nWeight,
//																						   bItalic?true:false,
//																						   bUnderline?true:false,
//																						   bStrikeout?true:false,
//																						   (int)nCharset);
										ERFontHolder pERFontHolder = new ERFontHolder(sFontName,(int)nHeight,bItalic, bUnderline, nWeight >= 700);
										m_FontHolderTable.put(new Integer((int)nFontHolderID), pERFontHolder);
							   		//}

								   if (nFontHolderID > m_nLastFontHolderID)
								   {
									  m_nLastFontHolderID = (int)nFontHolderID;
								   }
								}
						 	}
					  	}
				   	}

				   // Now overlay the preferences from the files
				   loadPreferences(false);
				}
			}
			else
			{
				// We have an upgrade situation
				handleUpgrade(pArchive);

				// Now overlay the preferences from the files
				loadPreferences(false);
			}
		}  // pArchive
	}
	
	public void saveCOLORREF(IProductArchiveElement pElement, String sAttrName, int nColorRef)
	{
		Color pColor = new Color(nColorRef);
		pElement.addAttributeString(sAttrName,
			(new Integer(pColor.getRed())).toString() + "," + (new Integer(pColor.getGreen())).toString() + "," + (new Integer(pColor.getBlue())).toString());
	}
	
	public int readCOLORREF(IProductArchiveElement pElement, String sAttrName)
	{
		IProductArchiveAttribute pColorAttr = pElement.getAttribute(sAttrName);
		if (pColorAttr != null)
		{
			ETList< String > tokens = StringUtilities.splitOnDelimiter(pElement.getAttributeString(sAttrName), ",");
			int nRed;
			int nGreen;
			int nBlue;

			int count = tokens != null ? tokens.size() : 0;
			if (count >= 3)
			{
				nRed = Integer.parseInt(tokens.get(0));
				nGreen = Integer.parseInt(tokens.get(1));
				nBlue = Integer.parseInt(tokens.get(2));
			}
			else
			{
				nRed = 0;
				nGreen = 0;
				nBlue = 0;
			}
			
			Color pColor = new Color(nRed,nGreen,nBlue);
			return pColor.getRGB();
		}
		return 0;
	}
	
	public void saveCOMBINEDSTRINGID(IProductArchiveElement pElement, String sAttrName, int nID)
	{
		int nID1 = getID1(nID);
		int nID2 = getID2(nID);

		String xsTemp = (new Integer((int)nID1)).toString() + "," + (new Integer((int)nID2)).toString();
		pElement.addAttributeString(sAttrName,xsTemp);
	}
	
	public long readCOMBINEDSTRINGID(IProductArchiveElement pElement, String sAttrName)
	{
		long nID = 0;
		String sTemp = pElement.getAttributeString(sAttrName);
		IProductArchiveAttribute pIDAttr = pElement.getAttribute(sAttrName);
		if (pIDAttr != null)
		{
			ETList< String > tokens = StringUtilities.splitOnDelimiter(sTemp,",");
			int nID1 = 0;
			int nID2 = 0;

			int count = tokens.size();
			for (int nIndex = 0; nIndex < count; ++nIndex)
			{
				if (nIndex == 0)
				{
					nID1 = Integer.parseInt(tokens.get(nIndex));
				}
				else if (nIndex == 1)
				{
					nID2 = Integer.parseInt(tokens.get(nIndex));
					break;
				}
			}

			nID = combineIDs(nID1,nID2);
		}
		return nID;
	}
	
	public void handleUpgrade(IProductArchive pArchive)
	{
//	   HR_PARM_CHECK( pArchive );
//	   HRESULT hr = S_OK;
//
//	   try
//	   {
//		  /* Heres an example of a saved 6.1 file 
//			 <CompartmentNameTable MaxIndexValue="4">
//				  <ADClassNameListCompartment TableIndex="1"/>
//				  <ADClassNameCompartment TableIndex="2"/>
//				  <ADAttributeListCompartment TableIndex="3"/>
//				  <ADOperationListCompartment TableIndex="4"/>
//			  </CompartmentNameTable>
//			  <ColorsTable MaxIndexValue="132">
//				  <ActivityEdgeBorderColor TableIndex="1" value="0"/>
//				  <ActivityFinalNodeBorderColor TableIndex="2" value="0"/>
//				  <ActivityFinalNodeFillColor TableIndex="3" value="255"/>
//				  <ActivityFinalNodeTextColor TableIndex="4" value="0"/>
//				  <ActivityGroupBorderColor TableIndex="5" value="0"/>
//				  <ActivityGroupFillColor TableIndex="6" value="16777215"/>
//				  <ActivityGroupTextColor TableIndex="7" value="0"/>
//				  <ArtifactBorderColor TableIndex="8" value="0"/>
//				  <ArtifactFillColor TableIndex="9" value="14211802"/>
//				  <AssociationEdgeBorderColor TableIndex="10" value="0"/>
//				  <CLR65280 TableIndex="11" value="65280"/>
//				  <ClassBorderColor TableIndex="12" value="0"/>
//				  <ClassFillColor TableIndex="13" value="65535"/>
//				  <ClassNodeBorderColor TableIndex="14" value="0"/>
//				  <ClassNodeFillColor TableIndex="15" value="10527017"/>
//				  <ClassNodeTextColor TableIndex="16" value="0"/>
//				  <ClassTextColor TableIndex="17" value="0"/>
//				  <CollaborationBorderColor TableIndex="18" value="0"/>
//				  <CollaborationFillColor TableIndex="19" value="6865000"/>
//				  <CollaborationLifelineBorderColor TableIndex="20" value="0"/>
//				  <CollaborationLifelineFillColor TableIndex="21" value="16777215"/>
//				  <CollaborationLifelineTextColor TableIndex="22" value="0"/>
//				  <CollaborationTextColor TableIndex="23" value="0"/>
//				  <CombinedFragmentBorderColor TableIndex="24" value="0"/>
//				  <CombinedFragmentFillColor TableIndex="25" value="16777215"/>
//				  <CombinedFragmentTextColor TableIndex="26" value="0"/>
//				  <CommentBorderColor TableIndex="27" value="8388608"/>
//				  <CommentEdgeBorderColor TableIndex="28" value="0"/>
//				  <CommentFillColor TableIndex="29" value="16777215"/>
//				  <CommentTextColor TableIndex="30" value="0"/>
//				  <ComponentBorderColor TableIndex="31" value="0"/>
//				  <ComponentFillColor TableIndex="32" value="707325"/>
//				  <ComponentTextColor TableIndex="33" value="0"/>
//				  <ConnectorEdgeBorderColor TableIndex="34" value="0"/>
//				  <ControlNodeBorderColor TableIndex="35" value="0"/>
//				  <ControlNodeFillColor TableIndex="36" value="16777215"/>
//				  <ControlNodeTextColor TableIndex="37" value="0"/>
//				  <DataTypeBorderColor TableIndex="38" value="0"/>
//				  <DataTypeFillColor TableIndex="39" value="65535"/>
//				  <DataTypeTextColor TableIndex="40" value="0"/>
//				  <DecisionMergeNodeBorderColor TableIndex="41" value="0"/>
//				  <DecisionMergeNodeFillColor TableIndex="42" value="15987882"/>
//				  <DecisionMergeNodeTextColor TableIndex="43" value="0"/>
//				  <DependencyEdgeBorderColor TableIndex="44" value="0"/>
//				  <DeploymentSpecBorderColor TableIndex="45" value="0"/>
//				  <DeploymentSpecFillColor TableIndex="46" value="13167304"/>
//				  <DeploymentSpecTextColor TableIndex="47" value="0"/>
//				  <DerivationClassifierBorderColor TableIndex="48" value="0"/>
//				  <DerivationClassifierFillColor TableIndex="49" value="65535"/>
//				  <DerivationEdgeBorderColor TableIndex="50" value="0"/>
//				  <EnumBorderColor TableIndex="51" value="0"/>
//				  <EnumFillColor TableIndex="52" value="65535"/>
//				  <FinalStateBorderColor TableIndex="53" value="0"/>
//				  <FinalStateFillColor TableIndex="54" value="255"/>
//				  <FinalStateTextColor TableIndex="55" value="0"/>
//				  <FlowFinalNodeBorderColor TableIndex="56" value="0"/>
//				  <FlowFinalNodeFillColor TableIndex="57" value="255"/>
//				  <FlowFinalNodeTextColor TableIndex="58" value="0"/>
//				  <ForkJoinNodeBorderColor TableIndex="59" value="0"/>
//				  <ForkJoinNodeFillColor TableIndex="60" value="0"/>
//				  <ForkJoinNodeTextColor TableIndex="61" value="0"/>
//				  <GeneralizationEdgeBorderColor TableIndex="62" value="0"/>
//				  <GraphicBorderColor TableIndex="63" value="0"/>
//				  <GraphicFillColor TableIndex="64" value="16777215"/>
//				  <GraphicTextColor TableIndex="65" value="0"/>
//				  <ImplementationEdgeBorderColor TableIndex="66" value="0"/>
//				  <InitialNodeBorderColor TableIndex="67" value="0"/>
//				  <InitialNodeFillColor TableIndex="68" value="0"/>
//				  <InitialNodeTextColor TableIndex="69" value="0"/>
//				  <InterfaceBorderColor TableIndex="70" value="0"/>
//				  <InterfaceFillColor TableIndex="71" value="65535"/>
//				  <InvocationNodeBorderColor TableIndex="72" value="0"/>
//				  <InvocationNodeFillColor TableIndex="73" value="10273948"/>
//				  <InvocationNodeTextColor TableIndex="74" value="0"/>
//				  <LabelBorderColor TableIndex="75" value="16777215"/>
//				  <LabelFillColor TableIndex="76" value="16777215"/>
//				  <LabelTextColor TableIndex="77" value="0"/>
//				  <LifelineBorderColor TableIndex="78" value="0"/>
//				  <LifelineFillColor TableIndex="79" value="16777215"/>
//				  <LifelineTextColor TableIndex="80" value="0"/>
//				  <MessageEdgeBorderColor TableIndex="81" value="0"/>
//				  <NestedLinkEdgeBorderColor TableIndex="82" value="0"/>
//				  <ObjectNodeBorderColor TableIndex="83" value="0"/>
//				  <ObjectNodeFillColor TableIndex="84" value="6004707"/>
//				  <ObjectNodeTextColor TableIndex="85" value="0"/>
//				  <PackageBorderColor TableIndex="86" value="0"/>
//				  <PackageFillColor TableIndex="87" value="61440"/>
//				  <PackageTextColor TableIndex="88" value="0"/>
//				  <PartFacadeEdgeBorderColor TableIndex="89" value="0"/>
//				  <PortBorderColor TableIndex="90" value="0"/>
//				  <PortFillColor TableIndex="91" value="6053042"/>
//				  <PortProvidedInterfaceEdgeBorderColor TableIndex="92" value="0"/>
//				  <PseudoStateChoiceBorderColor TableIndex="93" value="0"/>
//				  <PseudoStateChoiceFillColor TableIndex="94" value="15987882"/>
//				  <PseudoStateChoiceTextColor TableIndex="95" value="16777215"/>
//				  <PseudoStateDeepHistoryBorderColor TableIndex="96" value="0"/>
//				  <PseudoStateDeepHistoryFillColor TableIndex="97" value="13542576"/>
//				  <PseudoStateDeepHistoryTextColor TableIndex="98" value="0"/>
//				  <PseudoStateEntryPointBorderColor TableIndex="99" value="0"/>
//				  <PseudoStateEntryPointFillColor TableIndex="100" value="4227327"/>
//				  <PseudoStateEntryPointTextColor TableIndex="101" value="0"/>
//				  <PseudoStateInitialBorderColor TableIndex="102" value="0"/>
//				  <PseudoStateInitialFillColor TableIndex="103" value="32768"/>
//				  <PseudoStateInitialTextColor TableIndex="104" value="0"/>
//				  <PseudoStateJoinBorderColor TableIndex="105" value="0"/>
//				  <PseudoStateJoinFillColor TableIndex="106" value="0"/>
//				  <PseudoStateJoinTextColor TableIndex="107" value="0"/>
//				  <PseudoStateJunctionBorderColor TableIndex="108" value="0"/>
//				  <PseudoStateJunctionFillColor TableIndex="109" value="12545387"/>
//				  <PseudoStateJunctionTextColor TableIndex="110" value="0"/>
//				  <PseudoStateShallowHistoryBorderColor TableIndex="111" value="0"/>
//				  <PseudoStateShallowHistoryFillColor TableIndex="112" value="8905184"/>
//				  <PseudoStateShallowHistoryTextColor TableIndex="113" value="0"/>
//				  <PseudoStateStopBorderColor TableIndex="114" value="0"/>
//				  <PseudoStateStopFillColor TableIndex="115" value="255"/>
//				  <PseudoStateStopTextColor TableIndex="116" value="0"/>
//				  <QualifierBorderColor TableIndex="117" value="0"/>
//				  <QualifierFillColor TableIndex="118" value="65535"/>
//				  <QualifierTextColor TableIndex="119" value="0"/>
//				  <RobustnessBorderColor TableIndex="120" value="0"/>
//				  <RobustnessFillColor TableIndex="121" value="14155775"/>
//				  <RobustnessTextColor TableIndex="122" value="0"/>
//				  <StateBorderColor TableIndex="123" value="0"/>
//				  <StateFillColor TableIndex="124" value="13290389"/>
//				  <StateTextColor TableIndex="125" value="0"/>
//				  <StickBorderColor TableIndex="126" value="16744703"/>
//				  <StickColor TableIndex="127" value="16777215"/>
//				  <StickTextColor TableIndex="128" value="0"/>
//				  <TransitionEdgeBorderColor TableIndex="129" value="0"/>
//				  <UseCaseBorderColor TableIndex="130" value="0"/>
//				  <UseCaseFillColor TableIndex="131" value="15987882"/>
//				  <UseCaseTextColor TableIndex="132" value="0"/>
//			  </ColorsTable>
//			  <FontsTable MaxIndexValue="51">
//				  <ActivityGroupFont TableIndex="1" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <ArtifactFont TableIndex="2" name="Arial" CharSet="0" Height="10" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <ClassFont TableIndex="3" name="Arial" CharSet="0" Height="10" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <ClassNodeFont TableIndex="4" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <CollaborationFont TableIndex="5" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <CollaborationLifelineFont TableIndex="6" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <CombinedFragmentFont TableIndex="7" name="Arial" CharSet="0" Height="10" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <CommentFont TableIndex="8" name="Arial" CharSet="0" Height="10" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <ComponentFont TableIndex="9" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <ControlNodeFont TableIndex="10" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <DataTypeFont TableIndex="11" name="Arial" CharSet="0" Height="10" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <DefaultDocFont TableIndex="12" name="Arial" CharSet="0" Height="10" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <DefaultGridFont TableIndex="13" name="Arial" CharSet="0" Height="8" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <DeploymentSpecFont TableIndex="15" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <DerivationClassifierFont TableIndex="16" name="Arial" CharSet="0" Height="10" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <EnumFont TableIndex="17" name="Arial" CharSet="0" Height="10" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <ExpressionFont TableIndex="18" name="Arial" CharSet="0" Height="8" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <FinalStateFont TableIndex="19" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <GraphicFont TableIndex="20" name="Arial" CharSet="0" Height="8" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <InteractionFragmentFont TableIndex="21" name="Arial" CharSet="0" Height="10" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <InterfaceFont TableIndex="22" name="Arial" CharSet="0" Height="10" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <InvocationNodeFont TableIndex="23" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <LabelFont TableIndex="24" name="Arial" CharSet="0" Height="8" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <LifelineFont TableIndex="25" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <ListNameFont TableIndex="26" name="Arial" CharSet="0" Height="9" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <ListTitleFont TableIndex="27" name="Arial" CharSet="0" Height="8" Italic="1" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <NameFont TableIndex="28" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <ObjectNodeFont TableIndex="29" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <PackageFont TableIndex="30" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <PackageImportFont TableIndex="31" name="Arial" CharSet="0" Height="9" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <PackageInTabFont TableIndex="32" name="Arial" CharSet="0" Height="10" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <PortFont TableIndex="33" name="Arial" CharSet="0" Height="10" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <PseudoStateChoiceFont TableIndex="34" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <PseudoStateDeepHistoryFont TableIndex="35" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <PseudoStateEntryPointFont TableIndex="36" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <PseudoStateInitialFont TableIndex="37" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <PseudoStateJoinFont TableIndex="38" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <PseudoStateJunctionFont TableIndex="39" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <PseudoStateShallowHistoryFont TableIndex="40" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <PseudoStateStopFont TableIndex="41" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <QualifierFont TableIndex="42" name="Arial" CharSet="0" Height="10" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <RobustnessFont TableIndex="43" name="Arial" CharSet="0" Height="10" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <StateEventsFont TableIndex="44" name="Arial" CharSet="0" Height="8" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <StateFont TableIndex="45" name="Arial" CharSet="0" Height="10" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <StereotypeFont TableIndex="46" name="Arial" CharSet="0" Height="9" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <StickFont TableIndex="47" name="Arial" CharSet="0" Height="12" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//				  <SubPartitionFont TableIndex="48" name="Arial" CharSet="0" Height="8" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <TaggedValuesFont TableIndex="49" name="Arial" CharSet="0" Height="9" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <TemplateFont TableIndex="50" name="Arial" CharSet="0" Height="8" Italic="0" Strikeout="0" Underline="0" Weight="400" Color="0"/>
//				  <UseCaseFont TableIndex="51" name="Arial" CharSet="0" Height="10" Italic="0" Strikeout="0" Underline="0" Weight="700" Color="0"/>
//			  </FontsTable>
//		  */
//
//		  // We need to go through the colors table and initialize those colors
//		  if (pArchive)
//		  {
//			 /////////////////////////////////////////////////////////////////////////////////
//			 //  fonts
//			 //////////////////////////////////////////////////////////////////////////////////
//			 {
//				CComPtr < IProductArchiveElement > pFoundElement;
//				long nKey = 1;
//				VARIANT_BOOL bRemoved = false;
//
//				_VH(pArchive->GetTableEntry(CComBSTR(RESOURCEFONTTABLE_STRING), nKey, &bRemoved, &pFoundElement));
//				while (pFoundElement)
//				{
//				   if (!bRemoved)
//				   {
//					  CComBSTR sName;
//					  _VH( pFoundElement->get_ID( &sName ));
//
//					  CComPtr < IProductArchiveAttribute > pNameAttribute;
//					  CComPtr < IProductArchiveAttribute > pHeightAttribute;
//					  CComPtr < IProductArchiveAttribute > pWeightAttribute;
//					  CComPtr < IProductArchiveAttribute > pItalicAttribute;
//
//					  CComBSTR sFontName;
//					  long nCharset          = 0;
//					  long nHeight           = 0;
//					  long nItalic           = 0;
//					  long nStrikeout        = 0;
//					  long nUnderline        = 0;
//					  long nWeight           = 0;
//					  long nColor            = 0;
//
//					  _VH(pFoundElement->GetAttributeString( CComBSTR( RESOURCEFONTNAME_STRING      ), &sFontName , &pNameAttribute   ));
//					  _VH(pFoundElement->GetAttributeLong(   CComBSTR( RESOURCEFONTCHARSET_STRING   ), &nCharset  , 0                 ));
//					  _VH(pFoundElement->GetAttributeLong(   CComBSTR( RESOURCEFONTHEIGHT_STRING    ), &nHeight   , &pHeightAttribute ));
//					  _VH(pFoundElement->GetAttributeLong(   CComBSTR( RESOURCEFONTITALIC_STRING    ), &nItalic   , &pItalicAttribute ));
//					  _VH(pFoundElement->GetAttributeLong(   CComBSTR( RESOURCEFONTSTRIKEOUT_STRING ), &nStrikeout, 0                 ));
//					  _VH(pFoundElement->GetAttributeLong(   CComBSTR( RESOURCEFONTUNDERLINE_STRING ), &nUnderline, 0                 ));
//					  _VH(pFoundElement->GetAttributeLong(   CComBSTR( RESOURCEFONTWEIGHT_STRING    ), &nWeight   , &pWeightAttribute ));
//					  _VH(pFoundElement->GetAttributeLong(   CComBSTR( RESOURCEFONTCOLOR_STRING     ), &nColor    , 0                 ));
//
//					  if (pNameAttribute && pHeightAttribute && pWeightAttribute && pItalicAttribute)
//					  {
//						 USES_CONVERSION;
//
//						 // hack for older systems that stored height in logical units instead of points
//						 if( nHeight < 0 )
//						 {
//							nHeight = CGDISupport::LogicalUnitsToPoints( nHeight );
//						 }
//
//						 if( sFontName.Length() && sName.Length() )
//						 {
//							CResourceMgr::CFontToUpgrade* pNewFont = new CResourceMgr::CFontToUpgrade;
//
//							pNewFont->m_sFontName = sFontName;
//							pNewFont->m_nCharset = nCharset;
//							pNewFont->m_nHeight = nHeight;
//							pNewFont->m_nItalic = nItalic;
//							pNewFont->m_nStrikeout = nStrikeout;
//							pNewFont->m_nUnderline = nUnderline;
//							pNewFont->m_nWeight = nWeight;
//							pNewFont->m_nColor = nColor;
//							m_FontsToUpgrade[ xstring(W2T(sName))] = pNewFont;
//						 }
//					  }
//				   }
//
//				   pFoundElement = 0;
//				   nKey++;
//				   bRemoved = false;
//				   _VH(pArchive->GetTableEntry(CComBSTR(RESOURCEFONTTABLE_STRING), nKey, &bRemoved, &pFoundElement));
//				}
//			 }
//			 //////////////////////////////////////////////////////////////////////////////////
//			 //  colors
//			 //////////////////////////////////////////////////////////////////////////////////
//			 {
//				CComPtr < IProductArchiveElement > pFoundElement;
//				long nKey = 1;
//				VARIANT_BOOL bRemoved = false;
//				USES_CONVERSION;
//
//				_VH(pArchive->GetTableEntry(CComBSTR(RESOURCECOLORTABLE_STRING), nKey, &bRemoved, &pFoundElement));
//				while (pFoundElement)
//				{
//				   if (!bRemoved)
//				   {
//					  CComBSTR sName;
//					  long lColor = 0;
//					  _VH( pFoundElement->get_ID( &sName ));
//
//					  CComPtr < IProductArchiveAttribute > pColorAttribute;
//
//					  _VH(pFoundElement->GetAttributeLong(CComBSTR(RESOURCECOLORVALUE_STRING), &lColor, &pColorAttribute));
//					  ASSERT(pColorAttribute);
//
//					  CComBSTR stringValue;
//					  if( pColorAttribute && sName.Length() )
//					  {
//						 m_ColorsToUpgrade[ xstring(W2T(sName))] = (COLORREF)(lColor);
//					  }
//				   }
//
//				   pFoundElement = 0;
//				   nKey++;
//				   bRemoved = false;
//				   _VH(pArchive->GetTableEntry(CComBSTR(RESOURCECOLORTABLE_STRING), nKey, &bRemoved, &pFoundElement));
//				}
//			 }
//		  }
//
//		  _VH(UpgradeFoundFontsAndColors());
//	   }
//	   catch( _com_error& err )
//	   {
//		  hr = COMErrorManager::ReportError( err );
//	   }
//
//	   return hr;
	}
	
	public void upgradeFoundFontsAndColors()
	{
//	   HRESULT hr = S_OK;
//
//	   try
//	   {
//		  // Need the resource manager to translate between the old color names and the new ones
//		  CComPtr < IPresentationResourceMgr > pMgr;
//		  std::map < xstring , COLORREF >::iterator iterator1;
//		  std::map < xstring, CFontToUpgrade* >::iterator iterator2;
//		  bool bUpgraded = false;
//
//		  CProductHelper::Instance()->GetPresentationResourceMgr(&pMgr);
//		  ATLASSERT(pMgr);
//		  if (pMgr)
//		  {
//			 // Go through the found colors and fonts and translate them into our 
//			 // new 6.2 colors and fonts.
//
//			 // Upgrade the colors
//			 for (iterator1 = m_ColorsToUpgrade.begin() ; iterator1 != m_ColorsToUpgrade.end() ; ++iterator1)
//			 {
//				xstring xsName = iterator1->first;
//				COLORREF nColor = iterator1->second;
//				CComBSTR sDrawEngineName;
//				CComBSTR sResourceName;
//
//				if (xsName.c_str())
//				{
//				   _VH(pMgr->GetUpgradeString(CComBSTR(xsName.c_str()), &sDrawEngineName, &sResourceName));
//				   if (sDrawEngineName.Length() && sResourceName.Length() )
//				   {
//					  STRINGID nDrawEngineStringID = GetStringID(sDrawEngineName);
//					  STRINGID nResourceNameStringID = GetStringID(sResourceName);
//					  COMBINEDSTRINGID nID = COMBINE_STRINGIDS(nDrawEngineStringID, nResourceNameStringID);
//
//					  // Do the upgrade
//					  std::map < COMBINEDSTRINGID , COLORID >::iterator iterator;
//
//					  // First erase it then reestablish it using SetDefaultColor
//					  iterator = m_ColorIDs.find(nID);
//					  if (iterator != m_ColorIDs.end())
//					  {
//						 m_ColorIDs.erase(iterator);
//					  }
//					  SetDefaultColor(nDrawEngineStringID, nResourceNameStringID, nColor);
//					  bUpgraded = true;
//				   }
//				}
//			 }
//
//			 // Upgrade the fonts
//			 for (iterator2 = m_FontsToUpgrade.begin() ; iterator2 != m_FontsToUpgrade.end() ; ++iterator2)
//			 {
//				xstring xsName = iterator2->first;
//				CFontToUpgrade* pFont = iterator2->second;
//				CComBSTR sDrawEngineName;
//				CComBSTR sResourceName;
//
//				if (xsName.c_str() && pFont)
//				{
//				   _VH(pMgr->GetUpgradeString(CComBSTR(xsName.c_str()), &sDrawEngineName, &sResourceName));
//				   if (sDrawEngineName.Length() && sResourceName.Length() )
//				   {
//					  STRINGID nDrawEngineStringID = GetStringID(sDrawEngineName);
//					  STRINGID nResourceNameStringID = GetStringID(sResourceName);
//					  COMBINEDSTRINGID nID = COMBINE_STRINGIDS(nDrawEngineStringID, nResourceNameStringID);
//
//					  // First erase it
//					  std::map < COMBINEDSTRINGID , FONTID >::iterator iterator;
//
//					  iterator = m_FontIDs.find(nID);
//					  if (iterator != m_FontIDs.end())
//					  {
//						 m_FontIDs.erase(nID);
//					  }
//
//					  // Create an entry for the upgraded font
//					  SetUpgradedFont(sDrawEngineName, sResourceName, pFont);
//					  bUpgraded = true;
//				   }
//				}
//			 }
//		  }
//
//		  // Clear the colors
//		  m_ColorsToUpgrade.clear();
//
//		  // Clear the fonts
//		  std::map < xstring, CFontToUpgrade* >::iterator iterator;
//
//		  for (iterator2 = m_FontsToUpgrade.begin() ; iterator2 != m_FontsToUpgrade.end() ; ++iterator2)
//		  {
//			 delete iterator2->second;
//		  }
//		  m_FontsToUpgrade.clear();
//
//		  if (bUpgraded)
//		  {
//			 CComPtr < IAxDrawingAreaControl > pControl;
//			 GetDrawingArea(&pControl);
//			 if (pControl)
//			 {
//				pControl->put_IsDirty(true);
//			 }
//		  }
//	   }
//	   catch( _com_error& err )
//	   {
//		  hr = COMErrorManager::ReportError( err );
//	   }
//
//	   return hr;
	}
	
//	public void setUpgradedFont(String sDrawEngineName,String sResourceName,
//											   CResourceMgr::CFontToUpgrade* pFont)
//	{
//	   HRESULT hr = S_OK;
//
//	   try
//	   {
//		  if (sDrawEngineName && sResourceName && pFont)
//		  {
//			 // Do the upgrade
//			 STRINGID nDrawEngineStringID = GetStringID(sDrawEngineName);
//			 STRINGID nResourceNameStringID = GetStringID(sResourceName);
//
//			 COMBINEDSTRINGID nID = COMBINE_STRINGIDS(nDrawEngineStringID, nResourceNameStringID);
//
//			 // First we need to see if this font already exists
//			 FONTHOLDERID nExistingFontHolderID = GetDefaultFontHolderID(pFont->m_sFontName,
//																		 pFont->m_nHeight,
//																		 pFont->m_nWeight,
//																		 pFont->m_nItalic?true:false);
//
//			 if (nExistingFontHolderID < 0)
//			 {
//				// We found a new font, need to load a new font
//				m_nLastFontHolderID++;
//				nExistingFontHolderID = m_nLastFontHolderID;
//
//				m_FontHolderTable[nExistingFontHolderID] = new CERFontHolder(W2T(pFont->m_sFontName), 
//																		 pFont->m_nHeight,
//																		 (pFont->m_nWeight<700)?FW_NORMAL:FW_BOLD,
//																		 pFont->m_nItalic?true:false);
//			 }
//			 // Now add it to the font tables
//			 m_nLastFontID++;
//         
//			 if (m_FontIDs.find(nID) == m_FontIDs.end())
//			 {
//				// We might be overlaying, like when we read in a diagram, so don't wipe out
//				// the diagram preferences, only merge in the new ones
//				m_FontIDs[nID] = m_nLastFontID;
//				m_FontHolderIDs[m_nLastFontID] = PAIR_FONTHOLDERID_COMBINEDSTRINGID(nExistingFontHolderID, 0);
//			 }
//		  }
//	   }
//	   catch( _com_error& err )
//	   {
//		  hr = COMErrorManager::ReportError( err );
//	   }
//
//	   return hr;
//	}

	public void writeToArchive(IProductArchive pArchive)
	{
		if( pArchive != null)
		{
			// Create a resource element to hold the font/color information
			IProductArchiveElement pResourceElement = pArchive.createElement(IProductArchiveDefinitions.RESOURCES_STRING);

			// Populate it with our color/font tables
			if( pResourceElement != null)
			{
				IProductArchiveElement pStringsTable = pResourceElement.createElement(IProductArchiveDefinitions.STRINGS_TABLE);
				IProductArchiveElement pColorIDsTable = pResourceElement.createElement(IProductArchiveDefinitions.COLORIDS_TABLE);
				IProductArchiveElement pColorTable = pResourceElement.createElement(IProductArchiveDefinitions.COLOR_TABLE);
				IProductArchiveElement pFontIDsTable = pResourceElement.createElement(IProductArchiveDefinitions.FONTIDS_TABLE);
				IProductArchiveElement pFontHolderIDsTable = pResourceElement.createElement(IProductArchiveDefinitions.FONTHOLDERIDS_TABLE);
				IProductArchiveElement pFontHolderTable = pResourceElement.createElement(IProductArchiveDefinitions.FONTHOLDER_TABLE);

				if ( pStringsTable != null && pColorIDsTable != null && pColorTable != null&& 
					 pFontIDsTable!= null && pFontHolderIDsTable!= null && pFontHolderTable != null)
				{
				   // Now save the information - StringsTable
				   {
						Enumeration enumVal = m_StringsTable.keys();
						while (enumVal.hasMoreElements())
						{
							String obj = (String)enumVal.nextElement();
							
							if (obj != null)
							{
								IProductArchiveElement pEntry = pStringsTable.createElement(IProductArchiveDefinitions.TABLE_ENTRY);
								if (pEntry != null)
								 {
									Integer id = m_StringsTable.get(obj);
									pEntry.addAttributeString(IProductArchiveDefinitions.TYPE_STRING,obj);
									pEntry.addAttributeLong(IProductArchiveDefinitions.STRINGID_ENTRY,Integer.parseInt(id.toString()));
								 }
							}
						}
				   	}
				   	// ColorIDs
				   	{
						Enumeration enumVal = m_ColorIDs.keys();
						while (enumVal.hasMoreElements())
						{
							Integer id1 = (Integer)enumVal.nextElement();
							Integer id2 = m_ColorIDs.get(id1);
							if (id2 != null)
							{
								IProductArchiveElement pEntry = pColorIDsTable.createElement(IProductArchiveDefinitions.TABLE_ENTRY);
								if (pEntry != null)
								 {
									saveCOMBINEDSTRINGID(pEntry, IProductArchiveDefinitions.COMBINEDSTRINGID_ATTR, Integer.parseInt(id1.toString()));
									pEntry.addAttributeLong(IProductArchiveDefinitions.COLORID_ENTRY,Integer.parseInt(id2.toString()));
								 }
							}
						}
				   	}
				   	// ColorTable
				   	{
						Enumeration enumVal = m_ColorTable.keys();
						while (enumVal.hasMoreElements())
						{
							Integer id = (Integer)enumVal.nextElement();
							ETPairT<Integer, Integer> ids = m_ColorTable.get(id);
							if (ids != null)
							{
								IProductArchiveElement pEntry = pColorTable.createElement(IProductArchiveDefinitions.TABLE_ENTRY);
								if (pEntry != null)
								 {
									pEntry.addAttributeLong(IProductArchiveDefinitions.COLORID_ENTRY, Integer.parseInt(id.toString()));
									saveCOLORREF(pEntry, IProductArchiveDefinitions.COLORREF_ENTRY, Integer.parseInt(ids.getParamOne().toString()));
									saveCOMBINEDSTRINGID(pEntry,IProductArchiveDefinitions.COMBINEDSTRINGID_ATTR, Integer.parseInt(ids.getParamTwo().toString()));
								 }
							}
						}
				   }
				   // FontIDs
				   {
						Enumeration enumVal = m_FontIDs.keys();
						while (enumVal.hasMoreElements())
						{
							Integer cid = (Integer)enumVal.nextElement();
							Integer id = m_FontIDs.get(cid);
							if (cid != null)
							{
								IProductArchiveElement pEntry = pFontIDsTable.createElement(IProductArchiveDefinitions.TABLE_ENTRY);
								if (pEntry != null)
								 {
									saveCOMBINEDSTRINGID(pEntry,IProductArchiveDefinitions.COMBINEDSTRINGID_ATTR, Integer.parseInt(cid.toString()));
									pEntry.addAttributeLong(IProductArchiveDefinitions.FONTID_ENTRY, Integer.parseInt(id.toString()));
								 }
							}
						}
				   }
				   // FontHolderIDs
				   {
						Enumeration enumVal = m_FontHolderIDs.keys();
						while (enumVal.hasMoreElements())
						{
							Integer id = (Integer)enumVal.nextElement();
							ETPairT<Integer, Integer> ids = m_FontHolderIDs.get(id);
							if (ids != null)
							{
								IProductArchiveElement pEntry = pFontHolderIDsTable.createElement(IProductArchiveDefinitions.TABLE_ENTRY);
								if (pEntry != null)
								 {
									pEntry.addAttributeLong(IProductArchiveDefinitions.FONTID_ENTRY, Integer.parseInt(id.toString()));
									pEntry.addAttributeLong(IProductArchiveDefinitions.FONTHOLDERID_ENTRY,  Integer.parseInt(ids.getParamOne().toString()));
									saveCOMBINEDSTRINGID(pEntry,IProductArchiveDefinitions.COMBINEDSTRINGID_ATTR, Integer.parseInt(ids.getParamTwo().toString()));
								 }
							}
						}
				   }
				   // FontHolderTable
				   {
						Enumeration enumVal = m_FontHolderTable.keys();
						while (enumVal.hasMoreElements())
						{
							Integer obj = (Integer)enumVal.nextElement();
							ERFontHolder pHolder = m_FontHolderTable.get(obj);
							if (obj != null & pHolder != null)
							{
								IProductArchiveElement pEntry = pFontHolderTable.createElement(IProductArchiveDefinitions.TABLE_ENTRY);
								if (pEntry != null)
								 {
									pEntry.addAttributeLong(IProductArchiveDefinitions.FONTHOLDERID_ENTRY, obj.intValue());
									pEntry.addAttributeString(IProductArchiveDefinitions.RESOURCEFONTNAME_STRING,   pHolder.getFacename());
									pEntry.addAttributeLong(IProductArchiveDefinitions.RESOURCEFONTCHARSET_STRING,  pHolder.getCharset());
									pEntry.addAttributeLong(IProductArchiveDefinitions.RESOURCEFONTHEIGHT_STRING,   pHolder.getSize());
									pEntry.addAttributeBool(IProductArchiveDefinitions.RESOURCEFONTITALIC_STRING,   pHolder.getItalic());
									pEntry.addAttributeBool(IProductArchiveDefinitions.RESOURCEFONTSTRIKEOUT_STRING,pHolder.getStrikeout());   
									pEntry.addAttributeBool(IProductArchiveDefinitions.RESOURCEFONTUNDERLINE_STRING,pHolder.getUnderline());
									if (pHolder.getBold())
									{
										pEntry.addAttributeLong(IProductArchiveDefinitions.RESOURCEFONTWEIGHT_STRING, 700);  
									}
									else
									{
										pEntry.addAttributeLong(IProductArchiveDefinitions.RESOURCEFONTWEIGHT_STRING, 400);
									}
								 }
							}
						}
					}
				}
			}
		}
	}
	
	public synchronized static ResourceMgr instance( IDrawingAreaControl pDrawingAreaControl )
	{
	   if( pDrawingAreaControl !=null)
	   {
			ResourceMgr pMgr = m_Mgrs.get( pDrawingAreaControl );

		  if( pMgr == null )
		  {
			 pMgr = new ResourceMgr(pDrawingAreaControl);
			
			 m_Mgrs.put(pDrawingAreaControl, pMgr);
		  }
		  return pMgr;
	   }
	   return null;
	}
	
	public synchronized static ResourceMgr instance( IDrawingAreaControl pDrawingAreaControl, IProductArchive pArchive)
	{
		if( pDrawingAreaControl !=null)
		{
			ResourceMgr pMgr = m_Mgrs.get( pDrawingAreaControl );

		  if( pMgr == null )
		  {
			 pMgr = new ResourceMgr(pDrawingAreaControl, pArchive);
			
			 m_Mgrs.put(pDrawingAreaControl, pMgr);
		  }
		  return pMgr;
		}
		return null;		
	}
	
	
	public synchronized void revoke()
	{
	   if( m_RawDrawingAreaControl != null)
	   {
         IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
         if(ctrl != null)
         {
   		  ResourceMgr it = m_Mgrs.get( ctrl );
			  if( it != null )
				 m_Mgrs.remove( ctrl );  
   
           m_RawDrawingAreaControl.clear();
           m_RawDrawingAreaControl = null;
           
           m_StringsTable.clear();
           m_StringsTable = null;
           
           m_ColorIDs.clear();
           m_ColorIDs = null;
           
           m_ColorTable.clear();
           m_ColorTable = null;
           
           m_FontIDs.clear();
           m_FontIDs = null;
                      
           m_FontHolderIDs.clear();
           m_FontHolderIDs = null;
           

           m_FontHolderTable.clear();
           m_FontHolderTable = null;

           m_ColorsToUpgrade.clear();
           m_ColorsToUpgrade = null;
           
           m_FontsToUpgrade.clear();
           m_FontsToUpgrade = null;

           m_pDefaultFontHolder = null;         
   	   }
      }
	}
	
	public IDrawingAreaControl getDrawingArea()
	{
	   return (IDrawingAreaControl)m_RawDrawingAreaControl.get();
	}
	
	public void loadPreferences(boolean bResetAllTables)
	{
		// Go to the presentation resource file and extract all the fonts and colors
		IPresentationResourceMgr pMgr = ProductHelper.getPresentationResourceMgr();
		if (pMgr != null)
		{
			ETList <String > pDrawEngines = pMgr.getAllDrawEngineNames();
			int numDrawEngines = pDrawEngines != null ? pDrawEngines.size() : 0;

			 for (int i = 0 ; i < numDrawEngines ; i++)
			 {
				String sDrawEngine =pDrawEngines.get(i);
				if (sDrawEngine != null && sDrawEngine.length() > 0)
				{
				   ETList <String> pResourceNames = pMgr.getAllResourceNames(sDrawEngine);
				   long numResourceNames = pResourceNames != null ? pResourceNames.size() : 0;

				   for (int j = 0 ; j < numResourceNames ; j++)
				   {
					  String sResourceName = pResourceNames.get(j);
					  if (sResourceName!= null && sResourceName.length() > 0)
					  {
						 // Now we have a combination of the draw engine and resource name
						 String sType = pMgr.getResourceType(sDrawEngine, sResourceName);
						 if (sType.equals("font"))
						 {
							// Load the font
							loadFont(sDrawEngine, sResourceName, false, bResetAllTables);
							// Fonts can specify color too
							loadColor(sDrawEngine, sResourceName, false, bResetAllTables);
						 }
						 else if (sType.equals("color"))
						 {
							loadColor(sDrawEngine, sResourceName, false, bResetAllTables);
						 }
					  }
				   }
				}
			 }
		  }

		 // dumpToFile(NULL, false, false);
	}
	
	public Font getZoomedFont(int nFontID, double nDrawZoom)
	{
		Font  pFont = null;

		double nZoomLevel = nDrawZoom;
		if (nZoomLevel < 0)
		{
			 // Use the current zoom level
			 nZoomLevel = 1.0;

			if( m_RawDrawingAreaControl != null)
			{
            IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
            if(ctrl != null)
            {
				  nZoomLevel = ctrl.getCurrentZoom();
            }
			}
		  }

		  // Now look for this font by id
		  ETPairT<Integer, Integer> ids = m_FontHolderIDs.get(new Integer(nFontID));
		  if (ids != null)
		  {
		  	
			 Integer nFontHolderID = ids.getParamOne();
			ERFontHolder iterator2 = m_FontHolderTable.get(nFontHolderID);
			 if (iterator2 != null)
			 {
				pFont = iterator2.getFont(nZoomLevel);
			 }
		  }
      
		  if (pFont == null)
		  {
			pFont = m_pDefaultFontHolder.getFont(nZoomLevel);
		  }

		  // DumpToFile(NULL, false);

		return pFont;
	}
	
	public int getColor( int nColorID )
	{
		ETPairT<Integer, Integer> cid = m_ColorTable.get(new Integer(nColorID));
		return cid != null ? cid.getParamOne().intValue() : 0;
	}
	
	public String getString(int nID)
	{
		Enumeration enumVal = m_StringsTable.keys();
		while (enumVal.hasMoreElements())
		{
			String obj = (String)enumVal.nextElement();
			if (m_StringsTable.get(obj).intValue() == nID)
			{
				return obj;
			}
		}
		return "";
	}	
	
	public int getStringID(String sStringName)
	{
		Integer StringID = m_StringsTable.get(sStringName);
		if(StringID != null)
		{
			return Integer.parseInt(StringID.toString());
		}
		else
		{
			m_StringsTable.put(sStringName, new Integer(++m_nLastStringID));
			return m_nLastStringID;
		}
	}
	
	public void setDefaultColor(int nDrawEngineStringID, 
								int nResourceStringID,
								int nColor)
	{
	  if (nDrawEngineStringID > 0 && nResourceStringID > 0)
	  {
		 int nID = (int)combineIDs(nDrawEngineStringID, nResourceStringID);

		 // See if we've got a color
		 Integer iterator = m_ColorIDs.get(new Integer(nID));
		 if (iterator == null)
		 {
			// We don't have this color.  Load it up.
			m_nLastColorID++;
			int nFoundColorID = m_nLastColorID;

			ETPairT<Integer, Integer> ids = createIntPair(nColor, 0);
			m_ColorTable.put(new Integer(nFoundColorID), ids);
			m_ColorIDs.put(new Integer(nID), new Integer(nFoundColorID));
		 }
	  }
	}
	
	public boolean isValidFontID(int nFontID)
	{      
	   return m_FontHolderIDs != null && m_FontHolderIDs.get(new Integer(nFontID)) != null;
   }
	
	public boolean isValidColorID(int nColorID)
	{
	   return m_ColorTable != null && m_ColorTable.get(new Integer(nColorID)) != null;
	}
	
	public int getFontID(int nDrawEngineID, int nFontResourceStringID)
	{
      Integer nFontID = null;
      if(m_FontIDs != null)
      {
   	   long nID = combineIDs(nDrawEngineID, nFontResourceStringID);
   	
   	   nFontID = m_FontIDs.get(new Integer((int)nID));
   	   if (nFontID == null)
   	   {
   			// DumpToFile(NULL, false);
   	
   		  // Try loading from the preferences file
   		  String sDrawEngine = getString(nDrawEngineID);
   		  String sResource = getString(nFontResourceStringID);
   	
   		  if (sDrawEngine.length() > 0 && sResource.length() > 0)
   		  {
   			 loadFont(sDrawEngine, sResource, true, false);
   			 nFontID = m_FontIDs.get(new Integer((int)nID));
   		  }
   	   }
      }
	   return nFontID == null ? 0 : nFontID.intValue();
	}
	
	public int getColorID(int nDrawEngineID, int nColorResourceStringID)
	{
	   long nID = combineIDs(nDrawEngineID, nColorResourceStringID);
	
	   Integer iColorID = m_ColorIDs.get(new Integer((int)nID));
	   if (iColorID != null)
	   {
			return Integer.parseInt(iColorID.toString());
	   }
	   else
	   {
		  //DumpToFile(NULL, false);
	
		  // Try loading from the preferences file
		  String sDrawEngine = getString(nDrawEngineID);
		  String sResource = getString(nColorResourceStringID);
	
		  if (sDrawEngine.length() > 0 && sResource.length() > 0)
		  {
			 loadColor(sDrawEngine, sResource, true, false);
			 iColorID = m_ColorIDs.get(new Integer((int)nID));
			 if (iColorID != null)
			 {
				return Integer.parseInt(iColorID.toString());
			 }
		  }
	   }
	
	   return -1;
	}
	
	public void ResetAllTables()
	{
	   m_FontHolderTable.clear();
	   m_ColorIDs.clear();
	   m_ColorTable.clear();
	   m_FontIDs.clear();
	   m_FontHolderIDs.clear();
	   m_FontHolderTable.clear();
	   m_nLastColorID = 0;
	   m_nLastFontID = 0;
	   m_nLastFontHolderID = 0;
	}
	
	int getCombinedStringID(String sDrawEngine,String sResourceName)
	{
	   if (sDrawEngine!= null && sResourceName!= null)
	   {
		  int nDrawEngineID = getStringID(sDrawEngine);
		  int nResourceNameID = getStringID(sResourceName);
	
		  return combineIDs(nDrawEngineID, nResourceNameID);
	   }
	   return 0;
	}
	
	public void loadColor(String sDrawEngine, 
						  String sColorResource,
						  boolean bUseDefaultIfNotFound /* = false*/,
						  boolean bDeleteExisting /* = false*/)
	{
		IPresentationResourceMgr pMgr = ProductHelper.getPresentationResourceMgr();
		if (pMgr != null)
		{
			int nColor = pMgr.getColorResource(sDrawEngine, 
										 sColorResource);
	
			 // Account for any number of failures by using COLORREF == 0
			 if ( nColor ==-1 && bUseDefaultIfNotFound)
			 {
				nColor = pMgr.getColor();
			 }
	
			int nID = getCombinedStringID(sDrawEngine,sColorResource);

			// All default colors are allocated, one entry per string combo in the color
			// table so that if we change the color behind the colorid it changes all
			// instances (ie all stereotype compartment font colors change)
			m_nLastColorID++;
			int nFoundColorID = m_nLastColorID;

			ETPairT<Integer, Integer> cid = createIntPair(nColor, 0);
			m_ColorTable.put(new Integer(nFoundColorID),cid);
            
			// Delete the existing entry if we're told to
			if (bDeleteExisting)
			{
				 m_ColorIDs.remove(new Integer((int)nID));
			}

			// Now add it to the resource string to color id table
			if (m_ColorIDs.get(new Integer((int)nID)) == null)
			{
			   // We might be overlaying, like when we read in a diagram, so don't wipe out
			   // the diagram preferences, only merge in the new ones
			   m_ColorIDs.put(new Integer(nID),new Integer(nFoundColorID));
			}
		  }
	}
	
	public void loadFont(String sDrawEngine, 
								   String sFontResource,
								   boolean bUseDefaultIfNotFound /* = false*/,
								   boolean bDeleteExisting /* = false*/)
	{
		IPresentationResourceMgr pMgr = ProductHelper.getPresentationResourceMgr();
		if (pMgr != null)
		{
			 String sFaceName = "Arial";
			 int nHeight = 12;
			 long nWeight = 400;
			 long nColor = 0;
			 boolean bItalic = false;
			 boolean bFoundIt = false;
	
			ETFontType pETFontType = pMgr.getFontResource(sDrawEngine, sFontResource);
	
			 // Account for any number of failures by getting the default font and using that.
			 if ( pETFontType == null && bUseDefaultIfNotFound)
			 {
				pETFontType = pMgr.getFont();
				if ((pETFontType == null) || (pETFontType.getName() != null && pETFontType.getName().length() == 0 )) 
		
				{
				   // Restore back to hardcoded values
				   sFaceName = "Arial";
				   nHeight = 12;
				   nWeight = 400;
				   nColor = 0;
				   bItalic = false;
				}
			
			 }
			 else 
			 {
				sFaceName = pETFontType.getName();
				nHeight = pETFontType.getHeight();
				bItalic = pETFontType.getItalic();
				nWeight = pETFontType.getWeight();
			 }
	
			
			// First we need to see if this font already exists
			int nExistingFontHolderID = getDefaultFontHolderID(sFaceName,nHeight,nWeight,bItalic);

			if (nExistingFontHolderID < 0)
			{
			   // We found a new font, need to load a new font
			   m_nLastFontHolderID++;
			   nExistingFontHolderID = m_nLastFontHolderID;

//			   m_FontHolderTable[nExistingFontHolderID] = new ERFontHolder(sFaceName, 
//																		nHeight,
//																		(nWeight<700)?FW_NORMAL:FW_BOLD,
//																		bItalic?true:false);
				ERFontHolder pERFontHolder = new ERFontHolder(sFaceName,nHeight,bItalic,nWeight >= 700?true:false);
				m_FontHolderTable.put(new Integer(nExistingFontHolderID), pERFontHolder);
			}
			 
			// Now add it to the font tables
			m_nLastFontID++;
			long nID = getCombinedStringID(sDrawEngine,sFontResource);

			// Delete the existing entry if we're told to
			if (bDeleteExisting)
			{
				m_FontIDs.remove(new Integer((int)nID));
			}

			if (m_FontIDs.get(new Integer((int)nID)) == null)
			{
			   // We might be overlaying, like when we read in a diagram, so don't wipe out
			   // the diagram preferences, only merge in the new ones
			   m_FontIDs.put(new Integer((int)nID), new Integer(m_nLastFontID));
			   ETPairT<Integer, Integer>  cid = createIntPair(nExistingFontHolderID, 0);
			   m_FontHolderIDs.put(new Integer(m_nLastFontID), cid);
			}

		}
	}
	
	public int getDefaultFontHolderID(String sFacename,
									  long nHeight,
									  long nWeight,
									  boolean bItalic)
	{
	   if (sFacename != null)
	   {
			Enumeration enumVal = m_FontHolderTable.keys();
			while (enumVal.hasMoreElements())
			{
				 Integer obj = (Integer)enumVal.nextElement();
			     ERFontHolder pERFontHolder = m_FontHolderTable.get(obj);
			     if (pERFontHolder.isSame(sFacename, (int)nHeight, nWeight>=700?true:false, bItalic))
				{
				   return obj.intValue();
				}
			}
	   }
	   return -1;
	}

	public ETList<IDrawingProperty> getDrawingProperties(IDrawingPropertyProvider pProvider)
	{
		ETList<IDrawingProperty> pOurProperties = new ETArrayList<IDrawingProperty>();
		if (pProvider != null)
		{
			 // Do Fonts
			 Enumeration enumVal = m_FontIDs.keys();
		 	 while (enumVal.hasMoreElements())
			 {
				Integer obj = (Integer)enumVal.nextElement();
				Integer val = m_FontIDs.get(obj);
				int nStringID = obj.intValue();
				if (m_ColorIDs.get(new Integer(nStringID)) != null)
				{
				   int nColorID = m_ColorIDs.get(new Integer(nStringID)).intValue();
				   int nID1 = (int)getID1(nStringID);
				   int nID2 = (int)getID2(nStringID);
				   if (nID1 >0 && nID2>0)
				   {
					  String sString1 = getString(nID1);
					  String sString2 = getString(nID2);

					  Font pFont = getZoomedFont(val.intValue(), 1.0);
					  int nColor = getColor(nColorID);

					 if (pFont != null)
					 {
						IFontProperty pFontProperty = new FontProperty();

						pFontProperty.initialize2(pProvider,sString1, sString2, pFont, nColor);
						pOurProperties.add(pFontProperty);
					 }
				   }
				}
			 }
	
			 // Do Colors
			 Enumeration enumVal2 = m_ColorIDs.keys();
			 while (enumVal2.hasMoreElements())
			 {
				Integer obj = (Integer)enumVal2.nextElement();
				Integer val = m_FontIDs.get(obj);
				int nStringID = Integer.parseInt(obj.toString());
				IColorProperty pColorProperty = new ColorProperty();

				pColorProperty.initialize(pProvider, getString((int)getID1(nStringID)), getString((int)getID2(nStringID)),  getColor(val.intValue()));
				pOurProperties.add(pColorProperty);
			 }
	
		  }

	   return pOurProperties;
	}

	public void saveColor(String sDrawEngineType,
										 String sResourceName,
										 int nColor)
	{
		int nDrawEngineID = getStringID(sDrawEngineType);
		int nResourceID   = getStringID(sResourceName);
	
		if (nDrawEngineID > 0 && nResourceID > 0)
		{
			 // The false on the end of this means that this is a color to be used for all these draw engine
			 // resource combinations.  If it's non-zero then the de or compartment got specialized.
			 //int nAllocatedColorID = 0;
			 saveColor(nDrawEngineID, nResourceID, nColor, true);
		}
	}

	public void saveColor2(IColorProperty pProperty)
	{
		saveColor(pProperty.getDrawEngineName(), pProperty.getResourceName(), pProperty.getColor());
	}

	public void saveFont(String sDrawEngineName,
						String sResourceName,
						String sFaceName,
						int nHeight,
						int nWeight,
						boolean bItalic,
						int nColor)
	{
		int nDrawEngineID = getStringID(sDrawEngineName);
		int nResourceID   = getStringID(sResourceName);
	
		if (nDrawEngineID > 0 && nResourceID > 0)
		{
			 // The false on the end of this means that this is a font to be used for all these draw engine
			 // resource combinations.  If it's non-zero then the de or compartment got specialized.
			int nAllocatedColorID = 0;
			int nAllocatedFontID = 0;
			ETPairT<Integer, Integer> save = saveFont(nDrawEngineID, nResourceID, sFaceName, nHeight, nWeight, bItalic, nColor, true);
		}
	}

	public void saveFont2(IFontProperty pProperty)
	{	
		saveFont(pProperty.getDrawEngineName(), 
			pProperty.getResourceName(), 
			pProperty.getFaceName(), 
			pProperty.getSize(),
			pProperty.getWeight(),
			pProperty.getItalic(),
			pProperty.getColor());
	}

	public void resetToDefaultResource(String sDrawEngineName,
													  String sResourceName,
													  String sResourceType)
	{
		  // Get rid of all the specializations for this resource name
		  int nBothID = (int)getCombinedStringID(sDrawEngineName, sResourceName);
		  if (nBothID > 0)
		  {
			 // Go through the fonts and colors and remove any specializations related
			 // to this draw engine and resource name combinations
			 if (sResourceType.equals("font"))
			 {
				Enumeration enumVal = m_FontHolderIDs.keys();
				while (enumVal.hasMoreElements())
				{
				   Integer obj = (Integer)enumVal.nextElement();
				   ETPairT<Integer, Integer> val = m_FontHolderIDs.get(obj);
				   if (val.getParamTwo().intValue() == nBothID)
				   {
					  // Remove this font id.  Next time folks draw it'll come back as an invalid id
					  // and the default will be provided (ie the one with bothids == 0.
					  m_FontHolderIDs.remove(obj);
				   }
				}
			 }
			 else if (sResourceType.equals("color"))
			 {
				Enumeration enumVal = m_ColorTable.keys();
				while (enumVal.hasMoreElements())
				{
					Integer obj = (Integer)enumVal.nextElement();
					ETPairT<Integer, Integer> val = m_FontHolderIDs.get(obj);
				   if (val.getParamTwo().intValue() == nBothID)
				   {
					  // Remove this font id.  Next time folks draw it'll come back as an invalid id
					  // and the default will be provided (ie the one with bothids == 0.
					  m_ColorTable.remove(obj);
				   }
				}
			 }
		  }
	}

	public void resetToDefaultResources()
	{
	  // Reload from preferences and pass in true to wipe out all
	  // the older resources.
	  loadPreferences(true);
	}

	public void resetToDefaultResources2(String sDrawEngineName)
	{
		if (sDrawEngineName!=null)
		{
			Hashtable<Integer ,Integer> tempFontIDs = new Hashtable<Integer ,Integer>();
			Hashtable<Integer ,Integer> tempColorIDs = new Hashtable<Integer ,Integer>();
	
			 // Remove all the resources for this draw engine
			 int nID = getStringID(sDrawEngineName);
			 if (nID > 0)
			 {
				// Go through the tables and find those items with this int as
				// the top of the COMBINEDint.  We don't want to delete them because
				// they could have been placed there by compartments we haven't anticipated.  Rather
				// Remove them from the colors and fonts list and the add them back in once
				// the defaults have repopulated the list - but only if they don't exist in that
				// repopulated list.
				Enumeration enumVal1 = m_FontIDs.keys();	
				while (enumVal1.hasMoreElements())
				{
					Integer obj = (Integer)enumVal1.nextElement();
					Integer val = m_FontIDs.get(obj);
				   int nThisID = Integer.parseInt(obj.toString());
				   int nDEint = (int)getID1(nThisID);
				   int nResourceint = (int)getID2(nThisID);
	
				   if (nID == nDEint)
				   {
					  tempFontIDs.put(new Integer(nThisID), val);
					  // Remove this font id.  Next time folks draw it'll come back as an invalid id
					  // and the default will be provided (ie the one with bothids == 0.
					  m_FontIDs.remove(obj);
				   }
				}
	            
				Enumeration enumVal2 = m_ColorIDs.keys();
				while (enumVal2.hasMoreElements())
				{
					Integer obj = (Integer)enumVal2.nextElement();
				   int nThisID = Integer.parseInt(obj.toString());
				   //int nResourceint = (int)getID2(nThisID);
	
				   if (nID == (int)getID1(nThisID))
				   {
					  tempColorIDs.put(new Integer(nThisID),  m_FontIDs.get(obj));
	
					  // Remove this font id.  Next time folks draw it'll come back as an invalid id
					  // and the default will be provided (ie the one with bothids == 0.
					  m_ColorIDs.remove(obj);
				   }
				}
			 }
	
			 // Reload from preferences and pass in false to not wipe out all
			 // the older resources.
			 loadPreferences(false);
	
			 // Now add back those resources we removed so the load would work.  But only add those that
			 // we not placed there by the call to LoadPreferences which put the defaults back from the
			 // file.
			Enumeration enumVal3 = tempFontIDs.keys();	
			while (enumVal3.hasMoreElements())
			 {
				Integer obj = (Integer)enumVal3.nextElement();
				if (m_FontIDs.get(obj) == null)
				{
				   // Add this back
				   m_FontIDs.put(obj, tempFontIDs.get(obj));
				}
			 }
			 
			Enumeration enumVal4 = tempColorIDs.keys();
			while (enumVal4.hasMoreElements())
			 {
				Integer obj = (Integer)enumVal4.nextElement();
				if (m_ColorIDs.get(obj) == null)
				{
				   // Add this back
				   m_ColorIDs.put(obj, m_FontIDs.get(obj));
				}
			 }
		  }
	}

	public void dumpToFile(String sFile, boolean bAppendToExistingFile)
	{
//	   CStdioFile file;
//	   xstring tempFilename(_T("C:\\ResourceMgr.txt"));
//	   USES_CONVERSION;
//	
//	   if (sFile)
//	   {
//		  tempFilename = W2T(sFile);
//	   }
//	
//	   UINT nOpenFlags = CFile::modeCreate | CFile::modeWrite | CFile::typeText;
//	   if (bAppendToExistingFile)
//	   {
//		  // Don't delete the existing file
//		  nOpenFlags = CFile::modeWrite | CFile::typeText;
//	   }
//	
//	   if (file.Open(tempFilename.c_str(), nOpenFlags ) )
//	   {
//		  xstring outputString;
//		  xstring sTitleString;
//		  xstring sCreditsString;
//		  std::map < xstring, int >::iterator iterator1;
//		  std::map < COMBINEDint , COLORID >::iterator iterator2;
//		  std::map < COLORID , PAIR_COLORREF_COMBINEDint >::iterator iterator3;
//		  std::map < COMBINEDint , FONTID >::iterator iterator4;
//		  std::map < FONTID , PAIR_FONTHOLDERID_COMBINEDint >::iterator iterator5;
//		  std::map < FONTHOLDERID , CERFontHolder* >::iterator iterator6;
//	      
//		  sTitleString = StringUtilities::Format(_T("*** Begin Dump of ResourceMgr %x ***\n"), 
//												 (int)this);
//		  sCreditsString = StringUtilities::Format(_T("*** End Dump of ResourceMgr %x ***\n"), 
//												   (int)this);
//	
//		  file.WriteString( sTitleString.c_str() );
//		  file.WriteString( _T("m_StringsTable (xstring, int) : \n") );
//		  for (iterator1 = m_StringsTable.begin() ; iterator1 != m_StringsTable.end() ; ++iterator1)
//		  {
//			 xstring tempString(iterator1.first);
//			 int nID = iterator1.second;
//	
//			 outputString = StringUtilities::Format(_T("[%s] = %d\n"), 
//													tempString.size()?tempString.c_str():_T(""), 
//													nID);
//			 file.WriteString(outputString.c_str());
//		  }
//		  outputString = StringUtilities::Format(_T("m_nLastint = %d\n"), 
//												 m_nLastint);
//		  file.WriteString(outputString.c_str());
//	
//		  file.WriteString( _T("m_ColorIDs (COMBINEDint, COLORID)  : \n") );
//		  for (iterator2 = m_ColorIDs.begin() ; iterator2 != m_ColorIDs.end() ; ++iterator2)
//		  {
//			 COMBINEDint nID1 = iterator2.first;
//			 COLORID nID2 = iterator2.second;
//	
//			 int nDEID = 0;
//			 int nNameID = 0;
//	
//			 BREAK_intS(nID1, nDEID, nNameID);
//	
//			 outputString = StringUtilities::Format(_T("[%d:%d] = %d\n"), 
//													nDEID, 
//													nNameID,
//													nID2);
//			 file.WriteString(outputString.c_str());
//		  }
//	
//		  file.WriteString( _T("m_ColorTable (COLORID, (COLORREF,COMBINEDint) ) : \n") );
//		  for (iterator3 = m_ColorTable.begin() ; iterator3 != m_ColorTable.end() ; ++iterator3)
//		  {
//			 COLORID nColorID = iterator3.first;
//			 COLORREF nColor = (COLORREF)(iterator3.second.first);
//			 COMBINEDint nint = iterator3.second.second;
//	
//			 outputString = StringUtilities::Format(_T("[%d] = %d,%d\n"), 
//													nColorID, 
//													nColor,
//													nint);
//			 file.WriteString(outputString.c_str());
//		  }
//		  outputString = StringUtilities::Format(_T("m_nLastColorID = %d\n"), 
//												 m_nLastColorID);
//		  file.WriteString(outputString.c_str());
//	
//		  file.WriteString( _T("m_FontIDs (COMBINEDint, FONTID ) : \n") );
//		  for (iterator4 = m_FontIDs.begin() ; iterator4 != m_FontIDs.end() ; ++iterator4)
//		  {
//			 COMBINEDint nID1 = iterator4.first;
//			 FONTID nID2 = iterator4.second;
//	         
//			 int nDEID = 0;
//			 int nNameID = 0;
//	
//			 BREAK_intS(nID1, nDEID, nNameID);
//	
//			 outputString = StringUtilities::Format(_T("[%d:%d] = %d\n"), 
//													nDEID, 
//													nNameID,
//													nID2);
//			 file.WriteString(outputString.c_str());
//		  }
//	
//		  file.WriteString( _T("m_FontHolderIDs (FONTID, ( FONTHOLDERID, COMBINEDint ) ) : \n") );
//		  for (iterator5 = m_FontHolderIDs.begin() ; iterator5 != m_FontHolderIDs.end() ; ++iterator5)
//		  {
//			 FONTID nID1 = iterator5.first;
//			 FONTHOLDERID nID2 = iterator5.second.first;
//			 COMBINEDint nint = iterator5.second.second;
//	
//			 outputString = StringUtilities::Format(_T("[%d] = %d,%d\n"), 
//													nID1, 
//													nID2,
//													nint);
//			 file.WriteString(outputString.c_str());
//		  }
//		  outputString = StringUtilities::Format(_T("m_nLastFontID = %d\n"), 
//												 m_nLastFontID);
//		  file.WriteString(outputString.c_str());
//	
//		  file.WriteString( _T("m_FontHolderTable (FONTHOLDERID, CERFontHolder* ) : \n") );
//		  for (iterator6 = m_FontHolderTable.begin() ; iterator6 != m_FontHolderTable.end() ; ++iterator6)
//		  {
//			 FONTHOLDERID nID1 = iterator6.first;
//			 CERFontHolder* pHolder = iterator6.second;
//	
//			 if (pHolder)
//			 {
//				xstring xsFaceName = pHolder.GetFacename();
//				int nSize = pHolder.GetSize();
//				bool bBold = pHolder.GetBold();
//	            
//				outputString = StringUtilities::Format(_T("[%d] = 0x%X (%s, size=%d, bold=%d)\n"),
//													   nID1,
//													   pHolder,
//													   xsFaceName.size()?xsFaceName.c_str(): _T(""),
//													   nSize,
//													   bBold?1:0);
//			 }
//			 else
//			 {
//				outputString = StringUtilities::Format(_T("[%d] = 0x00000000\n"), 
//													   nID1);
//			 }
//	
//			 file.WriteString(outputString.c_str());
//		  }
//		  outputString = StringUtilities::Format(_T("m_nLastFontHolderID = %d\n"), 
//												 m_nLastFontHolderID);
//		  file.WriteString(outputString.c_str());
//		  file.WriteString( sCreditsString.c_str() );
//		  file.Close();
//	   }
//	
//	   return S_OK;
	}

	public int saveColor(int nDrawEngineID,
							 int nResourceID,
							 int nColor,
							 boolean bGeneralOverride)
	{
	   int nAllocatedColorID = 0;
	
	   // Find the resource and eliminate the id so that future folks that
	   // draw get the new color and don't use the old one.
	   int nBothIDs = (int)combineIDs(nDrawEngineID, nResourceID);
	
	   if (bGeneralOverride)
	   {
		  // We're changing the default color (ie all stereotype font colors)
	
		  // Remove the id from the color table so that future users reget the new color
		  Integer iterator1 = m_ColorIDs.get(new Integer(nBothIDs));
		  if (iterator1 != null)
		  {
			ETPairT<Integer, Integer> iterator2 = m_ColorTable.get(iterator1);
			 if (iterator2 != null)
			 {
				m_ColorTable.remove(iterator1);
			 }
		  }
	
		  m_nLastColorID++;
		  m_ColorIDs.put(new Integer(nBothIDs), new Integer(m_nLastColorID));  // Assign here to make it the default
		  ETPairT<Integer, Integer> val = createIntPair(nColor, 0);
		  m_ColorTable.put(new Integer(m_nLastColorID), val);
		  nAllocatedColorID = m_nLastColorID;
	   }
	   else
	   {
		  // We're changing just for this resource user
		  m_nLastColorID++;
		  ETPairT<Integer, Integer> val = createIntPair(nColor, nBothIDs);
		  m_ColorTable.put(new Integer(m_nLastColorID), val);
		  nAllocatedColorID = m_nLastColorID;
	   }
	
	   return nAllocatedColorID;
	}

	public ETPairT<Integer, Integer> saveFont(int nDrawEngineID,
								int nResourceID,
								String sFaceName,
								int nHeight,
								int nWeight,
								boolean bItalic,
								int nColor,
								boolean bGeneralOverride)
	{
		int nAllocatedFontID = 0;
		boolean bDidChange = false;
		
		ETPairT<Integer, Integer> retVal = new ETPairT<Integer, Integer>();
		
		int nAllocatedColorID = saveColor(nDrawEngineID, nResourceID, nColor, bGeneralOverride);
		if (nAllocatedColorID != 0)
		{
			 // Find the resource and eliminate the id so that future folks that
			 // draw get the new font and don't use the old one.
			int nBothIDs = (int)combineIDs(nDrawEngineID, nResourceID);
	
			 if (bGeneralOverride)
			 {
				// We're changing the default font (ie all stereotype font)
	
				// First get the default font holder id
				int nFontHolderID = getDefaultFontHolderID(sFaceName,
														  nHeight,
														  nWeight,
														  bItalic);
				if (nFontHolderID < 0)
				{
				   // We found a new font, need to load a new font
				   m_nLastFontHolderID++;
				   nFontHolderID = m_nLastFontHolderID;
	
//				   m_FontHolderTable.put(nFontHolderID, new CERFontHolder(W2T(sFaceName), 
//															 nHeight,
//															 (nWeight<700)?FW_NORMAL:FW_BOLD,
//															 bItalic?true:false));
					
					ERFontHolder pERFontHolder = new ERFontHolder(sFaceName,nHeight, bItalic,nWeight>=700?true:false);
					m_FontHolderTable.put(new Integer(nFontHolderID), pERFontHolder);
				}
	            
				// Remove the id from the color table so that future users reget the new color
				Integer iterator1 = m_FontIDs.get(new Integer(nBothIDs));
				if (iterator1 != null)
				{
				   Integer nOldFontID = iterator1;
	               
				   ETPairT<Integer, Integer> iterator2 = m_FontHolderIDs.get(nOldFontID);
				   if (iterator2 != null)
				   {
					  m_FontHolderIDs.remove(nOldFontID);
				   }
				}
	
				m_nLastFontID++;
				m_FontIDs.put(new Integer(nBothIDs),new Integer(m_nLastFontID)); // Assign here to make it the default
				ETPairT<Integer, Integer> val = createIntPair(nFontHolderID, 0);

				m_FontHolderIDs.put(new Integer(m_nLastFontID),  val);
				nAllocatedFontID = m_nLastFontID;
			 }
			 else
			 {
	
				// First get the default font holder id
				int nFontHolderID = getDefaultFontHolderID(sFaceName,
														  nHeight,
														  nWeight,
														  bItalic);
				if (nFontHolderID < 0)
				{
				   // We found a new font, need to load a new font
				   m_nLastFontHolderID++;
				   nFontHolderID = m_nLastFontHolderID;
	
//				   m_FontHolderTable.put(nFontHolderID,new CERFontHolder(W2T(sFaceName), 
//															 nHeight,
//															 (nWeight<700)?FW_NORMAL:FW_BOLD,
//															 bItalic?true:false));
					ERFontHolder pERFontHolder = new ERFontHolder(sFaceName, nHeight, bItalic, false, nWeight>=700?true:false);
					m_FontHolderTable.put(new Integer(nFontHolderID), pERFontHolder);
				}
	
				// We're changing just for this resource user
				m_nLastFontID++;
				ETPairT<Integer, Integer> Val = createIntPair(nFontHolderID, nBothIDs);
				m_FontHolderIDs.put(new Integer(m_nLastFontID),Val);
				nAllocatedFontID = m_nLastFontID;
	
				bDidChange = true;
			 }
	
			 if (bDidChange && m_RawDrawingAreaControl != null)
			 {
             IDrawingAreaControl ctrl = (IDrawingAreaControl)m_RawDrawingAreaControl.get();
             if(ctrl != null)
				 {
                ctrl.setIsDirty(true);
             } 
			 }
		}
	
		retVal.setParamOne(new Integer(nAllocatedFontID));
		retVal.setParamTwo(new Integer(nAllocatedColorID));
		return retVal;
	}

	public boolean displayFontDialog(IFontProperty pProperty)
	{
		boolean bChanged = false;
		Font font = FontChooser.selectFont();
		if (font != null)
		{
			pProperty.setWeight(font.isBold() ? 700 : 400);
			pProperty.setFaceName(font.getName());
			pProperty.setSize(font.getSize());
			pProperty.setItalic(font.isItalic());
			bChanged = true;
		}
		return bChanged;
	}

	public boolean displayColorDialog(IColorProperty pProperty)
	{
		boolean bChanged = false;
		Color color = JColorChooser.showDialog(null, 
                NbBundle.getMessage(ETEditableCompartment.class, "TITLE_Color_Chooser"), null);
		if (color != null)
		{
			pProperty.setColor(color.getRGB());
			bChanged = true;
		}

		return bChanged;
	}
}

/* Sample of how the tables look in memory
*** Begin Dump of ResourceMgr 37d19d0 ***
m_StringsTable (xstring, int) : 
[ADLabelDrawEngine] = 26
[ClassDrawEngine] = 1
[InterfaceDrawEngine] = 22
[classborder] = 3
[classfill] = 2
[ellipseborder] = 21
[ellipsefill] = 20
[label] = 23
[labelborder] = 25
[labelfill] = 24
[name] = 5
[packageattribute] = 15
[packageimport] = 8
[packageoperation] = 19
[privateattribute] = 13
[privateoperation] = 17
[protectedattribute] = 14
[protectedoperation] = 18
[publicattribute] = 12
[publicoperation] = 16
[statictext] = 10
[stereotype] = 6
[taggedvalues] = 7
[template] = 11
[templateparameters] = 9
[titlefont] = 4
m_nLastint = 26
m_ColorIDs (COMBINEDint, COLORID)  : 
[1:2] = 1
[1:3] = 2
[1:4] = 3
[1:5] = 4
[1:6] = 5
[1:7] = 6
[1:8] = 7
[1:9] = 8
[1:10] = 9
[1:11] = 10
[1:12] = 11
[1:13] = 12
[1:14] = 13
[1:15] = 14
[1:16] = 15
[1:17] = 16
[1:18] = 17
[1:19] = 18
m_ColorTable (COLORID, (COLORREF,COMBINEDint) ) : 
[1] = 65535,0
[2] = 0,0
[3] = 0,0
[4] = 0,0
[5] = 0,0
[6] = 0,0
[7] = 0,0
[8] = 0,0
[9] = 0,0
[10] = 0,0
[11] = 0,0
[12] = 0,0
[13] = 0,0
[14] = 0,0
[15] = 0,0
[16] = 0,0
[17] = 0,0
[18] = 0,0
m_nLastColorID = 18
m_FontIDs (COMBINEDint, FONTID ) : 
[1:4] = 1
[1:5] = 2
[1:6] = 3
[1:7] = 4
[1:8] = 5
[1:9] = 6
[1:10] = 7
[1:11] = 8
[1:12] = 9
[1:13] = 10
[1:14] = 11
[1:15] = 12
[1:16] = 13
[1:17] = 14
[1:18] = 15
[1:19] = 16
m_FontHolderIDs (FONTID, ( FONTHOLDERID, COMBINEDint ) ) : 
[1] = 1,0
[2] = 2,0
[3] = 3,0
[4] = 3,0
[5] = 3,0
[6] = 3,0
[7] = 3,0
[8] = 3,0
[9] = 3,0
[10] = 3,0
[11] = 3,0
[12] = 3,0
[13] = 3,0
[14] = 3,0
[15] = 3,0
[16] = 3,0
m_nLastFontID = 16
m_FontHolderTable (FONTHOLDERID, CERFontHolder* ) : 
[1] = 0x37EF4D0 (Arial, size=8, bold=0)
[2] = 0x37EF7F8 (Arial, size=12, bold=1)
[3] = 0x37EFB08 (Arial, size=12, bold=0)
m_nLastFontHolderID = 3
*** End Dump of ResourceMgr 37d19d0 ***
*/



