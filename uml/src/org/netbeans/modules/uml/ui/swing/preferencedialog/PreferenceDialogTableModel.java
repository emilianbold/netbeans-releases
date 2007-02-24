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


