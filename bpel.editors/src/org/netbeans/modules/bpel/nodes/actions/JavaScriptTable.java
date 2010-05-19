/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.bpel.nodes.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2009.04.07
 */
final class JavaScriptTable extends JTable {

    JavaScriptTable(String value, boolean isInput) {
        if (isInput) {
            myTitles = new String[] { JAVA_SCRIPT_VARIABLES, BPEL_VARIABLES };
        }
        else {
            myTitles = new String[] { BPEL_VARIABLES, JAVA_SCRIPT_VARIABLES };
        }
        myValue = value.trim();
        myModel = new Model();
        setModel(myModel);
        setSelection(0);
    }

    void add(String left, String right) {
        myModel.add(left, right);
        updateData();
    }

    void delete() {
        int row = getSelectedRow();

        if (row >= 0 && row < myModel.getRowCount()) {
            myModel.delete(row);
            setSelection(row);
            updateData();
        }
    }

    String getValue() {
        return myModel.getValue();
    }

    private void updateData() {
        revalidate();
        repaint();
    }

    private void setSelection(int index) {
        if (index >= myModel.getRowCount()) {
            index = myModel.getRowCount() - 1;
        }
        getSelectionModel().setSelectionInterval(index, index);
    }

    String[] getJavaScriptVariables(boolean isInput) {
        return myModel.getValues(isInput);
    }

    // ---------------------------------------------
    private class Model extends AbstractTableModel {

        Model() {
            readRecords();
        }

        public int getRowCount() {
            return myRecords.size(); 
        }

        public int getColumnCount() { 
            return myTitles.length; 
        } 

        @Override
        public String getColumnName(int column) { 
            return myTitles[column];
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public Object getValueAt(int row, int column) {
            if (row < 0 || row >= getRowCount()) {
                return null;
            }
            return myRecords.get(row).getData(column);
        }

        String getValue() {
            StringBuilder builder = new StringBuilder();
            Record record;

            for (int i=0; i < myRecords.size(); i++) {
                if (i > 0) {
                    builder.append(","); // NOI18N
                }
                record = myRecords.get(i);
                builder.append(record.getData(0) + "=" + record.getData(1));
            }
            return builder.toString();
        }

        String[] getValues(boolean isInput) {
            List<String> values = new ArrayList<String>();
            values.add(""); // NOI18N
            Record record;

            for (int i=0; i < myRecords.size(); i++) {
                String data = myRecords.get(i).getData(isInput ? 0 : 1);

                if ( !values.contains(data)) {
                    values.add(data);
                }
            }
            return values.toArray(new String[values.size()]);
        }

        private void readRecords() {
            myRecords = new ArrayList<Record>();
            StringTokenizer stk = new StringTokenizer(myValue, ","); // NOI18N
            String left;
            String right;
            int k;

            while (stk.hasMoreTokens()) {
                String token = stk.nextToken();
                k = token.indexOf("="); // NOI18N

                if (k == -1) {
                   continue;
                }
                left = token.substring(0, k).trim();
                right = token.substring(k + 1).trim();

                if (left.length() > 0 && right.length() > 0) {
                    myRecords.add(new Record(left, right));
                }
            }
        }

        void delete(int row) {
            myRecords.remove(row);
        }

        void add(String left, String right) {
            myRecords.add(new Record(left, right));
        }

        private List<Record> myRecords;
    }

    // --------------------------
    private static class Record {
        Record(String left, String right) {
            myData = new String[] {left, right};
        }

        String getData(int index) {
            return myData[index];
        }

        private String[] myData;
    }

    private Model myModel;
    private String myValue;
    private String[] myTitles;

    private static final String BPEL_VARIABLES = i18n(JavaScriptTable.class, "LBL_BPEL_Variables"); // NOI18N
    private static final String JAVA_SCRIPT_VARIABLES = i18n(JavaScriptTable.class, "LBL_JavaScript_Variables"); // NOI18N
}
