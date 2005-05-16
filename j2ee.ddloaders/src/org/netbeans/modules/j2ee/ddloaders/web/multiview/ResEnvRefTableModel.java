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

/** ResEnvRefTableModel - table model for resource env. references
 *
 * Created on April 11, 2005
 * @author  mkuchtiak
 */
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.openide.util.NbBundle;

public class ResEnvRefTableModel extends DDBeanTableModel
{
	private static final String[] columnNames = {
            NbBundle.getMessage(ResEnvRefTableModel.class,"TTL_ResRefName"),
            NbBundle.getMessage(ResEnvRefTableModel.class,"TTL_ResType"),
            NbBundle.getMessage(ResEnvRefTableModel.class,"TTL_Description")
        };

        protected String[] getColumnNames() {
            return columnNames;
        }

	public void setValueAt(Object value, int row, int column)
	{
		ResourceEnvRef param = getResourceEnvRef(row);

		if (column == 0) param.setResourceEnvRefName((String)value);
		else if (column == 1) param.setResourceEnvRefType((String)value);
		else param.setDescription((String)value);
	}


	public Object getValueAt(int row, int column)
	{
		ResourceEnvRef param = getResourceEnvRef(row);

		if (column == 0) return param.getResourceEnvRefName();
		else if (column == 1) return param.getResourceEnvRefType();
		else {
                    String desc = param.getDefaultDescription();
                    return desc==null?null:desc.trim();
                }
	}
        
	public CommonDDBean addRow(Object[] values)
	{
            try {
                WebApp webApp = (WebApp)getParent();
                ResourceEnvRef param=(ResourceEnvRef)webApp.createBean("ResourceEnvRef"); //NOI18N
                param.setResourceEnvRefName((String)values[0]);
                param.setResourceEnvRefType((String)values[1]);
                String desc = (String)values[2];
                param.setDescription(desc.length()>0?desc:null);
                webApp.addResourceEnvRef(param);
                getChildren().add(param);
                fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
                return param;
            } catch (ClassNotFoundException ex) {}
            return null;
	}

	public void editRow(int row, Object[] values)
	{
                ResourceEnvRef param = getResourceEnvRef(row);
                param.setResourceEnvRefName((String)values[0]);
                param.setResourceEnvRefType((String)values[1]);
                String desc = (String)values[2];
                param.setDescription(desc.length()>0?desc:null);
                fireTableRowsUpdated(row,row);
	}
        
	public void removeRow(int row)
	{
            WebApp webApp = (WebApp)getParent();
            webApp.removeResourceEnvRef(getResourceEnvRef(row));
            getChildren().remove(row);
            fireTableRowsDeleted(row, row);
            
	}
        
        ResourceEnvRef getResourceEnvRef(int row) {
            return (ResourceEnvRef)getChildren().get(row);
        }
}