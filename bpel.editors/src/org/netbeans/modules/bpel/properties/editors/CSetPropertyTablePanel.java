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
package org.netbeans.modules.bpel.properties.editors;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JLabel;
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
import org.netbeans.modules.bpel.nodes.actions.AddPropertyAction;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.bpel.properties.editors.controls.ObjectListTableModel;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.CorrelationProperty;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * This panel consists of the table which shows the list of Correlation
 * Properties which are associated to the Correlation Set.
 *
 * @author ads
 */
public class CSetPropertyTablePanel extends BaseTablePanel {
    
    static final long serialVersionUID = 1L;
    
    private CustomNodeEditor<CorrelationSet> myEditor;
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
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            // isn't supported
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
        
        @Override
        public Class<? extends Object> getColumnClass(int columnIndex) {
            return String.class;
        }
    }
    
    @Override
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
        
        tableView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(
            getClass(), "A11_DESCRIPTOR_CorrelationSetPropertyTable"));
        tableView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(
            getClass(), "A11_NAME_CorrelationSetPropertyTable"));
        
        JScrollPane scrollPane = new JScrollPane(tableView,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // for meeting of the A11 requirements: JTable should be labeled
        new JLabel(NbBundle.getMessage(getClass(), 
            "A11_DESCRIPTOR_CorrelationSetPropertyTable")).setLabelFor(tableView);
        add(scrollPane, BorderLayout.CENTER);

        setTableView(tableView);
    }
    
    @Override
    protected void doRefresh() {
    }
    
    @Override
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
    
    @Override
    protected synchronized void deleteRowImpl(ActionEvent event) {
        int[] rows2delete = getTableView().getSelectedRows();
        for (int index = rows2delete.length - 1; index >= 0; index--) {
            // Remove in back order to prevent index violation
            int rowIndex = rows2delete[index];
            tableModel.deleteRow(rowIndex);
        }
    }
    
    @Override
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
    
    @Override
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
    
    @Override
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
