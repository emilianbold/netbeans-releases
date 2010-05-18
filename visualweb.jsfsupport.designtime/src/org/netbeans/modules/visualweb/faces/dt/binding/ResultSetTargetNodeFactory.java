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
 * License.  When distributing the software, include this License Header
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
package org.netbeans.modules.visualweb.faces.dt.binding;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.sql.*;
import javax.faces.model.*;
import javax.swing.*;
import javax.swing.tree.*;
import com.sun.rave.designtime.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;

public class ResultSetTargetNodeFactory implements TargetNodeFactory {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(ResultSetTargetNodeFactory.class);

    public boolean supportsTargetClass(Class targetClass) {
        return ResultSet.class.isAssignableFrom(targetClass);
    }

    public BindingTargetNode
        createTargetNode(DefaultTreeModel treeModel, DesignBean bean, PropertyDescriptor[] propPath, Object propInstance) {
        return new ResultSetTargetNode(treeModel, bean, propPath, propInstance);
    }

    public class ResultSetTargetNode extends BindingTargetNode.PropertyTargetNode {
        protected ResultSet resultSet;
        public ResultSetTargetNode(DefaultTreeModel treeModel, DesignBean bean, PropertyDescriptor[] propPath, Object propInstance) {
            super(treeModel, bean, propPath, propInstance);
            if (propInstance == null) {
                propInstance = getPropInstance(bean, propPath);
            }
            if (propInstance instanceof ResultSet) {
                resultSet = (ResultSet)propInstance;
            }
        }
        public void lazyLoadCustomTargetNodes() {
            if (resultSet != null) {
                try {
                    ResultSetMetaData rsmd = resultSet.getMetaData();
                    int cols = rsmd.getColumnCount();
                    if (cols > 0) {
                        for (int i = 0; i < cols; i++) {
                            //System.out.println("*** Adding column : " + rsmd.getColumnName(i + 1));   //NOI18N
                            super.add(new ColumnNode(treeModel,
                                rsmd.getColumnName(i + 1),
                                rsmd.getColumnType(i + 1)));
                        }
                    }
                    super.add(new SelectItemsNode(treeModel, rsmd));
                }
                catch (Exception x) {
//                    x.printStackTrace();
                }
            }
        }

        public class ColumnNode extends BindingTargetNode {
            protected String columnName;
            protected int columnType;
            public ColumnNode(DefaultTreeModel treeModel, String columnName, int columnType) {
                super(treeModel);
                this.columnName = columnName;
                this.columnType = columnType;
            }
            public int getChildCount() {
                return 0;
            }
            public boolean lazyLoad() {
                return true;
            }
            public boolean isValidBindingTarget() {
                return true;
            }
            public String getBindingExpressionPart() {
                return "currentRow['" + columnName + "']";  //NOI81N
            }
            public Class getTargetTypeClass() {
                return getJavaType(columnType);
            }
            public String getDisplayText(boolean enableNode) {
                String tn = getTypeName(columnType);
                StringBuffer sb = new StringBuffer();
                sb.append("<html>");   //NOI18N
                if (!enableNode) {
                    sb.append("<font color=\"gray\">");   //NOI18N
                }
                sb.append(bundle.getMessage("column"));   //NOI18N
                sb.append(" ");   //NOI18N
                if (enableNode) {
                    sb.append("<b>");   //NOI18N
                }
                sb.append(columnName);
                if (enableNode) {
                    sb.append("</b>");   //NOI18N
                }
                sb.append(" &nbsp; <font size=\"-1\"><i>");   //NOI18N
                sb.append(tn);
                sb.append("</i></font>");   //NOI18N
                if (!enableNode) {
                    sb.append("</font>");   //NOI18N
                }
                sb.append("</html>");   //NOI18N
                return sb.toString();
            }
        }

