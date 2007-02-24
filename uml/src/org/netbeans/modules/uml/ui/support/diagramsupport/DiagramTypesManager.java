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
 * Created on Jun 11, 2003
 *
 *
 */
package org.netbeans.modules.uml.ui.support.diagramsupport;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author sumitabhk
 *
 * 
 */
public class DiagramTypesManager implements IDiagramTypesManager
{
	private static DiagramTypesManager m_Instance = null;
	private Document m_Doc = null;

	/**
	 * 
	 */
	private DiagramTypesManager()
	{
		super();
	}
	
	public static DiagramTypesManager instance()
	{
		if (m_Instance == null)
		{
			m_Instance = new DiagramTypesManager();
		}
		return m_Instance;
	}

	/**
	 * The display name for this diagram type (ie Class Diagram)
	 *
	 * @param sDiagramTypeName [in] The diagram type name ie "Class Diagram"
	 * @param pDiagramKind [out,retval] The DiagramKind enumeration that matches pDiagramTypeName
	 */
	public int getDiagramKind(String sDiagramTypeName)
	{
		int diaKind = IDiagramKind.DK_DIAGRAM;

		// Verify that we've setup the xml file correctly
		String query = "//DiagramTypes/DiagramType[@DiagramTypeName='";
		query += sDiagramTypeName;
		query += "']";
		
		String attributeToFind = "DiagramKindEnumValue";
		
		diaKind = getAttributeValue(query, attributeToFind);

		return diaKind;
	}

	/**
	 * Gets an attribute based on a query
	 *
	 * @param query [in] The query to perform
	 * @param attributeValue [in] The attribute for which we need the value of
	 * @param nValue [out] The value of attributeValue
	 *
	 * @return true if we successfully got the attribute value
	 */
	private int getAttributeValue(String query, String attributeToFind)
	{
		int retVal = 0;
		if (verifyFile() == true)
		{
         if(m_Doc != null)
         {
   			Node n = m_Doc.selectSingleNode(query);
   			if (n != null)
   			{
   				String value = XMLManip.getAttributeValue(n, attributeToFind);
               String test = value;
               //String value = ((Element)n).attributeValue(attributeToFind);
   				if (test != null)
   				{
   					retVal = Integer.parseInt(test);
   				}
   			}
         }
		}
		return retVal;
	}

	/**
	 * Opens and validates that the xml file is ok
	 *
	 * @return true if the file exists and we successfully loaded.
	 */
	private boolean verifyFile()
	{
		boolean valid = false;
		if (m_Doc == null)
		{
			String location = retrieveDefaultLocation();
			if (location.length() > 0)
			{
				m_Doc = XMLManip.getDOMDocument(location);
				if (m_Doc != null)
				{
					valid = true;
				}
			}
		}
		else
		{
			valid = true;
		}
		return valid;
	}

	/**
	 * Returns the location of the DiagramTypesManager file
	 *
	 * @return The absolute location of the DiagramTypesManager.etc file.
	 */
	private String retrieveDefaultLocation()
	{
		String location = null;
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		IConfigManager conMan = prod.getConfigManager();
		if (conMan != null)
		{
			location = conMan.getDefaultConfigLocation();
			location += "DiagramTypesManager.etc";
		}
		return location;
	}

	/**
	 * Returns the diagram kind (ie Class Diagram) for the argument diagram
	 *
	 * @param pDiagram [in] The diagram for which to get the type
	 * @param pVal [out,retval] The type of the argument diagram (ie Class Diagram)
	 */
	public String getDiagramTypeName(IDiagram pDiagram)
	{
		String retVal = "";
		if (pDiagram != null)
		{
			retVal = pDiagram.getDiagramKind2();
		}
		return retVal;
	}

