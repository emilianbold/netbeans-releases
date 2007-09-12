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
package org.netbeans.modules.bpel.properties.editors;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.editors.controls.BaseTablePanel;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.bpel.properties.editors.controls.QNameTableCellRenderer;
import org.netbeans.modules.bpel.nodes.CorrelationSetNode;
import org.netbeans.modules.bpel.nodes.actions.AddPropertyAction;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.bpel.properties.editors.controls.ObjectListTableModel;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.nodes.Node;

/**
 * This panel consists of the table which shows the list of Correlation
 * Properties which are associated to the Correlation Set.
 *
 * @author ads
 */
public class CSetPropertyTablePanel extends BaseTablePanel {
    
    static final long serialVersionUID = 1L;
    
    private CustomNodeEditor<CorrelationSet> myEditor;
    private CorrelationSetNode rootNode;
    private TableColumnModel columnModel;
    private MyTableModel tableModel;
    
    public CSetPropertyTablePanel( CustomNodeEditor<CorrelationSet> anEditor ) {
        super();
        this.myEditor = anEditor;
        createContent();
    }
    
    class MyColumnModel extends DefaultTableColumnModel {
        
        static final long serialVersionUID = 1L;
        
        public MyColumnModel() {
            super();
            //
            TableColumn column;
            Node.Property prop;
            //
            column = new TableColumn(0);
            column.setPreferredWidth(200);
            column.setIdentifier(PropertyType.NAME);
            column.setHeaderValue(PropertyType.NAME.getDisplayName());
            this.addColumn(column);
            //
            column = new TableColumn(1);
            column.setCellRenderer(new QNameTableCellRenderer());
            column.setPreferredWidth(500);
            column.setIdentifier(PropertyType.CORRELATON_PROPERTY_TYPE_NAME);
            column.setHeaderValue(PropertyType.CORRELATON_PROPERTY_TYPE_NAME.
                    getDisplayName());
            this.addColumn(column);
        }
    }
    
    class MyTableModel extends ObjectListTableModel<CorrelationProperty> {
        
        private CorrelationSet corrSet;
        
        public MyTableModel(CorrelationSet corrSet, TableColumnModel columnModel) {
            super(columnModel);
            assert corrSet != null;
            this.corrSet = corrSet;
            //
            reload();
        }
        
        public void reload() {
            List<WSDLReference<CorrelationProperty>> corrPropRefList =
                    corrSet.getProperties();
            if (corrPropRefList != null) {
                for (WSDLReference<CorrelationProperty> corrPropRef : corrPropRefList) {
                    CorrelationProperty corrProp = corrPropRef.get();
                    if (corrProp != null) {
                        addRow(corrProp);
                    }
                }
            }
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            CorrelationProperty cp = getRowObject(rowIndex);
            TableColumn column = columnModel.getColumn(columnIndex);
            //
            PropertyType propertyType = (PropertyType)column.getIdentifier();
            switch (propertyType) {
                case NAME:
                    return cp.getName();
                case CORRELATON_PROPERTY_TYPE_NAME:
                    NamedComponentReference<GlobalType> typeRef = cp.getType();
                    return typeRef != null ? typeRef.getQName() : null;
                default:
                    assert false : "The property: \"" + propertyType +
                            "\" isn't supported by the getValueAt()"; // NOI18N
            }
            return null;
        }
        
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            // isn't supported
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
        
        public Class<? extends Object> getColumnClass(int columnIndex) {
            return String.class;
        }
        
    }
    
    public void createContent() {
        super.createContent();
        //
        getButtonBar().btnEdit.setVisible(false);
        //
        columnModel = new MyColumnModel();
        //
        CorrelationSet corrSet = myEditor.getEditedObject();
        //
        tableModel = new MyTableModel(corrSet, columnModel);
        //
        JTable tableView = new JTable(tableModel, columnModel);
        tableView.getTableHeader().setReorderingAllowed(false);
        Dimension dim = tableView.getPreferredSize();
        dim.setSize(dim.getWidth(), 100d);
        tableView.setPreferredScrollableViewportSize(dim);
        //
        JScrollPane scrollPane = new JScrollPane(tableView,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //
        add(scrollPane, BorderLayout.CENTER);
        //
        setTableView(tableView);
    }
    
    protected void doRefresh() {
    }
    
    protected synchronized void addRow(ActionEvent event) {
        CorrelationSet corrSet = myEditor.getEditedObject();
        Set<CorrelationProperty> newCpSet = AddPropertyAction.chooseProperty(
                corrSet, tableModel.getRowsList(), myEditor.getLookup());
        if (newCpSet != null) {
            for (CorrelationProperty newCp : newCpSet) {
                if (newCp != null) {
                    tableModel.addRow(newCp);
                }
            }
        }
    }
    
    protected synchronized void deleteRowImpl(ActionEvent event) {
        int[] rows2delete = getTableView().getSelectedRows();
        for (int index = rows2delete.length - 1; index >= 0; index--) {
            // Remove in back order to prevent index violation
            int rowIndex = rows2delete[index];
            tableModel.deleteRow(rowIndex);
        }
    }
    
    public boolean applyNewValues() {
        CorrelationSet corrSet = myEditor.getEditedObject();
        //
        List<CorrelationProperty> corrPropList = tableModel.getRowsList();
        List<WSDLReference<CorrelationProperty>> corrPropRefList =
                new ArrayList<WSDLReference<CorrelationProperty>>();
        for (CorrelationProperty cp : corrPropList) {
            WSDLReference<CorrelationProperty> corrPropRef =
                    corrSet.createWSDLReference(cp, CorrelationProperty.class);
            corrPropRefList.add(corrPropRef);
        }
        //
        corrSet.setProperties(corrPropRefList);
        //
        return true;
    }
    
    public boolean subscribeListeners() {
        //
        // TODO Reimplement
//        // Subscribe to the WSDL model changes.
//        // Listen changes of Correlation Properties.
//        WSDLModel wsdlModel = WsdlResolverUtility.
//                getProcessRelatedWsdlModel(myEditor.getLookup());
//        if (wsdlModel != null) {
//            wsdlModel.addPropertyChangeListener(modelListener);
//        }
        //
        return true;
    }
    
    public boolean unsubscribeListeners() {
        //
        // TODO reimplement
//            // Unsubscribe from the WSDL model.
//            WSDLModel wsdlModel = WsdlResolverUtility.
//                    getProcessRelatedWsdlModel(myEditor.getLookup());
//            if (wsdlModel != null) {
//                wsdlModel.removePropertyChangeListener(modelListener);
//            }
        return true;
    }
    
    private BpelModel getModel(){
        return (BpelModel)myEditor.getLookup().lookup(BpelModel.class);
    }
    
    public List<CorrelationProperty> getCorrProperties() {
        return tableModel.getRowsList();
    }
    
}
