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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.loaders.mapping.multiview;

import org.netbeans.modules.hibernate.loaders.cfg.multiview.*;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.hibernate.mapping.model.HibernateMapping;
import org.openide.util.NbBundle;

/**
 * 
 * @author Dongmei Cao
 */
public class MetaDataTableModel extends AbstractTableModel {

    private static final String[] columnNames = {
        NbBundle.getMessage(MetaDataTableModel.class, "LBL_Meta_Attribute"),
        NbBundle.getMessage(MetaDataTableModel.class, "LBL_Meta_Inherit"),
        NbBundle.getMessage(MetaDataTableModel.class, "LBL_Meta_Value")
    };
    // Matches the attribute name used in org.netbeans.modules.hibernate.mapping.model.HibernateMapping
    private static final String[] attrNames = new String[]{"Attribute", "Inherit"};
    private HibernateMapping mapping;

    public MetaDataTableModel(HibernateMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
    // TODO
    }

    public Object getValueAt(int row, int column) {

        if (mapping == null) {
            return null;
        } else {
            if (column == 0 || column == 1) {
                return mapping.getAttributeValue(HibernateMapping.META, row, attrNames[column]);
            } else {
                return mapping.getMeta( row );
            }
        }
    }

    public int getRowCount() {
        if (mapping == null) {
            return 0;
        } else {
            return mapping.sizeMeta();
        }
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return (false);
    }

    public void addRow(String attribute, String inherit, String value) {

        int index = mapping.addMeta(value);
        mapping.setAttributeValue(HibernateMapping.META, index, attrNames[0], attribute);
        mapping.setAttributeValue(HibernateMapping.META, index, attrNames[1], inherit);

        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }

    public void editRow(int row, String attribute, String inherit, String value) {
        mapping.setMeta(row, value);
        mapping.setAttributeValue(HibernateMapping.META, row, attrNames[0], attribute);
        mapping.setAttributeValue(HibernateMapping.META, row, attrNames[1], inherit);


        fireTableRowsUpdated(row, row);
    }

    public void removeRow(int row) {
        String value = mapping.getMeta(row);
        mapping.removeMeta(value);

        fireTableRowsDeleted(row, row);
    }
}
