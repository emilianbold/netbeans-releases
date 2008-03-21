/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class GroupByPropertyEditor extends PropertyEditorSupport implements IPropertyEditor {

    private static final String LOG_CATEGORY = GroupByPropertyEditor.class.getName();
    private IProperty property;
    private static transient final Logger mLogger = Logger.getLogger(GroupByPropertyEditor.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
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
                mLogger.errorNoloc(mLoc.t("EDIT199: Error occured in setting the property value for Group By{0}from joinview table.", text), ex);
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

