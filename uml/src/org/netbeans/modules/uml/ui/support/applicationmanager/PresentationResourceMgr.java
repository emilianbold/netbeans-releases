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
 * Created on Feb 3, 2004
 *
 */
package org.netbeans.modules.uml.ui.support.applicationmanager;

import java.awt.Color;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.core.configstringframework.ConfigStringTranslator;
import org.netbeans.modules.uml.core.configstringframework.IConfigStringTranslator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.drawingproperties.ColorProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.ETFontType;
import org.netbeans.modules.uml.ui.support.drawingproperties.FontProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IColorProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import org.netbeans.modules.uml.ui.support.drawingproperties.IFontProperty;

/**
 * @author jingmingm
 *
 */
public class PresentationResourceMgr implements IPresentationResourceMgr
{
	public static final String TYPENAME = "type";
	public static final String FONT = "font";
	public static final String FACENAME = "facename";
	public static final String HEIGHT = "height";
	public static final String WEIGHT = "weight";
	public static final String COLORVALUE = "colorvalue";
	public static final String COLOR = "color";
	public static final String NAME = "name";
	public static final String RESOURCE = "resource";
	public static final String TYPE = "type";
	public static final String DEFAULTRESOURCE = "defaultresource";
	public static final String ITALIC = "italic";
	public static final String DRAWENGINE = "drawengine";
	public static final String DISPLAYNAME = "displayName";
	public static final String DESCRIPTION = "description";
	public static final String DIAGRAMTYPE = "diagramtype";
	public static final String ADVANCED = "advanced";
	public static final String STANDARDDRAWENGINE = "standarddrawengine";

	// The diagram types
	public static final String ACTIVITY_DIAGRAM  = "activity";
	public static final String CLASS_DIAGRAM  = "class";
	public static final String COLLABORATION_DIAGRAM  = "collaboration";
	public static final String COMPONENT_DIAGRAM  = "component";
	public static final String DEPLOYMENT_DIAGRAM  = "deployment";
	public static final String SEQUENCE_DIAGRAM  = "sequence";
	public static final String STATE_DIAGRAM  = "state";
	public static final String USECASE_DIAGRAM  = "usecase";
	public static final String ENTITY_DIAGRAM  = "entity";
	
	protected static final String IDR_OVERRIDDENCOLORS = "OverriddenColors.htm"; 
//"c:\\development\\uml\\gui\\support\\applicationmanager\\OverriddenColors.htm";
//	<?xml version="1.0"?>
//	<EMBT:OverriddenColors version="1.0" xmlns:EMBT="www.sun.com">
//		<Version>1</Version>
//	</EMBT:OverriddenColors>

	// The loaded document
	protected Document m_DefaultDoc = null;
	protected Document m_OverriddenDoc = null;

	// The default location of the files
	protected String m_DefaultResourceLoc = "";
	protected String m_OverriddenResourceLoc = "";
	
	protected void reset()
	{
		m_DefaultResourceLoc = "";
		m_OverriddenResourceLoc = "";
	}
	
	protected Node getOrCreateElement(String sDrawEngineName)
	{
		Node pFoundOrCreatedNode = null;
		
		boolean bIsValid = validateFiles();
		if (bIsValid && m_OverriddenDoc != null)
		{
			Element pRoot = m_OverriddenDoc.getRootElement();
			Node pRootNode = (Node)pRoot;
			if (pRootNode != null)
			{
				// Now see if the sDrawEngineName parent is there
				// Typical Entry
				//	<drawengine type="ClassDrawEngine">
				  //	   <resource type='color' name="fill" value="16777215"/>	
				  //	   <resource type='font' name="stereotype" facename="Arial" height="12" weight="700" color="16777215"/> 
				 //	</drawengine>
				// 
				// Do a select like follows
				// ./drawengine[@type='sDrawEngineName']/resource[@name='sResourceName') ]
				String query = "./drawengine[@type='" + sDrawEngineName + "']";
				pFoundOrCreatedNode = pRootNode.selectSingleNode(query);

				if (pFoundOrCreatedNode == null)
				{
					pFoundOrCreatedNode = (Node)XMLManip.createElement(pRoot, DRAWENGINE);

					if (pFoundOrCreatedNode != null)
					{
						XMLManip.setAttributeValue(pFoundOrCreatedNode, TYPE, sDrawEngineName);
					}
				}
			}
		}

		return pFoundOrCreatedNode;
	}
	
	protected  String getResourceType(Document pDomDocument, String sDrawEngineName, String sResourceName)
	{
		String sResourceType = "";

		Element pRoot = pDomDocument.getRootElement();
		Node pRootNode = (Node)pRoot;
		if (pRootNode != null)
		{
			 // Typical Entry
			 //	<drawengine type="ClassDrawEngine">
			   //	   <resource type='color' name="fill" value="16777215"/>	
			   //	   <resource type='font' name="stereotype" facename="Arial" height="12" weight="700" color="16777215"/> 
			  //	</drawengine>
			 // 
			 // Do a select like follows
			 // ./drawengine[@type='sDrawEngineName']/resource[@name='sResourceName') ]

			String query = "./drawengine[@type='" + sDrawEngineName + "']/resource[@name='" + sResourceName + "']";
			Node pNode = pRootNode.selectSingleNode(query);
			if (pNode != null)
			{
				sResourceType = XMLManip.getAttributeValue(pNode, TYPENAME);
			}
		}
		return sResourceType;
	}
	
