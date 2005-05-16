/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

// Netbeans
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.openide.util.NbBundle;

public class InitParamTableModel extends DDBeanTableModel
{
	private static final String[] columnNames = {
            NbBundle.getMessage(InitParamTableModel.class,"TTL_InitParamName"),
            NbBundle.getMessage(InitParamTableModel.class,"TTL_InitParamValue"),
            NbBundle.getMessage(InitParamTableModel.class,"TTL_Description")
        };

        protected String[] getColumnNames() {
            return columnNames;
        }

	public void setValueAt(Object value, int row, int column)
	{
		InitParam param = (InitParam)getChildren().get(row);

		if (column == 0) param.setParamName((String)value);
		else if (column == 1) param.setParamValue((String)value);
		else param.setDescription((String)value);
	}


	public Object getValueAt(int row, int column)
	{
		InitParam param = (InitParam)getChildren().get(row);

		if (column == 0) return param.getParamName();
		else if (column == 1) return param.getParamValue();
		else {
                    String desc = param.getDefaultDescription();
                    return (desc==null?null:desc.trim());
                }
	}
        
	public CommonDDBean addRow(Object[] values)
	{
            try {
                Object parent = getParent();
                InitParam param=null;
                if (parent instanceof Servlet)
                    param = (InitParam)((Servlet)parent).createBean("InitParam"); //NOI18N
                else if (parent instanceof Filter)
                    param = (InitParam)((Filter)parent).createBean("InitParam"); //NOI18N
                else
                    param = (InitParam)((WebApp)parent).createBean("InitParam"); //NOI18N
                param.setParamName((String)values[0]);
                param.setParamValue((String)values[1]);
                String desc = (String)values[2];
                if (desc.length()>0) param.setDescription(desc);
                if (parent instanceof Servlet)
                    ((Servlet)parent).addInitParam(param);
                else if (parent instanceof Filter)
                    ((Filter)parent).addInitParam(param);
                else
                    ((WebApp)parent).addContextParam(param);
                getChildren().add(param);
                fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
                return param;
            } catch (ClassNotFoundException ex) {}
            return null;
	}


	public void editRow(int row, Object[] values)
	{
                InitParam param = (InitParam)getChildren().get(row);
		param.setParamName((String)values[0]);
                param.setParamValue((String)values[1]);
                String desc=(String)values[2];
                if (desc.length()>0) param.setDescription(desc);
                fireTableRowsUpdated(row,row);
	}
        
	public void removeRow(int row)
	{
            Object parent = getParent();
            if (parent instanceof Servlet)
                ((Servlet)parent).removeInitParam((InitParam)getChildren().get(row));
            else if (parent instanceof Filter)
                ((Filter)parent).removeInitParam((InitParam)getChildren().get(row));
            else
                ((WebApp)parent).removeContextParam((InitParam)getChildren().get(row));
            getChildren().remove(row);
            fireTableRowsDeleted(row, row);
            
	}
}