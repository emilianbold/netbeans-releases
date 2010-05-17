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



package org.netbeans.modules.uml.ui.swing.preferencedialog;

import java.awt.Color;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.configstringframework.ConfigStringHelper;
import org.netbeans.modules.uml.core.configstringframework.IConfigStringTranslator;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;

/**
 * @author sumitabhk
 *
 */
public class PreferenceDialogTableModel extends AbstractTableModel implements ISwingPreferenceTableModel
{
	private PreferenceDialogUI m_PreferenceControl = null;
	private ETList< ETPairT<IPropertyElement, String> > m_Collection = null;
	/**
	 * 
	 */
	public PreferenceDialogTableModel()
	{
		super();
	}

	public PreferenceDialogTableModel(PreferenceDialogUI control)
	{
		super();
		m_PreferenceControl = control;
	}

	public PreferenceDialogTableModel(PreferenceDialogUI control, ETList<ETPairT<IPropertyElement, String>> values)
	{
		super();
		m_PreferenceControl = control;
		m_Collection = values;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount()
	{
		int retVal = 0;
		if (m_Collection != null)
		{
			retVal = m_Collection.size();
		}
		return retVal;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount()
	{
		return 2;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int arg0)
	{
		return " ";
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int arg0)
	{
		return String.class;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int arg0, int arg1)
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col)
	{
		Object retObj = null;
		if (m_Collection != null)
		{
			int count = m_Collection.size();
			if (row <= count)
			{
				for (int i=0; i<count; i++)
				{
					Object objKey = m_Collection.get(i).getParamOne();
					Object objVal = m_Collection.get(i).getParamTwo();
					
					if (i == row)
					{
						if (col == 0)
						{
							retObj = objKey;
						}
						else if (col == 1)
						{
							if (objKey instanceof IPropertyElement)
							{
								IConfigStringTranslator translator = ConfigStringHelper.instance().getTranslator();
								IPropertyElement pEle = (IPropertyElement)objKey;
								IPropertyDefinition pDef = pEle.getPropertyDefinition();
								String defName = pDef.getName();
								int pos = defName.indexOf("Font");
								if (pos >= 0)
								{
									IPropertyElement nameEle = pEle.getSubElement("FaceName", null);
									pEle = nameEle;
								}
								int pos2 = defName.indexOf("Color");
								if (pos2 >= 0)
								{
									retObj = pEle.getValue();
								}
								else
								{
									String value = pEle.getValue();
									retObj = translator.translate(pDef, value);
								}
							}
						}
						break;
					}
				}
			}
		}
		return retObj;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object arg0, int arg1, int arg2)
	{
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void addTableModelListener(TableModelListener arg0)
	{
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
	 */
	public void removeTableModelListener(TableModelListener arg0)
	{
	}
}


