/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.bpel.properties.editors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;
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
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.PatternedCorrelation;
import org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer;
import org.netbeans.modules.bpel.properties.choosers.CorrelationSetChooserPanel;
import org.netbeans.modules.bpel.properties.editors.controls.BaseTablePanel;
import org.netbeans.modules.bpel.properties.editors.controls.CustomNodeEditor;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.support.Initiate;
import org.netbeans.modules.bpel.model.api.support.Pattern;
import org.netbeans.modules.bpel.properties.ExtendedLookup;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.editors.controls.SimpleCustomEditor;
import org.openide.nodes.Node;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.editors.controls.filter.VisibilityScope;
import org.netbeans.modules.bpel.properties.editors.controls.valid.NodeChooserDescriptor;
import org.netbeans.modules.bpel.properties.editors.controls.valid.NodeEditorDescriptor;
import org.netbeans.modules.bpel.nodes.CorrelationSetNode;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.openide.util.Lookup;
import org.netbeans.modules.bpel.properties.editors.controls.CustomNodeEditor.EditingMode;
import org.netbeans.modules.bpel.properties.editors.controls.ObjectListTableModel;
import org.netbeans.modules.bpel.properties.editors.controls.TreeNodeChooser;
import org.netbeans.modules.bpel.properties.editors.controls.valid.BpelDialogDisplayer;
import org.netbeans.modules.bpel.properties.editors.controls.valid.DefaultValidator;
import org.netbeans.modules.bpel.properties.editors.controls.valid.ValidationExtension;
import org.netbeans.modules.bpel.properties.editors.controls.valid.Validator;
import org.openide.nodes.Children;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * This panel is a part of the Invoke property editor. It shows the table with
 * a set of related corelations.
 *
 * @author nk160297
 */