	/**
	 * Returns the diagram kind (ie Class Diagram) for the argument enumeration
	 *
	 * @param pDiagram [in] The diagram enumeration which we need to convert to a string (ie
	 * Class Diagram to DK_CLASS_DIAGRAM.
	 * @param pVal [out,retval] The returned type of the diagram enumeration as a string.
	 */
	public String getDiagramTypeName(int nDiagramKind)
	{
		String retVal = "";
		String query = "//DiagramTypes/DiagramType[@DiagramKindEnumValue='";
		query += nDiagramKind;
		query += "']";
		
		String attributeToFind = "DiagramTypeName";
		
		retVal = getStringAttributeValue(query, attributeToFind);
		
		return retVal;
	}

	/**
	 * Gets an attribute based on a query
	 *
	 * @param query [in] The query to perform
	 * @param attributeValue [in] The attribute for which we need the value of
	 * @param sValue [out] The value of attributeValue
	 *
	 * @return true if we successfully got the attribute value
	 */
	private String getStringAttributeValue(String query, String attributeToFind)
	{
		String retVal = null;
		if( verifyFile() == true)
		{
         if(m_Doc != null)
         {
   			Node n = m_Doc.selectSingleNode(query);
   			if (n != null)
   			{
   				retVal = XMLManip.getAttributeValue(n, attributeToFind);
   			}
         }
		}
		return retVal;
	}

	/**
	 * Returns the diagram kind without spaces (ie ClassDiagram) for the argument diagram
	 *
	 * @param pDiagram [in] The diagram for which to get the type sans spaces
	 * @param pVal [out,retval] The type of the diagram without spaces (ie ClassDiagram)
	 */
	public String getDiagramTypeNameNoSpaces(IDiagram pDiagram)
	{
		String diaTypeName = "";
		if (pDiagram != null)
		{
			String str = pDiagram.getDiagramKind2();
			diaTypeName = getDiagramTypeNameNoSpaces(str);
		}
		return diaTypeName;
	}

	/**
	 * Returns the diagram kind without spaces (ie ClassDiagram) for the argument diagram kind (ie Class Diagram)
	 *
	 * @param sDiagramTypeName [in] The diagram type name, with spaces (ie Class Diagram)
	 * @param pVal [out,retval] The type of the diagram without spaces (ie ClassDiagram)
	 */
	public String getDiagramTypeNameNoSpaces(String sDiagramTypeName)
	{
		String retVal = "";

		String query = "//DiagramTypes/DiagramType[@DiagramTypeName='";
		query += sDiagramTypeName;
		query += "']";
		
		String attributeToFind = "DiagramTypeNameNoSpaces";
		
		retVal = getStringAttributeValue(query, attributeToFind);
		
		return retVal;
	}

	/**
	 * Returns the diagram kind without spaces (ie Class Diagram) for the argument enumeration
	 *
	 * @param pDiagram [in] The diagram enumeration which we need to convert to a type string without spaces (ie
	 * Class Diagram to ClassDiagram.
	 * @param pVal [out,retval] The type of the diagram without spaces (ie ClassDiagram)
	 */
	public String getDiagramTypeNameNoSpaces(int nDiagramKind)
	{
		String retVal = "";

		String query = "//DiagramTypes/DiagramType[@DiagramKindEnumValue='";
		query += nDiagramKind;
		query += "']";
		
		String attributeToFind = "DiagramTypeNameNoSpaces";
		
		retVal = getStringAttributeValue(query, attributeToFind);
		
		return retVal;
	}

	/**
	 * Returns short diagram type for this long diagram type (ie Class Diagram to CLD)
	 *
	 * @param sDiagramTypeName [in] The diagram type name, with spaces (ie Class Diagram)
	 * @param pVal [out,retval] The short kind of this diagram (ie CLD)
	 */
	public String getShortDiagramTypeName(String sDiagramTypeName)
	{
		String retVal = "";

		String query = "//DiagramTypes/DiagramType[@DiagramTypeName='";
		query += sDiagramTypeName;
		query += "']";
		
		String attributeToFind = "ShortDiagramTypeName";
		
		retVal = getStringAttributeValue(query, attributeToFind);
		
		return retVal;
	}

