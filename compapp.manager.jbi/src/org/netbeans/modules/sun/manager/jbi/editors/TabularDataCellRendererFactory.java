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
package org.netbeans.modules.sun.manager.jbi.editors;

import java.awt.Component;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.xml.namespace.QName;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationDescriptor;
import org.netbeans.modules.sun.manager.jbi.management.model.constraint.EnumerationConstraint;
import org.netbeans.modules.sun.manager.jbi.management.model.constraint.JBIComponentConfigurationConstraint;

/**
 *
 * @author jqian
 */
public class TabularDataCellRendererFactory {

    private static TableCellRenderer STRING_RENDERER = new StringCellRenderer();
    private static TableCellRenderer BOOLEAN_RENDERER = new BooleanCellRenderer();
    private static TableCellRenderer DOUBLE_RENDERER = new DoubleCellRenderer();
    private static TableCellRenderer INTEGER_RENDERER = new IntegerCellRenderer();
    private static TableCellRenderer PASSWORD_RENDERER = new PasswordCellRenderer();
    private static TableCellRenderer TABULAR_DATA_RENDERER = new /*StringCellRenderer(); */ TabularDataCellRenderer();
    private static final Map<Class, TableCellRenderer> classMap =
            new HashMap<Class, TableCellRenderer>();
    private static final Map<ApplicationVariableType, TableCellRenderer> avTypeMap =
            new HashMap<ApplicationVariableType, TableCellRenderer>();
    

    static {
        classMap.put(String.class, STRING_RENDERER);
        classMap.put(Number.class, DOUBLE_RENDERER);
        classMap.put(Integer.class, INTEGER_RENDERER);
        classMap.put(Boolean.class, BOOLEAN_RENDERER);
        classMap.put(TabularData.class, TABULAR_DATA_RENDERER);

        avTypeMap.put(ApplicationVariableType.STRING, STRING_RENDERER);
        avTypeMap.put(ApplicationVariableType.NUMBER, DOUBLE_RENDERER);
        avTypeMap.put(ApplicationVariableType.BOOLEAN, BOOLEAN_RENDERER);
        avTypeMap.put(ApplicationVariableType.PASSWORD, PASSWORD_RENDERER);
    }

    public static TableCellRenderer getRenderer(Class clazz,
            JBIComponentConfigurationDescriptor descriptor) {
        if (descriptor != null) {
            return getRenderer(descriptor);
        } else {
            return getRenderer(clazz);
        }
    }

    public static TableCellRenderer getRenderer(Class clazz) {
        TableCellRenderer ret = classMap.get(clazz);

        if (ret == null) {
            ret = STRING_RENDERER;
        }

        return ret;
    }

    private static TableCellRenderer getRenderer(
            JBIComponentConfigurationDescriptor descriptor) {

        QName typeQName = descriptor.getTypeQName();

        List<JBIComponentConfigurationConstraint> constraints =
                descriptor.getConstraints();

        if (descriptor.isEncrypted()) {
            return PASSWORD_RENDERER;
        } else if (JBIComponentConfigurationDescriptor.XSD_STRING.equals(typeQName)) {
            if (constraints.size() == 1 &&
                    constraints.get(0) instanceof EnumerationConstraint) {
                List<String> options =
                        ((EnumerationConstraint) constraints.get(0)).getOptions();
                return new ComboBoxCellRenderer(options, descriptor.getDefaultValue());
            }
        } else if (JBIComponentConfigurationDescriptor.XSD_BOOLEAN.equals(typeQName)) {
            return BOOLEAN_RENDERER;
        }

//        System.err.println("WARNING: (schema-based) TabularDataCellEditorFactory: Unsupported type: " + typeQName);
        return STRING_RENDERER;
    }

    public static TableCellRenderer getRenderer(ApplicationVariableType avType) {
        TableCellRenderer ret = avTypeMap.get(avType);

        if (ret == null) {
            ret = STRING_RENDERER;
        }

        return ret;
    }
    //==========================================================================
    /**
     * A string renderer.
     */
    static class StringCellRenderer extends DefaultTableCellRenderer {

