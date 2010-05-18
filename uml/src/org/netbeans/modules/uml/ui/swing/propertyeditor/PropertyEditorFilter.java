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




