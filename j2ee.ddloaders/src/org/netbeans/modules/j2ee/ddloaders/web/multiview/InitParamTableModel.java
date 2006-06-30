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