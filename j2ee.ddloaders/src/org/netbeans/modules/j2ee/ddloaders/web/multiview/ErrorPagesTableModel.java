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
import org.netbeans.modules.j2ee.dd.api.web.ErrorPage;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.openide.util.NbBundle;

public class ErrorPagesTableModel extends DDBeanTableModel
{
	private static final String[] columnNames = {
            NbBundle.getMessage(ErrorPagesTableModel.class,"TTL_ErrorPageLocation"),
            NbBundle.getMessage(ErrorPagesTableModel.class,"TTL_ErrorCode"),
            NbBundle.getMessage(ErrorPagesTableModel.class,"TTL_ExceptionType")
        };

        protected String[] getColumnNames() {
            return columnNames;
        }

	public void setValueAt(Object value, int row, int column)
	{
		ErrorPage page = (ErrorPage)getChildren().get(row);
		if (column == 0) page.setLocation((String)value);
		else if (column == 1) page.setErrorCode((Integer)value);
		else page.setExceptionType((String)value);
	}


	public Object getValueAt(int row, int column)
	{
		ErrorPage page = (ErrorPage)getChildren().get(row);

		if (column == 0) return page.getLocation();
		else if (column == 1) return page.getErrorCode();
		else return page.getExceptionType();
	}
        
	public CommonDDBean addRow(Object[] values)
	{
            try {
                ErrorPage page = (ErrorPage)((WebApp)getParent()).createBean("ErrorPage"); //NOI18N
                page.setLocation((String)values[0]);
                if (values[1]!=null) page.setErrorCode((Integer)values[1]);;
                if (values[2]!=null) page.setExceptionType((String)values[2]);
                ((WebApp)getParent()).addErrorPage(page);
                getChildren().add(page);
                fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
                return page;
            } catch (ClassNotFoundException ex) {}
            return null;
	}


	public void editRow(int row, Object[] values)
	{
                ErrorPage page = (ErrorPage)getChildren().get(row);
		page.setLocation((String)values[0]);
                page.setErrorCode((Integer)values[1]);
                page.setExceptionType((String)values[2]);
                fireTableRowsUpdated(row,row);
	}
        
	public void removeRow(int row)
	{
            ((WebApp)getParent()).removeErrorPage((ErrorPage)getChildren().get(row));
            getChildren().remove(row);
            fireTableRowsDeleted(row, row);
            
	}
}