	protected IDrawingProperty loadFont(String sDrawEngine, String sFontResource)
	{
		IFontProperty pFoundProperty = new FontProperty();
		ETFontType pETFontType = getFontResource(sDrawEngine, sFontResource); 
		if (pETFontType != null)
		{
			pFoundProperty.initialize(null,
									sDrawEngine,
									sFontResource,
									pETFontType.getName(),
									(short)0,
									(short)pETFontType.getHeight(),
									pETFontType.getItalic(),
									false,
									false,
									pETFontType.getWeight(),
									pETFontType.getColor());

		}

	   return (IDrawingProperty)pFoundProperty;
	}

	protected IDrawingProperty loadColor(String sDrawEngine, String sColorResource)
	{
		IColorProperty pFoundProperty = new ColorProperty();
    	int nColor = getColorResource(sDrawEngine, sColorResource);
		//if (nColor != -1)
		{
			pFoundProperty.initialize(null, sDrawEngine, sColorResource, nColor);
		}
		return (IDrawingProperty)pFoundProperty;
	}
	
	protected ETList<String> getAllDrawEngineNames(Document pDomDocument)
	{
		ETList <String> pDrawEngines = new ETArrayList<String>();
		
		Element pRoot = pDomDocument.getRootElement();
		Node pRootNode = (Node)pRoot;
		if (pRootNode != null)
		{
			 // Typical Entry
			 //	<drawengine type="ClassDrawEngine">
			   //	   <resource type='color' name="fill" value="16777215"/>	
			   //	   <resource type='font' name="stereotype" facename="Arial" height="12" weight="700" color="16777215"/> 
			  //	</drawengine>
			 // 
			 // Do a select like follows
			 // ./drawengine[@type]

			String query = "./drawengine[@type]";
			List pNodes = pRootNode.selectNodes(query);
			if (pNodes != null)
			{
				int nLen = pNodes.size();
				for(int ii = 0; ii < nLen; ii++)
				{
					Node pNode = (Node)pNodes.get(ii);
					if (pNode != null)
					{
						String sDrawEngineName = XMLManip.getAttributeValue(pNode, TYPENAME);
						if (sDrawEngineName != null && sDrawEngineName.length() > 0)
						{
							boolean bAddIt = true;
							if (sDrawEngineName.equals("TitleBlockDrawEngine") ||
								sDrawEngineName.equals("ERViewDrawEngine") ||
								sDrawEngineName.equals("EREntityDrawEngine") ||
								sDrawEngineName.equals("EREntityAssociationEdgeDrawEngine") )
							{
								bAddIt = false;
							}

							if (bAddIt)
					 		{
								pDrawEngines.add(sDrawEngineName);
					 		}
				  		}
			   		}
				}
		 	}
	  	}
	  	
		return pDrawEngines;
   	}
	
	protected ETList<String> getAllResourceNames(Document pDomDocument, String sDrawEngineName)
	{
		ETList <String> pResourceNames = new ETArrayList<String>();
		
		Element pRoot = pDomDocument.getRootElement();
		Node pRootNode = (Node)pRoot;
		if (pRootNode != null)
		{
			 // Typical Entry
			 //	<drawengine type="ClassDrawEngine">
			   //	   <resource type='color' name="fill" value="16777215"/>	
			   //	   <resource type='font' name="stereotype" facename="Arial" height="12" weight="700" color="16777215"/> 
			  //	</drawengine>
			 // 
			 // Do a select like follows
			 // ./drawengine[@type=sDrawEngineName]/resource
			String query = "./drawengine[@type='" + sDrawEngineName + "']/resource";
			List pNodes = pRootNode.selectNodes(query);
			if (pNodes != null)
			{
				int nLen = pNodes.size();
				for(int ii = 0; ii < nLen; ii++)
				{
					Node pNode = (Node)pNodes.get(ii);
					if (pNode != null)
					{
						String sResourceName = XMLManip.getAttributeValue(pNode, NAME);
						if (sResourceName != null && sResourceName.length() > 0)
						{
							pResourceNames.add(sResourceName);
						}
					}
				}
			}
		}

		return pResourceNames;
	}
	
	protected ETPairT<String, String> retrieveDefaultPresentationResourceLocation()
	{
		ETPairT<String, String> retVal = new ETPairT<String, String>();

		if( m_DefaultResourceLoc.length() == 0 || m_OverriddenResourceLoc.length() == 0)
		{
			IProduct prod = ProductHelper.getProduct();
			IConfigManager config = null;
			if(prod != null)
			{
				config = prod.getConfigManager();
			}
			else
			{
				config = new ConfigManager();
			}
	
			if(config != null)
			{
				if (m_DefaultResourceLoc.length() == 0)
				{
					m_DefaultResourceLoc = config.getDefaultResourcesLocation();
				}
				if (m_OverriddenResourceLoc.length() == 0)
				{
					m_OverriddenResourceLoc = config.getOverriddenResourcesLocation();
				}
			}
		}
		
		retVal.setParamOne(m_DefaultResourceLoc);
		retVal.setParamTwo(m_OverriddenResourceLoc);
		return retVal;
	}
	
	protected ETFontType getFont(Document pDomDocument)
	{
		ETFontType retVal = null;
		
		Element pRoot = pDomDocument.getRootElement();
		Node pRootNode = (Node)pRoot;
		if (pRootNode != null)
		{
			// Typical Entry
			//	<defaultresource type="font" facename="Arial" height="12" weight="400" colorvalue="0"/>
			// 
			// Do a select like follows
			// ./defaultresource[@type='font']

			String query = "./defaultresource[@type='font']";
			Node pNode = pRootNode.selectSingleNode(query);

			if (pNode != null)
			{
				retVal = new ETFontType();
				retVal.setName(XMLManip.getAttributeValue(pNode, FACENAME));
				retVal.setHeight(XMLManip.getAttributeIntValue(pNode, HEIGHT));
				retVal.setWeight(XMLManip.getAttributeIntValue(pNode, WEIGHT));
				retVal.setItalic(XMLManip.getAttributeBooleanValue(pNode, ITALIC));	
				retVal.setColor(readCOLORREF(pNode));
			}
		}

		return retVal;
	}
	