	/**
	 * The UML Type for this diagram (ie StructuralDiagram)
	 *
	 * @param pDiagram [in] The diagram for which to get the type sans spaces
	 * @param pVal [out,retval] The uml type of the diagram (ie StructuralDiagram)
	 */
	public String getUMLType(IDiagram pDiagram)
	{
		String diaTypeName = getDiagramTypeName(pDiagram);
		String umlVal = getUMLType(diaTypeName);
		return umlVal;
	}

	/**
	 * The UML Type for this diagram (ie StructuralDiagram)
	 *
	 * @param sDiagramTypeName [in] The diagram type name, with spaces (ie Class Diagram)
	 * @param pVal [out,retval] The uml type of the diagram (ie StructuralDiagram)
	 */
	public String getUMLType(String sDiagramTypeName)
	{
		String retVal = "";

		String query = "//DiagramTypes/DiagramType[@DiagramTypeName='";
		query += sDiagramTypeName;
		query += "']";
		
		String attributeToFind = "UMLTypeName";
		
		retVal = getStringAttributeValue(query, attributeToFind);
		
		return retVal;
	}

	/**
	 * The UML Type for this diagram (ie StructuralDiagram)
	 *
	 * @param pDiagram [in] The diagram enumeration which we need to convert to a short type string (ie
	 * Class Diagram to CLD.
	 * @param pVal [out,retval] The uml type of the diagram (ie StructuralDiagram)
	 */
	public String getUMLType(int nDiagramKind)
	{
		String diaTypeName = getDiagramTypeName(nDiagramKind);
		String umlType = getUMLType(diaTypeName);
		return umlType;
	}

	/**
	 * The diagram engine controlling this behavior (ClassDiagram)
	 *
	 * @param pDiagram [in] The diagram for which to get the type sans spaces
	 * @param pVal [out,retval] The metatype of the engine that controls this diagrams behavior
	 */
	public String getDiagramEngine(IDiagram pDiagram)
	{
		String diaTypeName = getDiagramTypeName(pDiagram);
		String engine = getDiagramEngine(diaTypeName);
		return engine;
	}

	/**
	 * The diagram engine controlling this behavior (ClassDiagram)
	 *
	 * @param sDiagramTypeName [in] The diagram type name, with spaces (ie Class Diagram)
	 * @param pVal [out,retval] The metatype of the engine that controls this diagrams behavior
	 */
	public String getDiagramEngine(String sDiagramTypeName)
	{
		String retVal = "";

		String query = "//DiagramTypes/DiagramType[@DiagramTypeName='";
		query += sDiagramTypeName;
		query += "']";
		
		String attributeToFind = "DiagramEngine";
		
		retVal = getStringAttributeValue(query, attributeToFind);
		
		return retVal;
	}

	/**
	 * The diagram engine controlling this behavior (ClassDiagram)
	 *
	 * @param nDiagramKind [in] The diagram enumeration which we need to convert to diagram engine type
	 * @param pVal [out,retval] The metatype of the engine that controls this diagrams behavior
	 */
	public String getDiagramEngine(int nDiagramKind)
	{
		String diaTypeName = getDiagramTypeName(nDiagramKind);
		String engine = getDiagramEngine(diaTypeName);
		return engine;
	}

	/**
	 * The open diagram icon
	 *
	 * @param pDiagram [in] The diagram for which to get the icon name in CommonResources
	 * @param pVal [out,retval] The name of the icon in the CommonResources
	 */
	public String getOpenIcon(IDiagram pDiagram)
	{
		String diaTypeName = getDiagramTypeName(pDiagram);
		String icon = getOpenIcon(diaTypeName);
		return icon;
	}

