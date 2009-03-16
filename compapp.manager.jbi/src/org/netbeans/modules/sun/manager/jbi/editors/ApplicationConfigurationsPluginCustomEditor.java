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
package org.netbeans.modules.sun.manager.jbi.editors;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.KeyAlreadyExistsException;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationDescriptor;
import org.netbeans.modules.xml.wsdl.bindingsupport.appconfig.spi.ApplicationConfigurationEditorProvider;
import org.netbeans.modules.xml.wsdl.bindingsupport.appconfigeditor.ApplicationConfigurationEditorProviderFactory;
import org.netbeans.modules.xml.wsdl.bindingsupport.appconfigeditor.ui.ApplicationConfigurationUtils;

/**
 *
 * @author jqian
 */
public class ApplicationConfigurationsPluginCustomEditor extends SimpleTabularDataCustomEditor {

    private final String componentName;

    public ApplicationConfigurationsPluginCustomEditor(
            SimpleTabularDataEditor editor,
            String tableLabelText,
            String tableLabelDescription,
            JBIComponentConfigurationDescriptor descriptor,
            boolean isWritable,
            String componentName) {
        super(editor, tableLabelText, tableLabelDescription,
                descriptor, isWritable);

        this.componentName = componentName;
    }

    @Override
    protected void initColumnNames() {
        columnNames = new String[2];
        columnNames[0] = "configuraitonName";
        columnNames[1] = "applicationConfiguraiton";
        
        indexColumnCount = 1;
    }

    @Override
    protected JTable createTable(DefaultTableModel tableModel) {
        JTable ret = new JTable(tableModel) {

            @Override
            public Class getColumnClass(int column) {
                if (column == 0) {
                    return String.class;
                } else if (column == 1) {
                    return CompositeType.class;
                } else {
                    assert false;
                    return null; // FAIL
                }
            }

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                Class clazz = getColumnClass(column);

                if (column == 0) {
                    return TabularDataCellEditorFactory.getEditor(clazz);
                } else {
                    JTextField textField = new JTextField();
                    DefaultCellEditor defaultCellEditor = new DefaultCellEditor(textField);
                    defaultCellEditor.setClickCountToStart(1);
                    textField.setEnabled(false);
                    return new ApplicationConfigurationTableCellEditor(
                            componentName, defaultCellEditor, true);
                }
            }

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                Class clazz = getColumnClass(column);

                if (column == 0) {
                    return TabularDataCellRendererFactory.getRenderer(clazz);
                } else {
                    JTextField textField = new JTextField();
                    DefaultCellEditor defaultCellEditor = new DefaultCellEditor(textField);
                    defaultCellEditor.setClickCountToStart(1);
                    textField.setEnabled(false);
                    return new ApplicationConfigurationTableCellEditor(
                            componentName, defaultCellEditor, true);
                }
            }
        };

        return ret;
    }

    @Override
    protected Vector<Vector> getDataVector(TabularData tabularData) {

        Vector<Vector> dataVector = new Vector<Vector>();

        if (tabularData != null) {
            for (Object rowDataObj : tabularData.values()) {
                CompositeData rowComposite = (CompositeData) rowDataObj;
                Vector<Object> row = new Vector<Object>();
                row.add(rowComposite.get("configurationName"));
                row.add(rowComposite);
                dataVector.add(row);
            }
        }

        return dataVector;
    }

    @Override
    protected TabularData getTabularData() throws OpenDataException {

        TabularType tabularType = getTabularType();
        TabularData ret = new TabularDataSupport(tabularType);
        CompositeType compositeType = tabularType.getRowType();

        Vector<Vector> dataVector = getDataVector();
        for (int rowIndex = 0; rowIndex < dataVector.size(); rowIndex++) {
            Vector rowVector = dataVector.get(rowIndex);
            String configName = (String) rowVector.get(0);            
            if (configName.trim().length() == 0) {
                throw new InvalidKeyException("Empty configuration name is not allowed.");
            }
            
            CompositeData rowComposite = (CompositeData) rowVector.get(1);
            if (rowComposite == null) {
                // We don't have to throw this exception, but the message of
                // the exception thrown otherwise from CompositeData is rather
                // confusing.
                throw new RuntimeException("Application configuration at row #" + (rowIndex + 1) + " is not defined.");
            }

            // Update the configuration name in the composite data
            Map<String, Object> map = new HashMap<String, Object>();
            
            for (Object key : compositeType.keySet()) {
                map.put((String) key, rowComposite.get((String) key));
            }            
            map.put("configurationName", configName);

            CompositeData myRowComposite =
                    new CompositeDataSupport(compositeType, map);

            ret.put(myRowComposite);
        }

        return ret;
    }

    @Override
    protected Vector createRow() {
        Vector row = new Vector();

        row.add("");   // empty application configuration name
        row.add(null); // null composite data

        return row;
    }

    class ApplicationConfigurationTableCellEditor
            extends AbstractActionTableCellEditorDecorator
            implements TableCellRenderer {

        private JBIComponentConfigurationDescriptor descriptor;
        private boolean isWritable;
        private String componentName;

        public ApplicationConfigurationTableCellEditor(String componentName,
                TableCellEditor realEditor,
                boolean isWritable) {
            this(realEditor, null, isWritable);
            this.componentName = componentName;
        }

        public ApplicationConfigurationTableCellEditor(TableCellEditor realEditor,
                JBIComponentConfigurationDescriptor descriptor,
                boolean isWritable) {
            super(realEditor);
            this.descriptor = descriptor;
            this.isWritable = isWritable;
        }

        protected void editCell(JTable table, int row, int column) {
            CompositeData compositeData = (CompositeData) table.getValueAt(row, column);

            String appConfigName = (String) table.getValueAt(row, 0);

            ApplicationConfigurationEditorProvider provider =
                    ApplicationConfigurationEditorProviderFactory.getDefault().
                    getConfigurationProvider(componentName);
            compositeData = ApplicationConfigurationUtils.configApplicationConfiguration(
                    provider, componentName, appConfigName, compositeData);

            if (compositeData != null) {
                table.setValueAt(compositeData, row, column);
            }
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            Component ret = super.getTableCellEditorComponent(table, value, isSelected, row, column);
            TableCellEditor realEditor = getRealEditor();
            Component realEditorComponent = ((DefaultCellEditor) realEditor).getComponent();
            assert realEditorComponent instanceof JTextField;
            JTextField textField = (JTextField) realEditorComponent;
            textField.setText(value == null ? "<Not Defined>" : "<Defined>");

            return ret;
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            return getTableCellEditorComponent(table, value, isSelected, row, column);
        }
    }
}