	protected int getColor(Document pDomDocument)
	{
		int nColor = -1;

		Element pRoot = pDomDocument.getRootElement();
		Node pRootNode = (Node)pRoot;
		if (pRootNode != null)
		{
			 // Typical Entry
			 //	<defaultresource type="color" colorvalue="0"/>
			 // 
			 // Do a select like follows
			 // ./defaultresource[@type='color']

			String query = "./defaultresource[@type='color']";
			Node pNode = pRootNode.selectSingleNode(query);

			if (pNode != null)
			{
				nColor = readCOLORREF(pNode);
			}
		}

		return nColor;
	}

	protected  ETFontType getFontResource(Document pDomDocument, String sDrawEngineName, String sResourceName)
	{
		ETFontType retVal = null;
		Element pRoot = pDomDocument.getRootElement();
		Node pRootNode = (Node)pRoot;
		if (pRootNode != null)
		{
			 // Typical Entry
			 //	<drawengine type="ClassDrawEngine">
			   //	   <resource type='color' name="fill" value="16777215"/>	
			   //	   <resource type='font' name="stereotype" facename="Arial" height="12" weight="700" color="16777215"/> 
			  //	</drawengine>
			 // 
			 // Do a select like follows
			 // ./drawengine[@type='sDrawEngineName']/resource[@name='sResourceName') ]

			String query = "./drawengine[@type='" + sDrawEngineName + "']/resource[@name='" + sResourceName + "']";
			Node pNode = pRootNode.selectSingleNode(query);
			if (pNode != null)
			{
				retVal = new ETFontType();
				retVal.setName(XMLManip.getAttributeValue(pNode, FACENAME));
				retVal.setHeight(XMLManip.getAttributeIntValue(pNode, HEIGHT) + 4);
				retVal.setWeight(XMLManip.getAttributeIntValue(pNode, WEIGHT));	
				retVal.setItalic(XMLManip.getAttributeBooleanValue(pNode, ITALIC));
				retVal.setColor(readCOLORREF(pNode));
			}
		}


	   return retVal;
	}
	
	protected int getColorResource(Document pDomDocument, String sDrawEngineName, String sResourceName)
	{
		int nColor = -1;

		Element pRoot = pDomDocument.getRootElement();
		Node pRootNode = (Node)pRoot;
		if (pRootNode != null)
		{
			 // Typical Entry
			 //	<drawengine type="ClassDrawEngine">
			   //	   <resource type='color' name="fill" value="16777215"/>	
			   //	   <resource type='font' name="stereotype" facename="Arial" height="12" weight="700" color="16777215"/> 
			  //	</drawengine>
			 // 
			 // Do a select like follows
			 // ./drawengine[@type='sDrawEngineName']/resource[@name='sResourceName') ]
         
			String query ="./drawengine[@type='" + sDrawEngineName + "']/resource[@name='" + sResourceName + "']";
			Node pNode = pRootNode.selectSingleNode(query);
			if (pNode != null)
			{
				nColor = readCOLORREF(pNode);
			}
		 }

		return nColor;
	}
	
	public boolean validateFiles()
	{
		boolean bIsValid = false;;

		try
		{
			if (m_DefaultDoc == null && m_OverriddenDoc == null)
			{
				// Open the file and see if it's valid
				ETPairT<String, String> retVal = this.retrieveDefaultPresentationResourceLocation();
				String xsDefaultResources = retVal.getParamOne();
				String xsOverriddenResources = retVal.getParamTwo();
	
				if (xsDefaultResources != null && xsDefaultResources.length() > 0 &&
					xsOverriddenResources != null && xsOverriddenResources.length() >0)
				{
					// See if the file will load
					m_DefaultDoc = XMLManip.getDOMDocument(xsDefaultResources);
					m_OverriddenDoc = XMLManip.getDOMDocument(xsOverriddenResources);
					
					// If we fail to open the overridden colors file then create an empty one
					if (m_DefaultDoc != null && m_OverriddenDoc == null)
					{
						//String header = UMLXMLManip.retrieveXMLFragmentFromResource(getClass(), IDR_OVERRIDDENCOLORS);
						//m_OverriddenDoc = XMLManip.getDOMDocument(header);
						//m_OverriddenDoc = XMLManip.getDOMDocumentFromString("<?xml version='1.0'?><EMBT:OverriddenColors version='1.0' xmlns:EMBT='www.sun.com'><Version>1</Version></EMBT:OverriddenColors>");
						m_OverriddenDoc = XMLManip.loadXML("<?xml version='1.0'?><EMBT:OverriddenColors version='1.0' xmlns:EMBT='www.sun.com'><Version>1</Version></EMBT:OverriddenColors>");
						if (m_OverriddenDoc != null)
						{
							XMLManip.save(m_OverriddenDoc, xsOverriddenResources);
						}
					}
				}
	
				// Now we should be valid, otherwise we've got problems!
				if (m_DefaultDoc == null || m_OverriddenDoc == null)
				{
					reset();
					bIsValid = false;
				}
				else
				{
					bIsValid = true;
				}
			}
			
			if (m_DefaultDoc != null &&	m_OverriddenDoc != null)
			{
			   bIsValid = true;
			}
		}
		catch (Exception e)
		{
		}
	
		return bIsValid;
	}
	
