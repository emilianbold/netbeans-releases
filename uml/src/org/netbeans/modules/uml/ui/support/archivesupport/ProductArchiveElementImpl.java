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