        private static final Border myBorder = new EmptyBorder(1, 4, 1, 1);

        public StringCellRenderer() {
            super();
            setHorizontalAlignment(JLabel.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            setBorder(myBorder);
            return component;
        }
    }

    /**
     * A number renderer.
     */
    static class DoubleCellRenderer extends DefaultTableCellRenderer {

        private static final Border myBorder = new EmptyBorder(1, 4, 1, 1);
        NumberFormat formatter;

        public DoubleCellRenderer() {
            super();
            setHorizontalAlignment(JLabel.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            setBorder(myBorder);
            return component;
        }

        @Override
        public void setValue(Object value) {
            if (formatter == null) {
                formatter = NumberFormat.getInstance();
            }

            if (value == null) {
                setText(""); // NOI18N
            } else {
                try {
                    double d = Double.parseDouble(value.toString());
                    setText(formatter.format(d));
                } catch (NumberFormatException e) {
                    setText(value.toString());
                }
            }
        }
    }

    /**
     * A number renderer.
     */
    static class IntegerCellRenderer extends DefaultTableCellRenderer {

        private static final Border myBorder = new EmptyBorder(1, 4, 1, 1);
        NumberFormat formatter;

        public IntegerCellRenderer() {
            super();
            setHorizontalAlignment(JLabel.LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            setBorder(myBorder);
            return component;
        }

        @Override
        public void setValue(Object value) {
            if (formatter == null) {
                formatter = NumberFormat.getInstance();
            }

            if (value == null) {
                setText(""); // NOI18N
            } else {
                try {
                    int i = Integer.parseInt(value.toString());
                    setText(formatter.format(i));
                } catch (NumberFormatException e) {
                    setText(value.toString());
                }
            }
        }
    }

    /**
     * A boolean renderer.
     */
    static class BooleanCellRenderer extends JCheckBox implements TableCellRenderer {

        private static final Border myBorder = new EmptyBorder(1, 4, 1, 1);

        public BooleanCellRenderer() {
            super();
            setBorder(myBorder);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }

            setSelected(value != null &&
                    value.toString().equalsIgnoreCase("true")); // NOI18N

            return this;
        }
    }
    /**
     * A boolean renderer.
     */
    static class ComboBoxCellRenderer extends JComboBox implements TableCellRenderer {

//        private static final Border myBorder = new EmptyBorder(1, 4, 1, 1);

        public ComboBoxCellRenderer(List<String> options, String value) {
            super(options.toArray());
            if (value != null) {
                assert options.contains(value);
                setSelectedItem(value.toString());
            }
//            setBorder(myBorder);            
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }

            setSelectedItem(value.toString());

            return this;
        }
    }

    /**
     * A password renderer.
     */
    static class PasswordCellRenderer extends JPasswordField
            implements TableCellRenderer, Serializable {

        private static final Border myBorder = new EmptyBorder(1, 4, 1, 1);

        public PasswordCellRenderer() {
            super();
            setOpaque(true);
            setBorder(myBorder);
            setEchoChar('*'); // NOI18N
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }

            setValue(value);

            return this;
        }

        protected void setValue(Object value) {
            setText((value == null) ? "" : value.toString()); // NOI18N
        }
    }

    /**
     * A tabular data renderer.
     */
    static class TabularDataCellRenderer extends StringCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            if (value != null && value instanceof TabularData) {
                TabularData tabularData = (TabularData) value;

                StringBuilder sb = new StringBuilder();
                sb.append("{"); // NOI18N
                for (Object rowDataObj : tabularData.values()) {
                    CompositeData rowData = (CompositeData) rowDataObj;
                    String rowValues = getStringForRowData(rowData);
                    sb.append(rowValues);
                }
                sb.append("}"); //NOI18N
                value = sb.toString();
            }

            return super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
        }

        private String getStringForRowData(CompositeData rowData) {
            Collection rowValues = rowData.values();
            return rowValues.toString();
        }
    }
}
