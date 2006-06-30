/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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