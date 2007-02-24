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
 * Created on Jun 4, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.propertyeditor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JDialog;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.configstringframework.ConfigStringHelper;
import org.netbeans.modules.uml.core.configstringframework.IConfigStringTranslator;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.ui.support.ProductHelper;

/**
 * @author sumitabhk
 *
 */
public class PropertyEditorFilter
{
	private String m_DefinitionFile = null;
	private Document m_Doc = null;
	private TreeMap<String, String> m_CommonMap = new TreeMap<String, String>();
	private TreeMap<String, String> m_OtherMap = new TreeMap<String, String>();
	private String m_CurrentSel = "";

	/**
	 * 
	 */
	public PropertyEditorFilter()
	{
		super();
	}

	/**
	 * 
	 */
	public void build()
	{
		ICoreProduct prod = CoreProductManager.instance().getCoreProduct();
		if (prod != null)
		{
			IConfigManager conMan = prod.getConfigManager();
			if (conMan != null)
			{
				String home = conMan.getDefaultConfigLocation();
				m_DefinitionFile = home;
				m_DefinitionFile += "PropertyEditorFilter.etc";
			}
			
			IConfigStringTranslator trans = ConfigStringHelper.instance().getTranslator();
	
			// load the xml file that contains the definitions
			m_Doc = XMLManip.getDOMDocument(m_DefinitionFile);
			if (m_Doc != null)
			{
				//load the common values first
				String pattern = "PropertyEditorFilter/Common/Item";
				List list = m_Doc.selectNodes(pattern);
				if (list != null && list.size() > 0)
				{
					int count = list.size();
					for (int i=0; i<count; i++)
					{
						Node node = (Node)list.get(i);
						if (node instanceof Element)
						{
							org.dom4j.Element elem = (org.dom4j.Element)node;
							String name = elem.attributeValue("name");
							String displayName = elem.attributeValue("displayName");
							if (trans != null)
							{
								String transVal = trans.translate(null, displayName);
								m_CommonMap.put(name, transVal);
							}
						}
					}
				}
				
				//now load other values
				pattern = "PropertyEditorFilter/Other/Item";
				List list2 = m_Doc.selectNodes(pattern);
				if (list2 != null && list2.size() > 0)
				{
					int count = list2.size();
					for (int i=0; i<count; i++)
					{
						Node node = (Node)list2.get(i);
						if (node instanceof Element)
						{
							org.dom4j.Element elem = (org.dom4j.Element)node;
							String name = elem.attributeValue("name");
							String displayName = elem.attributeValue("displayName");
							if (trans != null)
							{
								String transVal = trans.translate(null, displayName);
								m_OtherMap.put(name, transVal);
							}
						}
					}
				}
			}
		}
	}

	public void showFilterDialog()
	{
		PropertyEditorFilterDialog dialog = new PropertyEditorFilterDialog();
		dialog.loadFilterDialog(this);
		dialog.show();
	}

	public Iterator getCommonMapIter()
	{
		Collection col = m_CommonMap.values();
		if (col != null)
		{
			return col.iterator();
		}
		return null;
	}

	public Iterator getOtherMapIter()
	{
		Collection col = m_OtherMap.values();
		if (col != null)
		{
			return col.iterator();
		}
		return null;
	}
	
	public void setCurrentSelection(String str)
	{
		m_CurrentSel = str;
	}
	
	public String getCurrentSelection()
	{
		return m_CurrentSel;
	}
}




