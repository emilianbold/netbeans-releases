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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.sql.framework.ui.view;

import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.netbeans.modules.sql.framework.model.SQLGroupBy;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.ui.editor.property.IProperty;
import org.netbeans.modules.sql.framework.ui.editor.property.IPropertyEditor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import com.sun.sql.framework.utils.Logger;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class GroupByPropertyEditor extends PropertyEditorSupport implements IPropertyEditor {

    private static final String LOG_CATEGORY = GroupByPropertyEditor.class.getName();
    private IProperty property;

    private TargetTable targetTable;
    IGraphViewContainer editor;

    public GroupByPropertyEditor(IGraphViewContainer editor, TargetTable tTable) {
        this.targetTable = tTable;
        this.editor = editor;
    }

    public String getAsText() {
        StringBuilder text = new StringBuilder(50);
        SQLGroupBy groupBy = (SQLGroupBy) this.getValue();
        if (groupBy != null) {
            List columns = groupBy.getColumns();
            Iterator it = columns.iterator();
            while (it.hasNext()) {
                Object colObj = it.next();
                text.append(colObj.toString());
                if (it.hasNext()) {
                    text.append(",");
                }
            }
        }
        return text.toString();
    }

    public Component getCustomEditor() {
        List groupByColumns = new ArrayList();
        if (this.getValue() != null) {
            groupByColumns = ((SQLGroupBy) this.getValue()).getColumns();
        }
        GroupByView view = new GroupByView(editor, targetTable, groupByColumns);
        view.setPreferredSize(new Dimension(350, 390));
        return view;
    }

    public IProperty getProperty() {
        return property;
    }

    /**
     * Sets the property value by parsing a given String. May raise
     * java.lang.IllegalArgumentException if either the String is badly formatted or if
     * this kind of property can't be expressed as text.
     * 
     * @param text The string to be parsed.
     */
    public void setAsText(String text) {
        if (!text.equals(this.getAsText())) {
            List allColumns = new ArrayList();
            try {
                List srcTables = targetTable.getSourceTableList();
                for (Iterator iter = srcTables.iterator(); iter.hasNext();) {
                    allColumns.addAll(((SourceTable) iter.next()).getColumnList());
                }
            } catch (Exception e) {
                // ignore
            }
            allColumns.addAll(this.targetTable.getColumnList());

            ArrayList columns = new ArrayList();
            StringTokenizer tok = new StringTokenizer(text, ",");
            while (tok.hasMoreTokens()) {
                String token = tok.nextToken();
                Object column = getExistingColumn(allColumns, token);
                if (column != null) {
                    columns.add(column);
                } else {
                    warnForInValidColumn(token);
                    return;
                }
            }

            this.setValue(SQLModelObjectFactory.getInstance().createGroupBy(columns, targetTable));
            try {
                if (this.property != null) {
                    this.property.setValue(this.getValue());
                }
            } catch (Exception ex) {
                Logger.printThrowable(Logger.WARN, LOG_CATEGORY, "setAsText", "Error occured in setting the property value for Group By " + text, ex);
            }
        }
    }

    public void setProperty(IProperty property) {
        this.property = property;
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    private Object getExistingColumn(List allColumns, String columnName) {
        Iterator it = allColumns.iterator();
        while (it.hasNext()) {
            Object column = it.next();
            if (columnName.equalsIgnoreCase(column.toString())) {
                return column;
            }
        }
        return null;
    }

    private void warnForInValidColumn(String columnName) {
        DialogDisplayer.getDefault().notify(
            new NotifyDescriptor.Message("The column " + columnName + " is invalid, please specify a valid column name.",
                NotifyDescriptor.WARNING_MESSAGE));
    }
}