	public String getSampleDiagramFilename(int nKind)
	{
		String sDiagramETLDFile = "";
		String sDiagramKind = "";

		 switch (nKind)
		 {
			 case IDiagramKind.DK_ACTIVITY_DIAGRAM : sDiagramKind = ACTIVITY_DIAGRAM; break;
			 case IDiagramKind.DK_CLASS_DIAGRAM : sDiagramKind = CLASS_DIAGRAM; break;
			 case IDiagramKind.DK_COLLABORATION_DIAGRAM : sDiagramKind = COLLABORATION_DIAGRAM; break;
			 case IDiagramKind.DK_COMPONENT_DIAGRAM : sDiagramKind = COMPONENT_DIAGRAM; break;
			 case IDiagramKind.DK_DEPLOYMENT_DIAGRAM : sDiagramKind = DEPLOYMENT_DIAGRAM; break;
			 case IDiagramKind.DK_SEQUENCE_DIAGRAM : sDiagramKind = SEQUENCE_DIAGRAM; break;
			 case IDiagramKind.DK_STATE_DIAGRAM : sDiagramKind = STATE_DIAGRAM; break;
			 case IDiagramKind.DK_USECASE_DIAGRAM : sDiagramKind = USECASE_DIAGRAM; break;
			 case IDiagramKind.DK_ENTITY_DIAGRAM : sDiagramKind = ENTITY_DIAGRAM; break;
		 }

		 if (sDiagramKind.length() > 0)
		 {
			// Here's a sample diagrams table in the DefaultColors.etc file
			/*	
			<SampleDiagrams>
				 <diagram type="class" location="ClassDiagram_1074699624.etld"/>		
				 <diagram type="component" location="ComponentDiagram_1074809903.etld"/>		
				 <diagram type="sequence" location="SequenceDiagram_1074811353.etld"/>
			 </SampleDiagrams>
			*/
			boolean bIsValid = validateFiles();
			if (bIsValid && m_DefaultDoc != null)
			{
				Element pRoot = m_DefaultDoc.getRootElement();
				Node pRootNode = (Node)pRoot;
				if (pRootNode != null)
				{
					String query = "./SampleDiagrams/diagram[@type='" + sDiagramKind + "']";
					Node pNode = pRootNode.selectSingleNode(query);
					if (pNode != null)
					{
						sDiagramETLDFile = XMLManip.getAttributeValue(pNode, "location");
					}
			   }
			}
		}
		
		return sDiagramETLDFile; 
	}
	
	public ETList<String> getStandardDrawEngines(int nKind)
	{
		ETList <String> pStandardDrawEngines = new ETArrayList<String>();

		// Get the default draw engines from the table at the top of the default colors file
		boolean bIsValid = validateFiles();
		if (bIsValid && m_DefaultDoc != null)
		{
			Element pRoot = m_DefaultDoc.getRootElement();
			Node pRootNode = (Node)pRoot;
			if (pRootNode != null)
			{
				String sDiagramKind = "";

				switch (nKind)
				{
					case IDiagramKind.DK_ACTIVITY_DIAGRAM : sDiagramKind = ACTIVITY_DIAGRAM; break;
					case IDiagramKind.DK_CLASS_DIAGRAM : sDiagramKind = CLASS_DIAGRAM; break;
					case IDiagramKind.DK_COLLABORATION_DIAGRAM : sDiagramKind = COLLABORATION_DIAGRAM; break;
					case IDiagramKind.DK_COMPONENT_DIAGRAM : sDiagramKind = COMPONENT_DIAGRAM; break;
					case IDiagramKind.DK_DEPLOYMENT_DIAGRAM : sDiagramKind = DEPLOYMENT_DIAGRAM; break;
					case IDiagramKind.DK_SEQUENCE_DIAGRAM : sDiagramKind = SEQUENCE_DIAGRAM; break;
					case IDiagramKind.DK_STATE_DIAGRAM : sDiagramKind = STATE_DIAGRAM; break;
					case IDiagramKind.DK_USECASE_DIAGRAM : sDiagramKind = USECASE_DIAGRAM; break;
					case IDiagramKind.DK_ENTITY_DIAGRAM : sDiagramKind = ENTITY_DIAGRAM; break;
				}

				 // Typical Entry	<SampleDiagrams>
				  // <SampleDiagrams>
				   //    <diagram type="class" location="ClassDiagram_1074699624.etld">
					//       <standarddrawengine name="ClassDrawEngine"/>
				   //    </diagram>
				  // </SampleDiagrams>
				 // 
				 // Do a select like follows
				 // ./drawengine[@type='sDrawEngineName']/resource[@name='sResourceName') ]

				String query = "./SampleDiagrams/diagram[@type='" + sDiagramKind + "']/standarddrawengine";
				List pNodes = pRootNode.selectNodes(query);
               
				if (pNodes != null)
				{
					int nLen = pNodes.size();
					for(int ii = 0; ii < nLen; ii++)
					{
						Node pNode = (Node)pNodes.get(ii);
						if (pNode != null)
						{
							String sDrawEngineName =  XMLManip.getAttributeValue(pNode, NAME);
							if (sDrawEngineName != null && sDrawEngineName.length() > 0)
							{
								// Don't add the erstudio types if they don't belong
								boolean bAddIt = true;
								if (sDrawEngineName.equals("TitleBlockDrawEngine") ||
									sDrawEngineName.equals("ERViewDrawEngine") ||
									sDrawEngineName.equals("EREntityDrawEngine") ||
									sDrawEngineName.equals("EREntityAssociationEdgeDrawEngine") )
								{
								   bAddIt = false;
								}

								if (bAddIt)
							 	{
									pStandardDrawEngines.add(sDrawEngineName);
								}
							}
						}
					}
				}
			}
		}
		
		return pStandardDrawEngines;
	}
	
