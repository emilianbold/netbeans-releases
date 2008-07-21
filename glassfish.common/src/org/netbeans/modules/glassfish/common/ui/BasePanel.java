// <editor-fold defaultstate="collapsed" desc=" License Header ">
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
//</editor-fold>

package org.netbeans.modules.glassfish.common.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.JTextComponent;
import org.openide.util.NbBundle;

abstract public class BasePanel extends JPanel {

    abstract protected String getPrefix();

    abstract protected List<Component> getDataComponents();

    /** this is likely to be called off the awt thread
     *
     * @param name
     * @param data
     */
    public void initializeData(String name, Map<String, String> data) {
        for (Component c : getDataComponents()) {
            // fill in the blanks...
            String compName = c.getName();
            if (compName != null) {
                // construct the key
                String key = getPrefix() + name + "." + compName;
                String value = data.get(key);
                if (null == value) {
                    value = NbBundle.getMessage(this.getClass(), "ERR_DATA_NOT_FOUND", key);
                } else {
                    c.setName(key); // for writing the field value back to the server
                }
                if (c instanceof JTextComponent) {
                    final JTextComponent jtc = (JTextComponent) c;
                    SwingUtilities.invokeLater(new TextFieldSetter(jtc, value));
                } else if (c instanceof AbstractButton) {
                    AbstractButton ab = (AbstractButton) c;
                    SwingUtilities.invokeLater(new ButtonSetter(ab, value));
                } else if (c instanceof JTable) {
                    JTable tab = (JTable) c;
                    SwingUtilities.invokeLater(new TableSetter(compName, tab, data));
                }
            }
        }
    }

    public Map<String,String> getData() {
        Map<String,String> retVal = new HashMap<String,String>(getDataComponents().size());
        for (Component c : getDataComponents()) {
            // fill in the blanks...
            String compName = c.getName();
            if (compName != null) {
                // construct the key
                String key = compName;
                if (c instanceof JTextComponent) {
                    final JTextComponent jtc = (JTextComponent) c;
                    retVal.put(key, jtc.getText());
                } else if (c instanceof AbstractButton) {
                    AbstractButton ab = (AbstractButton) c;
                    retVal.put(key, Boolean.toString(ab.isSelected()));
                } else if (c instanceof JTable) {
                    JTable tab = (JTable) c;
                    MyTableModel model = (MyTableModel) tab.getModel();
                    retVal.putAll(model.getData());
                }
            }
        }
        return retVal;
    }


    static class TextFieldSetter implements Runnable {
        private JTextComponent jtc;
        private String value;

        TextFieldSetter(JTextComponent jtc, String value) {
            this.jtc = jtc;
            this.value = value;
        }
        public void run() {
            jtc.setText(value);
        }
    }

    static class ButtonSetter implements Runnable {
        private AbstractButton button;
        private String value;
        ButtonSetter(AbstractButton button, String value) {
            this.button = button;
            this.value = value;
        }

        public void run() {
            button.setSelected(Boolean.parseBoolean(value));
        }
    }

    static class TableSetter implements Runnable {
        private JTable tab;
        private Map<String, String> data;
        private String spec;
        TableSetter(String spec, JTable tab, Map<String,String> data) {
            this.tab = tab;
            this.data = data;
            this.spec = spec;
        }

        public void run() {
            // build the row data
            String[] specComp = spec.split("\\.");
            int colCount = specComp.length - 1;
            if (0 >= colCount) {
                // probably should log something here, too...
                return;
            }
            List<String[]> l = new ArrayList<String[]>();
            String pattern = ".*\\."+specComp[0]+"\\..*\\."+specComp[1];
            Set<String> keys = data.keySet();
            String pushPrefix = null;
            for (String k : keys) {
                if (k.matches(pattern)) {
                    if (null == pushPrefix) {
                        int dex = k.lastIndexOf(specComp[0]);
                        pushPrefix = k.substring(0,dex);
                    }
                    String[] aRow = new String[colCount];
                    int dex = k.lastIndexOf(".");
                    String partialKey = k.substring(0, dex);
                    aRow[0] = data.get(k);
                    for (int i = 2 ; i < colCount+1; i++) {
                        aRow[i-1] = data.get(partialKey+"."+specComp[i]);
                        if (null == aRow[i-1]) aRow[i-1] = "";
                    }
                    l.add(aRow);
                }
            }
            tab.setModel(new MyTableModel(l.toArray(new String[l.size()][]),specComp,pushPrefix));
        }
    }

    static class MyTableModel extends AbstractTableModel {
        private String[][] rowData;
        private String pushPrefix;
        private String[] names;
        private String[] specComp;

        MyTableModel(String[][] rowData, String[] specComp, String pushPrefix) {
            this.rowData = rowData;
            this.specComp = specComp;
            this.pushPrefix = pushPrefix;
            if (rowData.length > 0) {
                int colCount = rowData[0].length;
                names = new String[colCount];
                for (int i = 0; i < colCount; i++) {
                    try {
                        names[i] = NbBundle.getMessage(this.getClass(),
                                "column-title." + specComp[0] + "." + specComp[i + 1]);
                    } catch (MissingResourceException mre) {
                        // TODO -- log the MRE
                        names[i] = specComp[i + 1];
                    }
                }
            }
        }

        public int getRowCount() {
            return rowData.length;
        }

        public int getColumnCount() {
            return rowData[0].length;
        }

        @Override
        public String getColumnName(int i) {
            if (i > -1 && i < getColumnCount())
                return names[i];
            return "";
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return rowData[rowIndex][columnIndex];
        }

        @Override
        public void setValueAt(Object arg0, int arg1, int arg2) {
            super.setValueAt(arg0, arg1, arg2);
            rowData[arg1][arg2] = (String) arg0;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col != 0;
        }

        public String getPushPrefix() {
            return pushPrefix;
        }

        public Map<String,String> getData() {
            Map<String,String> retVal = new HashMap<String,String>(getRowCount()*(getColumnCount()-1));
            for (int i = 0; i < getRowCount(); i++) {
                String key = pushPrefix + specComp[0]+"."+
                        getValueAt(i,0)+".";
                for (int j = 1; j < getColumnCount(); j++) {
                    key += specComp[j+1];
                    retVal.put(key, (String) getValueAt(i,j));
                }
            }
            return retVal;
        }
    }

    public static class Error extends BasePanel {
        public Error() {
        }
        protected String getPrefix() {
            return "";
        }
        protected List<Component> getDataComponents() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public void initializeData(String name, Map<String, String> data) {
            return;
        }
    }
}
