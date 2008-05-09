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
import java.util.ArrayList;
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
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.PatternedCorrelation;
import org.netbeans.modules.bpel.model.api.PatternedCorrelationContainer;
import org.netbeans.modules.bpel.properties.choosers.CorrelationSetChooserPanel;
import org.netbeans.modules.bpel.properties.editors.controls.BaseTablePanel;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.support.Initiate;
import org.netbeans.modules.bpel.model.api.support.Pattern;
import org.netbeans.modules.soa.ui.ExtendedLookup;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.editors.controls.SimpleCustomEditor;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.openide.nodes.Node;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.model.api.support.VisibilityScope;
import org.netbeans.modules.bpel.editors.api.ui.valid.NodeEditorDescriptor;
import org.netbeans.modules.bpel.nodes.CorrelationSetNode;
import org.netbeans.modules.bpel.properties.PropertyType;
import org.openide.util.Lookup;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor.EditingMode;
import org.netbeans.modules.bpel.properties.editors.controls.MessageConfigurationController;
import org.netbeans.modules.bpel.properties.editors.controls.ObjectListTableModel;
import org.netbeans.modules.bpel.properties.editors.controls.TreeNodeChooser;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.bpel.editors.api.ui.valid.ErrorMessagesBundle;
import org.netbeans.modules.soa.ui.form.valid.DefaultDialogDescriptor;
import org.netbeans.modules.soa.ui.form.valid.ValidStateManager;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
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
    private MessageConfigurationController myMcc;
    private DefaultValidator myValidator;
    //
    private TableColumnModel columnModel;
    private MyTableModel tableModel;
    
    public CorrelationPTablePanel(CustomNodeEditor<Invoke> anEditor,
            MessageConfigurationController mcc) {
        super();
        this.myEditor = anEditor;
        this.myMcc = mcc;
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
            final Initiate[] initiateArr = new Initiate[] {null,
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
            column.setPreferredWidth(40);
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
        
        @Override
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
    
    @Override
    public void createContent() {
        super.createContent();
        //
        columnModel = new MyColumnModel();
        //
        Invoke invoke = myEditor.getEditedObject();
        //
        tableModel = new MyTableModel(invoke, columnModel);
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
        //
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(
                FormBundle.class, "ACSN_LBL_Correlations_Tab")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(
                FormBundle.class, "ACSD_LBL_Correlations_Tab")); // NOI18N
        //
        setTableView(tableView);
    }
    
    @Override
    protected void addRow(ActionEvent event) {
        Set<CorrelationSet> csSet = chooseCorrelationSet();
        if (csSet != null) {
            for (CorrelationSet cs : csSet) {
            PatternedCorrelationLocal newPCorrLocal =
                    new PatternedCorrelationLocal(
                    cs, Initiate.NO, Pattern.NOT_SPECIFIED);
                //
                tableModel.addRow(newPCorrLocal);
            }
        }
    }
    
    private Set<CorrelationSet> chooseCorrelationSet() {
        Lookup lookup = myEditor.getLookup();
        BpelEntity entry = (BpelEntity)myEditor.getEditedObject();
        //
        final CorrelationSetChooserPanel csChooser = new CorrelationSetChooserPanel();
        //
        VisibilityScope visScope = new VisibilityScope(entry, lookup);
        Lookup contextLookup = new ExtendedLookup(lookup, visScope);
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
            Set<CorrelationSet> csSet = csChooser.getSelectedValue();
            return csSet;
        }
        return null;
        //        csChooser.afterClose();
    }
    
    @Override
    protected void editRow(ActionEvent event) {
        int rowIndex = getTableView().getSelectedRow();
        if (rowIndex == -1) return; // Nothing to edit because of nothing is selected
        //
        PatternedCorrelationLocal pCorrLocal = tableModel.getRowObject(rowIndex);
        if (pCorrLocal.getSet() == null) {
            Set<CorrelationSet> csSet = chooseCorrelationSet();
            if (csSet != null && !csSet.isEmpty()) {
                CorrelationSet cs = csSet.iterator().next();
                pCorrLocal.setSet(cs);
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
    
    private BpelModel getModel() {
        Lookup lookup = myEditor.getLookup();
        return (BpelModel)lookup.lookup(BpelModel.class);
    }
    
    @Override
    public boolean applyNewValues() {
        Invoke invoke = myEditor.getEditedObject();
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
    
    /**
     * Returns a flag which indicates if the WSDL operation is one-way or
     * request-response or null if operation isn't specified yet or
     * it is not accessible.
     * <P>
     * WSDL operation can have another forms: Solicit-response and Notification.
     * But they can't be applied for Invoke because they imply that
     * the partner calls the BPEL process, but invoke implies that BPEL process
     * calls the partner.
     */
    public Boolean isOneWayOperation() {
        Operation operation = myMcc.getCurrentOperation();
        if (operation != null) {
            if (operation instanceof OneWayOperation) {
                return Boolean.TRUE;
            }
            if (operation instanceof RequestResponseOperation) {
                return Boolean.FALSE;
            }
        }
        //
        return null;
    }
    
    public Validator getValidator() {
        if (myValidator == null) {
            myValidator = new MyValidator(myEditor);
        }
        return myValidator;
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(this.getClass());
    }
    
    private class MyValidator extends DefaultValidator {
        
        public MyValidator(ValidStateManager.Provider vsmProvider) {
            super(vsmProvider, ErrorMessagesBundle.class);
        }
        
        public void doFastValidation() {
        }
        
        @Override
        public void doDetailedValidation() {
            List<PatternedCorrelationLocal> pCorrLocalList = tableModel.getRowsList();
            if (pCorrLocalList != null && pCorrLocalList.size() != 0) {
                for (PatternedCorrelationLocal pCorrLocal : pCorrLocalList) {
                    CorrelationSet cSet = pCorrLocal.getSet();
                    if (cSet == null) {
                        addReasonKey(Severity.ERROR,
                                "ERR_INVALID_REF_CORR_TO_SET"); //NOI18N
                        break;
                    }
                }
            }
            //
            Boolean isOneWayOperation = isOneWayOperation();
            // Skip check of correlation sets in case of operation's type
            // isn't accessible.
            if (isOneWayOperation != null) {
                //
                // Check ambiguous usage of a Correlation Set
                checkDuplicates(isOneWayOperation);
                //
                // Check that the patters are specified or not for
                // all correlation sets depend on type of operation
                checkPatterns(isOneWayOperation);
            }
        }
        
        /**
         * Check that the patters are specified or not for
         * all correlation sets depend on type of operation.
         */
        private void checkPatterns(boolean isOneWayOperation) {
            List<PatternedCorrelationLocal> csList = tableModel.getRowsList();
            ArrayList<PatternedCorrelationLocal> csArrayList =
                    new ArrayList<PatternedCorrelationLocal>(csList);
            //
            for (int index1 = 0; index1 < csArrayList.size(); index1++) {
                PatternedCorrelationLocal cs = csArrayList.get(index1);
                Pattern pattern = cs.getPattern();
                if (isOneWayOperation &&
                        !(pattern == null || pattern == Pattern.NOT_SPECIFIED)) {
                    addReasonKey(Severity.ERROR,
                            "ERR_CORR_SET_PATTERN_DISALLOWED"); // NOI18N
                    break;
                }
                if (!isOneWayOperation &&
                        (pattern == null || pattern == Pattern.NOT_SPECIFIED)) {
                    addReasonKey(Severity.ERROR,
                            "ERR_CORR_SET_PATTERN_REQUIRED"); // NOI18N
                    break;
                }
            }
        }
        
        /**
         * Checks if the correlation sets' table has duplicate items.
         * <p>
         * If operation is one-way, then only the name of Correlation set
         * is taken into consideration.
         * <p>
         * If operation is request-response, then not only the name of
         * Correlation set is taken into consideration but pattern value as well.
         * The initialize value is ignored.
         */
        private void checkDuplicates(boolean isOneWayOperation) {
            List<PatternedCorrelationLocal> csList = tableModel.getRowsList();
            ArrayList<PatternedCorrelationLocal> csArrayList =
                    new ArrayList<PatternedCorrelationLocal>(csList);
            //
            for (int m = 0; m < csArrayList.size(); m++) {
                PatternedCorrelationLocal csLocal1 = csArrayList.get(m);
                CorrelationSet cs1 = csLocal1.getSet();
                if (cs1 == null) {
                    continue;
                }
                //
                for (int k = m + 1; k < csArrayList.size(); k++) {
                    PatternedCorrelationLocal csLocal2 = csArrayList.get(k);
                    CorrelationSet cs2 = csLocal2.getSet();
                    //
                    if (cs2 == null) {
                        continue;
                    }
                    //
                    if (cs1.equals(cs2)) {
                        if (isOneWayOperation) {
                            continue;
                        }
                        //
                        Pattern pattern1 = csLocal1.getPattern();
                        Pattern pattern2 = csLocal2.getPattern();
                        //
                        if ((pattern1 == Pattern.REQUEST &&
                                pattern2 == Pattern.RESPONSE) ||
                                (pattern1 == Pattern.RESPONSE &&
                                pattern2 == Pattern.REQUEST)) {
                            // Ok
                            continue;
                        } else {
                            // Another combination of patterns is considered as duplication
                            String csName = cs1.getName();
                            addReasonKey(Severity.ERROR, 
                                    "ERR_NOT_UNIQUE_PATTERNED_CORR_SET",
                                    csName); // NOI18N
                            //
                            return;
                        }
                    }
                }
            }
            return;
        }
    }
}