	public ETPairT<String, String> getUpgradeString(String sOldName)
	{
		ETPairT<String, String> retVal = new ETPairT<String, String>();

		// Here's a sample upgrade table in the DefaultColors.etc file
		   /*
		   <UpgradeTable>
				<resource type="color" oldname="ActivityEdgeBorderColor" newdrawengine="" newresource="activityedgecolor"/>
				<resource type="color" oldname="ActivityFinalNodeBorderColor" newdrawengine="" newresource=""/>
		   </UpgradeTable>
		   */
		
		   // If we know how to upgrade then newname is set to a string (ie activityedgecolor) if it's
		   // "" then we don't know how to upgrade that resource.
		boolean bIsValid = validateFiles();
		if (bIsValid && m_DefaultDoc != null)
		{
			Element pRoot = m_DefaultDoc.getRootElement();
			Node pRootNode = (Node)pRoot;
			if (pRootNode != null)
			{
				String query = "./UpgradeTable/resource[@oldname='" + sOldName + "']";
				Node pNode = pRootNode.selectSingleNode(query);
				if (pNode != null)
				{
					String sNewDrawEngine = XMLManip.getAttributeValue(pNode, "newdrawengine");
					String sNewResourceName = XMLManip.getAttributeValue(pNode, "newresource");

					if (sNewDrawEngine.length() > 0 && sNewResourceName.length() > 0)
					{
						retVal.setParamOne(sNewDrawEngine);
						retVal.setParamTwo(sNewResourceName);
					}
				}
			}
		}

		return retVal;
	}
	
	public ETFontType getFont()
	{
		ETFontType retVal= null;

		boolean bIsValid = validateFiles();
		if (bIsValid && m_OverriddenDoc != null && m_DefaultDoc != null)
		{
			retVal = getFont(m_OverriddenDoc);
			if (retVal == null)
			{
				retVal = getFont(m_DefaultDoc);
			}
		}
			  		
		return retVal;
	}
	
	public int getColor()
	{
		int nColor = -1;

		// Get the default color resource
		boolean bIsValid = validateFiles();
		if (bIsValid && m_OverriddenDoc != null && m_DefaultDoc != null)
		{
			nColor = getColor(m_OverriddenDoc);
			if (nColor == -1)
			{
				nColor = getColor(m_DefaultDoc);
			}
		}
		
		return nColor;
	}
	
	public ETFontType getFontResource(String sDrawEngineName, String sResourceName)
	{
		ETFontType retVal = null;
		
		boolean bIsValid = validateFiles();
		if (bIsValid && m_DefaultDoc != null && m_OverriddenDoc != null)
		{
			retVal = getFontResource(m_OverriddenDoc, sDrawEngineName, sResourceName);
			if (retVal == null)
			{
				retVal = getFontResource(m_DefaultDoc, sDrawEngineName, sResourceName);
			}
		}
		
		return retVal;
	}
	public int getColorResource(String sDrawEngineName, String sResourceName)
	{
		int nColor = -1;

		boolean bIsValid = validateFiles();
		if (bIsValid && m_DefaultDoc != null && m_OverriddenDoc != null)
		{
			nColor = getColorResource(m_OverriddenDoc, sDrawEngineName, sResourceName);
			if (nColor == -1)
			{
				nColor = getColorResource(m_DefaultDoc, sDrawEngineName, sResourceName);
			}
		}
		
		return nColor;		
	}

	public ETFontType getDefaultFontResource(String sDrawEngineName, String sResourceName)
	{
		ETFontType retVal = null;
		boolean bIsValid = validateFiles();
		if (bIsValid && m_DefaultDoc != null)
		{
			retVal = getFontResource(m_DefaultDoc, sDrawEngineName, sResourceName);
		}
		
		return retVal;
	}
	public int getDefaultColorResource(String sDrawEngineName, String sResourceName)
	{
		int nColor = -1;
		boolean bIsValid = validateFiles();
		if (bIsValid && m_DefaultDoc != null)
		{
			nColor = getColorResource(m_DefaultDoc, sDrawEngineName, sResourceName);
		}
		
		return nColor;
	}

	public ETFontType getOverriddenFontResource(String sDrawEngineName, String sResourceName)
	{
		ETFontType retVal = null;
		boolean bIsValid = validateFiles();
		if (bIsValid && m_OverriddenDoc != null)
		{
			retVal = getFontResource(m_OverriddenDoc, sDrawEngineName, sResourceName);
		}
		
		return retVal;
	}

	public int getOverriddenColorResource(String sDrawEngineName, String sResourceName)
	{
		int nColor = -1;
		boolean bIsValid = validateFiles();
		if (bIsValid && m_OverriddenDoc != null)
		{
			nColor = getColorResource(m_OverriddenDoc, sDrawEngineName, sResourceName);
		}
		
		return nColor;
	}

	public void saveOverriddenResources()
	{
		try
		{
			boolean bIsValid = validateFiles();
			if (bIsValid && m_OverriddenDoc != null && m_OverriddenResourceLoc.length() > 0)
			{
				XMLManip.save(m_OverriddenDoc, m_OverriddenResourceLoc);
			}
		}
		catch (Exception e)
		{
		}
	}

	public void removeOverriddenFontResource(String sDrawEngineName, String sResourceName)
	{
		boolean bIsValid = validateFiles();
		if (bIsValid && m_OverriddenDoc != null)
		{
			Element pRoot = m_OverriddenDoc.getRootElement();
			Node pRootNode = (Node)pRoot;
			if (pRootNode != null)
			{
				// Typical Entry
				//	<drawengine type="ClassDrawEngine">
				  //	   <resource type='color' name="fill" value="16777215"/>	
				  //	   <resource type='font' name="stereotype" facename="Arial" height="12" weight="700" color="16777215"/> 
				 //	</drawengine>
				// 
				// Do a select like follows
				// ./drawengine[@type='sDrawEngineName']/resource[@name='sResourceName') ]

				String query = "./drawengine[@type='" + sDrawEngineName + "']/resource[@name='" + sResourceName + "']";
				Node pNode = pRootNode.selectSingleNode(query);
				if (pNode != null)
				{
					Element parent = pNode.getParent();
					if(parent != null)
					{
						parent.remove(pNode);
					}
				}
			}
		}
	}

