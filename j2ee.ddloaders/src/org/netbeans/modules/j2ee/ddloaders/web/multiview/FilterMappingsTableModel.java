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
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.openide.util.NbBundle;

public class FilterMappingsTableModel extends DDBeanTableModel
{
	private static final String[] columnNames = {
            NbBundle.getMessage(FilterMappingsTableModel.class,"TTL_FilterName"),
            NbBundle.getMessage(FilterMappingsTableModel.class,"TTL_AppliesTo"),
            NbBundle.getMessage(FilterMappingsTableModel.class,"TTL_DispatcherTypes")
        };

        protected String[] getColumnNames() {
            return columnNames;
        }
        /*
	public void setValueAt(Object value, int row, int column)
	{
		FilterMapping map = (FilterMapping)getChildren().get(row);
		if (column == 0) map.setFilterName((String)value);
		else map.setDispatcher((String[])value);
	}
        */

	public Object getValueAt(int row, int column)
	{
		FilterMapping map = (FilterMapping)getChildren().get(row);

		if (column == 0) return map.getFilterName();
		else if (column==1) {
                    String urlPattern = map.getUrlPattern();
                    return (urlPattern==null?
                            NbBundle.getMessage(FilterMappingsTableModel.class,"TXT_appliesToServlet",map.getServletName()):
                            NbBundle.getMessage(FilterMappingsTableModel.class,"TXT_appliesToUrl",urlPattern));
                } else {
                    try {
                        return DDUtils.urlPatternList(map.getDispatcher());
                    } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {
                        return null;
                    }
                }
	}
        
	public CommonDDBean addRow(Object[] values)
	{
            try {
                FilterMapping map = (FilterMapping)((WebApp)getParent()).createBean("FilterMapping"); //NOI18N
                map.setFilterName((String)values[0]);
                if (values[1]!=null) map.setUrlPattern((String)values[1]);;
                if (values[2]!=null) map.setServletName((String)values[2]);
                try {
                    if (values[3]!=null) map.setDispatcher((String[])values[3]);
                } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {}
                ((WebApp)getParent()).addFilterMapping(map);
                getChildren().add(map);
                fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
                return map;
            } catch (ClassNotFoundException ex) {}
            return null;
	}


	public void editRow(int row, Object[] values)
	{
                FilterMapping map = (FilterMapping)getChildren().get(row);
		map.setFilterName((String)values[0]);
                map.setUrlPattern((String)values[1]);
                map.setServletName((String)values[2]);
                try {
                    map.setDispatcher((String[])values[3]);
                } catch (org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {}
                fireTableRowsUpdated(row,row);
	}
        
	public void removeRow(int row)
	{
            ((WebApp)getParent()).removeFilterMapping((FilterMapping)getChildren().get(row));
            getChildren().remove(row);
            fireTableRowsDeleted(row, row);
            
	}
}