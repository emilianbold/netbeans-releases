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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.bpel.model.api.BPELElementsBuilder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationContainer;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CorrelationsHolder;
import org.netbeans.modules.bpel.properties.choosers.CorrelationSetChooserPanel;
import org.netbeans.modules.bpel.properties.editors.controls.BaseTablePanel;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.bpel.properties.editors.controls.SimpleCustomEditor;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.support.Initiate;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.properties.Constants;
import org.openide.nodes.Node;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.model.api.support.VisibilityScope;
import org.netbeans.modules.bpel.editors.api.ui.valid.NodeEditorDescriptor;
import org.netbeans.modules.bpel.nodes.CorrelationSetNode;
import org.openide.util.Lookup;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor.EditingMode;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.netbeans.modules.bpel.properties.editors.controls.ObjectListTableModel;
import org.netbeans.modules.bpel.properties.editors.controls.TreeNodeChooser;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.bpel.editors.api.ui.valid.ErrorMessagesBundle;
import org.netbeans.modules.soa.ui.form.valid.DefaultDialogDescriptor;
import org.netbeans.modules.soa.ui.form.valid.ValidationExtension;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.openide.nodes.Children;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * This panel is a part of property editor of elements like Receive, Reply and OnMessage.
 * It shows the table with a set of related corelations.
 *
 * @author nk160297
 */