	public void removeOverriddenColorResource(String sDrawEngineName, String sResourceName)
	{
		boolean bIsValid = validateFiles();
		if (bIsValid && m_OverriddenDoc != null)
		{
			Element pRoot = m_OverriddenDoc.getRootElement();
			Node pRootNode = (Node)pRoot;
			if (pRootNode != null)
			{
				// Typical Entry
				//	<drawengine type="ClassDrawEngine">
				  //	   <resource type='color' name="fill" value="16777215"/>	
				  //	   <resource type='font' name="stereotype" facename="Arial" height="12" weight="700" color="16777215"/> 
				 //	</drawengine>
				// 
				// Do a select like follows
				// ./drawengine[@type='sDrawEngineName']/resource[@name='sResourceName') ]

				String query = "./drawengine[@type='" + sDrawEngineName + "']/resource[@name='" + sResourceName + "']";
				Node pNode = pRootNode.selectSingleNode(query);
				if (pNode != null)
				{
					Element parent = pNode.getParent();
					if(parent != null)
					{
						parent.remove(pNode);
					}
				}
			}
		}
	}

	public void saveOverriddenFontResource(String sDrawEngineName, String sResourceName, ETFontType pETFontType)
	{
		boolean bIsValid = validateFiles();
		if (bIsValid && m_OverriddenDoc != null)
		{
			Element pRoot = m_OverriddenDoc.getRootElement();
			Node pRootNode = (Node)pRoot;
			if (pRootNode != null)
			{
				// First delete the resource, then we can re-add it.
				removeOverriddenFontResource(sDrawEngineName,sResourceName);

				// Get or create the node
				Node pFoundOrCreatedNode = getOrCreateElement(sDrawEngineName);
				if (pFoundOrCreatedNode != null)
				{
					// Add the font child
					Node pCreatedNode = XMLManip.createElement((Element)pFoundOrCreatedNode,RESOURCE);
					if (pCreatedNode != null)
					{
						XMLManip.setAttributeValue(pCreatedNode, TYPE, FONT);
						XMLManip.setAttributeValue(pCreatedNode, NAME, sResourceName);
						XMLManip.setAttributeValue(pCreatedNode, FACENAME, pETFontType.getName());
						Integer height = new Integer(pETFontType.getHeight() - 4); // It was increased by when read in
						XMLManip.setAttributeValue(pCreatedNode, HEIGHT, height.toString());
						Integer weight = new Integer(pETFontType.getWeight());
						XMLManip.setAttributeValue(pCreatedNode, WEIGHT, weight.toString());
						Boolean italict = new Boolean(pETFontType.getItalic());
						XMLManip.setAttributeValue(pCreatedNode, ITALIC, italict.toString());
						saveCOLORREF(pCreatedNode, pETFontType.getColor());
					}
				}
			}
		}
	}
											
	public void saveOverriddenColorResource(String sDrawEngineName, String sResourceName, int nColor)
	{
		boolean bIsValid = validateFiles();
		if (bIsValid && m_OverriddenDoc != null)
		{
			Element pRoot = m_OverriddenDoc.getRootElement();
			Node pRootNode = (Node)pRoot;
			if (pRootNode != null)
			{
				// First delete the resource, then we can re-add it.
				removeOverriddenColorResource(sDrawEngineName,sResourceName);
            
				// Get or create the node
				Node pFoundOrCreatedNode = getOrCreateElement(sDrawEngineName);
				if (pFoundOrCreatedNode != null)
				{
					// Add the color child
					Node pCreatedNode = XMLManip.createElement((Element)pFoundOrCreatedNode,RESOURCE);
					if (pCreatedNode != null)
					{
						XMLManip.setAttributeValue(pCreatedNode, TYPE, COLOR);
						XMLManip.setAttributeValue(pCreatedNode, NAME, sResourceName);
						saveCOLORREF(pCreatedNode, nColor);
					}
				}
			}
		}
	}

	public ETList<String> getAllDrawEngineNames()
	{
		ETList<String> pDrawEngines = new ETArrayList<String>();

		boolean bIsValid = validateFiles();
		if (bIsValid && m_OverriddenDoc != null && m_DefaultDoc != null)
		{
			ETList<String> pFoundStrings = null;
			pFoundStrings = getAllDrawEngineNames(m_OverriddenDoc);
			pDrawEngines.addAll(pFoundStrings);
			pFoundStrings = getAllDrawEngineNames(m_DefaultDoc);
			pDrawEngines.addAll(pFoundStrings);
		}
		
		return pDrawEngines;
	}

	public ETList<String> getAllResourceNames(String sDrawEngineName)
	{
		ETList<String> pResourceNames = new ETArrayList<String>();
		
		boolean bIsValid = validateFiles();
		if (bIsValid && m_OverriddenDoc != null && m_DefaultDoc != null)
		{
			ETList<String> pFoundStrings = null;
//			pFoundStrings = getAllResourceNames(m_OverriddenDoc, sDrawEngineName);
//			pResourceNames.addAll(pFoundStrings);
			pFoundStrings = getAllResourceNames(m_DefaultDoc, sDrawEngineName);
			pResourceNames.addAll(pFoundStrings);
		}
		
		return pResourceNames;
	}