	/**
	 * The open diagram icon
	 *
	 * @param sDiagramTypeName [in] The diagram type name, with spaces (ie Class Diagram)
	 * @param pVal [out,retval] The name of the icon in the CommonResources
	 */
	public String getOpenIcon(String sDiagramTypeName)
	{
		String retVal = "";

		String query = "//DiagramTypes/DiagramType[@DiagramTypeName='";
		query += sDiagramTypeName;
		query += "']";
		
		String attributeToFind = "OpenIcon";
		
		retVal = getStringAttributeValue(query, attributeToFind);
		
		return retVal;
	}

	/**
	 * The open diagram icon
	 *
	 * @param nDiagramKind [in] The diagram enumeration which we need to convert to diagram engine type
	 * @param pVal [out,retval] The name of the icon in the CommonResources
	 */
	public String getOpenIcon(int nDiagramKind)
	{
		String diaTypeName = getDiagramTypeName(nDiagramKind);
		String icon = getOpenIcon(diaTypeName);
		return icon;
	}

	/**
	 * The closed diagram icon
	 *
	 * @param pDiagram [in] The diagram for which to get the icon name in CommonResources
	 * @param pVal [out,retval] The name of the icon in the CommonResources
	 */
	public String getClosedIcon(IDiagram pDiagram)
	{
		String diaTypeName = getDiagramTypeName(pDiagram);
		String icon = getClosedIcon(diaTypeName);
		return icon;
	}

	/**
	 * The closed diagram icon
	 *
	 * @param sDiagramTypeName [in] The diagram type name, with spaces (ie Class Diagram)
	 * @param pVal [out,retval] The name of the icon in the CommonResources
	 */
	public String getClosedIcon(String sDiagramTypeName)
	{
		String retVal = "";

		String query = "//DiagramTypes/DiagramType[@DiagramTypeName='";
		query += sDiagramTypeName;
		query += "']";
		
		String attributeToFind = "ClosedIcon";
		
		retVal = getStringAttributeValue(query, attributeToFind);
		
		return retVal;
	}

	/**
	 * The closed diagram icon
	 *
	 * @param nDiagramKind [in] The diagram enumeration which we need to convert to diagram engine type
	 * @param pVal [out,retval] The name of the icon in the CommonResources
	 */
	public String getClosedIcon(int nDiagramKind)
	{
		String diaTypeName = getDiagramTypeName(nDiagramKind);
		String icon = getClosedIcon(diaTypeName);
		return icon;
	}

	/**
	 * The broken diagram icon
	 *
	 * @param pDiagram [in] The diagram for which to get the icon name in CommonResources
	 * @param pVal [out,retval] The name of the icon in the CommonResources
	 */
	public String getBrokenIcon(IDiagram pDiagram)
	{
		String diaTypeName = getDiagramTypeName(pDiagram);
		String icon = getBrokenIcon(diaTypeName);
		return icon;
	}

	/**
	 * The broken diagram icon
	 *
	 * @param sDiagramTypeName [in] The diagram type name, with spaces (ie Class Diagram)
	 * @param pVal [out,retval] The name of the icon in the CommonResources
	 */
	public String getBrokenIcon(String sDiagramTypeName)
	{
		String retVal = "";

		String query = "//DiagramTypes/DiagramType[@DiagramTypeName='";
		query += sDiagramTypeName;
		query += "']";
		
		String attributeToFind = "BrokenIcon";
		
		retVal = getStringAttributeValue(query, attributeToFind);
		
		return retVal;
	}

	/**
	 * The broken diagram icon
	 *
	 * @param nDiagramKind [in] The diagram enumeration which we need to convert to diagram engine type
	 * @param pVal [out,retval] The name of the icon in the CommonResources
	 */
	public String getBrokenIcon(int nDiagramKind)
	{
		String diaTypeName = getDiagramTypeName(nDiagramKind);
		String icon = getBrokenIcon(diaTypeName);
		return icon;
	}

}



