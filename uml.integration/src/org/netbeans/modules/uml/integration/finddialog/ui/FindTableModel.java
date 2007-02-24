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

package org.netbeans.modules.uml.integration.finddialog.ui;
import java.util.Hashtable;
import javax.swing.Icon;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.integration.finddialog.FindUtilities;
import org.netbeans.modules.uml.ui.swing.preferencedialog.ISwingPreferenceTableModel;



/**
 * @author sumitabhk
 *
 */
public class FindTableModel extends AbstractTableModel implements ISwingPreferenceTableModel
{
	private FindDialogUI m_FindControl = null;
	private ReplaceDialogUI m_ReplaceControl = null;
	private ETList< Object > m_collection = null;
	
	private Hashtable <Integer, String> m_ColNameMap = new Hashtable <Integer, String>();

	public FindTableModel()
	{
		super();
	}

	public FindTableModel(FindDialogUI control)
	{
		super();
		m_FindControl = control;
		buildColumnMap();
	}
	public FindTableModel(FindDialogUI control, ETList<Object> values)
	{
		super();
		m_FindControl = control;
		m_collection = values;
		buildColumnMap();
	}
	public FindTableModel(ReplaceDialogUI control)
	{
		super();
		m_ReplaceControl = control;
		buildColumnMap();
	}
	public FindTableModel(ReplaceDialogUI control, ETList<Object> values)
	{
		super();
		m_ReplaceControl = control;
		m_collection = values;
		buildColumnMap();
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount()
	{
		int retVal = 0;
		if (m_collection != null)
		{
			retVal = retVal + m_collection.size();
		}
		return retVal;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount()
	{
		int count = 0;
		ETList<String> strs = FindUtilities.buildColumns();
		if (strs != null)
		{
			count = strs.size();
		}
		return count;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int arg0)
	{
		String dispColName = "";
		Integer i = new Integer(arg0);
		String colName = m_ColNameMap.get(i);
		if (colName != null && colName.length() > 1){
			dispColName = FindUtilities.translateString(colName);
		}
        return dispColName;              
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class getColumnClass(int arg0)
	{
        if ("IDS_ICON".equals(m_ColNameMap.get(arg0)))
            return Icon.class;
        else 
            return String.class;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int arg0, int arg1)
	{
		return false;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col)
	{
		Object retObj = null;
		if (m_collection != null)
		{
			int count = m_collection.size();
			if (row <= count)
			{
				for (int i=0; i<count; i++)
				{
					Object objVal = m_collection.get(i);
					IElement pEle = null;
					INamedElement namedEle = null;
					IProxyDiagram pDiag = null;
					if (objVal instanceof IElement)
					{
						pEle = (IElement)objVal; 
					}
					if (objVal instanceof INamedElement)
					{
						namedEle = (INamedElement)objVal; 
					}
					if (objVal instanceof IProxyDiagram)
					{
						pDiag = (IProxyDiagram)objVal;
					}
					if (i == row)
					{
						Integer theInt = new Integer(col);
						String colName = m_ColNameMap.get(theInt);
						//if (colName == null || colName.length() == 1)
                        if (colName.equals("IDS_ICON"))
						{
							// not displaying the Icon title
							CommonResourceManager mgr = CommonResourceManager.instance();
							retObj = mgr.getIconForDisp(objVal);
						}
						else if (colName.equals("IDS_NAME"))
						{
							if (namedEle != null){
							retObj = namedEle.getName();
							}
							else if (pDiag != null){
								retObj = pDiag.getName();
							}
						}
						else if (colName.equals("IDS_ALIAS"))
						{
							if (namedEle != null){
							retObj = namedEle.getAlias();
							}
							else if (pDiag != null){
								retObj = pDiag.getAlias();
							}
						}
						else if (colName.equals("IDS_TYPE"))
						{
							if (pEle != null){
								retObj = pEle.getExpandedElementType();
							}
							else if (pDiag != null){
								retObj = pDiag.getDiagramKindName();
							}
						}
						else if (colName.equals("IDS_FULLNAME"))
						{
							if (namedEle != null){
							retObj = namedEle.getQualifiedName2();
							}
							else if (pDiag != null){
								retObj = pDiag.getQualifiedName();
							}
						}
						else if (colName.equals("IDS_PROJECT"))
						{
							if (pEle != null){
								IProject pProj = pEle.getProject();
								if (pProj != null){
									retObj = pProj.getName();
								}
							}
							else if (pDiag != null){
								IProject pProj = pDiag.getProject();
								if (pProj != null){
									retObj = pProj.getName();
								}
							}
						}
						else if (colName.equals("IDS_ID"))
						{
							if (pEle != null){
								retObj = pEle.getXMIID();
							}
							else if (pDiag != null){
								retObj = pDiag.getXMIID();
							}
						}
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
	public IElement getElementAtRow(int row)
	{
		IElement retObj = null;
		if (m_collection != null)
		{
			int count = m_collection.size();
			if (row <= count)
			{
				for (int i=0; i<count; i++)
				{
					if (i == row)
					{
						Object obj = m_collection.get(i);
						if (obj instanceof IElement)
						{
							retObj = (IElement)obj;
						}
						break;
					}
				}
			}
		}
		return retObj;
	}
	public IProxyDiagram getDiagramAtRow(int row)
	{
		IProxyDiagram retObj = null;
		if (m_collection != null)
		{
			int count = m_collection.size();
			if (row <= count)
			{
				for (int i=0; i<count; i++)
				{
					if (i == row)
					{
						Object obj = m_collection.get(i);
						if (obj instanceof IProxyDiagram)
						{
							retObj = (IProxyDiagram)obj;
						}
						break;
					}
				}
			}
		}
		return retObj;
	}
	private void buildColumnMap()
	{	
		ETList<String> strs = FindUtilities.buildColumns();
		if (strs != null)
		{
			int count = strs.size();
			for (int x = 0; x < count; x++)
			{
				String colName = strs.get(x);
				if (colName != null && colName.length() > 1){
					Integer i = new Integer(x);
					m_ColNameMap.put(i, colName);
				}
			}
		}
	}
}
