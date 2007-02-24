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



package org.netbeans.modules.uml.ui.support.archivesupport;

import java.lang.reflect.Array;
import java.util.*;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 *
 * @author Trey Spiva
 */
public class ProductArchiveElementImpl implements IProductArchiveElement
{
	private Element m_Element = null;

	public ProductArchiveElementImpl()
	{
	}

	public ProductArchiveElementImpl(Element element)
	{
		setDOMElement(element);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement#getID()
	 */
	public String getID()
	{
		return getDOMElement() != null ? getDOMElement().getName() : "";
	}

	/**
	 * Creates a sub element named sID of this element
	 *
	 * @param sID The id (ie name) of the sub element
	 * @param pElement The created element
	 */
	public IProductArchiveElement createElement(String sID)
	{
		if (m_Element != null)
		{
			Element addedEle = XMLManip.createElement(m_Element, sID);
			if (addedEle != null)
			{
				IProductArchiveElement retEle = new ProductArchiveElementImpl();
				retEle.setDOMElement(addedEle);
				return retEle;
			}
		}
		return null;
	}

	/**
	 * Removes an attribute to this element
	 *
	 * @param sID The name of the attribute
	 */
	public void removeAttribute(String sID)
	{
		if (m_Element != null)
		{
			Attribute attr = m_Element.attribute(sID);
			if (attr != null)
			{
				m_Element.remove(attr);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement#addAttribute(java.lang.String, java.lang.Object)
	 */
	public IProductArchiveAttribute addAttribute(String sName, Object pVal)
	{
		return addAttributeString(sName, pVal.toString());
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement#addAttributeLong(java.lang.String, int)
	 */
	public IProductArchiveAttribute addAttributeLong(String sName, long nVal)
	{
		return addAttributeString(sName, Long.toString(nVal));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement#addAttributeBool(java.lang.String, boolean)
	 */
	public IProductArchiveAttribute addAttributeBool(String sName, boolean bVal)
	{
		return addAttributeString(sName, Boolean.toString(bVal));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement#addAttributeDouble(java.lang.String, double)
	 */
	public IProductArchiveAttribute addAttributeDouble(String sName, double fVal)
	{
		return addAttributeString(sName, Double.toString(fVal));
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement#addAttributeString(java.lang.String, java.lang.String)
	 */
	public IProductArchiveAttribute addAttributeString(String sName, String sVal)
	{
		if (m_Element != null)
		{
			m_Element.addAttribute(sName, sVal);
			return getAttribute(sName);
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement#getElements()
	 */
	public IProductArchiveElement[] getElements()
	{
		IProductArchiveElement[] childElements = null;
		Element xmlElement = getDOMElement();
		
		int nodeCount = xmlElement.nodeCount();
		
		if (nodeCount > 0)
		{
			childElements = (IProductArchiveElement[]) Array.newInstance(ProductArchiveElementImpl.class, nodeCount);
			int indx = 0;
			for (Iterator x = xmlElement.elementIterator(); x.hasNext();){
				Element childElement = (Element) x.next();
				if (childElement != null) {
					Array.set(childElements, indx, new ProductArchiveElementImpl(childElement));
					indx++;					
				}
			}
			
		}
		return childElements;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement#getAttributes()
	 */
	public IProductArchiveAttribute[] getAttributes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Gets an attribute by name
	 *
	 * @param sName The name of the attribute
	 * @param pVal The returned attribute
	 */
	public IProductArchiveAttribute getAttribute(String sName)
	{
		if (m_Element != null)
		{
			String query = "@" + sName;
			Node pAttr = XMLManip.selectSingleNode(m_Element, query);
			if (pAttr != null)
			{
				IProductArchiveAttribute retAttr = new ProductArchiveAttribute();
				retAttr.setDOMNode(pAttr);
				return retAttr;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement#getElement(java.lang.String)
	 */
	public IProductArchiveElement getElement(String sID)
	{
		Element xmlElement = getDOMElement();
		return xmlElement != null ? ProductArchiveImpl.getElement(xmlElement, sID) : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement#getDOMElement()
	 */
	public Element getDOMElement()
	{
		return m_Element;
	}

	public void setDOMElement(Element e)
	{
		m_Element = e;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement#getAttributeLong(java.lang.String)
	 */
	public long getAttributeLong(String sName)
	{
		Element xmlElement = getDOMElement();
		if (xmlElement != null)
		{
			String value = xmlElement.attributeValue(sName);
			if (value != null)
			{
				return Long.parseLong(value);
			}
		}

		return 0L;
	}

	/**
	 * Returns the value of the attribute as a string.
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement#getAttributeString(java.lang.String)
	 */
	public String getAttributeString(String sName)
	{
		Element xmlElement = getDOMElement();
		return xmlElement != null ? XMLManip.getAttributeValue( xmlElement, sName ) : "";
	}

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement#getAttributeBool(java.lang.String)
    */
   public boolean getAttributeBool(String sName)
   {
      return getAttributeBool( sName, false );
   }
   
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement#getAttributeBool(java.lang.String, boolean)
	 */
	public boolean getAttributeBool( String sName, boolean defaultValue )
	{
		Element xmlElement = getDOMElement();
		return xmlElement != null ? XMLManip.getAttributeBooleanValue( xmlElement, sName, defaultValue ) : defaultValue;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement#getAttributeDouble(java.lang.String)
	 */
	public double getAttributeDouble(String sName)
	{
		Element xmlElement = getDOMElement();
		return xmlElement != null ? XMLManip.getAttributeDoubleValue( xmlElement, sName ) : 0.0d;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement#isDeleted()
	 */
	public boolean isDeleted()
	{		
		return getAttribute(IProductArchiveDefinitions.TABLE_ENTRY_DELETED) != null;
	}

}