        public class SelectItemsNode extends BindingTargetNode {
            protected ResultSetMetaData metaData;
            public SelectItemsNode(DefaultTreeModel treeModel, ResultSetMetaData metaData) {
                super(treeModel);
                this.metaData = metaData;
                initCustomPanel();
                displayTextEnabled = getDisplayText(true);
                displayTextDisabled = getDisplayText(false);
            }
            protected String displayTextEnabled = null;
            protected String displayTextDisabled = null;
            public String getDisplayText(boolean enableNode) {
                if (enableNode && displayTextEnabled != null) {
                    return displayTextEnabled;
                }
                else if (!enableNode && displayTextDisabled != null) {
                    return displayTextDisabled;
                }
                StringBuffer sb = new StringBuffer();
                sb.append("<html>");    //NOI18N
                if (!enableNode) {
                    sb.append("<font color=\"gray\">");    //NOI18N
                }
                if (enableNode) {
                    sb.append("<b>");    //NOI18N
                }
                sb.append(bundle.getMessage("selectItems"));  //NOI81N
                if (enableNode) {
                    sb.append("</b>");    //NOI18N
                }
                sb.append(" &nbsp; <font size=\"-1\"><i>");    //NOI18N
                sb.append(bundle.getMessage("parenItemsForListBoxOr"));    //NOI18N
                sb.append("</i></font>");    //NOI18N
                if (!enableNode) {
                    sb.append("</font>");    //NOI18N
                }
                sb.append("</html>");    //NOI18N
                return sb.toString();
            }
            public int getChildCount() { return 0; }
            public boolean lazyLoad() { return true; }
            public Class getTargetTypeClass() {
                return SelectItem[].class;
            }
            public boolean isValidBindingTarget() {
                return true;
            }
            public String getBindingExpressionPart() {
                return "selectItems['" + getColumnPicks() + "']";    //NOI18N
            }
            String getColumnPicks() {
                StringBuffer sb = new StringBuffer();
                Object o = valueCombo.getSelectedItem();
                if (o instanceof ComboDisplayColumn) {
                    sb.append(((ComboDisplayColumn)o).columnName);
                }
                o = labelCombo.getSelectedItem();
                if (o instanceof ComboDisplayColumn) {
                    sb.append(",");    //NOI18N
                    sb.append(((ComboDisplayColumn)o).columnName);
                }
                o = descrCombo.getSelectedItem();
                if (o instanceof ComboDisplayColumn) {
                    sb.append(",");    //NOI18N
                    sb.append(((ComboDisplayColumn)o).columnName);
                }
                return sb.toString();
            }
            JPanel pickerPanel = new JPanel();
            JLabel valueLabel = new JLabel(bundle.getMessage("valueField"));   //NOI18N
            JLabel labelLabel = new JLabel(bundle.getMessage("displayField"));   //NOI18N
            JLabel descrLabel = new JLabel(bundle.getMessage("tooltipField"));  //NOI18N
            JComboBox valueCombo = new JComboBox();
            JComboBox labelCombo = new JComboBox();
            JComboBox descrCombo = new JComboBox();
            void initCustomPanel() {
                ComboDisplayColumnRenderer cdcr = new ComboDisplayColumnRenderer();
                valueCombo.setRenderer(cdcr);
                labelCombo.setRenderer(cdcr);
                descrCombo.setRenderer(cdcr);
                labelCombo.addItem(bundle.getMessage("noneBrackets"));  //NOI18N
                descrCombo.addItem(bundle.getMessage("noneBrackets"));  //NOI18N
                try {
                    int cols = metaData.getColumnCount();
                    if (cols > 0) {
                        for (int i = 0; i < cols; i++) {
                            ComboDisplayColumn col = new ComboDisplayColumn(metaData.getColumnName(i + 1),
                                metaData.getColumnType(i + 1));
                            valueCombo.addItem(col);
                            labelCombo.addItem(col);
                            descrCombo.addItem(col);
                        }
                    }
                }
                catch (Exception x) {
                    x.printStackTrace();
                }
                pickerPanel.setLayout(new GridBagLayout());
                pickerPanel.add(valueLabel, new GridBagConstraints(
                    0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 0, 2, 4), 0, 0));
                pickerPanel.add(labelLabel, new GridBagConstraints(
                    1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 0, 2, 4), 0, 0));
                pickerPanel.add(descrLabel, new GridBagConstraints(
                    2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 0, 2, 0), 0, 0));
                pickerPanel.add(valueCombo, new GridBagConstraints(
                    0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 0, 0, 4), 0, 0));
                pickerPanel.add(labelCombo, new GridBagConstraints(
                    1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 0, 0, 4), 0, 0));
                pickerPanel.add(descrCombo, new GridBagConstraints(
                    2, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 0, 0, 0), 0, 0));
                valueCombo.addActionListener(updateAdapter);
                labelCombo.addActionListener(updateAdapter);
                descrCombo.addActionListener(updateAdapter);
            }
            public JComponent getCustomDisplayPanel(ActionListener updateCallback) {
                this.updateCallback = updateCallback;
                return pickerPanel;
            }
            ActionListener updateCallback = null;
            ActionListener updateAdapter = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (updateCallback != null) {
                        updateCallback.actionPerformed(e);
                    }
                }
            };
        }
    }

    public class ComboDisplayColumn {
        public String columnName;
        public int columnType;
        public ComboDisplayColumn(String columnName, int columnType) {
            this.columnName = columnName;
            this.columnType = columnType;
        }
    }

    public class ComboDisplayColumnRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ComboDisplayColumn) {
                ComboDisplayColumn cdc = (ComboDisplayColumn)value;
                String tn = getTypeName(cdc.columnType);
                StringBuffer sb = new StringBuffer();
                sb.append("<html><b>");  //NOI18N
                sb.append(cdc.columnName);
                sb.append("</b> &nbsp; <font size=\"-1\"><i>");  //NOI18N
                sb.append(tn);
                sb.append("</i></font></html>");  //NOI18N
                this.setText(sb.toString());
            }
            else {
                this.setText(bundle.getMessage("noneBrackets"));  //NOI18N
            }
            return this;
        }
    }

    public static String getTypeName(int sqlType) {
        switch (sqlType) {
            case Types.SMALLINT:
                return "SMALLINT";  //NOI18N
            case Types.INTEGER:
                return "INTEGER";  //NOI18N
            case Types.TINYINT:
                return "TINYINT";  //NOI18N
            case Types.BIGINT:
                return "BIGINT";  //NOI18N
            case Types.BIT:
                return "BIT";  //NOI18N
            case Types.BOOLEAN:
                return "BOOLEAN";  //NOI18N
            case Types.DATE:
                return "DATE";  //NOI18N
            case Types.TIME:
                return "TIME";  //NOI18N
            case Types.DECIMAL:
                return "DECIMAL";  //NOI18N
            case Types.NUMERIC:
                return "NUMERIC";  //NOI18N
            case Types.DOUBLE:
                return "DOUBLE";  //NOI18N
            case Types.REAL:
                return "REAL";  //NOI18N
            case Types.FLOAT:
                return "FLOAT";  //NOI18N
            case Types.BINARY:
                return "BINARY";  //NOI18N
            case Types.CHAR:
                return "CHAR";  //NOI18N
            case Types.LONGVARCHAR:
                return "LONGVARCHAR";  //NOI18N
            case Types.VARCHAR:
                return "VARCHAR";  //NOI18N
            case Types.BLOB:
                return "BLOB";  //NOI18N
            case Types.CLOB:
                return "CLOB";  //NOI18N
            case Types.DATALINK:
                return "DATALINK";  //NOI18N
            case Types.DISTINCT:
                return "DISTINCT";  //NOI18N
            case Types.JAVA_OBJECT:
                return "JAVA_OBJECT";  //NOI18N
            case Types.LONGVARBINARY:
                return "LONGVARBINARY";  //NOI18N
            case Types.NULL:
                return "NULL";  //NOI18N
            case Types.OTHER:
                return "OTHER";  //NOI18N
            case Types.REF:
                return "REF";  //NOI18N
            case Types.STRUCT:
                return "STRUCT";  //NOI18N
            case Types.TIMESTAMP:
                return "TIMESTAMP";  //NOI18N
            case Types.VARBINARY:
                return "VARBINARY";  //NOI18N
        }
        return null;
    }

    public static Class getJavaType(int sqlType) {
        switch (sqlType) {
            case Types.SMALLINT:
                return Short.class;
            case Types.INTEGER:
                return Integer.class;
            case Types.TINYINT:
                return Byte.class;
            case Types.BIGINT:
                return Long.class;
            case Types.BIT:
            case Types.BOOLEAN:
                return Boolean.class;
            case Types.DATE:
            case Types.TIME:
                return Timestamp.class;
            case Types.DECIMAL:
            case Types.NUMERIC:
                return Number.class;
            case Types.DOUBLE:
            case Types.REAL:
                return Double.class;
            case Types.FLOAT:
                return Float.class;
            case Types.BINARY:
            case Types.CHAR:
            case Types.LONGVARCHAR:
            case Types.VARCHAR:
            case Types.BLOB:
            case Types.CLOB:
            case Types.DATALINK:
            case Types.DISTINCT:
            case Types.JAVA_OBJECT:
            case Types.LONGVARBINARY:
            case Types.NULL:
            case Types.OTHER:
            case Types.REF:
            case Types.STRUCT:
            case Types.TIMESTAMP:
            case Types.VARBINARY:
                return null;
        }
        return null;
    }
}
