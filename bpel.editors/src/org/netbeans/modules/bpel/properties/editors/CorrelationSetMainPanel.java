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

import org.netbeans.modules.bpel.model.api.BaseScope;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import static org.netbeans.modules.bpel.properties.PropertyType.NAME;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.CorrelationSetContainer;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor.EditingMode;
import org.netbeans.modules.soa.ui.form.EditorLifeCycleAdapter;
import org.netbeans.modules.bpel.model.api.support.VisibilityScope;
import org.netbeans.modules.soa.ui.form.valid.DefaultValidator;
import org.netbeans.modules.bpel.editors.api.ui.valid.ErrorMessagesBundle;
import org.netbeans.modules.soa.ui.form.valid.Validator;
import org.openide.util.NbBundle;

/**
 * This panel is the main part of the Correlation Set Custom Editor.
 * It contains the properties of the Correlation Set and the table with
 * Correlation Properties which are associated with the Correlation Set.
 *
 * @author  nk160297
 */
public class CorrelationSetMainPanel extends EditorLifeCycleAdapter
        implements Validator.Provider {
    
    static final long serialVersionUID = 1L;
    
    private CustomNodeEditor<CorrelationSet> myEditor;
    private DefaultValidator myValidator;
    
    /** Creates new form EdirVariablePanel */
    public CorrelationSetMainPanel(CustomNodeEditor<CorrelationSet> anEditor) {
        myEditor = anEditor;
        createContent();
    }
    
    private void bindControls2PropertyNames() {
        fldCorrelationSetName.putClientProperty(
                CustomNodeEditor.PROPERTY_BINDER, NAME);
    }
    
    @Override
    public void createContent() {
        initComponents();
        bindControls2PropertyNames();
    }
    
    @Override
    public boolean applyNewValues() {
        // Return false because of the panel has child CSetPropertyTablePanel
        return false;
    }
    
    @Override
    public boolean initControls() {
        // Return false because of the panel has child CSetPropertyTablePanel
        return false;
    }
    
    @Override
    public boolean subscribeListeners() {
        // Return false because of the panel has child CSetPropertyTablePanel
        return false;
    }
    
    @Override
    public boolean unsubscribeListeners() {
        // Return false because of the panel has child CSetPropertyTablePanel
        return false;
    }

    @Override
    public boolean afterClose() {
        // Return false because of the panel has child CSetPropertyTablePanel
        return false;
    }
    
    public DefaultValidator getValidator() {
        if (myValidator == null) {
            myValidator = new DefaultValidator(myEditor, ErrorMessagesBundle.class) {
                
                public void doFastValidation() {
                    String csName = fldCorrelationSetName.getText();
                    if (csName == null || csName.length() == 0) {
                        addReasonKey(Severity.ERROR, "ERR_NAME_EMPTY"); //NOI18N
                    }
                }
                
                @Override
                public void doDetailedValidation() {
                    super.doDetailedValidation();
                    //
                    // Check that the variable name is unique
                    CorrelationSetContainer csc = null;
                    if (myEditor.getEditingMode() ==
                            EditingMode.CREATE_NEW_INSTANCE) {
                        VisibilityScope visScope = (VisibilityScope)myEditor.
                                getLookup().lookup(VisibilityScope.class);
                        if (visScope != null) {
                            BaseScope scope = visScope.getClosestScope();
                            csc = scope.getCorrelationSetContainer();
                        } else {
                            // If the visibility scope isn't specified 
                            // then consider the process as a owner of 
                            // the correlation set container
                            BpelModel model = (BpelModel)myEditor.getLookup().
                                    lookup(BpelModel.class);
                            if (model != null) {
                                Process process = model.getProcess();
                                if (process != null) {
                                    csc = process.getCorrelationSetContainer();
                                }
                            }
                        }
//                    } else {
// A VetoException will be thorown if the name isn't unique.
//                    }
                        //
                        if (csc != null) {
                            String csName = fldCorrelationSetName.getText();
                            CorrelationSet[] corrSetArr = csc.getCorrelationSets();
                            for (CorrelationSet corrSet : corrSetArr) {
                                if (csName.equals(corrSet.getName())){
                                    addReasonKey(Severity.ERROR, 
                                            "ERR_NOT_UNIQUE_CORRELATION_SET_NAME"); //NOI18N
                                }
                            }
                        }
                    }
                }
                
            };
        }
        return myValidator;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        lblCorrelationSetName = new javax.swing.JLabel();
        fldCorrelationSetName = new javax.swing.JTextField();
        pnlPropertiesTable = new CSetPropertyTablePanel(myEditor);

        setPreferredSize(new java.awt.Dimension(400, 200));
        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSN_DLG_AddCorrelationSet"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_DLG_AddCorrelationSet"));
        lblCorrelationSetName.setLabelFor(fldCorrelationSetName);
        lblCorrelationSetName.setText(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_CorrelationSetName"));
        lblCorrelationSetName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FormBundle.class, "LBL_CorrelationSetName"));
        lblCorrelationSetName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FormBundle.class,"ACSD_CorrelationSetName"));

        pnlPropertiesTable.setBorder(javax.swing.BorderFactory.createTitledBorder(NbBundle.getMessage(FormBundle.class, "LBL_CorrelationSetBorderTitle")));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, pnlPropertiesTable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(lblCorrelationSetName)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(fldCorrelationSetName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblCorrelationSetName)
                    .add(fldCorrelationSetName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pnlPropertiesTable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField fldCorrelationSetName;
    private javax.swing.JLabel lblCorrelationSetName;
    private javax.swing.JPanel pnlPropertiesTable;
    // End of variables declaration//GEN-END:variables
}