public class CorrelationPTablePanel extends BaseTablePanel
        implements Validator.Provider, HelpCtx.Provider {
    
    static final long serialVersionUID = 1L;
    
    private CustomNodeEditor<Invoke> myEditor;
    private DefaultValidator myValidator;
    //
    private TableColumnModel columnModel;
    private MyTableModel tableModel;
    
    public CorrelationPTablePanel(CustomNodeEditor<Invoke> anEditor) {
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
            column.setPreferredWidth(70);
            column.setMaxWidth(70);
            this.addColumn(column);
            //
            column = new TableColumn(2);
            column.setIdentifier(PropertyType.CORRELATION_PATTERN);
            column.setHeaderValue(PropertyType.CORRELATION_PATTERN.getDisplayName());
            final Pattern[] patternsArr = new Pattern[] {
                Pattern.NOT_SPECIFIED, Pattern.REQUEST, Pattern.RESPONSE,
                Pattern.REQUEST_RESPONSE};
            column.setCellRenderer(new TableCellRenderer() {
                JComboBox cb = new JComboBox(patternsArr);
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    if (value != null && value instanceof Pattern) {
                        cb.setSelectedItem(value);
                    } else {
                        cb.setSelectedIndex(0);
                    }
                    return cb;
                }
            });
            column.setCellEditor(new DefaultCellEditor(new JComboBox(patternsArr)));
            column.setPreferredWidth(70);
            column.setMaxWidth(70);
            this.addColumn(column);
        }
    }
    
    class MyTableModel extends ObjectListTableModel<PatternedCorrelationLocal> {
        
        private Invoke invoke;
        
        public MyTableModel(Invoke invoke, TableColumnModel columnModel) {
            super(columnModel);
            assert invoke != null;
            this.invoke = invoke;
            //
            reload();
        }
        
        public void reload() {
            PatternedCorrelationContainer pCorrContainer =
                    invoke.getPatternedCorrelationContainer();
            if (pCorrContainer != null) {
                PatternedCorrelation[] pCorrArr =
                        pCorrContainer.getPatternedCorrelations();
                for (PatternedCorrelation pCorr : pCorrArr) {
                    PatternedCorrelationLocal pCorrLocal =
                            new PatternedCorrelationLocal(pCorr);
                    addRow(pCorrLocal);
                }
            }
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            PatternedCorrelationLocal pCorrLocal = getRowObject(rowIndex);
            TableColumn column = columnModel.getColumn(columnIndex);
            //
            PropertyType propertyType = (PropertyType)column.getIdentifier();
            switch (propertyType) {
                case CORRELATION_SET:
                    return pCorrLocal.getSet();
                case CORRELATION_INITIATE:
                    return pCorrLocal.getInitiate();
                case CORRELATION_PATTERN:
                    return pCorrLocal.getPattern();
                default:
                    assert false : "The property: \"" + propertyType +
                            "\" isn't supported by the getValueAt()"; // NOI18N
            }
            return null;
        }
        
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            PatternedCorrelationLocal pCorrLocal = getRowObject(rowIndex);
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
                case CORRELATION_PATTERN:
                    assert aValue == null || aValue instanceof Pattern;
                    pCorrLocal.setPattern((Pattern)aValue);
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
    class PatternedCorrelationLocal {
        private Initiate myInitiate;
        private Pattern myPattern;
        private CorrelationSet mySet;
        
        public PatternedCorrelationLocal(
                CorrelationSet set, Initiate initiate, Pattern pattern) {
            mySet = set;
            myInitiate = initiate;
            myPattern = pattern;
        }
        
        public PatternedCorrelationLocal(PatternedCorrelation modelEntity) {
            assert modelEntity != null;
            BpelReference<CorrelationSet> cSetRef = modelEntity.getSet();
            if (cSetRef != null) {
                mySet = cSetRef.get();
            }
            myInitiate = modelEntity.getInitiate();
            myPattern = modelEntity.getPattern();
        }
        
        public Initiate getInitiate() {
            return myInitiate;
        }
        
        public Pattern getPattern() {
            return myPattern;
        }
        
        public CorrelationSet getSet() {
            return mySet;
        }
        
        public void removeInitiate() {
            myInitiate = null;
        }
        
        public void removePattern() {
            myPattern = null;
        }
        
        public void setInitiate(Initiate value) {
            myInitiate = value;
        }
        
        public void setPattern(Pattern value) {
            myPattern = value;
        }
        
        public void setSet(CorrelationSet value) {
            mySet = value;
        }
        
        /**
         * Creats the new PartternedCorrelaton object and adds it to
         * model to the specified PatternedCorrelationContainer.
         *
         * The pcc parameter has to be already in the model,
         * otherwise the BpelReference<CorrelationSet> will be incorrect.
         */
        public PatternedCorrelation createObjectInModel(
                PatternedCorrelationContainer pcc) {
            PatternedCorrelation result = pcc.getBpelModel().getBuilder().
                    createPatternedCorrelation();
            pcc.addPatternedCorrelation(result);
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
            if (myPattern == null || myPattern == Pattern.NOT_SPECIFIED) {
                result.removePattern();
            } else {
                result.setPattern(myPattern);
            }
            //
            return result;
        }
    }
    
    public void createContent() {
        super.createContent();
        //
        columnModel = new MyColumnModel();
        //
        Invoke invoke = myEditor.getModelNode().getReference();
        //
        tableModel = new MyTableModel(invoke, columnModel);
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
        this.add(scrollPane, BorderLayout.CENTER);
        //
        setTableView(tableView);
    }
    
    protected void addRow(ActionEvent event) {
        CorrelationSet corrSet = chooseCorrelationSet(tableModel.getRowsList());
        if (corrSet != null) {
            PatternedCorrelationLocal newPCorrLocal =
                    new PatternedCorrelationLocal(
                    corrSet, Initiate.NO, Pattern.NOT_SPECIFIED);
            //
            tableModel.addRow(newPCorrLocal);
        }
    }
    
    private CorrelationSet chooseCorrelationSet(
            final List<PatternedCorrelationLocal> currCorrLocalList) {
        Lookup lookup = myEditor.getLookup();
        BpelEntity entry = (BpelEntity)myEditor.getModelNode().getReference();
        //
        final CorrelationSetChooserPanel csChooser = new CorrelationSetChooserPanel();
        //
        // Construct a validation extension which is intended to prevent
        // duplicate Properties in the CorrelationSet.
        ValidationExtension validationExt = new ValidationExtension() {
            public Validator getExtensionValidator() {
                Validator validator = new DefaultValidator(csChooser) {
                    public boolean doFastValidation() {
                        return true;
                    }
                    
                    public boolean doDetailedValidation() {
                        CorrelationSet newCs = csChooser.getSelectedCorrelationSet();
                        //
                        for (PatternedCorrelationLocal corr : currCorrLocalList) {
                            CorrelationSet cs = corr.getSet();
                            if (newCs.equals(cs)) {
                                addReasonKey("ERR_NOT_UNIQUE_CORR_SET"); // NOI18N
                                return false;
                            }
                        }
                        //
                        return true;
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
        NodeChooserDescriptor descriptor =
                new NodeChooserDescriptor(chooser, title);
        BpelDialogDisplayer.getDefault().notify(descriptor);
        if (descriptor.isOkHasPressed()) {
            CorrelationSet corrSet = csChooser.getSelectedCorrelationSet();
            return corrSet;
        }
        return null;
//        csChooser.afterClose();
    }
    
    protected void editRow(ActionEvent event) {
        int rowIndex = getTableView().getSelectedRow();
        if (rowIndex == -1) return; // Nothing to edit because of nothing is selected
        //
        PatternedCorrelationLocal pCorrLocal = tableModel.getRowObject(rowIndex);
        if (pCorrLocal.getSet() == null) {
            CorrelationSet corrSet = chooseCorrelationSet(tableModel.getRowsList());
            if (corrSet != null) {
                pCorrLocal.setSet(corrSet);
            }
        } else {
            CorrelationSet corrSet = pCorrLocal.getSet();
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
            BpelDialogDisplayer.getDefault().notify(descriptor);
            //
            return descriptor.isOkHasPressed();
        }
        //
        return false;
    }
    
    protected void deleteRowImpl(ActionEvent event) {
        int[] rows2delete = getTableView().getSelectedRows();
        //
        for (int index = rows2delete.length - 1; index >= 0; index--) {
            // Remove in back order to prevent index violation
            int rowIndex = rows2delete[index];
            tableModel.deleteRow(rowIndex);
        }
    }
    
    private BpelModel getModel() {
        Lookup lookup = myEditor.getLookup();
        return (BpelModel)lookup.lookup(BpelModel.class);
    }
    
    public boolean applyNewValues() {
        Invoke invoke = myEditor.getModelNode().getReference();
        PatternedCorrelationContainer pcc = invoke.getPatternedCorrelationContainer();
        //
        List<PatternedCorrelationLocal> pCorrLocalList = tableModel.getRowsList();
        if (pCorrLocalList == null || pCorrLocalList.size() == 0) {
            if (pcc != null) {
                invoke.removePatternedCorrelationContainer();
            }
        } else {
            if (pcc == null) {
                BPELElementsBuilder elementBuilder = getModel().getBuilder();
                pcc = elementBuilder.createPatternedCorrelationContainer();
                invoke.setPatternedCorrelationContainer(pcc);
                pcc = invoke.getPatternedCorrelationContainer();
                //
                for (PatternedCorrelationLocal pCorrLocal : pCorrLocalList) {
                    pCorrLocal.createObjectInModel(pcc);
                }
            } else {
                // Remove all current PatternedCorrelation first
                int pCorrSize = pcc.sizeOfPatternedCorrelation();
                for(int index = pCorrSize - 1; index >= 0; index--) {
                    pcc.removePatternedCorrelation(index);
                }
                //
                // Add new ones instead of old
                for (PatternedCorrelationLocal pCorrLocal : pCorrLocalList) {
                    pCorrLocal.createObjectInModel(pcc);
                }
            }
        }
        //
        return false;
    }
    
    public Validator getValidator() {
        if (myValidator == null) {
            myValidator = new DefaultValidator(myEditor) {
                
                public boolean doFastValidation() {
                    return true;
                }
                
                public boolean doDetailedValidation() {
                    boolean isValid = true;
                    //
                    List<PatternedCorrelationLocal> pCorrLocalList = tableModel.getRowsList();
                    if (pCorrLocalList != null && pCorrLocalList.size() != 0) {
                        for (PatternedCorrelationLocal pCorrLocal : pCorrLocalList) {
                            CorrelationSet cSet = pCorrLocal.getSet();
                            if (cSet == null) {
                                addReasonKey("ERR_INVALID_REF_CORR_TO_SET"); //NOI18N
                                isValid = false;
                                break;
                            }
                        }
                    }
                    //
                    return isValid;
                }
                
            };
        }
        return myValidator;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(this.getClass());
    }
    
}
