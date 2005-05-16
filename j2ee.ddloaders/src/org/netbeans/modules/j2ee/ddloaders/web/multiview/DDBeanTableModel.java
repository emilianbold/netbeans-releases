/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import java.util.List;
// Swing
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;

public abstract class DDBeanTableModel extends AbstractTableModel
{
	private List children;
        private CommonDDBean parent;
        
        protected abstract String[] getColumnNames();
        
        protected CommonDDBean getParent() {
            return parent;
        }
        
        protected List getChildren() {
            return children;
        }

	public int getColumnCount()
	{
		return getColumnNames().length;
	}


	public int getRowCount()
	{
		if (children != null)
		{
			return (children.size());
		}
		else
		{
			return (0);
		}
	}


        public String getColumnName(int column)
        {
		return getColumnNames()[column];
	}
        
	public boolean isCellEditable(int row, int column)
	{
		return (false);
	}

	public int getRowWithValue(int column, Object value)
	{
		for(int row = 0; row < getRowCount(); row++)
		{
			Object obj = getValueAt(row, column);
			if (obj.equals(value))
			{
				return (row);
			}
		}

		return (-1);
	}
        
	public abstract CommonDDBean addRow(Object[] values);

	public abstract void editRow(int row, Object[] values);
        
	public abstract void removeRow(int row);
        
	public void setData(CommonDDBean parent,CommonDDBean[] children) {
		this.parent = parent;
                this.children = new java.util.ArrayList();
                if (children==null) return;
                for(int i=0;i<children.length;i++)
                this.children.add(children[i]);
		fireTableDataChanged();
        }
        
}