public class CorrelationTablePanel extends BaseTablePanel
        implements Validator.Provider, HelpCtx.Provider {
    
    static final long serialVersionUID = 1L;
    
    private CustomNodeEditor<? extends CorrelationsHolder> myEditor;
    private DefaultValidator myValidator;
    //
    private TableColumnModel columnModel;
    private MyTableModel tableModel;
    
    public CorrelationTablePanel(CustomNodeEditor<? extends CorrelationsHolder> anEditor) {
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
            column.setPreferredWidth(150);
            column.setIdentifier(PropertyType.CORRELATION_SET);
            column.setHeaderValue(PropertyType.CORRELATION_SET.getDisplayName());
            column.setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected,
                            hasFocus, row, column);
                    if (value != null) {
                        assert value instanceof CorrelationSet;
                        CorrelationSet corrSet = (CorrelationSet)value;
                        String text = corrSet.getName();
                        setText(text);
                        setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 2));
                    }
                    return this;
                }
            });
            this.addColumn(column);
            //
            column = new TableColumn(1);
            column.setIdentifier(PropertyType.CORRELATION_INITIATE);
            column.setHeaderValue(PropertyType.CORRELATION_INITIATE.getDisplayName());
            final Initiate[] initiateArr = new Initiate[] {
                Initiate.NO, Initiate.YES, Initiate.JOIN};
            //
            final JComboBox editorCb = new JComboBox(initiateArr);
            editorCb.setEditable(true);
            column.setCellRenderer(new TableCellRenderer() {
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    assert value == null || value instanceof Initiate;
                    if (value == null) {
                        ((JTextComponent)editorCb.getEditor().getEditorComponent()).
                                setText(Constants.NOT_ASSIGNED);
                    } else if (((Initiate)value).isInvalid()) {
                        ((JTextComponent)editorCb.getEditor().getEditorComponent()).
                                setText(Constants.INVALID);
                    } else {
                        editorCb.setSelectedItem(value);
                    }
                    return editorCb;
                }
            });
            column.setCellEditor(new DefaultCellEditor(new JComboBox(initiateArr)));
            column.setPreferredWidth(40);
            this.addColumn(column);
        }
    }
    
    class MyTableModel extends ObjectListTableModel<CorrelationLocal> {
        
        private CorrelationsHolder corrHolder;
        
        public MyTableModel(CorrelationsHolder corrHolder, TableColumnModel columnModel) {
            super(columnModel);
            assert corrHolder != null;
            this.corrHolder = corrHolder;
            //
            reload();
        }
        
        public void reload() {
            CorrelationContainer container = corrHolder.getCorrelationContainer();
            if (container != null) {
                Correlation[] corrArr = container.getCorrelations();
                for (Correlation corr : corrArr) {
                    CorrelationLocal corrLocal = new CorrelationLocal(corr);
                    addRow(corrLocal);
                }
            }
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            CorrelationLocal pCorrLocal = getRowObject(rowIndex);
            TableColumn column = columnModel.getColumn(columnIndex);
            //
            PropertyType propertyType = (PropertyType)column.getIdentifier();
            switch (propertyType) {
                case CORRELATION_SET:
                    return pCorrLocal.getSet();
                case CORRELATION_INITIATE:
                    return pCorrLocal.getInitiate();
                default:
                    assert false : "The property: \"" + propertyType +
                            "\" isn't supported by the getValueAt()"; // NOI18N
            }
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            CorrelationLocal pCorrLocal = getRowObject(rowIndex);
            TableColumn column = columnModel.getColumn(columnIndex);
            //
            PropertyType propertyType = (PropertyType)column.getIdentifier();
            switch (propertyType) {
                case CORRELATION_SET:
                    // Not Editable column
                    break;
                case CORRELATION_INITIATE:
                    assert aValue == null || aValue instanceof Initiate;
                    pCorrLocal.setInitiate((Initiate)aValue);
                    break;
                default:
                    assert false : "The property: \"" + propertyType +
                            "\" isn't supported by the getValueAt()"; // NOI18N
            }
        }
        
    }
    
    /**
     * This object intended to be used as a table row element.
     */
    class CorrelationLocal {
        private Initiate myInitiate;
        private CorrelationSet mySet;
        
        public CorrelationLocal(CorrelationSet set, Initiate initiate) {
            mySet = set;
            myInitiate = initiate;
        }
        
        public CorrelationLocal(Correlation modelEntity) {
            assert modelEntity != null;
            BpelReference<CorrelationSet> cSetRef = modelEntity.getSet();
            if (cSetRef != null) {
                mySet = cSetRef.get();
            }
            myInitiate = modelEntity.getInitiate();
        }
        
        public Initiate getInitiate() {
            return myInitiate;
        }
        
        public CorrelationSet getSet() {
            return mySet;
        }
        
        public void removeInitiate() {
            myInitiate = null;
        }
        
        public void setInitiate(Initiate value) {
            myInitiate = value;
        }
        
        public void setSet(CorrelationSet value) {
            mySet = value;
        }
        
        /**
         * Creats the new Correlaton object and adds it to
         * model to the specified CorrelationContainer.
         *
         * The cc parameter has to be already in the model,
         * otherwise the BpelReference<CorrelationSet> will be incorrect.
         */
        public Correlation createObjectInModel(CorrelationContainer cc) {
            Correlation result = cc.getBpelModel().getBuilder().createCorrelation();
            cc.addCorrelation(result);
            //
            if (mySet == null) {
                assert false : "The CorrelationSet has to be specified";
            } else {
                BpelReference<CorrelationSet> cSetRef =
                        result.createReference(mySet, CorrelationSet.class);
                result.setSet(cSetRef);
            }
            //
            if (myInitiate == null) {
                result.removeInitiate();
            } else {
                result.setInitiate(myInitiate);
            }
            //
            return result;
        }
    }
    
    @Override
    public void createContent() {
        super.createContent();
        //
        columnModel = new MyColumnModel();
        //
        CorrelationsHolder corrHolder = myEditor.getEditedObject();
        //
        tableModel = new MyTableModel(corrHolder, columnModel);
        //
        JTable tableView = new JTable(tableModel, columnModel);
        tableView.getTableHeader().setReorderingAllowed(false);
        tableView.setRowHeight((int)(new JComboBox()).getPreferredSize().getHeight());
        Dimension dim = tableView.getPreferredSize();
        dim.setSize(dim.getWidth(), 100d);
        tableView.setPreferredScrollableViewportSize(dim);
        //
        JScrollPane scrollPane = new JScrollPane(tableView,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //
        this.add(scrollPane, BorderLayout.CENTER);
        // tableView.setPreferredSize(new Dimension(400, 200));
        //
        getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(FormBundle.class,"ACSN_LBL_Correlations_Tab")); // NOI18N
        getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(FormBundle.class,"ACSD_LBL_Correlations_Tab")); // NOI18N
        //
        setTableView(tableView);
    }

    @Override
    protected void addRow(ActionEvent event) {
        Set<CorrelationSet> csSet = chooseCorrelationSet(tableModel.getRowsList());
        if (csSet != null) {
            for (CorrelationSet cs : csSet) {
                CorrelationLocal newCorrLocal = 
                        new CorrelationLocal(cs, Initiate.NO);
                //
                tableModel.addRow(newCorrLocal);
            }
        }
    }
    
    private Set<CorrelationSet> chooseCorrelationSet(
            final List<CorrelationLocal> currCorrLocalList) {
        Lookup lookup = myEditor.getLookup();
        BpelEntity entry = (BpelEntity)myEditor.getEditedObject();
        //
        final CorrelationSetChooserPanel csChooser = new CorrelationSetChooserPanel();
        //
        // Construct a validation extension which is intended to prevent
        // duplicate Properties in the CorrelationSet.
        ValidationExtension validationExt = new ValidationExtension() {
            public Validator getExtensionValidator() {
                Validator validator = new DefaultValidator(
                        csChooser, ErrorMessagesBundle.class) {
                    public void doFastValidation() {
                    }
                    
                    @Override
                    public void doDetailedValidation() {
                        Set<CorrelationSet> newCsSet = csChooser.getSelectedValue();
                        //
                        for (CorrelationSet newCs : newCsSet) {
                            if (newCs == null) {
                                continue;
                            }
                            //
                            for (CorrelationLocal corr : currCorrLocalList) {
                                CorrelationSet cs = corr.getSet();
                                if (newCs.equals(cs)) {
                                    addReasonKey(Severity.ERROR, 
                                            "ERR_NOT_UNIQUE_CORR_SET", 
                                            cs.getName()); // NOI18N
                                }
                            }
                        }
                    }
                    
                };
                return validator;
            }
        };
        //
        VisibilityScope visScope = new VisibilityScope(entry, lookup);
        Lookup contextLookup = new ExtendedLookup(lookup, visScope, validationExt);
        //
        csChooser.setLookup(contextLookup);
        TreeNodeChooser chooser = new TreeNodeChooser(csChooser);
        chooser.initControls();
        //
        String title = NbBundle.getMessage(
                FormBundle.class, "DLG_CorrelationSetChooser"); // NOI18N
        DefaultDialogDescriptor descriptor =
                new DefaultDialogDescriptor(chooser, title);
        SoaDialogDisplayer.getDefault().notify(descriptor);
        if (descriptor.isOkHasPressed()) {
            Set<CorrelationSet> corrSet = csChooser.getSelectedValue();
            return corrSet;
        }
        return null;
//        csChooser.afterClose();
    }
    
    @Override
    protected void editRow(ActionEvent event) {
        int rowIndex = getTableView().getSelectedRow();
        if (rowIndex == -1) return; // Nothing to edit because of nothing is selected
        //
        CorrelationLocal corrLocal = tableModel.getRowObject(rowIndex);
        if (corrLocal.getSet() == null) {
            Set<CorrelationSet> csSet = chooseCorrelationSet(tableModel.getRowsList());
            if (csSet != null && !csSet.isEmpty()) {
                CorrelationSet cs = csSet.iterator().next();
                corrLocal.setSet(cs);
            }
        } else {
            CorrelationSet corrSet = corrLocal.getSet();
            if (editCorrelationSet(corrSet)) {
                tableModel.updateRow(rowIndex);
            }
        }
    }
    
    /**
     * Call the CorrelationSet property editor.
     * Return flag which indicates if the user pressed Ok button.
     */
    private boolean editCorrelationSet(CorrelationSet cSet) {
        Lookup lookup = myEditor.getLookup();
        if (cSet != null) {
            Children children = new CorrelationSetNode.MyChildren(cSet, lookup);
            CorrelationSetNode csNode =
                    new CorrelationSetNode(cSet, children, lookup);
            //
            SimpleCustomEditor customEditor =
                    new SimpleCustomEditor<CorrelationSet>(
                    csNode, CorrelationSetMainPanel.class,
                    EditingMode.EDIT_INSTANCE);
            String title = NbBundle.getMessage(
                    FormBundle.class, "DLG_EditCorrelationSet"); // NOI18N
            
            NodeEditorDescriptor descriptor =
                    new NodeEditorDescriptor(customEditor, title);
            SoaDialogDisplayer.getDefault().notify(descriptor);
            //
            return descriptor.isOkHasPressed();
        }
        //
        return false;
    }

    @Override
    protected void deleteRowImpl(ActionEvent event) {
        int[] rows2delete = getTableView().getSelectedRows();
        //
        for (int index = rows2delete.length - 1; index >= 0; index--) {
            // Remove in back order to prevent index violation
            int rowIndex = rows2delete[index];
            tableModel.deleteRow(rowIndex);
        }
    }
    
    private BpelModel getModel(){
        Lookup lookup = myEditor.getLookup();
        return (BpelModel)lookup.lookup(BpelModel.class);
    }

    @Override
    public boolean applyNewValues() {
        CorrelationsHolder corrHolder = myEditor.getEditedObject();
        CorrelationContainer cc = corrHolder.getCorrelationContainer();
        //
        List<CorrelationLocal> corrLocalList = tableModel.getRowsList();
        if (corrLocalList == null || corrLocalList.size() == 0) {
            if (cc != null) {
                corrHolder.removeCorrelationContainer();
            }
        } else {
            if (cc == null) {
                BPELElementsBuilder elementBuilder = getModel().getBuilder();
                cc = elementBuilder.createCorrelationContainer();
                corrHolder.setCorrelationContainer(cc);
                cc = corrHolder.getCorrelationContainer();
                //
                for (CorrelationLocal corrLocal : corrLocalList) {
                    corrLocal.createObjectInModel(cc);
                }
            } else {
                // Remove all current PatternedCorrelation first
                int corrSize = cc.sizeOfCorrelations();
                for(int index = corrSize - 1; index >= 0; index--) {
                    cc.removeCorrelation(index);
                }
                //
                // Add new ones instead of old
                for (CorrelationLocal corrLocal : corrLocalList) {
                    corrLocal.createObjectInModel(cc);
                }
            }
        }
        //
        return false;
    }
    
    public Validator getValidator() {
        if (myValidator == null) {
            myValidator = new DefaultValidator(myEditor, ErrorMessagesBundle.class) {
                
                public void doFastValidation() {
                }
                
                @Override
                public void doDetailedValidation() {
                    List<CorrelationLocal> corrLocalList = tableModel.getRowsList();
                    if (corrLocalList != null && corrLocalList.size() != 0) {
                        for (CorrelationLocal corrLocal : corrLocalList) {
                            CorrelationSet cSet = corrLocal.getSet();
                            if (cSet == null) {
                                addReasonKey(Severity.ERROR, 
                                        "ERR_INVALID_REF_CORR_TO_SET"); //NOI18N
                                break;
                            }
                        }
                    }
                }
                
            };
        }
        return myValidator;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(this.getClass());
    }
    
}
