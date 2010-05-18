/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.ldap.browser.attributetable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author anjeleevich
 */
public class AttributeTableModel extends AbstractTableModel {

    private List<AttributeTableRow> rows = new ArrayList<AttributeTableRow>();

    public AttributeNode addAttributeNode(String name,
            Object agregatedValue)
    {
        int n1 = 0;
        int n2 = rows.size() - 1;

        while (n1 <= n2) {
            int n = n1 + n2 >>> 1;
            String midName = rows.get(n).getName();
            int cmp = NAME_COMPARATOR.compare(midName, name);

            if (cmp < 0) {
                n1 = n + 1;
            } else if (cmp > 0) {
                n2 = n - 1;
            } else {
                break;
            }
        }

        int index = n1;

        AttributeNode node = new AttributeNode(this, name, agregatedValue);
        rows.add(index, node);

        return node;
    }

    public void unfold(int threshold) {
        if (threshold < 2) {
            return;
        }

        for (int i = rows.size() - 1; i >= 0; i--) {
            AttributeTableRow row = rows.get(i);
            if (!(row instanceof AttributeNode)) {
                continue;
            }

            AttributeNode node = (AttributeNode) row;
            if (node.getAttributeValueCount() >= threshold) {
                continue;
            }

            rows.remove(i);

            for (int j = node.getAttributeValueCount() - 1; j >= 0; j--) {
                AttributeValue value = node.getAttributeValue(j);
                rows.add(i, new AttributeNode(this, node.getName(),
                        value.getValue()));
            }
        }
    }

    public int getRowIndex(AttributeTableRow rowInstance) {
        return rows.indexOf(rowInstance);
    }

    public AttributeTableRow getRow(int row) {
        return rows.get(row);
    }

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return 2;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        AttributeTableRow row = rows.get(rowIndex);

        if (columnIndex == 0) {
            return row.getName();
        }

        if (columnIndex == 1) {
            return row.getValue();
        }

        throw new IndexOutOfBoundsException("" + columnIndex);
    }

    public void setExpanded(AttributeNode attributeNode, boolean expanded) {
        attributeNode.setExpanded(expanded);
    }

    void expandAttributeValues(AttributeNode attributeNode) {
        int index0 = rows.indexOf(attributeNode) + 1;
        int count = attributeNode.getAttributeValueCount();

        for (int i = 0; i < count; i++) {
            rows.add(index0 + i, attributeNode.getAttributeValue(i));
        }

        fireTableRowsInserted(index0, index0 + count - 1);
        fireTableRowsUpdated(index0 - 1, index0 - 1);
    }

    void insertAttributeValue(AttributeValue attributeValue) {
        AttributeNode attributeNode = attributeValue.getAttributeNode();

        int index = rows.indexOf(attributeNode) 
                + attributeNode.getAttributeValueCount();
        rows.add(attributeValue);

        fireTableRowsInserted(index, index);
    }

    void collapseAttributeValues(AttributeNode attributeNode) {
        int index0 = rows.indexOf(attributeNode) + 1;
        int count = attributeNode.getAttributeValueCount();

        for (int i = 0; i < count; i++) {
            rows.remove(index0);
        }

        fireTableRowsDeleted(index0, index0 + count - 1);
        fireTableRowsUpdated(index0 - 1, index0 - 1);
    }
    
    private static final Comparator<String> NAME_COMPARATOR
            = new Comparator<String>() 
    {
        public int compare(String s1, String s2) {
            int c = s1.compareToIgnoreCase(s2);
            if (c != 0) {
                return c;
            }

            return s1.compareTo(s2);
        }
    };
}