	public ETList<IDrawingProperty> getAllDrawingProperties(String sDrawEngineName)
	{
		ETList<IDrawingProperty> pProperties = new ETArrayList<IDrawingProperty>();
		
		ETList<String> pAllResourceNames = getAllResourceNames(sDrawEngineName);
		if (pAllResourceNames != null)
		{
			int count = pAllResourceNames.size();
			for (int i = 0 ; i < count ; i++)
			{
				String sThisName = pAllResourceNames.get(i);
				if (sThisName != null && sThisName.length() > 0)
				{
					IDrawingProperty pProperty = getDrawingProperty(sDrawEngineName, sThisName);
					if (pProperty != null)
					{
						pProperties.add(pProperty);
					}
				}
			}
		}
		
		return pProperties;
	}
	
	public String getResourceType(String sDrawEngineName, String sResourceName)
	{
		String sResourceType = "";
	
		// Get the type of this resource in the overridden and then in the default color table
		boolean bIsValid = validateFiles();
		if (bIsValid && m_OverriddenDoc != null && m_DefaultDoc != null)
		{
			sResourceType = getResourceType(m_OverriddenDoc, sDrawEngineName,sResourceName);
			if (sResourceType == "")
			{
				sResourceType = getResourceType(m_DefaultDoc, sDrawEngineName, sResourceName);
			}
		}
	
		return sResourceType;
	}
	
	public boolean isAdvanced(String sDrawEngineName, String sResourceName)
	{
		boolean bIsAdvanced = false;

		// Get the advanced flag from the default document
		boolean bIsValid = validateFiles();
		if (sDrawEngineName != null && sResourceName != null && bIsValid && m_DefaultDoc != null)
		{
			Element pRoot = m_DefaultDoc.getRootElement();
			Node pRootNode = (Node)pRoot;
			if (pRootNode != null)
			{
				// Typical Entry
				//	<drawengine type="ClassDrawEngine">
				  //	   <resource type='color' name="fill" value="16777215" advanced="1"/>	
				  //	   <resource type='font' name="stereotype" facename="Arial" height="12" weight="700" color="16777215"/> 
				 //	</drawengine>
				// 
				// Do a select like follows
				// ./drawengine[@type='sDrawEngineName']/resource[@name='sResourceName') ]

				String query = "./drawengine[@type='" + sDrawEngineName + "']/resource[@name='" + sResourceName + "']";
				Node pNode = pRootNode.selectSingleNode(query);
				if (pNode != null)
				{
					int nAdvanced = XMLManip.getAttributeIntValue(pNode, ADVANCED);
					bIsAdvanced = nAdvanced == 0? false:true;
				}
			}
		}
		return bIsAdvanced;
	}
	
	public IDrawingProperty	getDrawingProperty(String sDrawEngineName, String sResourceName)
	{
		IDrawingProperty pProperty = null;

		// Now we have a combination of the draw engine and resource name
		String sType = getResourceType(sDrawEngineName, sResourceName);
		if (sType.equals("font"))
		{
			pProperty = loadFont(sDrawEngineName, sResourceName);
		}
		else if (sType.equals("color"))
		{
			pProperty = loadColor(sDrawEngineName, sResourceName);
		}
		
		return pProperty;
	}
	
	public ETPairT<String, String> getDisplayName(String sDrawEngineName, String sResourceName)
	{
		ETPairT<String, String> retVal = new ETPairT<String, String>();
		String sDisplayName = "", sDescription = "";

		// Get the information from the default color document (defaultcolors.etc)
		boolean bIsValid = validateFiles();
		if (bIsValid && m_DefaultDoc != null)
		{
			Element pRoot = m_DefaultDoc.getRootElement();
			Node pRootNode = (Node)pRoot;
			if (pRootNode != null)
			{
				// Typical Entry
				//	<drawengine type="ClassDrawEngine">		
				//    <resource type="color" name="classfill" colorvalue="65535" displayName="PSK_CLASSFILLCOLOR"/>
				  //	   <resource type='font' name="stereotype" facename="Arial" height="12" weight="700" color="16777215"/> 
				 //	</drawengine>
				// 
				// Do a select like follows
				// ./drawengine[@type='sDrawEngineName']/resource[@name='sResourceName') ]

				String query = "";
				if (sResourceName != null && sResourceName.length() > 0)
				{
					// Get the resource description
					query =  "./drawengine[@type='" + sDrawEngineName + "']/resource[@name='" + sResourceName +"']";
				}
				else
				{
					// Get the drawengine description
					query = "./drawengine[@type='" + sDrawEngineName + "']";
				}
				
				Node pNode = pRootNode.selectSingleNode(query);
				if (pNode != null)
				{
					String sPSKName = XMLManip.getAttributeValue(pNode, DISPLAYNAME);
					String sPSKDescription = XMLManip.getAttributeValue(pNode, DESCRIPTION);
					String sTranslatedName = "";
					String sTranslatedDescription = "";
					if (sPSKName != null && sPSKName.length() > 0 || sPSKDescription != null && sPSKDescription.length() > 0)
					{
						IConfigStringTranslator pTranslator = new ConfigStringTranslator();
						if (pTranslator != null)
						{
							sTranslatedName = pTranslator.translateWord(sPSKName);
							sTranslatedDescription = pTranslator.translateWord(sPSKDescription);
	
							// if it actually got translated then return it
							if (!(sPSKName.equals(sTranslatedName)))
							{
								sDisplayName = sTranslatedName;
							}
							if (!(sPSKDescription.equals(sTranslatedDescription)))
							{
								sDescription = sTranslatedDescription;
							}
						}
					}
				}
			}
		}

		retVal.setParamOne(sDisplayName);
		retVal.setParamTwo(sDescription);
		return retVal;
	}
	public ETTripleT<String, String , Integer> getDrawEngineDisplayDetails(String sDrawEngineName)
	{
		ETTripleT<String, String , Integer> retVal = new ETTripleT<String, String , Integer>();
		
		String sDisplayName = "";
		String sDescription = "";
		int nPreferredDiagramKind = IDiagramKind.DK_UNKNOWN;

		// Get the information from the default color document (defaultcolors.etc)
		boolean bIsValid = validateFiles();
		if (bIsValid && m_DefaultDoc != null)
		{
			Element pRoot = m_DefaultDoc.getRootElement();
			Node pRootNode = (Node)pRoot;
			if (pRootNode != null)
			{
				// Typical Entry	
				// <drawengine type="ClassDrawEngine" displayName="PSK_CLASSDRAWENGINE" description="PSK_CLASSDRAWENGINE_DESC" diagramtype="class">
				//    <resource type="color" name="classfill" colorvalue="65535" displayName="PSK_CLASSFILLCOLOR"/>
				  //	   <resource type='font' name="stereotype" facename="Arial" height="12" weight="700" color="16777215"/> 
				 //	</drawengine>
				// 
				// Do a select like follows
				// ./drawengine[@type='sDrawEngineName']/resource[@name='sResourceName') ]

				String query = "./drawengine[@type=" + sDrawEngineName + "']";
				Node pNode = pRootNode.selectSingleNode(query);
				if (pNode != null)
				{
					String sPSKName = XMLManip.getAttributeValue(pNode, DISPLAYNAME);
					String sPSKDescription = XMLManip.getAttributeValue(pNode, DESCRIPTION);
					String sPreferredDiagram = XMLManip.getAttributeValue(pNode, DIAGRAMTYPE);
					String sTranslatedName = "";
					String sTranslatedDescription = "";
				   
					if (sPSKName.length() > 0 || sPSKDescription.length() > 0 || sPreferredDiagram.length() > 0)
					{
						IConfigStringTranslator pTranslator = new ConfigStringTranslator();
						if (pTranslator != null)
						{
							sTranslatedName = pTranslator.translateWord(sPSKName);
							sTranslatedDescription = pTranslator.translateWord(sPSKDescription);

							// if it actually got translated then return it
							if (!(sPSKName.equals(sTranslatedName)))
							{
								if (sDisplayName.length() > 0)
								{
									sDisplayName = sTranslatedName;
								}
							}
							if (!(sPSKDescription.equals(sTranslatedDescription)))
							{
								if (sDescription != null)
								{
									sDescription = sTranslatedDescription;
								}
							}
							if (sPreferredDiagram.length() > 0)
							{
								if (sPreferredDiagram.equals(ACTIVITY_DIAGRAM))
								{
									nPreferredDiagramKind = IDiagramKind.DK_ACTIVITY_DIAGRAM;
								}
								else if (sPreferredDiagram.equals(CLASS_DIAGRAM))
								{
									nPreferredDiagramKind = IDiagramKind.DK_CLASS_DIAGRAM;
								}
								else if (sPreferredDiagram.equals(COLLABORATION_DIAGRAM))
								{
									nPreferredDiagramKind = IDiagramKind.DK_COLLABORATION_DIAGRAM;
								}
								else if (sPreferredDiagram.equals(COMPONENT_DIAGRAM))
								{
									nPreferredDiagramKind = IDiagramKind.DK_COMPONENT_DIAGRAM;
								}
								else if (sPreferredDiagram.equals(DEPLOYMENT_DIAGRAM))
								{
									nPreferredDiagramKind = IDiagramKind.DK_DEPLOYMENT_DIAGRAM;
								}
								else if (sPreferredDiagram.equals(SEQUENCE_DIAGRAM))
								{
									nPreferredDiagramKind = IDiagramKind.DK_SEQUENCE_DIAGRAM;
								}
								else if (sPreferredDiagram.equals(STATE_DIAGRAM))
								{
									nPreferredDiagramKind = IDiagramKind.DK_STATE_DIAGRAM;
								}
								else if (sPreferredDiagram.equals(USECASE_DIAGRAM))
								{
									nPreferredDiagramKind = IDiagramKind.DK_USECASE_DIAGRAM;
								}
								else if (sPreferredDiagram.equals(ENTITY_DIAGRAM))
								{
									nPreferredDiagramKind = IDiagramKind.DK_ENTITY_DIAGRAM;
								}
							}
						}
					}
				}
			}
		}
		
		retVal.setParamOne(sDisplayName);
		retVal.setParamTwo(sDescription);
		retVal.setParamThree(new Integer(nPreferredDiagramKind));
		return retVal;
	}
	
	public void saveCOLORREF(Node pElement, int nColorRef)
	{
		Color pColor = new Color(nColorRef);
		int nRed   = pColor.getRed();
		int nGreen = pColor.getGreen();
		int nBlue  = pColor.getBlue();
		String xsTemp = nRed + "," + nGreen + "," + nBlue;
      
		XMLManip.setAttributeValue(pElement, COLORVALUE, xsTemp);
	}

	public int readCOLORREF(Node pElement)
	{
		int  nColor = 0;
		
		String xsTemp = XMLManip.getAttributeValue(pElement, COLORVALUE);
		if (xsTemp!= null && xsTemp.length() > 0)
		{         
			ETList<String > tokens = StringUtilities.splitOnDelimiter(xsTemp, ",");
			int nRed = Integer.parseInt(tokens.get(0));
			int nGreen = Integer.parseInt(tokens.get(1));
			int nBlue = Integer.parseInt(tokens.get(2));

			nColor = (new Color(nRed,nGreen,nBlue)).getRGB();
		}
		return nColor;
	}
}



